package dev.emortal.catnipbridge.config

@kotlinx.serialization.Serializable
data class CatnipBridgeConfig(
    val status: String = "mc.emortal.dev",
    val botToken: String = "",
    val guildID: String = "",
    val chatChannelID: String = "",
    val chatWebhookID: String = "",
    val playerJoinLeaveChannelID: String = ""
)