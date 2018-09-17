package android.content;

import android.accounts.Account;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISyncAdapter extends IInterface {

    public static abstract class Stub extends Binder implements ISyncAdapter {
        private static final String DESCRIPTOR = "android.content.ISyncAdapter";
        static final int TRANSACTION_cancelSync = 2;
        static final int TRANSACTION_initialize = 3;
        static final int TRANSACTION_startSync = 1;

        private static class Proxy implements ISyncAdapter {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void startSync(ISyncContext syncContext, String authority, Account account, Bundle extras) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (syncContext != null) {
                        iBinder = syncContext.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(authority);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_startSync);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_startSync);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_startSync, _data, null, Stub.TRANSACTION_startSync);
                } finally {
                    _data.recycle();
                }
            }

            public void cancelSync(ISyncContext syncContext) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (syncContext != null) {
                        iBinder = syncContext.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_cancelSync, _data, null, Stub.TRANSACTION_startSync);
                } finally {
                    _data.recycle();
                }
            }

            public void initialize(Account account, String authority) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_startSync);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authority);
                    this.mRemote.transact(Stub.TRANSACTION_initialize, _data, null, Stub.TRANSACTION_startSync);
                } finally {
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_startSync /*1*/:
                    Account account;
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    ISyncContext _arg0 = android.content.ISyncContext.Stub.asInterface(data.readStrongBinder());
                    String _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    startSync(_arg0, _arg1, account, bundle);
                    return true;
                case TRANSACTION_cancelSync /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    cancelSync(android.content.ISyncContext.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_initialize /*3*/:
                    Account account2;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account2 = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account2 = null;
                    }
                    initialize(account2, data.readString());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void cancelSync(ISyncContext iSyncContext) throws RemoteException;

    void initialize(Account account, String str) throws RemoteException;

    void startSync(ISyncContext iSyncContext, String str, Account account, Bundle bundle) throws RemoteException;
}
