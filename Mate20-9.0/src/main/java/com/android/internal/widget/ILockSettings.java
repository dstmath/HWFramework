package com.android.internal.widget;

import android.app.PendingIntent;
import android.app.trust.IStrongAuthTracker;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.security.keystore.recovery.KeyChainProtectionParams;
import android.security.keystore.recovery.KeyChainSnapshot;
import android.security.keystore.recovery.RecoveryCertPath;
import android.security.keystore.recovery.WrappedApplicationKey;
import com.android.internal.widget.ICheckCredentialProgressCallback;
import java.util.List;
import java.util.Map;

public interface ILockSettings extends IInterface {

    public static abstract class Stub extends Binder implements ILockSettings {
        private static final String DESCRIPTOR = "com.android.internal.widget.ILockSettings";
        static final int TRANSACTION_checkCredential = 9;
        static final int TRANSACTION_checkVoldPassword = 12;
        static final int TRANSACTION_clearExtendLockScreenPassword = 40;
        static final int TRANSACTION_closeSession = 38;
        static final int TRANSACTION_generateKey = 26;
        static final int TRANSACTION_getBoolean = 4;
        static final int TRANSACTION_getHashFactor = 15;
        static final int TRANSACTION_getKey = 28;
        static final int TRANSACTION_getKeyChainSnapshot = 25;
        static final int TRANSACTION_getLong = 5;
        static final int TRANSACTION_getRecoverySecretTypes = 35;
        static final int TRANSACTION_getRecoveryStatus = 33;
        static final int TRANSACTION_getSeparateProfileChallengeEnabled = 17;
        static final int TRANSACTION_getString = 6;
        static final int TRANSACTION_getStrongAuthForUser = 23;
        static final int TRANSACTION_havePassword = 14;
        static final int TRANSACTION_havePattern = 13;
        static final int TRANSACTION_importKey = 27;
        static final int TRANSACTION_initRecoveryServiceWithSigFile = 24;
        static final int TRANSACTION_recoverKeyChainSnapshot = 37;
        static final int TRANSACTION_registerStrongAuthTracker = 18;
        static final int TRANSACTION_removeKey = 29;
        static final int TRANSACTION_requireStrongAuth = 20;
        static final int TRANSACTION_resetKeyStore = 8;
        static final int TRANSACTION_setBoolean = 1;
        static final int TRANSACTION_setExtendLockScreenPassword = 39;
        static final int TRANSACTION_setLockCredential = 7;
        static final int TRANSACTION_setLong = 2;
        static final int TRANSACTION_setRecoverySecretTypes = 34;
        static final int TRANSACTION_setRecoveryStatus = 32;
        static final int TRANSACTION_setSeparateProfileChallengeEnabled = 16;
        static final int TRANSACTION_setServerParams = 31;
        static final int TRANSACTION_setSnapshotCreatedPendingIntent = 30;
        static final int TRANSACTION_setString = 3;
        static final int TRANSACTION_startRecoverySessionWithCertPath = 36;
        static final int TRANSACTION_systemReady = 21;
        static final int TRANSACTION_unregisterStrongAuthTracker = 19;
        static final int TRANSACTION_userPresent = 22;
        static final int TRANSACTION_verifyCredential = 10;
        static final int TRANSACTION_verifyTiedProfileChallenge = 11;

        private static class Proxy implements ILockSettings {
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

            public void setBoolean(String key, boolean value, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeInt(value);
                    _data.writeInt(userId);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setLong(String key, long value, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeLong(value);
                    _data.writeInt(userId);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setString(String key, String value, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeString(value);
                    _data.writeInt(userId);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getBoolean(String key, boolean defaultValue, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeInt(defaultValue);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getLong(String key, long defaultValue, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeLong(defaultValue);
                    _data.writeInt(userId);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getString(String key, String defaultValue, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeString(defaultValue);
                    _data.writeInt(userId);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setLockCredential(String credential, int type, String savedCredential, int requestedQuality, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(credential);
                    _data.writeInt(type);
                    _data.writeString(savedCredential);
                    _data.writeInt(requestedQuality);
                    _data.writeInt(userId);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void resetKeyStore(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public VerifyCredentialResponse checkCredential(String credential, int type, int userId, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(credential);
                    _data.writeInt(type);
                    _data.writeInt(userId);
                    VerifyCredentialResponse _result = null;
                    _data.writeStrongBinder(progressCallback != null ? progressCallback.asBinder() : null);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = VerifyCredentialResponse.CREATOR.createFromParcel(_reply);
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public VerifyCredentialResponse verifyCredential(String credential, int type, long challenge, int userId) throws RemoteException {
                VerifyCredentialResponse _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(credential);
                    _data.writeInt(type);
                    _data.writeLong(challenge);
                    _data.writeInt(userId);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = VerifyCredentialResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public VerifyCredentialResponse verifyTiedProfileChallenge(String credential, int type, long challenge, int userId) throws RemoteException {
                VerifyCredentialResponse _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(credential);
                    _data.writeInt(type);
                    _data.writeLong(challenge);
                    _data.writeInt(userId);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = VerifyCredentialResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean checkVoldPassword(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean havePattern(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean havePassword(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getHashFactor(String currentCredential, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(currentCredential);
                    _data.writeInt(userId);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createByteArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSeparateProfileChallengeEnabled(int userId, boolean enabled, String managedUserPassword) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(enabled);
                    _data.writeString(managedUserPassword);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getSeparateProfileChallengeEnabled(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerStrongAuthTracker(IStrongAuthTracker tracker) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(tracker != null ? tracker.asBinder() : null);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterStrongAuthTracker(IStrongAuthTracker tracker) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(tracker != null ? tracker.asBinder() : null);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requireStrongAuth(int strongAuthReason, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(strongAuthReason);
                    _data.writeInt(userId);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void systemReady() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void userPresent(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getStrongAuthForUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void initRecoveryServiceWithSigFile(String rootCertificateAlias, byte[] recoveryServiceCertFile, byte[] recoveryServiceSigFile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(rootCertificateAlias);
                    _data.writeByteArray(recoveryServiceCertFile);
                    _data.writeByteArray(recoveryServiceSigFile);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public KeyChainSnapshot getKeyChainSnapshot() throws RemoteException {
                KeyChainSnapshot _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (KeyChainSnapshot) KeyChainSnapshot.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String generateKey(String alias) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String importKey(String alias, byte[] keyBytes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    _data.writeByteArray(keyBytes);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getKey(String alias) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeKey(String alias) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSnapshotCreatedPendingIntent(PendingIntent intent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setServerParams(byte[] serverParams) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(serverParams);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setRecoveryStatus(String alias, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    _data.writeInt(status);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Map getRecoveryStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readHashMap(getClass().getClassLoader());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setRecoverySecretTypes(int[] secretTypes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(secretTypes);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getRecoverySecretTypes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] startRecoverySessionWithCertPath(String sessionId, String rootCertificateAlias, RecoveryCertPath verifierCertPath, byte[] vaultParams, byte[] vaultChallenge, List<KeyChainProtectionParams> secrets) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sessionId);
                    _data.writeString(rootCertificateAlias);
                    if (verifierCertPath != null) {
                        _data.writeInt(1);
                        verifierCertPath.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeByteArray(vaultParams);
                    _data.writeByteArray(vaultChallenge);
                    _data.writeTypedList(secrets);
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createByteArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Map recoverKeyChainSnapshot(String sessionId, byte[] recoveryKeyBlob, List<WrappedApplicationKey> applicationKeys) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sessionId);
                    _data.writeByteArray(recoveryKeyBlob);
                    _data.writeTypedList(applicationKeys);
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readHashMap(getClass().getClassLoader());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void closeSession(String sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sessionId);
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setExtendLockScreenPassword(String password, String phoneNumber, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    _data.writeString(phoneNumber);
                    _data.writeInt(userHandle);
                    boolean _result = false;
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean clearExtendLockScreenPassword(String password, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    _data.writeInt(userHandle);
                    boolean _result = false;
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ILockSettings asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ILockSettings)) {
                return new Proxy(obj);
            }
            return (ILockSettings) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                PendingIntent _arg0 = null;
                boolean _arg1 = false;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setBoolean(_arg02, _arg1, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        setLong(data.readString(), data.readLong(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        setString(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean _result = getBoolean(_arg03, _arg1, data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        long _result2 = getLong(data.readString(), data.readLong(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeLong(_result2);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result3 = getString(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result3);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        setLockCredential(data.readString(), data.readInt(), data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        resetKeyStore(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        VerifyCredentialResponse _result4 = checkCredential(data.readString(), data.readInt(), data.readInt(), ICheckCredentialProgressCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        if (_result4 != null) {
                            parcel2.writeInt(1);
                            _result4.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        VerifyCredentialResponse _result5 = verifyCredential(data.readString(), data.readInt(), data.readLong(), data.readInt());
                        reply.writeNoException();
                        if (_result5 != null) {
                            parcel2.writeInt(1);
                            _result5.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        VerifyCredentialResponse _result6 = verifyTiedProfileChallenge(data.readString(), data.readInt(), data.readLong(), data.readInt());
                        reply.writeNoException();
                        if (_result6 != null) {
                            parcel2.writeInt(1);
                            _result6.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result7 = checkVoldPassword(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result7);
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result8 = havePattern(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result8);
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result9 = havePassword(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result9);
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        byte[] _result10 = getHashFactor(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeByteArray(_result10);
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setSeparateProfileChallengeEnabled(_arg04, _arg1, data.readString());
                        reply.writeNoException();
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result11 = getSeparateProfileChallengeEnabled(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result11);
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        registerStrongAuthTracker(IStrongAuthTracker.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        unregisterStrongAuthTracker(IStrongAuthTracker.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        requireStrongAuth(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        systemReady();
                        reply.writeNoException();
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        userPresent(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result12 = getStrongAuthForUser(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result12);
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        initRecoveryServiceWithSigFile(data.readString(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        KeyChainSnapshot _result13 = getKeyChainSnapshot();
                        reply.writeNoException();
                        if (_result13 != null) {
                            parcel2.writeInt(1);
                            _result13.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result14 = generateKey(data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result14);
                        return true;
                    case 27:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result15 = importKey(data.readString(), data.createByteArray());
                        reply.writeNoException();
                        parcel2.writeString(_result15);
                        return true;
                    case 28:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result16 = getKey(data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result16);
                        return true;
                    case 29:
                        parcel.enforceInterface(DESCRIPTOR);
                        removeKey(data.readString());
                        reply.writeNoException();
                        return true;
                    case 30:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(parcel);
                        }
                        setSnapshotCreatedPendingIntent(_arg0);
                        reply.writeNoException();
                        return true;
                    case 31:
                        parcel.enforceInterface(DESCRIPTOR);
                        setServerParams(data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 32:
                        parcel.enforceInterface(DESCRIPTOR);
                        setRecoveryStatus(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 33:
                        parcel.enforceInterface(DESCRIPTOR);
                        Map _result17 = getRecoveryStatus();
                        reply.writeNoException();
                        parcel2.writeMap(_result17);
                        return true;
                    case 34:
                        parcel.enforceInterface(DESCRIPTOR);
                        setRecoverySecretTypes(data.createIntArray());
                        reply.writeNoException();
                        return true;
                    case 35:
                        parcel.enforceInterface(DESCRIPTOR);
                        int[] _result18 = getRecoverySecretTypes();
                        reply.writeNoException();
                        parcel2.writeIntArray(_result18);
                        return true;
                    case 36:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        String _arg12 = data.readString();
                        if (data.readInt() != 0) {
                            _arg0 = (RecoveryCertPath) RecoveryCertPath.CREATOR.createFromParcel(parcel);
                        }
                        byte[] _result19 = startRecoverySessionWithCertPath(_arg05, _arg12, _arg0, data.createByteArray(), data.createByteArray(), parcel.createTypedArrayList(KeyChainProtectionParams.CREATOR));
                        reply.writeNoException();
                        parcel2.writeByteArray(_result19);
                        return true;
                    case 37:
                        parcel.enforceInterface(DESCRIPTOR);
                        Map _result20 = recoverKeyChainSnapshot(data.readString(), data.createByteArray(), parcel.createTypedArrayList(WrappedApplicationKey.CREATOR));
                        reply.writeNoException();
                        parcel2.writeMap(_result20);
                        return true;
                    case 38:
                        parcel.enforceInterface(DESCRIPTOR);
                        closeSession(data.readString());
                        reply.writeNoException();
                        return true;
                    case 39:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result21 = setExtendLockScreenPassword(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result21);
                        return true;
                    case 40:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result22 = clearExtendLockScreenPassword(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result22);
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

    VerifyCredentialResponse checkCredential(String str, int i, int i2, ICheckCredentialProgressCallback iCheckCredentialProgressCallback) throws RemoteException;

    boolean checkVoldPassword(int i) throws RemoteException;

    boolean clearExtendLockScreenPassword(String str, int i) throws RemoteException;

    void closeSession(String str) throws RemoteException;

    String generateKey(String str) throws RemoteException;

    boolean getBoolean(String str, boolean z, int i) throws RemoteException;

    byte[] getHashFactor(String str, int i) throws RemoteException;

    String getKey(String str) throws RemoteException;

    KeyChainSnapshot getKeyChainSnapshot() throws RemoteException;

    long getLong(String str, long j, int i) throws RemoteException;

    int[] getRecoverySecretTypes() throws RemoteException;

    Map getRecoveryStatus() throws RemoteException;

    boolean getSeparateProfileChallengeEnabled(int i) throws RemoteException;

    String getString(String str, String str2, int i) throws RemoteException;

    int getStrongAuthForUser(int i) throws RemoteException;

    boolean havePassword(int i) throws RemoteException;

    boolean havePattern(int i) throws RemoteException;

    String importKey(String str, byte[] bArr) throws RemoteException;

    void initRecoveryServiceWithSigFile(String str, byte[] bArr, byte[] bArr2) throws RemoteException;

    Map recoverKeyChainSnapshot(String str, byte[] bArr, List<WrappedApplicationKey> list) throws RemoteException;

    void registerStrongAuthTracker(IStrongAuthTracker iStrongAuthTracker) throws RemoteException;

    void removeKey(String str) throws RemoteException;

    void requireStrongAuth(int i, int i2) throws RemoteException;

    void resetKeyStore(int i) throws RemoteException;

    void setBoolean(String str, boolean z, int i) throws RemoteException;

    boolean setExtendLockScreenPassword(String str, String str2, int i) throws RemoteException;

    void setLockCredential(String str, int i, String str2, int i2, int i3) throws RemoteException;

    void setLong(String str, long j, int i) throws RemoteException;

    void setRecoverySecretTypes(int[] iArr) throws RemoteException;

    void setRecoveryStatus(String str, int i) throws RemoteException;

    void setSeparateProfileChallengeEnabled(int i, boolean z, String str) throws RemoteException;

    void setServerParams(byte[] bArr) throws RemoteException;

    void setSnapshotCreatedPendingIntent(PendingIntent pendingIntent) throws RemoteException;

    void setString(String str, String str2, int i) throws RemoteException;

    byte[] startRecoverySessionWithCertPath(String str, String str2, RecoveryCertPath recoveryCertPath, byte[] bArr, byte[] bArr2, List<KeyChainProtectionParams> list) throws RemoteException;

    void systemReady() throws RemoteException;

    void unregisterStrongAuthTracker(IStrongAuthTracker iStrongAuthTracker) throws RemoteException;

    void userPresent(int i) throws RemoteException;

    VerifyCredentialResponse verifyCredential(String str, int i, long j, int i2) throws RemoteException;

    VerifyCredentialResponse verifyTiedProfileChallenge(String str, int i, long j, int i2) throws RemoteException;
}
