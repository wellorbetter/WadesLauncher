package com.wades.launcher.core.data.search

import com.wades.launcher.core.domain.model.AppInfo
import com.wades.launcher.core.domain.model.MatchType
import javax.inject.Inject

class PrefixMatcher @Inject constructor() : SearchMatcher {
    override fun match(app: AppInfo, keyword: String): SearchMatchResult? {
        val label = app.label.lowercase()
        val key = keyword.lowercase()
        return when {
            label == key -> SearchMatchResult(MatchType.EXACT, 1.0f)
            label.startsWith(key) -> SearchMatchResult(MatchType.PREFIX, 0.9f)
            else -> null
        }
    }
}
