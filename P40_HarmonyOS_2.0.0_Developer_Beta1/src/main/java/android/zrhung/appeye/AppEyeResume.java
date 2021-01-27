package android.zrhung.appeye;

import android.util.ZRHung;
import android.util.ZRHungInner;
import android.zrhung.ZrHungData;
import android.zrhung.ZrHungImpl;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.util.SlogEx;

public final class AppEyeResume extends ZrHungImpl {
    private static final String EVENT_TAG = "AppEyeResume:";
    private static final boolean IS_BETA_VERSION;
    private static final String TAG = "ZrHung.AppEyeResume";
    private static AppEyeResume appEyeResume = null;
    private static boolean isConfiged = false;
    private static boolean isEnabled = false;

    static {
        boolean z = false;
        if (SystemPropertiesEx.getInt("ro.logsystem.usertype", 0) == 3) {
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
        ZRHung.HungConfig cfg = ZRHung.getHungConfig(289);
        if (cfg == null || cfg.value == null) {
            SlogEx.e(TAG, "Failed to get config from zrhung289");
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
        if (args == null || args.getString("packageName") == null) {
            return false;
        }
        if ("false".equals(SystemPropertiesEx.get("ro.feature.dfr.appeye"))) {
            SlogEx.i(TAG, "Do not send resume event on low-end platform");
            return false;
        } else if (!isEnabled()) {
            return false;
        } else {
            String activityName = args.getString("activityName");
            String versionName = ZRHungInner.getVersionName(args.getString("packageName"));
            StringBuffer eventBuffer = new StringBuffer();
            eventBuffer.append(EVENT_TAG + activityName);
            eventBuffer.append(System.lineSeparator());
            eventBuffer.append("app Version: " + versionName);
            boolean isRet = sendAppEyeEvent(289, args, "p=" + args.getInt("pid"), eventBuffer.toString());
            if (!isRet) {
                SlogEx.e(TAG, "sendAppFreezeEvent failed!");
            }
            return isRet;
        }
    }
}
