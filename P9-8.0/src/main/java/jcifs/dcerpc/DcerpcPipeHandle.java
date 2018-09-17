package jcifs.dcerpc;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import jcifs.dcerpc.msrpc.samr;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import jcifs.smb.SmbNamedPipe;
import jcifs.util.Encdec;

public class DcerpcPipeHandle extends DcerpcHandle {
    SmbFileInputStream in = null;
    boolean isStart = true;
    SmbFileOutputStream out = null;
    SmbNamedPipe pipe;

    public DcerpcPipeHandle(String url, NtlmPasswordAuthentication auth) throws UnknownHostException, MalformedURLException, DcerpcException {
        this.binding = DcerpcHandle.parseBinding(url);
        url = "smb://" + this.binding.server + "/IPC$/" + this.binding.endpoint.substring(6);
        String params = "";
        String server = (String) this.binding.getOption("server");
        if (server != null) {
            params = params + "&server=" + server;
        }
        String address = (String) this.binding.getOption("address");
        if (server != null) {
            params = params + "&address=" + address;
        }
        if (params.length() > 0) {
            url = url + "?" + params.substring(1);
        }
        this.pipe = new SmbNamedPipe(url, 27198979, auth);
    }

    protected void doSendFragment(byte[] buf, int off, int length, boolean isDirect) throws IOException {
        if (this.out == null || this.out.isOpen()) {
            if (this.in == null) {
                this.in = (SmbFileInputStream) this.pipe.getNamedPipeInputStream();
            }
            if (this.out == null) {
                this.out = (SmbFileOutputStream) this.pipe.getNamedPipeOutputStream();
            }
            if (isDirect) {
                this.out.writeDirect(buf, off, length, 1);
                return;
            } else {
                this.out.write(buf, off, length);
                return;
            }
        }
        throw new IOException("DCERPC pipe is no longer open");
    }

    protected void doReceiveFragment(byte[] buf, boolean isDirect) throws IOException {
        boolean z = true;
        if (buf.length < this.max_recv) {
            throw new IllegalArgumentException("buffer too small");
        }
        int off;
        if (!this.isStart || isDirect) {
            off = this.in.readDirect(buf, 0, buf.length);
        } else {
            off = this.in.read(buf, 0, samr.ACB_AUTOLOCK);
        }
        if (buf[0] == (byte) 5 || buf[1] == (byte) 0) {
            if (((buf[3] & 255) & 2) != 2) {
                z = false;
            }
            this.isStart = z;
            int length = Encdec.dec_uint16le(buf, 8);
            if (length > this.max_recv) {
                throw new IOException("Unexpected fragment length: " + length);
            }
            while (off < length) {
                off += this.in.readDirect(buf, off, length - off);
            }
            return;
        }
        throw new IOException("Unexpected DCERPC PDU header");
    }

    public void close() throws IOException {
        this.state = 0;
        if (this.out != null) {
            this.out.close();
        }
    }
}
