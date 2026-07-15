package com.example.refiner_app

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object RefinerBackendClient {
    private const val MAX_INPUT_CHARACTERS = 12_000

    fun refine(context: Context, input: String): String {
        require(input.isNotBlank()) { "Select some text to refine." }
        require(input.length <= MAX_INPUT_CHARACTERS) {
            "The selected text is too long (maximum $MAX_INPUT_CHARACTERS characters)."
        }

        val url = context.getString(R.string.refiner_backend_url)
        val functionKey = context.getString(R.string.refiner_backend_key)
        check(url.startsWith("https://") && functionKey.isNotBlank()) {
            "Refiner backend is not configured in this build."
        }

        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 60_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("x-functions-key", functionKey)
        }

        return try {
            connection.outputStream.bufferedWriter(Charsets.UTF_8).use {
                it.write(JSONObject().put("text", input).toString())
            }
            val status = connection.responseCode
            val response = (if (status in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            })?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()

            val payload = runCatching { JSONObject(response) }.getOrNull()
            if (status !in 200..299) {
                throw IllegalStateException(
                    payload?.optString("error")?.takeIf { it.isNotBlank() }
                        ?: "Refiner request failed (HTTP $status).",
                )
            }

            val refined = payload?.optString("text").orEmpty().trim()
            check(refined.isNotEmpty()) {
                "The refinement service returned an empty response."
            }
            refined
        } finally {
            connection.disconnect()
        }
    }
}