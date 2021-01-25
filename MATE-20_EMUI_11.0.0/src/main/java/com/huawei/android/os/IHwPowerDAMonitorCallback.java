package com.huawei.android.os;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwPowerDAMonitorCallback extends IInterface {
    boolean isAwarePreventScreenOn(String str, String str2) throws RemoteException;

    void notifyWakeLockReleaseToIAware(int i, int i2, String str, String str2) throws RemoteException;

    void notifyWakeLockToIAware(int i, int i2, String str, String str2) throws RemoteException;

    public static class Default implements IHwPowerDAMonitorCallback {
        @Override // com.huawei.android.os.IHwPowerDAMonitorCallback
        public boolean isAwarePreventScreenOn(String pkgName, String tag) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.os.IHwPowerDAMonitorCallback
        public void notifyWakeLockToIAware(int uid, int pid, String packageName, String Tag) throws RemoteException {
        }

        @Override // com.huawei.android.os.IHwPowerDAMonitorCallback
        public void notifyWakeLockReleaseToIAware(int uid, int pid, String packageName, String Tag) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwPowerDAMonitorCallback {
        private static final String DESCRIPTOR = "com.huawei.android.os.IHwPowerDAMonitorCallback";
        static final int TRANSACTION_isAwarePreventScreenOn = 1;
        static final int TRANSACTION_notifyWakeLockReleaseToIAware = 3;
        static final int TRANSACTION_notifyWakeLockToIAware = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwPowerDAMonitorCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwPowerDAMonitorCallback)) {
                return new Proxy(obj);
            }
            return (IHwPowerDAMonitorCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "isAwarePreventScreenOn";
            }
            if (transactionCode == 2) {
                return "notifyWakeLockToIAware";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "notifyWakeLockReleaseToIAware";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean isAwarePreventScreenOn = isAwarePreventScreenOn(data.readString(), data.readString());
                reply.writeNoException();
                reply.writeInt(isAwarePreventScreenOn ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                notifyWakeLockToIAware(data.readInt(), data.readInt(), data.readString(), data.readString());
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                notifyWakeLockReleaseToIAware(data.readInt(), data.readInt(), data.readString(), data.readString());
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
        public static class Proxy implements IHwPowerDAMonitorCallback {
            public static IHwPowerDAMonitorCallback sDefaultImpl;
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

            @Override // com.huawei.android.os.IHwPowerDAMonitorCallback
            public boolean isAwarePreventScreenOn(String pkgName, String tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeString(tag);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAwarePreventScreenOn(pkgName, tag);
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

            @Override // com.huawei.android.os.IHwPowerDAMonitorCallback
            public void notifyWakeLockToIAware(int uid, int pid, String packageName, String Tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeString(packageName);
                    _data.writeString(Tag);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyWakeLockToIAware(uid, pid, packageName, Tag);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.os.IHwPowerDAMonitorCallback
            public void notifyWakeLockReleaseToIAware(int uid, int pid, String packageName, String Tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeString(packageName);
                    _data.writeString(Tag);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyWakeLockReleaseToIAware(uid, pid, packageName, Tag);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwPowerDAMonitorCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwPowerDAMonitorCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
