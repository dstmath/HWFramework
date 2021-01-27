package com.huawei.bd;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IBDService extends IInterface {
    void sendAccumulativeData(String str, int i, int i2) throws RemoteException;

    void sendAppActionData(String str, int i, String str2, int i2) throws RemoteException;

    public static class Default implements IBDService {
        @Override // com.huawei.bd.IBDService
        public void sendAppActionData(String pkgName, int eventID, String eventMsg, int priority) throws RemoteException {
        }

        @Override // com.huawei.bd.IBDService
        public void sendAccumulativeData(String pkgName, int eventID, int count) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IBDService {
        private static final String DESCRIPTOR = "com.huawei.bd.IBDService";
        static final int TRANSACTION_sendAccumulativeData = 2;
        static final int TRANSACTION_sendAppActionData = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBDService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBDService)) {
                return new Proxy(obj);
            }
            return (IBDService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                sendAppActionData(data.readString(), data.readInt(), data.readString(), data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                sendAccumulativeData(data.readString(), data.readInt(), data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IBDService {
            public static IBDService sDefaultImpl;
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

            @Override // com.huawei.bd.IBDService
            public void sendAppActionData(String pkgName, int eventID, String eventMsg, int priority) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(eventID);
                    _data.writeString(eventMsg);
                    _data.writeInt(priority);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().sendAppActionData(pkgName, eventID, eventMsg, priority);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.bd.IBDService
            public void sendAccumulativeData(String pkgName, int eventID, int count) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(eventID);
                    _data.writeInt(count);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().sendAccumulativeData(pkgName, eventID, count);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IBDService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IBDService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
