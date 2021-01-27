package android.location;

import android.annotation.UnsupportedAppUsage;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ILocationListener extends IInterface {
    @UnsupportedAppUsage
    void onLocationChanged(Location location) throws RemoteException;

    @UnsupportedAppUsage
    void onProviderDisabled(String str) throws RemoteException;

    @UnsupportedAppUsage
    void onProviderEnabled(String str) throws RemoteException;

    @UnsupportedAppUsage
    void onStatusChanged(String str, int i, Bundle bundle) throws RemoteException;

    public static class Default implements ILocationListener {
        @Override // android.location.ILocationListener
        public void onLocationChanged(Location location) throws RemoteException {
        }

        @Override // android.location.ILocationListener
        public void onProviderEnabled(String provider) throws RemoteException {
        }

        @Override // android.location.ILocationListener
        public void onProviderDisabled(String provider) throws RemoteException {
        }

        @Override // android.location.ILocationListener
        public void onStatusChanged(String provider, int status, Bundle extras) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ILocationListener {
        private static final String DESCRIPTOR = "android.location.ILocationListener";
        static final int TRANSACTION_onLocationChanged = 1;
        static final int TRANSACTION_onProviderDisabled = 3;
        static final int TRANSACTION_onProviderEnabled = 2;
        static final int TRANSACTION_onStatusChanged = 4;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onLocationChanged";
            }
            if (transactionCode == 2) {
                return "onProviderEnabled";
            }
            if (transactionCode == 3) {
                return "onProviderDisabled";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "onStatusChanged";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Location _arg0;
            Bundle _arg2;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = Location.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onLocationChanged(_arg0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onProviderEnabled(data.readString());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onProviderDisabled(data.readString());
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                String _arg02 = data.readString();
                int _arg1 = data.readInt();
                if (data.readInt() != 0) {
                    _arg2 = Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                onStatusChanged(_arg02, _arg1, _arg2);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ILocationListener {
            public static ILocationListener sDefaultImpl;
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

            @Override // android.location.ILocationListener
            public void onLocationChanged(Location location) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (location != null) {
                        _data.writeInt(1);
                        location.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onLocationChanged(location);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationListener
            public void onProviderEnabled(String provider) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onProviderEnabled(provider);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationListener
            public void onProviderDisabled(String provider) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onProviderDisabled(provider);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.location.ILocationListener
            public void onStatusChanged(String provider, int status, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(provider);
                    _data.writeInt(status);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onStatusChanged(provider, status, extras);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ILocationListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ILocationListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
