package dev.idzb.translitua

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class AbcMap(
    val abc: String,
    val digraphs: List<Symbol>,
    val symbols: List<Symbol>
)

data class Symbol(
    val cyr: String,
    val lat: String,
    @SerializedName("latStart")
    val latStart: String?
)

data class ValidationResult(
    val isValid: Boolean,
    val invalidChars: Set<Char> = emptySet()
)

object TransliterationService {
    private val abcMap: AbcMap
    private val cyrToLatMap: Map<Char, Symbol>
    private val digraphToLatMap: Map<String, Symbol>
    private val validUkrainianChars: Set<Char>

    private val apostrophes = setOf('\'', '\u2018', '\u2019', '`', '\u00B4', '\u02BC')

    init {
        val inputStream = TransliterationService::class.java.getResourceAsStream("/data/abcMap.json")
            ?: throw IllegalStateException("abcMap.json not found")

        abcMap = inputStream.reader(Charsets.UTF_8).use { Gson().fromJson(it, AbcMap::class.java) }

        cyrToLatMap = abcMap.symbols.associateBy { it.cyr[0] }
        digraphToLatMap = abcMap.digraphs.associateBy { it.cyr }
        validUkrainianChars = abcMap.abc.toSet()
    }

    private fun isCyrillic(char: Char): Boolean =
        Character.UnicodeBlock.of(char) == Character.UnicodeBlock.CYRILLIC

    /** A word is all caps only if it has at least two letters and none of them is lower case. */
    private fun isWordAllUpperCase(text: String, start: Int): Boolean {
        var letters = 0
        var i = start
        while (i < text.length && (text[i].isLetter() || text[i] in apostrophes)) {
            if (text[i].isLetter()) {
                if (text[i].isLowerCase()) return false
                letters++
            }
            i++
        }
        return letters > 1
    }

    private fun applyCase(latin: String, isUpper: Boolean, wordIsAllCaps: Boolean): String = when {
        !isUpper -> latin
        wordIsAllCaps -> latin.uppercase()
        else -> latin.replaceFirstChar { it.uppercase() }
    }

    fun validateText(text: String): ValidationResult {
        val invalidChars = mutableSetOf<Char>()

        for (char in text) {
            if (isCyrillic(char) && char !in validUkrainianChars) {
                invalidChars.add(char)
            }
        }

        return ValidationResult(
            isValid = invalidChars.isEmpty(),
            invalidChars = invalidChars
        )
    }

    fun transliterate(text: String): String {
        val result = StringBuilder()
        var i = 0
        var lastWasLetter = false
        var wordIsAllCaps = false

        while (i < text.length) {
            val char = text[i]

            if (char == 'ь' || char == 'Ь') {
                i++
                continue
            }

            if (char in apostrophes) {
                val insideWord = i > 0 && text[i - 1].isLetter() &&
                    i < text.length - 1 && text[i + 1].isLetter()

                if (!insideWord) {
                    result.append(char)
                    lastWasLetter = false
                }
                i++
                continue
            }

            val isWordStart = !lastWasLetter
            if (isWordStart) {
                wordIsAllCaps = isWordAllUpperCase(text, i)
            }

            val digraph = if (i + 1 < text.length) {
                digraphToLatMap[text.substring(i, i + 2).lowercase()]
            } else {
                null
            }
            val symbol = digraph ?: cyrToLatMap[char.lowercaseChar()]

            if (symbol == null) {
                result.append(char)
                lastWasLetter = false
                i++
                continue
            }

            val latinText = if (isWordStart && symbol.latStart != null) symbol.latStart else symbol.lat
            result.append(applyCase(latinText, char.isUpperCase(), wordIsAllCaps))
            lastWasLetter = true
            i += symbol.cyr.length
        }

        return result.toString()
    }
}
