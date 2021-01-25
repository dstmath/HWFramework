package android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwDockCallBack extends IInterface {
    public static final int REGISTER_DOCK_CALLBACK = 0;
    public static final int UNREGISTER_DOCK_CALLBACK = 1;

    void connect(int i) throws RemoteException;

    void dismiss() throws RemoteException;

    void dismissWithAnimation() throws RemoteException;

    boolean isEditState() throws RemoteException;

    public static class Default implements IHwDockCallBack {
        @Override // android.app.IHwDockCallBack
        public void connect(int navId) throws RemoteException {
        }

        @Override // android.app.IHwDockCallBack
        public void dismiss() throws RemoteException {
        }

        @Override // android.app.IHwDockCallBack
        public boolean isEditState() throws RemoteException {
            return false;
        }

        @Override // android.app.IHwDockCallBack
        public void dismissWithAnimation() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwDockCallBack {
        private static final String DESCRIPTOR = "android.app.IHwDockCallBack";
        static final int TRANSACTION_connect = 1;
        static final int TRANSACTION_dismiss = 2;
        static final int TRANSACTION_dismissWithAnimation = 4;
        static final int TRANSACTION_isEditState = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwDockCallBack asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwDockCallBack)) {
                return new Proxy(obj);
            }
            return (IHwDockCallBack) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "connect";
            }
            if (transactionCode == 2) {
                return "dismiss";
            }
            if (transactionCode == 3) {
                return "isEditState";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "dismissWithAnimation";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                connect(data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                dismiss();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                boolean isEditState = isEditState();
                reply.writeNoException();
                reply.writeInt(isEditState ? 1 : 0);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                dismissWithAnimation();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwDockCallBack {
            public static IHwDockCallBack sDefaultImpl;
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

            @Override // android.app.IHwDockCallBack
            public void connect(int navId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(navId);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().connect(navId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IHwDockCallBack
            public void dismiss() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dismiss();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IHwDockCallBack
            public boolean isEditState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isEditState();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IHwDockCallBack
            public void dismissWithAnimation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dismissWithAnimation();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwDockCallBack impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwDockCallBack getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
