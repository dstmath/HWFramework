package android.net.metrics;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IDnsEventListener extends IInterface {
    public static final int EVENT_GETADDRINFO = 1;
    public static final int EVENT_GETHOSTBYNAME = 2;

    public static abstract class Stub extends Binder implements IDnsEventListener {
        private static final String DESCRIPTOR = "android.net.metrics.IDnsEventListener";
        static final int TRANSACTION_onDnsEvent = 1;

        private static class Proxy implements IDnsEventListener {
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

            public void onDnsEvent(int netId, int eventType, int returnCode, int latencyMs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeInt(eventType);
                    _data.writeInt(returnCode);
                    _data.writeInt(latencyMs);
                    this.mRemote.transact(Stub.TRANSACTION_onDnsEvent, _data, null, Stub.TRANSACTION_onDnsEvent);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDnsEventListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDnsEventListener)) {
                return new Proxy(obj);
            }
            return (IDnsEventListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_onDnsEvent /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    onDnsEvent(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onDnsEvent(int i, int i2, int i3, int i4) throws RemoteException;
}
