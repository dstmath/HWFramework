package com.android.server.rms.iaware.dev;

import android.rms.iaware.AwareLog;
import java.util.ArrayList;
import java.util.List;

public class GpsActivityInfo {
    private static final String TAG = "GpsActivityInfo";
    private final List<String> mActivityNameList = new ArrayList();
    private int mMode = -1;
    private String mPackageName;

    public GpsActivityInfo(String packageName, int mode) {
        this.mPackageName = packageName;
        this.mMode = mode;
    }

    public boolean loadActivitys(String activitys) {
        int i = 0;
        if (activitys == null || activitys.trim().isEmpty()) {
            return false;
        }
        AwareLog.d(TAG, "loadActivitys, activitys : " + activitys);
        String[] activityArray = activitys.split(",");
        int length = activityArray.length;
        while (i < length) {
            String activityName = activityArray[i];
            if (activityName != null) {
                activityName = activityName.trim();
                if (!activityName.isEmpty()) {
                    this.mActivityNameList.add(activityName);
                }
            }
            i++;
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:3:0x0005, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isMatch(String packageName, String activityName) {
        if (packageName == null || activityName == null || !packageName.equals(this.mPackageName) || !this.mActivityNameList.contains(activityName)) {
            return false;
        }
        return true;
    }

    public int getLocationMode() {
        return this.mMode;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("GpsActivityInfo, packageName :").append(this.mPackageName);
        s.append(", mode : ").append(this.mMode);
        s.append(", mActivityNameList : [ ").append(this.mActivityNameList.toString()).append(" ]");
        return s.toString();
    }
}
