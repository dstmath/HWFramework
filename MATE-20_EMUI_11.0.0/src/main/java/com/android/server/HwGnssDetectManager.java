package com.android.server;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GnssStatus;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import com.android.server.LocationManagerService;
import com.android.server.location.LBSLog;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class HwGnssDetectManager {
    private static final int BLOCK_LEVEL_EXTREMELY_BAD = 1;
    private static final long DETECT_INTERVAL = 3600000;
    private static final String EXTRA_BLOCKING_TYPE = "extra_blocking_type";
    private static final int GNSS_DETECT_TIME = 3;
    private static final String GNSS_DETECT_TIME_FLAG = "gnss_cn0_detect_time_flag";
    private static final String GPS_FAULT_CODE = "631001001";
    private static final String GPS_FAULT_DESCRIPTION_CODE = "831001001";
    private static final String GPS_FAULT_NAME = "GPS_Switch_Fault";
    private static final String GPS_FAULT_SUGGESTION_CODE = "531001001";
    private static final String GPS_HANDLE_TYPE = "3";
    private static final String GPS_SIGNALWEAK_DESCRIPTION_CODE = "831001024";
    private static final String GPS_SIGNALWEAK_SUGGESTION_CODE = "531001024";
    private static final String HW_ID = "com.huawei.hwid";
    private static final String HW_LBS_SERVICE = "com.huawei.lbs";
    private static final int INDEX_0 = 0;
    private static final int INDEX_1 = 1;
    private static final int INDEX_2 = 2;
    private static final int IS_SUPPORT_GNSS_DETECT = 1;
    private static final String IS_SUPPORT_GNSS_DETECT_FLAG = "is_gnss_support_detect_flag";
    private static final String KEY_EXTRA_INFO = "EXTRA_INFO";
    private static final String KEY_FAULT_CODE = "FAULT_CODE";
    private static final String KEY_FAULT_DESCRIPTION = "FAULT_DESCRIPTION";
    private static final String KEY_FAULT_NAME = "FAULT_NAME";
    private static final String KEY_FAULT_SUGGESTION = "FAULT_SUGGESTION";
    private static final String KEY_HANDLE_TYPE = "HANDLE_TYPE";
    private static final String KEY_REPAIR_ID = "REPAIR_ID";
    private static final String LOW_COUNT_CN0_VALUE_FLAG = "low_count_cn0_value_flag";
    private static final int MG_MAX_VALUE_THRESHOLD = 4000;
    private static final int MG_MIN_VALUE_THRESHOLD = 1000;
    private static final String MG_VALUE_THRESHOLD_FLAG = "mg_value_threshold_flag";
    private static final int MSG_DEAL_GPS_DETECTION = 100;
    private static final int MSG_INIT_GPS_DETECTION = 101;
    private static final int MSG_MAGBETIC_VALUE_CHECK = 105;
    private static final int MSG_UNREGISTER_CALLBACK = 106;
    private static final int SENEOR_DETECT_TIME = 20000;
    private static final String SMART_FAULT_ACTION = "huawei.intent.action.SMART_NOTIFY_FAULT";
    private static final String SMART_FAULT_CLASS_NAME = "com.huawei.hwdetectrepair.smartnotify.eventlistener.InstantMessageReceiver";
    private static final String SMART_FAULT_PACKAGE_NAME = "com.huawei.hwdetectrepair";
    private static final String SMART_FAULT_PERMISSION = "huawei.permission.SMART_NOTIFY_FAULT";
    private static final String TAG = "HwGnssDetectManager";
    private static volatile HwGnssDetectManager sHwGnssDetectManager;
    private HashSet<LocationManagerService.Receiver> detectAppSet = new HashSet<>();
    private GpsDetectionHandler detectionHandler;
    private boolean isExistRegisterCn0Linstener = false;
    private boolean isSupportGnssSignalDetect = true;
    private Context mContext;
    private int mGnssDetectDealy = 3;
    private GnssStatusListener mGnssListener;
    private HandlerThread mHandlerThread;
    private HashMap<String, Long> mLastDetectTimeOfPkgs = new HashMap<>();
    private LocationManager mLocationManager;
    private int mLowCn0Value = 25;
    private int mLowMeanCn0Count = 0;
    private PackageManager mPackageManager;
    private MgValueSensorListener mSensorListener;
    private SensorManager mSensorManager;
    private Sensor magneticSensor;
    private int mgTotalCount = 0;
    private int mgValueCount = 0;
    private int mgValueThreshold = 1000;
    private LocationManagerServiceUtil util;

    static /* synthetic */ int access$008(HwGnssDetectManager x0) {
        int i = x0.mLowMeanCn0Count;
        x0.mLowMeanCn0Count = i + 1;
        return i;
    }

    static /* synthetic */ int access$408(HwGnssDetectManager x0) {
        int i = x0.mgTotalCount;
        x0.mgTotalCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$508(HwGnssDetectManager x0) {
        int i = x0.mgValueCount;
        x0.mgValueCount = i + 1;
        return i;
    }

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
        if (sHwGnssDetectManager == null) {
            synchronized (HwGnssDetectManager.class) {
                if (sHwGnssDetectManager == null) {
                    sHwGnssDetectManager = new HwGnssDetectManager(context);
                }
            }
        }
        return sHwGnssDetectManager;
    }

    private HwGnssDetectManager(Context context) {
        this.mContext = context;
        this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mHandlerThread = new HandlerThread("DetectManager");
        this.mHandlerThread.start();
        this.detectionHandler = new GpsDetectionHandler(this.mHandlerThread.getLooper());
        this.mPackageManager = this.mContext.getPackageManager();
        this.mSensorListener = new MgValueSensorListener();
        this.magneticSensor = this.mSensorManager.getDefaultSensor(14);
        this.util = LocationManagerServiceUtil.getDefault();
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
        if ("passive".equals(request.getProvider())) {
            return false;
        }
        if (!((PowerManager) this.mContext.getSystemService("power")).isScreenOn()) {
            LBSLog.i(TAG, false, "now is not screen on, need not detect!", new Object[0]);
            return false;
        } else if (!isForeGroundProc(packageName)) {
            LBSLog.i(TAG, false, "package %{public}s is not foreground, need not detect!", packageName);
            return false;
        } else {
            long now = SystemClock.elapsedRealtime();
            Long lastDetectTime = this.mLastDetectTimeOfPkgs.get(packageName);
            if (lastDetectTime != null && now - lastDetectTime.longValue() < 3600000) {
                return false;
            }
            this.mLastDetectTimeOfPkgs.put(packageName, Long.valueOf(now));
            return true;
        }
    }

    private ArrayList<String> getCommonDetect() {
        boolean isLocationEnabled;
        boolean isHighAcc = false;
        int locationMode = Settings.Secure.getInt(this.mContext.getContentResolver(), "location_mode", 0);
        if (locationMode == 0) {
            isLocationEnabled = false;
        } else {
            isLocationEnabled = true;
        }
        if (locationMode == 3) {
            isHighAcc = true;
        }
        boolean isWifiScanOpen = Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 0) == 1;
        LBSLog.i(TAG, false, "GPS common islocationEnabled %{public}b,Mode %{public}d, isHAcc %{public}b,isWifiScanOpen %{public}b", Boolean.valueOf(isLocationEnabled), Integer.valueOf(locationMode), Boolean.valueOf(isHighAcc), Boolean.valueOf(isWifiScanOpen));
        ArrayList<String> commonDetectedRepairIds = new ArrayList<>();
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

    public void startGnssDetected(LocationRequest request, LocationManagerService.Receiver receiver, String packageName) {
        this.isSupportGnssSignalDetect = Settings.Global.getInt(this.mContext.getContentResolver(), IS_SUPPORT_GNSS_DETECT_FLAG, 1) == 1;
        if (this.isSupportGnssSignalDetect && this.mPackageManager.hasSystemFeature("android.hardware.location.gps") && this.magneticSensor != null) {
            if (this.detectionHandler == null) {
                LBSLog.i(TAG, false, "Gnss Detection handler is null", new Object[0]);
            } else if (this.util == null) {
                LBSLog.i(TAG, false, "LocationManagerServiceUtil is null", new Object[0]);
            } else if (this.detectAppSet != null && isNavigatingApp(request, packageName)) {
                this.detectAppSet.add(receiver);
                if (this.isExistRegisterCn0Linstener) {
                    LBSLog.i(TAG, false, "Gnss Detection Process is exist", new Object[0]);
                    return;
                }
                this.mgValueThreshold = Settings.Global.getInt(this.mContext.getContentResolver(), MG_VALUE_THRESHOLD_FLAG, 1000);
                this.mGnssDetectDealy = Settings.Global.getInt(this.mContext.getContentResolver(), GNSS_DETECT_TIME_FLAG, 3);
                this.isExistRegisterCn0Linstener = true;
                this.detectionHandler.sendEmptyMessageDelayed(101, (long) (this.mGnssDetectDealy * 60 * 1000));
            }
        }
    }

    private boolean isNavigatingApp(LocationRequest request, String pkgName) {
        if (request.getWorkSource() == null || (!HW_LBS_SERVICE.equals(pkgName) && !HW_ID.equals(pkgName))) {
            return this.util.isMapTypeNavigatingApp(pkgName);
        }
        for (int i = 0; i < request.getWorkSource().size(); i++) {
            if (this.util.isMapTypeNavigatingApp(request.getWorkSource().getName(i))) {
                return true;
            }
        }
        return false;
    }

    public void cancelGnssSignalDetection(LocationManagerService.Receiver receiver) {
        GpsDetectionHandler gpsDetectionHandler;
        HashSet<LocationManagerService.Receiver> hashSet = this.detectAppSet;
        if (hashSet != null && hashSet.contains(receiver)) {
            this.detectAppSet.remove(receiver);
        }
        if (this.detectAppSet.size() == 0 && (gpsDetectionHandler = this.detectionHandler) != null) {
            this.isExistRegisterCn0Linstener = false;
            gpsDetectionHandler.sendEmptyMessage(106);
            this.detectAppSet.clear();
        }
    }

    public class GnssStatusListener extends GnssStatus.Callback {
        public GnssStatusListener() {
        }

        @Override // android.location.GnssStatus.Callback
        public void onStarted() {
        }

        @Override // android.location.GnssStatus.Callback
        public void onStopped() {
        }

        @Override // android.location.GnssStatus.Callback
        public void onFirstFix(int ttffMillis) {
        }

        @Override // android.location.GnssStatus.Callback
        public void onSatelliteStatusChanged(GnssStatus status) {
            if (HwGnssDetectManager.this.mLowMeanCn0Count > 60) {
                HwGnssDetectManager.this.mLowMeanCn0Count = 0;
                HwGnssDetectManager.this.detectionHandler.sendEmptyMessage(100);
            }
            double top5cn0MeanValue = HwGnssDetectManager.this.getSatelliteStatusTopCn0(status);
            if (top5cn0MeanValue >= ((double) HwGnssDetectManager.this.mLowCn0Value) || Math.abs(top5cn0MeanValue - 0.0d) <= 1.0E-6d) {
                HwGnssDetectManager.this.mLowMeanCn0Count = 0;
            } else {
                HwGnssDetectManager.access$008(HwGnssDetectManager.this);
            }
        }
    }

    public class MgValueSensorListener implements SensorEventListener {
        public MgValueSensorListener() {
        }

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == 14) {
                HwGnssDetectManager.access$408(HwGnssDetectManager.this);
                double x = (double) event.values[0];
                double y = (double) event.values[1];
                double z = (double) event.values[2];
                double magneticValue = Math.sqrt((x * x) + (y * y) + (z * z));
                if (magneticValue >= 1000.0d && magneticValue <= 4000.0d) {
                    LBSLog.i(HwGnssDetectManager.TAG, false, "mgValueCount ++ ", new Object[0]);
                    HwGnssDetectManager.access$508(HwGnssDetectManager.this);
                }
            }
        }

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private double getSatelliteStatusTopCn0(GnssStatus gnssStatus) {
        int satelliteCount = gnssStatus.getSatelliteCount();
        ArrayList<Float> cn0DbHzList = new ArrayList<>();
        for (int i = 0; i < satelliteCount; i++) {
            float cn0Value = gnssStatus.getCn0DbHz(i);
            if (((double) Math.abs(cn0Value - 0.0f)) > 1.0E-6d) {
                cn0DbHzList.add(Float.valueOf(cn0Value));
            }
        }
        double mean = 0.0d;
        Collections.reverse(cn0DbHzList);
        if (cn0DbHzList.size() < 5) {
            return 0.0d;
        }
        LBSLog.i(TAG, false, "cn0DbHzList size() is %{public}d", Integer.valueOf(cn0DbHzList.size()));
        for (int i2 = 0; i2 < 5; i2++) {
            mean += (double) cn0DbHzList.get(i2).floatValue();
        }
        return mean / 5.0d;
    }

    /* access modifiers changed from: private */
    public class GpsDetectionHandler extends Handler {
        GpsDetectionHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 100) {
                LBSLog.i(HwGnssDetectManager.TAG, false, "start check gps detection", new Object[0]);
                if (!HwGnssDetectManager.this.detectionHandler.hasMessages(105)) {
                    HwGnssDetectManager.this.mgValueCount = 0;
                    HwGnssDetectManager.this.mgTotalCount = 0;
                    SensorManager sensorManager = HwGnssDetectManager.this.mSensorManager;
                    MgValueSensorListener mgValueSensorListener = HwGnssDetectManager.this.mSensorListener;
                    Sensor sensor = HwGnssDetectManager.this.magneticSensor;
                    SensorManager unused = HwGnssDetectManager.this.mSensorManager;
                    boolean isSuccess = sensorManager.registerListener(mgValueSensorListener, sensor, 0);
                    LBSLog.i(HwGnssDetectManager.TAG, "registerLinstener is " + isSuccess);
                    HwGnssDetectManager.this.detectionHandler.sendEmptyMessageDelayed(105, 20000);
                }
            } else if (i == 101) {
                LBSLog.i(HwGnssDetectManager.TAG, false, "init to Test GPS", new Object[0]);
                if (HwGnssDetectManager.this.mGnssListener == null) {
                    HwGnssDetectManager hwGnssDetectManager = HwGnssDetectManager.this;
                    hwGnssDetectManager.mGnssListener = new GnssStatusListener();
                }
                HwGnssDetectManager hwGnssDetectManager2 = HwGnssDetectManager.this;
                hwGnssDetectManager2.mLowCn0Value = Settings.Global.getInt(hwGnssDetectManager2.mContext.getContentResolver(), HwGnssDetectManager.LOW_COUNT_CN0_VALUE_FLAG, HwGnssDetectManager.this.mLowCn0Value);
                LBSLog.i(HwGnssDetectManager.TAG, false, "registerGnssStatusCallback", new Object[0]);
                HwGnssDetectManager.this.mLowMeanCn0Count = 0;
                HwGnssDetectManager.this.mLocationManager.registerGnssStatusCallback(HwGnssDetectManager.this.mGnssListener);
            } else if (i == 105) {
                LBSLog.i(HwGnssDetectManager.TAG, false, "start check MAGBETIC_VALUE", new Object[0]);
                HwGnssDetectManager.this.mSensorManager.unregisterListener(HwGnssDetectManager.this.mSensorListener, HwGnssDetectManager.this.magneticSensor);
                if (HwGnssDetectManager.this.mgTotalCount != 0) {
                    try {
                        if (new BigDecimal(HwGnssDetectManager.this.mgValueCount).divide(new BigDecimal(HwGnssDetectManager.this.mgTotalCount), 2).doubleValue() > 0.7d) {
                            HwGnssDetectManager.this.sendBroadCastForSignalWeak();
                            HwGnssDetectManager.this.mgTotalCount = 0;
                            HwGnssDetectManager.this.mgValueCount = 0;
                            LBSLog.i(HwGnssDetectManager.TAG, false, "remove all token & messages", new Object[0]);
                            HwGnssDetectManager.this.mLocationManager.unregisterGnssStatusCallback(HwGnssDetectManager.this.mGnssListener);
                            return;
                        }
                        HwGnssDetectManager.this.mgTotalCount = 0;
                        HwGnssDetectManager.this.mgValueCount = 0;
                        HwGnssDetectManager.this.mLowMeanCn0Count = 0;
                    } catch (ArithmeticException e) {
                        LBSLog.i(HwGnssDetectManager.TAG, false, "ArithmeticException exception", new Object[0]);
                    }
                }
            } else if (i != 106) {
                LBSLog.e(HwGnssDetectManager.TAG, false, "do not support this message", new Object[0]);
            } else {
                HwGnssDetectManager.this.unregisterCallback();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterCallback() {
        LocationManager locationManager = this.mLocationManager;
        if (locationManager != null) {
            locationManager.unregisterGnssStatusCallback(this.mGnssListener);
        }
        SensorManager sensorManager = this.mSensorManager;
        if (sensorManager != null) {
            sensorManager.unregisterListener(this.mSensorListener, this.magneticSensor);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendBroadCastForSignalWeak() {
        LBSLog.i(TAG, false, "send broadcast to smart hint", new Object[0]);
        Intent intent = new Intent(SMART_FAULT_ACTION);
        intent.setClassName(SMART_FAULT_PACKAGE_NAME, SMART_FAULT_CLASS_NAME);
        intent.putExtra(EXTRA_BLOCKING_TYPE, 1);
        intent.putExtra(KEY_FAULT_DESCRIPTION, GPS_SIGNALWEAK_DESCRIPTION_CODE);
        intent.putExtra(KEY_FAULT_SUGGESTION, GPS_SIGNALWEAK_SUGGESTION_CODE);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, SMART_FAULT_PERMISSION);
    }
}
