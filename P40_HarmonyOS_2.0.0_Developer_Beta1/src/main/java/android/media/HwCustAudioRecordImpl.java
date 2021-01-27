package android.media;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.content.Context;
import android.os.Binder;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import java.util.List;

public class HwCustAudioRecordImpl extends HwCustAudioRecord {
    private static final boolean IS_HWLOGW_E = true;
    private static final boolean IS_OPEN_EC = SystemProperties.getBoolean("ro.config.open_dcm_ec", (boolean) IS_OPEN_EC);
    private static final String PAD_EC_KEY = "asr_algo_info#asr_algo_name";
    private static final String TAG = "HwCustAudioRecordImpl";
    private boolean mIsEcMicOpen = IS_OPEN_EC;
    private String mPadEcName = "";

    public String getAppName(Context context, int pid) {
        ActivityManager activityManager;
        List<ActivityManager.RunningAppProcessInfo> appProcesses;
        if (pid <= 0 || (activityManager = (ActivityManager) context.getSystemService("activity")) == null || (appProcesses = activityManager.getRunningAppProcesses()) == null || appProcesses.size() == 0) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.pid == pid && appProcess.importance == 100) {
                return appProcess.processName;
            }
        }
        return null;
    }

    public void preStartEC() {
        if (isSupportEc() && !this.mIsEcMicOpen) {
            Log.e(TAG, "Set ASR_SCENE=0");
            AudioSystem.setParameters("ASR_VENDOR=" + this.mPadEcName);
            AudioSystem.setParameters("ASR_SCENE=0");
            this.mIsEcMicOpen = IS_HWLOGW_E;
        }
    }

    public void stopEC() {
        if (isSupportEc() && this.mIsEcMicOpen) {
            Log.e(TAG, "set ASR_VENDOR=none;ASR_SCENE=-1");
            AudioSystem.setParameters("ASR_VENDOR=none");
            AudioSystem.setParameters("ASR_SCENE=-1");
            this.mIsEcMicOpen = IS_OPEN_EC;
        }
    }

    public String getPADECName(String padKey) {
        String param = AudioSystem.getParameters(padKey);
        Log.e(TAG, "getPADECName:" + param);
        return param;
    }

    public Context getContext() {
        return ActivityThread.currentApplication().getApplicationContext();
    }

    public boolean isSupportEc() {
        if (IS_OPEN_EC) {
            this.mPadEcName = getPADECName(PAD_EC_KEY);
            if (!TextUtils.isEmpty(this.mPadEcName)) {
                String currentName = getAppName(getContext(), Binder.getCallingPid());
                String dmcVoicePackageName = Settings.System.getString(getContext().getContentResolver(), "dcm_voice_package");
                if (!TextUtils.isEmpty(dmcVoicePackageName) && !TextUtils.isEmpty(currentName) && currentName.equals(dmcVoicePackageName)) {
                    Log.e(TAG, "isSupportEc");
                    return IS_HWLOGW_E;
                }
            }
            Log.e(TAG, "PAD_EC_KEY is null");
            return IS_OPEN_EC;
        }
        Log.e(TAG, "IS_OPEN_EC : " + IS_OPEN_EC);
        return IS_OPEN_EC;
    }
}
