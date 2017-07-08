package com.android.server.power;

import android.content.Context;

public interface IHwShutdownThread {
    public static final int SHUTDOWN_ANIMATION_WAIT_TIME = 2000;
    public static final String TAG = "HwShutdownThread";
    public static final String mShutdown_path1 = "/data/cust/media/shutdownanimation.zip";
    public static final String mShutdown_path2 = "/data/local/shutdownanimation.zip";
    public static final String mShutdown_path3 = "/system/media/shutdownanimation.zip";

    boolean isDoShutdownAnimation();

    boolean needRebootDialog(String str, Context context);

    boolean needRebootProgressDialog(boolean z, Context context);

    void resetValues();

    void waitShutdownAnimation();

    void waitShutdownAnimationComplete(Context context, long j);
}
