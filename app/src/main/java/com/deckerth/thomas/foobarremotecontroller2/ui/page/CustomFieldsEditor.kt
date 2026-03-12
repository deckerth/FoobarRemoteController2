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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel

private var errorMessage by mutableStateOf("")

@Composable
fun CustomFieldsEditor(vm: AppViewModel) {
    var displayCustomFieldsEditor by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
        },
        bottomBar = {
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
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
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(it.fieldReference)
                        }
                        IconButton(onClick = {
                            // TODO: Check usage in layouts
                            vm.customFields.removeCustomField(it.fieldReference)
                            vm.customFields.saveCustomFieldsSetting()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete field",
                                modifier = Modifier.size(16.dp)
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