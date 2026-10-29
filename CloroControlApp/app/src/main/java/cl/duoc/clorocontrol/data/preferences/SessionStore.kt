package cl.duoc.clorocontrol.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cl.duoc.clorocontrol.model.UserRole
import cl.duoc.clorocontrol.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "clorocontrol_session")

class SessionStore(private val context: Context) {
    private val emailKey = stringPreferencesKey("email")
    private val roleKey = stringPreferencesKey("role")

    val session: Flow<UserSession?> = context.sessionDataStore.data.map { preferences ->
        val email = preferences[emailKey]
        val role = UserRole.fromName(preferences[roleKey])
        if (email != null && role != null) UserSession(email, role) else null
    }

    suspend fun saveSession(session: UserSession) {
        context.sessionDataStore.edit { preferences ->
            preferences[emailKey] = session.email
            preferences[roleKey] = session.role.name
        }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { preferences ->
            preferences.remove(emailKey)
            preferences.remove(roleKey)
        }
    }
}
