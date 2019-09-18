package com.huawei.appgallery.assistant.service;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IGameObserver;
import com.huawei.android.app.IHwActivityNotifierEx;
import java.util.HashMap;
import java.util.Map;

public class AppGalleryAssistantService {
    private static final String ACTION_HMS_SERVICE = "com.huawei.hwid.NOTIFY_APP_STATE_SERVICE";
    private static final float CRITICAL_DOWN_ANGLE = -9.8f;
    private static final float CRITICAL_UP_ANGLE = 9.8f;
    private static final int GAME_TO_BACKGROUND = 0;
    private static final int GAME_TO_FOREGROUND = 1;
    private static final int GAME_TO_GAME = 3;
    private static final long HALF_DAY = 43200000;
    private static final String HMS_BUOY_NOTIFY_URI = "content://com.huawei.hwid.peripheralprovider";
    private static final String MAP_KEY_TIME = "time";
    private static final String MAP_KEY_VALUE = "value";
    private static final int MAP_VALUE_SHOW = 1;
    private static final long ONE_DAY = 86400000;
    private static final String PACKAGE_NAME_HMS = "com.huawei.hwid";
    private static final int SCREEN_ON_BY_FOREGROUND = 2;
    private static final int SDK_TYPE_GAMESDK = 1;
    private static final int SDK_TYPE_HMSSDK = 2;
    private static final int SDK_TYPE_NOSDK = 0;
    private static final String TAG = "AssistantService-901302";
    private static final long TIME_REVERSE_MAX = 3000;
    private static final String URI_ASSISTANT_SYNC = "content://com.huawei.gameassistant.provider.syncApp/sync";
    private static final String URI_HMS_SHOWBUOY = "content://com.huawei.hwid.gameservice.inshowbuoylist/showbuoy";
    private static AppGalleryAssistantService mAppGalleryAssistantService;
    private boolean isRegisterSensor = false;
    private String launcherPackageName;
    /* access modifiers changed from: private */
    public String mAppPackageName;
    /* access modifiers changed from: private */
    public final Context mContext;
    private String mGamePackageName;
    /* access modifiers changed from: private */
    public int mGameStatus = 0;
    private Sensor mGsensor;
    private SensorEventListener mGsensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.values[2] <= AppGalleryAssistantService.CRITICAL_DOWN_ANGLE && AppGalleryAssistantService.this.mReverseDownFlg < 0) {
                int unused = AppGalleryAssistantService.this.mReverseDownFlg = 0;
                long unused2 = AppGalleryAssistantService.this.timeStartReverse = System.currentTimeMillis();
            } else if (sensorEvent.values[2] >= AppGalleryAssistantService.CRITICAL_UP_ANGLE && AppGalleryAssistantService.this.mReverseDownFlg == 0) {
                int unused3 = AppGalleryAssistantService.this.mReverseDownFlg = -1;
                if (System.currentTimeMillis() - AppGalleryAssistantService.this.timeStartReverse > AppGalleryAssistantService.TIME_REVERSE_MAX) {
                    Log.d(AppGalleryAssistantService.TAG, "Reverse time more than 3s.");
                    return;
                }
                Log.d(AppGalleryAssistantService.TAG, "onReverseUp");
                AppGalleryAssistantService.this.onReverseUp();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };
    private Map<String, Map<String, Object>> mJointAppMap = new HashMap();
    /* access modifiers changed from: private */
    public int mReverseDownFlg = -1;
    private Map<String, Map<String, Object>> mSdkInfoMap = new HashMap();
    private SensorManager mSensorManager;
    private Map<String, Integer> mSystemAppMap = new HashMap();
    private long notifyAssistantTime = 0;
    /* access modifiers changed from: private */
    public long timeStartReverse = 0;

    private class HwGameObserver extends IGameObserver.Stub {
        private HwGameObserver() {
        }

        public void onGameListChanged() {
        }

        public void onGameStatusChanged(String packageName, int event) {
            if (AppGalleryAssistantService.this.isAppAssistantAttention()) {
                AppGalleryAssistantService.this.handleGameStatusByAppAssistant(packageName, event);
            }
            AssistantCallDndHelper.notifyGameBackground(AppGalleryAssistantService.this.mContext, event);
        }
    }

    private class ScreenOnReceiver extends BroadcastReceiver {
        private ScreenOnReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                Log.d(AppGalleryAssistantService.TAG, "ScreenOnReceiver on");
                if (AppGalleryAssistantService.this.mGameStatus != 0) {
                    int unused = AppGalleryAssistantService.this.mGameStatus = 2;
                    AppGalleryAssistantService.this.startService();
                }
                if (!TextUtils.isEmpty(AppGalleryAssistantService.this.mAppPackageName)) {
                    AppGalleryAssistantService.this.registerSensor(AppGalleryAssistantService.this.mContext);
                }
            } else if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                Log.d(AppGalleryAssistantService.TAG, "ScreenOnReceiver off");
                if (!TextUtils.isEmpty(AppGalleryAssistantService.this.mAppPackageName)) {
                    AppGalleryAssistantService.this.unRegisterSensor();
                }
            }
        }
    }

    public static synchronized AppGalleryAssistantService getInstance(Context context) {
        AppGalleryAssistantService appGalleryAssistantService;
        synchronized (AppGalleryAssistantService.class) {
            if (mAppGalleryAssistantService == null) {
                mAppGalleryAssistantService = new AppGalleryAssistantService(context);
            }
            appGalleryAssistantService = mAppGalleryAssistantService;
        }
        return appGalleryAssistantService;
    }

    private AppGalleryAssistantService(Context context) {
        Log.d(TAG, "Init AppGalleryAssistantService.");
        this.mContext = context;
        registerGameObserver();
        registerAppObserver(context);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        context.registerReceiver(new ScreenOnReceiver(), filter);
    }

    /* access modifiers changed from: private */
    public void unRegisterSensor() {
        Log.d(TAG, "unRegisterSensor");
        if (!(this.mSensorManager == null || this.mGsensor == null)) {
            this.mSensorManager.unregisterListener(this.mGsensorListener, this.mGsensor);
        }
        this.isRegisterSensor = false;
    }

    /* access modifiers changed from: private */
    public void onReverseUp() {
        Log.d(TAG, "onReverseUp mAppPackageName:" + this.mAppPackageName + ", isRegisterSensor:" + this.isRegisterSensor);
        if (!TextUtils.isEmpty(this.mAppPackageName) && this.isRegisterSensor) {
            notifyHMSSensorEvent(this.mAppPackageName);
        }
    }

    private void notifyHMSSensorEvent(String packageName) {
        Log.d(TAG, "notifyHMSSensorEvent");
        try {
            this.mContext.getContentResolver().call(Uri.parse(HMS_BUOY_NOTIFY_URI), "gyroEventNotify", packageName, null);
        } catch (Throwable e) {
            Log.e(TAG, "notifyHMSSensorEvent e:", e);
        }
    }

    /* access modifiers changed from: private */
    public void registerSensor(Context context) {
        Log.d(TAG, "start registerSensor");
        try {
            if (this.mSensorManager == null) {
                this.mSensorManager = (SensorManager) context.getSystemService("sensor");
            }
            if (this.mSensorManager != null) {
                if (this.mGsensor == null) {
                    this.mGsensor = this.mSensorManager.getDefaultSensor(1);
                }
                if (this.mGsensor != null) {
                    this.mSensorManager.registerListener(this.mGsensorListener, this.mGsensor, 1);
                    Log.d(TAG, "registerSensor complete.");
                    this.isRegisterSensor = true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "registerSensor exception:", e);
        }
    }

    private void registerAppObserver(Context context) {
        String str;
        try {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.HOME");
            ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);
            if (res.activityInfo != null) {
                if (TextUtils.isEmpty(res.activityInfo.packageName)) {
                    str = BuildConfig.FLAVOR;
                } else {
                    str = res.activityInfo.packageName;
                }
                this.launcherPackageName = str;
            }
            Log.d(TAG, "launcherPackageName:" + this.launcherPackageName);
            ActivityManagerEx.registerHwActivityNotifier(new IHwActivityNotifierEx() {
                public void call(Bundle extras) {
                    if (extras != null) {
                        String fromPackage = extras.getString("fromPackage");
                        String toPackage = extras.getString("toPackage");
                        Log.d(AppGalleryAssistantService.TAG, "registerHwActivityNotifier call fromPackage:" + fromPackage + ", toPackage:" + toPackage);
                        if (AppGalleryAssistantService.this.isSystemApp(fromPackage)) {
                            Log.d(AppGalleryAssistantService.TAG, "issystemapp:" + fromPackage);
                        } else {
                            AppGalleryAssistantService.this.backgroundEvent(fromPackage);
                        }
                        if (AppGalleryAssistantService.this.isSystemApp(toPackage)) {
                            Log.d(AppGalleryAssistantService.TAG, "issystemapp:" + toPackage);
                            return;
                        }
                        AppGalleryAssistantService.this.foregroundEvent(toPackage);
                        AppGalleryAssistantService.this.notifyAssistant();
                    }
                }
            }, "appSwitch");
        } catch (Throwable e) {
            Log.w(TAG, "registerProcessStatusObserver error:" + e.getMessage());
        }
    }

    /* access modifiers changed from: private */
    public void notifyAssistant() {
        if (System.currentTimeMillis() - this.notifyAssistantTime > HALF_DAY) {
            Log.d(TAG, "start notifyAssistant.");
            this.notifyAssistantTime = System.currentTimeMillis();
            Cursor cursor = null;
            try {
                Cursor cursor2 = this.mContext.getContentResolver().query(ContentUris.withAppendedId(Uri.parse(URI_ASSISTANT_SYNC), 0), null, null, null, null);
                Log.i(TAG, "notifyAssistant cursor == null :" + (cursor2 == null));
                if (!(cursor2 == null || cursor2.getCount() == 0)) {
                    Log.i(TAG, "notifyAssistant cursor.getcount : " + cursor2.getCount());
                    cursor2.moveToFirst();
                    Log.i(TAG, "notifyAssistant returnCode =  " + cursor2.getString(cursor2.getColumnIndex("rtnCode")));
                }
                if (cursor2 != null) {
                    cursor2.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }
    }

    private void registerGameObserver() {
        Log.d(TAG, "registerGameObserver.");
        ActivityManagerEx.registerGameObserver(new HwGameObserver());
    }

    /* access modifiers changed from: private */
    public boolean isAppAssistantAttention() {
        return SystemProperties.getInt("ro.config.gameassist_booster", 0) == 1 || SystemProperties.getInt("ro.config.gameassist.peripherals", 0) == 1;
    }

    /* access modifiers changed from: private */
    public void handleGameStatusByAppAssistant(String packageName, int event) {
        Log.d(TAG, "onGameStatusChanged packageName = " + packageName + ", event = " + event);
        if (event == 1 || event == 2 || event == GAME_TO_GAME) {
            this.mGamePackageName = packageName;
            switch (event) {
                case 1:
                    this.mGameStatus = 1;
                    break;
                case GAME_TO_GAME /*3*/:
                    this.mGameStatus = GAME_TO_GAME;
                    break;
                default:
                    this.mGameStatus = 0;
                    break;
            }
            startService();
        }
    }

    /* access modifiers changed from: private */
    public boolean isSystemApp(String packageName) {
        boolean z;
        if (this.mSystemAppMap.containsKey(packageName)) {
            if (this.mSystemAppMap.get(packageName).intValue() == 1) {
                z = true;
            } else {
                z = false;
            }
            return z;
        }
        try {
            if ((this.mContext.getPackageManager().getPackageInfo(packageName, 1).applicationInfo.flags & 1) != 0) {
                this.mSystemAppMap.put(packageName, 1);
                return true;
            }
            this.mSystemAppMap.put(packageName, 0);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "isSystemApp exception:" + e.getMessage());
            return false;
        }
    }

    public void foregroundEvent(String packageName) {
        long sdkType = getAppSDKType(packageName);
        if (sdkType == 0) {
            if (appInJoint(packageName, true)) {
                notifyHMSForegroundEvent(packageName);
                registerSensor(this.mContext);
            }
        } else if (sdkType == 2) {
            this.mAppPackageName = packageName;
            registerSensor(this.mContext);
        }
    }

    public void backgroundEvent(String packageName) {
        if (getAppSDKType(packageName) == 0) {
            if (appInJoint(packageName, false)) {
                notifyHMSBackgroundEvent(packageName);
                unRegisterSensor();
            }
        } else if (getAppSDKType(packageName) == 2) {
            notifyHMSBackgroundEvent(packageName);
            unRegisterSensor();
        }
    }

    private void notifyHMSForegroundEvent(String packageName) {
        Log.d(TAG, "notifyHMSForegroundEvent, packageName = " + packageName);
        this.mAppPackageName = packageName;
        startHMSService(packageName, 1);
    }

    private void notifyHMSBackgroundEvent(String packageName) {
        Log.d(TAG, "notifyHMSBackgroundEvent, packageName = " + packageName);
        startHMSService(packageName, 0);
        if (!TextUtils.isEmpty(this.mAppPackageName) && this.mAppPackageName.equals(packageName)) {
            this.mAppPackageName = null;
        }
    }

    private void startHMSService(String packageName, int appState) {
        Intent intent = new Intent(ACTION_HMS_SERVICE);
        intent.setPackage(PACKAGE_NAME_HMS);
        intent.putExtra("packageName", packageName);
        intent.putExtra("appState", appState);
        try {
            this.mContext.startService(intent);
        } catch (Throwable e) {
            Log.e(TAG, "Failure startHMSService.", e);
        }
    }

    private boolean appInJoint(String packageName, boolean checkTime) {
        if (!this.mJointAppMap.containsKey(packageName)) {
            return getAppTypeFromHMS(packageName);
        }
        Map<String, Object> appMap = this.mJointAppMap.get(packageName);
        int type = ((Integer) appMap.get(MAP_KEY_VALUE)).intValue();
        long time = ((Long) appMap.get(MAP_KEY_TIME)).longValue();
        if (checkTime && System.currentTimeMillis() - time > ONE_DAY) {
            return getAppTypeFromHMS(packageName);
        }
        Log.d(TAG, "appInJoint:" + type);
        return type == 1;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    private boolean getAppTypeFromHMS(String packageName) {
        Log.i(TAG, "query HMS InShowBuoy:" + packageName);
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(ContentUris.withAppendedId(Uri.parse(URI_HMS_SHOWBUOY), 0), null, "packageName=?", new String[]{packageName}, null);
            Log.i(TAG, "cursor == null :" + (cursor == null));
            if (!(cursor == null || cursor.getCount() == 0)) {
                Log.i(TAG, "cursor.getcount : " + cursor.getCount());
                cursor.moveToFirst();
                String returnCode = cursor.getString(cursor.getColumnIndex("rtnCode"));
                Log.i(TAG, "returnCode =  " + returnCode);
                char c = 65535;
                switch (returnCode.hashCode()) {
                    case 48:
                        if (returnCode.equals("0")) {
                            c = 0;
                        }
                    default:
                        switch (c) {
                            case AssistantCallDndHelper.MODE_UNSUPPORT_VAL:
                                String pkn = cursor.getString(cursor.getColumnIndex("packages"));
                                Log.d(TAG, "packageName = " + pkn);
                                Map<String, Object> info = new HashMap<>();
                                info.put(MAP_KEY_TIME, Long.valueOf(System.currentTimeMillis()));
                                if (packageName.equals(pkn)) {
                                    info.put(MAP_KEY_VALUE, 1);
                                    this.mJointAppMap.put(packageName, info);
                                    if (cursor == null) {
                                        return true;
                                    }
                                    cursor.close();
                                    return true;
                                }
                                info.put(MAP_KEY_VALUE, 0);
                                this.mJointAppMap.put(packageName, info);
                                if (cursor == null) {
                                    return false;
                                }
                                cursor.close();
                                return false;
                        }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
        return false;
    }

    private long getAppSDKType(String packageName) {
        if (!this.mSdkInfoMap.containsKey(packageName)) {
            return getSDKTypeFromInstallApp(packageName);
        }
        Map<String, Object> appMap = this.mSdkInfoMap.get(packageName);
        int type = ((Integer) appMap.get(MAP_KEY_VALUE)).intValue();
        if (System.currentTimeMillis() - ((Long) appMap.get(MAP_KEY_TIME)).longValue() > ONE_DAY) {
            return getSDKTypeFromInstallApp(packageName);
        }
        Log.d(TAG, "getAppSDKType packageName:" + packageName + ", type:" + type);
        return (long) type;
    }

    private long getSDKTypeFromInstallApp(String packageName) {
        try {
            PackageManager pm = this.mContext.getPackageManager();
            Bundle metaData = pm.getApplicationInfo(packageName, 128).metaData;
            if (metaData == null || metaData.get("com.huawei.hms.client.appid") == null) {
                ActivityInfo[] activities = pm.getPackageInfo(packageName, 1).activities;
                if (activities != null) {
                    boolean isGameSDK = false;
                    int length = activities.length;
                    int i = 0;
                    while (true) {
                        if (i >= length) {
                            break;
                        } else if ("com.huawei.gameservice.sdk.control.DummyActivity".equals(activities[i].name)) {
                            isGameSDK = true;
                            break;
                        } else {
                            i++;
                        }
                    }
                    if (isGameSDK) {
                        Log.d(TAG, "sdktype:GameSDK :" + packageName);
                        Map<String, Object> appMap = new HashMap<>();
                        appMap.put(MAP_KEY_VALUE, 1);
                        appMap.put(MAP_KEY_TIME, Long.valueOf(System.currentTimeMillis()));
                        this.mSdkInfoMap.put(packageName, appMap);
                        return 1;
                    }
                }
                Log.d(TAG, "sdktype:NoSDK :" + packageName);
                Map<String, Object> appMap2 = new HashMap<>();
                appMap2.put(MAP_KEY_VALUE, 0);
                appMap2.put(MAP_KEY_TIME, Long.valueOf(System.currentTimeMillis()));
                this.mSdkInfoMap.put(packageName, appMap2);
                return 0;
            }
            Log.d(TAG, "sdktype:HMSSDK :" + packageName);
            Map<String, Object> appMap3 = new HashMap<>();
            appMap3.put(MAP_KEY_VALUE, 2);
            appMap3.put(MAP_KEY_TIME, Long.valueOf(System.currentTimeMillis()));
            this.mSdkInfoMap.put(packageName, appMap3);
            return 2;
        } catch (Exception e) {
            Log.e(TAG, "getSDKTypeFromInstallApp error:" + e.getMessage());
        }
    }

    /* access modifiers changed from: private */
    public void startService() {
        Log.d(TAG, "startService, pkgName = " + this.mGamePackageName + ", direction = " + this.mGameStatus);
        Intent intent = new Intent("com.huawei.gameassistant.NOTIFY_GAME_SWITCH");
        intent.setPackage("com.huawei.gameassistant");
        intent.putExtra("pkgName", this.mGamePackageName);
        intent.putExtra("direction", this.mGameStatus);
        try {
            this.mContext.startService(intent);
        } catch (Throwable th) {
            Log.e(TAG, "Failure starting HwGameAssistantService");
        }
    }
}
