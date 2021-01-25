package com.android.server;

import android.content.Context;
import android.content.Intent;

public class DummyHwNLPManager implements HwNLPManager {
    private static HwNLPManager mInstance = new DummyHwNLPManager();

    public static HwNLPManager getDefault() {
        return mInstance;
    }

    @Override // com.android.server.HwNLPManager
    public boolean shouldSkipGoogleNlp(Intent intent, String processName) {
        return false;
    }

    @Override // com.android.server.HwNLPManager
    public boolean shouldSkipGoogleNlp(int pid) {
        return false;
    }

    @Override // com.android.server.HwNLPManager
    public boolean skipForeignNlpPackage(String action, String packageName) {
        return false;
    }

    @Override // com.android.server.HwNLPManager
    public boolean useCivilNlpPackage(String action, String packageName) {
        return false;
    }

    @Override // com.android.server.HwNLPManager
    public void setLocationManagerService(LocationManagerService service, Context context) {
    }

    @Override // com.android.server.HwNLPManager
    public void setPidGoogleLocation(int pid, String packageName) {
    }

    @Override // com.android.server.HwNLPManager
    public void setHwMultiNlpPolicy(Context context) {
    }
}
