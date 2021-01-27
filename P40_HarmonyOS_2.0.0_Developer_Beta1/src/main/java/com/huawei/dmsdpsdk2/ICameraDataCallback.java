package com.huawei.dmsdpsdk2;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SharedMemory;
import java.util.Map;

public interface ICameraDataCallback extends IInterface {
    void onVirCameraBufferDone(SharedMemory sharedMemory, Map map) throws RemoteException;

    public static class Default implements ICameraDataCallback {
        @Override // com.huawei.dmsdpsdk2.ICameraDataCallback
        public void onVirCameraBufferDone(SharedMemory shm, Map params) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICameraDataCallback {
        private static final String DESCRIPTOR = "com.huawei.dmsdpsdk.ICameraDataCallback";
        static final int TRANSACTION_onVirCameraBufferDone = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICameraDataCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICameraDataCallback)) {
                return new Proxy(obj);
            }
            return (ICameraDataCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            SharedMemory _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = (SharedMemory) SharedMemory.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onVirCameraBufferDone(_arg0, data.readHashMap(getClass().getClassLoader()));
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
        public static class Proxy implements ICameraDataCallback {
            public static ICameraDataCallback sDefaultImpl;
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

            @Override // com.huawei.dmsdpsdk2.ICameraDataCallback
            public void onVirCameraBufferDone(SharedMemory shm, Map params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (shm != null) {
                        _data.writeInt(1);
                        shm.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeMap(params);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onVirCameraBufferDone(shm, params);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ICameraDataCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ICameraDataCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
