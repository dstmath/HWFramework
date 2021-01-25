package android.net.wifi.aware;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.Map;

public interface IWifiAwareMacAddressProvider extends IInterface {
    void macAddress(Map map) throws RemoteException;

    public static class Default implements IWifiAwareMacAddressProvider {
        @Override // android.net.wifi.aware.IWifiAwareMacAddressProvider
        public void macAddress(Map peerIdToMacMap) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IWifiAwareMacAddressProvider {
        private static final String DESCRIPTOR = "android.net.wifi.aware.IWifiAwareMacAddressProvider";
        static final int TRANSACTION_macAddress = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWifiAwareMacAddressProvider asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWifiAwareMacAddressProvider)) {
                return new Proxy(obj);
            }
            return (IWifiAwareMacAddressProvider) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "macAddress";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                macAddress(data.readHashMap(getClass().getClassLoader()));
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IWifiAwareMacAddressProvider {
            public static IWifiAwareMacAddressProvider sDefaultImpl;
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

            @Override // android.net.wifi.aware.IWifiAwareMacAddressProvider
            public void macAddress(Map peerIdToMacMap) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeMap(peerIdToMacMap);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().macAddress(peerIdToMacMap);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IWifiAwareMacAddressProvider impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IWifiAwareMacAddressProvider getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
