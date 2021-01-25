package com.android.server.wifi;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;

public class HwWifiSpeedMeasure {
    private static final String KEY_AP_VENDOR = "ApVendor";
    private static final String KEY_LOCAL_DOWNLINK_SPEED = "LocalDownlinkSpeed";
    private static final String KEY_LOCAL_PING_LATENCY = "LoalPingLatency";
    private static final String KEY_LOCAL_UPLINK_SPEED = "LocalUplinkSpeed";
    private static final String KEY_NETWORK_DOWNLINK_SPEED = "NetworkDownlinkSpeed";
    private static final String KEY_NETWORK_PING_LATENCY = "NetworkPingLatency";
    private static final String KEY_NETWORK_UPLINK_SPEED = "NetworkUplinkSpeed";
    private static final int LEN_OF_VERSION1 = 8;
    private static final String SEPARATOR = ",";
    private static final String TAG = "HwWifiSpeedMeasure";
    private static final int VERSION_NUM_1 = 1;
    public String apVendor = "";
    public int localDownLinkSpeed = 0;
    public int localPingLatency = 0;
    public int localUpLinkSpeed = 0;
    public int networkDownLinkSpeed = 0;
    public int networkPingLatency = 0;
    public int networkUpLinkSpeed = 0;
    public int version = 0;

    private HwWifiSpeedMeasure() {
    }

    public static boolean reportSpeedMeasureResult(HwWifiCHRService hwWifiChrService, String info) {
        if (hwWifiChrService == null) {
            HwHiLog.d(TAG, false, "reportSpeedMeasureResult: hwWifiChrService is null", new Object[0]);
            return false;
        }
        HwWifiSpeedMeasure speedMeasureResult = createFromString(info);
        if (speedMeasureResult == null) {
            HwHiLog.d(TAG, false, "invalid info: %{public}s", new Object[]{info});
            return false;
        }
        HwHiLog.d(TAG, false, "info: %{public}s, result: %{public}s", new Object[]{info, speedMeasureResult.toString()});
        Bundle data = new Bundle();
        data.putString(KEY_AP_VENDOR, speedMeasureResult.apVendor);
        data.putInt(KEY_LOCAL_PING_LATENCY, speedMeasureResult.localPingLatency);
        data.putInt(KEY_LOCAL_UPLINK_SPEED, speedMeasureResult.localUpLinkSpeed);
        data.putInt(KEY_LOCAL_DOWNLINK_SPEED, speedMeasureResult.localDownLinkSpeed);
        data.putInt(KEY_NETWORK_PING_LATENCY, speedMeasureResult.networkPingLatency);
        data.putInt(KEY_NETWORK_UPLINK_SPEED, speedMeasureResult.networkUpLinkSpeed);
        data.putInt(KEY_NETWORK_DOWNLINK_SPEED, speedMeasureResult.networkDownLinkSpeed);
        hwWifiChrService.uploadDFTEvent(27, data);
        return true;
    }

    public static HwWifiSpeedMeasure createFromString(String info) {
        String[] items;
        if (TextUtils.isEmpty(info) || (items = info.split(SEPARATOR)) == null || items.length == 0) {
            return null;
        }
        try {
            int version2 = Integer.parseInt(items[0]);
            if (version2 == 1) {
                return parseVersion1(items);
            }
            HwHiLog.d(TAG, false, "createFromString: invalid version: %{public}d", new Object[]{Integer.valueOf(version2)});
            return null;
        } catch (NumberFormatException e) {
            HwHiLog.d(TAG, false, "createFromString: NumberFormatException", new Object[0]);
            return null;
        }
    }

    public String toString() {
        return "version:" + this.version + ",apVendor:" + this.apVendor + ",localPingLatency:" + this.localPingLatency + ",localUpLinkSpeed:" + this.localUpLinkSpeed + ",localDownLinkSpeed:" + this.localDownLinkSpeed + ",networkPingLatency:" + this.networkPingLatency + ",networkUpLinkSpeed:" + this.networkUpLinkSpeed + ",networkDownLinkSpeed:" + this.networkDownLinkSpeed;
    }

    private static HwWifiSpeedMeasure parseVersion1(String[] items) {
        if (items.length != 8) {
            HwHiLog.d(TAG, false, "parseVersion1: invalid length: %{public}d", new Object[]{Integer.valueOf(items.length)});
            return null;
        }
        HwWifiSpeedMeasure speedMeasureResult = new HwWifiSpeedMeasure();
        speedMeasureResult.version = 1;
        speedMeasureResult.apVendor = items[1];
        try {
            speedMeasureResult.localPingLatency = Integer.parseInt(items[2].trim());
            speedMeasureResult.localUpLinkSpeed = Integer.parseInt(items[3].trim());
            speedMeasureResult.localDownLinkSpeed = Integer.parseInt(items[4].trim());
            speedMeasureResult.networkPingLatency = Integer.parseInt(items[5].trim());
            speedMeasureResult.networkUpLinkSpeed = Integer.parseInt(items[6].trim());
            speedMeasureResult.networkDownLinkSpeed = Integer.parseInt(items[7].trim());
            return speedMeasureResult;
        } catch (NumberFormatException e) {
            HwHiLog.d(TAG, false, "error: NumberFormatException", new Object[0]);
            return null;
        }
    }
}
