package com.skyd.anivu.base.mvi

import android.os.Bundle

/**
 * Immutable object which contains all the required information to render a [MviView].
 */
interface MviViewState

/**
 * An interface that converts a [MviViewState] to a [Bundle] and vice versa.
 */
interface MviViewStateSaver<S : MviViewState> {
    fun S.toBundle(): Bundle
    fun restore(bundle: Bundle?): S
}
