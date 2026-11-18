# Personal AI Creative Studio (Android)

Android Kotlin + Jetpack Compose application (`com.aistudio.creativestudio.xmzpqr`) featuring an emotionally responsive Persian/English Personal & Phone Secretary, Dual-Voice/Video Companion, Tri-Tier Memory & Neuro-Endocrine Engine, ZIP Project Analyzer/Code Completer, and automated Release APK & AAB build pipeline.

## Automated API, Secrets & Signing Architecture
- **Gemini AI (`Firebase.ai` + App Check)**: Keyless server-verified setup via `app/google-services.json`, `res/values/firebase_applet_config.xml` (`recaptcha_site_key` for Release APK/AAB), and `AppCheckInit.kt`. No `GEMINI_API_KEY` is required or embedded in code.
- **Secrets Gradle Plugin (`.env` / `.env.example`)**: Configured via `com.google.android.libraries.mapsplatform.secrets-gradle-plugin:2.0.1` in `app/build.gradle.kts`. Set optional `GITHUB_PERSONAL_TOKEN` in AI Studio's **Secrets** panel (or GitHub Actions Secrets) to auto-inject into `BuildConfig.GITHUB_PERSONAL_TOKEN`.
- **Gradle Wrapper & Signed Release APK/AAB**: Includes `./gradlew` (`gradle/wrapper/`) and `.github/workflows/android-release.yml` which automatically restores `debug.keystore` from `debug.keystore.base64` (matching the SHA-1 registered in `app/google-services.json`) and builds signed `app-release.apk`, `app-release.aab`, and `app-debug.apk`.
