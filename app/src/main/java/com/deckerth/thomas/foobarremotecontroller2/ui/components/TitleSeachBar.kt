package com.deckerth.thomas.foobarremotecontroller2.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.page.DropdownSelector
import com.deckerth.thomas.foobarremotecontroller2.ui.theme.Foobar2000RemoteControllerTheme
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel

private var searchText = mutableStateOf("")
private var genre = mutableStateOf("")
private var genreSelected = mutableStateOf(false)
private var highRes = mutableStateOf(false)

@Composable
fun TitleSearchBarDialog(
    vm: AppViewModel? = null,
    modifier: Modifier = Modifier,
    onSearch: (String, String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { searchText.value = ""; onDismiss() },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(
                    stringResource(R.string.button_reset_filter),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSearch(
                    searchText.value,
                    if (genreSelected.value) genre.value else "",
                    highRes.value
                )
            }
            ) {
                Text(
                    stringResource(R.string.button_set_filter),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.filter_titles),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                TitleSearchBar(onSearch = { searchText.value = it })
                GenreDropDown(vm)
                Row {
                    Checkbox(
                        checked = highRes.value,
                        onCheckedChange = { highRes.value = it }
                    )
                    Text(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.CenterVertically),
                        text = stringResource(R.string.filter_high_res),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
    )
}

@Composable
fun TitleSearchBar(
    modifier: Modifier = Modifier,
    onClosed: () -> Unit = {},
    onSearch: (String) -> Unit,
    alwaysShowClearButton: Boolean = false
) {
    var isSearching by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchText.value,
                onValueChange = {
                    searchText.value = it
                    isSearching = it.isNotBlank()
                },
                modifier = modifier
                    .weight(1f)
                    .padding(8.dp),
                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                leadingIcon = { Icon(Icons.Filled.Search, "Search Icon") },
                trailingIcon = {
                    if (alwaysShowClearButton || searchText.value.isNotBlank())
                        IconButton(onClick = {
                            searchText.value = ""
                            isSearching = false
                            onClosed()
                        }) {
                            Icon(Icons.Filled.Clear, "Clear Icon")
                        }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    onSearch(searchText.value)
                }),
                singleLine = true,
                shape = RoundedCornerShape(32.dp),
            )
        }
    }
}

@Composable
fun GenreDropDown(vm: AppViewModel? = null, modifier: Modifier = Modifier) {
    val genres = if (vm == null)
        listOf("All Genres")
    else
        vm.displayedPlaylist!!.getGenres(addAllGenresText = true)
    if (genre.value.isBlank()) genre.value = genres[0]
    Row {
        Text(
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.CenterVertically),
            text = stringResource(R.string.filter_genre),
            style = MaterialTheme.typography.bodyMedium
        )
        DropdownSelector(
            modifier = modifier,
            values = genres,
            selectedItem = genre.value,
            onClick = {
                if (it != null) {
                    genre.value = it
                    genreSelected.value = genre.value != genres[0]
                }
            }
        )
    }

}

@Preview(
    showBackground = true,
)

@Composable
fun SearchPreview() {
    Foobar2000RemoteControllerTheme {
        TitleSearchBar(onClosed = {}, onSearch = {}, alwaysShowClearButton = true)
    }
}

@Preview(
    showBackground = true,
)

@Composable
fun SearchBoxPreview() {
    Foobar2000RemoteControllerTheme {
        TitleSearchBarDialog(onSearch = { _, _, _ -> }, onDismiss = {})
    }
}