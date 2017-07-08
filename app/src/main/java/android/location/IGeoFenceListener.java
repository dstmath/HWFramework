package android.location;

import android.app.PendingIntent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGeoFenceListener extends IInterface {

    public static abstract class Stub extends Binder implements IGeoFenceListener {
        private static final String DESCRIPTOR = "android.location.IGeoFenceListener";
        static final int TRANSACTION_geoFenceExpired = 1;

        private static class Proxy implements IGeoFenceListener {
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

            public void geoFenceExpired(PendingIntent intent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_geoFenceExpired);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_geoFenceExpired, _data, null, Stub.TRANSACTION_geoFenceExpired);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGeoFenceListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGeoFenceListener)) {
                return new Proxy(obj);
            }
            return (IGeoFenceListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_geoFenceExpired /*1*/:
                    PendingIntent pendingIntent;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent = null;
                    }
                    geoFenceExpired(pendingIntent);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void geoFenceExpired(PendingIntent pendingIntent) throws RemoteException;
}
