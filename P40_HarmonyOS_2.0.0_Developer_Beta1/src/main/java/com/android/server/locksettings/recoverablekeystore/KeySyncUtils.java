package com.android.server.locksettings.recoverablekeystore;

import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.AEADBadTagException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class KeySyncUtils {
    private static final byte[] ENCRYPTED_APPLICATION_KEY_HEADER = "V1 encrypted_application_key".getBytes(StandardCharsets.UTF_8);
    private static final int KEY_CLAIMANT_LENGTH_BYTES = 16;
    private static final byte[] LOCALLY_ENCRYPTED_RECOVERY_KEY_HEADER = "V1 locally_encrypted_recovery_key".getBytes(StandardCharsets.UTF_8);
    private static final String PUBLIC_KEY_FACTORY_ALGORITHM = "EC";
    private static final byte[] RECOVERY_CLAIM_HEADER = "V1 KF_claim".getBytes(StandardCharsets.UTF_8);
    private static final String RECOVERY_KEY_ALGORITHM = "AES";
    private static final int RECOVERY_KEY_SIZE_BITS = 256;
    private static final byte[] RECOVERY_RESPONSE_HEADER = "V1 reencrypted_recovery_key".getBytes(StandardCharsets.UTF_8);
    private static final byte[] THM_ENCRYPTED_RECOVERY_KEY_HEADER = "V1 THM_encrypted_recovery_key".getBytes(StandardCharsets.UTF_8);
    private static final byte[] THM_KF_HASH_PREFIX = "THM_KF_hash".getBytes(StandardCharsets.UTF_8);

    public static byte[] thmEncryptRecoveryKey(PublicKey publicKey, byte[] lockScreenHash, byte[] vaultParams, SecretKey recoveryKey) throws NoSuchAlgorithmException, InvalidKeyException {
        return SecureBox.encrypt(publicKey, calculateThmKfHash(lockScreenHash), concat(THM_ENCRYPTED_RECOVERY_KEY_HEADER, vaultParams), locallyEncryptRecoveryKey(lockScreenHash, recoveryKey));
    }

    public static byte[] calculateThmKfHash(byte[] lockScreenHash) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(THM_KF_HASH_PREFIX);
        messageDigest.update(lockScreenHash);
        return messageDigest.digest();
    }

    @VisibleForTesting
    static byte[] locallyEncryptRecoveryKey(byte[] lockScreenHash, SecretKey recoveryKey) throws NoSuchAlgorithmException, InvalidKeyException {
        return SecureBox.encrypt(null, lockScreenHash, LOCALLY_ENCRYPTED_RECOVERY_KEY_HEADER, recoveryKey.getEncoded());
    }

    public static SecretKey generateRecoveryKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(RECOVERY_KEY_ALGORITHM);
        keyGenerator.init(256, new SecureRandom());
        return keyGenerator.generateKey();
    }

    public static Map<String, byte[]> encryptKeysWithRecoveryKey(SecretKey recoveryKey, Map<String, Pair<SecretKey, byte[]>> keys) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] header;
        HashMap<String, byte[]> encryptedKeys = new HashMap<>();
        for (String alias : keys.keySet()) {
            SecretKey key = (SecretKey) keys.get(alias).first;
            byte[] metadata = (byte[]) keys.get(alias).second;
            if (metadata == null) {
                header = ENCRYPTED_APPLICATION_KEY_HEADER;
            } else {
                header = concat(ENCRYPTED_APPLICATION_KEY_HEADER, metadata);
            }
            encryptedKeys.put(alias, SecureBox.encrypt(null, recoveryKey.getEncoded(), header, key.getEncoded()));
        }
        return encryptedKeys;
    }

    public static byte[] generateKeyClaimant() {
        byte[] key = new byte[16];
        new SecureRandom().nextBytes(key);
        return key;
    }

    public static byte[] encryptRecoveryClaim(PublicKey publicKey, byte[] vaultParams, byte[] challenge, byte[] thmKfHash, byte[] keyClaimant) throws NoSuchAlgorithmException, InvalidKeyException {
        return SecureBox.encrypt(publicKey, null, concat(RECOVERY_CLAIM_HEADER, vaultParams, challenge), concat(thmKfHash, keyClaimant));
    }

    public static byte[] decryptRecoveryClaimResponse(byte[] keyClaimant, byte[] vaultParams, byte[] encryptedResponse) throws NoSuchAlgorithmException, InvalidKeyException, AEADBadTagException {
        return SecureBox.decrypt(null, keyClaimant, concat(RECOVERY_RESPONSE_HEADER, vaultParams), encryptedResponse);
    }

    public static byte[] decryptRecoveryKey(byte[] lskfHash, byte[] encryptedRecoveryKey) throws NoSuchAlgorithmException, InvalidKeyException, AEADBadTagException {
        return SecureBox.decrypt(null, lskfHash, LOCALLY_ENCRYPTED_RECOVERY_KEY_HEADER, encryptedRecoveryKey);
    }

    public static byte[] decryptApplicationKey(byte[] recoveryKey, byte[] encryptedApplicationKey, byte[] applicationKeyMetadata) throws NoSuchAlgorithmException, InvalidKeyException, AEADBadTagException {
        byte[] header;
        if (applicationKeyMetadata == null) {
            header = ENCRYPTED_APPLICATION_KEY_HEADER;
        } else {
            header = concat(ENCRYPTED_APPLICATION_KEY_HEADER, applicationKeyMetadata);
        }
        return SecureBox.decrypt(null, recoveryKey, header, encryptedApplicationKey);
    }

    public static PublicKey deserializePublicKey(byte[] key) throws InvalidKeySpecException {
        try {
            return KeyFactory.getInstance(PUBLIC_KEY_FACTORY_ALGORITHM).generatePublic(new X509EncodedKeySpec(key));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] packVaultParams(PublicKey thmPublicKey, long counterId, int maxAttempts, byte[] vaultHandle) {
        return ByteBuffer.allocate(vaultHandle.length + 77).order(ByteOrder.LITTLE_ENDIAN).put(SecureBox.encodePublicKey(thmPublicKey)).putLong(counterId).putInt(maxAttempts).put(vaultHandle).array();
    }

    @VisibleForTesting
    static byte[] concat(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte[] concatenated = new byte[length];
        int pos = 0;
        for (byte[] array2 : arrays) {
            System.arraycopy(array2, 0, concatenated, pos, array2.length);
            pos += array2.length;
        }
        return concatenated;
    }

    private KeySyncUtils() {
    }
}
