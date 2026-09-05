package com.musclesOS.adil.utils.animation

/**
 * Legacy marker retained for binary/source compatibility with older lessons.
 * The splash is now rendered entirely by LiquidSurfaceView.
 */
import android.animation.Animator
import android.animation.ValueAnimator
import android.transition.Transition
import android.transition.TransitionValues
import android.view.ViewGroup

class HoldWindowTransition(private val holdDuration: Long) : Transition() {
    init { duration = holdDuration }
    override fun captureStartValues(transitionValues: TransitionValues) {}
    override fun captureEndValues(transitionValues: TransitionValues) {}
    override fun createAnimator(
        sceneRoot: ViewGroup, startValues: TransitionValues?, endValues: TransitionValues?
    ): Animator = ValueAnimator.ofFloat(0f, 1f).apply { duration = holdDuration }
}
