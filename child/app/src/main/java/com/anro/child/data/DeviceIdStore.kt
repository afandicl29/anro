package com.anro.child.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "anro_preferences")

class DeviceIdStore(
    private val context: Context
) {

    companion object {
        private val DEVICE_ID_KEY = stringPreferencesKey("device_id")
    }

    suspend fun getDeviceId(): String {

        val preferences = context.dataStore.data.first()

        val existingId = preferences[DEVICE_ID_KEY]

        if (existingId != null) {
            return existingId
        }

        val newId = UUID.randomUUID().toString()

        context.dataStore.edit {
            it[DEVICE_ID_KEY] = newId
        }

        return newId
    }
}

