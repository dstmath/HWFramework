package android.net.dhcp;

import android.net.INetworkStackStatusCallback;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IDhcpServer extends IInterface {
    public static final int STATUS_INVALID_ARGUMENT = 2;
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_UNKNOWN = 0;
    public static final int STATUS_UNKNOWN_ERROR = 3;
    public static final int VERSION = 3;

    int getInterfaceVersion() throws RemoteException;

    void start(INetworkStackStatusCallback iNetworkStackStatusCallback) throws RemoteException;

    void stop(INetworkStackStatusCallback iNetworkStackStatusCallback) throws RemoteException;

    void updateParams(DhcpServingParamsParcel dhcpServingParamsParcel, INetworkStackStatusCallback iNetworkStackStatusCallback) throws RemoteException;

    public static class Default implements IDhcpServer {
        @Override // android.net.dhcp.IDhcpServer
        public void start(INetworkStackStatusCallback cb) throws RemoteException {
        }

        @Override // android.net.dhcp.IDhcpServer
        public void updateParams(DhcpServingParamsParcel params, INetworkStackStatusCallback cb) throws RemoteException {
        }

        @Override // android.net.dhcp.IDhcpServer
        public void stop(INetworkStackStatusCallback cb) throws RemoteException {
        }

        @Override // android.net.dhcp.IDhcpServer
        public int getInterfaceVersion() {
            return -1;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IDhcpServer {
        private static final String DESCRIPTOR = "android.net.dhcp.IDhcpServer";
        static final int TRANSACTION_getInterfaceVersion = 16777215;
        static final int TRANSACTION_start = 1;
        static final int TRANSACTION_stop = 3;
        static final int TRANSACTION_updateParams = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDhcpServer asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDhcpServer)) {
                return new Proxy(obj);
            }
            return (IDhcpServer) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            DhcpServingParamsParcel _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                start(INetworkStackStatusCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = DhcpServingParamsParcel.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                updateParams(_arg0, INetworkStackStatusCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                stop(INetworkStackStatusCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == TRANSACTION_getInterfaceVersion) {
                data.enforceInterface(DESCRIPTOR);
                reply.writeNoException();
                reply.writeInt(getInterfaceVersion());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IDhcpServer {
            public static IDhcpServer sDefaultImpl;
            private int mCachedVersion = -1;
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

            @Override // android.net.dhcp.IDhcpServer
            public void start(INetworkStackStatusCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().start(cb);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.dhcp.IDhcpServer
            public void updateParams(DhcpServingParamsParcel params, INetworkStackStatusCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().updateParams(params, cb);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.dhcp.IDhcpServer
            public void stop(INetworkStackStatusCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().stop(cb);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.dhcp.IDhcpServer
            public int getInterfaceVersion() throws RemoteException {
                if (this.mCachedVersion == -1) {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    try {
                        data.writeInterfaceToken(Stub.DESCRIPTOR);
                        this.mRemote.transact(Stub.TRANSACTION_getInterfaceVersion, data, reply, 0);
                        reply.readException();
                        this.mCachedVersion = reply.readInt();
                    } finally {
                        reply.recycle();
                        data.recycle();
                    }
                }
                return this.mCachedVersion;
            }
        }

        public static boolean setDefaultImpl(IDhcpServer impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IDhcpServer getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
