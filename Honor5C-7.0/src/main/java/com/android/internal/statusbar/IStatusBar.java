package com.android.internal.statusbar;

import android.content.ComponentName;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IStatusBar extends IInterface {

    public static abstract class Stub extends Binder implements IStatusBar {
        private static final String DESCRIPTOR = "com.android.internal.statusbar.IStatusBar";
        static final int TRANSACTION_addQsTile = 31;
        static final int TRANSACTION_animateCollapsePanels = 6;
        static final int TRANSACTION_animateExpandNotificationsPanel = 4;
        static final int TRANSACTION_animateExpandSettingsPanel = 5;
        static final int TRANSACTION_appTransitionCancelled = 24;
        static final int TRANSACTION_appTransitionFinished = 26;
        static final int TRANSACTION_appTransitionPending = 23;
        static final int TRANSACTION_appTransitionStarting = 25;
        static final int TRANSACTION_buzzBeepBlinked = 11;
        static final int TRANSACTION_cancelPreloadRecentApps = 19;
        static final int TRANSACTION_clickQsTile = 33;
        static final int TRANSACTION_disable = 3;
        static final int TRANSACTION_dismissKeyboardShortcutsMenu = 21;
        static final int TRANSACTION_hideRecentApps = 15;
        static final int TRANSACTION_isNotificationPanelExpanded = 34;
        static final int TRANSACTION_notificationLightOff = 12;
        static final int TRANSACTION_notificationLightPulse = 13;
        static final int TRANSACTION_onCameraLaunchGestureDetected = 29;
        static final int TRANSACTION_preloadRecentApps = 18;
        static final int TRANSACTION_remQsTile = 32;
        static final int TRANSACTION_removeIcon = 2;
        static final int TRANSACTION_setIcon = 1;
        static final int TRANSACTION_setImeWindowStatus = 9;
        static final int TRANSACTION_setSystemUiVisibility = 7;
        static final int TRANSACTION_setWindowState = 10;
        static final int TRANSACTION_showAssistDisclosure = 27;
        static final int TRANSACTION_showRecentApps = 14;
        static final int TRANSACTION_showScreenPinningRequest = 20;
        static final int TRANSACTION_showTvPictureInPictureMenu = 30;
        static final int TRANSACTION_startAssist = 28;
        static final int TRANSACTION_toggleKeyboardShortcutsMenu = 22;
        static final int TRANSACTION_toggleRecentApps = 16;
        static final int TRANSACTION_toggleSplitScreen = 17;
        static final int TRANSACTION_topAppWindowChanged = 8;

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
                        _data.writeInt(Stub.TRANSACTION_setIcon);
                        icon.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setIcon, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_removeIcon, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_disable, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_animateExpandNotificationsPanel, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_animateExpandSettingsPanel, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_animateCollapsePanels, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_setIcon);
                        fullscreenBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (dockedBounds != null) {
                        _data.writeInt(Stub.TRANSACTION_setIcon);
                        dockedBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setSystemUiVisibility, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void topAppWindowChanged(boolean menuVisible) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (menuVisible) {
                        i = Stub.TRANSACTION_setIcon;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_topAppWindowChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setImeWindowStatus(IBinder token, int vis, int backDisposition, boolean showImeSwitcher) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(vis);
                    _data.writeInt(backDisposition);
                    if (showImeSwitcher) {
                        i = Stub.TRANSACTION_setIcon;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setImeWindowStatus, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_setWindowState, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void buzzBeepBlinked() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_buzzBeepBlinked, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notificationLightOff() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_notificationLightOff, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notificationLightPulse(int argb, int millisOn, int millisOff) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(argb);
                    _data.writeInt(millisOn);
                    _data.writeInt(millisOff);
                    this.mRemote.transact(Stub.TRANSACTION_notificationLightPulse, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void showRecentApps(boolean triggeredFromAltTab, boolean fromHome) throws RemoteException {
                int i = Stub.TRANSACTION_setIcon;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (triggeredFromAltTab) {
                        i2 = Stub.TRANSACTION_setIcon;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!fromHome) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_showRecentApps, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void hideRecentApps(boolean triggeredFromAltTab, boolean triggeredFromHomeKey) throws RemoteException {
                int i = Stub.TRANSACTION_setIcon;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (triggeredFromAltTab) {
                        i2 = Stub.TRANSACTION_setIcon;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!triggeredFromHomeKey) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_hideRecentApps, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_toggleRecentApps, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_toggleSplitScreen, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_preloadRecentApps, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_cancelPreloadRecentApps, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_showScreenPinningRequest, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_dismissKeyboardShortcutsMenu, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_toggleKeyboardShortcutsMenu, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_appTransitionPending, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_appTransitionCancelled, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_appTransitionStarting, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_appTransitionFinished, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_showAssistDisclosure, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_setIcon);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_startAssist, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_onCameraLaunchGestureDetected, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void showTvPictureInPictureMenu() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_showTvPictureInPictureMenu, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_setIcon);
                        tile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_addQsTile, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_setIcon);
                        tile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_remQsTile, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_setIcon);
                        tile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_clickQsTile, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_isNotificationPanelExpanded, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ComponentName componentName;
            switch (code) {
                case TRANSACTION_setIcon /*1*/:
                    StatusBarIcon statusBarIcon;
                    data.enforceInterface(DESCRIPTOR);
                    String _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        statusBarIcon = (StatusBarIcon) StatusBarIcon.CREATOR.createFromParcel(data);
                    } else {
                        statusBarIcon = null;
                    }
                    setIcon(_arg0, statusBarIcon);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeIcon /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeIcon(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_disable /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    disable(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_animateExpandNotificationsPanel /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    animateExpandNotificationsPanel();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_animateExpandSettingsPanel /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    animateExpandSettingsPanel(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_animateCollapsePanels /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    animateCollapsePanels();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setSystemUiVisibility /*7*/:
                    Rect rect;
                    Rect rect2;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg02 = data.readInt();
                    int _arg1 = data.readInt();
                    int _arg2 = data.readInt();
                    int _arg3 = data.readInt();
                    if (data.readInt() != 0) {
                        rect = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        rect = null;
                    }
                    if (data.readInt() != 0) {
                        rect2 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        rect2 = null;
                    }
                    setSystemUiVisibility(_arg02, _arg1, _arg2, _arg3, rect, rect2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_topAppWindowChanged /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    topAppWindowChanged(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setImeWindowStatus /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    setImeWindowStatus(data.readStrongBinder(), data.readInt(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setWindowState /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    setWindowState(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_buzzBeepBlinked /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    buzzBeepBlinked();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_notificationLightOff /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    notificationLightOff();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_notificationLightPulse /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    notificationLightPulse(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_showRecentApps /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    showRecentApps(data.readInt() != 0, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_hideRecentApps /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    hideRecentApps(data.readInt() != 0, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_toggleRecentApps /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    toggleRecentApps();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_toggleSplitScreen /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    toggleSplitScreen();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_preloadRecentApps /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    preloadRecentApps();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_cancelPreloadRecentApps /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    cancelPreloadRecentApps();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_showScreenPinningRequest /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    showScreenPinningRequest(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_dismissKeyboardShortcutsMenu /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    dismissKeyboardShortcutsMenu();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_toggleKeyboardShortcutsMenu /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    toggleKeyboardShortcutsMenu(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_appTransitionPending /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    appTransitionPending();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_appTransitionCancelled /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    appTransitionCancelled();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_appTransitionStarting /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    appTransitionStarting(data.readLong(), data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_appTransitionFinished /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    appTransitionFinished();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_showAssistDisclosure /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    showAssistDisclosure();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_startAssist /*28*/:
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    startAssist(bundle);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onCameraLaunchGestureDetected /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    onCameraLaunchGestureDetected(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_showTvPictureInPictureMenu /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    showTvPictureInPictureMenu();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addQsTile /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    addQsTile(componentName);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_remQsTile /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    remQsTile(componentName);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clickQsTile /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    clickQsTile(componentName);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isNotificationPanelExpanded /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = isNotificationPanelExpanded();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_setIcon : 0);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
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

    void buzzBeepBlinked() throws RemoteException;

    void cancelPreloadRecentApps() throws RemoteException;

    void clickQsTile(ComponentName componentName) throws RemoteException;

    void disable(int i, int i2) throws RemoteException;

    void dismissKeyboardShortcutsMenu() throws RemoteException;

    void hideRecentApps(boolean z, boolean z2) throws RemoteException;

    boolean isNotificationPanelExpanded() throws RemoteException;

    void notificationLightOff() throws RemoteException;

    void notificationLightPulse(int i, int i2, int i3) throws RemoteException;

    void onCameraLaunchGestureDetected(int i) throws RemoteException;

    void preloadRecentApps() throws RemoteException;

    void remQsTile(ComponentName componentName) throws RemoteException;

    void removeIcon(String str) throws RemoteException;

    void setIcon(String str, StatusBarIcon statusBarIcon) throws RemoteException;

    void setImeWindowStatus(IBinder iBinder, int i, int i2, boolean z) throws RemoteException;

    void setSystemUiVisibility(int i, int i2, int i3, int i4, Rect rect, Rect rect2) throws RemoteException;

    void setWindowState(int i, int i2) throws RemoteException;

    void showAssistDisclosure() throws RemoteException;

    void showRecentApps(boolean z, boolean z2) throws RemoteException;

    void showScreenPinningRequest(int i) throws RemoteException;

    void showTvPictureInPictureMenu() throws RemoteException;

    void startAssist(Bundle bundle) throws RemoteException;

    void toggleKeyboardShortcutsMenu(int i) throws RemoteException;

    void toggleRecentApps() throws RemoteException;

    void toggleSplitScreen() throws RemoteException;

    void topAppWindowChanged(boolean z) throws RemoteException;
}
