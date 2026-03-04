package com.wades.launcher.core.data.repository

import com.wades.launcher.core.data.search.CompositeSearchEngine
import com.wades.launcher.core.domain.model.SearchQuery
import com.wades.launcher.core.domain.model.SearchResult
import com.wades.launcher.core.domain.repository.AppRepository
import com.wades.launcher.core.domain.repository.SearchRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val appRepository: AppRepository,
    private val searchEngine: CompositeSearchEngine,
) : SearchRepository {

    override suspend fun search(query: SearchQuery): List<SearchResult> {
        val apps = appRepository.observeAllApps().first()
        return searchEngine.search(apps, query)
    }
}
