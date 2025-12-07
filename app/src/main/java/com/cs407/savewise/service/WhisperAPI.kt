package com.cs407.savewise.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File

object WhisperApi {

    private const val API_URL = "https://api.openai.com/v1/audio/transcriptions"
    private val API_KEY = EnvHelper.WHISPER_KEY

    suspend fun transcribe(file: File): String = withContext(Dispatchers.IO) {

        val client = OkHttpClient()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("model", "whisper-1")
            .addFormDataPart(
                "file",
                file.name,
                file.asRequestBody("audio/wav".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url(API_URL)
            .header("Authorization", "Bearer $API_KEY")
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val raw = response.body!!.string()

        println("🔍 Whisper Response:\n$raw")

        val json = JSONObject(raw)

        // ❗ 如果返回包含 error 字段 → 抛异常（不会崩溃）
        if (json.has("error")) {
            val msg = json.getJSONObject("error").getString("message")
            throw Exception("Whisper API Error: $msg")
        }

        // ❗ 如果没有 text 字段，返回空文本而不是崩溃
        if (!json.has("text")) {
            throw Exception("Whisper no text return")
        }

        json.getString("text")
    }
}