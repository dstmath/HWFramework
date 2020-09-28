package com.huawei.servicehost.normal;

import android.hardware.camera2.impl.CameraMetadataNative;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IIPRequest4Metadata extends IInterface {
    void setMetadata(CameraMetadataNative cameraMetadataNative) throws RemoteException;

    public static class Default implements IIPRequest4Metadata {
        @Override // com.huawei.servicehost.normal.IIPRequest4Metadata
        public void setMetadata(CameraMetadataNative val) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IIPRequest4Metadata {
        private static final String DESCRIPTOR = "com.huawei.servicehost.normal.IIPRequest4Metadata";
        static final int TRANSACTION_setMetadata = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIPRequest4Metadata asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIPRequest4Metadata)) {
                return new Proxy(obj);
            }
            return (IIPRequest4Metadata) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            CameraMetadataNative _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = (CameraMetadataNative) CameraMetadataNative.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                setMetadata(_arg0);
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
        public static class Proxy implements IIPRequest4Metadata {
            public static IIPRequest4Metadata sDefaultImpl;
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

            @Override // com.huawei.servicehost.normal.IIPRequest4Metadata
            public void setMetadata(CameraMetadataNative val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (val != null) {
                        _data.writeInt(1);
                        val.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setMetadata(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IIPRequest4Metadata impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IIPRequest4Metadata getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
