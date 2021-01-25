package com.huawei.servicehost.normal;

import android.hardware.camera2.impl.CameraMetadataNative;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IIPEvent4CaptureInfo extends IInterface {
    String getModeName() throws RemoteException;

    int getRequestCount() throws RemoteException;

    CameraMetadataNative getRequestMetadata(int i) throws RemoteException;

    public static class Default implements IIPEvent4CaptureInfo {
        @Override // com.huawei.servicehost.normal.IIPEvent4CaptureInfo
        public String getModeName() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.normal.IIPEvent4CaptureInfo
        public int getRequestCount() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.servicehost.normal.IIPEvent4CaptureInfo
        public CameraMetadataNative getRequestMetadata(int index) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IIPEvent4CaptureInfo {
        private static final String DESCRIPTOR = "com.huawei.servicehost.normal.IIPEvent4CaptureInfo";
        static final int TRANSACTION_getModeName = 1;
        static final int TRANSACTION_getRequestCount = 2;
        static final int TRANSACTION_getRequestMetadata = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIPEvent4CaptureInfo asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIPEvent4CaptureInfo)) {
                return new Proxy(obj);
            }
            return (IIPEvent4CaptureInfo) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                String _result = getModeName();
                reply.writeNoException();
                reply.writeString(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _result2 = getRequestCount();
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                CameraMetadataNative _result3 = getRequestMetadata(data.readInt());
                reply.writeNoException();
                if (_result3 != null) {
                    reply.writeInt(1);
                    _result3.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IIPEvent4CaptureInfo {
            public static IIPEvent4CaptureInfo sDefaultImpl;
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

            @Override // com.huawei.servicehost.normal.IIPEvent4CaptureInfo
            public String getModeName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getModeName();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.normal.IIPEvent4CaptureInfo
            public int getRequestCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRequestCount();
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

            @Override // com.huawei.servicehost.normal.IIPEvent4CaptureInfo
            public CameraMetadataNative getRequestMetadata(int index) throws RemoteException {
                CameraMetadataNative _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(index);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRequestMetadata(index);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (CameraMetadataNative) CameraMetadataNative.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IIPEvent4CaptureInfo impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IIPEvent4CaptureInfo getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
