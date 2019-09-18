package com.android.server;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.location.LocationRequest;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import com.android.server.LocationManagerService;
import java.util.ArrayList;
import java.util.HashMap;

public class HwGnssDetectManager {
    private static final long DETECT_INTERVAL = 3600000;
    private static final String GPS_FAULT_CODE = "631001001";
    private static final String GPS_FAULT_DESCRIPTION_CODE = "831001001";
    private static final String GPS_FAULT_NAME = "GPS_Switch_Fault";
    private static final String GPS_FAULT_SUGGESTION_CODE = "531001001";
    private static final String GPS_HANDLE_TYPE = "3";
    private static final String KEY_EXTRA_INFO = "EXTRA_INFO";
    private static final String KEY_FAULT_CODE = "FAULT_CODE";
    private static final String KEY_FAULT_DESCRIPTION = "FAULT_DESCRIPTION";
    private static final String KEY_FAULT_NAME = "FAULT_NAME";
    private static final String KEY_FAULT_SUGGESTION = "FAULT_SUGGESTION";
    private static final String KEY_HANDLE_TYPE = "HANDLE_TYPE";
    private static final String KEY_REPAIR_ID = "REPAIR_ID";
    private static final String SMART_FAULT_ACTION = "huawei.intent.action.SMART_NOTIFY_FAULT";
    private static final String SMART_FAULT_CLASS_NAME = "com.huawei.hwdetectrepair.smartnotify.eventlistener.InstantMessageReceiver";
    private static final String SMART_FAULT_PACKAGE_NAME = "com.huawei.hwdetectrepair";
    private static final String SMART_FAULT_PERMISSION = "huawei.permission.SMART_NOTIFY_FAULT";
    private static final String TAG = "HwGnssDetectManager";
    private static volatile HwGnssDetectManager mHwGnssDetectManager;
    private Context mContext;
    private HashMap<String, Long> mLastDetectTimeOfPkgs = new HashMap<>();

    private static class GPSRepairid {
        static final String REPAIR_SETTING_GPS_MODE_SWITCH_VALUE1 = "REPAIR_SETTING_GPS_MODE_SWITCH_VALUE1";
        static final String REPAIR_SETTING_GPS_MODE_SWITCH_VALUE2 = "REPAIR_SETTING_GPS_MODE_SWITCH_VALUE2";
        static final String REPAIR_SETTING_GPS_MODE_SWITCH_VALUE3 = "REPAIR_SETTING_GPS_MODE_SWITCH_VALUE3";
        static final String REPAIR_SETTING_GPS_SWITCH_OFF = "REPAIR_SETTING_GPS_SWITCH_OFF";
        static final String REPAIR_SETTING_GPS_SWITCH_ON = "REPAIR_SETTING_GPS_SWITCH_ON";
        static final String REPAIR_SETTING_WLAN_SCAN_SWITCH_OFF = "REPAIR_SETTING_WLAN_SCAN_SWITCH_OFF";
        static final String REPAIR_SETTING_WLAN_SCAN_SWITCH_ON = "REPAIR_SETTING_WLAN_SCAN_SWITCH_ON";

        private GPSRepairid() {
        }
    }

    public static HwGnssDetectManager getInstance(Context context) {
        if (mHwGnssDetectManager == null) {
            synchronized (HwGnssDetectManager.class) {
                if (mHwGnssDetectManager == null) {
                    mHwGnssDetectManager = new HwGnssDetectManager(context);
                }
            }
        }
        return mHwGnssDetectManager;
    }

    private HwGnssDetectManager(Context context) {
        this.mContext = context;
    }

    public void hwRequestLocationUpdatesLocked(LocationRequest request, LocationManagerService.Receiver receiver, int pid, int uid, String packageName) {
        if (shouldDetect(request, receiver, pid, uid, packageName)) {
            broadcastDetect(packageName, getCommonDetect());
        }
    }

    public ArrayList<String> gnssDetect(String packageName) {
        return getCommonDetect();
    }

    private boolean shouldDetect(LocationRequest request, LocationManagerService.Receiver receiver, int pid, int uid, String packageName) {
        String str = packageName;
        if ("passive".equals(request.getProvider())) {
            return false;
        }
        if (!((PowerManager) this.mContext.getSystemService("power")).isScreenOn()) {
            Log.d(TAG, "now is not screen on, need not detect!");
            return false;
        } else if (!isForeGroundProc(str)) {
            Log.d(TAG, "package " + str + " is not foreground, need not detect!");
            return false;
        } else {
            long now = SystemClock.elapsedRealtime();
            Long lastDetectTime = this.mLastDetectTimeOfPkgs.get(str);
            if (lastDetectTime != null && now - lastDetectTime.longValue() < 3600000) {
                return false;
            }
            this.mLastDetectTimeOfPkgs.put(str, Long.valueOf(now));
            return true;
        }
    }

    private ArrayList<String> getCommonDetect() {
        boolean isLocationEnabled;
        boolean isWifiScanOpen;
        boolean isHighAcc = false;
        ArrayList<String> commonDetectedRepairIds = new ArrayList<>();
        boolean isWifiScanOpen2 = false;
        int locationMode = Settings.Secure.getInt(this.mContext.getContentResolver(), "location_mode", 0);
        if (locationMode == 0) {
            isLocationEnabled = false;
        } else {
            isLocationEnabled = true;
        }
        if (locationMode == 3) {
            isHighAcc = true;
        }
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 0) == 1) {
            isWifiScanOpen2 = true;
        }
        Log.d(TAG, "GPS common detect " + " isLocationEnabled " + isLocationEnabled + " locationMode " + locationMode + " isHighAcc " + isHighAcc + " isWifiScanOpen " + isWifiScanOpen);
        if (!isLocationEnabled) {
            commonDetectedRepairIds.add("REPAIR_SETTING_GPS_SWITCH_ON");
        }
        if (isLocationEnabled && !isHighAcc) {
            commonDetectedRepairIds.add("REPAIR_SETTING_GPS_MODE_SWITCH_VALUE1");
        }
        if (!isWifiScanOpen) {
            commonDetectedRepairIds.add("REPAIR_SETTING_WLAN_SCAN_SWITCH_ON");
        }
        return commonDetectedRepairIds;
    }

    private void broadcastDetect(String extraInfo, ArrayList<String> repairIds) {
        if (repairIds != null && !repairIds.isEmpty()) {
            Intent intent = new Intent(SMART_FAULT_ACTION);
            intent.setClassName(SMART_FAULT_PACKAGE_NAME, SMART_FAULT_CLASS_NAME);
            intent.putExtra(KEY_EXTRA_INFO, extraInfo);
            intent.putExtra(KEY_HANDLE_TYPE, "3");
            intent.putExtra(KEY_FAULT_NAME, GPS_FAULT_NAME);
            intent.putExtra(KEY_FAULT_CODE, GPS_FAULT_CODE);
            intent.putExtra(KEY_FAULT_DESCRIPTION, GPS_FAULT_DESCRIPTION_CODE);
            intent.putExtra(KEY_FAULT_SUGGESTION, GPS_FAULT_SUGGESTION_CODE);
            intent.putStringArrayListExtra(KEY_REPAIR_ID, repairIds);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, SMART_FAULT_PERMISSION);
        }
    }

    private boolean isForeGroundProc(String packageName) {
        return ((ActivityManager) this.mContext.getSystemService("activity")).getPackageImportance(packageName) <= 125;
    }
}
