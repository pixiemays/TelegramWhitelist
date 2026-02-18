package org.pixiemays.telegramWhitelist;

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import org.slf4j.Logger
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import java.nio.file.Path

@Plugin(
    id = "telegramwhitelist",
    name = "TelegramWhitelist",
    version = BuildConstants.VERSION
    ,description = "plugin for add to whitelist from telegram bot"
    ,authors = ["pixiemays"]
)
class TelegramWhitelist @Inject constructor(private val server: ProxyServer,
                                            private val logger: Logger,
                                            @DataDirectory private val dataDirectory: Path
) {
    private var bot: TelegramBot? = null
    private var botSession: DefaultBotSession? = null

    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {
        val configFile = dataDirectory.resolve("config.yml").toFile()
        val defaultConfig = javaClass.classLoader.getResourceAsStream("config.yml")
            ?: error("config.yml not found")

        val config = try {
            PluginConfig.load(configFile, defaultConfig)
        } catch (e: Exception) {
            logger.error("Config load error: ${e.message}")
            return
        }

        if (config.botToken == "YOUR_BOT_TOKEN_HERE") {
            logger.error("Set bot-token in config.yml!")
            return
        }

        if (config.allowedUsers.isEmpty()) {
            logger.warn("allowed-users empty")
        }

        logger.info("loaded ${config.allowedUsers.size} allowed users.")

        server.scheduler.buildTask(this, Runnable {
            startBot(config)
        }).schedule()
    }

    private fun startBot(config: PluginConfig) {
        try {
            val telegramBot = TelegramBot(
                config = config,
                logger = logger,
                onCommand = { nick -> executeCommand(config.commandTemplate, nick) }
            )

            val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
            botsApi.registerBot(telegramBot)

            bot = telegramBot
            logger.info("Bot successful started!")
        } catch (e: TelegramApiException) {
            logger.error("Bot start error: ${e.message}")
        }
    }

    private fun executeCommand(template: String, nick: String): Boolean {
        return try {
            val command = template.replace("{nick}", nick)
            logger.info("Executing: $command")
            server.commandManager.executeAsync(server.consoleCommandSource, command)
            true
        } catch (e: Exception) {
            logger.error("Execute command error '$nick': ${e.message}")
            false
        }
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        logger.info("Bot shutdown")
        try {
            botSession?.stop()
        } catch (e: Exception) {
            logger.warn("Shutdown error: ${e.message}")
        }
    }
}
