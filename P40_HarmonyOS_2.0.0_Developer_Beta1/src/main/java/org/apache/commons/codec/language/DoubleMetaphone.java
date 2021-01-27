package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;

@Deprecated
public class DoubleMetaphone implements StringEncoder {
    private static final String[] ES_EP_EB_EL_EY_IB_IL_IN_IE_EI_ER = {"ES", "EP", "EB", "EL", "EY", "IB", "IL", "IN", "IE", "EI", "ER"};
    private static final String[] L_R_N_M_B_H_F_V_W_SPACE = {"L", "R", "N", "M", "B", "H", "F", "V", "W", " "};
    private static final String[] L_T_K_S_N_M_B_Z = {"L", "T", "K", "S", "N", "M", "B", "Z"};
    private static final String[] SILENT_START = {"GN", "KN", "PN", "WR", "PS"};
    private static final String VOWELS = "AEIOUY";
    protected int maxCodeLen = 4;

    public String doubleMetaphone(String value) {
        return doubleMetaphone(value, false);
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:69:0x0019 */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:95:0x0019 */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r7v0, types: [org.apache.commons.codec.language.DoubleMetaphone] */
    /* JADX WARN: Type inference failed for: r1v0, types: [boolean] */
    /* JADX WARN: Type inference failed for: r1v1, types: [int] */
    /* JADX WARN: Type inference failed for: r1v2 */
    /* JADX WARN: Type inference failed for: r1v3, types: [int] */
    /* JADX WARN: Type inference failed for: r1v4, types: [int] */
    /* JADX WARN: Type inference failed for: r1v5, types: [int] */
    /* JADX WARN: Type inference failed for: r1v6 */
    /* JADX WARN: Type inference failed for: r1v7, types: [int] */
    /* JADX WARN: Type inference failed for: r1v8, types: [int] */
    /* JADX WARN: Type inference failed for: r1v9 */
    /* JADX WARN: Type inference failed for: r1v10, types: [int] */
    /* JADX WARN: Type inference failed for: r1v11, types: [int] */
    /* JADX WARN: Type inference failed for: r1v12, types: [int] */
    /* JADX WARN: Type inference failed for: r1v13 */
    /* JADX WARN: Type inference failed for: r1v14, types: [int] */
    /* JADX WARN: Type inference failed for: r1v15 */
    /* JADX WARN: Type inference failed for: r1v16 */
    /* JADX WARN: Type inference failed for: r1v17, types: [int] */
    /* JADX WARN: Type inference failed for: r1v18 */
    /* JADX WARN: Type inference failed for: r1v19, types: [int] */
    /* JADX WARN: Type inference failed for: r1v20, types: [int] */
    /* JADX WARN: Type inference failed for: r1v21, types: [int] */
    /* JADX WARN: Type inference failed for: r1v22 */
    /* JADX WARN: Type inference failed for: r1v23, types: [int] */
    /* JADX WARN: Type inference failed for: r1v24, types: [int] */
    /* JADX WARN: Type inference failed for: r1v25, types: [int] */
    /* JADX WARN: Type inference failed for: r1v26, types: [int] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public String doubleMetaphone(String value, boolean alternate) {
        String value2 = cleanInput(value);
        if (value2 == null) {
            return null;
        }
        boolean slavoGermanic = isSlavoGermanic(value2);
        ?? isSilentStart = isSilentStart(value2);
        DoubleMetaphoneResult result = new DoubleMetaphoneResult(getMaxCodeLen());
        while (!result.isComplete() && isSilentStart <= value2.length() - 1) {
            int index = isSilentStart == true ? 1 : 0;
            int index2 = isSilentStart == true ? 1 : 0;
            int index3 = isSilentStart == true ? 1 : 0;
            char charAt = value2.charAt(index);
            if (charAt == 199) {
                result.append('S');
                isSilentStart++;
            } else if (charAt != 209) {
                switch (charAt) {
                    case 'A':
                    case 'E':
                    case 'I':
                    case 'O':
                    case 'U':
                    case 'Y':
                        isSilentStart = handleAEIOUY(value2, result, isSilentStart);
                        continue;
                    case 'B':
                        result.append('P');
                        isSilentStart = charAt(value2, isSilentStart + 1) == 'B' ? isSilentStart + 2 : isSilentStart + 1;
                        continue;
                    case 'C':
                        isSilentStart = handleC(value2, result, isSilentStart);
                        continue;
                    case 'D':
                        isSilentStart = handleD(value2, result, isSilentStart);
                        continue;
                    case 'F':
                        result.append('F');
                        isSilentStart = charAt(value2, isSilentStart + 1) == 'F' ? isSilentStart + 2 : isSilentStart + 1;
                        continue;
                    case 'G':
                        isSilentStart = handleG(value2, result, isSilentStart, slavoGermanic);
                        continue;
                    case 'H':
                        isSilentStart = handleH(value2, result, isSilentStart);
                        continue;
                    case 'J':
                        isSilentStart = handleJ(value2, result, isSilentStart, slavoGermanic);
                        continue;
                    case 'K':
                        result.append('K');
                        isSilentStart = charAt(value2, isSilentStart + 1) == 'K' ? isSilentStart + 2 : isSilentStart + 1;
                        continue;
                    case 'L':
                        isSilentStart = handleL(value2, result, isSilentStart);
                        continue;
                    case 'M':
                        result.append('M');
                        isSilentStart = conditionM0(value2, isSilentStart) ? isSilentStart + 2 : isSilentStart + 1;
                        continue;
                    case 'N':
                        result.append('N');
                        isSilentStart = charAt(value2, isSilentStart + 1) == 'N' ? isSilentStart + 2 : isSilentStart + 1;
                        continue;
                    case 'P':
                        isSilentStart = handleP(value2, result, isSilentStart);
                        continue;
                    case 'Q':
                        result.append('K');
                        isSilentStart = charAt(value2, isSilentStart + 1) == 'Q' ? isSilentStart + 2 : isSilentStart + 1;
                        continue;
                    case 'R':
                        isSilentStart = handleR(value2, result, isSilentStart, slavoGermanic);
                        continue;
                    case 'S':
                        isSilentStart = handleS(value2, result, isSilentStart, slavoGermanic);
                        continue;
                    case 'T':
                        isSilentStart = handleT(value2, result, isSilentStart);
                        continue;
                    case 'V':
                        result.append('F');
                        isSilentStart = charAt(value2, isSilentStart + 1) == 'V' ? isSilentStart + 2 : isSilentStart + 1;
                        continue;
                    case 'W':
                        isSilentStart = handleW(value2, result, isSilentStart);
                        continue;
                    case 'X':
                        isSilentStart = handleX(value2, result, isSilentStart);
                        continue;
                    case 'Z':
                        isSilentStart = handleZ(value2, result, isSilentStart, slavoGermanic);
                        continue;
                    default:
                        isSilentStart++;
                        continue;
                }
            } else {
                result.append('N');
                isSilentStart++;
            }
        }
        return alternate ? result.getAlternate() : result.getPrimary();
    }

    @Override // org.apache.commons.codec.Encoder
    public Object encode(Object obj) throws EncoderException {
        if (obj instanceof String) {
            return doubleMetaphone((String) obj);
        }
        throw new EncoderException("DoubleMetaphone encode parameter is not of type String");
    }

    @Override // org.apache.commons.codec.StringEncoder
    public String encode(String value) {
        return doubleMetaphone(value);
    }

    public boolean isDoubleMetaphoneEqual(String value1, String value2) {
        return isDoubleMetaphoneEqual(value1, value2, false);
    }

    public boolean isDoubleMetaphoneEqual(String value1, String value2, boolean alternate) {
        return doubleMetaphone(value1, alternate).equals(doubleMetaphone(value2, alternate));
    }

    public int getMaxCodeLen() {
        return this.maxCodeLen;
    }

    public void setMaxCodeLen(int maxCodeLen2) {
        this.maxCodeLen = maxCodeLen2;
    }

    private int handleAEIOUY(String value, DoubleMetaphoneResult result, int index) {
        if (index == 0) {
            result.append('A');
        }
        return index + 1;
    }

    private int handleC(String value, DoubleMetaphoneResult result, int index) {
        if (conditionC0(value, index)) {
            result.append('K');
            return index + 2;
        } else if (index == 0 && contains(value, index, 6, "CAESAR")) {
            result.append('S');
            return index + 2;
        } else if (contains(value, index, 2, "CH")) {
            return handleCH(value, result, index);
        } else {
            if (contains(value, index, 2, "CZ") && !contains(value, index - 2, 4, "WICZ")) {
                result.append('S', 'X');
                return index + 2;
            } else if (contains(value, index + 1, 3, "CIA")) {
                result.append('X');
                return index + 3;
            } else if (contains(value, index, 2, "CC") && (index != 1 || charAt(value, 0) != 'M')) {
                return handleCC(value, result, index);
            } else {
                if (contains(value, index, 2, "CK", "CG", "CQ")) {
                    result.append('K');
                    return index + 2;
                } else if (contains(value, index, 2, "CI", "CE", "CY")) {
                    if (contains(value, index, 3, "CIO", "CIE", "CIA")) {
                        result.append('S', 'X');
                    } else {
                        result.append('S');
                    }
                    return index + 2;
                } else {
                    result.append('K');
                    if (contains(value, index + 1, 2, " C", " Q", " G")) {
                        return index + 3;
                    }
                    if (!contains(value, index + 1, 1, "C", "K", "Q") || contains(value, index + 1, 2, "CE", "CI")) {
                        return index + 1;
                    }
                    return index + 2;
                }
            }
        }
    }

    private int handleCC(String value, DoubleMetaphoneResult result, int index) {
        if (!contains(value, index + 2, 1, "I", "E", "H") || contains(value, index + 2, 2, "HU")) {
            result.append('K');
            return index + 2;
        }
        if (!(index == 1 && charAt(value, index - 1) == 'A') && !contains(value, index - 1, 5, "UCCEE", "UCCES")) {
            result.append('X');
        } else {
            result.append("KS");
        }
        return index + 3;
    }

    private int handleCH(String value, DoubleMetaphoneResult result, int index) {
        if (index > 0 && contains(value, index, 4, "CHAE")) {
            result.append('K', 'X');
            return index + 2;
        } else if (conditionCH0(value, index)) {
            result.append('K');
            return index + 2;
        } else if (conditionCH1(value, index)) {
            result.append('K');
            return index + 2;
        } else {
            if (index <= 0) {
                result.append('X');
            } else if (contains(value, 0, 2, "MC")) {
                result.append('K');
            } else {
                result.append('X', 'K');
            }
            return index + 2;
        }
    }

    private int handleD(String value, DoubleMetaphoneResult result, int index) {
        if (contains(value, index, 2, "DG")) {
            if (contains(value, index + 2, 1, "I", "E", "Y")) {
                result.append('J');
                return index + 3;
            }
            result.append("TK");
            return index + 2;
        } else if (contains(value, index, 2, "DT", "DD")) {
            result.append('T');
            return index + 2;
        } else {
            result.append('T');
            return index + 1;
        }
    }

    private int handleG(String value, DoubleMetaphoneResult result, int index, boolean slavoGermanic) {
        if (charAt(value, index + 1) == 'H') {
            return handleGH(value, result, index);
        }
        if (charAt(value, index + 1) == 'N') {
            if (index == 1 && isVowel(charAt(value, 0)) && !slavoGermanic) {
                result.append("KN", "N");
            } else if (contains(value, index + 2, 2, "EY") || charAt(value, index + 1) == 'Y' || slavoGermanic) {
                result.append("KN");
            } else {
                result.append("N", "KN");
            }
            return index + 2;
        } else if (contains(value, index + 1, 2, "LI") && !slavoGermanic) {
            result.append("KL", "L");
            return index + 2;
        } else if (index == 0 && (charAt(value, index + 1) == 'Y' || contains(value, index + 1, 2, ES_EP_EB_EL_EY_IB_IL_IN_IE_EI_ER))) {
            result.append('K', 'J');
            return index + 2;
        } else if ((contains(value, index + 1, 2, "ER") || charAt(value, index + 1) == 'Y') && !contains(value, 0, 6, "DANGER", "RANGER", "MANGER") && !contains(value, index - 1, 1, "E", "I") && !contains(value, index - 1, 3, "RGY", "OGY")) {
            result.append('K', 'J');
            return index + 2;
        } else if (contains(value, index + 1, 1, "E", "I", "Y") || contains(value, index - 1, 4, "AGGI", "OGGI")) {
            if (contains(value, 0, 4, "VAN ", "VON ") || contains(value, 0, 3, "SCH") || contains(value, index + 1, 2, "ET")) {
                result.append('K');
            } else if (contains(value, index + 1, 4, "IER")) {
                result.append('J');
            } else {
                result.append('J', 'K');
            }
            return index + 2;
        } else if (charAt(value, index + 1) == 'G') {
            int index2 = index + 2;
            result.append('K');
            return index2;
        } else {
            int index3 = index + 1;
            result.append('K');
            return index3;
        }
    }

    private int handleGH(String value, DoubleMetaphoneResult result, int index) {
        if (index > 0 && !isVowel(charAt(value, index - 1))) {
            result.append('K');
            return index + 2;
        } else if (index == 0) {
            if (charAt(value, index + 2) == 'I') {
                result.append('J');
            } else {
                result.append('K');
            }
            return index + 2;
        } else if ((index > 1 && contains(value, index - 2, 1, "B", "H", "D")) || ((index > 2 && contains(value, index - 3, 1, "B", "H", "D")) || (index > 3 && contains(value, index - 4, 1, "B", "H")))) {
            return index + 2;
        } else {
            if (index > 2 && charAt(value, index - 1) == 'U' && contains(value, index - 3, 1, "C", "G", "L", "R", "T")) {
                result.append('F');
            } else if (index > 0 && charAt(value, index - 1) != 'I') {
                result.append('K');
            }
            return index + 2;
        }
    }

    private int handleH(String value, DoubleMetaphoneResult result, int index) {
        if ((index != 0 && !isVowel(charAt(value, index - 1))) || !isVowel(charAt(value, index + 1))) {
            return index + 1;
        }
        result.append('H');
        return index + 2;
    }

    private int handleJ(String value, DoubleMetaphoneResult result, int index, boolean slavoGermanic) {
        if (contains(value, index, 4, "JOSE") || contains(value, 0, 4, "SAN ")) {
            if ((index == 0 && charAt(value, index + 4) == ' ') || value.length() == 4 || contains(value, 0, 4, "SAN ")) {
                result.append('H');
            } else {
                result.append('J', 'H');
            }
            return index + 1;
        }
        if (index == 0 && !contains(value, index, 4, "JOSE")) {
            result.append('J', 'A');
        } else if (isVowel(charAt(value, index - 1)) && !slavoGermanic && (charAt(value, index + 1) == 'A' || charAt(value, index + 1) == 'O')) {
            result.append('J', 'H');
        } else if (index == value.length() - 1) {
            result.append('J', ' ');
        } else if (!contains(value, index + 1, 1, L_T_K_S_N_M_B_Z) && !contains(value, index - 1, 1, "S", "K", "L")) {
            result.append('J');
        }
        if (charAt(value, index + 1) == 'J') {
            return index + 2;
        }
        return index + 1;
    }

    private int handleL(String value, DoubleMetaphoneResult result, int index) {
        result.append('L');
        if (charAt(value, index + 1) != 'L') {
            return index + 1;
        }
        if (conditionL0(value, index)) {
            result.appendAlternate(' ');
        }
        return index + 2;
    }

    private int handleP(String value, DoubleMetaphoneResult result, int index) {
        if (charAt(value, index + 1) == 'H') {
            result.append('F');
            return index + 2;
        }
        result.append('P');
        return contains(value, index + 1, 1, "P", "B") ? index + 2 : index + 1;
    }

    private int handleR(String value, DoubleMetaphoneResult result, int index, boolean slavoGermanic) {
        if (index != value.length() - 1 || slavoGermanic || !contains(value, index - 2, 2, "IE") || contains(value, index - 4, 2, "ME", "MA")) {
            result.append('R');
        } else {
            result.appendAlternate('R');
        }
        return charAt(value, index + 1) == 'R' ? index + 2 : index + 1;
    }

    private int handleS(String value, DoubleMetaphoneResult result, int index, boolean slavoGermanic) {
        if (contains(value, index - 1, 3, "ISL", "YSL")) {
            return index + 1;
        }
        if (index == 0 && contains(value, index, 5, "SUGAR")) {
            result.append('X', 'S');
            return index + 1;
        } else if (contains(value, index, 2, "SH")) {
            if (contains(value, index + 1, 4, "HEIM", "HOEK", "HOLM", "HOLZ")) {
                result.append('S');
            } else {
                result.append('X');
            }
            return index + 2;
        } else if (contains(value, index, 3, "SIO", "SIA") || contains(value, index, 4, "SIAN")) {
            if (slavoGermanic) {
                result.append('S');
            } else {
                result.append('S', 'X');
            }
            return index + 3;
        } else if ((index == 0 && contains(value, index + 1, 1, "M", "N", "L", "W")) || contains(value, index + 1, 1, "Z")) {
            result.append('S', 'X');
            return contains(value, index + 1, 1, "Z") ? index + 2 : index + 1;
        } else if (contains(value, index, 2, "SC")) {
            return handleSC(value, result, index);
        } else {
            if (index != value.length() - 1 || !contains(value, index - 2, 2, "AI", "OI")) {
                result.append('S');
            } else {
                result.appendAlternate('S');
            }
            return contains(value, index + 1, 1, "S", "Z") ? index + 2 : index + 1;
        }
    }

    private int handleSC(String value, DoubleMetaphoneResult result, int index) {
        if (charAt(value, index + 2) == 'H') {
            if (contains(value, index + 3, 2, "OO", "ER", "EN", "UY", "ED", "EM")) {
                if (contains(value, index + 3, 2, "ER", "EN")) {
                    result.append("X", "SK");
                } else {
                    result.append("SK");
                }
            } else if (index != 0 || isVowel(charAt(value, 3)) || charAt(value, 3) == 'W') {
                result.append('X');
            } else {
                result.append('X', 'S');
            }
        } else if (contains(value, index + 2, 1, "I", "E", "Y")) {
            result.append('S');
        } else {
            result.append("SK");
        }
        return index + 3;
    }

    private int handleT(String value, DoubleMetaphoneResult result, int index) {
        if (contains(value, index, 4, "TION")) {
            result.append('X');
            return index + 3;
        } else if (contains(value, index, 3, "TIA", "TCH")) {
            result.append('X');
            return index + 3;
        } else if (contains(value, index, 2, "TH") || contains(value, index, 3, "TTH")) {
            if (contains(value, index + 2, 2, "OM", "AM") || contains(value, 0, 4, "VAN ", "VON ") || contains(value, 0, 3, "SCH")) {
                result.append('T');
            } else {
                result.append('0', 'T');
            }
            return index + 2;
        } else {
            result.append('T');
            return contains(value, index + 1, 1, "T", "D") ? index + 2 : index + 1;
        }
    }

    private int handleW(String value, DoubleMetaphoneResult result, int index) {
        if (contains(value, index, 2, "WR")) {
            result.append('R');
            return index + 2;
        } else if (index == 0 && (isVowel(charAt(value, index + 1)) || contains(value, index, 2, "WH"))) {
            if (isVowel(charAt(value, index + 1))) {
                result.append('A', 'F');
            } else {
                result.append('A');
            }
            return index + 1;
        } else if ((index == value.length() - 1 && isVowel(charAt(value, index - 1))) || contains(value, index - 1, 5, "EWSKI", "EWSKY", "OWSKI", "OWSKY") || contains(value, 0, 3, "SCH")) {
            result.appendAlternate('F');
            return index + 1;
        } else if (!contains(value, index, 4, "WICZ", "WITZ")) {
            return index + 1;
        } else {
            result.append("TS", "FX");
            return index + 4;
        }
    }

    private int handleX(String value, DoubleMetaphoneResult result, int index) {
        if (index == 0) {
            result.append('S');
            return index + 1;
        }
        if (index != value.length() - 1 || (!contains(value, index - 3, 3, "IAU", "EAU") && !contains(value, index - 2, 2, "AU", "OU"))) {
            result.append("KS");
        }
        return contains(value, index + 1, 1, "C", "X") ? index + 2 : index + 1;
    }

    private int handleZ(String value, DoubleMetaphoneResult result, int index, boolean slavoGermanic) {
        if (charAt(value, index + 1) == 'H') {
            result.append('J');
            return index + 2;
        }
        if (contains(value, index + 1, 2, "ZO", "ZI", "ZA") || (slavoGermanic && index > 0 && charAt(value, index - 1) != 'T')) {
            result.append("S", "TS");
        } else {
            result.append('S');
        }
        return charAt(value, index + 1) == 'Z' ? index + 2 : index + 1;
    }

    private boolean conditionC0(String value, int index) {
        if (contains(value, index, 4, "CHIA")) {
            return true;
        }
        if (index <= 1 || isVowel(charAt(value, index - 2)) || !contains(value, index - 1, 3, "ACH")) {
            return false;
        }
        char c = charAt(value, index + 2);
        if ((c == 'I' || c == 'E') && !contains(value, index - 2, 6, "BACHER", "MACHER")) {
            return false;
        }
        return true;
    }

    private boolean conditionCH0(String value, int index) {
        if (index != 0) {
            return false;
        }
        if ((contains(value, index + 1, 5, "HARAC", "HARIS") || contains(value, index + 1, 3, "HOR", "HYM", "HIA", "HEM")) && !contains(value, 0, 5, "CHORE")) {
            return true;
        }
        return false;
    }

    private boolean conditionCH1(String value, int index) {
        if (!contains(value, 0, 4, "VAN ", "VON ") && !contains(value, 0, 3, "SCH") && !contains(value, index - 2, 6, "ORCHES", "ARCHIT", "ORCHID") && !contains(value, index + 2, 1, "T", "S")) {
            if (!contains(value, index - 1, 1, "A", "O", "U", "E") && index != 0) {
                return false;
            }
            if (!contains(value, index + 2, 1, L_R_N_M_B_H_F_V_W_SPACE) && index + 1 != value.length() - 1) {
                return false;
            }
        }
        return true;
    }

    private boolean conditionL0(String value, int index) {
        if (index == value.length() - 3 && contains(value, index - 1, 4, "ILLO", "ILLA", "ALLE")) {
            return true;
        }
        if ((contains(value, index - 1, 2, "AS", "OS") || contains(value, value.length() - 1, 1, "A", "O")) && contains(value, index - 1, 4, "ALLE")) {
            return true;
        }
        return false;
    }

    private boolean conditionM0(String value, int index) {
        if (charAt(value, index + 1) == 'M') {
            return true;
        }
        if (!contains(value, index - 1, 3, "UMB") || (index + 1 != value.length() - 1 && !contains(value, index + 2, 2, "ER"))) {
            return false;
        }
        return true;
    }

    private boolean isSlavoGermanic(String value) {
        return value.indexOf(87) > -1 || value.indexOf(75) > -1 || value.indexOf("CZ") > -1 || value.indexOf("WITZ") > -1;
    }

    private boolean isVowel(char ch) {
        return VOWELS.indexOf(ch) != -1;
    }

    private boolean isSilentStart(String value) {
        int i = 0;
        while (true) {
            String[] strArr = SILENT_START;
            if (i >= strArr.length) {
                return false;
            }
            if (value.startsWith(strArr[i])) {
                return true;
            }
            i++;
        }
    }

    private String cleanInput(String input) {
        if (input == null) {
            return null;
        }
        String input2 = input.trim();
        if (input2.length() == 0) {
            return null;
        }
        return input2.toUpperCase();
    }

    /* access modifiers changed from: protected */
    public char charAt(String value, int index) {
        if (index < 0 || index >= value.length()) {
            return 0;
        }
        return value.charAt(index);
    }

    private static boolean contains(String value, int start, int length, String criteria) {
        return contains(value, start, length, new String[]{criteria});
    }

    private static boolean contains(String value, int start, int length, String criteria1, String criteria2) {
        return contains(value, start, length, new String[]{criteria1, criteria2});
    }

    private static boolean contains(String value, int start, int length, String criteria1, String criteria2, String criteria3) {
        return contains(value, start, length, new String[]{criteria1, criteria2, criteria3});
    }

    private static boolean contains(String value, int start, int length, String criteria1, String criteria2, String criteria3, String criteria4) {
        return contains(value, start, length, new String[]{criteria1, criteria2, criteria3, criteria4});
    }

    private static boolean contains(String value, int start, int length, String criteria1, String criteria2, String criteria3, String criteria4, String criteria5) {
        return contains(value, start, length, new String[]{criteria1, criteria2, criteria3, criteria4, criteria5});
    }

    private static boolean contains(String value, int start, int length, String criteria1, String criteria2, String criteria3, String criteria4, String criteria5, String criteria6) {
        return contains(value, start, length, new String[]{criteria1, criteria2, criteria3, criteria4, criteria5, criteria6});
    }

    protected static boolean contains(String value, int start, int length, String[] criteria) {
        if (start < 0 || start + length > value.length()) {
            return false;
        }
        String target = value.substring(start, start + length);
        for (String str : criteria) {
            if (target.equals(str)) {
                return true;
            }
        }
        return false;
    }

    public class DoubleMetaphoneResult {
        private StringBuffer alternate = new StringBuffer(DoubleMetaphone.this.getMaxCodeLen());
        private int maxLength;
        private StringBuffer primary = new StringBuffer(DoubleMetaphone.this.getMaxCodeLen());

        public DoubleMetaphoneResult(int maxLength2) {
            this.maxLength = maxLength2;
        }

        public void append(char value) {
            appendPrimary(value);
            appendAlternate(value);
        }

        public void append(char primary2, char alternate2) {
            appendPrimary(primary2);
            appendAlternate(alternate2);
        }

        public void appendPrimary(char value) {
            if (this.primary.length() < this.maxLength) {
                this.primary.append(value);
            }
        }

        public void appendAlternate(char value) {
            if (this.alternate.length() < this.maxLength) {
                this.alternate.append(value);
            }
        }

        public void append(String value) {
            appendPrimary(value);
            appendAlternate(value);
        }

        public void append(String primary2, String alternate2) {
            appendPrimary(primary2);
            appendAlternate(alternate2);
        }

        public void appendPrimary(String value) {
            int addChars = this.maxLength - this.primary.length();
            if (value.length() <= addChars) {
                this.primary.append(value);
            } else {
                this.primary.append(value.substring(0, addChars));
            }
        }

        public void appendAlternate(String value) {
            int addChars = this.maxLength - this.alternate.length();
            if (value.length() <= addChars) {
                this.alternate.append(value);
            } else {
                this.alternate.append(value.substring(0, addChars));
            }
        }

        public String getPrimary() {
            return this.primary.toString();
        }

        public String getAlternate() {
            return this.alternate.toString();
        }

        public boolean isComplete() {
            return this.primary.length() >= this.maxLength && this.alternate.length() >= this.maxLength;
        }
    }
}
