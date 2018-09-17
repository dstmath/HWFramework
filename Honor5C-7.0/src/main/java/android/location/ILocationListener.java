package android.location;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ILocationListener extends IInterface {

    public static abstract class Stub extends Binder implements ILocationListener {
        private static final String DESCRIPTOR = "android.location.ILocationListener";
        static final int TRANSACTION_onLocationChanged = 1;
        static final int TRANSACTION_onProviderDisabled = 4;
        static final int TRANSACTION_onProviderEnabled = 3;
        static final int TRANSACTION_onStatusChanged = 2;

        private static class Proxy implements ILocationListener {
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

            public void onLocationChanged(Location location) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (location != null) {
                        _data.writeInt(Stub.TRANSACTION_onLocationChanged);
                        location.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onLocationChanged, _data, null, Stub.TRANSACTION_onLocationChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    _data.writeInt(status);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_onLocationChanged);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onStatusChanged, _data, null, Stub.TRANSACTION_onLocationChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onProviderEnabled(String provider) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    this.mRemote.transact(Stub.TRANSACTION_onProviderEnabled, _data, null, Stub.TRANSACTION_onLocationChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onProviderDisabled(String provider) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    this.mRemote.transact(Stub.TRANSACTION_onProviderDisabled, _data, null, Stub.TRANSACTION_onLocationChanged);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ILocationListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ILocationListener)) {
                return new Proxy(obj);
            }
            return (ILocationListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_onLocationChanged /*1*/:
                    Location location;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        location = (Location) Location.CREATOR.createFromParcel(data);
                    } else {
                        location = null;
                    }
                    onLocationChanged(location);
                    return true;
                case TRANSACTION_onStatusChanged /*2*/:
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    String _arg0 = data.readString();
                    int _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    onStatusChanged(_arg0, _arg1, bundle);
                    return true;
                case TRANSACTION_onProviderEnabled /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    onProviderEnabled(data.readString());
                    return true;
                case TRANSACTION_onProviderDisabled /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    onProviderDisabled(data.readString());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onLocationChanged(Location location) throws RemoteException;

    void onProviderDisabled(String str) throws RemoteException;

    void onProviderEnabled(String str) throws RemoteException;

    void onStatusChanged(String str, int i, Bundle bundle) throws RemoteException;
}
