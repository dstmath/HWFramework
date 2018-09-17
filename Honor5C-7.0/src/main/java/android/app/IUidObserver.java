package android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IUidObserver extends IInterface {

    public static abstract class Stub extends Binder implements IUidObserver {
        private static final String DESCRIPTOR = "android.app.IUidObserver";
        static final int TRANSACTION_onUidActive = 3;
        static final int TRANSACTION_onUidGone = 2;
        static final int TRANSACTION_onUidIdle = 4;
        static final int TRANSACTION_onUidStateChanged = 1;

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

            public void onUidStateChanged(int uid, int procState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(procState);
                    this.mRemote.transact(Stub.TRANSACTION_onUidStateChanged, _data, null, Stub.TRANSACTION_onUidStateChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onUidGone(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_onUidGone, _data, null, Stub.TRANSACTION_onUidStateChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onUidActive(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_onUidActive, _data, null, Stub.TRANSACTION_onUidStateChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void onUidIdle(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_onUidIdle, _data, null, Stub.TRANSACTION_onUidStateChanged);
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
            switch (code) {
                case TRANSACTION_onUidStateChanged /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    onUidStateChanged(data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_onUidGone /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onUidGone(data.readInt());
                    return true;
                case TRANSACTION_onUidActive /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    onUidActive(data.readInt());
                    return true;
                case TRANSACTION_onUidIdle /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    onUidIdle(data.readInt());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onUidActive(int i) throws RemoteException;

    void onUidGone(int i) throws RemoteException;

    void onUidIdle(int i) throws RemoteException;

    void onUidStateChanged(int i, int i2) throws RemoteException;
}
