package com.android.internal.widget;

import android.annotation.UnsupportedAppUsage;
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
    VerifyCredentialResponse checkCredential(byte[] bArr, int i, int i2, ICheckCredentialProgressCallback iCheckCredentialProgressCallback) throws RemoteException;

    boolean checkVoldPassword(int i) throws RemoteException;

    boolean clearExtendLockScreenPassword(String str, int i) throws RemoteException;

    void closeSession(String str) throws RemoteException;

    String generateKey(String str) throws RemoteException;

    String generateKeyWithMetadata(String str, byte[] bArr) throws RemoteException;

    @UnsupportedAppUsage
    boolean getBoolean(String str, boolean z, int i) throws RemoteException;

    byte[] getHashFactor(byte[] bArr, int i) throws RemoteException;

    String getKey(String str) throws RemoteException;

    KeyChainSnapshot getKeyChainSnapshot() throws RemoteException;

    @UnsupportedAppUsage
    long getLong(String str, long j, int i) throws RemoteException;

    int getPasswordBackendStatus() throws RemoteException;

    int[] getRecoverySecretTypes() throws RemoteException;

    Map getRecoveryStatus() throws RemoteException;

    int getRemainAllowedRetryCount(int i) throws RemoteException;

    long getRemainLockedTime(int i) throws RemoteException;

    boolean getSeparateProfileChallengeEnabled(int i) throws RemoteException;

    @UnsupportedAppUsage
    String getString(String str, String str2, int i) throws RemoteException;

    int getStrongAuthForUser(int i) throws RemoteException;

    int getStrongAuthSolution(int i) throws RemoteException;

    boolean hasPendingEscrowToken(int i) throws RemoteException;

    @UnsupportedAppUsage
    boolean havePassword(int i) throws RemoteException;

    @UnsupportedAppUsage
    boolean havePattern(int i) throws RemoteException;

    String importKey(String str, byte[] bArr) throws RemoteException;

    String importKeyWithMetadata(String str, byte[] bArr, byte[] bArr2) throws RemoteException;

    void initRecoveryServiceWithSigFile(String str, byte[] bArr, byte[] bArr2) throws RemoteException;

    Map recoverKeyChainSnapshot(String str, byte[] bArr, List<WrappedApplicationKey> list) throws RemoteException;

    void registerStrongAuthTracker(IStrongAuthTracker iStrongAuthTracker) throws RemoteException;

    void removeKey(String str) throws RemoteException;

    void requireStrongAuth(int i, int i2) throws RemoteException;

    void resetKeyStore(int i) throws RemoteException;

    @UnsupportedAppUsage
    void setBoolean(String str, boolean z, int i) throws RemoteException;

    boolean setExtendLockScreenPassword(String str, String str2, int i) throws RemoteException;

    void setLockCredential(byte[] bArr, int i, byte[] bArr2, int i2, int i3, boolean z) throws RemoteException;

    @UnsupportedAppUsage
    void setLong(String str, long j, int i) throws RemoteException;

    void setRecoverySecretTypes(int[] iArr) throws RemoteException;

    void setRecoveryStatus(String str, int i) throws RemoteException;

    void setSeparateProfileChallengeEnabled(int i, boolean z, byte[] bArr) throws RemoteException;

    void setServerParams(byte[] bArr) throws RemoteException;

    void setSnapshotCreatedPendingIntent(PendingIntent pendingIntent) throws RemoteException;

    @UnsupportedAppUsage
    void setString(String str, String str2, int i) throws RemoteException;

    byte[] startRecoverySessionWithCertPath(String str, String str2, RecoveryCertPath recoveryCertPath, byte[] bArr, byte[] bArr2, List<KeyChainProtectionParams> list) throws RemoteException;

    void systemReady() throws RemoteException;

    void unregisterStrongAuthTracker(IStrongAuthTracker iStrongAuthTracker) throws RemoteException;

    void userPresent(int i) throws RemoteException;

    VerifyCredentialResponse verifyCredential(byte[] bArr, int i, long j, int i2) throws RemoteException;

    VerifyCredentialResponse verifyTiedProfileChallenge(byte[] bArr, int i, long j, int i2) throws RemoteException;

    public static class Default implements ILockSettings {
        @Override // com.android.internal.widget.ILockSettings
        public void setBoolean(String key, boolean value, int userId) throws RemoteException {
        }

        @Override // com.android.internal.widget.ILockSettings
        public void setLong(String key, long value, int userId) throws RemoteException {
        }

        @Override // com.android.internal.widget.ILockSettings
        public void setString(String key, String value, int userId) throws RemoteException {
        }

        @Override // com.android.internal.widget.ILockSettings
        public boolean getBoolean(String key, boolean defaultValue, int userId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.widget.ILockSettings
        public long getLong(String key, long defaultValue, int userId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.widget.ILockSettings
        public String getString(String key, String defaultValue, int userId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.widget.ILockSettings
        public void setLockCredential(byte[] credential, int type, byte[] savedCredential, int requestedQuality, int userId, boolean allowUntrustedChange) throws RemoteException {
        }

        @Override // com.android.internal.widget.ILockSettings
        public void resetKeyStore(int userId) throws RemoteException {
        }

        @Override // com.android.internal.widget.ILockSettings
        public VerifyCredentialResponse checkCredential(byte[] credential, int type, int userId, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.widget.ILockSettings
        public VerifyCredentialResponse verifyCredential(byte[] credential, int type, long challenge, int userId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.widget.ILockSettings
        public VerifyCredentialResponse verifyTiedProfileChallenge(byte[] credential, int type, long challenge, int userId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.widget.ILockSettings
        public boolean checkVoldPassword(int userId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.widget.ILockSettings
        public boolean havePattern(int userId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.widget.ILockSettings
        public boolean havePassword(int userId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.widget.ILockSettings
        public byte[] getHashFactor(byte[] currentCredential, int userId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.widget.ILockSettings
        public void setSeparateProfileChallengeEnabled(int userId, boolean enabled, byte[] managedUserPassword) throws RemoteException {
        }

        @Override // com.android.internal.widget.ILockSettings
        public boolean getSeparateProfileChallengeEnabled(int userId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.widget.ILockSettings
        public void registerStrongAuthTracker(IStrongAuthTracker tracker) throws RemoteException {
        }

        @Override // com.android.internal.widget.ILockSettings
        public void unregisterStrongAuthTracker(IStrongAuthTracker tracker) throws RemoteException {
        }

        @Override // com.android.internal.widget.ILockSettings
        public void requireStrongAuth(int strongAuthReason, int userId) throws RemoteException {
        }

        @Override // com.android.internal.widget.ILockSettings
        public void systemReady() throws RemoteException {
        }

        @Override // com.android.internal.widget.ILockSettings
        public void userPresent(int userId) throws RemoteException {
        }

        @Override // com.android.internal.widget.ILockSettings
        public int getStrongAuthForUser(int userId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.widget.ILockSettings
        public boolean hasPendingEscrowToken(int userId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.widget.ILockSettings
        public void initRecoveryServiceWithSigFile(String rootCertificateAlias, byte[] recoveryServiceCertFile, byte[] recoveryServiceSigFile) throws RemoteException {
        }

        @Override // com.android.internal.widget.ILockSettings
        public KeyChainSnapshot getKeyChainSnapshot() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.widget.ILockSettings
        public String generateKey(String alias) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.widget.ILockSettings
        public String generateKeyWithMetadata(String alias, byte[] metadata) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.widget.ILockSettings
        public String importKey(String alias, byte[] keyBytes) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.widget.ILockSettings
        public String importKeyWithMetadata(String alias, byte[] keyBytes, byte[] metadata) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.widget.ILockSettings
        public String getKey(String alias) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.widget.ILockSettings
        public void removeKey(String alias) throws RemoteException {
        }

        @Override // com.android.internal.widget.ILockSettings
        public void setSnapshotCreatedPendingIntent(PendingIntent intent) throws RemoteException {
        }

        @Override // com.android.internal.widget.ILockSettings
        public void setServerParams(byte[] serverParams) throws RemoteException {
        }

        @Override // com.android.internal.widget.ILockSettings
        public void setRecoveryStatus(String alias, int status) throws RemoteException {
        }

        @Override // com.android.internal.widget.ILockSettings
        public Map getRecoveryStatus() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.widget.ILockSettings
        public void setRecoverySecretTypes(int[] secretTypes) throws RemoteException {
        }

        @Override // com.android.internal.widget.ILockSettings
        public int[] getRecoverySecretTypes() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.widget.ILockSettings
        public byte[] startRecoverySessionWithCertPath(String sessionId, String rootCertificateAlias, RecoveryCertPath verifierCertPath, byte[] vaultParams, byte[] vaultChallenge, List<KeyChainProtectionParams> list) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.widget.ILockSettings
        public Map recoverKeyChainSnapshot(String sessionId, byte[] recoveryKeyBlob, List<WrappedApplicationKey> list) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.widget.ILockSettings
        public void closeSession(String sessionId) throws RemoteException {
        }

        @Override // com.android.internal.widget.ILockSettings
        public boolean setExtendLockScreenPassword(String password, String phoneNumber, int userHandle) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.widget.ILockSettings
        public boolean clearExtendLockScreenPassword(String password, int userHandle) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.widget.ILockSettings
        public int getRemainAllowedRetryCount(int userId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.widget.ILockSettings
        public long getRemainLockedTime(int userId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.widget.ILockSettings
        public int getStrongAuthSolution(int userId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.widget.ILockSettings
        public int getPasswordBackendStatus() throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ILockSettings {
        private static final String DESCRIPTOR = "com.android.internal.widget.ILockSettings";
        static final int TRANSACTION_checkCredential = 9;
        static final int TRANSACTION_checkVoldPassword = 12;
        static final int TRANSACTION_clearExtendLockScreenPassword = 43;
        static final int TRANSACTION_closeSession = 41;
        static final int TRANSACTION_generateKey = 27;
        static final int TRANSACTION_generateKeyWithMetadata = 28;
        static final int TRANSACTION_getBoolean = 4;
        static final int TRANSACTION_getHashFactor = 15;
        static final int TRANSACTION_getKey = 31;
        static final int TRANSACTION_getKeyChainSnapshot = 26;
        static final int TRANSACTION_getLong = 5;
        static final int TRANSACTION_getPasswordBackendStatus = 47;
        static final int TRANSACTION_getRecoverySecretTypes = 38;
        static final int TRANSACTION_getRecoveryStatus = 36;
        static final int TRANSACTION_getRemainAllowedRetryCount = 44;
        static final int TRANSACTION_getRemainLockedTime = 45;
        static final int TRANSACTION_getSeparateProfileChallengeEnabled = 17;
        static final int TRANSACTION_getString = 6;
        static final int TRANSACTION_getStrongAuthForUser = 23;
        static final int TRANSACTION_getStrongAuthSolution = 46;
        static final int TRANSACTION_hasPendingEscrowToken = 24;
        static final int TRANSACTION_havePassword = 14;
        static final int TRANSACTION_havePattern = 13;
        static final int TRANSACTION_importKey = 29;
        static final int TRANSACTION_importKeyWithMetadata = 30;
        static final int TRANSACTION_initRecoveryServiceWithSigFile = 25;
        static final int TRANSACTION_recoverKeyChainSnapshot = 40;
        static final int TRANSACTION_registerStrongAuthTracker = 18;
        static final int TRANSACTION_removeKey = 32;
        static final int TRANSACTION_requireStrongAuth = 20;
        static final int TRANSACTION_resetKeyStore = 8;
        static final int TRANSACTION_setBoolean = 1;
        static final int TRANSACTION_setExtendLockScreenPassword = 42;
        static final int TRANSACTION_setLockCredential = 7;
        static final int TRANSACTION_setLong = 2;
        static final int TRANSACTION_setRecoverySecretTypes = 37;
        static final int TRANSACTION_setRecoveryStatus = 35;
        static final int TRANSACTION_setSeparateProfileChallengeEnabled = 16;
        static final int TRANSACTION_setServerParams = 34;
        static final int TRANSACTION_setSnapshotCreatedPendingIntent = 33;
        static final int TRANSACTION_setString = 3;
        static final int TRANSACTION_startRecoverySessionWithCertPath = 39;
        static final int TRANSACTION_systemReady = 21;
        static final int TRANSACTION_unregisterStrongAuthTracker = 19;
        static final int TRANSACTION_userPresent = 22;
        static final int TRANSACTION_verifyCredential = 10;
        static final int TRANSACTION_verifyTiedProfileChallenge = 11;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "setBoolean";
                case 2:
                    return "setLong";
                case 3:
                    return "setString";
                case 4:
                    return "getBoolean";
                case 5:
                    return "getLong";
                case 6:
                    return "getString";
                case 7:
                    return "setLockCredential";
                case 8:
                    return "resetKeyStore";
                case 9:
                    return "checkCredential";
                case 10:
                    return "verifyCredential";
                case 11:
                    return "verifyTiedProfileChallenge";
                case 12:
                    return "checkVoldPassword";
                case 13:
                    return "havePattern";
                case 14:
                    return "havePassword";
                case 15:
                    return "getHashFactor";
                case 16:
                    return "setSeparateProfileChallengeEnabled";
                case 17:
                    return "getSeparateProfileChallengeEnabled";
                case 18:
                    return "registerStrongAuthTracker";
                case 19:
                    return "unregisterStrongAuthTracker";
                case 20:
                    return "requireStrongAuth";
                case 21:
                    return "systemReady";
                case 22:
                    return "userPresent";
                case 23:
                    return "getStrongAuthForUser";
                case 24:
                    return "hasPendingEscrowToken";
                case 25:
                    return "initRecoveryServiceWithSigFile";
                case 26:
                    return "getKeyChainSnapshot";
                case 27:
                    return "generateKey";
                case 28:
                    return "generateKeyWithMetadata";
                case 29:
                    return "importKey";
                case 30:
                    return "importKeyWithMetadata";
                case 31:
                    return "getKey";
                case 32:
                    return "removeKey";
                case 33:
                    return "setSnapshotCreatedPendingIntent";
                case 34:
                    return "setServerParams";
                case 35:
                    return "setRecoveryStatus";
                case 36:
                    return "getRecoveryStatus";
                case 37:
                    return "setRecoverySecretTypes";
                case 38:
                    return "getRecoverySecretTypes";
                case 39:
                    return "startRecoverySessionWithCertPath";
                case 40:
                    return "recoverKeyChainSnapshot";
                case 41:
                    return "closeSession";
                case 42:
                    return "setExtendLockScreenPassword";
                case 43:
                    return "clearExtendLockScreenPassword";
                case 44:
                    return "getRemainAllowedRetryCount";
                case 45:
                    return "getRemainLockedTime";
                case 46:
                    return "getStrongAuthSolution";
                case 47:
                    return "getPasswordBackendStatus";
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
            PendingIntent _arg0;
            RecoveryCertPath _arg2;
            if (code != 1598968902) {
                boolean _arg1 = false;
                boolean _arg12 = false;
                boolean _arg5 = false;
                boolean _arg13 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setBoolean(_arg02, _arg1, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        setLong(data.readString(), data.readLong(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        setString(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        boolean z = getBoolean(_arg03, _arg13, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(z ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        long _result = getLong(data.readString(), data.readLong(), data.readInt());
                        reply.writeNoException();
                        reply.writeLong(_result);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        String _result2 = getString(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result2);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _arg04 = data.createByteArray();
                        int _arg14 = data.readInt();
                        byte[] _arg22 = data.createByteArray();
                        int _arg3 = data.readInt();
                        int _arg4 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setLockCredential(_arg04, _arg14, _arg22, _arg3, _arg4, _arg5);
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        resetKeyStore(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        VerifyCredentialResponse _result3 = checkCredential(data.createByteArray(), data.readInt(), data.readInt(), ICheckCredentialProgressCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        VerifyCredentialResponse _result4 = verifyCredential(data.createByteArray(), data.readInt(), data.readLong(), data.readInt());
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        VerifyCredentialResponse _result5 = verifyTiedProfileChallenge(data.createByteArray(), data.readInt(), data.readLong(), data.readInt());
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean checkVoldPassword = checkVoldPassword(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(checkVoldPassword ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        boolean havePattern = havePattern(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(havePattern ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        boolean havePassword = havePassword(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(havePassword ? 1 : 0);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result6 = getHashFactor(data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeByteArray(_result6);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        setSeparateProfileChallengeEnabled(_arg05, _arg12, data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        boolean separateProfileChallengeEnabled = getSeparateProfileChallengeEnabled(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(separateProfileChallengeEnabled ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        registerStrongAuthTracker(IStrongAuthTracker.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterStrongAuthTracker(IStrongAuthTracker.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        requireStrongAuth(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        systemReady();
                        reply.writeNoException();
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        userPresent(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = getStrongAuthForUser(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasPendingEscrowToken = hasPendingEscrowToken(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(hasPendingEscrowToken ? 1 : 0);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        initRecoveryServiceWithSigFile(data.readString(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        KeyChainSnapshot _result8 = getKeyChainSnapshot();
                        reply.writeNoException();
                        if (_result8 != null) {
                            reply.writeInt(1);
                            _result8.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        String _result9 = generateKey(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result9);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        String _result10 = generateKeyWithMetadata(data.readString(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeString(_result10);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        String _result11 = importKey(data.readString(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeString(_result11);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        String _result12 = importKeyWithMetadata(data.readString(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeString(_result12);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        String _result13 = getKey(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result13);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        removeKey(data.readString());
                        reply.writeNoException();
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        setSnapshotCreatedPendingIntent(_arg0);
                        reply.writeNoException();
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        setServerParams(data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        setRecoveryStatus(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        Map _result14 = getRecoveryStatus();
                        reply.writeNoException();
                        reply.writeMap(_result14);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        setRecoverySecretTypes(data.createIntArray());
                        reply.writeNoException();
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result15 = getRecoverySecretTypes();
                        reply.writeNoException();
                        reply.writeIntArray(_result15);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg06 = data.readString();
                        String _arg15 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = RecoveryCertPath.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        byte[] _result16 = startRecoverySessionWithCertPath(_arg06, _arg15, _arg2, data.createByteArray(), data.createByteArray(), data.createTypedArrayList(KeyChainProtectionParams.CREATOR));
                        reply.writeNoException();
                        reply.writeByteArray(_result16);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        Map _result17 = recoverKeyChainSnapshot(data.readString(), data.createByteArray(), data.createTypedArrayList(WrappedApplicationKey.CREATOR));
                        reply.writeNoException();
                        reply.writeMap(_result17);
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        closeSession(data.readString());
                        reply.writeNoException();
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        boolean extendLockScreenPassword = setExtendLockScreenPassword(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(extendLockScreenPassword ? 1 : 0);
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        boolean clearExtendLockScreenPassword = clearExtendLockScreenPassword(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(clearExtendLockScreenPassword ? 1 : 0);
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        int _result18 = getRemainAllowedRetryCount(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        long _result19 = getRemainLockedTime(data.readInt());
                        reply.writeNoException();
                        reply.writeLong(_result19);
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        int _result20 = getStrongAuthSolution(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        int _result21 = getPasswordBackendStatus();
                        reply.writeNoException();
                        reply.writeInt(_result21);
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
        public static class Proxy implements ILockSettings {
            public static ILockSettings sDefaultImpl;
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

            @Override // com.android.internal.widget.ILockSettings
            public void setBoolean(String key, boolean value, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeInt(value ? 1 : 0);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setBoolean(key, value, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public void setLong(String key, long value, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeLong(value);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setLong(key, value, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public void setString(String key, String value, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeString(value);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setString(key, value, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public boolean getBoolean(String key, boolean defaultValue, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    boolean _result = true;
                    _data.writeInt(defaultValue ? 1 : 0);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBoolean(key, defaultValue, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public long getLong(String key, long defaultValue, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeLong(defaultValue);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLong(key, defaultValue, userId);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public String getString(String key, String defaultValue, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeString(defaultValue);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getString(key, defaultValue, userId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public void setLockCredential(byte[] credential, int type, byte[] savedCredential, int requestedQuality, int userId, boolean allowUntrustedChange) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeByteArray(credential);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(type);
                        try {
                            _data.writeByteArray(savedCredential);
                            try {
                                _data.writeInt(requestedQuality);
                            } catch (Throwable th2) {
                                th = th2;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(userId);
                        _data.writeInt(allowUntrustedChange ? 1 : 0);
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().setLockCredential(credential, type, savedCredential, requestedQuality, userId, allowUntrustedChange);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public void resetKeyStore(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resetKeyStore(userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public VerifyCredentialResponse checkCredential(byte[] credential, int type, int userId, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
                VerifyCredentialResponse _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(credential);
                    _data.writeInt(type);
                    _data.writeInt(userId);
                    _data.writeStrongBinder(progressCallback != null ? progressCallback.asBinder() : null);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkCredential(credential, type, userId, progressCallback);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = VerifyCredentialResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public VerifyCredentialResponse verifyCredential(byte[] credential, int type, long challenge, int userId) throws RemoteException {
                VerifyCredentialResponse _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(credential);
                    _data.writeInt(type);
                    _data.writeLong(challenge);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().verifyCredential(credential, type, challenge, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = VerifyCredentialResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public VerifyCredentialResponse verifyTiedProfileChallenge(byte[] credential, int type, long challenge, int userId) throws RemoteException {
                VerifyCredentialResponse _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(credential);
                    _data.writeInt(type);
                    _data.writeLong(challenge);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().verifyTiedProfileChallenge(credential, type, challenge, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = VerifyCredentialResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public boolean checkVoldPassword(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkVoldPassword(userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public boolean havePattern(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().havePattern(userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public boolean havePassword(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().havePassword(userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public byte[] getHashFactor(byte[] currentCredential, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(currentCredential);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHashFactor(currentCredential, userId);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public void setSeparateProfileChallengeEnabled(int userId, boolean enabled, byte[] managedUserPassword) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(enabled ? 1 : 0);
                    _data.writeByteArray(managedUserPassword);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSeparateProfileChallengeEnabled(userId, enabled, managedUserPassword);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public boolean getSeparateProfileChallengeEnabled(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSeparateProfileChallengeEnabled(userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public void registerStrongAuthTracker(IStrongAuthTracker tracker) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(tracker != null ? tracker.asBinder() : null);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerStrongAuthTracker(tracker);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public void unregisterStrongAuthTracker(IStrongAuthTracker tracker) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(tracker != null ? tracker.asBinder() : null);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterStrongAuthTracker(tracker);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public void requireStrongAuth(int strongAuthReason, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(strongAuthReason);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requireStrongAuth(strongAuthReason, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public void systemReady() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(21, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().systemReady();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public void userPresent(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().userPresent(userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public int getStrongAuthForUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getStrongAuthForUser(userId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public boolean hasPendingEscrowToken(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasPendingEscrowToken(userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public void initRecoveryServiceWithSigFile(String rootCertificateAlias, byte[] recoveryServiceCertFile, byte[] recoveryServiceSigFile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(rootCertificateAlias);
                    _data.writeByteArray(recoveryServiceCertFile);
                    _data.writeByteArray(recoveryServiceSigFile);
                    if (this.mRemote.transact(25, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().initRecoveryServiceWithSigFile(rootCertificateAlias, recoveryServiceCertFile, recoveryServiceSigFile);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public KeyChainSnapshot getKeyChainSnapshot() throws RemoteException {
                KeyChainSnapshot _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getKeyChainSnapshot();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = KeyChainSnapshot.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public String generateKey(String alias) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().generateKey(alias);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public String generateKeyWithMetadata(String alias, byte[] metadata) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    _data.writeByteArray(metadata);
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().generateKeyWithMetadata(alias, metadata);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public String importKey(String alias, byte[] keyBytes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    _data.writeByteArray(keyBytes);
                    if (!this.mRemote.transact(29, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().importKey(alias, keyBytes);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public String importKeyWithMetadata(String alias, byte[] keyBytes, byte[] metadata) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    _data.writeByteArray(keyBytes);
                    _data.writeByteArray(metadata);
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().importKeyWithMetadata(alias, keyBytes, metadata);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public String getKey(String alias) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getKey(alias);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public void removeKey(String alias) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    if (this.mRemote.transact(32, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeKey(alias);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
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
                    if (this.mRemote.transact(33, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSnapshotCreatedPendingIntent(intent);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public void setServerParams(byte[] serverParams) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(serverParams);
                    if (this.mRemote.transact(34, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setServerParams(serverParams);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public void setRecoveryStatus(String alias, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alias);
                    _data.writeInt(status);
                    if (this.mRemote.transact(35, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setRecoveryStatus(alias, status);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public Map getRecoveryStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(36, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRecoveryStatus();
                    }
                    _reply.readException();
                    Map _result = _reply.readHashMap(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public void setRecoverySecretTypes(int[] secretTypes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(secretTypes);
                    if (this.mRemote.transact(37, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setRecoverySecretTypes(secretTypes);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public int[] getRecoverySecretTypes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRecoverySecretTypes();
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public byte[] startRecoverySessionWithCertPath(String sessionId, String rootCertificateAlias, RecoveryCertPath verifierCertPath, byte[] vaultParams, byte[] vaultChallenge, List<KeyChainProtectionParams> secrets) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(sessionId);
                        try {
                            _data.writeString(rootCertificateAlias);
                            if (verifierCertPath != null) {
                                _data.writeInt(1);
                                verifierCertPath.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                        } catch (Throwable th) {
                            th = th;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeByteArray(vaultParams);
                        try {
                            _data.writeByteArray(vaultChallenge);
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeTypedList(secrets);
                            if (this.mRemote.transact(39, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                byte[] _result = _reply.createByteArray();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            byte[] startRecoverySessionWithCertPath = Stub.getDefaultImpl().startRecoverySessionWithCertPath(sessionId, rootCertificateAlias, verifierCertPath, vaultParams, vaultChallenge, secrets);
                            _reply.recycle();
                            _data.recycle();
                            return startRecoverySessionWithCertPath;
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public Map recoverKeyChainSnapshot(String sessionId, byte[] recoveryKeyBlob, List<WrappedApplicationKey> applicationKeys) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sessionId);
                    _data.writeByteArray(recoveryKeyBlob);
                    _data.writeTypedList(applicationKeys);
                    if (!this.mRemote.transact(40, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().recoverKeyChainSnapshot(sessionId, recoveryKeyBlob, applicationKeys);
                    }
                    _reply.readException();
                    Map _result = _reply.readHashMap(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public void closeSession(String sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sessionId);
                    if (this.mRemote.transact(41, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().closeSession(sessionId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public boolean setExtendLockScreenPassword(String password, String phoneNumber, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    _data.writeString(phoneNumber);
                    _data.writeInt(userHandle);
                    boolean _result = false;
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setExtendLockScreenPassword(password, phoneNumber, userHandle);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public boolean clearExtendLockScreenPassword(String password, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    _data.writeInt(userHandle);
                    boolean _result = false;
                    if (!this.mRemote.transact(43, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().clearExtendLockScreenPassword(password, userHandle);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public int getRemainAllowedRetryCount(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(44, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRemainAllowedRetryCount(userId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public long getRemainLockedTime(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(45, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRemainLockedTime(userId);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public int getStrongAuthSolution(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(46, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getStrongAuthSolution(userId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.widget.ILockSettings
            public int getPasswordBackendStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(47, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPasswordBackendStatus();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ILockSettings impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ILockSettings getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
