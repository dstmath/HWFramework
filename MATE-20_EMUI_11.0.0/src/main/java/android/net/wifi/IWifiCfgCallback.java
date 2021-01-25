package android.net.wifi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IWifiCfgCallback extends IInterface {
    void onNotifyWifiCfg(int i, byte[] bArr) throws RemoteException;

    public static class Default implements IWifiCfgCallback {
        @Override // android.net.wifi.IWifiCfgCallback
        public void onNotifyWifiCfg(int cfgType, byte[] data) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IWifiCfgCallback {
        private static final String DESCRIPTOR = "android.net.wifi.IWifiCfgCallback";
        static final int TRANSACTION_onNotifyWifiCfg = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWifiCfgCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWifiCfgCallback)) {
                return new Proxy(obj);
            }
            return (IWifiCfgCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "onNotifyWifiCfg";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                byte[] _arg1 = data.createByteArray();
                onNotifyWifiCfg(_arg0, _arg1);
                reply.writeNoException();
                reply.writeByteArray(_arg1);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IWifiCfgCallback {
            public static IWifiCfgCallback sDefaultImpl;
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

            @Override // android.net.wifi.IWifiCfgCallback
            public void onNotifyWifiCfg(int cfgType, byte[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cfgType);
                    _data.writeByteArray(data);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.readByteArray(data);
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onNotifyWifiCfg(cfgType, data);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IWifiCfgCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IWifiCfgCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
