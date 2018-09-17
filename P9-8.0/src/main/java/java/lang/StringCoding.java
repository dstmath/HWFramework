package java.lang;

import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Arrays;
import sun.misc.MessageUtils;
import sun.nio.cs.ArrayDecoder;
import sun.nio.cs.ArrayEncoder;
import sun.nio.cs.HistoricallyNamedCharset;

class StringCoding {
    private static final ThreadLocal<SoftReference<StringDecoder>> decoder = new ThreadLocal();
    private static final ThreadLocal<SoftReference<StringEncoder>> encoder = new ThreadLocal();
    private static boolean warnUnsupportedCharset = true;

    private static class StringDecoder {
        private final CharsetDecoder cd;
        private final Charset cs;
        private final boolean isTrusted;
        private final String requestedCharsetName;

        /* synthetic */ StringDecoder(Charset cs, String rcn, StringDecoder -this2) {
            this(cs, rcn);
        }

        private StringDecoder(Charset cs, String rcn) {
            this.requestedCharsetName = rcn;
            this.cs = cs;
            this.cd = cs.newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
            this.isTrusted = cs.getClass().getClassLoader() == null;
        }

        String charsetName() {
            if (this.cs instanceof HistoricallyNamedCharset) {
                return ((HistoricallyNamedCharset) this.cs).historicalName();
            }
            return this.cs.name();
        }

        final String requestedCharsetName() {
            return this.requestedCharsetName;
        }

        char[] decode(byte[] ba, int off, int len) {
            char[] ca = new char[StringCoding.scale(len, this.cd.maxCharsPerByte())];
            if (len == 0) {
                return ca;
            }
            if (this.cd instanceof ArrayDecoder) {
                return StringCoding.safeTrim(ca, ((ArrayDecoder) this.cd).decode(ba, off, len, ca), this.cs, this.isTrusted);
            }
            this.cd.reset();
            ByteBuffer bb = ByteBuffer.wrap(ba, off, len);
            CharBuffer cb = CharBuffer.wrap(ca);
            try {
                CoderResult cr = this.cd.decode(bb, cb, true);
                if (!cr.isUnderflow()) {
                    cr.throwException();
                }
                cr = this.cd.flush(cb);
                if (!cr.isUnderflow()) {
                    cr.throwException();
                }
                return StringCoding.safeTrim(ca, cb.position(), this.cs, this.isTrusted);
            } catch (Throwable x) {
                throw new Error(x);
            }
        }
    }

    private static class StringEncoder {
        private CharsetEncoder ce;
        private Charset cs;
        private final boolean isTrusted;
        private final String requestedCharsetName;

        /* synthetic */ StringEncoder(Charset cs, String rcn, StringEncoder -this2) {
            this(cs, rcn);
        }

        private StringEncoder(Charset cs, String rcn) {
            this.requestedCharsetName = rcn;
            this.cs = cs;
            this.ce = cs.newEncoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
            this.isTrusted = cs.getClass().getClassLoader() == null;
        }

        String charsetName() {
            if (this.cs instanceof HistoricallyNamedCharset) {
                return ((HistoricallyNamedCharset) this.cs).historicalName();
            }
            return this.cs.name();
        }

        final String requestedCharsetName() {
            return this.requestedCharsetName;
        }

        byte[] encode(char[] ca, int off, int len) {
            byte[] ba = new byte[StringCoding.scale(len, this.ce.maxBytesPerChar())];
            if (len == 0) {
                return ba;
            }
            if (this.ce instanceof ArrayEncoder) {
                return StringCoding.safeTrim(ba, ((ArrayEncoder) this.ce).encode(ca, off, len, ba), this.cs, this.isTrusted);
            }
            this.ce.reset();
            ByteBuffer bb = ByteBuffer.wrap(ba);
            try {
                CoderResult cr = this.ce.encode(CharBuffer.wrap(ca, off, len).asReadOnlyBuffer(), bb, true);
                if (!cr.isUnderflow()) {
                    cr.throwException();
                }
                cr = this.ce.flush(bb);
                if (!cr.isUnderflow()) {
                    cr.throwException();
                }
                return StringCoding.safeTrim(ba, bb.position(), this.cs, this.isTrusted);
            } catch (Throwable x) {
                throw new Error(x);
            }
        }
    }

    private StringCoding() {
    }

    private static <T> T deref(ThreadLocal<SoftReference<T>> tl) {
        SoftReference<T> sr = (SoftReference) tl.get();
        if (sr == null) {
            return null;
        }
        return sr.get();
    }

    private static <T> void set(ThreadLocal<SoftReference<T>> tl, T ob) {
        tl.set(new SoftReference(ob));
    }

    private static byte[] safeTrim(byte[] ba, int len, Charset cs, boolean isTrusted) {
        if (len == ba.length && isTrusted) {
            return ba;
        }
        return Arrays.copyOf(ba, len);
    }

    private static char[] safeTrim(char[] ca, int len, Charset cs, boolean isTrusted) {
        if (len == ca.length && isTrusted) {
            return ca;
        }
        return Arrays.copyOf(ca, len);
    }

    private static int scale(int len, float expansionFactor) {
        return (int) (((double) len) * ((double) expansionFactor));
    }

    private static Charset lookupCharset(String csn) {
        if (!Charset.isSupported(csn)) {
            return null;
        }
        try {
            return Charset.forName(csn);
        } catch (Throwable x) {
            throw new Error(x);
        }
    }

    private static void warnUnsupportedCharset(String csn) {
        if (warnUnsupportedCharset) {
            MessageUtils.err("WARNING: Default charset " + csn + " not supported, using ISO-8859-1 instead");
            warnUnsupportedCharset = false;
        }
    }

    /* JADX WARNING: Missing block: B:8:0x0023, code:
            if ((r5 ^ 1) != 0) goto L_0x0025;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static char[] decode(String charsetName, byte[] ba, int off, int len) throws UnsupportedEncodingException {
        StringDecoder sd = (StringDecoder) deref(decoder);
        String csn = charsetName == null ? "ISO-8859-1" : charsetName;
        if (sd != null) {
            int i;
            if (csn.equals(sd.requestedCharsetName())) {
                i = 1;
            } else {
                i = csn.equals(sd.charsetName());
            }
        }
        sd = null;
        try {
            Charset cs = lookupCharset(csn);
            if (cs != null) {
                sd = new StringDecoder(cs, csn, null);
            }
        } catch (IllegalCharsetNameException e) {
        }
        if (sd == null) {
            throw new UnsupportedEncodingException(csn);
        }
        set(decoder, sd);
        return sd.decode(ba, off, len);
    }

    static char[] decode(Charset cs, byte[] ba, int off, int len) {
        CharsetDecoder cd = cs.newDecoder();
        char[] ca = new char[scale(len, cd.maxCharsPerByte())];
        if (len == 0) {
            return ca;
        }
        boolean isTrusted = false;
        if (System.getSecurityManager() != null) {
            if (cs.getClass().getClassLoader() == null) {
                isTrusted = true;
            } else {
                isTrusted = false;
            }
            if (!isTrusted) {
                ba = Arrays.copyOfRange(ba, off, off + len);
                off = 0;
            }
        }
        cd.onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE).reset();
        if (cd instanceof ArrayDecoder) {
            return safeTrim(ca, ((ArrayDecoder) cd).decode(ba, off, len, ca), cs, isTrusted);
        }
        ByteBuffer bb = ByteBuffer.wrap(ba, off, len);
        CharBuffer cb = CharBuffer.wrap(ca);
        try {
            CoderResult cr = cd.decode(bb, cb, true);
            if (!cr.isUnderflow()) {
                cr.throwException();
            }
            cr = cd.flush(cb);
            if (!cr.isUnderflow()) {
                cr.throwException();
            }
            return safeTrim(ca, cb.position(), cs, isTrusted);
        } catch (Throwable x) {
            throw new Error(x);
        }
    }

    static char[] decode(byte[] ba, int off, int len) {
        String csn = Charset.defaultCharset().name();
        try {
            return decode(csn, ba, off, len);
        } catch (UnsupportedEncodingException e) {
            warnUnsupportedCharset(csn);
            try {
                return decode("ISO-8859-1", ba, off, len);
            } catch (UnsupportedEncodingException x) {
                MessageUtils.err("ISO-8859-1 charset not available: " + x.toString());
                System.exit(1);
                return null;
            }
        }
    }

    /* JADX WARNING: Missing block: B:8:0x0023, code:
            if ((r5 ^ 1) != 0) goto L_0x0025;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static byte[] encode(String charsetName, char[] ca, int off, int len) throws UnsupportedEncodingException {
        StringEncoder se = (StringEncoder) deref(encoder);
        String csn = charsetName == null ? "ISO-8859-1" : charsetName;
        if (se != null) {
            int i;
            if (csn.equals(se.requestedCharsetName())) {
                i = 1;
            } else {
                i = csn.equals(se.charsetName());
            }
        }
        se = null;
        try {
            Charset cs = lookupCharset(csn);
            if (cs != null) {
                se = new StringEncoder(cs, csn, null);
            }
        } catch (IllegalCharsetNameException e) {
        }
        if (se == null) {
            throw new UnsupportedEncodingException(csn);
        }
        set(encoder, se);
        return se.encode(ca, off, len);
    }

    static byte[] encode(Charset cs, char[] ca, int off, int len) {
        CharsetEncoder ce = cs.newEncoder();
        byte[] ba = new byte[scale(len, ce.maxBytesPerChar())];
        if (len == 0) {
            return ba;
        }
        boolean isTrusted = false;
        if (System.getSecurityManager() != null) {
            if (cs.getClass().getClassLoader() == null) {
                isTrusted = true;
            } else {
                isTrusted = false;
            }
            if (!isTrusted) {
                ca = Arrays.copyOfRange(ca, off, off + len);
                off = 0;
            }
        }
        ce.onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE).reset();
        if (ce instanceof ArrayEncoder) {
            return safeTrim(ba, ((ArrayEncoder) ce).encode(ca, off, len, ba), cs, isTrusted);
        }
        ByteBuffer bb = ByteBuffer.wrap(ba);
        try {
            CoderResult cr = ce.encode(CharBuffer.wrap(ca, off, len).asReadOnlyBuffer(), bb, true);
            if (!cr.isUnderflow()) {
                cr.throwException();
            }
            cr = ce.flush(bb);
            if (!cr.isUnderflow()) {
                cr.throwException();
            }
            return safeTrim(ba, bb.position(), cs, isTrusted);
        } catch (Throwable x) {
            throw new Error(x);
        }
    }

    static byte[] encode(char[] ca, int off, int len) {
        String csn = Charset.defaultCharset().name();
        try {
            return encode(csn, ca, off, len);
        } catch (UnsupportedEncodingException e) {
            warnUnsupportedCharset(csn);
            try {
                return encode("ISO-8859-1", ca, off, len);
            } catch (UnsupportedEncodingException x) {
                MessageUtils.err("ISO-8859-1 charset not available: " + x.toString());
                System.exit(1);
                return null;
            }
        }
    }
}
