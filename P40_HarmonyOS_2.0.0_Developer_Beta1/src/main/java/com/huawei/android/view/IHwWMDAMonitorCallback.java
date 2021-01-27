package com.huawei.android.view;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwWMDAMonitorCallback extends IInterface {
    boolean isResourceNeeded(String str) throws RemoteException;

    void reportData(String str, long j, Bundle bundle) throws RemoteException;

    public static class Default implements IHwWMDAMonitorCallback {
        @Override // com.huawei.android.view.IHwWMDAMonitorCallback
        public boolean isResourceNeeded(String resourceid) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.view.IHwWMDAMonitorCallback
        public void reportData(String resourceid, long timestamp, Bundle args) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwWMDAMonitorCallback {
        private static final String DESCRIPTOR = "com.huawei.android.view.IHwWMDAMonitorCallback";
        static final int TRANSACTION_isResourceNeeded = 1;
        static final int TRANSACTION_reportData = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwWMDAMonitorCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwWMDAMonitorCallback)) {
                return new Proxy(obj);
            }
            return (IHwWMDAMonitorCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "isResourceNeeded";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "reportData";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg2;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean isResourceNeeded = isResourceNeeded(data.readString());
                reply.writeNoException();
                reply.writeInt(isResourceNeeded ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                String _arg0 = data.readString();
                long _arg1 = data.readLong();
                if (data.readInt() != 0) {
                    _arg2 = Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                reportData(_arg0, _arg1, _arg2);
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
        public static class Proxy implements IHwWMDAMonitorCallback {
            public static IHwWMDAMonitorCallback sDefaultImpl;
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

            @Override // com.huawei.android.view.IHwWMDAMonitorCallback
            public boolean isResourceNeeded(String resourceid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(resourceid);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isResourceNeeded(resourceid);
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

            @Override // com.huawei.android.view.IHwWMDAMonitorCallback
            public void reportData(String resourceid, long timestamp, Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(resourceid);
                    _data.writeLong(timestamp);
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportData(resourceid, timestamp, args);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwWMDAMonitorCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwWMDAMonitorCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
