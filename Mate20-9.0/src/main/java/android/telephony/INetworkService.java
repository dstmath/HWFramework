package android.telephony;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.INetworkServiceCallback;

public interface INetworkService extends IInterface {

    public static abstract class Stub extends Binder implements INetworkService {
        private static final String DESCRIPTOR = "android.telephony.INetworkService";
        static final int TRANSACTION_createNetworkServiceProvider = 1;
        static final int TRANSACTION_getNetworkRegistrationState = 3;
        static final int TRANSACTION_registerForNetworkRegistrationStateChanged = 4;
        static final int TRANSACTION_removeNetworkServiceProvider = 2;
        static final int TRANSACTION_unregisterForNetworkRegistrationStateChanged = 5;

        private static class Proxy implements INetworkService {
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

            public void createNetworkServiceProvider(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void removeNetworkServiceProvider(int slotId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void getNetworkRegistrationState(int slotId, int domain, INetworkServiceCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(domain);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void registerForNetworkRegistrationStateChanged(int slotId, INetworkServiceCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void unregisterForNetworkRegistrationStateChanged(int slotId, INetworkServiceCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INetworkService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INetworkService)) {
                return new Proxy(obj);
            }
            return (INetworkService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        createNetworkServiceProvider(data.readInt());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        removeNetworkServiceProvider(data.readInt());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        getNetworkRegistrationState(data.readInt(), data.readInt(), INetworkServiceCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        registerForNetworkRegistrationStateChanged(data.readInt(), INetworkServiceCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterForNetworkRegistrationStateChanged(data.readInt(), INetworkServiceCallback.Stub.asInterface(data.readStrongBinder()));
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

    void createNetworkServiceProvider(int i) throws RemoteException;

    void getNetworkRegistrationState(int i, int i2, INetworkServiceCallback iNetworkServiceCallback) throws RemoteException;

    void registerForNetworkRegistrationStateChanged(int i, INetworkServiceCallback iNetworkServiceCallback) throws RemoteException;

    void removeNetworkServiceProvider(int i) throws RemoteException;

    void unregisterForNetworkRegistrationStateChanged(int i, INetworkServiceCallback iNetworkServiceCallback) throws RemoteException;
}
