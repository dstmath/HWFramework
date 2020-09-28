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
    static final byte[] C1 = {43, 32, 13, 59, -121, 84, 92, -3, -44, -3, -42, -84, 120, -123, -13, 104, -67, -108, 101, 56, 106, 52, -32, -78, 101, 16, 60, -41, -92, 103, -6, -31, 48, -44, -114, 89, 10, 53, 4, Byte.MAX_VALUE, -51, 60, -72, -125, -41, -71, 39};

    public static byte[] decode(byte[] btCipher, int iLen, byte[] btKey, int iKeyLen) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        return encode_decode(btCipher, iLen, btKey, iKeyLen, 1);
    }

    private static byte[] encode_decode(byte[] btData, int iLen, byte[] btKey, int iKeyLen, int iFlag) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        int l_iMode;
        if (btData == null || btKey == null) {
            return null;
        }
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
        for (int ii = 0; ii < 16; ii++) {
            l_btKey[ii] = 0;
        }
        for (int ii2 = 0; ii2 < iKeyLen; ii2++) {
            l_btKey[ii2] = btKey[ii2];
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
