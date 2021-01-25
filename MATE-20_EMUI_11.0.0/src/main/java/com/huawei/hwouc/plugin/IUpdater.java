package com.huawei.hwouc.plugin;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.hwouc.plugin.IUpdaterCallback;

public interface IUpdater extends IInterface {
    void cancel(int i) throws RemoteException;

    void cancelAll() throws RemoteException;

    int install(OucPluginInfo oucPluginInfo, IUpdaterCallback iUpdaterCallback) throws RemoteException;

    int unInstall(OucPluginInfo oucPluginInfo, IUpdaterCallback iUpdaterCallback) throws RemoteException;

    public static class Default implements IUpdater {
        @Override // com.huawei.hwouc.plugin.IUpdater
        public int install(OucPluginInfo pluginInfo, IUpdaterCallback callback) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.hwouc.plugin.IUpdater
        public int unInstall(OucPluginInfo pluginInfo, IUpdaterCallback callback) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.hwouc.plugin.IUpdater
        public void cancel(int taskId) throws RemoteException {
        }

        @Override // com.huawei.hwouc.plugin.IUpdater
        public void cancelAll() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IUpdater {
        private static final String DESCRIPTOR = "com.huawei.hwouc.plugin.IUpdater";
        static final int TRANSACTION_cancel = 3;
        static final int TRANSACTION_cancelAll = 4;
        static final int TRANSACTION_install = 1;
        static final int TRANSACTION_unInstall = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IUpdater asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IUpdater)) {
                return new Proxy(obj);
            }
            return (IUpdater) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            OucPluginInfo _arg0;
            OucPluginInfo _arg02;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = OucPluginInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                int _result = install(_arg0, IUpdaterCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = OucPluginInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                int _result2 = unInstall(_arg02, IUpdaterCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                cancel(data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                cancelAll();
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
        public static class Proxy implements IUpdater {
            public static IUpdater sDefaultImpl;
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

            @Override // com.huawei.hwouc.plugin.IUpdater
            public int install(OucPluginInfo pluginInfo, IUpdaterCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (pluginInfo != null) {
                        _data.writeInt(1);
                        pluginInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().install(pluginInfo, callback);
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

            @Override // com.huawei.hwouc.plugin.IUpdater
            public int unInstall(OucPluginInfo pluginInfo, IUpdaterCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (pluginInfo != null) {
                        _data.writeInt(1);
                        pluginInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unInstall(pluginInfo, callback);
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

            @Override // com.huawei.hwouc.plugin.IUpdater
            public void cancel(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancel(taskId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwouc.plugin.IUpdater
            public void cancelAll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelAll();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IUpdater impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IUpdater getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
