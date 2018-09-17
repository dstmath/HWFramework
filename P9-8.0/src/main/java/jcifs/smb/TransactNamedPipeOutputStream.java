package jcifs.smb;

import java.io.IOException;
import jcifs.dcerpc.msrpc.samr;

class TransactNamedPipeOutputStream extends SmbFileOutputStream {
    private boolean dcePipe;
    private String path;
    private SmbNamedPipe pipe;
    private byte[] tmp = new byte[1];

    TransactNamedPipeOutputStream(SmbNamedPipe pipe) throws IOException {
        boolean z = true;
        super(pipe, false, (pipe.pipeType & -65281) | 32);
        this.pipe = pipe;
        if ((pipe.pipeType & SmbNamedPipe.PIPE_TYPE_DCE_TRANSACT) != SmbNamedPipe.PIPE_TYPE_DCE_TRANSACT) {
            z = false;
        }
        this.dcePipe = z;
        this.path = pipe.unc;
    }

    public void close() throws IOException {
        this.pipe.close();
    }

    public void write(int b) throws IOException {
        this.tmp[0] = (byte) b;
        write(this.tmp, 0, 1);
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (len < 0) {
            len = 0;
        }
        if ((this.pipe.pipeType & 256) == 256) {
            this.pipe.send(new TransWaitNamedPipe(this.path), new TransWaitNamedPipeResponse());
            this.pipe.send(new TransCallNamedPipe(this.path, b, off, len), new TransCallNamedPipeResponse(this.pipe));
        } else if ((this.pipe.pipeType & 512) == 512) {
            ensureOpen();
            TransTransactNamedPipe req = new TransTransactNamedPipe(this.pipe.fid, b, off, len);
            if (this.dcePipe) {
                req.maxDataCount = samr.ACB_AUTOLOCK;
            }
            this.pipe.send(req, new TransTransactNamedPipeResponse(this.pipe));
        }
    }
}
