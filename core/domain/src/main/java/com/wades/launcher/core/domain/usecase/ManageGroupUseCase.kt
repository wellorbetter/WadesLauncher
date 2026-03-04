package com.wades.launcher.core.domain.usecase

import com.wades.launcher.core.domain.model.LauncherGroup
import com.wades.launcher.core.domain.repository.LayoutRepository
import kotlinx.coroutines.flow.first

class ManageGroupUseCase(
    private val layoutRepository: LayoutRepository,
) {
    suspend fun createGroup(name: String, sortOrder: Int): LauncherGroup {
        require(name.isNotBlank()) { "Group name must not be blank" }
        require(sortOrder >= 0) { "Sort order must be non-negative" }
        val group = LauncherGroup(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            sortOrder = sortOrder,
        )
        layoutRepository.saveGroup(group)
        return group
    }

    suspend fun renameGroup(groupId: String, newName: String) {
        require(newName.isNotBlank()) { "Group name must not be blank" }
        val layout = layoutRepository.observeLayout().first()
        val group = layout.groups.find { it.id == groupId }
            ?: error("Group $groupId not found")
        layoutRepository.updateGroup(group.copy(name = newName))
    }

    suspend fun deleteGroup(groupId: String) {
        layoutRepository.deleteGroup(groupId)
    }

    suspend fun addAppToGroup(groupId: String, packageName: String) {
        layoutRepository.addAppToGroup(groupId, packageName)
    }

    suspend fun removeAppFromGroup(groupId: String, packageName: String) {
        layoutRepository.removeAppFromGroup(groupId, packageName)
    }

    suspend fun moveApp(fromGroupId: String, toGroupId: String, packageName: String) {
        layoutRepository.moveApp(fromGroupId, toGroupId, packageName)
    }

    suspend fun reorderGroups(groupIds: List<String>) {
        layoutRepository.reorderGroups(groupIds)
    }
}
