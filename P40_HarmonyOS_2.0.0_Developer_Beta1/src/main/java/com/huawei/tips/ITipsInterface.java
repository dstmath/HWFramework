package com.huawei.tips;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ITipsInterface extends IInterface {
    void sendTips(String str, String str2, String str3) throws RemoteException;

    public static class Default implements ITipsInterface {
        @Override // com.huawei.tips.ITipsInterface
        public void sendTips(String targetAction, String recommendType, String featureID) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ITipsInterface {
        private static final String DESCRIPTOR = "com.huawei.tips.ITipsInterface";
        static final int TRANSACTION_sendTips = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITipsInterface asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITipsInterface)) {
                return new Proxy(obj);
            }
            return (ITipsInterface) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                sendTips(data.readString(), data.readString(), data.readString());
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
        public static class Proxy implements ITipsInterface {
            public static ITipsInterface sDefaultImpl;
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

            @Override // com.huawei.tips.ITipsInterface
            public void sendTips(String targetAction, String recommendType, String featureID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(targetAction);
                    _data.writeString(recommendType);
                    _data.writeString(featureID);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendTips(targetAction, recommendType, featureID);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ITipsInterface impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ITipsInterface getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
