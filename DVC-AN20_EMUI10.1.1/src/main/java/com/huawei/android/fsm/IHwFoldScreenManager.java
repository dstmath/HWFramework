package com.huawei.android.fsm;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.android.fsm.IFoldDisplayModeListener;
import com.huawei.android.fsm.IFoldFsmTipsRequestListener;
import com.huawei.android.fsm.IFoldableStateListener;

public interface IHwFoldScreenManager extends IInterface {
    int getDisplayMode() throws RemoteException;

    int getFoldableState() throws RemoteException;

    int getPosture() throws RemoteException;

    int lockDisplayMode(int i) throws RemoteException;

    void registerFoldDisplayMode(IFoldDisplayModeListener iFoldDisplayModeListener) throws RemoteException;

    void registerFoldableState(IFoldableStateListener iFoldableStateListener, int i) throws RemoteException;

    void registerFsmTipsRequestListener(IFoldFsmTipsRequestListener iFoldFsmTipsRequestListener, int i) throws RemoteException;

    int reqShowTipsToFsm(int i, Bundle bundle) throws RemoteException;

    int setDisplayMode(int i) throws RemoteException;

    int unlockDisplayMode() throws RemoteException;

    void unregisterFoldDisplayMode(IFoldDisplayModeListener iFoldDisplayModeListener) throws RemoteException;

    void unregisterFoldableState(IFoldableStateListener iFoldableStateListener) throws RemoteException;

    void unregisterFsmTipsRequestListener(IFoldFsmTipsRequestListener iFoldFsmTipsRequestListener) throws RemoteException;

    public static class Default implements IHwFoldScreenManager {
        @Override // com.huawei.android.fsm.IHwFoldScreenManager
        public int getPosture() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.fsm.IHwFoldScreenManager
        public int getFoldableState() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.fsm.IHwFoldScreenManager
        public void registerFoldableState(IFoldableStateListener listener, int type) throws RemoteException {
        }

        @Override // com.huawei.android.fsm.IHwFoldScreenManager
        public void unregisterFoldableState(IFoldableStateListener listener) throws RemoteException {
        }

        @Override // com.huawei.android.fsm.IHwFoldScreenManager
        public int setDisplayMode(int mode) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.fsm.IHwFoldScreenManager
        public int getDisplayMode() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.fsm.IHwFoldScreenManager
        public int lockDisplayMode(int mode) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.fsm.IHwFoldScreenManager
        public int unlockDisplayMode() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.fsm.IHwFoldScreenManager
        public void registerFoldDisplayMode(IFoldDisplayModeListener listener) throws RemoteException {
        }

        @Override // com.huawei.android.fsm.IHwFoldScreenManager
        public void unregisterFoldDisplayMode(IFoldDisplayModeListener listener) throws RemoteException {
        }

        @Override // com.huawei.android.fsm.IHwFoldScreenManager
        public int reqShowTipsToFsm(int reqTipsType, Bundle data) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.fsm.IHwFoldScreenManager
        public void registerFsmTipsRequestListener(IFoldFsmTipsRequestListener listener, int type) throws RemoteException {
        }

        @Override // com.huawei.android.fsm.IHwFoldScreenManager
        public void unregisterFsmTipsRequestListener(IFoldFsmTipsRequestListener listener) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwFoldScreenManager {
        private static final String DESCRIPTOR = "com.huawei.android.fsm.IHwFoldScreenManager";
        static final int TRANSACTION_getDisplayMode = 6;
        static final int TRANSACTION_getFoldableState = 2;
        static final int TRANSACTION_getPosture = 1;
        static final int TRANSACTION_lockDisplayMode = 7;
        static final int TRANSACTION_registerFoldDisplayMode = 9;
        static final int TRANSACTION_registerFoldableState = 3;
        static final int TRANSACTION_registerFsmTipsRequestListener = 12;
        static final int TRANSACTION_reqShowTipsToFsm = 11;
        static final int TRANSACTION_setDisplayMode = 5;
        static final int TRANSACTION_unlockDisplayMode = 8;
        static final int TRANSACTION_unregisterFoldDisplayMode = 10;
        static final int TRANSACTION_unregisterFoldableState = 4;
        static final int TRANSACTION_unregisterFsmTipsRequestListener = 13;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwFoldScreenManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwFoldScreenManager)) {
                return new Proxy(obj);
            }
            return (IHwFoldScreenManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getPosture";
                case 2:
                    return "getFoldableState";
                case 3:
                    return "registerFoldableState";
                case 4:
                    return "unregisterFoldableState";
                case 5:
                    return "setDisplayMode";
                case 6:
                    return "getDisplayMode";
                case 7:
                    return "lockDisplayMode";
                case 8:
                    return "unlockDisplayMode";
                case 9:
                    return "registerFoldDisplayMode";
                case 10:
                    return "unregisterFoldDisplayMode";
                case 11:
                    return "reqShowTipsToFsm";
                case 12:
                    return "registerFsmTipsRequestListener";
                case 13:
                    return "unregisterFsmTipsRequestListener";
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
            Bundle _arg1;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getPosture();
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = getFoldableState();
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        registerFoldableState(IFoldableStateListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterFoldableState(IFoldableStateListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = setDisplayMode(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getDisplayMode();
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = lockDisplayMode(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = unlockDisplayMode();
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        registerFoldDisplayMode(IFoldDisplayModeListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterFoldDisplayMode(IFoldDisplayModeListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        int _result7 = reqShowTipsToFsm(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        registerFsmTipsRequestListener(IFoldFsmTipsRequestListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterFsmTipsRequestListener(IFoldFsmTipsRequestListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
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
        public static class Proxy implements IHwFoldScreenManager {
            public static IHwFoldScreenManager sDefaultImpl;
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

            @Override // com.huawei.android.fsm.IHwFoldScreenManager
            public int getPosture() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPosture();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.fsm.IHwFoldScreenManager
            public int getFoldableState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFoldableState();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.fsm.IHwFoldScreenManager
            public void registerFoldableState(IFoldableStateListener listener, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeInt(type);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerFoldableState(listener, type);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.fsm.IHwFoldScreenManager
            public void unregisterFoldableState(IFoldableStateListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterFoldableState(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.fsm.IHwFoldScreenManager
            public int setDisplayMode(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDisplayMode(mode);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.fsm.IHwFoldScreenManager
            public int getDisplayMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDisplayMode();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.fsm.IHwFoldScreenManager
            public int lockDisplayMode(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().lockDisplayMode(mode);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.fsm.IHwFoldScreenManager
            public int unlockDisplayMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unlockDisplayMode();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.fsm.IHwFoldScreenManager
            public void registerFoldDisplayMode(IFoldDisplayModeListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerFoldDisplayMode(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.fsm.IHwFoldScreenManager
            public void unregisterFoldDisplayMode(IFoldDisplayModeListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterFoldDisplayMode(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.fsm.IHwFoldScreenManager
            public int reqShowTipsToFsm(int reqTipsType, Bundle data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reqTipsType);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().reqShowTipsToFsm(reqTipsType, data);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.fsm.IHwFoldScreenManager
            public void registerFsmTipsRequestListener(IFoldFsmTipsRequestListener listener, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeInt(type);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerFsmTipsRequestListener(listener, type);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.fsm.IHwFoldScreenManager
            public void unregisterFsmTipsRequestListener(IFoldFsmTipsRequestListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterFsmTipsRequestListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwFoldScreenManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwFoldScreenManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
