package android.view;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IInputMonitorHost extends IInterface {
    void dispose() throws RemoteException;

    void pilferPointers() throws RemoteException;

    public static class Default implements IInputMonitorHost {
        @Override // android.view.IInputMonitorHost
        public void pilferPointers() throws RemoteException {
        }

        @Override // android.view.IInputMonitorHost
        public void dispose() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IInputMonitorHost {
        private static final String DESCRIPTOR = "android.view.IInputMonitorHost";
        static final int TRANSACTION_dispose = 2;
        static final int TRANSACTION_pilferPointers = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IInputMonitorHost asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IInputMonitorHost)) {
                return new Proxy(obj);
            }
            return (IInputMonitorHost) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "pilferPointers";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "dispose";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                pilferPointers();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                dispose();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IInputMonitorHost {
            public static IInputMonitorHost sDefaultImpl;
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

            @Override // android.view.IInputMonitorHost
            public void pilferPointers() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().pilferPointers();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IInputMonitorHost
            public void dispose() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dispose();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IInputMonitorHost impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IInputMonitorHost getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
