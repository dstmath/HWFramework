package jcifs.util;

import java.io.IOException;
import java.io.InputStream;
import jcifs.smb.SmbConstants;

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

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0059  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x005f  */
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
                    if (!(ch == 32 || ch == 9)) {
                        if (ch == 35) {
                            state = 2;
                            break;
                        } else {
                            state = 3;
                            if (ch != 32 || ch == 9) {
                                state = 4;
                                break;
                            } else {
                                type[t] = ch;
                                t++;
                                break;
                            }
                        }
                    }
                    break;
                case 2:
                    if (ch == 10) {
                        x = 0;
                        t = 0;
                        state = 1;
                        break;
                    } else {
                        break;
                    }
                case 3:
                    if (ch != 32) {
                        break;
                    }
                    state = 4;
                    break;
                case 4:
                    if (!(ch == 32 || ch == 9)) {
                        state = 5;
                        switch (ch) {
                            case 9:
                            case SmbConstants.DEFAULT_MAX_MPX_COUNT /* 10 */:
                            case 32:
                            case 35:
                                int i = 0;
                                while (i < x && x == ext.length && buf[i] == ext[i]) {
                                    i++;
                                }
                                if (i == ext.length) {
                                    return new String(type, 0, t, "ASCII");
                                }
                                if (ch == 35) {
                                    state = 2;
                                } else if (ch == 10) {
                                    t = 0;
                                    state = 1;
                                }
                                x = 0;
                                continue;
                                continue;
                            default:
                                buf[x] = ch;
                                x++;
                                continue;
                                continue;
                        }
                    }
                    break;
                case 5:
                    switch (ch) {
                        case 9:
                        case SmbConstants.DEFAULT_MAX_MPX_COUNT /* 10 */:
                        case 32:
                        case 35:
                            break;
                    }
            }
        }
        return def;
    }
}
