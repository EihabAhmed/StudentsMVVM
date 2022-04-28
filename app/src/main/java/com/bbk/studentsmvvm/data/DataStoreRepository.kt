package com.bbk.studentsmvvm.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.bbk.studentsmvvm.util.Constants.Companion.PREFERENCES_BACK_ONLINE
import com.bbk.studentsmvvm.util.Constants.Companion.PREFERENCES_NAME
import com.bbk.studentsmvvm.util.Constants.Companion.TOKEN
import com.bbk.studentsmvvm.util.Constants.Companion.USER_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

@ActivityRetainedScoped
class DataStoreRepository @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)
    }

    private object PreferencesKeys {
        val backOnline = booleanPreferencesKey(PREFERENCES_BACK_ONLINE)
        val token = stringPreferencesKey(TOKEN)
        val userName = stringPreferencesKey(USER_NAME)
    }

    suspend fun saveBackOnline(backOnline: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.backOnline] = backOnline
        }
    }

    val readBackOnline: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val backOnline = preferences[PreferencesKeys.backOnline] ?: false
            backOnline
        }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.token] = token
        }
    }

    val readToken: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val token = preferences[PreferencesKeys.token] ?: ""
            token
        }

    suspend fun saveUserName(userName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.userName] = userName
        }
    }

    val readUserName: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val userName = preferences[PreferencesKeys.userName] ?: ""
            userName
        }
}