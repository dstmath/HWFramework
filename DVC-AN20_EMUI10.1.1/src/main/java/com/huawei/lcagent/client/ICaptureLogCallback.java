package com.huawei.lcagent.client;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICaptureLogCallback extends IInterface {
    void onComplete(String str, String str2, int i) throws RemoteException;

    void onProgress(int i) throws RemoteException;

    public static class Default implements ICaptureLogCallback {
        @Override // com.huawei.lcagent.client.ICaptureLogCallback
        public void onProgress(int progress) throws RemoteException {
        }

        @Override // com.huawei.lcagent.client.ICaptureLogCallback
        public void onComplete(String pathName, String fileName, int fileSize) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICaptureLogCallback {
        private static final String DESCRIPTOR = "com.huawei.lcagent.client.ICaptureLogCallback";
        static final int TRANSACTION_onComplete = 2;
        static final int TRANSACTION_onProgress = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICaptureLogCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICaptureLogCallback)) {
                return new Proxy(obj);
            }
            return (ICaptureLogCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onProgress(data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onComplete(data.readString(), data.readString(), data.readInt());
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
        public static class Proxy implements ICaptureLogCallback {
            public static ICaptureLogCallback sDefaultImpl;
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

            @Override // com.huawei.lcagent.client.ICaptureLogCallback
            public void onProgress(int progress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(progress);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onProgress(progress);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.lcagent.client.ICaptureLogCallback
            public void onComplete(String pathName, String fileName, int fileSize) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pathName);
                    _data.writeString(fileName);
                    _data.writeInt(fileSize);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onComplete(pathName, fileName, fileSize);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ICaptureLogCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ICaptureLogCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
