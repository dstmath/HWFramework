package com.nxp.nfc;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISecureElementCallback extends IInterface {
    void onServiceConnected() throws RemoteException;

    void onServiceDisconnected() throws RemoteException;

    public static class Default implements ISecureElementCallback {
        @Override // com.nxp.nfc.ISecureElementCallback
        public void onServiceConnected() throws RemoteException {
        }

        @Override // com.nxp.nfc.ISecureElementCallback
        public void onServiceDisconnected() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISecureElementCallback {
        private static final String DESCRIPTOR = "com.nxp.nfc.ISecureElementCallback";
        static final int TRANSACTION_onServiceConnected = 1;
        static final int TRANSACTION_onServiceDisconnected = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISecureElementCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISecureElementCallback)) {
                return new Proxy(obj);
            }
            return (ISecureElementCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onServiceConnected();
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onServiceDisconnected();
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ISecureElementCallback {
            public static ISecureElementCallback sDefaultImpl;
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

            @Override // com.nxp.nfc.ISecureElementCallback
            public void onServiceConnected() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onServiceConnected();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.ISecureElementCallback
            public void onServiceDisconnected() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onServiceDisconnected();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISecureElementCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISecureElementCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
