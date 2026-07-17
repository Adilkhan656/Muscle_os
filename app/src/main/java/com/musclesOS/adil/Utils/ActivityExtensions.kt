package com.musclesOS.adil.Utils

import android.content.res.ColorStateList
import android.graphics.Color
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.musclesOS.adil.R

/**
 * Extension function for AppCompatActivity to execute code only if internet is available.
 * Shows a toast message if there is no connection.
 */
fun AppCompatActivity.requireInternet(
    block: () -> Unit
) {

    if (NetworkUtils.isInternetAvailable(this)) {

        block()

    } else {

        Toast.makeText(
            this,
            "No Internet Connection",
            Toast.LENGTH_SHORT
        ).show()

    }
}


/**
 * Common utility to handle loading states across activities.
 * Handles the scrim, the lottie animation, and disabling/enabling buttons.
 */
fun setLoadingState(
    show: Boolean,
    scrim: View? = null,
    animation: View? = null,
    vararg viewsToDisable: View
) {
    scrim?.visibility = if (show) View.VISIBLE else View.GONE
    animation?.visibility = if (show) View.VISIBLE else View.GONE
    viewsToDisable.forEach { it.isEnabled = !show }
}
/**
 * Sets up a password toggle for an ImageView and its associated EditText.
 * Handles the visibility transformation and tint changes based on input.
 */
fun ImageView.bindPasswordToggle(
    editText: EditText
) {

    isEnabled = false

    val activeColor = ContextCompat.getColor(context, R.color.text_main)
    val inactiveColor = ContextCompat.getColor(context, R.color.text_hint)

    imageTintList = ColorStateList.valueOf(inactiveColor)

    editText.addTextChangedListener {

        val enabled = !it.isNullOrEmpty()

        isEnabled = enabled

        imageTintList = ColorStateList.valueOf(
            if (enabled) activeColor else inactiveColor
        )
    }

    setOnClickListener {

        if (!isEnabled) return@setOnClickListener

        val cursor = editText.selectionStart

        val visible =
            editText.transformationMethod !is PasswordTransformationMethod

        editText.transformationMethod =
            if (visible)
                PasswordTransformationMethod.getInstance()
            else
                HideReturnsTransformationMethod.getInstance()

        editText.setSelection(cursor)
    }
}
