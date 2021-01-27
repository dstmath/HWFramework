package ohos.global.icu.text;

import ohos.global.icu.impl.UBiDiProps;

public final class ArabicShaping {
    private static final int ALEFTYPE = 32;
    private static final int DESHAPE_MODE = 1;
    public static final int DIGITS_AN2EN = 64;
    public static final int DIGITS_EN2AN = 32;
    public static final int DIGITS_EN2AN_INIT_AL = 128;
    public static final int DIGITS_EN2AN_INIT_LR = 96;
    public static final int DIGITS_MASK = 224;
    public static final int DIGITS_NOOP = 0;
    public static final int DIGIT_TYPE_AN = 0;
    public static final int DIGIT_TYPE_AN_EXTENDED = 256;
    public static final int DIGIT_TYPE_MASK = 256;
    private static final char HAMZA06_CHAR = 1569;
    private static final char HAMZAFE_CHAR = 65152;
    private static final int IRRELEVANT = 4;
    public static final int LAMALEF_AUTO = 65536;
    public static final int LAMALEF_BEGIN = 3;
    public static final int LAMALEF_END = 2;
    public static final int LAMALEF_MASK = 65539;
    public static final int LAMALEF_NEAR = 1;
    public static final int LAMALEF_RESIZE = 0;
    private static final char LAMALEF_SPACE_SUB = 65535;
    private static final int LAMTYPE = 16;
    private static final char LAM_CHAR = 1604;
    public static final int LENGTH_FIXED_SPACES_AT_BEGINNING = 3;
    public static final int LENGTH_FIXED_SPACES_AT_END = 2;
    public static final int LENGTH_FIXED_SPACES_NEAR = 1;
    public static final int LENGTH_GROW_SHRINK = 0;
    public static final int LENGTH_MASK = 65539;
    public static final int LETTERS_MASK = 24;
    public static final int LETTERS_NOOP = 0;
    public static final int LETTERS_SHAPE = 8;
    public static final int LETTERS_SHAPE_TASHKEEL_ISOLATED = 24;
    public static final int LETTERS_UNSHAPE = 16;
    private static final int LINKL = 2;
    private static final int LINKR = 1;
    private static final int LINK_MASK = 3;
    private static final char NEW_TAIL_CHAR = 65139;
    private static final char OLD_TAIL_CHAR = 8203;
    public static final int SEEN_MASK = 7340032;
    public static final int SEEN_TWOCELL_NEAR = 2097152;
    private static final char SHADDA06_CHAR = 1617;
    private static final char SHADDA_CHAR = 65148;
    private static final char SHADDA_TATWEEL_CHAR = 65149;
    private static final int SHAPE_MODE = 0;
    public static final int SHAPE_TAIL_NEW_UNICODE = 134217728;
    public static final int SHAPE_TAIL_TYPE_MASK = 134217728;
    public static final int SPACES_RELATIVE_TO_TEXT_BEGIN_END = 67108864;
    public static final int SPACES_RELATIVE_TO_TEXT_MASK = 67108864;
    private static final char SPACE_CHAR = ' ';
    public static final int TASHKEEL_BEGIN = 262144;
    public static final int TASHKEEL_END = 393216;
    public static final int TASHKEEL_MASK = 917504;
    public static final int TASHKEEL_REPLACE_BY_TATWEEL = 786432;
    public static final int TASHKEEL_RESIZE = 524288;
    private static final char TASHKEEL_SPACE_SUB = 65534;
    private static final char TATWEEL_CHAR = 1600;
    public static final int TEXT_DIRECTION_LOGICAL = 0;
    public static final int TEXT_DIRECTION_MASK = 4;
    public static final int TEXT_DIRECTION_VISUAL_LTR = 4;
    public static final int TEXT_DIRECTION_VISUAL_RTL = 0;
    public static final int YEHHAMZA_MASK = 58720256;
    public static final int YEHHAMZA_TWOCELL_NEAR = 16777216;
    private static final char YEH_HAMZAFE_CHAR = 65161;
    private static final char YEH_HAMZA_CHAR = 1574;
    private static final int[] araLink = {4385, 4897, 5377, 5921, 6403, 7457, 7939, 8961, 9475, 10499, 11523, 12547, 13571, 14593, 15105, 15617, 16129, 16643, 17667, 18691, 19715, 20739, 21763, 22787, 23811, 0, 0, 0, 0, 0, 3, 24835, 25859, 26883, 27923, 28931, 29955, 30979, 32001, 32513, 33027, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0, 34049, 34561, 35073, 35585, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 33, 33, 0, 33, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 3, 3, 3, 3, 1, 1};
    private static int[] convertFEto06 = {1611, 1611, 1612, 1612, 1613, 1613, 1614, 1614, 1615, 1615, 1616, 1616, 1617, 1617, 1618, 1618, 1569, 1570, 1570, 1571, 1571, 1572, 1572, 1573, 1573, 1574, 1574, 1574, 1574, 1575, 1575, 1576, 1576, 1576, 1576, 1577, 1577, 1578, 1578, 1578, 1578, 1579, 1579, 1579, 1579, 1580, 1580, 1580, 1580, 1581, 1581, 1581, 1581, 1582, 1582, 1582, 1582, 1583, 1583, 1584, 1584, 1585, 1585, 1586, 1586, 1587, 1587, 1587, 1587, 1588, 1588, 1588, 1588, 1589, 1589, 1589, 1589, 1590, 1590, 1590, 1590, 1591, 1591, 1591, 1591, 1592, 1592, 1592, 1592, 1593, 1593, 1593, 1593, 1594, 1594, 1594, 1594, 1601, 1601, 1601, 1601, 1602, 1602, 1602, 1602, 1603, 1603, 1603, 1603, 1604, 1604, 1604, 1604, 1605, 1605, 1605, 1605, 1606, 1606, 1606, 1606, 1607, 1607, 1607, 1607, 1608, 1608, 1609, 1609, 1610, 1610, 1610, 1610, 1628, 1628, 1629, 1629, 1630, 1630, 1631, 1631};
    private static final char[] convertNormalizedLamAlef = {1570, 1571, 1573, 1575};
    private static final int[] irrelevantPos = {0, 2, 4, 6, 8, 10, 12, 14};
    private static final int[] presLink = {3, 3, 3, 0, 3, 0, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 32, 33, 32, 33, 0, 1, 32, 33, 0, 2, 3, 1, 32, 33, 0, 2, 3, 1, 0, 1, 0, 2, 3, 1, 0, 2, 3, 1, 0, 2, 3, 1, 0, 2, 3, 1, 0, 2, 3, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 2, 3, 1, 0, 2, 3, 1, 0, 2, 3, 1, 0, 2, 3, 1, 0, 2, 3, 1, 0, 2, 3, 1, 0, 2, 3, 1, 0, 2, 3, 1, 0, 2, 3, 1, 0, 2, 3, 1, 0, 2, 3, 1, 16, 18, 19, 17, 0, 2, 3, 1, 0, 2, 3, 1, 0, 2, 3, 1, 0, 1, 0, 1, 0, 2, 3, 1, 0, 1, 0, 1, 0, 1, 0, 1};
    private static final int[][][] shapeTable = {new int[][]{new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 1, 0, 3}, new int[]{0, 1, 0, 1}}, new int[][]{new int[]{0, 0, 2, 2}, new int[]{0, 0, 1, 2}, new int[]{0, 1, 1, 2}, new int[]{0, 1, 1, 3}}, new int[][]{new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}, new int[]{0, 1, 0, 3}, new int[]{0, 1, 0, 3}}, new int[][]{new int[]{0, 0, 1, 2}, new int[]{0, 0, 1, 2}, new int[]{0, 1, 1, 2}, new int[]{0, 1, 1, 3}}};
    private static final int[] tailFamilyIsolatedFinal = {1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1};
    private static final int[] tashkeelMedial = {0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1};
    private static final char[] yehHamzaToYeh = {65263, 65264};
    private boolean isLogical;
    private final int options;
    private boolean spacesRelativeToTextBeginEnd;
    private char tailChar;

    private static char changeLamAlef(char c) {
        if (c == 1570) {
            return 1628;
        }
        if (c == 1571) {
            return 1629;
        }
        if (c != 1573) {
            return c != 1575 ? (char) 0 : 1631;
        }
        return 1630;
    }

    private static boolean isAlefChar(char c) {
        return c == 1570 || c == 1571 || c == 1573 || c == 1575;
    }

    private static boolean isAlefMaksouraChar(char c) {
        return c == 65263 || c == 65264 || c == 1609;
    }

    private static boolean isLamAlefChar(char c) {
        return c >= 65269 && c <= 65276;
    }

    private static boolean isNormalizedLamAlefChar(char c) {
        return c >= 1628 && c <= 1631;
    }

    private static int isSeenFamilyChar(char c) {
        return (c < 1587 || c > 1590) ? 0 : 1;
    }

    private static boolean isTailChar(char c) {
        return c == 8203 || c == 65139;
    }

    private static boolean isTashkeelChar(char c) {
        return c >= 1611 && c <= 1618;
    }

    private static boolean isTashkeelCharFE(char c) {
        return c != 65141 && c >= 65136 && c <= 65151;
    }

    private static boolean isYehHamzaChar(char c) {
        return c == 65161 || c == 65162;
    }

    private static int specialChar(char c) {
        if ((c > 1569 && c < 1574) || c == 1575) {
            return 1;
        }
        if (c > 1582 && c < 1587) {
            return 1;
        }
        if ((c > 1607 && c < 1610) || c == 1577) {
            return 1;
        }
        if (c >= 1611 && c <= 1618) {
            return 2;
        }
        if ((c < 1619 || c > 1621) && c != 1648) {
            return (c < 65136 || c > 65151) ? 0 : 3;
        }
        return 3;
    }

    public int shape(char[] cArr, int i, int i2, char[] cArr2, int i3, int i4) throws ArabicShapingException {
        if (cArr == null) {
            throw new IllegalArgumentException("source can not be null");
        } else if (i < 0 || i2 < 0 || i + i2 > cArr.length) {
            throw new IllegalArgumentException("bad source start (" + i + ") or length (" + i2 + ") for buffer of length " + cArr.length);
        } else if (cArr2 == null && i4 != 0) {
            throw new IllegalArgumentException("null dest requires destSize == 0");
        } else if (i4 == 0 || (i3 >= 0 && i4 >= 0 && i3 + i4 <= cArr2.length)) {
            int i5 = this.options;
            if ((i5 & TASHKEEL_MASK) == 0 || (i5 & TASHKEEL_MASK) == 262144 || (i5 & TASHKEEL_MASK) == 393216 || (i5 & TASHKEEL_MASK) == 524288 || (i5 & TASHKEEL_MASK) == 786432) {
                int i6 = this.options;
                if ((i6 & 65539) == 0 || (i6 & 65539) == 3 || (i6 & 65539) == 2 || (i6 & 65539) == 0 || (i6 & 65539) == 65536 || (i6 & 65539) == 1) {
                    int i7 = this.options;
                    if ((917504 & i7) == 0 || (i7 & 24) != 16) {
                        return internalShape(cArr, i, i2, cArr2, i3, i4);
                    }
                    throw new IllegalArgumentException("Tashkeel replacement should not be enabled in deshaping mode ");
                }
                throw new IllegalArgumentException("Wrong Lam Alef argument");
            }
            throw new IllegalArgumentException("Wrong Tashkeel argument");
        } else {
            throw new IllegalArgumentException("bad dest start (" + i3 + ") or size (" + i4 + ") for buffer of length " + cArr2.length);
        }
    }

    public void shape(char[] cArr, int i, int i2) throws ArabicShapingException {
        if ((this.options & 65539) != 0) {
            shape(cArr, i, i2, cArr, i, i2);
            return;
        }
        throw new ArabicShapingException("Cannot shape in place with length option resize.");
    }

    public String shape(String str) throws ArabicShapingException {
        char[] charArray = str.toCharArray();
        int i = this.options;
        char[] cArr = ((65539 & i) == 0 && (i & 24) == 16) ? new char[(charArray.length * 2)] : charArray;
        return new String(cArr, 0, shape(charArray, 0, charArray.length, cArr, 0, cArr.length));
    }

    public ArabicShaping(int i) {
        this.options = i;
        if ((i & 224) <= 128) {
            boolean z = true;
            this.isLogical = (i & 4) == 0;
            this.spacesRelativeToTextBeginEnd = (i & 67108864) != 67108864 ? false : z;
            if ((i & 134217728) == 134217728) {
                this.tailChar = NEW_TAIL_CHAR;
            } else {
                this.tailChar = OLD_TAIL_CHAR;
            }
        } else {
            throw new IllegalArgumentException("bad DIGITS options");
        }
    }

    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == ArabicShaping.class && this.options == ((ArabicShaping) obj).options;
    }

    public int hashCode() {
        return this.options;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append('[');
        int i = this.options & 65539;
        if (i == 0) {
            sb.append("LamAlef resize");
        } else if (i == 1) {
            sb.append("LamAlef spaces at near");
        } else if (i == 2) {
            sb.append("LamAlef spaces at end");
        } else if (i == 3) {
            sb.append("LamAlef spaces at begin");
        } else if (i == 65536) {
            sb.append("lamAlef auto");
        }
        int i2 = this.options & 4;
        if (i2 == 0) {
            sb.append(", logical");
        } else if (i2 == 4) {
            sb.append(", visual");
        }
        int i3 = this.options & 24;
        if (i3 == 0) {
            sb.append(", no letter shaping");
        } else if (i3 == 8) {
            sb.append(", shape letters");
        } else if (i3 == 16) {
            sb.append(", unshape letters");
        } else if (i3 == 24) {
            sb.append(", shape letters tashkeel isolated");
        }
        if ((this.options & SEEN_MASK) == 2097152) {
            sb.append(", Seen at near");
        }
        if ((this.options & YEHHAMZA_MASK) == 16777216) {
            sb.append(", Yeh Hamza at near");
        }
        int i4 = this.options & TASHKEEL_MASK;
        if (i4 == 262144) {
            sb.append(", Tashkeel at begin");
        } else if (i4 == 393216) {
            sb.append(", Tashkeel at end");
        } else if (i4 == 524288) {
            sb.append(", Tashkeel resize");
        } else if (i4 == 786432) {
            sb.append(", Tashkeel replace with tatweel");
        }
        int i5 = this.options & 224;
        if (i5 == 0) {
            sb.append(", no digit shaping");
        } else if (i5 == 32) {
            sb.append(", shape digits to AN");
        } else if (i5 == 64) {
            sb.append(", shape digits to EN");
        } else if (i5 == 96) {
            sb.append(", shape digits to AN contextually: default EN");
        } else if (i5 == 128) {
            sb.append(", shape digits to AN contextually: default AL");
        }
        int i6 = this.options & 256;
        if (i6 == 0) {
            sb.append(", standard Arabic-Indic digits");
        } else if (i6 == 256) {
            sb.append(", extended Arabic-Indic digits");
        }
        sb.append("]");
        return sb.toString();
    }

    private void shapeToArabicDigitsWithContext(char[] cArr, int i, int i2, char c, boolean z) {
        UBiDiProps uBiDiProps = UBiDiProps.INSTANCE;
        char c2 = (char) (c - '0');
        int i3 = i2 + i;
        while (true) {
            i3--;
            if (i3 >= i) {
                char c3 = cArr[i3];
                int i4 = uBiDiProps.getClass(c3);
                if (i4 == 0 || i4 == 1) {
                    z = false;
                } else if (i4 != 2) {
                    if (i4 == 13) {
                        z = true;
                    }
                } else if (z && c3 <= '9') {
                    cArr[i3] = (char) (c3 + c2);
                }
            } else {
                return;
            }
        }
    }

    private static void invertBuffer(char[] cArr, int i, int i2) {
        for (int i3 = (i2 + i) - 1; i < i3; i3--) {
            char c = cArr[i];
            cArr[i] = cArr[i3];
            cArr[i3] = c;
            i++;
        }
    }

    private static int getLink(char c) {
        if (c >= 1570 && c <= 1747) {
            return araLink[c - 1570];
        }
        if (c == 8205) {
            return 3;
        }
        if (c >= 8301 && c <= 8303) {
            return 4;
        }
        if (c < 65136 || c > 65276) {
            return 0;
        }
        return presLink[c - 65136];
    }

    private static int countSpacesLeft(char[] cArr, int i, int i2) {
        int i3 = i + i2;
        for (int i4 = i; i4 < i3; i4++) {
            if (cArr[i4] != ' ') {
                return i4 - i;
            }
        }
        return i2;
    }

    private static int countSpacesRight(char[] cArr, int i, int i2) {
        int i3 = i + i2;
        int i4 = i3;
        do {
            i4--;
            if (i4 < i) {
                return i2;
            }
        } while (cArr[i4] == ' ');
        return (i3 - 1) - i4;
    }

    private static int isSeenTailFamilyChar(char c) {
        if (c < 65201 || c >= 65215) {
            return 0;
        }
        return tailFamilyIsolatedFinal[c - 65201];
    }

    private static int isTashkeelOnTatweelChar(char c) {
        if (c < 65136 || c > 65151 || c == 65139 || c == 65141 || c == 65149) {
            return ((c < 64754 || c > 64756) && c != 65149) ? 0 : 2;
        }
        return tashkeelMedial[c - 65136];
    }

    private static int isIsolatedTashkeelChar(char c) {
        if (c < 65136 || c > 65151 || c == 65139 || c == 65141) {
            return (c < 64606 || c > 64611) ? 0 : 1;
        }
        return 1 - tashkeelMedial[c - 65136];
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x000d, code lost:
        if (r0 != 24) goto L_0x0068;
     */
    private int calculateSize(char[] cArr, int i, int i2) {
        int i3 = this.options & 24;
        if (i3 != 8) {
            if (i3 == 16) {
                int i4 = i + i2;
                while (i < i4) {
                    if (isLamAlefChar(cArr[i])) {
                        i2++;
                    }
                    i++;
                }
            }
            return i2;
        }
        if (this.isLogical) {
            int i5 = (i + i2) - 1;
            while (i < i5) {
                if ((cArr[i] == 1604 && isAlefChar(cArr[i + 1])) || isTashkeelCharFE(cArr[i])) {
                    i2--;
                }
                i++;
            }
        } else {
            int i6 = i + i2;
            for (int i7 = i + 1; i7 < i6; i7++) {
                if ((cArr[i7] == 1604 && isAlefChar(cArr[i7 - 1])) || isTashkeelCharFE(cArr[i7])) {
                    i2--;
                }
            }
        }
        return i2;
    }

    private static int countSpaceSub(char[] cArr, int i, char c) {
        int i2 = 0;
        for (int i3 = 0; i3 < i; i3++) {
            if (cArr[i3] == c) {
                i2++;
            }
        }
        return i2;
    }

    private static void shiftArray(char[] cArr, int i, int i2, char c) {
        int i3 = i2;
        while (true) {
            i2--;
            if (i2 >= i) {
                char c2 = cArr[i2];
                if (!(c2 == c || i3 - 1 == i2)) {
                    cArr[i3] = c2;
                }
            } else {
                return;
            }
        }
    }

    private static int flipArray(char[] cArr, int i, int i2, int i3) {
        if (i3 <= i) {
            return i2;
        }
        while (i3 < i2) {
            cArr[i] = cArr[i3];
            i++;
            i3++;
        }
        return i;
    }

    private static int handleTashkeelWithTatweel(char[] cArr, int i) {
        for (int i2 = 0; i2 < i; i2++) {
            if (isTashkeelOnTatweelChar(cArr[i2]) == 1) {
                cArr[i2] = TATWEEL_CHAR;
            } else if (isTashkeelOnTatweelChar(cArr[i2]) == 2) {
                cArr[i2] = SHADDA_TATWEEL_CHAR;
            } else if (isIsolatedTashkeelChar(cArr[i2]) == 1 && cArr[i2] != 65148) {
                cArr[i2] = SPACE_CHAR;
            }
        }
        return i;
    }

    private int handleGeneratedSpaces(char[] cArr, int i, int i2) {
        int i3 = i;
        int i4 = i2;
        int i5 = this.options;
        int i6 = 65539 & i5;
        int i7 = i5 & TASHKEEL_MASK;
        if ((!this.spacesRelativeToTextBeginEnd) && (!this.isLogical)) {
            if (i6 == 2) {
                i6 = 3;
            } else if (i6 == 3) {
                i6 = 2;
            }
            if (i7 == 262144) {
                i7 = 393216;
            } else if (i7 == 393216) {
                i7 = 262144;
            }
        }
        if (i6 == 1) {
            int i8 = i3 + i4;
            while (i3 < i8) {
                if (cArr[i3] == 65535) {
                    cArr[i3] = SPACE_CHAR;
                }
                i3++;
            }
        } else {
            int i9 = i3 + i4;
            int countSpaceSub = countSpaceSub(cArr, i4, 65535);
            int countSpaceSub2 = countSpaceSub(cArr, i4, TASHKEEL_SPACE_SUB);
            boolean z = false;
            boolean z2 = i6 == 2;
            boolean z3 = i7 == 393216;
            if (z2 && i6 == 2) {
                shiftArray(cArr, i3, i9, 65535);
                while (countSpaceSub > i3) {
                    countSpaceSub--;
                    cArr[countSpaceSub] = SPACE_CHAR;
                }
            }
            if (z3 && i7 == 393216) {
                shiftArray(cArr, i3, i9, TASHKEEL_SPACE_SUB);
                while (countSpaceSub2 > i3) {
                    countSpaceSub2--;
                    cArr[countSpaceSub2] = SPACE_CHAR;
                }
            }
            boolean z4 = i6 == 0;
            boolean z5 = i7 == 524288;
            if (z4 && i6 == 0) {
                shiftArray(cArr, i3, i9, 65535);
                countSpaceSub = flipArray(cArr, i3, i9, countSpaceSub);
                i4 = countSpaceSub - i3;
            }
            if (z5 && i7 == 524288) {
                shiftArray(cArr, i3, i9, TASHKEEL_SPACE_SUB);
                countSpaceSub2 = flipArray(cArr, i3, i9, countSpaceSub2);
                i4 = countSpaceSub2 - i3;
            }
            boolean z6 = i6 == 3 || i6 == 65536;
            if (i7 == 262144) {
                z = true;
            }
            if (z6 && (i6 == 3 || i6 == 65536)) {
                shiftArray(cArr, i3, i9, 65535);
                for (int flipArray = flipArray(cArr, i3, i9, countSpaceSub); flipArray < i9; flipArray++) {
                    cArr[flipArray] = SPACE_CHAR;
                }
            }
            if (z && i7 == 262144) {
                shiftArray(cArr, i3, i9, TASHKEEL_SPACE_SUB);
                for (int flipArray2 = flipArray(cArr, i3, i9, countSpaceSub2); flipArray2 < i9; flipArray2++) {
                    cArr[flipArray2] = SPACE_CHAR;
                }
            }
        }
        return i4;
    }

    private boolean expandCompositCharAtBegin(char[] cArr, int i, int i2, int i3) {
        if (i3 > countSpacesRight(cArr, i, i2)) {
            return true;
        }
        int i4 = i2 + i;
        int i5 = i4 - i3;
        while (true) {
            i5--;
            if (i5 < i) {
                return false;
            }
            char c = cArr[i5];
            if (isNormalizedLamAlefChar(c)) {
                int i6 = i4 - 1;
                cArr[i6] = LAM_CHAR;
                i4 = i6 - 1;
                cArr[i4] = convertNormalizedLamAlef[c - 1628];
            } else {
                i4--;
                cArr[i4] = c;
            }
        }
    }

    private boolean expandCompositCharAtEnd(char[] cArr, int i, int i2, int i3) {
        if (i3 > countSpacesLeft(cArr, i, i2)) {
            return true;
        }
        int i4 = i2 + i;
        for (int i5 = i3 + i; i5 < i4; i5++) {
            char c = cArr[i5];
            if (isNormalizedLamAlefChar(c)) {
                int i6 = i + 1;
                cArr[i] = convertNormalizedLamAlef[c - 1628];
                cArr[i6] = LAM_CHAR;
                i = i6 + 1;
            } else {
                cArr[i] = c;
                i++;
            }
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0032, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0069, code lost:
        return true;
     */
    private boolean expandCompositCharAtNear(char[] cArr, int i, int i2, int i3, int i4, int i5) {
        if (isNormalizedLamAlefChar(cArr[i])) {
            return true;
        }
        int i6 = i2 + i;
        while (true) {
            i6--;
            if (i6 < i) {
                return false;
            }
            char c = cArr[i6];
            if (i5 != 1 || !isNormalizedLamAlefChar(c)) {
                if (i4 != 1 || isSeenTailFamilyChar(c) != 1) {
                    if (i3 == 1 && isYehHamzaChar(c)) {
                        if (i6 <= i) {
                            break;
                        }
                        int i7 = i6 - 1;
                        if (cArr[i7] != ' ') {
                            break;
                        }
                        cArr[i6] = yehHamzaToYeh[c - YEH_HAMZAFE_CHAR];
                        cArr[i7] = HAMZAFE_CHAR;
                    }
                } else if (i6 <= i) {
                    break;
                } else {
                    int i8 = i6 - 1;
                    if (cArr[i8] != ' ') {
                        break;
                    }
                    cArr[i8] = this.tailChar;
                }
            } else if (i6 <= i || cArr[i6 - 1] != ' ') {
                break;
            } else {
                cArr[i6] = LAM_CHAR;
                i6--;
                cArr[i6] = convertNormalizedLamAlef[c - 1628];
            }
        }
        return true;
    }

    private int expandCompositChar(char[] cArr, int i, int i2, int i3, int i4) throws ArabicShapingException {
        int i5 = this.options;
        int i6 = 65539 & i5;
        int i7 = 7340032 & i5;
        int i8 = i5 & YEHHAMZA_MASK;
        if (!this.isLogical && !this.spacesRelativeToTextBeginEnd) {
            if (i6 == 2) {
                i6 = 3;
            } else if (i6 == 3) {
                i6 = 2;
            }
        }
        if (i4 == 1) {
            if (i6 == 65536) {
                if (this.isLogical) {
                    boolean expandCompositCharAtEnd = expandCompositCharAtEnd(cArr, i, i2, i3);
                    if (expandCompositCharAtEnd) {
                        expandCompositCharAtEnd = expandCompositCharAtBegin(cArr, i, i2, i3);
                    }
                    if (expandCompositCharAtEnd) {
                        expandCompositCharAtEnd = expandCompositCharAtNear(cArr, i, i2, 0, 0, 1);
                    }
                    if (!expandCompositCharAtEnd) {
                        return i2;
                    }
                    throw new ArabicShapingException("No spacefor lamalef");
                }
                boolean expandCompositCharAtBegin = expandCompositCharAtBegin(cArr, i, i2, i3);
                if (expandCompositCharAtBegin) {
                    expandCompositCharAtBegin = expandCompositCharAtEnd(cArr, i, i2, i3);
                }
                if (expandCompositCharAtBegin) {
                    expandCompositCharAtBegin = expandCompositCharAtNear(cArr, i, i2, 0, 0, 1);
                }
                if (!expandCompositCharAtBegin) {
                    return i2;
                }
                throw new ArabicShapingException("No spacefor lamalef");
            } else if (i6 == 2) {
                if (!expandCompositCharAtEnd(cArr, i, i2, i3)) {
                    return i2;
                }
                throw new ArabicShapingException("No spacefor lamalef");
            } else if (i6 == 3) {
                if (!expandCompositCharAtBegin(cArr, i, i2, i3)) {
                    return i2;
                }
                throw new ArabicShapingException("No spacefor lamalef");
            } else if (i6 == 1) {
                if (!expandCompositCharAtNear(cArr, i, i2, 0, 0, 1)) {
                    return i2;
                }
                throw new ArabicShapingException("No spacefor lamalef");
            } else if (i6 != 0) {
                return i2;
            } else {
                int i9 = i + i2;
                int i10 = i9 + i3;
                while (true) {
                    i9--;
                    if (i9 < i) {
                        return i2 + i3;
                    }
                    char c = cArr[i9];
                    if (isNormalizedLamAlefChar(c)) {
                        int i11 = i10 - 1;
                        cArr[i11] = LAM_CHAR;
                        i10 = i11 - 1;
                        cArr[i10] = convertNormalizedLamAlef[c - 1628];
                    } else {
                        i10--;
                        cArr[i10] = c;
                    }
                }
            }
        } else if (i7 == 2097152 && expandCompositCharAtNear(cArr, i, i2, 0, 1, 0)) {
            throw new ArabicShapingException("No space for Seen tail expansion");
        } else if (i8 != 16777216 || !expandCompositCharAtNear(cArr, i, i2, 1, 0, 0)) {
            return i2;
        } else {
            throw new ArabicShapingException("No space for YehHamza expansion");
        }
    }

    private int normalize(char[] cArr, int i, int i2) {
        int i3 = i2 + i;
        int i4 = 0;
        while (i < i3) {
            char c = cArr[i];
            if (c >= 65136 && c <= 65276) {
                if (isLamAlefChar(c)) {
                    i4++;
                }
                cArr[i] = (char) convertFEto06[c - 65136];
            }
            i++;
        }
        return i4;
    }

    private int deshapeNormalize(char[] cArr, int i, int i2) {
        int i3 = 0;
        boolean z = (this.options & YEHHAMZA_MASK) == 16777216;
        boolean z2 = (this.options & SEEN_MASK) == 2097152;
        int i4 = i + i2;
        while (i < i4) {
            char c = cArr[i];
            if (z && ((c == 1569 || c == 65152) && i < i2 - 1)) {
                int i5 = i + 1;
                if (isAlefMaksouraChar(cArr[i5])) {
                    cArr[i] = SPACE_CHAR;
                    cArr[i5] = YEH_HAMZA_CHAR;
                    i++;
                }
            }
            if (!z2 || !isTailChar(c) || i >= i2 - 1 || isSeenTailFamilyChar(cArr[i + 1]) != 1) {
                if (c >= 65136 && c <= 65276) {
                    if (isLamAlefChar(c)) {
                        i3++;
                    }
                    cArr[i] = (char) convertFEto06[c - 65136];
                }
                i++;
            } else {
                cArr[i] = SPACE_CHAR;
                i++;
            }
        }
        return i3;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x007f, code lost:
        if (r20[r1] == 1574) goto L_0x0092;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0090, code lost:
        if (r20[r1] == 1574) goto L_0x0092;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0092, code lost:
        r10 = r2;
     */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00aa  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00ad  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x00eb  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x010a  */
    private int shapeUnicode(char[] cArr, int i, int i2, int i3, int i4) throws ArabicShapingException {
        int i5;
        int specialChar;
        char c;
        char c2;
        int normalize = normalize(cArr, i, i2);
        int i6 = 1;
        int i7 = (i + i2) - 1;
        int i8 = i7;
        int link = getLink(cArr[i7]);
        int i9 = 0;
        boolean z = false;
        int i10 = 0;
        int i11 = 0;
        int i12 = 0;
        int i13 = 0;
        int i14 = 0;
        loop0:
        while (true) {
            int i15 = -2;
            while (i7 >= 0) {
                if ((link & 65280) != 0 || isTashkeelChar(cArr[i7])) {
                    int i16 = i7 - 1;
                    int i17 = i14;
                    int i18 = -2;
                    while (i18 < 0) {
                        if (i16 == -1) {
                            i18 = Integer.MAX_VALUE;
                            i17 = 0;
                        } else {
                            i17 = getLink(cArr[i16]);
                            if ((i17 & 4) == 0) {
                                i18 = i16;
                            } else {
                                i16--;
                            }
                        }
                    }
                    if ((link & 32) > 0 && (i12 & 16) > 0) {
                        char changeLamAlef = changeLamAlef(cArr[i7]);
                        if (changeLamAlef != 0) {
                            cArr[i7] = 65535;
                            cArr[i8] = changeLamAlef;
                            i7 = i8;
                        }
                        link = getLink(changeLamAlef);
                        i9 = i6;
                        i12 = i13;
                    }
                    if (i7 <= 0 || cArr[i7 - 1] != ' ') {
                        if (i7 == 0) {
                            if (isSeenFamilyChar(cArr[i7]) != i6) {
                            }
                        }
                        specialChar = specialChar(cArr[i7]);
                        int i19 = shapeTable[i17 & 3][i12 & 3][link & 3];
                        if (specialChar != i6) {
                            i19 &= 1;
                        } else if (specialChar == 2) {
                            if (i4 != 0 || (i12 & 2) == 0 || (i17 & 1) == 0 || cArr[i7] == 1612 || cArr[i7] == 1613 || ((i17 & 32) == 32 && (i12 & 16) == 16)) {
                                if (i4 == 2) {
                                    char c3 = cArr[i7];
                                    c2 = SHADDA06_CHAR;
                                    if (c3 == 1617) {
                                        c = 1617;
                                    }
                                } else {
                                    c2 = SHADDA06_CHAR;
                                }
                                c = c2;
                                i19 = 0;
                                if (specialChar == 2) {
                                    cArr[i7] = (char) ((link >> 8) + 65136 + i19);
                                } else if (i4 != 2 || cArr[i7] == c) {
                                    cArr[i7] = (char) (irrelevantPos[cArr[i7] - 1611] + 65136 + i19);
                                } else {
                                    cArr[i7] = TASHKEEL_SPACE_SUB;
                                    i15 = i18;
                                    i14 = i17;
                                    z = true;
                                }
                                i15 = i18;
                                i14 = i17;
                            } else {
                                c = SHADDA06_CHAR;
                            }
                            i19 = 1;
                            if (specialChar == 2) {
                            }
                            i15 = i18;
                            i14 = i17;
                        }
                        c = SHADDA06_CHAR;
                        if (specialChar == 2) {
                        }
                        i15 = i18;
                        i14 = i17;
                    } else if (isSeenFamilyChar(cArr[i7]) != i6) {
                    }
                    i10 = i6;
                    specialChar = specialChar(cArr[i7]);
                    int i192 = shapeTable[i17 & 3][i12 & 3][link & 3];
                    if (specialChar != i6) {
                    }
                    c = SHADDA06_CHAR;
                    if (specialChar == 2) {
                    }
                    i15 = i18;
                    i14 = i17;
                }
                if ((link & 4) == 0) {
                    i8 = i7;
                    i13 = i12;
                    i12 = link;
                }
                i7--;
                if (i7 == i15) {
                    link = i14;
                    i6 = 1;
                } else {
                    if (i7 != -1) {
                        link = getLink(cArr[i7]);
                    }
                    i6 = 1;
                }
            }
            break loop0;
        }
        if (i9 != 0 || z) {
            i5 = handleGeneratedSpaces(cArr, i, i2);
        } else {
            i5 = i2;
        }
        return (i10 == 0 && i11 == 0) ? i5 : expandCompositChar(cArr, i, i5, normalize, 0);
    }

    private int deShapeUnicode(char[] cArr, int i, int i2, int i3) throws ArabicShapingException {
        int deshapeNormalize = deshapeNormalize(cArr, i, i2);
        return deshapeNormalize != 0 ? expandCompositChar(cArr, i, i2, deshapeNormalize, 1) : i2;
    }

    /* JADX WARNING: Removed duplicated region for block: B:43:0x0093  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00c8  */
    private int internalShape(char[] cArr, int i, int i2, char[] cArr2, int i3, int i4) throws ArabicShapingException {
        char c;
        int i5;
        char c2;
        if (i2 == 0) {
            return 0;
        }
        if (i4 == 0) {
            int i6 = this.options;
            return ((i6 & 24) == 0 || (i6 & 65539) != 0) ? i2 : calculateSize(cArr, i, i2);
        }
        char[] cArr3 = new char[(i2 * 2)];
        System.arraycopy(cArr, i, cArr3, 0, i2);
        if (this.isLogical) {
            invertBuffer(cArr3, 0, i2);
        }
        int i7 = this.options;
        int i8 = i7 & 24;
        if (i8 != 8) {
            if (i8 == 16) {
                i2 = deShapeUnicode(cArr3, 0, i2, i4);
            } else if (i8 == 24) {
                i2 = shapeUnicode(cArr3, 0, i2, i4, 1);
            }
        } else if ((i7 & TASHKEEL_MASK) == 0 || (i7 & TASHKEEL_MASK) == 786432) {
            int shapeUnicode = shapeUnicode(cArr3, 0, i2, i4, 0);
            if ((917504 & this.options) == 786432) {
                i2 = handleTashkeelWithTatweel(cArr3, i2);
            } else {
                i2 = shapeUnicode;
            }
        } else {
            i2 = shapeUnicode(cArr3, 0, i2, i4, 2);
        }
        if (i2 <= i4) {
            int i9 = this.options;
            if ((i9 & 224) != 0) {
                int i10 = i9 & 256;
                if (i10 == 0) {
                    c2 = 1632;
                } else if (i10 != 256) {
                    c = '0';
                    i5 = this.options & 224;
                    if (i5 != 32) {
                        int i11 = c - '0';
                        for (int i12 = 0; i12 < i2; i12++) {
                            char c3 = cArr3[i12];
                            if (c3 <= '9' && c3 >= '0') {
                                cArr3[i12] = (char) (cArr3[i12] + i11);
                            }
                        }
                    } else if (i5 == 64) {
                        char c4 = (char) (c + '\t');
                        int i13 = '0' - c;
                        for (int i14 = 0; i14 < i2; i14++) {
                            char c5 = cArr3[i14];
                            if (c5 <= c4 && c5 >= c) {
                                cArr3[i14] = (char) (cArr3[i14] + i13);
                            }
                        }
                    } else if (i5 == 96) {
                        shapeToArabicDigitsWithContext(cArr3, 0, i2, c, false);
                    } else if (i5 == 128) {
                        shapeToArabicDigitsWithContext(cArr3, 0, i2, c, true);
                    }
                } else {
                    c2 = 1776;
                }
                c = c2;
                i5 = this.options & 224;
                if (i5 != 32) {
                }
            }
            if (this.isLogical) {
                invertBuffer(cArr3, 0, i2);
            }
            System.arraycopy(cArr3, 0, cArr2, i3, i2);
            return i2;
        }
        throw new ArabicShapingException("not enough room for result data");
    }
}
