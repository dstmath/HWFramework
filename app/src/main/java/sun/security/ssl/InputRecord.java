package sun.security.ssl;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.regex.Pattern;
import javax.crypto.BadPaddingException;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLProtocolException;
import sun.misc.FloatConsts;
import sun.misc.HexDumpEncoder;
import sun.security.util.DerValue;

class InputRecord extends ByteArrayInputStream implements Record {
    static final Debug debug = null;
    private static final byte[] v2NoCipher = null;
    private boolean appDataValid;
    private int exlen;
    boolean formatVerified;
    private HandshakeHash handshakeHash;
    private ProtocolVersion helloVersion;
    private boolean isClosed;
    private int lastHashed;
    private byte[] v2Buf;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.InputRecord.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.InputRecord.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.InputRecord.<clinit>():void");
    }

    InputRecord() {
        super(new byte[Record.maxRecordSize]);
        this.formatVerified = true;
        setHelloVersion(ProtocolVersion.DEFAULT_HELLO);
        this.pos = 5;
        this.count = 5;
        this.lastHashed = this.count;
        this.exlen = 0;
        this.v2Buf = null;
    }

    void setHelloVersion(ProtocolVersion helloVersion) {
        this.helloVersion = helloVersion;
    }

    ProtocolVersion getHelloVersion() {
        return this.helloVersion;
    }

    void enableFormatChecks() {
        this.formatVerified = false;
    }

    boolean isAppDataValid() {
        return this.appDataValid;
    }

    void setAppDataValid(boolean value) {
        this.appDataValid = value;
    }

    byte contentType() {
        return this.buf[0];
    }

    void setHandshakeHash(HandshakeHash handshakeHash) {
        this.handshakeHash = handshakeHash;
    }

    HandshakeHash getHandshakeHash() {
        return this.handshakeHash;
    }

    void decrypt(MAC signer, CipherBox box) throws BadPaddingException {
        BadPaddingException reservedBPE = null;
        int tagLen = signer.MAClen();
        int cipheredLength = this.count - 5;
        if (!box.isNullCipher()) {
            if (box.sanityCheck(tagLen, cipheredLength)) {
                try {
                    this.count = box.decrypt(this.buf, 5, cipheredLength, tagLen) + 5;
                } catch (BadPaddingException bpe) {
                    reservedBPE = bpe;
                }
            } else {
                throw new BadPaddingException("ciphertext sanity check failed");
            }
        }
        if (tagLen != 0) {
            int contentLen = (this.count - tagLen) - 5;
            if (contentLen < 0) {
                if (reservedBPE == null) {
                    reservedBPE = new BadPaddingException("bad record");
                }
                contentLen = ((cipheredLength + 5) - tagLen) - 5;
            }
            this.count -= tagLen;
            if (checkMacTags(contentType(), this.buf, 5, contentLen, signer, false) && r14 == null) {
                reservedBPE = new BadPaddingException("bad record MAC");
            }
            if (box.isCBCMode()) {
                int remainingLen = calculateRemainingLen(signer, cipheredLength, contentLen);
                if (remainingLen > this.buf.length) {
                    throw new RuntimeException("Internal buffer capacity error");
                }
                checkMacTags(contentType(), this.buf, 0, remainingLen, signer, true);
            }
        }
        if (reservedBPE != null) {
            throw reservedBPE;
        }
    }

    static boolean checkMacTags(byte contentType, byte[] buffer, int offset, int contentLen, MAC signer, boolean isSimulated) {
        int tagLen = signer.MAClen();
        byte[] hash = signer.compute(contentType, buffer, offset, contentLen, isSimulated);
        if (hash != null && tagLen == hash.length) {
            return compareMacTags(buffer, offset + contentLen, hash)[0] != 0;
        } else {
            throw new RuntimeException("Internal MAC error");
        }
    }

    private static int[] compareMacTags(byte[] buffer, int offset, byte[] tag) {
        int[] results = new int[]{0, 0};
        for (int i = 0; i < tag.length; i++) {
            if (buffer[offset + i] != tag[i]) {
                results[0] = results[0] + 1;
            } else {
                results[1] = results[1] + 1;
            }
        }
        return results;
    }

    static int calculateRemainingLen(MAC signer, int fullLen, int usedLen) {
        int blockLen = signer.hashBlockLen();
        int minimalPaddingLen = signer.minimalPaddingLen();
        return (((int) (Math.ceil(((double) (fullLen + (13 - (blockLen - minimalPaddingLen)))) / (((double) blockLen) * 1.0d)) - Math.ceil(((double) (usedLen + (13 - (blockLen - minimalPaddingLen)))) / (((double) blockLen) * 1.0d)))) * signer.hashBlockLen()) + 1;
    }

    void ignore(int bytes) {
        if (bytes > 0) {
            this.pos += bytes;
            this.lastHashed = this.pos;
        }
    }

    void doHashes() {
        int len = this.pos - this.lastHashed;
        if (len > 0) {
            hashInternal(this.buf, this.lastHashed, len);
            this.lastHashed = this.pos;
        }
    }

    private void hashInternal(byte[] databuf, int offset, int len) {
        if (debug != null && Debug.isOn("data")) {
            try {
                HexDumpEncoder hd = new HexDumpEncoder();
                System.out.println("[read] MD5 and SHA1 hashes:  len = " + len);
                hd.encodeBuffer(new ByteArrayInputStream(databuf, offset, len), System.out);
            } catch (IOException e) {
            }
        }
        this.handshakeHash.update(databuf, offset, len);
    }

    void queueHandshake(InputRecord r) throws IOException {
        int len;
        doHashes();
        if (this.pos > 5) {
            len = this.count - this.pos;
            if (len != 0) {
                System.arraycopy(this.buf, this.pos, this.buf, 5, len);
            }
            this.pos = 5;
            this.lastHashed = this.pos;
            this.count = len + 5;
        }
        len = r.available() + this.count;
        if (this.buf.length < len) {
            byte[] newbuf = new byte[len];
            System.arraycopy(this.buf, 0, newbuf, 0, this.count);
            this.buf = newbuf;
        }
        System.arraycopy(r.buf, r.pos, this.buf, this.count, len - this.count);
        this.count = len;
        len = r.lastHashed - r.pos;
        if (this.pos == 5) {
            this.lastHashed += len;
            r.pos = r.count;
            return;
        }
        throw new SSLProtocolException("?? confused buffer hashing ??");
    }

    public void close() {
        this.appDataValid = false;
        this.isClosed = true;
        this.mark = 0;
        this.pos = 0;
        this.count = 0;
    }

    private int readFully(InputStream s, byte[] b, int off, int len) throws IOException {
        int n = 0;
        while (n < len) {
            int readLen = s.read(b, off + n, len - n);
            if (readLen < 0) {
                return readLen;
            }
            if (debug != null && Debug.isOn("packet")) {
                try {
                    HexDumpEncoder hd = new HexDumpEncoder();
                    ByteBuffer bb = ByteBuffer.wrap(b, off + n, readLen);
                    System.out.println("[Raw read]: length = " + bb.remaining());
                    hd.encodeBuffer(bb, System.out);
                } catch (IOException e) {
                }
            }
            n += readLen;
            this.exlen += readLen;
        }
        return n;
    }

    void read(InputStream s, OutputStream o) throws IOException {
        if (!this.isClosed) {
            if (this.exlen < 5) {
                if (readFully(s, this.buf, this.exlen, 5 - this.exlen) < 0) {
                    throw new EOFException("SSL peer shut down incorrectly");
                }
                this.pos = 5;
                this.count = 5;
                this.lastHashed = this.pos;
            }
            if (this.formatVerified) {
                readV3Record(s, o);
            } else {
                this.formatVerified = true;
                if (this.buf[0] == 22 || this.buf[0] == 21) {
                    readV3Record(s, o);
                } else {
                    handleUnknownRecord(s, o);
                }
            }
        }
    }

    private void readV3Record(InputStream s, OutputStream o) throws IOException {
        Object recordVersion = ProtocolVersion.valueOf(this.buf[1], this.buf[2]);
        if (recordVersion.v < ProtocolVersion.MIN.v || recordVersion.major > ProtocolVersion.MAX.major) {
            throw new SSLException("Unsupported record version " + recordVersion);
        }
        int contentLen = ((this.buf[3] & 255) << 8) + (this.buf[4] & 255);
        if (contentLen < 0 || contentLen > 33300) {
            throw new SSLProtocolException("Bad InputRecord size, count = " + contentLen + ", buf.length = " + this.buf.length);
        }
        if (contentLen > this.buf.length - 5) {
            byte[] newbuf = new byte[(contentLen + 5)];
            System.arraycopy(this.buf, 0, newbuf, 0, 5);
            this.buf = newbuf;
        }
        if (this.exlen >= contentLen + 5 || readFully(s, this.buf, this.exlen, (contentLen + 5) - this.exlen) >= 0) {
            this.count = contentLen + 5;
            this.exlen = 0;
            if (debug != null && Debug.isOn("record")) {
                if (this.count < 0 || this.count > 16916) {
                    System.out.println(Thread.currentThread().getName() + ", Bad InputRecord size" + ", count = " + this.count);
                }
                System.out.println(Thread.currentThread().getName() + ", READ: " + recordVersion + " " + contentName(contentType()) + ", length = " + available());
                return;
            }
            return;
        }
        throw new SSLException("SSL peer shut down incorrectly");
    }

    private void handleUnknownRecord(InputStream s, OutputStream o) throws IOException {
        if ((this.buf[0] & Pattern.CANON_EQ) == 0 || this.buf[2] != (byte) 1) {
            if ((this.buf[0] & Pattern.CANON_EQ) == 0 || this.buf[2] != 4) {
                for (int i = 0; i < v2NoCipher.length; i++) {
                    if (this.buf[i] != v2NoCipher[i]) {
                        throw new SSLException("Unrecognized SSL message, plaintext connection?");
                    }
                }
                throw new SSLException("SSL V2.0 servers are not supported.");
            }
            throw new SSLException("SSL V2.0 servers are not supported.");
        } else if (this.helloVersion != ProtocolVersion.SSL20Hello) {
            throw new SSLHandshakeException("SSLv2Hello is disabled");
        } else if (ProtocolVersion.valueOf(this.buf[3], this.buf[4]) == ProtocolVersion.SSL20Hello) {
            try {
                writeBuffer(o, v2NoCipher, 0, v2NoCipher.length);
            } catch (Exception e) {
            }
            throw new SSLException("Unsupported SSL v2.0 ClientHello");
        } else {
            int len = (((this.buf[0] & FloatConsts.MAX_EXPONENT) << 8) + (this.buf[1] & 255)) - 3;
            if (this.v2Buf == null) {
                this.v2Buf = new byte[len];
            }
            if (this.exlen >= len + 5 || readFully(s, this.v2Buf, this.exlen - 5, (len + 5) - this.exlen) >= 0) {
                this.exlen = 0;
                hashInternal(this.buf, 2, 3);
                hashInternal(this.v2Buf, 0, len);
                V2toV3ClientHello(this.v2Buf);
                this.v2Buf = null;
                this.lastHashed = this.count;
                if (debug != null && Debug.isOn("record")) {
                    System.out.println(Thread.currentThread().getName() + ", READ:  SSL v2, contentType = " + contentName(contentType()) + ", translated length = " + available());
                    return;
                }
                return;
            }
            throw new EOFException("SSL peer shut down incorrectly");
        }
    }

    void writeBuffer(OutputStream s, byte[] buf, int off, int len) throws IOException {
        s.write(buf, 0, len);
        s.flush();
    }

    private void V2toV3ClientHello(byte[] v2Msg) throws SSLException {
        int i;
        byte[] bArr;
        int i2;
        int j;
        this.buf[0] = DerValue.tag_IA5String;
        this.buf[1] = this.buf[3];
        this.buf[2] = this.buf[4];
        this.buf[5] = (byte) 1;
        this.buf[9] = this.buf[1];
        this.buf[10] = this.buf[2];
        this.count = 11;
        int cipherSpecLen = ((v2Msg[0] & 255) << 8) + (v2Msg[1] & 255);
        int sessionIdLen = ((v2Msg[2] & 255) << 8) + (v2Msg[3] & 255);
        int nonceLen = ((v2Msg[4] & 255) << 8) + (v2Msg[5] & 255);
        int offset = (cipherSpecLen + 6) + sessionIdLen;
        if (nonceLen < 32) {
            for (i = 0; i < 32 - nonceLen; i++) {
                bArr = this.buf;
                i2 = this.count;
                this.count = i2 + 1;
                bArr[i2] = (byte) 0;
            }
            System.arraycopy(v2Msg, offset, this.buf, this.count, nonceLen);
            this.count += nonceLen;
        } else {
            System.arraycopy(v2Msg, (nonceLen - 32) + offset, this.buf, this.count, 32);
            this.count += 32;
        }
        offset -= sessionIdLen;
        bArr = this.buf;
        i2 = this.count;
        this.count = i2 + 1;
        bArr[i2] = (byte) sessionIdLen;
        System.arraycopy(v2Msg, offset, this.buf, this.count, sessionIdLen);
        this.count += sessionIdLen;
        offset -= cipherSpecLen;
        i = 0;
        int j2 = this.count + 2;
        while (i < cipherSpecLen) {
            if (v2Msg[offset + i] != null) {
                j = j2;
            } else {
                j = j2 + 1;
                this.buf[j2] = v2Msg[(offset + i) + 1];
                j2 = j + 1;
                this.buf[j] = v2Msg[(offset + i) + 2];
                j = j2;
            }
            i += 3;
            j2 = j;
        }
        j = j2 - (this.count + 2);
        bArr = this.buf;
        i2 = this.count;
        this.count = i2 + 1;
        bArr[i2] = (byte) (j >>> 8);
        bArr = this.buf;
        i2 = this.count;
        this.count = i2 + 1;
        bArr[i2] = (byte) j;
        this.count += j;
        bArr = this.buf;
        i2 = this.count;
        this.count = i2 + 1;
        bArr[i2] = (byte) 1;
        bArr = this.buf;
        i2 = this.count;
        this.count = i2 + 1;
        bArr[i2] = (byte) 0;
        this.buf[3] = (byte) (this.count - 5);
        this.buf[4] = (byte) ((this.count - 5) >>> 8);
        this.buf[6] = (byte) 0;
        this.buf[7] = (byte) (((this.count - 5) - 4) >>> 8);
        this.buf[8] = (byte) ((this.count - 5) - 4);
        this.pos = 5;
    }

    static String contentName(int contentType) {
        switch (contentType) {
            case Record.trailerSize /*20*/:
                return "Change Cipher Spec";
            case 21:
                return "Alert";
            case ZipConstants.LOCLEN /*22*/:
                return "Handshake";
            case SecureRandom.DEFAULT_SDK_TARGET_FOR_CRYPTO_PROVIDER_WORKAROUND /*23*/:
                return "Application Data";
            default:
                return "contentType = " + contentType;
        }
    }
}
