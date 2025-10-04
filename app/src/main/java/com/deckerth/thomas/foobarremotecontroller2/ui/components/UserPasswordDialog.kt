package com.deckerth.thomas.foobarremotecontroller2.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel

private var user by mutableStateOf("")
private var password by mutableStateOf("")

private var passwordVisible by mutableStateOf(false)

@Composable
fun UserPasswordDialog(vm: AppViewModel, modifier: Modifier = Modifier) {
    user = ""
    password = ""
    passwordVisible = false
    AlertDialog(
        onDismissRequest = {
            vm.askForPassword = false
        }, dismissButton = {
            TextButton(onClick = { vm.askForPassword = false }) {
                Text(
                    stringResource(R.string.cancel), style = MaterialTheme.typography.bodyMedium
                )
            }
        }, confirmButton = {
            TextButton(
                onClick = {
                    vm.askForPassword = false
                    if (user.isNotBlank()) {
                        vm.credentialsManager.setNewUserPassword(user, password)
                        vm.playlistsViewModel.startPlayerObserver()
                    }
                },
            ) {
                Text(
                    stringResource(R.string.connect),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }, title = {
            Text(
                text = stringResource(R.string.signInToAccessFoobar),
                style = MaterialTheme.typography.headlineSmall
            )
        }, text = {
            Column {
                OutlinedTextField(
                    value = user,
                    onValueChange = { user = it },
                    label = { Text(stringResource(R.string.user)) },
                    singleLine = true,
                    modifier = modifier
                        .padding(8.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.password)) },
                    singleLine = true,
                    // 1. Conditionally apply the visual transformation
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    // 2. Set the keyboard type for password input
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    // 3. Add the visibility toggle icon
                    trailingIcon = {
                        if (passwordVisible)
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    painter = painterResource(R.drawable.visibility_off),
                                    contentDescription = "Hide password"
                                )
                            }
                        else
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    painter = painterResource(R.drawable.visibility),
                                    contentDescription = "Show password"
                                )
                            }
                    },
                    modifier = modifier
                        .padding(8.dp)
                )
            }
        }, modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
    )
}