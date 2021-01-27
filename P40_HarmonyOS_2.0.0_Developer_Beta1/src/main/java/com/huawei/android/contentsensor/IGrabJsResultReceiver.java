package com.huawei.android.contentsensor;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGrabJsResultReceiver extends IInterface {
    void onJsResultCallBack(int i, Bundle bundle, int i2) throws RemoteException;

    public static class Default implements IGrabJsResultReceiver {
        @Override // com.huawei.android.contentsensor.IGrabJsResultReceiver
        public void onJsResultCallBack(int resultCode, Bundle data, int token) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IGrabJsResultReceiver {
        private static final String DESCRIPTOR = "com.huawei.android.contentsensor.IGrabJsResultReceiver";
        static final int TRANSACTION_onJsResultCallBack = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGrabJsResultReceiver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGrabJsResultReceiver)) {
                return new Proxy(obj);
            }
            return (IGrabJsResultReceiver) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg1;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                if (data.readInt() != 0) {
                    _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                onJsResultCallBack(_arg0, _arg1, data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IGrabJsResultReceiver {
            public static IGrabJsResultReceiver sDefaultImpl;
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

            @Override // com.huawei.android.contentsensor.IGrabJsResultReceiver
            public void onJsResultCallBack(int resultCode, Bundle data, int token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(resultCode);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(token);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onJsResultCallBack(resultCode, data, token);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IGrabJsResultReceiver impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IGrabJsResultReceiver getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
