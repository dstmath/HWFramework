package android.encrypt;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public final class AES128_ECB_HEX {
    static final byte[] C2 = new byte[]{(byte) 87, (byte) 14, (byte) 36, (byte) -66, (byte) 75, (byte) 48, (byte) 43, (byte) 91, (byte) 32, (byte) 62, (byte) 122, (byte) 55, (byte) -69, (byte) -112, (byte) -81, (byte) 60, (byte) -90, (byte) -123, (byte) -60, (byte) 113, (byte) 25, (byte) 101, (byte) -101, (byte) 0, (byte) -27, (byte) -106, (byte) 105, (byte) 110, (byte) 123, (byte) -4, (byte) -121, (byte) 47, (byte) 77, (byte) -101, (byte) -8, (byte) 54, (byte) -51, (byte) 75, (byte) 73, (byte) 61, (byte) 29, (byte) -113, (byte) 39, (byte) -62, (byte) 50, (byte) 37, (byte) -99};

    public static byte[] decode(String stHex, byte[] btKey, int iKeyLen) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        return AES128_ECB.decode(HEX.decode(stHex), 0, btKey, iKeyLen);
    }

    private AES128_ECB_HEX() {
    }
}
