package com.musclesOS.adil.data

import android.graphics.PointF
import com.musclesOS.adil.ui.onboarding.LabelSide
import com.musclesOS.adil.ui.onboarding.MuscleRegion

/**
 * MuscleData.kt
 *
 * Starter set of tappable muscle regions for BodyMapView.
 * Path data is combined from the "front muscles" source (left + right
 * shapes merged into a single region), so tapping either side counts
 * as hitting that muscle group.
 *
 * IMPORTANT: The coordinates below come from a small SVG coordinate
 * system (roughly 0-33 wide, 0-95 tall). In BodyMapView.kt, set:
 *     viewBoxWidth = 33f
 *     viewBoxHeight = 95f
 * as a starting point, then nudge these two numbers until the shapes
 * line up with your background body image. This is normal - the exact
 * viewBox wasn't included in the file you pasted, so we're estimating
 * from the coordinate ranges we can see.
 */
object MuscleData {

    val CHEST = MuscleRegion(
        id = "chest",
        displayName = "Chest",
        // chest-upper-left + chest-lower-left + chest-upper-right + chest-lower-right combined
        pathData = "m 20.337455,17.085495 1.72942,3.09103 1.890,0.94 -0.5,0.3 -6.8, -2.1 z " +
                "m 16.66,19.72 6.8,2.1 -0.65,0.5 -0.90604,2.63773 -2.09968,0.86537 -3.34524,-1.655 0.2,-3.8 z " +
                "m 11.351215,17.085495 -1.7294199,3.09103 -1.890,0.94 0.5,0.3 6.8,-2.1 z " +
                "m 15.03,19.72 -6.8,2.1 0.65,0.5 0.90586,2.63773 2.0996699,0.86537 3.34636,-1.655 -0.2,-3.8 z",
        anchor = PointF(15.8f, 20f),   // roughly the center of the chest area
        labelSide = LabelSide.LEFT,
        labelSlot = 0
    )

    val ABS = MuscleRegion(
        id = "abs",
        displayName = "Abs",
        // abs-upper-left + abs-upper-right + abs-lower-right + abs-lower-left combined
        pathData = "m 19.641935,34.707615 1.81341,-1.36479 0.15748,1.83347 1.28642,2.37338 -1.98044,2.73652 -1.03109,0.16554 -0.37026,-3.88816 z " +
                "m 12.045985,34.707615 -1.81341,-1.36479 -0.15748,1.83347 -1.2856799,2.37432 1.9804499,2.73595 1.03109,0.16554 0.37119,-3.88721 z " +
                "m 15.636055,44.919735 -0.60647,-5.91209 -0.015,-3.84879 -2.18479,-1.07533 -0.24746,7.03017 z " +
                "m 16.051865,44.919165 0.60628,-5.91209 0.0154,-3.84915 2.18404,-1.07515 0.24746,7.03017 z",
        anchor = PointF(15.85f, 40f),  // roughly the center of the abs area
        labelSide = LabelSide.LEFT,
        labelSlot = 1
    )

    /** Everything the app should register with BodyMapView.setRegions(...) for now. */
    val ALL = listOf(CHEST, ABS)
}
