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
 * Development-only direct Groq client.
 * The API key is intentionally read from local.properties for development.
 * Before public production release, move this request behind a trusted backend.
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
                    put("age", profile.age)
                    put("gender", profile.gender)
                    put("heightCm", profile.heightCm)
                    put("currentWeightKg", profile.weightKg)
                    put("targetWeightKg", profile.targetWeightKg)
                    put("activityLevel", profile.activityLevel)
                    put("experienceLevel", profile.experienceLevel)
                    put("goals", JSONArray(profile.goals))
                    put("focusAreas", JSONArray(profile.focusAreas))
                    put("customGoal", profile.customGoal)
                }

                val systemPrompt = """
                    You are the MuscleOS AI Fitness Planner.

                    Create a realistic, personalized 30-day fitness and nutrition plan using ONLY
                    the supplied profile. The target weight is a hard input and must be considered
                    when setting calories, macros, training volume, and progress guidance.

                    Return ONLY valid JSON. Do not return markdown, code fences, or explanations.
                    Do not promise that the target weight will be reached in 30 days.
                    Do not prescribe medication or diagnose medical conditions.
                    Prefer practical foods and exercises that a normal beginner/intermediate user can do.
                    Keep the 30-day output compact enough for a mobile app.

                    Required JSON shape:
                    {
                      "profile_summary": string,
                      "starting_weight_kg": number,
                      "target_weight_kg": number,
                      "weight_change_goal_kg": number,
                      "daily_calories": number,
                      "protein_grams": number,
                      "carbs_grams": number,
                      "fat_grams": number,
                      "weekly_training_days": number,
                      "progress_guidance": [string],
                      "recommendations": [string],
                      "safety_notes": [string],
                      "workout_plan": [
                        {
                          "day": number,
                          "focus": string,
                          "exercises": [
                            {"name": string, "sets": number, "reps": string, "rest_seconds": number}
                          ],
                          "cardio": string,
                          "recovery": string
                        }
                      ],
                      "nutrition_plan": [
                        {
                          "day": number,
                          "calories": number,
                          "protein_grams": number,
                          "breakfast": string,
                          "lunch": string,
                          "dinner": string,
                          "snack": string
                        }
                      ]
                    }

                    workout_plan MUST contain exactly 30 day objects and nutrition_plan MUST contain exactly 30 day objects.
                    Vary workouts and meals across the month while keeping the plan coherent.
                """.trimIndent()

                val requestJson = JSONObject().apply {
                    put("model", MODEL)
                    put("temperature", 0.35)
                    put("max_completion_tokens", 7500)
                    put("response_format", JSONObject().put("type", "json_object"))
                    put(
                        "messages",
                        JSONArray().apply {
                            put(JSONObject().apply {
                                put("role", "system")
                                put("content", systemPrompt)
                            })
                            put(JSONObject().apply {
                                put("role", "user")
                                put("content", profileJson.toString())
                            })
                        }
                    )
                }

                val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 30_000
                    readTimeout = 120_000
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
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error body returned"
                }
                connection.disconnect()

                if (statusCode !in 200..299) {
                    Log.e(TAG, "Groq HTTP $statusCode: $responseBody")
                    return@withContext Result.failure(
                        IllegalStateException("Groq API returned HTTP $statusCode: $responseBody")
                    )
                }

                val content = JSONObject(responseBody)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                // Validate that the model returned JSON before handing it to the app.
                JSONObject(content)
                Result.success(content)
            } catch (e: Exception) {
                Log.e(TAG, "Groq request failed", e)
                Result.failure(e)
            }
        }
}
