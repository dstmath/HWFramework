package com.huawei.pluginmanager;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.pluginmanager.IPluginQueryCallback;
import com.huawei.pluginmanager.IPluginQueryDetailCallback;
import com.huawei.pluginmanager.IPluginUpdateStateListener;
import java.util.List;

public interface IPluginManager extends IInterface {
    void cancelInstall(String str, int i) throws RemoteException;

    int queryPluginBasicInfoByCategory(String str, List<String> list, IPluginQueryCallback iPluginQueryCallback) throws RemoteException;

    int queryPluginBasicInfoByName(String str, List<String> list, IPluginQueryCallback iPluginQueryCallback) throws RemoteException;

    int queryPluginDetailInfo(String str, List<String> list, IPluginQueryDetailCallback iPluginQueryDetailCallback) throws RemoteException;

    void registerAutoUpdate(String str, boolean z) throws RemoteException;

    int startInstall(String str, List<String> list, int i, IPluginUpdateStateListener iPluginUpdateStateListener) throws RemoteException;

    int uninstall(String str, List<String> list, int i, IPluginUpdateStateListener iPluginUpdateStateListener) throws RemoteException;

    public static class Default implements IPluginManager {
        @Override // com.huawei.pluginmanager.IPluginManager
        public int queryPluginBasicInfoByCategory(String packageName, List<String> list, IPluginQueryCallback callback) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.pluginmanager.IPluginManager
        public int queryPluginBasicInfoByName(String packageName, List<String> list, IPluginQueryCallback callback) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.pluginmanager.IPluginManager
        public int queryPluginDetailInfo(String packageName, List<String> list, IPluginQueryDetailCallback callback) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.pluginmanager.IPluginManager
        public void registerAutoUpdate(String packageName, boolean isOn) throws RemoteException {
        }

        @Override // com.huawei.pluginmanager.IPluginManager
        public int startInstall(String packageName, List<String> list, int flags, IPluginUpdateStateListener listener) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.pluginmanager.IPluginManager
        public int uninstall(String packageName, List<String> list, int flags, IPluginUpdateStateListener listener) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.pluginmanager.IPluginManager
        public void cancelInstall(String packageName, int taskId) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IPluginManager {
        private static final String DESCRIPTOR = "com.huawei.pluginmanager.IPluginManager";
        static final int TRANSACTION_cancelInstall = 7;
        static final int TRANSACTION_queryPluginBasicInfoByCategory = 1;
        static final int TRANSACTION_queryPluginBasicInfoByName = 2;
        static final int TRANSACTION_queryPluginDetailInfo = 3;
        static final int TRANSACTION_registerAutoUpdate = 4;
        static final int TRANSACTION_startInstall = 5;
        static final int TRANSACTION_uninstall = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPluginManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPluginManager)) {
                return new Proxy(obj);
            }
            return (IPluginManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = queryPluginBasicInfoByCategory(data.readString(), data.createStringArrayList(), IPluginQueryCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = queryPluginBasicInfoByName(data.readString(), data.createStringArrayList(), IPluginQueryCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = queryPluginDetailInfo(data.readString(), data.createStringArrayList(), IPluginQueryDetailCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        registerAutoUpdate(data.readString(), data.readInt() != 0);
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = startInstall(data.readString(), data.createStringArrayList(), data.readInt(), IPluginUpdateStateListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = uninstall(data.readString(), data.createStringArrayList(), data.readInt(), IPluginUpdateStateListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        cancelInstall(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IPluginManager {
            public static IPluginManager sDefaultImpl;
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

            @Override // com.huawei.pluginmanager.IPluginManager
            public int queryPluginBasicInfoByCategory(String packageName, List<String> pluginCategorys, IPluginQueryCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStringList(pluginCategorys);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryPluginBasicInfoByCategory(packageName, pluginCategorys, callback);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.pluginmanager.IPluginManager
            public int queryPluginBasicInfoByName(String packageName, List<String> pluginNames, IPluginQueryCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStringList(pluginNames);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryPluginBasicInfoByName(packageName, pluginNames, callback);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.pluginmanager.IPluginManager
            public int queryPluginDetailInfo(String packageName, List<String> pluginNames, IPluginQueryDetailCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStringList(pluginNames);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryPluginDetailInfo(packageName, pluginNames, callback);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.pluginmanager.IPluginManager
            public void registerAutoUpdate(String packageName, boolean isOn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(isOn ? 1 : 0);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerAutoUpdate(packageName, isOn);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.pluginmanager.IPluginManager
            public int startInstall(String packageName, List<String> pluginNames, int flags, IPluginUpdateStateListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStringList(pluginNames);
                    _data.writeInt(flags);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startInstall(packageName, pluginNames, flags, listener);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.pluginmanager.IPluginManager
            public int uninstall(String packageName, List<String> pluginNames, int flags, IPluginUpdateStateListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStringList(pluginNames);
                    _data.writeInt(flags);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().uninstall(packageName, pluginNames, flags, listener);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.pluginmanager.IPluginManager
            public void cancelInstall(String packageName, int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(taskId);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelInstall(packageName, taskId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IPluginManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPluginManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
