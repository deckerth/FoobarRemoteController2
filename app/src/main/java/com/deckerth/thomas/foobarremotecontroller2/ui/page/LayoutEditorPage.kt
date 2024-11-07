package com.deckerth.thomas.foobarremotecontroller2.ui.page

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.LayoutField
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.LayoutViewModel
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable


@Composable
fun LayoutEditorPage(
    modifier: Modifier = Modifier,
    vm: LayoutViewModel
) {
    val state = rememberReorderableLazyListState(
        onMove = vm::moveField,
        canDragOver = vm::isFieldDraggable,
        onDragEnd = vm::onDragEnd
    )
    LazyColumn(
        state = state.listState,
        modifier = modifier.reorderable(state)
    ) {
        items(vm.layoutFields.value, { item -> item.key }) { item ->
            ReorderableItem(state, item.key) { dragging ->
                val elevation = animateDpAsState(if (dragging) 8.dp else 0.dp)
                if (item.isSectionTitle) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Text(
                            text = item.sectionTitle,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                } else {
                    var isClicked by remember { mutableStateOf(false) }
                    Column(
                        modifier = Modifier
                            .detectReorderAfterLongPress(state)
                            .shadow(elevation.value)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { isClicked = true }
                    ) {
                        Text(
                            text = item.layoutItem!!.item.text,
                            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        )
                        Text(
                            text = item.layoutItem.verbose(),
                            modifier = Modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                            maxLines = 2,
                            style = MaterialTheme.typography.bodySmall
                        )
                        HorizontalDivider()
                    }
                    if (isClicked) {
                        EditProperties(item,
                            onDismiss = { dirty: Boolean ->
                                if (dirty) {
                                    vm.saveChanges()
                                }
                                isClicked = false
                            })
                    }
                }
            }
        }
    }
}

@Composable
fun EditProperties(
    layoutField: LayoutField,
    onDismiss: (Boolean) -> Unit
) {
    var dirty by remember { mutableStateOf(false) }
    var italic by remember { mutableStateOf(layoutField.layoutItem!!.italic) }
    var bold by remember { mutableStateOf(layoutField.layoutItem!!.bold) }
    var itemSize by remember { mutableStateOf(layoutField.layoutItem!!.itemSize) }

    AlertDialog(
        onDismissRequest = { },
        dismissButton = {
            TextButton(onClick = { onDismiss(false) }) {
                Text(
                    stringResource(android.R.string.cancel),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onDismiss(dirty) }) {
                Text(
                    stringResource(android.R.string.ok),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        title = {
            Text(
                text = "Font",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        italic,
                        onCheckedChange = { italic = !italic; dirty = true; layoutField.layoutItem!!.italic = italic })

                    Text("Italic")
                    Checkbox(
                        bold,
                        onCheckedChange = { bold = !bold; dirty = true; layoutField.layoutItem!!.bold = bold })
                    Text("Bold")
                }
                TextField(value = itemSize.text,onValueChange = { }, readOnly = true)
            }
        },
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    )
}
