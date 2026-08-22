package com.musclesOS.adil.data

/**
 * MuscleOption.kt
 *
 * One selectable focus-area option. It always shows as a row in the list
 * on the right. If hasBodyDot = true, it ALSO gets a tappable dot on the
 * photo, connected to its row by an animated line.
 *
 * xBias / yBias work like ConstraintLayout bias: 0.0 = left/top edge of
 * the photo, 1.0 = right/bottom edge, 0.5 = center. To move a dot,
 * change these two numbers and rerun the app.
 *
 * TO ADD A NEW MUSCLE: just add one more line to the ALL list below.
 * No XML editing needed - the row and (if applicable) the dot are both
 * generated automatically from this list.
 */
data class MuscleOption(
    val id: String,
    val displayName: String,
    val hasBodyDot: Boolean = true,
    val xBias: Float = 0.5f,
    val yBias: Float = 0.5f
)

object MuscleOptions {
    val ALL = listOf(
        MuscleOption(id = "full_body", displayName = "Full body", hasBodyDot = false),
        MuscleOption(id = "back", displayName = "Back", hasBodyDot = false),
        MuscleOption(id = "chest", displayName = "Chest", xBias = 0.52f, yBias = 0.34f),
        MuscleOption(id = "arms", displayName = "Arms", xBias = 0.18f, yBias = 0.43f),
        MuscleOption(id = "abs", displayName = "Abs", xBias = 0.50f, yBias = 0.57f),
        MuscleOption(id = "glutes", displayName = "Butt", hasBodyDot = false),
        MuscleOption(id = "legs", displayName = "Legs", xBias = 0.48f, yBias = 0.82f)
    )
}
