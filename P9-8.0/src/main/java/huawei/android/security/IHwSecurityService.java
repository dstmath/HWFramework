package huawei.android.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwSecurityService extends IInterface {

    public static abstract class Stub extends Binder implements IHwSecurityService {
        private static final String DESCRIPTOR = "huawei.android.security.IHwSecurityService";
        static final int TRANSACTION_bind = 1;
        static final int TRANSACTION_querySecurityInterface = 3;
        static final int TRANSACTION_unBind = 2;

        private static class Proxy implements IHwSecurityService {
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

            public IBinder bind(int pluginId, IBinder client) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pluginId);
                    _data.writeStrongBinder(client);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unBind(int pluginId, IBinder client) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pluginId);
                    _data.writeStrongBinder(client);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IBinder querySecurityInterface(int pluginId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pluginId);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwSecurityService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwSecurityService)) {
                return new Proxy(obj);
            }
            return (IHwSecurityService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IBinder _result;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _result = bind(data.readInt(), data.readStrongBinder());
                    reply.writeNoException();
                    reply.writeStrongBinder(_result);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    unBind(data.readInt(), data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _result = querySecurityInterface(data.readInt());
                    reply.writeNoException();
                    reply.writeStrongBinder(_result);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    IBinder bind(int i, IBinder iBinder) throws RemoteException;

    IBinder querySecurityInterface(int i) throws RemoteException;

    void unBind(int i, IBinder iBinder) throws RemoteException;
}
