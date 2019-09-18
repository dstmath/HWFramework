package android.zrhung.appeye;

import android.os.SystemProperties;
import android.util.Slog;
import android.util.ZRHung;
import android.zrhung.ZrHungData;
import android.zrhung.ZrHungImpl;

public final class AppBootFail extends ZrHungImpl {
    private static final String KEYWORD = "AppBootFail:";
    private static final String TAG = "ZrHung.AppBootFail";
    private static AppBootFail mAppBootFail = null;
    private static boolean mConfiged = false;
    private static boolean mEnabled = false;

    private AppBootFail(String wpName) {
        super(wpName);
    }

    public static AppBootFail getInstance(String wpName) {
        if (mAppBootFail == null) {
            mAppBootFail = new AppBootFail(wpName);
        }
        return mAppBootFail;
    }

    private boolean isEnabled() {
        if (mConfiged) {
            return mEnabled;
        }
        ZRHung.HungConfig cfg = ZRHung.getHungConfig(264);
        if (cfg == null || cfg.value == null) {
            Slog.e(TAG, "Failed to get config from zrhung");
            return false;
        }
        mEnabled = cfg.value.split(",")[0].trim().equals("1");
        mConfiged = true;
        return mEnabled;
    }

    public boolean sendEvent(ZrHungData args) {
        boolean ret = false;
        if (args == null || args.getString("packageName") == null) {
            return false;
        }
        if ("false".equals(SystemProperties.get("ro.feature.dfr.appeye"))) {
            Slog.i(TAG, "Do not sent bootfail event on low-end platform");
            return false;
        } else if (!isEnabled()) {
            return false;
        } else {
            String packageName = args.getString("packageName");
            try {
                ret = sendAppEyeEvent(264, args, null, KEYWORD + packageName);
                if (!ret) {
                    Slog.e(TAG, " sendAppFreezeEvent failed!");
                }
            } catch (Exception ex) {
                Slog.e(TAG, "exception info ex:" + ex);
            }
            return ret;
        }
    }
}
