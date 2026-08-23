# Share OnboardingViewModel and Update Data Types

This plan outlines the steps to share the `OnboardingViewModel` across all onboarding fragments and update the `UserProfile` data types for height and weight as requested.

## Proposed Changes

### [Data Model]

#### [MODIFY] [UserProfile.kt](file:///Users/adilkhan/AndroidStudioProjects/MuscleOS/app/src/main/java/com/musclesOS/adil/data/UserProfile.kt)
- Change `weightKg` type from `Float` to `Double`.
- Keep `heightCm` as `Int`.

---

### [ViewModel]

#### [MODIFY] [OnboardingViewModel.kt](file:///Users/adilkhan/AndroidStudioProjects/MuscleOS/app/src/main/java/com/musclesOS/adil/ui/onboarding/viewmodel/OnboardingViewModel.kt)
- Update `updateWeight` parameter type to `Double`.

---

### [Onboarding Fragments]

#### [MODIFY] [HeightFragment.kt](file:///Users/adilkhan/AndroidStudioProjects/MuscleOS/app/src/main/java/com/musclesOS/adil/ui/onboarding/fragment/HeightFragment.kt)
- Inject `OnboardingViewModel` using `activityViewModels()`.
- Set `step = 1.0` in `setupScale()`.
- Update `initClickListener` to save the selected height (rounded to `Int`) before navigation.

#### [MODIFY] [WeightFragment.kt](file:///Users/adilkhan/AndroidStudioProjects/MuscleOS/app/src/main/java/com/musclesOS/adil/ui/onboarding/fragment/WeightFragment.kt)
- Inject `OnboardingViewModel` using `activityViewModels()`.
- Update `initClickListener` to save the selected weight (as `Double`) before navigation.

#### [MODIFY] [FocusAreaFragment.kt](file:///Users/adilkhan/AndroidStudioProjects/MuscleOS/app/src/main/java/com/musclesOS/adil/ui/onboarding/fragment/FocusAreaFragment.kt)
- Inject `OnboardingViewModel` using `activityViewModels()`.
- Update `onClickListener` to save `selectedIds` before navigation.

#### [MODIFY] [GoalFragment.kt](file:///Users/adilkhan/AndroidStudioProjects/MuscleOS/app/src/main/java/com/musclesOS/adil/ui/onboarding/fragment/GoalFragment.kt)
- Inject `OnboardingViewModel` using `activityViewModels()`.
- Update `setupContinueButton` to save `selectedGoalIds` and `customGoal` (if any) before navigation.

## Verification Plan

### Automated Tests
- Build the project to ensure no type mismatches.

### Manual Verification
- Deploy to device/emulator.
- Walk through the onboarding flow.
- Verify (via logs or debugger) that the `OnboardingViewModel` holds the correct data at the end of the flow.
