package com.wades.launcher.core.domain.repository

import com.wades.launcher.core.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    fun observeAllApps(): Flow<List<AppInfo>>
    suspend fun getAppByPackageName(packageName: String): AppInfo?
    suspend fun updateUsageCount(packageName: String)
    suspend fun setHidden(packageName: String, hidden: Boolean)
}
