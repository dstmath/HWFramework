package libcore.io;

import dalvik.bytecode.Opcodes;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

public final class Base64 {
    private static final byte[] BASE_64_ALPHABET = null;
    private static final byte END_OF_INPUT = (byte) -3;
    private static final int FIRST_OUTPUT_BYTE_MASK = 16515072;
    private static final int FOURTH_OUTPUT_BYTE_MASK = 63;
    private static final byte PAD_AS_BYTE = (byte) -1;
    private static final int SECOND_OUTPUT_BYTE_MASK = 258048;
    private static final int THIRD_OUTPUT_BYTE_MASK = 4032;
    private static final byte WHITESPACE_AS_BYTE = (byte) -2;

    private static class InvalidBase64ByteException extends Exception {
        private InvalidBase64ByteException() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: libcore.io.Base64.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: libcore.io.Base64.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: libcore.io.Base64.<clinit>():void");
    }

    private static byte[] initializeBase64Alphabet() {
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes(StandardCharsets.US_ASCII);
    }

    private Base64() {
    }

    public static String encode(byte[] in) {
        int len = in.length;
        byte[] output = new byte[computeEncodingOutputLen(len)];
        int i = 0;
        int outputIndex = 0;
        while (i < len) {
            int outputIndex2;
            int byteTripletAsInt = in[i] & Opcodes.OP_CONST_CLASS_JUMBO;
            if (i + 1 < len) {
                byteTripletAsInt = (byteTripletAsInt << 8) | (in[i + 1] & Opcodes.OP_CONST_CLASS_JUMBO);
                if (i + 2 < len) {
                    byteTripletAsInt = (byteTripletAsInt << 8) | (in[i + 2] & Opcodes.OP_CONST_CLASS_JUMBO);
                } else {
                    byteTripletAsInt <<= 2;
                }
            } else {
                byteTripletAsInt <<= 4;
            }
            if (i + 2 < len) {
                outputIndex2 = outputIndex + 1;
                output[outputIndex] = BASE_64_ALPHABET[(FIRST_OUTPUT_BYTE_MASK & byteTripletAsInt) >>> 18];
                outputIndex = outputIndex2;
            }
            if (i + 1 < len) {
                outputIndex2 = outputIndex + 1;
                output[outputIndex] = BASE_64_ALPHABET[(SECOND_OUTPUT_BYTE_MASK & byteTripletAsInt) >>> 12];
            } else {
                outputIndex2 = outputIndex;
            }
            outputIndex = outputIndex2 + 1;
            output[outputIndex2] = BASE_64_ALPHABET[(byteTripletAsInt & THIRD_OUTPUT_BYTE_MASK) >>> 6];
            outputIndex2 = outputIndex + 1;
            output[outputIndex] = BASE_64_ALPHABET[byteTripletAsInt & FOURTH_OUTPUT_BYTE_MASK];
            i += 3;
            outputIndex = outputIndex2;
        }
        int inLengthMod3 = len % 3;
        if (inLengthMod3 > 0) {
            outputIndex2 = outputIndex + 1;
            output[outputIndex] = (byte) 61;
            if (inLengthMod3 == 1) {
                outputIndex = outputIndex2 + 1;
                output[outputIndex2] = (byte) 61;
                outputIndex2 = outputIndex;
            }
        }
        return new String(output, StandardCharsets.US_ASCII);
    }

    private static int computeEncodingOutputLen(int inLength) {
        int inLengthMod3 = inLength % 3;
        int outputLen = (inLength / 3) * 4;
        if (inLengthMod3 == 2) {
            return outputLen + 4;
        }
        if (inLengthMod3 == 1) {
            return outputLen + 4;
        }
        return outputLen;
    }

    public static byte[] decode(byte[] in) {
        return decode(in, in.length);
    }

    public static byte[] decode(byte[] in, int len) {
        byte[] bArr = null;
        int inLength = Math.min(in.length, len);
        ByteArrayOutputStream output = new ByteArrayOutputStream(((inLength / 4) * 3) + 3);
        int[] pos = new int[1];
        while (pos[0] < inLength) {
            try {
                int byteTripletAsInt = 0;
                for (int j = 0; j < 4; j++) {
                    byte c = getNextByte(in, pos, inLength);
                    if (c == END_OF_INPUT || c == PAD_AS_BYTE) {
                        switch (j) {
                            case XmlPullParser.START_DOCUMENT /*0*/:
                            case NodeFilter.SHOW_ELEMENT /*1*/:
                                if (c == END_OF_INPUT) {
                                    bArr = output.toByteArray();
                                }
                                return bArr;
                            case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                                if (c == END_OF_INPUT) {
                                    return checkNoTrailingAndReturn(output, in, pos[0], inLength);
                                }
                                pos[0] = pos[0] + 1;
                                c = getNextByte(in, pos, inLength);
                                if (c == END_OF_INPUT) {
                                    return checkNoTrailingAndReturn(output, in, pos[0], inLength);
                                }
                                if (c != PAD_AS_BYTE) {
                                    return null;
                                }
                                output.write(byteTripletAsInt >> 4);
                                return checkNoTrailingAndReturn(output, in, pos[0], inLength);
                            case XmlPullParser.END_TAG /*3*/:
                                if (c == PAD_AS_BYTE) {
                                    byteTripletAsInt >>= 2;
                                    output.write(byteTripletAsInt >> 8);
                                    output.write(byteTripletAsInt & Opcodes.OP_CONST_CLASS_JUMBO);
                                }
                                return checkNoTrailingAndReturn(output, in, pos[0], inLength);
                            default:
                                break;
                        }
                    }
                    byteTripletAsInt = (byteTripletAsInt << 6) + (c & Opcodes.OP_CONST_CLASS_JUMBO);
                    pos[0] = pos[0] + 1;
                }
                output.write(byteTripletAsInt >> 16);
                output.write((byteTripletAsInt >> 8) & Opcodes.OP_CONST_CLASS_JUMBO);
                output.write(byteTripletAsInt & Opcodes.OP_CONST_CLASS_JUMBO);
            } catch (InvalidBase64ByteException e) {
                return null;
            }
        }
        return checkNoTrailingAndReturn(output, in, pos[0], inLength);
    }

    private static byte getNextByte(byte[] in, int[] pos, int inLength) throws InvalidBase64ByteException {
        while (pos[0] < inLength) {
            byte c = base64AlphabetToNumericalValue(in[pos[0]]);
            if (c != -2) {
                return c;
            }
            pos[0] = pos[0] + 1;
        }
        return END_OF_INPUT;
    }

    private static byte[] checkNoTrailingAndReturn(ByteArrayOutputStream output, byte[] in, int i, int inLength) throws InvalidBase64ByteException {
        while (i < inLength) {
            byte c = base64AlphabetToNumericalValue(in[i]);
            if (c != -2 && c != -1) {
                return null;
            }
            i++;
        }
        return output.toByteArray();
    }

    private static byte base64AlphabetToNumericalValue(byte c) throws InvalidBase64ByteException {
        if (65 <= c && c <= 90) {
            return (byte) (c - 65);
        }
        if (97 <= c && c <= 122) {
            return (byte) ((c - 97) + 26);
        }
        if (48 <= c && c <= 57) {
            return (byte) ((c - 48) + 52);
        }
        if (c == 43) {
            return (byte) 62;
        }
        if (c == 47) {
            return (byte) 63;
        }
        if (c == 61) {
            return PAD_AS_BYTE;
        }
        if (c == 32 || c == 9 || c == 13 || c == 10) {
            return WHITESPACE_AS_BYTE;
        }
        throw new InvalidBase64ByteException();
    }
}
