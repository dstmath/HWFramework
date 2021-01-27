package com.android.server.wifi.util;

import android.security.keystore.KeyGenParameterSpec;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.DigestException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class DataIntegrityChecker {
    private static final String ALIAS_SUFFIX = ".data-integrity-checker-key";
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final String DIGEST_ALGORITHM = "SHA-256";
    private static final String FILE_SUFFIX = ".encrypted-checksum";
    private static final int GCM_TAG_LENGTH = 128;
    private static final boolean KEYSTORE_FAILURE_RETURN_VALUE = true;
    private static final String KEY_STORE = "AndroidKeyStore";
    private static final boolean REQUEST_BUG_REPORT = false;
    private static final String TAG = "DataIntegrityChecker";
    private final File mIntegrityFile;

    public DataIntegrityChecker(String integrityFilename) {
        if (!TextUtils.isEmpty(integrityFilename)) {
            this.mIntegrityFile = new File(integrityFilename + FILE_SUFFIX);
            return;
        }
        throw new NullPointerException("integrityFilename must not be null or the empty string");
    }

    public void update(byte[] data) {
        if (data == null || data.length < 1) {
            reportException(new Exception("No data to update"), "No data to update.");
            return;
        }
        byte[] digest = getDigest(data);
        if (digest != null && digest.length >= 1) {
            EncryptedData integrityData = encrypt(digest, this.mIntegrityFile.getName() + ALIAS_SUFFIX);
            if (integrityData != null) {
                writeIntegrityData(integrityData, this.mIntegrityFile);
            } else {
                reportException(new Exception("integrityData null upon update"), "integrityData null upon update");
            }
        }
    }

    public boolean isOk(byte[] data) throws DigestException {
        byte[] currentDigest;
        if (data == null || data.length < 1 || (currentDigest = getDigest(data)) == null || currentDigest.length < 1) {
            return true;
        }
        try {
            EncryptedData encryptedData = readIntegrityData(this.mIntegrityFile);
            if (encryptedData != null) {
                byte[] storedDigest = decrypt(encryptedData);
                if (storedDigest == null) {
                    return true;
                }
                return constantTimeEquals(storedDigest, currentDigest);
            }
            throw new DigestException("No stored digest is available to compare.");
        } catch (IOException e) {
            reportException(e, "readIntegrityData had an IO exception");
            return true;
        } catch (ClassNotFoundException e2) {
            reportException(e2, "readIntegrityData could not find the class EncryptedData");
            return true;
        }
    }

    private byte[] getDigest(byte[] data) {
        try {
            return MessageDigest.getInstance(DIGEST_ALGORITHM).digest(data);
        } catch (NoSuchAlgorithmException e) {
            reportException(e, "getDigest could not find algorithm: SHA-256");
            return null;
        }
    }

    private EncryptedData encrypt(byte[] data, String keyAlias) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            SecretKey secretKeyReference = getOrCreateSecretKey(keyAlias);
            if (secretKeyReference != null) {
                cipher.init(1, secretKeyReference);
                return new EncryptedData(cipher.doFinal(data), cipher.getIV(), keyAlias);
            }
            reportException(new Exception("secretKeyReference is null."), "secretKeyReference is null.");
            return null;
        } catch (NoSuchAlgorithmException e) {
            reportException(e, "encrypt could not find the algorithm: AES/GCM/NoPadding");
            return null;
        } catch (NoSuchPaddingException e2) {
            reportException(e2, "encrypt had a padding exception");
            return null;
        } catch (InvalidKeyException e3) {
            reportException(e3, "encrypt received an invalid key");
            return null;
        } catch (BadPaddingException e4) {
            reportException(e4, "encrypt had a padding problem");
            return null;
        } catch (IllegalBlockSizeException e5) {
            reportException(e5, "encrypt had an illegal block size");
            return null;
        }
    }

    private byte[] decrypt(EncryptedData encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(128, encryptedData.getIv());
            SecretKey secretKeyReference = getOrCreateSecretKey(encryptedData.getKeyAlias());
            if (secretKeyReference == null) {
                return null;
            }
            cipher.init(2, secretKeyReference, spec);
            return cipher.doFinal(encryptedData.getEncryptedData());
        } catch (NoSuchAlgorithmException e) {
            reportException(e, "decrypt could not find cipher algorithm AES/GCM/NoPadding");
            return null;
        } catch (NoSuchPaddingException e2) {
            reportException(e2, "decrypt could not find padding algorithm");
            return null;
        } catch (IllegalBlockSizeException e3) {
            reportException(e3, "decrypt had a illegal block size");
            return null;
        } catch (BadPaddingException e4) {
            reportException(e4, "decrypt had bad padding");
            return null;
        } catch (InvalidKeyException e5) {
            reportException(e5, "decrypt had an invalid key");
            return null;
        } catch (InvalidAlgorithmParameterException e6) {
            reportException(e6, "decrypt had an invalid algorithm parameter");
            return null;
        }
    }

    private SecretKey getOrCreateSecretKey(String keyAlias) {
        SecretKey secretKey = null;
        try {
            KeyStore keyStore = KeyStore.getInstance(KEY_STORE);
            keyStore.load(null);
            if (keyStore.containsAlias(keyAlias)) {
                KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(keyAlias, null);
                if (secretKeyEntry != null) {
                    secretKey = secretKeyEntry.getSecretKey();
                } else {
                    reportException(new Exception("keystore contains the alias and the secret key entry was null"), "keystore contains the alias and the secret key entry was null");
                }
                return secretKey;
            }
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", KEY_STORE);
            keyGenerator.init(new KeyGenParameterSpec.Builder(keyAlias, 3).setBlockModes("GCM").setEncryptionPaddings("NoPadding").build());
            return keyGenerator.generateKey();
        } catch (CertificateException e) {
            reportException(e, "getOrCreateSecretKey had a certificate exception.");
            return null;
        } catch (InvalidAlgorithmParameterException e2) {
            reportException(e2, "getOrCreateSecretKey had an invalid algorithm parameter");
            return null;
        } catch (IOException e3) {
            reportException(e3, "getOrCreateSecretKey had an IO exception.");
            return null;
        } catch (KeyStoreException e4) {
            reportException(e4, "getOrCreateSecretKey cannot find the keystore: AndroidKeyStore");
            return null;
        } catch (NoSuchAlgorithmException e5) {
            reportException(e5, "getOrCreateSecretKey cannot find algorithm");
            return null;
        } catch (NoSuchProviderException e6) {
            reportException(e6, "getOrCreateSecretKey cannot find crypto provider");
            return null;
        } catch (UnrecoverableEntryException e7) {
            reportException(e7, "getOrCreateSecretKey had an unrecoverable entry exception.");
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0018, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0019, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001c, code lost:
        throw r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x001f, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0020, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0023, code lost:
        throw r2;
     */
    private void writeIntegrityData(EncryptedData encryptedData, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(encryptedData);
            $closeResource(null, oos);
            $closeResource(null, fos);
        } catch (FileNotFoundException e) {
            reportException(e, "writeIntegrityData could not find the integrity file");
        } catch (IOException e2) {
            reportException(e2, "writeIntegrityData had an IO exception");
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001b, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001c, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001f, code lost:
        throw r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0022, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0023, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0026, code lost:
        throw r3;
     */
    private EncryptedData readIntegrityData(File file) throws IOException, ClassNotFoundException {
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            EncryptedData encryptedData = (EncryptedData) ois.readObject();
            $closeResource(null, ois);
            $closeResource(null, fis);
            return encryptedData;
        } catch (FileNotFoundException e) {
            Log.w(TAG, "readIntegrityData could not find integrity file");
            return null;
        }
    }

    private boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null || a.length != b.length) {
            return false;
        }
        byte differenceAccumulator = 0;
        for (int i = 0; i < a.length; i++) {
            differenceAccumulator = (byte) ((a[i] ^ b[i]) | differenceAccumulator);
        }
        if (differenceAccumulator == 0) {
            return true;
        }
        return false;
    }

    private void reportException(Exception exception, String error) {
        Log.wtf(TAG, "An irrecoverable key store error was encountered: " + error);
    }
}
