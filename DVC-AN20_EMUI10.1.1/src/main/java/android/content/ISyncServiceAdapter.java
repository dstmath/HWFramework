package android.content;

import android.annotation.UnsupportedAppUsage;
import android.content.ISyncContext;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISyncServiceAdapter extends IInterface {
    @UnsupportedAppUsage
    void cancelSync(ISyncContext iSyncContext) throws RemoteException;

    @UnsupportedAppUsage
    void startSync(ISyncContext iSyncContext, Bundle bundle) throws RemoteException;

    public static class Default implements ISyncServiceAdapter {
        @Override // android.content.ISyncServiceAdapter
        public void startSync(ISyncContext syncContext, Bundle extras) throws RemoteException {
        }

        @Override // android.content.ISyncServiceAdapter
        public void cancelSync(ISyncContext syncContext) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISyncServiceAdapter {
        private static final String DESCRIPTOR = "android.content.ISyncServiceAdapter";
        static final int TRANSACTION_cancelSync = 2;
        static final int TRANSACTION_startSync = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISyncServiceAdapter asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISyncServiceAdapter)) {
                return new Proxy(obj);
            }
            return (ISyncServiceAdapter) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "startSync";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "cancelSync";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg1;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                ISyncContext _arg0 = ISyncContext.Stub.asInterface(data.readStrongBinder());
                if (data.readInt() != 0) {
                    _arg1 = Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                startSync(_arg0, _arg1);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                cancelSync(ISyncContext.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ISyncServiceAdapter {
            public static ISyncServiceAdapter sDefaultImpl;
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

            @Override // android.content.ISyncServiceAdapter
            public void startSync(ISyncContext syncContext, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(syncContext != null ? syncContext.asBinder() : null);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().startSync(syncContext, extras);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.content.ISyncServiceAdapter
            public void cancelSync(ISyncContext syncContext) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(syncContext != null ? syncContext.asBinder() : null);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().cancelSync(syncContext);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISyncServiceAdapter impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISyncServiceAdapter getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
