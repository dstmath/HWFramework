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
    private static final boolean HWLOGW_I = true;
    private static final boolean IS_OPEN_EC = SystemProperties.getBoolean("ro.config.open_cust_ec", IS_OPEN_EC);
    private static final String PAD_EC_KEY = "asr_algo_info#asr_algo_name";
    private static final String TAG = "HwCustAudioRecordImpl";
    private boolean mIsECMicOpen = IS_OPEN_EC;
    private String mPadECName = "";

    public void preStartEC() {
        if (isSupportEC() && !this.mIsECMicOpen) {
            Log.i(TAG, "Set ASR_VENDOR=" + this.mPadECName + ":ASR_SCENE=0");
            StringBuilder sb = new StringBuilder();
            sb.append("ASR_VENDOR=");
            sb.append(this.mPadECName);
            AudioSystem.setParameters(sb.toString());
            AudioSystem.setParameters("ASR_SCENE=0");
            this.mIsECMicOpen = HWLOGW_I;
        }
    }

    public void stopEC() {
        if (isSupportEC() && this.mIsECMicOpen) {
            Log.i(TAG, "set ASR_VENDOR=none;ASR_SCENE=-1");
            AudioSystem.setParameters("ASR_VENDOR=none");
            AudioSystem.setParameters("ASR_SCENE=-1");
            this.mIsECMicOpen = IS_OPEN_EC;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v3, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v2, resolved type: android.app.ActivityManager} */
    /* JADX WARNING: Multi-variable type inference failed */
    private String getAppName(Context context, int pid) {
        if (pid <= 0) {
            return null;
        }
        ActivityManager activityManager = null;
        List<ActivityManager.RunningAppProcessInfo> appProcesses = null;
        if (context != null) {
            activityManager = context.getSystemService("activity");
        }
        if (activityManager != null) {
            appProcesses = activityManager.getRunningAppProcesses();
        }
        if (appProcesses == null || appProcesses.size() == 0) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.pid == pid && appProcess.importance == 100) {
                return appProcess.processName;
            }
        }
        return null;
    }

    private Context getContext() {
        return ActivityThread.currentApplication().getApplicationContext();
    }

    private boolean isSupportEC() {
        this.mPadECName = AudioSystem.getParameters(PAD_EC_KEY);
        if (!IS_OPEN_EC || TextUtils.isEmpty(this.mPadECName)) {
            return IS_OPEN_EC;
        }
        String currentName = getAppName(getContext(), Binder.getCallingPid());
        String voicePackageName = Settings.System.getString(getContext().getContentResolver(), "cust_voice_package");
        Log.i(TAG, "current_package_name:" + currentName + " ; VoicePackageName: " + voicePackageName);
        if (TextUtils.isEmpty(voicePackageName) || TextUtils.isEmpty(currentName) || !currentName.equals(voicePackageName)) {
            return IS_OPEN_EC;
        }
        Log.i(TAG, "isSupportEC");
        return HWLOGW_I;
    }
}
