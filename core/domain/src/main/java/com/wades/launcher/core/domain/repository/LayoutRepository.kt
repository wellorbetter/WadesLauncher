package com.wades.launcher.core.domain.repository

import com.wades.launcher.core.domain.model.LauncherGroup
import com.wades.launcher.core.domain.model.LauncherLayout
import kotlinx.coroutines.flow.Flow

interface LayoutRepository {
    fun observeLayout(): Flow<LauncherLayout>
    suspend fun saveGroup(group: LauncherGroup)
    suspend fun updateGroup(group: LauncherGroup)
    suspend fun deleteGroup(groupId: String)
    suspend fun reorderGroups(groupIds: List<String>)
    suspend fun addAppToGroup(groupId: String, packageName: String)
    suspend fun removeAppFromGroup(groupId: String, packageName: String)
    /** Atomically moves [packageName] from [fromGroupId] to [toGroupId]. */
    suspend fun moveApp(fromGroupId: String, toGroupId: String, packageName: String)
}
