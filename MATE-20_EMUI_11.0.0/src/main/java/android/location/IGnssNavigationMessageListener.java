package android.location;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGnssNavigationMessageListener extends IInterface {
    void onGnssNavigationMessageReceived(GnssNavigationMessage gnssNavigationMessage) throws RemoteException;

    void onStatusChanged(int i) throws RemoteException;

    public static class Default implements IGnssNavigationMessageListener {
        @Override // android.location.IGnssNavigationMessageListener
        public void onGnssNavigationMessageReceived(GnssNavigationMessage event) throws RemoteException {
        }

        @Override // android.location.IGnssNavigationMessageListener
        public void onStatusChanged(int status) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IGnssNavigationMessageListener {
        private static final String DESCRIPTOR = "android.location.IGnssNavigationMessageListener";
        static final int TRANSACTION_onGnssNavigationMessageReceived = 1;
        static final int TRANSACTION_onStatusChanged = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGnssNavigationMessageListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGnssNavigationMessageListener)) {
                return new Proxy(obj);
            }
            return (IGnssNavigationMessageListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onGnssNavigationMessageReceived";
            }
            if (transactionCode != 2) {
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
            GnssNavigationMessage _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = GnssNavigationMessage.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onGnssNavigationMessageReceived(_arg0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onStatusChanged(data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IGnssNavigationMessageListener {
            public static IGnssNavigationMessageListener sDefaultImpl;
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

            @Override // android.location.IGnssNavigationMessageListener
            public void onGnssNavigationMessageReceived(GnssNavigationMessage event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onGnssNavigationMessageReceived(event);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.location.IGnssNavigationMessageListener
            public void onStatusChanged(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onStatusChanged(status);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IGnssNavigationMessageListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IGnssNavigationMessageListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
