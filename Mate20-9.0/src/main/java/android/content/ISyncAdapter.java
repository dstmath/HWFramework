package android.content;

import android.accounts.Account;
import android.content.ISyncAdapterUnsyncableAccountCallback;
import android.content.ISyncContext;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISyncAdapter extends IInterface {

    public static abstract class Stub extends Binder implements ISyncAdapter {
        private static final String DESCRIPTOR = "android.content.ISyncAdapter";
        static final int TRANSACTION_cancelSync = 3;
        static final int TRANSACTION_onUnsyncableAccount = 1;
        static final int TRANSACTION_startSync = 2;

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

            public void onUnsyncableAccount(ISyncAdapterUnsyncableAccountCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void cancelSync(ISyncContext syncContext) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(syncContext != null ? syncContext.asBinder() : null);
                    this.mRemote.transact(3, _data, null, 1);
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
            Account _arg2;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onUnsyncableAccount(ISyncAdapterUnsyncableAccountCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        ISyncContext _arg0 = ISyncContext.Stub.asInterface(data.readStrongBinder());
                        String _arg1 = data.readString();
                        Bundle _arg3 = null;
                        if (data.readInt() != 0) {
                            _arg2 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        startSync(_arg0, _arg1, _arg2, _arg3);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        cancelSync(ISyncContext.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void cancelSync(ISyncContext iSyncContext) throws RemoteException;

    void onUnsyncableAccount(ISyncAdapterUnsyncableAccountCallback iSyncAdapterUnsyncableAccountCallback) throws RemoteException;

    void startSync(ISyncContext iSyncContext, String str, Account account, Bundle bundle) throws RemoteException;
}
