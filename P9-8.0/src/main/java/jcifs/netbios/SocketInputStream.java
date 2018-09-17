package jcifs.netbios;

import java.io.IOException;
import java.io.InputStream;

class SocketInputStream extends InputStream {
    private static final int TMP_BUFFER_SIZE = 256;
    private int bip;
    private byte[] header = new byte[4];
    private InputStream in;
    private int n;
    private SessionServicePacket ssp;
    private byte[] tmp = new byte[256];
    private int tot;

    SocketInputStream(InputStream in) {
        this.in = in;
    }

    public synchronized int read() throws IOException {
        int i;
        if (read(this.tmp, 0, 1) < 0) {
            i = -1;
        } else {
            i = this.tmp[0] & 255;
        }
        return i;
    }

    public synchronized int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public synchronized int read(byte[] b, int off, int len) throws IOException {
        int i = -1;
        synchronized (this) {
            if (len != 0) {
                this.tot = 0;
                while (true) {
                    if (this.bip <= 0) {
                        switch (SessionServicePacket.readPacketType(this.in, this.header, 0)) {
                            case NbtException.CONNECTION_REFUSED /*-1*/:
                                if (this.tot > 0) {
                                    i = this.tot;
                                    break;
                                }
                                break;
                            case 0:
                                this.bip = SessionServicePacket.readLength(this.header, 0);
                                break;
                            case 133:
                                break;
                            default:
                                break;
                        }
                    }
                    this.n = this.in.read(b, off, Math.min(len, this.bip));
                    if (this.n != -1) {
                        this.tot += this.n;
                        off += this.n;
                        len -= this.n;
                        this.bip -= this.n;
                        if (len == 0) {
                            i = this.tot;
                        }
                    } else if (this.tot > 0) {
                        i = this.tot;
                    }
                }
            } else {
                i = 0;
            }
        }
        return i;
    }

    public synchronized long skip(long numbytes) throws IOException {
        long j = 0;
        synchronized (this) {
            if (numbytes > 0) {
                long n = numbytes;
                while (n > 0) {
                    int r = read(this.tmp, 0, (int) Math.min(256, n));
                    if (r < 0) {
                        break;
                    }
                    n -= (long) r;
                }
                j = numbytes - n;
            }
        }
        return j;
    }

    public int available() throws IOException {
        if (this.bip > 0) {
            return this.bip;
        }
        return this.in.available();
    }

    public void close() throws IOException {
        this.in.close();
    }
}
