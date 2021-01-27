package com.android.server.wifi.cast.P2pSharing;

import android.util.Base64;
import android.util.wifi.HwHiLog;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

class EncryptGcm {
    private static final String ALGORITHM = "AES";
    private static final String DEFAULT_CIPHER_AND_PADDING = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private static final int MODULE_LENGTH = 3;
    private static final String TAG = "P2pSharing:EncryptGCM";

    EncryptGcm() {
    }

    public static byte[] getRandomBytes(int length) {
        try {
            byte[] results = new byte[length];
            SecureRandom.getInstanceStrong().nextBytes(results);
            return results;
        } catch (NoSuchAlgorithmException e) {
            return new byte[0];
        }
    }

    public static byte[] encrypt(byte[] data, byte[] sessionKey, byte[] aadBytes) {
        byte[] aad;
        if (Utils.isEmptyByteArray(data) || Utils.isEmptyByteArray(sessionKey)) {
            return new byte[0];
        }
        byte[] iv = getRandomBytes(12);
        if (Utils.isEmptyByteArray(aadBytes)) {
            aad = getRandomBytes(12);
        } else {
            aad = new byte[aadBytes.length];
            System.arraycopy(aadBytes, 0, aad, 0, aadBytes.length);
        }
        return Base64.encode(gcmEncrypt(data, sessionKey, iv, aad), 0);
    }

    public static byte[] decrypt(byte[] data, byte[] sessionKey) {
        if (!Utils.isEmptyByteArray(data) && data.length >= 24 && !Utils.isEmptyByteArray(sessionKey)) {
            return gcmDecrypt(Base64.decode(data, 0), sessionKey);
        }
        HwHiLog.e(TAG, false, "key or data is null.", new Object[0]);
        return new byte[0];
    }

    private static byte[] gcmEncrypt(byte[] data, byte[] cipherKey, byte[] iv, byte[] aad) {
        int aadLength = aad == null ? 0 : aad.length;
        ArrayList<byte[]> params = new ArrayList<>(2);
        params.add(iv);
        params.add(aad);
        byte[] cipherData = doCipher(1, data, cipherKey, 16, params);
        byte[] ivLengthByteArray = Utils.convertInt2Byte(iv.length);
        byte[] tagLengthByteArray = Utils.convertInt2Byte(16);
        byte[] addLengthByteArray = Utils.convertInt2Byte(aadLength);
        byte[] result = new byte[(iv.length + 12 + aadLength + cipherData.length)];
        System.arraycopy(ivLengthByteArray, 0, result, 0, 4);
        System.arraycopy(tagLengthByteArray, 0, result, 4, 4);
        System.arraycopy(addLengthByteArray, 0, result, 8, 4);
        System.arraycopy(iv, 0, result, 12, iv.length);
        if (aad != null) {
            System.arraycopy(aad, 0, result, iv.length + 12, aadLength);
        }
        System.arraycopy(cipherData, 0, result, iv.length + 12 + aadLength, cipherData.length);
        return result;
    }

    private static byte[] gcmDecrypt(byte[] data, byte[] cipherKey) {
        byte[] paramLength = new byte[4];
        System.arraycopy(data, 0, paramLength, 0, 4);
        int ivLength = Utils.convertByte2Int(paramLength);
        byte[] iv = new byte[ivLength];
        System.arraycopy(data, 12, iv, 0, ivLength);
        System.arraycopy(data, 8, paramLength, 0, 4);
        int aadLength = Utils.convertByte2Int(paramLength);
        byte[] aad = aadLength == 0 ? null : new byte[aadLength];
        if (aad != null) {
            System.arraycopy(data, ivLength + 12, aad, 0, aadLength);
        }
        byte[] cipherData = new byte[(((data.length - ivLength) - aadLength) - 12)];
        System.arraycopy(data, ivLength + 12 + aadLength, cipherData, 0, cipherData.length);
        ArrayList<byte[]> params = new ArrayList<>(2);
        params.add(iv);
        params.add(aad);
        System.arraycopy(data, 4, paramLength, 0, 4);
        return doCipher(2, cipherData, cipherKey, Utils.convertByte2Int(paramLength), params);
    }

    private static byte[] doCipher(int opMode, byte[] data, byte[] cipherKey, int tagLength, ArrayList<byte[]> params) {
        if (params.size() < 2) {
            return new byte[0];
        }
        try {
            byte[] aad = params.get(1);
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_AND_PADDING);
            cipher.init(opMode, new SecretKeySpec(cipherKey, ALGORITHM), new GCMParameterSpec(tagLength * 8, params.get(0)));
            if (aad != null) {
                cipher.updateAAD(aad);
            }
            return cipher.doFinal(data);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            HwHiLog.e(TAG, false, "decrypt error", new Object[0]);
            return new byte[0];
        }
    }
}
