package com.android.server.backup.utils;

import android.util.Slog;
import com.android.server.backup.BackupManagerService;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtils {
    public static final String ENCRYPTION_ALGORITHM_NAME = "AES-256";
    public static final int PBKDF2_HASH_ROUNDS = 10000;
    private static final int PBKDF2_KEY_SIZE = 256;
    public static final int PBKDF2_SALT_SIZE = 512;

    public static SecretKey buildPasswordKey(String algorithm, String pw, byte[] salt, int rounds) {
        return buildCharArrayKey(algorithm, pw.toCharArray(), salt, rounds);
    }

    public static String buildPasswordHash(String algorithm, String pw, byte[] salt, int rounds) {
        SecretKey key = buildPasswordKey(algorithm, pw, salt, rounds);
        if (key != null) {
            return byteArrayToHex(key.getEncoded());
        }
        return null;
    }

    public static String byteArrayToHex(byte[] data) {
        StringBuilder buf = new StringBuilder(data.length * 2);
        for (byte b : data) {
            buf.append(Byte.toHexString(b, true));
        }
        return buf.toString();
    }

    public static byte[] hexToByteArray(String digits) {
        int bytes = digits.length() / 2;
        if (bytes * 2 == digits.length()) {
            byte[] result = new byte[bytes];
            for (int i = 0; i < digits.length(); i += 2) {
                result[i / 2] = (byte) Integer.parseInt(digits.substring(i, i + 2), 16);
            }
            return result;
        }
        throw new IllegalArgumentException("Hex string must have an even number of digits");
    }

    public static byte[] makeKeyChecksum(String algorithm, byte[] pwBytes, byte[] salt, int rounds) {
        char[] mkAsChar = new char[pwBytes.length];
        for (int i = 0; i < pwBytes.length; i++) {
            mkAsChar[i] = (char) pwBytes[i];
        }
        return buildCharArrayKey(algorithm, mkAsChar, salt, rounds).getEncoded();
    }

    private static SecretKey buildCharArrayKey(String algorithm, char[] pwArray, byte[] salt, int rounds) {
        try {
            return SecretKeyFactory.getInstance(algorithm).generateSecret(new PBEKeySpec(pwArray, salt, rounds, 256));
        } catch (InvalidKeySpecException e) {
            Slog.e(BackupManagerService.TAG, "Invalid key spec for PBKDF2!");
            return null;
        } catch (NoSuchAlgorithmException e2) {
            Slog.e(BackupManagerService.TAG, "PBKDF2 unavailable!");
            return null;
        }
    }
}
