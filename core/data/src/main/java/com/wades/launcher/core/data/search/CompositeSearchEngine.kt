package com.wades.launcher.core.data.search

import com.wades.launcher.core.domain.model.AppInfo
import com.wades.launcher.core.domain.model.SearchQuery
import com.wades.launcher.core.domain.model.SearchResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompositeSearchEngine @Inject constructor(
    private val prefixMatcher: PrefixMatcher,
    private val pinyinMatcher: PinyinMatcher,
    private val fuzzyMatcher: FuzzyMatcher,
) {
    fun search(apps: List<AppInfo>, query: SearchQuery): List<SearchResult> {
        val keyword = query.keyword.trim()
        if (keyword.isBlank()) return emptyList()

        val matchers = buildList {
            add(prefixMatcher)
            if (query.matchPinyin) add(pinyinMatcher)
            if (query.matchFuzzy) add(fuzzyMatcher)
        }

        return apps.mapNotNull { app ->
            matchers.firstNotNullOfOrNull { matcher ->
                matcher.match(app, keyword)?.let { result ->
                    SearchResult(app = app, matchType = result.matchType, score = result.score)
                }
            }
        }
            .sortedByDescending { it.score }
            .take(query.maxResults)
    }
}
