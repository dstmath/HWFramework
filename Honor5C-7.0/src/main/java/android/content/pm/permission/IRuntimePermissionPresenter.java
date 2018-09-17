package android.content.pm.permission;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteCallback;
import android.os.RemoteException;

public interface IRuntimePermissionPresenter extends IInterface {

    public static abstract class Stub extends Binder implements IRuntimePermissionPresenter {
        private static final String DESCRIPTOR = "android.content.pm.permission.IRuntimePermissionPresenter";
        static final int TRANSACTION_getAppPermissions = 1;
        static final int TRANSACTION_getAppsUsingPermissions = 2;

        private static class Proxy implements IRuntimePermissionPresenter {
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

            public void getAppPermissions(String packageName, RemoteCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (callback != null) {
                        _data.writeInt(Stub.TRANSACTION_getAppPermissions);
                        callback.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getAppPermissions, _data, null, Stub.TRANSACTION_getAppPermissions);
                } finally {
                    _data.recycle();
                }
            }

            public void getAppsUsingPermissions(boolean system, RemoteCallback callback) throws RemoteException {
                int i = Stub.TRANSACTION_getAppPermissions;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!system) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (callback != null) {
                        _data.writeInt(Stub.TRANSACTION_getAppPermissions);
                        callback.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getAppsUsingPermissions, _data, null, Stub.TRANSACTION_getAppPermissions);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRuntimePermissionPresenter asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IRuntimePermissionPresenter)) {
                return new Proxy(obj);
            }
            return (IRuntimePermissionPresenter) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            RemoteCallback remoteCallback;
            switch (code) {
                case TRANSACTION_getAppPermissions /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    String _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        remoteCallback = (RemoteCallback) RemoteCallback.CREATOR.createFromParcel(data);
                    } else {
                        remoteCallback = null;
                    }
                    getAppPermissions(_arg0, remoteCallback);
                    return true;
                case TRANSACTION_getAppsUsingPermissions /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _arg02 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        remoteCallback = (RemoteCallback) RemoteCallback.CREATOR.createFromParcel(data);
                    } else {
                        remoteCallback = null;
                    }
                    getAppsUsingPermissions(_arg02, remoteCallback);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void getAppPermissions(String str, RemoteCallback remoteCallback) throws RemoteException;

    void getAppsUsingPermissions(boolean z, RemoteCallback remoteCallback) throws RemoteException;
}
