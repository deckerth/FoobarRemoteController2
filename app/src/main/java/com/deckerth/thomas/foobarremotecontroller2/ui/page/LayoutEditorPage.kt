package com.deckerth.thomas.foobarremotecontroller2.ui.page

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.burnoutcrew.reorderable.ItemPosition
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.reorderable

data class ItemData(val title: String, val key: String, val isLocked: Boolean = false)

class ReorderListViewModel : ViewModel() {
    var cats by mutableStateOf(List(500) { ItemData("Cat $it", "id$it") })
    var dogs by mutableStateOf(List(500) {
        if (it.mod(10) == 0) ItemData("Locked", "id$it", true) else ItemData("Dog $it", "id$it")
    })

    fun moveCat(from: ItemPosition, to: ItemPosition) {
        cats = cats.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    fun moveDog(from: ItemPosition, to: ItemPosition) {
        dogs = dogs.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    fun isDogDragEnabled(draggedOver: ItemPosition, dragging: ItemPosition) = dogs.getOrNull(draggedOver.index)?.isLocked != true
}

@Composable
private fun VerticalReorderList(
    modifier: Modifier = Modifier,
    vm: ReorderListViewModel = viewModel(),
) {
    val state = rememberReorderableLazyListState(onMove = vm::moveDog, canDragOver = vm::isDogDragEnabled)
    LazyColumn(
        state = state.listState,
        modifier = modifier.reorderable(state)
    ) {
        items(vm.dogs, { item -> item.key }) { item ->
            ReorderableItem(state, item.key) { dragging ->
                val elevation = animateDpAsState(if (dragging) 8.dp else 0.dp)
                if (item.isLocked) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray)
                    ) {
                        Text(
                            text = item.title,
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
                            text = item.title,
                            modifier = Modifier.padding(16.dp)
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun DragList() {
    VerticalReorderList()
}

@Composable
fun LayoutEditorPage() {

}