package com.huawei.zxing.aztec.encoder;

import com.huawei.internal.telephony.uicc.IccConstantsEx;
import com.huawei.lcagent.client.MetricConstant;
import com.huawei.zxing.common.BitArray;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public final class HighLevelEncoder {
    private static final int[][] CHAR_MAP = null;
    static final int[][] LATCH_TABLE = null;
    static final int MODE_DIGIT = 2;
    static final int MODE_LOWER = 1;
    static final int MODE_MIXED = 3;
    static final String[] MODE_NAMES = null;
    static final int MODE_PUNCT = 4;
    static final int MODE_UPPER = 0;
    static final int[][] SHIFT_TABLE = null;
    private final byte[] text;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.aztec.encoder.HighLevelEncoder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.aztec.encoder.HighLevelEncoder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.aztec.encoder.HighLevelEncoder.<clinit>():void");
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
            if (index + MODE_LOWER < this.text.length) {
                nextChar = this.text[index + MODE_LOWER];
            } else {
                nextChar = 0;
            }
            switch (this.text[index]) {
                case MetricConstant.AUDIO_METRIC_ID /*13*/:
                    if (nextChar != 10) {
                        pairCode = 0;
                        break;
                    }
                    pairCode = MODE_DIGIT;
                    break;
                case (byte) 44:
                    if (nextChar != 32) {
                        pairCode = 0;
                        break;
                    }
                    pairCode = MODE_PUNCT;
                    break;
                case (byte) 46:
                    if (nextChar != 32) {
                        pairCode = 0;
                        break;
                    }
                    pairCode = MODE_MIXED;
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
                index += MODE_LOWER;
            } else {
                states = updateStateListForChar(states, index);
            }
            index += MODE_LOWER;
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
        char ch = (char) (this.text[index] & IccConstantsEx.RUIM_SMS_BEARER_DATA_LEN);
        boolean charInCurrentTable = CHAR_MAP[state.getMode()][ch] > 0;
        State stateNoBinary = null;
        int mode = 0;
        while (mode <= MODE_PUNCT) {
            int charInMode = CHAR_MAP[mode][ch];
            if (charInMode > 0) {
                if (stateNoBinary == null) {
                    stateNoBinary = state.endBinaryShift(index);
                }
                if (charInCurrentTable && mode != state.getMode()) {
                    if (mode == MODE_DIGIT) {
                    }
                    if (!charInCurrentTable && SHIFT_TABLE[state.getMode()][mode] >= 0) {
                        result.add(stateNoBinary.shiftAndAppend(mode, charInMode));
                    }
                }
                result.add(stateNoBinary.latchAndAppend(mode, charInMode));
                result.add(stateNoBinary.shiftAndAppend(mode, charInMode));
            }
            mode += MODE_LOWER;
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
        result.add(stateNoBinary.latchAndAppend(MODE_PUNCT, pairCode));
        if (state.getMode() != MODE_PUNCT) {
            result.add(stateNoBinary.shiftAndAppend(MODE_PUNCT, pairCode));
        }
        if (pairCode == MODE_MIXED || pairCode == MODE_PUNCT) {
            result.add(stateNoBinary.latchAndAppend(MODE_DIGIT, 16 - pairCode).latchAndAppend(MODE_DIGIT, MODE_LOWER));
        }
        if (state.getBinaryShiftByteCount() > 0) {
            result.add(state.addBinaryShiftChar(index).addBinaryShiftChar(index + MODE_LOWER));
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
