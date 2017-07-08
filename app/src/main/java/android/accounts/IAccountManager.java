package android.accounts;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAccountManager extends IInterface {

    public static abstract class Stub extends Binder implements IAccountManager {
        private static final String DESCRIPTOR = "android.accounts.IAccountManager";
        static final int TRANSACTION_accountAuthenticated = 28;
        static final int TRANSACTION_addAccount = 23;
        static final int TRANSACTION_addAccountAsUser = 24;
        static final int TRANSACTION_addAccountExplicitly = 10;
        static final int TRANSACTION_addSharedAccountsFromParentUser = 32;
        static final int TRANSACTION_clearPassword = 19;
        static final int TRANSACTION_confirmCredentialsAsUser = 27;
        static final int TRANSACTION_copyAccountToUser = 14;
        static final int TRANSACTION_editProperties = 26;
        static final int TRANSACTION_finishSessionAsUser = 38;
        static final int TRANSACTION_getAccounts = 4;
        static final int TRANSACTION_getAccountsAsUser = 7;
        static final int TRANSACTION_getAccountsByFeatures = 9;
        static final int TRANSACTION_getAccountsByTypeForPackage = 6;
        static final int TRANSACTION_getAccountsForPackage = 5;
        static final int TRANSACTION_getAuthToken = 22;
        static final int TRANSACTION_getAuthTokenLabel = 29;
        static final int TRANSACTION_getAuthenticatorTypes = 3;
        static final int TRANSACTION_getPassword = 1;
        static final int TRANSACTION_getPreviousName = 34;
        static final int TRANSACTION_getSharedAccountsAsUser = 30;
        static final int TRANSACTION_getUserData = 2;
        static final int TRANSACTION_hasFeatures = 8;
        static final int TRANSACTION_invalidateAuthToken = 15;
        static final int TRANSACTION_isCredentialsUpdateSuggested = 40;
        static final int TRANSACTION_peekAuthToken = 16;
        static final int TRANSACTION_removeAccount = 11;
        static final int TRANSACTION_removeAccountAsUser = 12;
        static final int TRANSACTION_removeAccountExplicitly = 13;
        static final int TRANSACTION_removeSharedAccountAsUser = 31;
        static final int TRANSACTION_renameAccount = 33;
        static final int TRANSACTION_renameSharedAccountAsUser = 35;
        static final int TRANSACTION_setAuthToken = 17;
        static final int TRANSACTION_setPassword = 18;
        static final int TRANSACTION_setUserData = 20;
        static final int TRANSACTION_someUserHasAccount = 39;
        static final int TRANSACTION_startAddAccountSession = 36;
        static final int TRANSACTION_startUpdateCredentialsSession = 37;
        static final int TRANSACTION_updateAppPermission = 21;
        static final int TRANSACTION_updateCredentials = 25;

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
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getPassword, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(key);
                    this.mRemote.transact(Stub.TRANSACTION_getUserData, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getAuthenticatorTypes, _data, _reply, 0);
                    _reply.readException();
                    AuthenticatorDescription[] _result = (AuthenticatorDescription[]) _reply.createTypedArray(AuthenticatorDescription.CREATOR);
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getAccounts, _data, _reply, 0);
                    _reply.readException();
                    Account[] _result = (Account[]) _reply.createTypedArray(Account.CREATOR);
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getAccountsForPackage, _data, _reply, 0);
                    _reply.readException();
                    Account[] _result = (Account[]) _reply.createTypedArray(Account.CREATOR);
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getAccountsByTypeForPackage, _data, _reply, 0);
                    _reply.readException();
                    Account[] _result = (Account[]) _reply.createTypedArray(Account.CREATOR);
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getAccountsAsUser, _data, _reply, 0);
                    _reply.readException();
                    Account[] _result = (Account[]) _reply.createTypedArray(Account.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void hasFeatures(IAccountManagerResponse response, Account account, String[] features, String opPackageName) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringArray(features);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(Stub.TRANSACTION_hasFeatures, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getAccountsByFeatures(IAccountManagerResponse response, String accountType, String[] features, String opPackageName) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(accountType);
                    _data.writeStringArray(features);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(Stub.TRANSACTION_getAccountsByFeatures, _data, _reply, 0);
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
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(password);
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_addAccountExplicitly, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeAccount(IAccountManagerResponse response, Account account, boolean expectActivityLaunch) throws RemoteException {
                int i = Stub.TRANSACTION_getPassword;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!expectActivityLaunch) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_removeAccount, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeAccountAsUser(IAccountManagerResponse response, Account account, boolean expectActivityLaunch, int userId) throws RemoteException {
                int i = Stub.TRANSACTION_getPassword;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!expectActivityLaunch) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_removeAccountAsUser, _data, _reply, 0);
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
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_removeAccountExplicitly, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void copyAccountToUser(IAccountManagerResponse response, Account account, int userFrom, int userTo) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userFrom);
                    _data.writeInt(userTo);
                    this.mRemote.transact(Stub.TRANSACTION_copyAccountToUser, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_invalidateAuthToken, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authTokenType);
                    this.mRemote.transact(Stub.TRANSACTION_peekAuthToken, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authTokenType);
                    _data.writeString(authToken);
                    this.mRemote.transact(Stub.TRANSACTION_setAuthToken, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(password);
                    this.mRemote.transact(Stub.TRANSACTION_setPassword, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_clearPassword, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(key);
                    _data.writeString(value);
                    this.mRemote.transact(Stub.TRANSACTION_setUserData, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateAppPermission(Account account, String authTokenType, int uid, boolean value) throws RemoteException {
                int i = Stub.TRANSACTION_getPassword;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authTokenType);
                    _data.writeInt(uid);
                    if (!value) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_updateAppPermission, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getAuthToken(IAccountManagerResponse response, Account account, String authTokenType, boolean notifyOnAuthFailure, boolean expectActivityLaunch, Bundle options) throws RemoteException {
                IBinder iBinder = null;
                int i = Stub.TRANSACTION_getPassword;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authTokenType);
                    _data.writeInt(notifyOnAuthFailure ? Stub.TRANSACTION_getPassword : 0);
                    if (!expectActivityLaunch) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (options != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getAuthToken, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addAccount(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch, Bundle options) throws RemoteException {
                int i = Stub.TRANSACTION_getPassword;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(accountType);
                    _data.writeString(authTokenType);
                    _data.writeStringArray(requiredFeatures);
                    if (!expectActivityLaunch) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (options != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_addAccount, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addAccountAsUser(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch, Bundle options, int userId) throws RemoteException {
                int i = Stub.TRANSACTION_getPassword;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(accountType);
                    _data.writeString(authTokenType);
                    _data.writeStringArray(requiredFeatures);
                    if (!expectActivityLaunch) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (options != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_addAccountAsUser, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateCredentials(IAccountManagerResponse response, Account account, String authTokenType, boolean expectActivityLaunch, Bundle options) throws RemoteException {
                int i = Stub.TRANSACTION_getPassword;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authTokenType);
                    if (!expectActivityLaunch) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (options != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_updateCredentials, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void editProperties(IAccountManagerResponse response, String accountType, boolean expectActivityLaunch) throws RemoteException {
                IBinder iBinder = null;
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(accountType);
                    if (expectActivityLaunch) {
                        i = Stub.TRANSACTION_getPassword;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_editProperties, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void confirmCredentialsAsUser(IAccountManagerResponse response, Account account, Bundle options, boolean expectActivityLaunch, int userId) throws RemoteException {
                int i = Stub.TRANSACTION_getPassword;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (options != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!expectActivityLaunch) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_confirmCredentialsAsUser, _data, _reply, 0);
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
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_accountAuthenticated, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getAuthTokenLabel(IAccountManagerResponse response, String accountType, String authTokenType) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(accountType);
                    _data.writeString(authTokenType);
                    this.mRemote.transact(Stub.TRANSACTION_getAuthTokenLabel, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getSharedAccountsAsUser, _data, _reply, 0);
                    _reply.readException();
                    Account[] _result = (Account[]) _reply.createTypedArray(Account.CREATOR);
                    return _result;
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
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_removeSharedAccountAsUser, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addSharedAccountsFromParentUser(int parentUserId, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(parentUserId);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_addSharedAccountsFromParentUser, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void renameAccount(IAccountManagerResponse response, Account accountToRename, String newName) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (accountToRename != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        accountToRename.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(newName);
                    this.mRemote.transact(Stub.TRANSACTION_renameAccount, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getPreviousName, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                    if (accountToRename != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        accountToRename.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(newName);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_renameSharedAccountAsUser, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startAddAccountSession(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch, Bundle options) throws RemoteException {
                int i = Stub.TRANSACTION_getPassword;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(accountType);
                    _data.writeString(authTokenType);
                    _data.writeStringArray(requiredFeatures);
                    if (!expectActivityLaunch) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (options != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_startAddAccountSession, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startUpdateCredentialsSession(IAccountManagerResponse response, Account account, String authTokenType, boolean expectActivityLaunch, Bundle options) throws RemoteException {
                int i = Stub.TRANSACTION_getPassword;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(authTokenType);
                    if (!expectActivityLaunch) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (options != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_startUpdateCredentialsSession, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void finishSessionAsUser(IAccountManagerResponse response, Bundle sessionBundle, boolean expectActivityLaunch, Bundle appInfo, int userId) throws RemoteException {
                int i = Stub.TRANSACTION_getPassword;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (sessionBundle != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        sessionBundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!expectActivityLaunch) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (appInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        appInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_finishSessionAsUser, _data, _reply, 0);
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
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_someUserHasAccount, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void isCredentialsUpdateSuggested(IAccountManagerResponse response, Account account, String statusToken) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (response != null) {
                        iBinder = response.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_getPassword);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(statusToken);
                    this.mRemote.transact(Stub.TRANSACTION_isCredentialsUpdateSuggested, _data, _reply, 0);
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Account account;
            String _result;
            Account[] _result2;
            IAccountManagerResponse _arg0;
            Account account2;
            String _arg1;
            Bundle bundle;
            boolean _result3;
            String _arg2;
            boolean _arg3;
            boolean _arg4;
            Bundle bundle2;
            String[] _arg32;
            Bundle bundle3;
            switch (code) {
                case TRANSACTION_getPassword /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _result = getPassword(account);
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_getUserData /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _result = getUserData(account, data.readString());
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_getAuthenticatorTypes /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    AuthenticatorDescription[] _result4 = getAuthenticatorTypes(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedArray(_result4, TRANSACTION_getPassword);
                    return true;
                case TRANSACTION_getAccounts /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getAccounts(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeTypedArray(_result2, TRANSACTION_getPassword);
                    return true;
                case TRANSACTION_getAccountsForPackage /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getAccountsForPackage(data.readString(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeTypedArray(_result2, TRANSACTION_getPassword);
                    return true;
                case TRANSACTION_getAccountsByTypeForPackage /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getAccountsByTypeForPackage(data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeTypedArray(_result2, TRANSACTION_getPassword);
                    return true;
                case TRANSACTION_getAccountsAsUser /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getAccountsAsUser(data.readString(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeTypedArray(_result2, TRANSACTION_getPassword);
                    return true;
                case TRANSACTION_hasFeatures /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        account2 = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account2 = null;
                    }
                    hasFeatures(_arg0, account2, data.createStringArray(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getAccountsByFeatures /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    getAccountsByFeatures(android.accounts.IAccountManagerResponse.Stub.asInterface(data.readStrongBinder()), data.readString(), data.createStringArray(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addAccountExplicitly /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result3 = addAccountExplicitly(account, _arg1, bundle);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getPassword : 0);
                    return true;
                case TRANSACTION_removeAccount /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        account2 = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account2 = null;
                    }
                    removeAccount(_arg0, account2, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeAccountAsUser /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        account2 = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account2 = null;
                    }
                    removeAccountAsUser(_arg0, account2, data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeAccountExplicitly /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _result3 = removeAccountExplicitly(account);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getPassword : 0);
                    return true;
                case TRANSACTION_copyAccountToUser /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        account2 = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account2 = null;
                    }
                    copyAccountToUser(_arg0, account2, data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_invalidateAuthToken /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    invalidateAuthToken(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_peekAuthToken /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _result = peekAuthToken(account, data.readString());
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_setAuthToken /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    setAuthToken(account, data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setPassword /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    setPassword(account, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearPassword /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    clearPassword(account);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setUserData /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    setUserData(account, data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_updateAppPermission /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    updateAppPermission(account, data.readString(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getAuthToken /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        account2 = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account2 = null;
                    }
                    _arg2 = data.readString();
                    _arg3 = data.readInt() != 0;
                    _arg4 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle2 = null;
                    }
                    getAuthToken(_arg0, account2, _arg2, _arg3, _arg4, bundle2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addAccount /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                    _arg1 = data.readString();
                    _arg2 = data.readString();
                    _arg32 = data.createStringArray();
                    _arg4 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle2 = null;
                    }
                    addAccount(_arg0, _arg1, _arg2, _arg32, _arg4, bundle2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addAccountAsUser /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                    _arg1 = data.readString();
                    _arg2 = data.readString();
                    _arg32 = data.createStringArray();
                    _arg4 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle2 = null;
                    }
                    addAccountAsUser(_arg0, _arg1, _arg2, _arg32, _arg4, bundle2, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_updateCredentials /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        account2 = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account2 = null;
                    }
                    _arg2 = data.readString();
                    _arg3 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        bundle3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle3 = null;
                    }
                    updateCredentials(_arg0, account2, _arg2, _arg3, bundle3);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_editProperties /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    editProperties(android.accounts.IAccountManagerResponse.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_confirmCredentialsAsUser /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        account2 = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account2 = null;
                    }
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    confirmCredentialsAsUser(_arg0, account2, bundle, data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_accountAuthenticated /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _result3 = accountAuthenticated(account);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getPassword : 0);
                    return true;
                case TRANSACTION_getAuthTokenLabel /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    getAuthTokenLabel(android.accounts.IAccountManagerResponse.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getSharedAccountsAsUser /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getSharedAccountsAsUser(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedArray(_result2, TRANSACTION_getPassword);
                    return true;
                case TRANSACTION_removeSharedAccountAsUser /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _result3 = removeSharedAccountAsUser(account, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getPassword : 0);
                    return true;
                case TRANSACTION_addSharedAccountsFromParentUser /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    addSharedAccountsFromParentUser(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_renameAccount /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        account2 = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account2 = null;
                    }
                    renameAccount(_arg0, account2, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getPreviousName /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _result = getPreviousName(account);
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case TRANSACTION_renameSharedAccountAsUser /*35*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _result3 = renameSharedAccountAsUser(account, data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getPassword : 0);
                    return true;
                case TRANSACTION_startAddAccountSession /*36*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                    _arg1 = data.readString();
                    _arg2 = data.readString();
                    _arg32 = data.createStringArray();
                    _arg4 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle2 = null;
                    }
                    startAddAccountSession(_arg0, _arg1, _arg2, _arg32, _arg4, bundle2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_startUpdateCredentialsSession /*37*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        account2 = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account2 = null;
                    }
                    _arg2 = data.readString();
                    _arg3 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        bundle3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle3 = null;
                    }
                    startUpdateCredentialsSession(_arg0, account2, _arg2, _arg3, bundle3);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_finishSessionAsUser /*38*/:
                    Bundle bundle4;
                    Bundle bundle5;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        bundle4 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle4 = null;
                    }
                    boolean _arg22 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        bundle5 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle5 = null;
                    }
                    finishSessionAsUser(_arg0, bundle4, _arg22, bundle5, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_someUserHasAccount /*39*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account = null;
                    }
                    _result3 = someUserHasAccount(account);
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_getPassword : 0);
                    return true;
                case TRANSACTION_isCredentialsUpdateSuggested /*40*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.accounts.IAccountManagerResponse.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        account2 = (Account) Account.CREATOR.createFromParcel(data);
                    } else {
                        account2 = null;
                    }
                    isCredentialsUpdateSuggested(_arg0, account2, data.readString());
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean accountAuthenticated(Account account) throws RemoteException;

    void addAccount(IAccountManagerResponse iAccountManagerResponse, String str, String str2, String[] strArr, boolean z, Bundle bundle) throws RemoteException;

    void addAccountAsUser(IAccountManagerResponse iAccountManagerResponse, String str, String str2, String[] strArr, boolean z, Bundle bundle, int i) throws RemoteException;

    boolean addAccountExplicitly(Account account, String str, Bundle bundle) throws RemoteException;

    void addSharedAccountsFromParentUser(int i, int i2) throws RemoteException;

    void clearPassword(Account account) throws RemoteException;

    void confirmCredentialsAsUser(IAccountManagerResponse iAccountManagerResponse, Account account, Bundle bundle, boolean z, int i) throws RemoteException;

    void copyAccountToUser(IAccountManagerResponse iAccountManagerResponse, Account account, int i, int i2) throws RemoteException;

    void editProperties(IAccountManagerResponse iAccountManagerResponse, String str, boolean z) throws RemoteException;

    void finishSessionAsUser(IAccountManagerResponse iAccountManagerResponse, Bundle bundle, boolean z, Bundle bundle2, int i) throws RemoteException;

    Account[] getAccounts(String str, String str2) throws RemoteException;

    Account[] getAccountsAsUser(String str, int i, String str2) throws RemoteException;

    void getAccountsByFeatures(IAccountManagerResponse iAccountManagerResponse, String str, String[] strArr, String str2) throws RemoteException;

    Account[] getAccountsByTypeForPackage(String str, String str2, String str3) throws RemoteException;

    Account[] getAccountsForPackage(String str, int i, String str2) throws RemoteException;

    void getAuthToken(IAccountManagerResponse iAccountManagerResponse, Account account, String str, boolean z, boolean z2, Bundle bundle) throws RemoteException;

    void getAuthTokenLabel(IAccountManagerResponse iAccountManagerResponse, String str, String str2) throws RemoteException;

    AuthenticatorDescription[] getAuthenticatorTypes(int i) throws RemoteException;

    String getPassword(Account account) throws RemoteException;

    String getPreviousName(Account account) throws RemoteException;

    Account[] getSharedAccountsAsUser(int i) throws RemoteException;

    String getUserData(Account account, String str) throws RemoteException;

    void hasFeatures(IAccountManagerResponse iAccountManagerResponse, Account account, String[] strArr, String str) throws RemoteException;

    void invalidateAuthToken(String str, String str2) throws RemoteException;

    void isCredentialsUpdateSuggested(IAccountManagerResponse iAccountManagerResponse, Account account, String str) throws RemoteException;

    String peekAuthToken(Account account, String str) throws RemoteException;

    void removeAccount(IAccountManagerResponse iAccountManagerResponse, Account account, boolean z) throws RemoteException;

    void removeAccountAsUser(IAccountManagerResponse iAccountManagerResponse, Account account, boolean z, int i) throws RemoteException;

    boolean removeAccountExplicitly(Account account) throws RemoteException;

    boolean removeSharedAccountAsUser(Account account, int i) throws RemoteException;

    void renameAccount(IAccountManagerResponse iAccountManagerResponse, Account account, String str) throws RemoteException;

    boolean renameSharedAccountAsUser(Account account, String str, int i) throws RemoteException;

    void setAuthToken(Account account, String str, String str2) throws RemoteException;

    void setPassword(Account account, String str) throws RemoteException;

    void setUserData(Account account, String str, String str2) throws RemoteException;

    boolean someUserHasAccount(Account account) throws RemoteException;

    void startAddAccountSession(IAccountManagerResponse iAccountManagerResponse, String str, String str2, String[] strArr, boolean z, Bundle bundle) throws RemoteException;

    void startUpdateCredentialsSession(IAccountManagerResponse iAccountManagerResponse, Account account, String str, boolean z, Bundle bundle) throws RemoteException;

    void updateAppPermission(Account account, String str, int i, boolean z) throws RemoteException;

    void updateCredentials(IAccountManagerResponse iAccountManagerResponse, Account account, String str, boolean z, Bundle bundle) throws RemoteException;
}
