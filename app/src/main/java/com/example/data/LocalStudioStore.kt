package com.example.data

import android.content.Context
import android.util.Log
import com.example.model.PersistedStudioState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

private const val TAG = "LocalStudioStore"
private const val STORE_FILENAME = "studio_state.json"

class LocalStudioStore(context: Context) {
    private val stateFile = File(context.applicationContext.filesDir, STORE_FILENAME)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    suspend fun load(): PersistedStudioState? = withContext(Dispatchers.IO) {
        try {
            if (!stateFile.exists()) return@withContext null
            val raw = stateFile.readText(Charsets.UTF_8)
            if (raw.isBlank()) return@withContext null
            json.decodeFromString(PersistedStudioState.serializer(), raw)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read persisted studio state", e)
            null
        }
    }

    suspend fun save(state: PersistedStudioState) = withContext(Dispatchers.IO) {
        try {
            val encoded = json.encodeToString(PersistedStudioState.serializer(), state)
            stateFile.writeText(encoded, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist studio state", e)
        }
    }
}
