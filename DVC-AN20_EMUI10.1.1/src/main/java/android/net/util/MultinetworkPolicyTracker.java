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
import android.provider.Settings;
import android.util.Slog;
import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
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

    public MultinetworkPolicyTracker(Context ctx, Handler handler) {
        this(ctx, handler, null);
    }

    public MultinetworkPolicyTracker(Context ctx, Handler handler, Runnable avoidBadWifiCallback) {
        this.mAvoidBadWifi = true;
        this.mContext = ctx;
        this.mHandler = handler;
        this.mReevaluateRunnable = new Runnable(avoidBadWifiCallback) {
            /* class android.net.util.$$Lambda$MultinetworkPolicyTracker$0siHK6f4lHJz8hbdHbT6G4KpV4 */
            private final /* synthetic */ Runnable f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                MultinetworkPolicyTracker.this.lambda$new$0$MultinetworkPolicyTracker(this.f$1);
            }
        };
        this.mSettingsUris = Arrays.asList(Settings.Global.getUriFor(Settings.Global.NETWORK_AVOID_BAD_WIFI), Settings.Global.getUriFor(Settings.Global.NETWORK_METERED_MULTIPATH_PREFERENCE));
        this.mResolver = this.mContext.getContentResolver();
        this.mSettingObserver = new SettingObserver();
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class android.net.util.MultinetworkPolicyTracker.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                MultinetworkPolicyTracker.this.reevaluate();
            }
        };
        updateAvoidBadWifi();
        updateMeteredMultipathPreference();
    }

    public /* synthetic */ void lambda$new$0$MultinetworkPolicyTracker(Runnable avoidBadWifiCallback) {
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
        intentFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
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
        return this.mContext.getResources().getInteger(R.integer.config_networkAvoidBadWifi) == 0;
    }

    public boolean shouldNotifyWifiUnvalidated() {
        return configRestrictsAvoidBadWifi() && getAvoidBadWifiSetting() == null;
    }

    public String getAvoidBadWifiSetting() {
        return Settings.Global.getString(this.mResolver, Settings.Global.NETWORK_AVOID_BAD_WIFI);
    }

    @VisibleForTesting
    public void reevaluate() {
        this.mHandler.post(this.mReevaluateRunnable);
    }

    public boolean updateAvoidBadWifi() {
        boolean settingAvoidBadWifi = "1".equals(getAvoidBadWifiSetting());
        boolean prev = this.mAvoidBadWifi;
        this.mAvoidBadWifi = settingAvoidBadWifi || !configRestrictsAvoidBadWifi();
        return this.mAvoidBadWifi != prev;
    }

    public int configMeteredMultipathPreference() {
        return this.mContext.getResources().getInteger(R.integer.config_networkMeteredMultipathPreference);
    }

    public void updateMeteredMultipathPreference() {
        try {
            this.mMeteredMultipathPreference = Integer.parseInt(Settings.Global.getString(this.mResolver, Settings.Global.NETWORK_METERED_MULTIPATH_PREFERENCE));
        } catch (NumberFormatException e) {
            this.mMeteredMultipathPreference = configMeteredMultipathPreference();
        }
    }

    private class SettingObserver extends ContentObserver {
        public SettingObserver() {
            super(null);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            Slog.wtf(MultinetworkPolicyTracker.TAG, "Should never be reached.");
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            if (!MultinetworkPolicyTracker.this.mSettingsUris.contains(uri)) {
                String str = MultinetworkPolicyTracker.TAG;
                Slog.wtf(str, "Unexpected settings observation: " + uri);
            }
            MultinetworkPolicyTracker.this.reevaluate();
        }
    }
}
