package android.security.keymaster;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IKeyAttestationApplicationIdProvider extends IInterface {
    KeyAttestationApplicationId getKeyAttestationApplicationId(int i) throws RemoteException;

    public static class Default implements IKeyAttestationApplicationIdProvider {
        @Override // android.security.keymaster.IKeyAttestationApplicationIdProvider
        public KeyAttestationApplicationId getKeyAttestationApplicationId(int uid) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IKeyAttestationApplicationIdProvider {
        private static final String DESCRIPTOR = "android.security.keymaster.IKeyAttestationApplicationIdProvider";
        static final int TRANSACTION_getKeyAttestationApplicationId = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IKeyAttestationApplicationIdProvider asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IKeyAttestationApplicationIdProvider)) {
                return new Proxy(obj);
            }
            return (IKeyAttestationApplicationIdProvider) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "getKeyAttestationApplicationId";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                KeyAttestationApplicationId _result = getKeyAttestationApplicationId(data.readInt());
                reply.writeNoException();
                if (_result != null) {
                    reply.writeInt(1);
                    _result.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IKeyAttestationApplicationIdProvider {
            public static IKeyAttestationApplicationIdProvider sDefaultImpl;
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

            @Override // android.security.keymaster.IKeyAttestationApplicationIdProvider
            public KeyAttestationApplicationId getKeyAttestationApplicationId(int uid) throws RemoteException {
                KeyAttestationApplicationId _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getKeyAttestationApplicationId(uid);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = KeyAttestationApplicationId.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IKeyAttestationApplicationIdProvider impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IKeyAttestationApplicationIdProvider getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
