package com.wades.launcher.core.domain.model

data class SearchQuery(
    val keyword: String,
    val matchPinyin: Boolean = true,
    val matchFuzzy: Boolean = true,
    val maxResults: Int = 20,
)

data class SearchResult(
    val app: AppInfo,
    val matchType: MatchType,
    val score: Float,
)

enum class MatchType {
    EXACT,
    PREFIX,
    PINYIN,
    FUZZY,
}
