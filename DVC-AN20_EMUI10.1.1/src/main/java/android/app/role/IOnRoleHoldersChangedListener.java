package android.app.role;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOnRoleHoldersChangedListener extends IInterface {
    void onRoleHoldersChanged(String str, int i) throws RemoteException;

    public static class Default implements IOnRoleHoldersChangedListener {
        @Override // android.app.role.IOnRoleHoldersChangedListener
        public void onRoleHoldersChanged(String roleName, int userId) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOnRoleHoldersChangedListener {
        private static final String DESCRIPTOR = "android.app.role.IOnRoleHoldersChangedListener";
        static final int TRANSACTION_onRoleHoldersChanged = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOnRoleHoldersChangedListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOnRoleHoldersChangedListener)) {
                return new Proxy(obj);
            }
            return (IOnRoleHoldersChangedListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "onRoleHoldersChanged";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onRoleHoldersChanged(data.readString(), data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IOnRoleHoldersChangedListener {
            public static IOnRoleHoldersChangedListener sDefaultImpl;
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

            @Override // android.app.role.IOnRoleHoldersChangedListener
            public void onRoleHoldersChanged(String roleName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(roleName);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRoleHoldersChanged(roleName, userId);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOnRoleHoldersChangedListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOnRoleHoldersChangedListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
