package com.wades.launcher.core.data.icon

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.LruCache
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconCache @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8

    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    private val diskCacheDir = File(context.cacheDir, "icon_cache")
    private val packageManager: PackageManager = context.packageManager

    init {
        diskCacheDir.mkdirs()
    }

    suspend fun getIcon(packageName: String, componentName: String): Bitmap? {
        val key = componentName

        // Level 1: Memory
        memoryCache.get(key)?.let { return it }

        // Level 2: Disk
        val diskFile = diskFileFor(key)
        if (diskFile.exists()) {
            return withContext(Dispatchers.IO) {
                val bitmap = BitmapFactory.decodeFile(diskFile.absolutePath)
                bitmap?.also { memoryCache.put(key, it) }
            }
        }

        // Level 3: PackageManager
        return withContext(Dispatchers.IO) {
            try {
                val component = ComponentName.unflattenFromString(componentName)
                    ?: return@withContext null
                val drawable = packageManager.getActivityIcon(component)
                val bitmap = drawableToBitmap(drawable)
                memoryCache.put(key, bitmap)
                saveToDisk(diskFile, bitmap)
                bitmap
            } catch (e: Exception) {
                Log.d("IconCache", "Failed to load icon for $componentName", e)
                null
            }
        }
    }

    suspend fun getAppIcon(packageName: String): Bitmap? {
        val key = "app_$packageName"

        memoryCache.get(key)?.let { return it }

        val diskFile = diskFileFor(key)
        if (diskFile.exists()) {
            return withContext(Dispatchers.IO) {
                val bitmap = BitmapFactory.decodeFile(diskFile.absolutePath)
                bitmap?.also { memoryCache.put(key, it) }
            }
        }

        return withContext(Dispatchers.IO) {
            try {
                val drawable = packageManager.getApplicationIcon(packageName)
                val bitmap = drawableToBitmap(drawable)
                memoryCache.put(key, bitmap)
                saveToDisk(diskFile, bitmap)
                bitmap
            } catch (e: Exception) {
                Log.d("IconCache", "Failed to load app icon for $packageName", e)
                null
            }
        }
    }

    fun invalidate(packageName: String) {
        // Memory: keys are componentName which starts with packageName
        memoryCache.snapshot().keys
            .filter { it.startsWith(packageName) }
            .forEach { memoryCache.remove(it) }

        // Disk: files are named by safe componentName prefix
        diskCacheDir.listFiles()
            ?.filter { it.nameWithoutExtension.startsWith(safeKey(packageName)) }
            ?.forEach { it.delete() }
    }

    private fun diskFileFor(componentName: String): File {
        return File(diskCacheDir, "${safeKey(componentName)}.webp")
    }

    private fun safeKey(name: String): String {
        return name.replace("/", "_").replace(".", "_")
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) return drawable.bitmap

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888,
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun saveToDisk(file: File, bitmap: Bitmap) {
        try {
            FileOutputStream(file).use { out ->
                val format = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
                bitmap.compress(format, 90, out)
      }
        } catch (e: Exception) {
            Log.d("IconCache", "Failed to save icon to disk", e)
        }
    }
}
