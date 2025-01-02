package com.deckerth.thomas.foobarremotecontroller2.ui.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ViewsWithLayout
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.selectedView

enum class LayoutArea {
    PLAYER,
    ALBUM,
    TITLE
}

var selectedArea : LayoutArea = LayoutArea.PLAYER

@Composable
fun CenteredButton(text: String,
                   onClick: () -> Unit,
                   modifier: Modifier = Modifier){
    Box(contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxWidth()){
        Button(onClick = { onClick() }
        ) {
            Text(text)
        }
    }
}

@Composable
fun LayoutSelection() {
    Column {
        Spacer(Modifier.height(40.dp))
        PageTitle("Layouts")
        Title(stringResource(R.string.layout_item_title))
        PreferenceItem<Boolean>(title = stringResource(R.string.choose_player_layout), summary = "",
                                onClick = {
                                    selectedView = ViewsWithLayout.PLAYER
                                    mainActivity.navigateTo("Layout editor")
                                } )

        Title(stringResource(R.string.layout_playlist))
        PreferenceItem<Boolean>(title = stringResource(R.string.choose_album_layout), summary = "",
            onClick = {
                selectedView = ViewsWithLayout.ALBUM
                mainActivity.navigateTo("Layout editor")
            } )
        PreferenceItem<Boolean>(title = stringResource(R.string.choose_title_layout), summary = "",
            onClick = {
                selectedView = ViewsWithLayout.TITLE
                mainActivity.navigateTo("Layout editor")
            } )

    }
}

@Preview(
    showBackground = true
)
@Composable
fun LayoutSelectionPreview(){
    LayoutSelection()
}
