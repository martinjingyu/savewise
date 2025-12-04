package com.cs407.savewise.service

import com.cs407.savewise.model.ExpenseRecord
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONArray


class ChatRepository {
    fun sendPromptToGpt(prompt: String): Flow<Result<String>> {
        return callbackFlow {
            GptApiHelper.askGpt(
                prompt,
                onResponse = { trySend(Result.success(it)) },
                onError = { trySend(Result.failure(Exception(it))) }
            )
            awaitClose { }
        }
    }
    fun sendTextToGpt(
        text: String,
        expenses: List<ExpenseRecord>
    ): Flow<Result<String>> {
        return callbackFlow {
            GptApiHelper.chatText(
                text = text,
                expenses = expenses,
                onResponse = { trySend(Result.success(it)) },
                onError = { trySend(Result.failure(Exception(it))) }
            )
            awaitClose { }
        }
    }
}
object GptApiHelper {

    private const val API_KEY = "CaiYiOZxKiwqTa4RPVSPvcYSsnIlXqPw4rRBGyZQ3mSPJjrg5kWLJQQJ99BKACYeBjFXJ3w3AAABACOGA75q"
    private const val ENDPOINT = "https://intern-jingyu-jh-east.openai.azure.com"
    private const val DEPLOYMENT = "gpt-4o" // Azure 的 deployment 名
    private const val API_VERSION = "2024-12-01-preview"
    val url =
        "$ENDPOINT/openai/deployments/$DEPLOYMENT/chat/completions?api-version=$API_VERSION"

    val client = OkHttpClient()
    val systemPrompt = """
You are an assistant that extracts structured expense information from natural language.

Given any user sentence, convert it into a strict JSON object with the following fields:

{
  "title": "a short title (max 5 words)",
  "category": "one of [Food, Transport, Shopping, Bills, Entertainment, Other]",
  "amount": number (float, no currency symbol)
}

Rules:
- Detect the amount even if spoken casually ("five bucks", "2 dollars", "七块钱").
- If no amount exists, set amount = 0.
- Title should summarize the transaction.
- Category should match the nature of the expense.
- Output ONLY valid JSON. Do not add explanations.
""".trimIndent()
    fun askGpt(
        prompt: String,
        onResponse: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val messagesArray = org.json.JSONArray().apply {

            // ⭐ system prompt
            put(
                JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                }
            )

            // ⭐ user prompt
            put(
                JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                }
            )
        }

        val bodyJson = JSONObject().apply {
            put("messages", messagesArray)
            put("temperature", 1.0)
            put("max_tokens", 200)
            put("top_p", 1.0)
        }
        val body = bodyJson.toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .addHeader("api-key", API_KEY)  // ❗Azure 必须用 api-key
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val raw = response.body?.string()

                if (!response.isSuccessful) {
                    onError("HTTP ${response.code}: $raw")
                    return
                }

                try {
                    val json = JSONObject(raw)
                    val content =
                        json.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")

                    println(content)
                    onResponse(content)

                } catch (e: Exception) {
                    onError("Parse error: ${e.message}\nRaw: $raw")
                }
            }
        })
    }
    fun chatText(
        text: String,
        expenses: List<ExpenseRecord>,
        onResponse: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val expensesJson = JSONArray().apply {
            expenses.forEach { exp ->
                put(JSONObject().apply {
                    // 根据你的 ExpenseRecord 字段自行增减
                    put("title", exp.title)
                    put("category", exp.category)
                    put("amount", exp.amount)
                    put("date", exp.date)   // 如果是 Long/时间戳也没问题
                    put("id", exp.id)
                })
            }
        }

        val messagesArray = JSONArray().apply {

            put(JSONObject().apply {
                put("role", "system")
                put(
                    "content",
                    """
You are a AI Finance Assistance assistant in an App Savewise. Your name is Savewise AI.
You will receive user's question AND a JSON array of their recent expenses.
Use the expenses as context to answer briefly and helpfully.
If user asks about spending, summarize trends based on expenses.
Keep responses short and concise.
""".trimIndent()
                )
            })

            // ① 先给 GPT expenses 上下文
            put(JSONObject().apply {
                put("role", "user")
                put(
                    "content",
                    "Here are my recent expenses (JSON): $expensesJson"
                )
            })

            // ② 再给用户真正的问题
            put(JSONObject().apply {
                put("role", "user")
                put("content", text)
            })
        }

        val bodyJson = JSONObject().apply {
            put("messages", messagesArray)
            put("temperature", 0.8)
            put("max_tokens", 500)
            put("top_p", 1.0)
        }

        post(bodyJson, onResponse, onError)
    }

    // 抽成公共 post，避免重复
    private fun post(
        bodyJson: JSONObject,
        onResponse: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val body = bodyJson.toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .addHeader("api-key", API_KEY)
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val raw = response.body?.string()

                if (!response.isSuccessful) {
                    onError("HTTP ${response.code}: $raw")
                    return
                }

                try {
                    val json = JSONObject(raw)
                    val content =
                        json.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")

                    onResponse(content)
                } catch (e: Exception) {
                    onError("Parse error: ${e.message}\nRaw: $raw")
                }
            }
        })
    }
}