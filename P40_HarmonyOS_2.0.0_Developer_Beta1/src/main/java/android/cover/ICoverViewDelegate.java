package android.cover;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICoverViewDelegate extends IInterface {
    void addCoverScreenWindow() throws RemoteException;

    void removeCoverScreenWindow() throws RemoteException;

    public static class Default implements ICoverViewDelegate {
        @Override // android.cover.ICoverViewDelegate
        public void addCoverScreenWindow() throws RemoteException {
        }

        @Override // android.cover.ICoverViewDelegate
        public void removeCoverScreenWindow() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICoverViewDelegate {
        private static final String DESCRIPTOR = "android.cover.ICoverViewDelegate";
        static final int TRANSACTION_addCoverScreenWindow = 1;
        static final int TRANSACTION_removeCoverScreenWindow = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICoverViewDelegate asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICoverViewDelegate)) {
                return new Proxy(obj);
            }
            return (ICoverViewDelegate) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                addCoverScreenWindow();
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                removeCoverScreenWindow();
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ICoverViewDelegate {
            public static ICoverViewDelegate sDefaultImpl;
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

            @Override // android.cover.ICoverViewDelegate
            public void addCoverScreenWindow() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addCoverScreenWindow();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.cover.ICoverViewDelegate
            public void removeCoverScreenWindow() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeCoverScreenWindow();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ICoverViewDelegate impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ICoverViewDelegate getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
