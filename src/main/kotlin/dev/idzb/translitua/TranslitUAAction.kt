package dev.idzb.translitua

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction

class TranslitUAAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        val document = editor.document

        WriteCommandAction.runWriteCommandAction(project) {
            editor.caretModel.allCarets.forEach { caret ->
                val selectedText = caret.selectedText
                if (selectedText != null) {
                    val start = caret.selectionStart
                    val end = caret.selectionEnd
                    val transliterated = TransliterationService.transliterate(selectedText)
                    document.replaceString(start, end, transliterated)
                }
            }
        }
    }
}
