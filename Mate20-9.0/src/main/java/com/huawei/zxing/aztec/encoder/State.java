package com.huawei.zxing.aztec.encoder;

import com.huawei.zxing.common.BitArray;
import java.util.Deque;
import java.util.LinkedList;

final class State {
    static final State INITIAL_STATE = new State(Token.EMPTY, 0, 0, 0);
    private final int binaryShiftByteCount;
    private final int bitCount;
    private final int mode;
    private final Token token;

    private State(Token token2, int mode2, int binaryBytes, int bitCount2) {
        this.token = token2;
        this.mode = mode2;
        this.binaryShiftByteCount = binaryBytes;
        this.bitCount = bitCount2;
    }

    /* access modifiers changed from: package-private */
    public int getMode() {
        return this.mode;
    }

    /* access modifiers changed from: package-private */
    public Token getToken() {
        return this.token;
    }

    /* access modifiers changed from: package-private */
    public int getBinaryShiftByteCount() {
        return this.binaryShiftByteCount;
    }

    /* access modifiers changed from: package-private */
    public int getBitCount() {
        return this.bitCount;
    }

    /* access modifiers changed from: package-private */
    public State latchAndAppend(int mode2, int value) {
        int bitCount2 = this.bitCount;
        Token token2 = this.token;
        if (mode2 != this.mode) {
            int latch = HighLevelEncoder.LATCH_TABLE[this.mode][mode2];
            token2 = token2.add(65535 & latch, latch >> 16);
            bitCount2 += latch >> 16;
        }
        int latchModeBitCount = mode2 == 2 ? 4 : 5;
        return new State(token2.add(value, latchModeBitCount), mode2, 0, bitCount2 + latchModeBitCount);
    }

    /* access modifiers changed from: package-private */
    public State shiftAndAppend(int mode2, int value) {
        Token token2 = this.token;
        int thisModeBitCount = this.mode == 2 ? 4 : 5;
        return new State(token2.add(HighLevelEncoder.SHIFT_TABLE[this.mode][mode2], thisModeBitCount).add(value, 5), this.mode, 0, this.bitCount + thisModeBitCount + 5);
    }

    /* access modifiers changed from: package-private */
    public State addBinaryShiftChar(int index) {
        int deltaBitCount;
        Token token2 = this.token;
        int mode2 = this.mode;
        int bitCount2 = this.bitCount;
        if (this.mode == 4 || this.mode == 2) {
            int latch = HighLevelEncoder.LATCH_TABLE[mode2][0];
            token2 = token2.add(65535 & latch, latch >> 16);
            bitCount2 += latch >> 16;
            mode2 = 0;
        }
        if (this.binaryShiftByteCount == 0 || this.binaryShiftByteCount == 31) {
            deltaBitCount = 18;
        } else {
            deltaBitCount = this.binaryShiftByteCount == 62 ? 9 : 8;
        }
        State result = new State(token2, mode2, this.binaryShiftByteCount + 1, bitCount2 + deltaBitCount);
        if (result.binaryShiftByteCount == 2078) {
            return result.endBinaryShift(index + 1);
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public State endBinaryShift(int index) {
        if (this.binaryShiftByteCount == 0) {
            return this;
        }
        return new State(this.token.addBinaryShift(index - this.binaryShiftByteCount, this.binaryShiftByteCount), this.mode, 0, this.bitCount);
    }

    /* access modifiers changed from: package-private */
    public boolean isBetterThanOrEqualTo(State other) {
        int mySize = this.bitCount + (HighLevelEncoder.LATCH_TABLE[this.mode][other.mode] >> 16);
        if (other.binaryShiftByteCount > 0 && (this.binaryShiftByteCount == 0 || this.binaryShiftByteCount > other.binaryShiftByteCount)) {
            mySize += 10;
        }
        return mySize <= other.bitCount;
    }

    /* access modifiers changed from: package-private */
    public BitArray toBitArray(byte[] text) {
        Deque<Token> symbols = new LinkedList<>();
        for (Token token2 = endBinaryShift(text.length).token; token2 != null; token2 = token2.getPrevious()) {
            symbols.addFirst(token2);
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
