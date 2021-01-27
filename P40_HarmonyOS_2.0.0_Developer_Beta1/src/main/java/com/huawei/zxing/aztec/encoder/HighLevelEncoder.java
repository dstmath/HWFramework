package com.huawei.zxing.aztec.encoder;

import com.huawei.zxing.common.BitArray;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public final class HighLevelEncoder {
    private static final int[][] CHAR_MAP = ((int[][]) Array.newInstance(int.class, 5, 256));
    static final int[][] LATCH_TABLE = {new int[]{0, 327708, 327710, 327709, 656318}, new int[]{590318, 0, 327710, 327709, 656318}, new int[]{262158, 590300, 0, 590301, 932798}, new int[]{327709, 327708, 656318, 0, 327710}, new int[]{327711, 656380, 656382, 656381, 0}};
    static final int MODE_DIGIT = 2;
    static final int MODE_LOWER = 1;
    static final int MODE_MIXED = 3;
    static final String[] MODE_NAMES = {"UPPER", "LOWER", "DIGIT", "MIXED", "PUNCT"};
    static final int MODE_PUNCT = 4;
    static final int MODE_UPPER = 0;
    static final int[][] SHIFT_TABLE = ((int[][]) Array.newInstance(int.class, 6, 6));
    private final byte[] text;

    static {
        CHAR_MAP[0][32] = 1;
        for (int c = 65; c <= 90; c++) {
            CHAR_MAP[0][c] = (c - 65) + 2;
        }
        CHAR_MAP[1][32] = 1;
        for (int c2 = 97; c2 <= 122; c2++) {
            CHAR_MAP[1][c2] = (c2 - 97) + 2;
        }
        CHAR_MAP[2][32] = 1;
        for (int c3 = 48; c3 <= 57; c3++) {
            CHAR_MAP[2][c3] = (c3 - 48) + 2;
        }
        int[][] iArr = CHAR_MAP;
        iArr[2][44] = 12;
        iArr[2][46] = 13;
        int[] mixedTable = {0, 32, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 27, 28, 29, 30, 31, 64, 92, 94, 95, 96, 124, 126, 127};
        for (int i = 0; i < mixedTable.length; i++) {
            CHAR_MAP[3][mixedTable[i]] = i;
        }
        int[] punctTable = {0, 13, 0, 0, 0, 0, 33, 39, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 58, 59, 60, 61, 62, 63, 91, 93, 123, 125};
        for (int i2 = 0; i2 < punctTable.length; i2++) {
            if (punctTable[i2] > 0) {
                CHAR_MAP[4][punctTable[i2]] = i2;
            }
        }
        for (int[] table : SHIFT_TABLE) {
            Arrays.fill(table, -1);
        }
        int[][] iArr2 = SHIFT_TABLE;
        iArr2[0][4] = 0;
        iArr2[1][4] = 0;
        iArr2[1][0] = 28;
        iArr2[3][4] = 0;
        iArr2[2][4] = 0;
        iArr2[2][0] = 15;
    }

    public HighLevelEncoder(byte[] text2) {
        this.text = text2;
    }

    public BitArray encode() {
        int pairCode;
        Collection<State> states = Collections.singletonList(State.INITIAL_STATE);
        int index = 0;
        while (true) {
            byte[] bArr = this.text;
            if (index >= bArr.length) {
                return ((State) Collections.min(states, new Comparator<State>() {
                    /* class com.huawei.zxing.aztec.encoder.HighLevelEncoder.AnonymousClass1 */

                    public int compare(State a, State b) {
                        return a.getBitCount() - b.getBitCount();
                    }
                })).toBitArray(this.text);
            }
            int i = 0;
            byte b = index + 1 < bArr.length ? bArr[index + 1] : 0;
            byte b2 = this.text[index];
            if (b2 == 13) {
                if (b == 10) {
                    i = 2;
                }
                pairCode = i;
            } else if (b2 == 44) {
                if (b == 32) {
                    i = 4;
                }
                pairCode = i;
            } else if (b2 == 46) {
                if (b == 32) {
                    i = 3;
                }
                pairCode = i;
            } else if (b2 != 58) {
                pairCode = 0;
            } else {
                if (b == 32) {
                    i = 5;
                }
                pairCode = i;
            }
            if (pairCode > 0) {
                states = updateStateListForPair(states, index, pairCode);
                index++;
            } else {
                states = updateStateListForChar(states, index);
            }
            index++;
        }
    }

    private Collection<State> updateStateListForChar(Iterable<State> states, int index) {
        Collection<State> result = new LinkedList<>();
        for (State state : states) {
            updateStateForChar(state, index, result);
        }
        return simplifyStates(result);
    }

    private void updateStateForChar(State state, int index, Collection<State> result) {
        char ch = (char) (this.text[index] & 255);
        boolean charInCurrentTable = CHAR_MAP[state.getMode()][ch] > 0;
        State stateNoBinary = null;
        for (int mode = 0; mode <= 4; mode++) {
            int charInMode = CHAR_MAP[mode][ch];
            if (charInMode > 0) {
                if (stateNoBinary == null) {
                    stateNoBinary = state.endBinaryShift(index);
                }
                if (!charInCurrentTable || mode == state.getMode() || mode == 2) {
                    result.add(stateNoBinary.latchAndAppend(mode, charInMode));
                }
                if (!charInCurrentTable && SHIFT_TABLE[state.getMode()][mode] >= 0) {
                    result.add(stateNoBinary.shiftAndAppend(mode, charInMode));
                }
            }
        }
        if (state.getBinaryShiftByteCount() > 0 || CHAR_MAP[state.getMode()][ch] == 0) {
            result.add(state.addBinaryShiftChar(index));
        }
    }

    private static Collection<State> updateStateListForPair(Iterable<State> states, int index, int pairCode) {
        Collection<State> result = new LinkedList<>();
        for (State state : states) {
            updateStateForPair(state, index, pairCode, result);
        }
        return simplifyStates(result);
    }

    private static void updateStateForPair(State state, int index, int pairCode, Collection<State> result) {
        State stateNoBinary = state.endBinaryShift(index);
        result.add(stateNoBinary.latchAndAppend(4, pairCode));
        if (state.getMode() != 4) {
            result.add(stateNoBinary.shiftAndAppend(4, pairCode));
        }
        if (pairCode == 3 || pairCode == 4) {
            result.add(stateNoBinary.latchAndAppend(2, 16 - pairCode).latchAndAppend(2, 1));
        }
        if (state.getBinaryShiftByteCount() > 0) {
            result.add(state.addBinaryShiftChar(index).addBinaryShiftChar(index + 1));
        }
    }

    private static Collection<State> simplifyStates(Iterable<State> states) {
        List<State> result = new LinkedList<>();
        for (State newState : states) {
            boolean add = true;
            Iterator<State> iterator = result.iterator();
            while (true) {
                if (!iterator.hasNext()) {
                    break;
                }
                State oldState = iterator.next();
                if (oldState.isBetterThanOrEqualTo(newState)) {
                    add = false;
                    break;
                } else if (newState.isBetterThanOrEqualTo(oldState)) {
                    iterator.remove();
                }
            }
            if (add) {
                result.add(newState);
            }
        }
        return result;
    }
}
