package dev.emortal.catnipbridge

import com.mewna.catnip.entity.builder.EmbedBuilder
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import dev.emortal.catnipbridge.CatnipBridgePlugin.Companion.chatWebhook
import dev.emortal.catnipbridge.CatnipBridgePlugin.Companion.joinLeaveChannel
import dev.emortal.catnipbridge.CatnipBridgePlugin.Companion.server
import java.awt.Color
import java.time.OffsetDateTime

object EventListener {

    @Subscribe(order = PostOrder.LATE)
    fun playerChatEvent(e: PlayerChatEvent) {
        if (e.result == PlayerChatEvent.ChatResult.denied()) return

        val filteredMessage = e.message
            .replace("@", "")

        chatWebhook.executeWebhook(filteredMessage, e.player.username, "https://crafatar.com/renders/head/${e.player.uniqueId}")
    }

    @Subscribe
    fun playerJoin(e: PlayerChooseInitialServerEvent) {
        joinLeaveChannel.sendMessage(
            EmbedBuilder()
                .author(e.player.username, null, "https://crafatar.com/avatars/${e.player.uniqueId}")
                .description("**${e.player.username}** joined the server\n${server.allPlayers.size} online")
                .timestamp(OffsetDateTime.now())
                .color(Color.green)
                .build()
        )
    }

    @Subscribe
    fun playerLeave(e: DisconnectEvent) {
        joinLeaveChannel.sendMessage(
            EmbedBuilder()
                .author(e.player.username, null, "https://crafatar.com/avatars/${e.player.uniqueId}")
                .description("**${e.player.username}** left the server\n${server.allPlayers.size} online")
                .timestamp(OffsetDateTime.now())
                .color(Color.red)
                .build()
        )
    }

}