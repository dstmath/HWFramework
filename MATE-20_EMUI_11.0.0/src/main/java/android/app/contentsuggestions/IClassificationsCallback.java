package android.app.contentsuggestions;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IClassificationsCallback extends IInterface {
    void onContentClassificationsAvailable(int i, List<ContentClassification> list) throws RemoteException;

    public static class Default implements IClassificationsCallback {
        @Override // android.app.contentsuggestions.IClassificationsCallback
        public void onContentClassificationsAvailable(int statusCode, List<ContentClassification> list) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IClassificationsCallback {
        private static final String DESCRIPTOR = "android.app.contentsuggestions.IClassificationsCallback";
        static final int TRANSACTION_onContentClassificationsAvailable = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IClassificationsCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IClassificationsCallback)) {
                return new Proxy(obj);
            }
            return (IClassificationsCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "onContentClassificationsAvailable";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onContentClassificationsAvailable(data.readInt(), data.createTypedArrayList(ContentClassification.CREATOR));
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IClassificationsCallback {
            public static IClassificationsCallback sDefaultImpl;
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

            @Override // android.app.contentsuggestions.IClassificationsCallback
            public void onContentClassificationsAvailable(int statusCode, List<ContentClassification> classifications) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(statusCode);
                    _data.writeTypedList(classifications);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onContentClassificationsAvailable(statusCode, classifications);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IClassificationsCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IClassificationsCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
