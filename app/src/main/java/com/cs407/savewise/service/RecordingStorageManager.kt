package com.cs407.savewise.service

import android.content.Context
import java.io.File

/**
 * Simple helper to persist microphone recordings locally and prune them
 * based on the user's retention preference.
 */
class RecordingStorageManager(private val context: Context) {
    private val recordingsDir: File = File(context.filesDir, "recordings").apply { mkdirs() }

    /**
     * Save a copy of the given source file into the app's recordings directory.
     */
    fun saveCopy(source: File): File? {
        if (!source.exists()) return null
        return try {
            val dest = File(recordingsDir, "recording_${System.currentTimeMillis()}.wav")
            source.copyTo(dest, overwrite = true)
            dest
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Delete recordings older than [days] days. If days == 0, keep everything.
     */
    fun cleanupOlderThan(days: Int) {
        if (days == 0) return // "Never" keep all
        val cutoff = System.currentTimeMillis() - days * 24L * 60L * 60L * 1000L
        recordingsDir.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoff) {
                runCatching { file.delete() }
            }
        }
    }
}
