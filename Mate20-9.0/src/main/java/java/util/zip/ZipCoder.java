package java.util.zip;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import sun.nio.cs.ArrayDecoder;
import sun.nio.cs.ArrayEncoder;

final class ZipCoder {
    private Charset cs;
    private CharsetDecoder dec;
    private CharsetEncoder enc;
    private boolean isUTF8;
    private ZipCoder utf8;

    /* access modifiers changed from: package-private */
    public String toString(byte[] ba, int length) {
        CharsetDecoder cd = decoder().reset();
        int len = (int) (((float) length) * cd.maxCharsPerByte());
        char[] ca = new char[len];
        if (len == 0) {
            return new String(ca);
        }
        if (!this.isUTF8 || !(cd instanceof ArrayDecoder)) {
            ByteBuffer bb = ByteBuffer.wrap(ba, 0, length);
            CharBuffer cb = CharBuffer.wrap(ca);
            CoderResult cr = cd.decode(bb, cb, true);
            if (cr.isUnderflow()) {
                CoderResult cr2 = cd.flush(cb);
                if (cr2.isUnderflow()) {
                    return new String(ca, 0, cb.position());
                }
                throw new IllegalArgumentException(cr2.toString());
            }
            throw new IllegalArgumentException(cr.toString());
        }
        int clen = ((ArrayDecoder) cd).decode(ba, 0, length, ca);
        if (clen != -1) {
            return new String(ca, 0, clen);
        }
        throw new IllegalArgumentException("MALFORMED");
    }

    /* access modifiers changed from: package-private */
    public String toString(byte[] ba) {
        return toString(ba, ba.length);
    }

    /* access modifiers changed from: package-private */
    public byte[] getBytes(String s) {
        CharsetEncoder ce = encoder().reset();
        char[] ca = s.toCharArray();
        int len = (int) (((float) ca.length) * ce.maxBytesPerChar());
        byte[] ba = new byte[len];
        if (len == 0) {
            return ba;
        }
        if (!this.isUTF8 || !(ce instanceof ArrayEncoder)) {
            ByteBuffer bb = ByteBuffer.wrap(ba);
            CoderResult cr = ce.encode(CharBuffer.wrap(ca), bb, true);
            if (cr.isUnderflow()) {
                CoderResult cr2 = ce.flush(bb);
                if (!cr2.isUnderflow()) {
                    throw new IllegalArgumentException(cr2.toString());
                } else if (bb.position() == ba.length) {
                    return ba;
                } else {
                    return Arrays.copyOf(ba, bb.position());
                }
            } else {
                throw new IllegalArgumentException(cr.toString());
            }
        } else {
            int blen = ((ArrayEncoder) ce).encode(ca, 0, ca.length, ba);
            if (blen != -1) {
                return Arrays.copyOf(ba, blen);
            }
            throw new IllegalArgumentException("MALFORMED");
        }
    }

    /* access modifiers changed from: package-private */
    public byte[] getBytesUTF8(String s) {
        if (this.isUTF8) {
            return getBytes(s);
        }
        if (this.utf8 == null) {
            this.utf8 = new ZipCoder(StandardCharsets.UTF_8);
        }
        return this.utf8.getBytes(s);
    }

    /* access modifiers changed from: package-private */
    public String toStringUTF8(byte[] ba, int len) {
        if (this.isUTF8) {
            return toString(ba, len);
        }
        if (this.utf8 == null) {
            this.utf8 = new ZipCoder(StandardCharsets.UTF_8);
        }
        return this.utf8.toString(ba, len);
    }

    /* access modifiers changed from: package-private */
    public boolean isUTF8() {
        return this.isUTF8;
    }

    private ZipCoder(Charset cs2) {
        this.cs = cs2;
        this.isUTF8 = cs2.name().equals(StandardCharsets.UTF_8.name());
    }

    static ZipCoder get(Charset charset) {
        return new ZipCoder(charset);
    }

    private CharsetDecoder decoder() {
        if (this.dec == null) {
            this.dec = this.cs.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
        }
        return this.dec;
    }

    private CharsetEncoder encoder() {
        if (this.enc == null) {
            this.enc = this.cs.newEncoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
        }
        return this.enc;
    }
}
