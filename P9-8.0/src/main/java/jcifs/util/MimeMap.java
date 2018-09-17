package jcifs.util;

import java.io.IOException;
import java.io.InputStream;

public class MimeMap {
    private static final int IN_SIZE = 7000;
    private static final int ST_COMM = 2;
    private static final int ST_EXT = 5;
    private static final int ST_GAP = 4;
    private static final int ST_START = 1;
    private static final int ST_TYPE = 3;
    private byte[] in = new byte[IN_SIZE];
    private int inLen;

    public MimeMap() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("jcifs/util/mime.map");
        this.inLen = 0;
        while (true) {
            int n = is.read(this.in, this.inLen, 7000 - this.inLen);
            if (n == -1) {
                break;
            }
            this.inLen += n;
        }
        if (this.inLen < 100 || this.inLen == IN_SIZE) {
            throw new IOException("Error reading jcifs/util/mime.map resource");
        }
        is.close();
    }

    public String getMimeType(String extension) throws IOException {
        return getMimeType(extension, "application/octet-stream");
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String getMimeType(String extension, String def) throws IOException {
        byte[] type = new byte[128];
        byte[] buf = new byte[16];
        byte[] ext = extension.toLowerCase().getBytes("ASCII");
        int state = 1;
        int x = 0;
        int t = 0;
        for (int off = 0; off < this.inLen; off++) {
            byte ch = this.in[off];
            switch (state) {
                case 1:
                    if (!(ch == (byte) 32 || ch == (byte) 9)) {
                        if (ch == (byte) 35) {
                            state = 2;
                            break;
                        }
                        state = 3;
                    }
                case 2:
                    if (ch != (byte) 10) {
                        break;
                    }
                    x = 0;
                    t = 0;
                    state = 1;
                    break;
                case 3:
                    if (ch != (byte) 32 && ch != (byte) 9) {
                        int t2 = t + 1;
                        type[t] = ch;
                        t = t2;
                        break;
                    }
                    state = 4;
                    break;
                case 4:
                    if (!(ch == (byte) 32 || ch == (byte) 9)) {
                        state = 5;
                    }
                case 5:
                    switch (ch) {
                        case (byte) 9:
                        case SmbConstants.DEFAULT_MAX_MPX_COUNT /*10*/:
                        case (byte) 32:
                        case (byte) 35:
                            int i = 0;
                            while (i < x && x == ext.length && buf[i] == ext[i]) {
                                i++;
                            }
                            if (i != ext.length) {
                                if (ch == (byte) 35) {
                                    state = 2;
                                } else if (ch == (byte) 10) {
                                    x = 0;
                                    t = 0;
                                    state = 1;
                                }
                                x = 0;
                                break;
                            }
                            return new String(type, 0, t, "ASCII");
                            break;
                        default:
                            int x2 = x + 1;
                            buf[x] = ch;
                            x = x2;
                            break;
                    }
                default:
                    break;
            }
        }
        return def;
    }
}
