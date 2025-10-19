package dev.idzb.translitua

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.InputStreamReader

data class AbcMap(
    val abc: String,
    val symbols: List<Symbol>
)

data class Symbol(
    val cyr: String,
    val lat: String,
    @SerializedName("latStart")
    val latStart: String?
)

object TransliterationService {
    private val abcMap: AbcMap
    private val cyrToLatMap: Map<Char, Symbol>

    private val apostrophes = setOf('\'', '\u2018', '\u2019', '`', '\u00B4', '\u02BC')

    init {
        val inputStream = TransliterationService::class.java.getResourceAsStream("/data/abcMap.json")
            ?: throw IllegalStateException("abcMap.json not found")

        val reader = InputStreamReader(inputStream)
        abcMap = Gson().fromJson(reader, AbcMap::class.java)
        reader.close()

        cyrToLatMap = abcMap.symbols.associateBy { it.cyr[0] }
    }

    private fun isAllUpperCase(text: String, start: Int): Boolean {
        var i = start
        while (i < text.length && text[i].isLetter()) {
            if (text[i].isLowerCase()) return false
            i++
        }
        return true
    }

    fun transliterate(text: String): String {
        val result = StringBuilder()
        var i = 0
        var lastWasLetter = false

        while (i < text.length) {
            val char = text[i]

            if (char == 'ь' || char == 'Ь') {
                i++
                continue
            }

            if (char in apostrophes) {
                val hasLetterBefore = i > 0 && text[i - 1].isLetter()
                val hasLetterAfter = i < text.length - 1 && text[i + 1].isLetter()

                if (hasLetterBefore && hasLetterAfter) {
                    i++
                    continue
                } else {
                    result.append(char)
                    lastWasLetter = false
                    i++
                    continue
                }
            }

            val lowerChar = char.lowercaseChar()
            val isUpper = char.isUpperCase()

            val symbol = cyrToLatMap[lowerChar]
            if (symbol != null) {
                val isWordStart = !lastWasLetter

                val latinText = if (isWordStart && symbol.latStart != null) {
                    symbol.latStart
                } else {
                    symbol.lat
                }

                val transliterated = if (isUpper) {
                    val allCaps = isWordStart && isAllUpperCase(text, i)
                    if (allCaps) {
                        latinText.uppercase()
                    } else {
                        latinText.replaceFirstChar { it.uppercase() }
                    }
                } else {
                    latinText
                }

                result.append(transliterated)
                lastWasLetter = true
            } else {
                result.append(char)
                lastWasLetter = false
            }

            i++
        }

        return result.toString()
    }
}