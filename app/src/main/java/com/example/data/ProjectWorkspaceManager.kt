package com.example.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.model.DeployTemplateType
import com.example.model.ImportedProjectWorkspace
import com.example.model.WorkspaceFileEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ProjectWorkspaceManager(private val context: Context) {

    private val workspacesRoot: File
        get() = File(context.filesDir, "imported_projects").apply { mkdirs() }

    private val textExtensions = setOf(
        "kt", "kts", "java", "xml", "json", "md", "txt", "gradle", "properties",
        "pro", "ts", "tsx", "js", "jsx", "css", "scss", "html", "htm", "yml",
        "yaml", "rules", "env", "example", "gitignore", "sh", "py", "sql"
    )

    private val ignoredSegments = setOf(
        ".git", "build", ".gradle", "node_modules", "dist", ".idea", "__MACOSX"
    )

    suspend fun extractZipFromUri(uri: Uri): ImportedProjectWorkspace = withContext(Dispatchers.IO) {
        val rawName = resolveDisplayName(uri)
        val cleanBaseName = rawName
            .removeSuffix(".zip")
            .removeSuffix(".ZIP")
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .trim('_')
            .ifBlank { "aistudio_project_${System.currentTimeMillis()}" }

        val projectId = "${cleanBaseName}_${System.currentTimeMillis() % 10_000}"
        val targetDir = File(workspacesRoot, projectId).apply { mkdirs() }
        val canonicalRoot = targetDir.canonicalFile

        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("امکان باز کردن فایل زیپ انتخاب‌شده وجود ندارد.")

        var extractedCount = 0
        inputStream.use { raw ->
            ZipInputStream(BufferedInputStream(raw)).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    val entryName = entry.name.replace("\\", "/").trimStart('/')
                    if (entryName.isNotBlank() && !shouldIgnorePath(entryName)) {
                        val outFile = File(canonicalRoot, entryName).canonicalFile
                        val rootPrefix = canonicalRoot.path + File.separator
                        if (outFile.path != canonicalRoot.path && !outFile.path.startsWith(rootPrefix)) {
                            throw SecurityException("مسیر ناامن در فایل زیپ شناسایی و مسدود شد: $entryName")
                        }
                        if (entry.isDirectory) {
                            outFile.mkdirs()
                        } else {
                            outFile.parentFile?.mkdirs()
                            FileOutputStream(outFile).use { fos ->
                                zis.copyTo(fos)
                            }
                            extractedCount++
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        }

        if (extractedCount == 0) {
            targetDir.deleteRecursively()
            throw IllegalStateException("فایل زیپ خالی است یا فایل قابل استخراجی در آن پیدا نشد.")
        }

        // Flatten single top-level wrapper folder if AI Studio/GitHub zipped a root folder
        normalizeSingleWrapperFolder(targetDir)

        loadWorkspace(projectId)
            ?: throw IllegalStateException("خطا در بارگذاری فضای کاری پروژه استخراج‌شده.")
    }

    private fun normalizeSingleWrapperFolder(projectDir: File) {
        val children = projectDir.listFiles()?.toList().orEmpty()
        if (children.size == 1 && children.first().isDirectory) {
            val wrapper = children.first()
            wrapper.listFiles()?.forEach { inner ->
                val dest = File(projectDir, inner.name)
                if (!dest.exists()) {
                    inner.renameTo(dest)
                }
            }
            if (wrapper.listFiles()?.isEmpty() == true) {
                wrapper.delete()
            }
        }
    }

    suspend fun listSavedWorkspaces(): List<ImportedProjectWorkspace> = withContext(Dispatchers.IO) {
        val dirs = workspacesRoot.listFiles()?.filter { it.isDirectory }
            ?.sortedByDescending { it.lastModified() }
            .orEmpty()
        dirs.mapNotNull { dir ->
            loadWorkspace(dir.name)
        }
    }

    suspend fun loadWorkspace(projectId: String): ImportedProjectWorkspace? = withContext(Dispatchers.IO) {
        val dir = File(workspacesRoot, projectId)
        if (!dir.exists() || !dir.isDirectory) return@withContext null
        val files = collectWorkspaceFiles(dir)
        ImportedProjectWorkspace(
            projectId = projectId,
            displayName = projectId.substringBeforeLast('_').ifBlank { projectId },
            rootPath = dir.absolutePath,
            fileCount = files.size,
            files = files
        )
    }

    suspend fun readTextFile(projectId: String, relativePath: String): String = withContext(Dispatchers.IO) {
        val file = resolveSafeProjectFile(projectId, relativePath)
        if (!file.exists() || !file.isFile) {
            throw IllegalStateException("فایل $relativePath پیدا نشد.")
        }
        val bytes = file.readBytes()
        if (bytes.size > 350_000) {
            bytes.copyOf(350_000).toString(Charsets.UTF_8) + "\n\n// [فایل بزرگ بود؛ ۳۵۰ کیلوبایت اول نمایش داده شد]"
        } else {
            bytes.toString(Charsets.UTF_8)
        }
    }

    suspend fun writeTextFile(projectId: String, relativePath: String, content: String): ImportedProjectWorkspace =
        withContext(Dispatchers.IO) {
            val cleanRel = relativePath.replace("\\", "/").trimStart('/')
            require(cleanRel.isNotBlank()) { "مسیر فایل نمی‌تواند خالی باشد." }
            val file = resolveSafeProjectFile(projectId, cleanRel)
            file.parentFile?.mkdirs()
            file.writeText(content, Charsets.UTF_8)
            loadWorkspace(projectId)
                ?: throw IllegalStateException("به‌روزرسانی فضای کاری ناموفق بود.")
        }

    suspend fun deleteWorkspace(projectId: String) = withContext(Dispatchers.IO) {
        val dir = File(workspacesRoot, projectId)
        if (dir.exists()) {
            dir.deleteRecursively()
        }
    }

    suspend fun collectFilesForGitHubPush(
        projectId: String,
        maxFiles: Int = 35,
        maxFileSizeBytes: Long = 220_000L
    ): List<Pair<String, ByteArray>> = withContext(Dispatchers.IO) {
        val dir = File(workspacesRoot, projectId)
        val entries = collectWorkspaceFiles(dir)
            .filter { it.sizeBytes <= maxFileSizeBytes }
            .sortedWith(
                compareByDescending<WorkspaceFileEntry> { it.isEditableText }
                    .thenBy { it.relativePath }
            )
            .take(maxFiles)

        entries.mapNotNull { entry ->
            val f = resolveSafeProjectFile(projectId, entry.relativePath)
            if (f.exists() && f.isFile) entry.relativePath to f.readBytes() else null
        }
    }

    suspend fun exportWorkspaceToZipUri(projectId: String, destUri: Uri): Int = withContext(Dispatchers.IO) {
        val dir = File(workspacesRoot, projectId)
        if (!dir.exists()) throw IllegalStateException("پروژه پیدا نشد.")
        val files = collectWorkspaceFiles(dir)
        val out = context.contentResolver.openOutputStream(destUri)
            ?: throw IllegalStateException("امکان ایجاد فایل زیپ خروجی وجود ندارد.")

        var written = 0
        out.use { stream ->
            ZipOutputStream(BufferedOutputStream(stream)).use { zos ->
                files.forEach { entry ->
                    val file = resolveSafeProjectFile(projectId, entry.relativePath)
                    if (file.exists() && file.isFile) {
                        zos.putNextEntry(ZipEntry(entry.relativePath))
                        FileInputStream(file).use { fis -> fis.copyTo(zos) }
                        zos.closeEntry()
                        written++
                    }
                }
            }
        }
        written
    }

    suspend fun buildProjectContextForAi(
        projectId: String,
        openFilePath: String? = null
    ): Pair<String, String> = withContext(Dispatchers.IO) {
        val dir = File(workspacesRoot, projectId)
        val files = collectWorkspaceFiles(dir)
        val treeText = files.take(120).joinToString("\n") { "${it.relativePath} (${it.sizeBytes}B)" }

        val priorityNames = listOf(
            "package.json", "metadata.json", "AndroidManifest.xml", "build.gradle.kts",
            "app/build.gradle.kts", "settings.gradle.kts", "firebase.json",
            "firestore.rules", "README.md", "tsconfig.json", "App.tsx", "src/App.tsx",
            "MainActivity.kt", "app/src/main/java/com/example/MainActivity.kt"
        )
        val selectedKeyFiles = files
            .filter { entry ->
                entry.isEditableText && (
                    entry.relativePath in priorityNames ||
                        priorityNames.any { entry.relativePath.endsWith("/$it") || entry.relativePath.equals(it, ignoreCase = true) }
                    )
            }
            .take(6)

        val snippets = buildString {
            selectedKeyFiles.forEach { entry ->
                val f = resolveSafeProjectFile(projectId, entry.relativePath)
                if (f.exists() && entry.relativePath != openFilePath) {
                    val content = f.readText(Charsets.UTF_8).take(2600)
                    appendLine("=== FILE: ${entry.relativePath} ===")
                    appendLine(content)
                    appendLine()
                }
            }
        }
        treeText to snippets
    }

    fun buildDeployTemplateFiles(
        templateType: DeployTemplateType,
        projectName: String
    ): List<Pair<String, String>> {
        val safeName = projectName.ifBlank { "ai-studio-project" }
        return when (templateType) {
            DeployTemplateType.GitHubActionsAndroidApk -> listOf(
                ".github/workflows/android-apk.yml" to """
                    name: Build Android APK ($safeName)
                    on:
                      push:
                        branches: [ "main", "master" ]
                      workflow_dispatch:
                    jobs:
                      build:
                        runs-on: ubuntu-latest
                        steps:
                          - uses: actions/checkout@v4
                          - name: Set up JDK 21
                            uses: actions/setup-java@v4
                            with:
                              java-version: '21'
                              distribution: 'temurin'
                          - name: Setup Gradle
                            uses: gradle/actions/setup-gradle@v4
                          - name: Build Debug APK
                            run: |
                              if [ -f "./gradlew" ]; then chmod +x ./gradlew && ./gradlew assembleDebug; else gradle assembleDebug; fi
                          - name: Upload APK Artifact
                            uses: actions/upload-artifact@v4
                            with:
                              name: app-debug-apk
                              path: app/build/outputs/apk/debug/*.apk
                """.trimIndent()
            )
            DeployTemplateType.FirebaseHostingAndRules -> listOf(
                "firebase.json" to """
                    {
                      "hosting": {
                        "public": "dist",
                        "ignore": ["firebase.json", "**/.*", "**/node_modules/**"],
                        "rewrites": [{ "source": "**", "destination": "/index.html" }]
                      },
                      "firestore": {
                        "rules": "firestore.rules"
                      }
                    }
                """.trimIndent(),
                ".github/workflows/firebase-deploy.yml" to """
                    name: Deploy to Firebase Hosting & Firestore
                    on:
                      push:
                        branches: [ "main" ]
                      workflow_dispatch:
                    jobs:
                      deploy:
                        runs-on: ubuntu-latest
                        steps:
                          - uses: actions/checkout@v4
                          - uses: actions/setup-node@v4
                            with:
                              node-version: '20'
                          - run: if [ -f package.json ]; then npm ci && npm run build; fi
                          - uses: FirebaseExtended/action-hosting-deploy@v0
                            with:
                              repoToken: ${'$'}{{ secrets.GITHUB_TOKEN }}
                              firebaseServiceAccount: ${'$'}{{ secrets.FIREBASE_SERVICE_ACCOUNT }}
                              channelId: live
                """.trimIndent()
            )
            DeployTemplateType.AiStudioGitHubPagesWeb -> listOf(
                ".github/workflows/pages-deploy.yml" to """
                    name: Build & Deploy AI Studio App to GitHub Pages
                    on:
                      push:
                        branches: [ "main" ]
                      workflow_dispatch:
                    permissions:
                      contents: read
                      pages: write
                      id-token: write
                    jobs:
                      build:
                        runs-on: ubuntu-latest
                        steps:
                          - uses: actions/checkout@v4
                          - uses: actions/setup-node@v4
                            with:
                              node-version: '20'
                          - run: npm install && npm run build
                          - uses: actions/upload-pages-artifact@v3
                            with:
                              path: ./dist
                      deploy:
                        environment:
                          name: github-pages
                          url: ${'$'}{{ steps.deployment.outputs.page_url }}
                        runs-on: ubuntu-latest
                        needs: build
                        steps:
                          - id: deployment
                            uses: actions/deploy-pages@v4
                """.trimIndent()
            )
        }
    }

    private fun resolveSafeProjectFile(projectId: String, relativePath: String): File {
        val projectRoot = File(workspacesRoot, projectId).canonicalFile
        val cleanRel = relativePath.replace("\\", "/").trimStart('/')
        val target = File(projectRoot, cleanRel).canonicalFile
        val prefix = projectRoot.path + File.separator
        if (target.path != projectRoot.path && !target.path.startsWith(prefix)) {
            throw SecurityException("دسترسی خارج از فضای کاری پروژه مسدود شد.")
        }
        return target
    }

    private fun collectWorkspaceFiles(root: File): List<WorkspaceFileEntry> {
        if (!root.exists()) return emptyList()
        val results = mutableListOf<WorkspaceFileEntry>()
        root.walkTopDown()
            .onEnter { dir -> dir.name !in ignoredSegments }
            .filter { it.isFile }
            .forEach { file ->
                val rel = file.relativeTo(root).invariantSeparatorsPath
                val ext = file.extension.lowercase()
                val fileNameLower = file.name.lowercase()
                val isText = ext in textExtensions ||
                    fileNameLower in setOf("dockerfile", "makefile", ".gitignore", ".env", ".env.example")
                results.add(
                    WorkspaceFileEntry(
                        relativePath = rel,
                        sizeBytes = file.length(),
                        isEditableText = isText
                    )
                )
            }
        return results.sortedBy { it.relativePath }
    }

    private fun shouldIgnorePath(path: String): Boolean {
        val parts = path.split('/')
        return parts.any { it in ignoredSegments }
    }

    private fun resolveDisplayName(uri: Uri): String {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) {
                return cursor.getString(idx).orEmpty()
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/') ?: "project.zip"
    }
}
