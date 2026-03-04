package com.wades.launcher.core.data.search

import com.wades.launcher.core.domain.model.AppInfo
import com.wades.launcher.core.domain.model.MatchType

interface SearchMatcher {
    fun match(app: AppInfo, keyword: String): SearchMatchResult?
}

data class SearchMatchResult(
    val matchType: MatchType,
    val score: Float,
)
