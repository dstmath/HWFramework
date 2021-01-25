package com.android.server.backup;

import android.content.ContentResolver;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.KeyValueListParser;
import android.util.KeyValueSettingObserver;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;

public class BackupManagerConstants extends KeyValueSettingObserver {
    @VisibleForTesting
    public static final String BACKUP_FINISHED_NOTIFICATION_RECEIVERS = "backup_finished_notification_receivers";
    @VisibleForTesting
    public static final String DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS = "";
    @VisibleForTesting
    public static final long DEFAULT_FULL_BACKUP_INTERVAL_MILLISECONDS = 86400000;
    @VisibleForTesting
    public static final int DEFAULT_FULL_BACKUP_REQUIRED_NETWORK_TYPE = 2;
    @VisibleForTesting
    public static final boolean DEFAULT_FULL_BACKUP_REQUIRE_CHARGING = true;
    @VisibleForTesting
    public static final long DEFAULT_KEY_VALUE_BACKUP_FUZZ_MILLISECONDS = 600000;
    @VisibleForTesting
    public static final long DEFAULT_KEY_VALUE_BACKUP_INTERVAL_MILLISECONDS = 14400000;
    @VisibleForTesting
    public static final int DEFAULT_KEY_VALUE_BACKUP_REQUIRED_NETWORK_TYPE = 1;
    @VisibleForTesting
    public static final boolean DEFAULT_KEY_VALUE_BACKUP_REQUIRE_CHARGING = true;
    @VisibleForTesting
    public static final String FULL_BACKUP_INTERVAL_MILLISECONDS = "full_backup_interval_milliseconds";
    @VisibleForTesting
    public static final String FULL_BACKUP_REQUIRED_NETWORK_TYPE = "full_backup_required_network_type";
    @VisibleForTesting
    public static final String FULL_BACKUP_REQUIRE_CHARGING = "full_backup_require_charging";
    @VisibleForTesting
    public static final String KEY_VALUE_BACKUP_FUZZ_MILLISECONDS = "key_value_backup_fuzz_milliseconds";
    @VisibleForTesting
    public static final String KEY_VALUE_BACKUP_INTERVAL_MILLISECONDS = "key_value_backup_interval_milliseconds";
    @VisibleForTesting
    public static final String KEY_VALUE_BACKUP_REQUIRED_NETWORK_TYPE = "key_value_backup_required_network_type";
    @VisibleForTesting
    public static final String KEY_VALUE_BACKUP_REQUIRE_CHARGING = "key_value_backup_require_charging";
    private static final String SETTING = "backup_manager_constants";
    private static final String TAG = "BackupManagerConstants";
    private String[] mBackupFinishedNotificationReceivers;
    private long mFullBackupIntervalMilliseconds;
    private boolean mFullBackupRequireCharging;
    private int mFullBackupRequiredNetworkType;
    private long mKeyValueBackupFuzzMilliseconds;
    private long mKeyValueBackupIntervalMilliseconds;
    private boolean mKeyValueBackupRequireCharging;
    private int mKeyValueBackupRequiredNetworkType;

    public BackupManagerConstants(Handler handler, ContentResolver resolver) {
        super(handler, resolver, Settings.Secure.getUriFor(SETTING));
    }

    public String getSettingValue(ContentResolver resolver) {
        return Settings.Secure.getString(resolver, SETTING);
    }

    public synchronized void update(KeyValueListParser parser) {
        this.mKeyValueBackupIntervalMilliseconds = parser.getLong(KEY_VALUE_BACKUP_INTERVAL_MILLISECONDS, 14400000);
        this.mKeyValueBackupFuzzMilliseconds = parser.getLong(KEY_VALUE_BACKUP_FUZZ_MILLISECONDS, 600000);
        this.mKeyValueBackupRequireCharging = parser.getBoolean(KEY_VALUE_BACKUP_REQUIRE_CHARGING, true);
        this.mKeyValueBackupRequiredNetworkType = parser.getInt(KEY_VALUE_BACKUP_REQUIRED_NETWORK_TYPE, 1);
        this.mFullBackupIntervalMilliseconds = parser.getLong(FULL_BACKUP_INTERVAL_MILLISECONDS, 86400000);
        this.mFullBackupRequireCharging = parser.getBoolean(FULL_BACKUP_REQUIRE_CHARGING, true);
        this.mFullBackupRequiredNetworkType = parser.getInt(FULL_BACKUP_REQUIRED_NETWORK_TYPE, 2);
        String backupFinishedNotificationReceivers = parser.getString(BACKUP_FINISHED_NOTIFICATION_RECEIVERS, "");
        if (backupFinishedNotificationReceivers.isEmpty()) {
            this.mBackupFinishedNotificationReceivers = new String[0];
        } else {
            this.mBackupFinishedNotificationReceivers = backupFinishedNotificationReceivers.split(":");
        }
    }

    public synchronized long getKeyValueBackupIntervalMilliseconds() {
        Slog.v(TAG, "getKeyValueBackupIntervalMilliseconds(...) returns " + this.mKeyValueBackupIntervalMilliseconds);
        return this.mKeyValueBackupIntervalMilliseconds;
    }

    public synchronized long getKeyValueBackupFuzzMilliseconds() {
        Slog.v(TAG, "getKeyValueBackupFuzzMilliseconds(...) returns " + this.mKeyValueBackupFuzzMilliseconds);
        return this.mKeyValueBackupFuzzMilliseconds;
    }

    public synchronized boolean getKeyValueBackupRequireCharging() {
        Slog.v(TAG, "getKeyValueBackupRequireCharging(...) returns " + this.mKeyValueBackupRequireCharging);
        return this.mKeyValueBackupRequireCharging;
    }

    public synchronized int getKeyValueBackupRequiredNetworkType() {
        Slog.v(TAG, "getKeyValueBackupRequiredNetworkType(...) returns " + this.mKeyValueBackupRequiredNetworkType);
        return this.mKeyValueBackupRequiredNetworkType;
    }

    public synchronized long getFullBackupIntervalMilliseconds() {
        Slog.v(TAG, "getFullBackupIntervalMilliseconds(...) returns " + this.mFullBackupIntervalMilliseconds);
        return this.mFullBackupIntervalMilliseconds;
    }

    public synchronized boolean getFullBackupRequireCharging() {
        Slog.v(TAG, "getFullBackupRequireCharging(...) returns " + this.mFullBackupRequireCharging);
        return this.mFullBackupRequireCharging;
    }

    public synchronized int getFullBackupRequiredNetworkType() {
        Slog.v(TAG, "getFullBackupRequiredNetworkType(...) returns " + this.mFullBackupRequiredNetworkType);
        return this.mFullBackupRequiredNetworkType;
    }

    public synchronized String[] getBackupFinishedNotificationReceivers() {
        Slog.v(TAG, "getBackupFinishedNotificationReceivers(...) returns " + TextUtils.join(", ", this.mBackupFinishedNotificationReceivers));
        return this.mBackupFinishedNotificationReceivers;
    }
}
