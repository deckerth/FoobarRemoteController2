package com.deckerth.thomas.foobarremotecontroller2.ui.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ViewsWithLayout
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel

@Composable
fun LayoutSelection(appViewModel: AppViewModel) {
    Column {
        Spacer(Modifier.height(40.dp))
        PageTitle("Layouts")
        Title(stringResource(R.string.layout_item_title))
        PreferenceItem<Boolean>(title = stringResource(R.string.choose_player_layout), summary = "",
            onClick = {
                appViewModel.selectedView = ViewsWithLayout.PLAYER
                mainActivity!!.navigateTo("Layout editor")
            })

        Title(stringResource(R.string.layout_playlist))
        PreferenceItem<Boolean>(title = stringResource(R.string.choose_album_layout), summary = "",
            onClick = {
                appViewModel.selectedView = ViewsWithLayout.ALBUM
                mainActivity!!.navigateTo("Layout editor")
            })
        PreferenceItem<Boolean>(title = stringResource(R.string.choose_title_layout), summary = "",
            onClick = {
                appViewModel.selectedView = ViewsWithLayout.TITLE
                mainActivity!!.navigateTo("Layout editor")
            })

    }
}

@Preview(
    showBackground = true
)
@Composable
fun LayoutSelectionPreview() {
    LayoutSelection(AppViewModel())
}
