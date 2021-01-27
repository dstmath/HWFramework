package com.android.server.wifi.cast.P2pSharing;

import android.security.keystore.KeyGenParameterSpec;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Calendar;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

class EncryptRsa {
    private static final String CIPHER_AND_PADDING_MODE = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final int DECRYPT_BLOCK_LENGTH = 256;
    private static final int ENCRYPT_BLOCK_LENGTH = 128;
    private static final String ENCRYPT_KEY_ALIAS = "EncryptionForP2PSharing";
    private static final String KEYSTORE_TYPE = "AndroidKeyStore";
    private static final int KEY_LENGTH = 2048;
    private static final String SIGNATURE_MODE = "SHA256WithRSA/PSS";
    private static final String SIGN_KEY_ALIAS = "SignatureForP2PSharing";
    private static final String TAG = "P2pSharing:EncryptRsa";
    private static final int VALID_YEAR = 20;
    private static OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-1"), PSource.PSpecified.DEFAULT);
    private KeyStore keyStore;
    private char[] recoveryAlias = Arrays.toString(EncryptGcm.getRandomBytes(16)).toCharArray();

    EncryptRsa() {
    }

    private void createEncryptKeys() {
        initKeyStore(ENCRYPT_KEY_ALIAS, 3, "OAEPPadding");
    }

    private void createSignatureKeys() {
        initKeyStore(SIGN_KEY_ALIAS, 12, "PSS");
    }

    private void initKeyStore(String alias, int keyPurpose, String paddings) {
        try {
            this.keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
            this.keyStore.load(null);
            createNewKeys(alias, genParameterSpec(alias, keyPurpose, paddings));
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            HwHiLog.e(TAG, false, "Init KeyStore Exception", new Object[0]);
        }
    }

    private AlgorithmParameterSpec genParameterSpec(String alias, int keyPurpose, String paddings) {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(1, 20);
        KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(alias, keyPurpose).setDigests("SHA-256").setKeySize(2048).setCertificateNotBefore(start.getTime()).setCertificateNotAfter(end.getTime());
        if (keyPurpose == 3) {
            builder.setEncryptionPaddings(paddings);
            return builder.build();
        }
        if (keyPurpose == 12) {
            builder.setSignaturePaddings(paddings);
        }
        return builder.build();
    }

    private void createNewKeys(String alias, AlgorithmParameterSpec spec) {
        if (TextUtils.isEmpty(alias)) {
            HwHiLog.w(TAG, false, "Alias is empty", new Object[0]);
            return;
        }
        try {
            if (this.keyStore.containsAlias(alias)) {
                HwHiLog.w(TAG, false, "the key has been created", new Object[0]);
                return;
            }
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", KEYSTORE_TYPE);
            generator.initialize(spec);
            generator.generateKeyPair();
            HwHiLog.i(TAG, false, "Key pair are created", new Object[0]);
        } catch (InvalidAlgorithmParameterException | KeyStoreException | NoSuchAlgorithmException | NoSuchProviderException | ProviderException e) {
            HwHiLog.e(TAG, false, "createNewKeys Exception", new Object[0]);
        }
    }

    /* access modifiers changed from: package-private */
    public byte[] getEncryptPublicKey() {
        createEncryptKeys();
        return getPublicKey(ENCRYPT_KEY_ALIAS);
    }

    /* access modifiers changed from: package-private */
    public byte[] getSignPublicKey() {
        createSignatureKeys();
        return getPublicKey(SIGN_KEY_ALIAS);
    }

    private byte[] getPublicKey(String alias) {
        if (TextUtils.isEmpty(alias)) {
            HwHiLog.e(TAG, false, "Get pub key alias is empty", new Object[0]);
        }
        try {
            Certificate certificate = this.keyStore.getCertificate(alias);
            if (certificate == null) {
                HwHiLog.e(TAG, false, "certificate is null", new Object[0]);
                return new byte[0];
            }
            PublicKey pubKey = certificate.getPublicKey();
            if (pubKey != null) {
                return pubKey.getEncoded();
            }
            HwHiLog.e(TAG, false, "pubKey is null", new Object[0]);
            return new byte[0];
        } catch (KeyStoreException e) {
            HwHiLog.e(TAG, false, "KeyStore Exception", new Object[0]);
            return new byte[0];
        }
    }

    private Key byte2PublicKey(byte[] pubStr) {
        try {
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubStr));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            HwHiLog.e(TAG, false, "byte2PublicKey error", new Object[0]);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public byte[] encryptByteArray(byte[] needEncryptWord, byte[] key) {
        if (Utils.isEmptyByteArray(needEncryptWord) || Utils.isEmptyByteArray(key)) {
            HwHiLog.w(TAG, false, "Encrypt - array is empty", new Object[0]);
            return new byte[0];
        }
        Key publicKey = byte2PublicKey(key);
        if (needEncryptWord.length <= 128) {
            return doCipher(1, needEncryptWord, publicKey, needEncryptWord.length);
        }
        return doCipherBlocks(1, needEncryptWord, publicKey);
    }

    /* access modifiers changed from: package-private */
    public byte[] decryptByteArray(byte[] needDecryptWord) {
        if (Utils.isEmptyByteArray(needDecryptWord)) {
            HwHiLog.e(TAG, false, "ByteArray empty", new Object[0]);
            return new byte[0];
        }
        try {
            Key privateKey = this.keyStore.getKey(ENCRYPT_KEY_ALIAS, this.recoveryAlias);
            if (needDecryptWord.length < 256) {
                return doCipher(2, needDecryptWord, privateKey, needDecryptWord.length);
            }
            return doCipherBlocks(2, needDecryptWord, privateKey);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException e) {
            HwHiLog.e(TAG, false, "decryptByteArray key exception", new Object[0]);
            return new byte[0];
        }
    }

    private byte[] doCipher(int opmode, byte[] needCipherData, Key key, int cipherLen) {
        if (Utils.isEmptyByteArray(needCipherData) || key == null || needCipherData.length < cipherLen) {
            HwHiLog.e(TAG, false, "Data or Key is empty", new Object[0]);
            return new byte[0];
        }
        byte[] tmp = new byte[cipherLen];
        System.arraycopy(needCipherData, 0, tmp, 0, cipherLen);
        try {
            Cipher output = Cipher.getInstance(CIPHER_AND_PADDING_MODE);
            output.init(opmode, key, oaepParams);
            return output.doFinal(tmp);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            HwHiLog.e(TAG, false, "doCipher Exception: " + opmode, new Object[0]);
            return new byte[0];
        }
    }

    private byte[] doCipherBlocks(int opmode, byte[] needCipherData, Key key) {
        int resultBlockLen = 128;
        int blockLen = opmode == 1 ? 128 : 256;
        if (opmode == 1) {
            resultBlockLen = 256;
        }
        int blockNum = needCipherData.length / blockLen;
        if (needCipherData.length % blockLen != 0) {
            blockNum++;
        }
        byte[] result = new byte[(blockNum * resultBlockLen)];
        byte[] needCipherBlock = new byte[blockLen];
        int cipheredSize = 0;
        for (int blockIndex = 0; blockIndex < blockNum; blockIndex++) {
            int cipherLen = Math.min(blockLen, needCipherData.length - (blockIndex * blockLen));
            System.arraycopy(needCipherData, blockIndex * blockLen, needCipherBlock, 0, cipherLen);
            byte[] ciphered = doCipher(opmode, needCipherBlock, key, cipherLen);
            System.arraycopy(ciphered, 0, result, cipheredSize, ciphered.length);
            cipheredSize += ciphered.length;
        }
        if (cipheredSize == result.length) {
            return result;
        }
        byte[] cipheredBytes = new byte[cipheredSize];
        System.arraycopy(result, 0, cipheredBytes, 0, cipheredSize);
        Utils.resetArrays(result);
        return cipheredBytes;
    }

    /* access modifiers changed from: package-private */
    public void clearKeyStore() {
        try {
            this.keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
            this.keyStore.load(null);
            this.keyStore.deleteEntry(ENCRYPT_KEY_ALIAS);
            this.keyStore.deleteEntry(SIGN_KEY_ALIAS);
            Arrays.fill(this.recoveryAlias, '0');
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            HwHiLog.e(TAG, false, "Clear KeyStore error", new Object[0]);
        }
        HwHiLog.i(TAG, false, "KeyStore cleared", new Object[0]);
    }

    /* access modifiers changed from: package-private */
    public byte[] sign(byte[] data) {
        try {
            Signature signature = Signature.getInstance(SIGNATURE_MODE);
            Key privateKey = this.keyStore.getKey(SIGN_KEY_ALIAS, this.recoveryAlias);
            if (!(privateKey instanceof PrivateKey)) {
                HwHiLog.w(TAG, false, "Signature failed: key error", new Object[0]);
                return new byte[0];
            }
            signature.initSign((PrivateKey) privateKey);
            signature.update(data);
            return signature.sign();
        } catch (InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | SignatureException | UnrecoverableKeyException e) {
            HwHiLog.e(TAG, false, "Signature failed", new Object[0]);
            return new byte[0];
        }
    }

    /* access modifiers changed from: package-private */
    public boolean verify(byte[] data, byte[] publicKey, byte[] sign) {
        try {
            Signature signature = Signature.getInstance(SIGNATURE_MODE);
            Key pubKey = byte2PublicKey(publicKey);
            if (!(pubKey instanceof PublicKey)) {
                return false;
            }
            signature.initVerify((PublicKey) pubKey);
            signature.update(data);
            return signature.verify(sign);
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            HwHiLog.e(TAG, false, "Verify failed", new Object[0]);
            return false;
        }
    }
}
