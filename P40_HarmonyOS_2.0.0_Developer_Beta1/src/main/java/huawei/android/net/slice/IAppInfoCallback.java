package huawei.android.net.slice;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAppInfoCallback extends IInterface {
    void onPermissionCheckCallback(boolean z) throws RemoteException;

    public static class Default implements IAppInfoCallback {
        @Override // huawei.android.net.slice.IAppInfoCallback
        public void onPermissionCheckCallback(boolean isSuccess) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAppInfoCallback {
        private static final String DESCRIPTOR = "huawei.android.net.slice.IAppInfoCallback";
        static final int TRANSACTION_onPermissionCheckCallback = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAppInfoCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAppInfoCallback)) {
                return new Proxy(obj);
            }
            return (IAppInfoCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onPermissionCheckCallback(data.readInt() != 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IAppInfoCallback {
            public static IAppInfoCallback sDefaultImpl;
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

            @Override // huawei.android.net.slice.IAppInfoCallback
            public void onPermissionCheckCallback(boolean isSuccess) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isSuccess ? 1 : 0);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onPermissionCheckCallback(isSuccess);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAppInfoCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAppInfoCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
