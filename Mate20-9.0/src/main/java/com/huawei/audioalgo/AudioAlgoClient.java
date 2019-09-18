package com.huawei.audioalgo;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.content.Context;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import java.util.Iterator;
import java.util.List;

public class AudioAlgoClient {
    private static final String[] HUAWEIPROCESSING_CTL_WHITE_LIST = {"com.huawei.audioapp_clientexample", "com.huawei.mbdrc", "com.huawei.multimediadebug", "com.huawei.camera", "com.huawei.android.karaokeeffect", "com.android.soundrecorder", "com.android.soundrecordermultimic", "com.audiocn.kalaok", "com.audiocn.karaoke.huawei.pad"};
    private static final String TAG = "AudioAlgoClient";

    public native String getParameter_native(String str) throws RemoteException;

    public native int setParameter_native(String str) throws RemoteException;

    static {
        System.loadLibrary("audioalgoservice_jni");
    }

    public void bindService(Context context) {
    }

    public void unbindService(Context context) {
    }

    public int setParameter(String pathNameKeyValuePair) throws RemoteException {
        boolean Permission = false;
        int i = 0;
        while (true) {
            if (i >= HUAWEIPROCESSING_CTL_WHITE_LIST.length) {
                break;
            } else if (HUAWEIPROCESSING_CTL_WHITE_LIST[i].equals(getPackageName(Process.myPid()))) {
                Permission = true;
                break;
            } else {
                i++;
            }
        }
        if (Permission) {
            return setParameter_native(pathNameKeyValuePair);
        }
        Log.e(TAG, "SetParameter Permission Denied: " + getPackageName(Process.myPid()));
        return 0;
    }

    public String getParameter(String pathNameKey) throws RemoteException {
        boolean Permission = false;
        int i = 0;
        while (true) {
            if (i >= HUAWEIPROCESSING_CTL_WHITE_LIST.length) {
                break;
            } else if (HUAWEIPROCESSING_CTL_WHITE_LIST[i].equals(getPackageName(Process.myPid()))) {
                Permission = true;
                break;
            } else {
                i++;
            }
        }
        if (Permission) {
            return getParameter_native(pathNameKey);
        }
        Log.e(TAG, "Getparameter Permission Denied" + getPackageName(Process.myPid()));
        return null;
    }

    private String getPackageName(int pid) {
        if (pid <= 0) {
            return null;
        }
        Context context = ActivityThread.currentApplication();
        if (context == null) {
            return null;
        }
        ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
        if (activityManager == null) {
            return null;
        }
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return null;
        }
        String packageName = null;
        Iterator<ActivityManager.RunningAppProcessInfo> it = appProcesses.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ActivityManager.RunningAppProcessInfo appProcess = it.next();
            if (appProcess.pid == pid) {
                packageName = appProcess.processName;
                break;
            }
        }
        return packageName;
    }
}
