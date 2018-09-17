package android.encrypt;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public final class AES128_ECB {
    private static final int AES_128_KEY_LEN = 16;
    static final byte[] C1 = new byte[]{(byte) 43, (byte) 32, (byte) 13, (byte) 59, (byte) -121, (byte) 84, (byte) 92, (byte) -3, (byte) -44, (byte) -3, (byte) -42, (byte) -84, (byte) 120, (byte) -123, (byte) -13, (byte) 104, (byte) -67, (byte) -108, (byte) 101, (byte) 56, (byte) 106, (byte) 52, (byte) -32, (byte) -78, (byte) 101, (byte) 16, (byte) 60, (byte) -41, (byte) -92, (byte) 103, (byte) -6, (byte) -31, (byte) 48, (byte) -44, (byte) -114, (byte) 89, (byte) 10, (byte) 53, (byte) 4, Byte.MAX_VALUE, (byte) -51, (byte) 60, (byte) -72, (byte) -125, (byte) -41, (byte) -71, (byte) 39};

    public static byte[] decode(byte[] btCipher, int iLen, byte[] btKey, int iKeyLen) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        return encode_decode(btCipher, iLen, btKey, iKeyLen, 1);
    }

    private static byte[] encode_decode(byte[] btData, int iLen, byte[] btKey, int iKeyLen, int iFlag) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        if (btData == null || btKey == null) {
            return null;
        }
        int ii;
        int l_iMode;
        if (iLen <= 0 || iLen > btData.length) {
            iLen = btData.length;
        }
        if (iKeyLen <= 0 || iKeyLen > btKey.length) {
            iKeyLen = btKey.length;
        }
        if (iKeyLen > 16) {
            iKeyLen = 16;
        }
        byte[] l_btKey = new byte[16];
        for (ii = 0; ii < 16; ii++) {
            l_btKey[ii] = (byte) 0;
        }
        for (ii = 0; ii < iKeyLen; ii++) {
            l_btKey[ii] = btKey[ii];
        }
        Cipher l_oCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        if (iFlag == 0) {
            l_iMode = 1;
        } else {
            l_iMode = 2;
        }
        l_oCipher.init(l_iMode, new SecretKeySpec(l_btKey, 0, 16, "AES"));
        return l_oCipher.doFinal(btData, 0, iLen);
    }

    private AES128_ECB() {
    }
}
