package android.encrypt;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public final class AES128_ECB_HEX {
    static final byte[] C2 = {87, 14, 36, -66, 75, 48, 43, 91, 32, 62, 122, 55, -69, -112, -81, 60, -90, -123, -60, 113, 25, 101, -101, 0, -27, -106, 105, 110, 123, -4, -121, 47, 77, -101, -8, 54, -51, 75, 73, 61, 29, -113, 39, -62, 50, 37, -99};

    public static byte[] decode(String stHex, byte[] btKey, int iKeyLen) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        return AES128_ECB.decode(HEX.decode(stHex), 0, btKey, iKeyLen);
    }

    private AES128_ECB_HEX() {
    }
}
