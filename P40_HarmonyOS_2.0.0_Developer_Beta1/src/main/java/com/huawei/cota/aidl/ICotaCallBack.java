package com.huawei.cota.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICotaCallBack extends IInterface {
    void onAppInstallFinish(int i) throws RemoteException;

    public static class Default implements ICotaCallBack {
        @Override // com.huawei.cota.aidl.ICotaCallBack
        public void onAppInstallFinish(int apksInstallStatus) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICotaCallBack {
        private static final String DESCRIPTOR = "com.huawei.cota.aidl.ICotaCallBack";
        static final int TRANSACTION_onAppInstallFinish = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICotaCallBack asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICotaCallBack)) {
                return new Proxy(obj);
            }
            return (ICotaCallBack) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onAppInstallFinish(data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ICotaCallBack {
            public static ICotaCallBack sDefaultImpl;
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

            @Override // com.huawei.cota.aidl.ICotaCallBack
            public void onAppInstallFinish(int apksInstallStatus) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(apksInstallStatus);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAppInstallFinish(apksInstallStatus);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ICotaCallBack impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ICotaCallBack getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
