package com.huawei.audioalgo;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.content.Context;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
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
        boolean permission = false;
        int i = 0;
        while (true) {
            String[] strArr = HUAWEIPROCESSING_CTL_WHITE_LIST;
            if (i >= strArr.length) {
                break;
            } else if (strArr[i].equals(getPackageName(Process.myPid()))) {
                permission = true;
                break;
            } else {
                i++;
            }
        }
        if (permission) {
            return setParameter_native(pathNameKeyValuePair);
        }
        Log.e(TAG, "SetParameter permission Denied: " + getPackageName(Process.myPid()));
        return 0;
    }

    public String getParameter(String pathNameKey) throws RemoteException {
        boolean permission = false;
        int i = 0;
        while (true) {
            String[] strArr = HUAWEIPROCESSING_CTL_WHITE_LIST;
            if (i >= strArr.length) {
                break;
            } else if (strArr[i].equals(getPackageName(Process.myPid()))) {
                permission = true;
                break;
            } else {
                i++;
            }
        }
        if (permission) {
            return getParameter_native(pathNameKey);
        }
        Log.e(TAG, "Getparameter permission Denied" + getPackageName(Process.myPid()));
        return null;
    }

    private String getPackageName(int pid) {
        Context context;
        ActivityManager activityManager;
        List<ActivityManager.RunningAppProcessInfo> appProcesses;
        if (pid <= 0 || (context = ActivityThread.currentApplication()) == null || (activityManager = (ActivityManager) context.getSystemService("activity")) == null || (appProcesses = activityManager.getRunningAppProcesses()) == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }
}
