package com.huawei.android.magicwin;

import android.common.HwPartMagicWindowFactory;
import android.magicwin.IHwMagicWindow;
import android.os.RemoteException;
import java.util.Map;

public class HwMagicWindowManagerEx {
    private static final Object LOCK = new Object();
    private static volatile IHwMagicWindow sMagicWindowService = null;

    private static IHwMagicWindow getDefault() {
        if (sMagicWindowService == null) {
            synchronized (LOCK) {
                if (sMagicWindowService == null) {
                    sMagicWindowService = HwPartMagicWindowFactory.loadFactory().getHwMagicWindowManager().getService();
                }
            }
        }
        return sMagicWindowService;
    }

    public static boolean setHwMagicWinEnabled(String pkg, boolean isEnabled) throws RemoteException {
        getDefault();
        if (sMagicWindowService != null) {
            return sMagicWindowService.setHwMagicWinEnabled(pkg, isEnabled);
        }
        return false;
    }

    public static Map<String, Boolean> getHwMagicWinEnabledApps() throws RemoteException {
        getDefault();
        if (sMagicWindowService != null) {
            return sMagicWindowService.getHwMagicWinEnabledApps();
        }
        return null;
    }

    public static boolean getHwMagicWinEnabled(String pkg) throws RemoteException {
        getDefault();
        if (sMagicWindowService != null) {
            return sMagicWindowService.getHwMagicWinEnabled(pkg);
        }
        return false;
    }

    public static void updateAppMagicWinStatusInMultiDevice(int reason, int targetDisplayid, int targetWidth, int targetHeight) throws RemoteException {
        if (getDefault() != null) {
            getDefault().updateAppMagicWinStatusInMultiDevice(reason, targetDisplayid, targetWidth, targetHeight);
        }
    }

    public static boolean notifyConnectionState(boolean isSink, boolean isConnected) throws RemoteException {
        return getDefault() != null && getDefault().notifyConnectionState(isSink, isConnected);
    }

    public static boolean isSupportMagicWindowSink() throws RemoteException {
        return getDefault() != null && getDefault().isSupportMagicWindowSink();
    }
}
