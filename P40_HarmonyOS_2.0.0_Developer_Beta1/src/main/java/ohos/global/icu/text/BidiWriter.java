package ohos.global.icu.text;

import ohos.global.icu.lang.UCharacter;

final class BidiWriter {
    static final char LRM_CHAR = 8206;
    static final int MASK_R_AL = 8194;
    static final char RLM_CHAR = 8207;

    private static boolean IsCombining(int i) {
        return ((1 << i) & 448) != 0;
    }

    BidiWriter() {
    }

    private static String doWriteForward(String str, int i) {
        int i2 = i & 10;
        if (i2 == 0) {
            return str;
        }
        int i3 = 0;
        if (i2 == 2) {
            StringBuffer stringBuffer = new StringBuffer(str.length());
            do {
                int charAt = UTF16.charAt(str, i3);
                i3 += UTF16.getCharCount(charAt);
                UTF16.append(stringBuffer, UCharacter.getMirror(charAt));
            } while (i3 < str.length());
            return stringBuffer.toString();
        } else if (i2 != 8) {
            StringBuffer stringBuffer2 = new StringBuffer(str.length());
            do {
                int charAt2 = UTF16.charAt(str, i3);
                i3 += UTF16.getCharCount(charAt2);
                if (!Bidi.IsBidiControlChar(charAt2)) {
                    UTF16.append(stringBuffer2, UCharacter.getMirror(charAt2));
                }
            } while (i3 < str.length());
            return stringBuffer2.toString();
        } else {
            StringBuilder sb = new StringBuilder(str.length());
            while (true) {
                int i4 = i3 + 1;
                char charAt3 = str.charAt(i3);
                if (!Bidi.IsBidiControlChar(charAt3)) {
                    sb.append(charAt3);
                }
                if (i4 >= str.length()) {
                    return sb.toString();
                }
                i3 = i4;
            }
        }
    }

    private static String doWriteForward(char[] cArr, int i, int i2, int i3) {
        return doWriteForward(new String(cArr, i, i2 - i), i3);
    }

    static String writeReverse(String str, int i) {
        int charAt;
        int i2;
        StringBuffer stringBuffer = new StringBuffer(str.length());
        int i3 = i & 11;
        if (i3 == 0) {
            int length = str.length();
            while (true) {
                int charCount = length - UTF16.getCharCount(UTF16.charAt(str, length - 1));
                stringBuffer.append(str.substring(charCount, length));
                if (charCount <= 0) {
                    break;
                }
                length = charCount;
            }
        } else if (i3 != 1) {
            int length2 = str.length();
            while (true) {
                int charAt2 = UTF16.charAt(str, length2 - 1);
                int charCount2 = length2 - UTF16.getCharCount(charAt2);
                if ((i & 1) != 0) {
                    while (charCount2 > 0 && IsCombining(UCharacter.getType(charAt2))) {
                        charAt2 = UTF16.charAt(str, charCount2 - 1);
                        charCount2 -= UTF16.getCharCount(charAt2);
                    }
                }
                if ((i & 8) == 0 || !Bidi.IsBidiControlChar(charAt2)) {
                    if ((i & 2) != 0) {
                        int mirror = UCharacter.getMirror(charAt2);
                        UTF16.append(stringBuffer, mirror);
                        i2 = UTF16.getCharCount(mirror) + charCount2;
                    } else {
                        i2 = charCount2;
                    }
                    stringBuffer.append(str.substring(i2, length2));
                }
                if (charCount2 <= 0) {
                    break;
                }
                length2 = charCount2;
            }
        } else {
            int length3 = str.length();
            while (true) {
                int i4 = length3;
                do {
                    charAt = UTF16.charAt(str, i4 - 1);
                    i4 -= UTF16.getCharCount(charAt);
                    if (i4 <= 0) {
                        break;
                    }
                } while (IsCombining(UCharacter.getType(charAt)));
                stringBuffer.append(str.substring(i4, length3));
                if (i4 <= 0) {
                    break;
                }
                length3 = i4;
            }
        }
        return stringBuffer.toString();
    }

    static String doWriteReverse(char[] cArr, int i, int i2, int i3) {
        return writeReverse(new String(cArr, i, i2 - i), i3);
    }

    static String writeReordered(Bidi bidi, int i) {
        char[] cArr = bidi.text;
        int countRuns = bidi.countRuns();
        if ((bidi.reorderingOptions & 1) != 0) {
            i = (i | 4) & -9;
        }
        if ((bidi.reorderingOptions & 2) != 0) {
            i = (i | 8) & -5;
        }
        if (!(bidi.reorderingMode == 4 || bidi.reorderingMode == 5 || bidi.reorderingMode == 6 || bidi.reorderingMode == 3)) {
            i &= -5;
        }
        int i2 = i & 4;
        StringBuilder sb = new StringBuilder(i2 != 0 ? bidi.length * 2 : bidi.length);
        if ((i & 16) == 0) {
            if (i2 == 0) {
                for (int i3 = 0; i3 < countRuns; i3++) {
                    BidiRun visualRun = bidi.getVisualRun(i3);
                    if (visualRun.isEvenRun()) {
                        sb.append(doWriteForward(cArr, visualRun.start, visualRun.limit, i & -3));
                    } else {
                        sb.append(doWriteReverse(cArr, visualRun.start, visualRun.limit, i));
                    }
                }
            } else {
                byte[] bArr = bidi.dirProps;
                for (int i4 = 0; i4 < countRuns; i4++) {
                    BidiRun visualRun2 = bidi.getVisualRun(i4);
                    int i5 = bidi.runs[i4].insertRemove;
                    if (i5 < 0) {
                        i5 = 0;
                    }
                    if (visualRun2.isEvenRun()) {
                        if (bidi.isInverse() && bArr[visualRun2.start] != 0) {
                            i5 |= 1;
                        }
                        char c = (i5 & 1) != 0 ? 8206 : (i5 & 4) != 0 ? (char) 8207 : 0;
                        if (c != 0) {
                            sb.append(c);
                        }
                        sb.append(doWriteForward(cArr, visualRun2.start, visualRun2.limit, i & -3));
                        if (bidi.isInverse() && bArr[visualRun2.limit - 1] != 0) {
                            i5 |= 2;
                        }
                        char c2 = (i5 & 2) != 0 ? 8206 : (i5 & 8) != 0 ? (char) 8207 : 0;
                        if (c2 != 0) {
                            sb.append(c2);
                        }
                    } else {
                        if (bidi.isInverse() && !bidi.testDirPropFlagAt(MASK_R_AL, visualRun2.limit - 1)) {
                            i5 |= 4;
                        }
                        char c3 = (i5 & 1) != 0 ? 8206 : (i5 & 4) != 0 ? (char) 8207 : 0;
                        if (c3 != 0) {
                            sb.append(c3);
                        }
                        sb.append(doWriteReverse(cArr, visualRun2.start, visualRun2.limit, i));
                        if (bidi.isInverse() && (Bidi.DirPropFlag(bArr[visualRun2.start]) & MASK_R_AL) == 0) {
                            i5 |= 8;
                        }
                        char c4 = (i5 & 2) != 0 ? 8206 : (i5 & 8) != 0 ? (char) 8207 : 0;
                        if (c4 != 0) {
                            sb.append(c4);
                        }
                    }
                }
            }
        } else if (i2 != 0) {
            byte[] bArr2 = bidi.dirProps;
            while (true) {
                countRuns--;
                if (countRuns < 0) {
                    break;
                }
                BidiRun visualRun3 = bidi.getVisualRun(countRuns);
                if (visualRun3.isEvenRun()) {
                    if (bArr2[visualRun3.limit - 1] != 0) {
                        sb.append(LRM_CHAR);
                    }
                    sb.append(doWriteReverse(cArr, visualRun3.start, visualRun3.limit, i & -3));
                    if (bArr2[visualRun3.start] != 0) {
                        sb.append(LRM_CHAR);
                    }
                } else {
                    if ((Bidi.DirPropFlag(bArr2[visualRun3.start]) & MASK_R_AL) == 0) {
                        sb.append(RLM_CHAR);
                    }
                    sb.append(doWriteForward(cArr, visualRun3.start, visualRun3.limit, i));
                    if ((Bidi.DirPropFlag(bArr2[visualRun3.limit - 1]) & MASK_R_AL) == 0) {
                        sb.append(RLM_CHAR);
                    }
                }
            }
        } else {
            while (true) {
                countRuns--;
                if (countRuns < 0) {
                    break;
                }
                BidiRun visualRun4 = bidi.getVisualRun(countRuns);
                if (visualRun4.isEvenRun()) {
                    sb.append(doWriteReverse(cArr, visualRun4.start, visualRun4.limit, i & -3));
                } else {
                    sb.append(doWriteForward(cArr, visualRun4.start, visualRun4.limit, i));
                }
            }
        }
        return sb.toString();
    }
}
