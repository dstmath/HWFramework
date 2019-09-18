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
    private String[] mAnrPkgNames = new String[16];
    private long[] mAnrTimes = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private boolean mConfiged = false;
    private int mCount = 0;
    private boolean mEnabled = false;
    private boolean mEnabledFastANR = false;
    private int mEnd = 0;
    private int mIndex = 0;
    private int mInterval = 0;
    private String[] mPackageList = null;
    private int mStart = 0;
    private int mThreshold = 0;
    private long mUploadTime = 0;

    public AppEyeANR(String wpName) {
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

    private boolean getCoreANRConfig() {
        boolean z = false;
        if (this.mConfiged) {
            if (this.mEnabled && this.mInterval > 0 && this.mPackageList != null && this.mThreshold > 1 && this.mThreshold < 10) {
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
            this.mPackageList = cfg.value.split(",");
            if (this.mPackageList.length < 5) {
                this.mConfiged = true;
                return false;
            }
            this.mEnabled = this.mPackageList[0].trim().equals("1");
            this.mThreshold = parseInt(this.mPackageList[1].trim());
            this.mInterval = parseInt(this.mPackageList[2].trim());
            this.mEnabledFastANR = this.mPackageList[3].trim().equals("1");
            this.mConfiged = true;
            if (this.mEnabled && this.mInterval > 0 && this.mThreshold > 1 && this.mThreshold < 10) {
                z = true;
            }
            return z;
        }
    }

    private boolean isMANR(String anrInfo) {
        long currentTime = System.currentTimeMillis();
        this.mAnrTimes[this.mIndex] = currentTime;
        this.mAnrPkgNames[this.mIndex] = anrInfo;
        this.mIndex = (this.mIndex + 1) % 16;
        this.mCount++;
        if (this.mCount < this.mThreshold) {
            return false;
        }
        this.mEnd = ((this.mStart + this.mThreshold) - 1) % 16;
        if (this.mAnrTimes[this.mStart] + ((long) (this.mInterval * 1000)) < this.mAnrTimes[this.mEnd] || (this.mUploadTime != 0 && this.mUploadTime + ((long) (this.mInterval * 1000)) > this.mAnrTimes[this.mStart])) {
            this.mCount--;
            this.mStart = (this.mStart + 1) % 16;
            return false;
        }
        this.mCount = 0;
        this.mStart = (this.mStart + this.mThreshold) % 16;
        this.mUploadTime = currentTime;
        return true;
    }

    private boolean isCoreApp(String packageName) {
        for (String trim : this.mPackageList) {
            if (trim.trim().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public int init(ZrHungData args) {
        getCoreANRConfig();
        return 0;
    }

    public boolean check(ZrHungData args) {
        if (!getCoreANRConfig()) {
            return false;
        }
        Log.i(TAG, "check:" + this.mEnabledFastANR);
        return this.mEnabledFastANR;
    }

    public boolean sendEvent(ZrHungData args) {
        if (args == null || !isZrHungDataValid(args) || !getCoreANRConfig()) {
            return false;
        }
        String packageName = args.getString("packageName");
        String activityName = args.getString(DataContract.AppProperty.ACTIVITY_NAME);
        StringBuilder anrInfo = new StringBuilder();
        anrInfo.append(" PackageName:");
        anrInfo.append(packageName);
        anrInfo.append(" ActivityName:");
        anrInfo.append(activityName);
        if ("APP_CRASH".equals(args.getString("WpName"))) {
            Log.i(TAG, "send APP_CRASH events");
            return sendAppEyeEvent(287, args, null, "APP_CRASH:" + anrInfo.toString());
        } else if (isCoreApp(packageName)) {
            return sendAppEyeEvent(269, args, null, "CANR:" + anrInfo.toString());
        } else if (isMANR(anrInfo.toString())) {
            return sendAppEyeEvent(268, args, null, "MANR:" + getCurrentMANRInfo());
        } else {
            return sendAppEyeEvent(267, args, null, "ANR:" + anrInfo.toString());
        }
    }

    private String getCurrentMANRInfo() {
        StringBuilder multAnrInfo = new StringBuilder();
        int start = (this.mStart - this.mThreshold) % 16;
        int end = this.mEnd;
        if (start < 0) {
            start += 16;
        }
        while (true) {
            multAnrInfo.append("\n");
            multAnrInfo.append("time:");
            long time = this.mAnrTimes[start];
            if (time > 0) {
                multAnrInfo.append(Utils.getDateFormatValue(time, Utils.DATE_FORMAT_DETAIL));
            }
            multAnrInfo.append(this.mAnrPkgNames[start]);
            if (start == end) {
                return multAnrInfo.toString();
            }
            start = (start + 1) % 16;
        }
    }

    private boolean isZrHungDataValid(ZrHungData args) {
        if (args.getString("packageName") != null) {
            return true;
        }
        String processName = args.getString("processName");
        if (processName == null) {
            return false;
        }
        args.putString("packageName", processName);
        return true;
    }
}
