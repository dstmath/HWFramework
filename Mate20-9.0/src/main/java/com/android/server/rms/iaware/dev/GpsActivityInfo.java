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
        if (activitys == null || activitys.trim().isEmpty()) {
            return false;
        }
        AwareLog.d(TAG, "loadActivitys, activitys : " + activitys);
        for (String activityName : activitys.split(",")) {
            if (activityName != null) {
                String activityName2 = activityName.trim();
                if (!activityName2.isEmpty()) {
                    this.mActivityNameList.add(activityName2);
                }
            }
        }
        return true;
    }

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
        return "GpsActivityInfo, packageName :" + this.mPackageName + ", mode : " + this.mMode + ", mActivityNameList : [ " + this.mActivityNameList.toString() + " ]";
    }
}
