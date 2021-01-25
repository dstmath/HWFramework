package android.location;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGnssMeasurementsListener extends IInterface {
    void onGnssMeasurementsReceived(GnssMeasurementsEvent gnssMeasurementsEvent) throws RemoteException;

    void onStatusChanged(int i) throws RemoteException;

    public static class Default implements IGnssMeasurementsListener {
        @Override // android.location.IGnssMeasurementsListener
        public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) throws RemoteException {
        }

        @Override // android.location.IGnssMeasurementsListener
        public void onStatusChanged(int status) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IGnssMeasurementsListener {
        private static final String DESCRIPTOR = "android.location.IGnssMeasurementsListener";
        static final int TRANSACTION_onGnssMeasurementsReceived = 1;
        static final int TRANSACTION_onStatusChanged = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGnssMeasurementsListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGnssMeasurementsListener)) {
                return new Proxy(obj);
            }
            return (IGnssMeasurementsListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onGnssMeasurementsReceived";
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
            GnssMeasurementsEvent _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = GnssMeasurementsEvent.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onGnssMeasurementsReceived(_arg0);
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
        public static class Proxy implements IGnssMeasurementsListener {
            public static IGnssMeasurementsListener sDefaultImpl;
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

            @Override // android.location.IGnssMeasurementsListener
            public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) throws RemoteException {
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
                        Stub.getDefaultImpl().onGnssMeasurementsReceived(event);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.location.IGnssMeasurementsListener
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

        public static boolean setDefaultImpl(IGnssMeasurementsListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IGnssMeasurementsListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
