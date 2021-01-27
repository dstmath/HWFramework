package android.rms.iaware;

import android.os.IBinder;
import android.os.RemoteException;

public final class DeviceInfo {
    public static final int DEFAULT_LEVEL = -1;
    public static final int HIGH_LEVEL = 1;
    public static final int LOW_LEVEL = 3;
    public static final int MID_LEVEL = 2;
    private static final String TAG = "DeviceInfo";
    private static int sLevel = -1;
    private static int sRamSize = 0;

    public static int getDeviceLevel() {
        if (sLevel != -1) {
            AwareLog.d(TAG, "getDeviceLevel level:" + sLevel);
            return sLevel;
        }
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService != null) {
                sLevel = IAwareCMSManager.getDeviceLevel(awareService);
            } else {
                AwareLog.e(TAG, "getDeviceLevel can not find service IAwareCMSService.");
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "getDeviceLevel occur RemoteException");
        }
        AwareLog.d(TAG, "getDeviceLevel level:" + sLevel);
        return sLevel;
    }
}
