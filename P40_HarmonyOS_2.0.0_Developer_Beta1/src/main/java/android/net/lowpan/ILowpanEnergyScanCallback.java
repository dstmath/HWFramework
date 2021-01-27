package android.net.lowpan;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ILowpanEnergyScanCallback extends IInterface {
    void onEnergyScanFinished() throws RemoteException;

    void onEnergyScanResult(int i, int i2) throws RemoteException;

    public static class Default implements ILowpanEnergyScanCallback {
        @Override // android.net.lowpan.ILowpanEnergyScanCallback
        public void onEnergyScanResult(int channel, int rssi) throws RemoteException {
        }

        @Override // android.net.lowpan.ILowpanEnergyScanCallback
        public void onEnergyScanFinished() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ILowpanEnergyScanCallback {
        private static final String DESCRIPTOR = "android.net.lowpan.ILowpanEnergyScanCallback";
        static final int TRANSACTION_onEnergyScanFinished = 2;
        static final int TRANSACTION_onEnergyScanResult = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ILowpanEnergyScanCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ILowpanEnergyScanCallback)) {
                return new Proxy(obj);
            }
            return (ILowpanEnergyScanCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onEnergyScanResult";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "onEnergyScanFinished";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onEnergyScanResult(data.readInt(), data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onEnergyScanFinished();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ILowpanEnergyScanCallback {
            public static ILowpanEnergyScanCallback sDefaultImpl;
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

            @Override // android.net.lowpan.ILowpanEnergyScanCallback
            public void onEnergyScanResult(int channel, int rssi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channel);
                    _data.writeInt(rssi);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onEnergyScanResult(channel, rssi);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.lowpan.ILowpanEnergyScanCallback
            public void onEnergyScanFinished() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onEnergyScanFinished();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ILowpanEnergyScanCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ILowpanEnergyScanCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
