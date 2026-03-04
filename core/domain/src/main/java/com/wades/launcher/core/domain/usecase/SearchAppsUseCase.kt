package com.wades.launcher.core.domain.usecase

import com.wades.launcher.core.domain.model.SearchQuery
import com.wades.launcher.core.domain.model.SearchResult
import com.wades.launcher.core.domain.repository.SearchRepository

class SearchAppsUseCase(
    private val searchRepository: SearchRepository,
) {
    suspend operator fun invoke(query: SearchQuery): List<SearchResult> {
        if (query.keyword.isBlank()) return emptyList()
        return searchRepository.search(query)
    }
}
