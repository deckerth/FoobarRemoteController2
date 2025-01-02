package com.deckerth.thomas.foobarremotecontroller2.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.theme.Foobar2000RemoteControllerTheme


@Composable
fun WelcomeText(modifier: Modifier = Modifier) {
    val headingStyle = SpanStyle(
        color = MaterialTheme.colorScheme.secondary,
        fontSize = MaterialTheme.typography.titleLarge.fontSize,
        fontWeight = MaterialTheme.typography.titleLarge.fontWeight
    )
    val textStyle = SpanStyle(
        color = MaterialTheme.colorScheme.secondary,
        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
        fontWeight = MaterialTheme.typography.bodyLarge.fontWeight
    )
    val boldTextStyle = SpanStyle(
        color = MaterialTheme.colorScheme.secondary,
        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
        fontWeight = FontWeight(FontWeight.Bold.weight)
    )

    val annotatedString = buildAnnotatedString {
        withStyle(headingStyle) {
            append(stringResource(R.string.welcome_heading))
        }

        withStyle(textStyle) {
            append("\n\n" + stringResource(R.string.welcome_start) + "\n")
        }

        withStyle(textStyle) {
            append("\n" + stringResource(R.string.welcome_then_press))
        }

        withStyle(boldTextStyle) {
            append(stringResource(R.string.welcome_next))
        }

        withStyle(textStyle) {
            append(stringResource(R.string.welcome_start2))
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier
            .fillMaxWidth()
    )
}

@Composable
fun DeviceNotFoundText(modifier: Modifier = Modifier) {
    val headingStyle = SpanStyle(
        color = MaterialTheme.colorScheme.secondary,
        fontSize = MaterialTheme.typography.titleLarge.fontSize,
        fontWeight = MaterialTheme.typography.titleLarge.fontWeight
    )
    val textStyle = SpanStyle(
        color = MaterialTheme.colorScheme.secondary,
        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
        fontWeight = MaterialTheme.typography.bodyLarge.fontWeight
    )
    val linkStyle = SpanStyle(
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline,
        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
        fontWeight = MaterialTheme.typography.bodyLarge.fontWeight
    )
    val boldTextStyle = SpanStyle(
        color = MaterialTheme.colorScheme.secondary,
        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
        fontWeight = FontWeight(FontWeight.Bold.weight)
    )

    val annotatedString = buildAnnotatedString {
        withStyle(headingStyle) {
            append(stringResource(R.string.device_not_found_heading))
        }

        withStyle(textStyle) {
            append("\n\n" + stringResource(R.string.device_not_found_start))
        }

        withLink(LinkAnnotation.Url(url = "https://github.com/hyperblast/beefweb?tab=readme-ov-file#how-to-install")) {
            withStyle(linkStyle) {
                append(stringResource(R.string.device_not_found_link))
            }
        }

        withStyle(textStyle) {
            append(stringResource(R.string.device_not_found_start2) + "\n")
        }

        withStyle(textStyle) {
            append("\n" + stringResource(R.string.device_not_found_text1))
        }

        withStyle(boldTextStyle) {
            append(stringResource(R.string.device_not_found_search_again))
        }

        withStyle(textStyle) {
            append(stringResource(R.string.device_not_found_text2))
        }

        withStyle(boldTextStyle) {
            append(stringResource(R.string.welcome_next))
        }

        withStyle(textStyle) {
            append(stringResource(R.string.device_not_found_text4))
        }
    }
    Text(
        text = annotatedString,
        modifier = modifier
            .fillMaxWidth()
    )
}

@Preview(
    showBackground = true,
)
@Composable
fun WelcomePreview() {
    Foobar2000RemoteControllerTheme {
        WelcomeText()
    }
}

@Preview(
    showBackground = true,
)
@Composable
fun DeviceNotFoundPreview() {
    Foobar2000RemoteControllerTheme {
        DeviceNotFoundText()
    }
}