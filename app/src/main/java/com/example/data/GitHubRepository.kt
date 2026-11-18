package com.example.data

import android.content.Context
import android.content.pm.PackageManager
import com.example.model.CodingAppInfo
import com.example.model.GitHubEventDto
import com.example.model.GitHubUserDto
import com.example.model.GitHubRepoDto
import com.example.model.TermuxCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

import android.util.Base64
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.URLEncoder

data class GitHubSnapshot(
    val user: GitHubUserDto,
    val repos: List<GitHubRepoDto>,
    val events: List<GitHubEventDto>
)

class GitHubRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun fetchUserActivity(username: String, personalToken: String = ""): GitHubSnapshot =
        withContext(Dispatchers.IO) {
            val cleanUser = username.trim()
            require(cleanUser.isNotEmpty()) { "GitHub username cannot be empty." }

            val userJson = requestJson("https://api.github.com/users/$cleanUser", personalToken)
            val reposJson = requestJson(
                "https://api.github.com/users/$cleanUser/repos?sort=updated&per_page=10",
                personalToken
            )
            val eventsEndpoint = if (personalToken.isNotBlank()) {
                "https://api.github.com/users/$cleanUser/events?per_page=15"
            } else {
                "https://api.github.com/users/$cleanUser/events/public?per_page=15"
            }
            val eventsJson = requestJson(eventsEndpoint, personalToken)

            GitHubSnapshot(
                user = json.decodeFromString<GitHubUserDto>(userJson),
                repos = json.decodeFromString<List<GitHubRepoDto>>(reposJson),
                events = json.decodeFromString<List<GitHubEventDto>>(eventsJson)
            )
        }

    suspend fun ensureRepositoryExists(
        owner: String,
        repoName: String,
        personalToken: String,
        isPrivate: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        val cleanToken = personalToken.trim()
        require(cleanToken.isNotEmpty()) { "برای ایجاد مخزن یا پوش در گیت‌هاب، Personal Access Token لازم است." }
        val cleanOwner = owner.trim()
        val cleanRepo = repoName.trim()
        require(cleanOwner.isNotEmpty() && cleanRepo.isNotEmpty()) { "نام کاربر و نام مخزن نمی‌تواند خالی باشد." }

        val checkUrl = "https://api.github.com/repos/$cleanOwner/$cleanRepo"
        val checkResult = sendHttp("GET", checkUrl, cleanToken, null)
        if (checkResult.first in 200..299) {
            return@withContext JSONObject(checkResult.second).optString("html_url", "https://github.com/$cleanOwner/$cleanRepo")
        }

        val createBody = JSONObject().apply {
            put("name", cleanRepo)
            put("description", "Pushed & completed via Personal AI Studio Android Companion")
            put("private", isPrivate)
            put("auto_init", true)
        }.toString()
        val createResult = sendHttp("POST", "https://api.github.com/user/repos", cleanToken, createBody)
        if (createResult.first !in 200..299) {
            throw IllegalStateException("خطا در ساخت مخزن گیت‌هاب (HTTP ${createResult.first}): ${createResult.second.take(180)}")
        }
        JSONObject(createResult.second).optString("html_url", "https://github.com/$cleanOwner/$cleanRepo")
    }

    suspend fun pushFilesToRepository(
        owner: String,
        repoName: String,
        branch: String,
        personalToken: String,
        files: List<Pair<String, ByteArray>>,
        commitMessage: String,
        onFileProgress: (Int, Int, String) -> Unit
    ): Int = withContext(Dispatchers.IO) {
        val cleanToken = personalToken.trim()
        val cleanOwner = owner.trim()
        val cleanRepo = repoName.trim()
        val cleanBranch = branch.trim().ifBlank { "main" }
        require(cleanToken.isNotEmpty()) { "Personal Access Token وارد نشده است." }

        var committed = 0
        val total = files.size
        files.forEachIndexed { index, (relPath, bytes) ->
            onFileProgress(index + 1, total, relPath)
            val encodedPath = relPath.split('/').joinToString("/") { segment ->
                URLEncoder.encode(segment, "UTF-8").replace("+", "%20")
            }
            val apiUrl = "https://api.github.com/repos/$cleanOwner/$cleanRepo/contents/$encodedPath?ref=$cleanBranch"
            val existing = sendHttp("GET", apiUrl, cleanToken, null)
            val existingSha = if (existing.first == 200) {
                runCatching { JSONObject(existing.second).optString("sha").takeIf { it.isNotBlank() } }.getOrNull()
            } else {
                null
            }

            val base64Content = Base64.encodeToString(bytes, Base64.NO_WRAP)
            val payload = JSONObject().apply {
                put("message", "$commitMessage ($relPath)")
                put("content", base64Content)
                put("branch", cleanBranch)
                if (existingSha != null) {
                    put("sha", existingSha)
                }
            }.toString()

            val putUrl = "https://api.github.com/repos/$cleanOwner/$cleanRepo/contents/$encodedPath"
            val putRes = sendHttp("PUT", putUrl, cleanToken, payload)
            if (putRes.first !in 200..299) {
                throw IllegalStateException("خطا در آپلود $relPath (HTTP ${putRes.first}): ${putRes.second.take(160)}")
            }
            committed++
        }
        committed
    }

    private fun requestJson(urlString: String, personalToken: String): String {
        val (code, body) = sendHttp("GET", urlString, personalToken, null)
        if (code !in 200..299) {
            throw IllegalStateException("GitHub API returned HTTP $code: ${body.take(160)}")
        }
        return body
    }

    private fun sendHttp(
        method: String,
        urlString: String,
        personalToken: String,
        jsonBody: String?
    ): Pair<Int, String> {
        val url = URL(urlString)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 18_000
            readTimeout = 20_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            setRequestProperty("User-Agent", "PersonalAICreativeStudio-Android")
            if (personalToken.isNotBlank()) {
                setRequestProperty("Authorization", "Bearer ${personalToken.trim()}")
            }
            if (jsonBody != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }
        return try {
            if (jsonBody != null) {
                OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(jsonBody)
                }
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.let {
                BufferedReader(InputStreamReader(it)).use { reader -> reader.readText() }
            }.orEmpty()
            code to body
        } finally {
            connection.disconnect()
        }
    }
}

object CodingAppsCatalog {

    private data class CodingAppSpec(
        val displayName: String,
        val packageName: String,
        val category: String,
        val fallbackUrl: String
    )

    private val specs = listOf(
        CodingAppSpec("Termux", "com.termux", "Terminal & Git CLI", "https://termux.dev"),
        CodingAppSpec("Termux:API", "com.termux.api", "Android Hardware Bridge", "https://wiki.termux.com/wiki/Termux:API"),
        CodingAppSpec("GitHub", "com.github.android", "Git & Pull Requests", "https://github.com"),
        CodingAppSpec("Acode Editor", "com.foxdebug.acode", "Mobile Code IDE", "https://acode.app"),
        CodingAppSpec("Acode Free", "com.foxdebug.acodefree", "Mobile Code IDE", "https://acode.app"),
        CodingAppSpec("Spck Editor", "io.spck", "Web & Git Editor", "https://spck.io"),
        CodingAppSpec("Dcoder", "com.paprbit.dcoder", "Mobile Compiler IDE", "https://dcoder.tech")
    )

    fun detectInstalledApps(context: Context): List<CodingAppInfo> {
        val pm = context.packageManager
        return specs.map { spec ->
            val installed = try {
                pm.getPackageInfo(spec.packageName, PackageManager.PackageInfoFlags.of(0))
                true
            } catch (_: Exception) {
                false
            }
            CodingAppInfo(
                displayName = spec.displayName,
                packageName = spec.packageName,
                category = spec.category,
                fallbackUrl = spec.fallbackUrl,
                isInstalled = installed
            )
        }
    }

    val defaultTermuxCommands = listOf(
        TermuxCommand(
            title = "Update Termux & Install Git + GitHub CLI",
            description = "Prepares Termux with git, gh CLI, and OpenSSH",
            command = "pkg update && pkg upgrade -y && pkg install -y git gh openssh"
        ),
        TermuxCommand(
            title = "GitHub CLI Login & Status",
            description = "Authenticate your GitHub account inside Termux",
            command = "gh auth login && gh status"
        ),
        TermuxCommand(
            title = "Clone Studio Repository",
            description = "Clones your imported GitHub workspace into Termux",
            command = "git clone https://github.com/siavashstara-art/My-personal-ai-application-.git"
        ),
        TermuxCommand(
            title = "Quick Commit & Push",
            description = "Stage, commit, and push local changes from Termux",
            command = "git status && git add . && git commit -m \"Update from mobile\" && git push"
        ),
        TermuxCommand(
            title = "Build Signed Release APK & AAB",
            description = "Restores keystore if needed and builds both app-release.apk and app-release.aab",
            command = "[ -f debug.keystore ] || base64 -d debug.keystore.base64 > debug.keystore && chmod +x ./gradlew && ./gradlew assembleRelease bundleRelease"
        )
    )
}
