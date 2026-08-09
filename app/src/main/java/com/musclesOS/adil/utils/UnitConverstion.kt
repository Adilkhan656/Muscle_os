package com.musclesOS.adil.utils


object UnitConverter {

    private const val KG_TO_LB = 2.2046226218
    private const val CM_PER_INCH = 2.54

    fun kgToLb(kg: Double): Double {
        return kg * KG_TO_LB
    }

    fun lbToKg(lb: Double): Double {
        return lb / KG_TO_LB
    }

    fun cmToInches(cm: Double): Double {
        return cm / CM_PER_INCH
    }

    fun inchesToCm(inches: Double): Double {
        return inches * CM_PER_INCH
    }

    fun cmToFeetAndInches(cm: Double): Pair<Int, Double> {

        val totalInches = cmToInches(cm)

        val feet = (totalInches / 12).toInt()

        val inches = totalInches - (feet * 12)

        return Pair(feet, inches)
    }

    fun feetAndInchesToCm(
        feet: Int,
        inches: Double
    ): Double {

        val totalInches =
            (feet * 12) + inches

        return inchesToCm(totalInches)
    }
}