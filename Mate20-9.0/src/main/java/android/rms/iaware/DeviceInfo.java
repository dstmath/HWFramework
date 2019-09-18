package android.rms.iaware;

import android.app.ActivityManager;
import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;

public final class DeviceInfo {
    public static final int DEFAULT_LEVEL = -1;
    public static final int HIGH_LEVEL = 1;
    public static final int LOW_LEVEL = 3;
    public static final int MID_LEVEL = 2;
    private static final String TAG = "DeviceInfo";
    private static int mLevel = -1;
    private static int mRamSize = 0;

    public static int getDeviceRAM(Context context) {
        if (context == null) {
            AwareLog.e(TAG, "getDeviceRAM context is null");
            return 0;
        } else if (mRamSize != 0) {
            AwareLog.d(TAG, "getDeviceRAM ramSize:" + mRamSize + "MB");
            return mRamSize;
        } else {
            ActivityManager manager = (ActivityManager) context.getSystemService("activity");
            if (manager == null) {
                return 0;
            }
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            manager.getMemoryInfo(memInfo);
            mRamSize = (int) ((((memInfo.totalMem >> 20) + 1023) >> 10) << 10);
            AwareLog.d(TAG, "getDeviceRAM memInfo.totalMem:" + memInfo.totalMem + " ramSize:" + mRamSize + "MB");
            return mRamSize;
        }
    }

    public static int getDeviceLevel() {
        if (mLevel != -1) {
            AwareLog.d(TAG, "getDeviceLevel level:" + mLevel);
            return mLevel;
        }
        try {
            IBinder awareservice = IAwareCMSManager.getICMSManager();
            if (awareservice != null) {
                mLevel = IAwareCMSManager.getDeviceLevel(awareservice);
            } else {
                AwareLog.e(TAG, "getDeviceLevel can not find service IAwareCMSService.");
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "getDeviceLevel occur RemoteException");
        }
        AwareLog.d(TAG, "getDeviceLevel level:" + mLevel);
        return mLevel;
    }
}
