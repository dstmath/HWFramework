package com.android.server.location;

import com.android.server.PPPOEStateMachine;
import com.android.server.display.Utils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;

public class HwCryptoUtility {

    static class AESLocalDbCrypto {
        AESLocalDbCrypto() {
        }

        public static String encrypt(String key, String plaintext) throws Exception {
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] ivBytes = new byte[16];
            new SecureRandom().nextBytes(ivBytes);
            cipher.init(1, skeySpec, new IvParameterSpec(ivBytes));
            return new String(Hex.encodeHex(ivBytes)) + new String(Hex.encodeHex(cipher.doFinal(plaintext.getBytes("UTF-8"))));
        }

        public static String decrypt(String key, String encrypted) throws Exception {
            byte[] ivBytes = Hex.decodeHex(encrypted.substring(0, 32).toCharArray());
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(2, skeySpec, new IvParameterSpec(ivBytes));
            return new String(cipher.doFinal(Hex.decodeHex(encrypted.substring(32, encrypted.length()).toCharArray())));
        }
    }

    static class Sha256Encrypt {
        Sha256Encrypt() {
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public static String Encrypt(String strSrc, String encName) {
            byte[] bt = strSrc.getBytes();
            if (encName != null) {
                try {
                } catch (NoSuchAlgorithmException e) {
                    return null;
                }
            }
            encName = "SHA-256";
            MessageDigest md = MessageDigest.getInstance(encName);
            md.update(bt);
            return bytes2Hex(md.digest());
        }

        public static String bytes2Hex(byte[] bts) {
            StringBuffer des = new StringBuffer();
            for (byte b : bts) {
                String tmp = Integer.toHexString(b & Utils.MAXINUM_TEMPERATURE);
                if (tmp.length() == 1) {
                    des.append(PPPOEStateMachine.PHASE_DEAD);
                }
                des.append(tmp);
            }
            return des.toString();
        }
    }
}
