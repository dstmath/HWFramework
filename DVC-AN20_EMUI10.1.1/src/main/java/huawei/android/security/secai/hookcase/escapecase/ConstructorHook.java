package huawei.android.security.secai.hookcase.escapecase;

import android.util.Log;
import com.huawei.hwpartsecurity.BuildConfig;

public class ConstructorHook {
    private static final String TAG = "ConstructorHook";

    private ConstructorHook() {
    }

    public static void constructorHook(Object obj, String val) {
        constructorBackup(obj, "Hook Success");
    }

    public static void constructorBackup(Object obj, String val) {
        for (int i = 0; i < 1; i++) {
            if (val.equals(BuildConfig.FLAVOR)) {
                Log.i(TAG, "Call System Backup Method: AudioRecord startRecordingBackup(MediaSyncEvent).");
            }
        }
    }
}
