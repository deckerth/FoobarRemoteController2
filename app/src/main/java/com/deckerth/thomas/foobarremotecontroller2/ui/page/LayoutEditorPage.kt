package com.deckerth.thomas.foobarremotecontroller2.ui.page

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ItemSize
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ProgressBarFormat
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.StandardLayoutItems
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.TextAlignment
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ViewsWithLayout
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.getItemSizesForAlbums
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.getItemSizesForFonts
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.getProgressBarFormats
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.LayoutField
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.LayoutViewModel

@Composable
fun LayoutEditorPage(
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel,
    layoutViewModel: LayoutViewModel
) {
    // State for drag and drop
    var draggedItemId by remember { mutableStateOf<Int?>(null) }
    var draggedIndex by remember { mutableIntStateOf(-1) }
    var totalDragOffset by remember { mutableFloatStateOf(0f) }
    // IMPORTANT: lets pointerInput read the latest list without restarting the gesture
    val latestItems by rememberUpdatedState(layoutViewModel.layoutFields.value)

    // Better than a hard-coded 80.dp: measure the actual card height once.

    var measuredItemHeightPx by remember { mutableFloatStateOf(0f) }
    val spacingDp = 8.dp
    val spacingPx = with(LocalDensity.current) { spacingDp.toPx() }
    val fallbackItemHeightPx = with(LocalDensity.current) { 80.dp.toPx() }

    fun <T> List<T>.move(from: Int, to: Int): List<T> {
        if (from == to) return this
        val m = toMutableList()
        val item = m.removeAt(from)
        m.add(to, item)
        return m
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(spacingDp)
    ) {
        itemsIndexed(
            items = layoutViewModel.layoutFields.value,
            key = { _, item -> item.key }
        ) { _, item ->
            val isDragging = draggedItemId == item.key

            // "Fly over" effect
            val flyElevation by animateDpAsState(
                targetValue = if (isDragging) 24.dp else 0.dp,
                animationSpec = spring(),
                label = "flyElevation"
            )
            val flyScale by animateFloatAsState(
                targetValue = if (isDragging) 1.02f else 1f,
                animationSpec = spring(),
                label = "flyScale"
            )

            // Snap to finger while dragging; spring back when released.
            val translationY by animateFloatAsState(
                targetValue = if (isDragging) totalDragOffset else 0f,
                animationSpec = if (isDragging) snap() else spring(),
                label = "translationY"
            )
            var isClicked by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = {
                        isClicked = !isClicked && !item.isSectionTitle
                    })
                    .onSizeChanged { measuredItemHeightPx = it.height.toFloat() }
                    // Animate OTHER items when list order changes.
                    .then(if (!isDragging) Modifier.animateItem() else Modifier)
                    .zIndex(if (isDragging) 10f else 0f)
                    .scale(flyScale)
                    .shadow(
                        elevation = flyElevation,
                        shape = RoundedCornerShape(8.dp),
                        clip = false
                    )
                    .graphicsLayer { this.translationY = translationY }
                    .pointerInput(item.key, measuredItemHeightPx) {
                        if (!item.isSectionTitle)
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    draggedItemId = item.key
                                    draggedIndex = latestItems.indexOfFirst { it.key == item.key }
                                    totalDragOffset = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()

                                    // Accumulate the drag offset
                                    totalDragOffset += dragAmount.y

                                    val itemStepPx =
                                        (if (measuredItemHeightPx > 0f) measuredItemHeightPx else fallbackItemHeightPx) + spacingPx

                                    val currentItems = latestItems
                                    val from = draggedIndex
                                    if (from !in currentItems.indices) return@detectDragGesturesAfterLongPress

                                    val offsetItems = totalDragOffset / itemStepPx

                                    val to = if (offsetItems < 0)
                                        (from.toFloat() + offsetItems + 0.9f).toInt() // ceiling
                                            .coerceIn(0, currentItems.lastIndex)
                                    else
                                        (from.toFloat() + offsetItems).toInt()
                                            .coerceIn(0, currentItems.lastIndex)

                                    if (to != from && to != 0) {
                                        // Reorder DURING the drag -> other items animate via animateItemPlacement()
                                        layoutViewModel.layoutFields.value = currentItems.move(from, to)
                                        layoutViewModel.dirty.value = true
                                        draggedIndex = to
                                        // Compensate offset so the card stays under the finger after the move
                                        totalDragOffset -= (to - from) * itemStepPx
                                    }
                                },
                                onDragEnd = {
                                    draggedItemId = null
                                    totalDragOffset = 0f
                                    draggedIndex = -1
                                    layoutViewModel.saveChanges()
                                },
                                onDragCancel = {
                                    draggedItemId = null
                                    totalDragOffset = 0f
                                    draggedIndex = -1
                                }
                            )
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = if (item.isSectionTitle) RoundedCornerShape(0.dp) else RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    if (item.isSectionTitle) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column() {
                        if (item.isSectionTitle) {
                            Text(
                                text = item.sectionTitle,
                            )
                        } else if (item.layoutItem != null) {
                            Text(
                                text = item.layoutItem.getText(appViewModel),
                            )
                            if (item.layoutItem.item != StandardLayoutItems.PROGRESS || layoutViewModel.currentView != ViewsWithLayout.PLAYER)
                                Text(
                                    text = item.layoutItem.verbose(),
                                    maxLines = 2,
                                    style = MaterialTheme.typography.bodySmall
                                )
                        }
                    }
                    if (!item.isSectionTitle)
                        Text(
                            text = "⋮⋮",
                            style = MaterialTheme.typography.headlineSmall
                        )
                }
            } // end Card
            if (isClicked) {
                if (item.layoutItem!!.item == StandardLayoutItems.PROGRESS) {
                    if (layoutViewModel.currentView != ViewsWithLayout.PLAYER)
                        EditProgressBarProperties(
                            item,
                            onDismiss = { dirty: Boolean ->
                                if (dirty) {
                                    layoutViewModel.dirty.value = true
                                    layoutViewModel.saveChanges()
                                }
                                isClicked = false
                            })
                } else
                    EditProperties(
                        item,
                        onDismiss = { dirty: Boolean ->
                            if (dirty) {
                                layoutViewModel.dirty.value = true
                                layoutViewModel.saveChanges()
                            }
                            isClicked = false
                        })
            }
        } // items Indexed
    }
    if (layoutViewModel.history.value.isNotEmpty())
        Box(
            modifier = Modifier.fillMaxSize()
        )
        {
            FloatingActionButton(
                onClick = { layoutViewModel.undo() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp) // optional spacing from edges
                    .size(48.dp)
            )
            { Icon(Icons.AutoMirrored.Filled.Undo, "Undo") }
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
                text = stringResource(R.string.item_format),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                if (layoutField.layoutItem!!.item != StandardLayoutItems.ARTWORK) {
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
                        values = if (layoutField.layoutItem.item == StandardLayoutItems.ARTWORK) getItemSizesForAlbums() else getItemSizesForFonts(),
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
                            values = if (layoutField.layoutItem.item == StandardLayoutItems.ARTWORK) getItemSizesForAlbums() else getItemSizesForFonts(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProgressBarProperties(
    layoutField: LayoutField,
    onDismiss: (Boolean) -> Unit
) {
    var dirty by remember { mutableStateOf(false) }
    var progressBarFormat: ProgressBarFormat by remember { mutableStateOf(layoutField.layoutItem!!.progressBarFormat) }
    var progressBarShowTimings: Boolean by remember { mutableStateOf(layoutField.layoutItem!!.progressBarShowTimings) }
    var progressBarReplacesBackgroundColoring: Boolean by remember { mutableStateOf(layoutField.layoutItem!!.progressBarReplacesBackgroundColoring) }
    var waveSpeed: Int by remember { mutableIntStateOf(layoutField.layoutItem!!.waveSpeed) }

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
                    layoutField.layoutItem!!.progressBarFormat = progressBarFormat
                    layoutField.layoutItem.progressBarShowTimings = progressBarShowTimings
                    layoutField.layoutItem.progressBarReplacesBackgroundColoring =
                        progressBarReplacesBackgroundColoring
                    layoutField.layoutItem.waveSpeed = waveSpeed
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
                text = stringResource(R.string.item_format),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                // Format
                AlternativeDropdown(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    values = getProgressBarFormats(),
                    selectedItem = progressBarFormat,
                    label = stringResource(R.string.progress_bar_format),
                    onClick = { format: ProgressBarFormat? ->
                        if (format != null) {
                            progressBarFormat = format; dirty = true
                        }
                    },
                    getText = { format: ProgressBarFormat -> format.text })

                // Wave speed
                if (progressBarFormat != ProgressBarFormat.FLAT)
                    Column {
                        Text(
                            modifier = Modifier.padding(top = 12.dp, end = 8.dp),
                            text = stringResource(R.string.progress_bar_wave_speed)
                        )
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                modifier = Modifier.padding(top = 12.dp, start = 8.dp, end = 8.dp),
                                text = waveSpeed.toString()
                            )
                            Slider(
                                value = waveSpeed.toFloat(),
                                onValueChange = { waveSpeed = it.toInt(); dirty = true },
                                valueRange = 0f..10f,
                                steps = 11
                            )
                        }
                    }
                // Show timings
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = progressBarShowTimings,
                        onCheckedChange = { progressBarShowTimings = it; dirty = true }
                    )
                    Text(
                        modifier = Modifier
                            .padding(8.dp),
                        text = stringResource(R.string.show_progress_bar_timings),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Background coloring
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = !progressBarReplacesBackgroundColoring,
                        onCheckedChange = {
                            progressBarReplacesBackgroundColoring = !it; dirty = true
                        }
                    )
                    Text(
                        modifier = Modifier
                            .padding(8.dp),
                        text = stringResource(R.string.show_progress_background_coloring),
                        style = MaterialTheme.typography.bodyMedium
                    )
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
    Column() {
        InputChip(
            modifier = modifier.height(IntrinsicSize.Max),
            onClick = {
                expanded = !expanded
            },
            label = {
                Row(modifier = modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = getText(currentItem),
                        maxLines = 2
                    )
                }
            },
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
            DropdownMenu(expanded = true, onDismissRequest = { expanded = false }) {
                for (option in values) {
                    DropdownMenuItem(onClick = {
                        currentItem = option
                        expanded = false
                        onClick(option)
                    }, text = { Text(text = getText(option)) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> AlternativeDropdown(
    modifier: Modifier = Modifier,
    values: List<T>,
    selectedItem: T,
    onClick: (T?) -> Unit,
    label: String = "",
    getText: (T) -> String = { v -> v.toString() }
) {
    var expanded by remember { mutableStateOf(false) }
    var currentItem by remember { mutableStateOf(selectedItem) }
    currentItem = selectedItem

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = getText(currentItem),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            for (option in values) {
                DropdownMenuItem(onClick = {
                    currentItem = option
                    expanded = false
                    onClick(option)
                }, text = { Text(text = getText(option)) }
                )
            }
        }
    }
}



