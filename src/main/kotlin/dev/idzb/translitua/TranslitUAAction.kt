package dev.idzb.translitua

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction

class TranslitUAAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabled = editor != null && editor.selectionModel.hasSelection(true)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        val document = editor.document

        val selections = editor.caretModel.allCarets.mapNotNull { caret ->
            caret.selectedText?.let { Triple(caret.selectionStart, caret.selectionEnd, it) }
        }
        if (selections.isEmpty()) return

        val validationResult = TransliterationService.validateText(selections.joinToString("") { it.third })

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
            // Replace from the end so that earlier offsets stay valid as lengths change.
            selections.sortedByDescending { it.first }.forEach { (start, end, selectedText) ->
                document.replaceString(start, end, TransliterationService.transliterate(selectedText))
            }
        }
    }
}
