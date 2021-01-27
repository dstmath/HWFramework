package com.android.server.policy;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import com.android.server.policy.WindowManagerPolicy;
import java.util.List;

public class HwCustPhoneWindowManagerImpl extends HwCustPhoneWindowManager {
    private static final int ALLOWED_INTERVAL = 100;
    private static final int ALLOWED_POWER_KEY_INTERVAL = 500;
    private static final String CBS_CLASS_ACTIVITY = "com.android.cellbroadcastreceiver.CellBroadcastAlertDialog";
    public static final String CUST_SIM_OPERATOR = SystemProperties.get("ro.config.hw_cbs_mcc");
    private static final boolean DISABLE_HOME_KEY = SystemProperties.getBoolean("ro.config.disableHomeKey", false);
    private static final String EMERGENCY_ALARM_STOP_BROADCAST_ACTION = "com.huawei.EMERGENCY_ALARM_STOP_BROADCAST";
    private static final String EMERGENCY_ALERT_BROADCAST_ACTION = "com.huawei.EMERGENCY_ALERT_BROADCAST";
    private static final String EMERGENCY_ALERT_BROADCAST_SHAKE_ACTION = "com.huawei.EMERGENCY_ALERT_EMERGENCY_ALERT_BROADCAST_SHAKE";
    private static final String EMERGENCY_BROADCAST_PERMISSION = "huawei.android.permission.EMERGENCY_ALERT";
    private static final boolean EMERGENCY_ENABLED = SystemProperties.getBoolean("ro.config.enable_sos", false);
    private static final boolean HWRIDEMODE_FEATURE_SUPPORTED = SystemProperties.getBoolean("ro.config.ride_mode", false);
    private static final boolean IS_DOCOMO = SystemProperties.get("ro.product.custom", "NULL").contains("docomo");
    private static final boolean IS_QUICK_RECORD_SUPPORTED = SystemProperties.getBoolean("ro.config.hw_quickrecord", false);
    private static final String KEY_COMBINATION = "KEY_COMBINATION";
    private static final String POWER_AND_VOLUME_DOWN = "POWER_AND_VOLUME_DOWN";
    private static final String POWER_AND_VOLUME_UP = "POWER_AND_VOLUME_UP";
    private static final String POWER_KEY_PRESS_THREE_TIMES = "POWER_KEY_PRESS_THREE_TIMES";
    private static final int PRESSED_COUNT_THREE = 3;
    private static final int SIM_MCCMNC_LENGTH = 3;
    private static final String SOS_PACKAGE = "com.huawei.sos";
    private static final String TAG = "HwCustPhoneWindowManager";
    private static final int TIME_OUT = 500;
    private static final long VOLUMEUP_DOUBLE_CLICK_TIMEOUT = 400;
    private static final String VOLUME_UP_AND_VOLUME_DOWN = "VOLUME_UP_AND_VOLUME_DOWN";
    private Context mContext;
    private long mLastPowerKeyPressedTime = 0;
    private long mLastPressOfPowerKey = 0;
    private long mLastPressOfVolumeDownKey = 0;
    private long mLastPressOfVolumeUpKey = 0;
    private long mLastVolumeUpKeyDownTime;
    private int mPowerKeyPressedCount = 0;
    private PowerManager.WakeLock mVolumeUpWakeLock;

    /* access modifiers changed from: package-private */
    public void processShakeKey(int keyCode, Context context) {
        if (keyCode == 26) {
            Intent intent = new Intent(EMERGENCY_ALERT_BROADCAST_SHAKE_ACTION);
            intent.setPackage(SOS_PACKAGE);
            context.sendBroadcast(intent, EMERGENCY_BROADCAST_PERMISSION);
        }
    }

    /* access modifiers changed from: package-private */
    public void processEmergency(int keyCode, Context context) {
        switch (keyCode) {
            case 24:
                this.mLastPressOfVolumeUpKey = System.currentTimeMillis();
                break;
            case 25:
                this.mLastPressOfVolumeDownKey = System.currentTimeMillis();
                break;
            case 26:
                this.mLastPressOfPowerKey = System.currentTimeMillis();
                break;
            default:
                return;
        }
        analyseAndSendEmergencyBroadcast(keyCode, context);
    }

    /* access modifiers changed from: package-private */
    public void analyseAndSendEmergencyBroadcast(int keyCode, Context context) {
        String extra = "";
        switch (keyCode) {
            case 24:
                if (System.currentTimeMillis() - this.mLastPressOfVolumeDownKey >= 100) {
                    if (System.currentTimeMillis() - this.mLastPressOfPowerKey < 100) {
                        extra = POWER_AND_VOLUME_UP;
                        break;
                    }
                } else {
                    extra = VOLUME_UP_AND_VOLUME_DOWN;
                    break;
                }
                break;
            case 25:
                if (System.currentTimeMillis() - this.mLastPressOfVolumeUpKey >= 100) {
                    if (System.currentTimeMillis() - this.mLastPressOfPowerKey < 100) {
                        extra = POWER_AND_VOLUME_DOWN;
                        break;
                    }
                } else {
                    extra = VOLUME_UP_AND_VOLUME_DOWN;
                    break;
                }
                break;
            case 26:
                if (System.currentTimeMillis() - this.mLastPressOfVolumeUpKey >= 100) {
                    if (System.currentTimeMillis() - this.mLastPressOfVolumeDownKey >= 100) {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - this.mLastPowerKeyPressedTime >= 500) {
                            this.mLastPowerKeyPressedTime = currentTime;
                            this.mPowerKeyPressedCount = 1;
                            break;
                        } else {
                            this.mLastPowerKeyPressedTime = currentTime;
                            this.mPowerKeyPressedCount++;
                            if (this.mPowerKeyPressedCount == 3) {
                                extra = POWER_KEY_PRESS_THREE_TIMES;
                                this.mLastPowerKeyPressedTime = 0;
                                this.mPowerKeyPressedCount = 0;
                                break;
                            }
                        }
                    } else {
                        extra = POWER_AND_VOLUME_DOWN;
                        break;
                    }
                } else {
                    extra = POWER_AND_VOLUME_UP;
                    break;
                }
                break;
            default:
                return;
        }
        if (extra.equals("")) {
            return;
        }
        if (POWER_AND_VOLUME_DOWN.equals(extra)) {
            Intent intent = new Intent(EMERGENCY_ALARM_STOP_BROADCAST_ACTION);
            intent.setPackage(SOS_PACKAGE);
            context.sendBroadcast(intent, EMERGENCY_BROADCAST_PERMISSION);
            return;
        }
        Intent intent2 = new Intent(EMERGENCY_ALERT_BROADCAST_ACTION);
        intent2.putExtra(KEY_COMBINATION, extra);
        intent2.setPackage(SOS_PACKAGE);
        context.sendBroadcast(intent2, EMERGENCY_BROADCAST_PERMISSION);
    }

    public void processCustInterceptKey(int keyCode, boolean down, Context context) {
        this.mContext = context;
        Context context2 = this.mContext;
        if (context2 != null) {
            if (down && EMERGENCY_ENABLED) {
                processShakeKey(keyCode, context2);
                processEmergency(keyCode, this.mContext);
            }
            boolean isSupVolumnkeyAnswerCall = SystemProperties.getBoolean("persist.sys.volume_call", false);
            Log.d(TAG, "answer call by keyCode = " + keyCode + ", down = " + down + ", isSupVolumnkeyAnswerCall = " + isSupVolumnkeyAnswerCall);
            if (down && isSupVolumnkeyAnswerCall) {
                answerRingingCall(keyCode, this.mContext);
            }
        }
    }

    public void volumnkeyWakeup(Context context, boolean isScreenOn, PowerManager powerManager) {
        if (context != null && powerManager != null && !isScreenOn && Settings.Secure.getInt(context.getContentResolver(), "incall_power_button_behavior", 1) == 2) {
            powerManager.wakeUp(SystemClock.uptimeMillis());
        }
    }

    public boolean isVolumnkeyWakeup(Context context) {
        if (context != null) {
            return "true".equalsIgnoreCase(Settings.Global.getString(context.getContentResolver(), "is_volumnkey_wakeup"));
        }
        return false;
    }

    private void notifyRapidRecordService(Context context) {
        String pkgName;
        Intent intent = new Intent("com.huawei.RapidRecord");
        if (checkPackageInstalled(context, "com.huawei.soundrecorder")) {
            pkgName = "com.huawei.soundrecorder";
        } else if (checkPackageInstalled(context, "com.android.soundrecorder")) {
            pkgName = "com.android.soundrecorder";
        } else {
            Log.e(TAG, "Recorder not installed...");
            return;
        }
        Log.d(TAG, "recorder pkgName: " + pkgName);
        intent.setPackage(pkgName);
        intent.putExtra("command", "start");
        context.startService(intent);
        if (this.mVolumeUpWakeLock == null) {
            PowerManager powerManager = (PowerManager) context.getSystemService("power");
            if (powerManager != null) {
                this.mVolumeUpWakeLock = powerManager.newWakeLock(1, "PhoneWindowManager.mVolumeUpWakeLock");
            } else {
                return;
            }
        }
        this.mVolumeUpWakeLock.acquire(500);
        Log.d(TAG, "start Rapid Record Service, command:" + intent.getExtras().get("command"));
    }

    private boolean checkPackageInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 128);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private boolean isDeviceProvisioned(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "device_provisioned", 0) != 0;
    }

    public boolean interceptVolumeUpKey(KeyEvent event, Context context, boolean isScreenOn, boolean keyguardActive, boolean isMusicOrFMOrVoiceCallActive, boolean isInjected, boolean down) {
        if (!IS_QUICK_RECORD_SUPPORTED) {
            return HwCustPhoneWindowManagerImpl.super.interceptVolumeUpKey(event, context, isScreenOn, keyguardActive, isMusicOrFMOrVoiceCallActive, isInjected, down);
        }
        if (!down || context == null || event == null) {
            return false;
        }
        if (!(!isScreenOn || keyguardActive) || isInjected || (event.getFlags() & 1024) != 0) {
            return false;
        }
        if (HWRIDEMODE_FEATURE_SUPPORTED && SystemProperties.getBoolean("sys.ride_mode", false)) {
            Log.i(TAG, "interceptVolumeUpKey: ride mode on");
            return false;
        } else if (SystemProperties.getBoolean("sys.super_power_save", false)) {
            Log.i(TAG, "interceptVolumeUpKey: power saving mode on");
            return false;
        } else if (!isDeviceProvisioned(context)) {
            Log.i(TAG, "interceptVolumeUpKey: Device is not Provisioned");
            return false;
        } else if (isMusicOrFMOrVoiceCallActive) {
            Log.i(TAG, "interceptVolumeUpKey: Music or FM or VoiceCall is active");
            return false;
        } else {
            if (event.getEventTime() - this.mLastVolumeUpKeyDownTime < VOLUMEUP_DOUBLE_CLICK_TIMEOUT) {
                notifyRapidRecordService(context);
            }
            this.mLastVolumeUpKeyDownTime = event.getEventTime();
            Log.d(TAG, "interceptVolumeUpKey now=" + SystemClock.uptimeMillis() + " EventTime=" + event.getEventTime());
            return true;
        }
    }

    public boolean disableHomeKey(Context context) {
        if (context == null) {
            return false;
        }
        if (DISABLE_HOME_KEY && "com.celltick.lockscreen".equals(getTopApp(context))) {
            return true;
        }
        if (!isCustSimOperator(CUST_SIM_OPERATOR, context) || !CBS_CLASS_ACTIVITY.equals(getTopActivity(context))) {
            return false;
        }
        return true;
    }

    public String getTopApp(Context context) {
        ActivityManager activityManager;
        List<ActivityManager.RunningTaskInfo> runningTask;
        ComponentName componentName;
        if (context == null || (activityManager = (ActivityManager) context.getSystemService("activity")) == null || (runningTask = activityManager.getRunningTasks(1)) == null || runningTask.size() <= 0 || (componentName = runningTask.get(0).topActivity) == null) {
            return null;
        }
        return componentName.getPackageName();
    }

    private String getTopActivity(Context context) {
        ActivityManager activityManager;
        List<ActivityManager.RunningTaskInfo> runningTask;
        ComponentName componentName;
        if (context == null || (activityManager = (ActivityManager) context.getSystemService("activity")) == null || (runningTask = activityManager.getRunningTasks(1)) == null || runningTask.size() <= 0 || (componentName = runningTask.get(0).topActivity) == null) {
            return null;
        }
        return componentName.getClassName();
    }

    private boolean isCustSimOperator(String custSimOperator, Context context) {
        TelephonyManager telephonyManager;
        if (TextUtils.isEmpty(custSimOperator) || context == null || (telephonyManager = TelephonyManager.from(context)) == null) {
            return false;
        }
        if (!telephonyManager.isMultiSimEnabled()) {
            return isCustPlmn(custSimOperator, telephonyManager.getSimOperator());
        }
        boolean flag = false;
        if (isCustPlmn(custSimOperator, telephonyManager.getSimOperator(0)) || isCustPlmn(custSimOperator, telephonyManager.getSimOperator(1))) {
            flag = true;
        }
        return flag;
    }

    private boolean isCustPlmn(String custPlmnsString, String simMccMnc) {
        if (!(simMccMnc == null || simMccMnc.length() < 3 || custPlmnsString == null)) {
            String[] custPlmns = custPlmnsString.split(";");
            for (String custPlmn : custPlmns) {
                if (simMccMnc.substring(0, 3).equals(custPlmn) || simMccMnc.equals(custPlmn)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void answerRingingCall(int keyCode, Context context) {
        TelecomManager telecomManager;
        Log.d(TAG, "acceptRingingCall by KeyEvent.KEYCODE_VOLUME keyCode = " + keyCode);
        if ((keyCode == 24 || keyCode == 25) && (telecomManager = TelecomManager.from(context)) != null && telecomManager.isRinging()) {
            telecomManager.acceptRingingCall();
        }
    }

    public int updateSystemBarsLw(Context context, WindowManagerPolicy.WindowState focusedWindow, int systemBarView) {
        if (!IS_DOCOMO || !isGoogleCountSignInSetupWizard(context, focusedWindow)) {
            return systemBarView;
        }
        return (systemBarView | 1073741824) & -32777;
    }

    private boolean isGoogleCountSignInSetupWizard(Context context, WindowManagerPolicy.WindowState focusedWindow) {
        String windowName;
        if (context == null || focusedWindow == null || (windowName = focusedWindow.toString()) == null || !windowName.contains("com.google.android.gms") || Settings.Secure.getInt(context.getContentResolver(), "device_provisioned", 1) != 0) {
            return false;
        }
        return true;
    }
}
