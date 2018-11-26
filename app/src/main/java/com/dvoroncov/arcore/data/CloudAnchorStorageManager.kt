package com.dvoroncov.arcore.data

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CloudAnchorStorageManager(activity: Activity) {

    private val preferences: SharedPreferences = activity.getPreferences(Context.MODE_PRIVATE)
    private val reference: DatabaseReference

    val nextShortCode: Int
        get() {
            val shortCode = preferences.getInt(NEXT_SHORT_CODE, INITIAL_SHORT_CODE)
            preferences.edit()
                    .putInt(NEXT_SHORT_CODE, shortCode + 1)
                    .apply()
            return shortCode
        }

    init {
        val database = FirebaseDatabase.getInstance()
        reference = database.reference
    }

    fun saveCloudAnchorID(shortCode: Int, cloudAnchorId: String) {
        preferences.edit()
                .putString(KEY_PREFIX + shortCode, cloudAnchorId)
                .apply()
    }

    fun getCloudAnchorID(shortCode: Int): String? {
        return preferences.getString(KEY_PREFIX + shortCode, "")
    }

    companion object {

        private const val NEXT_SHORT_CODE = "next_short_code"
        private const val KEY_PREFIX = "anchor;"
        private const val INITIAL_SHORT_CODE = 0
    }
}