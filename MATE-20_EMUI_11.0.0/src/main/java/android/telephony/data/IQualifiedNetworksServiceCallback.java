package android.telephony.data;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IQualifiedNetworksServiceCallback extends IInterface {
    void onQualifiedNetworkTypesChanged(int i, int[] iArr) throws RemoteException;

    public static class Default implements IQualifiedNetworksServiceCallback {
        @Override // android.telephony.data.IQualifiedNetworksServiceCallback
        public void onQualifiedNetworkTypesChanged(int apnTypes, int[] qualifiedNetworkTypes) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IQualifiedNetworksServiceCallback {
        private static final String DESCRIPTOR = "android.telephony.data.IQualifiedNetworksServiceCallback";
        static final int TRANSACTION_onQualifiedNetworkTypesChanged = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IQualifiedNetworksServiceCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IQualifiedNetworksServiceCallback)) {
                return new Proxy(obj);
            }
            return (IQualifiedNetworksServiceCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "onQualifiedNetworkTypesChanged";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onQualifiedNetworkTypesChanged(data.readInt(), data.createIntArray());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IQualifiedNetworksServiceCallback {
            public static IQualifiedNetworksServiceCallback sDefaultImpl;
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

            @Override // android.telephony.data.IQualifiedNetworksServiceCallback
            public void onQualifiedNetworkTypesChanged(int apnTypes, int[] qualifiedNetworkTypes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(apnTypes);
                    _data.writeIntArray(qualifiedNetworkTypes);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onQualifiedNetworkTypesChanged(apnTypes, qualifiedNetworkTypes);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IQualifiedNetworksServiceCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IQualifiedNetworksServiceCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
