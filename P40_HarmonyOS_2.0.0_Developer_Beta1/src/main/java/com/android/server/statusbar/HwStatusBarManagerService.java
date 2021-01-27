package com.android.server.statusbar;

import android.content.Context;
import android.hardware.biometrics.IBiometricServiceReceiverInternal;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.CollectData;
import android.util.Slog;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.android.internal.statusbar.IStatusBar;
import com.android.server.HwServiceExFactory;
import com.android.server.LocalServices;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.hidata.wavemapping.cons.WMStateCons;
import com.android.server.wm.WindowManagerService;
import com.huawei.android.biometric.BiometricServiceReceiverListenerEx;
import com.huawei.server.HwPartIawareUtil;
import com.huawei.server.fingerprint.DefaultFingerViewController;
import com.huawei.server.fingerprint.HwPartFingerprintFactory;
import huawei.android.hardware.fingerprint.FingerprintManagerEx;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class HwStatusBarManagerService extends StatusBarManagerService implements IHwStatusBarEventListener {
    private static final boolean DEBUG = false;
    private static final int FINGERPRINT_HARDWARE_OUTSCREEN = 0;
    private static final int FINGERPRINT_TYPE_UNDEFINED = -1;
    private static final Integer[] INTRESTED_KEYEVENT_CODE = {25, 24};
    private static final boolean IS_TV = "tv".equals(SystemProperties.get("ro.build.characteristics", AppActConstant.VALUE_DEFAULT));
    private static final int MSG_NOTIFY_AUTHENCATION = 87;
    private static final String TAG = "HwStatusBarManagerService";
    private boolean isStatusBarExpanded;
    volatile IStatusBar mBar;
    private Context mContext;
    private DefaultFingerViewController mFingerViewController;
    private int mHardwareType = -1;
    private HwNotificationDelegate mHwNotificationDelegate;
    private List<Integer> mIntrestedKeyCodes;
    private final Object mLock = new Object();
    private IHwStatusBarManagerServiceEx mStatusBarManagerServiceEx;

    public interface HwNotificationDelegate {
        void onNotificationResidentClear(String str, String str2, int i, int i2);
    }

    private void enforceStatusBar() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR", "StatusBarManagerService");
    }

    public HwStatusBarManagerService(Context context, WindowManagerService windowManager) {
        super(context, windowManager);
        this.mContext = context;
        this.mIntrestedKeyCodes = Arrays.asList(INTRESTED_KEYEVENT_CODE);
        this.mStatusBarManagerServiceEx = HwServiceExFactory.getHwStatusBarManagerServiceEx();
        this.mStatusBarManagerServiceEx.init(this);
    }

    public void setHwNotificationDelegate(HwNotificationDelegate delegate) {
        Slog.d(TAG, "setHwNotificationCallbacks delegate = " + delegate);
        this.mHwNotificationDelegate = delegate;
    }

    public void onKeyEvent(KeyEvent event) {
        if (this.mIntrestedKeyCodes.contains(Integer.valueOf(event.getKeyCode()))) {
            transactKeyEvent(event);
        }
    }

    public void onLauncherShadowStateChange(boolean isEnable) {
        transactLauncherShadowState(isEnable);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        Bundle arg0;
        if (code == 101) {
            data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
            transactToStatusBar(101, "resentapp", "resentapp", data.readInt());
            reply.writeNoException();
            return true;
        } else if (code != 121) {
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
                case 108:
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
                            } else {
                                arg0 = null;
                            }
                            startAssist(arg0);
                            reply.writeNoException();
                            return true;
                        case 125:
                            if (data != null) {
                                data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                                int pid = data.readInt();
                                if (pid >= 0) {
                                    reclaimProcessMemory(pid);
                                }
                                reply.writeNoException();
                            }
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

    private void reclaimProcessMemory(int pid) {
        if (!IS_TV) {
            try {
                this.mContext.enforceCallingOrSelfPermission("android.permission.STATUS_BAR", "StatusBarManagerService");
                Slog.i(TAG, "reclaimProcessAll: " + pid);
                HwPartIawareUtil.reclaimProcessAll(pid, false);
            } catch (SecurityException e) {
                Slog.e(TAG, "InterruptedException");
            } catch (Exception e2) {
                Slog.e(TAG, "reclaimProcessMemory exception");
            }
        }
    }

    private void startAssist(Bundle args) {
        enforceStatusBar();
        ((StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class)).startAssist(args);
    }

    private void transactToStatusBar(int code, String transactName, String paramName, int paramValue) {
        enforceStatusBar();
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
                    localRemoteException.printStackTrace();
                    reply.recycle();
                } catch (Throwable th) {
                    reply.recycle();
                    data.recycle();
                    throw th;
                }
                data.recycle();
            }
        }
    }

    private void transactToStatusBar(int code, String transactName, int isEmuiStyle, int statusbarColor, int navigationBarColor, int isEmuiLightStyle) {
        enforceStatusBar();
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
                    localRemoteException.printStackTrace();
                    reply.recycle();
                } catch (Throwable th) {
                    reply.recycle();
                    data.recycle();
                    throw th;
                }
                data.recycle();
            }
        }
    }

    public void expandNotificationsPanel() {
        HwStatusBarManagerService.super.expandNotificationsPanel();
        this.isStatusBarExpanded = true;
    }

    public void collapsePanels() {
        HwStatusBarManagerService.super.collapsePanels();
        this.isStatusBarExpanded = false;
    }

    public boolean statusBarExpanded() {
        return this.isStatusBarExpanded;
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
        int resId = AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_STATUS_BAR);
        if (resManager.isResourceNeeded(resId)) {
            Bundle bundle = new Bundle();
            bundle.putInt("eventid", event);
            CollectData data = new CollectData(resId, System.currentTimeMillis(), bundle);
            long id = Binder.clearCallingIdentity();
            resManager.reportData(data);
            Binder.restoreCallingIdentity(id);
        }
    }

    public void toggleSplitScreenByLineGesture(int code, String transactName, int param1, int param2) {
        enforceStatusBar();
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
                    localRemoteException.printStackTrace();
                    reply.recycle();
                } catch (Throwable th) {
                    reply.recycle();
                    data.recycle();
                    throw th;
                }
                data.recycle();
            }
        }
    }

    private void transactRecentCall(int code, String transactName) {
        enforceStatusBar();
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
                    localRemoteException.printStackTrace();
                    reply.recycle();
                } catch (Throwable th) {
                    reply.recycle();
                    data.recycle();
                    throw th;
                }
                data.recycle();
            }
        }
    }

    private void notchTransactToStatusBarStatus(int code, String transactName, int isEmuiStyle, int statusbarColor, int navigationBarColor, int isEmuiLightStyle, int statusbarStateFlag, int barVisibility) {
        enforceStatusBar();
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
                    Slog.d(TAG, "RemoteException happen in the method notchTransactToStatusBarStatus!");
                    reply.recycle();
                } catch (Throwable th) {
                    reply.recycle();
                    data.recycle();
                    throw th;
                }
                data.recycle();
            }
        }
    }

    private void transactGestureNavEvent(int code, String transactName, MotionEvent event) {
        enforceStatusBar();
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
                } catch (RemoteException e) {
                    Slog.d(TAG, "exception occur in " + transactName + ", event:" + event, e);
                } finally {
                    data.recycle();
                }
            }
        }
    }

    private void transactKeyEvent(KeyEvent event) {
        enforceStatusBar();
        synchronized (this.mLock) {
            this.mBar = getStatusBar();
            if (this.mBar != null) {
                Parcel data = Parcel.obtain();
                try {
                    IBinder statusBarBinder = this.mBar.asBinder();
                    if (statusBarBinder != null) {
                        data.writeInterfaceToken("com.android.internal.statusbar.IStatusBar");
                        data.writeParcelable(event, 0);
                        statusBarBinder.transact(131, data, null, 1);
                    }
                } catch (RemoteException e) {
                    Slog.d(TAG, "exception occur in transactKeyEvent, event:" + event, e);
                } finally {
                    data.recycle();
                }
            }
        }
    }

    private void transactLauncherShadowState(boolean isEnable) {
        enforceStatusBar();
        synchronized (this.mLock) {
            this.mBar = getStatusBar();
            if (this.mBar != null) {
                Parcel data = Parcel.obtain();
                try {
                    IBinder statusBarBinder = this.mBar.asBinder();
                    if (statusBarBinder != null) {
                        data.writeInterfaceToken("com.android.internal.statusbar.IStatusBar");
                        data.writeInt(isEnable ? 1 : 0);
                        statusBarBinder.transact(WMStateCons.MSG_BACK_4G_COVERAGE, data, null, 1);
                    }
                } catch (RemoteException e) {
                    Slog.d(TAG, "exception occur in transactLauncherShadowState, isEnable:" + isEnable, e);
                } finally {
                    data.recycle();
                }
            }
        }
    }

    public void showBiometricDialog(Bundle bundle, IBiometricServiceReceiverInternal receiver, int type, boolean isRequireConfirmation, int userId) {
        if (this.mFingerViewController == null) {
            this.mHardwareType = FingerprintManagerEx.getHardwareType();
            if (this.mHardwareType > 0) {
                Slog.d(TAG, "the phone has inscreen fingerprints");
                this.mFingerViewController = HwPartFingerprintFactory.loadFactory().getFingerViewController(this.mContext);
            }
        }
        DefaultFingerViewController defaultFingerViewController = this.mFingerViewController;
        if (defaultFingerViewController == null || type != 1) {
            Slog.d(TAG, "showBiometricDialog hw statusbar");
            HwStatusBarManagerService.super.showBiometricDialog(bundle, receiver, type, isRequireConfirmation, userId);
            return;
        }
        Optional<Handler> fingerHandler = defaultFingerViewController.getFingerHandler();
        if (fingerHandler.isPresent()) {
            Bundle fingerBundle = new Bundle();
            fingerBundle.putInt("userId", userId);
            fingerBundle.putBundle("bundle", bundle);
            Message message = fingerHandler.get().obtainMessage();
            message.what = MSG_NOTIFY_AUTHENCATION;
            message.setData(fingerBundle);
            BiometricServiceReceiverListenerEx listenr = new BiometricServiceReceiverListenerEx();
            listenr.setBiometricServiceReceiver(receiver);
            this.mFingerViewController.setBiometricServiceReceiver(listenr);
            this.mFingerViewController.setBiometricRequireConfirmation(isRequireConfirmation);
            fingerHandler.get().sendMessage(message);
        }
    }
}
