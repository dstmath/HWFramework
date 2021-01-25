package com.android.server.wifi.cast;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.MSS.HwMSSUtils;

public class CastOptUtils {
    protected static final int CMD_SET_GO_CSA_CHANNEL = 161;
    protected static final int CMD_SET_GO_RADAR_DETECT = 163;
    private static final String DEVICE_TYPE_DEFAULT = "default";
    private static final String DEVICE_TYPE_PAD = "tablet";
    protected static final int DEVICE_TYPE_PAD_ID = 1;
    protected static final int DEVICE_TYPE_PC_ID = 3;
    protected static final int DEVICE_TYPE_PHONE_ID = 0;
    private static final String DEVICE_TYPE_TV = "tv";
    protected static final int DEVICE_TYPE_TV_ID = 2;
    protected static final int INVALID_DEVICE_TYPE = -1;
    private static final boolean IS_CAST_OPT_ENABLE = SystemProperties.getBoolean("ro.config.hw_wifi_cast_opt", true);
    protected static final boolean IS_CN_AREA = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    private static final int P2P_ALLOW_DFS_CN = 1;
    private static final int P2P_ALLOW_DFS_OVERSEA = 2;
    private static final int P2P_ALLOW_INDOOR_CN = 4;
    private static final int P2P_ALLOW_INDOOR_OVERSEA = 8;
    protected static final String P2P_INTERFACE = "p2p0";
    private static final int PROP_P2P_ALLOW_CHANNELS = SystemProperties.getInt("hw_mc.wifi.p2p_allow_dfs_indoor", 13);
    private static final int SYNC_WIFI_SWITCH_OFF = 0;
    private static final int SYNC_WIFI_SWITCH_ON = 1;
    private static final String TAG = "CastOptUtils";
    private static final String WIFI_SYNC_CONFIGURATION_SWITCH = "wifi_configuration_sync";

    private CastOptUtils() {
    }

    protected static boolean isCastOptSupported() {
        boolean isSupport = IS_CAST_OPT_ENABLE && (HwMSSUtils.is1103() || HwMSSUtils.is1105() || HwMSSUtils.is1102A());
        HwHiLog.i(TAG, false, "isCastOptSupported: %{public}s", new Object[]{String.valueOf(isSupport)});
        return isSupport;
    }

    protected static boolean isP2pRadarDetectSupported() {
        boolean isSupport = HwMSSUtils.is1105();
        HwHiLog.i(TAG, false, "isP2pRadarDetectSupported: %{public}s", new Object[]{String.valueOf(isSupport)});
        return isSupport;
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0042  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0054  */
    protected static int getDeviceType() {
        char c;
        String deviceType = SystemProperties.get("ro.build.characteristics", "");
        int deviceTypeId = -1;
        int hashCode = deviceType.hashCode();
        if (hashCode != -881377690) {
            if (hashCode != 3714) {
                if (hashCode == 1544803905 && deviceType.equals(DEVICE_TYPE_DEFAULT)) {
                    c = 0;
                    if (c != 0) {
                        deviceTypeId = 0;
                    } else if (c == 1) {
                        deviceTypeId = 1;
                    } else if (c != 2) {
                        HwHiLog.i(TAG, false, "getDeviceType: unknown device type %{public}s", new Object[]{deviceType});
                    } else {
                        deviceTypeId = 2;
                    }
                    HwHiLog.i(TAG, false, "getDeviceType: %{public}d", new Object[]{Integer.valueOf(deviceTypeId)});
                    return deviceTypeId;
                }
            } else if (deviceType.equals(DEVICE_TYPE_TV)) {
                c = 2;
                if (c != 0) {
                }
                HwHiLog.i(TAG, false, "getDeviceType: %{public}d", new Object[]{Integer.valueOf(deviceTypeId)});
                return deviceTypeId;
            }
        } else if (deviceType.equals(DEVICE_TYPE_PAD)) {
            c = 1;
            if (c != 0) {
            }
            HwHiLog.i(TAG, false, "getDeviceType: %{public}d", new Object[]{Integer.valueOf(deviceTypeId)});
            return deviceTypeId;
        }
        c = 65535;
        if (c != 0) {
        }
        HwHiLog.i(TAG, false, "getDeviceType: %{public}d", new Object[]{Integer.valueOf(deviceTypeId)});
        return deviceTypeId;
    }

    protected static boolean isDbdcSupported() {
        boolean isSupport = HwMSSUtils.is1103() || HwMSSUtils.is1105();
        HwHiLog.i(TAG, false, "isDbdcSupported: %{public}s", new Object[]{String.valueOf(isSupport)});
        return isSupport;
    }

    protected static boolean isCsaSupported() {
        boolean isSupport = HwMSSUtils.is1103() || HwMSSUtils.is1105();
        HwHiLog.i(TAG, false, "isCsaSupported: %{public}s", new Object[]{String.valueOf(isSupport)});
        return isSupport;
    }

    protected static boolean isSyncWifiConfigSwitchOn(Context context) {
        return context != null && Settings.System.getInt(context.getContentResolver(), WIFI_SYNC_CONFIGURATION_SWITCH, 0) == 1;
    }

    protected static String createQuotedSsid(String ssid) {
        return "\"" + ssid + "\"";
    }

    protected static boolean isAllowDfsChannels() {
        if ((PROP_P2P_ALLOW_CHANNELS & 1) != 1 || !IS_CN_AREA) {
            return (PROP_P2P_ALLOW_CHANNELS & 2) == 2 && !IS_CN_AREA;
        }
        return true;
    }

    protected static boolean isAllowIndoorChannels() {
        return ((PROP_P2P_ALLOW_CHANNELS & 4) == 4 && IS_CN_AREA) || ((PROP_P2P_ALLOW_CHANNELS & 8) == 8 && !IS_CN_AREA);
    }
}
