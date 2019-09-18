package com.huawei.android.server.clipboard;

import android.content.IClipboard;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import com.huawei.android.content.IOnPrimaryClipGetedListener;
import com.huawei.android.server.clipboard.IHwClipboardServiceManager;

public class HwClipboardServiceManager {
    private static final Singleton<IHwClipboardServiceManager> IHwClipboardServiceManagerSingleton = new Singleton<IHwClipboardServiceManager>() {
        /* access modifiers changed from: protected */
        public IHwClipboardServiceManager create() {
            try {
                if (HwClipboardServiceManager.getClipboardService() != null) {
                    return IHwClipboardServiceManager.Stub.asInterface(HwClipboardServiceManager.clipboardService.getHwInnerService());
                }
                return null;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    };
    private static final String TAG = "HwClipboardServiceManager";
    /* access modifiers changed from: private */
    public static IClipboard clipboardService;

    /* access modifiers changed from: private */
    public static IClipboard getClipboardService() {
        if (clipboardService != null) {
            return clipboardService;
        }
        try {
            clipboardService = IClipboard.Stub.asInterface(ServiceManager.getServiceOrThrow("clipboard"));
        } catch (ServiceManager.ServiceNotFoundException e) {
            clipboardService = null;
            Log.d(TAG, "get Clipboard_Service failed");
        }
        return clipboardService;
    }

    private static IHwClipboardServiceManager getService() {
        return (IHwClipboardServiceManager) IHwClipboardServiceManagerSingleton.get();
    }

    public static void addPrimaryClipGetedListener(IOnPrimaryClipGetedListener listener, String callingPackage) {
        IHwClipboardServiceManager hwcsm = getService();
        if (hwcsm != null) {
            try {
                hwcsm.addPrimaryClipGetedListener(listener, callingPackage);
            } catch (RemoteException e) {
                Log.d(TAG, "addPrimaryClipGetedListener failed");
            }
        } else {
            Log.d(TAG, "hwcsm is null");
        }
    }

    public static void removePrimaryClipGetedListener(IOnPrimaryClipGetedListener listener) {
        IHwClipboardServiceManager hwcsm = getService();
        if (hwcsm != null) {
            try {
                hwcsm.removePrimaryClipGetedListener(listener);
            } catch (RemoteException e) {
                Log.d(TAG, "addPrimaryClipGetedListener failed");
            }
        } else {
            Log.d(TAG, "hwcsm is null");
        }
    }

    public static void setGetWaitTime(int waitTime) {
        IHwClipboardServiceManager hwcsm = getService();
        if (hwcsm != null) {
            try {
                hwcsm.setGetWaitTime(waitTime);
            } catch (RemoteException e) {
                Log.d(TAG, "addPrimaryClipGetedListener failed");
            }
        } else {
            Log.d(TAG, "hwcsm is null");
        }
    }
}
