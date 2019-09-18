package java.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Base64 {

    private static class DecInputStream extends InputStream {
        private final int[] base64;
        private int bits = 0;
        private boolean closed = false;
        private boolean eof = false;
        private final InputStream is;
        private final boolean isMIME;
        private int nextin = 18;
        private int nextout = -8;
        private byte[] sbBuf = new byte[1];

        DecInputStream(InputStream is2, int[] base642, boolean isMIME2) {
            this.is = is2;
            this.base64 = base642;
            this.isMIME = isMIME2;
        }

        public int read() throws IOException {
            if (read(this.sbBuf, 0, 1) == -1) {
                return -1;
            }
            return this.sbBuf[0] & Character.DIRECTIONALITY_UNDEFINED;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (this.closed) {
                throw new IOException("Stream is closed");
            } else if (this.eof && this.nextout < 0) {
                return -1;
            } else {
                if (off < 0 || len < 0 || len > b.length - off) {
                    throw new IndexOutOfBoundsException();
                }
                int oldOff = off;
                if (this.nextout >= 0) {
                    while (len != 0) {
                        int off2 = off + 1;
                        b[off] = (byte) (this.bits >> this.nextout);
                        len--;
                        this.nextout -= 8;
                        if (this.nextout < 0) {
                            this.bits = 0;
                            off = off2;
                        } else {
                            off = off2;
                        }
                    }
                    return off - oldOff;
                }
                while (true) {
                    if (len <= 0) {
                        break;
                    }
                    int v = this.is.read();
                    if (v == -1) {
                        this.eof = true;
                        if (this.nextin != 18) {
                            if (this.nextin != 12) {
                                int off3 = off + 1;
                                b[off] = (byte) (this.bits >> 16);
                                int len2 = len - 1;
                                if (this.nextin == 0) {
                                    if (len2 == 0) {
                                        this.bits >>= 8;
                                        this.nextout = 0;
                                    } else {
                                        off = off3 + 1;
                                        b[off3] = (byte) (this.bits >> 8);
                                    }
                                }
                                off = off3;
                            } else {
                                throw new IOException("Base64 stream has one un-decoded dangling byte.");
                            }
                        }
                        if (off == oldOff) {
                            return -1;
                        }
                        return off - oldOff;
                    } else if (v != 61) {
                        int i = this.base64[v];
                        int v2 = i;
                        if (i != -1) {
                            this.bits |= v2 << this.nextin;
                            if (this.nextin == 0) {
                                this.nextin = 18;
                                this.nextout = 16;
                                while (this.nextout >= 0) {
                                    int off4 = off + 1;
                                    b[off] = (byte) (this.bits >> this.nextout);
                                    len--;
                                    this.nextout -= 8;
                                    if (len == 0 && this.nextout >= 0) {
                                        return off4 - oldOff;
                                    }
                                    off = off4;
                                }
                                this.bits = 0;
                            } else {
                                this.nextin -= 6;
                            }
                        } else if (!this.isMIME) {
                            throw new IOException("Illegal base64 character " + Integer.toString(v2, 16));
                        }
                    } else if (this.nextin == 18 || this.nextin == 12 || (this.nextin == 6 && this.is.read() != 61)) {
                        throw new IOException("Illegal base64 ending sequence:" + this.nextin);
                    } else {
                        int off5 = off + 1;
                        b[off] = (byte) (this.bits >> 16);
                        int len3 = len - 1;
                        if (this.nextin == 0) {
                            if (len3 == 0) {
                                this.bits >>= 8;
                                this.nextout = 0;
                            } else {
                                off = off5 + 1;
                                b[off5] = (byte) (this.bits >> 8);
                                this.eof = true;
                            }
                        }
                        off = off5;
                        this.eof = true;
                    }
                }
                return off - oldOff;
            }
        }

        public int available() throws IOException {
            if (!this.closed) {
                return this.is.available();
            }
            throw new IOException("Stream is closed");
        }

        public void close() throws IOException {
            if (!this.closed) {
                this.closed = true;
                this.is.close();
            }
        }
    }

    public static class Decoder {
        static final Decoder RFC2045 = new Decoder(false, true);
        static final Decoder RFC4648 = new Decoder(false, false);
        static final Decoder RFC4648_URLSAFE = new Decoder(true, false);
        /* access modifiers changed from: private */
        public static final int[] fromBase64 = new int[256];
        private static final int[] fromBase64URL = new int[256];
        private final boolean isMIME;
        private final boolean isURL;

        private Decoder(boolean isURL2, boolean isMIME2) {
            this.isURL = isURL2;
            this.isMIME = isMIME2;
        }

        static {
            Arrays.fill(fromBase64, -1);
            for (int i = 0; i < Encoder.toBase64.length; i++) {
                fromBase64[Encoder.toBase64[i]] = i;
            }
            fromBase64[61] = -2;
            Arrays.fill(fromBase64URL, -1);
            for (int i2 = 0; i2 < Encoder.toBase64URL.length; i2++) {
                fromBase64URL[Encoder.toBase64URL[i2]] = i2;
            }
            fromBase64URL[61] = -2;
        }

        public byte[] decode(byte[] src) {
            byte[] dst = new byte[outLength(src, 0, src.length)];
            int ret = decode0(src, 0, src.length, dst);
            if (ret != dst.length) {
                return Arrays.copyOf(dst, ret);
            }
            return dst;
        }

        public byte[] decode(String src) {
            return decode(src.getBytes(StandardCharsets.ISO_8859_1));
        }

        public int decode(byte[] src, byte[] dst) {
            if (dst.length >= outLength(src, 0, src.length)) {
                return decode0(src, 0, src.length, dst);
            }
            throw new IllegalArgumentException("Output byte array is too small for decoding all input bytes");
        }

        public ByteBuffer decode(ByteBuffer buffer) {
            int sl;
            int sp;
            byte[] src;
            int pos0 = buffer.position();
            try {
                if (buffer.hasArray()) {
                    src = buffer.array();
                    sp = buffer.arrayOffset() + buffer.position();
                    sl = buffer.arrayOffset() + buffer.limit();
                    buffer.position(buffer.limit());
                } else {
                    src = new byte[buffer.remaining()];
                    buffer.get(src);
                    sp = 0;
                    sl = src.length;
                }
                byte[] dst = new byte[outLength(src, sp, sl)];
                return ByteBuffer.wrap(dst, 0, decode0(src, sp, sl, dst));
            } catch (IllegalArgumentException iae) {
                buffer.position(pos0);
                throw iae;
            }
        }

        public InputStream wrap(InputStream is) {
            Objects.requireNonNull(is);
            return new DecInputStream(is, this.isURL ? fromBase64URL : fromBase64, this.isMIME);
        }

        private int outLength(byte[] src, int sp, int sl) {
            int[] base64 = this.isURL ? fromBase64URL : fromBase64;
            int paddings = 0;
            int len = sl - sp;
            int n = 0;
            if (len == 0) {
                return 0;
            }
            if (len >= 2) {
                if (this.isMIME) {
                    while (true) {
                        if (sp >= sl) {
                            break;
                        }
                        int sp2 = sp + 1;
                        int sp3 = src[sp] & 255;
                        if (sp3 == 61) {
                            len -= (sl - sp2) + 1;
                            int b = sp2;
                            break;
                        }
                        int i = base64[sp3];
                        int b2 = i;
                        if (i == -1) {
                            n++;
                        }
                        sp = sp2;
                    }
                    len -= n;
                } else if (src[sl - 1] == 61) {
                    paddings = 0 + 1;
                    if (src[sl - 2] == 61) {
                        paddings++;
                    }
                }
                if (paddings == 0 && (len & 3) != 0) {
                    paddings = 4 - (len & 3);
                }
                return (3 * ((len + 3) / 4)) - paddings;
            } else if (this.isMIME && base64[0] == -1) {
                return 0;
            } else {
                throw new IllegalArgumentException("Input byte[] should at least have 2 bytes for base64 bytes");
            }
        }

        private int decode0(byte[] src, int sp, int sl, byte[] dst) {
            int[] base64 = this.isURL ? fromBase64URL : fromBase64;
            int bits = 0;
            int dp = 0;
            int b = sp;
            int shiftto = 18;
            while (true) {
                if (b >= sl) {
                    break;
                }
                int sp2 = b + 1;
                int i = base64[src[b] & 255];
                int b2 = i;
                if (i >= 0) {
                    bits |= b2 << shiftto;
                    shiftto -= 6;
                    if (shiftto < 0) {
                        int dp2 = dp + 1;
                        dst[dp] = (byte) (bits >> 16);
                        int dp3 = dp2 + 1;
                        dst[dp2] = (byte) (bits >> 8);
                        dst[dp3] = (byte) bits;
                        shiftto = 18;
                        bits = 0;
                        dp = dp3 + 1;
                    }
                } else if (b2 == -2) {
                    if (shiftto == 6) {
                        if (sp2 != sl) {
                            int sp3 = sp2 + 1;
                            if (src[sp2] == 61) {
                                sp2 = sp3;
                            } else {
                                int i2 = sp3;
                            }
                        }
                        throw new IllegalArgumentException("Input byte array has wrong 4-byte ending unit");
                    }
                    if (shiftto != 18) {
                        b = sp2;
                    }
                    throw new IllegalArgumentException("Input byte array has wrong 4-byte ending unit");
                } else if (!this.isMIME) {
                    throw new IllegalArgumentException("Illegal base64 character " + Integer.toString(src[sp2 - 1], 16));
                }
                b = sp2;
            }
            if (shiftto == 6) {
                dst[dp] = (byte) (bits >> 16);
                dp++;
            } else if (shiftto == 0) {
                int dp4 = dp + 1;
                dst[dp] = (byte) (bits >> 16);
                dp = dp4 + 1;
                dst[dp4] = (byte) (bits >> 8);
            } else if (shiftto == 12) {
                throw new IllegalArgumentException("Last unit does not have enough valid bits");
            }
            while (b < sl) {
                if (this.isMIME) {
                    int sp4 = b + 1;
                    if (base64[src[b]] < 0) {
                        b = sp4;
                    } else {
                        b = sp4;
                    }
                }
                throw new IllegalArgumentException("Input byte array has incorrect ending byte at " + b);
            }
            return dp;
        }
    }

    private static class EncOutputStream extends FilterOutputStream {
        private int b0;
        private int b1;
        private int b2;
        private final char[] base64;
        private boolean closed = false;
        private final boolean doPadding;
        private int leftover = 0;
        private final int linemax;
        private int linepos = 0;
        private final byte[] newline;

        EncOutputStream(OutputStream os, char[] base642, byte[] newline2, int linemax2, boolean doPadding2) {
            super(os);
            this.base64 = base642;
            this.newline = newline2;
            this.linemax = linemax2;
            this.doPadding = doPadding2;
        }

        public void write(int b) throws IOException {
            write(new byte[]{(byte) (b & 255)}, 0, 1);
        }

        private void checkNewline() throws IOException {
            if (this.linepos == this.linemax) {
                this.out.write(this.newline);
                this.linepos = 0;
            }
        }

        public void write(byte[] b, int off, int len) throws IOException {
            if (this.closed) {
                throw new IOException("Stream is closed");
            } else if (off < 0 || len < 0 || len > b.length - off) {
                throw new ArrayIndexOutOfBoundsException();
            } else if (len != 0) {
                if (this.leftover != 0) {
                    if (this.leftover == 1) {
                        int off2 = off + 1;
                        this.b1 = b[off] & 255;
                        len--;
                        if (len == 0) {
                            this.leftover++;
                            return;
                        }
                        off = off2;
                    }
                    this.b2 = b[off] & 255;
                    len--;
                    checkNewline();
                    this.out.write((int) this.base64[this.b0 >> 2]);
                    this.out.write((int) this.base64[((this.b0 << 4) & 63) | (this.b1 >> 4)]);
                    this.out.write((int) this.base64[((this.b1 << 2) & 63) | (this.b2 >> 6)]);
                    this.out.write((int) this.base64[this.b2 & 63]);
                    this.linepos += 4;
                    off++;
                }
                int off3 = len / 3;
                this.leftover = len - (off3 * 3);
                while (true) {
                    int nBits24 = off3 - 1;
                    if (off3 <= 0) {
                        break;
                    }
                    checkNewline();
                    int off4 = off + 1;
                    int off5 = off4 + 1;
                    int bits = ((b[off] & 255) << 16) | ((b[off4] & 255) << 8) | (b[off5] & 255);
                    this.out.write((int) this.base64[(bits >>> 18) & 63]);
                    this.out.write((int) this.base64[(bits >>> 12) & 63]);
                    this.out.write((int) this.base64[(bits >>> 6) & 63]);
                    this.out.write((int) this.base64[bits & 63]);
                    this.linepos += 4;
                    off = off5 + 1;
                    off3 = nBits24;
                }
                if (this.leftover == 1) {
                    this.b0 = b[off] & 255;
                    int i = off + 1;
                } else if (this.leftover == 2) {
                    int off6 = off + 1;
                    this.b0 = b[off] & 255;
                    int i2 = off6 + 1;
                    this.b1 = b[off6] & 255;
                }
            }
        }

        public void close() throws IOException {
            if (!this.closed) {
                this.closed = true;
                if (this.leftover == 1) {
                    checkNewline();
                    this.out.write((int) this.base64[this.b0 >> 2]);
                    this.out.write((int) this.base64[(this.b0 << 4) & 63]);
                    if (this.doPadding) {
                        this.out.write(61);
                        this.out.write(61);
                    }
                } else if (this.leftover == 2) {
                    checkNewline();
                    this.out.write((int) this.base64[this.b0 >> 2]);
                    this.out.write((int) this.base64[((this.b0 << 4) & 63) | (this.b1 >> 4)]);
                    this.out.write((int) this.base64[(this.b1 << 2) & 63]);
                    if (this.doPadding) {
                        this.out.write(61);
                    }
                }
                this.leftover = 0;
                this.out.close();
            }
        }
    }

    public static class Encoder {
        private static final byte[] CRLF = {13, 10};
        private static final int MIMELINEMAX = 76;
        static final Encoder RFC2045 = new Encoder(false, CRLF, MIMELINEMAX, true);
        static final Encoder RFC4648 = new Encoder(false, null, -1, true);
        static final Encoder RFC4648_URLSAFE = new Encoder(true, null, -1, true);
        /* access modifiers changed from: private */
        public static final char[] toBase64 = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', Locale.PRIVATE_USE_EXTENSION, 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
        /* access modifiers changed from: private */
        public static final char[] toBase64URL = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', Locale.PRIVATE_USE_EXTENSION, 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};
        private final boolean doPadding;
        private final boolean isURL;
        private final int linemax;
        private final byte[] newline;

        private Encoder(boolean isURL2, byte[] newline2, int linemax2, boolean doPadding2) {
            this.isURL = isURL2;
            this.newline = newline2;
            this.linemax = linemax2;
            this.doPadding = doPadding2;
        }

        private final int outLength(int srclen) {
            int len;
            if (this.doPadding) {
                len = 4 * ((srclen + 2) / 3);
            } else {
                int n = srclen % 3;
                len = (4 * (srclen / 3)) + (n == 0 ? 0 : n + 1);
            }
            if (this.linemax > 0) {
                return len + (((len - 1) / this.linemax) * this.newline.length);
            }
            return len;
        }

        public byte[] encode(byte[] src) {
            byte[] dst = new byte[outLength(src.length)];
            int ret = encode0(src, 0, src.length, dst);
            if (ret != dst.length) {
                return Arrays.copyOf(dst, ret);
            }
            return dst;
        }

        public int encode(byte[] src, byte[] dst) {
            if (dst.length >= outLength(src.length)) {
                return encode0(src, 0, src.length, dst);
            }
            throw new IllegalArgumentException("Output byte array is too small for encoding all input bytes");
        }

        public String encodeToString(byte[] src) {
            byte[] encoded = encode(src);
            return new String(encoded, 0, 0, encoded.length);
        }

        public ByteBuffer encode(ByteBuffer buffer) {
            int ret;
            byte[] dst = new byte[outLength(buffer.remaining())];
            if (buffer.hasArray()) {
                ret = encode0(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.arrayOffset() + buffer.limit(), dst);
                buffer.position(buffer.limit());
            } else {
                byte[] src = new byte[buffer.remaining()];
                buffer.get(src);
                ret = encode0(src, 0, src.length, dst);
            }
            if (ret != dst.length) {
                dst = Arrays.copyOf(dst, ret);
            }
            return ByteBuffer.wrap(dst);
        }

        public OutputStream wrap(OutputStream os) {
            Objects.requireNonNull(os);
            EncOutputStream encOutputStream = new EncOutputStream(os, this.isURL ? toBase64URL : toBase64, this.newline, this.linemax, this.doPadding);
            return encOutputStream;
        }

        public Encoder withoutPadding() {
            if (!this.doPadding) {
                return this;
            }
            return new Encoder(this.isURL, this.newline, this.linemax, false);
        }

        private int encode0(byte[] src, int off, int end, byte[] dst) {
            int i = end;
            char[] base64 = this.isURL ? toBase64URL : toBase64;
            int sp = off;
            int slen = ((i - off) / 3) * 3;
            int sl = off + slen;
            if (this.linemax > 0 && slen > (this.linemax / 4) * 3) {
                slen = (this.linemax / 4) * 3;
            }
            int sp2 = sp;
            int sp3 = 0;
            while (sp2 < sl) {
                int sl0 = Math.min(sp2 + slen, sl);
                int sp0 = sp2;
                int sp02 = sp3;
                while (sp0 < sl0) {
                    int sp03 = sp0 + 1;
                    int sp04 = sp03 + 1;
                    int i2 = ((src[sp0] & 255) << 16) | ((src[sp03] & 255) << 8);
                    int sp05 = sp04 + 1;
                    int bits = i2 | (src[sp04] & 255);
                    int dp0 = sp02 + 1;
                    dst[sp02] = (byte) base64[(bits >>> 18) & 63];
                    int dp02 = dp0 + 1;
                    dst[dp0] = (byte) base64[(bits >>> 12) & 63];
                    int dp03 = dp02 + 1;
                    dst[dp02] = (byte) base64[(bits >>> 6) & 63];
                    dst[dp03] = (byte) base64[bits & 63];
                    sp02 = dp03 + 1;
                    sp0 = sp05;
                }
                int dlen = ((sl0 - sp2) / 3) * 4;
                sp3 += dlen;
                sp2 = sl0;
                if (dlen == this.linemax && sp2 < i) {
                    byte[] bArr = this.newline;
                    int length = bArr.length;
                    int dp = sp3;
                    int dp2 = 0;
                    while (dp2 < length) {
                        dst[dp] = bArr[dp2];
                        dp2++;
                        dp++;
                    }
                    sp3 = dp;
                }
            }
            if (sp2 < i) {
                int sp4 = sp2 + 1;
                int sp5 = src[sp2] & 255;
                int dp3 = sp3 + 1;
                dst[sp3] = (byte) base64[sp5 >> 2];
                if (sp4 == i) {
                    int dp4 = dp3 + 1;
                    dst[dp3] = (byte) base64[(sp5 << 4) & 63];
                    if (this.doPadding == 0) {
                        return dp4;
                    }
                    int dp5 = dp4 + 1;
                    dst[dp4] = 61;
                    int dp6 = dp5 + 1;
                    dst[dp5] = 61;
                    return dp6;
                }
                int sp6 = sp4 + 1;
                int sp7 = src[sp4] & 255;
                int dp7 = dp3 + 1;
                dst[dp3] = (byte) base64[((sp5 << 4) & 63) | (sp7 >> 4)];
                int dp8 = dp7 + 1;
                dst[dp7] = (byte) base64[(sp7 << 2) & 63];
                if (this.doPadding != 0) {
                    dst[dp8] = 61;
                    int b1 = sp6;
                    return dp8 + 1;
                }
                int i3 = sp6;
                return dp8;
            }
            int i4 = sp2;
            return sp3;
        }
    }

    private Base64() {
    }

    public static Encoder getEncoder() {
        return Encoder.RFC4648;
    }

    public static Encoder getUrlEncoder() {
        return Encoder.RFC4648_URLSAFE;
    }

    public static Encoder getMimeEncoder() {
        return Encoder.RFC2045;
    }

    public static Encoder getMimeEncoder(int lineLength, byte[] lineSeparator) {
        Objects.requireNonNull(lineSeparator);
        int[] base64 = Decoder.fromBase64;
        int length = lineSeparator.length;
        int i = 0;
        while (i < length) {
            byte b = lineSeparator[i];
            if (base64[b & Character.DIRECTIONALITY_UNDEFINED] == -1) {
                i++;
            } else {
                throw new IllegalArgumentException("Illegal base64 line separator character 0x" + Integer.toString(b, 16));
            }
        }
        if (lineLength <= 0) {
            return Encoder.RFC4648;
        }
        Encoder encoder = new Encoder(false, lineSeparator, (lineLength >> 2) << 2, true);
        return encoder;
    }

    public static Decoder getDecoder() {
        return Decoder.RFC4648;
    }

    public static Decoder getUrlDecoder() {
        return Decoder.RFC4648_URLSAFE;
    }

    public static Decoder getMimeDecoder() {
        return Decoder.RFC2045;
    }
}
