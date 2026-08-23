package com.musclesOS.adil.data

import androidx.annotation.DrawableRes
import com.musclesOS.adil.R

data class GoalOption(

    val id: String,

    val title: String,

    @DrawableRes
    val imageRes: Int
)


object GoalOptions {

    val ALL = listOf(

        GoalOption(
            id = "fat_loss",
            title = "Fat Loss",
            imageRes = R.drawable.goal_fat_loss
        ),

        GoalOption(
            id = "muscle_gain",
            title = "Build Muscle",
            imageRes = R.drawable.goal_muscle_gain
        ),

        GoalOption(
            id = "strength",
            title = "Get Stronger",
            imageRes = R.drawable.goal_strength
        ),

        GoalOption(
            id = "endurance",
            title = "Improve Endurance",
            imageRes = R.drawable.goal_endurance
        ),

        GoalOption(
            id = "fitness",
            title = "Stay Fit",
            imageRes = R.drawable.goal_fitness
        ),

        GoalOption(
            id = "recomposition",
            title = "Recompose",
            imageRes = R.drawable.goal_recomposition
        )
    )
}