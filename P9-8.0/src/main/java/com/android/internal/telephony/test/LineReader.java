package com.android.internal.telephony.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/* compiled from: ModelInterpreter */
class LineReader {
    static final int BUFFER_SIZE = 4096;
    byte[] mBuffer = new byte[4096];
    InputStream mInStream;

    LineReader(InputStream s) {
        this.mInStream = s;
    }

    String getNextLine() {
        return getNextLine(false);
    }

    String getNextLineCtrlZ() {
        return getNextLine(true);
    }

    String getNextLine(boolean ctrlZ) {
        int i;
        int i2 = 0;
        while (true) {
            try {
                i = i2;
                int result = this.mInStream.read();
                if (result >= 0) {
                    if (ctrlZ && result == 26) {
                        break;
                    } else if (result == 13 || result == 10) {
                        if (i != 0) {
                            break;
                        }
                        i2 = i;
                    } else {
                        i2 = i + 1;
                        try {
                            this.mBuffer[i] = (byte) result;
                        } catch (IOException e) {
                        } catch (IndexOutOfBoundsException e2) {
                        }
                    }
                } else {
                    return null;
                }
            } catch (IOException e3) {
                i2 = i;
            } catch (IndexOutOfBoundsException e4) {
                i2 = i;
            }
        }
        i2 = i;
        try {
            return new String(this.mBuffer, 0, i2, "US-ASCII");
        } catch (UnsupportedEncodingException e5) {
            System.err.println("ATChannel: implausable UnsupportedEncodingException");
            return null;
        }
        return null;
        System.err.println("ATChannel: buffer overflow");
        return new String(this.mBuffer, 0, i2, "US-ASCII");
    }
}
