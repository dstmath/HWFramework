package tmsdkobf;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class mc {
    private static final char[] zI = "0123456789abcdef".toCharArray();

    public static byte[] bT(String str) {
        return l(str.getBytes());
    }

    public static String bU(String str) {
        byte[] bT = bT(str);
        if (bT == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder(bT.length * 2);
        byte[] bArr = bT;
        for (byte b : bT) {
            stringBuilder.append(Integer.toHexString(b & 255).substring(0, 1));
        }
        return stringBuilder.toString();
    }

    public static byte[] l(byte[] bArr) {
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");
            instance.update(bArr);
            return instance.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String m(byte[] -l_2_R) {
        StringBuilder stringBuilder = new StringBuilder(-l_2_R.length * 3);
        for (byte b : -l_2_R) {
            int i = b & 255;
            stringBuilder.append(zI[i >> 4]);
            stringBuilder.append(zI[i & 15]);
        }
        return stringBuilder.toString().toUpperCase();
    }

    public static String n(byte[] bArr) {
        return m(l(bArr));
    }
}
