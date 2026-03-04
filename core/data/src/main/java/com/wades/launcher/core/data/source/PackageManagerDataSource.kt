package com.wades.launcher.core.data.source

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import com.wades.launcher.core.domain.model.AppInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackageManagerDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val packageManager: PackageManager = context.packageManager
    private val pinyinFormat = HanyuPinyinOutputFormat().apply {
        caseType = HanyuPinyinCaseType.LOWERCASE
        toneType = HanyuPinyinToneType.WITHOUT_TONE
    }

    fun queryLauncherApps(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return packageManager.queryIntentActivities(intent, 0)
            .map { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo
                val label = resolveInfo.loadLabel(packageManager).toString()
                val installTime = try {
                    packageManager.getPackageInfo(activityInfo.packageName, 0).firstInstallTime
                } catch (_: Exception) {
                    0L
                }
                AppInfo(
                    packageName = activityInfo.packageName,
                    label = label,
                    componentName = "${activityInfo.packageName}/${activityInfo.name}",
                    sortKey = toPinyinSortKey(label),
                    installedTimestamp = installTime,
                )
            }
            .sortedBy { it.sortKey }
    }

    private fun toPinyinSortKey(text: String): String {
        val sb = StringBuilder()
        for (ch in text) {
            if (ch.code > 0x4E00 && ch.code < 0x9FFF) {
                val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(ch, pinyinFormat)
                if (pinyinArray != null && pinyinArray.isNotEmpty()) {
                    sb.append(pinyinArray[0])
                } else {
                    sb.append(ch)
                }
            } else {
                sb.append(ch.lowercaseChar())
            }
        }
        return sb.toString()
    }

    fun observePackageChanges(): Flow<PackageEvent> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val packageName = intent.data?.schemeSpecificPart ?: return
                val event = when (intent.action) {
                    Intent.ACTION_PACKAGE_ADDED -> PackageEvent.Added(packageName)
                    Intent.ACTION_PACKAGE_REMOVED -> PackageEvent.Removed(packageName)
                    Intent.ACTION_PACKAGE_REPLACED -> PackageEvent.Updated(packageName)
                    else -> return
                }
                trySend(event)
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }
}

sealed interface PackageEvent {
    data class Added(val packageName: String) : PackageEvent
    data class Removed(val packageName: String) : PackageEvent
    data class Updated(val packageName: String) : PackageEvent
}
