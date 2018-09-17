package jcifs.util;

import java.io.IOException;
import java.io.InputStream;
import jcifs.smb.SmbFile;

public class MimeMap {
    private static final int IN_SIZE = 7000;
    private static final int ST_COMM = 2;
    private static final int ST_EXT = 5;
    private static final int ST_GAP = 4;
    private static final int ST_START = 1;
    private static final int ST_TYPE = 3;
    private byte[] in;
    private int inLen;

    public MimeMap() throws IOException {
        this.in = new byte[IN_SIZE];
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String getMimeType(String extension, String def) throws IOException {
        byte[] type = new byte[SmbConstants.FLAGS_RESPONSE];
        byte[] buf = new byte[16];
        byte[] ext = extension.toLowerCase().getBytes("ASCII");
        int state = ST_START;
        int x = 0;
        int t = 0;
        for (int off = 0; off < this.inLen; off += ST_START) {
            byte ch = this.in[off];
            switch (state) {
                case ST_START /*1*/:
                    if (!(ch == 32 || ch == 9)) {
                        if (ch == 35) {
                            state = ST_COMM;
                            break;
                        }
                        state = ST_TYPE;
                    }
                case ST_COMM /*2*/:
                    if (ch != 10) {
                        break;
                    }
                    x = 0;
                    t = 0;
                    state = ST_START;
                    break;
                case ST_TYPE /*3*/:
                    if (ch != 32 && ch != 9) {
                        int t2 = t + ST_START;
                        type[t] = ch;
                        t = t2;
                        break;
                    }
                    state = ST_GAP;
                    break;
                case ST_GAP /*4*/:
                    if (!(ch == 32 || ch == 9)) {
                        state = ST_EXT;
                    }
                case ST_EXT /*5*/:
                    switch (ch) {
                        case SmbConstants.FLAGS_OFFSET /*9*/:
                        case SmbConstants.DEFAULT_MAX_MPX_COUNT /*10*/:
                        case SmbFile.TYPE_PRINTER /*32*/:
                        case (byte) 35:
                            int i = 0;
                            while (i < x && x == ext.length && buf[i] == ext[i]) {
                                i += ST_START;
                            }
                            if (i != ext.length) {
                                if (ch == 35) {
                                    state = ST_COMM;
                                } else if (ch == 10) {
                                    x = 0;
                                    t = 0;
                                    state = ST_START;
                                }
                                x = 0;
                                break;
                            }
                            return new String(type, 0, t, "ASCII");
                            break;
                        default:
                            int x2 = x + ST_START;
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
