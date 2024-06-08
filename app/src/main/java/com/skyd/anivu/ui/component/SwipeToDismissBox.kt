package com.skyd.anivu.ui.component

import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalDensity

@Composable
fun rememberSwipeToDismissBoxState(
    vararg inputs: Any?,
    initialValue: SwipeToDismissBoxValue = SwipeToDismissBoxValue.Settled,
    confirmValueChange: (SwipeToDismissBoxValue) -> Boolean = { true },
    positionalThreshold: (totalDistance: Float) -> Float =
        SwipeToDismissBoxDefaults.positionalThreshold,
): SwipeToDismissBoxState {
    val density = LocalDensity.current
    return rememberSaveable(
        inputs = inputs,
        saver = SwipeToDismissBoxState.Saver(
            confirmValueChange = confirmValueChange,
            density = density,
            positionalThreshold = positionalThreshold
        )
    ) {
        SwipeToDismissBoxState(initialValue, density, confirmValueChange, positionalThreshold)
    }
}