package android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IUidObserver extends IInterface {

    public static abstract class Stub extends Binder implements IUidObserver {
        private static final String DESCRIPTOR = "android.app.IUidObserver";
        static final int TRANSACTION_onUidActive = 2;
        static final int TRANSACTION_onUidCachedChanged = 5;
        static final int TRANSACTION_onUidGone = 1;
        static final int TRANSACTION_onUidIdle = 3;
        static final int TRANSACTION_onUidStateChanged = 4;

        private static class Proxy implements IUidObserver {
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

            public void onUidGone(int uid, boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(disabled);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onUidActive(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onUidIdle(int uid, boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(disabled);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onUidStateChanged(int uid, int procState, long procStateSeq) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(procState);
                    _data.writeLong(procStateSeq);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onUidCachedChanged(int uid, boolean cached) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(cached);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IUidObserver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IUidObserver)) {
                return new Proxy(obj);
            }
            return (IUidObserver) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                boolean _arg1 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        onUidGone(_arg0, _arg1);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onUidActive(data.readInt());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        onUidIdle(_arg02, _arg1);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onUidStateChanged(data.readInt(), data.readInt(), data.readLong());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        onUidCachedChanged(_arg03, _arg1);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void onUidActive(int i) throws RemoteException;

    void onUidCachedChanged(int i, boolean z) throws RemoteException;

    void onUidGone(int i, boolean z) throws RemoteException;

    void onUidIdle(int i, boolean z) throws RemoteException;

    void onUidStateChanged(int i, int i2, long j) throws RemoteException;
}
