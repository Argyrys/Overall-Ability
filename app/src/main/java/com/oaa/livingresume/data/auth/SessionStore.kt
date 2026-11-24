package com.oaa.livingresume.data.auth

import android.content.Context

/**
 * Tiny persistent session store for the mock login flow.
 * Saves the signed-in student id so the app reopens already authenticated.
 * (Swap for Supabase Auth session persistence when a real backend is wired in.)
 */
class SessionStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** The currently signed-in student id, or null when signed out. */
    val studentId: String?
        get() = prefs.getString(KEY_STUDENT_ID, null)

    fun save(studentId: String) {
        prefs.edit().putString(KEY_STUDENT_ID, studentId).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY_STUDENT_ID).apply()
    }

    private companion object {
        const val PREFS_NAME = "oaa_session"
        const val KEY_STUDENT_ID = "student_id"
    }
}
