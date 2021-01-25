package com.android.internal.hwsystemui;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.KeyEvent;

public interface IHwSystemUIController extends IInterface {
    void closeClienTopWindow() throws RemoteException;

    void dispatchKeyEventForExclusiveKeyboard(KeyEvent keyEvent) throws RemoteException;

    void hideImeStatusIcon() throws RemoteException;

    void lockScreen() throws RemoteException;

    void screenshotPcDisplay() throws RemoteException;

    void showClientStartMenu() throws RemoteException;

    void showClientTopBar() throws RemoteException;

    void showImeStatusIcon(int i, String str) throws RemoteException;

    void triggerSwitchClientTaskView(boolean z) throws RemoteException;

    void userActivityOnDesktop() throws RemoteException;

    public static class Default implements IHwSystemUIController {
        @Override // com.android.internal.hwsystemui.IHwSystemUIController
        public void showClientTopBar() throws RemoteException {
        }

        @Override // com.android.internal.hwsystemui.IHwSystemUIController
        public void showClientStartMenu() throws RemoteException {
        }

        @Override // com.android.internal.hwsystemui.IHwSystemUIController
        public void screenshotPcDisplay() throws RemoteException {
        }

        @Override // com.android.internal.hwsystemui.IHwSystemUIController
        public void closeClienTopWindow() throws RemoteException {
        }

        @Override // com.android.internal.hwsystemui.IHwSystemUIController
        public void triggerSwitchClientTaskView(boolean show) throws RemoteException {
        }

        @Override // com.android.internal.hwsystemui.IHwSystemUIController
        public void userActivityOnDesktop() throws RemoteException {
        }

        @Override // com.android.internal.hwsystemui.IHwSystemUIController
        public void lockScreen() throws RemoteException {
        }

        @Override // com.android.internal.hwsystemui.IHwSystemUIController
        public void dispatchKeyEventForExclusiveKeyboard(KeyEvent ke) throws RemoteException {
        }

        @Override // com.android.internal.hwsystemui.IHwSystemUIController
        public void showImeStatusIcon(int iconResId, String pkgName) throws RemoteException {
        }

        @Override // com.android.internal.hwsystemui.IHwSystemUIController
        public void hideImeStatusIcon() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwSystemUIController {
        private static final String DESCRIPTOR = "com.android.internal.hwsystemui.IHwSystemUIController";
        static final int TRANSACTION_closeClienTopWindow = 4;
        static final int TRANSACTION_dispatchKeyEventForExclusiveKeyboard = 8;
        static final int TRANSACTION_hideImeStatusIcon = 10;
        static final int TRANSACTION_lockScreen = 7;
        static final int TRANSACTION_screenshotPcDisplay = 3;
        static final int TRANSACTION_showClientStartMenu = 2;
        static final int TRANSACTION_showClientTopBar = 1;
        static final int TRANSACTION_showImeStatusIcon = 9;
        static final int TRANSACTION_triggerSwitchClientTaskView = 5;
        static final int TRANSACTION_userActivityOnDesktop = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwSystemUIController asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwSystemUIController)) {
                return new Proxy(obj);
            }
            return (IHwSystemUIController) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "showClientTopBar";
                case 2:
                    return "showClientStartMenu";
                case 3:
                    return "screenshotPcDisplay";
                case 4:
                    return "closeClienTopWindow";
                case 5:
                    return "triggerSwitchClientTaskView";
                case 6:
                    return "userActivityOnDesktop";
                case 7:
                    return "lockScreen";
                case 8:
                    return "dispatchKeyEventForExclusiveKeyboard";
                case 9:
                    return "showImeStatusIcon";
                case 10:
                    return "hideImeStatusIcon";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            KeyEvent _arg0;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        showClientTopBar();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        showClientStartMenu();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        screenshotPcDisplay();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        closeClienTopWindow();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        triggerSwitchClientTaskView(data.readInt() != 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        userActivityOnDesktop();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        lockScreen();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = KeyEvent.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        dispatchKeyEventForExclusiveKeyboard(_arg0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        showImeStatusIcon(data.readInt(), data.readString());
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        hideImeStatusIcon();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwSystemUIController {
            public static IHwSystemUIController sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.android.internal.hwsystemui.IHwSystemUIController
            public void showClientTopBar() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().showClientTopBar();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.hwsystemui.IHwSystemUIController
            public void showClientStartMenu() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().showClientStartMenu();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.hwsystemui.IHwSystemUIController
            public void screenshotPcDisplay() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().screenshotPcDisplay();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.hwsystemui.IHwSystemUIController
            public void closeClienTopWindow() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().closeClienTopWindow();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.hwsystemui.IHwSystemUIController
            public void triggerSwitchClientTaskView(boolean show) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(show ? 1 : 0);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().triggerSwitchClientTaskView(show);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.hwsystemui.IHwSystemUIController
            public void userActivityOnDesktop() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().userActivityOnDesktop();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.hwsystemui.IHwSystemUIController
            public void lockScreen() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().lockScreen();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.hwsystemui.IHwSystemUIController
            public void dispatchKeyEventForExclusiveKeyboard(KeyEvent ke) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ke != null) {
                        _data.writeInt(1);
                        ke.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dispatchKeyEventForExclusiveKeyboard(ke);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.hwsystemui.IHwSystemUIController
            public void showImeStatusIcon(int iconResId, String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(iconResId);
                    _data.writeString(pkgName);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().showImeStatusIcon(iconResId, pkgName);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.hwsystemui.IHwSystemUIController
            public void hideImeStatusIcon() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().hideImeStatusIcon();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwSystemUIController impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwSystemUIController getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
