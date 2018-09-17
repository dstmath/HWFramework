package android.net.util;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.util.Slog;
import java.util.Arrays;
import java.util.List;

public class MultinetworkPolicyTracker {
    private static String TAG = MultinetworkPolicyTracker.class.getSimpleName();
    private volatile boolean mAvoidBadWifi;
    private final BroadcastReceiver mBroadcastReceiver;
    private final Context mContext;
    private final Handler mHandler;
    private volatile int mMeteredMultipathPreference;
    private final Runnable mReevaluateRunnable;
    private final ContentResolver mResolver;
    private final SettingObserver mSettingObserver;
    private final List<Uri> mSettingsUris;

    private class SettingObserver extends ContentObserver {
        public SettingObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            Slog.wtf(MultinetworkPolicyTracker.TAG, "Should never be reached.");
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (!MultinetworkPolicyTracker.this.mSettingsUris.contains(uri)) {
                Slog.wtf(MultinetworkPolicyTracker.TAG, "Unexpected settings observation: " + uri);
            }
            MultinetworkPolicyTracker.this.reevaluate();
        }
    }

    public MultinetworkPolicyTracker(Context ctx, Handler handler) {
        this(ctx, handler, null);
    }

    public MultinetworkPolicyTracker(Context ctx, Handler handler, Runnable avoidBadWifiCallback) {
        this.mAvoidBadWifi = true;
        this.mContext = ctx;
        this.mHandler = handler;
        this.mReevaluateRunnable = new -$Lambda$uAc0-SfvAbf39zjOK01b1lQkeVs(this, avoidBadWifiCallback);
        this.mSettingsUris = Arrays.asList(new Uri[]{Global.getUriFor("network_avoid_bad_wifi"), Global.getUriFor("network_metered_multipath_preference")});
        this.mResolver = this.mContext.getContentResolver();
        this.mSettingObserver = new SettingObserver();
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                MultinetworkPolicyTracker.this.reevaluate();
            }
        };
        updateAvoidBadWifi();
        updateMeteredMultipathPreference();
    }

    /* synthetic */ void lambda$-android_net_util_MultinetworkPolicyTracker_2908(Runnable avoidBadWifiCallback) {
        if (updateAvoidBadWifi() && avoidBadWifiCallback != null) {
            avoidBadWifiCallback.run();
        }
        updateMeteredMultipathPreference();
    }

    public void start() {
        for (Uri uri : this.mSettingsUris) {
            this.mResolver.registerContentObserver(uri, false, this.mSettingObserver);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, intentFilter, null, null);
        reevaluate();
    }

    public void shutdown() {
        this.mResolver.unregisterContentObserver(this.mSettingObserver);
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
    }

    public boolean getAvoidBadWifi() {
        return this.mAvoidBadWifi;
    }

    public int getMeteredMultipathPreference() {
        return this.mMeteredMultipathPreference;
    }

    public boolean configRestrictsAvoidBadWifi() {
        return this.mContext.getResources().getInteger(17694817) == 0;
    }

    public boolean shouldNotifyWifiUnvalidated() {
        return configRestrictsAvoidBadWifi() && getAvoidBadWifiSetting() == null;
    }

    public String getAvoidBadWifiSetting() {
        return Global.getString(this.mResolver, "network_avoid_bad_wifi");
    }

    public void reevaluate() {
        this.mHandler.post(this.mReevaluateRunnable);
    }

    public boolean updateAvoidBadWifi() {
        boolean z;
        boolean settingAvoidBadWifi = "1".equals(getAvoidBadWifiSetting());
        boolean prev = this.mAvoidBadWifi;
        if (settingAvoidBadWifi) {
            z = true;
        } else {
            z = configRestrictsAvoidBadWifi() ^ 1;
        }
        this.mAvoidBadWifi = z;
        if (this.mAvoidBadWifi != prev) {
            return true;
        }
        return false;
    }

    public int configMeteredMultipathPreference() {
        return this.mContext.getResources().getInteger(17694818);
    }

    public void updateMeteredMultipathPreference() {
        try {
            this.mMeteredMultipathPreference = Integer.parseInt(Global.getString(this.mResolver, "network_metered_multipath_preference"));
        } catch (NumberFormatException e) {
            this.mMeteredMultipathPreference = configMeteredMultipathPreference();
        }
    }
}
