package android.net;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INetdEventCallback extends IInterface {

    public static abstract class Stub extends Binder implements INetdEventCallback {
        private static final String DESCRIPTOR = "android.net.INetdEventCallback";
        static final int TRANSACTION_onConnectEvent = 2;
        static final int TRANSACTION_onDnsEvent = 1;

        private static class Proxy implements INetdEventCallback {
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

            public void onDnsEvent(String hostname, String[] ipAddresses, int ipAddressesCount, long timestamp, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(hostname);
                    _data.writeStringArray(ipAddresses);
                    _data.writeInt(ipAddressesCount);
                    _data.writeLong(timestamp);
                    _data.writeInt(uid);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onConnectEvent(String ipAddr, int port, long timestamp, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ipAddr);
                    _data.writeInt(port);
                    _data.writeLong(timestamp);
                    _data.writeInt(uid);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INetdEventCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INetdEventCallback)) {
                return new Proxy(obj);
            }
            return (INetdEventCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onDnsEvent(data.readString(), data.createStringArray(), data.readInt(), data.readLong(), data.readInt());
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    onConnectEvent(data.readString(), data.readInt(), data.readLong(), data.readInt());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onConnectEvent(String str, int i, long j, int i2) throws RemoteException;

    void onDnsEvent(String str, String[] strArr, int i, long j, int i2) throws RemoteException;
}
