package com.wades.launcher.core.domain.usecase

import com.wades.launcher.core.domain.model.GroupType
import com.wades.launcher.core.domain.model.LauncherGroup
import com.wades.launcher.core.domain.repository.AppRepository
import com.wades.launcher.core.domain.repository.LayoutRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.filter
import java.util.UUID

// Category → package prefixes for classification
private val CATEGORY_MAP = linkedMapOf(
    "社交" to listOf(
        "com.tencent.mm", "com.tencent.mobileqq", "com.tencent.tim",
        "com.sina.weibo", "com.twitter", "com.whatsapp", "com.facebook",
        "org.telegram", "com.instagram", "com.snapchat", "com.linkedin",
        "jp.naver.line", "com.discord",
    ),
    "影音" to listOf(
        "com.netease.cloudmusic", "com.kugou", "com.tencent.qqmusic",
        "com.spotify", "com.apple.android.music", "tv.danmaku.bili",
        "com.ss.android.ugc.aweme", "com.kuaishou", "com.youku",
        "com.tencent.qqlive", "com.iqiyi", "com.ximalaya",
    ),
    "工具" to listOf(
        "com.android.dialer", "com.android.mms", "com.android.camera",
        "com.android.settings", "com.android.calculator2", "com.android.deskclock",
        "com.google.android.apps.photos", "com.android.chrome",
        "org.mozilla.firefox", "com.UCMobile", "com.quark.browser",
        "com.android.filemanager", "com.android.calendar",
    ),
    "办公" to listOf(
        "com.alibaba.android.rimet", "com.tencent.wework",
        "com.microsoft.office", "com.google.android.apps.docs",
        "com.tencent.docs", "us.zoom.videomeetings",
    ),
    "生活" to listOf(
        "com.taobao.taobao", "com.eg.android.AlipayGphone",
        "com.autonavi.minimap", "com.ss.android.article.news",
        "com.sankuai.meituan", "me.ele", "com.jingdong",
        "com.dianping", "com.Qunar",
    ),
    "游戏" to listOf(
        "com.tencent.tmgp", "com.miHoYo", "com.netease.g",
        "com.supercell", "com.garena", "com.activision",
    ),
)

class ClassifyAppUseCase(
    private val layoutRepository: LayoutRepository,
    private val appRepository: AppRepository,
) {
    /**
     * Initialize default CATEGORY groups if none exist.
     * Matches installed apps against CATEGORY_MAP prefixes.
     */
    suspend fun initCategoryGroups() {
        val layout = layoutRepository.observeLayout().first()
        val existingCategories = layout.groups.filter { it.type == GroupType.CATEGORY }
        if (existingCategories.isNotEmpty()) return

        val allApps = appRepository.observeAllApps().first { it.isNotEmpty() }
        val installedPackages = allApps.map { it.packageName }.toSet()

        var sortOrder = 100 // offset to avoid collision with user groups
        CATEGORY_MAP.forEach { (categoryName, prefixes) ->
            val matched = installedPackages.filter { pkg ->
                prefixes.any { prefix -> pkg.startsWith(prefix) }
            }
            if (matched.isNotEmpty()) {
                val group = LauncherGroup(
                    id = UUID.randomUUID().toString(),
                    name = categoryName,
                    sortOrder = sortOrder++,
                    appPackageNames = matched,
                    type = GroupType.CATEGORY,
                )
                layoutRepository.saveGroup(group)
            }
        }
    }

    /**
     * Classify a single new app into existing CATEGORY groups.
     */
    suspend fun classifyApp(packageName: String) {
        val layout = layoutRepository.observeLayout().first()
        val categories = layout.groups.filter { it.type == GroupType.CATEGORY }

        for ((categoryName, prefixes) in CATEGORY_MAP) {
            if (prefixes.any { packageName.startsWith(it) }) {
                val group = categories.find { it.name == categoryName } ?: continue
                if (packageName !in group.appPackageNames) {
                    layoutRepository.addAppToGroup(group.id, packageName)
                }
                return
            }
        }
    }
}
