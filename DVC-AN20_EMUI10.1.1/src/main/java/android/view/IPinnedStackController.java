package android.view;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPinnedStackController extends IInterface {
    int getDisplayRotation() throws RemoteException;

    void setIsMinimized(boolean z) throws RemoteException;

    void setMinEdgeSize(int i) throws RemoteException;

    public static class Default implements IPinnedStackController {
        @Override // android.view.IPinnedStackController
        public void setIsMinimized(boolean isMinimized) throws RemoteException {
        }

        @Override // android.view.IPinnedStackController
        public void setMinEdgeSize(int minEdgeSize) throws RemoteException {
        }

        @Override // android.view.IPinnedStackController
        public int getDisplayRotation() throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IPinnedStackController {
        private static final String DESCRIPTOR = "android.view.IPinnedStackController";
        static final int TRANSACTION_getDisplayRotation = 3;
        static final int TRANSACTION_setIsMinimized = 1;
        static final int TRANSACTION_setMinEdgeSize = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPinnedStackController asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPinnedStackController)) {
                return new Proxy(obj);
            }
            return (IPinnedStackController) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "setIsMinimized";
            }
            if (transactionCode == 2) {
                return "setMinEdgeSize";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "getDisplayRotation";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                setIsMinimized(data.readInt() != 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                setMinEdgeSize(data.readInt());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _result = getDisplayRotation();
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IPinnedStackController {
            public static IPinnedStackController sDefaultImpl;
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

            @Override // android.view.IPinnedStackController
            public void setIsMinimized(boolean isMinimized) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isMinimized ? 1 : 0);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setIsMinimized(isMinimized);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IPinnedStackController
            public void setMinEdgeSize(int minEdgeSize) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(minEdgeSize);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setMinEdgeSize(minEdgeSize);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.view.IPinnedStackController
            public int getDisplayRotation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDisplayRotation();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IPinnedStackController impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPinnedStackController getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
