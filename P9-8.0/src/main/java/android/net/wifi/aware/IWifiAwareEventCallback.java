package android.net.wifi.aware;

import android.net.wifi.RttManager.ParcelableRttResults;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IWifiAwareEventCallback extends IInterface {

    public static abstract class Stub extends Binder implements IWifiAwareEventCallback {
        private static final String DESCRIPTOR = "android.net.wifi.aware.IWifiAwareEventCallback";
        static final int TRANSACTION_onConnectFail = 2;
        static final int TRANSACTION_onConnectSuccess = 1;
        static final int TRANSACTION_onIdentityChanged = 3;
        static final int TRANSACTION_onRangingAborted = 6;
        static final int TRANSACTION_onRangingFailure = 5;
        static final int TRANSACTION_onRangingSuccess = 4;

        private static class Proxy implements IWifiAwareEventCallback {
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

            public void onConnectSuccess(int clientId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientId);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onConnectFail(int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reason);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onIdentityChanged(byte[] mac) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(mac);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onRangingSuccess(int rangingId, ParcelableRttResults results) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rangingId);
                    if (results != null) {
                        _data.writeInt(1);
                        results.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onRangingFailure(int rangingId, int reason, String description) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rangingId);
                    _data.writeInt(reason);
                    _data.writeString(description);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onRangingAborted(int rangingId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rangingId);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWifiAwareEventCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWifiAwareEventCallback)) {
                return new Proxy(obj);
            }
            return (IWifiAwareEventCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onConnectSuccess(data.readInt());
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    onConnectFail(data.readInt());
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    onIdentityChanged(data.createByteArray());
                    return true;
                case 4:
                    ParcelableRttResults _arg1;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg1 = (ParcelableRttResults) ParcelableRttResults.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    onRangingSuccess(_arg0, _arg1);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    onRangingFailure(data.readInt(), data.readInt(), data.readString());
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    onRangingAborted(data.readInt());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onConnectFail(int i) throws RemoteException;

    void onConnectSuccess(int i) throws RemoteException;

    void onIdentityChanged(byte[] bArr) throws RemoteException;

    void onRangingAborted(int i) throws RemoteException;

    void onRangingFailure(int i, int i2, String str) throws RemoteException;

    void onRangingSuccess(int i, ParcelableRttResults parcelableRttResults) throws RemoteException;
}
