package com.musclesOS.adil.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.musclesOS.adil.data.local.UserProfileEntity
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class FirestoreUserProfileDataSource(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private companion object {
        const val USERS_COLLECTION = "users"
        const val FIRESTORE_OPERATION_TIMEOUT_MS = 15_000L
    }

    suspend fun saveUserProfile(profile: UserProfileEntity): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(
                    IllegalStateException("User is not authenticated")
                )

            if (userId != profile.userId) {
                return Result.failure(
                    IllegalStateException("Authenticated user does not match profile")
                )
            }

            val userData = hashMapOf(
                "userId" to profile.userId,
                "gender" to profile.gender,
                "age" to profile.age,
                "heightCm" to profile.heightCm,
                "weightKg" to profile.weightKg,
                "activityLevel" to profile.activityLevel,
                "experienceLevel" to profile.experienceLevel,
                "focusAreas" to profile.focusAreas,
                "goals" to profile.goals,
                "customGoal" to profile.customGoal,
                "isOnboardingCompleted" to profile.isOnboardingCompleted,
                "createdAt" to profile.createdAt,
                "updatedAt" to profile.updatedAt
            )

            // Do not leave the onboarding screen waiting for minutes if Firestore
            // cannot reach the backend. Fail fast and let the UI show the real error.
            withTimeout(FIRESTORE_OPERATION_TIMEOUT_MS) {
                firestore
                    .collection(USERS_COLLECTION)
                    .document(userId)
                    .set(userData)
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchUserProfile(userId: String): Result<UserProfileEntity?> {
        return try {
            val document = withTimeout(FIRESTORE_OPERATION_TIMEOUT_MS) {
                firestore
                    .collection(USERS_COLLECTION)
                    .document(userId)
                    .get()
                    .await()
            }

            if (!document.exists()) {
                return Result.success(null)
            }

            val data = document.data ?: return Result.success(null)

            val profile = UserProfileEntity(
                userId = data["userId"] as? String ?: userId,
                gender = data["gender"] as? String ?: "",
                age = (data["age"] as? Long)?.toInt() ?: 0,
                heightCm = (data["heightCm"] as? Long)?.toInt() ?: 0,
                weightKg = (data["weightKg"] as? Double) ?: 0.0,
                activityLevel = data["activityLevel"] as? String ?: "",
                experienceLevel = data["experienceLevel"] as? String ?: "",
                focusAreas = (data["focusAreas"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                goals = (data["goals"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                customGoal = data["customGoal"] as? String ?: "",
                isOnboardingCompleted = data["isOnboardingCompleted"] as? Boolean ?: false,
                createdAt = (data["createdAt"] as? Long) ?: 0L,
                updatedAt = (data["updatedAt"] as? Long) ?: 0L
            )

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
