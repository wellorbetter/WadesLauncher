package com.wades.launcher.core.data.mapper

import com.wades.launcher.core.data.local.GroupEntity
import com.wades.launcher.core.domain.model.GroupType
import com.wades.launcher.core.domain.model.LauncherGroup

fun GroupEntity.toDomain(): LauncherGroup = LauncherGroup(
    id = id,
    name = name,
    sortOrder = sortOrder,
    appPackageNames = if (appPackageNames.isBlank()) emptyList()
    else appPackageNames.split(",").filter { it.isNotBlank() },
    type = try { GroupType.valueOf(type) } catch (_: Exception) { GroupType.USER },
    isExpanded = isExpanded,
)

fun LauncherGroup.toEntity(): GroupEntity = GroupEntity(
    id = id,
    name = name,
    sortOrder = sortOrder,
    appPackageNames = appPackageNames.joinToString(","),
    type = type.name,
    isExpanded = isExpanded,
)
