package java.nio.charset;

import java.lang.ref.WeakReference;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public abstract class CharsetEncoder {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int ST_CODING = 1;
    private static final int ST_END = 2;
    private static final int ST_FLUSHED = 3;
    private static final int ST_RESET = 0;
    private static String[] stateNames;
    private final float averageBytesPerChar;
    private WeakReference<CharsetDecoder> cachedDecoder;
    private final Charset charset;
    private CodingErrorAction malformedInputAction;
    private final float maxBytesPerChar;
    private byte[] replacement;
    private int state;
    private CodingErrorAction unmappableCharacterAction;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.nio.charset.CharsetEncoder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.nio.charset.CharsetEncoder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.nio.charset.CharsetEncoder.<clinit>():void");
    }

    protected abstract CoderResult encodeLoop(CharBuffer charBuffer, ByteBuffer byteBuffer);

    protected CharsetEncoder(Charset cs, float averageBytesPerChar, float maxBytesPerChar, byte[] replacement) {
        this(cs, averageBytesPerChar, maxBytesPerChar, replacement, -assertionsDisabled);
    }

    CharsetEncoder(Charset cs, float averageBytesPerChar, float maxBytesPerChar, byte[] replacement, boolean trusted) {
        this.malformedInputAction = CodingErrorAction.REPORT;
        this.unmappableCharacterAction = CodingErrorAction.REPORT;
        this.state = 0;
        this.cachedDecoder = null;
        this.charset = cs;
        if (averageBytesPerChar <= 0.0f) {
            throw new IllegalArgumentException("Non-positive averageBytesPerChar");
        } else if (maxBytesPerChar <= 0.0f) {
            throw new IllegalArgumentException("Non-positive maxBytesPerChar");
        } else if (Charset.atBugLevel("1.4") || averageBytesPerChar <= maxBytesPerChar) {
            this.replacement = replacement;
            this.averageBytesPerChar = averageBytesPerChar;
            this.maxBytesPerChar = maxBytesPerChar;
            if (!trusted) {
                replaceWith(replacement);
            }
        } else {
            throw new IllegalArgumentException("averageBytesPerChar exceeds maxBytesPerChar");
        }
    }

    protected CharsetEncoder(Charset cs, float averageBytesPerChar, float maxBytesPerChar) {
        byte[] bArr = new byte[ST_CODING];
        bArr[0] = (byte) 63;
        this(cs, averageBytesPerChar, maxBytesPerChar, bArr);
    }

    public final Charset charset() {
        return this.charset;
    }

    public final byte[] replacement() {
        return this.replacement;
    }

    public final CharsetEncoder replaceWith(byte[] newReplacement) {
        if (newReplacement == null) {
            throw new IllegalArgumentException("Null replacement");
        }
        int len = newReplacement.length;
        if (len == 0) {
            throw new IllegalArgumentException("Empty replacement");
        } else if (((float) len) > this.maxBytesPerChar) {
            throw new IllegalArgumentException("Replacement too long");
        } else if (isLegalReplacement(newReplacement)) {
            this.replacement = newReplacement;
            implReplaceWith(newReplacement);
            return this;
        } else {
            throw new IllegalArgumentException("Illegal replacement");
        }
    }

    protected void implReplaceWith(byte[] newReplacement) {
    }

    public boolean isLegalReplacement(byte[] repl) {
        CharsetDecoder dec;
        ByteBuffer bb;
        WeakReference<CharsetDecoder> wr = this.cachedDecoder;
        if (wr != null) {
            dec = (CharsetDecoder) wr.get();
            if (dec != null) {
                dec.reset();
                bb = ByteBuffer.wrap(repl);
                if (dec.decode(bb, CharBuffer.allocate((int) (((float) bb.remaining()) * dec.maxCharsPerByte())), true).isError()) {
                    return true;
                }
                return -assertionsDisabled;
            }
        }
        dec = charset().newDecoder();
        dec.onMalformedInput(CodingErrorAction.REPORT);
        dec.onUnmappableCharacter(CodingErrorAction.REPORT);
        this.cachedDecoder = new WeakReference(dec);
        bb = ByteBuffer.wrap(repl);
        if (dec.decode(bb, CharBuffer.allocate((int) (((float) bb.remaining()) * dec.maxCharsPerByte())), true).isError()) {
            return true;
        }
        return -assertionsDisabled;
    }

    public CodingErrorAction malformedInputAction() {
        return this.malformedInputAction;
    }

    public final CharsetEncoder onMalformedInput(CodingErrorAction newAction) {
        if (newAction == null) {
            throw new IllegalArgumentException("Null action");
        }
        this.malformedInputAction = newAction;
        implOnMalformedInput(newAction);
        return this;
    }

    protected void implOnMalformedInput(CodingErrorAction newAction) {
    }

    public CodingErrorAction unmappableCharacterAction() {
        return this.unmappableCharacterAction;
    }

    public final CharsetEncoder onUnmappableCharacter(CodingErrorAction newAction) {
        if (newAction == null) {
            throw new IllegalArgumentException("Null action");
        }
        this.unmappableCharacterAction = newAction;
        implOnUnmappableCharacter(newAction);
        return this;
    }

    protected void implOnUnmappableCharacter(CodingErrorAction newAction) {
    }

    public final float averageBytesPerChar() {
        return this.averageBytesPerChar;
    }

    public final float maxBytesPerChar() {
        return this.maxBytesPerChar;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final CoderResult encode(CharBuffer in, ByteBuffer out, boolean endOfInput) {
        CoderResult cr;
        int newState = endOfInput ? ST_END : ST_CODING;
        if (!(this.state == 0 || this.state == ST_CODING || (endOfInput && this.state == ST_END))) {
            throwIllegalStateException(this.state, newState);
        }
        this.state = newState;
        while (true) {
            try {
                cr = encodeLoop(in, out);
                if (cr.isOverflow()) {
                    return cr;
                }
                if (cr.isUnderflow()) {
                    if (!endOfInput || !in.hasRemaining()) {
                        return cr;
                    }
                    cr = CoderResult.malformedForLength(in.remaining());
                }
                CodingErrorAction action = null;
                if (!cr.isMalformed()) {
                    if (!cr.isUnmappable()) {
                        if (!-assertionsDisabled) {
                            break;
                        }
                    }
                    action = this.unmappableCharacterAction;
                } else {
                    action = this.malformedInputAction;
                }
                if (action == CodingErrorAction.REPORT) {
                    return cr;
                }
                if (action == CodingErrorAction.REPLACE) {
                    if (out.remaining() < this.replacement.length) {
                        return CoderResult.OVERFLOW;
                    }
                    out.put(this.replacement);
                }
                if (action == CodingErrorAction.IGNORE || action == CodingErrorAction.REPLACE) {
                    in.position(in.position() + cr.length());
                } else if (!-assertionsDisabled) {
                    break;
                }
            } catch (BufferUnderflowException x) {
                throw new CoderMalfunctionError(x);
            } catch (BufferOverflowException x2) {
                throw new CoderMalfunctionError(x2);
            }
        }
        return cr;
    }

    public final CoderResult flush(ByteBuffer out) {
        if (this.state == ST_END) {
            CoderResult cr = implFlush(out);
            if (cr.isUnderflow()) {
                this.state = ST_FLUSHED;
            }
            return cr;
        }
        if (this.state != ST_FLUSHED) {
            throwIllegalStateException(this.state, ST_FLUSHED);
        }
        return CoderResult.UNDERFLOW;
    }

    protected CoderResult implFlush(ByteBuffer out) {
        return CoderResult.UNDERFLOW;
    }

    public final CharsetEncoder reset() {
        implReset();
        this.state = 0;
        return this;
    }

    protected void implReset() {
    }

    public final ByteBuffer encode(CharBuffer in) throws CharacterCodingException {
        int n = (int) (((float) in.remaining()) * averageBytesPerChar());
        ByteBuffer out = ByteBuffer.allocate(n);
        if (n == 0 && in.remaining() == 0) {
            return out;
        }
        reset();
        while (true) {
            CoderResult cr = in.hasRemaining() ? encode(in, out, true) : CoderResult.UNDERFLOW;
            if (cr.isUnderflow()) {
                cr = flush(out);
            }
            if (cr.isUnderflow()) {
                out.flip();
                return out;
            } else if (cr.isOverflow()) {
                n = (n * ST_END) + ST_CODING;
                ByteBuffer o = ByteBuffer.allocate(n);
                out.flip();
                o.put(out);
                out = o;
            } else {
                cr.throwException();
            }
        }
    }

    private boolean canEncode(CharBuffer cb) {
        if (!cb.hasRemaining()) {
            return true;
        }
        boolean z = this.state;
        if (z == -assertionsDisabled) {
            reset();
        } else {
            z = this.state;
            if (z) {
                z = this.state;
                throwIllegalStateException(z, ST_CODING);
            }
        }
        CodingErrorAction ma = malformedInputAction();
        CodingErrorAction ua = unmappableCharacterAction();
        try {
            onMalformedInput(CodingErrorAction.REPORT);
            onUnmappableCharacter(CodingErrorAction.REPORT);
            z = encode(cb).hasRemaining();
            return z;
        } catch (CharacterCodingException e) {
            return -assertionsDisabled;
        } finally {
            onMalformedInput(ma);
            onUnmappableCharacter(ua);
            reset();
        }
    }

    public boolean canEncode(char c) {
        CharBuffer cb = CharBuffer.allocate(ST_CODING);
        cb.put(c);
        cb.flip();
        return canEncode(cb);
    }

    public boolean canEncode(CharSequence cs) {
        CharBuffer cb;
        if (cs instanceof CharBuffer) {
            cb = ((CharBuffer) cs).duplicate();
        } else {
            cb = CharBuffer.wrap(cs);
        }
        return canEncode(cb);
    }

    private void throwIllegalStateException(int from, int to) {
        throw new IllegalStateException("Current state = " + stateNames[from] + ", new state = " + stateNames[to]);
    }
}
