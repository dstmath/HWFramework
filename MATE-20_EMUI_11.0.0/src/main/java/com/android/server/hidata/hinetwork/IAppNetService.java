package com.android.server.hidata.hinetwork;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAppNetService extends IInterface {
    void detectTimeDelay(String str, int i, String str2, String str3) throws RemoteException;

    String getAccelerateEffect(String str) throws RemoteException;

    String getEstablishedInfo(String str, int i) throws RemoteException;

    void useAccelerate(String str, int i) throws RemoteException;

    public static class Default implements IAppNetService {
        @Override // com.android.server.hidata.hinetwork.IAppNetService
        public String getEstablishedInfo(String packageName, int flag) throws RemoteException {
            return null;
        }

        @Override // com.android.server.hidata.hinetwork.IAppNetService
        public void detectTimeDelay(String packageName, int networkType, String ip, String key) throws RemoteException {
        }

        @Override // com.android.server.hidata.hinetwork.IAppNetService
        public void useAccelerate(String packageName, int flag) throws RemoteException {
        }

        @Override // com.android.server.hidata.hinetwork.IAppNetService
        public String getAccelerateEffect(String packageName) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAppNetService {
        private static final String DESCRIPTOR = "com.android.server.hidata.hinetwork.IAppNetService";
        static final int TRANSACTION_detectTimeDelay = 2;
        static final int TRANSACTION_getAccelerateEffect = 4;
        static final int TRANSACTION_getEstablishedInfo = 1;
        static final int TRANSACTION_useAccelerate = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAppNetService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAppNetService)) {
                return new Proxy(obj);
            }
            return (IAppNetService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                String _result = getEstablishedInfo(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeString(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                detectTimeDelay(data.readString(), data.readInt(), data.readString(), data.readString());
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                useAccelerate(data.readString(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                String _result2 = getAccelerateEffect(data.readString());
                reply.writeNoException();
                reply.writeString(_result2);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IAppNetService {
            public static IAppNetService sDefaultImpl;
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

            @Override // com.android.server.hidata.hinetwork.IAppNetService
            public String getEstablishedInfo(String packageName, int flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flag);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEstablishedInfo(packageName, flag);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.server.hidata.hinetwork.IAppNetService
            public void detectTimeDelay(String packageName, int networkType, String ip, String key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(networkType);
                    _data.writeString(ip);
                    _data.writeString(key);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().detectTimeDelay(packageName, networkType, ip, key);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.server.hidata.hinetwork.IAppNetService
            public void useAccelerate(String packageName, int flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flag);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().useAccelerate(packageName, flag);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.server.hidata.hinetwork.IAppNetService
            public String getAccelerateEffect(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAccelerateEffect(packageName);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAppNetService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAppNetService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
