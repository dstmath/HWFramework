package android.zrhung.appeye;

import android.rms.iaware.DataContract;
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
    private String[] mAnrPkgNames = new String[16];
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
            return this.mIsEnabled && this.mInterval > 0 && this.mPackageList != null && (i2 > 1 && i2 < 10);
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
            return this.mIsEnabled && this.mInterval > 0 && (i = this.mThreshold) > 1 && i < 10;
        }
    }

    private boolean isMANR(String anrInfo) {
        long currentTime = System.currentTimeMillis();
        long[] jArr = this.mAnrTimes;
        int i = this.mIndex;
        jArr[i] = currentTime;
        this.mAnrPkgNames[i] = anrInfo;
        this.mIndex = (i + 1) % 16;
        this.mCount++;
        int i2 = this.mCount;
        int i3 = this.mThreshold;
        if (i2 < i3) {
            return false;
        }
        int i4 = this.mStart;
        this.mEnd = ((i3 + i4) - 1) % 16;
        long j = jArr[i4];
        int i5 = this.mInterval;
        if (j + ((long) (i5 * 1000)) >= jArr[this.mEnd]) {
            long j2 = this.mUploadTime;
            if (j2 == 0 || j2 + ((long) (i5 * 1000)) <= jArr[i4]) {
                this.mCount = 0;
                this.mStart = (this.mStart + this.mThreshold) % 16;
                this.mUploadTime = currentTime;
                return true;
            }
        }
        this.mCount--;
        this.mStart = (this.mStart + 1) % 16;
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
        String packageName = zrHungData.getString(DataContract.BaseProperty.PACKAGE_NAME);
        String activityName = zrHungData.getString(DataContract.AppProperty.ACTIVITY_NAME);
        StringBuilder anrInfo = new StringBuilder();
        anrInfo.append(" PackageName:");
        anrInfo.append(packageName);
        anrInfo.append(" ActivityName:");
        anrInfo.append(activityName);
        if ("APP_CRASH".equals(zrHungData.getString("WpName"))) {
            Log.i(TAG, "send APP_CRASH events");
            return sendAppEyeEvent(ZRHung.APPEYE_CRASH, zrHungData, null, "APP_CRASH:" + anrInfo.toString());
        } else if (isCoreApp(packageName)) {
            return sendAppEyeEvent(ZRHung.APPEYE_CANR, zrHungData, null, "CANR:" + anrInfo.toString());
        } else if (isMANR(anrInfo.toString())) {
            return sendAppEyeEvent(ZRHung.APPEYE_MANR, zrHungData, null, "MANR:" + getCurrentMANRInfo());
        } else {
            return sendAppEyeEvent(ZRHung.APPEYE_ANR, zrHungData, null, "ANR:" + anrInfo.toString());
        }
    }

    private String getCurrentMANRInfo() {
        StringBuilder multAnrInfo = new StringBuilder();
        int start = (this.mStart - this.mThreshold) % 16;
        if (start < 0) {
            start += 16;
        }
        while (true) {
            multAnrInfo.append(System.lineSeparator());
            multAnrInfo.append("time:");
            long time = this.mAnrTimes[start];
            if (time > 0) {
                multAnrInfo.append(Utils.getDateFormatValue(time, Utils.DATE_FORMAT_DETAIL));
            }
            multAnrInfo.append(this.mAnrPkgNames[start]);
            if (start == this.mEnd) {
                return multAnrInfo.toString();
            }
            start = (start + 1) % 16;
        }
    }

    private boolean isZrHungDataValid(ZrHungData zrHungData) {
        if (zrHungData.getString(DataContract.BaseProperty.PACKAGE_NAME) != null) {
            return true;
        }
        String processName = zrHungData.getString("processName");
        if (processName == null) {
            return false;
        }
        zrHungData.putString(DataContract.BaseProperty.PACKAGE_NAME, processName);
        return true;
    }
}
