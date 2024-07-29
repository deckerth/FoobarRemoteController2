package com.deckerth.thomas.foobarremotecontroller2

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.deckerth.thomas.foobarremotecontroller2.connector.PlaylistAccess
import com.deckerth.thomas.foobarremotecontroller2.model.ITitle

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import com.deckerth.thomas.foobarremotecontroller2.model.Title
import com.deckerth.thomas.foobarremotecontroller2.ui.theme.Foobar2000RemoteControllerTheme

@Composable
fun PlaylistPage() {
    Playlist(titles = list)
}



@Composable
fun TitleCard(title: ITitle) {
    var isExpanded by remember { mutableStateOf(false) }
    val surfaceColor by animateColorAsState(
        if (isExpanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
    )
    ElevatedCard(
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable { isExpanded = !isExpanded }
    ) {
        Row(modifier = Modifier.padding(all = 8.dp)) {
            AsyncImage(
                model = title.artworkUrl,
                //placeholder = painterResource(R.drawable.album),
                contentDescription = "Album Picture",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier
                    .padding(1.dp)) {
                    Text(
                        text = title.album,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = title.title,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = title.artist,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TitleCardPreview() {
    var title = Title(
        "",
        "",
        "",
        "",
        "Ibrahim Ferrer (Buena Vista Social Club Presents)",
        "Mamí Me Gustó",
        "Ibrahim Ferrer",
        "",
        "",
        "",
        "",
        "",
        "",
    )
    TitleCard(title)
}

@Composable
fun Playlist(titles: List<ITitle>) {
    LazyColumn {
        items(titles) { title ->
            TitleCard(title)
        }
    }
}

@Preview(
    showBackground = true,
)
@Composable
fun PlaylistPreview() {
    Foobar2000RemoteControllerTheme {
        var title = Title(
            "",
            "",
            "",
            "",
            "Ibrahim Ferrer (Buena Vista Social Club Presents)",
            "Mamí Me Gustó",
            "Ibrahim Ferrer",
            "",
            "",
            "",
            "",
            "",
            ""
        )
        var titles: List<ITitle> = listOf(title, title, title, title)
//        var titles = PlaylistAccess().getCurrentPlaylist();
//        if (titles == null) {
//            return@ComposePlaylistTheme
//        }
        Playlist(titles = titles)
    }
}