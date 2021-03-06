package dev.emortal.catnipbridge

import com.mewna.catnip.entity.builder.EmbedBuilder
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import dev.emortal.catnipbridge.CatnipBridgePlugin.Companion.chatWebhook
import dev.emortal.catnipbridge.CatnipBridgePlugin.Companion.joinLeaveChannel
import dev.emortal.catnipbridge.CatnipBridgePlugin.Companion.server
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.Color
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object EventListener {

    val discordScope = CoroutineScope(Dispatchers.IO)

    @Subscribe(order = PostOrder.LATE)
    fun playerChatEvent(e: PlayerChatEvent) {
        if (e.result == PlayerChatEvent.ChatResult.denied()) return

        val filteredMessage = e.message
            .replace("@", "")



        discordScope.launch {
            chatWebhook
                ?.executeWebhook(filteredMessage, e.player.username, "https://crafatar.com/renders/head/${e.player.uniqueId}?size=256&default=MHF_Question&overlay")
                ?.onErrorComplete()
        }

    }

    val loggedInPlayers = ConcurrentHashMap.newKeySet<UUID>()

    @Subscribe
    fun playerJoin(e: ServerPostConnectEvent) {
        loggedInPlayers.add(e.player.uniqueId)

        if (e.previousServer == null) {
            discordScope.launch {
                joinLeaveChannel
                    ?.sendMessage(
                        EmbedBuilder()
                            .author(e.player.username, null, "https://crafatar.com/avatars/${e.player.uniqueId}?size=128&default=MHF_Question&overlay")
                            .description("**${e.player.username}** joined the server\n${server.allPlayers.size} online")
                            .timestamp(OffsetDateTime.now())
                            .color(Color.green)
                            .build()
                    )
            }
        }

    }

    @Subscribe
    fun playerLeave(e: DisconnectEvent) {
        if (!loggedInPlayers.contains(e.player.uniqueId)) return

        discordScope.launch {
            joinLeaveChannel
                ?.sendMessage(
                    EmbedBuilder()
                        .author(e.player.username, null, "https://crafatar.com/avatars/${e.player.uniqueId}?size=128&default=MHF_Question&overlay")
                        .description("**${e.player.username}** left the server\n${server.allPlayers.size} online")
                        .timestamp(OffsetDateTime.now())
                        .color(Color.red)
                        .build()
                )
                ?.onErrorComplete()
        }

        loggedInPlayers.remove(e.player.uniqueId)
    }

}