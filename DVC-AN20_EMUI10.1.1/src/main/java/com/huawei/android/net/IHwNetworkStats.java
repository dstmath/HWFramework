package com.huawei.android.net;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwNetworkStats extends IInterface {
    int getPerformPollDelay() throws RemoteException;

    boolean setAlertPeriodType(int i) throws RemoteException;

    public static class Default implements IHwNetworkStats {
        @Override // com.huawei.android.net.IHwNetworkStats
        public boolean setAlertPeriodType(int period) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.net.IHwNetworkStats
        public int getPerformPollDelay() throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwNetworkStats {
        private static final String DESCRIPTOR = "com.huawei.android.net.IHwNetworkStats";
        static final int TRANSACTION_getPerformPollDelay = 2;
        static final int TRANSACTION_setAlertPeriodType = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwNetworkStats asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwNetworkStats)) {
                return new Proxy(obj);
            }
            return (IHwNetworkStats) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "setAlertPeriodType";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "getPerformPollDelay";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean alertPeriodType = setAlertPeriodType(data.readInt());
                reply.writeNoException();
                reply.writeInt(alertPeriodType ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _result = getPerformPollDelay();
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwNetworkStats {
            public static IHwNetworkStats sDefaultImpl;
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

            @Override // com.huawei.android.net.IHwNetworkStats
            public boolean setAlertPeriodType(int period) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(period);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setAlertPeriodType(period);
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

            @Override // com.huawei.android.net.IHwNetworkStats
            public int getPerformPollDelay() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPerformPollDelay();
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

        public static boolean setDefaultImpl(IHwNetworkStats impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwNetworkStats getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
