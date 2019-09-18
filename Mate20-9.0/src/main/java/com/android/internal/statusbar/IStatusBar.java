package com.android.internal.statusbar;

import android.content.ComponentName;
import android.graphics.Rect;
import android.hardware.biometrics.IBiometricPromptReceiver;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IStatusBar extends IInterface {

    public static abstract class Stub extends Binder implements IStatusBar {
        private static final String DESCRIPTOR = "com.android.internal.statusbar.IStatusBar";
        static final int TRANSACTION_addQsTile = 33;
        static final int TRANSACTION_animateCollapsePanels = 6;
        static final int TRANSACTION_animateExpandNotificationsPanel = 4;
        static final int TRANSACTION_animateExpandSettingsPanel = 5;
        static final int TRANSACTION_appTransitionCancelled = 23;
        static final int TRANSACTION_appTransitionFinished = 25;
        static final int TRANSACTION_appTransitionPending = 22;
        static final int TRANSACTION_appTransitionStarting = 24;
        static final int TRANSACTION_cancelPreloadRecentApps = 18;
        static final int TRANSACTION_clickQsTile = 35;
        static final int TRANSACTION_disable = 3;
        static final int TRANSACTION_dismissKeyboardShortcutsMenu = 20;
        static final int TRANSACTION_handleSystemKey = 36;
        static final int TRANSACTION_hideFingerprintDialog = 44;
        static final int TRANSACTION_hideRecentApps = 14;
        static final int TRANSACTION_isNotificationPanelExpanded = 45;
        static final int TRANSACTION_onCameraLaunchGestureDetected = 28;
        static final int TRANSACTION_onFingerprintAuthenticated = 41;
        static final int TRANSACTION_onFingerprintError = 43;
        static final int TRANSACTION_onFingerprintHelp = 42;
        static final int TRANSACTION_onProposedRotationChanged = 31;
        static final int TRANSACTION_preloadRecentApps = 17;
        static final int TRANSACTION_remQsTile = 34;
        static final int TRANSACTION_removeIcon = 2;
        static final int TRANSACTION_setIcon = 1;
        static final int TRANSACTION_setImeWindowStatus = 11;
        static final int TRANSACTION_setSystemUiVisibility = 9;
        static final int TRANSACTION_setTopAppHidesStatusBar = 32;
        static final int TRANSACTION_setWindowState = 12;
        static final int TRANSACTION_showAssistDisclosure = 26;
        static final int TRANSACTION_showFingerprintDialog = 40;
        static final int TRANSACTION_showGlobalActionsMenu = 30;
        static final int TRANSACTION_showPictureInPictureMenu = 29;
        static final int TRANSACTION_showPinningEnterExitToast = 37;
        static final int TRANSACTION_showPinningEscapeToast = 38;
        static final int TRANSACTION_showRecentApps = 13;
        static final int TRANSACTION_showScreenPinningRequest = 19;
        static final int TRANSACTION_showShutdownUi = 39;
        static final int TRANSACTION_showWirelessChargingAnimation = 8;
        static final int TRANSACTION_startAssist = 27;
        static final int TRANSACTION_toggleKeyboardShortcutsMenu = 21;
        static final int TRANSACTION_togglePanel = 7;
        static final int TRANSACTION_toggleRecentApps = 15;
        static final int TRANSACTION_toggleSplitScreen = 16;
        static final int TRANSACTION_topAppWindowChanged = 10;

        private static class Proxy implements IStatusBar {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void setIcon(String slot, StatusBarIcon icon) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(slot);
                    if (icon != null) {
                        _data.writeInt(1);
                        icon.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeIcon(String slot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(slot);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disable(int state1, int state2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state1);
                    _data.writeInt(state2);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void animateExpandNotificationsPanel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void animateExpandSettingsPanel(String subPanel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(subPanel);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void animateCollapsePanels() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void togglePanel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void showWirelessChargingAnimation(int batteryLevel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(batteryLevel);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSystemUiVisibility(int vis, int fullscreenStackVis, int dockedStackVis, int mask, Rect fullscreenBounds, Rect dockedBounds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(vis);
                    _data.writeInt(fullscreenStackVis);
                    _data.writeInt(dockedStackVis);
                    _data.writeInt(mask);
                    if (fullscreenBounds != null) {
                        _data.writeInt(1);
                        fullscreenBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (dockedBounds != null) {
                        _data.writeInt(1);
                        dockedBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void topAppWindowChanged(boolean menuVisible) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(menuVisible);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setImeWindowStatus(IBinder token, int vis, int backDisposition, boolean showImeSwitcher) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(vis);
                    _data.writeInt(backDisposition);
                    _data.writeInt(showImeSwitcher);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setWindowState(int window, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(window);
                    _data.writeInt(state);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void showRecentApps(boolean triggeredFromAltTab) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(triggeredFromAltTab);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void hideRecentApps(boolean triggeredFromAltTab, boolean triggeredFromHomeKey) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(triggeredFromAltTab);
                    _data.writeInt(triggeredFromHomeKey);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void toggleRecentApps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void toggleSplitScreen() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void preloadRecentApps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelPreloadRecentApps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void showScreenPinningRequest(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void dismissKeyboardShortcutsMenu() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void toggleKeyboardShortcutsMenu(int deviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceId);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void appTransitionPending() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void appTransitionCancelled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void appTransitionStarting(long statusBarAnimationsStartTime, long statusBarAnimationsDuration) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(statusBarAnimationsStartTime);
                    _data.writeLong(statusBarAnimationsDuration);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void appTransitionFinished() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void showAssistDisclosure() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startAssist(Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onCameraLaunchGestureDetected(int source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(source);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void showPictureInPictureMenu() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void showGlobalActionsMenu() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onProposedRotationChanged(int rotation, boolean isValid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rotation);
                    _data.writeInt(isValid);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setTopAppHidesStatusBar(boolean hidesStatusBar) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hidesStatusBar);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addQsTile(ComponentName tile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tile != null) {
                        _data.writeInt(1);
                        tile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void remQsTile(ComponentName tile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tile != null) {
                        _data.writeInt(1);
                        tile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clickQsTile(ComponentName tile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tile != null) {
                        _data.writeInt(1);
                        tile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void handleSystemKey(int key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(key);
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void showPinningEnterExitToast(boolean entering) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(entering);
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void showPinningEscapeToast() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void showShutdownUi(boolean isReboot, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isReboot);
                    _data.writeString(reason);
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void showFingerprintDialog(Bundle bundle, IBiometricPromptReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onFingerprintAuthenticated() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onFingerprintHelp(String message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(message);
                    this.mRemote.transact(42, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onFingerprintError(String error) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(error);
                    this.mRemote.transact(43, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void hideFingerprintDialog() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(44, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isNotificationPanelExpanded() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(45, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IStatusBar asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IStatusBar)) {
                return new Proxy(obj);
            }
            return (IStatusBar) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: com.android.internal.statusbar.StatusBarIcon} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v28, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v36, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v40, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v44, resolved type: android.content.ComponentName} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v53, resolved type: android.os.Bundle} */
        /* JADX WARNING: type inference failed for: r0v1 */
        /* JADX WARNING: type inference failed for: r0v10 */
        /* JADX WARNING: type inference failed for: r0v61 */
        /* JADX WARNING: type inference failed for: r0v62 */
        /* JADX WARNING: type inference failed for: r0v63 */
        /* JADX WARNING: type inference failed for: r0v64 */
        /* JADX WARNING: type inference failed for: r0v65 */
        /* JADX WARNING: type inference failed for: r0v66 */
        /* JADX WARNING: type inference failed for: r0v67 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Rect _arg4;
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                ? _arg0 = 0;
                boolean _arg1 = false;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg0 = StatusBarIcon.CREATOR.createFromParcel(parcel);
                        }
                        setIcon(_arg02, _arg0);
                        reply.writeNoException();
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        removeIcon(data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        disable(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        animateExpandNotificationsPanel();
                        reply.writeNoException();
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        animateExpandSettingsPanel(data.readString());
                        reply.writeNoException();
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        animateCollapsePanels();
                        reply.writeNoException();
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        togglePanel();
                        reply.writeNoException();
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        showWirelessChargingAnimation(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        int _arg12 = data.readInt();
                        int _arg2 = data.readInt();
                        int _arg3 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg4 = (Rect) Rect.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg4 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg0 = (Rect) Rect.CREATOR.createFromParcel(parcel);
                        }
                        setSystemUiVisibility(_arg03, _arg12, _arg2, _arg3, _arg4, _arg0);
                        reply.writeNoException();
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        topAppWindowChanged(_arg1);
                        reply.writeNoException();
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg04 = data.readStrongBinder();
                        int _arg13 = data.readInt();
                        int _arg22 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setImeWindowStatus(_arg04, _arg13, _arg22, _arg1);
                        reply.writeNoException();
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        setWindowState(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        showRecentApps(_arg1);
                        reply.writeNoException();
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _arg05 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        hideRecentApps(_arg05, _arg1);
                        reply.writeNoException();
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        toggleRecentApps();
                        reply.writeNoException();
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        toggleSplitScreen();
                        reply.writeNoException();
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        preloadRecentApps();
                        reply.writeNoException();
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        cancelPreloadRecentApps();
                        reply.writeNoException();
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        showScreenPinningRequest(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        dismissKeyboardShortcutsMenu();
                        reply.writeNoException();
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        toggleKeyboardShortcutsMenu(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        appTransitionPending();
                        reply.writeNoException();
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        appTransitionCancelled();
                        reply.writeNoException();
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        appTransitionStarting(data.readLong(), data.readLong());
                        reply.writeNoException();
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        appTransitionFinished();
                        reply.writeNoException();
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        showAssistDisclosure();
                        reply.writeNoException();
                        return true;
                    case 27:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        startAssist(_arg0);
                        reply.writeNoException();
                        return true;
                    case 28:
                        parcel.enforceInterface(DESCRIPTOR);
                        onCameraLaunchGestureDetected(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 29:
                        parcel.enforceInterface(DESCRIPTOR);
                        showPictureInPictureMenu();
                        reply.writeNoException();
                        return true;
                    case 30:
                        parcel.enforceInterface(DESCRIPTOR);
                        showGlobalActionsMenu();
                        reply.writeNoException();
                        return true;
                    case 31:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        onProposedRotationChanged(_arg06, _arg1);
                        reply.writeNoException();
                        return true;
                    case 32:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setTopAppHidesStatusBar(_arg1);
                        reply.writeNoException();
                        return true;
                    case 33:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        addQsTile(_arg0);
                        reply.writeNoException();
                        return true;
                    case 34:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        remQsTile(_arg0);
                        reply.writeNoException();
                        return true;
                    case 35:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        clickQsTile(_arg0);
                        reply.writeNoException();
                        return true;
                    case 36:
                        parcel.enforceInterface(DESCRIPTOR);
                        handleSystemKey(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 37:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        showPinningEnterExitToast(_arg1);
                        reply.writeNoException();
                        return true;
                    case 38:
                        parcel.enforceInterface(DESCRIPTOR);
                        showPinningEscapeToast();
                        reply.writeNoException();
                        return true;
                    case 39:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        showShutdownUi(_arg1, data.readString());
                        reply.writeNoException();
                        return true;
                    case 40:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        showFingerprintDialog(_arg0, IBiometricPromptReceiver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 41:
                        parcel.enforceInterface(DESCRIPTOR);
                        onFingerprintAuthenticated();
                        reply.writeNoException();
                        return true;
                    case 42:
                        parcel.enforceInterface(DESCRIPTOR);
                        onFingerprintHelp(data.readString());
                        reply.writeNoException();
                        return true;
                    case 43:
                        parcel.enforceInterface(DESCRIPTOR);
                        onFingerprintError(data.readString());
                        reply.writeNoException();
                        return true;
                    case 44:
                        parcel.enforceInterface(DESCRIPTOR);
                        hideFingerprintDialog();
                        reply.writeNoException();
                        return true;
                    case 45:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result = isNotificationPanelExpanded();
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void addQsTile(ComponentName componentName) throws RemoteException;

    void animateCollapsePanels() throws RemoteException;

    void animateExpandNotificationsPanel() throws RemoteException;

    void animateExpandSettingsPanel(String str) throws RemoteException;

    void appTransitionCancelled() throws RemoteException;

    void appTransitionFinished() throws RemoteException;

    void appTransitionPending() throws RemoteException;

    void appTransitionStarting(long j, long j2) throws RemoteException;

    void cancelPreloadRecentApps() throws RemoteException;

    void clickQsTile(ComponentName componentName) throws RemoteException;

    void disable(int i, int i2) throws RemoteException;

    void dismissKeyboardShortcutsMenu() throws RemoteException;

    void handleSystemKey(int i) throws RemoteException;

    void hideFingerprintDialog() throws RemoteException;

    void hideRecentApps(boolean z, boolean z2) throws RemoteException;

    boolean isNotificationPanelExpanded() throws RemoteException;

    void onCameraLaunchGestureDetected(int i) throws RemoteException;

    void onFingerprintAuthenticated() throws RemoteException;

    void onFingerprintError(String str) throws RemoteException;

    void onFingerprintHelp(String str) throws RemoteException;

    void onProposedRotationChanged(int i, boolean z) throws RemoteException;

    void preloadRecentApps() throws RemoteException;

    void remQsTile(ComponentName componentName) throws RemoteException;

    void removeIcon(String str) throws RemoteException;

    void setIcon(String str, StatusBarIcon statusBarIcon) throws RemoteException;

    void setImeWindowStatus(IBinder iBinder, int i, int i2, boolean z) throws RemoteException;

    void setSystemUiVisibility(int i, int i2, int i3, int i4, Rect rect, Rect rect2) throws RemoteException;

    void setTopAppHidesStatusBar(boolean z) throws RemoteException;

    void setWindowState(int i, int i2) throws RemoteException;

    void showAssistDisclosure() throws RemoteException;

    void showFingerprintDialog(Bundle bundle, IBiometricPromptReceiver iBiometricPromptReceiver) throws RemoteException;

    void showGlobalActionsMenu() throws RemoteException;

    void showPictureInPictureMenu() throws RemoteException;

    void showPinningEnterExitToast(boolean z) throws RemoteException;

    void showPinningEscapeToast() throws RemoteException;

    void showRecentApps(boolean z) throws RemoteException;

    void showScreenPinningRequest(int i) throws RemoteException;

    void showShutdownUi(boolean z, String str) throws RemoteException;

    void showWirelessChargingAnimation(int i) throws RemoteException;

    void startAssist(Bundle bundle) throws RemoteException;

    void toggleKeyboardShortcutsMenu(int i) throws RemoteException;

    void togglePanel() throws RemoteException;

    void toggleRecentApps() throws RemoteException;

    void toggleSplitScreen() throws RemoteException;

    void topAppWindowChanged(boolean z) throws RemoteException;
}
