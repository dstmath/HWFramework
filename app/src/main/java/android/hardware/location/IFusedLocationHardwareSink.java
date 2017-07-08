package android.hardware.location;

import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IFusedLocationHardwareSink extends IInterface {

    public static abstract class Stub extends Binder implements IFusedLocationHardwareSink {
        private static final String DESCRIPTOR = "android.hardware.location.IFusedLocationHardwareSink";
        static final int TRANSACTION_onCapabilities = 3;
        static final int TRANSACTION_onDiagnosticDataAvailable = 2;
        static final int TRANSACTION_onLocationAvailable = 1;
        static final int TRANSACTION_onStatusChanged = 4;

        private static class Proxy implements IFusedLocationHardwareSink {
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

            public void onLocationAvailable(Location[] locations) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(locations, 0);
                    this.mRemote.transact(Stub.TRANSACTION_onLocationAvailable, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onDiagnosticDataAvailable(String data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(data);
                    this.mRemote.transact(Stub.TRANSACTION_onDiagnosticDataAvailable, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onCapabilities(int capabilities) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(capabilities);
                    this.mRemote.transact(Stub.TRANSACTION_onCapabilities, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onStatusChanged(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    this.mRemote.transact(Stub.TRANSACTION_onStatusChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IFusedLocationHardwareSink asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IFusedLocationHardwareSink)) {
                return new Proxy(obj);
            }
            return (IFusedLocationHardwareSink) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_onLocationAvailable /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    onLocationAvailable((Location[]) data.createTypedArray(Location.CREATOR));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onDiagnosticDataAvailable /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onDiagnosticDataAvailable(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onCapabilities /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    onCapabilities(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onStatusChanged /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    onStatusChanged(data.readInt());
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onCapabilities(int i) throws RemoteException;

    void onDiagnosticDataAvailable(String str) throws RemoteException;

    void onLocationAvailable(Location[] locationArr) throws RemoteException;

    void onStatusChanged(int i) throws RemoteException;
}
