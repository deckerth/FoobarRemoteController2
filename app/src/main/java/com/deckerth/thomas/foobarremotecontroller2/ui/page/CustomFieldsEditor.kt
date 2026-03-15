package com.deckerth.thomas.foobarremotecontroller2.ui.page

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.model.CustomField
import com.deckerth.thomas.foobarremotecontroller2.ui.components.InitStyles
import com.deckerth.thomas.foobarremotecontroller2.ui.components.mediumLinkStyle
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ViewsWithLayout
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.layoutManager
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import com.deckerth.thomas.foobarremotecontroller2.ui.theme.Foobar2000RemoteControllerTheme
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel

private var errorMessage by mutableStateOf("")
private var errorWithName by mutableStateOf(false)
private var errorWithReference by mutableStateOf(false)


val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal)
)

@Composable
fun CustomFieldsEditor(vm: AppViewModel) {
    var displayCustomFieldsEditor by remember { mutableStateOf(false) }
    var askForRemovalOfCustomFieldFromLayouts by remember { mutableStateOf(false) }
    var fieldToEdit by remember { mutableStateOf<CustomField?>(null) }
    var fieldName by remember { mutableStateOf("") }

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
                            .clickable(onClick = {
                                fieldToEdit = it
                                displayCustomFieldsEditor = true
                            })
                    ) {
                        Row(Modifier.padding(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = it.fieldName,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(it.fieldReference, fontFamily = JetBrainsMono)
                            }
                            IconButton(onClick = {
                                fieldName = it.fieldName
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
            CustomFieldEditor(vm, onDismiss = {
                displayCustomFieldsEditor = false
                errorWithName = false
                errorWithReference = false
                errorMessage = ""
                fieldToEdit = null
            }, fieldToEdit)
        }
        if (askForRemovalOfCustomFieldFromLayouts) {
            val layouts = layoutManager.getViewModesWith(fieldName)
            if (!layouts.isEmpty()) {
                AskForRemovalOfCustomFieldFromLayouts(
                    fieldName, layouts,
                    onDismiss = { askForRemovalOfCustomFieldFromLayouts = false },
                    onConfirm = {
                        askForRemovalOfCustomFieldFromLayouts = false
                        vm.customFields.removeCustomField(fieldName)
                        vm.customFields.saveCustomFieldsSetting()
                        layoutManager.removeFieldFromLayouts(fieldName)
                    })
            } else {
                askForRemovalOfCustomFieldFromLayouts = false
                vm.customFields.removeCustomField(fieldName)
                vm.customFields.saveCustomFieldsSetting()
            }
            vm.customFields.saveCustomFieldsSetting()
        }
    }
}

@Composable
fun CustomFieldEditor(vm: AppViewModel, onDismiss: () -> Unit, fieldToEdit: CustomField? = null) {
    var fieldName by remember { mutableStateOf("") }
    var fieldReference by remember { mutableStateOf("") }

    InitStyles()
    val annotatedString = buildAnnotatedString {
        withLink(LinkAnnotation.Url(url = "https://wiki.hydrogenaudio.org/index.php?title=Foobar2000:Title_Formatting_Reference")) {
            withStyle(mediumLinkStyle) {
                append(stringResource(R.string.link_title_formatting_reference))
            }
        }
    }

    if (fieldToEdit != null) {
        fieldName = fieldToEdit.fieldName
        fieldReference = fieldToEdit.fieldReference
    }

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
                    if (fieldToEdit == null)
                        vm.customFields.addCustomField(fieldReference, fieldName)
                    else
                        vm.customFields.setFieldReference(fieldName, fieldReference)
                    vm.customFields.saveCustomFieldsSetting()
                    onDismiss()
                },
                enabled = !errorWithReference && !errorWithName && fieldReference.isNotEmpty() && fieldName.isNotEmpty()
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
                    onValueChange = {
                        fieldName = it; checkFieldReference(
                        vm,
                        fieldName,
                        fieldReference
                    )
                    },
                    label = { Text(stringResource(R.string.custom_field_name)) },
                    placeholder = { Text(stringResource(R.string.custom_field_contributor)) },
                    singleLine = true,
                    isError = errorWithName,
                    enabled = fieldToEdit == null
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField( // TODO: Help button with link to hydrogen docs
                    value = fieldReference,
                    onValueChange = {
                        fieldReference = it; checkFieldReference(
                        vm,
                        fieldName,
                        fieldReference
                    )
                    },
                    label = { Text(stringResource(R.string.custom_field_reference)) },
                    isError = errorWithReference,
                    placeholder = { Text("%contributor%", fontFamily = JetBrainsMono) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = JetBrainsMono
                    ),
                    singleLine = true,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
//                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.Center) {
//                    Text (annotatedString)
//                }
                Text (modifier = Modifier.padding(bottom = 8.dp), text = annotatedString)

                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }
            }

        }
    )
}

private fun checkFieldReference(
    vm: AppViewModel,
    fieldName: String,
    fieldReference: String
): Boolean {
    errorWithName = false
    errorWithReference = false
    errorMessage = ""
    if (!fieldReference.isEmpty() && vm.customFields.iaStandardField(fieldReference)) {
        errorMessage = mainActivity!!.baseContext.getString(R.string.error_standard_field)
        errorWithReference = true
        return false
    }
    if (!fieldName.isEmpty() && vm.customFields.hasField(fieldName)) {
        errorMessage = mainActivity!!.baseContext.getString(R.string.error_duplicate_field)
        errorWithName = true
        return false
    }
    return true
}

@Composable
fun AskForRemovalOfCustomFieldFromLayouts(
    fieldName: String,
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
            stringResource(R.string.view_with_field_reference, fieldName, views)
        else
            stringResource(R.string.views_with_field_reference, fieldName, views)
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

