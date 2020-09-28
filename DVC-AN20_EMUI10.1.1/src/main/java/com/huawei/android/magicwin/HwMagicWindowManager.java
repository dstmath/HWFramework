package com.huawei.android.magicwin;

import android.common.HwFrameworkFactory;
import android.magicwin.IHwMagicWindow;
import android.os.RemoteException;
import android.util.Singleton;
import java.util.Map;

public class HwMagicWindowManager {
    private static final Singleton<IHwMagicWindow> GDEFAULT = new Singleton<IHwMagicWindow>() {
        /* class com.huawei.android.magicwin.HwMagicWindowManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        public IHwMagicWindow create() {
            return HwFrameworkFactory.getHwMagicWindow().getService();
        }
    };

    private static IHwMagicWindow getDefault() {
        return (IHwMagicWindow) GDEFAULT.get();
    }

    public static boolean setHwMagicWinEnabled(String pkg, boolean isEnabled) throws RemoteException {
        return getDefault().setHwMagicWinEnabled(pkg, isEnabled);
    }

    public static Map<String, Boolean> getHwMagicWinEnabledApps() throws RemoteException {
        return getDefault().getHwMagicWinEnabledApps();
    }
}
