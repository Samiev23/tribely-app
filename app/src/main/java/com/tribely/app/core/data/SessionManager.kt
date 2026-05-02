package com.tribely.app.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "tribely_session")

class SessionManager(private val context: Context) {

    companion object {
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val GROUP_ID = stringPreferencesKey("group_id")
        private val GROUP_NAME = stringPreferencesKey("group_name")
        private val GROUP_INVITE_CODE = stringPreferencesKey("group_invite_code")
    }

    val userIdFlow: Flow<String?> = context.sessionDataStore.data
        .map { it[USER_ID] }

    val userNameFlow: Flow<String?> = context.sessionDataStore.data
        .map { it[USER_NAME] }

    val groupIdFlow: Flow<String?> = context.sessionDataStore.data
        .map { it[GROUP_ID] }

    val groupNameFlow: Flow<String?> = context.sessionDataStore.data
        .map { it[GROUP_NAME] }

    val groupInviteCodeFlow: Flow<String?> = context.sessionDataStore.data
        .map { it[GROUP_INVITE_CODE] }

    suspend fun getUserId(): String? = userIdFlow.first()

    suspend fun getGroupId(): String? = groupIdFlow.first()

    suspend fun saveSession(userId: String, userName: String) {
        context.sessionDataStore.edit {
            it[USER_ID] = userId
            it[USER_NAME] = userName
        }
    }

    suspend fun saveGroup(groupId: String, groupName: String, inviteCode: String) {
        context.sessionDataStore.edit {
            it[GROUP_ID] = groupId
            it[GROUP_NAME] = groupName
            it[GROUP_INVITE_CODE] = inviteCode
        }
    }

    suspend fun clearGroup() {
        context.sessionDataStore.edit {
            it.remove(GROUP_ID)
            it.remove(GROUP_NAME)
            it.remove(GROUP_INVITE_CODE)
        }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { it.clear() }
    }
}
