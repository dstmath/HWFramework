package com.huawei.hwouc.plugin;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.hwouc.plugin.ICheckerCallback;
import java.util.Map;

public interface IChecker extends IInterface {
    int checkBaseInfo(Map map, ICheckerCallback iCheckerCallback) throws RemoteException;

    int checkByName(OucPluginInfo oucPluginInfo, ICheckerCallback iCheckerCallback) throws RemoteException;

    public static class Default implements IChecker {
        @Override // com.huawei.hwouc.plugin.IChecker
        public int checkBaseInfo(Map pluginBaseInfoMap, ICheckerCallback callback) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.hwouc.plugin.IChecker
        public int checkByName(OucPluginInfo pluginInfo, ICheckerCallback callback) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IChecker {
        private static final String DESCRIPTOR = "com.huawei.hwouc.plugin.IChecker";
        static final int TRANSACTION_checkBaseInfo = 1;
        static final int TRANSACTION_checkByName = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IChecker asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IChecker)) {
                return new Proxy(obj);
            }
            return (IChecker) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            OucPluginInfo _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = checkBaseInfo(data.readHashMap(getClass().getClassLoader()), ICheckerCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = OucPluginInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                int _result2 = checkByName(_arg0, ICheckerCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IChecker {
            public static IChecker sDefaultImpl;
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

            @Override // com.huawei.hwouc.plugin.IChecker
            public int checkBaseInfo(Map pluginBaseInfoMap, ICheckerCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeMap(pluginBaseInfoMap);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkBaseInfo(pluginBaseInfoMap, callback);
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

            @Override // com.huawei.hwouc.plugin.IChecker
            public int checkByName(OucPluginInfo pluginInfo, ICheckerCallback callback) throws RemoteException {
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
                        return Stub.getDefaultImpl().checkByName(pluginInfo, callback);
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
        }

        public static boolean setDefaultImpl(IChecker impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IChecker getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
