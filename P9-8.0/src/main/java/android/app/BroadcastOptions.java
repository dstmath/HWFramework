package android.app;

import android.os.Bundle;

public class BroadcastOptions {
    static final String KEY_MAX_MANIFEST_RECEIVER_API_LEVEL = "android:broadcast.maxManifestReceiverApiLevel";
    static final String KEY_MIN_MANIFEST_RECEIVER_API_LEVEL = "android:broadcast.minManifestReceiverApiLevel";
    static final String KEY_TEMPORARY_APP_WHITELIST_DURATION = "android:broadcast.temporaryAppWhitelistDuration";
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

    public Bundle toBundle() {
        Bundle b = new Bundle();
        if (this.mTemporaryAppWhitelistDuration > 0) {
            b.putLong(KEY_TEMPORARY_APP_WHITELIST_DURATION, this.mTemporaryAppWhitelistDuration);
        }
        if (this.mMinManifestReceiverApiLevel != 0) {
            b.putInt(KEY_MIN_MANIFEST_RECEIVER_API_LEVEL, this.mMinManifestReceiverApiLevel);
        }
        if (this.mMaxManifestReceiverApiLevel != 10000) {
            b.putInt(KEY_MAX_MANIFEST_RECEIVER_API_LEVEL, this.mMaxManifestReceiverApiLevel);
        }
        return b.isEmpty() ? null : b;
    }
}
