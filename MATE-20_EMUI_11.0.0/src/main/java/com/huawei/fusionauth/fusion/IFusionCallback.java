package com.huawei.fusionauth.fusion;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IFusionCallback extends IInterface {
    void onAuthFinish(int i, Bundle bundle) throws RemoteException;

    void onAuthProcess(int i, Bundle bundle) throws RemoteException;

    void onAuthStart(Bundle bundle) throws RemoteException;

    public static abstract class Stub extends Binder implements IFusionCallback {
        private static final String DESCRIPTOR = "com.huawei.fusionauth.fusion.IFusionCallback";
        static final int TRANSACTION_onAuthFinish = 3;
        static final int TRANSACTION_onAuthProcess = 2;
        static final int TRANSACTION_onAuthStart = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IFusionCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IFusionCallback)) {
                return new Proxy(obj);
            }
            return (IFusionCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg0;
            Bundle _arg1;
            Bundle _arg12;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onAuthStart(_arg0);
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _arg02 = data.readInt();
                if (data.readInt() != 0) {
                    _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                onAuthProcess(_arg02, _arg1);
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _arg03 = data.readInt();
                if (data.readInt() != 0) {
                    _arg12 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg12 = null;
                }
                onAuthFinish(_arg03, _arg12);
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IFusionCallback {
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

            @Override // com.huawei.fusionauth.fusion.IFusionCallback
            public void onAuthStart(Bundle extra) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (extra != null) {
                        _data.writeInt(1);
                        extra.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.fusionauth.fusion.IFusionCallback
            public void onAuthProcess(int process, Bundle extra) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(process);
                    if (extra != null) {
                        _data.writeInt(1);
                        extra.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.fusionauth.fusion.IFusionCallback
            public void onAuthFinish(int result, Bundle extra) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(result);
                    if (extra != null) {
                        _data.writeInt(1);
                        extra.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
