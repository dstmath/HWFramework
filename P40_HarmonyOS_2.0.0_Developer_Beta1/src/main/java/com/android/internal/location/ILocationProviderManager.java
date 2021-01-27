package com.android.internal.location;

import android.annotation.UnsupportedAppUsage;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface ILocationProviderManager extends IInterface {
    @UnsupportedAppUsage
    void onReportLocation(Location location) throws RemoteException;

    void onSetAdditionalProviderPackages(List<String> list) throws RemoteException;

    @UnsupportedAppUsage
    void onSetEnabled(boolean z) throws RemoteException;

    @UnsupportedAppUsage
    void onSetProperties(ProviderProperties providerProperties) throws RemoteException;

    public static class Default implements ILocationProviderManager {
        @Override // com.android.internal.location.ILocationProviderManager
        public void onSetAdditionalProviderPackages(List<String> list) throws RemoteException {
        }

        @Override // com.android.internal.location.ILocationProviderManager
        public void onSetEnabled(boolean enabled) throws RemoteException {
        }

        @Override // com.android.internal.location.ILocationProviderManager
        public void onSetProperties(ProviderProperties properties) throws RemoteException {
        }

        @Override // com.android.internal.location.ILocationProviderManager
        public void onReportLocation(Location location) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ILocationProviderManager {
        private static final String DESCRIPTOR = "com.android.internal.location.ILocationProviderManager";
        static final int TRANSACTION_onReportLocation = 4;
        static final int TRANSACTION_onSetAdditionalProviderPackages = 1;
        static final int TRANSACTION_onSetEnabled = 2;
        static final int TRANSACTION_onSetProperties = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ILocationProviderManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ILocationProviderManager)) {
                return new Proxy(obj);
            }
            return (ILocationProviderManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onSetAdditionalProviderPackages";
            }
            if (transactionCode == 2) {
                return "onSetEnabled";
            }
            if (transactionCode == 3) {
                return "onSetProperties";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "onReportLocation";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ProviderProperties _arg0;
            Location _arg02;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onSetAdditionalProviderPackages(data.createStringArrayList());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onSetEnabled(data.readInt() != 0);
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = ProviderProperties.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onSetProperties(_arg0);
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = Location.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                onReportLocation(_arg02);
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ILocationProviderManager {
            public static ILocationProviderManager sDefaultImpl;
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

            @Override // com.android.internal.location.ILocationProviderManager
            public void onSetAdditionalProviderPackages(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onSetAdditionalProviderPackages(packageNames);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.location.ILocationProviderManager
            public void onSetEnabled(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onSetEnabled(enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.location.ILocationProviderManager
            public void onSetProperties(ProviderProperties properties) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (properties != null) {
                        _data.writeInt(1);
                        properties.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onSetProperties(properties);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.location.ILocationProviderManager
            public void onReportLocation(Location location) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (location != null) {
                        _data.writeInt(1);
                        location.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onReportLocation(location);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ILocationProviderManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ILocationProviderManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
