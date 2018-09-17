package android.net.metrics;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INetdEventListener extends IInterface {
    public static final int DNS_REPORTED_IP_ADDRESSES_LIMIT = 10;
    public static final int EVENT_GETADDRINFO = 1;
    public static final int EVENT_GETHOSTBYNAME = 2;
    public static final int REPORTING_LEVEL_FULL = 2;
    public static final int REPORTING_LEVEL_METRICS = 1;
    public static final int REPORTING_LEVEL_NONE = 0;

    public static abstract class Stub extends Binder implements INetdEventListener {
        private static final String DESCRIPTOR = "android.net.metrics.INetdEventListener";
        static final int TRANSACTION_onConnectEvent = 2;
        static final int TRANSACTION_onDnsEvent = 1;

        private static class Proxy implements INetdEventListener {
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

            public void onDnsEvent(int netId, int eventType, int returnCode, int latencyMs, String hostname, String[] ipAddresses, int ipAddressesCount, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeInt(eventType);
                    _data.writeInt(returnCode);
                    _data.writeInt(latencyMs);
                    _data.writeString(hostname);
                    _data.writeStringArray(ipAddresses);
                    _data.writeInt(ipAddressesCount);
                    _data.writeInt(uid);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onConnectEvent(int netId, int error, int latencyMs, String ipAddr, int port, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeInt(error);
                    _data.writeInt(latencyMs);
                    _data.writeString(ipAddr);
                    _data.writeInt(port);
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

        public static INetdEventListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INetdEventListener)) {
                return new Proxy(obj);
            }
            return (INetdEventListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onDnsEvent(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readString(), data.createStringArray(), data.readInt(), data.readInt());
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    int _arg1 = data.readInt();
                    onConnectEvent(_arg0, _arg1, data.readInt(), data.readString(), data.readInt(), data.readInt());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onConnectEvent(int i, int i2, int i3, String str, int i4, int i5) throws RemoteException;

    void onDnsEvent(int i, int i2, int i3, int i4, String str, String[] strArr, int i5, int i6) throws RemoteException;
}
