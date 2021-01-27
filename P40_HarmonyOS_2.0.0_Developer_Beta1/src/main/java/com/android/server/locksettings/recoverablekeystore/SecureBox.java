package com.android.server.locksettings.recoverablekeystore;

import com.android.internal.annotations.VisibleForTesting;
import java.math.BigInteger;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECFieldFp;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SecureBox {
    private static final BigInteger BIG_INT_02 = BigInteger.valueOf(2);
    private static final String CIPHER_ALG = "AES";
    private static final byte[] CONSTANT_01 = {1};
    private static final String EC_ALG = "EC";
    private static final int EC_COORDINATE_LEN_BYTES = 32;
    private static final String EC_P256_COMMON_NAME = "secp256r1";
    private static final String EC_P256_OPENSSL_NAME = "prime256v1";
    private static final BigInteger EC_PARAM_A = EC_PARAM_P.subtract(new BigInteger("3"));
    private static final BigInteger EC_PARAM_B = new BigInteger("5ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b", 16);
    private static final BigInteger EC_PARAM_P = new BigInteger("ffffffff00000001000000000000000000000000ffffffffffffffffffffffff", 16);
    @VisibleForTesting
    static final ECParameterSpec EC_PARAM_SPEC = new ECParameterSpec(new EllipticCurve(new ECFieldFp(EC_PARAM_P), EC_PARAM_A, EC_PARAM_B), new ECPoint(new BigInteger("6b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296", 16), new BigInteger("4fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf51f5", 16)), new BigInteger("ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551", 16), 1);
    private static final int EC_PUBLIC_KEY_LEN_BYTES = 65;
    private static final byte EC_PUBLIC_KEY_PREFIX = 4;
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final String ENC_ALG = "AES/GCM/NoPadding";
    private static final int GCM_KEY_LEN_BYTES = 16;
    private static final int GCM_NONCE_LEN_BYTES = 12;
    private static final int GCM_TAG_LEN_BYTES = 16;
    private static final byte[] HKDF_INFO_WITHOUT_PUBLIC_KEY = "SHARED HKDF-SHA-256 AES-128-GCM".getBytes(StandardCharsets.UTF_8);
    private static final byte[] HKDF_INFO_WITH_PUBLIC_KEY = "P256 HKDF-SHA-256 AES-128-GCM".getBytes(StandardCharsets.UTF_8);
    private static final byte[] HKDF_SALT = concat("SECUREBOX".getBytes(StandardCharsets.UTF_8), VERSION);
    private static final String KA_ALG = "ECDH";
    private static final String MAC_ALG = "HmacSHA256";
    private static final byte[] VERSION = {2, 0};

    /* access modifiers changed from: private */
    public enum AesGcmOperation {
        ENCRYPT,
        DECRYPT
    }

    private SecureBox() {
    }

    public static KeyPair genKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(EC_ALG);
        try {
            keyPairGenerator.initialize(new ECGenParameterSpec(EC_P256_OPENSSL_NAME));
            return keyPairGenerator.generateKeyPair();
        } catch (InvalidAlgorithmParameterException e) {
            try {
                keyPairGenerator.initialize(new ECGenParameterSpec(EC_P256_COMMON_NAME));
                return keyPairGenerator.generateKeyPair();
            } catch (InvalidAlgorithmParameterException ex) {
                throw new NoSuchAlgorithmException("Unable to find the NIST P-256 curve", ex);
            }
        }
    }

    public static byte[] encrypt(PublicKey theirPublicKey, byte[] sharedSecret, byte[] header, byte[] payload) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] hkdfInfo;
        byte[] dhSecret;
        KeyPair senderKeyPair;
        byte[] sharedSecret2 = emptyByteArrayIfNull(sharedSecret);
        if (theirPublicKey == null && sharedSecret2.length == 0) {
            throw new IllegalArgumentException("Both the public key and shared secret are empty");
        }
        byte[] header2 = emptyByteArrayIfNull(header);
        byte[] payload2 = emptyByteArrayIfNull(payload);
        if (theirPublicKey == null) {
            senderKeyPair = null;
            dhSecret = EMPTY_BYTE_ARRAY;
            hkdfInfo = HKDF_INFO_WITHOUT_PUBLIC_KEY;
        } else {
            senderKeyPair = genKeyPair();
            dhSecret = dhComputeSecret(senderKeyPair.getPrivate(), theirPublicKey);
            hkdfInfo = HKDF_INFO_WITH_PUBLIC_KEY;
        }
        byte[] randNonce = genRandomNonce();
        byte[] ciphertext = aesGcmEncrypt(hkdfDeriveKey(concat(dhSecret, sharedSecret2), HKDF_SALT, hkdfInfo), randNonce, payload2, header2);
        if (senderKeyPair == null) {
            return concat(VERSION, randNonce, ciphertext);
        }
        return concat(VERSION, encodePublicKey(senderKeyPair.getPublic()), randNonce, ciphertext);
    }

    public static byte[] decrypt(PrivateKey ourPrivateKey, byte[] sharedSecret, byte[] header, byte[] encryptedPayload) throws NoSuchAlgorithmException, InvalidKeyException, AEADBadTagException {
        byte[] dhSecret;
        byte[] senderPublicKeyBytes;
        byte[] sharedSecret2 = emptyByteArrayIfNull(sharedSecret);
        if (ourPrivateKey == null && sharedSecret2.length == 0) {
            throw new IllegalArgumentException("Both the private key and shared secret are empty");
        }
        byte[] header2 = emptyByteArrayIfNull(header);
        if (encryptedPayload != null) {
            ByteBuffer ciphertextBuffer = ByteBuffer.wrap(encryptedPayload);
            if (Arrays.equals(readEncryptedPayload(ciphertextBuffer, VERSION.length), VERSION)) {
                if (ourPrivateKey == null) {
                    senderPublicKeyBytes = EMPTY_BYTE_ARRAY;
                    dhSecret = HKDF_INFO_WITHOUT_PUBLIC_KEY;
                } else {
                    byte[] dhSecret2 = dhComputeSecret(ourPrivateKey, decodePublicKey(readEncryptedPayload(ciphertextBuffer, 65)));
                    dhSecret = HKDF_INFO_WITH_PUBLIC_KEY;
                    senderPublicKeyBytes = dhSecret2;
                }
                return aesGcmDecrypt(hkdfDeriveKey(concat(senderPublicKeyBytes, sharedSecret2), HKDF_SALT, dhSecret), readEncryptedPayload(ciphertextBuffer, 12), readEncryptedPayload(ciphertextBuffer, ciphertextBuffer.remaining()), header2);
            }
            throw new AEADBadTagException("The payload was not encrypted by SecureBox v2");
        }
        throw new NullPointerException("Encrypted payload must not be null.");
    }

    private static byte[] readEncryptedPayload(ByteBuffer buffer, int length) throws AEADBadTagException {
        byte[] output = new byte[length];
        try {
            buffer.get(output);
            return output;
        } catch (BufferUnderflowException e) {
            throw new AEADBadTagException("The encrypted payload is too short");
        }
    }

    private static byte[] dhComputeSecret(PrivateKey ourPrivateKey, PublicKey theirPublicKey) throws NoSuchAlgorithmException, InvalidKeyException {
        KeyAgreement agreement = KeyAgreement.getInstance(KA_ALG);
        try {
            agreement.init(ourPrivateKey);
            agreement.doPhase(theirPublicKey, true);
            return agreement.generateSecret();
        } catch (RuntimeException ex) {
            throw new InvalidKeyException(ex);
        }
    }

    private static SecretKey hkdfDeriveKey(byte[] secret, byte[] salt, byte[] info) throws NoSuchAlgorithmException {
        Mac mac = Mac.getInstance(MAC_ALG);
        try {
            mac.init(new SecretKeySpec(salt, MAC_ALG));
            try {
                mac.init(new SecretKeySpec(mac.doFinal(secret), MAC_ALG));
                mac.update(info);
                return new SecretKeySpec(Arrays.copyOf(mac.doFinal(CONSTANT_01), 16), CIPHER_ALG);
            } catch (InvalidKeyException ex) {
                throw new RuntimeException(ex);
            }
        } catch (InvalidKeyException ex2) {
            throw new RuntimeException(ex2);
        }
    }

    private static byte[] aesGcmEncrypt(SecretKey key, byte[] nonce, byte[] plaintext, byte[] aad) throws NoSuchAlgorithmException, InvalidKeyException {
        try {
            return aesGcmInternal(AesGcmOperation.ENCRYPT, key, nonce, plaintext, aad);
        } catch (AEADBadTagException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static byte[] aesGcmDecrypt(SecretKey key, byte[] nonce, byte[] ciphertext, byte[] aad) throws NoSuchAlgorithmException, InvalidKeyException, AEADBadTagException {
        return aesGcmInternal(AesGcmOperation.DECRYPT, key, nonce, ciphertext, aad);
    }

    private static byte[] aesGcmInternal(AesGcmOperation operation, SecretKey key, byte[] nonce, byte[] text, byte[] aad) throws NoSuchAlgorithmException, InvalidKeyException, AEADBadTagException {
        try {
            Cipher cipher = Cipher.getInstance(ENC_ALG);
            GCMParameterSpec spec = new GCMParameterSpec(128, nonce);
            try {
                if (operation == AesGcmOperation.DECRYPT) {
                    cipher.init(2, key, spec);
                } else {
                    cipher.init(1, key, spec);
                }
                try {
                    cipher.updateAAD(aad);
                    return cipher.doFinal(text);
                } catch (AEADBadTagException ex) {
                    throw ex;
                } catch (BadPaddingException | IllegalBlockSizeException ex2) {
                    throw new RuntimeException(ex2);
                }
            } catch (InvalidAlgorithmParameterException ex3) {
                throw new RuntimeException(ex3);
            }
        } catch (NoSuchPaddingException ex4) {
            throw new RuntimeException(ex4);
        }
    }

    static byte[] encodePublicKey(PublicKey publicKey) {
        ECPoint point = ((ECPublicKey) publicKey).getW();
        byte[] x = point.getAffineX().toByteArray();
        byte[] y = point.getAffineY().toByteArray();
        byte[] output = new byte[65];
        System.arraycopy(y, 0, output, 65 - y.length, y.length);
        System.arraycopy(x, 0, output, 33 - x.length, x.length);
        output[0] = 4;
        return output;
    }

    @VisibleForTesting
    static PublicKey decodePublicKey(byte[] keyBytes) throws NoSuchAlgorithmException, InvalidKeyException {
        BigInteger x = new BigInteger(1, Arrays.copyOfRange(keyBytes, 1, 33));
        BigInteger y = new BigInteger(1, Arrays.copyOfRange(keyBytes, 33, 65));
        validateEcPoint(x, y);
        try {
            return KeyFactory.getInstance(EC_ALG).generatePublic(new ECPublicKeySpec(new ECPoint(x, y), EC_PARAM_SPEC));
        } catch (InvalidKeySpecException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void validateEcPoint(BigInteger x, BigInteger y) throws InvalidKeyException {
        if (x.compareTo(EC_PARAM_P) >= 0 || y.compareTo(EC_PARAM_P) >= 0 || x.signum() == -1 || y.signum() == -1) {
            throw new InvalidKeyException("Point lies outside of the expected curve");
        } else if (!y.modPow(BIG_INT_02, EC_PARAM_P).equals(x.modPow(BIG_INT_02, EC_PARAM_P).add(EC_PARAM_A).mod(EC_PARAM_P).multiply(x).add(EC_PARAM_B).mod(EC_PARAM_P))) {
            throw new InvalidKeyException("Point lies outside of the expected curve");
        }
    }

    private static byte[] genRandomNonce() throws NoSuchAlgorithmException {
        byte[] nonce = new byte[12];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    /* JADX INFO: Multiple debug info for r1v2 byte[]: [D('i' int), D('output' byte[])] */
    @VisibleForTesting
    static byte[] concat(byte[]... inputs) {
        int length = 0;
        for (int i = 0; i < inputs.length; i++) {
            if (inputs[i] == null) {
                inputs[i] = EMPTY_BYTE_ARRAY;
            }
            length += inputs[i].length;
        }
        byte[] output = new byte[length];
        int outputPos = 0;
        for (byte[] input : inputs) {
            System.arraycopy(input, 0, output, outputPos, input.length);
            outputPos += input.length;
        }
        return output;
    }

    private static byte[] emptyByteArrayIfNull(byte[] input) {
        return input == null ? EMPTY_BYTE_ARRAY : input;
    }
}
