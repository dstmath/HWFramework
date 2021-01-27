package android.hardware;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICameraServiceProxy extends IInterface {
    public static final int CAMERA_API_LEVEL_1 = 1;
    public static final int CAMERA_API_LEVEL_2 = 2;
    public static final int CAMERA_FACING_BACK = 0;
    public static final int CAMERA_FACING_EXTERNAL = 2;
    public static final int CAMERA_FACING_FRONT = 1;
    public static final int CAMERA_STATE_ACTIVE = 1;
    public static final int CAMERA_STATE_CLOSED = 3;
    public static final int CAMERA_STATE_IDLE = 2;
    public static final int CAMERA_STATE_OPEN = 0;

    boolean isLightStrapCaseOn() throws RemoteException;

    void notifyCameraState(String str, int i, int i2, String str2, int i3) throws RemoteException;

    void pingForUserUpdate() throws RemoteException;

    public static class Default implements ICameraServiceProxy {
        @Override // android.hardware.ICameraServiceProxy
        public void pingForUserUpdate() throws RemoteException {
        }

        @Override // android.hardware.ICameraServiceProxy
        public void notifyCameraState(String cameraId, int facing, int newCameraState, String clientName, int apiLevel) throws RemoteException {
        }

        @Override // android.hardware.ICameraServiceProxy
        public boolean isLightStrapCaseOn() throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICameraServiceProxy {
        private static final String DESCRIPTOR = "android.hardware.ICameraServiceProxy";
        static final int TRANSACTION_isLightStrapCaseOn = 3;
        static final int TRANSACTION_notifyCameraState = 2;
        static final int TRANSACTION_pingForUserUpdate = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICameraServiceProxy asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICameraServiceProxy)) {
                return new Proxy(obj);
            }
            return (ICameraServiceProxy) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "pingForUserUpdate";
            }
            if (transactionCode == 2) {
                return "notifyCameraState";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "isLightStrapCaseOn";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                pingForUserUpdate();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                notifyCameraState(data.readString(), data.readInt(), data.readInt(), data.readString(), data.readInt());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                boolean isLightStrapCaseOn = isLightStrapCaseOn();
                reply.writeNoException();
                reply.writeInt(isLightStrapCaseOn ? 1 : 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ICameraServiceProxy {
            public static ICameraServiceProxy sDefaultImpl;
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

            @Override // android.hardware.ICameraServiceProxy
            public void pingForUserUpdate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().pingForUserUpdate();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.ICameraServiceProxy
            public void notifyCameraState(String cameraId, int facing, int newCameraState, String clientName, int apiLevel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(cameraId);
                    _data.writeInt(facing);
                    _data.writeInt(newCameraState);
                    _data.writeString(clientName);
                    _data.writeInt(apiLevel);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyCameraState(cameraId, facing, newCameraState, clientName, apiLevel);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.ICameraServiceProxy
            public boolean isLightStrapCaseOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isLightStrapCaseOn();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
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

        public static boolean setDefaultImpl(ICameraServiceProxy impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ICameraServiceProxy getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
