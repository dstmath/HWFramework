package com.huawei.distributed.net;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import java.util.concurrent.atomic.AtomicBoolean;

public class DistributedNetSettings {
    private static final int BATTERY_PLUGGED_ANY = 7;
    private static final String DISTRIBUTED_NET_ENABLED = "distributed_net_enabled";
    private static final String DISTRIBUTED_NET_METERED = "distributed_net_metered";
    private static final Uri DISTRIBUTED_NET_METERED_URI = Settings.System.getUriFor(DISTRIBUTED_NET_METERED);
    private static final Uri DISTRIBUTED_NET_URI = Settings.System.getUriFor(DISTRIBUTED_NET_ENABLED);
    private static final int OFF = 0;
    private static final int ON = 1;
    private static final String TAG = "DistributedNetSettings";
    private Context mContext;
    private final AtomicBoolean mIsCharging = new AtomicBoolean(false);
    private Listener mListener;
    private final ContentObserver mObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        /* class com.huawei.distributed.net.DistributedNetSettings.AnonymousClass1 */

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange, Uri uri) {
            Log.i(DistributedNetSettings.TAG, "Observe settings change " + uri);
            if (DistributedNetSettings.this.mListener != null) {
                if (DistributedNetSettings.DISTRIBUTED_NET_URI.equals(uri)) {
                    DistributedNetSettings.this.mListener.onNetEnabledChanged(DistributedNetSettings.this.isDistributedNetEnabled());
                } else if (DistributedNetSettings.DISTRIBUTED_NET_METERED_URI.equals(uri)) {
                    DistributedNetSettings.this.mListener.onNetMeteredChanged(DistributedNetSettings.this.isDistributedNetMetered());
                }
            }
        }
    };
    private final BroadcastReceiver mPowerStateReceiver = new BroadcastReceiver() {
        /* class com.huawei.distributed.net.DistributedNetSettings.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null || TextUtils.isEmpty(intent.getAction())) {
                Log.e(DistributedNetSettings.TAG, "mPwoerStateReceiver receive empty intent.");
                return;
            }
            String action = intent.getAction();
            char c = 65535;
            int hashCode = action.hashCode();
            if (hashCode != -1886648615) {
                if (hashCode == 1019184907 && action.equals("android.intent.action.ACTION_POWER_CONNECTED")) {
                    c = 0;
                }
            } else if (action.equals("android.intent.action.ACTION_POWER_DISCONNECTED")) {
                c = 1;
            }
            if (c == 0) {
                handleChargeModeChange(true);
            } else if (c == 1) {
                handleChargeModeChange(false);
            }
        }

        private void handleChargeModeChange(boolean isInCharge) {
            DistributedNetSettings.this.mIsCharging.set(isInCharge);
            if (DistributedNetSettings.this.mListener != null) {
                DistributedNetSettings.this.mListener.onDeviceChargingChanged(isInCharge);
            }
        }
    };

    public interface Listener {
        void onDeviceChargingChanged(boolean z);

        void onNetEnabledChanged(boolean z);

        void onNetMeteredChanged(boolean z);
    }

    public DistributedNetSettings(Context context, Listener listener) {
        if (context != null) {
            this.mContext = context;
            this.mListener = listener;
            registerSettingsObserver();
            listenChargingState();
            fetchInitialChargingState();
            return;
        }
        throw new IllegalArgumentException("context is null.");
    }

    public boolean isDistributedNetEnabled() {
        return Settings.System.getInt(this.mContext.getContentResolver(), DISTRIBUTED_NET_ENABLED, 0) == 1;
    }

    public boolean isDistributedNetMetered() {
        return Settings.System.getInt(this.mContext.getContentResolver(), DISTRIBUTED_NET_METERED, 0) == 1;
    }

    public boolean isCharging() {
        return this.mIsCharging.get();
    }

    public void resetDistributedNetSettings() {
        Settings.System.putInt(this.mContext.getContentResolver(), DISTRIBUTED_NET_ENABLED, 0);
        Settings.System.putInt(this.mContext.getContentResolver(), DISTRIBUTED_NET_METERED, 0);
    }

    private void registerSettingsObserver() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        contentResolver.registerContentObserver(DISTRIBUTED_NET_URI, false, this.mObserver);
        contentResolver.registerContentObserver(DISTRIBUTED_NET_METERED_URI, false, this.mObserver);
    }

    private void listenChargingState() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        intentFilter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        this.mContext.registerReceiver(this.mPowerStateReceiver, intentFilter);
    }

    private void fetchInitialChargingState() {
        Intent batteryStatus = this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        if (batteryStatus != null) {
            boolean z = false;
            int plugged = batteryStatus.getIntExtra("plugged", 0);
            AtomicBoolean atomicBoolean = this.mIsCharging;
            if ((plugged & BATTERY_PLUGGED_ANY) != 0) {
                z = true;
            }
            atomicBoolean.set(z);
        }
    }
}
