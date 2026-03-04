package com.wades.launcher.core.data.search

import com.wades.launcher.core.domain.model.AppInfo
import com.wades.launcher.core.domain.model.MatchType
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import javax.inject.Inject

class PinyinMatcher @Inject constructor() : SearchMatcher {

    private val format = HanyuPinyinOutputFormat().apply {
        caseType = HanyuPinyinCaseType.LOWERCASE
        toneType = HanyuPinyinToneType.WITHOUT_TONE
    }

    override fun match(app: AppInfo, keyword: String): SearchMatchResult? {
        val key = keyword.lowercase()
        val fullPinyin = toPinyin(app.label)
        val initials = toPinyinInitials(app.label)

        return when {
            fullPinyin.startsWith(key) -> SearchMatchResult(MatchType.PINYIN, 0.85f)
            initials.startsWith(key) -> SearchMatchResult(MatchType.PINYIN, 0.8f)
            fullPinyin.contains(key) -> SearchMatchResult(MatchType.PINYIN, 0.7f)
            else -> null
        }
    }

    private fun toPinyin(text: String): String {
        return buildString {
            for (char in text) {
                val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(char, format)
                if (pinyinArray != null && pinyinArray.isNotEmpty()) {
                    append(pinyinArray[0])
                } else {
                    append(char.lowercaseChar())
                }
            }
        }
    }

    private fun toPinyinInitials(text: String): String {
        return buildString {
            for (char in text) {
                val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(char, format)
                if (pinyinArray != null && pinyinArray.isNotEmpty()) {
                    append(pinyinArray[0][0])
                } else if (char.isLetter()) {
                    append(char.lowercaseChar())
                }
            }
        }
    }
}
