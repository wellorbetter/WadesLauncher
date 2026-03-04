package com.wades.launcher.core.data.repository

import androidx.room.withTransaction
import com.wades.launcher.core.data.local.GroupDao
import com.wades.launcher.core.data.local.LauncherDatabase
import com.wades.launcher.core.data.mapper.toDomain
import com.wades.launcher.core.data.mapper.toEntity
import com.wades.launcher.core.domain.model.LauncherGroup
import com.wades.launcher.core.domain.model.LauncherLayout
import com.wades.launcher.core.domain.model.LayoutConfig
import com.wades.launcher.core.domain.repository.LayoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LayoutRepositoryImpl @Inject constructor(
    private val groupDao: GroupDao,
    private val database: LauncherDatabase,
) : LayoutRepository {

    override fun observeLayout(): Flow<LauncherLayout> {
        return groupDao.observeAll().map { entities ->
            val groups = entities.map { it.toDomain() }
            LauncherLayout(groups = groups, config = LayoutConfig())
        }
    }

    override suspend fun saveGroup(group: LauncherGroup) {
        groupDao.insert(group.toEntity())
    }

    override suspend fun deleteGroup(groupId: String) {
        groupDao.deleteById(groupId)
    }

    override suspend fun updateGroup(group: LauncherGroup) {
        groupDao.update(group.toEntity())
    }

    override suspend fun reorderGroups(groupIds: List<String>) {
        groupDao.reorderAll(groupIds)
    }

    override suspend fun addAppToGroup(groupId: String, packageName: String) {
        database.withTransaction {
            val entity = groupDao.getById(groupId) ?: return@withTransaction
            val current = entity.appPackageNames
                .split(",")
                .filter { it.isNotBlank() }
            if (packageName !in current) {
                val updated = (current + packageName).joinToString(",")
                groupDao.update(entity.copy(appPackageNames = updated))
            }
        }
    }

    override suspend fun removeAppFromGroup(groupId: String, packageName: String) {
        database.withTransaction {
            val entity = groupDao.getById(groupId) ?: return@withTransaction
            val updated = entity.appPackageNames
                .split(",")
                .filter { it.isNotBlank() && it != packageName }
                .joinToString(",")
            groupDao.update(entity.copy(appPackageNames = updated))
        }
    }

    override suspend fun moveApp(fromGroupId: String, toGroupId: String, packageName: String) {
        database.withTransaction {
            removeAppFromGroup(fromGroupId, packageName)
            addAppToGroup(toGroupId, packageName)
        }
    }
}
