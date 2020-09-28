package com.android.internal.inputmethod;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.InputChannel;
import com.android.internal.inputmethod.IMultiClientInputMethodSession;
import com.android.internal.view.IInputMethodSession;

public interface IMultiClientInputMethodPrivilegedOperations extends IInterface {
    void acceptClient(int i, IInputMethodSession iInputMethodSession, IMultiClientInputMethodSession iMultiClientInputMethodSession, InputChannel inputChannel) throws RemoteException;

    IBinder createInputMethodWindowToken(int i) throws RemoteException;

    void deleteInputMethodWindowToken(IBinder iBinder) throws RemoteException;

    boolean isUidAllowedOnDisplay(int i, int i2) throws RemoteException;

    void reportImeWindowTarget(int i, int i2, IBinder iBinder) throws RemoteException;

    void setActive(int i, boolean z) throws RemoteException;

    public static class Default implements IMultiClientInputMethodPrivilegedOperations {
        @Override // com.android.internal.inputmethod.IMultiClientInputMethodPrivilegedOperations
        public IBinder createInputMethodWindowToken(int displayId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.inputmethod.IMultiClientInputMethodPrivilegedOperations
        public void deleteInputMethodWindowToken(IBinder token) throws RemoteException {
        }

        @Override // com.android.internal.inputmethod.IMultiClientInputMethodPrivilegedOperations
        public void acceptClient(int clientId, IInputMethodSession session, IMultiClientInputMethodSession multiClientSession, InputChannel writeChannel) throws RemoteException {
        }

        @Override // com.android.internal.inputmethod.IMultiClientInputMethodPrivilegedOperations
        public void reportImeWindowTarget(int clientId, int targetWindowHandle, IBinder imeWindowToken) throws RemoteException {
        }

        @Override // com.android.internal.inputmethod.IMultiClientInputMethodPrivilegedOperations
        public boolean isUidAllowedOnDisplay(int displayId, int uid) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.inputmethod.IMultiClientInputMethodPrivilegedOperations
        public void setActive(int clientId, boolean active) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMultiClientInputMethodPrivilegedOperations {
        private static final String DESCRIPTOR = "com.android.internal.inputmethod.IMultiClientInputMethodPrivilegedOperations";
        static final int TRANSACTION_acceptClient = 3;
        static final int TRANSACTION_createInputMethodWindowToken = 1;
        static final int TRANSACTION_deleteInputMethodWindowToken = 2;
        static final int TRANSACTION_isUidAllowedOnDisplay = 5;
        static final int TRANSACTION_reportImeWindowTarget = 4;
        static final int TRANSACTION_setActive = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMultiClientInputMethodPrivilegedOperations asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMultiClientInputMethodPrivilegedOperations)) {
                return new Proxy(obj);
            }
            return (IMultiClientInputMethodPrivilegedOperations) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "createInputMethodWindowToken";
                case 2:
                    return "deleteInputMethodWindowToken";
                case 3:
                    return "acceptClient";
                case 4:
                    return "reportImeWindowTarget";
                case 5:
                    return "isUidAllowedOnDisplay";
                case 6:
                    return "setActive";
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
            InputChannel _arg3;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result = createInputMethodWindowToken(data.readInt());
                        reply.writeNoException();
                        reply.writeStrongBinder(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        deleteInputMethodWindowToken(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        IInputMethodSession _arg1 = IInputMethodSession.Stub.asInterface(data.readStrongBinder());
                        IMultiClientInputMethodSession _arg2 = IMultiClientInputMethodSession.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg3 = InputChannel.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        acceptClient(_arg0, _arg1, _arg2, _arg3);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        reportImeWindowTarget(data.readInt(), data.readInt(), data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isUidAllowedOnDisplay = isUidAllowedOnDisplay(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isUidAllowedOnDisplay ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        setActive(data.readInt(), data.readInt() != 0);
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
        public static class Proxy implements IMultiClientInputMethodPrivilegedOperations {
            public static IMultiClientInputMethodPrivilegedOperations sDefaultImpl;
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

            @Override // com.android.internal.inputmethod.IMultiClientInputMethodPrivilegedOperations
            public IBinder createInputMethodWindowToken(int displayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createInputMethodWindowToken(displayId);
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.inputmethod.IMultiClientInputMethodPrivilegedOperations
            public void deleteInputMethodWindowToken(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deleteInputMethodWindowToken(token);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.inputmethod.IMultiClientInputMethodPrivilegedOperations
            public void acceptClient(int clientId, IInputMethodSession session, IMultiClientInputMethodSession multiClientSession, InputChannel writeChannel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientId);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(session != null ? session.asBinder() : null);
                    if (multiClientSession != null) {
                        iBinder = multiClientSession.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (writeChannel != null) {
                        _data.writeInt(1);
                        writeChannel.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().acceptClient(clientId, session, multiClientSession, writeChannel);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.inputmethod.IMultiClientInputMethodPrivilegedOperations
            public void reportImeWindowTarget(int clientId, int targetWindowHandle, IBinder imeWindowToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientId);
                    _data.writeInt(targetWindowHandle);
                    _data.writeStrongBinder(imeWindowToken);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportImeWindowTarget(clientId, targetWindowHandle, imeWindowToken);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.inputmethod.IMultiClientInputMethodPrivilegedOperations
            public boolean isUidAllowedOnDisplay(int displayId, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isUidAllowedOnDisplay(displayId, uid);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.inputmethod.IMultiClientInputMethodPrivilegedOperations
            public void setActive(int clientId, boolean active) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientId);
                    _data.writeInt(active ? 1 : 0);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setActive(clientId, active);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMultiClientInputMethodPrivilegedOperations impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMultiClientInputMethodPrivilegedOperations getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
