package android.print;

import android.os.Binder;
import android.os.IBinder;
import android.os.ICancellationSignal;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;

public interface ILayoutResultCallback extends IInterface {
    void onLayoutCanceled(int i) throws RemoteException;

    void onLayoutFailed(CharSequence charSequence, int i) throws RemoteException;

    void onLayoutFinished(PrintDocumentInfo printDocumentInfo, boolean z, int i) throws RemoteException;

    void onLayoutStarted(ICancellationSignal iCancellationSignal, int i) throws RemoteException;

    public static class Default implements ILayoutResultCallback {
        @Override // android.print.ILayoutResultCallback
        public void onLayoutStarted(ICancellationSignal cancellation, int sequence) throws RemoteException {
        }

        @Override // android.print.ILayoutResultCallback
        public void onLayoutFinished(PrintDocumentInfo info, boolean changed, int sequence) throws RemoteException {
        }

        @Override // android.print.ILayoutResultCallback
        public void onLayoutFailed(CharSequence error, int sequence) throws RemoteException {
        }

        @Override // android.print.ILayoutResultCallback
        public void onLayoutCanceled(int sequence) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ILayoutResultCallback {
        private static final String DESCRIPTOR = "android.print.ILayoutResultCallback";
        static final int TRANSACTION_onLayoutCanceled = 4;
        static final int TRANSACTION_onLayoutFailed = 3;
        static final int TRANSACTION_onLayoutFinished = 2;
        static final int TRANSACTION_onLayoutStarted = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ILayoutResultCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ILayoutResultCallback)) {
                return new Proxy(obj);
            }
            return (ILayoutResultCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onLayoutStarted";
            }
            if (transactionCode == 2) {
                return "onLayoutFinished";
            }
            if (transactionCode == 3) {
                return "onLayoutFailed";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "onLayoutCanceled";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PrintDocumentInfo _arg0;
            CharSequence _arg02;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onLayoutStarted(ICancellationSignal.Stub.asInterface(data.readStrongBinder()), data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = PrintDocumentInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onLayoutFinished(_arg0, data.readInt() != 0, data.readInt());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                onLayoutFailed(_arg02, data.readInt());
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                onLayoutCanceled(data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ILayoutResultCallback {
            public static ILayoutResultCallback sDefaultImpl;
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

            @Override // android.print.ILayoutResultCallback
            public void onLayoutStarted(ICancellationSignal cancellation, int sequence) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cancellation != null ? cancellation.asBinder() : null);
                    _data.writeInt(sequence);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onLayoutStarted(cancellation, sequence);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.print.ILayoutResultCallback
            public void onLayoutFinished(PrintDocumentInfo info, boolean changed, int sequence) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 0;
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (changed) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeInt(sequence);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onLayoutFinished(info, changed, sequence);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.print.ILayoutResultCallback
            public void onLayoutFailed(CharSequence error, int sequence) throws RemoteException {
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
                        Stub.getDefaultImpl().onLayoutFailed(error, sequence);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.print.ILayoutResultCallback
            public void onLayoutCanceled(int sequence) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sequence);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onLayoutCanceled(sequence);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ILayoutResultCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ILayoutResultCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
