package com.huawei.servicehost;

import android.hardware.camera2.impl.CameraMetadataNative;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IImageProcessService extends IInterface {

    public static abstract class Stub extends Binder implements IImageProcessService {
        private static final String DESCRIPTOR = "com.huawei.servicehost.IImageProcessService";
        static final int TRANSACTION_createIPSession = 1;
        static final int TRANSACTION_dualCameraMode = 5;
        static final int TRANSACTION_getGlobalSession = 2;
        static final int TRANSACTION_getSupportedMode = 3;
        static final int TRANSACTION_queryCapability = 4;

        private static class Proxy implements IImageProcessService {
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

            public IImageProcessSession createIPSession(String type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(type);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    IImageProcessSession _result = com.huawei.servicehost.IImageProcessSession.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IGlobalSession getGlobalSession() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    IGlobalSession _result = com.huawei.servicehost.IGlobalSession.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSupportedMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void queryCapability(String cameraId, CameraMetadataNative nativeMeta) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(cameraId);
                    if (nativeMeta != null) {
                        _data.writeInt(1);
                        nativeMeta.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        nativeMeta.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int dualCameraMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IImageProcessService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IImageProcessService)) {
                return new Proxy(obj);
            }
            return (IImageProcessService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IBinder iBinder = null;
            int _result;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    IImageProcessSession _result2 = createIPSession(data.readString());
                    reply.writeNoException();
                    if (_result2 != null) {
                        iBinder = _result2.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    IGlobalSession _result3 = getGlobalSession();
                    reply.writeNoException();
                    if (_result3 != null) {
                        iBinder = _result3.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getSupportedMode();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 4:
                    CameraMetadataNative _arg1;
                    data.enforceInterface(DESCRIPTOR);
                    String _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (CameraMetadataNative) CameraMetadataNative.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    queryCapability(_arg0, _arg1);
                    reply.writeNoException();
                    if (_arg1 != null) {
                        reply.writeInt(1);
                        _arg1.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _result = dualCameraMode();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    IImageProcessSession createIPSession(String str) throws RemoteException;

    int dualCameraMode() throws RemoteException;

    IGlobalSession getGlobalSession() throws RemoteException;

    int getSupportedMode() throws RemoteException;

    void queryCapability(String str, CameraMetadataNative cameraMetadataNative) throws RemoteException;
}
