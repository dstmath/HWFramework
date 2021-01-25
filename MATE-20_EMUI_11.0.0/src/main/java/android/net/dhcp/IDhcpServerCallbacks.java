package android.net.dhcp;

import android.net.dhcp.IDhcpServer;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IDhcpServerCallbacks extends IInterface {
    public static final int VERSION = 3;

    int getInterfaceVersion() throws RemoteException;

    void onDhcpServerCreated(int i, IDhcpServer iDhcpServer) throws RemoteException;

    public static class Default implements IDhcpServerCallbacks {
        @Override // android.net.dhcp.IDhcpServerCallbacks
        public void onDhcpServerCreated(int statusCode, IDhcpServer server) throws RemoteException {
        }

        @Override // android.net.dhcp.IDhcpServerCallbacks
        public int getInterfaceVersion() {
            return -1;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IDhcpServerCallbacks {
        private static final String DESCRIPTOR = "android.net.dhcp.IDhcpServerCallbacks";
        static final int TRANSACTION_getInterfaceVersion = 16777215;
        static final int TRANSACTION_onDhcpServerCreated = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDhcpServerCallbacks asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDhcpServerCallbacks)) {
                return new Proxy(obj);
            }
            return (IDhcpServerCallbacks) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onDhcpServerCreated(data.readInt(), IDhcpServer.Stub.asInterface(data.readStrongBinder()));
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
        public static class Proxy implements IDhcpServerCallbacks {
            public static IDhcpServerCallbacks sDefaultImpl;
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

            @Override // android.net.dhcp.IDhcpServerCallbacks
            public void onDhcpServerCreated(int statusCode, IDhcpServer server) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(statusCode);
                    _data.writeStrongBinder(server != null ? server.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDhcpServerCreated(statusCode, server);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.dhcp.IDhcpServerCallbacks
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

        public static boolean setDefaultImpl(IDhcpServerCallbacks impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IDhcpServerCallbacks getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
