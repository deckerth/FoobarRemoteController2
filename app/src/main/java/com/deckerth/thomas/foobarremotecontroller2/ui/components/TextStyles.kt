package com.deckerth.thomas.foobarremotecontroller2.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

var stylesInitialized = false
lateinit var textStyle: SpanStyle
lateinit var linkStyle: SpanStyle
lateinit var boldTextStyle: SpanStyle
lateinit var boldLinkStyle: SpanStyle
lateinit var titleStyle: SpanStyle
lateinit var headingStyle: SpanStyle
lateinit var smallTextStyle: SpanStyle

@Composable
fun InitStyles() {
    if (stylesInitialized) return

    textStyle = SpanStyle(
        color = MaterialTheme.colorScheme.secondary,
        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
        fontWeight = MaterialTheme.typography.bodyLarge.fontWeight
    )
    smallTextStyle = SpanStyle(
        color = MaterialTheme.colorScheme.secondary,
        fontSize = MaterialTheme.typography.bodySmall.fontSize,
        fontWeight = MaterialTheme.typography.bodySmall.fontWeight
    )
    linkStyle = SpanStyle(
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline,
        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
        fontWeight = MaterialTheme.typography.bodyLarge.fontWeight
    )
    boldTextStyle = SpanStyle(
        color = MaterialTheme.colorScheme.secondary,
        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
        fontWeight = FontWeight(FontWeight.Bold.weight)
    )
    boldLinkStyle = SpanStyle(
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline,
        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
        fontWeight = FontWeight(FontWeight.Bold.weight)
    )
    titleStyle = SpanStyle(
        color = MaterialTheme.colorScheme.primary,
        fontSize = MaterialTheme.typography.titleMedium.fontSize,
        fontWeight = FontWeight(FontWeight.Bold.weight)
    )
    headingStyle = SpanStyle(
        color = MaterialTheme.colorScheme.primary,
        fontSize = MaterialTheme.typography.headlineMedium.fontSize,
        fontWeight = FontWeight(FontWeight.Bold.weight)
    )
    stylesInitialized = true
}
