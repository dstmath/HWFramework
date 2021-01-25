package android.print;

import android.os.Binder;
import android.os.IBinder;
import android.os.ICancellationSignal;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;

public interface IWriteResultCallback extends IInterface {
    void onWriteCanceled(int i) throws RemoteException;

    void onWriteFailed(CharSequence charSequence, int i) throws RemoteException;

    void onWriteFinished(PageRange[] pageRangeArr, int i) throws RemoteException;

    void onWriteStarted(ICancellationSignal iCancellationSignal, int i) throws RemoteException;

    public static class Default implements IWriteResultCallback {
        @Override // android.print.IWriteResultCallback
        public void onWriteStarted(ICancellationSignal cancellation, int sequence) throws RemoteException {
        }

        @Override // android.print.IWriteResultCallback
        public void onWriteFinished(PageRange[] pages, int sequence) throws RemoteException {
        }

        @Override // android.print.IWriteResultCallback
        public void onWriteFailed(CharSequence error, int sequence) throws RemoteException {
        }

        @Override // android.print.IWriteResultCallback
        public void onWriteCanceled(int sequence) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IWriteResultCallback {
        private static final String DESCRIPTOR = "android.print.IWriteResultCallback";
        static final int TRANSACTION_onWriteCanceled = 4;
        static final int TRANSACTION_onWriteFailed = 3;
        static final int TRANSACTION_onWriteFinished = 2;
        static final int TRANSACTION_onWriteStarted = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWriteResultCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWriteResultCallback)) {
                return new Proxy(obj);
            }
            return (IWriteResultCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onWriteStarted";
            }
            if (transactionCode == 2) {
                return "onWriteFinished";
            }
            if (transactionCode == 3) {
                return "onWriteFailed";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "onWriteCanceled";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            CharSequence _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onWriteStarted(ICancellationSignal.Stub.asInterface(data.readStrongBinder()), data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onWriteFinished((PageRange[]) data.createTypedArray(PageRange.CREATOR), data.readInt());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onWriteFailed(_arg0, data.readInt());
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                onWriteCanceled(data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IWriteResultCallback {
            public static IWriteResultCallback sDefaultImpl;
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

            @Override // android.print.IWriteResultCallback
            public void onWriteStarted(ICancellationSignal cancellation, int sequence) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cancellation != null ? cancellation.asBinder() : null);
                    _data.writeInt(sequence);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onWriteStarted(cancellation, sequence);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.print.IWriteResultCallback
            public void onWriteFinished(PageRange[] pages, int sequence) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(pages, 0);
                    _data.writeInt(sequence);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onWriteFinished(pages, sequence);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.print.IWriteResultCallback
            public void onWriteFailed(CharSequence error, int sequence) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (error != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(error, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(sequence);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onWriteFailed(error, sequence);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.print.IWriteResultCallback
            public void onWriteCanceled(int sequence) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sequence);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onWriteCanceled(sequence);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IWriteResultCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IWriteResultCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
