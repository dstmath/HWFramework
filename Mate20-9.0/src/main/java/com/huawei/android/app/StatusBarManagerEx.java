package com.huawei.android.app;

import android.app.StatusBarManager;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.NotificationVisibility;

public class StatusBarManagerEx {
    private static final int CODE_TRANSACT_CANCELPRELOAD_RECENT_APPS = 110;
    private static final int CODE_TRANSACT_PRELOAD_RECENT_APPS = 109;
    private static final int CODE_TRANSACT_START_ASSIST = 124;
    private static final int CODE_TRANSACT_TOGGLE_RECENT_APPS = 108;
    private static final int CODE_TRANSACT_TOGGLE_SPLITSCREEN_BY_LINEGESTURE = 107;
    public static final int DISABLE_BACK = 4194304;
    public static final int DISABLE_EXPAND = 65536;
    public static final int DISABLE_HOME = 2097152;
    public static final int DISABLE_MASK = 67043328;
    public static final int DISABLE_NONE = 0;
    public static final int DISABLE_SEARCH = 33554432;

    public static void toggleRecentApps() throws RemoteException {
        transactToStatusBarManager(IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar")), 108);
    }

    public static void preloadRecentApps() throws RemoteException {
        transactToStatusBarManager(IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar")), 109);
    }

    public static void cancelPreloadRecentApps() throws RemoteException {
        transactToStatusBarManager(IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar")), 110);
    }

    public static void startAssist(Bundle args) throws RemoteException {
        Log.d("StatusBarManager", "startAssist");
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            IStatusBarService statusBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
            if (statusBarService == null) {
                Log.e("StatusBarManager", "start Assist statusBarService is null!");
                return;
            }
            data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
            if (args != null) {
                data.writeInt(1);
                args.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }
            statusBarService.asBinder().transact(124, data, reply, 0);
            reply.readException();
            reply.recycle();
            data.recycle();
        } finally {
            reply.recycle();
            data.recycle();
        }
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

    public static void collapsePanels(Context context) {
        if (context != null) {
            ((StatusBarManager) context.getSystemService("statusbar")).collapsePanels();
        }
    }

    public static boolean isNotificationsPanelExpand(Context context) {
        if (context != null) {
            return ((StatusBarManager) context.getSystemService("statusbar")).isNotificationsPanelExpand();
        }
        return false;
    }

    public static void onNotificationClear(String pkg, String tag, int id, int userId, String key, int dismissalSurface, int rank, int count) throws RemoteException {
        String str = key;
        IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar")).onNotificationClear(pkg, tag, id, userId, str, dismissalSurface, NotificationVisibility.obtain(str, rank, count, true));
    }

    public static void onNotificationClick(String key, int rank, int count) throws RemoteException {
        IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar")).onNotificationClick(key, NotificationVisibility.obtain(key, rank, count, true));
    }

    public static void clearNotificationEffects() throws RemoteException {
        IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar")).clearNotificationEffects();
    }

    public static void onNotificationError(String pkg, String tag, int id, int uid, int initialPid, String message, int userId) throws RemoteException {
        IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar")).onNotificationError(pkg, tag, id, uid, initialPid, message, userId);
    }

    public static void onClearAllNotifications(int userId) throws RemoteException {
        IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar")).onClearAllNotifications(userId);
    }

    public static void toggleSplitScreenByLineGesture(int centerX, int centerY) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            IStatusBarService statusBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
            if (statusBarService != null) {
                IBinder statusBarServiceBinder = statusBarService.asBinder();
                StringBuilder sb = new StringBuilder();
                sb.append("statusBarServiceBinder ");
                sb.append(statusBarServiceBinder != null);
                Log.d("StatusBarManager", sb.toString());
                if (statusBarServiceBinder != null) {
                    Log.d("StatusBarManager", "Transact: toggleSplitScreenByLineGesture to status bar service");
                    data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    data.writeInt(centerX);
                    data.writeInt(centerY);
                    statusBarServiceBinder.transact(107, data, reply, 0);
                }
            }
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
    }
}
