package com.wades.launcher.core.data.search

import com.wades.launcher.core.domain.model.AppInfo
import com.wades.launcher.core.domain.model.MatchType
import javax.inject.Inject

class FuzzyMatcher @Inject constructor() : SearchMatcher {
    override fun match(app: AppInfo, keyword: String): SearchMatchResult? {
        val label = app.label.lowercase()
        val key = keyword.lowercase()

        if (label.contains(key)) {
            return SearchMatchResult(MatchType.FUZZY, 0.6f)
        }

        // Simple subsequence matching
        if (isSubsequence(key, label)) {
            return SearchMatchResult(MatchType.FUZZY, 0.4f)
        }

        return null
    }

    private fun isSubsequence(sub: String, str: String): Boolean {
        var i = 0
        for (char in str) {
            if (i < sub.length && char == sub[i]) i++
        }
        return i == sub.length
    }
}
