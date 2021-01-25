package android.content;

import android.accounts.Account;
import android.annotation.UnsupportedAppUsage;
import android.content.ISyncAdapterUnsyncableAccountCallback;
import android.content.ISyncContext;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISyncAdapter extends IInterface {
    @UnsupportedAppUsage
    void cancelSync(ISyncContext iSyncContext) throws RemoteException;

    @UnsupportedAppUsage
    void onUnsyncableAccount(ISyncAdapterUnsyncableAccountCallback iSyncAdapterUnsyncableAccountCallback) throws RemoteException;

    @UnsupportedAppUsage
    void startSync(ISyncContext iSyncContext, String str, Account account, Bundle bundle) throws RemoteException;

    public static class Default implements ISyncAdapter {
        @Override // android.content.ISyncAdapter
        public void onUnsyncableAccount(ISyncAdapterUnsyncableAccountCallback cb) throws RemoteException {
        }

        @Override // android.content.ISyncAdapter
        public void startSync(ISyncContext syncContext, String authority, Account account, Bundle extras) throws RemoteException {
        }

        @Override // android.content.ISyncAdapter
        public void cancelSync(ISyncContext syncContext) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISyncAdapter {
        private static final String DESCRIPTOR = "android.content.ISyncAdapter";
        static final int TRANSACTION_cancelSync = 3;
        static final int TRANSACTION_onUnsyncableAccount = 1;
        static final int TRANSACTION_startSync = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISyncAdapter asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISyncAdapter)) {
                return new Proxy(obj);
            }
            return (ISyncAdapter) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onUnsyncableAccount";
            }
            if (transactionCode == 2) {
                return "startSync";
            }
            if (transactionCode != 3) {
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
            Account _arg2;
            Bundle _arg3;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onUnsyncableAccount(ISyncAdapterUnsyncableAccountCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                ISyncContext _arg0 = ISyncContext.Stub.asInterface(data.readStrongBinder());
                String _arg1 = data.readString();
                if (data.readInt() != 0) {
                    _arg2 = Account.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                if (data.readInt() != 0) {
                    _arg3 = Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg3 = null;
                }
                startSync(_arg0, _arg1, _arg2, _arg3);
                return true;
            } else if (code == 3) {
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
        public static class Proxy implements ISyncAdapter {
            public static ISyncAdapter sDefaultImpl;
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

            @Override // android.content.ISyncAdapter
            public void onUnsyncableAccount(ISyncAdapterUnsyncableAccountCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onUnsyncableAccount(cb);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.content.ISyncAdapter
            public void startSync(ISyncContext syncContext, String authority, Account account, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(syncContext != null ? syncContext.asBinder() : null);
                    _data.writeString(authority);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().startSync(syncContext, authority, account, extras);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.content.ISyncAdapter
            public void cancelSync(ISyncContext syncContext) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(syncContext != null ? syncContext.asBinder() : null);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().cancelSync(syncContext);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISyncAdapter impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISyncAdapter getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
