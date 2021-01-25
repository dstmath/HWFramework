package com.huawei.servicehost.pp;

import android.hardware.camera2.impl.CameraMetadataNative;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.servicehost.ImageWrap;

public interface IIPEvent4Thumbnail extends IInterface {
    String getFilePath() throws RemoteException;

    ImageWrap getImage() throws RemoteException;

    CameraMetadataNative getMetadata() throws RemoteException;

    public static class Default implements IIPEvent4Thumbnail {
        @Override // com.huawei.servicehost.pp.IIPEvent4Thumbnail
        public CameraMetadataNative getMetadata() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.pp.IIPEvent4Thumbnail
        public ImageWrap getImage() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.pp.IIPEvent4Thumbnail
        public String getFilePath() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IIPEvent4Thumbnail {
        private static final String DESCRIPTOR = "com.huawei.servicehost.pp.IIPEvent4Thumbnail";
        static final int TRANSACTION_getFilePath = 3;
        static final int TRANSACTION_getImage = 2;
        static final int TRANSACTION_getMetadata = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIPEvent4Thumbnail asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIPEvent4Thumbnail)) {
                return new Proxy(obj);
            }
            return (IIPEvent4Thumbnail) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                CameraMetadataNative _result = getMetadata();
                reply.writeNoException();
                if (_result != null) {
                    reply.writeInt(1);
                    _result.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                ImageWrap _result2 = getImage();
                reply.writeNoException();
                if (_result2 != null) {
                    reply.writeInt(1);
                    _result2.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                String _result3 = getFilePath();
                reply.writeNoException();
                reply.writeString(_result3);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IIPEvent4Thumbnail {
            public static IIPEvent4Thumbnail sDefaultImpl;
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

            @Override // com.huawei.servicehost.pp.IIPEvent4Thumbnail
            public CameraMetadataNative getMetadata() throws RemoteException {
                CameraMetadataNative _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMetadata();
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

            @Override // com.huawei.servicehost.pp.IIPEvent4Thumbnail
            public ImageWrap getImage() throws RemoteException {
                ImageWrap _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getImage();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ImageWrap.CREATOR.createFromParcel(_reply);
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

            @Override // com.huawei.servicehost.pp.IIPEvent4Thumbnail
            public String getFilePath() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFilePath();
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
        }

        public static boolean setDefaultImpl(IIPEvent4Thumbnail impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IIPEvent4Thumbnail getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
