package com.android.internal.telephony.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/* compiled from: ModelInterpreter */
class LineReader {
    static final int BUFFER_SIZE = 4096;
    byte[] mBuffer;
    InputStream mInStream;

    LineReader(InputStream s) {
        this.mBuffer = new byte[BUFFER_SIZE];
        this.mInStream = s;
    }

    String getNextLine() {
        return getNextLine(false);
    }

    String getNextLineCtrlZ() {
        return getNextLine(true);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    String getNextLine(boolean ctrlZ) {
        int i;
        int i2 = 0;
        while (true) {
            try {
                int result = this.mInStream.read();
                if (result >= 0) {
                    if (ctrlZ && result == 26) {
                        break;
                    }
                    if (result == 13 || result == 10) {
                        if (i2 != 0) {
                            break;
                        }
                        i = i2;
                    } else {
                        i = i2 + 1;
                        try {
                            this.mBuffer[i2] = (byte) result;
                        } catch (IOException e) {
                        } catch (IndexOutOfBoundsException e2) {
                        }
                    }
                    i2 = i;
                } else {
                    return null;
                }
            } catch (IOException e3) {
                i = i2;
            } catch (IndexOutOfBoundsException e4) {
                i = i2;
            }
        }
        i = i2;
        try {
            return new String(this.mBuffer, 0, i, "US-ASCII");
        } catch (UnsupportedEncodingException e5) {
            System.err.println("ATChannel: implausable UnsupportedEncodingException");
            return null;
        }
    }
}
