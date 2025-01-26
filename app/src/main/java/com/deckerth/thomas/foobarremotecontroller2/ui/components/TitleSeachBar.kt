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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleSearchBarWithResultList(
    modifier: Modifier = Modifier,
    onClosed: () -> Unit,
    onSearch: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var active by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
    ) {
        SearchBar(
            query = searchText,
            onQueryChange = { searchText = it },
            active = active,
            onActiveChange = { active = it },
            onSearch = { onSearch(searchText) },
            placeholder = { Text("Search...") },
            leadingIcon = { Icon(Icons.Filled.Search, "Search Icon") },
            trailingIcon = {
                if (isSearching) {
                    IconButton(onClick = {
                        searchText = ""
                        isSearching = false
                        onClosed()
                    }) {
                        Icon(Icons.Filled.Clear, "Clear Icon")
                    }
                }
            },
            content = { })
    }
}


/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleSearchBar2() {

    val textFieldState = rememberTextFieldState()
    var expanded by remember { mutableStateOf(false) }

    Box(Modifier
        .fillMaxSize()
        .semantics { isTraversalGroup = true }) {
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .semantics { traversalIndex = 0f },
            inputField = {
                SearchBarDefaults.InputField(
                    state = textFieldState,
                    onSearch = { expanded = false },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = { Text("Hinted search text") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = { Icon(Icons.Default.MoreVert, contentDescription = null) },
                )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                repeat(4) { idx ->
                    val resultText = "Suggestion $idx"
                    ListItem(
                        headlineContent = { Text(resultText) },
                        supportingContent = { Text("Additional info") },
                        leadingContent = { Icon(Icons.Filled.Star, contentDescription = null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier =
                        Modifier
                            .clickable {
                                textFieldState.setTextAndPlaceCursorAtEnd(resultText)
                                expanded = false
                            }
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, top = 72.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.semantics { traversalIndex = 1f },
        ) {
            val list = List(100) { "Text $it" }
            items(count = list.size) {
                Text(
                    text = list[it],
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
            }
        }
    }
}*/


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