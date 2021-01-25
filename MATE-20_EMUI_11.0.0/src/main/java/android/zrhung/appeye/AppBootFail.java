package android.zrhung.appeye;

import android.util.ZRHung;
import android.zrhung.ZrHungData;
import android.zrhung.ZrHungImpl;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.util.SlogEx;

public final class AppBootFail extends ZrHungImpl {
    private static final String EVENT_TAG = "AppBootFail:";
    private static final String TAG = "ZrHung.AppBootFail";
    private static AppBootFail appBootFail = null;
    private static boolean isConfiged = false;
    private static boolean isEnabled = false;

    private AppBootFail(String wpName) {
        super(wpName);
    }

    public static synchronized AppBootFail getInstance(String wpName) {
        AppBootFail appBootFail2;
        synchronized (AppBootFail.class) {
            if (appBootFail == null) {
                appBootFail = new AppBootFail(wpName);
            }
            appBootFail2 = appBootFail;
        }
        return appBootFail2;
    }

    private boolean isEnabled() {
        if (isConfiged) {
            return isEnabled;
        }
        ZRHung.HungConfig cfg = ZRHung.getHungConfig(264);
        if (cfg == null || cfg.value == null) {
            SlogEx.e(TAG, "Failed to get config from zrhung");
            return false;
        }
        String[] values = cfg.value.split(",");
        if (values[0] == null) {
            return false;
        }
        isEnabled = "1".equals(values[0].trim());
        isConfiged = true;
        return isEnabled;
    }

    @Override // android.zrhung.ZrHungImpl
    public boolean sendEvent(ZrHungData zrHungData) {
        if (zrHungData == null || zrHungData.getString("packageName") == null) {
            return false;
        }
        if ("false".equals(SystemPropertiesEx.get("ro.feature.dfr.appeye"))) {
            SlogEx.i(TAG, "Do not send bootfail event on low-end platform");
            return false;
        } else if (!isEnabled()) {
            return false;
        } else {
            String packageName = zrHungData.getString("packageName");
            boolean isRet = sendAppEyeEvent(264, zrHungData, null, EVENT_TAG + packageName);
            if (!isRet) {
                SlogEx.e(TAG, "sendAppFreezeEvent failed!");
            }
            return isRet;
        }
    }
}
