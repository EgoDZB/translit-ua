package dev.idzb.translitua

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransliterationServiceTest {

    @Test
    fun `soft sign is dropped and not reported as invalid`() {
        assertEquals("Lviv", TransliterationService.transliterate("Львів"))
        assertTrue(TransliterationService.validateText("Львів").isValid)
    }

    @Test
    fun `digraphs stay upper case inside an all caps word`() {
        assertEquals("BORSHCH", TransliterationService.transliterate("БОРЩ"))
        assertEquals("BUKH", TransliterationService.transliterate("БУХ"))
        assertEquals("KHASHCHI", TransliterationService.transliterate("ХАЩІ"))
    }

    @Test
    fun `single letter word is capitalised, not upper cased`() {
        assertEquals("Ya khochu", TransliterationService.transliterate("Я хочу"))
        assertEquals("Ye", TransliterationService.transliterate("Є"))
    }

    @Test
    fun `zgh digraph disambiguates from zh`() {
        assertEquals("Zghorany", TransliterationService.transliterate("Згорани"))
        assertEquals("Rozghon", TransliterationService.transliterate("Розгон"))
        assertEquals("ZGHORANY", TransliterationService.transliterate("ЗГОРАНИ"))
    }

    @Test
    fun `latin characters pass through without a warning`() {
        assertEquals("Lviv city", TransliterationService.transliterate("Львів city"))
        assertTrue(TransliterationService.validateText("Львів city").isValid)
    }

    @Test
    fun `non-ukrainian cyrillic is reported as invalid`() {
        val result = TransliterationService.validateText("Львовъ")
        assertEquals(false, result.isValid)
        assertEquals(setOf('ъ'), result.invalidChars)
    }

    @Test
    fun `word start forms are used only at word start`() {
        assertEquals("Yosyp", TransliterationService.transliterate("Йосип"))
        assertEquals("Yurii", TransliterationService.transliterate("Юрій"))
        assertEquals("Zinaida", TransliterationService.transliterate("Зінаїда"))
    }

    @Test
    fun `apostrophe is dropped inside a word`() {
        assertEquals("Imia", TransliterationService.transliterate("Ім'я"))
        assertEquals("bie", TransliterationService.transliterate("б'є"))
        assertEquals("BIE", TransliterationService.transliterate("Б'Є"))
    }

    @Test
    fun `official table samples`() {
        assertEquals("Alushta", TransliterationService.transliterate("Алушта"))
        assertEquals("Zhytomyr", TransliterationService.transliterate("Житомир"))
        assertEquals("Shchyrets", TransliterationService.transliterate("Щирець"))
        assertEquals("Feodosiia", TransliterationService.transliterate("Феодосія"))
        assertEquals("Yahotyn", TransliterationService.transliterate("Яготин"))
    }
}
