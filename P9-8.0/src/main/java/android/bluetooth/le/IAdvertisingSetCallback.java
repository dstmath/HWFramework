package android.bluetooth.le;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAdvertisingSetCallback extends IInterface {

    public static abstract class Stub extends Binder implements IAdvertisingSetCallback {
        private static final String DESCRIPTOR = "android.bluetooth.le.IAdvertisingSetCallback";
        static final int TRANSACTION_onAdvertisingDataSet = 5;
        static final int TRANSACTION_onAdvertisingEnabled = 4;
        static final int TRANSACTION_onAdvertisingParametersUpdated = 7;
        static final int TRANSACTION_onAdvertisingSetStarted = 1;
        static final int TRANSACTION_onAdvertisingSetStopped = 3;
        static final int TRANSACTION_onOwnAddressRead = 2;
        static final int TRANSACTION_onPeriodicAdvertisingDataSet = 9;
        static final int TRANSACTION_onPeriodicAdvertisingEnabled = 10;
        static final int TRANSACTION_onPeriodicAdvertisingParametersUpdated = 8;
        static final int TRANSACTION_onScanResponseDataSet = 6;

        private static class Proxy implements IAdvertisingSetCallback {
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

            public void onAdvertisingSetStarted(int advertiserId, int tx_power, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(advertiserId);
                    _data.writeInt(tx_power);
                    _data.writeInt(status);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onOwnAddressRead(int advertiserId, int addressType, String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(advertiserId);
                    _data.writeInt(addressType);
                    _data.writeString(address);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onAdvertisingSetStopped(int advertiserId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(advertiserId);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onAdvertisingEnabled(int advertiserId, boolean enable, int status) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(advertiserId);
                    if (!enable) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeInt(status);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onAdvertisingDataSet(int advertiserId, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(advertiserId);
                    _data.writeInt(status);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onScanResponseDataSet(int advertiserId, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(advertiserId);
                    _data.writeInt(status);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onAdvertisingParametersUpdated(int advertiserId, int tx_power, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(advertiserId);
                    _data.writeInt(tx_power);
                    _data.writeInt(status);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPeriodicAdvertisingParametersUpdated(int advertiserId, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(advertiserId);
                    _data.writeInt(status);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPeriodicAdvertisingDataSet(int advertiserId, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(advertiserId);
                    _data.writeInt(status);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPeriodicAdvertisingEnabled(int advertiserId, boolean enable, int status) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(advertiserId);
                    if (!enable) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeInt(status);
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAdvertisingSetCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAdvertisingSetCallback)) {
                return new Proxy(obj);
            }
            return (IAdvertisingSetCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onAdvertisingSetStarted(data.readInt(), data.readInt(), data.readInt());
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    onOwnAddressRead(data.readInt(), data.readInt(), data.readString());
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    onAdvertisingSetStopped(data.readInt());
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    onAdvertisingEnabled(data.readInt(), data.readInt() != 0, data.readInt());
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    onAdvertisingDataSet(data.readInt(), data.readInt());
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    onScanResponseDataSet(data.readInt(), data.readInt());
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    onAdvertisingParametersUpdated(data.readInt(), data.readInt(), data.readInt());
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    onPeriodicAdvertisingParametersUpdated(data.readInt(), data.readInt());
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    onPeriodicAdvertisingDataSet(data.readInt(), data.readInt());
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    onPeriodicAdvertisingEnabled(data.readInt(), data.readInt() != 0, data.readInt());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onAdvertisingDataSet(int i, int i2) throws RemoteException;

    void onAdvertisingEnabled(int i, boolean z, int i2) throws RemoteException;

    void onAdvertisingParametersUpdated(int i, int i2, int i3) throws RemoteException;

    void onAdvertisingSetStarted(int i, int i2, int i3) throws RemoteException;

    void onAdvertisingSetStopped(int i) throws RemoteException;

    void onOwnAddressRead(int i, int i2, String str) throws RemoteException;

    void onPeriodicAdvertisingDataSet(int i, int i2) throws RemoteException;

    void onPeriodicAdvertisingEnabled(int i, boolean z, int i2) throws RemoteException;

    void onPeriodicAdvertisingParametersUpdated(int i, int i2) throws RemoteException;

    void onScanResponseDataSet(int i, int i2) throws RemoteException;
}
