package com.deckerth.thomas.foobarremotecontroller2.ui.page

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.LayoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayoutEditorMainPage(
    appViewModel: AppViewModel,
    vm: LayoutViewModel = viewModel(
        factory = LayoutViewModel.Factory(appViewModel)
    )
) {
    val navController = rememberNavController()
    var editorMode by remember {
        mutableStateOf(true)
    }

    vm.currentView = appViewModel.selectedView

    Scaffold(
        topBar = {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
            ) {
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                    onClick = {
                        navController.popBackStack()
                        navController.navigate("Editor"); editorMode = true
                    },
                    selected = editorMode
                )
                {
                    Text(stringResource(R.string.edit_layout))
                }
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                    onClick = {
                        navController.popBackStack()
                        navController.navigate("Preview"); editorMode = false
                    },
                    selected = !editorMode
                )
                {
                    Text(stringResource(R.string.preview_layout))
                }
            }
        },
        bottomBar = {

        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "Editor",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("Preview") {
                LayoutPreviewPage(appViewModel = appViewModel, vm )
            }
            composable("Editor") {
                LayoutEditorPage(appViewModel = appViewModel, layoutViewModel = vm)
            }
        }

    }
}
