package com.skyd.anivu.ui.fragment.settings.data.autodelete

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoDelete
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.skyd.anivu.R
import com.skyd.anivu.base.BaseComposeFragment
import com.skyd.anivu.ext.dataStore
import com.skyd.anivu.ext.getOrDefault
import com.skyd.anivu.model.preference.data.autodelete.AutoDeleteArticleBeforePreference
import com.skyd.anivu.model.preference.data.autodelete.AutoDeleteArticleFrequencyPreference
import com.skyd.anivu.model.preference.data.autodelete.UseAutoDeletePreference
import com.skyd.anivu.ui.component.AniVuTopBar
import com.skyd.anivu.ui.component.AniVuTopBarStyle
import com.skyd.anivu.ui.component.BannerItem
import com.skyd.anivu.ui.component.BaseSettingsItem
import com.skyd.anivu.ui.component.SwitchSettingsItem
import com.skyd.anivu.ui.component.dialog.SliderDialog
import com.skyd.anivu.ui.local.LocalAutoDeleteArticleBefore
import com.skyd.anivu.ui.local.LocalAutoDeleteArticleFrequency
import com.skyd.anivu.ui.local.LocalUseAutoDelete
import dagger.hilt.android.AndroidEntryPoint
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds


@AndroidEntryPoint
class AutoDeleteArticleFragment : BaseComposeFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = setContentBase { AutoDeleteScreen() }
}

@Composable
fun AutoDeleteScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            AniVuTopBar(
                style = AniVuTopBarStyle.Large,
                scrollBehavior = scrollBehavior,
                title = { Text(text = stringResource(R.string.auto_delete_screen_name)) },
            )
        }
    ) { paddingValues ->
        val useAutoDelete = LocalUseAutoDelete.current
        var openAutoDeleteFrequencyDialog by rememberSaveable { mutableStateOf(false) }
        var openAutoDeleteBeforeDialog by rememberSaveable { mutableStateOf(false) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = paddingValues,
        ) {
            item {
                BannerItem {
                    SwitchSettingsItem(
                        imageVector = Icons.Outlined.AutoDelete,
                        text = stringResource(id = R.string.enable),
                        checked = useAutoDelete,
                        onCheckedChange = {
                            UseAutoDeletePreference.put(
                                context = context, scope = scope, value = it,
                            )
                        }
                    )
                }
            }
            item {
                val autoDeleteArticleFrequency = LocalAutoDeleteArticleFrequency.current
                BaseSettingsItem(
                    icon = rememberVectorPainter(Icons.Outlined.Timer),
                    text = stringResource(id = R.string.auto_delete_article_fragment_delete_frequency),
                    descriptionText = AutoDeleteArticleFrequencyPreference.toDisplayNameMilliseconds(
                        context,
                        autoDeleteArticleFrequency,
                    ),
                    onClick = { openAutoDeleteFrequencyDialog = true },
                    enabled = useAutoDelete,
                )
            }
            item {
                val autoDeleteArticleBefore = LocalAutoDeleteArticleBefore.current
                BaseSettingsItem(
                    icon = painterResource(id = R.drawable.ic_calendar_clock_24),
                    text = stringResource(id = R.string.auto_delete_article_fragment_delete_before),
                    descriptionText = AutoDeleteArticleBeforePreference.toDisplayNameMilliseconds(
                        context,
                        autoDeleteArticleBefore,
                    ),
                    onClick = { openAutoDeleteBeforeDialog = true },
                    enabled = useAutoDelete,
                )
            }
        }

        if (openAutoDeleteFrequencyDialog) {
            AutoDeleteFrequencyDialog(
                onDismissRequest = { openAutoDeleteFrequencyDialog = false },
                onConfirm = {
                    AutoDeleteArticleFrequencyPreference.put(
                        context, scope, it.inWholeMilliseconds,
                    )
                    openAutoDeleteFrequencyDialog = false
                }
            )
        }
        if (openAutoDeleteBeforeDialog) {
            AutoDeleteBeforeDialog(
                onDismissRequest = { openAutoDeleteBeforeDialog = false },
                onConfirm = {
                    AutoDeleteArticleBeforePreference.put(
                        context, scope, it.inWholeMilliseconds,
                    )
                    openAutoDeleteBeforeDialog = false
                }
            )
        }
    }
}

@Composable
private fun AutoDeleteFrequencyDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (Duration) -> Unit,
) {
    val context = LocalContext.current
    var frequencyDay by rememberSaveable {
        mutableLongStateOf(
            context.dataStore
                .getOrDefault(AutoDeleteArticleFrequencyPreference)
                .milliseconds
                .inWholeDays
        )
    }
    SliderDialog(
        onDismissRequest = onDismissRequest,
        value = frequencyDay.toFloat(),
        onValueChange = { frequencyDay = it.toLong() },
        valueRange = 1f..365f,
        valueLabel = {
            Text(
                text = AutoDeleteArticleFrequencyPreference.toDisplayNameDays(
                    context = context,
                    days = frequencyDay,
                ),
                style = MaterialTheme.typography.labelLarge,
            )
        },
        icon = { Icon(imageVector = Icons.Outlined.Timer, contentDescription = null) },
        title = { Text(text = stringResource(id = R.string.auto_delete_article_fragment_delete_frequency)) },
        confirmButton = {
            val enabled by remember { derivedStateOf { frequencyDay >= 1 } }
            TextButton(
                onClick = { onConfirm(frequencyDay.days) },
                enabled = enabled,
            ) {
                Text(text = stringResource(id = R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )
}

@Composable
private fun AutoDeleteBeforeDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (Duration) -> Unit,
) {
    val context = LocalContext.current
    var beforeDay by rememberSaveable {
        mutableLongStateOf(
            context.dataStore
                .getOrDefault(AutoDeleteArticleBeforePreference)
                .milliseconds
                .inWholeDays
        )
    }
    SliderDialog(
        onDismissRequest = onDismissRequest,
        value = beforeDay.toFloat(),
        onValueChange = { beforeDay = it.toLong() },
        valueRange = 1f..365f,
        valueLabel = {
            Text(
                text = AutoDeleteArticleBeforePreference.toDisplayNameDays(
                    context = context,
                    days = beforeDay,
                ),
                style = MaterialTheme.typography.labelLarge,
            )
        },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_calendar_clock_24),
                contentDescription = null,
            )
        },
        title = { Text(text = stringResource(id = R.string.auto_delete_article_fragment_delete_before)) },
        confirmButton = {
            val enabled by remember { derivedStateOf { beforeDay >= 1 } }
            TextButton(
                onClick = { onConfirm(beforeDay.days) },
                enabled = enabled,
            ) {
                Text(text = stringResource(id = R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )
}