package android.service.attention;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.service.attention.IAttentionCallback;

public interface IAttentionService extends IInterface {
    void cancelAttentionCheck(IAttentionCallback iAttentionCallback) throws RemoteException;

    void checkAttention(IAttentionCallback iAttentionCallback) throws RemoteException;

    public static class Default implements IAttentionService {
        @Override // android.service.attention.IAttentionService
        public void checkAttention(IAttentionCallback callback) throws RemoteException {
        }

        @Override // android.service.attention.IAttentionService
        public void cancelAttentionCheck(IAttentionCallback callback) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAttentionService {
        private static final String DESCRIPTOR = "android.service.attention.IAttentionService";
        static final int TRANSACTION_cancelAttentionCheck = 2;
        static final int TRANSACTION_checkAttention = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAttentionService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAttentionService)) {
                return new Proxy(obj);
            }
            return (IAttentionService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "checkAttention";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "cancelAttentionCheck";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                checkAttention(IAttentionCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                cancelAttentionCheck(IAttentionCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IAttentionService {
            public static IAttentionService sDefaultImpl;
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

            @Override // android.service.attention.IAttentionService
            public void checkAttention(IAttentionCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().checkAttention(callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.attention.IAttentionService
            public void cancelAttentionCheck(IAttentionCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().cancelAttentionCheck(callback);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAttentionService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAttentionService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
