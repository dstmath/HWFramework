package com.android.server;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.CpuUsageInfo;
import android.os.IHardwarePropertiesManager.Stub;
import android.os.UserHandle;
import com.android.server.vr.VrManagerInternal;

public class HardwarePropertiesManagerService extends Stub {
    private final Context mContext;
    private final Object mLock;

    private static native CpuUsageInfo[] nativeGetCpuUsages();

    private static native float[] nativeGetDeviceTemperatures(int i, int i2);

    private static native float[] nativeGetFanSpeeds();

    private static native void nativeInit();

    public HardwarePropertiesManagerService(Context context) {
        this.mLock = new Object();
        this.mContext = context;
        synchronized (this.mLock) {
            nativeInit();
        }
    }

    public float[] getDeviceTemperatures(String callingPackage, int type, int source) throws SecurityException {
        float[] nativeGetDeviceTemperatures;
        enforceHardwarePropertiesRetrievalAllowed(callingPackage);
        synchronized (this.mLock) {
            nativeGetDeviceTemperatures = nativeGetDeviceTemperatures(type, source);
        }
        return nativeGetDeviceTemperatures;
    }

    public CpuUsageInfo[] getCpuUsages(String callingPackage) throws SecurityException {
        CpuUsageInfo[] nativeGetCpuUsages;
        enforceHardwarePropertiesRetrievalAllowed(callingPackage);
        synchronized (this.mLock) {
            nativeGetCpuUsages = nativeGetCpuUsages();
        }
        return nativeGetCpuUsages;
    }

    public float[] getFanSpeeds(String callingPackage) throws SecurityException {
        float[] nativeGetFanSpeeds;
        enforceHardwarePropertiesRetrievalAllowed(callingPackage);
        synchronized (this.mLock) {
            nativeGetFanSpeeds = nativeGetFanSpeeds();
        }
        return nativeGetFanSpeeds;
    }

    private void enforceHardwarePropertiesRetrievalAllowed(String callingPackage) throws SecurityException {
        try {
            int uid = this.mContext.getPackageManager().getPackageUid(callingPackage, 0);
            if (Binder.getCallingUid() != uid) {
                throw new SecurityException("The caller has faked the package name.");
            }
            int userId = UserHandle.getUserId(uid);
            VrManagerInternal vrService = (VrManagerInternal) LocalServices.getService(VrManagerInternal.class);
            DevicePolicyManager dpm = (DevicePolicyManager) this.mContext.getSystemService(DevicePolicyManager.class);
            if (!dpm.isDeviceOwnerApp(callingPackage) && !dpm.isProfileOwnerApp(callingPackage) && !vrService.isCurrentVrListener(callingPackage, userId)) {
                throw new SecurityException("The caller is not a device or profile owner or bound VrListenerService.");
            }
        } catch (NameNotFoundException e) {
            throw new SecurityException("The caller has faked the package name.");
        }
    }
}
