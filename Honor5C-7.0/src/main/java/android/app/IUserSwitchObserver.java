package android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.IRemoteCallback;
import android.os.Parcel;
import android.os.RemoteException;

public interface IUserSwitchObserver extends IInterface {

    public static abstract class Stub extends Binder implements IUserSwitchObserver {
        private static final String DESCRIPTOR = "android.app.IUserSwitchObserver";
        static final int TRANSACTION_onForegroundProfileSwitch = 3;
        static final int TRANSACTION_onUserSwitchComplete = 2;
        static final int TRANSACTION_onUserSwitching = 1;

        private static class Proxy implements IUserSwitchObserver {
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

            public void onUserSwitching(int newUserId, IRemoteCallback reply) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(newUserId);
                    if (reply != null) {
                        iBinder = reply.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_onUserSwitching, _data, null, Stub.TRANSACTION_onUserSwitching);
                } finally {
                    _data.recycle();
                }
            }

            public void onUserSwitchComplete(int newUserId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(newUserId);
                    this.mRemote.transact(Stub.TRANSACTION_onUserSwitchComplete, _data, null, Stub.TRANSACTION_onUserSwitching);
                } finally {
                    _data.recycle();
                }
            }

            public void onForegroundProfileSwitch(int newProfileId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(newProfileId);
                    this.mRemote.transact(Stub.TRANSACTION_onForegroundProfileSwitch, _data, null, Stub.TRANSACTION_onUserSwitching);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IUserSwitchObserver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IUserSwitchObserver)) {
                return new Proxy(obj);
            }
            return (IUserSwitchObserver) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_onUserSwitching /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    onUserSwitching(data.readInt(), android.os.IRemoteCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_onUserSwitchComplete /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onUserSwitchComplete(data.readInt());
                    return true;
                case TRANSACTION_onForegroundProfileSwitch /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    onForegroundProfileSwitch(data.readInt());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onForegroundProfileSwitch(int i) throws RemoteException;

    void onUserSwitchComplete(int i) throws RemoteException;

    void onUserSwitching(int i, IRemoteCallback iRemoteCallback) throws RemoteException;
}
