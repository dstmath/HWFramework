package com.android.server.accounts;

import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

class CryptoHelper {
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;
    private static final String KEY_ALGORITHM = "AES";
    private static final String KEY_CIPHER = "cipher";
    private static final String KEY_IV = "iv";
    private static final String KEY_MAC = "mac";
    private static final String MAC_ALGORITHM = "HMACSHA256";
    private static final String TAG = "Account";
    private static CryptoHelper sInstance;
    private final SecretKey mEncryptionKey = KeyGenerator.getInstance(KEY_ALGORITHM).generateKey();
    private final SecretKey mMacKey = KeyGenerator.getInstance(MAC_ALGORITHM).generateKey();

    static synchronized CryptoHelper getInstance() throws NoSuchAlgorithmException {
        CryptoHelper cryptoHelper;
        synchronized (CryptoHelper.class) {
            if (sInstance == null) {
                sInstance = new CryptoHelper();
            }
            cryptoHelper = sInstance;
        }
        return cryptoHelper;
    }

    private CryptoHelper() throws NoSuchAlgorithmException {
    }

    Bundle encryptBundle(Bundle bundle) throws GeneralSecurityException {
        Preconditions.checkNotNull(bundle, "Cannot encrypt null bundle.");
        Parcel parcel = Parcel.obtain();
        bundle.writeToParcel(parcel, 0);
        byte[] clearBytes = parcel.marshall();
        parcel.recycle();
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(1, this.mEncryptionKey);
        byte[] encryptedBytes = cipher.doFinal(clearBytes);
        byte[] iv = cipher.getIV();
        byte[] mac = createMac(encryptedBytes, iv);
        Bundle encryptedBundle = new Bundle();
        encryptedBundle.putByteArray(KEY_CIPHER, encryptedBytes);
        encryptedBundle.putByteArray(KEY_MAC, mac);
        encryptedBundle.putByteArray(KEY_IV, iv);
        return encryptedBundle;
    }

    Bundle decryptBundle(Bundle bundle) throws GeneralSecurityException {
        Preconditions.checkNotNull(bundle, "Cannot decrypt null bundle.");
        byte[] iv = bundle.getByteArray(KEY_IV);
        byte[] encryptedBytes = bundle.getByteArray(KEY_CIPHER);
        if (verifyMac(encryptedBytes, iv, bundle.getByteArray(KEY_MAC))) {
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(2, this.mEncryptionKey, ivSpec);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            Parcel decryptedParcel = Parcel.obtain();
            decryptedParcel.unmarshall(decryptedBytes, 0, decryptedBytes.length);
            decryptedParcel.setDataPosition(0);
            Bundle decryptedBundle = new Bundle();
            decryptedBundle.readFromParcel(decryptedParcel);
            decryptedParcel.recycle();
            return decryptedBundle;
        }
        Log.w(TAG, "Escrow mac mismatched!");
        return null;
    }

    private boolean verifyMac(byte[] cipherArray, byte[] iv, byte[] macArray) throws GeneralSecurityException {
        if (cipherArray != null && cipherArray.length != 0 && macArray != null && macArray.length != 0) {
            return constantTimeArrayEquals(macArray, createMac(cipherArray, iv));
        }
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "Cipher or MAC is empty!");
        }
        return false;
    }

    private byte[] createMac(byte[] cipher, byte[] iv) throws GeneralSecurityException {
        Mac mac = Mac.getInstance(MAC_ALGORITHM);
        mac.init(this.mMacKey);
        mac.update(cipher);
        mac.update(iv);
        return mac.doFinal();
    }

    private static boolean constantTimeArrayEquals(byte[] a, byte[] b) {
        boolean z = true;
        if (a == null || b == null) {
            if (a != b) {
                z = false;
            }
            return z;
        } else if (a.length != b.length) {
            return false;
        } else {
            boolean isEqual = true;
            for (int i = 0; i < b.length; i++) {
                int i2;
                if (a[i] == b[i]) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                isEqual &= i2;
            }
            return isEqual;
        }
    }
}
