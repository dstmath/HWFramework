package com.huawei.pluginmanager;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPluginUpdateStateListener extends IInterface {
    void onProgress(int i, int i2) throws RemoteException;

    void onStatus(int i, int i2, String str) throws RemoteException;

    public static class Default implements IPluginUpdateStateListener {
        @Override // com.huawei.pluginmanager.IPluginUpdateStateListener
        public void onProgress(int taskId, int percent) throws RemoteException {
        }

        @Override // com.huawei.pluginmanager.IPluginUpdateStateListener
        public void onStatus(int taskId, int status, String message) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IPluginUpdateStateListener {
        private static final String DESCRIPTOR = "com.huawei.pluginmanager.IPluginUpdateStateListener";
        static final int TRANSACTION_onProgress = 1;
        static final int TRANSACTION_onStatus = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPluginUpdateStateListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPluginUpdateStateListener)) {
                return new Proxy(obj);
            }
            return (IPluginUpdateStateListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onProgress(data.readInt(), data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onStatus(data.readInt(), data.readInt(), data.readString());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IPluginUpdateStateListener {
            public static IPluginUpdateStateListener sDefaultImpl;
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

            @Override // com.huawei.pluginmanager.IPluginUpdateStateListener
            public void onProgress(int taskId, int percent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeInt(percent);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onProgress(taskId, percent);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.pluginmanager.IPluginUpdateStateListener
            public void onStatus(int taskId, int status, String message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeInt(status);
                    _data.writeString(message);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onStatus(taskId, status, message);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IPluginUpdateStateListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPluginUpdateStateListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
