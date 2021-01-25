package android.location;

import android.app.PendingIntent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGeoFenceListener extends IInterface {
    void geoFenceExpired(PendingIntent pendingIntent) throws RemoteException;

    public static class Default implements IGeoFenceListener {
        @Override // android.location.IGeoFenceListener
        public void geoFenceExpired(PendingIntent intent) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IGeoFenceListener {
        private static final String DESCRIPTOR = "android.location.IGeoFenceListener";
        static final int TRANSACTION_geoFenceExpired = 1;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PendingIntent _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                geoFenceExpired(_arg0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IGeoFenceListener {
            public static IGeoFenceListener sDefaultImpl;
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

            @Override // android.location.IGeoFenceListener
            public void geoFenceExpired(PendingIntent intent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().geoFenceExpired(intent);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IGeoFenceListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IGeoFenceListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
