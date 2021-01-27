package com.huawei.servicehost;

import android.hardware.camera2.impl.CameraMetadataNative;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.servicehost.IGlobalSession;
import com.huawei.servicehost.IImageProcessSession;

public interface IImageProcessService extends IInterface {
    IImageProcessSession createIPSession(String str) throws RemoteException;

    int dualCameraMode() throws RemoteException;

    IGlobalSession getGlobalSession() throws RemoteException;

    int getSupportedMode() throws RemoteException;

    void queryCapability(String str, CameraMetadataNative cameraMetadataNative) throws RemoteException;

    public static class Default implements IImageProcessService {
        @Override // com.huawei.servicehost.IImageProcessService
        public IImageProcessSession createIPSession(String type) throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.IImageProcessService
        public IGlobalSession getGlobalSession() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.IImageProcessService
        public int getSupportedMode() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.servicehost.IImageProcessService
        public void queryCapability(String cameraId, CameraMetadataNative nativeMeta) throws RemoteException {
        }

        @Override // com.huawei.servicehost.IImageProcessService
        public int dualCameraMode() throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IImageProcessService {
        private static final String DESCRIPTOR = "com.huawei.servicehost.IImageProcessService";
        static final int TRANSACTION_createIPSession = 1;
        static final int TRANSACTION_dualCameraMode = 5;
        static final int TRANSACTION_getGlobalSession = 2;
        static final int TRANSACTION_getSupportedMode = 3;
        static final int TRANSACTION_queryCapability = 4;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            CameraMetadataNative _arg1;
            IBinder iBinder = null;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                IImageProcessSession _result = createIPSession(data.readString());
                reply.writeNoException();
                if (_result != null) {
                    iBinder = _result.asBinder();
                }
                reply.writeStrongBinder(iBinder);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                IGlobalSession _result2 = getGlobalSession();
                reply.writeNoException();
                if (_result2 != null) {
                    iBinder = _result2.asBinder();
                }
                reply.writeStrongBinder(iBinder);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _result3 = getSupportedMode();
                reply.writeNoException();
                reply.writeInt(_result3);
                return true;
            } else if (code == 4) {
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
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                int _result4 = dualCameraMode();
                reply.writeNoException();
                reply.writeInt(_result4);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IImageProcessService {
            public static IImageProcessService sDefaultImpl;
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

            @Override // com.huawei.servicehost.IImageProcessService
            public IImageProcessSession createIPSession(String type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(type);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createIPSession(type);
                    }
                    _reply.readException();
                    IImageProcessSession _result = IImageProcessSession.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.IImageProcessService
            public IGlobalSession getGlobalSession() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getGlobalSession();
                    }
                    _reply.readException();
                    IGlobalSession _result = IGlobalSession.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.IImageProcessService
            public int getSupportedMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSupportedMode();
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

            @Override // com.huawei.servicehost.IImageProcessService
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
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        if (_reply.readInt() != 0) {
                            nativeMeta.readFromParcel(_reply);
                        }
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().queryCapability(cameraId, nativeMeta);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.IImageProcessService
            public int dualCameraMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().dualCameraMode();
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
        }

        public static boolean setDefaultImpl(IImageProcessService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IImageProcessService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
