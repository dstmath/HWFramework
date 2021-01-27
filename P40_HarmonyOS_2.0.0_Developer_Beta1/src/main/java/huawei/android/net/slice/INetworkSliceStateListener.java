package huawei.android.net.slice;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INetworkSliceStateListener extends IInterface {
    void onNetworkSliceStateChanged(int i) throws RemoteException;

    public static class Default implements INetworkSliceStateListener {
        @Override // huawei.android.net.slice.INetworkSliceStateListener
        public void onNetworkSliceStateChanged(int retCode) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INetworkSliceStateListener {
        private static final String DESCRIPTOR = "huawei.android.net.slice.INetworkSliceStateListener";
        static final int TRANSACTION_onNetworkSliceStateChanged = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INetworkSliceStateListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INetworkSliceStateListener)) {
                return new Proxy(obj);
            }
            return (INetworkSliceStateListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onNetworkSliceStateChanged(data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements INetworkSliceStateListener {
            public static INetworkSliceStateListener sDefaultImpl;
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

            @Override // huawei.android.net.slice.INetworkSliceStateListener
            public void onNetworkSliceStateChanged(int retCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(retCode);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onNetworkSliceStateChanged(retCode);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(INetworkSliceStateListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INetworkSliceStateListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
