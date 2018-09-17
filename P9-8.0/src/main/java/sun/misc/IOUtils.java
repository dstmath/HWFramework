package sun.misc;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class IOUtils {
    public static byte[] readFully(InputStream is, int length, boolean readAll) throws IOException {
        byte[] output = new byte[0];
        if (length == -1) {
            length = Integer.MAX_VALUE;
        }
        int pos = 0;
        while (pos < length) {
            int bytesToRead;
            if (pos >= output.length) {
                bytesToRead = Math.min(length - pos, output.length + 1024);
                if (output.length < pos + bytesToRead) {
                    output = Arrays.copyOf(output, pos + bytesToRead);
                }
            } else {
                bytesToRead = output.length - pos;
            }
            int cc = is.read(output, pos, bytesToRead);
            if (cc >= 0) {
                pos += cc;
            } else if (readAll && length != Integer.MAX_VALUE) {
                throw new EOFException("Detect premature EOF");
            } else if (output.length != pos) {
                return Arrays.copyOf(output, pos);
            } else {
                return output;
            }
        }
        return output;
    }
}
