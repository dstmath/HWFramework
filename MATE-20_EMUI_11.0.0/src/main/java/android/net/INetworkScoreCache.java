package android.net;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface INetworkScoreCache extends IInterface {
    void clearScores() throws RemoteException;

    void updateScores(List<ScoredNetwork> list) throws RemoteException;

    public static class Default implements INetworkScoreCache {
        @Override // android.net.INetworkScoreCache
        public void updateScores(List<ScoredNetwork> list) throws RemoteException {
        }

        @Override // android.net.INetworkScoreCache
        public void clearScores() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INetworkScoreCache {
        private static final String DESCRIPTOR = "android.net.INetworkScoreCache";
        static final int TRANSACTION_clearScores = 2;
        static final int TRANSACTION_updateScores = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INetworkScoreCache asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INetworkScoreCache)) {
                return new Proxy(obj);
            }
            return (INetworkScoreCache) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "updateScores";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "clearScores";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                updateScores(data.createTypedArrayList(ScoredNetwork.CREATOR));
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                clearScores();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements INetworkScoreCache {
            public static INetworkScoreCache sDefaultImpl;
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

            @Override // android.net.INetworkScoreCache
            public void updateScores(List<ScoredNetwork> networks) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(networks);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().updateScores(networks);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkScoreCache
            public void clearScores() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().clearScores();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(INetworkScoreCache impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INetworkScoreCache getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
