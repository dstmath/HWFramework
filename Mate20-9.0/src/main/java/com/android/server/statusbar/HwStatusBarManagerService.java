package com.android.server.statusbar;

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.CollectData;
import android.util.Slog;
import android.view.MotionEvent;
import com.android.internal.statusbar.IStatusBar;
import com.android.server.LocalServices;
import com.android.server.hidata.appqoe.HwAPPQoEUtils;
import com.android.server.wm.WindowManagerService;

public class HwStatusBarManagerService extends StatusBarManagerService {
    private static final boolean DEBUG = false;
    private static final String TAG = "HwStatusBarManagerService";
    volatile IStatusBar mBar;
    private Context mContext;
    private HwNotificationDelegate mHwNotificationDelegate;
    Object mLock = new Object();
    private boolean mStatusBarExpanded;

    public interface HwNotificationDelegate {
        void onNotificationResidentClear(String str, String str2, int i, int i2);
    }

    private void enforceStatusBar() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR", "StatusBarManagerService");
    }

    public HwStatusBarManagerService(Context context, WindowManagerService windowManager) {
        super(context, windowManager);
        this.mContext = context;
    }

    public void setHwNotificationDelegate(HwNotificationDelegate delegate) {
        Slog.d(TAG, "setHwNotificationCallbacks delegate = " + delegate);
        this.mHwNotificationDelegate = delegate;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code == 101) {
            data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
            transactToStatusBar(101, "resentapp", "resentapp", data.readInt());
            reply.writeNoException();
            return true;
        } else if (code != 121) {
            Bundle arg0 = null;
            switch (code) {
                case 105:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    String packName = data.readString();
                    String tag = data.readString();
                    int id = data.readInt();
                    int usrid = data.readInt();
                    if (this.mHwNotificationDelegate != null) {
                        Slog.d(TAG, "onNotificationResidentClear packName = " + packName + " usrid:" + usrid);
                        this.mHwNotificationDelegate.onNotificationResidentClear(packName, tag, id, usrid);
                    }
                    reply.writeNoException();
                    return true;
                case 106:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    transactToStatusBar(106, "SystemUIColor", data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 107:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    toggleSplitScreenByLineGesture(107, "toggleSplitScreenByLineGesture", data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case HwAPPQoEUtils.MSG_APP_STATE_UNKNOW:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    transactRecentCall(code, "toggleRecentApps");
                    reply.writeNoException();
                    return true;
                case 109:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    transactRecentCall(code, "preloadRecentApps");
                    reply.writeNoException();
                    return true;
                case 110:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    transactRecentCall(code, "cancelPreloadRecentApps");
                    reply.writeNoException();
                    return true;
                case 111:
                    data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                    transactToStatusBar(111, "unlockScreenPinningTest", null, 0);
                    reply.writeNoException();
                    Slog.d(TAG, "Transact unlockScreenPinningTest to status bar");
                    return true;
                default:
                    switch (code) {
                        case 123:
                            data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                            transactGestureNavEvent(123, "transactGestureNavEvent", (MotionEvent) data.readParcelable(MotionEvent.class.getClassLoader()));
                            return true;
                        case 124:
                            data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                            if (data.readInt() != 0) {
                                arg0 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                            }
                            startAssist(arg0);
                            reply.writeNoException();
                            return true;
                        default:
                            return HwStatusBarManagerService.super.onTransact(code, data, reply, flags);
                    }
            }
        } else {
            data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
            notchTransactToStatusBarStatus(121, "notchTransactToStatusBarStatus", data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
            reply.writeNoException();
            return true;
        }
    }

    private void startAssist(Bundle args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR", "StatusBarManagerService");
        ((StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class)).startAssist(args);
    }

    private void transactToStatusBar(int code, String transactName, String paramName, int paramValue) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR", "StatusBarManagerService");
        synchronized (this.mLock) {
            this.mBar = getStatusBar();
            if (this.mBar != null) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    IBinder statusBarBinder = this.mBar.asBinder();
                    if (statusBarBinder != null) {
                        Slog.d(TAG, "Transact:" + transactName + " to status bar");
                        data.writeInterfaceToken("com.android.internal.statusbar.IStatusBar");
                        if (paramName != null) {
                            data.writeInt(paramValue);
                        }
                        statusBarBinder.transact(code, data, reply, 0);
                    }
                    reply.recycle();
                } catch (RemoteException localRemoteException) {
                    try {
                        localRemoteException.printStackTrace();
                        reply.recycle();
                    } catch (Throwable th) {
                        reply.recycle();
                        data.recycle();
                        throw th;
                    }
                }
                data.recycle();
            }
        }
    }

    private void transactToStatusBar(int code, String transactName, int isEmuiStyle, int statusbarColor, int navigationBarColor, int isEmuiLightStyle) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR", "StatusBarManagerService");
        synchronized (this.mLock) {
            this.mBar = getStatusBar();
            if (this.mBar != null) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    IBinder statusBarBinder = this.mBar.asBinder();
                    if (statusBarBinder != null) {
                        data.writeInterfaceToken("com.android.internal.statusbar.IStatusBar");
                        data.writeInt(isEmuiStyle);
                        data.writeInt(statusbarColor);
                        data.writeInt(navigationBarColor);
                        data.writeInt(isEmuiLightStyle);
                        statusBarBinder.transact(code, data, reply, 0);
                    }
                    reply.recycle();
                } catch (RemoteException localRemoteException) {
                    try {
                        localRemoteException.printStackTrace();
                        reply.recycle();
                    } catch (Throwable th) {
                        reply.recycle();
                        data.recycle();
                        throw th;
                    }
                }
                data.recycle();
            }
        }
    }

    public void expandNotificationsPanel() {
        HwStatusBarManagerService.super.expandNotificationsPanel();
        this.mStatusBarExpanded = true;
    }

    public void collapsePanels() {
        HwStatusBarManagerService.super.collapsePanels();
        this.mStatusBarExpanded = false;
    }

    public boolean statusBarExpanded() {
        return this.mStatusBarExpanded;
    }

    public void onPanelHidden() throws RemoteException {
        HwStatusBarManagerService.super.onPanelHidden();
        reportToAware(90015);
    }

    public void onPanelRevealed(boolean clearNotificationEffects, int numItems) {
        HwStatusBarManagerService.super.onPanelRevealed(clearNotificationEffects, numItems);
        reportToAware(20015);
    }

    private void reportToAware(int event) {
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (resManager == null) {
            Slog.w(TAG, "iAware is not started");
            return;
        }
        int resid = AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_STATUS_BAR);
        if (resManager.isResourceNeeded(resid)) {
            Bundle bundle = new Bundle();
            bundle.putInt("eventid", event);
            CollectData data = new CollectData(resid, System.currentTimeMillis(), bundle);
            long id = Binder.clearCallingIdentity();
            resManager.reportData(data);
            Binder.restoreCallingIdentity(id);
        }
    }

    public void toggleSplitScreenByLineGesture(int code, String transactName, int param1, int param2) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR", "StatusBarManagerService");
        synchronized (this.mLock) {
            this.mBar = getStatusBar();
            if (this.mBar != null) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    IBinder statusBarBinder = this.mBar.asBinder();
                    if (statusBarBinder != null) {
                        data.writeInterfaceToken("com.android.internal.statusbar.IStatusBar");
                        data.writeInt(param1);
                        data.writeInt(param2);
                        statusBarBinder.transact(code, data, reply, 0);
                    }
                    reply.recycle();
                } catch (RemoteException localRemoteException) {
                    try {
                        localRemoteException.printStackTrace();
                        reply.recycle();
                    } catch (Throwable th) {
                        reply.recycle();
                        data.recycle();
                        throw th;
                    }
                }
                data.recycle();
            }
        }
    }

    private void transactRecentCall(int code, String transactName) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR", "StatusBarManagerService");
        synchronized (this.mLock) {
            this.mBar = getStatusBar();
            if (this.mBar != null) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    IBinder statusBarBinder = this.mBar.asBinder();
                    if (statusBarBinder != null) {
                        data.writeInterfaceToken("com.android.internal.statusbar.IStatusBar");
                        statusBarBinder.transact(code, data, reply, 0);
                    }
                    reply.recycle();
                } catch (RemoteException localRemoteException) {
                    try {
                        localRemoteException.printStackTrace();
                        reply.recycle();
                    } catch (Throwable th) {
                        reply.recycle();
                        data.recycle();
                        throw th;
                    }
                }
                data.recycle();
            }
        }
    }

    private void notchTransactToStatusBarStatus(int code, String transactName, int isEmuiStyle, int statusbarColor, int navigationBarColor, int isEmuiLightStyle, int statusbarStateFlag, int barVisibility) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR", "StatusBarManagerService");
        synchronized (this.mLock) {
            this.mBar = getStatusBar();
            if (this.mBar != null) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    IBinder statusBarBinder = this.mBar.asBinder();
                    if (statusBarBinder != null) {
                        data.writeInterfaceToken("com.android.internal.statusbar.IStatusBar");
                        data.writeInt(isEmuiStyle);
                        data.writeInt(statusbarColor);
                        data.writeInt(navigationBarColor);
                        data.writeInt(isEmuiLightStyle);
                        data.writeInt(statusbarStateFlag);
                        data.writeInt(barVisibility);
                        statusBarBinder.transact(code, data, reply, 0);
                    }
                    reply.recycle();
                } catch (RemoteException e) {
                    try {
                        Slog.d(TAG, "RemoteException happen in the method notchTransactToStatusBarStatus!");
                        reply.recycle();
                    } catch (Throwable th) {
                        reply.recycle();
                        data.recycle();
                        throw th;
                    }
                }
                data.recycle();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    private void transactGestureNavEvent(int code, String transactName, MotionEvent event) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR", "StatusBarManagerService");
        synchronized (this.mLock) {
            this.mBar = getStatusBar();
            if (this.mBar != null) {
                Parcel data = Parcel.obtain();
                try {
                    IBinder statusBarBinder = this.mBar.asBinder();
                    if (statusBarBinder != null) {
                        data.writeInterfaceToken("com.android.internal.statusbar.IStatusBar");
                        data.writeParcelable(event, 0);
                        statusBarBinder.transact(code, data, null, 0);
                    }
                    data.recycle();
                } catch (RemoteException e) {
                    try {
                        Slog.d(TAG, "exception occur in " + transactName + ", event:" + event, e);
                        data.recycle();
                    } catch (Throwable th) {
                        data.recycle();
                        throw th;
                    }
                }
            }
        }
    }
}
