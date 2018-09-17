package java.nio.charset;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public abstract class CharsetDecoder {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int ST_CODING = 1;
    private static final int ST_END = 2;
    private static final int ST_FLUSHED = 3;
    private static final int ST_RESET = 0;
    private static String[] stateNames;
    private final float averageCharsPerByte;
    private final Charset charset;
    private CodingErrorAction malformedInputAction;
    private final float maxCharsPerByte;
    private String replacement;
    private int state;
    private CodingErrorAction unmappableCharacterAction;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.nio.charset.CharsetDecoder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.nio.charset.CharsetDecoder.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.nio.charset.CharsetDecoder.<clinit>():void");
    }

    protected abstract CoderResult decodeLoop(ByteBuffer byteBuffer, CharBuffer charBuffer);

    private CharsetDecoder(Charset cs, float averageCharsPerByte, float maxCharsPerByte, String replacement) {
        this.malformedInputAction = CodingErrorAction.REPORT;
        this.unmappableCharacterAction = CodingErrorAction.REPORT;
        this.state = 0;
        this.charset = cs;
        if (averageCharsPerByte <= 0.0f) {
            throw new IllegalArgumentException("Non-positive averageCharsPerByte");
        } else if (maxCharsPerByte <= 0.0f) {
            throw new IllegalArgumentException("Non-positive maxCharsPerByte");
        } else if (Charset.atBugLevel("1.4") || averageCharsPerByte <= maxCharsPerByte) {
            this.replacement = replacement;
            this.averageCharsPerByte = averageCharsPerByte;
            this.maxCharsPerByte = maxCharsPerByte;
        } else {
            throw new IllegalArgumentException("averageCharsPerByte exceeds maxCharsPerByte");
        }
    }

    protected CharsetDecoder(Charset cs, float averageCharsPerByte, float maxCharsPerByte) {
        this(cs, averageCharsPerByte, maxCharsPerByte, "\ufffd");
    }

    public final Charset charset() {
        return this.charset;
    }

    public final String replacement() {
        return this.replacement;
    }

    public final CharsetDecoder replaceWith(String newReplacement) {
        if (newReplacement == null) {
            throw new IllegalArgumentException("Null replacement");
        }
        int len = newReplacement.length();
        if (len == 0) {
            throw new IllegalArgumentException("Empty replacement");
        } else if (((float) len) > this.maxCharsPerByte) {
            throw new IllegalArgumentException("Replacement too long");
        } else {
            this.replacement = newReplacement;
            implReplaceWith(newReplacement);
            return this;
        }
    }

    protected void implReplaceWith(String newReplacement) {
    }

    public CodingErrorAction malformedInputAction() {
        return this.malformedInputAction;
    }

    public final CharsetDecoder onMalformedInput(CodingErrorAction newAction) {
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

    public final CharsetDecoder onUnmappableCharacter(CodingErrorAction newAction) {
        if (newAction == null) {
            throw new IllegalArgumentException("Null action");
        }
        this.unmappableCharacterAction = newAction;
        implOnUnmappableCharacter(newAction);
        return this;
    }

    protected void implOnUnmappableCharacter(CodingErrorAction newAction) {
    }

    public final float averageCharsPerByte() {
        return this.averageCharsPerByte;
    }

    public final float maxCharsPerByte() {
        return this.maxCharsPerByte;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final CoderResult decode(ByteBuffer in, CharBuffer out, boolean endOfInput) {
        CoderResult cr;
        int newState = endOfInput ? ST_END : ST_CODING;
        if (!(this.state == 0 || this.state == ST_CODING || (endOfInput && this.state == ST_END))) {
            throwIllegalStateException(this.state, newState);
        }
        this.state = newState;
        while (true) {
            try {
                cr = decodeLoop(in, out);
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
                    if (out.remaining() < this.replacement.length()) {
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

    public final CoderResult flush(CharBuffer out) {
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

    protected CoderResult implFlush(CharBuffer out) {
        return CoderResult.UNDERFLOW;
    }

    public final CharsetDecoder reset() {
        implReset();
        this.state = 0;
        return this;
    }

    protected void implReset() {
    }

    public final CharBuffer decode(ByteBuffer in) throws CharacterCodingException {
        int n = (int) (((float) in.remaining()) * averageCharsPerByte());
        CharBuffer out = CharBuffer.allocate(n);
        if (n == 0 && in.remaining() == 0) {
            return out;
        }
        reset();
        while (true) {
            CoderResult cr = in.hasRemaining() ? decode(in, out, true) : CoderResult.UNDERFLOW;
            if (cr.isUnderflow()) {
                cr = flush(out);
            }
            if (cr.isUnderflow()) {
                out.flip();
                return out;
            } else if (cr.isOverflow()) {
                n = (n * ST_END) + ST_CODING;
                CharBuffer o = CharBuffer.allocate(n);
                out.flip();
                o.put(out);
                out = o;
            } else {
                cr.throwException();
            }
        }
    }

    public boolean isAutoDetecting() {
        return -assertionsDisabled;
    }

    public boolean isCharsetDetected() {
        throw new UnsupportedOperationException();
    }

    public Charset detectedCharset() {
        throw new UnsupportedOperationException();
    }

    private void throwIllegalStateException(int from, int to) {
        throw new IllegalStateException("Current state = " + stateNames[from] + ", new state = " + stateNames[to]);
    }
}
