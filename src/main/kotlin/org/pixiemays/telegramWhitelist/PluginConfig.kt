package org.pixiemays.telegramWhitelist

import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.InputStream

data class PluginConfig(
    val botToken: String,
    val allowedUsers: Set<Long>,
    val commandTemplate: String
) {
    companion object {
        fun load(configFile: File, defaultStream: InputStream): PluginConfig {
            if (!configFile.exists()) {
                configFile.parentFile.mkdirs()
                defaultStream.use { input ->
                    configFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }

            val yaml = Yaml()
            val data: Map<String, Any> = configFile.inputStream().use { yaml.load(it) }

            val botToken = data["bot-token"] as? String
                ?: error("bot-token not set in config.yml")

            @Suppress("UNCHECKED_CAST")
            val rawUsers = data["allowed-users"] as? List<*> ?: emptyList<Any>()
            val allowedUsers = rawUsers.mapNotNull {
                when (it) {
                    is Int  -> it.toLong()
                    is Long -> it
                    is String -> it.toLongOrNull()
                    else -> null
                }
            }.toSet()

            val commandTemplate = (data["command-template"] as? String) ?: "swl add {nick}"

            return PluginConfig(
                botToken = botToken,
                allowedUsers = allowedUsers,
                commandTemplate = commandTemplate
            )
        }
    }
}