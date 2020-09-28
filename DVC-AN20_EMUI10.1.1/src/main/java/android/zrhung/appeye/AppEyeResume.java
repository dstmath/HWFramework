package android.zrhung.appeye;

import android.os.SystemProperties;
import android.rms.iaware.DataContract;
import android.util.Slog;
import android.util.ZRHung;
import android.zrhung.ZrHungData;
import android.zrhung.ZrHungImpl;

public final class AppEyeResume extends ZrHungImpl {
    private static final String EVENT_TAG = "AppEyeResume:";
    private static final boolean IS_BETA_VERSION;
    private static final String TAG = "ZrHung.AppEyeResume";
    private static AppEyeResume appEyeResume = null;
    private static boolean isConfiged = false;
    private static boolean isEnabled = false;

    static {
        boolean z = false;
        if (SystemProperties.getInt("ro.logsystem.usertype", 0) == 3) {
            z = true;
        }
        IS_BETA_VERSION = z;
    }

    private AppEyeResume(String wpName) {
        super(wpName);
    }

    public static synchronized AppEyeResume getInstance(String wpName) {
        AppEyeResume appEyeResume2;
        synchronized (AppEyeResume.class) {
            if (appEyeResume == null) {
                appEyeResume = new AppEyeResume(wpName);
            }
            appEyeResume2 = appEyeResume;
        }
        return appEyeResume2;
    }

    private boolean isEnabled() {
        if (isConfiged) {
            return isEnabled;
        }
        ZRHung.HungConfig cfg = ZRHung.getHungConfig(ZRHung.APPEYE_RESUME);
        if (cfg == null || cfg.value == null) {
            Slog.e(TAG, "Failed to get config from zrhung289");
            return false;
        }
        String[] values = cfg.value.split(",");
        if (values[0] == null) {
            return false;
        }
        isEnabled = values[0].trim().equals("1");
        isConfiged = true;
        return isEnabled;
    }

    @Override // android.zrhung.ZrHungImpl
    public boolean sendEvent(ZrHungData args) {
        if (args == null || args.getString(DataContract.BaseProperty.PACKAGE_NAME) == null || !IS_BETA_VERSION) {
            return false;
        }
        if ("false".equals(SystemProperties.get("ro.feature.dfr.appeye"))) {
            Slog.i(TAG, "Do not send resume event on low-end platform");
            return false;
        } else if (!isEnabled()) {
            return false;
        } else {
            String activityName = args.getString(DataContract.AppProperty.ACTIVITY_NAME);
            String versionName = ZRHung.getVersionName(args.getString(DataContract.BaseProperty.PACKAGE_NAME));
            StringBuffer eventBuffer = new StringBuffer();
            eventBuffer.append(EVENT_TAG + activityName);
            eventBuffer.append(System.lineSeparator());
            eventBuffer.append("app Version: " + versionName);
            boolean isRet = sendAppEyeEvent(ZRHung.APPEYE_RESUME, args, "p=" + args.getInt("pid"), eventBuffer.toString());
            if (!isRet) {
                Slog.e(TAG, "sendAppFreezeEvent failed!");
            }
            return isRet;
        }
    }
}
