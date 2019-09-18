package android.zrhung.appeye;

import android.util.Log;
import android.util.ZRHung;
import android.zrhung.ZrHungData;
import android.zrhung.ZrHungImpl;

public final class AppEyeHK extends ZrHungImpl {
    private static final int KEY_DOWN_ARRAY_SIZE = 16;
    private static final int KEY_DOWN_DURATION_MILLIS_MIN = 100;
    private static final int KEY_DOWN_THRESHOLD_MAX = 10;
    private static final int KEY_DOWN_THRESHOLD_MIN = 1;
    private static final String TAG = "ZrHung.AppEyeHK";
    private boolean mConfiged = false;
    private int mCount = 0;
    private long[] mDownTimes = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private int mDuration = 0;
    private boolean mEnabled = false;
    private int mEnd = 0;
    private int mIndex = 0;
    private int mStart = 0;
    private int mThreshold = 0;

    public AppEyeHK(String wpName) {
        super(wpName);
    }

    private int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            Log.e(TAG, "parseInt NumberFormatException e = " + e.getMessage());
            return -1;
        }
    }

    private boolean getHKConfig() {
        boolean z = false;
        if (this.mConfiged) {
            if (this.mEnabled && this.mDuration >= 100 && this.mThreshold >= 1 && this.mThreshold <= 10) {
                z = true;
            }
            return z;
        }
        ZRHung.HungConfig cfg = getConfig();
        if (cfg == null) {
            return false;
        }
        if (cfg.status != 0) {
            if (cfg.status != 1) {
                this.mConfiged = true;
            }
            return false;
        } else if (cfg.value == null) {
            this.mConfiged = true;
            return false;
        } else {
            String[] values = cfg.value.split(",");
            if (values.length < 3) {
                this.mConfiged = true;
                return false;
            }
            this.mEnabled = values[0].trim().equals("1");
            this.mThreshold = parseInt(values[1].trim());
            this.mDuration = parseInt(values[2].trim());
            this.mConfiged = true;
            if (this.mEnabled && this.mDuration >= 100 && this.mThreshold >= 1 && this.mThreshold <= 10) {
                z = true;
            }
            return z;
        }
    }

    private long matchDownPattern(long downTime) {
        this.mDownTimes[this.mIndex] = downTime;
        this.mIndex = (this.mIndex + 1) % 16;
        this.mCount++;
        if (this.mCount < this.mThreshold) {
            return -1;
        }
        this.mEnd = ((this.mStart + this.mThreshold) - 1) % 16;
        if (this.mDownTimes[this.mStart] + ((long) this.mDuration) >= this.mDownTimes[this.mEnd]) {
            long startTime = this.mDownTimes[this.mStart];
            this.mCount = 0;
            this.mStart = (this.mStart + this.mThreshold) % 16;
            return startTime;
        }
        this.mCount--;
        this.mStart = (this.mStart + 1) % 16;
        return -1;
    }

    public int init(ZrHungData args) {
        getHKConfig();
        return 0;
    }

    public boolean check(ZrHungData args) {
        if (args == null || !getHKConfig()) {
            return false;
        }
        long startTime = matchDownPattern(args.getLong("downTime"));
        if (startTime == -1) {
            return true;
        }
        return ZRHung.sendHungEvent(515, null, "HK:" + startTime);
    }
}
