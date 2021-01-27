package android.content.pm;

import android.annotation.UnsupportedAppUsage;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPackageDeleteObserver2 extends IInterface {
    @UnsupportedAppUsage
    void onPackageDeleted(String str, int i, String str2) throws RemoteException;

    void onUserActionRequired(Intent intent) throws RemoteException;

    public static class Default implements IPackageDeleteObserver2 {
        @Override // android.content.pm.IPackageDeleteObserver2
        public void onUserActionRequired(Intent intent) throws RemoteException {
        }

        @Override // android.content.pm.IPackageDeleteObserver2
        public void onPackageDeleted(String packageName, int returnCode, String msg) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IPackageDeleteObserver2 {
        private static final String DESCRIPTOR = "android.content.pm.IPackageDeleteObserver2";
        static final int TRANSACTION_onPackageDeleted = 2;
        static final int TRANSACTION_onUserActionRequired = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPackageDeleteObserver2 asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPackageDeleteObserver2)) {
                return new Proxy(obj);
            }
            return (IPackageDeleteObserver2) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onUserActionRequired";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "onPackageDeleted";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Intent _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = Intent.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onUserActionRequired(_arg0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onPackageDeleted(data.readString(), data.readInt(), data.readString());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IPackageDeleteObserver2 {
            public static IPackageDeleteObserver2 sDefaultImpl;
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

            @Override // android.content.pm.IPackageDeleteObserver2
            public void onUserActionRequired(Intent intent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onUserActionRequired(intent);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IPackageDeleteObserver2
            public void onPackageDeleted(String packageName, int returnCode, String msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(returnCode);
                    _data.writeString(msg);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onPackageDeleted(packageName, returnCode, msg);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IPackageDeleteObserver2 impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPackageDeleteObserver2 getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
