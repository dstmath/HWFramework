package com.huawei.android.hardware.display;

import android.content.Context;
import android.hardware.display.IDisplayManager;
import android.os.Binder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import com.huawei.android.hardware.display.IHwDisplayManager;

public class HwDisplayManager {
    private static final Singleton<IHwDisplayManager> IDisplayManagerSingleton = new Singleton<IHwDisplayManager>() {
        /* class com.huawei.android.hardware.display.HwDisplayManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IHwDisplayManager create() {
            try {
                return IHwDisplayManager.Stub.asInterface(HwDisplayManager.getDisplayService().getHwInnerService());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    };
    private static final String TAG = "HwDisplayManager";
    private static IDisplayManager sDisplayManagerService;

    /* access modifiers changed from: private */
    public static IDisplayManager getDisplayService() {
        IDisplayManager iDisplayManager = sDisplayManagerService;
        if (iDisplayManager != null) {
            return iDisplayManager;
        }
        sDisplayManagerService = IDisplayManager.Stub.asInterface(ServiceManager.getService(Context.DISPLAY_SERVICE));
        return sDisplayManagerService;
    }

    public static IHwDisplayManager getService() {
        return IDisplayManagerSingleton.get();
    }

    public static boolean createVrDisplay(String displayName, int[] displayParams) {
        if (getService() == null) {
            Log.e(TAG, "displayservice is invaild in createVrDisplay.");
            return false;
        } else if (displayName == null || displayParams == null) {
            Log.e(TAG, "Params is invaild in createVrDisplay.");
            return false;
        } else {
            long token = Binder.clearCallingIdentity();
            try {
                return getService().createVrDisplay(displayName, displayParams);
            } catch (RemoteException e) {
                Log.e(TAG, "createVrDisplay failed!");
                return false;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    public static boolean destroyVrDisplay(String displayName) {
        if (getService() == null || displayName == null) {
            return false;
        }
        long token = Binder.clearCallingIdentity();
        try {
            return getService().destroyVrDisplay(displayName);
        } catch (RemoteException e) {
            Log.e(TAG, "createVrDisplay failed!");
            return false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public static boolean destroyAllVrDisplay() {
        if (getService() == null) {
            return false;
        }
        long token = Binder.clearCallingIdentity();
        try {
            return getService().destroyAllVrDisplay();
        } catch (RemoteException e) {
            Log.e(TAG, "createVrDisplay failed!");
            return false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }
}
