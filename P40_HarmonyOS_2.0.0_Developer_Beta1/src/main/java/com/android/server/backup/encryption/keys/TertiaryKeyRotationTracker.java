package com.android.server.backup.encryption.keys;

import android.content.Context;
import android.content.SharedPreferences;
import com.android.internal.annotations.VisibleForTesting;

public class TertiaryKeyRotationTracker {
    private static final boolean DEBUG = false;
    private static final int MAX_BACKUPS_UNTIL_TERTIARY_KEY_ROTATION = 31;
    private static final String SHARED_PREFERENCES_NAME = "tertiary_key_rotation_tracker";
    private static final String TAG = "TertiaryKeyRotationTracker";
    private final SharedPreferences mSharedPreferences;

    public static TertiaryKeyRotationTracker getInstance(Context context) {
        return new TertiaryKeyRotationTracker(context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0));
    }

    @VisibleForTesting
    TertiaryKeyRotationTracker(SharedPreferences sharedPreferences) {
        this.mSharedPreferences = sharedPreferences;
    }

    public boolean isKeyRotationDue(String packageName) {
        return getBackupsSinceRotation(packageName) >= 31;
    }

    public void recordBackup(String packageName) {
        this.mSharedPreferences.edit().putInt(packageName, getBackupsSinceRotation(packageName) + 1).apply();
    }

    public void resetCountdown(String packageName) {
        this.mSharedPreferences.edit().putInt(packageName, 0).apply();
    }

    public void markAllForRotation() {
        SharedPreferences.Editor editor = this.mSharedPreferences.edit();
        for (String packageName : this.mSharedPreferences.getAll().keySet()) {
            editor.putInt(packageName, 31);
        }
        editor.apply();
    }

    private int getBackupsSinceRotation(String packageName) {
        return this.mSharedPreferences.getInt(packageName, 0);
    }
}
