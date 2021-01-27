package com.android.server.wifi;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.wifi.HwHiLog;

public class HwWifiSettingsStore implements IHwWifiSettingsStoreEx {
    private static final int AIRPALNE_ENABLE_WIFI_ALL_AREA = 1;
    private static final int AIRPALNE_ENABLE_WIFI_CHINA_AREA = 2;
    private static final int AIRPALNE_ENABLE_WIFI_OVERSEA = 4;
    private static final int AIRPALNE_ENABLE_WIFI_PROP = SystemProperties.getInt("hw_mc.connectivity.airplane_enable_wifi_bt", 2);
    private static final boolean IS_CHINA_AREA = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
    private static final String TAG = "HwWifiSettingsStore";
    private static volatile HwWifiSettingsStore sHwWifiSettingsStore = null;
    private final int isLocationModeEnabled = Settings.Secure.getInt(this.mContext.getContentResolver(), "location_mode", 0);
    private Context mContext;

    private HwWifiSettingsStore(Context context) {
        this.mContext = context;
        initAirplaneModeRadios();
    }

    public static HwWifiSettingsStore createHwWifiSettingsStore(Context context) {
        if (sHwWifiSettingsStore == null) {
            synchronized (HwWifiSettingsStore.class) {
                if (sHwWifiSettingsStore == null) {
                    sHwWifiSettingsStore = new HwWifiSettingsStore(context);
                }
            }
        }
        return sHwWifiSettingsStore;
    }

    private boolean isWifiEnabledWhenAirplaneSupported() {
        int i = AIRPALNE_ENABLE_WIFI_PROP;
        boolean isSupport = (i & 1) != 0 || ((i & 2) != 0 && IS_CHINA_AREA) || ((AIRPALNE_ENABLE_WIFI_PROP & 4) != 0 && !IS_CHINA_AREA);
        HwHiLog.i(TAG, false, "isWifiEnabledWhenAirplaneSupported=%{public}s", new Object[]{String.valueOf(isSupport)});
        return isSupport;
    }

    private void initAirplaneModeRadios() {
        if (!isWifiEnabledWhenAirplaneSupported()) {
            resetAirplaneModeRadios();
        }
    }

    public synchronized void resetAirplaneModeRadios() {
        String airplaneModeRadiosInit;
        String airplaneModeRadios = Settings.Global.getString(this.mContext.getContentResolver(), "airplane_mode_radios");
        if (airplaneModeRadios != null) {
            if (!airplaneModeRadios.contains("wifi")) {
                if (!airplaneModeRadios.isEmpty()) {
                    airplaneModeRadiosInit = airplaneModeRadios + ",wifi";
                } else {
                    airplaneModeRadiosInit = "wifi";
                }
                Settings.Global.putString(this.mContext.getContentResolver(), "airplane_mode_radios", airplaneModeRadiosInit);
            }
        }
    }

    public int getLocationMode() {
        return this.isLocationModeEnabled;
    }

    public void changeAirplaneModeRadios(boolean isAirplaneModeOn, boolean isWifiEnable) {
        String airplaneModeRadiosWhenWifiOff;
        String airplaneModeRadiosWhenWifiOn;
        if (this.mContext == null || !isWifiEnabledWhenAirplaneSupported() || !isAirplaneModeOn) {
            HwHiLog.i(TAG, false, "Do not support to enable wifi when Airplane is on", new Object[0]);
            return;
        }
        String airplaneModeRadios = Settings.Global.getString(this.mContext.getContentResolver(), "airplane_mode_radios");
        boolean isAirplaneSensitive = airplaneModeRadios == null || airplaneModeRadios.contains("wifi");
        HwHiLog.i(TAG, false, "wifiEnable=%{public}s isAirplaneSensitive=%{public}s", new Object[]{String.valueOf(isWifiEnable), String.valueOf(isAirplaneSensitive)});
        if (!isWifiEnable || !isAirplaneSensitive) {
            if (isWifiEnable || isAirplaneSensitive) {
                HwHiLog.i(TAG, false, "Not support to set wifi state in airplane mode.", new Object[0]);
                return;
            }
            if (!airplaneModeRadios.isEmpty()) {
                airplaneModeRadiosWhenWifiOff = airplaneModeRadios + ",wifi";
            } else {
                airplaneModeRadiosWhenWifiOff = "wifi";
            }
            Settings.Global.putString(this.mContext.getContentResolver(), "airplane_mode_radios", airplaneModeRadiosWhenWifiOff);
        } else if (airplaneModeRadios == null) {
            HwHiLog.i(TAG, false, "airplaneModeRadios is null", new Object[0]);
        } else {
            if (!airplaneModeRadios.equals("wifi")) {
                airplaneModeRadiosWhenWifiOn = airplaneModeRadios.replaceAll(",wifi|wifi,", "");
            } else {
                airplaneModeRadiosWhenWifiOn = "";
            }
            Settings.Global.putString(this.mContext.getContentResolver(), "airplane_mode_radios", airplaneModeRadiosWhenWifiOn);
        }
    }
}
