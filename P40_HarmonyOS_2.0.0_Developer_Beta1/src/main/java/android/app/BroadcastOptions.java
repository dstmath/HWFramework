package android.app;

import android.annotation.SystemApi;
import android.os.Bundle;

@SystemApi
public class BroadcastOptions {
    static final String KEY_ALLOW_BACKGROUND_ACTIVITY_STARTS = "android:broadcast.allowBackgroundActivityStarts";
    static final String KEY_DONT_SEND_TO_RESTRICTED_APPS = "android:broadcast.dontSendToRestrictedApps";
    static final String KEY_MAX_MANIFEST_RECEIVER_API_LEVEL = "android:broadcast.maxManifestReceiverApiLevel";
    static final String KEY_MIN_MANIFEST_RECEIVER_API_LEVEL = "android:broadcast.minManifestReceiverApiLevel";
    static final String KEY_TEMPORARY_APP_WHITELIST_DURATION = "android:broadcast.temporaryAppWhitelistDuration";
    private boolean mAllowBackgroundActivityStarts;
    private boolean mDontSendToRestrictedApps = false;
    private int mMaxManifestReceiverApiLevel = 10000;
    private int mMinManifestReceiverApiLevel = 0;
    private long mTemporaryAppWhitelistDuration;

    public static BroadcastOptions makeBasic() {
        return new BroadcastOptions();
    }

    private BroadcastOptions() {
    }

    public BroadcastOptions(Bundle opts) {
        this.mTemporaryAppWhitelistDuration = opts.getLong(KEY_TEMPORARY_APP_WHITELIST_DURATION);
        this.mMinManifestReceiverApiLevel = opts.getInt(KEY_MIN_MANIFEST_RECEIVER_API_LEVEL, 0);
        this.mMaxManifestReceiverApiLevel = opts.getInt(KEY_MAX_MANIFEST_RECEIVER_API_LEVEL, 10000);
        this.mDontSendToRestrictedApps = opts.getBoolean(KEY_DONT_SEND_TO_RESTRICTED_APPS, false);
        this.mAllowBackgroundActivityStarts = opts.getBoolean(KEY_ALLOW_BACKGROUND_ACTIVITY_STARTS, false);
    }

    public void setTemporaryAppWhitelistDuration(long duration) {
        this.mTemporaryAppWhitelistDuration = duration;
    }

    public long getTemporaryAppWhitelistDuration() {
        return this.mTemporaryAppWhitelistDuration;
    }

    public void setMinManifestReceiverApiLevel(int apiLevel) {
        this.mMinManifestReceiverApiLevel = apiLevel;
    }

    public int getMinManifestReceiverApiLevel() {
        return this.mMinManifestReceiverApiLevel;
    }

    public void setMaxManifestReceiverApiLevel(int apiLevel) {
        this.mMaxManifestReceiverApiLevel = apiLevel;
    }

    public int getMaxManifestReceiverApiLevel() {
        return this.mMaxManifestReceiverApiLevel;
    }

    public void setDontSendToRestrictedApps(boolean dontSendToRestrictedApps) {
        this.mDontSendToRestrictedApps = dontSendToRestrictedApps;
    }

    public boolean isDontSendToRestrictedApps() {
        return this.mDontSendToRestrictedApps;
    }

    public void setBackgroundActivityStartsAllowed(boolean allowBackgroundActivityStarts) {
        this.mAllowBackgroundActivityStarts = allowBackgroundActivityStarts;
    }

    public boolean allowsBackgroundActivityStarts() {
        return this.mAllowBackgroundActivityStarts;
    }

    public Bundle toBundle() {
        Bundle b = new Bundle();
        long j = this.mTemporaryAppWhitelistDuration;
        if (j > 0) {
            b.putLong(KEY_TEMPORARY_APP_WHITELIST_DURATION, j);
        }
        int i = this.mMinManifestReceiverApiLevel;
        if (i != 0) {
            b.putInt(KEY_MIN_MANIFEST_RECEIVER_API_LEVEL, i);
        }
        int i2 = this.mMaxManifestReceiverApiLevel;
        if (i2 != 10000) {
            b.putInt(KEY_MAX_MANIFEST_RECEIVER_API_LEVEL, i2);
        }
        if (this.mDontSendToRestrictedApps) {
            b.putBoolean(KEY_DONT_SEND_TO_RESTRICTED_APPS, true);
        }
        if (this.mAllowBackgroundActivityStarts) {
            b.putBoolean(KEY_ALLOW_BACKGROUND_ACTIVITY_STARTS, true);
        }
        if (b.isEmpty()) {
            return null;
        }
        return b;
    }
}
