package com.android.server.policy;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManagerPolicy.WindowState;
import com.android.server.dreams.HwCustDreamManagerServiceImpl;
import java.util.List;

public class HwCustPhoneWindowManagerImpl extends HwCustPhoneWindowManager {
    private static final int ALLOWED_INTERVAL = 100;
    private static final int ALLOWED_POWER_KEY_INTERVAL = 500;
    private static final String CBS_CLASS_ACTIVITY = "com.android.cellbroadcastreceiver.ui.CellBroadcastAlertDialog";
    private static final String EMERGENCY_ALARM_STOP_BROADCAST_ACTION = "com.huawei.EMERGENCY_ALARM_STOP_BROADCAST";
    private static final String EMERGENCY_ALERT_BROADCAST_ACTION = "com.huawei.EMERGENCY_ALERT_BROADCAST";
    private static final String EMERGENCY_ALERT_BROADCAST_SHAKE_ACTION = "com.huawei.EMERGENCY_ALERT_EMERGENCY_ALERT_BROADCAST_SHAKE";
    private static final String EMERGENCY_BROADCAST_PERMISSION = "huawei.android.permission.EMERGENCY_ALERT";
    private static final boolean HWRIDEMODE_FEATURE_SUPPORTED = SystemProperties.getBoolean("ro.config.ride_mode", false);
    private static final boolean IS_DOCOMO = SystemProperties.get("ro.product.custom", "NULL").contains("docomo");
    private static final String KEY_COMBINATION = "KEY_COMBINATION";
    private static final String POWER_AND_VOLUME_DOWN = "POWER_AND_VOLUME_DOWN";
    private static final String POWER_AND_VOLUME_UP = "POWER_AND_VOLUME_UP";
    private static final String POWER_KEY_PRESS_THREE_TIMES = "POWER_KEY_PRESS_THREE_TIMES";
    private static final String SOS_PACKAGE = "com.huawei.sos";
    static final String TAG = "HwCustPhoneWindowManager";
    private static final long VOLUMEUP_DOUBLE_CLICK_TIMEOUT = 400;
    private static final String VOLUME_UP_AND_VOLUME_DOWN = "VOLUME_UP_AND_VOLUME_DOWN";
    private static final boolean isQuickRecordSupported = SystemProperties.getBoolean("ro.config.hw_quickrecord", false);
    public static final String mCustSimOperator = SystemProperties.get("ro.config.hw_cbs_mcc");
    private static final boolean mDisableHomeKey = SystemProperties.getBoolean("ro.config.disableHomeKey", false);
    private static final boolean sEmergencyEnabled = SystemProperties.getBoolean("ro.config.enable_sos", false);
    private Context mContext;
    private long mLastPowerKeyPressedTime = 0;
    private long mLastVolumeUpKeyDownTime;
    private int mPowerKeyPressedCount = 0;
    private WakeLock mVolumeUpWakeLock;
    private long sLastPressOfPowerKey = 0;
    private long sLastPressOfVolumeDownKey = 0;
    private long sLastPressOfVolumeUpKey = 0;

    public int selectAnimationLw(int transit) {
        if (!HwCustDreamManagerServiceImpl.mChargingAlbumSupported) {
            return super.selectAnimationLw(transit);
        }
        if (transit == 1) {
            return 17432578;
        }
        if (transit == 2) {
            return 17432721;
        }
        return 0;
    }

    public boolean isChargingAlbumSupported() {
        if (HwCustDreamManagerServiceImpl.mChargingAlbumSupported) {
            return true;
        }
        return super.isChargingAlbumSupported();
    }

    void processShakeKey(int keyCode, Context context) {
        if (keyCode == 26) {
            Intent intent = new Intent(EMERGENCY_ALERT_BROADCAST_SHAKE_ACTION);
            intent.setPackage(SOS_PACKAGE);
            context.sendBroadcast(intent, EMERGENCY_BROADCAST_PERMISSION);
        }
    }

    void processEmergency(int keyCode, Context context) {
        switch (keyCode) {
            case 24:
                this.sLastPressOfVolumeUpKey = System.currentTimeMillis();
                break;
            case 25:
                this.sLastPressOfVolumeDownKey = System.currentTimeMillis();
                break;
            case 26:
                this.sLastPressOfPowerKey = System.currentTimeMillis();
                break;
            default:
                return;
        }
        analyseAndSendEmergencyBroadcast(keyCode, context);
    }

    void analyseAndSendEmergencyBroadcast(int keyCode, Context context) {
        String extra = "";
        switch (keyCode) {
            case 24:
                if (System.currentTimeMillis() - this.sLastPressOfVolumeDownKey >= 100) {
                    if (System.currentTimeMillis() - this.sLastPressOfPowerKey < 100) {
                        extra = POWER_AND_VOLUME_UP;
                        break;
                    }
                }
                extra = VOLUME_UP_AND_VOLUME_DOWN;
                break;
                break;
            case 25:
                if (System.currentTimeMillis() - this.sLastPressOfVolumeUpKey >= 100) {
                    if (System.currentTimeMillis() - this.sLastPressOfPowerKey < 100) {
                        extra = POWER_AND_VOLUME_DOWN;
                        break;
                    }
                }
                extra = VOLUME_UP_AND_VOLUME_DOWN;
                break;
                break;
            case 26:
                if (System.currentTimeMillis() - this.sLastPressOfVolumeUpKey >= 100) {
                    if (System.currentTimeMillis() - this.sLastPressOfVolumeDownKey >= 100) {
                        long lCurrentTime = System.currentTimeMillis();
                        if (lCurrentTime - this.mLastPowerKeyPressedTime >= 500) {
                            this.mLastPowerKeyPressedTime = lCurrentTime;
                            this.mPowerKeyPressedCount = 1;
                            break;
                        }
                        this.mLastPowerKeyPressedTime = lCurrentTime;
                        this.mPowerKeyPressedCount++;
                        if (this.mPowerKeyPressedCount == 3) {
                            extra = POWER_KEY_PRESS_THREE_TIMES;
                            this.mLastPowerKeyPressedTime = 0;
                            this.mPowerKeyPressedCount = 0;
                            break;
                        }
                    }
                    extra = POWER_AND_VOLUME_DOWN;
                    break;
                }
                extra = POWER_AND_VOLUME_UP;
                break;
                break;
            default:
                return;
        }
        if (!extra.equals("")) {
            Intent intent;
            if (POWER_AND_VOLUME_DOWN.equals(extra)) {
                intent = new Intent(EMERGENCY_ALARM_STOP_BROADCAST_ACTION);
                intent.setPackage(SOS_PACKAGE);
                context.sendBroadcast(intent, EMERGENCY_BROADCAST_PERMISSION);
            } else {
                intent = new Intent(EMERGENCY_ALERT_BROADCAST_ACTION);
                intent.putExtra(KEY_COMBINATION, extra);
                intent.setPackage(SOS_PACKAGE);
                context.sendBroadcast(intent, EMERGENCY_BROADCAST_PERMISSION);
            }
        }
    }

    public void processCustInterceptKey(int keyCode, boolean down, Context context) {
        this.mContext = context;
        if (this.mContext != null) {
            if (down && sEmergencyEnabled) {
                processShakeKey(keyCode, this.mContext);
                processEmergency(keyCode, this.mContext);
            }
            boolean isSupVolumnkeyAnswerCall = SystemProperties.getBoolean("persist.sys.volume_call", false);
            Log.d(TAG, "answer call by keyCode = " + keyCode + ", down = " + down + ", isSupVolumnkeyAnswerCall = " + isSupVolumnkeyAnswerCall);
            if (down && isSupVolumnkeyAnswerCall) {
                answerRingingCall(keyCode, this.mContext);
            }
        }
    }

    public void volumnkeyWakeup(Context mContext, boolean isScreenOn, PowerManager mPowerManager) {
        if (mContext != null && mPowerManager != null && (isScreenOn ^ 1) != 0 && Secure.getInt(mContext.getContentResolver(), "incall_power_button_behavior", 1) == 2) {
            mPowerManager.wakeUp(SystemClock.uptimeMillis());
        }
    }

    public boolean isVolumnkeyWakeup() {
        return SystemProperties.getBoolean("ro.config.is_volumnkey_wakeup", false);
    }

    private void notifyRapidRecordService(Context context) {
        Intent intent = new Intent("com.huawei.RapidRecord");
        intent.setPackage("com.android.soundrecorder");
        intent.putExtra("command", "start");
        context.startService(intent);
        if (this.mVolumeUpWakeLock == null) {
            this.mVolumeUpWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "PhoneWindowManager.mVolumeUpWakeLock");
        }
        this.mVolumeUpWakeLock.acquire(500);
        Log.d(TAG, "start Rapid Record Service, command:" + intent.getExtras().get("command"));
    }

    private boolean isDeviceProvisioned(Context context) {
        if (Global.getInt(context.getContentResolver(), "device_provisioned", 0) != 0) {
            return true;
        }
        return false;
    }

    public boolean interceptVolumeUpKey(KeyEvent event, Context context, boolean isScreenOn, boolean keyguardActive, boolean isMusicOrFMOrVoiceCallActive, boolean isInjected, boolean down) {
        if (!isQuickRecordSupported) {
            return super.interceptVolumeUpKey(event, context, isScreenOn, keyguardActive, isMusicOrFMOrVoiceCallActive, isInjected, down);
        }
        if (!down || context == null) {
            return false;
        }
        if ((isScreenOn && (keyguardActive ^ 1) != 0) || isInjected || (event.getFlags() & 1024) != 0) {
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
        if (mDisableHomeKey && "com.celltick.lockscreen".equals(getTopApp(context))) {
            return true;
        }
        if (isCustSimOperator(mCustSimOperator, context)) {
            if (CBS_CLASS_ACTIVITY.equals(getTopActivity(context))) {
                return true;
            }
        }
        return false;
    }

    public String getTopApp(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
        if (activityManager != null) {
            List<RunningTaskInfo> runningTask = activityManager.getRunningTasks(1);
            if (runningTask != null && runningTask.size() > 0) {
                ComponentName cn = ((RunningTaskInfo) runningTask.get(0)).topActivity;
                if (cn != null) {
                    return cn.getPackageName();
                }
            }
        }
        return null;
    }

    private String getTopActivity(Context context) {
        if (context != null) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
            if (activityManager != null) {
                List<RunningTaskInfo> runningTask = activityManager.getRunningTasks(1);
                if (runningTask != null && runningTask.size() > 0) {
                    ComponentName cn = ((RunningTaskInfo) runningTask.get(0)).topActivity;
                    if (cn != null) {
                        return cn.getClassName();
                    }
                }
            }
        }
        return null;
    }

    private boolean isCustSimOperator(String mCustSimOperator, Context context) {
        boolean flag = false;
        if (TextUtils.isEmpty(mCustSimOperator)) {
            return false;
        }
        if (context != null) {
            TelephonyManager mTelephonyManager = TelephonyManager.from(context);
            if (mTelephonyManager != null) {
                flag = mTelephonyManager.isMultiSimEnabled() ? !isCustPlmn(mCustSimOperator, mTelephonyManager.getSimOperator(0)) ? isCustPlmn(mCustSimOperator, mTelephonyManager.getSimOperator(1)) : true : isCustPlmn(mCustSimOperator, mTelephonyManager.getSimOperator());
            }
        }
        return flag;
    }

    private boolean isCustPlmn(String custPlmnsString, String simMccMnc) {
        if (!(simMccMnc == null || simMccMnc.length() < 3 || custPlmnsString == null)) {
            String[] custPlmns = custPlmnsString.split(";");
            int i = 0;
            while (i < custPlmns.length) {
                if (simMccMnc.substring(0, 3).equals(custPlmns[i]) || simMccMnc.equals(custPlmns[i])) {
                    return true;
                }
                i++;
            }
        }
        return false;
    }

    private void answerRingingCall(int keyCode, Context context) {
        Log.d(TAG, "acceptRingingCall by KeyEvent.KEYCODE_VOLUME keyCode = " + keyCode);
        switch (keyCode) {
            case 24:
            case 25:
                TelecomManager telecomManager = TelecomManager.from(context);
                if (telecomManager != null && telecomManager.isRinging()) {
                    telecomManager.acceptRingingCall();
                }
                return;
            default:
                return;
        }
    }

    public int updateSystemBarsLw(Context context, WindowState focusedWindow, int vis) {
        if (IS_DOCOMO && isGoogleCountSignInSetupWizard(context, focusedWindow)) {
            return (vis | 1073741824) & -32777;
        }
        return vis;
    }

    private boolean isGoogleCountSignInSetupWizard(Context context, WindowState focusedWindow) {
        if (context == null || focusedWindow == null) {
            return false;
        }
        String packagesName = "com.google.android.gms";
        String windowName = focusedWindow.toString();
        return windowName != null && windowName.contains("com.google.android.gms") && Secure.getInt(context.getContentResolver(), "device_provisioned", 1) == 0;
    }
}
