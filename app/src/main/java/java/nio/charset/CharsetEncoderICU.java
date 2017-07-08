package java.nio.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Map;
import libcore.icu.ICU;
import libcore.icu.NativeConverter;
import libcore.util.EmptyArray;

final class CharsetEncoderICU extends CharsetEncoder {
    private static final Map<String, byte[]> DEFAULT_REPLACEMENTS = null;
    private static final int INPUT_OFFSET = 0;
    private static final int INVALID_CHAR_COUNT = 2;
    private static final int OUTPUT_OFFSET = 1;
    private char[] allocatedInput;
    private byte[] allocatedOutput;
    private final long converterHandle;
    private int[] data;
    private int inEnd;
    private char[] input;
    private int outEnd;
    private byte[] output;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.nio.charset.CharsetEncoderICU.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.nio.charset.CharsetEncoderICU.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.nio.charset.CharsetEncoderICU.<clinit>():void");
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
        this.data = new int[3];
        this.input = null;
        this.output = null;
        this.allocatedInput = null;
        this.allocatedOutput = null;
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
        this.data[INPUT_OFFSET] = INPUT_OFFSET;
        this.data[OUTPUT_OFFSET] = INPUT_OFFSET;
        this.data[INVALID_CHAR_COUNT] = INPUT_OFFSET;
        this.output = null;
        this.input = null;
        this.allocatedInput = null;
        this.allocatedOutput = null;
        this.inEnd = INPUT_OFFSET;
        this.outEnd = INPUT_OFFSET;
    }

    protected CoderResult implFlush(ByteBuffer out) {
        try {
            CoderResult coderResult;
            this.input = EmptyArray.CHAR;
            this.inEnd = INPUT_OFFSET;
            this.data[INPUT_OFFSET] = INPUT_OFFSET;
            this.data[OUTPUT_OFFSET] = getArray(out);
            this.data[INVALID_CHAR_COUNT] = INPUT_OFFSET;
            int error = NativeConverter.encode(this.converterHandle, this.input, this.inEnd, this.output, this.outEnd, this.data, true);
            if (ICU.U_FAILURE(error)) {
                if (error == 15) {
                    coderResult = CoderResult.OVERFLOW;
                    return coderResult;
                } else if (error == 11) {
                    if (this.data[INVALID_CHAR_COUNT] > 0) {
                        coderResult = CoderResult.malformedForLength(this.data[INVALID_CHAR_COUNT]);
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
        this.data[INPUT_OFFSET] = getArray(in);
        this.data[OUTPUT_OFFSET] = getArray(out);
        this.data[INVALID_CHAR_COUNT] = INPUT_OFFSET;
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
                coderResult = CoderResult.unmappableForLength(this.data[INVALID_CHAR_COUNT]);
                setPosition(in);
                setPosition(out);
                return coderResult;
            } else if (error == 12) {
                coderResult = CoderResult.malformedForLength(this.data[INVALID_CHAR_COUNT]);
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
        return INPUT_OFFSET;
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
        in.get(this.allocatedInput, INPUT_OFFSET, this.inEnd);
        in.position(pos);
        this.input = this.allocatedInput;
        return INPUT_OFFSET;
    }

    private void setPosition(ByteBuffer out) {
        if (out.hasArray()) {
            out.position(this.data[OUTPUT_OFFSET] - out.arrayOffset());
        } else {
            out.put(this.output, INPUT_OFFSET, this.data[OUTPUT_OFFSET]);
        }
        this.output = null;
    }

    private void setPosition(CharBuffer in) {
        int position = (in.position() + this.data[INPUT_OFFSET]) - this.data[INVALID_CHAR_COUNT];
        if (position < 0) {
            position = INPUT_OFFSET;
        }
        in.position(position);
        this.input = null;
    }
}
