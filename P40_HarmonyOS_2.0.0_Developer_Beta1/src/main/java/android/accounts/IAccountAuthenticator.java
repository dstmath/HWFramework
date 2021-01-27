package android.accounts;

import android.accounts.IAccountAuthenticatorResponse;
import android.annotation.UnsupportedAppUsage;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAccountAuthenticator extends IInterface {
    @UnsupportedAppUsage
    void addAccount(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, String str, String str2, String[] strArr, Bundle bundle) throws RemoteException;

    void addAccountFromCredentials(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, Account account, Bundle bundle) throws RemoteException;

    @UnsupportedAppUsage
    void confirmCredentials(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, Account account, Bundle bundle) throws RemoteException;

    @UnsupportedAppUsage
    void editProperties(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, String str) throws RemoteException;

    void finishSession(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, String str, Bundle bundle) throws RemoteException;

    void getAccountCredentialsForCloning(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, Account account) throws RemoteException;

    @UnsupportedAppUsage
    void getAccountRemovalAllowed(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, Account account) throws RemoteException;

    @UnsupportedAppUsage
    void getAuthToken(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, Account account, String str, Bundle bundle) throws RemoteException;

    @UnsupportedAppUsage
    void getAuthTokenLabel(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, String str) throws RemoteException;

    @UnsupportedAppUsage
    void hasFeatures(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, Account account, String[] strArr) throws RemoteException;

    void isCredentialsUpdateSuggested(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, Account account, String str) throws RemoteException;

    void startAddAccountSession(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, String str, String str2, String[] strArr, Bundle bundle) throws RemoteException;

    void startUpdateCredentialsSession(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, Account account, String str, Bundle bundle) throws RemoteException;

    @UnsupportedAppUsage
    void updateCredentials(IAccountAuthenticatorResponse iAccountAuthenticatorResponse, Account account, String str, Bundle bundle) throws RemoteException;

    public static class Default implements IAccountAuthenticator {
        @Override // android.accounts.IAccountAuthenticator
        public void addAccount(IAccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws RemoteException {
        }

        @Override // android.accounts.IAccountAuthenticator
        public void confirmCredentials(IAccountAuthenticatorResponse response, Account account, Bundle options) throws RemoteException {
        }

        @Override // android.accounts.IAccountAuthenticator
        public void getAuthToken(IAccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws RemoteException {
        }

        @Override // android.accounts.IAccountAuthenticator
        public void getAuthTokenLabel(IAccountAuthenticatorResponse response, String authTokenType) throws RemoteException {
        }

        @Override // android.accounts.IAccountAuthenticator
        public void updateCredentials(IAccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws RemoteException {
        }

        @Override // android.accounts.IAccountAuthenticator
        public void editProperties(IAccountAuthenticatorResponse response, String accountType) throws RemoteException {
        }

        @Override // android.accounts.IAccountAuthenticator
        public void hasFeatures(IAccountAuthenticatorResponse response, Account account, String[] features) throws RemoteException {
        }

        @Override // android.accounts.IAccountAuthenticator
        public void getAccountRemovalAllowed(IAccountAuthenticatorResponse response, Account account) throws RemoteException {
        }

        @Override // android.accounts.IAccountAuthenticator
        public void getAccountCredentialsForCloning(IAccountAuthenticatorResponse response, Account account) throws RemoteException {
        }

        @Override // android.accounts.IAccountAuthenticator
        public void addAccountFromCredentials(IAccountAuthenticatorResponse response, Account account, Bundle accountCredentials) throws RemoteException {
        }

        @Override // android.accounts.IAccountAuthenticator
        public void startAddAccountSession(IAccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws RemoteException {
        }

        @Override // android.accounts.IAccountAuthenticator
        public void startUpdateCredentialsSession(IAccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws RemoteException {
        }

        @Override // android.accounts.IAccountAuthenticator
        public void finishSession(IAccountAuthenticatorResponse response, String accountType, Bundle sessionBundle) throws RemoteException {
        }

        @Override // android.accounts.IAccountAuthenticator
        public void isCredentialsUpdateSuggested(IAccountAuthenticatorResponse response, Account account, String statusToken) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "addAccount";
                case 2:
                    return "confirmCredentials";
                case 3:
                    return "getAuthToken";
                case 4:
                    return "getAuthTokenLabel";
                case 5:
                    return "updateCredentials";
                case 6:
                    return "editProperties";
                case 7:
                    return "hasFeatures";
                case 8:
                    return "getAccountRemovalAllowed";
                case 9:
                    return "getAccountCredentialsForCloning";
                case 10:
                    return "addAccountFromCredentials";
                case 11:
                    return "startAddAccountSession";
                case 12:
                    return "startUpdateCredentialsSession";
                case 13:
                    return "finishSession";
                case 14:
                    return "isCredentialsUpdateSuggested";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg4;
            Account _arg1;
            Bundle _arg2;
            Account _arg12;
            Bundle _arg3;
            Account _arg13;
            Bundle _arg32;
            Account _arg14;
            Account _arg15;
            Account _arg16;
            Account _arg17;
            Bundle _arg22;
            Bundle _arg42;
            Account _arg18;
            Bundle _arg33;
            Bundle _arg23;
            Account _arg19;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        IAccountAuthenticatorResponse _arg0 = IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                        String _arg110 = data.readString();
                        String _arg24 = data.readString();
                        String[] _arg34 = data.createStringArray();
                        if (data.readInt() != 0) {
                            _arg4 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        addAccount(_arg0, _arg110, _arg24, _arg34, _arg4);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        IAccountAuthenticatorResponse _arg02 = IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg1 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        confirmCredentials(_arg02, _arg1, _arg2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        IAccountAuthenticatorResponse _arg03 = IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg12 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        String _arg25 = data.readString();
                        if (data.readInt() != 0) {
                            _arg3 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        getAuthToken(_arg03, _arg12, _arg25, _arg3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        getAuthTokenLabel(IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder()), data.readString());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        IAccountAuthenticatorResponse _arg04 = IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg13 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        String _arg26 = data.readString();
                        if (data.readInt() != 0) {
                            _arg32 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg32 = null;
                        }
                        updateCredentials(_arg04, _arg13, _arg26, _arg32);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        editProperties(IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder()), data.readString());
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        IAccountAuthenticatorResponse _arg05 = IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg14 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        hasFeatures(_arg05, _arg14, data.createStringArray());
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        IAccountAuthenticatorResponse _arg06 = IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg15 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        getAccountRemovalAllowed(_arg06, _arg15);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        IAccountAuthenticatorResponse _arg07 = IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg16 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg16 = null;
                        }
                        getAccountCredentialsForCloning(_arg07, _arg16);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        IAccountAuthenticatorResponse _arg08 = IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg17 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg17 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg22 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        addAccountFromCredentials(_arg08, _arg17, _arg22);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        IAccountAuthenticatorResponse _arg09 = IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                        String _arg111 = data.readString();
                        String _arg27 = data.readString();
                        String[] _arg35 = data.createStringArray();
                        if (data.readInt() != 0) {
                            _arg42 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg42 = null;
                        }
                        startAddAccountSession(_arg09, _arg111, _arg27, _arg35, _arg42);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        IAccountAuthenticatorResponse _arg010 = IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg18 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg18 = null;
                        }
                        String _arg28 = data.readString();
                        if (data.readInt() != 0) {
                            _arg33 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg33 = null;
                        }
                        startUpdateCredentialsSession(_arg010, _arg18, _arg28, _arg33);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        IAccountAuthenticatorResponse _arg011 = IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                        String _arg112 = data.readString();
                        if (data.readInt() != 0) {
                            _arg23 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        finishSession(_arg011, _arg112, _arg23);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        IAccountAuthenticatorResponse _arg012 = IAccountAuthenticatorResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg19 = Account.CREATOR.createFromParcel(data);
                        } else {
                            _arg19 = null;
                        }
                        isCredentialsUpdateSuggested(_arg012, _arg19, data.readString());
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IAccountAuthenticator {
            public static IAccountAuthenticator sDefaultImpl;
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

            @Override // android.accounts.IAccountAuthenticator
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
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().addAccount(response, accountType, authTokenType, requiredFeatures, options);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accounts.IAccountAuthenticator
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
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().confirmCredentials(response, account, options);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accounts.IAccountAuthenticator
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
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getAuthToken(response, account, authTokenType, options);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accounts.IAccountAuthenticator
            public void getAuthTokenLabel(IAccountAuthenticatorResponse response, String authTokenType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    _data.writeString(authTokenType);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getAuthTokenLabel(response, authTokenType);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accounts.IAccountAuthenticator
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
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().updateCredentials(response, account, authTokenType, options);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accounts.IAccountAuthenticator
            public void editProperties(IAccountAuthenticatorResponse response, String accountType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    _data.writeString(accountType);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().editProperties(response, accountType);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accounts.IAccountAuthenticator
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
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().hasFeatures(response, account, features);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accounts.IAccountAuthenticator
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
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getAccountRemovalAllowed(response, account);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accounts.IAccountAuthenticator
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
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getAccountCredentialsForCloning(response, account);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accounts.IAccountAuthenticator
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
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().addAccountFromCredentials(response, account, accountCredentials);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accounts.IAccountAuthenticator
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
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().startAddAccountSession(response, accountType, authTokenType, requiredFeatures, options);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accounts.IAccountAuthenticator
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
                    if (this.mRemote.transact(12, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().startUpdateCredentialsSession(response, account, authTokenType, options);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accounts.IAccountAuthenticator
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
                    if (this.mRemote.transact(13, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().finishSession(response, accountType, sessionBundle);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.accounts.IAccountAuthenticator
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
                    if (this.mRemote.transact(14, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().isCredentialsUpdateSuggested(response, account, statusToken);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAccountAuthenticator impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAccountAuthenticator getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
