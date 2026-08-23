package com.musclesOS.adil.utils.animation

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import com.musclesOS.adil.R

object OnboardingAnimations {

    // Target values for the "selected/popped" image state.
    private const val POP_SCALE = 1.22f
    private const val POP_TRANSLATION_Y = -24f

    fun selectGender(card: View, image: View) {
        image.animate().cancel()
        card.animate().cancel()

        // SNAP instantly to the final popped state — no secondary bounce.
        // A grow-past-then-shrink-back bounce on top of the snap is what
        // caused the laggy/jittery feel: two motions stacked in ~180ms,
        // and a fast next tap would cancel it mid-bounce leaving the image
        // at a slightly-off size that compounded over repeated taps.
        // A clean snap has zero jitter and still feels responsive.
        image.scaleX = POP_SCALE
        image.scaleY = POP_SCALE
        image.translationY = POP_TRANSLATION_Y

        card.scaleX = 1f
        card.scaleY = 1f
        card.translationZ = 0f
    }

    fun deselectGender(card: View, image: View) {
        image.animate().cancel()
        card.animate().cancel()

        // Same principle: snap to neutral immediately so it's never left
        // half-popped when the other card is rapidly selected instead.
        image.scaleX = 1f
        image.scaleY = 1f
        image.translationY = 0f

        card.scaleX = 1f
        card.scaleY = 1f
        card.translationZ = 0f

        // Optional light settle animation for polish only — not required
        // for correctness, so interruption is harmless.
        image.animate()
            .scaleX(1f)
            .scaleY(1f)
            .translationY(0f)
            .setDuration(120)
            .setInterpolator(DecelerateInterpolator(1.5f))
            .start()
    }

    fun fadeInSlideIn(view: View, index: Int, delayOffset: Long = 0L) {
        view.alpha = 0f
        view.translationX = -24f
        view.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(300)
            .setStartDelay(delayOffset + (index * 50L))
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    fun fadeInSlideUp(view: View, delay: Long) {
        view.alpha = 0f
        view.translationY = 20f
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(450)
            .setStartDelay(delay)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    fun animateGenderEntrance(card: View, image: View, delay: Long) {
        card.alpha = 0f
        card.translationY = 25f
        card.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(delay)
            .setInterpolator(DecelerateInterpolator())
            .start()

        image.alpha = 0f
        image.translationY = 40f
        image.scaleX = 0.88f
        image.scaleY = 0.88f
        image.animate()
            .alpha(1f)
            .translationY(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(700)
            .setStartDelay(delay + 80)
            .setInterpolator(OvershootInterpolator(0.65f))
            .start()
    }

    fun applyGoalEntranceAnimations(
        headers: List<View>,
        subtitle: View,
        goalItems: List<View>,
        bottomBar: View
    ) {
        headers.forEachIndexed { index, view ->
            fadeInSlideIn(view, index)
        }

        subtitle.alpha = 0f
        subtitle.translationY = -20f
        subtitle.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(100)
            .start()

        goalItems.forEachIndexed { index, itemView ->
            itemView.alpha = 0f
            itemView.translationY = 40f
            itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(200L + index * 60L)
                .start()
        }

        bottomBar.alpha = 0f
        bottomBar.translationY = 30f
        bottomBar.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(550)
            .start()
    }

    fun animateGoalSelection(itemView: View, moveY: Float, onComplete: () -> Unit) {
        itemView.bringToFront()

        val border = itemView.findViewById<View>(R.id.selectionBorder)
        val minus = itemView.findViewById<View>(R.id.minusContainer)

        border.animate().alpha(1f).setDuration(200).start()
        minus.animate().alpha(1f).setDuration(200).start()

        itemView.animate().cancel()
        itemView.animate()
            .translationX(0f)
            .translationY(moveY)
            .scaleX(1.15f)
            .scaleY(1.15f)
            .setDuration(550)
            .setInterpolator(OvershootInterpolator(0.6f))
            .withEndAction { onComplete() }
            .start()
    }

    fun animateGoalDeselection(itemView: View) {
        val border = itemView.findViewById<View>(R.id.selectionBorder)
        val minus = itemView.findViewById<View>(R.id.minusContainer)

        border.animate().alpha(0f).setDuration(200).start()
        minus.animate().alpha(0f).setDuration(200).start()

        itemView.animate().cancel()
        itemView.animate()
            .translationX(0f)
            .translationY(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(450)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    fun createFloatingAnimator(view: View, baseY: Float): ObjectAnimator {
        return ObjectAnimator.ofFloat(
            view,
            View.TRANSLATION_Y,
            baseY,
            baseY - 10f,
            baseY
        ).apply {
            duration = 1800
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }
}
