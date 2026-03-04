package com.wades.launcher.core.domain.usecase

import com.wades.launcher.core.domain.model.AppInfo
import com.wades.launcher.core.domain.model.LauncherGroup
import com.wades.launcher.core.domain.model.LauncherLayout
import com.wades.launcher.core.domain.repository.AppRepository
import com.wades.launcher.core.domain.repository.LayoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

data class GroupedApps(
    val group: LauncherGroup,
    val apps: List<AppInfo>,
)

class GetGroupedAppsUseCase(
    private val appRepository: AppRepository,
    private val layoutRepository: LayoutRepository,
) {
    operator fun invoke(): Flow<List<GroupedApps>> {
        return combine(
            appRepository.observeAllApps(),
            layoutRepository.observeLayout().distinctUntilChanged(),
        ) { apps, layout ->
            buildGroupedApps(apps, layout)
        }
    }

    private fun buildGroupedApps(
        apps: List<AppInfo>,
        layout: LauncherLayout,
    ): List<GroupedApps> {
        val visibleApps = apps.filter { !it.isHidden }
        val appMap = visibleApps.associateBy { it.packageName }
        val assignedPackages = layout.groups.flatMap { it.appPackageNames }.toSet()

        val grouped = layout.groups
            .sortedBy { it.sortOrder }
            .map { group ->
                GroupedApps(
                    group = group,
                    apps = group.appPackageNames.mapNotNull { appMap[it] },
                )
            }

        val unassigned = visibleApps.filter { it.packageName !in assignedPackages }
        return if (unassigned.isEmpty()) {
            grouped
        } else {
            grouped + GroupedApps(
                group = LauncherGroup(
                    id = "uncategorized",
                    name = "全部应用",
                    sortOrder = Int.MAX_VALUE,
                ),
                apps = unassigned,
            )
        }
    }
}
