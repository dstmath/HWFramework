package com.android.server;

import android.content.Context;
import android.content.Intent;

public class DummyHwNLPManager implements HwNLPManager {
    private static HwNLPManager mInstance = new DummyHwNLPManager();

    public boolean shouldSkipGoogleNlp(Intent intent, String processName) {
        return false;
    }

    public static HwNLPManager getDefault() {
        return mInstance;
    }

    public boolean shouldSkipGoogleNlp(int pid) {
        return false;
    }

    public boolean skipForeignNlpPackage(String action, String packageName) {
        return false;
    }

    public boolean useCivilNlpPackage(String action, String packageName) {
        return false;
    }

    public void setLocationManagerService(LocationManagerService service, Context context) {
    }

    public void setPidGoogleLocation(int pid, String packageName) {
    }

    public void setHwMultiNlpPolicy(Context context) {
    }
}
