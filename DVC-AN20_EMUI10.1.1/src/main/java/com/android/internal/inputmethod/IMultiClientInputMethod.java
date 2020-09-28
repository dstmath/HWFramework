package com.android.internal.inputmethod;

import android.content.ContentResolver;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.internal.inputmethod.IMultiClientInputMethodPrivilegedOperations;

public interface IMultiClientInputMethod extends IInterface {
    void addClient(int i, int i2, int i3, int i4) throws RemoteException;

    void initialize(IMultiClientInputMethodPrivilegedOperations iMultiClientInputMethodPrivilegedOperations) throws RemoteException;

    void removeClient(int i) throws RemoteException;

    public static class Default implements IMultiClientInputMethod {
        @Override // com.android.internal.inputmethod.IMultiClientInputMethod
        public void initialize(IMultiClientInputMethodPrivilegedOperations privOps) throws RemoteException {
        }

        @Override // com.android.internal.inputmethod.IMultiClientInputMethod
        public void addClient(int clientId, int uid, int pid, int selfReportedDisplayId) throws RemoteException {
        }

        @Override // com.android.internal.inputmethod.IMultiClientInputMethod
        public void removeClient(int clientId) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMultiClientInputMethod {
        private static final String DESCRIPTOR = "com.android.internal.inputmethod.IMultiClientInputMethod";
        static final int TRANSACTION_addClient = 2;
        static final int TRANSACTION_initialize = 1;
        static final int TRANSACTION_removeClient = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMultiClientInputMethod asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMultiClientInputMethod)) {
                return new Proxy(obj);
            }
            return (IMultiClientInputMethod) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return ContentResolver.SYNC_EXTRAS_INITIALIZE;
            }
            if (transactionCode == 2) {
                return "addClient";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "removeClient";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                initialize(IMultiClientInputMethodPrivilegedOperations.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                addClient(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                removeClient(data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IMultiClientInputMethod {
            public static IMultiClientInputMethod sDefaultImpl;
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

            @Override // com.android.internal.inputmethod.IMultiClientInputMethod
            public void initialize(IMultiClientInputMethodPrivilegedOperations privOps) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(privOps != null ? privOps.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().initialize(privOps);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.inputmethod.IMultiClientInputMethod
            public void addClient(int clientId, int uid, int pid, int selfReportedDisplayId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientId);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeInt(selfReportedDisplayId);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().addClient(clientId, uid, pid, selfReportedDisplayId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.inputmethod.IMultiClientInputMethod
            public void removeClient(int clientId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientId);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().removeClient(clientId);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMultiClientInputMethod impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMultiClientInputMethod getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
