package android.zrhung.appeye;

import android.rms.utils.Utils;
import android.util.Log;
import android.util.ZRHung;
import android.zrhung.ZrHungData;
import android.zrhung.ZrHungImpl;

public final class AppEyeANR extends ZrHungImpl {
    private static final int MANR_ARRAY_SIZE = 16;
    private static final int MANR_MILLIS_PER_SECOND = 1000;
    private static final int MANR_THRESHOLD_MAX = 10;
    private static final int MANR_THRESHOLD_MIN = 1;
    private static final String TAG = "ZrHung.AppEyeANR";
    private static AppEyeANR instance = new AppEyeANR("appeye_anr");
    private String[] mAnrPkgNames = new String[MANR_ARRAY_SIZE];
    private long[] mAnrTimes = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private int mCount = 0;
    private int mEnd = 0;
    private int mIndex = 0;
    private int mInterval = 0;
    private boolean mIsConfiged = false;
    private boolean mIsEnabled = false;
    private boolean mIsEnabledFastANR = false;
    private String[] mPackageList = null;
    private int mStart = 0;
    private int mThreshold = 0;
    private long mUploadTime = 0;

    private AppEyeANR(String wpName) {
        super(wpName);
    }

    public static AppEyeANR getInstance() {
        return instance;
    }

    private int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            Log.e(TAG, "parseInt NumberFormatException e = " + e.getMessage());
            return -1;
        }
    }

    private boolean getCoreANRConfig() {
        int i;
        if (this.mIsConfiged) {
            int i2 = this.mThreshold;
            return this.mIsEnabled && this.mInterval > 0 && this.mPackageList != null && (i2 > 1 && i2 < MANR_THRESHOLD_MAX);
        }
        ZRHung.HungConfig cfg = getConfig();
        if (cfg == null) {
            return false;
        }
        if (cfg.status != 0) {
            if (cfg.status != 1) {
                this.mIsConfiged = true;
            }
            return false;
        } else if (cfg.value == null) {
            this.mIsConfiged = true;
            return false;
        } else {
            this.mPackageList = cfg.value.split(",");
            String[] strArr = this.mPackageList;
            if (strArr.length < 5) {
                this.mIsConfiged = true;
                return false;
            }
            this.mIsEnabled = strArr[0].trim().equals("1");
            this.mThreshold = parseInt(this.mPackageList[1].trim());
            this.mInterval = parseInt(this.mPackageList[2].trim());
            this.mIsEnabledFastANR = this.mPackageList[3].trim().equals("1");
            this.mIsConfiged = true;
            return this.mIsEnabled && this.mInterval > 0 && (i = this.mThreshold) > 1 && i < MANR_THRESHOLD_MAX;
        }
    }

    private boolean isMANR(String anrInfo) {
        long currentTime = System.currentTimeMillis();
        long[] jArr = this.mAnrTimes;
        int i = this.mIndex;
        jArr[i] = currentTime;
        this.mAnrPkgNames[i] = anrInfo;
        this.mIndex = (i + 1) % MANR_ARRAY_SIZE;
        this.mCount++;
        int i2 = this.mCount;
        int i3 = this.mThreshold;
        if (i2 < i3) {
            return false;
        }
        int i4 = this.mStart;
        this.mEnd = ((i3 + i4) - 1) % MANR_ARRAY_SIZE;
        long j = jArr[i4];
        int i5 = this.mInterval;
        if (j + ((long) (i5 * MANR_MILLIS_PER_SECOND)) >= jArr[this.mEnd]) {
            long j2 = this.mUploadTime;
            if (j2 == 0 || j2 + ((long) (i5 * MANR_MILLIS_PER_SECOND)) <= jArr[i4]) {
                this.mCount = 0;
                this.mStart = (this.mStart + this.mThreshold) % MANR_ARRAY_SIZE;
                this.mUploadTime = currentTime;
                return true;
            }
        }
        this.mCount--;
        this.mStart = (this.mStart + 1) % MANR_ARRAY_SIZE;
        return false;
    }

    private boolean isCoreApp(String packageName) {
        int i = 0;
        while (true) {
            String[] strArr = this.mPackageList;
            if (i >= strArr.length) {
                return false;
            }
            if (strArr[i].trim().equals(packageName)) {
                return true;
            }
            i++;
        }
    }

    @Override // android.zrhung.ZrHungImpl
    public int init(ZrHungData zrHungData) {
        getCoreANRConfig();
        return 0;
    }

    @Override // android.zrhung.ZrHungImpl
    public boolean check(ZrHungData zrHungData) {
        if (!getCoreANRConfig()) {
            return false;
        }
        Log.i(TAG, "check:" + this.mIsEnabledFastANR);
        return this.mIsEnabledFastANR;
    }

    @Override // android.zrhung.ZrHungImpl
    public boolean sendEvent(ZrHungData zrHungData) {
        if (zrHungData == null || !isZrHungDataValid(zrHungData) || !getCoreANRConfig()) {
            return false;
        }
        String packageName = zrHungData.getString("packageName");
        String activityName = zrHungData.getString("activityName");
        StringBuilder anrInfo = new StringBuilder();
        anrInfo.append(" PackageName:");
        anrInfo.append(packageName);
        anrInfo.append(" ActivityName:");
        anrInfo.append(activityName);
        if ("APP_CRASH".equals(zrHungData.getString("WpName"))) {
            Log.i(TAG, "send APP_CRASH events");
            return sendAppEyeEvent(287, zrHungData, null, "APP_CRASH:" + anrInfo.toString());
        } else if (isCoreApp(packageName)) {
            return sendAppEyeEvent(269, zrHungData, null, "CANR:" + anrInfo.toString());
        } else if (isMANR(anrInfo.toString())) {
            return sendAppEyeEvent(268, zrHungData, null, "MANR:" + getCurrentMANRInfo());
        } else {
            return sendAppEyeEvent(267, zrHungData, null, "ANR:" + anrInfo.toString());
        }
    }

    private String getCurrentMANRInfo() {
        StringBuilder multAnrInfo = new StringBuilder();
        int start = (this.mStart - this.mThreshold) % MANR_ARRAY_SIZE;
        if (start < 0) {
            start += MANR_ARRAY_SIZE;
        }
        while (true) {
            multAnrInfo.append(System.lineSeparator());
            multAnrInfo.append("time:");
            long time = this.mAnrTimes[start];
            if (time > 0) {
                multAnrInfo.append(Utils.getDateFormatValue(time, "yyyy-MM-dd hh:mm:ss"));
            }
            multAnrInfo.append(this.mAnrPkgNames[start]);
            if (start == this.mEnd) {
                return multAnrInfo.toString();
            }
            start = (start + 1) % MANR_ARRAY_SIZE;
        }
    }

    private boolean isZrHungDataValid(ZrHungData zrHungData) {
        if (zrHungData.getString("packageName") != null) {
            return true;
        }
        String processName = zrHungData.getString("processName");
        if (processName == null) {
            return false;
        }
        zrHungData.putString("packageName", processName);
        return true;
    }
}
