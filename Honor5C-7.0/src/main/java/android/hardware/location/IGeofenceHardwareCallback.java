package android.hardware.location;

import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGeofenceHardwareCallback extends IInterface {

    public static abstract class Stub extends Binder implements IGeofenceHardwareCallback {
        private static final String DESCRIPTOR = "android.hardware.location.IGeofenceHardwareCallback";
        static final int TRANSACTION_onGeofenceAdd = 2;
        static final int TRANSACTION_onGeofencePause = 4;
        static final int TRANSACTION_onGeofenceRemove = 3;
        static final int TRANSACTION_onGeofenceResume = 5;
        static final int TRANSACTION_onGeofenceTransition = 1;

        private static class Proxy implements IGeofenceHardwareCallback {
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

            public void onGeofenceTransition(int geofenceId, int transition, Location location, long timestamp, int monitoringType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(geofenceId);
                    _data.writeInt(transition);
                    if (location != null) {
                        _data.writeInt(Stub.TRANSACTION_onGeofenceTransition);
                        location.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(timestamp);
                    _data.writeInt(monitoringType);
                    this.mRemote.transact(Stub.TRANSACTION_onGeofenceTransition, _data, null, Stub.TRANSACTION_onGeofenceTransition);
                } finally {
                    _data.recycle();
                }
            }

            public void onGeofenceAdd(int geofenceId, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(geofenceId);
                    _data.writeInt(status);
                    this.mRemote.transact(Stub.TRANSACTION_onGeofenceAdd, _data, null, Stub.TRANSACTION_onGeofenceTransition);
                } finally {
                    _data.recycle();
                }
            }

            public void onGeofenceRemove(int geofenceId, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(geofenceId);
                    _data.writeInt(status);
                    this.mRemote.transact(Stub.TRANSACTION_onGeofenceRemove, _data, null, Stub.TRANSACTION_onGeofenceTransition);
                } finally {
                    _data.recycle();
                }
            }

            public void onGeofencePause(int geofenceId, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(geofenceId);
                    _data.writeInt(status);
                    this.mRemote.transact(Stub.TRANSACTION_onGeofencePause, _data, null, Stub.TRANSACTION_onGeofenceTransition);
                } finally {
                    _data.recycle();
                }
            }

            public void onGeofenceResume(int geofenceId, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(geofenceId);
                    _data.writeInt(status);
                    this.mRemote.transact(Stub.TRANSACTION_onGeofenceResume, _data, null, Stub.TRANSACTION_onGeofenceTransition);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGeofenceHardwareCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGeofenceHardwareCallback)) {
                return new Proxy(obj);
            }
            return (IGeofenceHardwareCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_onGeofenceTransition /*1*/:
                    Location location;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    int _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        location = (Location) Location.CREATOR.createFromParcel(data);
                    } else {
                        location = null;
                    }
                    onGeofenceTransition(_arg0, _arg1, location, data.readLong(), data.readInt());
                    return true;
                case TRANSACTION_onGeofenceAdd /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onGeofenceAdd(data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_onGeofenceRemove /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    onGeofenceRemove(data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_onGeofencePause /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    onGeofencePause(data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_onGeofenceResume /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    onGeofenceResume(data.readInt(), data.readInt());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onGeofenceAdd(int i, int i2) throws RemoteException;

    void onGeofencePause(int i, int i2) throws RemoteException;

    void onGeofenceRemove(int i, int i2) throws RemoteException;

    void onGeofenceResume(int i, int i2) throws RemoteException;

    void onGeofenceTransition(int i, int i2, Location location, long j, int i3) throws RemoteException;
}
