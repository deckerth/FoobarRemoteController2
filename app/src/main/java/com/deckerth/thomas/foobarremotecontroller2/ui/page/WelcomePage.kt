@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.deckerth.thomas.foobarremotecontroller2.ui.page

import android.os.Process
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import com.deckerth.thomas.foobarremotecontroller2.ui.theme.Foobar2000RemoteControllerTheme
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomePage(vm : AppViewModel? = null) {

    WelcomePageBackPressHandler()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = mainActivity!!.appBarLabel) },
                modifier = Modifier.fillMaxWidth(),
                //colors = TopAppBarDefaults.topAppBarColors(
                //    containerColor = TopAppBarDefaults.topAppBarColors().scrolledContainerColor
                //)
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        WizardPage(vm = vm, Modifier.padding(innerPadding),
            onCancel = {
                mainActivity!!.finish()
            },
            onFinished = {
                mainActivity!!.recreate()
            })
    }
}

@Composable
fun WelcomePageBackPressHandler() {
    BackHandler {
            Process.killProcess(Process.myPid())
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun WelcomePagePreview() {
    Foobar2000RemoteControllerTheme {
        Scaffold { innerPadding ->
            Box(
                modifier = Modifier.padding(innerPadding)
            ) {
                WelcomePage()
            }
        }
    }
}
