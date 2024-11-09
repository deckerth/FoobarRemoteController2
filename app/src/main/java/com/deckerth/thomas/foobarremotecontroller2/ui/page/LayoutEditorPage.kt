package com.deckerth.thomas.foobarremotecontroller2.ui.page

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ItemSize
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.LayoutItems
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.TextAlignment
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.getItemSizesForAlbums
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.getItemSizesForFonts
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
                val elevation = animateDpAsState(if (dragging) 8.dp else 0.dp, label = "")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProperties(
    layoutField: LayoutField,
    onDismiss: (Boolean) -> Unit
) {
    var dirty by remember { mutableStateOf(false) }
    var italic by remember { mutableStateOf(layoutField.layoutItem!!.italic) }
    var bold by remember { mutableStateOf(layoutField.layoutItem!!.bold) }
    var itemSize by remember { mutableStateOf(layoutField.layoutItem!!.itemSize) }
    var maxLines by remember { mutableIntStateOf(layoutField.layoutItem!!.maxLines) }
    var alignment by remember { mutableStateOf(layoutField.layoutItem!!.alignment) }

    AlertDialog(
        onDismissRequest = { onDismiss(false) },
        dismissButton = {
            TextButton(onClick = { onDismiss(false) }) {
                Text(
                    stringResource(android.R.string.cancel),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (dirty) {
                    layoutField.layoutItem!!.italic = italic
                    layoutField.layoutItem.bold = bold
                    layoutField.layoutItem.itemSize = itemSize
                    layoutField.layoutItem.maxLines = maxLines
                    layoutField.layoutItem.alignment = alignment
                    onDismiss(true)
                } else
                    onDismiss(false)
            }) {
                Text(
                    stringResource(android.R.string.ok),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        title = {
            Text(
                text = if (layoutField.layoutItem!!.item == LayoutItems.ARTWORK) stringResource(R.string.item_format) else stringResource(
                    R.string.item_format
                ),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                if (layoutField.layoutItem!!.item != LayoutItems.ARTWORK) {
                    // Font settings
                    Text(
                        modifier = Modifier.padding(bottom = 4.dp),
                        text = stringResource(R.string.font_properties)
                    )

                    // Font
                    DropdownSelector(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp),
                        values = if (layoutField.layoutItem.item == LayoutItems.ARTWORK) getItemSizesForAlbums() else getItemSizesForFonts(),
                        selectedItem = itemSize,
                        onClick = { size: ItemSize? ->
                            if (size != null) {
                                itemSize = size; dirty = true
                            }
                        },
                        getText = { size: ItemSize -> size.text })

                    // Italic, Bold
                    MultiChoiceSegmentedButtonRow(
                        modifier = Modifier
                            //.width(120.dp)
                            .padding(top = 4.dp)
                    ) {
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(0, 2),
                            onCheckedChange = { italic = !italic; dirty = true },
                            checked = italic
                        )
                        {
                            Icon(
                                painter = painterResource(R.drawable.format_italic),
                                contentDescription = stringResource(R.string.desc_play),
                                modifier = Modifier
                                    .size(24.dp)
                            )
                        }
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(1, 2),
                            onCheckedChange = { bold = !bold; dirty = true },
                            checked = bold
                        )
                        {
                            Icon(
                                painter = painterResource(R.drawable.format_bold),
                                contentDescription = stringResource(R.string.desc_play),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Checkbox(
//                            italic,
//                            onCheckedChange = {
//                                italic = !italic; dirty = true
//                            })
//
//                        Text(stringResource(R.string.font_italic))
//                        Checkbox(
//                            bold,
//                            onCheckedChange = {
//                                bold = !bold; dirty = true
//                            })
//                        Text(text = stringResource(R.string.font_bold))
//                    }

                    // Alignment
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = stringResource(R.string.text_alignment)
                    )
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                        //.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
                    ) {
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(0, 3),
                            onClick = { alignment = TextAlignment.LEFT; dirty = true },
                            selected = alignment == TextAlignment.LEFT
                        )
                        {
                            Icon(
                                painter = painterResource(R.drawable.format_align_left),
                                contentDescription = stringResource(R.string.desc_play),
                                modifier = Modifier
                                    .size(24.dp)
                            )
                        }
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(1, 3),
                            onClick = { alignment = TextAlignment.CENTER; dirty = true },
                            selected = alignment == TextAlignment.CENTER
                        )
                        {
                            Icon(
                                painter = painterResource(R.drawable.format_align_center),
                                contentDescription = stringResource(R.string.desc_play),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(2, 3),
                            onClick = { alignment = TextAlignment.RIGHT; dirty = true },
                            selected = alignment == TextAlignment.RIGHT
                        )
                        {
                            Icon(
                                painter = painterResource(R.drawable.format_align_right),
                                contentDescription = stringResource(R.string.desc_play),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // No of lines
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            modifier = Modifier.padding(top = 12.dp, end = 8.dp),
                            text = stringResource(R.string.max_lines)
                        )
                        Text(
                            modifier = Modifier.padding(top = 12.dp, start = 8.dp, end = 8.dp),
                            text = maxLines.toString()
                        )
                        Slider(
                            value = maxLines.toFloat(),
                            onValueChange = { maxLines = it.toInt(); dirty = true },
                            valueRange = 1f..15f,
                            steps = 15
                        )

                    }
                } else
                // Size
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            modifier = Modifier.padding(end = 8.dp),
                            text = stringResource(R.string.size_property)
                        )
                        DropdownSelector(
                            modifier = Modifier.fillMaxWidth(),
                            values = if (layoutField.layoutItem.item == LayoutItems.ARTWORK) getItemSizesForAlbums() else getItemSizesForFonts(),
                            selectedItem = itemSize,
                            onClick = { size: ItemSize? ->
                                if (size != null) {
                                    itemSize = size; dirty = true
                                }
                            },
                            getText = { size: ItemSize -> size.text })
                    }
            }

        },
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    )
}

@Composable
fun <T> DropdownSelector(
    modifier: Modifier = Modifier,
    values: List<T>,
    selectedItem: T,
    onClick: (T?) -> Unit,
    getText: (T) -> String = { v -> v.toString() }
) {
    var expanded by remember { mutableStateOf(false) }
    var currentItem by remember { mutableStateOf(selectedItem) }
    currentItem = selectedItem
    Column {
        InputChip(
            modifier = modifier,
            onClick = {
                expanded = !expanded
            },
            label = { Text(text = getText(currentItem)) },
            selected = expanded,
            trailingIcon = {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Localized description",
                    Modifier.size(InputChipDefaults.AvatarSize)
                )
            },
        )
        if (expanded) {
            DropdownMenu(expanded = true, onDismissRequest = { onClick(null) }) {
                for (option in values) {
                    DropdownMenuItem(onClick = {
                        currentItem = option
                        expanded = false
                        onClick(option)
                    }, text = {
                        Text(text = getText(option))
                    })
                }
            }
        }
    }
}

