package com.musclesOS.adil.Utils

import android.util.Patterns

object Validator{
    fun validateName(name: String): String?{
        if (name.isBlank()) return "Enter Your Name"

        return null
    }
    fun validateEmail(email: String): String?{
        if(email.isBlank())return "Enter Your Email Address"
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Enter a valid Email Address"
        return null
    }
    fun validatePassword(password: String): String?{
        if (password.isBlank()) return "Enter Your Password"
        if (password.length < 6) return "Password must be at least 6 characters long"
        if (!password.matches(Regex(".*[A-Z].*"))) return "Password must contain at least one uppercase letter"
        if (!password.matches(Regex(".*[a-z].*"))) return "Password must contain at least one lowercase letter"
        if (!password.matches(Regex(".*\\d.*"))) return "Password must contain at least one digit"
        if (!password.matches(Regex(".*[!@#\$%^&*()_+\\-=\\[\\]{};':\",.<>/?].*"))) return "Password must contain at least one special character"
        return null
    }
}