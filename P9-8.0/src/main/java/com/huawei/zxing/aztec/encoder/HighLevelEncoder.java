package com.huawei.zxing.aztec.encoder;

import com.huawei.android.util.JlogConstantsEx;
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
    private static final int[][] CHAR_MAP = ((int[][]) Array.newInstance(Integer.TYPE, new int[]{5, 256}));
    static final int[][] LATCH_TABLE = new int[][]{new int[]{0, 327708, 327710, 327709, 656318}, new int[]{590318, 0, 327710, 327709, 656318}, new int[]{262158, 590300, 0, 590301, 932798}, new int[]{327709, 327708, 656318, 0, 327710}, new int[]{327711, 656380, 656382, 656381, 0}};
    static final int MODE_DIGIT = 2;
    static final int MODE_LOWER = 1;
    static final int MODE_MIXED = 3;
    static final String[] MODE_NAMES = new String[]{"UPPER", "LOWER", "DIGIT", "MIXED", "PUNCT"};
    static final int MODE_PUNCT = 4;
    static final int MODE_UPPER = 0;
    static final int[][] SHIFT_TABLE = ((int[][]) Array.newInstance(Integer.TYPE, new int[]{6, 6}));
    private final byte[] text;

    static {
        int c;
        int i;
        CHAR_MAP[0][32] = 1;
        for (c = 65; c <= 90; c++) {
            CHAR_MAP[0][c] = (c - 65) + 2;
        }
        CHAR_MAP[1][32] = 1;
        for (c = 97; c <= JlogConstantsEx.JLID_DEF_CONTACT_ITEM_CLICK; c++) {
            CHAR_MAP[1][c] = (c - 97) + 2;
        }
        CHAR_MAP[2][32] = 1;
        for (c = 48; c <= 57; c++) {
            CHAR_MAP[2][c] = (c - 48) + 2;
        }
        CHAR_MAP[2][44] = 12;
        CHAR_MAP[2][46] = 13;
        int[] mixedTable = new int[]{0, 32, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 27, 28, 29, 30, 31, 64, 92, 94, 95, 96, JlogConstantsEx.JLID_NEW_CONTACT_SAVE_CLICK, JlogConstantsEx.JLID_NEW_CONTACT_SELECT_ACCOUNT, 127};
        for (i = 0; i < mixedTable.length; i++) {
            CHAR_MAP[3][mixedTable[i]] = i;
        }
        int[] punctTable = new int[]{0, 13, 0, 0, 0, 0, 33, 39, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 58, 59, 60, 61, 62, 63, 91, 93, JlogConstantsEx.JLID_NEW_CONTACT_CLICK, JlogConstantsEx.JLID_EDIT_CONTACT_CLICK};
        for (i = 0; i < punctTable.length; i++) {
            if (punctTable[i] > 0) {
                CHAR_MAP[4][punctTable[i]] = i;
            }
        }
        for (int[] table : SHIFT_TABLE) {
            Arrays.fill(table, -1);
        }
        SHIFT_TABLE[0][4] = 0;
        SHIFT_TABLE[1][4] = 0;
        SHIFT_TABLE[1][0] = 28;
        SHIFT_TABLE[3][4] = 0;
        SHIFT_TABLE[2][4] = 0;
        SHIFT_TABLE[2][0] = 15;
    }

    public HighLevelEncoder(byte[] text) {
        this.text = text;
    }

    public BitArray encode() {
        Collection<State> states = Collections.singletonList(State.INITIAL_STATE);
        int index = 0;
        while (index < this.text.length) {
            int nextChar;
            int pairCode;
            if (index + 1 < this.text.length) {
                nextChar = this.text[index + 1];
            } else {
                nextChar = 0;
            }
            switch (this.text[index]) {
                case (byte) 13:
                    if (nextChar != 10) {
                        pairCode = 0;
                        break;
                    }
                    pairCode = 2;
                    break;
                case (byte) 44:
                    if (nextChar != 32) {
                        pairCode = 0;
                        break;
                    }
                    pairCode = 4;
                    break;
                case (byte) 46:
                    if (nextChar != 32) {
                        pairCode = 0;
                        break;
                    }
                    pairCode = 3;
                    break;
                case (byte) 58:
                    if (nextChar != 32) {
                        pairCode = 0;
                        break;
                    }
                    pairCode = 5;
                    break;
                default:
                    pairCode = 0;
                    break;
            }
            if (pairCode > 0) {
                states = updateStateListForPair(states, index, pairCode);
                index++;
            } else {
                states = updateStateListForChar(states, index);
            }
            index++;
        }
        return ((State) Collections.min(states, new Comparator<State>() {
            public int compare(State a, State b) {
                return a.getBitCount() - b.getBitCount();
            }
        })).toBitArray(this.text);
    }

    private Collection<State> updateStateListForChar(Iterable<State> states, int index) {
        Collection<State> result = new LinkedList();
        for (State state : states) {
            updateStateForChar(state, index, result);
        }
        return simplifyStates(result);
    }

    private void updateStateForChar(State state, int index, Collection<State> result) {
        char ch = (char) (this.text[index] & 255);
        boolean charInCurrentTable = CHAR_MAP[state.getMode()][ch] > 0;
        State stateNoBinary = null;
        int mode = 0;
        while (mode <= 4) {
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
            mode++;
        }
        if (state.getBinaryShiftByteCount() > 0 || CHAR_MAP[state.getMode()][ch] == 0) {
            result.add(state.addBinaryShiftChar(index));
        }
    }

    private static Collection<State> updateStateListForPair(Iterable<State> states, int index, int pairCode) {
        Collection<State> result = new LinkedList();
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
        List<State> result = new LinkedList();
        for (State newState : states) {
            boolean add = true;
            Iterator<State> iterator = result.iterator();
            while (iterator.hasNext()) {
                State oldState = (State) iterator.next();
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
