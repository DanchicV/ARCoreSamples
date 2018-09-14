package com.dvoroncov.arcore_samples.cloud_anchor.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class StorageManager {

    private static final String NEXT_SHORT_CODE = "next_short_code";
    private static final String KEY_PREFIX = "anchor;";
    private static final int INITIAL_SHORT_CODE = 0;

    private SharedPreferences preferences;

    public StorageManager(Activity activity) {
        this.preferences = activity.getPreferences(Context.MODE_PRIVATE);
    }

    public int getNextShortCode() {
        int shortCode = preferences.getInt(NEXT_SHORT_CODE, INITIAL_SHORT_CODE);
        preferences.edit()
                .putInt(NEXT_SHORT_CODE, shortCode + 1)
                .apply();
        return shortCode;
    }

    public void saveCloudAnchorID(int shortCode, String cloudAnchorId) {
        preferences.edit()
                .putString(KEY_PREFIX + shortCode, cloudAnchorId)
                .apply();
    }

    public String getCloudAnchorID(int shortCode) {
        return preferences.getString(KEY_PREFIX + shortCode, "");
    }
}