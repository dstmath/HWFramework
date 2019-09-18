package com.android.server.locksettings;

import android.content.Context;
import android.content.pm.UserInfo;
import android.hardware.weaver.V1_0.IWeaver;
import android.hardware.weaver.V1_0.WeaverConfig;
import android.hardware.weaver.V1_0.WeaverReadResponse;
import android.os.RemoteException;
import android.os.UserManager;
import android.service.gatekeeper.GateKeeperResponse;
import android.service.gatekeeper.IGateKeeperService;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.internal.widget.ICheckCredentialProgressCallback;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.VerifyCredentialResponse;
import com.android.server.locksettings.LockSettingsStorage;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import libcore.util.HexEncoding;

public class SyntheticPasswordManager {
    public static final long DEFAULT_HANDLE = 0;
    private static final String DEFAULT_PASSWORD = "default-password";
    private static final int INVALID_WEAVER_SLOT = -1;
    private static final String PASSWORD_DATA_NAME = "pwd";
    private static final int PASSWORD_SALT_LENGTH = 16;
    private static final int PASSWORD_SCRYPT_N = 11;
    private static final int PASSWORD_SCRYPT_P = 1;
    private static final int PASSWORD_SCRYPT_R = 3;
    private static final int PASSWORD_TOKEN_LENGTH = 32;
    private static final byte[] PERSONALISATION_SECDISCARDABLE = "secdiscardable-transform".getBytes();
    private static final byte[] PERSONALISATION_WEAVER_KEY = "weaver-key".getBytes();
    private static final byte[] PERSONALISATION_WEAVER_PASSWORD = "weaver-pwd".getBytes();
    private static final byte[] PERSONALISATION_WEAVER_TOKEN = "weaver-token".getBytes();
    /* access modifiers changed from: private */
    public static final byte[] PERSONALIZATION_AUTHSECRET_KEY = "authsecret-hal".getBytes();
    /* access modifiers changed from: private */
    public static final byte[] PERSONALIZATION_E0 = "e0-encryption".getBytes();
    /* access modifiers changed from: private */
    public static final byte[] PERSONALIZATION_FBE_KEY = "fbe-key".getBytes();
    /* access modifiers changed from: private */
    public static final byte[] PERSONALIZATION_KEY_STORE_PASSWORD = "keystore-password".getBytes();
    /* access modifiers changed from: private */
    public static final byte[] PERSONALIZATION_PASSWORD_HASH = "pw-hash".getBytes();
    /* access modifiers changed from: private */
    public static final byte[] PERSONALIZATION_SP_GK_AUTH = "sp-gk-authentication".getBytes();
    /* access modifiers changed from: private */
    public static final byte[] PERSONALIZATION_SP_SPLIT = "sp-split".getBytes();
    private static final byte[] PERSONALIZATION_USER_GK_AUTH = "user-gk-authentication".getBytes();
    private static final int SECDISCARDABLE_LENGTH = 16384;
    private static final String SECDISCARDABLE_NAME = "secdis";
    private static final String SP_BLOB_NAME = "spblob";
    private static final String SP_E0_NAME = "e0";
    private static final String SP_HANDLE_NAME = "handle";
    private static final String SP_P1_NAME = "p1";
    private static final byte SYNTHETIC_PASSWORD_LENGTH = 32;
    private static final byte SYNTHETIC_PASSWORD_PASSWORD_BASED = 0;
    private static final byte SYNTHETIC_PASSWORD_TOKEN_BASED = 1;
    private static final byte SYNTHETIC_PASSWORD_VERSION = 2;
    private static final byte SYNTHETIC_PASSWORD_VERSION_V1 = 1;
    private static final String TAG = "LSS-SPM";
    private static final String WEAVER_SLOT_NAME = "weaver";
    private static final byte WEAVER_VERSION = 1;
    protected static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private final Context mContext;
    protected LockSettingsStorage mStorage;
    private final UserManager mUserManager;
    private IWeaver mWeaver;
    private WeaverConfig mWeaverConfig;
    private ArrayMap<Integer, ArrayMap<Long, TokenData>> tokenMap = new ArrayMap<>();

    static class AuthenticationResult {
        public AuthenticationToken authToken;
        public int credentialType;
        public VerifyCredentialResponse gkResponse;

        AuthenticationResult() {
        }
    }

    static class AuthenticationToken {
        /* access modifiers changed from: private */
        public byte[] E0;
        /* access modifiers changed from: private */
        public byte[] P1;
        /* access modifiers changed from: private */
        public String syntheticPassword;

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

        public byte[] deriveVendorAuthSecret() {
            return SyntheticPasswordCrypto.personalisedHash(SyntheticPasswordManager.PERSONALIZATION_AUTHSECRET_KEY, this.syntheticPassword.getBytes());
        }

        public byte[] derivePasswordHashFactor() {
            return SyntheticPasswordCrypto.personalisedHash(SyntheticPasswordManager.PERSONALIZATION_PASSWORD_HASH, this.syntheticPassword.getBytes());
        }

        private void initialize(byte[] P0, byte[] P12) {
            this.P1 = P12;
            this.syntheticPassword = String.valueOf(HexEncoding.encode(SyntheticPasswordCrypto.personalisedHash(SyntheticPasswordManager.PERSONALIZATION_SP_SPLIT, P0, P12)));
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

        public static PasswordData create(int passwordType2) {
            PasswordData result = new PasswordData();
            result.scryptN = 11;
            result.scryptR = 3;
            result.scryptP = 1;
            result.passwordType = passwordType2;
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
            int handleLen = buffer.getInt();
            if (handleLen > 0) {
                result.passwordHandle = new byte[handleLen];
                buffer.get(result.passwordHandle);
            } else {
                result.passwordHandle = null;
            }
            return result;
        }

        public byte[] toBytes() {
            ByteBuffer buffer = ByteBuffer.allocate(11 + this.salt.length + 4 + (this.passwordHandle != null ? this.passwordHandle.length : 0));
            buffer.putInt(this.passwordType);
            buffer.put(this.scryptN);
            buffer.put(this.scryptR);
            buffer.put(this.scryptP);
            buffer.putInt(this.salt.length);
            buffer.put(this.salt);
            if (this.passwordHandle == null || this.passwordHandle.length <= 0) {
                buffer.putInt(0);
            } else {
                buffer.putInt(this.passwordHandle.length);
                buffer.put(this.passwordHandle);
            }
            return buffer.array();
        }
    }

    static class TokenData {
        byte[] aggregatedSecret;
        byte[] secdiscardableOnDisk;
        byte[] weaverSecret;

        TokenData() {
        }
    }

    /* access modifiers changed from: package-private */
    public native byte[] nativeScrypt(byte[] bArr, byte[] bArr2, int i, int i2, int i3, int i4);

    /* access modifiers changed from: package-private */
    public native long nativeSidFromPasswordHandle(byte[] bArr);

    public SyntheticPasswordManager(Context context, LockSettingsStorage storage, UserManager userManager) {
        this.mContext = context;
        this.mStorage = storage;
        this.mUserManager = userManager;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public IWeaver getWeaverService() throws RemoteException {
        try {
            return IWeaver.getService();
        } catch (NoSuchElementException e) {
            Slog.i(TAG, "Device does not support weaver");
            return null;
        }
    }

    public synchronized void initWeaverService() {
        if (this.mWeaver == null) {
            try {
                this.mWeaverConfig = null;
                this.mWeaver = getWeaverService();
                if (this.mWeaver != null) {
                    this.mWeaver.getConfig(new IWeaver.getConfigCallback() {
                        public final void onValues(int i, WeaverConfig weaverConfig) {
                            SyntheticPasswordManager.lambda$initWeaverService$0(SyntheticPasswordManager.this, i, weaverConfig);
                        }
                    });
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to get weaver service", e);
            }
        } else {
            return;
        }
        return;
    }

    public static /* synthetic */ void lambda$initWeaverService$0(SyntheticPasswordManager syntheticPasswordManager, int status, WeaverConfig config) {
        if (status != 0 || config.slots <= 0) {
            Slog.e(TAG, "Failed to get weaver config, status " + status + " slots: " + config.slots);
            syntheticPasswordManager.mWeaver = null;
            return;
        }
        syntheticPasswordManager.mWeaverConfig = config;
    }

    private synchronized boolean isWeaverAvailable() {
        if (this.mWeaver == null) {
            initWeaverService();
        }
        return this.mWeaver != null && this.mWeaverConfig.slots > 0;
    }

    private byte[] weaverEnroll(int slot, byte[] key, byte[] value) throws RemoteException {
        if (slot == -1 || slot >= this.mWeaverConfig.slots) {
            throw new RuntimeException("Invalid slot for weaver");
        }
        if (key == null) {
            key = new byte[this.mWeaverConfig.keySize];
        } else if (key.length != this.mWeaverConfig.keySize) {
            throw new RuntimeException("Invalid key size for weaver");
        }
        if (value == null) {
            value = secureRandom(this.mWeaverConfig.valueSize);
        }
        int writeStatus = this.mWeaver.write(slot, toByteArrayList(key), toByteArrayList(value));
        if (writeStatus == 0) {
            return value;
        }
        Log.e(TAG, "weaver write failed, slot: " + slot + " status: " + writeStatus);
        return null;
    }

    private VerifyCredentialResponse weaverVerify(int slot, byte[] key) throws RemoteException {
        if (slot == -1 || slot >= this.mWeaverConfig.slots) {
            throw new RuntimeException("Invalid slot for weaver");
        }
        if (key == null) {
            key = new byte[this.mWeaverConfig.keySize];
        } else if (key.length != this.mWeaverConfig.keySize) {
            throw new RuntimeException("Invalid key size for weaver");
        }
        VerifyCredentialResponse[] response = new VerifyCredentialResponse[1];
        this.mWeaver.read(slot, toByteArrayList(key), new IWeaver.readCallback(response, slot) {
            private final /* synthetic */ VerifyCredentialResponse[] f$0;
            private final /* synthetic */ int f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void onValues(int i, WeaverReadResponse weaverReadResponse) {
                SyntheticPasswordManager.lambda$weaverVerify$1(this.f$0, this.f$1, i, weaverReadResponse);
            }
        });
        return response[0];
    }

    static /* synthetic */ void lambda$weaverVerify$1(VerifyCredentialResponse[] response, int slot, int status, WeaverReadResponse readResponse) {
        switch (status) {
            case 0:
                response[0] = new VerifyCredentialResponse(fromByteArrayList(readResponse.value));
                return;
            case 1:
                response[0] = VerifyCredentialResponse.ERROR;
                Log.e(TAG, "weaver read failed (FAILED), slot: " + slot);
                return;
            case 2:
                if (readResponse.timeout == 0) {
                    response[0] = VerifyCredentialResponse.ERROR;
                    Log.e(TAG, "weaver read failed (INCORRECT_KEY), slot: " + slot);
                    return;
                }
                response[0] = new VerifyCredentialResponse(readResponse.timeout);
                Log.e(TAG, "weaver read failed (INCORRECT_KEY/THROTTLE), slot: " + slot);
                return;
            case 3:
                response[0] = new VerifyCredentialResponse(readResponse.timeout);
                Log.e(TAG, "weaver read failed (THROTTLE), slot: " + slot);
                return;
            default:
                response[0] = VerifyCredentialResponse.ERROR;
                Log.e(TAG, "weaver read unknown status " + status + ", slot: " + slot);
                return;
        }
    }

    public void removeUser(int userId) {
        for (Long longValue : this.mStorage.listSyntheticPasswordHandlesForUser(SP_BLOB_NAME, userId)) {
            long handle = longValue.longValue();
            destroyWeaverSlot(handle, userId);
            destroySPBlobKey(getHandleName(handle));
        }
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
            return;
        }
        saveSyntheticPasswordHandle(response.getPayload(), userId);
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
        byte[] unused = authToken.E0 = loadState(SP_E0_NAME, 0, userId);
        byte[] unused2 = authToken.P1 = loadState(SP_P1_NAME, 0, userId);
        return (authToken.E0 == null || authToken.P1 == null) ? false : true;
    }

    private void saveEscrowData(AuthenticationToken authToken, int userId) {
        saveState(SP_E0_NAME, authToken.E0, 0, userId);
        saveState(SP_P1_NAME, authToken.P1, 0, userId);
    }

    public boolean hasEscrowData(int userId) {
        return hasState(SP_E0_NAME, 0, userId) && hasState(SP_P1_NAME, 0, userId);
    }

    public void destroyEscrowData(int userId) {
        destroyState(SP_E0_NAME, 0, userId);
        destroyState(SP_P1_NAME, 0, userId);
    }

    private int loadWeaverSlot(long handle, int userId) {
        byte[] data = loadState(WEAVER_SLOT_NAME, handle, userId);
        if (data == null || data.length != 5) {
            return -1;
        }
        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.put(data, 0, data.length);
        buffer.flip();
        if (buffer.get() == 1) {
            return buffer.getInt();
        }
        Log.e(TAG, "Invalid weaver slot version of handle " + handle);
        return -1;
    }

    private void saveWeaverSlot(int slot, long handle, int userId) {
        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.put((byte) 1);
        buffer.putInt(slot);
        saveState(WEAVER_SLOT_NAME, buffer.array(), handle, userId);
    }

    private void destroyWeaverSlot(long handle, int userId) {
        int slot = loadWeaverSlot(handle, userId);
        destroyState(WEAVER_SLOT_NAME, handle, userId);
        if (slot == -1) {
            return;
        }
        if (!getUsedWeaverSlots().contains(Integer.valueOf(slot))) {
            Log.i(TAG, "Destroy weaver slot " + slot + " for user " + userId);
            try {
                weaverEnroll(slot, null, null);
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to destroy slot", e);
            }
        } else {
            Log.w(TAG, "Skip destroying reused weaver slot " + slot + " for user " + userId);
        }
    }

    private Set<Integer> getUsedWeaverSlots() {
        Map<Integer, List<Long>> slotHandles = this.mStorage.listSyntheticPasswordHandlesForAllUsers(WEAVER_SLOT_NAME);
        HashSet<Integer> slots = new HashSet<>();
        for (Map.Entry<Integer, List<Long>> entry : slotHandles.entrySet()) {
            for (Long handle : entry.getValue()) {
                slots.add(Integer.valueOf(loadWeaverSlot(handle.longValue(), entry.getKey().intValue())));
            }
        }
        return slots;
    }

    private int getNextAvailableWeaverSlot() {
        Set<Integer> usedSlots = getUsedWeaverSlots();
        for (int i = 0; i < this.mWeaverConfig.slots; i++) {
            if (!usedSlots.contains(Integer.valueOf(i))) {
                return i;
            }
        }
        throw new RuntimeException("Run out of weaver slots.");
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0087  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x002e  */
    public long createPasswordBasedSyntheticPassword(IGateKeeperService gatekeeper, String credential, int credentialType, AuthenticationToken authToken, int requestedQuality, int userId) throws RemoteException {
        String credential2;
        int credentialType2;
        long sid;
        byte[] applicationId;
        IGateKeeperService iGateKeeperService = gatekeeper;
        int i = requestedQuality;
        int i2 = userId;
        if (credential != null) {
            int i3 = credentialType;
            if (i3 != -1) {
                credential2 = credential;
                credentialType2 = i3;
                long handle = generateHandle();
                PasswordData pwd = PasswordData.create(credentialType2);
                byte[] pwdToken = computePasswordToken(credential2, pwd);
                if (!isWeaverAvailable()) {
                    int weaverSlot = getNextAvailableWeaverSlot();
                    Log.i(TAG, "Weaver enroll password to slot " + weaverSlot + " for user " + i2);
                    byte[] weaverSecret = weaverEnroll(weaverSlot, passwordTokenToWeaverKey(pwdToken), null);
                    if (weaverSecret == null) {
                        Log.e(TAG, "Fail to enroll user password under weaver " + i2);
                        return 0;
                    }
                    saveWeaverSlot(weaverSlot, handle, i2);
                    synchronizeWeaverFrpPassword(pwd, i, i2, weaverSlot);
                    pwd.passwordHandle = null;
                    applicationId = transformUnderWeaverSecret(pwdToken, weaverSecret);
                    sid = 0;
                } else {
                    iGateKeeperService.clearSecureUserId(fakeUid(i2));
                    GateKeeperResponse response = iGateKeeperService.enroll(fakeUid(i2), null, null, passwordTokenToGkInput(pwdToken));
                    if (response.getResponseCode() != 0) {
                        Log.e(TAG, "Fail to enroll user password when creating SP for user " + i2);
                        return 0;
                    }
                    pwd.passwordHandle = response.getPayload();
                    long sid2 = sidFromPasswordHandle(pwd.passwordHandle);
                    byte[] applicationId2 = transformUnderSecdiscardable(pwdToken, createSecdiscardable(handle, i2));
                    synchronizeFrpPassword(pwd, i, i2);
                    sid = sid2;
                    applicationId = applicationId2;
                }
                saveState(PASSWORD_DATA_NAME, pwd.toBytes(), handle, i2);
                long handle2 = handle;
                byte[] bArr = pwdToken;
                createSyntheticPasswordBlob(handle, (byte) 0, authToken, applicationId, sid, i2);
                return handle2;
            }
        } else {
            int i4 = credentialType;
        }
        credential2 = DEFAULT_PASSWORD;
        credentialType2 = -1;
        long handle3 = generateHandle();
        PasswordData pwd2 = PasswordData.create(credentialType2);
        byte[] pwdToken2 = computePasswordToken(credential2, pwd2);
        if (!isWeaverAvailable()) {
        }
        saveState(PASSWORD_DATA_NAME, pwd2.toBytes(), handle3, i2);
        long handle22 = handle3;
        byte[] bArr2 = pwdToken2;
        createSyntheticPasswordBlob(handle3, (byte) 0, authToken, applicationId, sid, i2);
        return handle22;
    }

    public VerifyCredentialResponse verifyFrpCredential(IGateKeeperService gatekeeper, String userCredential, int credentialType, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        LockSettingsStorage.PersistentData persistentData = this.mStorage.readPersistentDataBlock();
        if (persistentData.type == 1) {
            PasswordData pwd = PasswordData.fromBytes(persistentData.payload);
            return VerifyCredentialResponse.fromGateKeeperResponse(gatekeeper.verifyChallenge(fakeUid(persistentData.userId), 0, pwd.passwordHandle, passwordTokenToGkInput(computePasswordToken(userCredential, pwd))));
        } else if (persistentData.type == 2) {
            return weaverVerify(persistentData.userId, passwordTokenToWeaverKey(computePasswordToken(userCredential, PasswordData.fromBytes(persistentData.payload)))).stripPayload();
        } else {
            Log.e(TAG, "persistentData.type must be TYPE_SP or TYPE_SP_WEAVER, but is " + persistentData.type);
            return VerifyCredentialResponse.ERROR;
        }
    }

    public void migrateFrpPasswordLocked(long handle, UserInfo userInfo, int requestedQuality) {
        if (this.mStorage.getPersistentDataBlock() != null && LockPatternUtils.userOwnsFrpCredential(this.mContext, userInfo)) {
            PasswordData pwd = PasswordData.fromBytes(loadState(PASSWORD_DATA_NAME, handle, userInfo.id));
            if (pwd.passwordType != -1) {
                int weaverSlot = loadWeaverSlot(handle, userInfo.id);
                if (weaverSlot != -1) {
                    synchronizeWeaverFrpPassword(pwd, requestedQuality, userInfo.id, weaverSlot);
                } else {
                    synchronizeFrpPassword(pwd, requestedQuality, userInfo.id);
                }
            }
        }
    }

    private void synchronizeFrpPassword(PasswordData pwd, int requestedQuality, int userId) {
        if (this.mStorage.getPersistentDataBlock() != null && LockPatternUtils.userOwnsFrpCredential(this.mContext, this.mUserManager.getUserInfo(userId))) {
            if (pwd.passwordType != -1) {
                this.mStorage.writePersistentDataBlock(1, userId, requestedQuality, pwd.toBytes());
            } else {
                this.mStorage.writePersistentDataBlock(0, userId, 0, null);
            }
        }
    }

    private void synchronizeWeaverFrpPassword(PasswordData pwd, int requestedQuality, int userId, int weaverSlot) {
        if (this.mStorage.getPersistentDataBlock() != null && LockPatternUtils.userOwnsFrpCredential(this.mContext, this.mUserManager.getUserInfo(userId))) {
            if (pwd.passwordType != -1) {
                this.mStorage.writePersistentDataBlock(2, weaverSlot, requestedQuality, pwd.toBytes());
            } else {
                this.mStorage.writePersistentDataBlock(0, 0, 0, null);
            }
        }
    }

    public long createTokenBasedSyntheticPassword(byte[] token, int userId) {
        long handle = generateHandle();
        if (!this.tokenMap.containsKey(Integer.valueOf(userId))) {
            this.tokenMap.put(Integer.valueOf(userId), new ArrayMap());
        }
        TokenData tokenData = new TokenData();
        byte[] secdiscardable = secureRandom(16384);
        if (isWeaverAvailable()) {
            tokenData.weaverSecret = secureRandom(this.mWeaverConfig.valueSize);
            tokenData.secdiscardableOnDisk = SyntheticPasswordCrypto.encrypt(tokenData.weaverSecret, PERSONALISATION_WEAVER_TOKEN, secdiscardable);
        } else {
            tokenData.secdiscardableOnDisk = secdiscardable;
            tokenData.weaverSecret = null;
        }
        tokenData.aggregatedSecret = transformUnderSecdiscardable(token, secdiscardable);
        this.tokenMap.get(Integer.valueOf(userId)).put(Long.valueOf(handle), tokenData);
        return handle;
    }

    public Set<Long> getPendingTokensForUser(int userId) {
        if (!this.tokenMap.containsKey(Integer.valueOf(userId))) {
            return Collections.emptySet();
        }
        return this.tokenMap.get(Integer.valueOf(userId)).keySet();
    }

    public boolean removePendingToken(long handle, int userId) {
        boolean z = false;
        if (!this.tokenMap.containsKey(Integer.valueOf(userId))) {
            return false;
        }
        if (this.tokenMap.get(Integer.valueOf(userId)).remove(Long.valueOf(handle)) != null) {
            z = true;
        }
        return z;
    }

    public boolean activateTokenBasedSyntheticPassword(long handle, AuthenticationToken authToken, int userId) {
        if (!this.tokenMap.containsKey(Integer.valueOf(userId))) {
            return false;
        }
        TokenData tokenData = (TokenData) this.tokenMap.get(Integer.valueOf(userId)).get(Long.valueOf(handle));
        if (tokenData == null) {
            return false;
        }
        if (!loadEscrowData(authToken, userId)) {
            Log.w(TAG, "User is not escrowable");
            return false;
        }
        if (isWeaverAvailable()) {
            int slot = getNextAvailableWeaverSlot();
            try {
                Log.i(TAG, "Weaver enroll token to slot " + slot + " for user " + userId);
                weaverEnroll(slot, null, tokenData.weaverSecret);
                saveWeaverSlot(slot, handle, userId);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to enroll weaver secret when activating token", e);
                return false;
            }
        }
        saveSecdiscardable(handle, tokenData.secdiscardableOnDisk, userId);
        createSyntheticPasswordBlob(handle, (byte) 1, authToken, tokenData.aggregatedSecret, 0, userId);
        this.tokenMap.get(Integer.valueOf(userId)).remove(Long.valueOf(handle));
        return true;
    }

    private void createSyntheticPasswordBlob(long handle, byte type, AuthenticationToken authToken, byte[] applicationId, long sid, int userId) {
        byte[] bytes;
        boolean z;
        byte b = type;
        int i = userId;
        if (b == 1) {
            bytes = authToken.computeP0();
        } else {
            bytes = authToken.syntheticPassword.getBytes();
        }
        byte[] secret = bytes;
        UserInfo userInfo = this.mUserManager.getUserInfo(i);
        if (userInfo == null) {
            Log.e(TAG, "userId " + i + " do not have userInfo, we dont think it's a ManagedProfile");
        }
        String handleName = getHandleName(handle);
        if (userInfo == null) {
            z = false;
        } else {
            z = userInfo.isManagedProfile();
        }
        byte[] content = createSPBlob(handleName, secret, applicationId, sid, z);
        byte[] blob = new byte[(content.length + 1 + 1)];
        blob[0] = 2;
        blob[1] = b;
        System.arraycopy(content, 0, blob, 2, content.length);
        saveState(SP_BLOB_NAME, blob, handle, i);
        flog(TAG, "createSyntheticPasswordBlob U" + i + " T" + b);
    }

    public AuthenticationResult unwrapPasswordBasedSyntheticPassword(IGateKeeperService gatekeeper, long handle, String credential, int userId, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        int weaverSlot;
        long sid;
        byte[] applicationId;
        int i;
        long j = handle;
        int i2 = userId;
        String credential2 = credential == null ? DEFAULT_PASSWORD : credential;
        AuthenticationResult result = new AuthenticationResult();
        PasswordData pwd = PasswordData.fromBytes(loadState(PASSWORD_DATA_NAME, j, i2));
        result.credentialType = pwd.passwordType;
        byte[] pwdToken = computePasswordToken(credential2, pwd);
        int weaverSlot2 = loadWeaverSlot(j, i2);
        if (weaverSlot2 == -1) {
            byte[] gkPwdToken = passwordTokenToGkInput(pwdToken);
            GateKeeperResponse response = gatekeeper.verifyChallenge(fakeUid(i2), 0, pwd.passwordHandle, gkPwdToken);
            int responseCode = response.getResponseCode();
            if (responseCode == 0) {
                result.gkResponse = VerifyCredentialResponse.OK;
                if (response.getShouldReEnroll()) {
                    GateKeeperResponse reenrollResponse = gatekeeper.enroll(fakeUid(i2), pwd.passwordHandle, gkPwdToken, gkPwdToken);
                    if (reenrollResponse.getResponseCode() == 0) {
                        pwd.passwordHandle = reenrollResponse.getPayload();
                        GateKeeperResponse gateKeeperResponse = reenrollResponse;
                        byte[] bArr = gkPwdToken;
                        weaverSlot = weaverSlot2;
                        int i3 = responseCode;
                        GateKeeperResponse gateKeeperResponse2 = response;
                        saveState(PASSWORD_DATA_NAME, pwd.toBytes(), j, i2);
                        if (pwd.passwordType == 1) {
                            i = 65536;
                        } else {
                            i = 327680;
                        }
                        synchronizeFrpPassword(pwd, i, i2);
                    } else {
                        GateKeeperResponse gateKeeperResponse3 = response;
                        byte[] bArr2 = gkPwdToken;
                        weaverSlot = weaverSlot2;
                        int i4 = responseCode;
                        Log.w(TAG, "Fail to re-enroll user password for user " + i2);
                    }
                } else {
                    byte[] bArr3 = gkPwdToken;
                    weaverSlot = weaverSlot2;
                    int i5 = responseCode;
                }
                sid = sidFromPasswordHandle(pwd.passwordHandle);
                applicationId = transformUnderSecdiscardable(pwdToken, loadSecdiscardable(j, i2));
            } else {
                GateKeeperResponse response2 = response;
                byte[] bArr4 = gkPwdToken;
                int i6 = weaverSlot2;
                PasswordData passwordData = pwd;
                AuthenticationResult result2 = result;
                int responseCode2 = responseCode;
                byte[] bArr5 = pwdToken;
                if (responseCode2 == 1) {
                    result2.gkResponse = new VerifyCredentialResponse(response2.getTimeout());
                    return result2;
                }
                result2.gkResponse = VerifyCredentialResponse.ERROR;
                return result2;
            }
        } else if (!isWeaverAvailable()) {
            Log.e(TAG, "No weaver service to unwrap password based SP");
            result.gkResponse = VerifyCredentialResponse.ERROR;
            return result;
        } else {
            result.gkResponse = weaverVerify(weaverSlot2, passwordTokenToWeaverKey(pwdToken));
            if (result.gkResponse.getResponseCode() != 0) {
                return result;
            }
            sid = 0;
            applicationId = transformUnderWeaverSecret(pwdToken, result.gkResponse.getPayload());
            weaverSlot = weaverSlot2;
        }
        if (progressCallback != null) {
            progressCallback.onCredentialVerified();
        }
        int i7 = weaverSlot;
        result.authToken = unwrapSyntheticPasswordBlob(j, (byte) 0, applicationId, sid, i2);
        PasswordData passwordData2 = pwd;
        byte[] bArr6 = pwdToken;
        AuthenticationResult result3 = result;
        result3.gkResponse = verifyChallenge(gatekeeper, result.authToken, 0, i2);
        return result3;
    }

    public AuthenticationResult unwrapTokenBasedSyntheticPassword(IGateKeeperService gatekeeper, long handle, byte[] token, int userId) throws RemoteException {
        long j = handle;
        int i = userId;
        AuthenticationResult result = new AuthenticationResult();
        byte[] secdiscardable = loadSecdiscardable(j, i);
        int slotId = loadWeaverSlot(j, i);
        if (slotId != -1) {
            if (!isWeaverAvailable()) {
                Log.e(TAG, "No weaver service to unwrap token based SP");
                result.gkResponse = VerifyCredentialResponse.ERROR;
                return result;
            }
            VerifyCredentialResponse response = weaverVerify(slotId, null);
            if (response.getResponseCode() != 0 || response.getPayload() == null) {
                Log.e(TAG, "Failed to retrieve weaver secret when unwrapping token");
                result.gkResponse = VerifyCredentialResponse.ERROR;
                return result;
            }
            secdiscardable = SyntheticPasswordCrypto.decrypt(response.getPayload(), PERSONALISATION_WEAVER_TOKEN, secdiscardable);
        }
        result.authToken = unwrapSyntheticPasswordBlob(j, (byte) 1, transformUnderSecdiscardable(token, secdiscardable), 0, i);
        if (result.authToken != null) {
            result.gkResponse = verifyChallenge(gatekeeper, result.authToken, 0, i);
            if (result.gkResponse == null) {
                result.gkResponse = VerifyCredentialResponse.OK;
            }
        } else {
            result.gkResponse = VerifyCredentialResponse.ERROR;
        }
        return result;
    }

    private AuthenticationToken unwrapSyntheticPasswordBlob(long handle, byte type, byte[] applicationId, long sid, int userId) {
        byte[] secret;
        AuthenticationToken result;
        byte b = type;
        byte[] bArr = applicationId;
        int i = userId;
        long j = handle;
        byte[] blob = loadState(SP_BLOB_NAME, j, i);
        if (blob == null) {
            return null;
        }
        byte version = blob[0];
        if (version != 2 && version != 1) {
            throw new RuntimeException("Unknown blob version");
        } else if (blob[1] == b) {
            if (version == 1) {
                secret = SyntheticPasswordCrypto.decryptBlobV1(getHandleName(handle), Arrays.copyOfRange(blob, 2, blob.length), bArr);
            } else {
                secret = decryptSPBlob(getHandleName(handle), Arrays.copyOfRange(blob, 2, blob.length), bArr);
            }
            byte[] secret2 = secret;
            if (secret2 == null) {
                errorLog(TAG, "Fail to decrypt SP for user " + i);
                return null;
            }
            AuthenticationToken result2 = new AuthenticationToken();
            if (b != 1) {
                String unused = result2.syntheticPassword = new String(secret2);
            } else if (!loadEscrowData(result2, i)) {
                Log.e(TAG, "User is not escrowable: " + i);
                return null;
            } else {
                result2.recreate(secret2);
            }
            if (version == 1) {
                warnLog(TAG, "Upgrade v1 SP blob for user " + i + ", type = " + b);
                byte[] bArr2 = secret2;
                result = result2;
                byte b2 = version;
                createSyntheticPasswordBlob(j, b, result2, bArr, sid, i);
            } else {
                result = result2;
                byte b3 = version;
            }
            return result;
        } else {
            throw new RuntimeException("Invalid blob type");
        }
    }

    public VerifyCredentialResponse verifyChallenge(IGateKeeperService gatekeeper, AuthenticationToken auth, long challenge, int userId) throws RemoteException {
        VerifyCredentialResponse result;
        byte[] spHandle = loadSyntheticPasswordHandle(userId);
        if (spHandle == null) {
            return null;
        }
        GateKeeperResponse response = gatekeeper.verifyChallenge(userId, challenge, spHandle, auth.deriveGkPassword());
        int responseCode = response.getResponseCode();
        if (responseCode == 0) {
            result = new VerifyCredentialResponse(response.getPayload());
            if (response.getShouldReEnroll()) {
                GateKeeperResponse response2 = gatekeeper.enroll(userId, spHandle, spHandle, auth.deriveGkPassword());
                if (response2.getResponseCode() == 0) {
                    saveSyntheticPasswordHandle(response2.getPayload(), userId);
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
        flog(TAG, "destroyTokenBasedSyntheticPassword u" + userId + " " + handle);
    }

    public void destroyPasswordBasedSyntheticPassword(long handle, int userId) {
        destroySyntheticPassword(handle, userId);
        destroyState(SECDISCARDABLE_NAME, handle, userId);
        destroyState(PASSWORD_DATA_NAME, handle, userId);
        flog(TAG, "destroyPasswordBasedSyntheticPassword u" + userId + " " + handle);
    }

    private void destroySyntheticPassword(long handle, int userId) {
        destroyState(SP_BLOB_NAME, handle, userId);
        destroySPBlobKey(getHandleName(handle));
        if (hasState(WEAVER_SLOT_NAME, handle, userId)) {
            destroyWeaverSlot(handle, userId);
        }
        flog(TAG, "destroySyntheticPassword u" + userId + " " + handle);
    }

    private byte[] transformUnderWeaverSecret(byte[] data, byte[] secret) {
        byte[] weaverSecret = SyntheticPasswordCrypto.personalisedHash(PERSONALISATION_WEAVER_PASSWORD, secret);
        byte[] result = new byte[(data.length + weaverSecret.length)];
        System.arraycopy(data, 0, result, 0, data.length);
        System.arraycopy(weaverSecret, 0, result, data.length, weaverSecret.length);
        return result;
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
        saveSecdiscardable(handle, data, userId);
        return data;
    }

    private void saveSecdiscardable(long handle, byte[] secdiscardable, int userId) {
        saveState(SECDISCARDABLE_NAME, secdiscardable, handle, userId);
    }

    private byte[] loadSecdiscardable(long handle, int userId) {
        return loadState(SECDISCARDABLE_NAME, handle, userId);
    }

    private boolean hasState(String stateName, long handle, int userId) {
        return !ArrayUtils.isEmpty(loadState(stateName, handle, userId));
    }

    private byte[] loadState(String stateName, long handle, int userId) {
        return this.mStorage.readSyntheticPasswordState(userId, handle, stateName);
    }

    /* access modifiers changed from: protected */
    public void saveState(String stateName, byte[] data, long handle, int userId) {
        this.mStorage.writeSyntheticPasswordState(userId, handle, stateName, data);
    }

    /* access modifiers changed from: protected */
    public void destroyState(String stateName, long handle, int userId) {
        this.mStorage.deleteSyntheticPasswordState(userId, handle, stateName);
    }

    /* access modifiers changed from: protected */
    public byte[] decryptSPBlob(String blobKeyName, byte[] blob, byte[] applicationId) {
        return SyntheticPasswordCrypto.decryptBlob(blobKeyName, blob, applicationId);
    }

    /* access modifiers changed from: protected */
    public byte[] createSPBlob(String blobKeyName, byte[] data, byte[] applicationId, long sid, boolean managedProfile) {
        return SyntheticPasswordCrypto.createBlob(blobKeyName, data, applicationId, sid, managedProfile);
    }

    /* access modifiers changed from: protected */
    public byte[] createSPBlob(String blobKeyName, byte[] data, byte[] applicationId, long sid) {
        return SyntheticPasswordCrypto.createBlob(blobKeyName, data, applicationId, sid, false);
    }

    /* access modifiers changed from: protected */
    public void destroySPBlobKey(String keyAlias) {
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

    private byte[] passwordTokenToWeaverKey(byte[] token) {
        byte[] key = SyntheticPasswordCrypto.personalisedHash(PERSONALISATION_WEAVER_KEY, token);
        if (key.length >= this.mWeaverConfig.keySize) {
            return Arrays.copyOf(key, this.mWeaverConfig.keySize);
        }
        throw new RuntimeException("weaver key length too small");
    }

    /* access modifiers changed from: protected */
    public long sidFromPasswordHandle(byte[] handle) {
        return nativeSidFromPasswordHandle(handle);
    }

    /* access modifiers changed from: protected */
    public byte[] scrypt(String password, byte[] salt, int N, int r, int p, int outLen) {
        return nativeScrypt(password.getBytes(), salt, N, r, p, outLen);
    }

    protected static ArrayList<Byte> toByteArrayList(byte[] data) {
        ArrayList<Byte> result = new ArrayList<>(data.length);
        for (byte valueOf : data) {
            result.add(Byte.valueOf(valueOf));
        }
        return result;
    }

    protected static byte[] fromByteArrayList(ArrayList<Byte> data) {
        byte[] result = new byte[data.size()];
        for (int i = 0; i < data.size(); i++) {
            result[i] = data.get(i).byteValue();
        }
        return result;
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

    /* access modifiers changed from: package-private */
    public void errorLog(String tag, String msg) {
    }

    /* access modifiers changed from: package-private */
    public void warnLog(String tag, String msg) {
    }

    /* access modifiers changed from: package-private */
    public void flog(String tag, String msg) {
    }
}
