package jcifs.smb;

import java.io.IOException;
import jcifs.dcerpc.msrpc.samr;

class TransactNamedPipeOutputStream extends SmbFileOutputStream {
    private boolean dcePipe;
    private String path;
    private SmbNamedPipe pipe;
    private byte[] tmp = new byte[1];

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    TransactNamedPipeOutputStream(SmbNamedPipe pipe2) throws IOException {
        super(pipe2, false, (pipe2.pipeType & -65281) | 32);
        boolean z = true;
        this.pipe = pipe2;
        this.dcePipe = (pipe2.pipeType & SmbNamedPipe.PIPE_TYPE_DCE_TRANSACT) != 1536 ? false : z;
        this.path = pipe2.unc;
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
