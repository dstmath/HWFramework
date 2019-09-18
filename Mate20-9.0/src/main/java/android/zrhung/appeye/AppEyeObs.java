package android.zrhung.appeye;

import android.util.Slog;
import android.util.ZRHung;
import android.zrhung.ZrHungData;
import android.zrhung.ZrHungImpl;

public final class AppEyeObs extends ZrHungImpl {
    private static final String KEYWORD = "AppEyeObs:";
    private static final String TAG = "ZrHung.AppEyeObs";
    private static AppEyeObs mAppEyeObs = null;
    private static boolean mConfiged = false;
    private static boolean mEnabled = false;
    private static int mThreshold = 80;
    private long mFlowControl = 1000;
    private long mOldTime = 0;

    private AppEyeObs(String wpName) {
        super(wpName);
    }

    public static AppEyeObs getInstance(String wpName) {
        if (mAppEyeObs == null) {
            mAppEyeObs = new AppEyeObs(wpName);
        }
        return mAppEyeObs;
    }

    private boolean isEnabled() {
        if (mConfiged) {
            return mEnabled;
        }
        ZRHung.HungConfig cfg = ZRHung.getHungConfig(277);
        if (cfg == null || cfg.value == null) {
            Slog.e(TAG, "Failed to get config from zrhung");
            return false;
        } else if (cfg.status == 1) {
            return false;
        } else {
            if (cfg.status == -1 || cfg.status == -2) {
                Slog.e(TAG, "get config return NOT_SUPPORT or NO_CONFIG");
                mConfiged = true;
                return false;
            }
            String[] values = cfg.value.split(",");
            if (values.length >= 3) {
                this.mFlowControl = ((long) Integer.parseInt(values[2].trim())) * 1000;
                mThreshold = Integer.parseInt(values[1].trim());
                mEnabled = values[0].trim().equals("1");
            } else if (values.length >= 2) {
                mThreshold = Integer.parseInt(values[1].trim());
                mEnabled = values[0].trim().equals("1");
            } else if (values.length >= 1) {
                mEnabled = values[0].trim().equals("1");
            } else {
                Slog.e(TAG, "config values len is not correct");
                mConfiged = true;
                return false;
            }
            mConfiged = true;
            return mEnabled;
        }
    }

    public boolean sendEvent(ZrHungData args) {
        long curTime = System.currentTimeMillis();
        if (curTime - this.mOldTime < this.mFlowControl) {
            return false;
        }
        this.mOldTime = curTime;
        if (args == null || args.getString("packageName") == null || args.getInt("count", -1) == -1 || !isEnabled()) {
            return false;
        }
        int count = args.getInt("count", -1);
        if (count < mThreshold) {
            return false;
        }
        String packageName = args.getString("packageName");
        return sendAppEyeEvent(277, args, null, KEYWORD + packageName + ", count=" + count);
    }
}
