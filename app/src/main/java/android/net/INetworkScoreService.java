package android.net;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INetworkScoreService extends IInterface {

    public static abstract class Stub extends Binder implements INetworkScoreService {
        private static final String DESCRIPTOR = "android.net.INetworkScoreService";
        static final int TRANSACTION_clearScores = 2;
        static final int TRANSACTION_disableScoring = 4;
        static final int TRANSACTION_registerNetworkScoreCache = 5;
        static final int TRANSACTION_setActiveScorer = 3;
        static final int TRANSACTION_updateScores = 1;

        private static class Proxy implements INetworkScoreService {
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

            public boolean updateScores(ScoredNetwork[] networks) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(networks, 0);
                    this.mRemote.transact(Stub.TRANSACTION_updateScores, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean clearScores() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_clearScores, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setActiveScorer(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_setActiveScorer, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disableScoring() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_disableScoring, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerNetworkScoreCache(int networkType, INetworkScoreCache scoreCache) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(networkType);
                    if (scoreCache != null) {
                        iBinder = scoreCache.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerNetworkScoreCache, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INetworkScoreService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INetworkScoreService)) {
                return new Proxy(obj);
            }
            return (INetworkScoreService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            boolean _result;
            switch (code) {
                case TRANSACTION_updateScores /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = updateScores((ScoredNetwork[]) data.createTypedArray(ScoredNetwork.CREATOR));
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_updateScores;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_clearScores /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = clearScores();
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_updateScores;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_setActiveScorer /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setActiveScorer(data.readString());
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_updateScores;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_disableScoring /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    disableScoring();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registerNetworkScoreCache /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    registerNetworkScoreCache(data.readInt(), android.net.INetworkScoreCache.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean clearScores() throws RemoteException;

    void disableScoring() throws RemoteException;

    void registerNetworkScoreCache(int i, INetworkScoreCache iNetworkScoreCache) throws RemoteException;

    boolean setActiveScorer(String str) throws RemoteException;

    boolean updateScores(ScoredNetwork[] scoredNetworkArr) throws RemoteException;
}
