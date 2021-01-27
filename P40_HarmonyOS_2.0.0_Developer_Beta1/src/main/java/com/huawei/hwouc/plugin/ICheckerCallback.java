package com.huawei.hwouc.plugin;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.Map;

public interface ICheckerCallback extends IInterface {
    void onCheckBaseResult(int i, int i2, Map map) throws RemoteException;

    void onCheckResult(int i, int i2, Map map) throws RemoteException;

    public static class Default implements ICheckerCallback {
        @Override // com.huawei.hwouc.plugin.ICheckerCallback
        public void onCheckBaseResult(int taskId, int status, Map newPluginBaseInfoMap) throws RemoteException {
        }

        @Override // com.huawei.hwouc.plugin.ICheckerCallback
        public void onCheckResult(int taskId, int status, Map newOucPluginDetailInfoMap) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICheckerCallback {
        private static final String DESCRIPTOR = "com.huawei.hwouc.plugin.ICheckerCallback";
        static final int TRANSACTION_onCheckBaseResult = 1;
        static final int TRANSACTION_onCheckResult = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICheckerCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICheckerCallback)) {
                return new Proxy(obj);
            }
            return (ICheckerCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onCheckBaseResult(data.readInt(), data.readInt(), data.readHashMap(getClass().getClassLoader()));
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onCheckResult(data.readInt(), data.readInt(), data.readHashMap(getClass().getClassLoader()));
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
        public static class Proxy implements ICheckerCallback {
            public static ICheckerCallback sDefaultImpl;
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

            @Override // com.huawei.hwouc.plugin.ICheckerCallback
            public void onCheckBaseResult(int taskId, int status, Map newPluginBaseInfoMap) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeInt(status);
                    _data.writeMap(newPluginBaseInfoMap);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onCheckBaseResult(taskId, status, newPluginBaseInfoMap);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwouc.plugin.ICheckerCallback
            public void onCheckResult(int taskId, int status, Map newOucPluginDetailInfoMap) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeInt(status);
                    _data.writeMap(newOucPluginDetailInfoMap);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onCheckResult(taskId, status, newOucPluginDetailInfoMap);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ICheckerCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ICheckerCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
