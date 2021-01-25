package android.net.wifi.HwQoE;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwQoECallback extends IInterface {
    void onNetworkEvaluate(boolean z, HwQoEQualityInfo hwQoEQualityInfo) throws RemoteException;

    void onNetworkStateChange(int i) throws RemoteException;

    public static class Default implements IHwQoECallback {
        @Override // android.net.wifi.HwQoE.IHwQoECallback
        public void onNetworkEvaluate(boolean result, HwQoEQualityInfo info) throws RemoteException {
        }

        @Override // android.net.wifi.HwQoE.IHwQoECallback
        public void onNetworkStateChange(int state) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwQoECallback {
        private static final String DESCRIPTOR = "android.net.wifi.HwQoE.IHwQoECallback";
        static final int TRANSACTION_onNetworkEvaluate = 1;
        static final int TRANSACTION_onNetworkStateChange = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwQoECallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwQoECallback)) {
                return new Proxy(obj);
            }
            return (IHwQoECallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onNetworkEvaluate";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "onNetworkStateChange";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            HwQoEQualityInfo _arg1;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean _arg0 = data.readInt() != 0;
                if (data.readInt() != 0) {
                    _arg1 = HwQoEQualityInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                onNetworkEvaluate(_arg0, _arg1);
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onNetworkStateChange(data.readInt());
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
        public static class Proxy implements IHwQoECallback {
            public static IHwQoECallback sDefaultImpl;
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

            @Override // android.net.wifi.HwQoE.IHwQoECallback
            public void onNetworkEvaluate(boolean result, HwQoEQualityInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(result ? 1 : 0);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onNetworkEvaluate(result, info);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.HwQoE.IHwQoECallback
            public void onNetworkStateChange(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onNetworkStateChange(state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwQoECallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwQoECallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
