package android.location;

import android.hardware.location.IGeofenceHardware;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGeofenceProvider extends IInterface {

    public static abstract class Stub extends Binder implements IGeofenceProvider {
        private static final String DESCRIPTOR = "android.location.IGeofenceProvider";
        static final int TRANSACTION_setGeofenceHardware = 1;

        private static class Proxy implements IGeofenceProvider {
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

            public void setGeofenceHardware(IGeofenceHardware proxy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(proxy != null ? proxy.asBinder() : null);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGeofenceProvider asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGeofenceProvider)) {
                return new Proxy(obj);
            }
            return (IGeofenceProvider) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                setGeofenceHardware(IGeofenceHardware.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void setGeofenceHardware(IGeofenceHardware iGeofenceHardware) throws RemoteException;
}
