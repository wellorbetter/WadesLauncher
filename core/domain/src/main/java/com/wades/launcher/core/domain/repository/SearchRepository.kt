package com.wades.launcher.core.domain.repository

import com.wades.launcher.core.domain.model.SearchQuery
import com.wades.launcher.core.domain.model.SearchResult

interface SearchRepository {
    suspend fun search(query: SearchQuery): List<SearchResult>
}
