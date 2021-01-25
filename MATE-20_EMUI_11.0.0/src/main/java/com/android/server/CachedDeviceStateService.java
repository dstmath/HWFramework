package com.android.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManagerInternal;
import android.os.PowerManager;
import android.util.Slog;
import com.android.internal.os.CachedDeviceState;

public class CachedDeviceStateService extends SystemService {
    private static final String TAG = "CachedDeviceStateService";
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.CachedDeviceStateService.AnonymousClass1 */

        /* JADX WARNING: Removed duplicated region for block: B:17:0x003c  */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x0055  */
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            char c;
            String action = intent.getAction();
            int hashCode = action.hashCode();
            boolean z = false;
            if (hashCode != -2128145023) {
                if (hashCode != -1538406691) {
                    if (hashCode == -1454123155 && action.equals("android.intent.action.SCREEN_ON")) {
                        c = 1;
                        if (c != 0) {
                            CachedDeviceState cachedDeviceState = CachedDeviceStateService.this.mDeviceState;
                            if (intent.getIntExtra("plugged", 0) != 0) {
                                z = true;
                            }
                            cachedDeviceState.setCharging(z);
                            return;
                        } else if (c == 1) {
                            CachedDeviceStateService.this.mDeviceState.setScreenInteractive(true);
                            return;
                        } else if (c == 2) {
                            CachedDeviceStateService.this.mDeviceState.setScreenInteractive(false);
                            return;
                        } else {
                            return;
                        }
                    }
                } else if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                    c = 0;
                    if (c != 0) {
                    }
                }
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                c = 2;
                if (c != 0) {
                }
            }
            c = 65535;
            if (c != 0) {
            }
        }
    };
    private final CachedDeviceState mDeviceState = new CachedDeviceState();

    public CachedDeviceStateService(Context context) {
        super(context);
    }

    @Override // com.android.server.SystemService
    public void onStart() {
        publishLocalService(CachedDeviceState.Readonly.class, this.mDeviceState.getReadonlyClient());
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        if (500 == phase) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BATTERY_CHANGED");
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.addAction("android.intent.action.SCREEN_OFF");
            filter.setPriority(1000);
            getContext().registerReceiver(this.mBroadcastReceiver, filter);
            this.mDeviceState.setCharging(queryIsCharging());
            this.mDeviceState.setScreenInteractive(queryScreenInteractive(getContext()));
        }
    }

    private boolean queryIsCharging() {
        BatteryManagerInternal batteryManager = (BatteryManagerInternal) LocalServices.getService(BatteryManagerInternal.class);
        if (batteryManager == null) {
            Slog.wtf(TAG, "BatteryManager null while starting CachedDeviceStateService");
            return true;
        } else if (batteryManager.getPlugType() != 0) {
            return true;
        } else {
            return false;
        }
    }

    private boolean queryScreenInteractive(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(PowerManager.class);
        if (powerManager != null) {
            return powerManager.isInteractive();
        }
        Slog.wtf(TAG, "PowerManager null while starting CachedDeviceStateService");
        return false;
    }
}
