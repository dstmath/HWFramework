package android.companion;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICompanionDeviceDiscoveryServiceCallback extends IInterface {
    void onDeviceSelected(String str, int i, String str2) throws RemoteException;

    void onDeviceSelectionCancel() throws RemoteException;

    public static class Default implements ICompanionDeviceDiscoveryServiceCallback {
        @Override // android.companion.ICompanionDeviceDiscoveryServiceCallback
        public void onDeviceSelected(String packageName, int userId, String deviceAddress) throws RemoteException {
        }

        @Override // android.companion.ICompanionDeviceDiscoveryServiceCallback
        public void onDeviceSelectionCancel() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICompanionDeviceDiscoveryServiceCallback {
        private static final String DESCRIPTOR = "android.companion.ICompanionDeviceDiscoveryServiceCallback";
        static final int TRANSACTION_onDeviceSelected = 1;
        static final int TRANSACTION_onDeviceSelectionCancel = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICompanionDeviceDiscoveryServiceCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICompanionDeviceDiscoveryServiceCallback)) {
                return new Proxy(obj);
            }
            return (ICompanionDeviceDiscoveryServiceCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onDeviceSelected";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "onDeviceSelectionCancel";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onDeviceSelected(data.readString(), data.readInt(), data.readString());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onDeviceSelectionCancel();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ICompanionDeviceDiscoveryServiceCallback {
            public static ICompanionDeviceDiscoveryServiceCallback sDefaultImpl;
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

            @Override // android.companion.ICompanionDeviceDiscoveryServiceCallback
            public void onDeviceSelected(String packageName, int userId, String deviceAddress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    _data.writeString(deviceAddress);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDeviceSelected(packageName, userId, deviceAddress);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.companion.ICompanionDeviceDiscoveryServiceCallback
            public void onDeviceSelectionCancel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDeviceSelectionCancel();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ICompanionDeviceDiscoveryServiceCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ICompanionDeviceDiscoveryServiceCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
