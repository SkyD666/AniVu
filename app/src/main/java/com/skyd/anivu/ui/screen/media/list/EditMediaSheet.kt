package com.skyd.anivu.ui.screen.media.list

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.skyd.anivu.R
import com.skyd.anivu.ext.fileSize
import com.skyd.anivu.ext.openWith
import com.skyd.anivu.ext.toUri
import com.skyd.anivu.model.bean.MediaGroupBean
import com.skyd.anivu.model.bean.MediaGroupBean.Companion.isDefaultGroup
import com.skyd.anivu.ui.component.dialog.DeleteWarningDialog
import com.skyd.anivu.ui.screen.feed.SheetChip
import java.io.File

@Composable
fun EditMediaSheet(
    onDismissRequest: () -> Unit,
    file: File,
    currentGroup: MediaGroupBean,
    groups: List<MediaGroupBean>,
    onDelete: (File) -> Unit,
    onGroupChange: (MediaGroupBean) -> Unit,
    openCreateGroupDialog: () -> Unit,
) {
    val context = LocalContext.current

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            InfoArea(file = file)
            Spacer(modifier = Modifier.height(20.dp))

            // Options
            OptionArea(
                onOpenWith = { file.toUri(context).openWith(context) },
                onDelete = {
                    onDelete(file)
                    onDismissRequest()
                },
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Group
            GroupArea(
                currentGroup = currentGroup,
                groups = groups,
                onGroupChange = onGroupChange,
                openCreateGroupDialog = openCreateGroupDialog,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InfoArea(file: File) {
    Row {
        Box(modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp)),
                text = file.name.orEmpty(),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val description = file.length().fileSize(LocalContext.current)
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp)),
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun OptionArea(
    deleteWarningText: String = stringResource(id = R.string.media_screen_delete_file_warning),
    onOpenWith: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    var openDeleteWarningDialog by rememberSaveable { mutableStateOf(false) }

    if (onOpenWith != null || onDelete != null) {
        Text(
            text = stringResource(id = R.string.media_options),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        FlowRow(
            modifier = Modifier.padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            overflow = FlowRowOverflow.Visible
        ) {
            if (onOpenWith != null) {
                SheetChip(
                    icon = Icons.AutoMirrored.Outlined.OpenInNew,
                    text = stringResource(id = R.string.open_with),
                    onClick = onOpenWith,
                )
            }
            if (onDelete != null) {
                SheetChip(
                    icon = Icons.Outlined.Delete,
                    iconTint = MaterialTheme.colorScheme.onError,
                    iconBackgroundColor = MaterialTheme.colorScheme.error,
                    text = stringResource(id = R.string.delete),
                    onClick = { openDeleteWarningDialog = true },
                )
            }
        }

        DeleteWarningDialog(
            visible = openDeleteWarningDialog,
            text = deleteWarningText,
            onDismissRequest = { openDeleteWarningDialog = false },
            onDismiss = { openDeleteWarningDialog = false },
            onConfirm = { onDelete?.invoke() },
        )
    }
}

@Composable
internal fun GroupArea(
    title: String = stringResource(id = R.string.media_group),
    currentGroup: MediaGroupBean,
    groups: List<MediaGroupBean>,
    onGroupChange: (MediaGroupBean) -> Unit,
    openCreateGroupDialog: () -> Unit,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
    )
    FlowRow(
        modifier = Modifier.padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        overflow = FlowRowOverflow.Visible
    ) {
        groups.forEach { group ->
            val selected = currentGroup.name == group.name &&
                    currentGroup.isDefaultGroup() == group.isDefaultGroup()
            SheetChip(
                modifier = Modifier.animateContentSize(),
                icon = if (selected) Icons.Outlined.Check else null,
                text = group.name,
                contentDescription = if (selected) stringResource(id = R.string.item_selected) else null,
                onClick = { onGroupChange(group) },
            )
        }
        SheetChip(
            icon = Icons.Outlined.Add,
            text = null,
            contentDescription = stringResource(id = R.string.media_screen_add_group),
            onClick = openCreateGroupDialog,
        )
    }
}