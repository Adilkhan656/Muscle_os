package com.musclesOS.adil.data.remote

import android.util.Log
import com.musclesOS.adil.BuildConfig
import com.musclesOS.adil.data.local.UserProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Development-only direct client for the Groq API.
 *
 * IMPORTANT: the API key is currently exposed through BuildConfig and can be
 * extracted from an APK. This is acceptable only for development/testing.
 * Before production release, move this request behind a trusted backend.
 */
class GroqApiService {

    companion object {
        private const val TAG = "GROQ"
        private const val ENDPOINT = "https://api.groq.com/openai/v1/chat/completions"
        private const val MODEL = "openai/gpt-oss-20b"
    }

    suspend fun generateTestPlan(profile: UserProfileEntity): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val apiKey = BuildConfig.GROQ_API_KEY.trim()
                if (apiKey.isBlank()) {
                    return@withContext Result.failure(
                        IllegalStateException("GROQ_API_KEY is missing. Add groq_api_key to local.properties.")
                    )
                }

                val profileJson = JSONObject().apply {
                    put("gender", profile.gender)
                    put("age", profile.age)
                    put("heightCm", profile.heightCm)
                    put("currentWeightKg", profile.weightKg)
                    put("activityLevel", profile.activityLevel)
                    put("experienceLevel", profile.experienceLevel)
                    put("focusAreas", JSONArray(profile.focusAreas))
                    put("goals", JSONArray(profile.goals))
                    put("customGoal", profile.customGoal)
                }

                val systemPrompt = """
                    You are the MuscleOS AI Fitness Planner.
                    Analyze the user's fitness profile and provide a concise personalized plan.
                    Consider age, gender, height, current weight, activity level, experience,
                    goals, focus areas, and custom goal.
                    Do not promise a specific amount of weight loss or muscle gain.
                    Keep recommendations realistic and safe.
                    Return ONLY valid JSON with these keys:
                    summary, daily_calories, protein_grams, recommendations.
                    The recommendations value must be an array of strings.
                """.trimIndent()

                val requestJson = JSONObject().apply {
                    put("model", MODEL)
                    put("temperature", 0.2)
                    put("response_format", JSONObject().put("type", "json_object"))
                    put(
                        "messages",
                        JSONArray().apply {
                            put(
                                JSONObject().apply {
                                    put("role", "system")
                                    put("content", systemPrompt)
                                }
                            )
                            put(
                                JSONObject().apply {
                                    put("role", "user")
                                    put("content", profileJson.toString())
                                }
                            )
                        }
                    )
                }

                val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 30_000
                    readTimeout = 60_000
                    doOutput = true
                    setRequestProperty("Authorization", "Bearer $apiKey")
                    setRequestProperty("Content-Type", "application/json")
                }

                connection.outputStream.use { output ->
                    output.write(requestJson.toString().toByteArray(Charsets.UTF_8))
                }

                val statusCode = connection.responseCode
                val responseBody = if (statusCode in 200..299) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    connection.errorStream?.bufferedReader()?.use { it.readText() }
                        ?: "No error body returned"
                }

                connection.disconnect()

                if (statusCode !in 200..299) {
                    Log.e(TAG, "Groq HTTP $statusCode: $responseBody")
                    return@withContext Result.failure(
                        IllegalStateException("Groq API returned HTTP $statusCode: $responseBody")
                    )
                }

                val responseJson = JSONObject(responseBody)
                val content = responseJson
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                Result.success(content)
            } catch (e: Exception) {
                Log.e(TAG, "Groq request failed", e)
                Result.failure(e)
            }
        }
}
