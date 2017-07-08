package android.service.quicksettings;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IQSTileService extends IInterface {

    public static abstract class Stub extends Binder implements IQSTileService {
        private static final String DESCRIPTOR = "android.service.quicksettings.IQSTileService";
        static final int TRANSACTION_onClick = 5;
        static final int TRANSACTION_onStartListening = 3;
        static final int TRANSACTION_onStopListening = 4;
        static final int TRANSACTION_onTileAdded = 1;
        static final int TRANSACTION_onTileRemoved = 2;
        static final int TRANSACTION_onUnlockComplete = 6;

        private static class Proxy implements IQSTileService {
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

            public void onTileAdded() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onTileAdded, _data, null, Stub.TRANSACTION_onTileAdded);
                } finally {
                    _data.recycle();
                }
            }

            public void onTileRemoved() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onTileRemoved, _data, null, Stub.TRANSACTION_onTileAdded);
                } finally {
                    _data.recycle();
                }
            }

            public void onStartListening() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onStartListening, _data, null, Stub.TRANSACTION_onTileAdded);
                } finally {
                    _data.recycle();
                }
            }

            public void onStopListening() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onStopListening, _data, null, Stub.TRANSACTION_onTileAdded);
                } finally {
                    _data.recycle();
                }
            }

            public void onClick(IBinder wtoken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(wtoken);
                    this.mRemote.transact(Stub.TRANSACTION_onClick, _data, null, Stub.TRANSACTION_onTileAdded);
                } finally {
                    _data.recycle();
                }
            }

            public void onUnlockComplete() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onUnlockComplete, _data, null, Stub.TRANSACTION_onTileAdded);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IQSTileService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IQSTileService)) {
                return new Proxy(obj);
            }
            return (IQSTileService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_onTileAdded /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    onTileAdded();
                    return true;
                case TRANSACTION_onTileRemoved /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onTileRemoved();
                    return true;
                case TRANSACTION_onStartListening /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    onStartListening();
                    return true;
                case TRANSACTION_onStopListening /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    onStopListening();
                    return true;
                case TRANSACTION_onClick /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    onClick(data.readStrongBinder());
                    return true;
                case TRANSACTION_onUnlockComplete /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    onUnlockComplete();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onClick(IBinder iBinder) throws RemoteException;

    void onStartListening() throws RemoteException;

    void onStopListening() throws RemoteException;

    void onTileAdded() throws RemoteException;

    void onTileRemoved() throws RemoteException;

    void onUnlockComplete() throws RemoteException;
}
