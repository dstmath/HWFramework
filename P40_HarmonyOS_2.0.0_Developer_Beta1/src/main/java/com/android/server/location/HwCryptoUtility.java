package com.android.server.location;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class HwCryptoUtility {
    private static final int SEED_SIZE = 16;
    private static final String TAG = "HwCryptoUtility";

    public static class AESLocalDbCrypto {
        public static String encrypt(String key, String plaintext) {
            try {
                SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                byte[] ivBytes = new byte[16];
                new SecureRandom().nextBytes(ivBytes);
                cipher.init(1, skeySpec, new IvParameterSpec(ivBytes));
                String ciphertextStr = Sha256Encrypt.bytes2Hex(cipher.doFinal(plaintext.getBytes("UTF-8")));
                String ivStr = Sha256Encrypt.bytes2Hex(ivBytes);
                return ivStr + ciphertextStr;
            } catch (Exception e) {
                LBSLog.e(HwCryptoUtility.TAG, false, "encrypt exception occured!", new Object[0]);
                return null;
            }
        }

        public static String decrypt(String key, String encrypted) {
            try {
                byte[] ivBytes = Sha256Encrypt.hex2Bytes(encrypted.substring(0, 32).toCharArray());
                SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(2, skeySpec, new IvParameterSpec(ivBytes));
                return new String(cipher.doFinal(Sha256Encrypt.hex2Bytes(encrypted.substring(32, encrypted.length()).toCharArray())));
            } catch (Exception e) {
                LBSLog.e(HwCryptoUtility.TAG, false, "decrypt exception occured!", new Object[0]);
                return null;
            }
        }
    }

    static class Sha256Encrypt {
        Sha256Encrypt() {
        }

        public static String bytes2Hex(byte[] byteArray) {
            StringBuffer des = new StringBuffer();
            for (byte singleByte : byteArray) {
                String tmp = Integer.toHexString(singleByte & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY);
                if (tmp.length() == 1) {
                    des.append("0");
                }
                des.append(tmp);
            }
            return des.toString();
        }

        /* JADX WARNING: Code restructure failed: missing block: B:5:0x000f, code lost:
            if ("".equals(r2) != false) goto L_0x0011;
         */
        public static String encrypt(String strSrc, String encName) {
            String encryptName = encName;
            byte[] bt = strSrc.getBytes();
            if (encryptName != null) {
                try {
                } catch (NoSuchAlgorithmException e) {
                    return null;
                }
            }
            encryptName = "SHA-256";
            MessageDigest md = MessageDigest.getInstance(encryptName);
            md.update(bt);
            return bytes2Hex(md.digest());
        }

        public static byte[] hex2Bytes(char[] data) {
            int len = data.length;
            byte[] out = new byte[(len >> 1)];
            int i = 0;
            int j = 0;
            while (j < len) {
                int j2 = j + 1;
                j = j2 + 1;
                out[i] = (byte) (((Character.digit(data[j], 16) << 4) | Character.digit(data[j2], 16)) & 255);
                i++;
            }
            return out;
        }
    }
}
