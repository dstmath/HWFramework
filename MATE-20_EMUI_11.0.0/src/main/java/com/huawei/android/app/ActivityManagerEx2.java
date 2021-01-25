package com.huawei.android.app;

import android.app.ActivityManager;
import android.os.RemoteException;
import android.util.Log;

public class ActivityManagerEx2 {
    public static final int PROCESS_STATE_CACHED_EMPTY = 20;
    public static final int PROCESS_STATE_TOP = 2;
    public static final int PROCESS_STATE_UNKNOWN = -1;
    private static final String TAG = "ActivityManagerEx2";
    public static final int UID_OBSERVER_GONE = 2;
    public static final int UID_OBSERVER_PROCSTATE = 1;

    public static void registerUidObserver(UidObserverEx observer, int which, int cutpoint, String callingPackage) {
        try {
            ActivityManager.getService().registerUidObserver(observer, which, cutpoint, callingPackage);
        } catch (RemoteException e) {
            Log.e(TAG, "registerUidObserver getService() couldn't connect");
        }
    }
}
