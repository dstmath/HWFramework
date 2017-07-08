package sun.security.ssl;

import java.io.IOException;
import java.io.OutputStream;
import sun.security.util.DerValue;

class AppOutputStream extends OutputStream {
    private SSLSocketImpl c;
    private final byte[] oneByte;
    OutputRecord r;

    AppOutputStream(SSLSocketImpl conn) {
        this.oneByte = new byte[1];
        this.r = new OutputRecord(DerValue.tag_UtcTime);
        this.c = conn;
    }

    public synchronized void write(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len != 0) {
            this.c.checkWrite();
            boolean isFirstRecordOfThePayload = true;
            do {
                int howmuch;
                boolean holdRecord = false;
                if (isFirstRecordOfThePayload) {
                    try {
                        if (this.c.needToSplitPayload()) {
                            howmuch = Math.min(1, this.r.availableDataBytes());
                            if (len != 1 && howmuch == 1) {
                                holdRecord = true;
                            }
                            if (isFirstRecordOfThePayload && howmuch != 0) {
                                isFirstRecordOfThePayload = false;
                            }
                            if (howmuch > 0) {
                                this.r.write(b, off, howmuch);
                                off += howmuch;
                                len -= howmuch;
                            }
                            this.c.writeRecord(this.r, holdRecord);
                            this.c.checkWrite();
                        }
                    } catch (Exception e) {
                        this.c.handleException(e);
                    }
                }
                howmuch = Math.min(len, this.r.availableDataBytes());
                isFirstRecordOfThePayload = false;
                if (howmuch > 0) {
                    this.r.write(b, off, howmuch);
                    off += howmuch;
                    len -= howmuch;
                }
                this.c.writeRecord(this.r, holdRecord);
                this.c.checkWrite();
            } while (len > 0);
            return;
        } else {
            return;
        }
    }

    public synchronized void write(int i) throws IOException {
        this.oneByte[0] = (byte) i;
        write(this.oneByte, 0, 1);
    }

    public void close() throws IOException {
        this.c.close();
    }
}
