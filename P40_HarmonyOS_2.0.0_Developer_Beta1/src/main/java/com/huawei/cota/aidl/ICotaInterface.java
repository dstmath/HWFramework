package com.huawei.cota.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.cota.aidl.ICotaCallBack;

public interface ICotaInterface extends IInterface {
    int getApksInstallStatus() throws RemoteException;

    boolean registerCallBack(ICotaCallBack iCotaCallBack, String str) throws RemoteException;

    void startAutoInstall(String str, String str2, String str3) throws RemoteException;

    void startInstallApks() throws RemoteException;

    boolean unregisterCallBack(ICotaCallBack iCotaCallBack, String str) throws RemoteException;

    public static class Default implements ICotaInterface {
        @Override // com.huawei.cota.aidl.ICotaInterface
        public boolean registerCallBack(ICotaCallBack callback, String packageName) throws RemoteException {
            return false;
        }

        @Override // com.huawei.cota.aidl.ICotaInterface
        public boolean unregisterCallBack(ICotaCallBack callback, String packageName) throws RemoteException {
            return false;
        }

        @Override // com.huawei.cota.aidl.ICotaInterface
        public void startInstallApks() throws RemoteException {
        }

        @Override // com.huawei.cota.aidl.ICotaInterface
        public int getApksInstallStatus() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.cota.aidl.ICotaInterface
        public void startAutoInstall(String apkInstallConfig, String removeableApkInstallConfig, String strMccMnc) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICotaInterface {
        private static final String DESCRIPTOR = "com.huawei.cota.aidl.ICotaInterface";
        static final int TRANSACTION_getApksInstallStatus = 4;
        static final int TRANSACTION_registerCallBack = 1;
        static final int TRANSACTION_startAutoInstall = 5;
        static final int TRANSACTION_startInstallApks = 3;
        static final int TRANSACTION_unregisterCallBack = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICotaInterface asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICotaInterface)) {
                return new Proxy(obj);
            }
            return (ICotaInterface) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean registerCallBack = registerCallBack(ICotaCallBack.Stub.asInterface(data.readStrongBinder()), data.readString());
                reply.writeNoException();
                reply.writeInt(registerCallBack ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                boolean unregisterCallBack = unregisterCallBack(ICotaCallBack.Stub.asInterface(data.readStrongBinder()), data.readString());
                reply.writeNoException();
                reply.writeInt(unregisterCallBack ? 1 : 0);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                startInstallApks();
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                int _result = getApksInstallStatus();
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                startAutoInstall(data.readString(), data.readString(), data.readString());
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
        public static class Proxy implements ICotaInterface {
            public static ICotaInterface sDefaultImpl;
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

            @Override // com.huawei.cota.aidl.ICotaInterface
            public boolean registerCallBack(ICotaCallBack callback, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerCallBack(callback, packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.cota.aidl.ICotaInterface
            public boolean unregisterCallBack(ICotaCallBack callback, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterCallBack(callback, packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.cota.aidl.ICotaInterface
            public void startInstallApks() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startInstallApks();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.cota.aidl.ICotaInterface
            public int getApksInstallStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getApksInstallStatus();
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

            @Override // com.huawei.cota.aidl.ICotaInterface
            public void startAutoInstall(String apkInstallConfig, String removeableApkInstallConfig, String strMccMnc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(apkInstallConfig);
                    _data.writeString(removeableApkInstallConfig);
                    _data.writeString(strMccMnc);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startAutoInstall(apkInstallConfig, removeableApkInstallConfig, strMccMnc);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ICotaInterface impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ICotaInterface getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
