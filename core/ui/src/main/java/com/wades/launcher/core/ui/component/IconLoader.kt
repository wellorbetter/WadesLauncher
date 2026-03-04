package com.wades.launcher.core.ui.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

typealias IconLoaderFn = suspend (packageName: String, componentName: String) -> Bitmap?
typealias AppIconLoaderFn = suspend (packageName: String) -> Bitmap?

val LocalIconLoader = staticCompositionLocalOf<IconLoaderFn> {
    { _, _ -> null }
}

val LocalAppIconLoader = staticCompositionLocalOf<AppIconLoaderFn> {
    { _ -> null }
}

@Composable
fun rememberAppIconBitmap(
    packageName: String,
    componentName: String,
): Bitmap? {
    val iconLoader = LocalIconLoader.current
    var bitmap by remember(componentName) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(componentName) {
        bitmap = iconLoader(packageName, componentName)
    }
    return bitmap
}

@Composable
fun rememberAppIcon(packageName: String): Bitmap? {
    val appIconLoader = LocalAppIconLoader.current
    var bitmap by remember(packageName) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(packageName) {
        bitmap = appIconLoader(packageName)
    }
    return bitmap
}

@Composable
fun AppIconImage(
    bitmap: Bitmap,
    modifier: Modifier = Modifier,
) {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Crop,
    )
}
