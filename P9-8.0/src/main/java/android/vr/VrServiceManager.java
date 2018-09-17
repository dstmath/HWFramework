package android.vr;

import android.content.Context;
import android.util.Log;
import android.vr.IVrServiceManager.Stub;

public class VrServiceManager extends Stub {
    private static String TAG = "VrServiceManager";
    private Context mContext;
    private final IVRManagerService mService;

    public VrServiceManager(IVRManagerService service, Context context) {
        Log.d(TAG, "VrServiceManager constructer");
        this.mService = service;
        this.mContext = context;
    }

    public double getVsync() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.VR", null);
        try {
            return this.mService.getVsync();
        } catch (Exception e) {
            Log.e(TAG, "call service error");
            return -1.0d;
        }
    }

    public boolean startFrontBufferDisplay() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.VR", null);
        try {
            return this.mService.startFrontBufferDisplay();
        } catch (Exception e) {
            Log.e(TAG, "call service error");
            return false;
        }
    }

    public boolean stopFrontBufferDisplay() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.VR", null);
        try {
            return this.mService.stopFrontBufferDisplay();
        } catch (Exception e) {
            Log.e(TAG, "call service error");
            return false;
        }
    }

    public int setSchedFifo(int tid, int rtPriority) {
        return -1;
    }

    public int setVrPlatPerf(int cpuLevel, int gpuLevel) {
        return -1;
    }

    public int releaseVrPlatPerf() {
        return -1;
    }

    public int[] getAvailableFreqLevels() {
        return new int[]{-1};
    }
}
