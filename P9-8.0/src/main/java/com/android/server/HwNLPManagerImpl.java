package com.android.server;

import android.content.Context;
import android.content.Intent;
import com.android.server.am.MultiNlpUtils;
import com.android.server.location.HwMultiNlpPolicy;

public class HwNLPManagerImpl implements HwNLPManager {
    private static HwNLPManager mInstance = new HwNLPManagerImpl();

    public boolean shouldSkipGoogleNlp(Intent intent, String processName) {
        return MultiNlpUtils.shouldSkipGoogleNlp(intent, processName);
    }

    public static HwNLPManager getDefault() {
        return mInstance;
    }

    public boolean shouldSkipGoogleNlp(int pid) {
        return LocationManagerServiceUtil.shouldSkipGoogleNlp(pid);
    }

    public boolean skipForeignNlpPackage(String action, String packageName) {
        return LocationManagerServiceUtil.skipForeignNlpPackage(action, packageName);
    }

    public boolean useCivilNlpPackage(String action, String packageName) {
        return LocationManagerServiceUtil.useCivilNlpPackage(action, packageName);
    }

    public void setLocationManagerService(LocationManagerService service, Context context) {
        LocationManagerServiceUtil.getDefault(service, context);
    }

    public void setPidGoogleLocation(int pid, String processName) {
        LocationManagerServiceUtil.setPidGoogleLocation(pid, processName);
    }

    public void setHwMultiNlpPolicy(Context context) {
        HwMultiNlpPolicy.getDefault(context);
    }
}
