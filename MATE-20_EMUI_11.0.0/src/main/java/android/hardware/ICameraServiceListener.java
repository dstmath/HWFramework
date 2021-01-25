package android.hardware;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICameraServiceListener extends IInterface {
    public static final int STATUS_ENUMERATING = 2;
    public static final int STATUS_NOT_AVAILABLE = -2;
    public static final int STATUS_NOT_PRESENT = 0;
    public static final int STATUS_PRESENT = 1;
    public static final int STATUS_UNKNOWN = -1;
    public static final int TORCH_STATUS_AVAILABLE_OFF = 1;
    public static final int TORCH_STATUS_AVAILABLE_ON = 2;
    public static final int TORCH_STATUS_NOT_AVAILABLE = 0;
    public static final int TORCH_STATUS_UNKNOWN = -1;

    void onCameraAccessPrioritiesChanged() throws RemoteException;

    void onStatusChanged(int i, String str) throws RemoteException;

    void onTorchStatusChanged(int i, String str) throws RemoteException;

    public static class Default implements ICameraServiceListener {
        @Override // android.hardware.ICameraServiceListener
        public void onStatusChanged(int status, String cameraId) throws RemoteException {
        }

        @Override // android.hardware.ICameraServiceListener
        public void onTorchStatusChanged(int status, String cameraId) throws RemoteException {
        }

        @Override // android.hardware.ICameraServiceListener
        public void onCameraAccessPrioritiesChanged() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICameraServiceListener {
        private static final String DESCRIPTOR = "android.hardware.ICameraServiceListener";
        static final int TRANSACTION_onCameraAccessPrioritiesChanged = 3;
        static final int TRANSACTION_onStatusChanged = 1;
        static final int TRANSACTION_onTorchStatusChanged = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICameraServiceListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICameraServiceListener)) {
                return new Proxy(obj);
            }
            return (ICameraServiceListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onStatusChanged";
            }
            if (transactionCode == 2) {
                return "onTorchStatusChanged";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "onCameraAccessPrioritiesChanged";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onStatusChanged(data.readInt(), data.readString());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onTorchStatusChanged(data.readInt(), data.readString());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onCameraAccessPrioritiesChanged();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ICameraServiceListener {
            public static ICameraServiceListener sDefaultImpl;
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

            @Override // android.hardware.ICameraServiceListener
            public void onStatusChanged(int status, String cameraId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeString(cameraId);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onStatusChanged(status, cameraId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.ICameraServiceListener
            public void onTorchStatusChanged(int status, String cameraId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeString(cameraId);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onTorchStatusChanged(status, cameraId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.ICameraServiceListener
            public void onCameraAccessPrioritiesChanged() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onCameraAccessPrioritiesChanged();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ICameraServiceListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ICameraServiceListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
