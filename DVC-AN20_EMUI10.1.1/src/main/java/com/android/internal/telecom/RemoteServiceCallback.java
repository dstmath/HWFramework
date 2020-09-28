package com.android.internal.telecom;

import android.content.ComponentName;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface RemoteServiceCallback extends IInterface {
    void onError() throws RemoteException;

    void onResult(List<ComponentName> list, List<IBinder> list2) throws RemoteException;

    public static class Default implements RemoteServiceCallback {
        @Override // com.android.internal.telecom.RemoteServiceCallback
        public void onError() throws RemoteException {
        }

        @Override // com.android.internal.telecom.RemoteServiceCallback
        public void onResult(List<ComponentName> list, List<IBinder> list2) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements RemoteServiceCallback {
        private static final String DESCRIPTOR = "com.android.internal.telecom.RemoteServiceCallback";
        static final int TRANSACTION_onError = 1;
        static final int TRANSACTION_onResult = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static RemoteServiceCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof RemoteServiceCallback)) {
                return new Proxy(obj);
            }
            return (RemoteServiceCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onError";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "onResult";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onError();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onResult(data.createTypedArrayList(ComponentName.CREATOR), data.createBinderArrayList());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements RemoteServiceCallback {
            public static RemoteServiceCallback sDefaultImpl;
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

            @Override // com.android.internal.telecom.RemoteServiceCallback
            public void onError() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onError();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.RemoteServiceCallback
            public void onResult(List<ComponentName> components, List<IBinder> callServices) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(components);
                    _data.writeBinderList(callServices);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onResult(components, callServices);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(RemoteServiceCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static RemoteServiceCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
