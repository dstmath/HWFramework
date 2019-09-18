package java.nio.charset;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public abstract class CharsetDecoder {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int ST_CODING = 1;
    private static final int ST_END = 2;
    private static final int ST_FLUSHED = 3;
    private static final int ST_RESET = 0;
    private static String[] stateNames = {"RESET", "CODING", "CODING_END", "FLUSHED"};
    private final float averageCharsPerByte;
    private final Charset charset;
    private CodingErrorAction malformedInputAction;
    private final float maxCharsPerByte;
    private String replacement;
    private int state;
    private CodingErrorAction unmappableCharacterAction;

    /* access modifiers changed from: protected */
    public abstract CoderResult decodeLoop(ByteBuffer byteBuffer, CharBuffer charBuffer);

    private CharsetDecoder(Charset cs, float averageCharsPerByte2, float maxCharsPerByte2, String replacement2) {
        this.malformedInputAction = CodingErrorAction.REPORT;
        this.unmappableCharacterAction = CodingErrorAction.REPORT;
        this.state = 0;
        this.charset = cs;
        if (averageCharsPerByte2 <= 0.0f) {
            throw new IllegalArgumentException("Non-positive averageCharsPerByte");
        } else if (maxCharsPerByte2 <= 0.0f) {
            throw new IllegalArgumentException("Non-positive maxCharsPerByte");
        } else if (Charset.atBugLevel("1.4") || averageCharsPerByte2 <= maxCharsPerByte2) {
            this.replacement = replacement2;
            this.averageCharsPerByte = averageCharsPerByte2;
            this.maxCharsPerByte = maxCharsPerByte2;
        } else {
            throw new IllegalArgumentException("averageCharsPerByte exceeds maxCharsPerByte");
        }
    }

    protected CharsetDecoder(Charset cs, float averageCharsPerByte2, float maxCharsPerByte2) {
        this(cs, averageCharsPerByte2, maxCharsPerByte2, "�");
    }

    public final Charset charset() {
        return this.charset;
    }

    public final String replacement() {
        return this.replacement;
    }

    public final CharsetDecoder replaceWith(String newReplacement) {
        if (newReplacement != null) {
            int len = newReplacement.length();
            if (len == 0) {
                throw new IllegalArgumentException("Empty replacement");
            } else if (((float) len) <= this.maxCharsPerByte) {
                this.replacement = newReplacement;
                implReplaceWith(this.replacement);
                return this;
            } else {
                throw new IllegalArgumentException("Replacement too long");
            }
        } else {
            throw new IllegalArgumentException("Null replacement");
        }
    }

    /* access modifiers changed from: protected */
    public void implReplaceWith(String newReplacement) {
    }

    public CodingErrorAction malformedInputAction() {
        return this.malformedInputAction;
    }

    public final CharsetDecoder onMalformedInput(CodingErrorAction newAction) {
        if (newAction != null) {
            this.malformedInputAction = newAction;
            implOnMalformedInput(newAction);
            return this;
        }
        throw new IllegalArgumentException("Null action");
    }

    /* access modifiers changed from: protected */
    public void implOnMalformedInput(CodingErrorAction newAction) {
    }

    public CodingErrorAction unmappableCharacterAction() {
        return this.unmappableCharacterAction;
    }

    public final CharsetDecoder onUnmappableCharacter(CodingErrorAction newAction) {
        if (newAction != null) {
            this.unmappableCharacterAction = newAction;
            implOnUnmappableCharacter(newAction);
            return this;
        }
        throw new IllegalArgumentException("Null action");
    }

    /* access modifiers changed from: protected */
    public void implOnUnmappableCharacter(CodingErrorAction newAction) {
    }

    public final float averageCharsPerByte() {
        return this.averageCharsPerByte;
    }

    public final float maxCharsPerByte() {
        return this.maxCharsPerByte;
    }

    public final CoderResult decode(ByteBuffer in, CharBuffer out, boolean endOfInput) {
        CoderResult cr;
        int newState = endOfInput ? 2 : 1;
        if (!(this.state == 0 || this.state == 1 || (endOfInput && this.state == 2))) {
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
                if (cr.isMalformed()) {
                    action = this.malformedInputAction;
                } else if (cr.isUnmappable()) {
                    action = this.unmappableCharacterAction;
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
        if (this.state == 2) {
            CoderResult cr = implFlush(out);
            if (cr.isUnderflow()) {
                this.state = 3;
            }
            return cr;
        }
        if (this.state != 3) {
            throwIllegalStateException(this.state, 3);
        }
        return CoderResult.UNDERFLOW;
    }

    /* access modifiers changed from: protected */
    public CoderResult implFlush(CharBuffer out) {
        return CoderResult.UNDERFLOW;
    }

    public final CharsetDecoder reset() {
        implReset();
        this.state = 0;
        return this;
    }

    /* access modifiers changed from: protected */
    public void implReset() {
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
                n = (2 * n) + 1;
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
        return $assertionsDisabled;
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
