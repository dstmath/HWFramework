package com.huawei.android.server.clipboard;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.android.content.IOnPrimaryClipGetedListener;

public interface IHwClipboardServiceManager extends IInterface {
    void addPrimaryClipGetedListener(IOnPrimaryClipGetedListener iOnPrimaryClipGetedListener, String str) throws RemoteException;

    void removePrimaryClipGetedListener(IOnPrimaryClipGetedListener iOnPrimaryClipGetedListener) throws RemoteException;

    void setGetWaitTime(int i) throws RemoteException;

    public static class Default implements IHwClipboardServiceManager {
        @Override // com.huawei.android.server.clipboard.IHwClipboardServiceManager
        public void addPrimaryClipGetedListener(IOnPrimaryClipGetedListener listener, String callingPackage) throws RemoteException {
        }

        @Override // com.huawei.android.server.clipboard.IHwClipboardServiceManager
        public void removePrimaryClipGetedListener(IOnPrimaryClipGetedListener listener) throws RemoteException {
        }

        @Override // com.huawei.android.server.clipboard.IHwClipboardServiceManager
        public void setGetWaitTime(int waitTime) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwClipboardServiceManager {
        private static final String DESCRIPTOR = "com.huawei.android.server.clipboard.IHwClipboardServiceManager";
        static final int TRANSACTION_addPrimaryClipGetedListener = 1;
        static final int TRANSACTION_removePrimaryClipGetedListener = 2;
        static final int TRANSACTION_setGetWaitTime = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwClipboardServiceManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwClipboardServiceManager)) {
                return new Proxy(obj);
            }
            return (IHwClipboardServiceManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "addPrimaryClipGetedListener";
            }
            if (transactionCode == 2) {
                return "removePrimaryClipGetedListener";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "setGetWaitTime";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                addPrimaryClipGetedListener(IOnPrimaryClipGetedListener.Stub.asInterface(data.readStrongBinder()), data.readString());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                removePrimaryClipGetedListener(IOnPrimaryClipGetedListener.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                setGetWaitTime(data.readInt());
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
        public static class Proxy implements IHwClipboardServiceManager {
            public static IHwClipboardServiceManager sDefaultImpl;
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

            @Override // com.huawei.android.server.clipboard.IHwClipboardServiceManager
            public void addPrimaryClipGetedListener(IOnPrimaryClipGetedListener listener, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addPrimaryClipGetedListener(listener, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.server.clipboard.IHwClipboardServiceManager
            public void removePrimaryClipGetedListener(IOnPrimaryClipGetedListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removePrimaryClipGetedListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.server.clipboard.IHwClipboardServiceManager
            public void setGetWaitTime(int waitTime) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(waitTime);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setGetWaitTime(waitTime);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwClipboardServiceManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwClipboardServiceManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
