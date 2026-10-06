package com.example.placementcompanion.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class DataStoreManager(private val context: Context) {
    fun companyChecklist(companyId: Long): Flow<Set<String>> = context.dataStore.data.map {
        it[stringSetPreferencesKey("company_checklist_$companyId")] ?: emptySet()
    }

    suspend fun setChecklistItem(companyId: Long, item: String, completed: Boolean) {
        context.dataStore.edit { prefs ->
            val key = stringSetPreferencesKey("company_checklist_$companyId")
            val values = prefs[key] ?: emptySet()
            prefs[key] = if (completed) values + item else values - item
        }
    }

    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
        val KEY_SELECTED_TARGET_COMPANY = stringPreferencesKey("target_company")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    val targetCompany: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SELECTED_TARGET_COMPANY] ?: "Amazon"
    }

    suspend fun setTargetCompany(company: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SELECTED_TARGET_COMPANY] = company
        }
    }
}
