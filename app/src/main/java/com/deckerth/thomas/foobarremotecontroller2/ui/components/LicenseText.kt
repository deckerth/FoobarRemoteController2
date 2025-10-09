package com.deckerth.thomas.foobarremotecontroller2.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.theme.Foobar2000RemoteControllerTheme

@Composable
fun LicenseText(modifier: Modifier = Modifier) {
    InitStyles()

    val annotatedString = buildAnnotatedString {
        withStyle(boldTextStyle) {
            append(stringResource(R.string.copyright_text) + "\n\n")
        }
        withStyle(textStyle) {
            append(stringResource(R.string.version_text) + "\n\n")
        }
        withLink(LinkAnnotation.Url(url = "https://github.com/deckerth/FoobarRemoteController2/wiki/Privacy-Statement")) {
            withStyle(boldLinkStyle) {
                append(stringResource(R.string.privacy_statement) + "\n\n")
            }
        }
        withStyle(boldTextStyle) {
            append(stringResource(R.string.third_party_text) + "\n\n")
        }
        withStyle(textStyle) {
            append(stringResource(R.string.library_usage_text))
        }
        withLink(LinkAnnotation.Url(url = "https://github.com/aclassen/ComposeReorderable/tree/main")) {
            withStyle(linkStyle) {
                append(stringResource(R.string.compose_reorderable) + "\n\n")
            }
        }
        withStyle(textStyle) {
            append(stringResource(R.string.license_text))
        }
        withLink(LinkAnnotation.Url(url = "https://www.apache.org/licenses/LICENSE-2.0")) {
            withStyle(linkStyle) {
                append(stringResource(R.string.apache_2_0) + "\n\n")
            }
        }
        withLink(LinkAnnotation.Url(url = "https://github.com/deckerth/FoobarRemoteController2/discussions")) {
            withStyle(boldLinkStyle) {
                append(stringResource(R.string.support_text) + "\n\n")
            }
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
fun LicensePreview() {
    Foobar2000RemoteControllerTheme {
        LicenseText()
    }
}
