package dev.idzb.translitua

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction

class TranslitUAAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        val document = editor.document

        val allSelectedTexts = editor.caretModel.allCarets.mapNotNull { it.selectedText }
        val combinedText = allSelectedTexts.joinToString("")

        val validationResult = TransliterationService.validateText(combinedText)

        if (!validationResult.isValid) {
            val invalidCharsStr = validationResult.invalidChars.joinToString(", ") { "'$it'" }
            NotificationGroupManager.getInstance()
                .getNotificationGroup("TranslitUA.NotificationGroup")
                .createNotification(
                    "TranslitUA: Invalid Characters Detected",
                    "The selected text contains non-Ukrainian characters: $invalidCharsStr",
                    NotificationType.WARNING
                )
                .notify(project)
        }

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
