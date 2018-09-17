package sun.security.util;

import java.util.Comparator;

public class ByteArrayTagOrder implements Comparator<byte[]> {
    public final int compare(byte[] bytes1, byte[] bytes2) {
        return (bytes1[0] | 32) - (bytes2[0] | 32);
    }
}
