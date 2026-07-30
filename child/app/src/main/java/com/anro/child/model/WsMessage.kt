package com.anro.child.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class WsMessage(
    val type: String,
    val deviceId: String? = null,
    val targetId: String? = null,
    val sessionId: String? = null,
    val payload: JsonObject? = null
)

