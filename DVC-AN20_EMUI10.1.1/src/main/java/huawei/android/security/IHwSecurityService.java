package huawei.android.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwSecurityService extends IInterface {
    IBinder bind(int i, IBinder iBinder) throws RemoteException;

    IBinder querySecurityInterface(int i) throws RemoteException;

    void unBind(int i, IBinder iBinder) throws RemoteException;

    public static class Default implements IHwSecurityService {
        @Override // huawei.android.security.IHwSecurityService
        public IBinder bind(int pluginId, IBinder client) throws RemoteException {
            return null;
        }

        @Override // huawei.android.security.IHwSecurityService
        public void unBind(int pluginId, IBinder client) throws RemoteException {
        }

        @Override // huawei.android.security.IHwSecurityService
        public IBinder querySecurityInterface(int pluginId) throws RemoteException {
            return null;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwSecurityService {
        private static final String DESCRIPTOR = "huawei.android.security.IHwSecurityService";
        static final int TRANSACTION_bind = 1;
        static final int TRANSACTION_querySecurityInterface = 3;
        static final int TRANSACTION_unBind = 2;

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

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                IBinder _result = bind(data.readInt(), data.readStrongBinder());
                reply.writeNoException();
                reply.writeStrongBinder(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                unBind(data.readInt(), data.readStrongBinder());
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                IBinder _result2 = querySecurityInterface(data.readInt());
                reply.writeNoException();
                reply.writeStrongBinder(_result2);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwSecurityService {
            public static IHwSecurityService sDefaultImpl;
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

            @Override // huawei.android.security.IHwSecurityService
            public IBinder bind(int pluginId, IBinder client) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pluginId);
                    _data.writeStrongBinder(client);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().bind(pluginId, client);
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwSecurityService
            public void unBind(int pluginId, IBinder client) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pluginId);
                    _data.writeStrongBinder(client);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unBind(pluginId, client);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwSecurityService
            public IBinder querySecurityInterface(int pluginId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pluginId);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().querySecurityInterface(pluginId);
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwSecurityService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwSecurityService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
