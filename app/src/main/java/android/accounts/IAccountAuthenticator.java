package android.accounts;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAccountAuthenticator extends IInterface {

    public static abstract class Stub extends Binder implements IAccountAuthenticator {
        private static final String DESCRIPTOR = "android.accounts.IAccountAuthenticator";
        static final int TRANSACTION_addAccount = 1;
        static final int TRANSACTION_addAccountFromCredentials = 10;
        static final int TRANSACTION_confirmCredentials = 2;
        static final int TRANSACTION_editProperties = 6;
        static final int TRANSACTION_finishSession = 13;
        static final int TRANSACTION_getAccountCredentialsForCloning = 9;
        static final int TRANSACTION_getAccountRemovalAllowed = 8;
        static final int TRANSACTION_getAuthToken = 3;
        static final int TRANSACTION_getAuthTokenLabel = 4;
        static final int TRANSACTION_hasFeatures = 7;
        static final int TRANSACTION_isCredentialsUpdateSuggested = 14;
        static final int TRANSACTION_startAddAccountSession = 11;
        static final int TRANSACTION_startUpdateCredentialsSession = 12;
        static final int TRANSACTION_updateCredentials = 5;

        private static class Proxy implements IAccountAuthenticator {
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

            public void addAccount(IAccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(accountType);
                    _data.writeString(authTokenType);
                    _data.writeStringArray(requiredFeatures);
                    if (options != null) {
                        _data.writeInt(Stub.TRANSACTION_addAccount);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_addAccount, _data, null, Stub.TRANSACTION_addAccount);
                } finally {
                    _data.recycle();
                }
            }

            public void confirmCredentials(IAccountAuthenticatorResponse response, Account account, Bundle options) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_addAccount);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (options != null) {
                        _data.writeInt(Stub.TRANSACTION_addAccount);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_confirmCredentials, _data, null, Stub.TRANSACTION_addAccount);
                } finally {
                    _data.recycle();
                }
            }

            public void getAuthToken(IAccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_addAccount);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authTokenType);
                    if (options != null) {
                        _data.writeInt(Stub.TRANSACTION_addAccount);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getAuthToken, _data, null, Stub.TRANSACTION_addAccount);
                } finally {
                    _data.recycle();
                }
            }

            public void getAuthTokenLabel(IAccountAuthenticatorResponse response, String authTokenType) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(authTokenType);
                    this.mRemote.transact(Stub.TRANSACTION_getAuthTokenLabel, _data, null, Stub.TRANSACTION_addAccount);
                } finally {
                    _data.recycle();
                }
            }

            public void updateCredentials(IAccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_addAccount);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authTokenType);
                    if (options != null) {
                        _data.writeInt(Stub.TRANSACTION_addAccount);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_updateCredentials, _data, null, Stub.TRANSACTION_addAccount);
                } finally {
                    _data.recycle();
                }
            }

            public void editProperties(IAccountAuthenticatorResponse response, String accountType) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(accountType);
                    this.mRemote.transact(Stub.TRANSACTION_editProperties, _data, null, Stub.TRANSACTION_addAccount);
                } finally {
                    _data.recycle();
                }
            }

            public void hasFeatures(IAccountAuthenticatorResponse response, Account account, String[] features) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_addAccount);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringArray(features);
                    this.mRemote.transact(Stub.TRANSACTION_hasFeatures, _data, null, Stub.TRANSACTION_addAccount);
                } finally {
                    _data.recycle();
                }
            }

            public void getAccountRemovalAllowed(IAccountAuthenticatorResponse response, Account account) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_addAccount);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getAccountRemovalAllowed, _data, null, Stub.TRANSACTION_addAccount);
                } finally {
                    _data.recycle();
                }
            }

            public void getAccountCredentialsForCloning(IAccountAuthenticatorResponse response, Account account) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_addAccount);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getAccountCredentialsForCloning, _data, null, Stub.TRANSACTION_addAccount);
                } finally {
                    _data.recycle();
                }
            }

            public void addAccountFromCredentials(IAccountAuthenticatorResponse response, Account account, Bundle accountCredentials) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_addAccount);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (accountCredentials != null) {
                        _data.writeInt(Stub.TRANSACTION_addAccount);
                        accountCredentials.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_addAccountFromCredentials, _data, null, Stub.TRANSACTION_addAccount);
                } finally {
                    _data.recycle();
                }
            }

            public void startAddAccountSession(IAccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(accountType);
                    _data.writeString(authTokenType);
                    _data.writeStringArray(requiredFeatures);
                    if (options != null) {
                        _data.writeInt(Stub.TRANSACTION_addAccount);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_startAddAccountSession, _data, null, Stub.TRANSACTION_addAccount);
                } finally {
                    _data.recycle();
                }
            }

            public void startUpdateCredentialsSession(IAccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_addAccount);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authTokenType);
                    if (options != null) {
                        _data.writeInt(Stub.TRANSACTION_addAccount);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_startUpdateCredentialsSession, _data, null, Stub.TRANSACTION_addAccount);
                } finally {
                    _data.recycle();
                }
            }

            public void finishSession(IAccountAuthenticatorResponse response, String accountType, Bundle sessionBundle) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(accountType);
                    if (sessionBundle != null) {
                        _data.writeInt(Stub.TRANSACTION_addAccount);
                        sessionBundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_finishSession, _data, null, Stub.TRANSACTION_addAccount);
                } finally {
                    _data.recycle();
                }
            }

            public void isCredentialsUpdateSuggested(IAccountAuthenticatorResponse response, Account account, String statusToken) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_addAccount);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(statusToken);
                    this.mRemote.transact(Stub.TRANSACTION_isCredentialsUpdateSuggested, _data, null, Stub.TRANSACTION_addAccount);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAccountAuthenticator asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAccountAuthenticator)) {
                return new Proxy(obj);
            }
            return (IAccountAuthenticator) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IAccountAuthenticatorResponse _arg0;
            String _arg1;
            String _arg2;
            String[] _arg3;
            Bundle bundle;
            Account account;
            Bundle bundle2;
            Bundle bundle3;
            switch (code) {
                case TRANSACTION_addAccount /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                    _arg1 = data.readString();
                    _arg2 = data.readString();
                    _arg3 = data.createStringArray();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    addAccount(_arg0, _arg1, _arg2, _arg3, bundle);
                    return true;
                case TRANSACTION_confirmCredentials /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    if (data.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle2 = null;
                    }
                    confirmCredentials(_arg0, account, bundle2);
                    return true;
                case TRANSACTION_getAuthToken /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _arg2 = data.readString();
                    if (data.readInt() != 0) {
                        bundle3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle3 = null;
                    }
                    getAuthToken(_arg0, account, _arg2, bundle3);
                    return true;
                case TRANSACTION_getAuthTokenLabel /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    getAuthTokenLabel(android.accounts.IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder()), data.readString());
                    return true;
                case TRANSACTION_updateCredentials /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _arg2 = data.readString();
                    if (data.readInt() != 0) {
                        bundle3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle3 = null;
                    }
                    updateCredentials(_arg0, account, _arg2, bundle3);
                    return true;
                case TRANSACTION_editProperties /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    editProperties(android.accounts.IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder()), data.readString());
                    return true;
                case TRANSACTION_hasFeatures /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    hasFeatures(_arg0, account, data.createStringArray());
                    return true;
                case TRANSACTION_getAccountRemovalAllowed /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    getAccountRemovalAllowed(_arg0, account);
                    return true;
                case TRANSACTION_getAccountCredentialsForCloning /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    getAccountCredentialsForCloning(_arg0, account);
                    return true;
                case TRANSACTION_addAccountFromCredentials /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    if (data.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle2 = null;
                    }
                    addAccountFromCredentials(_arg0, account, bundle2);
                    return true;
                case TRANSACTION_startAddAccountSession /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                    _arg1 = data.readString();
                    _arg2 = data.readString();
                    _arg3 = data.createStringArray();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    startAddAccountSession(_arg0, _arg1, _arg2, _arg3, bundle);
                    return true;
                case TRANSACTION_startUpdateCredentialsSession /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _arg2 = data.readString();
                    if (data.readInt() != 0) {
                        bundle3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle3 = null;
                    }
                    startUpdateCredentialsSession(_arg0, account, _arg2, bundle3);
                    return true;
                case TRANSACTION_finishSession /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle2 = null;
                    }
                    finishSession(_arg0, _arg1, bundle2);
                    return true;
                case TRANSACTION_isCredentialsUpdateSuggested /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    isCredentialsUpdateSuggested(_arg0, account, data.readString());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addAccount(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, String str, String str2, String[] strArr, Bundle bundle) throws RemoteException;

    void addAccountFromCredentials(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, Account account, Bundle bundle) throws RemoteException;

    void confirmCredentials(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, Account account, Bundle bundle) throws RemoteException;

    void editProperties(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, String str) throws RemoteException;

    void finishSession(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, String str, Bundle bundle) throws RemoteException;

    void getAccountCredentialsForCloning(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, Account account) throws RemoteException;

    void getAccountRemovalAllowed(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, Account account) throws RemoteException;

    void getAuthToken(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, Account account, String str, Bundle bundle) throws RemoteException;

    void getAuthTokenLabel(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, String str) throws RemoteException;

    void hasFeatures(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, Account account, String[] strArr) throws RemoteException;

    void isCredentialsUpdateSuggested(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, Account account, String str) throws RemoteException;

    void startAddAccountSession(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, String str, String str2, String[] strArr, Bundle bundle) throws RemoteException;

    void startUpdateCredentialsSession(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, Account account, String str, Bundle bundle) throws RemoteException;

    void updateCredentials(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, Account account, String str, Bundle bundle) throws RemoteException;
}
