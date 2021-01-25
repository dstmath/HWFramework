package android.net.wifi.aware;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IWifiAwareEventCallback extends IInterface {
    void onConnectFail(int i) throws RemoteException;

    void onConnectSuccess(int i) throws RemoteException;

    void onIdentityChanged(byte[] bArr) throws RemoteException;

    public static class Default implements IWifiAwareEventCallback {
        @Override // android.net.wifi.aware.IWifiAwareEventCallback
        public void onConnectSuccess(int clientId) throws RemoteException {
        }

        @Override // android.net.wifi.aware.IWifiAwareEventCallback
        public void onConnectFail(int reason) throws RemoteException {
        }

        @Override // android.net.wifi.aware.IWifiAwareEventCallback
        public void onIdentityChanged(byte[] mac) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IWifiAwareEventCallback {
        private static final String DESCRIPTOR = "android.net.wifi.aware.IWifiAwareEventCallback";
        static final int TRANSACTION_onConnectFail = 2;
        static final int TRANSACTION_onConnectSuccess = 1;
        static final int TRANSACTION_onIdentityChanged = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWifiAwareEventCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWifiAwareEventCallback)) {
                return new Proxy(obj);
            }
            return (IWifiAwareEventCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onConnectSuccess";
            }
            if (transactionCode == 2) {
                return "onConnectFail";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "onIdentityChanged";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onConnectSuccess(data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onConnectFail(data.readInt());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onIdentityChanged(data.createByteArray());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IWifiAwareEventCallback {
            public static IWifiAwareEventCallback sDefaultImpl;
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

            @Override // android.net.wifi.aware.IWifiAwareEventCallback
            public void onConnectSuccess(int clientId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientId);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onConnectSuccess(clientId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.aware.IWifiAwareEventCallback
            public void onConnectFail(int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reason);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onConnectFail(reason);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.aware.IWifiAwareEventCallback
            public void onIdentityChanged(byte[] mac) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(mac);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onIdentityChanged(mac);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IWifiAwareEventCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IWifiAwareEventCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
