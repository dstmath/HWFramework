package java.nio.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import libcore.icu.ICU;
import libcore.icu.NativeConverter;
import libcore.util.EmptyArray;

final class CharsetDecoderICU extends CharsetDecoder {
    private static final int INPUT_OFFSET = 0;
    private static final int INVALID_BYTE_COUNT = 2;
    private static final int MAX_CHARS_PER_BYTE = 2;
    private static final int OUTPUT_OFFSET = 1;
    private byte[] allocatedInput = null;
    private char[] allocatedOutput = null;
    private long converterHandle = 0;
    private final int[] data = new int[3];
    private int inEnd;
    private byte[] input = null;
    private int outEnd;
    private char[] output = null;

    public static CharsetDecoderICU newInstance(Charset cs, String icuCanonicalName) {
        long address = 0;
        try {
            address = NativeConverter.openConverter(icuCanonicalName);
            CharsetDecoderICU result = new CharsetDecoderICU(cs, NativeConverter.getAveCharsPerByte(address), address);
            NativeConverter.registerConverter(result, address);
            result.updateCallback();
            return result;
        } catch (Throwable t) {
            if (address != 0) {
                NativeConverter.closeConverter(address);
            }
            throw t;
        }
    }

    private CharsetDecoderICU(Charset cs, float averageCharsPerByte, long address) {
        super(cs, averageCharsPerByte, 2.0f);
        this.converterHandle = address;
    }

    /* access modifiers changed from: protected */
    public void implReplaceWith(String newReplacement) {
        updateCallback();
    }

    /* access modifiers changed from: protected */
    public final void implOnMalformedInput(CodingErrorAction newAction) {
        updateCallback();
    }

    /* access modifiers changed from: protected */
    public final void implOnUnmappableCharacter(CodingErrorAction newAction) {
        updateCallback();
    }

    private void updateCallback() {
        NativeConverter.setCallbackDecode(this.converterHandle, this);
    }

    /* access modifiers changed from: protected */
    public void implReset() {
        NativeConverter.resetByteToChar(this.converterHandle);
        this.data[0] = 0;
        this.data[1] = 0;
        this.data[2] = 0;
        this.output = null;
        this.input = null;
        this.allocatedInput = null;
        this.allocatedOutput = null;
        this.inEnd = 0;
        this.outEnd = 0;
    }

    /* access modifiers changed from: protected */
    public final CoderResult implFlush(CharBuffer out) {
        try {
            this.input = EmptyArray.BYTE;
            this.inEnd = 0;
            this.data[0] = 0;
            this.data[1] = getArray(out);
            this.data[2] = 0;
            int error = NativeConverter.decode(this.converterHandle, this.input, this.inEnd, this.output, this.outEnd, this.data, true);
            if (ICU.U_FAILURE(error)) {
                if (error == 15) {
                    return CoderResult.OVERFLOW;
                }
                if (error == 11) {
                    if (this.data[2] > 0) {
                        CoderResult malformedForLength = CoderResult.malformedForLength(this.data[2]);
                        setPosition(out);
                        implReset();
                        return malformedForLength;
                    }
                }
            }
            CoderResult coderResult = CoderResult.UNDERFLOW;
            setPosition(out);
            implReset();
            return coderResult;
        } finally {
            setPosition(out);
            implReset();
        }
    }

    /* access modifiers changed from: protected */
    public CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
        if (!in.hasRemaining()) {
            return CoderResult.UNDERFLOW;
        }
        this.data[0] = getArray(in);
        this.data[1] = getArray(out);
        try {
            int error = NativeConverter.decode(this.converterHandle, this.input, this.inEnd, this.output, this.outEnd, this.data, false);
            if (!ICU.U_FAILURE(error)) {
                CoderResult coderResult = CoderResult.UNDERFLOW;
                setPosition(in);
                setPosition(out);
                return coderResult;
            } else if (error == 15) {
                return CoderResult.OVERFLOW;
            } else {
                if (error == 10) {
                    CoderResult unmappableForLength = CoderResult.unmappableForLength(this.data[2]);
                    setPosition(in);
                    setPosition(out);
                    return unmappableForLength;
                } else if (error == 12) {
                    CoderResult malformedForLength = CoderResult.malformedForLength(this.data[2]);
                    setPosition(in);
                    setPosition(out);
                    return malformedForLength;
                } else {
                    throw new AssertionError(error);
                }
            }
        } finally {
            setPosition(in);
            setPosition(out);
        }
    }

    private int getArray(CharBuffer out) {
        if (out.hasArray()) {
            this.output = out.array();
            this.outEnd = out.arrayOffset() + out.limit();
            return out.arrayOffset() + out.position();
        }
        this.outEnd = out.remaining();
        if (this.allocatedOutput == null || this.outEnd > this.allocatedOutput.length) {
            this.allocatedOutput = new char[this.outEnd];
        }
        this.output = this.allocatedOutput;
        return 0;
    }

    private int getArray(ByteBuffer in) {
        if (in.hasArray()) {
            this.input = in.array();
            this.inEnd = in.arrayOffset() + in.limit();
            return in.arrayOffset() + in.position();
        }
        this.inEnd = in.remaining();
        if (this.allocatedInput == null || this.inEnd > this.allocatedInput.length) {
            this.allocatedInput = new byte[this.inEnd];
        }
        int pos = in.position();
        in.get(this.allocatedInput, 0, this.inEnd);
        in.position(pos);
        this.input = this.allocatedInput;
        return 0;
    }

    private void setPosition(CharBuffer out) {
        if (out.hasArray()) {
            out.position(out.position() + this.data[1]);
        } else {
            out.put(this.output, 0, this.data[1]);
        }
        this.output = null;
    }

    private void setPosition(ByteBuffer in) {
        in.position(in.position() + this.data[0]);
        this.input = null;
    }
}
