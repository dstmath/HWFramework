package com.android.server;

import android.os.RemoteException;
import android.service.gatekeeper.GateKeeperResponse;
import android.service.gatekeeper.IGateKeeperService;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.util.ArrayUtils;
import com.android.internal.widget.VerifyCredentialResponse;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import libcore.util.HexEncoding;

public class SyntheticPasswordManager {
    public static final long DEFAULT_HANDLE = 0;
    private static final String DEFAULT_PASSWORD = "default-password";
    private static final String PASSWORD_DATA_NAME = "pwd";
    private static final int PASSWORD_SALT_LENGTH = 16;
    private static final int PASSWORD_SCRYPT_N = 11;
    private static final int PASSWORD_SCRYPT_P = 1;
    private static final int PASSWORD_SCRYPT_R = 3;
    private static final int PASSWORD_TOKEN_LENGTH = 32;
    private static final byte[] PERSONALISATION_SECDISCARDABLE = "secdiscardable-transform".getBytes();
    private static final byte[] PERSONALIZATION_E0 = "e0-encryption".getBytes();
    private static final byte[] PERSONALIZATION_FBE_KEY = "fbe-key".getBytes();
    private static final byte[] PERSONALIZATION_KEY_STORE_PASSWORD = "keystore-password".getBytes();
    private static final byte[] PERSONALIZATION_SP_GK_AUTH = "sp-gk-authentication".getBytes();
    private static final byte[] PERSONALIZATION_SP_SPLIT = "sp-split".getBytes();
    private static final byte[] PERSONALIZATION_USER_GK_AUTH = "user-gk-authentication".getBytes();
    private static final int SECDISCARDABLE_LENGTH = 16384;
    private static final String SECDISCARDABLE_NAME = "secdis";
    private static final String SP_BLOB_NAME = "spblob";
    private static final String SP_E0_NAME = "e0";
    private static final String SP_HANDLE_NAME = "handle";
    private static final String SP_P1_NAME = "p1";
    private static final byte SYNTHETIC_PASSWORD_LENGTH = (byte) 32;
    private static final byte SYNTHETIC_PASSWORD_PASSWORD_BASED = (byte) 0;
    private static final byte SYNTHETIC_PASSWORD_TOKEN_BASED = (byte) 1;
    private static final byte SYNTHETIC_PASSWORD_VERSION = (byte) 1;
    private static final String TAG = "SyntheticPasswordManager";
    protected static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private LockSettingsStorage mStorage;
    private ArrayMap<Integer, ArrayMap<Long, byte[]>> tokenMap = new ArrayMap();

    static class AuthenticationResult {
        public AuthenticationToken authToken;
        public VerifyCredentialResponse gkResponse;

        AuthenticationResult() {
        }
    }

    static class AuthenticationToken {
        private byte[] E0;
        private byte[] P1;
        private String syntheticPassword;

        AuthenticationToken() {
        }

        public String deriveKeyStorePassword() {
            return SyntheticPasswordManager.bytesToHex(SyntheticPasswordCrypto.personalisedHash(SyntheticPasswordManager.PERSONALIZATION_KEY_STORE_PASSWORD, this.syntheticPassword.getBytes()));
        }

        public byte[] deriveGkPassword() {
            return SyntheticPasswordCrypto.personalisedHash(SyntheticPasswordManager.PERSONALIZATION_SP_GK_AUTH, this.syntheticPassword.getBytes());
        }

        public byte[] deriveDiskEncryptionKey() {
            return SyntheticPasswordCrypto.personalisedHash(SyntheticPasswordManager.PERSONALIZATION_FBE_KEY, this.syntheticPassword.getBytes());
        }

        private void initialize(byte[] P0, byte[] P1) {
            this.P1 = P1;
            this.syntheticPassword = String.valueOf(HexEncoding.encode(SyntheticPasswordCrypto.personalisedHash(SyntheticPasswordManager.PERSONALIZATION_SP_SPLIT, P0, P1)));
            this.E0 = SyntheticPasswordCrypto.encrypt(this.syntheticPassword.getBytes(), SyntheticPasswordManager.PERSONALIZATION_E0, P0);
        }

        public void recreate(byte[] secret) {
            initialize(secret, this.P1);
        }

        protected static AuthenticationToken create() {
            AuthenticationToken result = new AuthenticationToken();
            result.initialize(SyntheticPasswordManager.secureRandom(32), SyntheticPasswordManager.secureRandom(32));
            return result;
        }

        public byte[] computeP0() {
            if (this.E0 == null) {
                return null;
            }
            return SyntheticPasswordCrypto.decrypt(this.syntheticPassword.getBytes(), SyntheticPasswordManager.PERSONALIZATION_E0, this.E0);
        }
    }

    static class PasswordData {
        public byte[] passwordHandle;
        public int passwordType;
        byte[] salt;
        byte scryptN;
        byte scryptP;
        byte scryptR;

        PasswordData() {
        }

        public static PasswordData create(int passwordType) {
            PasswordData result = new PasswordData();
            result.scryptN = (byte) 11;
            result.scryptR = (byte) 3;
            result.scryptP = (byte) 1;
            result.passwordType = passwordType;
            result.salt = SyntheticPasswordManager.secureRandom(16);
            return result;
        }

        public static PasswordData fromBytes(byte[] data) {
            PasswordData result = new PasswordData();
            ByteBuffer buffer = ByteBuffer.allocate(data.length);
            buffer.put(data, 0, data.length);
            buffer.flip();
            result.passwordType = buffer.getInt();
            result.scryptN = buffer.get();
            result.scryptR = buffer.get();
            result.scryptP = buffer.get();
            result.salt = new byte[buffer.getInt()];
            buffer.get(result.salt);
            result.passwordHandle = new byte[buffer.getInt()];
            buffer.get(result.passwordHandle);
            return result;
        }

        public byte[] toBytes() {
            ByteBuffer buffer = ByteBuffer.allocate(((this.salt.length + 11) + 4) + this.passwordHandle.length);
            buffer.putInt(this.passwordType);
            buffer.put(this.scryptN);
            buffer.put(this.scryptR);
            buffer.put(this.scryptP);
            buffer.putInt(this.salt.length);
            buffer.put(this.salt);
            buffer.putInt(this.passwordHandle.length);
            buffer.put(this.passwordHandle);
            return buffer.array();
        }
    }

    native byte[] nativeScrypt(byte[] bArr, byte[] bArr2, int i, int i2, int i3, int i4);

    native long nativeSidFromPasswordHandle(byte[] bArr);

    public SyntheticPasswordManager(LockSettingsStorage storage) {
        this.mStorage = storage;
    }

    public int getCredentialType(long handle, int userId) {
        byte[] passwordData = loadState(PASSWORD_DATA_NAME, handle, userId);
        if (passwordData != null) {
            return PasswordData.fromBytes(passwordData).passwordType;
        }
        Log.w(TAG, "getCredentialType: encountered empty password data for user " + userId);
        return -1;
    }

    public AuthenticationToken newSyntheticPasswordAndSid(IGateKeeperService gatekeeper, byte[] hash, String credential, int userId) throws RemoteException {
        AuthenticationToken result = AuthenticationToken.create();
        if (hash != null) {
            GateKeeperResponse response = gatekeeper.enroll(userId, hash, credential.getBytes(), result.deriveGkPassword());
            if (response.getResponseCode() != 0) {
                Log.w(TAG, "Fail to migrate SID, assuming no SID, user " + userId);
                clearSidForUser(userId);
            } else {
                saveSyntheticPasswordHandle(response.getPayload(), userId);
            }
        } else {
            clearSidForUser(userId);
        }
        saveEscrowData(result, userId);
        return result;
    }

    public void newSidForUser(IGateKeeperService gatekeeper, AuthenticationToken authToken, int userId) throws RemoteException {
        GateKeeperResponse response = gatekeeper.enroll(userId, null, null, authToken.deriveGkPassword());
        if (response.getResponseCode() != 0) {
            Log.e(TAG, "Fail to create new SID for user " + userId);
        } else {
            saveSyntheticPasswordHandle(response.getPayload(), userId);
        }
    }

    public void clearSidForUser(int userId) {
        destroyState(SP_HANDLE_NAME, 0, userId);
    }

    public boolean hasSidForUser(int userId) {
        return hasState(SP_HANDLE_NAME, 0, userId);
    }

    private byte[] loadSyntheticPasswordHandle(int userId) {
        return loadState(SP_HANDLE_NAME, 0, userId);
    }

    private void saveSyntheticPasswordHandle(byte[] spHandle, int userId) {
        saveState(SP_HANDLE_NAME, spHandle, 0, userId);
    }

    private boolean loadEscrowData(AuthenticationToken authToken, int userId) {
        authToken.E0 = loadState(SP_E0_NAME, 0, userId);
        authToken.P1 = loadState(SP_P1_NAME, 0, userId);
        if (authToken.E0 == null || authToken.P1 == null) {
            return false;
        }
        return true;
    }

    private void saveEscrowData(AuthenticationToken authToken, int userId) {
        saveState(SP_E0_NAME, authToken.E0, 0, userId);
        saveState(SP_P1_NAME, authToken.P1, 0, userId);
    }

    public boolean hasEscrowData(int userId) {
        if (hasState(SP_E0_NAME, 0, userId)) {
            return hasState(SP_P1_NAME, 0, userId);
        }
        return false;
    }

    public void destroyEscrowData(int userId) {
        destroyState(SP_E0_NAME, 0, userId);
        destroyState(SP_P1_NAME, 0, userId);
    }

    public long createPasswordBasedSyntheticPassword(IGateKeeperService gatekeeper, String credential, int credentialType, AuthenticationToken authToken, int userId) throws RemoteException {
        if (credential == null || credentialType == -1) {
            credentialType = -1;
            credential = DEFAULT_PASSWORD;
        }
        long handle = generateHandle();
        PasswordData pwd = PasswordData.create(credentialType);
        byte[] pwdToken = computePasswordToken(credential, pwd);
        gatekeeper.clearSecureUserId(fakeUid(userId));
        GateKeeperResponse response = gatekeeper.enroll(fakeUid(userId), null, null, passwordTokenToGkInput(pwdToken));
        if (response.getResponseCode() != 0) {
            Log.e(TAG, "Fail to enroll user password when creating SP for user " + userId);
            return 0;
        }
        pwd.passwordHandle = response.getPayload();
        long sid = sidFromPasswordHandle(pwd.passwordHandle);
        saveState(PASSWORD_DATA_NAME, pwd.toBytes(), handle, userId);
        AuthenticationToken authenticationToken = authToken;
        createSyntheticPasswordBlob(handle, (byte) 0, authenticationToken, transformUnderSecdiscardable(pwdToken, createSecdiscardable(handle, userId)), sid, userId);
        return handle;
    }

    public long createTokenBasedSyntheticPassword(byte[] token, int userId) {
        long handle = generateHandle();
        byte[] applicationId = transformUnderSecdiscardable(token, createSecdiscardable(handle, userId));
        if (!this.tokenMap.containsKey(Integer.valueOf(userId))) {
            this.tokenMap.put(Integer.valueOf(userId), new ArrayMap());
        }
        ((ArrayMap) this.tokenMap.get(Integer.valueOf(userId))).put(Long.valueOf(handle), applicationId);
        return handle;
    }

    public Set<Long> getPendingTokensForUser(int userId) {
        if (this.tokenMap.containsKey(Integer.valueOf(userId))) {
            return ((ArrayMap) this.tokenMap.get(Integer.valueOf(userId))).keySet();
        }
        return Collections.emptySet();
    }

    public boolean removePendingToken(long handle, int userId) {
        if (!this.tokenMap.containsKey(Integer.valueOf(userId))) {
            return false;
        }
        return ((ArrayMap) this.tokenMap.get(Integer.valueOf(userId))).remove(Long.valueOf(handle)) != null;
    }

    public boolean activateTokenBasedSyntheticPassword(long handle, AuthenticationToken authToken, int userId) {
        if (!this.tokenMap.containsKey(Integer.valueOf(userId))) {
            return false;
        }
        byte[] applicationId = (byte[]) ((ArrayMap) this.tokenMap.get(Integer.valueOf(userId))).get(Long.valueOf(handle));
        if (applicationId == null) {
            return false;
        }
        if (loadEscrowData(authToken, userId)) {
            createSyntheticPasswordBlob(handle, (byte) 1, authToken, applicationId, 0, userId);
            ((ArrayMap) this.tokenMap.get(Integer.valueOf(userId))).remove(Long.valueOf(handle));
            return true;
        }
        Log.w(TAG, "User is not escrowable");
        return false;
    }

    private void createSyntheticPasswordBlob(long handle, byte type, AuthenticationToken authToken, byte[] applicationId, long sid, int userId) {
        byte[] secret;
        if (type == (byte) 1) {
            secret = authToken.computeP0();
        } else {
            secret = authToken.syntheticPassword.getBytes();
        }
        byte[] content = createSPBlob(getHandleName(handle), secret, applicationId, sid);
        byte[] blob = new byte[((content.length + 1) + 1)];
        blob[0] = (byte) 1;
        blob[1] = type;
        System.arraycopy(content, 0, blob, 2, content.length);
        saveState(SP_BLOB_NAME, blob, handle, userId);
    }

    public AuthenticationResult unwrapPasswordBasedSyntheticPassword(IGateKeeperService gatekeeper, long handle, String credential, int userId) throws RemoteException {
        if (credential == null) {
            credential = DEFAULT_PASSWORD;
        }
        AuthenticationResult result = new AuthenticationResult();
        PasswordData pwd = PasswordData.fromBytes(loadState(PASSWORD_DATA_NAME, handle, userId));
        byte[] pwdToken = computePasswordToken(credential, pwd);
        byte[] gkPwdToken = passwordTokenToGkInput(pwdToken);
        GateKeeperResponse response = gatekeeper.verifyChallenge(fakeUid(userId), 0, pwd.passwordHandle, gkPwdToken);
        int responseCode = response.getResponseCode();
        if (responseCode == 0) {
            result.gkResponse = VerifyCredentialResponse.OK;
            if (response.getShouldReEnroll()) {
                GateKeeperResponse reenrollResponse = gatekeeper.enroll(fakeUid(userId), pwd.passwordHandle, gkPwdToken, gkPwdToken);
                if (reenrollResponse.getResponseCode() == 0) {
                    pwd.passwordHandle = reenrollResponse.getPayload();
                    saveState(PASSWORD_DATA_NAME, pwd.toBytes(), handle, userId);
                } else {
                    Log.w(TAG, "Fail to re-enroll user password for user " + userId);
                }
            }
            long j = handle;
            result.authToken = unwrapSyntheticPasswordBlob(j, (byte) 0, transformUnderSecdiscardable(pwdToken, loadSecdiscardable(handle, userId)), userId);
            result.gkResponse = verifyChallenge(gatekeeper, result.authToken, 0, userId);
            return result;
        } else if (responseCode == 1) {
            result.gkResponse = new VerifyCredentialResponse(response.getTimeout());
            return result;
        } else {
            result.gkResponse = VerifyCredentialResponse.ERROR;
            return result;
        }
    }

    public AuthenticationResult unwrapTokenBasedSyntheticPassword(IGateKeeperService gatekeeper, long handle, byte[] token, int userId) throws RemoteException {
        AuthenticationResult result = new AuthenticationResult();
        result.authToken = unwrapSyntheticPasswordBlob(handle, (byte) 1, transformUnderSecdiscardable(token, loadSecdiscardable(handle, userId)), userId);
        if (result.authToken != null) {
            result.gkResponse = verifyChallenge(gatekeeper, result.authToken, 0, userId);
            if (result.gkResponse == null) {
                result.gkResponse = VerifyCredentialResponse.OK;
            }
        } else {
            result.gkResponse = VerifyCredentialResponse.ERROR;
        }
        return result;
    }

    private AuthenticationToken unwrapSyntheticPasswordBlob(long handle, byte type, byte[] applicationId, int userId) {
        byte[] blob = loadState(SP_BLOB_NAME, handle, userId);
        if (blob == null) {
            return null;
        }
        if (blob[0] != (byte) 1) {
            throw new RuntimeException("Unknown blob version");
        } else if (blob[1] != type) {
            throw new RuntimeException("Invalid blob type");
        } else {
            byte[] secret = decryptSPBlob(getHandleName(handle), Arrays.copyOfRange(blob, 2, blob.length), applicationId);
            if (secret == null) {
                Log.e(TAG, "Fail to decrypt SP for user " + userId);
                return null;
            }
            AuthenticationToken result = new AuthenticationToken();
            if (type != (byte) 1) {
                result.syntheticPassword = new String(secret);
            } else if (loadEscrowData(result, userId)) {
                result.recreate(secret);
            } else {
                Log.e(TAG, "User is not escrowable: " + userId);
                return null;
            }
            return result;
        }
    }

    public VerifyCredentialResponse verifyChallenge(IGateKeeperService gatekeeper, AuthenticationToken auth, long challenge, int userId) throws RemoteException {
        byte[] spHandle = loadSyntheticPasswordHandle(userId);
        if (spHandle == null) {
            return null;
        }
        VerifyCredentialResponse result;
        GateKeeperResponse response = gatekeeper.verifyChallenge(userId, challenge, spHandle, auth.deriveGkPassword());
        int responseCode = response.getResponseCode();
        if (responseCode == 0) {
            result = new VerifyCredentialResponse(response.getPayload());
            if (response.getShouldReEnroll()) {
                response = gatekeeper.enroll(userId, spHandle, spHandle, auth.deriveGkPassword());
                if (response.getResponseCode() == 0) {
                    saveSyntheticPasswordHandle(response.getPayload(), userId);
                    return verifyChallenge(gatekeeper, auth, challenge, userId);
                }
                Log.w(TAG, "Fail to re-enroll SP handle for user " + userId);
            }
        } else {
            result = responseCode == 1 ? new VerifyCredentialResponse(response.getTimeout()) : VerifyCredentialResponse.ERROR;
        }
        return result;
    }

    public boolean existsHandle(long handle, int userId) {
        return hasState(SP_BLOB_NAME, handle, userId);
    }

    public void destroyTokenBasedSyntheticPassword(long handle, int userId) {
        destroySyntheticPassword(handle, userId);
        destroyState(SECDISCARDABLE_NAME, handle, userId);
    }

    public void destroyPasswordBasedSyntheticPassword(long handle, int userId) {
        destroySyntheticPassword(handle, userId);
        destroyState(SECDISCARDABLE_NAME, handle, userId);
        destroyState(PASSWORD_DATA_NAME, handle, userId);
    }

    private void destroySyntheticPassword(long handle, int userId) {
        destroyState(SP_BLOB_NAME, handle, userId);
        destroySPBlobKey(getHandleName(handle));
    }

    private byte[] transformUnderSecdiscardable(byte[] data, byte[] rawSecdiscardable) {
        byte[] secdiscardable = SyntheticPasswordCrypto.personalisedHash(PERSONALISATION_SECDISCARDABLE, rawSecdiscardable);
        byte[] result = new byte[(data.length + secdiscardable.length)];
        System.arraycopy(data, 0, result, 0, data.length);
        System.arraycopy(secdiscardable, 0, result, data.length, secdiscardable.length);
        return result;
    }

    private byte[] createSecdiscardable(long handle, int userId) {
        byte[] data = secureRandom(16384);
        saveState(SECDISCARDABLE_NAME, data, handle, userId);
        return data;
    }

    private byte[] loadSecdiscardable(long handle, int userId) {
        return loadState(SECDISCARDABLE_NAME, handle, userId);
    }

    private boolean hasState(String stateName, long handle, int userId) {
        return ArrayUtils.isEmpty(loadState(stateName, handle, userId)) ^ 1;
    }

    private byte[] loadState(String stateName, long handle, int userId) {
        return this.mStorage.readSyntheticPasswordState(userId, handle, stateName);
    }

    private void saveState(String stateName, byte[] data, long handle, int userId) {
        this.mStorage.writeSyntheticPasswordState(userId, handle, stateName, data);
    }

    private void destroyState(String stateName, long handle, int userId) {
        this.mStorage.deleteSyntheticPasswordState(userId, handle, stateName);
    }

    protected byte[] decryptSPBlob(String blobKeyName, byte[] blob, byte[] applicationId) {
        return SyntheticPasswordCrypto.decryptBlob(blobKeyName, blob, applicationId);
    }

    protected byte[] createSPBlob(String blobKeyName, byte[] data, byte[] applicationId, long sid) {
        return SyntheticPasswordCrypto.createBlob(blobKeyName, data, applicationId, sid);
    }

    protected void destroySPBlobKey(String keyAlias) {
        SyntheticPasswordCrypto.destroyBlobKey(keyAlias);
    }

    public static long generateHandle() {
        long result;
        SecureRandom rng = new SecureRandom();
        do {
            result = rng.nextLong();
        } while (result == 0);
        return result;
    }

    private int fakeUid(int uid) {
        return 100000 + uid;
    }

    protected static byte[] secureRandom(int length) {
        try {
            return SecureRandom.getInstance("SHA1PRNG").generateSeed(length);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getHandleName(long handle) {
        return String.format("%s%x", new Object[]{"synthetic_password_", Long.valueOf(handle)});
    }

    private byte[] computePasswordToken(String password, PasswordData data) {
        return scrypt(password, data.salt, 1 << data.scryptN, 1 << data.scryptR, 1 << data.scryptP, 32);
    }

    private byte[] passwordTokenToGkInput(byte[] token) {
        return SyntheticPasswordCrypto.personalisedHash(PERSONALIZATION_USER_GK_AUTH, token);
    }

    protected long sidFromPasswordHandle(byte[] handle) {
        return nativeSidFromPasswordHandle(handle);
    }

    protected byte[] scrypt(String password, byte[] salt, int N, int r, int p, int outLen) {
        return nativeScrypt(password.getBytes(), salt, N, r, p, outLen);
    }

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return "null";
        }
        char[] hexChars = new char[(bytes.length * 2)];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 255;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[(j * 2) + 1] = hexArray[v & 15];
        }
        return new String(hexChars);
    }
}
