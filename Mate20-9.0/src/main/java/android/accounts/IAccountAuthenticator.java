package android.accounts;

import android.accounts.IAccountAuthenticatorResponse;
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
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    _data.writeString(accountType);
                    _data.writeString(authTokenType);
                    _data.writeStringArray(requiredFeatures);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void confirmCredentials(IAccountAuthenticatorResponse response, Account account, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void getAuthToken(IAccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authTokenType);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void getAuthTokenLabel(IAccountAuthenticatorResponse response, String authTokenType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    _data.writeString(authTokenType);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void updateCredentials(IAccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authTokenType);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void editProperties(IAccountAuthenticatorResponse response, String accountType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    _data.writeString(accountType);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void hasFeatures(IAccountAuthenticatorResponse response, Account account, String[] features) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringArray(features);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void getAccountRemovalAllowed(IAccountAuthenticatorResponse response, Account account) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void getAccountCredentialsForCloning(IAccountAuthenticatorResponse response, Account account) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void addAccountFromCredentials(IAccountAuthenticatorResponse response, Account account, Bundle accountCredentials) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (accountCredentials != null) {
                        _data.writeInt(1);
                        accountCredentials.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void startAddAccountSession(IAccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    _data.writeString(accountType);
                    _data.writeString(authTokenType);
                    _data.writeStringArray(requiredFeatures);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void startUpdateCredentialsSession(IAccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authTokenType);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void finishSession(IAccountAuthenticatorResponse response, String accountType, Bundle sessionBundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    _data.writeString(accountType);
                    if (sessionBundle != null) {
                        _data.writeInt(1);
                        sessionBundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void isCredentialsUpdateSuggested(IAccountAuthenticatorResponse response, Account account, String statusToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(statusToken);
                    this.mRemote.transact(14, _data, null, 1);
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

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v8, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v12, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v18, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v24, resolved type: android.accounts.Account} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v28, resolved type: android.accounts.Account} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v32, resolved type: android.accounts.Account} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v36, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v45, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v49, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v53, resolved type: android.accounts.Account} */
        /* JADX WARNING: type inference failed for: r0v2 */
        /* JADX WARNING: type inference failed for: r0v3 */
        /* JADX WARNING: type inference failed for: r0v40 */
        /* JADX WARNING: type inference failed for: r0v58 */
        /* JADX WARNING: type inference failed for: r0v59 */
        /* JADX WARNING: type inference failed for: r0v60 */
        /* JADX WARNING: type inference failed for: r0v61 */
        /* JADX WARNING: type inference failed for: r0v62 */
        /* JADX WARNING: type inference failed for: r0v63 */
        /* JADX WARNING: type inference failed for: r0v64 */
        /* JADX WARNING: type inference failed for: r0v65 */
        /* JADX WARNING: type inference failed for: r0v66 */
        /* JADX WARNING: type inference failed for: r0v67 */
        /* JADX WARNING: type inference failed for: r0v68 */
        /* JADX WARNING: type inference failed for: r0v69 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Account _arg1;
            Account _arg12;
            Account _arg13;
            Account _arg14;
            Account _arg15;
            int i = code;
            Parcel parcel = data;
            if (i != 1598968902) {
                ? _arg16 = 0;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountAuthenticatorResponse _arg0 = IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                        String _arg17 = data.readString();
                        String _arg2 = data.readString();
                        String[] _arg3 = data.createStringArray();
                        if (data.readInt() != 0) {
                            _arg16 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        addAccount(_arg0, _arg17, _arg2, _arg3, _arg16);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountAuthenticatorResponse _arg02 = IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg1 = Account.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg16 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        confirmCredentials(_arg02, _arg1, _arg16);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountAuthenticatorResponse _arg03 = IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg12 = Account.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg12 = null;
                        }
                        String _arg22 = data.readString();
                        if (data.readInt() != 0) {
                            _arg16 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        getAuthToken(_arg03, _arg12, _arg22, _arg16);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        getAuthTokenLabel(IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder()), data.readString());
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountAuthenticatorResponse _arg04 = IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg13 = Account.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg13 = null;
                        }
                        String _arg23 = data.readString();
                        if (data.readInt() != 0) {
                            _arg16 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        updateCredentials(_arg04, _arg13, _arg23, _arg16);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        editProperties(IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder()), data.readString());
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountAuthenticatorResponse _arg05 = IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg16 = Account.CREATOR.createFromParcel(parcel);
                        }
                        hasFeatures(_arg05, _arg16, data.createStringArray());
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountAuthenticatorResponse _arg06 = IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg16 = Account.CREATOR.createFromParcel(parcel);
                        }
                        getAccountRemovalAllowed(_arg06, _arg16);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountAuthenticatorResponse _arg07 = IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg16 = Account.CREATOR.createFromParcel(parcel);
                        }
                        getAccountCredentialsForCloning(_arg07, _arg16);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountAuthenticatorResponse _arg08 = IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg14 = Account.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg14 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg16 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        addAccountFromCredentials(_arg08, _arg14, _arg16);
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountAuthenticatorResponse _arg09 = IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                        String _arg18 = data.readString();
                        String _arg24 = data.readString();
                        String[] _arg32 = data.createStringArray();
                        if (data.readInt() != 0) {
                            _arg16 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        startAddAccountSession(_arg09, _arg18, _arg24, _arg32, _arg16);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountAuthenticatorResponse _arg010 = IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg15 = Account.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg15 = null;
                        }
                        String _arg25 = data.readString();
                        if (data.readInt() != 0) {
                            _arg16 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        startUpdateCredentialsSession(_arg010, _arg15, _arg25, _arg16);
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountAuthenticatorResponse _arg011 = IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                        String _arg19 = data.readString();
                        if (data.readInt() != 0) {
                            _arg16 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        finishSession(_arg011, _arg19, _arg16);
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountAuthenticatorResponse _arg012 = IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg16 = Account.CREATOR.createFromParcel(parcel);
                        }
                        isCredentialsUpdateSuggested(_arg012, _arg16, data.readString());
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
