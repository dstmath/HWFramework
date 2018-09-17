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

        DecInputStream(InputStream is, int[] base64, boolean isMIME) {
            this.is = is;
            this.base64 = base64;
            this.isMIME = isMIME;
        }

        public int read() throws IOException {
            return read(this.sbBuf, 0, 1) == -1 ? -1 : this.sbBuf[0] & 255;
        }

        /* JADX WARNING: Removed duplicated region for block: B:25:0x0051  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public int read(byte[] b, int off, int len) throws IOException {
            if (this.closed) {
                throw new IOException("Stream is closed");
            } else if (this.eof && this.nextout < 0) {
                return -1;
            } else {
                if (off < 0 || len < 0 || len > b.length - off) {
                    throw new IndexOutOfBoundsException();
                }
                int i;
                int oldOff = off;
                if (this.nextout >= 0) {
                    do {
                        i = off;
                        if (len == 0) {
                            return i - oldOff;
                        }
                        off = i + 1;
                        b[i] = (byte) (this.bits >> this.nextout);
                        len--;
                        this.nextout -= 8;
                    } while (this.nextout >= 0);
                    this.bits = 0;
                    i = off;
                    while (len > 0) {
                        int v = this.is.read();
                        if (v != -1) {
                            if (v != 61) {
                                v = this.base64[v];
                                if (v != -1) {
                                    this.bits |= v << this.nextin;
                                    if (this.nextin == 0) {
                                        this.nextin = 18;
                                        this.nextout = 16;
                                        while (true) {
                                            off = i;
                                            if (this.nextout < 0) {
                                                this.bits = 0;
                                                break;
                                            }
                                            i = off + 1;
                                            b[off] = (byte) (this.bits >> this.nextout);
                                            len--;
                                            this.nextout -= 8;
                                            i = off + 1;
                                            b[off] = (byte) (this.bits >> this.nextout);
                                            len--;
                                            this.nextout -= 8;
                                            if (len == 0 && this.nextout >= 0) {
                                                return i - oldOff;
                                            }
                                        }
                                    } else {
                                        this.nextin -= 6;
                                        off = i;
                                    }
                                }
                                if (!this.isMIME) {
                                    throw new IOException("Illegal base64 character " + Integer.toString(v, 16));
                                }
                            }
                            if (this.nextin == 18 || this.nextin == 12 || (this.nextin == 6 && this.is.read() != 61)) {
                                throw new IOException("Illegal base64 ending sequence:" + this.nextin);
                            }
                            off = i + 1;
                            b[i] = (byte) (this.bits >> 16);
                            len--;
                            if (this.nextin == 0) {
                                if (len == 0) {
                                    this.bits >>= 8;
                                    this.nextout = 0;
                                } else {
                                    i = off + 1;
                                    b[off] = (byte) (this.bits >> 8);
                                    off = i;
                                }
                            }
                            this.eof = true;
                            return off - oldOff;
                        }
                        this.eof = true;
                        if (this.nextin == 18) {
                            off = i;
                        } else if (this.nextin == 12) {
                            throw new IOException("Base64 stream has one un-decoded dangling byte.");
                        } else {
                            off = i + 1;
                            b[i] = (byte) (this.bits >> 16);
                            len--;
                            if (this.nextin == 0) {
                                if (len == 0) {
                                    this.bits >>= 8;
                                    this.nextout = 0;
                                } else {
                                    i = off + 1;
                                    b[off] = (byte) (this.bits >> 8);
                                    off = i;
                                }
                            }
                        }
                        if (off == oldOff) {
                            return -1;
                        }
                        return off - oldOff;
                    }
                    off = i;
                    return off - oldOff;
                }
                i = off;
                while (len > 0) {
                }
                off = i;
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
        private static final int[] fromBase64 = new int[256];
        private static final int[] fromBase64URL = new int[256];
        private final boolean isMIME;
        private final boolean isURL;

        private Decoder(boolean isURL, boolean isMIME) {
            this.isURL = isURL;
            this.isMIME = isMIME;
        }

        static {
            int i;
            Arrays.fill(fromBase64, -1);
            for (i = 0; i < Encoder.toBase64.length; i++) {
                fromBase64[Encoder.toBase64[i]] = i;
            }
            fromBase64[61] = -2;
            Arrays.fill(fromBase64URL, -1);
            for (i = 0; i < Encoder.toBase64URL.length; i++) {
                fromBase64URL[Encoder.toBase64URL[i]] = i;
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
            int pos0 = buffer.position();
            try {
                byte[] src;
                int sp;
                int sl;
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
            if (len == 0) {
                return 0;
            }
            if (len >= 2) {
                if (this.isMIME) {
                    int n = 0;
                    int sp2 = sp;
                    while (sp2 < sl) {
                        sp = sp2 + 1;
                        int b = src[sp2] & 255;
                        if (b == 61) {
                            len -= (sl - sp) + 1;
                            break;
                        }
                        if (base64[b] == -1) {
                            n++;
                        }
                        sp2 = sp;
                    }
                    len -= n;
                } else if (src[sl - 1] == (byte) 61) {
                    paddings = 1;
                    if (src[sl - 2] == (byte) 61) {
                        paddings = 1 + 1;
                    }
                }
                if (paddings == 0 && (len & 3) != 0) {
                    paddings = 4 - (len & 3);
                }
                return (((len + 3) / 4) * 3) - paddings;
            } else if (this.isMIME && base64[0] == -1) {
                return 0;
            } else {
                throw new IllegalArgumentException("Input byte[] should at least have 2 bytes for base64 bytes");
            }
        }

        /* JADX WARNING: Missing block: B:19:0x0038, code:
            if (r5 != 18) goto L_0x003a;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private int decode0(byte[] src, int sp, int sl, byte[] dst) {
            int i;
            int[] base64 = this.isURL ? fromBase64URL : fromBase64;
            int bits = 0;
            int shiftto = 18;
            int dp = 0;
            int sp2 = sp;
            while (sp2 < sl) {
                sp = sp2 + 1;
                int b = base64[src[sp2] & 255];
                if (b >= 0) {
                    bits |= b << shiftto;
                    shiftto -= 6;
                    if (shiftto < 0) {
                        i = dp + 1;
                        dst[dp] = (byte) (bits >> 16);
                        dp = i + 1;
                        dst[i] = (byte) (bits >> 8);
                        i = dp + 1;
                        dst[dp] = (byte) bits;
                        shiftto = 18;
                        bits = 0;
                    } else {
                        i = dp;
                    }
                    dp = i;
                    sp2 = sp;
                } else if (b == -2) {
                    if (shiftto == 6) {
                        if (sp != sl) {
                            sp2 = sp + 1;
                            if (src[sp] != (byte) 61) {
                                sp = sp2;
                            } else {
                                sp = sp2;
                            }
                        }
                        throw new IllegalArgumentException("Input byte array has wrong 4-byte ending unit");
                    }
                } else if (this.isMIME) {
                    sp2 = sp;
                } else {
                    throw new IllegalArgumentException("Illegal base64 character " + Integer.toString(src[sp - 1], 16));
                }
            }
            sp = sp2;
            if (shiftto == 6) {
                i = dp + 1;
                dst[dp] = (byte) (bits >> 16);
                sp2 = sp;
            } else if (shiftto == 0) {
                i = dp + 1;
                dst[dp] = (byte) (bits >> 16);
                dp = i + 1;
                dst[i] = (byte) (bits >> 8);
                i = dp;
                sp2 = sp;
            } else if (shiftto == 12) {
                throw new IllegalArgumentException("Last unit does not have enough valid bits");
            } else {
                i = dp;
                sp2 = sp;
            }
            while (sp2 < sl) {
                if (this.isMIME) {
                    sp = sp2 + 1;
                    if (base64[src[sp2]] < 0) {
                        sp2 = sp;
                    }
                } else {
                    sp = sp2;
                }
                throw new IllegalArgumentException("Input byte array has incorrect ending byte at " + sp);
            }
            return i;
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

        EncOutputStream(OutputStream os, char[] base64, byte[] newline, int linemax, boolean doPadding) {
            super(os);
            this.base64 = base64;
            this.newline = newline;
            this.linemax = linemax;
            this.doPadding = doPadding;
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
                int off2;
                if (this.leftover != 0) {
                    if (this.leftover == 1) {
                        off2 = off + 1;
                        this.b1 = b[off] & 255;
                        len--;
                        if (len == 0) {
                            this.leftover++;
                            return;
                        }
                        off = off2;
                    }
                    off2 = off + 1;
                    this.b2 = b[off] & 255;
                    len--;
                    checkNewline();
                    this.out.write(this.base64[this.b0 >> 2]);
                    this.out.write(this.base64[((this.b0 << 4) & 63) | (this.b1 >> 4)]);
                    this.out.write(this.base64[((this.b1 << 2) & 63) | (this.b2 >> 6)]);
                    this.out.write(this.base64[this.b2 & 63]);
                    this.linepos += 4;
                    off = off2;
                }
                int i = len / 3;
                this.leftover = len - (i * 3);
                while (true) {
                    int nBits24 = i;
                    off2 = off;
                    i = nBits24 - 1;
                    if (nBits24 <= 0) {
                        break;
                    }
                    checkNewline();
                    off = off2 + 1;
                    off2 = off + 1;
                    off = off2 + 1;
                    int bits = (((b[off2] & 255) << 16) | ((b[off] & 255) << 8)) | (b[off2] & 255);
                    this.out.write(this.base64[(bits >>> 18) & 63]);
                    this.out.write(this.base64[(bits >>> 12) & 63]);
                    this.out.write(this.base64[(bits >>> 6) & 63]);
                    this.out.write(this.base64[bits & 63]);
                    this.linepos += 4;
                }
                if (this.leftover == 1) {
                    off = off2 + 1;
                    this.b0 = b[off2] & 255;
                } else if (this.leftover == 2) {
                    off = off2 + 1;
                    this.b0 = b[off2] & 255;
                    off2 = off + 1;
                    this.b1 = b[off] & 255;
                    off = off2;
                }
            }
        }

        public void close() throws IOException {
            if (!this.closed) {
                this.closed = true;
                if (this.leftover == 1) {
                    checkNewline();
                    this.out.write(this.base64[this.b0 >> 2]);
                    this.out.write(this.base64[(this.b0 << 4) & 63]);
                    if (this.doPadding) {
                        this.out.write(61);
                        this.out.write(61);
                    }
                } else if (this.leftover == 2) {
                    checkNewline();
                    this.out.write(this.base64[this.b0 >> 2]);
                    this.out.write(this.base64[((this.b0 << 4) & 63) | (this.b1 >> 4)]);
                    this.out.write(this.base64[(this.b1 << 2) & 63]);
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
        private static final byte[] CRLF = new byte[]{(byte) 13, (byte) 10};
        private static final int MIMELINEMAX = 76;
        static final Encoder RFC2045 = new Encoder(false, CRLF, MIMELINEMAX, true);
        static final Encoder RFC4648 = new Encoder(false, null, -1, true);
        static final Encoder RFC4648_URLSAFE = new Encoder(true, null, -1, true);
        private static final char[] toBase64 = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', Locale.PRIVATE_USE_EXTENSION, 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
        private static final char[] toBase64URL = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', Locale.PRIVATE_USE_EXTENSION, 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};
        private final boolean doPadding;
        private final boolean isURL;
        private final int linemax;
        private final byte[] newline;

        /* synthetic */ Encoder(boolean isURL, byte[] newline, int linemax, boolean doPadding, Encoder -this4) {
            this(isURL, newline, linemax, doPadding);
        }

        private Encoder(boolean isURL, byte[] newline, int linemax, boolean doPadding) {
            this.isURL = isURL;
            this.newline = newline;
            this.linemax = linemax;
            this.doPadding = doPadding;
        }

        private final int outLength(int srclen) {
            int len;
            int i = 0;
            if (this.doPadding) {
                len = ((srclen + 2) / 3) * 4;
            } else {
                int n = srclen % 3;
                int i2 = (srclen / 3) * 4;
                if (n != 0) {
                    i = n + 1;
                }
                len = i2 + i;
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
            return new EncOutputStream(os, this.isURL ? toBase64URL : toBase64, this.newline, this.linemax, this.doPadding);
        }

        public Encoder withoutPadding() {
            if (this.doPadding) {
                return new Encoder(this.isURL, this.newline, this.linemax, false);
            }
            return this;
        }

        private int encode0(byte[] src, int off, int end, byte[] dst) {
            int dp;
            int sp;
            char[] base64 = this.isURL ? toBase64URL : toBase64;
            int sp2 = off;
            int slen = ((end - off) / 3) * 3;
            int sl = off + slen;
            if (this.linemax > 0 && slen > (this.linemax / 4) * 3) {
                slen = (this.linemax / 4) * 3;
            }
            int dp2 = 0;
            while (true) {
                dp = dp2;
                sp = sp2;
                if (sp >= sl) {
                    break;
                }
                int sl0 = Math.min(sp + slen, sl);
                int sp0 = sp;
                int dp0 = dp;
                while (true) {
                    int sp02 = sp0;
                    if (sp02 >= sl0) {
                        break;
                    }
                    sp0 = sp02 + 1;
                    sp02 = sp0 + 1;
                    sp0 = sp02 + 1;
                    int bits = (((src[sp02] & 255) << 16) | ((src[sp0] & 255) << 8)) | (src[sp02] & 255);
                    int i = dp0 + 1;
                    dst[dp0] = (byte) base64[(bits >>> 18) & 63];
                    dp0 = i + 1;
                    dst[i] = (byte) base64[(bits >>> 12) & 63];
                    i = dp0 + 1;
                    dst[dp0] = (byte) base64[(bits >>> 6) & 63];
                    dp0 = i + 1;
                    dst[i] = (byte) base64[bits & 63];
                }
                int dlen = ((sl0 - sp) / 3) * 4;
                dp2 = dp + dlen;
                sp2 = sl0;
                if (dlen == this.linemax && sl0 < end) {
                    byte[] bArr = this.newline;
                    int i2 = 0;
                    int length = bArr.length;
                    while (true) {
                        dp = dp2;
                        if (i2 >= length) {
                            break;
                        }
                        dp2 = dp + 1;
                        dst[dp] = bArr[i2];
                        i2++;
                    }
                    dp2 = dp;
                }
            }
            if (sp < end) {
                sp2 = sp + 1;
                int b0 = src[sp] & 255;
                dp2 = dp + 1;
                dst[dp] = (byte) base64[b0 >> 2];
                if (sp2 == end) {
                    dp = dp2 + 1;
                    dst[dp2] = (byte) base64[(b0 << 4) & 63];
                    if (!this.doPadding) {
                        return dp;
                    }
                    dp2 = dp + 1;
                    dst[dp] = (byte) 61;
                    dp = dp2 + 1;
                    dst[dp2] = (byte) 61;
                    return dp;
                }
                sp = sp2 + 1;
                int b1 = src[sp2] & 255;
                dp = dp2 + 1;
                dst[dp2] = (byte) base64[((b0 << 4) & 63) | (b1 >> 4)];
                dp2 = dp + 1;
                dst[dp] = (byte) base64[(b1 << 2) & 63];
                if (this.doPadding) {
                    dp = dp2 + 1;
                    dst[dp2] = (byte) 61;
                    sp2 = sp;
                    return dp;
                }
                return dp2;
            }
            sp2 = sp;
            return dp;
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
        for (byte b : lineSeparator) {
            if (base64[b & 255] != -1) {
                throw new IllegalArgumentException("Illegal base64 line separator character 0x" + Integer.toString(b, 16));
            }
        }
        if (lineLength <= 0) {
            return Encoder.RFC4648;
        }
        return new Encoder(false, lineSeparator, (lineLength >> 2) << 2, true, null);
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
