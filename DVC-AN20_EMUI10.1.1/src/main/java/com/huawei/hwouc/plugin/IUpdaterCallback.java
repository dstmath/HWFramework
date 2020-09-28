package com.huawei.hwouc.plugin;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IUpdaterCallback extends IInterface {
    void onProgress(int i, int i2) throws RemoteException;

    void onStatus(int i, int i2, String str) throws RemoteException;

    public static class Default implements IUpdaterCallback {
        @Override // com.huawei.hwouc.plugin.IUpdaterCallback
        public void onProgress(int taskId, int percent) throws RemoteException {
        }

        @Override // com.huawei.hwouc.plugin.IUpdaterCallback
        public void onStatus(int taskId, int status, String message) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IUpdaterCallback {
        private static final String DESCRIPTOR = "com.huawei.hwouc.plugin.IUpdaterCallback";
        static final int TRANSACTION_onProgress = 1;
        static final int TRANSACTION_onStatus = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IUpdaterCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IUpdaterCallback)) {
                return new Proxy(obj);
            }
            return (IUpdaterCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onProgress(data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onStatus(data.readInt(), data.readInt(), data.readString());
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
        public static class Proxy implements IUpdaterCallback {
            public static IUpdaterCallback sDefaultImpl;
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

            @Override // com.huawei.hwouc.plugin.IUpdaterCallback
            public void onProgress(int taskId, int percent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeInt(percent);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onProgress(taskId, percent);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwouc.plugin.IUpdaterCallback
            public void onStatus(int taskId, int status, String message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeInt(status);
                    _data.writeString(message);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onStatus(taskId, status, message);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IUpdaterCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IUpdaterCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
