package jcifs.smb;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import jcifs.dcerpc.msrpc.samr;
import jcifs.util.LogStream;
import jcifs.util.transport.TransportException;

public class SmbFileInputStream extends InputStream {
    private int access;
    SmbFile file;
    private long fp;
    private int openFlags;
    private int readSize;
    private byte[] tmp;

    public SmbFileInputStream(String url) throws SmbException, MalformedURLException, UnknownHostException {
        this(new SmbFile(url));
    }

    public SmbFileInputStream(SmbFile file2) throws SmbException, MalformedURLException, UnknownHostException {
        this(file2, 1);
    }

    SmbFileInputStream(SmbFile file2, int openFlags2) throws SmbException, MalformedURLException, UnknownHostException {
        this.tmp = new byte[1];
        this.file = file2;
        this.openFlags = openFlags2 & 65535;
        this.access = (openFlags2 >>> 16) & 65535;
        if (file2.type != 16) {
            file2.open(openFlags2, this.access, 128, 0);
            this.openFlags &= -81;
        } else {
            file2.connect0();
        }
        this.readSize = Math.min(file2.tree.session.transport.rcv_buf_size - 70, file2.tree.session.transport.server.maxBufferSize - 70);
    }

    /* access modifiers changed from: protected */
    public IOException seToIoe(SmbException se) {
        IOException ioe = se;
        Throwable root = se.getRootCause();
        if (root instanceof TransportException) {
            ioe = (TransportException) root;
            root = ((TransportException) ioe).getRootCause();
        }
        if (!(root instanceof InterruptedException)) {
            return ioe;
        }
        IOException ioe2 = new InterruptedIOException(root.getMessage());
        ioe2.initCause(root);
        return ioe2;
    }

    @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        try {
            this.file.close();
            this.tmp = null;
        } catch (SmbException se) {
            throw seToIoe(se);
        }
    }

    @Override // java.io.InputStream
    public int read() throws IOException {
        if (read(this.tmp, 0, 1) == -1) {
            return -1;
        }
        return this.tmp[0] & 255;
    }

    @Override // java.io.InputStream
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override // java.io.InputStream
    public int read(byte[] b, int off, int len) throws IOException {
        return readDirect(b, off, len);
    }

    public int readDirect(byte[] b, int off, int len) throws IOException {
        int r;
        int n;
        if (len <= 0) {
            return 0;
        }
        long start = this.fp;
        if (this.tmp == null) {
            throw new IOException("Bad file descriptor");
        }
        this.file.open(this.openFlags, this.access, 128, 0);
        SmbFile smbFile = this.file;
        LogStream logStream = SmbFile.log;
        if (LogStream.level >= 4) {
            SmbFile smbFile2 = this.file;
            SmbFile.log.println("read: fid=" + this.file.fid + ",off=" + off + ",len=" + len);
        }
        SmbComReadAndXResponse response = new SmbComReadAndXResponse(b, off);
        if (this.file.type == 16) {
            response.responseTimeout = 0;
        }
        do {
            if (len > this.readSize) {
                r = this.readSize;
            } else {
                r = len;
            }
            SmbFile smbFile3 = this.file;
            LogStream logStream2 = SmbFile.log;
            if (LogStream.level >= 4) {
                SmbFile smbFile4 = this.file;
                SmbFile.log.println("read: len=" + len + ",r=" + r + ",fp=" + this.fp);
            }
            try {
                SmbComReadAndX request = new SmbComReadAndX(this.file.fid, this.fp, r, null);
                if (this.file.type == 16) {
                    request.remaining = samr.ACB_AUTOLOCK;
                    request.maxCount = samr.ACB_AUTOLOCK;
                    request.minCount = samr.ACB_AUTOLOCK;
                }
                this.file.send(request, response);
                n = response.dataLength;
                if (n > 0) {
                    this.fp += (long) n;
                    len -= n;
                    response.off += n;
                    if (len <= 0) {
                        break;
                    }
                } else {
                    return (int) (this.fp - start > 0 ? this.fp - start : -1);
                }
            } catch (SmbException se) {
                if (this.file.type == 16 && se.getNtStatus() == -1073741493) {
                    return -1;
                }
                throw seToIoe(se);
            }
        } while (n == r);
        return (int) (this.fp - start);
    }

    @Override // java.io.InputStream
    public int available() throws IOException {
        if (this.file.type != 16) {
            return 0;
        }
        try {
            SmbNamedPipe pipe = (SmbNamedPipe) this.file;
            this.file.open(32, pipe.pipeType & 16711680, 128, 0);
            TransPeekNamedPipe req = new TransPeekNamedPipe(this.file.unc, this.file.fid);
            TransPeekNamedPipeResponse resp = new TransPeekNamedPipeResponse(pipe);
            pipe.send(req, resp);
            if (resp.status != 1 && resp.status != 4) {
                return resp.available;
            }
            this.file.opened = false;
            return 0;
        } catch (SmbException se) {
            throw seToIoe(se);
        }
    }

    @Override // java.io.InputStream
    public long skip(long n) throws IOException {
        if (n <= 0) {
            return 0;
        }
        this.fp += n;
        return n;
    }
}
