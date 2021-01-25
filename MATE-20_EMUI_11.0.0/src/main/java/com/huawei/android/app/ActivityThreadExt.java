package com.huawei.android.app;

import android.app.ActivityThread;
import android.os.RemoteException;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class ActivityThreadExt {
    public static boolean performDexOptMode(String packageName, boolean checkProfiles, String targetCompilerFilter, boolean force, boolean bootComplete, String splitName) throws RemoteException {
        return ActivityThread.getPackageManager().performDexOptMode(packageName, checkProfiles, targetCompilerFilter, force, bootComplete, splitName);
    }
}
