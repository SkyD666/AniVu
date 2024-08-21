package com.skyd.anivu.ui.component

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.skyd.anivu.appContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val scope = CoroutineScope(Dispatchers.Main.immediate)

fun CharSequence.showToast(duration: Int = Toast.LENGTH_SHORT) {
    scope.launch {
        val toast = Toast.makeText(appContext, this@showToast, duration)
        toast.duration = duration
        toast.show()
    }
}

@Composable
fun Toast(vararg keys: Any?, text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    LaunchedEffect(*keys) {
        text.showToast(duration)
    }
}