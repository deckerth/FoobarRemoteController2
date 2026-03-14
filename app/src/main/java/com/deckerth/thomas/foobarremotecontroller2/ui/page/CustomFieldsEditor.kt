package com.deckerth.thomas.foobarremotecontroller2.ui.page

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ViewsWithLayout
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.layoutManager
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import com.deckerth.thomas.foobarremotecontroller2.ui.theme.Foobar2000RemoteControllerTheme
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel

private var errorMessage by mutableStateOf("")

@Composable
fun CustomFieldsEditor(navController: NavController, vm: AppViewModel) {
    var displayCustomFieldsEditor by remember { mutableStateOf(false) }
    var askForRemovalOfCustomFieldFromLayouts by remember { mutableStateOf(false) }
    var fieldReference by remember { mutableStateOf("") }
    val navBackStackEntry = remember { navController.currentBackStackEntry!! }
    val impactedViews = remember { mutableStateOf(listOf<ViewsWithLayout>()) }

    // When leaving the page, trigger data updates for views with changes regarding custom fields
    DisposableEffect(navBackStackEntry) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP || event == Lifecycle.Event.ON_DESTROY) {
                vm.triggerDataUpdatesForViews(impactedViews.value)
            }
        }

        navBackStackEntry.lifecycle.addObserver(observer)

        onDispose {
            navBackStackEntry.lifecycle.removeObserver(observer)
        }
    }
    Scaffold(
        topBar = {
        },
        bottomBar = {
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        if (vm.customFields.customFieldsList.value.customFields.isEmpty())
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.no_custom_fields_defined))
            }
        else
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = rememberLazyListState(),
                contentPadding = innerPadding
            ) {
                items(vm.customFields.customFieldsList.value.customFields) {
                    ElevatedCard(
                        modifier = Modifier
                            .animateContentSize()
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                    ) {
                        Row(Modifier.padding(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = it.fieldName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(it.fieldReference)
                            }
                            IconButton(onClick = {
                                fieldReference = it.fieldReference
                                askForRemovalOfCustomFieldFromLayouts = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete field"
                                )
                            }
                        }
                    }
                }
            }
        Box(
            modifier = Modifier.fillMaxSize()
        )
        {
            FloatingActionButton(
                onClick = { displayCustomFieldsEditor = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp) // optional spacing from edges
                    .size(48.dp)
            )
            { Icon(Icons.Default.Add, "Add field") }
        }
        if (displayCustomFieldsEditor) {
            CustomFieldEditor(vm, onDismiss = { displayCustomFieldsEditor = false })
        }
        if (askForRemovalOfCustomFieldFromLayouts) {
            val layouts = layoutManager.getViewModesWith(fieldReference)
            if (!layouts.isEmpty()) {
                AskForRemovalOfCustomFieldFromLayouts(
                    fieldReference, layouts,
                    onDismiss = { askForRemovalOfCustomFieldFromLayouts = false },
                    onConfirm = {
                        askForRemovalOfCustomFieldFromLayouts = false
                        layoutManager.removeFieldFromLayouts(fieldReference)
                        vm.customFields.removeCustomField(fieldReference)
                        vm.customFields.saveCustomFieldsSetting()
                        for (layout in layouts)
                            if (!impactedViews.value.contains(layout)) impactedViews.value += layout
                    })
            }
        }
    }
}

@Composable
fun CustomFieldEditor(vm: AppViewModel, onDismiss: () -> Unit) {
    var fieldName by remember { mutableStateOf("") }
    var fieldReference by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(
                    stringResource(R.string.button_cancel),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    vm.customFields.addCustomField(fieldReference, fieldName)
                    vm.customFields.saveCustomFieldsSetting()
                    onDismiss()
                },
                enabled = checkFieldReference(vm, fieldReference)
            ) {
                Text(
                    stringResource(R.string.button_save),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.button_custom_field),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.wrapContentSize()
            ) {
                OutlinedTextField(
                    value = fieldName,
                    onValueChange = { fieldName = it },
                    label = { Text(stringResource(R.string.custom_field_name)) },
                    placeholder = { Text(stringResource(R.string.custom_field_contributor)) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fieldReference,
                    onValueChange = { fieldReference = it },
                    label = { Text(stringResource(R.string.custom_field_reference)) },
                    isError = !checkFieldReference(
                        vm,
                        fieldReference
                    ) && errorMessage.isNotEmpty(),
                    placeholder = { Text("%contributor%") }
                )
                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }
            }

        }
    )
}

private fun checkFieldReference(vm: AppViewModel, fieldReference: String): Boolean {
    if (fieldReference.isEmpty()) return false
    if (vm.customFields.iaStandardField(fieldReference)) {
        errorMessage = mainActivity!!.baseContext.getString(R.string.error_standard_field)
        return false
    }
    if (vm.customFields.hasField(fieldReference)) {
        errorMessage = mainActivity!!.baseContext.getString(R.string.error_duplicate_field)
        return false
    }
    errorMessage = ""
    return true
}

@Composable
fun AskForRemovalOfCustomFieldFromLayouts(
    fieldReference: String,
    layouts: List<ViewsWithLayout>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit = {}
) {
    if (layouts.isEmpty()) return
    var views = layouts[0].getText(layouts[0])
    for (i in 1 until layouts.size)
        views += ", " + layouts[i].getText(layouts[i])

    val usages =
        if (layouts.size == 1)
            stringResource(R.string.view_with_field_reference, fieldReference, views)
        else
            stringResource(R.string.views_with_field_reference, fieldReference, views)
    val question = stringResource(R.string.ask_for_removal_of_custom_field_from_layouts)

    AlertDialog(
        onDismissRequest = { onDismiss() },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(
                    stringResource(R.string.cancel),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                }
            ) {
                Text(
                    stringResource(R.string.button_delete),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        title = { Text(question) },
        text = { Text(text = usages) }
    )
}

@Preview(
    showBackground = true,
)
@Composable
fun QuestionRemoveFieldPreview() {
    Foobar2000RemoteControllerTheme {
        AskForRemovalOfCustomFieldFromLayouts(
            "%contributor%",
            listOf(ViewsWithLayout.PLAYER, ViewsWithLayout.ALBUM),
            { },
            { })
    }
}

