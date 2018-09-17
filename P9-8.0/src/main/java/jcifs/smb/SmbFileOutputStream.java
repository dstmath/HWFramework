package jcifs.smb;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import jcifs.util.LogStream;

public class SmbFileOutputStream extends OutputStream {
    private int access;
    private boolean append;
    private SmbFile file;
    private long fp;
    private int openFlags;
    private SmbComWrite req;
    private SmbComWriteAndX reqx;
    private SmbComWriteResponse rsp;
    private SmbComWriteAndXResponse rspx;
    private byte[] tmp;
    private boolean useNTSmbs;
    private int writeSize;

    public SmbFileOutputStream(String url) throws SmbException, MalformedURLException, UnknownHostException {
        this(url, false);
    }

    public SmbFileOutputStream(SmbFile file) throws SmbException, MalformedURLException, UnknownHostException {
        this(file, false);
    }

    public SmbFileOutputStream(String url, boolean append) throws SmbException, MalformedURLException, UnknownHostException {
        this(new SmbFile(url), append);
    }

    public SmbFileOutputStream(SmbFile file, boolean append) throws SmbException, MalformedURLException, UnknownHostException {
        this(file, append, append ? 22 : 82);
    }

    public SmbFileOutputStream(String url, int shareAccess) throws SmbException, MalformedURLException, UnknownHostException {
        this(new SmbFile(url, "", null, shareAccess), false);
    }

    SmbFileOutputStream(SmbFile file, boolean append, int openFlags) throws SmbException, MalformedURLException, UnknownHostException {
        this.tmp = new byte[1];
        this.file = file;
        this.append = append;
        this.openFlags = openFlags;
        this.access = (openFlags >>> 16) & 65535;
        if (append) {
            try {
                this.fp = file.length();
            } catch (SmbAuthException sae) {
                throw sae;
            } catch (SmbException e) {
                this.fp = 0;
            }
        }
        if ((file instanceof SmbNamedPipe) && file.unc.startsWith("\\pipe\\")) {
            file.unc = file.unc.substring(5);
            file.send(new TransWaitNamedPipe("\\pipe" + file.unc), new TransWaitNamedPipeResponse());
        }
        file.open(openFlags, this.access | 2, 128, 0);
        this.openFlags &= -81;
        this.writeSize = file.tree.session.transport.snd_buf_size - 70;
        this.useNTSmbs = file.tree.session.transport.hasCapability(16);
        if (this.useNTSmbs) {
            this.reqx = new SmbComWriteAndX();
            this.rspx = new SmbComWriteAndXResponse();
            return;
        }
        this.req = new SmbComWrite();
        this.rsp = new SmbComWriteResponse();
    }

    public void close() throws IOException {
        this.file.close();
        this.tmp = null;
    }

    public void write(int b) throws IOException {
        this.tmp[0] = (byte) b;
        write(this.tmp, 0, 1);
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public boolean isOpen() {
        return this.file.isOpen();
    }

    void ensureOpen() throws IOException {
        if (!this.file.isOpen()) {
            this.file.open(this.openFlags, this.access | 2, 128, 0);
            if (this.append) {
                this.fp = this.file.length();
            }
        }
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (!this.file.isOpen() && (this.file instanceof SmbNamedPipe)) {
            this.file.send(new TransWaitNamedPipe("\\pipe" + this.file.unc), new TransWaitNamedPipeResponse());
        }
        writeDirect(b, off, len, 0);
    }

    public void writeDirect(byte[] b, int off, int len, int flags) throws IOException {
        if (len > 0) {
            if (this.tmp == null) {
                throw new IOException("Bad file descriptor");
            }
            ensureOpen();
            SmbFile smbFile = this.file;
            LogStream logStream = SmbFile.log;
            if (LogStream.level >= 4) {
                smbFile = this.file;
                SmbFile.log.println("write: fid=" + this.file.fid + ",off=" + off + ",len=" + len);
            }
            do {
                int w;
                if (len > this.writeSize) {
                    w = this.writeSize;
                } else {
                    w = len;
                }
                if (this.useNTSmbs) {
                    this.reqx.setParam(this.file.fid, this.fp, len - w, b, off, w);
                    if ((flags & 1) != 0) {
                        this.reqx.setParam(this.file.fid, this.fp, len, b, off, w);
                        this.reqx.writeMode = 8;
                    } else {
                        this.reqx.writeMode = 0;
                    }
                    this.file.send(this.reqx, this.rspx);
                    this.fp += this.rspx.count;
                    len = (int) (((long) len) - this.rspx.count);
                    off = (int) (((long) off) + this.rspx.count);
                    continue;
                } else {
                    this.req.setParam(this.file.fid, this.fp, len - w, b, off, w);
                    this.fp += this.rsp.count;
                    len = (int) (((long) len) - this.rsp.count);
                    off = (int) (((long) off) + this.rsp.count);
                    this.file.send(this.req, this.rsp);
                    continue;
                }
            } while (len > 0);
        }
    }
}
