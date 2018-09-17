package android.net.wifi.aware;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IWifiAwareDiscoverySessionCallback extends IInterface {

    public static abstract class Stub extends Binder implements IWifiAwareDiscoverySessionCallback {
        private static final String DESCRIPTOR = "android.net.wifi.aware.IWifiAwareDiscoverySessionCallback";
        static final int TRANSACTION_onMatch = 5;
        static final int TRANSACTION_onMessageReceived = 8;
        static final int TRANSACTION_onMessageSendFail = 7;
        static final int TRANSACTION_onMessageSendSuccess = 6;
        static final int TRANSACTION_onSessionConfigFail = 3;
        static final int TRANSACTION_onSessionConfigSuccess = 2;
        static final int TRANSACTION_onSessionStarted = 1;
        static final int TRANSACTION_onSessionTerminated = 4;

        private static class Proxy implements IWifiAwareDiscoverySessionCallback {
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

            public void onSessionStarted(int discoverySessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(discoverySessionId);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onSessionConfigSuccess() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onSessionConfigFail(int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reason);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onSessionTerminated(int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reason);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onMatch(int peerId, byte[] serviceSpecificInfo, byte[] matchFilter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(peerId);
                    _data.writeByteArray(serviceSpecificInfo);
                    _data.writeByteArray(matchFilter);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onMessageSendSuccess(int messageId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onMessageSendFail(int messageId, int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(messageId);
                    _data.writeInt(reason);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onMessageReceived(int peerId, byte[] message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(peerId);
                    _data.writeByteArray(message);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWifiAwareDiscoverySessionCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWifiAwareDiscoverySessionCallback)) {
                return new Proxy(obj);
            }
            return (IWifiAwareDiscoverySessionCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onSessionStarted(data.readInt());
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    onSessionConfigSuccess();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    onSessionConfigFail(data.readInt());
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    onSessionTerminated(data.readInt());
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    onMatch(data.readInt(), data.createByteArray(), data.createByteArray());
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    onMessageSendSuccess(data.readInt());
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    onMessageSendFail(data.readInt(), data.readInt());
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    onMessageReceived(data.readInt(), data.createByteArray());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onMatch(int i, byte[] bArr, byte[] bArr2) throws RemoteException;

    void onMessageReceived(int i, byte[] bArr) throws RemoteException;

    void onMessageSendFail(int i, int i2) throws RemoteException;

    void onMessageSendSuccess(int i) throws RemoteException;

    void onSessionConfigFail(int i) throws RemoteException;

    void onSessionConfigSuccess() throws RemoteException;

    void onSessionStarted(int i) throws RemoteException;

    void onSessionTerminated(int i) throws RemoteException;
}
