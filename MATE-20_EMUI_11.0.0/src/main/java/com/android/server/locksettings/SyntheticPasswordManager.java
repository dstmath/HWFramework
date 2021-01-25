package com.android.server.locksettings;

import android.content.Context;
import android.content.pm.UserInfo;
import android.hardware.weaver.V1_0.IWeaver;
import android.hardware.weaver.V1_0.WeaverConfig;
import android.hardware.weaver.V1_0.WeaverReadResponse;
import android.os.RemoteException;
import android.os.UserManager;
import android.security.Scrypt;
import android.service.gatekeeper.GateKeeperResponse;
import android.service.gatekeeper.IGateKeeperService;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
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
import vendor.huawei.hardware.weaver.V1_1.IWeaver;

public class SyntheticPasswordManager {
    public static final long DEFAULT_HANDLE = 0;
    private static final byte[] DEFAULT_PASSWORD = "default-password".getBytes();
    protected static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes();
    private static final int INVALID_WEAVER_SLOT = -1;
    protected static final String PASSWORD_DATA_NAME = "pwd";
    private static final int PASSWORD_SALT_LENGTH = 16;
    private static final int PASSWORD_SCRYPT_N = 11;
    private static final int PASSWORD_SCRYPT_P = 1;
    private static final int PASSWORD_SCRYPT_R = 3;
    private static final int PASSWORD_TOKEN_LENGTH = 32;
    private static final byte[] PERSONALISATION_CONTEXT = "android-synthetic-password-personalization-context".getBytes();
    private static final byte[] PERSONALISATION_SECDISCARDABLE = "secdiscardable-transform".getBytes();
    private static final byte[] PERSONALISATION_WEAVER_KEY = "weaver-key".getBytes();
    private static final byte[] PERSONALISATION_WEAVER_PASSWORD = "weaver-pwd".getBytes();
    private static final byte[] PERSONALISATION_WEAVER_TOKEN = "weaver-token".getBytes();
    private static final byte[] PERSONALIZATION_AUTHSECRET_KEY = "authsecret-hal".getBytes();
    private static final byte[] PERSONALIZATION_E0 = "e0-encryption".getBytes();
    private static final byte[] PERSONALIZATION_FBE_KEY = "fbe-key".getBytes();
    private static final byte[] PERSONALIZATION_KEY_STORE_PASSWORD = "keystore-password".getBytes();
    private static final byte[] PERSONALIZATION_PASSWORD_HASH = "pw-hash".getBytes();
    private static final byte[] PERSONALIZATION_SP_GK_AUTH = "sp-gk-authentication".getBytes();
    private static final byte[] PERSONALIZATION_SP_SPLIT = "sp-split".getBytes();
    private static final byte[] PERSONALIZATION_USER_GK_AUTH = "user-gk-authentication".getBytes();
    private static final int SECDISCARDABLE_LENGTH = 16384;
    protected static final String SECDISCARDABLE_NAME = "secdis";
    protected static final String SP_BLOB_NAME = "spblob";
    private static final String SP_E0_NAME = "e0";
    protected static final String SP_HANDLE_NAME = "handle";
    private static final String SP_P1_NAME = "p1";
    private static final byte SYNTHETIC_PASSWORD_LENGTH = 32;
    private static final byte SYNTHETIC_PASSWORD_PASSWORD_BASED = 0;
    private static final byte SYNTHETIC_PASSWORD_TOKEN_BASED = 1;
    private static final byte SYNTHETIC_PASSWORD_VERSION_V1 = 1;
    private static final byte SYNTHETIC_PASSWORD_VERSION_V2 = 2;
    private static final byte SYNTHETIC_PASSWORD_VERSION_V3 = 3;
    private static final String TAG = "LSS-SPM";
    private static final String WEAVER_HIDL_SERVICE_NAME = "huaweiweaver";
    protected static final String WEAVER_SLOT_NAME = "weaver";
    private static final byte WEAVER_VERSION = 1;
    private final Context mContext;
    private PasswordSlotManager mPasswordSlotManager;
    protected LockSettingsStorage mStorage;
    private final UserManager mUserManager;
    protected IWeaver mWeaver;
    private WeaverConfig mWeaverConfig;
    private ArrayMap<Integer, ArrayMap<Long, TokenData>> tokenMap = new ArrayMap<>();

    /* access modifiers changed from: package-private */
    public native long nativeSidFromPasswordHandle(byte[] bArr);

    public static class AuthenticationResult {
        public AuthenticationToken authToken;
        public int credentialType;
        public VerifyCredentialResponse gkResponse;

        AuthenticationResult() {
        }
    }

    public static class AuthenticationToken {
        private byte[] E0;
        private byte[] P1;
        private final byte mVersion;
        private String syntheticPassword;

        AuthenticationToken(byte version) {
            this.mVersion = version;
        }

        private byte[] derivePassword(byte[] personalization) {
            if (this.mVersion == 3) {
                return new SP800Derive(this.syntheticPassword.getBytes()).withContext(personalization, SyntheticPasswordManager.PERSONALISATION_CONTEXT);
            }
            return SyntheticPasswordCrypto.personalisedHash(personalization, this.syntheticPassword.getBytes());
        }

        public byte[] deriveKeyStorePassword() {
            return SyntheticPasswordManager.bytesToHex(derivePassword(SyntheticPasswordManager.PERSONALIZATION_KEY_STORE_PASSWORD));
        }

        public byte[] deriveGkPassword() {
            return derivePassword(SyntheticPasswordManager.PERSONALIZATION_SP_GK_AUTH);
        }

        public byte[] deriveDiskEncryptionKey() {
            return derivePassword(SyntheticPasswordManager.PERSONALIZATION_FBE_KEY);
        }

        public byte[] deriveVendorAuthSecret() {
            return derivePassword(SyntheticPasswordManager.PERSONALIZATION_AUTHSECRET_KEY);
        }

        public byte[] derivePasswordHashFactor() {
            return derivePassword(SyntheticPasswordManager.PERSONALIZATION_PASSWORD_HASH);
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
            AuthenticationToken result = new AuthenticationToken((byte) 3);
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

    public static class PasswordData {
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
            int length = this.salt.length + 11 + 4;
            byte[] bArr = this.passwordHandle;
            ByteBuffer buffer = ByteBuffer.allocate(length + (bArr != null ? bArr.length : 0));
            buffer.putInt(this.passwordType);
            buffer.put(this.scryptN);
            buffer.put(this.scryptR);
            buffer.put(this.scryptP);
            buffer.putInt(this.salt.length);
            buffer.put(this.salt);
            byte[] bArr2 = this.passwordHandle;
            if (bArr2 == null || bArr2.length <= 0) {
                buffer.putInt(0);
            } else {
                buffer.putInt(bArr2.length);
                buffer.put(this.passwordHandle);
            }
            return buffer.array();
        }
    }

    public static class TokenData {
        byte[] aggregatedSecret;
        LockPatternUtils.EscrowTokenStateChangeCallback mCallback;
        byte[] secdiscardableOnDisk;
        byte[] weaverSecret;

        TokenData() {
        }
    }

    public SyntheticPasswordManager(Context context, LockSettingsStorage storage, UserManager userManager, PasswordSlotManager passwordSlotManager) {
        this.mContext = context;
        this.mStorage = storage;
        this.mUserManager = userManager;
        this.mPasswordSlotManager = passwordSlotManager;
    }

    public IWeaver getWeaverService() {
        try {
            return IWeaver.getService(WEAVER_HIDL_SERVICE_NAME);
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
                        /* class com.android.server.locksettings.$$Lambda$SyntheticPasswordManager$WjMVqfQ1YUbeAiLzyAhyepqPFI */

                        @Override // android.hardware.weaver.V1_0.IWeaver.getConfigCallback
                        public final void onValues(int i, WeaverConfig weaverConfig) {
                            SyntheticPasswordManager.this.lambda$initWeaverService$0$SyntheticPasswordManager(i, weaverConfig);
                        }
                    });
                    this.mPasswordSlotManager.refreshActiveSlots(getUsedWeaverSlots());
                    weaverLinkToDeath();
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to get weaver service", e);
            }
        }
    }

    public /* synthetic */ void lambda$initWeaverService$0$SyntheticPasswordManager(int status, WeaverConfig config) {
        if (status != 0 || config.slots <= 0) {
            Slog.e(TAG, "Failed to get weaver config, status " + status + " slots: " + config.slots);
            this.mWeaver = null;
            return;
        }
        this.mWeaverConfig = config;
    }

    private synchronized boolean isWeaverAvailable() {
        if (this.mWeaver == null) {
            initWeaverService();
        }
        return this.mWeaver != null && this.mWeaverConfig.slots > 0;
    }

    private byte[] weaverEnroll(int slot, byte[] key, byte[] value) {
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

    private VerifyCredentialResponse weaverVerify(int slot, byte[] key) {
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
            /* class com.android.server.locksettings.$$Lambda$SyntheticPasswordManager$aWnbfYziDTrRrLqWFePMTj6dy0 */
            private final /* synthetic */ VerifyCredentialResponse[] f$0;
            private final /* synthetic */ int f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // android.hardware.weaver.V1_0.IWeaver.readCallback
            public final void onValues(int i, WeaverReadResponse weaverReadResponse) {
                SyntheticPasswordManager.lambda$weaverVerify$1(this.f$0, this.f$1, i, weaverReadResponse);
            }
        });
        return response[0];
    }

    static /* synthetic */ void lambda$weaverVerify$1(VerifyCredentialResponse[] response, int slot, int status, WeaverReadResponse readResponse) {
        if (status == 0) {
            response[0] = new VerifyCredentialResponse(fromByteArrayList(readResponse.value));
        } else if (status == 1) {
            response[0] = VerifyCredentialResponse.ERROR;
            Log.e(TAG, "weaver read failed (FAILED), slot: " + slot);
        } else if (status != 2) {
            if (status != 3) {
                response[0] = VerifyCredentialResponse.ERROR;
                Log.e(TAG, "weaver read unknown status " + status + ", slot: " + slot);
                return;
            }
            response[0] = new VerifyCredentialResponse(readResponse.timeout);
            Log.e(TAG, "weaver read failed (THROTTLE), slot: " + slot);
        } else if (readResponse.timeout == 0) {
            response[0] = VerifyCredentialResponse.ERROR;
            Log.e(TAG, "weaver read failed (INCORRECT_KEY), slot: " + slot);
        } else {
            response[0] = new VerifyCredentialResponse(readResponse.timeout);
            Log.e(TAG, "weaver read failed (INCORRECT_KEY/THROTTLE), slot: " + slot);
        }
    }

    public void removeUser(int userId) {
        for (Long l : this.mStorage.listSyntheticPasswordHandlesForUser(SP_BLOB_NAME, userId)) {
            long handle = l.longValue();
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

    public AuthenticationToken newSyntheticPasswordAndSid(IGateKeeperService gatekeeper, byte[] hash, byte[] credential, int userId) {
        AuthenticationToken result = AuthenticationToken.create();
        if (hash != null) {
            GateKeeperResponse response = gatekeeper.enroll(userId, hash, credential, result.deriveGkPassword());
            if (response.getResponseCode() != 0) {
                warnLog(TAG, "Fail to migrate SID, assuming no SID, user " + userId);
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

    public void newSidForUser(IGateKeeperService gatekeeper, AuthenticationToken authToken, int userId) {
        GateKeeperResponse response = gatekeeper.enroll(userId, (byte[]) null, (byte[]) null, authToken.deriveGkPassword());
        if (response.getResponseCode() != 0) {
            errorLog(TAG, "Fail to create new SID for user " + userId);
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
        if (spHandle != null && spHandle.length != 0) {
            updateCredentialTypeFlag((long) spHandle[0], userId);
        }
    }

    private boolean loadEscrowData(AuthenticationToken authToken, int userId) {
        authToken.E0 = loadState(SP_E0_NAME, 0, userId);
        authToken.P1 = loadState(SP_P1_NAME, 0, userId);
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

    public int loadWeaverSlot(long handle, int userId) {
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
        updateWeaverTypeFlag(1, userId);
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
                this.mPasswordSlotManager.markSlotDeleted(slot);
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
        usedSlots.addAll(this.mPasswordSlotManager.getUsedSlots());
        for (int i = 0; i < this.mWeaverConfig.slots; i++) {
            if (!usedSlots.contains(Integer.valueOf(i))) {
                return i;
            }
        }
        throw new RuntimeException("Run out of weaver slots.");
    }

    /* JADX INFO: Multiple debug info for r0v6 byte[]: [D('weaverSlot' int), D('applicationId' byte[])] */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0093  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0032  */
    public long createPasswordBasedSyntheticPassword(IGateKeeperService gatekeeper, byte[] credential, int credentialType, AuthenticationToken authToken, int requestedQuality, int userId) {
        byte[] credential2;
        int credentialType2;
        long sid;
        byte[] applicationId;
        if (credential != null) {
            if (credentialType != -1) {
                credential2 = credential;
                credentialType2 = credentialType;
                long handle = generateHandle();
                PasswordData pwd = PasswordData.create(credentialType2);
                byte[] pwdToken = computePasswordToken(credential2, pwd);
                if (!isWeaverAvailable()) {
                    int weaverSlot = getNextAvailableWeaverSlot();
                    Log.i(TAG, "Weaver enroll password to slot " + weaverSlot + " for user " + userId);
                    byte[] weaverSecret = weaverEnroll(weaverSlot, passwordTokenToWeaverKey(pwdToken), null);
                    if (weaverSecret == null) {
                        warnLog(TAG, "Fail to enroll user password under weaver " + userId);
                        this.mStorage.reportCriticalError(1010, "weaver enroll error");
                        return 0;
                    }
                    saveWeaverSlot(weaverSlot, handle, userId);
                    this.mPasswordSlotManager.markSlotInUse(weaverSlot);
                    synchronizeWeaverFrpPassword(pwd, requestedQuality, userId, weaverSlot);
                    pwd.passwordHandle = null;
                    applicationId = transformUnderWeaverSecret(pwdToken, weaverSecret);
                    sid = 0;
                } else {
                    gatekeeper.clearSecureUserId(fakeUid(userId));
                    GateKeeperResponse response = gatekeeper.enroll(fakeUid(userId), (byte[]) null, (byte[]) null, passwordTokenToGkInput(pwdToken));
                    if (response.getResponseCode() != 0) {
                        warnLog(TAG, "Fail to enroll user password when creating SP for user " + userId);
                        this.mStorage.reportCriticalError(1010, "gatakeeper enroll error");
                        return 0;
                    }
                    pwd.passwordHandle = response.getPayload();
                    long sid2 = sidFromPasswordHandle(pwd.passwordHandle);
                    byte[] applicationId2 = transformUnderSecdiscardable(pwdToken, createSecdiscardable(handle, userId));
                    synchronizeFrpPassword(pwd, requestedQuality, userId);
                    sid = sid2;
                    applicationId = applicationId2;
                }
                saveState(PASSWORD_DATA_NAME, pwd.toBytes(), handle, userId);
                createSyntheticPasswordBlob(handle, (byte) 0, authToken, applicationId, sid, userId);
                return handle;
            }
        }
        credentialType2 = -1;
        credential2 = DEFAULT_PASSWORD;
        long handle2 = generateHandle();
        PasswordData pwd2 = PasswordData.create(credentialType2);
        byte[] pwdToken2 = computePasswordToken(credential2, pwd2);
        if (!isWeaverAvailable()) {
        }
        saveState(PASSWORD_DATA_NAME, pwd2.toBytes(), handle2, userId);
        createSyntheticPasswordBlob(handle2, (byte) 0, authToken, applicationId, sid, userId);
        return handle2;
    }

    public VerifyCredentialResponse verifyFrpCredential(IGateKeeperService gatekeeper, byte[] userCredential, int credentialType, ICheckCredentialProgressCallback progressCallback) {
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
        if (LockPatternUtils.userOwnsFrpCredential(this.mContext, userInfo)) {
            byte[] pwdData = loadState(PASSWORD_DATA_NAME, handle, userInfo.id);
            if (ArrayUtils.isEmpty(pwdData)) {
                Log.e(TAG, "migrateFrpPasswordLocked pwd data is null");
                return;
            }
            PasswordData pwd = PasswordData.fromBytes(pwdData);
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
        if (!LockPatternUtils.userOwnsFrpCredential(this.mContext, this.mUserManager.getUserInfo(userId))) {
            return;
        }
        if (pwd.passwordType != -1) {
            this.mStorage.writePersistentDataBlock(1, userId, requestedQuality, pwd.toBytes());
        } else {
            this.mStorage.writePersistentDataBlock(0, userId, 0, null);
        }
    }

    private void synchronizeWeaverFrpPassword(PasswordData pwd, int requestedQuality, int userId, int weaverSlot) {
        if (!LockPatternUtils.userOwnsFrpCredential(this.mContext, this.mUserManager.getUserInfo(userId))) {
            return;
        }
        if (pwd.passwordType != -1) {
            this.mStorage.writePersistentDataBlock(2, weaverSlot, requestedQuality, pwd.toBytes());
        } else {
            this.mStorage.writePersistentDataBlock(0, 0, 0, null);
        }
    }

    public long createTokenBasedSyntheticPassword(byte[] token, int userId, LockPatternUtils.EscrowTokenStateChangeCallback changeCallback) {
        long handle = generateHandle();
        if (!this.tokenMap.containsKey(Integer.valueOf(userId))) {
            this.tokenMap.put(Integer.valueOf(userId), new ArrayMap<>());
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
        tokenData.mCallback = changeCallback;
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
        if (this.tokenMap.containsKey(Integer.valueOf(userId)) && this.tokenMap.get(Integer.valueOf(userId)).remove(Long.valueOf(handle)) != null) {
            return true;
        }
        return false;
    }

    public boolean activateTokenBasedSyntheticPassword(long handle, AuthenticationToken authToken, int userId) {
        TokenData tokenData;
        if (!this.tokenMap.containsKey(Integer.valueOf(userId)) || (tokenData = this.tokenMap.get(Integer.valueOf(userId)).get(Long.valueOf(handle))) == null) {
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
                this.mPasswordSlotManager.markSlotInUse(slot);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to enroll weaver secret when activating token", e);
                return false;
            }
        }
        saveSecdiscardable(handle, tokenData.secdiscardableOnDisk, userId);
        createSyntheticPasswordBlob(handle, (byte) 1, authToken, tokenData.aggregatedSecret, 0, userId);
        this.tokenMap.get(Integer.valueOf(userId)).remove(Long.valueOf(handle));
        if (tokenData.mCallback == null) {
            return true;
        }
        tokenData.mCallback.onEscrowTokenActivated(handle, userId);
        return true;
    }

    private void createSyntheticPasswordBlob(long handle, byte type, AuthenticationToken authToken, byte[] applicationId, long sid, int userId) {
        byte[] secret;
        if (type == 1) {
            secret = authToken.computeP0();
        } else {
            secret = authToken.syntheticPassword.getBytes();
        }
        SyntheticPasswordCrypto.setCurrentUserId(userId);
        byte[] content = createSPBlob(getHandleName(handle), secret, applicationId, sid);
        byte[] blob = new byte[(content.length + 1 + 1)];
        if (authToken.mVersion == 3) {
            blob[0] = 3;
        } else {
            blob[0] = 2;
        }
        blob[1] = type;
        System.arraycopy(content, 0, blob, 2, content.length);
        saveState(SP_BLOB_NAME, blob, handle, userId);
        warnLog(TAG, "createSyntheticPasswordBlob U" + userId + " T" + ((int) type));
    }

    public AuthenticationResult unwrapPasswordBasedSyntheticPassword(IGateKeeperService gatekeeper, long handle, byte[] credential, int userId, ICheckCredentialProgressCallback progressCallback) {
        byte[] credential2;
        long sid;
        boolean z;
        byte[] credential3;
        int i;
        if (credential == null) {
            credential2 = DEFAULT_PASSWORD;
        } else {
            credential2 = credential;
        }
        AuthenticationResult result = new AuthenticationResult();
        byte[] pwdDate = loadState(PASSWORD_DATA_NAME, handle, userId);
        if (ArrayUtils.isEmpty(pwdDate)) {
            Log.e(TAG, "pwd date is null");
            result.gkResponse = VerifyCredentialResponse.ERROR;
            return result;
        }
        PasswordData pwd = PasswordData.fromBytes(pwdDate);
        result.credentialType = pwd.passwordType;
        byte[] pwdToken = computePasswordToken(credential2, pwd);
        int weaverSlot = loadWeaverSlot(handle, userId);
        if (weaverSlot == -1) {
            byte[] gkPwdToken = passwordTokenToGkInput(pwdToken);
            GateKeeperResponse response = gatekeeper.verifyChallenge(fakeUid(userId), 0, pwd.passwordHandle, gkPwdToken);
            int responseCode = response.getResponseCode();
            if (responseCode == 0) {
                result.gkResponse = VerifyCredentialResponse.OK;
                if (response.getShouldReEnroll()) {
                    Log.i(TAG, "unwrapPasswordBasedSyntheticPassword should reenroll user " + userId);
                    GateKeeperResponse reenrollResponse = gatekeeper.enroll(fakeUid(userId), pwd.passwordHandle, gkPwdToken, gkPwdToken);
                    if (reenrollResponse.getResponseCode() == 0) {
                        pwd.passwordHandle = reenrollResponse.getPayload();
                        z = true;
                        saveState(PASSWORD_DATA_NAME, pwd.toBytes(), handle, userId);
                        if (pwd.passwordType == 1) {
                            i = 65536;
                        } else {
                            i = 327680;
                        }
                        synchronizeFrpPassword(pwd, i, userId);
                    } else {
                        z = true;
                        warnLog(TAG, "Fail to re-enroll user password for user " + userId);
                    }
                } else {
                    z = true;
                }
                sid = sidFromPasswordHandle(pwd.passwordHandle);
                credential3 = transformUnderSecdiscardable(pwdToken, loadSecdiscardable(handle, userId));
            } else if (responseCode == 1) {
                result.gkResponse = new VerifyCredentialResponse(response.getTimeout());
                return result;
            } else {
                result.gkResponse = VerifyCredentialResponse.ERROR;
                return result;
            }
        } else if (!isWeaverAvailable()) {
            warnLog(TAG, "No weaver service to unwrap password based SP");
            result.gkResponse = VerifyCredentialResponse.ERROR;
            return result;
        } else {
            result.gkResponse = weaverVerify(weaverSlot, passwordTokenToWeaverKey(pwdToken));
            if (result.gkResponse.getResponseCode() != 0) {
                return result;
            }
            sid = 0;
            credential3 = transformUnderWeaverSecret(pwdToken, result.gkResponse.getPayload());
            z = true;
        }
        if (progressCallback != null) {
            progressCallback.onCredentialVerified();
        }
        result.authToken = unwrapSyntheticPasswordBlob(handle, (byte) 0, credential3, sid, userId);
        StringBuilder sb = new StringBuilder();
        sb.append("unwrapSyntheticPasswordBlob finish: user=");
        sb.append(userId);
        if (result.authToken != null) {
            z = false;
        }
        sb.append(z);
        flog(TAG, sb.toString());
        result.gkResponse = verifyChallenge(gatekeeper, result.authToken, 0, userId);
        return result;
    }

    public AuthenticationResult unwrapTokenBasedSyntheticPassword(IGateKeeperService gatekeeper, long handle, byte[] token, int userId) {
        byte[] secdiscardable;
        AuthenticationResult result = new AuthenticationResult();
        byte[] secdiscardable2 = loadSecdiscardable(handle, userId);
        int slotId = loadWeaverSlot(handle, userId);
        if (slotId == -1) {
            secdiscardable = secdiscardable2;
        } else if (!isWeaverAvailable()) {
            Log.e(TAG, "No weaver service to unwrap token based SP");
            result.gkResponse = VerifyCredentialResponse.ERROR;
            return result;
        } else {
            VerifyCredentialResponse response = weaverVerify(slotId, null);
            if (response.getResponseCode() != 0 || response.getPayload() == null) {
                Log.e(TAG, "Failed to retrieve weaver secret when unwrapping token");
                result.gkResponse = VerifyCredentialResponse.ERROR;
                return result;
            }
            secdiscardable = SyntheticPasswordCrypto.decrypt(response.getPayload(), PERSONALISATION_WEAVER_TOKEN, secdiscardable2);
        }
        result.authToken = unwrapSyntheticPasswordBlob(handle, (byte) 1, transformUnderSecdiscardable(token, secdiscardable), 0, userId);
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

    private AuthenticationToken unwrapSyntheticPasswordBlob(long handle, byte type, byte[] applicationId, long sid, int userId) {
        byte[] secret;
        byte[] blob = loadState(SP_BLOB_NAME, handle, userId);
        if (blob == null) {
            return null;
        }
        byte version = blob[0];
        if (version != 3 && version != 2 && version != 1) {
            throw new RuntimeException("Unknown blob version");
        } else if (blob[1] == type) {
            if (version == 1) {
                secret = SyntheticPasswordCrypto.decryptBlobV1(getHandleName(handle), Arrays.copyOfRange(blob, 2, blob.length), applicationId);
            } else {
                secret = decryptSPBlob(getHandleName(handle), Arrays.copyOfRange(blob, 2, blob.length), applicationId);
            }
            if (secret == null) {
                errorLog(TAG, "Fail to decrypt SP for user " + userId);
                return null;
            }
            AuthenticationToken result = new AuthenticationToken(version);
            if (type != 1) {
                result.syntheticPassword = new String(secret);
            } else if (!loadEscrowData(result, userId)) {
                Log.e(TAG, "User is not escrowable: " + userId);
                return null;
            } else {
                result.recreate(secret);
            }
            if (version != 1) {
                return result;
            }
            warnLog(TAG, "Upgrade v1 SP blob for user " + userId + ", type = " + ((int) type));
            createSyntheticPasswordBlob(handle, type, result, applicationId, sid, userId);
            return result;
        } else {
            throw new RuntimeException("Invalid blob type");
        }
    }

    public VerifyCredentialResponse verifyChallenge(IGateKeeperService gatekeeper, AuthenticationToken auth, long challenge, int userId) {
        byte[] spHandle;
        if (auth == null || (spHandle = loadSyntheticPasswordHandle(userId)) == null) {
            return null;
        }
        GateKeeperResponse response = gatekeeper.verifyChallenge(userId, challenge, spHandle, auth.deriveGkPassword());
        int responseCode = response.getResponseCode();
        if (responseCode == 0) {
            VerifyCredentialResponse result = new VerifyCredentialResponse(response.getPayload());
            if (!response.getShouldReEnroll()) {
                return result;
            }
            Log.i(TAG, "verifyChallenge should reenroll user " + userId);
            GateKeeperResponse response2 = gatekeeper.enroll(userId, spHandle, auth.deriveGkPassword(), auth.deriveGkPassword());
            if (response2.getResponseCode() == 0) {
                saveSyntheticPasswordHandle(response2.getPayload(), userId);
                return verifyChallenge(gatekeeper, auth, challenge, userId);
            }
            warnLog(TAG, "Fail to re-enroll SP handle for user " + userId);
            return result;
        } else if (responseCode == 1) {
            return new VerifyCredentialResponse(response.getTimeout());
        } else {
            return VerifyCredentialResponse.ERROR;
        }
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

    public void saveState(String stateName, byte[] data, long handle, int userId) {
        this.mStorage.writeSyntheticPasswordState(userId, handle, stateName, data);
    }

    public void destroyState(String stateName, long handle, int userId) {
        this.mStorage.deleteSyntheticPasswordState(userId, handle, stateName);
    }

    public byte[] decryptSPBlob(String blobKeyName, byte[] blob, byte[] applicationId) {
        return SyntheticPasswordCrypto.decryptBlob(blobKeyName, blob, applicationId);
    }

    public byte[] createSPBlob(String blobKeyName, byte[] data, byte[] applicationId, long sid) {
        return SyntheticPasswordCrypto.createBlob(blobKeyName, data, applicationId, sid);
    }

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

    public static int fakeUid(int uid) {
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
        return String.format("%s%x", "synthetic_password_", Long.valueOf(handle));
    }

    private byte[] computePasswordToken(byte[] password, PasswordData data) {
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

    public long sidFromPasswordHandle(byte[] handle) {
        return nativeSidFromPasswordHandle(handle);
    }

    public byte[] scrypt(byte[] password, byte[] salt, int n, int r, int p, int outLen) {
        return new Scrypt().scrypt(password, salt, n, r, p, outLen);
    }

    protected static ArrayList<Byte> toByteArrayList(byte[] data) {
        ArrayList<Byte> result = new ArrayList<>(data.length);
        for (byte b : data) {
            result.add(Byte.valueOf(b));
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

    public static byte[] bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return "null".getBytes();
        }
        byte[] hexBytes = new byte[(bytes.length * 2)];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 255;
            byte[] bArr = HEX_ARRAY;
            hexBytes[j * 2] = bArr[v >>> 4];
            hexBytes[(j * 2) + 1] = bArr[v & 15];
        }
        return hexBytes;
    }

    public void errorLog(String tag, String msg) {
    }

    public void warnLog(String tag, String msg) {
    }

    public void flog(String tag, String msg) {
    }

    public void updateWeaverTypeFlag(int flag, int userId) {
    }

    public void updateCredentialTypeFlag(long flag, int userId) {
    }

    public void weaverLinkToDeath() {
    }

    public void backupPasswordBasedSyntheticPassword(long newHandle, long lastHandle, int userId) {
    }

    public boolean isUseWeaver(long handle, int userId) {
        return loadWeaverSlot(handle, userId) != -1;
    }
}
