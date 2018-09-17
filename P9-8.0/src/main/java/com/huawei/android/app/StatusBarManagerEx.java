package com.huawei.android.app;

import android.app.StatusBarManager;
import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.IStatusBarService.Stub;

public class StatusBarManagerEx {
    private static final int CODE_TRANSACT_CANCELPRELOAD_RECENT_APPS = 110;
    private static final int CODE_TRANSACT_PRELOAD_RECENT_APPS = 109;
    private static final int CODE_TRANSACT_TOGGLE_RECENT_APPS = 108;
    public static final int DISABLE_BACK = 4194304;
    public static final int DISABLE_EXPAND = 65536;
    public static final int DISABLE_HOME = 2097152;
    public static final int DISABLE_NONE = 0;
    public static final int DISABLE_SEARCH = 33554432;

    public static void toggleRecentApps() throws RemoteException {
        transactToStatusBarManager(Stub.asInterface(ServiceManager.getService("statusbar")), 108);
    }

    public static void preloadRecentApps() throws RemoteException {
        transactToStatusBarManager(Stub.asInterface(ServiceManager.getService("statusbar")), 109);
    }

    public static void cancelPreloadRecentApps() throws RemoteException {
        transactToStatusBarManager(Stub.asInterface(ServiceManager.getService("statusbar")), 110);
    }

    private static boolean transactToStatusBarManager(IStatusBarService statusBarService, int code) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        if (statusBarService != null) {
            IBinder BarService = statusBarService.asBinder();
            if (BarService != null) {
                data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                BarService.transact(code, data, reply, 0);
            }
        }
        if (data != null) {
            data.recycle();
        }
        if (reply != null) {
            reply.recycle();
        }
        return true;
    }

    public static int getDisableNoneFlag() {
        return 0;
    }

    public void disable(Context context, int what) {
        ((StatusBarManager) context.getSystemService("statusbar")).disable(what);
    }

    public static void expandNotificationsPanel(Context context) {
        ((StatusBarManager) context.getSystemService("statusbar")).expandNotificationsPanel();
    }
}
