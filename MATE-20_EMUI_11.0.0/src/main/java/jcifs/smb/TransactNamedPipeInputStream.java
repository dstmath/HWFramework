package jcifs.smb;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import jcifs.util.LogStream;

/* access modifiers changed from: package-private */
public class TransactNamedPipeInputStream extends SmbFileInputStream {
    private static final int INIT_PIPE_SIZE = 4096;
    private int beg_idx;
    private boolean dcePipe;
    Object lock;
    private int nxt_idx;
    private byte[] pipe_buf = new byte[4096];
    private int used;

    TransactNamedPipeInputStream(SmbNamedPipe pipe) throws SmbException, MalformedURLException, UnknownHostException {
        super(pipe, (pipe.pipeType & -65281) | 32);
        this.dcePipe = (pipe.pipeType & SmbNamedPipe.PIPE_TYPE_DCE_TRANSACT) != 1536;
        this.lock = new Object();
    }

    @Override // jcifs.smb.SmbFileInputStream, java.io.InputStream
    public int read() throws IOException {
        int result;
        synchronized (this.lock) {
            while (this.used == 0) {
                try {
                    this.lock.wait();
                } catch (InterruptedException ie) {
                    throw new IOException(ie.getMessage());
                }
            }
            result = this.pipe_buf[this.beg_idx] & 255;
            this.beg_idx = (this.beg_idx + 1) % this.pipe_buf.length;
        }
        return result;
    }

    @Override // jcifs.smb.SmbFileInputStream, java.io.InputStream
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override // jcifs.smb.SmbFileInputStream, java.io.InputStream
    public int read(byte[] b, int off, int len) throws IOException {
        int result;
        if (len <= 0) {
            return 0;
        }
        synchronized (this.lock) {
            while (this.used == 0) {
                try {
                    this.lock.wait();
                } catch (InterruptedException ie) {
                    throw new IOException(ie.getMessage());
                }
            }
            int i = this.pipe_buf.length - this.beg_idx;
            result = len > this.used ? this.used : len;
            if (this.used <= i || result <= i) {
                System.arraycopy(this.pipe_buf, this.beg_idx, b, off, result);
            } else {
                System.arraycopy(this.pipe_buf, this.beg_idx, b, off, i);
                System.arraycopy(this.pipe_buf, 0, b, off + i, result - i);
            }
            this.used -= result;
            this.beg_idx = (this.beg_idx + result) % this.pipe_buf.length;
        }
        return result;
    }

    @Override // jcifs.smb.SmbFileInputStream, java.io.InputStream
    public int available() throws IOException {
        SmbFile smbFile = this.file;
        LogStream logStream = SmbFile.log;
        if (LogStream.level < 3) {
            return 0;
        }
        SmbFile smbFile2 = this.file;
        SmbFile.log.println("Named Pipe available() does not apply to TRANSACT Named Pipes");
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int receive(byte[] b, int off, int len) {
        if (len > this.pipe_buf.length - this.used) {
            int new_size = this.pipe_buf.length * 2;
            if (len > new_size - this.used) {
                new_size = len + this.used;
            }
            byte[] tmp = this.pipe_buf;
            this.pipe_buf = new byte[new_size];
            int i = tmp.length - this.beg_idx;
            if (this.used > i) {
                System.arraycopy(tmp, this.beg_idx, this.pipe_buf, 0, i);
                System.arraycopy(tmp, 0, this.pipe_buf, i, this.used - i);
            } else {
                System.arraycopy(tmp, this.beg_idx, this.pipe_buf, 0, this.used);
            }
            this.beg_idx = 0;
            this.nxt_idx = this.used;
        }
        int i2 = this.pipe_buf.length - this.nxt_idx;
        if (len > i2) {
            System.arraycopy(b, off, this.pipe_buf, this.nxt_idx, i2);
            System.arraycopy(b, off + i2, this.pipe_buf, 0, len - i2);
        } else {
            System.arraycopy(b, off, this.pipe_buf, this.nxt_idx, len);
        }
        this.nxt_idx = (this.nxt_idx + len) % this.pipe_buf.length;
        this.used += len;
        return len;
    }

    public int dce_read(byte[] b, int off, int len) throws IOException {
        return super.read(b, off, len);
    }
}
