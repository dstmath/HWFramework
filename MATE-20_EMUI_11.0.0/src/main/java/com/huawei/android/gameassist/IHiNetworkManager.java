package com.huawei.android.gameassist;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHiNetworkManager extends IInterface {
    int onDetectTimeDelayResult(String str) throws RemoteException;

    int onOpenAccelerateResult(String str) throws RemoteException;

    public static class Default implements IHiNetworkManager {
        @Override // com.huawei.android.gameassist.IHiNetworkManager
        public int onOpenAccelerateResult(String acceletrateResult) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.gameassist.IHiNetworkManager
        public int onDetectTimeDelayResult(String timeDelayResult) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHiNetworkManager {
        private static final String DESCRIPTOR = "com.huawei.android.gameassist.IHiNetworkManager";
        static final int TRANSACTION_onDetectTimeDelayResult = 2;
        static final int TRANSACTION_onOpenAccelerateResult = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHiNetworkManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHiNetworkManager)) {
                return new Proxy(obj);
            }
            return (IHiNetworkManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onOpenAccelerateResult";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "onDetectTimeDelayResult";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = onOpenAccelerateResult(data.readString());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _result2 = onDetectTimeDelayResult(data.readString());
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
        public static class Proxy implements IHiNetworkManager {
            public static IHiNetworkManager sDefaultImpl;
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

            @Override // com.huawei.android.gameassist.IHiNetworkManager
            public int onOpenAccelerateResult(String acceletrateResult) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(acceletrateResult);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onOpenAccelerateResult(acceletrateResult);
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

            @Override // com.huawei.android.gameassist.IHiNetworkManager
            public int onDetectTimeDelayResult(String timeDelayResult) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(timeDelayResult);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onDetectTimeDelayResult(timeDelayResult);
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

        public static boolean setDefaultImpl(IHiNetworkManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHiNetworkManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
