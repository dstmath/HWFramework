package android.encrypt;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class PasswordEncrypter {
    private static final byte[] C3 = new byte[]{(byte) 43, (byte) 66, (byte) -6, (byte) -44, (byte) -78, (byte) 76, (byte) 59, (byte) 62, (byte) 71, (byte) 52, (byte) 28, (byte) -9, (byte) -64, (byte) -31, (byte) 66, (byte) 57, (byte) 50, (byte) -19, (byte) 34, (byte) -64, (byte) 91, (byte) 91, (byte) 120, (byte) 51, (byte) 74, (byte) 118, (byte) 33, (byte) -7, (byte) 71, (byte) -81, (byte) 110, (byte) -85, (byte) -11, (byte) -91, (byte) 73, (byte) 28, (byte) -14, (byte) -4, (byte) 30, (byte) 108, (byte) -119, (byte) -85, (byte) 125, (byte) 48, (byte) -87, (byte) 24, (byte) -29};
    private static final String MASTER_PASSWORD = getKey(AES128_ECB.C1, AES128_ECB_HEX.C2, C3);

    public static String getKey(byte[] c1, byte[] c2, byte[] c3) {
        return new String(right(XOR(c1, left(XOR(c3, left(c2, 2)), 6)), 4), Charset.defaultCharset());
    }

    private static byte[] right(byte[] source, int count) {
        byte[] temp = (byte[]) source.clone();
        for (int i = 0; i < count; i++) {
            byte m = temp[temp.length - 1];
            for (int j = temp.length - 1; j > 0; j--) {
                temp[j] = temp[j - 1];
            }
            temp[0] = m;
        }
        return temp;
    }

    private static byte[] left(byte[] source, int count) {
        byte[] temp = (byte[]) source.clone();
        for (int i = 0; i < count; i++) {
            byte m = temp[0];
            for (int j = 0; j < temp.length - 1; j++) {
                temp[j] = temp[j + 1];
            }
            temp[temp.length - 1] = m;
        }
        return temp;
    }

    private static byte[] XOR(byte[] m, byte[] n) {
        byte[] temp = new byte[m.length];
        for (int i = 0; i < m.length; i++) {
            temp[i] = (byte) (m[i] ^ n[i]);
        }
        return temp;
    }

    public static String decrypter(String password) {
        String string = password;
        try {
            return new String(AES128_ECB_HEX.decode(password, HwPasswordUtil.getSecretKey(MASTER_PASSWORD).getBytes(), 0));
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return string;
        } catch (BadPaddingException e2) {
            e2.printStackTrace();
            return string;
        } catch (IllegalBlockSizeException e3) {
            e3.printStackTrace();
            return string;
        } catch (NoSuchAlgorithmException e4) {
            e4.printStackTrace();
            return string;
        } catch (NoSuchPaddingException e5) {
            e5.printStackTrace();
            return string;
        } catch (Exception e6) {
            e6.printStackTrace();
            return string;
        }
    }
}
