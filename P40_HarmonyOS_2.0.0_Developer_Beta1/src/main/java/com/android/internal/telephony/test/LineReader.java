package com.android.internal.telephony.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/* compiled from: ModelInterpreter */
class LineReader {
    static final int BUFFER_SIZE = 4096;
    byte[] mBuffer = new byte[BUFFER_SIZE];
    InputStream mInStream;

    LineReader(InputStream s) {
        this.mInStream = s;
    }

    /* access modifiers changed from: package-private */
    public String getNextLine() {
        return getNextLine(false);
    }

    /* access modifiers changed from: package-private */
    public String getNextLineCtrlZ() {
        return getNextLine(true);
    }

    /* access modifiers changed from: package-private */
    public String getNextLine(boolean ctrlZ) {
        int i = 0;
        while (true) {
            try {
                int result = this.mInStream.read();
                if (result >= 0) {
                    if (ctrlZ && result == 26) {
                        break;
                    }
                    if (result != 13) {
                        if (result != 10) {
                            int i2 = i + 1;
                            try {
                                this.mBuffer[i] = (byte) result;
                                i = i2;
                            } catch (IOException e) {
                                return null;
                            } catch (IndexOutOfBoundsException e2) {
                                i = i2;
                                System.err.println("ATChannel: buffer overflow");
                                return new String(this.mBuffer, 0, i, "US-ASCII");
                            }
                        }
                    }
                    if (i == 0) {
                    }
                } else {
                    return null;
                }
            } catch (IOException e3) {
                return null;
            } catch (IndexOutOfBoundsException e4) {
                System.err.println("ATChannel: buffer overflow");
                return new String(this.mBuffer, 0, i, "US-ASCII");
            }
        }
        try {
            return new String(this.mBuffer, 0, i, "US-ASCII");
        } catch (UnsupportedEncodingException e5) {
            System.err.println("ATChannel: implausable UnsupportedEncodingException");
            return null;
        }
    }
}
