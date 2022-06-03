package dev.emortal.catnipbridge

import com.google.inject.Inject
import com.mewna.catnip.Catnip
import com.mewna.catnip.CatnipOptions
import com.mewna.catnip.entity.channel.TextChannel
import com.mewna.catnip.entity.channel.Webhook
import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.entity.user.Presence
import com.mewna.catnip.shard.DiscordEvent
import com.mewna.catnip.shard.GatewayIntent
import com.mewna.catnip.shard.LifecycleEvent
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import dev.emortal.catnipbridge.EventListener.discordScope
import dev.emortal.catnipbridge.config.ConfigHelper
import dev.emortal.catnipbridge.config.CatnipBridgeConfig
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import java.awt.Color
import java.nio.file.Path
import java.time.Duration
import java.util.logging.Logger

@Plugin(
    id = "catnipbridge",
    name = "CatnipBridge",
    version = "1.0.0",
    description = "Discord <-> Minecraft chat integration",
    dependencies = []
)
class CatnipBridgePlugin @Inject constructor(private val server: ProxyServer, private val logger: Logger) {

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        config = ConfigHelper.initConfigFile(configPath, CatnipBridgeConfig())
        plugin = this

        Companion.server = server

        server.eventManager.register(this, EventListener)

        if (config.botToken.isEmpty()) return

        discordScope.launch {
            val options = CatnipOptions(config.botToken)
                .initialPresence(Presence.of(Presence.OnlineStatus.ONLINE, Presence.Activity.of(config.status, Presence.ActivityType.PLAYING)))
                .intents(setOf(GatewayIntent.GUILDS, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_WEBHOOKS))

            Catnip.catnipAsync(options).subscribe { catnip ->
                CatnipBridgePlugin.catnip = catnip

                catnip.observable<Message>(DiscordEvent.MESSAGE_CREATE)
                    .filter { msg -> msg.channelId() == config.chatChannelID && !msg.author().bot() }
                    .subscribe { msg ->

                        val highestRole = msg.member()?.orderedRoles()?.lastOrNull()

                        val name = Component.text()

                        if (highestRole != null) {
                            name.append(Component.text(highestRole.name(), TextColor.color(highestRole.color()), TextDecoration.BOLD))
                            name.append(Component.space())
                        }
                        name.append(Component.text(msg.author().username(), NamedTextColor.GRAY))

                        val component = Component.text()
                            .append(Component.text("DISCORD", TextColor.color(114, 137, 218), TextDecoration.BOLD))
                            .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
                            .append(name)
                            .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                            .append(Component.text(msg.content(), NamedTextColor.GRAY))
                            .build()

                        server.allPlayers.forEach {
                            it.sendMessage(component)
                        }
                    }

                catnip.connect()

                // Event fires when cache is collected
                catnip.observable(LifecycleEvent.CHUNKING_DONE).subscribe {
                    logger.info("Chunking done")
                    catnip.cache().channel(config.guildID, config.playerJoinLeaveChannelID).subscribe {
                        joinLeaveChannel = it.asTextChannel()
                        logger.info("Join leave channel found")
                    }
                }

                catnip.observable(DiscordEvent.READY)
                    .subscribe {
                        logger.info("CatnipBridge ready")

                        catnip.rest().webhook().getWebhook(config.chatWebhookID).subscribe { webhook ->
                            chatWebhook = webhook
                            logger.info("Webhook found")
                        }


                    }
            }
        }



        logger.info("[CatnipBridge] Catnip collected")
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        catnip.shutdown()
    }

    companion object {
        lateinit var server: ProxyServer
        lateinit var plugin: CatnipBridgePlugin

        lateinit var catnip: Catnip

        var chatWebhook: Webhook? = null
        var joinLeaveChannel: TextChannel? = null

        lateinit var config: CatnipBridgeConfig
        val configPath = Path.of("./catnipconfig.json")
    }

}