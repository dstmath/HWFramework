package huawei.android.hardware.usb;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwUsbManagerEx extends IInterface {

    public static abstract class Stub extends Binder implements IHwUsbManagerEx {
        private static final String DESCRIPTOR = "huawei.android.hardware.usb.IHwUsbManagerEx";
        static final int TRANSACTION_allowUsbHDB = 1;
        static final int TRANSACTION_clearUsbHDBKeys = 3;
        static final int TRANSACTION_denyUsbHDB = 2;

        private static class Proxy implements IHwUsbManagerEx {
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

            public void allowUsbHDB(boolean alwaysAllow, String publicKey) throws RemoteException {
                int i = Stub.TRANSACTION_allowUsbHDB;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!alwaysAllow) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeString(publicKey);
                    this.mRemote.transact(Stub.TRANSACTION_allowUsbHDB, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void denyUsbHDB() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_denyUsbHDB, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearUsbHDBKeys() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_clearUsbHDBKeys, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwUsbManagerEx asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwUsbManagerEx)) {
                return new Proxy(obj);
            }
            return (IHwUsbManagerEx) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_allowUsbHDB /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    allowUsbHDB(data.readInt() != 0, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_denyUsbHDB /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    denyUsbHDB();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearUsbHDBKeys /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearUsbHDBKeys();
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void allowUsbHDB(boolean z, String str) throws RemoteException;

    void clearUsbHDBKeys() throws RemoteException;

    void denyUsbHDB() throws RemoteException;
}
