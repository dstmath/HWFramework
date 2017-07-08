package android.content.pm;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPackageDeleteObserver2 extends IInterface {

    public static abstract class Stub extends Binder implements IPackageDeleteObserver2 {
        private static final String DESCRIPTOR = "android.content.pm.IPackageDeleteObserver2";
        static final int TRANSACTION_onPackageDeleted = 2;
        static final int TRANSACTION_onUserActionRequired = 1;

        private static class Proxy implements IPackageDeleteObserver2 {
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

            public void onUserActionRequired(Intent intent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_onUserActionRequired);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onUserActionRequired, _data, null, Stub.TRANSACTION_onUserActionRequired);
                } finally {
                    _data.recycle();
                }
            }

            public void onPackageDeleted(String packageName, int returnCode, String msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(returnCode);
                    _data.writeString(msg);
                    this.mRemote.transact(Stub.TRANSACTION_onPackageDeleted, _data, null, Stub.TRANSACTION_onUserActionRequired);
                } finally {
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_onUserActionRequired /*1*/:
                    Intent intent;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    onUserActionRequired(intent);
                    return true;
                case TRANSACTION_onPackageDeleted /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onPackageDeleted(data.readString(), data.readInt(), data.readString());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onPackageDeleted(String str, int i, String str2) throws RemoteException;

    void onUserActionRequired(Intent intent) throws RemoteException;
}
