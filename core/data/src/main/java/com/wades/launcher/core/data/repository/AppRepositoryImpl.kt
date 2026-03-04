package com.wades.launcher.core.data.repository

import com.wades.launcher.core.data.local.AppUsageDao
import com.wades.launcher.core.data.source.PackageEvent
import com.wades.launcher.core.data.source.PackageManagerDataSource
import com.wades.launcher.core.domain.model.AppInfo
import com.wades.launcher.core.domain.repository.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.Closeable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepositoryImpl @Inject constructor(
    private val packageManagerDataSource: PackageManagerDataSource,
    private val appUsageDao: AppUsageDao,
) : AppRepository, Closeable {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _pmApps = MutableStateFlow<List<AppInfo>>(emptyList())

    init {
        loadApps()
        observePackageChanges()
    }

    override fun observeAllApps(): Flow<List<AppInfo>> {
        return combine(_pmApps, appUsageDao.observeAll()) { apps, usages ->
            val usageMap = usages.associateBy { it.packageName }
            apps.map { app ->
                val usage = usageMap[app.packageName]
                app.copy(
                    usageCount = usage?.usageCount ?: 0,
                    lastUsedTimestamp = usage?.lastUsedTimestamp ?: 0L,
                    isHidden = usage?.isHidden ?: false,
                )
            }.filter { !it.isHidden }
        }
    }

    override suspend fun getAppByPackageName(packageName: String): AppInfo? {
        return _pmApps.value.find { it.packageName == packageName }
    }

    override suspend fun updateUsageCount(packageName: String) {
        appUsageDao.upsertUsage(packageName, System.currentTimeMillis())
    }

    override suspend fun setHidden(packageName: String, hidden: Boolean) {
        appUsageDao.upsertHidden(packageName, hidden)
    }

    override fun close() {
        scope.cancel()
    }

    private fun loadApps() {
        scope.launch {
            _pmApps.value = packageManagerDataSource.queryLauncherApps()
        }
    }

    private fun observePackageChanges() {
        scope.launch {
            packageManagerDataSource.observePackageChanges().collect { event ->
                when (event) {
                    is PackageEvent.Added,
                    is PackageEvent.Updated,
                    is PackageEvent.Removed -> loadApps()
                }
            }
        }
    }
}
