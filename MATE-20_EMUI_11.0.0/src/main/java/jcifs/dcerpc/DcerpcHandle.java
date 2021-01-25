package jcifs.dcerpc;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.security.Principal;
import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.smb.BufferCache;
import jcifs.smb.NtlmPasswordAuthentication;

public abstract class DcerpcHandle implements DcerpcConstants {
    private static int call_id = 1;
    protected DcerpcBinding binding;
    protected int max_recv = this.max_xmit;
    protected int max_xmit = 4280;
    protected DcerpcSecurityProvider securityProvider = null;
    protected int state = 0;

    public abstract void close() throws IOException;

    /* access modifiers changed from: protected */
    public abstract void doReceiveFragment(byte[] bArr, boolean z) throws IOException;

    /* access modifiers changed from: protected */
    public abstract void doSendFragment(byte[] bArr, int i, int i2, boolean z) throws IOException;

    protected static DcerpcBinding parseBinding(String str) throws DcerpcException {
        char[] arr = str.toCharArray();
        String proto = null;
        String key = null;
        DcerpcBinding binding2 = null;
        int si = 0;
        int mark = 0;
        int state2 = 0;
        do {
            char ch = arr[si];
            switch (state2) {
                case 0:
                    if (ch == ':') {
                        proto = str.substring(mark, si);
                        mark = si + 1;
                        state2 = 1;
                        break;
                    }
                    break;
                case 1:
                    if (ch == '\\') {
                        mark = si + 1;
                        break;
                    } else {
                        state2 = 2;
                    }
                case 2:
                    if (ch == '[') {
                        if (str.substring(mark, si).trim().length() == 0) {
                        }
                        binding2 = new DcerpcBinding(proto, str.substring(mark, si));
                        mark = si + 1;
                        state2 = 5;
                        break;
                    }
                    break;
                case 3:
                case 4:
                default:
                    si = arr.length;
                    break;
                case 5:
                    if (ch != '=') {
                        if (ch == ',' || ch == ']') {
                            String val = str.substring(mark, si).trim();
                            if (key == null) {
                                key = "endpoint";
                            }
                            binding2.setOption(key, val);
                            key = null;
                            break;
                        }
                    } else {
                        key = str.substring(mark, si).trim();
                        mark = si + 1;
                        break;
                    }
            }
            si++;
        } while (si < arr.length);
        if (binding2 != null && binding2.endpoint != null) {
            return binding2;
        }
        throw new DcerpcException("Invalid binding URL: " + str);
    }

    public static DcerpcHandle getHandle(String url, NtlmPasswordAuthentication auth) throws UnknownHostException, MalformedURLException, DcerpcException {
        if (url.startsWith("ncacn_np:")) {
            return new DcerpcPipeHandle(url, auth);
        }
        throw new DcerpcException("DCERPC transport not supported: " + url);
    }

    public void bind() throws DcerpcException, IOException {
        synchronized (this) {
            try {
                this.state = 1;
                sendrecv(new DcerpcBind(this.binding, this));
            } catch (IOException ioe) {
                this.state = 0;
                throw ioe;
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public void sendrecv(DcerpcMessage msg) throws DcerpcException, IOException {
        if (this.state == 0) {
            bind();
        }
        boolean isDirect = true;
        byte[] stub = BufferCache.getBuffer();
        try {
            NdrBuffer buf = new NdrBuffer(stub, 0);
            msg.flags = 3;
            int i = call_id;
            call_id = i + 1;
            msg.call_id = i;
            msg.encode(buf);
            if (this.securityProvider != null) {
                buf.setIndex(0);
                this.securityProvider.wrap(buf);
            }
            int tot = buf.getLength() - 24;
            int off = 0;
            while (off < tot) {
                int n = tot - off;
                if (n + 24 > this.max_xmit) {
                    msg.flags &= -3;
                    n = this.max_xmit - 24;
                } else {
                    msg.flags |= 2;
                    isDirect = false;
                    msg.alloc_hint = n;
                }
                msg.length = n + 24;
                if (off > 0) {
                    msg.flags &= -2;
                }
                if ((msg.flags & 3) != 3) {
                    buf.start = off;
                    buf.reset();
                    msg.encode_header(buf);
                    buf.enc_ndr_long(msg.alloc_hint);
                    buf.enc_ndr_short(0);
                    buf.enc_ndr_short(msg.getOpnum());
                }
                doSendFragment(stub, off, msg.length, isDirect);
                off += n;
            }
            doReceiveFragment(stub, isDirect);
            buf.reset();
            buf.setIndex(8);
            buf.setLength(buf.dec_ndr_short());
            if (this.securityProvider != null) {
                this.securityProvider.unwrap(buf);
            }
            buf.setIndex(0);
            msg.decode_header(buf);
            int off2 = 24;
            if (msg.ptype == 2 && !msg.isFlagSet(2)) {
                off2 = msg.length;
            }
            byte[] frag = null;
            NdrBuffer fbuf = null;
            while (!msg.isFlagSet(2)) {
                if (frag == null) {
                    frag = new byte[this.max_recv];
                    fbuf = new NdrBuffer(frag, 0);
                }
                doReceiveFragment(frag, isDirect);
                fbuf.reset();
                fbuf.setIndex(8);
                fbuf.setLength(fbuf.dec_ndr_short());
                if (this.securityProvider != null) {
                    this.securityProvider.unwrap(fbuf);
                }
                fbuf.reset();
                msg.decode_header(fbuf);
                int stub_frag_len = msg.length - 24;
                if (off2 + stub_frag_len > stub.length) {
                    byte[] tmp = new byte[(off2 + stub_frag_len)];
                    System.arraycopy(stub, 0, tmp, 0, off2);
                    stub = tmp;
                }
                System.arraycopy(frag, 24, stub, off2, stub_frag_len);
                off2 += stub_frag_len;
            }
            msg.decode(new NdrBuffer(stub, 0));
            BufferCache.releaseBuffer(stub);
            DcerpcException de = msg.getResult();
            if (de != null) {
                throw de;
            }
        } catch (Throwable th) {
            BufferCache.releaseBuffer(stub);
            throw th;
        }
    }

    public void setDcerpcSecurityProvider(DcerpcSecurityProvider securityProvider2) {
        this.securityProvider = securityProvider2;
    }

    public String getServer() {
        if (this instanceof DcerpcPipeHandle) {
            return ((DcerpcPipeHandle) this).pipe.getServer();
        }
        return null;
    }

    public Principal getPrincipal() {
        if (this instanceof DcerpcPipeHandle) {
            return ((DcerpcPipeHandle) this).pipe.getPrincipal();
        }
        return null;
    }

    public String toString() {
        return this.binding.toString();
    }
}
