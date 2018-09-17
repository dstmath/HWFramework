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

    public static abstract class Stub extends Binder implements ICameraServiceListener {
        private static final String DESCRIPTOR = "android.hardware.ICameraServiceListener";
        static final int TRANSACTION_onStatusChanged = 1;
        static final int TRANSACTION_onTorchStatusChanged = 2;

        private static class Proxy implements ICameraServiceListener {
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

            public void onStatusChanged(int status, int cameraId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeInt(cameraId);
                    this.mRemote.transact(Stub.TRANSACTION_onStatusChanged, _data, null, Stub.TRANSACTION_onStatusChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onTorchStatusChanged(int status, String cameraId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeString(cameraId);
                    this.mRemote.transact(Stub.TRANSACTION_onTorchStatusChanged, _data, null, Stub.TRANSACTION_onStatusChanged);
                } finally {
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_onStatusChanged /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    onStatusChanged(data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_onTorchStatusChanged /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onTorchStatusChanged(data.readInt(), data.readString());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onStatusChanged(int i, int i2) throws RemoteException;

    void onTorchStatusChanged(int i, String str) throws RemoteException;
}
