package com.android.server.content;

import android.content.Context;
import android.database.ContentObserver;
import android.provider.Settings;
import android.util.KeyValueListParser;
import android.util.Slog;
import com.android.internal.os.BackgroundThread;
import java.io.PrintWriter;

public class SyncManagerConstants extends ContentObserver {
    private static final int DEF_EXEMPTION_TEMP_WHITELIST_DURATION_IN_SECONDS = 600;
    private static final int DEF_INITIAL_SYNC_RETRY_TIME_IN_SECONDS = 30;
    private static final int DEF_MAX_RETRIES_WITH_APP_STANDBY_EXEMPTION = 5;
    private static final int DEF_MAX_SYNC_RETRY_TIME_IN_SECONDS = 3600;
    private static final float DEF_RETRY_TIME_INCREASE_FACTOR = 2.0f;
    private static final String KEY_EXEMPTION_TEMP_WHITELIST_DURATION_IN_SECONDS = "exemption_temp_whitelist_duration_in_seconds";
    private static final String KEY_INITIAL_SYNC_RETRY_TIME_IN_SECONDS = "initial_sync_retry_time_in_seconds";
    private static final String KEY_MAX_RETRIES_WITH_APP_STANDBY_EXEMPTION = "max_retries_with_app_standby_exemption";
    private static final String KEY_MAX_SYNC_RETRY_TIME_IN_SECONDS = "max_sync_retry_time_in_seconds";
    private static final String KEY_RETRY_TIME_INCREASE_FACTOR = "retry_time_increase_factor";
    private static final String TAG = "SyncManagerConfig";
    private final Context mContext;
    private int mInitialSyncRetryTimeInSeconds = 30;
    private int mKeyExemptionTempWhitelistDurationInSeconds = 600;
    private final Object mLock = new Object();
    private int mMaxRetriesWithAppStandbyExemption = 5;
    private int mMaxSyncRetryTimeInSeconds = DEF_MAX_SYNC_RETRY_TIME_IN_SECONDS;
    private float mRetryTimeIncreaseFactor = DEF_RETRY_TIME_INCREASE_FACTOR;

    protected SyncManagerConstants(Context context) {
        super(null);
        this.mContext = context;
    }

    public void start() {
        BackgroundThread.getHandler().post(new Runnable() {
            /* class com.android.server.content.$$Lambda$SyncManagerConstants$qo5ldQVp10jCUY9aavBZDKP2k6Q */

            @Override // java.lang.Runnable
            public final void run() {
                SyncManagerConstants.this.lambda$start$0$SyncManagerConstants();
            }
        });
    }

    public /* synthetic */ void lambda$start$0$SyncManagerConstants() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("sync_manager_constants"), false, this);
        refresh();
    }

    @Override // android.database.ContentObserver
    public void onChange(boolean selfChange) {
        refresh();
    }

    private void refresh() {
        synchronized (this.mLock) {
            String newValue = Settings.Global.getString(this.mContext.getContentResolver(), "sync_manager_constants");
            KeyValueListParser parser = new KeyValueListParser(',');
            try {
                parser.setString(newValue);
            } catch (IllegalArgumentException e) {
                Slog.wtf(TAG, "Bad constants: " + newValue);
            }
            this.mInitialSyncRetryTimeInSeconds = parser.getInt(KEY_INITIAL_SYNC_RETRY_TIME_IN_SECONDS, 30);
            this.mMaxSyncRetryTimeInSeconds = parser.getInt(KEY_MAX_SYNC_RETRY_TIME_IN_SECONDS, (int) DEF_MAX_SYNC_RETRY_TIME_IN_SECONDS);
            this.mRetryTimeIncreaseFactor = parser.getFloat(KEY_RETRY_TIME_INCREASE_FACTOR, (float) DEF_RETRY_TIME_INCREASE_FACTOR);
            this.mMaxRetriesWithAppStandbyExemption = parser.getInt(KEY_MAX_RETRIES_WITH_APP_STANDBY_EXEMPTION, 5);
            this.mKeyExemptionTempWhitelistDurationInSeconds = parser.getInt(KEY_EXEMPTION_TEMP_WHITELIST_DURATION_IN_SECONDS, 600);
        }
    }

    public int getInitialSyncRetryTimeInSeconds() {
        int i;
        synchronized (this.mLock) {
            i = this.mInitialSyncRetryTimeInSeconds;
        }
        return i;
    }

    public float getRetryTimeIncreaseFactor() {
        float f;
        synchronized (this.mLock) {
            f = this.mRetryTimeIncreaseFactor;
        }
        return f;
    }

    public int getMaxSyncRetryTimeInSeconds() {
        int i;
        synchronized (this.mLock) {
            i = this.mMaxSyncRetryTimeInSeconds;
        }
        return i;
    }

    public int getMaxRetriesWithAppStandbyExemption() {
        int i;
        synchronized (this.mLock) {
            i = this.mMaxRetriesWithAppStandbyExemption;
        }
        return i;
    }

    public int getKeyExemptionTempWhitelistDurationInSeconds() {
        int i;
        synchronized (this.mLock) {
            i = this.mKeyExemptionTempWhitelistDurationInSeconds;
        }
        return i;
    }

    public void dump(PrintWriter pw, String prefix) {
        synchronized (this.mLock) {
            pw.print(prefix);
            pw.println("SyncManager Config:");
            pw.print(prefix);
            pw.print("  mInitialSyncRetryTimeInSeconds=");
            pw.println(this.mInitialSyncRetryTimeInSeconds);
            pw.print(prefix);
            pw.print("  mRetryTimeIncreaseFactor=");
            pw.println(this.mRetryTimeIncreaseFactor);
            pw.print(prefix);
            pw.print("  mMaxSyncRetryTimeInSeconds=");
            pw.println(this.mMaxSyncRetryTimeInSeconds);
            pw.print(prefix);
            pw.print("  mMaxRetriesWithAppStandbyExemption=");
            pw.println(this.mMaxRetriesWithAppStandbyExemption);
            pw.print(prefix);
            pw.print("  mKeyExemptionTempWhitelistDurationInSeconds=");
            pw.println(this.mKeyExemptionTempWhitelistDurationInSeconds);
        }
    }
}
