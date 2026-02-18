package org.pixiemays.telegramWhitelist

import org.slf4j.Logger
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

class TelegramBot(
    private val config: PluginConfig,
    private val logger: Logger,
    private val onCommand: (nick: String) -> Boolean
) : TelegramLongPollingBot(config.botToken) {

    override fun getBotUsername(): String = "TelegramWhitelist"

    override fun onUpdateReceived(update: Update) {
        if (!update.hasMessage()) return
        val message = update.message
        if (!message.hasText()) return
        if (!message.isUserMessage) return

        val senderId = message.from.id.toLong()
        val chatId = message.chatId.toString()
        val text = message.text.trim()

        if (senderId !in config.allowedUsers) {
            reply(chatId, "❌ You are not allowed.")
            logger.warn("An unauthorized user tried to use a bot: $senderId")
            return
        }

        val nickRegex = Regex("^[a-zA-Z0-9_]{3,32}$")
        if (!nickRegex.matches(text)) {
            reply(chatId, "Incorrect nick.\nOnly send player nick (3–16 chars, a-z, 0-9, _).")
            return
        }

        val nick = text
        logger.info("user $senderId request to add: $nick")

        val success = onCommand(nick)

        if (success) {
            reply(chatId, "Player added: ${nick}")
        } else {
            reply(chatId, "ERROR. Check console.")
        }
    }

    private fun reply(chatId: String, text: String) {
        try {
            execute(
                SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .parseMode("Markdown")
                    .build()
            )
        } catch (e: TelegramApiException) {
            logger.error("Cannot to sent message in Telegram: ${e.message}")
        }
    }
}