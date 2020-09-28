package huawei.android.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOnHwPermissionChangeListener extends IInterface {
    void onPermissionChanged(String str, String str2, long j, int i) throws RemoteException;

    public static class Default implements IOnHwPermissionChangeListener {
        @Override // huawei.android.security.IOnHwPermissionChangeListener
        public void onPermissionChanged(String caller, String pkgName, long permType, int operation) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOnHwPermissionChangeListener {
        private static final String DESCRIPTOR = "huawei.android.security.IOnHwPermissionChangeListener";
        static final int TRANSACTION_onPermissionChanged = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOnHwPermissionChangeListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOnHwPermissionChangeListener)) {
                return new Proxy(obj);
            }
            return (IOnHwPermissionChangeListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onPermissionChanged(data.readString(), data.readString(), data.readLong(), data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IOnHwPermissionChangeListener {
            public static IOnHwPermissionChangeListener sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // huawei.android.security.IOnHwPermissionChangeListener
            public void onPermissionChanged(String caller, String pkgName, long permType, int operation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(caller);
                    _data.writeString(pkgName);
                    _data.writeLong(permType);
                    _data.writeInt(operation);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onPermissionChanged(caller, pkgName, permType, operation);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOnHwPermissionChangeListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOnHwPermissionChangeListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
