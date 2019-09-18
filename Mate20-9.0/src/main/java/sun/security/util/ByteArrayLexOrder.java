package sun.security.util;

import java.util.Comparator;

public class ByteArrayLexOrder implements Comparator<byte[]> {
    public final int compare(byte[] bytes1, byte[] bytes2) {
        int i = 0;
        while (i < bytes1.length && i < bytes2.length) {
            int diff = (bytes1[i] & Character.DIRECTIONALITY_UNDEFINED) - (bytes2[i] & Character.DIRECTIONALITY_UNDEFINED);
            if (diff != 0) {
                return diff;
            }
            i++;
        }
        return bytes1.length - bytes2.length;
    }
}
