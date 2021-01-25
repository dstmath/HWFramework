package com.huawei.hwdockbar;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IDockAidlInterface extends IInterface {
    void connect(int i) throws RemoteException;

    void dismiss() throws RemoteException;

    void dismissWithAnimation() throws RemoteException;

    boolean isEditState() throws RemoteException;

    public static class Default implements IDockAidlInterface {
        @Override // com.huawei.hwdockbar.IDockAidlInterface
        public void connect(int navId) throws RemoteException {
        }

        @Override // com.huawei.hwdockbar.IDockAidlInterface
        public void dismiss() throws RemoteException {
        }

        @Override // com.huawei.hwdockbar.IDockAidlInterface
        public boolean isEditState() throws RemoteException {
            return false;
        }

        @Override // com.huawei.hwdockbar.IDockAidlInterface
        public void dismissWithAnimation() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IDockAidlInterface {
        private static final String DESCRIPTOR = "com.huawei.hwdockbar.IDockAidlInterface";
        static final int TRANSACTION_connect = 1;
        static final int TRANSACTION_dismiss = 2;
        static final int TRANSACTION_dismissWithAnimation = 4;
        static final int TRANSACTION_isEditState = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDockAidlInterface asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDockAidlInterface)) {
                return new Proxy(obj);
            }
            return (IDockAidlInterface) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                connect(data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                dismiss();
                reply.writeNoException();
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
        public static class Proxy implements IDockAidlInterface {
            public static IDockAidlInterface sDefaultImpl;
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

            @Override // com.huawei.hwdockbar.IDockAidlInterface
            public void connect(int navId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(navId);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().connect(navId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwdockbar.IDockAidlInterface
            public void dismiss() throws RemoteException {
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
                    Stub.getDefaultImpl().dismiss();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hwdockbar.IDockAidlInterface
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

            @Override // com.huawei.hwdockbar.IDockAidlInterface
            public void dismissWithAnimation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().dismissWithAnimation();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IDockAidlInterface impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IDockAidlInterface getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
