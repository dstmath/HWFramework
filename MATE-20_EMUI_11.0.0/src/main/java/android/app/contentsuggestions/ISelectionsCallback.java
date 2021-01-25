package android.app.contentsuggestions;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface ISelectionsCallback extends IInterface {
    void onContentSelectionsAvailable(int i, List<ContentSelection> list) throws RemoteException;

    public static class Default implements ISelectionsCallback {
        @Override // android.app.contentsuggestions.ISelectionsCallback
        public void onContentSelectionsAvailable(int statusCode, List<ContentSelection> list) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISelectionsCallback {
        private static final String DESCRIPTOR = "android.app.contentsuggestions.ISelectionsCallback";
        static final int TRANSACTION_onContentSelectionsAvailable = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISelectionsCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISelectionsCallback)) {
                return new Proxy(obj);
            }
            return (ISelectionsCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "onContentSelectionsAvailable";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onContentSelectionsAvailable(data.readInt(), data.createTypedArrayList(ContentSelection.CREATOR));
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ISelectionsCallback {
            public static ISelectionsCallback sDefaultImpl;
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

            @Override // android.app.contentsuggestions.ISelectionsCallback
            public void onContentSelectionsAvailable(int statusCode, List<ContentSelection> selections) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(statusCode);
                    _data.writeTypedList(selections);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onContentSelectionsAvailable(statusCode, selections);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISelectionsCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISelectionsCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
