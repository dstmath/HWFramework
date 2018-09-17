package com.android.internal.statusbar;

import android.content.ComponentName;
import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.List;

public interface IStatusBarService extends IInterface {

    public static abstract class Stub extends Binder implements IStatusBarService {
        private static final String DESCRIPTOR = "com.android.internal.statusbar.IStatusBarService";
        static final int TRANSACTION_addTile = 24;
        static final int TRANSACTION_clearNotificationEffects = 15;
        static final int TRANSACTION_clickTile = 26;
        static final int TRANSACTION_collapsePanels = 2;
        static final int TRANSACTION_disable = 3;
        static final int TRANSACTION_disable2 = 5;
        static final int TRANSACTION_disable2ForUser = 6;
        static final int TRANSACTION_disableForUser = 4;
        static final int TRANSACTION_expandNotificationsPanel = 1;
        static final int TRANSACTION_expandSettingsPanel = 11;
        static final int TRANSACTION_isNotificationsPanelExpand = 27;
        static final int TRANSACTION_onClearAllNotifications = 19;
        static final int TRANSACTION_onNotificationActionClick = 17;
        static final int TRANSACTION_onNotificationClear = 20;
        static final int TRANSACTION_onNotificationClick = 16;
        static final int TRANSACTION_onNotificationError = 18;
        static final int TRANSACTION_onNotificationExpansionChanged = 22;
        static final int TRANSACTION_onNotificationVisibilityChanged = 21;
        static final int TRANSACTION_onPanelHidden = 14;
        static final int TRANSACTION_onPanelRevealed = 13;
        static final int TRANSACTION_registerStatusBar = 12;
        static final int TRANSACTION_remTile = 25;
        static final int TRANSACTION_removeIcon = 9;
        static final int TRANSACTION_setIcon = 7;
        static final int TRANSACTION_setIconVisibility = 8;
        static final int TRANSACTION_setImeWindowStatus = 10;
        static final int TRANSACTION_setSystemUiVisibility = 23;

        private static class Proxy implements IStatusBarService {
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

            public void expandNotificationsPanel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_expandNotificationsPanel, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void collapsePanels() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_collapsePanels, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disable(int what, IBinder token, String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(what);
                    _data.writeStrongBinder(token);
                    _data.writeString(pkg);
                    this.mRemote.transact(Stub.TRANSACTION_disable, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disableForUser(int what, IBinder token, String pkg, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(what);
                    _data.writeStrongBinder(token);
                    _data.writeString(pkg);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_disableForUser, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disable2(int what, IBinder token, String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(what);
                    _data.writeStrongBinder(token);
                    _data.writeString(pkg);
                    this.mRemote.transact(Stub.TRANSACTION_disable2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disable2ForUser(int what, IBinder token, String pkg, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(what);
                    _data.writeStrongBinder(token);
                    _data.writeString(pkg);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_disable2ForUser, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setIcon(String slot, String iconPackage, int iconId, int iconLevel, String contentDescription) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(slot);
                    _data.writeString(iconPackage);
                    _data.writeInt(iconId);
                    _data.writeInt(iconLevel);
                    _data.writeString(contentDescription);
                    this.mRemote.transact(Stub.TRANSACTION_setIcon, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setIconVisibility(String slot, boolean visible) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(slot);
                    if (visible) {
                        i = Stub.TRANSACTION_expandNotificationsPanel;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setIconVisibility, _data, _reply, 0);
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
                        i = Stub.TRANSACTION_expandNotificationsPanel;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setImeWindowStatus, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void expandSettingsPanel(String subPanel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(subPanel);
                    this.mRemote.transact(Stub.TRANSACTION_expandSettingsPanel, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerStatusBar(IStatusBar callbacks, List<String> iconSlots, List<StatusBarIcon> iconList, int[] switches, List<IBinder> binders, Rect fullscreenStackBounds, Rect dockedStackBounds) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callbacks != null) {
                        iBinder = callbacks.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (switches == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(switches.length);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_registerStatusBar, _data, _reply, 0);
                    _reply.readException();
                    _reply.readStringList(iconSlots);
                    _reply.readTypedList(iconList, StatusBarIcon.CREATOR);
                    _reply.readIntArray(switches);
                    _reply.readBinderList(binders);
                    if (_reply.readInt() != 0) {
                        fullscreenStackBounds.readFromParcel(_reply);
                    }
                    if (_reply.readInt() != 0) {
                        dockedStackBounds.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onPanelRevealed(boolean clearNotificationEffects, int numItems) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (clearNotificationEffects) {
                        i = Stub.TRANSACTION_expandNotificationsPanel;
                    }
                    _data.writeInt(i);
                    _data.writeInt(numItems);
                    this.mRemote.transact(Stub.TRANSACTION_onPanelRevealed, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onPanelHidden() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onPanelHidden, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearNotificationEffects() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_clearNotificationEffects, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onNotificationClick(String key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    this.mRemote.transact(Stub.TRANSACTION_onNotificationClick, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onNotificationActionClick(String key, int actionIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeInt(actionIndex);
                    this.mRemote.transact(Stub.TRANSACTION_onNotificationActionClick, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onNotificationError(String pkg, String tag, int id, int uid, int initialPid, String message, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeString(tag);
                    _data.writeInt(id);
                    _data.writeInt(uid);
                    _data.writeInt(initialPid);
                    _data.writeString(message);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_onNotificationError, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onClearAllNotifications(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_onClearAllNotifications, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onNotificationClear(String pkg, String tag, int id, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeString(tag);
                    _data.writeInt(id);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_onNotificationClear, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onNotificationVisibilityChanged(NotificationVisibility[] newlyVisibleKeys, NotificationVisibility[] noLongerVisibleKeys) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(newlyVisibleKeys, 0);
                    _data.writeTypedArray(noLongerVisibleKeys, 0);
                    this.mRemote.transact(Stub.TRANSACTION_onNotificationVisibilityChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onNotificationExpansionChanged(String key, boolean userAction, boolean expanded) throws RemoteException {
                int i = Stub.TRANSACTION_expandNotificationsPanel;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    if (userAction) {
                        i2 = Stub.TRANSACTION_expandNotificationsPanel;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!expanded) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onNotificationExpansionChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSystemUiVisibility(int vis, int mask, String cause) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(vis);
                    _data.writeInt(mask);
                    _data.writeString(cause);
                    this.mRemote.transact(Stub.TRANSACTION_setSystemUiVisibility, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addTile(ComponentName tile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tile != null) {
                        _data.writeInt(Stub.TRANSACTION_expandNotificationsPanel);
                        tile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_addTile, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void remTile(ComponentName tile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tile != null) {
                        _data.writeInt(Stub.TRANSACTION_expandNotificationsPanel);
                        tile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_remTile, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clickTile(ComponentName tile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tile != null) {
                        _data.writeInt(Stub.TRANSACTION_expandNotificationsPanel);
                        tile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_clickTile, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isNotificationsPanelExpand() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isNotificationsPanelExpand, _data, _reply, 0);
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

        public static IStatusBarService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IStatusBarService)) {
                return new Proxy(obj);
            }
            return (IStatusBarService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ComponentName componentName;
            switch (code) {
                case TRANSACTION_expandNotificationsPanel /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    expandNotificationsPanel();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_collapsePanels /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    collapsePanels();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_disable /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    disable(data.readInt(), data.readStrongBinder(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_disableForUser /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    disableForUser(data.readInt(), data.readStrongBinder(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_disable2 /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    disable2(data.readInt(), data.readStrongBinder(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_disable2ForUser /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    disable2ForUser(data.readInt(), data.readStrongBinder(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setIcon /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    setIcon(data.readString(), data.readString(), data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setIconVisibility /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    setIconVisibility(data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeIcon /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeIcon(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setImeWindowStatus /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    setImeWindowStatus(data.readStrongBinder(), data.readInt(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_expandSettingsPanel /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    expandSettingsPanel(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registerStatusBar /*12*/:
                    int[] iArr;
                    data.enforceInterface(DESCRIPTOR);
                    IStatusBar _arg0 = com.android.internal.statusbar.IStatusBar.Stub.asInterface(data.readStrongBinder());
                    List<String> _arg1 = new ArrayList();
                    List<StatusBarIcon> _arg2 = new ArrayList();
                    int _arg3_length = data.readInt();
                    if (_arg3_length < 0) {
                        iArr = null;
                    } else {
                        iArr = new int[_arg3_length];
                    }
                    List<IBinder> _arg4 = new ArrayList();
                    Rect _arg5 = new Rect();
                    Rect _arg6 = new Rect();
                    registerStatusBar(_arg0, _arg1, _arg2, iArr, _arg4, _arg5, _arg6);
                    reply.writeNoException();
                    reply.writeStringList(_arg1);
                    reply.writeTypedList(_arg2);
                    reply.writeIntArray(iArr);
                    reply.writeBinderList(_arg4);
                    if (_arg5 != null) {
                        reply.writeInt(TRANSACTION_expandNotificationsPanel);
                        _arg5.writeToParcel(reply, TRANSACTION_expandNotificationsPanel);
                    } else {
                        reply.writeInt(0);
                    }
                    if (_arg6 != null) {
                        reply.writeInt(TRANSACTION_expandNotificationsPanel);
                        _arg6.writeToParcel(reply, TRANSACTION_expandNotificationsPanel);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_onPanelRevealed /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    onPanelRevealed(data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onPanelHidden /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    onPanelHidden();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearNotificationEffects /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearNotificationEffects();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onNotificationClick /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    onNotificationClick(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onNotificationActionClick /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    onNotificationActionClick(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onNotificationError /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    onNotificationError(data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onClearAllNotifications /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    onClearAllNotifications(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onNotificationClear /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    onNotificationClear(data.readString(), data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onNotificationVisibilityChanged /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    onNotificationVisibilityChanged((NotificationVisibility[]) data.createTypedArray(NotificationVisibility.CREATOR), (NotificationVisibility[]) data.createTypedArray(NotificationVisibility.CREATOR));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onNotificationExpansionChanged /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    onNotificationExpansionChanged(data.readString(), data.readInt() != 0, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setSystemUiVisibility /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    setSystemUiVisibility(data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addTile /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    addTile(componentName);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_remTile /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    remTile(componentName);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clickTile /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    clickTile(componentName);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isNotificationsPanelExpand /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = isNotificationsPanelExpand();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_expandNotificationsPanel : 0);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addTile(ComponentName componentName) throws RemoteException;

    void clearNotificationEffects() throws RemoteException;

    void clickTile(ComponentName componentName) throws RemoteException;

    void collapsePanels() throws RemoteException;

    void disable(int i, IBinder iBinder, String str) throws RemoteException;

    void disable2(int i, IBinder iBinder, String str) throws RemoteException;

    void disable2ForUser(int i, IBinder iBinder, String str, int i2) throws RemoteException;

    void disableForUser(int i, IBinder iBinder, String str, int i2) throws RemoteException;

    void expandNotificationsPanel() throws RemoteException;

    void expandSettingsPanel(String str) throws RemoteException;

    boolean isNotificationsPanelExpand() throws RemoteException;

    void onClearAllNotifications(int i) throws RemoteException;

    void onNotificationActionClick(String str, int i) throws RemoteException;

    void onNotificationClear(String str, String str2, int i, int i2) throws RemoteException;

    void onNotificationClick(String str) throws RemoteException;

    void onNotificationError(String str, String str2, int i, int i2, int i3, String str3, int i4) throws RemoteException;

    void onNotificationExpansionChanged(String str, boolean z, boolean z2) throws RemoteException;

    void onNotificationVisibilityChanged(NotificationVisibility[] notificationVisibilityArr, NotificationVisibility[] notificationVisibilityArr2) throws RemoteException;

    void onPanelHidden() throws RemoteException;

    void onPanelRevealed(boolean z, int i) throws RemoteException;

    void registerStatusBar(IStatusBar iStatusBar, List<String> list, List<StatusBarIcon> list2, int[] iArr, List<IBinder> list3, Rect rect, Rect rect2) throws RemoteException;

    void remTile(ComponentName componentName) throws RemoteException;

    void removeIcon(String str) throws RemoteException;

    void setIcon(String str, String str2, int i, int i2, String str3) throws RemoteException;

    void setIconVisibility(String str, boolean z) throws RemoteException;

    void setImeWindowStatus(IBinder iBinder, int i, int i2, boolean z) throws RemoteException;

    void setSystemUiVisibility(int i, int i2, String str) throws RemoteException;
}
