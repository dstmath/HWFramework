package android.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IConfirmationPromptCallback extends IInterface {
    void onConfirmationPromptCompleted(int i, byte[] bArr) throws RemoteException;

    public static class Default implements IConfirmationPromptCallback {
        @Override // android.security.IConfirmationPromptCallback
        public void onConfirmationPromptCompleted(int result, byte[] dataThatWasConfirmed) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IConfirmationPromptCallback {
        private static final String DESCRIPTOR = "android.security.IConfirmationPromptCallback";
        static final int TRANSACTION_onConfirmationPromptCompleted = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IConfirmationPromptCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IConfirmationPromptCallback)) {
                return new Proxy(obj);
            }
            return (IConfirmationPromptCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "onConfirmationPromptCompleted";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onConfirmationPromptCompleted(data.readInt(), data.createByteArray());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IConfirmationPromptCallback {
            public static IConfirmationPromptCallback sDefaultImpl;
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

            @Override // android.security.IConfirmationPromptCallback
            public void onConfirmationPromptCompleted(int result, byte[] dataThatWasConfirmed) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(result);
                    _data.writeByteArray(dataThatWasConfirmed);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onConfirmationPromptCompleted(result, dataThatWasConfirmed);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IConfirmationPromptCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IConfirmationPromptCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
