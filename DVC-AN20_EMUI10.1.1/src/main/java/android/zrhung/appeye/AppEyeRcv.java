package android.zrhung.appeye;

import android.rms.iaware.DataContract;
import android.util.Slog;
import android.util.ZRHung;
import android.zrhung.ZrHungData;
import android.zrhung.ZrHungImpl;

public final class AppEyeRcv extends ZrHungImpl {
    private static final String ENABLE = "1";
    private static final String KEYWORD = "AppEyeRcv:";
    private static final String TAG = "ZrHung.AppEyeRcv";
    private static AppEyeRcv appEyeRcv = null;
    private static boolean isConfiged = false;
    private static boolean isEnabled = false;
    private static int threshold = 160;
    private long mFlowControl = 3000;
    private long mOldTime = 0;

    private AppEyeRcv(String wpName) {
        super(wpName);
    }

    public static synchronized AppEyeRcv getInstance(String wpName) {
        AppEyeRcv appEyeRcv2;
        synchronized (AppEyeRcv.class) {
            if (appEyeRcv == null) {
                appEyeRcv = new AppEyeRcv(wpName);
            }
            appEyeRcv2 = appEyeRcv;
        }
        return appEyeRcv2;
    }

    private boolean isEnabled() {
        if (isConfiged) {
            return isEnabled;
        }
        ZRHung.HungConfig cfg = ZRHung.getHungConfig(ZRHung.APPEYE_RCV);
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
            Slog.e(TAG, "parseInt NumberFormatException e = " + e.getMessage());
            return -1;
        }
    }

    @Override // android.zrhung.ZrHungImpl
    public boolean sendEvent(ZrHungData args) {
        int count;
        long curTime = System.currentTimeMillis();
        long j = this.mOldTime;
        if (j > 0 && curTime - j < this.mFlowControl) {
            return false;
        }
        this.mOldTime = curTime;
        if (args == null || args.getString(DataContract.BaseProperty.PACKAGE_NAME) == null || args.getInt("count", -1) == -1 || !isEnabled() || (count = args.getInt("count", -1)) < threshold) {
            return false;
        }
        String packageName = args.getString(DataContract.BaseProperty.PACKAGE_NAME);
        return sendAppEyeEvent(ZRHung.APPEYE_RCV, args, null, KEYWORD + packageName + ", count=" + count);
    }
}
