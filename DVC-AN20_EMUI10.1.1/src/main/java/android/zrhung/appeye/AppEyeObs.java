package android.zrhung.appeye;

import android.rms.iaware.DataContract;
import android.util.Slog;
import android.util.ZRHung;
import android.zrhung.ZrHungData;
import android.zrhung.ZrHungImpl;

public final class AppEyeObs extends ZrHungImpl {
    private static final String EVENT_TAG = "AppEyeObs:";
    private static final String TAG = "ZrHung.AppEyeObs";
    private static AppEyeObs appEyeObs = null;
    private static boolean isConfiged = false;
    private static boolean isEnabled = false;
    private static int threshold = 80;
    private long mFlowControl = 1000;
    private long mOldTime = 0;

    private AppEyeObs(String wpName) {
        super(wpName);
    }

    public static synchronized AppEyeObs getInstance(String wpName) {
        AppEyeObs appEyeObs2;
        synchronized (AppEyeObs.class) {
            if (appEyeObs == null) {
                appEyeObs = new AppEyeObs(wpName);
            }
            appEyeObs2 = appEyeObs;
        }
        return appEyeObs2;
    }

    private boolean isEnabled() {
        if (isConfiged) {
            return isEnabled;
        }
        ZRHung.HungConfig cfg = ZRHung.getHungConfig(ZRHung.APPEYE_OBS);
        if (cfg == null || cfg.value == null) {
            Slog.e(TAG, "Failed to get config from zrhung");
            return false;
        } else if (cfg.status == 1) {
            return false;
        } else {
            if (cfg.status == -1 || cfg.status == -2) {
                Slog.e(TAG, "get config return NOT_SUPPORT or NO_CONFIG");
                isConfiged = true;
                return false;
            }
            String[] values = cfg.value.split(",");
            if (values.length >= 3) {
                this.mFlowControl = ((long) parseInt(values[2].trim())) * 1000;
                threshold = parseInt(values[1].trim());
                isEnabled = values[0].trim().equals("1");
            } else if (values.length >= 2) {
                threshold = parseInt(values[1].trim());
                isEnabled = values[0].trim().equals("1");
            } else if (values.length >= 1) {
                isEnabled = values[0].trim().equals("1");
            } else {
                Slog.e(TAG, "config values len is not correct");
                isConfiged = true;
                return false;
            }
            isConfiged = true;
            return isEnabled;
        }
    }

    private int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            Slog.e(TAG, "parseInt NumberFormatException");
            return -1;
        }
    }

    @Override // android.zrhung.ZrHungImpl
    public boolean sendEvent(ZrHungData zrHungData) {
        int count;
        long curTime = System.currentTimeMillis();
        if (curTime - this.mOldTime < this.mFlowControl) {
            return false;
        }
        this.mOldTime = curTime;
        if (zrHungData == null || zrHungData.getString(DataContract.BaseProperty.PACKAGE_NAME) == null || zrHungData.getInt("count", -1) == -1 || !isEnabled() || (count = zrHungData.getInt("count", -1)) < threshold) {
            return false;
        }
        String packageName = zrHungData.getString(DataContract.BaseProperty.PACKAGE_NAME);
        return sendAppEyeEvent(ZRHung.APPEYE_OBS, zrHungData, null, EVENT_TAG + packageName + ", count=" + count);
    }
}
