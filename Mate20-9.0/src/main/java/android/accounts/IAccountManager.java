package android.accounts;

import android.accounts.IAccountManagerResponse;
import android.content.IntentSender;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.UserHandle;
import java.util.Map;

public interface IAccountManager extends IInterface {

    public static abstract class Stub extends Binder implements IAccountManager {
        private static final String DESCRIPTOR = "android.accounts.IAccountManager";
        static final int TRANSACTION_accountAuthenticated = 29;
        static final int TRANSACTION_addAccount = 24;
        static final int TRANSACTION_addAccountAsUser = 25;
        static final int TRANSACTION_addAccountExplicitly = 11;
        static final int TRANSACTION_addAccountExplicitlyWithVisibility = 43;
        static final int TRANSACTION_addSharedAccountsFromParentUser = 33;
        static final int TRANSACTION_clearPassword = 20;
        static final int TRANSACTION_confirmCredentialsAsUser = 28;
        static final int TRANSACTION_copyAccountToUser = 15;
        static final int TRANSACTION_createRequestAccountAccessIntentSenderAsUser = 50;
        static final int TRANSACTION_editProperties = 27;
        static final int TRANSACTION_finishSessionAsUser = 39;
        static final int TRANSACTION_getAccountByTypeAndFeatures = 9;
        static final int TRANSACTION_getAccountVisibility = 45;
        static final int TRANSACTION_getAccounts = 4;
        static final int TRANSACTION_getAccountsAndVisibilityForPackage = 46;
        static final int TRANSACTION_getAccountsAsUser = 7;
        static final int TRANSACTION_getAccountsByFeatures = 10;
        static final int TRANSACTION_getAccountsByTypeForPackage = 6;
        static final int TRANSACTION_getAccountsForPackage = 5;
        static final int TRANSACTION_getAuthToken = 23;
        static final int TRANSACTION_getAuthTokenLabel = 30;
        static final int TRANSACTION_getAuthenticatorTypes = 3;
        static final int TRANSACTION_getPackagesAndVisibilityForAccount = 42;
        static final int TRANSACTION_getPassword = 1;
        static final int TRANSACTION_getPreviousName = 35;
        static final int TRANSACTION_getSharedAccountsAsUser = 31;
        static final int TRANSACTION_getUserData = 2;
        static final int TRANSACTION_hasAccountAccess = 49;
        static final int TRANSACTION_hasFeatures = 8;
        static final int TRANSACTION_invalidateAuthToken = 16;
        static final int TRANSACTION_isCredentialsUpdateSuggested = 41;
        static final int TRANSACTION_onAccountAccessed = 51;
        static final int TRANSACTION_peekAuthToken = 17;
        static final int TRANSACTION_registerAccountListener = 47;
        static final int TRANSACTION_removeAccount = 12;
        static final int TRANSACTION_removeAccountAsUser = 13;
        static final int TRANSACTION_removeAccountExplicitly = 14;
        static final int TRANSACTION_removeSharedAccountAsUser = 32;
        static final int TRANSACTION_renameAccount = 34;
        static final int TRANSACTION_renameSharedAccountAsUser = 36;
        static final int TRANSACTION_setAccountVisibility = 44;
        static final int TRANSACTION_setAuthToken = 18;
        static final int TRANSACTION_setPassword = 19;
        static final int TRANSACTION_setUserData = 21;
        static final int TRANSACTION_someUserHasAccount = 40;
        static final int TRANSACTION_startAddAccountSession = 37;
        static final int TRANSACTION_startUpdateCredentialsSession = 38;
        static final int TRANSACTION_unregisterAccountListener = 48;
        static final int TRANSACTION_updateAppPermission = 22;
        static final int TRANSACTION_updateCredentials = 26;

        private static class Proxy implements IAccountManager {
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

            public String getPassword(Account account) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getUserData(Account account, String key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(key);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public AuthenticatorDescription[] getAuthenticatorTypes(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return (AuthenticatorDescription[]) _reply.createTypedArray(AuthenticatorDescription.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Account[] getAccounts(String accountType, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(accountType);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return (Account[]) _reply.createTypedArray(Account.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Account[] getAccountsForPackage(String packageName, int uid, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(uid);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return (Account[]) _reply.createTypedArray(Account.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Account[] getAccountsByTypeForPackage(String type, String packageName, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(type);
                    _data.writeString(packageName);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return (Account[]) _reply.createTypedArray(Account.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Account[] getAccountsAsUser(String accountType, int userId, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(accountType);
                    _data.writeInt(userId);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return (Account[]) _reply.createTypedArray(Account.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void hasFeatures(IAccountManagerResponse response, Account account, String[] features, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
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
                    _data.writeString(opPackageName);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getAccountByTypeAndFeatures(IAccountManagerResponse response, String accountType, String[] features, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    _data.writeString(accountType);
                    _data.writeStringArray(features);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getAccountsByFeatures(IAccountManagerResponse response, String accountType, String[] features, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    _data.writeString(accountType);
                    _data.writeStringArray(features);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean addAccountExplicitly(Account account, String password, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(password);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeAccount(IAccountManagerResponse response, Account account, boolean expectActivityLaunch) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(expectActivityLaunch);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeAccountAsUser(IAccountManagerResponse response, Account account, boolean expectActivityLaunch, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(expectActivityLaunch);
                    _data.writeInt(userId);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean removeAccountExplicitly(Account account) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void copyAccountToUser(IAccountManagerResponse response, Account account, int userFrom, int userTo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userFrom);
                    _data.writeInt(userTo);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void invalidateAuthToken(String accountType, String authToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(accountType);
                    _data.writeString(authToken);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String peekAuthToken(Account account, String authTokenType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authTokenType);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAuthToken(Account account, String authTokenType, String authToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authTokenType);
                    _data.writeString(authToken);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPassword(Account account, String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(password);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearPassword(Account account) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setUserData(Account account, String key, String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(key);
                    _data.writeString(value);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateAppPermission(Account account, String authTokenType, int uid, boolean value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authTokenType);
                    _data.writeInt(uid);
                    _data.writeInt(value);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getAuthToken(IAccountManagerResponse response, Account account, String authTokenType, boolean notifyOnAuthFailure, boolean expectActivityLaunch, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
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
                    _data.writeInt(notifyOnAuthFailure);
                    _data.writeInt(expectActivityLaunch);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addAccount(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    _data.writeString(accountType);
                    _data.writeString(authTokenType);
                    _data.writeStringArray(requiredFeatures);
                    _data.writeInt(expectActivityLaunch);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addAccountAsUser(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch, Bundle options, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    _data.writeString(accountType);
                    _data.writeString(authTokenType);
                    _data.writeStringArray(requiredFeatures);
                    _data.writeInt(expectActivityLaunch);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateCredentials(IAccountManagerResponse response, Account account, String authTokenType, boolean expectActivityLaunch, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
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
                    _data.writeInt(expectActivityLaunch);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void editProperties(IAccountManagerResponse response, String accountType, boolean expectActivityLaunch) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    _data.writeString(accountType);
                    _data.writeInt(expectActivityLaunch);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void confirmCredentialsAsUser(IAccountManagerResponse response, Account account, Bundle options, boolean expectActivityLaunch, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
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
                    _data.writeInt(expectActivityLaunch);
                    _data.writeInt(userId);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean accountAuthenticated(Account account) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getAuthTokenLabel(IAccountManagerResponse response, String accountType, String authTokenType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    _data.writeString(accountType);
                    _data.writeString(authTokenType);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Account[] getSharedAccountsAsUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    return (Account[]) _reply.createTypedArray(Account.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean removeSharedAccountAsUser(Account account, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addSharedAccountsFromParentUser(int parentUserId, int userId, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(parentUserId);
                    _data.writeInt(userId);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void renameAccount(IAccountManagerResponse response, Account accountToRename, String newName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    if (accountToRename != null) {
                        _data.writeInt(1);
                        accountToRename.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(newName);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getPreviousName(Account account) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean renameSharedAccountAsUser(Account accountToRename, String newName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (accountToRename != null) {
                        _data.writeInt(1);
                        accountToRename.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(newName);
                    _data.writeInt(userId);
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startAddAccountSession(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    _data.writeString(accountType);
                    _data.writeString(authTokenType);
                    _data.writeStringArray(requiredFeatures);
                    _data.writeInt(expectActivityLaunch);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startUpdateCredentialsSession(IAccountManagerResponse response, Account account, String authTokenType, boolean expectActivityLaunch, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
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
                    _data.writeInt(expectActivityLaunch);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void finishSessionAsUser(IAccountManagerResponse response, Bundle sessionBundle, boolean expectActivityLaunch, Bundle appInfo, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(response != null ? response.asBinder() : null);
                    if (sessionBundle != null) {
                        _data.writeInt(1);
                        sessionBundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(expectActivityLaunch);
                    if (appInfo != null) {
                        _data.writeInt(1);
                        appInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean someUserHasAccount(Account account) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void isCredentialsUpdateSuggested(IAccountManagerResponse response, Account account, String statusToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
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
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Map getPackagesAndVisibilityForAccount(Account account) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(42, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readHashMap(getClass().getClassLoader());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean addAccountExplicitlyWithVisibility(Account account, String password, Bundle extras, Map visibility) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(password);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeMap(visibility);
                    this.mRemote.transact(43, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setAccountVisibility(Account a, String packageName, int newVisibility) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (a != null) {
                        _data.writeInt(1);
                        a.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    _data.writeInt(newVisibility);
                    this.mRemote.transact(44, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAccountVisibility(Account a, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (a != null) {
                        _data.writeInt(1);
                        a.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(45, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Map getAccountsAndVisibilityForPackage(String packageName, String accountType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(accountType);
                    this.mRemote.transact(46, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readHashMap(getClass().getClassLoader());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerAccountListener(String[] accountTypes, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(accountTypes);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(47, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterAccountListener(String[] accountTypes, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(accountTypes);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(48, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasAccountAccess(Account account, String packageName, UserHandle userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    if (userHandle != null) {
                        _data.writeInt(1);
                        userHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(49, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IntentSender createRequestAccountAccessIntentSenderAsUser(Account account, String packageName, UserHandle userHandle) throws RemoteException {
                IntentSender _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    if (userHandle != null) {
                        _data.writeInt(1);
                        userHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(50, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = IntentSender.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onAccountAccessed(String token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(token);
                    this.mRemote.transact(51, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAccountManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAccountManager)) {
                return new Proxy(obj);
            }
            return (IAccountManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v14, resolved type: android.accounts.Account} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v20, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v24, resolved type: android.accounts.Account} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v28, resolved type: android.accounts.Account} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v36, resolved type: android.accounts.Account} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v60, resolved type: android.accounts.Account} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v87, resolved type: android.accounts.Account} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v106, resolved type: android.accounts.Account} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v114, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v129, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v133, resolved type: android.os.UserHandle} */
        /* JADX WARNING: type inference failed for: r1v0 */
        /* JADX WARNING: type inference failed for: r1v1 */
        /* JADX WARNING: type inference failed for: r1v5 */
        /* JADX WARNING: type inference failed for: r1v32 */
        /* JADX WARNING: type inference failed for: r1v41 */
        /* JADX WARNING: type inference failed for: r1v45 */
        /* JADX WARNING: type inference failed for: r1v49 */
        /* JADX WARNING: type inference failed for: r1v53 */
        /* JADX WARNING: type inference failed for: r1v56 */
        /* JADX WARNING: type inference failed for: r1v70 */
        /* JADX WARNING: type inference failed for: r1v76 */
        /* JADX WARNING: type inference failed for: r1v82 */
        /* JADX WARNING: type inference failed for: r1v91 */
        /* JADX WARNING: type inference failed for: r1v95 */
        /* JADX WARNING: type inference failed for: r1v102 */
        /* JADX WARNING: type inference failed for: r1v110 */
        /* JADX WARNING: type inference failed for: r1v118 */
        /* JADX WARNING: type inference failed for: r1v122 */
        /* JADX WARNING: type inference failed for: r1v137 */
        /* JADX WARNING: type inference failed for: r1v138 */
        /* JADX WARNING: type inference failed for: r1v139 */
        /* JADX WARNING: type inference failed for: r1v140 */
        /* JADX WARNING: type inference failed for: r1v141 */
        /* JADX WARNING: type inference failed for: r1v142 */
        /* JADX WARNING: type inference failed for: r1v143 */
        /* JADX WARNING: type inference failed for: r1v144 */
        /* JADX WARNING: type inference failed for: r1v145 */
        /* JADX WARNING: type inference failed for: r1v146 */
        /* JADX WARNING: type inference failed for: r1v147 */
        /* JADX WARNING: type inference failed for: r1v148 */
        /* JADX WARNING: type inference failed for: r1v149 */
        /* JADX WARNING: type inference failed for: r1v150 */
        /* JADX WARNING: type inference failed for: r1v151 */
        /* JADX WARNING: type inference failed for: r1v152 */
        /* JADX WARNING: type inference failed for: r1v153 */
        /* JADX WARNING: type inference failed for: r1v154 */
        /* JADX WARNING: type inference failed for: r1v155 */
        /* JADX WARNING: type inference failed for: r1v156 */
        /* JADX WARNING: type inference failed for: r1v157 */
        /* JADX WARNING: type inference failed for: r1v158 */
        /* JADX WARNING: type inference failed for: r1v159 */
        /* JADX WARNING: type inference failed for: r1v160 */
        /* JADX WARNING: type inference failed for: r1v161 */
        /* JADX WARNING: type inference failed for: r1v162 */
        /* JADX WARNING: type inference failed for: r1v163 */
        /* JADX WARNING: type inference failed for: r1v164 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Account _arg0;
            Account _arg1;
            Bundle _arg5;
            Bundle _arg52;
            Bundle _arg53;
            Account _arg12;
            Bundle _arg4;
            Account _arg13;
            Bundle _arg54;
            Account _arg14;
            Bundle _arg42;
            Bundle _arg15;
            Bundle _arg3;
            Account _arg02;
            Account _arg03;
            Account _arg04;
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                boolean _arg2 = false;
                ? _arg22 = 0;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        String _result = getPassword(_arg22);
                        reply.writeNoException();
                        parcel2.writeString(_result);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        String _result2 = getUserData(_arg22, data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result2);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        AuthenticatorDescription[] _result3 = getAuthenticatorTypes(data.readInt());
                        reply.writeNoException();
                        parcel2.writeTypedArray(_result3, 1);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        Account[] _result4 = getAccounts(data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeTypedArray(_result4, 1);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        Account[] _result5 = getAccountsForPackage(data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeTypedArray(_result5, 1);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        Account[] _result6 = getAccountsByTypeForPackage(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeTypedArray(_result6, 1);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        Account[] _result7 = getAccountsAsUser(data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeTypedArray(_result7, 1);
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountManagerResponse _arg05 = IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        hasFeatures(_arg05, _arg22, data.createStringArray(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        getAccountByTypeAndFeatures(IAccountManagerResponse.Stub.asInterface(data.readStrongBinder()), data.readString(), data.createStringArray(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        getAccountsByFeatures(IAccountManagerResponse.Stub.asInterface(data.readStrongBinder()), data.readString(), data.createStringArray(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Account.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg0 = null;
                        }
                        String _arg16 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result8 = addAccountExplicitly(_arg0, _arg16, _arg22);
                        reply.writeNoException();
                        parcel2.writeInt(_result8);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountManagerResponse _arg06 = IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        removeAccount(_arg06, _arg22, _arg2);
                        reply.writeNoException();
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountManagerResponse _arg07 = IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        removeAccountAsUser(_arg07, _arg22, _arg2, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result9 = removeAccountExplicitly(_arg22);
                        reply.writeNoException();
                        parcel2.writeInt(_result9);
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountManagerResponse _arg08 = IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        copyAccountToUser(_arg08, _arg22, data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        invalidateAuthToken(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        String _result10 = peekAuthToken(_arg22, data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result10);
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        setAuthToken(_arg22, data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        setPassword(_arg22, data.readString());
                        reply.writeNoException();
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        clearPassword(_arg22);
                        reply.writeNoException();
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        setUserData(_arg22, data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        String _arg17 = data.readString();
                        int _arg23 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        updateAppPermission(_arg22, _arg17, _arg23, _arg2);
                        reply.writeNoException();
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountManagerResponse _arg09 = IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg1 = Account.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg1 = null;
                        }
                        String _arg24 = data.readString();
                        boolean _arg32 = data.readInt() != 0;
                        boolean _arg43 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg5 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg5 = null;
                        }
                        getAuthToken(_arg09, _arg1, _arg24, _arg32, _arg43, _arg5);
                        reply.writeNoException();
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountManagerResponse _arg010 = IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                        String _arg18 = data.readString();
                        String _arg25 = data.readString();
                        String[] _arg33 = data.createStringArray();
                        boolean _arg44 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg52 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg52 = null;
                        }
                        addAccount(_arg010, _arg18, _arg25, _arg33, _arg44, _arg52);
                        reply.writeNoException();
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountManagerResponse _arg011 = IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                        String _arg19 = data.readString();
                        String _arg26 = data.readString();
                        String[] _arg34 = data.createStringArray();
                        boolean _arg45 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg53 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg53 = null;
                        }
                        addAccountAsUser(_arg011, _arg19, _arg26, _arg34, _arg45, _arg53, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountManagerResponse _arg012 = IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg12 = Account.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg12 = null;
                        }
                        String _arg27 = data.readString();
                        boolean _arg35 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg4 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg4 = null;
                        }
                        updateCredentials(_arg012, _arg12, _arg27, _arg35, _arg4);
                        reply.writeNoException();
                        return true;
                    case 27:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountManagerResponse _arg013 = IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                        String _arg110 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        editProperties(_arg013, _arg110, _arg2);
                        reply.writeNoException();
                        return true;
                    case 28:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountManagerResponse _arg014 = IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg13 = Account.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg13 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        confirmCredentialsAsUser(_arg014, _arg13, _arg22, data.readInt() != 0, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 29:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result11 = accountAuthenticated(_arg22);
                        reply.writeNoException();
                        parcel2.writeInt(_result11);
                        return true;
                    case 30:
                        parcel.enforceInterface(DESCRIPTOR);
                        getAuthTokenLabel(IAccountManagerResponse.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 31:
                        parcel.enforceInterface(DESCRIPTOR);
                        Account[] _result12 = getSharedAccountsAsUser(data.readInt());
                        reply.writeNoException();
                        parcel2.writeTypedArray(_result12, 1);
                        return true;
                    case 32:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result13 = removeSharedAccountAsUser(_arg22, data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result13);
                        return true;
                    case 33:
                        parcel.enforceInterface(DESCRIPTOR);
                        addSharedAccountsFromParentUser(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 34:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountManagerResponse _arg015 = IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        renameAccount(_arg015, _arg22, data.readString());
                        reply.writeNoException();
                        return true;
                    case 35:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        String _result14 = getPreviousName(_arg22);
                        reply.writeNoException();
                        parcel2.writeString(_result14);
                        return true;
                    case 36:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result15 = renameSharedAccountAsUser(_arg22, data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result15);
                        return true;
                    case 37:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountManagerResponse _arg016 = IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                        String _arg111 = data.readString();
                        String _arg28 = data.readString();
                        String[] _arg36 = data.createStringArray();
                        boolean _arg46 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg54 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg54 = null;
                        }
                        startAddAccountSession(_arg016, _arg111, _arg28, _arg36, _arg46, _arg54);
                        reply.writeNoException();
                        return true;
                    case 38:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountManagerResponse _arg017 = IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg14 = Account.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg14 = null;
                        }
                        String _arg29 = data.readString();
                        boolean _arg37 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg42 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg42 = null;
                        }
                        startUpdateCredentialsSession(_arg017, _arg14, _arg29, _arg37, _arg42);
                        reply.writeNoException();
                        return true;
                    case 39:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountManagerResponse _arg018 = IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg15 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg15 = null;
                        }
                        boolean _arg210 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg3 = null;
                        }
                        finishSessionAsUser(_arg018, _arg15, _arg210, _arg3, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 40:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result16 = someUserHasAccount(_arg22);
                        reply.writeNoException();
                        parcel2.writeInt(_result16);
                        return true;
                    case 41:
                        parcel.enforceInterface(DESCRIPTOR);
                        IAccountManagerResponse _arg019 = IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        isCredentialsUpdateSuggested(_arg019, _arg22, data.readString());
                        reply.writeNoException();
                        return true;
                    case 42:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        Map _result17 = getPackagesAndVisibilityForAccount(_arg22);
                        reply.writeNoException();
                        parcel2.writeMap(_result17);
                        return true;
                    case 43:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = Account.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg02 = null;
                        }
                        String _arg112 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result18 = addAccountExplicitlyWithVisibility(_arg02, _arg112, _arg22, parcel.readHashMap(getClass().getClassLoader()));
                        reply.writeNoException();
                        parcel2.writeInt(_result18);
                        return true;
                    case 44:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result19 = setAccountVisibility(_arg22, data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result19);
                        return true;
                    case 45:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = Account.CREATOR.createFromParcel(parcel);
                        }
                        int _result20 = getAccountVisibility(_arg22, data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result20);
                        return true;
                    case 46:
                        parcel.enforceInterface(DESCRIPTOR);
                        Map _result21 = getAccountsAndVisibilityForPackage(data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeMap(_result21);
                        return true;
                    case 47:
                        parcel.enforceInterface(DESCRIPTOR);
                        registerAccountListener(data.createStringArray(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 48:
                        parcel.enforceInterface(DESCRIPTOR);
                        unregisterAccountListener(data.createStringArray(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 49:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = Account.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg03 = null;
                        }
                        String _arg113 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = (UserHandle) UserHandle.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result22 = hasAccountAccess(_arg03, _arg113, _arg22);
                        reply.writeNoException();
                        parcel2.writeInt(_result22);
                        return true;
                    case 50:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = Account.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg04 = null;
                        }
                        String _arg114 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = (UserHandle) UserHandle.CREATOR.createFromParcel(parcel);
                        }
                        IntentSender _result23 = createRequestAccountAccessIntentSenderAsUser(_arg04, _arg114, _arg22);
                        reply.writeNoException();
                        if (_result23 != null) {
                            parcel2.writeInt(1);
                            _result23.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 51:
                        parcel.enforceInterface(DESCRIPTOR);
                        onAccountAccessed(data.readString());
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    boolean accountAuthenticated(Account account) throws RemoteException;

    void addAccount(IAccountManagerResponse iAccountManagerResponse, String str, String str2, String[] strArr, boolean z, Bundle bundle) throws RemoteException;

    void addAccountAsUser(IAccountManagerResponse iAccountManagerResponse, String str, String str2, String[] strArr, boolean z, Bundle bundle, int i) throws RemoteException;

    boolean addAccountExplicitly(Account account, String str, Bundle bundle) throws RemoteException;

    boolean addAccountExplicitlyWithVisibility(Account account, String str, Bundle bundle, Map map) throws RemoteException;

    void addSharedAccountsFromParentUser(int i, int i2, String str) throws RemoteException;

    void clearPassword(Account account) throws RemoteException;

    void confirmCredentialsAsUser(IAccountManagerResponse iAccountManagerResponse, Account account, Bundle bundle, boolean z, int i) throws RemoteException;

    void copyAccountToUser(IAccountManagerResponse iAccountManagerResponse, Account account, int i, int i2) throws RemoteException;

    IntentSender createRequestAccountAccessIntentSenderAsUser(Account account, String str, UserHandle userHandle) throws RemoteException;

    void editProperties(IAccountManagerResponse iAccountManagerResponse, String str, boolean z) throws RemoteException;

    void finishSessionAsUser(IAccountManagerResponse iAccountManagerResponse, Bundle bundle, boolean z, Bundle bundle2, int i) throws RemoteException;

    void getAccountByTypeAndFeatures(IAccountManagerResponse iAccountManagerResponse, String str, String[] strArr, String str2) throws RemoteException;

    int getAccountVisibility(Account account, String str) throws RemoteException;

    Account[] getAccounts(String str, String str2) throws RemoteException;

    Map getAccountsAndVisibilityForPackage(String str, String str2) throws RemoteException;

    Account[] getAccountsAsUser(String str, int i, String str2) throws RemoteException;

    void getAccountsByFeatures(IAccountManagerResponse iAccountManagerResponse, String str, String[] strArr, String str2) throws RemoteException;

    Account[] getAccountsByTypeForPackage(String str, String str2, String str3) throws RemoteException;

    Account[] getAccountsForPackage(String str, int i, String str2) throws RemoteException;

    void getAuthToken(IAccountManagerResponse iAccountManagerResponse, Account account, String str, boolean z, boolean z2, Bundle bundle) throws RemoteException;

    void getAuthTokenLabel(IAccountManagerResponse iAccountManagerResponse, String str, String str2) throws RemoteException;

    AuthenticatorDescription[] getAuthenticatorTypes(int i) throws RemoteException;

    Map getPackagesAndVisibilityForAccount(Account account) throws RemoteException;

    String getPassword(Account account) throws RemoteException;

    String getPreviousName(Account account) throws RemoteException;

    Account[] getSharedAccountsAsUser(int i) throws RemoteException;

    String getUserData(Account account, String str) throws RemoteException;

    boolean hasAccountAccess(Account account, String str, UserHandle userHandle) throws RemoteException;

    void hasFeatures(IAccountManagerResponse iAccountManagerResponse, Account account, String[] strArr, String str) throws RemoteException;

    void invalidateAuthToken(String str, String str2) throws RemoteException;

    void isCredentialsUpdateSuggested(IAccountManagerResponse iAccountManagerResponse, Account account, String str) throws RemoteException;

    void onAccountAccessed(String str) throws RemoteException;

    String peekAuthToken(Account account, String str) throws RemoteException;

    void registerAccountListener(String[] strArr, String str) throws RemoteException;

    void removeAccount(IAccountManagerResponse iAccountManagerResponse, Account account, boolean z) throws RemoteException;

    void removeAccountAsUser(IAccountManagerResponse iAccountManagerResponse, Account account, boolean z, int i) throws RemoteException;

    boolean removeAccountExplicitly(Account account) throws RemoteException;

    boolean removeSharedAccountAsUser(Account account, int i) throws RemoteException;

    void renameAccount(IAccountManagerResponse iAccountManagerResponse, Account account, String str) throws RemoteException;

    boolean renameSharedAccountAsUser(Account account, String str, int i) throws RemoteException;

    boolean setAccountVisibility(Account account, String str, int i) throws RemoteException;

    void setAuthToken(Account account, String str, String str2) throws RemoteException;

    void setPassword(Account account, String str) throws RemoteException;

    void setUserData(Account account, String str, String str2) throws RemoteException;

    boolean someUserHasAccount(Account account) throws RemoteException;

    void startAddAccountSession(IAccountManagerResponse iAccountManagerResponse, String str, String str2, String[] strArr, boolean z, Bundle bundle) throws RemoteException;

    void startUpdateCredentialsSession(IAccountManagerResponse iAccountManagerResponse, Account account, String str, boolean z, Bundle bundle) throws RemoteException;

    void unregisterAccountListener(String[] strArr, String str) throws RemoteException;

    void updateAppPermission(Account account, String str, int i, boolean z) throws RemoteException;

    void updateCredentials(IAccountManagerResponse iAccountManagerResponse, Account account, String str, boolean z, Bundle bundle) throws RemoteException;
}
