package com.huawei.zxing.aztec.encoder;

import com.huawei.attestation.HwAttestationStatus;
import com.huawei.zxing.common.BitArray;
import java.util.Deque;
import java.util.LinkedList;

final class State {
    static final State INITIAL_STATE = null;
    private final int binaryShiftByteCount;
    private final int bitCount;
    private final int mode;
    private final Token token;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.aztec.encoder.State.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.aztec.encoder.State.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.aztec.encoder.State.<clinit>():void");
    }

    private State(Token token, int mode, int binaryBytes, int bitCount) {
        this.token = token;
        this.mode = mode;
        this.binaryShiftByteCount = binaryBytes;
        this.bitCount = bitCount;
    }

    int getMode() {
        return this.mode;
    }

    Token getToken() {
        return this.token;
    }

    int getBinaryShiftByteCount() {
        return this.binaryShiftByteCount;
    }

    int getBitCount() {
        return this.bitCount;
    }

    State latchAndAppend(int mode, int value) {
        int bitCount = this.bitCount;
        Token token = this.token;
        if (mode != this.mode) {
            int latch = HighLevelEncoder.LATCH_TABLE[this.mode][mode];
            token = token.add(HwAttestationStatus.CERT_INVAILD_LENGTH & latch, latch >> 16);
            bitCount += latch >> 16;
        }
        int latchModeBitCount = mode == 2 ? 4 : 5;
        return new State(token.add(value, latchModeBitCount), mode, 0, bitCount + latchModeBitCount);
    }

    State shiftAndAppend(int mode, int value) {
        Token token = this.token;
        int thisModeBitCount = this.mode == 2 ? 4 : 5;
        return new State(token.add(HighLevelEncoder.SHIFT_TABLE[this.mode][mode], thisModeBitCount).add(value, 5), this.mode, 0, (this.bitCount + thisModeBitCount) + 5);
    }

    State addBinaryShiftChar(int index) {
        Token token = this.token;
        int mode = this.mode;
        int bitCount = this.bitCount;
        if (this.mode == 4 || this.mode == 2) {
            int latch = HighLevelEncoder.LATCH_TABLE[mode][0];
            token = token.add(HwAttestationStatus.CERT_INVAILD_LENGTH & latch, latch >> 16);
            bitCount += latch >> 16;
            mode = 0;
        }
        int deltaBitCount = (this.binaryShiftByteCount == 0 || this.binaryShiftByteCount == 31) ? 18 : this.binaryShiftByteCount == 62 ? 9 : 8;
        State result = new State(token, mode, this.binaryShiftByteCount + 1, bitCount + deltaBitCount);
        if (result.binaryShiftByteCount == 2078) {
            return result.endBinaryShift(index + 1);
        }
        return result;
    }

    State endBinaryShift(int index) {
        if (this.binaryShiftByteCount == 0) {
            return this;
        }
        return new State(this.token.addBinaryShift(index - this.binaryShiftByteCount, this.binaryShiftByteCount), this.mode, 0, this.bitCount);
    }

    boolean isBetterThanOrEqualTo(State other) {
        int mySize = this.bitCount + (HighLevelEncoder.LATCH_TABLE[this.mode][other.mode] >> 16);
        if (other.binaryShiftByteCount > 0 && (this.binaryShiftByteCount == 0 || this.binaryShiftByteCount > other.binaryShiftByteCount)) {
            mySize += 10;
        }
        if (mySize <= other.bitCount) {
            return true;
        }
        return false;
    }

    BitArray toBitArray(byte[] text) {
        Deque<Token> symbols = new LinkedList();
        for (Token token = endBinaryShift(text.length).token; token != null; token = token.getPrevious()) {
            symbols.addFirst(token);
        }
        BitArray bitArray = new BitArray();
        for (Token symbol : symbols) {
            symbol.appendTo(bitArray, text);
        }
        return bitArray;
    }

    public String toString() {
        return String.format("%s bits=%d bytes=%d", new Object[]{HighLevelEncoder.MODE_NAMES[this.mode], Integer.valueOf(this.bitCount), Integer.valueOf(this.binaryShiftByteCount)});
    }
}
