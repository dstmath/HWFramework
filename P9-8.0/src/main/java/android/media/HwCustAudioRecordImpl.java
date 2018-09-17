package android.media;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityThread;
import android.content.Context;
import android.os.Binder;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;
import java.util.List;

public class HwCustAudioRecordImpl extends HwCustAudioRecord {
    private static final boolean HWLOGW_E = true;
    private static final String PAD_EC_KEY = "asr_algo_info#asr_algo_name";
    private static final String TAG = "HwCustAudioRecordImpl";
    private static final boolean isOpenEC = SystemProperties.getBoolean("ro.config.open_dcm_ec", false);
    private boolean isEcMicOpen = false;
    private String padECame = "";

    public String getAppName(Context mContext, int pid) {
        if (pid <= 0) {
            return null;
        }
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService("activity");
        if (activityManager == null) {
            return null;
        }
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null || appProcesses.size() == 0) {
            return null;
        }
        String packageName = null;
        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.pid == pid && appProcess.importance == 100) {
                packageName = appProcess.processName;
                break;
            }
        }
        return packageName;
    }

    public void preStartEC() {
        if (isSupportEc() && (this.isEcMicOpen ^ 1) != 0) {
            Log.e(TAG, "Set ASR_VENDOR=" + this.padECame + ":" + "ASR_SCENE=0");
            AudioSystem.setParameters("ASR_VENDOR=" + this.padECame);
            AudioSystem.setParameters("ASR_SCENE=0");
            this.isEcMicOpen = HWLOGW_E;
        }
    }

    public void stopEC() {
        if (isSupportEc() && this.isEcMicOpen) {
            Log.e(TAG, "set ASR_VENDOR=none;ASR_SCENE=-1");
            AudioSystem.setParameters("ASR_VENDOR=none");
            AudioSystem.setParameters("ASR_SCENE=-1");
            this.isEcMicOpen = false;
        }
    }

    public String getPADECName(String pad_key) {
        String param = AudioSystem.getParameters(pad_key);
        Log.e(TAG, "getPADECName:" + param);
        return param;
    }

    public Context getContext() {
        return ActivityThread.currentApplication().getApplicationContext();
    }

    public boolean isSupportEc() {
        if (isOpenEC) {
            this.padECame = getPADECName(PAD_EC_KEY);
            if (!TextUtils.isEmpty(this.padECame)) {
                String current_name = getAppName(getContext(), Binder.getCallingPid());
                String dmcVoicePackageName = System.getString(getContext().getContentResolver(), "dcm_voice_package");
                Log.e(TAG, " current_package_name:" + current_name + " ; " + " dmcVoicePackageName: " + dmcVoicePackageName);
                if (!(TextUtils.isEmpty(dmcVoicePackageName) || (TextUtils.isEmpty(current_name) ^ 1) == 0 || !current_name.equals(dmcVoicePackageName))) {
                    Log.e(TAG, "isSupportEc ");
                    return HWLOGW_E;
                }
            }
            Log.e(TAG, "PAD_EC_KEY is null ");
            return false;
        }
        Log.e(TAG, "isOpenEC : " + isOpenEC);
        return false;
    }
}
