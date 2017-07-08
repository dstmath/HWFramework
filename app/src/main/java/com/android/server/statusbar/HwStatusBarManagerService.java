package com.android.server.statusbar;

import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Slog;
import com.android.internal.statusbar.IStatusBar;
import com.android.server.rms.iaware.cpu.CPUFeature;
import com.android.server.wifipro.WifiProCommonDefs;
import com.android.server.wm.WindowManagerService;

public class HwStatusBarManagerService extends StatusBarManagerService {
    private static final boolean DEBUG = false;
    private static final String TAG = "HwStatusBarManagerService";
    volatile IStatusBar mBar;
    private Context mContext;
    private HwNotificationDelegate mHwNotificationDelegate;
    Object mLock;
    private boolean mStatusBarExpanded;

    public interface HwNotificationDelegate {
        void onNotificationResidentClear(String str, String str2, int i, int i2);
    }

    private void enforceStatusBar() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR", "StatusBarManagerService");
    }

    public HwStatusBarManagerService(Context context, WindowManagerService windowManager) {
        super(context, windowManager);
        this.mLock = new Object();
        this.mContext = context;
    }

    public void setHwNotificationDelegate(HwNotificationDelegate delegate) {
        Slog.d(TAG, "setHwNotificationCallbacks delegate = " + delegate);
        this.mHwNotificationDelegate = delegate;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case WifiProCommonDefs.TYEP_HAS_INTERNET /*101*/:
                data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                transactToStatusBar(WifiProCommonDefs.TYEP_HAS_INTERNET, "resentapp", "resentapp", data.readInt());
                reply.writeNoException();
                return true;
            case CPUFeature.MSG_MOVETO_BACKGROUND /*105*/:
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
            case CPUFeature.MSG_PROCESS_GROUP_CHANGE /*106*/:
                data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                transactToStatusBar(CPUFeature.MSG_PROCESS_GROUP_CHANGE, "SystemUIColor", data.readInt(), data.readInt(), data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            case CPUFeature.MSG_BOOST_KILL_SWITCH /*107*/:
                data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                toggleSplitScreenByLineGesture(CPUFeature.MSG_BOOST_KILL_SWITCH, "toggleSplitScreenByLineGesture", data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            case CPUFeature.MSG_START_BIGDATAPROCRECORD /*108*/:
                data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                transactRecentCall(code, "toggleRecentApps");
                reply.writeNoException();
                return true;
            case CPUFeature.MSG_STOP_BIGDATAPROCRECORD /*109*/:
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
                return super.onTransact(code, data, reply, flags);
        }
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
                    data.recycle();
                } catch (RemoteException localRemoteException) {
                    localRemoteException.printStackTrace();
                    reply.recycle();
                    data.recycle();
                } catch (Throwable th) {
                    reply.recycle();
                    data.recycle();
                }
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
                    data.recycle();
                } catch (RemoteException localRemoteException) {
                    localRemoteException.printStackTrace();
                    reply.recycle();
                    data.recycle();
                } catch (Throwable th) {
                    reply.recycle();
                    data.recycle();
                }
            }
        }
    }

    public void expandNotificationsPanel() {
        super.expandNotificationsPanel();
        this.mStatusBarExpanded = true;
    }

    public void collapsePanels() {
        super.collapsePanels();
        this.mStatusBarExpanded = DEBUG;
    }

    public boolean statusBarExpanded() {
        return this.mStatusBarExpanded;
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
                    data.recycle();
                } catch (RemoteException localRemoteException) {
                    localRemoteException.printStackTrace();
                    reply.recycle();
                    data.recycle();
                } catch (Throwable th) {
                    reply.recycle();
                    data.recycle();
                }
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
                    data.recycle();
                } catch (RemoteException localRemoteException) {
                    localRemoteException.printStackTrace();
                    reply.recycle();
                    data.recycle();
                } catch (Throwable th) {
                    reply.recycle();
                    data.recycle();
                }
            }
        }
    }
}
