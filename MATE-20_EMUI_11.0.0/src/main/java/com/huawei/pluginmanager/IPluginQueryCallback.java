package com.huawei.pluginmanager;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IPluginQueryCallback extends IInterface {
    void onResult(int i, List<CloudPluginInfo> list) throws RemoteException;

    void onStatus(int i, int i2, String str) throws RemoteException;

    public static class Default implements IPluginQueryCallback {
        @Override // com.huawei.pluginmanager.IPluginQueryCallback
        public void onResult(int taskId, List<CloudPluginInfo> list) throws RemoteException {
        }

        @Override // com.huawei.pluginmanager.IPluginQueryCallback
        public void onStatus(int taskId, int status, String message) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IPluginQueryCallback {
        private static final String DESCRIPTOR = "com.huawei.pluginmanager.IPluginQueryCallback";
        static final int TRANSACTION_onResult = 1;
        static final int TRANSACTION_onStatus = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPluginQueryCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPluginQueryCallback)) {
                return new Proxy(obj);
            }
            return (IPluginQueryCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onResult(data.readInt(), data.createTypedArrayList(CloudPluginInfo.CREATOR));
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
        public static class Proxy implements IPluginQueryCallback {
            public static IPluginQueryCallback sDefaultImpl;
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

            @Override // com.huawei.pluginmanager.IPluginQueryCallback
            public void onResult(int taskId, List<CloudPluginInfo> cloudPlugins) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeTypedList(cloudPlugins);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onResult(taskId, cloudPlugins);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.pluginmanager.IPluginQueryCallback
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

        public static boolean setDefaultImpl(IPluginQueryCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPluginQueryCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
