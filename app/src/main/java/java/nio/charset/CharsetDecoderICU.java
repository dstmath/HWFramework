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
    private byte[] allocatedInput;
    private char[] allocatedOutput;
    private long converterHandle;
    private final int[] data;
    private int inEnd;
    private byte[] input;
    private int outEnd;
    private char[] output;

    public static CharsetDecoderICU newInstance(Charset cs, String icuCanonicalName) {
        long address = 0;
        try {
            address = NativeConverter.openConverter(icuCanonicalName);
            CharsetDecoderICU result = new CharsetDecoderICU(cs, NativeConverter.getAveCharsPerByte(address), address);
            address = 0;
            result.updateCallback();
            return result;
        } catch (Throwable th) {
            if (address != 0) {
                NativeConverter.closeConverter(address);
            }
        }
    }

    private CharsetDecoderICU(Charset cs, float averageCharsPerByte, long address) {
        super(cs, averageCharsPerByte, 2.0f);
        this.data = new int[3];
        this.converterHandle = 0;
        this.input = null;
        this.output = null;
        this.allocatedInput = null;
        this.allocatedOutput = null;
        this.converterHandle = address;
        NativeConverter.registerConverter(this, this.converterHandle);
    }

    protected void implReplaceWith(String newReplacement) {
        updateCallback();
    }

    protected final void implOnMalformedInput(CodingErrorAction newAction) {
        updateCallback();
    }

    protected final void implOnUnmappableCharacter(CodingErrorAction newAction) {
        updateCallback();
    }

    private void updateCallback() {
        NativeConverter.setCallbackDecode(this.converterHandle, this);
    }

    protected void implReset() {
        NativeConverter.resetByteToChar(this.converterHandle);
        this.data[INPUT_OFFSET] = INPUT_OFFSET;
        this.data[OUTPUT_OFFSET] = INPUT_OFFSET;
        this.data[MAX_CHARS_PER_BYTE] = INPUT_OFFSET;
        this.output = null;
        this.input = null;
        this.allocatedInput = null;
        this.allocatedOutput = null;
        this.inEnd = INPUT_OFFSET;
        this.outEnd = INPUT_OFFSET;
    }

    protected final CoderResult implFlush(CharBuffer out) {
        try {
            CoderResult coderResult;
            this.input = EmptyArray.BYTE;
            this.inEnd = INPUT_OFFSET;
            this.data[INPUT_OFFSET] = INPUT_OFFSET;
            this.data[OUTPUT_OFFSET] = getArray(out);
            this.data[MAX_CHARS_PER_BYTE] = INPUT_OFFSET;
            int error = NativeConverter.decode(this.converterHandle, this.input, this.inEnd, this.output, this.outEnd, this.data, true);
            if (ICU.U_FAILURE(error)) {
                if (error == 15) {
                    coderResult = CoderResult.OVERFLOW;
                    return coderResult;
                } else if (error == 11) {
                    if (this.data[MAX_CHARS_PER_BYTE] > 0) {
                        coderResult = CoderResult.malformedForLength(this.data[MAX_CHARS_PER_BYTE]);
                        setPosition(out);
                        implReset();
                        return coderResult;
                    }
                }
            }
            coderResult = CoderResult.UNDERFLOW;
            setPosition(out);
            implReset();
            return coderResult;
        } finally {
            setPosition(out);
            implReset();
        }
    }

    protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
        if (!in.hasRemaining()) {
            return CoderResult.UNDERFLOW;
        }
        this.data[INPUT_OFFSET] = getArray(in);
        this.data[OUTPUT_OFFSET] = getArray(out);
        try {
            int error = NativeConverter.decode(this.converterHandle, this.input, this.inEnd, this.output, this.outEnd, this.data, false);
            CoderResult coderResult;
            if (!ICU.U_FAILURE(error)) {
                coderResult = CoderResult.UNDERFLOW;
                setPosition(in);
                setPosition(out);
                return coderResult;
            } else if (error == 15) {
                coderResult = CoderResult.OVERFLOW;
                return coderResult;
            } else if (error == 10) {
                coderResult = CoderResult.unmappableForLength(this.data[MAX_CHARS_PER_BYTE]);
                setPosition(in);
                setPosition(out);
                return coderResult;
            } else if (error == 12) {
                coderResult = CoderResult.malformedForLength(this.data[MAX_CHARS_PER_BYTE]);
                setPosition(in);
                setPosition(out);
                return coderResult;
            } else {
                throw new AssertionError(error);
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
        return INPUT_OFFSET;
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
        in.get(this.allocatedInput, INPUT_OFFSET, this.inEnd);
        in.position(pos);
        this.input = this.allocatedInput;
        return INPUT_OFFSET;
    }

    private void setPosition(CharBuffer out) {
        if (out.hasArray()) {
            out.position(out.position() + this.data[OUTPUT_OFFSET]);
        } else {
            out.put(this.output, INPUT_OFFSET, this.data[OUTPUT_OFFSET]);
        }
        this.output = null;
    }

    private void setPosition(ByteBuffer in) {
        in.position(in.position() + this.data[INPUT_OFFSET]);
        this.input = null;
    }
}
