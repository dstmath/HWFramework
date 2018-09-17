package java.nio.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;
import libcore.icu.ICU;
import libcore.icu.NativeConverter;
import libcore.util.EmptyArray;

final class CharsetEncoderICU extends CharsetEncoder {
    private static final Map<String, byte[]> DEFAULT_REPLACEMENTS = new HashMap();
    private static final int INPUT_OFFSET = 0;
    private static final int INVALID_CHAR_COUNT = 2;
    private static final int OUTPUT_OFFSET = 1;
    private char[] allocatedInput = null;
    private byte[] allocatedOutput = null;
    private final long converterHandle;
    private int[] data = new int[3];
    private int inEnd;
    private char[] input = null;
    private int outEnd;
    private byte[] output = null;

    static {
        byte[] questionMark = new byte[]{(byte) 63};
        DEFAULT_REPLACEMENTS.put("UTF-8", questionMark);
        DEFAULT_REPLACEMENTS.put("ISO-8859-1", questionMark);
        DEFAULT_REPLACEMENTS.put("US-ASCII", questionMark);
    }

    public static CharsetEncoderICU newInstance(Charset cs, String icuCanonicalName) {
        long address = 0;
        try {
            address = NativeConverter.openConverter(icuCanonicalName);
            return new CharsetEncoderICU(cs, NativeConverter.getAveBytesPerChar(address), (float) NativeConverter.getMaxBytesPerChar(address), makeReplacement(icuCanonicalName, address), address);
        } catch (Throwable th) {
            if (address != 0) {
                NativeConverter.closeConverter(address);
            }
        }
    }

    private static byte[] makeReplacement(String icuCanonicalName, long address) {
        byte[] replacement = (byte[]) DEFAULT_REPLACEMENTS.get(icuCanonicalName);
        if (replacement != null) {
            return (byte[]) replacement.clone();
        }
        return NativeConverter.getSubstitutionBytes(address);
    }

    private CharsetEncoderICU(Charset cs, float averageBytesPerChar, float maxBytesPerChar, byte[] replacement, long address) {
        super(cs, averageBytesPerChar, maxBytesPerChar, replacement, true);
        this.converterHandle = address;
        NativeConverter.registerConverter(this, this.converterHandle);
        updateCallback();
    }

    protected void implReplaceWith(byte[] newReplacement) {
        updateCallback();
    }

    protected void implOnMalformedInput(CodingErrorAction newAction) {
        updateCallback();
    }

    protected void implOnUnmappableCharacter(CodingErrorAction newAction) {
        updateCallback();
    }

    private void updateCallback() {
        NativeConverter.setCallbackEncode(this.converterHandle, this);
    }

    protected void implReset() {
        NativeConverter.resetCharToByte(this.converterHandle);
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

    protected CoderResult implFlush(ByteBuffer out) {
        try {
            CoderResult coderResult;
            this.input = EmptyArray.CHAR;
            this.inEnd = 0;
            this.data[0] = 0;
            this.data[1] = getArray(out);
            this.data[2] = 0;
            int error = NativeConverter.encode(this.converterHandle, this.input, this.inEnd, this.output, this.outEnd, this.data, true);
            if (ICU.U_FAILURE(error)) {
                if (error == 15) {
                    coderResult = CoderResult.OVERFLOW;
                    return coderResult;
                } else if (error == 11) {
                    if (this.data[2] > 0) {
                        coderResult = CoderResult.malformedForLength(this.data[2]);
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

    protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
        if (!in.hasRemaining()) {
            return CoderResult.UNDERFLOW;
        }
        this.data[0] = getArray(in);
        this.data[1] = getArray(out);
        this.data[2] = 0;
        try {
            int error = NativeConverter.encode(this.converterHandle, this.input, this.inEnd, this.output, this.outEnd, this.data, false);
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
                coderResult = CoderResult.unmappableForLength(this.data[2]);
                setPosition(in);
                setPosition(out);
                return coderResult;
            } else if (error == 12) {
                coderResult = CoderResult.malformedForLength(this.data[2]);
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

    private int getArray(ByteBuffer out) {
        if (out.hasArray()) {
            this.output = out.array();
            this.outEnd = out.arrayOffset() + out.limit();
            return out.arrayOffset() + out.position();
        }
        this.outEnd = out.remaining();
        if (this.allocatedOutput == null || this.outEnd > this.allocatedOutput.length) {
            this.allocatedOutput = new byte[this.outEnd];
        }
        this.output = this.allocatedOutput;
        return 0;
    }

    private int getArray(CharBuffer in) {
        if (in.hasArray()) {
            this.input = in.array();
            this.inEnd = in.arrayOffset() + in.limit();
            return in.arrayOffset() + in.position();
        }
        this.inEnd = in.remaining();
        if (this.allocatedInput == null || this.inEnd > this.allocatedInput.length) {
            this.allocatedInput = new char[this.inEnd];
        }
        int pos = in.position();
        in.get(this.allocatedInput, 0, this.inEnd);
        in.position(pos);
        this.input = this.allocatedInput;
        return 0;
    }

    private void setPosition(ByteBuffer out) {
        if (out.hasArray()) {
            out.position(this.data[1] - out.arrayOffset());
        } else {
            out.put(this.output, 0, this.data[1]);
        }
        this.output = null;
    }

    private void setPosition(CharBuffer in) {
        int position = (in.position() + this.data[0]) - this.data[2];
        if (position < 0) {
            position = 0;
        }
        in.position(position);
        this.input = null;
    }
}
