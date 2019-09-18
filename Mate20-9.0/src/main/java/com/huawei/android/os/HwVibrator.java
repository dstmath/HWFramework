package com.huawei.android.os;

import android.os.Binder;
import android.os.IVibratorService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import com.huawei.android.os.IHwVibrator;

public class HwVibrator {
    private static final Singleton<IHwVibrator> IVibratorSingleton = new Singleton<IHwVibrator>() {
        /* access modifiers changed from: protected */
        public IHwVibrator create() {
            try {
                IVibratorService vibratorService = IVibratorService.Stub.asInterface(ServiceManager.getService("vibrator"));
                if (vibratorService != null) {
                    return IHwVibrator.Stub.asInterface(vibratorService.getHwInnerService());
                }
                Log.e(HwVibrator.TAG, "failed to connect VibratorService!");
                return null;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    };
    private static final String TAG = "HwVibrator";
    private static final Binder mToken = new Binder();

    public static IHwVibrator getService() {
        return (IHwVibrator) IVibratorSingleton.get();
    }

    public static boolean isSupportHwVibrator(String type) {
        try {
            return getService().isSupportHwVibrator(type);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void setHwVibrator(int pid, String opPkg, String type) {
        try {
            getService().setHwVibrator(pid, opPkg, mToken, type);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void stopHwVibrator(int pid, String opPkg, String type) {
        try {
            getService().stopHwVibrator(pid, opPkg, mToken, type);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void setHwParameter(String command) {
        try {
            getService().setHwParameter(command);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static String getHwParameter(String command) {
        try {
            return getService().getHwParameter(command);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }
}
