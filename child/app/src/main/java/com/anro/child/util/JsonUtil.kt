package com.anro.child.util

import kotlinx.serialization.json.Json

object JsonUtil {

    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }
}
