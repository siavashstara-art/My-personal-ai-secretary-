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

    private fun requestJson(urlString: String, personalToken: String): String {
        val url = URL(urlString)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "PersonalAICreativeStudio-Android")
            if (personalToken.isNotBlank()) {
                setRequestProperty("Authorization", "Bearer ${personalToken.trim()}")
            }
        }
        return try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.let {
                BufferedReader(InputStreamReader(it)).use { reader -> reader.readText() }
            }.orEmpty()
            if (code !in 200..299) {
                throw IllegalStateException("GitHub API returned HTTP $code: ${body.take(160)}")
            }
            body
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
        )
    )
}
