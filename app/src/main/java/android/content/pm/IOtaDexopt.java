package android.content.pm;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOtaDexopt extends IInterface {

    public static abstract class Stub extends Binder implements IOtaDexopt {
        private static final String DESCRIPTOR = "android.content.pm.IOtaDexopt";
        static final int TRANSACTION_cleanup = 2;
        static final int TRANSACTION_dexoptNextPackage = 4;
        static final int TRANSACTION_isDone = 3;
        static final int TRANSACTION_prepare = 1;

        private static class Proxy implements IOtaDexopt {
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

            public void prepare() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_prepare, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cleanup() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_cleanup, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isDone() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isDone, _data, _reply, 0);
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

            public void dexoptNextPackage() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_dexoptNextPackage, _data, _reply, 0);
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

        public static IOtaDexopt asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOtaDexopt)) {
                return new Proxy(obj);
            }
            return (IOtaDexopt) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_prepare /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    prepare();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_cleanup /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    cleanup();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isDone /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = isDone();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_prepare : 0);
                    return true;
                case TRANSACTION_dexoptNextPackage /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    dexoptNextPackage();
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

    void cleanup() throws RemoteException;

    void dexoptNextPackage() throws RemoteException;

    boolean isDone() throws RemoteException;

    void prepare() throws RemoteException;
}
