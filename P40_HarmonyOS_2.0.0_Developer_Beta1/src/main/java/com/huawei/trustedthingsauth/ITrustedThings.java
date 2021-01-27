package com.huawei.trustedthingsauth;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.trustedthingsauth.ITrustedThingsCallback;

public interface ITrustedThings extends IInterface {
    void isFeatureSupported(String str, ITrustedThingsCallback iTrustedThingsCallback) throws RemoteException;

    void notifySetUp(String str, ITrustedThingsCallback iTrustedThingsCallback) throws RemoteException;

    void startAuth(String str, ITrustedThingsCallback iTrustedThingsCallback) throws RemoteException;

    void stopAuth(String str) throws RemoteException;

    public static abstract class Stub extends Binder implements ITrustedThings {
        private static final String DESCRIPTOR = "com.huawei.trustedthingsauth.ITrustedThings";
        static final int TRANSACTION_isFeatureSupported = 1;
        static final int TRANSACTION_notifySetUp = 2;
        static final int TRANSACTION_startAuth = 3;
        static final int TRANSACTION_stopAuth = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITrustedThings asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITrustedThings)) {
                return new Proxy(obj);
            }
            return (ITrustedThings) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                isFeatureSupported(data.readString(), ITrustedThingsCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                notifySetUp(data.readString(), ITrustedThingsCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                startAuth(data.readString(), ITrustedThingsCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                stopAuth(data.readString());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements ITrustedThings {
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

            @Override // com.huawei.trustedthingsauth.ITrustedThings
            public void isFeatureSupported(String deviceId, ITrustedThingsCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.trustedthingsauth.ITrustedThings
            public void notifySetUp(String deviceId, ITrustedThingsCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.trustedthingsauth.ITrustedThings
            public void startAuth(String deviceId, ITrustedThingsCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.trustedthingsauth.ITrustedThings
            public void stopAuth(String deviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }
    }
}
