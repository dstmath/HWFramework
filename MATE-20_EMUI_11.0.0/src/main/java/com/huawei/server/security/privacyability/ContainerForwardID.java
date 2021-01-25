package com.huawei.server.security.privacyability;

import android.text.TextUtils;
import android.util.Log;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ContainerForwardID {
    private static final byte[] COMPONENT_ONE = {-13, -106, 115, 34, 118, 65, -120, -114, -69, 45, 39, -70, -61, 34, -2, -91};
    private static final String COMPONENT_PATH = "/data/system/certcompat.xml";
    private static final byte[] COMPONENT_TWO = {-84, 34, -48, -32, 116, 90, -30, -86, -47, -72, 70, -127, -6, -45, 72, 70};
    private static final int KEY_LENGTH = 16;
    private static final int SHIFT_LEFT = 4;
    private static final int SHIFT_RIGHT = 2;
    private static final String TAG = "ContainerForwardID";
    private static volatile ContainerForwardID sInstance = null;
    private byte[] mKey;

    private ContainerForwardID() {
        this.mKey = new byte[16];
        this.mKey = keyAssemble();
    }

    public static ContainerForwardID getInstance() {
        if (sInstance == null) {
            synchronized (ContainerForwardID.class) {
                if (sInstance == null) {
                    sInstance = new ContainerForwardID();
                }
            }
        }
        return sInstance;
    }

    /* access modifiers changed from: package-private */
    public synchronized String generateID(String containerID, String contentProviderTag) throws IllegalArgumentException {
        if (!TextUtils.isEmpty(containerID)) {
            if (!TextUtils.isEmpty(contentProviderTag)) {
                StringBuilder containerForwardID = new StringBuilder(contentProviderTag);
                try {
                    SecretKeySpec anonymizationKey = new SecretKeySpec(this.mKey, "HmacSHA256");
                    Mac mac = Mac.getInstance("HmacSHA256");
                    mac.init(anonymizationKey);
                    containerForwardID.append(encrypt(mac.doFinal(contentProviderTag.getBytes("UTF-8")), containerID + contentProviderTag));
                    return containerForwardID.toString();
                } catch (NoSuchAlgorithmException e) {
                    Log.e(TAG, "generateID NoSuchAlgorithmException:" + e.getMessage());
                    return null;
                } catch (InvalidKeyException e2) {
                    Log.e(TAG, "generateID InvalidKeyException:" + e2.getMessage());
                    return null;
                } catch (UnsupportedEncodingException e3) {
                    Log.e(TAG, "generateID UnsupportedEncodingException:" + e3.getMessage());
                    return null;
                } catch (IllegalArgumentException e4) {
                    throw new IllegalArgumentException();
                } catch (Exception e5) {
                    Log.e(TAG, "generateID Exception.");
                    return null;
                }
            }
        }
        return null;
    }

    private static String encrypt(byte[] hmac, String plainText) {
        if (hmac == null || plainText == null) {
            return null;
        }
        byte[] iv = subByte(hmac, 0);
        byte[] keyBytes = subByte(hmac, 16);
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        SecretKeySpec aesKey = new SecretKeySpec(keyBytes, "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(1, aesKey, zeroIv);
            return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "encrypt NoSuchAlgorithmException:" + e.getMessage());
            return null;
        } catch (NoSuchPaddingException e2) {
            Log.e(TAG, "encrypt NoSuchPaddingException:" + e2.getMessage());
            return null;
        } catch (InvalidKeyException e3) {
            Log.e(TAG, "encrypt InvalidKeyException:" + e3.getMessage());
            return null;
        } catch (InvalidAlgorithmParameterException e4) {
            Log.e(TAG, "encrypt InvalidAlgorithmParameterException:" + e4.getMessage());
            return null;
        } catch (IllegalBlockSizeException e5) {
            Log.e(TAG, "encrypt IllegalBlockSizeException:" + e5.getMessage());
            return null;
        } catch (BadPaddingException e6) {
            Log.e(TAG, "encrypt BadPaddingException:" + e6.getMessage());
            return null;
        } catch (UnsupportedEncodingException e7) {
            Log.e(TAG, "encrypt UnsupportedEncodingException:" + e7.getMessage());
            return null;
        } catch (Exception e8) {
            Log.e(TAG, "encrypt Exception.");
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0021, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0026, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0027, code lost:
        r6.addSuppressed(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002a, code lost:
        throw r7;
     */
    private static byte[] keyAssemble() {
        byte[] componentThree = new byte[16];
        try {
            FileInputStream inputStream = new FileInputStream(COMPONENT_PATH);
            if (inputStream.read(componentThree, 0, 16) < 16) {
                Log.e(TAG, "file is unusually small");
            }
            inputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "keyAssemble FileNotFoundException: File not found");
        } catch (Exception e2) {
            Log.e(TAG, "keyAssemble Exception.");
        }
        byte[] shiftByteLeft = new byte[16];
        byte[] shiftByteRight = new byte[16];
        for (int i = 0; i < 16; i++) {
            shiftByteLeft[i] = (byte) (COMPONENT_ONE[i] << 4);
        }
        byte[] xorByteResult = xorByte(shiftByteLeft, COMPONENT_TWO);
        for (int i2 = 0; i2 < 16; i2++) {
            shiftByteRight[i2] = (byte) (xorByteResult[i2] >> 2);
        }
        byte[] inputByte = xorByte(shiftByteRight, componentThree);
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(inputByte);
            return subByte(messageDigest.digest(), 0);
        } catch (NoSuchAlgorithmException e3) {
            Log.e(TAG, "keyAssemble NoSuchAlgorithmException:" + e3.getMessage());
            return null;
        } catch (Exception e4) {
            Log.e(TAG, "keyAssemble Exception.");
            return null;
        }
    }

    private static byte[] subByte(byte[] b, int offset) {
        byte[] sub = new byte[16];
        System.arraycopy(b, offset, sub, 0, 16);
        return sub;
    }

    private static byte[] xorByte(byte[] byteArrayOne, byte[] byteArrayTwo) {
        byte[] result = new byte[16];
        for (int i = 0; i < 16; i++) {
            result[i] = (byte) (byteArrayOne[i] ^ byteArrayTwo[i]);
        }
        return result;
    }
}
