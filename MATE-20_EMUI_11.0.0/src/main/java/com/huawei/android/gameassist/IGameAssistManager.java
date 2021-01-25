package com.huawei.android.gameassist;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGameAssistManager extends IInterface {
    void notifyKeyEvent() throws RemoteException;

    void setFlashing(int i, int i2, int i3, int i4) throws RemoteException;

    void turnOff() throws RemoteException;

    public static class Default implements IGameAssistManager {
        @Override // com.huawei.android.gameassist.IGameAssistManager
        public void notifyKeyEvent() throws RemoteException {
        }

        @Override // com.huawei.android.gameassist.IGameAssistManager
        public void setFlashing(int color, int mode, int onMS, int offMS) throws RemoteException {
        }

        @Override // com.huawei.android.gameassist.IGameAssistManager
        public void turnOff() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IGameAssistManager {
        private static final String DESCRIPTOR = "com.huawei.android.gameassist.IGameAssistManager";
        static final int TRANSACTION_notifyKeyEvent = 1;
        static final int TRANSACTION_setFlashing = 2;
        static final int TRANSACTION_turnOff = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGameAssistManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGameAssistManager)) {
                return new Proxy(obj);
            }
            return (IGameAssistManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "notifyKeyEvent";
            }
            if (transactionCode == 2) {
                return "setFlashing";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "turnOff";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                notifyKeyEvent();
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                setFlashing(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                turnOff();
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
        public static class Proxy implements IGameAssistManager {
            public static IGameAssistManager sDefaultImpl;
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

            @Override // com.huawei.android.gameassist.IGameAssistManager
            public void notifyKeyEvent() throws RemoteException {
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
                    Stub.getDefaultImpl().notifyKeyEvent();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.gameassist.IGameAssistManager
            public void setFlashing(int color, int mode, int onMS, int offMS) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(color);
                    _data.writeInt(mode);
                    _data.writeInt(onMS);
                    _data.writeInt(offMS);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFlashing(color, mode, onMS, offMS);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.gameassist.IGameAssistManager
            public void turnOff() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().turnOff();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IGameAssistManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IGameAssistManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
