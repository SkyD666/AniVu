package com.skyd.anivu.ui.component.html

import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.core.text.parseAsHtml

@Composable
fun HtmlText(
    modifier: Modifier = Modifier,
    htmlFlags: Int = FROM_HTML_MODE_LEGACY,
    text: String,
) {
    val context = LocalContext.current
    val textColor = LocalContentColor.current
    var componentWidth by remember { mutableIntStateOf(0) }
    AndroidView(
        modifier = modifier.onGloballyPositioned {
            componentWidth = it.size.width
        },
        factory = { c ->
            TextView(c).apply {
                movementMethod = LinkMovementMethod.getInstance()
                setTextIsSelectable(true)
                setTextColor(textColor.toArgb())
            }
        },
        update = { textView ->
            textView.text = text.parseAsHtml(
                htmlFlags,
                imageGetter = ImageGetter(
                    context = context,
                    maxWidth = { componentWidth },
                    onSuccess = { _, _ ->
                        textView.text = textView.text
                    }
                ),
                tagHandler = null,
            )
        }
    )
}