package com.deckerth.thomas.foobarremotecontroller2.ui.page

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.model.Playlist
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ItemSize
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.LayoutDescription
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.LayoutItem
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.LayoutItems
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.getLayoutItemsFor
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.layoutManager
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.LayoutField
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.LayoutViewModel
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.selectedView
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable


@Composable
fun LayoutEditorPage(
    modifier: Modifier = Modifier,
    vm: LayoutViewModel
) {
    val state = rememberReorderableLazyListState(onMove = vm::moveField, canDragOver = vm::isFieldDraggable, onDragEnd = vm::onDragEnd)
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
                    Column(
                        modifier = Modifier
                            .detectReorderAfterLongPress(state)
                            .shadow(elevation.value)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = item.layoutItem!!.item.text,
                            modifier = Modifier.padding(16.dp)
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
