package com.musclesOS.adil.data

/**
 * MuscleOption.kt
 *
 * xBias / yBias: 0.0 (Left/Top) to 1.0 (Right/Bottom).
 * glowScale: Size of the blur (e.g. 1.5 for big area, 0.7 for small).
 */
data class MuscleOption(
    val id: String,
    val displayName: String,
    val hasBodyDot: Boolean = true,
    val xBias: Float = 0.5f,
    val yBias: Float = 0.5f,
    val glowScale: Float = 1.0f,
    val hScale: Float = 1.0f,
    val vScale: Float = 1.0f
)

object MuscleOptions {

    val ALL = listOf(

        MuscleOption(
            id = "full_body",
            displayName = "Full body",
            xBias = 0.50f,
            yBias = 0.40f,
            glowScale = 1.5f
        ),

        MuscleOption(
            id = "back",
            displayName = "Back",
            xBias = 0.54f,
            yBias = 0.20f,
            glowScale = 1.2f,
            hScale = 1.3f,
            vScale = 0.8f
        ),

        MuscleOption(
            id = "shoulders",
            displayName = "Shoulders",
            xBias = 0.44f,
            yBias = 0.24f,
            glowScale = 0.9f,
            hScale = 1.4f,
            vScale = 0.8f
        ),

        MuscleOption(
            id = "chest",
            displayName = "Chest",
            xBias = 0.50f,
            yBias = 0.28f,
            glowScale = 2.0f,
            hScale = 1.5f,
            vScale = 0.9f
        ),

        MuscleOption(
            id = "arms",
            displayName = "Arms",
            xBias = 0.44f,
            yBias = 0.33f,
            glowScale = 1.1f,
            hScale = 0.7f,
            vScale = 1.4f
        ),

        MuscleOption(
            id = "abs",
            displayName = "Abs",
            xBias = 0.50f,
            yBias = 0.37f,
            glowScale = 1.2f,
            hScale = 1.9f,
            vScale = 1.9f
        ),

        MuscleOption(
            id = "butt",
            displayName = "Butt",
            xBias = 0.55f,
            yBias = 0.58f,
            glowScale = 1.1f,
            hScale = 1.4f,
            vScale = 0.9f
        ),

        MuscleOption(
            id = "legs",
            displayName = "Legs",
            xBias = 0.47f,
            yBias = 0.59f,
            glowScale = 1.4f,
            hScale = 1.8f,
            vScale = 1.5f
        ),
    )
}
