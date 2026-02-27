package org.pixiemays.telegramWhitelist

import org.slf4j.Logger
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

class TelegramBot(
    private val config: PluginConfig,
    private val logger: Logger,
    private val onCommand: (commandKey: String, nick: String) -> Boolean
) : TelegramLongPollingBot(config.botToken) {

    override fun getBotUsername(): String = "TelegramWhitelist"

    override fun onUpdateReceived(update: Update) {
        if (!update.hasMessage()) return
        val message = update.message
        if (!message.hasText()) return
        if (!message.isUserMessage) return

        val senderId = message.from.id
        val chatId = message.chatId.toString()
        val text = message.text.trim()

        if (senderId !in config.allowedUsers) {
            reply(chatId, "❌ You are not allowed.")
            logger.warn("Unauthorized user: $senderId")
            return
        }

        val availableKeys = config.commands.keys.joinToString(" | ")

        val parts = text.split(" ")
        if (parts.size != 2) {
            reply(chatId, "Usage: <code>&lt;action&gt; &lt;nick&gt;</code>\nActions: <code>$availableKeys</code>")
            return
        }

        val commandKey = parts[0].lowercase()
        val nick = parts[1]

        if (commandKey !in config.commands) {
            reply(chatId, "Unknown action: <code>$commandKey</code>\nAvailable: <code>$availableKeys</code>")
            return
        }

        val nickRegex = Regex("^[a-zA-Z0-9_]{3,32}$")
        if (!nickRegex.matches(nick)) {
            reply(chatId, "Incorrect nick. Only a-z, 0-9, _ (3–32 chars).")
            return
        }

        logger.info("User ${message.from.userName}($senderId): $commandKey $nick")

        val success = onCommand(commandKey, nick)
        if (success) {
            reply(chatId, "Done: <code>$commandKey $nick</code>")
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
                    .parseMode("HTML")
                    .build()
            )
        } catch (e: TelegramApiException) {
            logger.error("Cannot to sent message in Telegram: ${e.message}")
        }
    }
}