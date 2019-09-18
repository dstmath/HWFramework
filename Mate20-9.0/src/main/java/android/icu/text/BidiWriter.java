package android.icu.text;

import android.icu.lang.UCharacter;

final class BidiWriter {
    static final char LRM_CHAR = '‎';
    static final int MASK_R_AL = 8194;
    static final char RLM_CHAR = '‏';

    BidiWriter() {
    }

    private static boolean IsCombining(int type) {
        return ((1 << type) & 448) != 0;
    }

    private static String doWriteForward(String src, int options) {
        int i = options & 10;
        if (i == 0) {
            return src;
        }
        int c = 0;
        if (i == 2) {
            StringBuffer dest = new StringBuffer(src.length());
            while (true) {
                int i2 = c;
                int i3 = UTF16.charAt(src, i2);
                int i4 = i2 + UTF16.getCharCount(i3);
                UTF16.append(dest, UCharacter.getMirror(i3));
                if (i4 >= src.length()) {
                    return dest.toString();
                }
                c = i4;
            }
        } else if (i != 8) {
            StringBuffer dest2 = new StringBuffer(src.length());
            while (true) {
                int i5 = c;
                int i6 = UTF16.charAt(src, i5);
                int i7 = i5 + UTF16.getCharCount(i6);
                if (!Bidi.IsBidiControlChar(i6)) {
                    UTF16.append(dest2, UCharacter.getMirror(i6));
                }
                if (i7 >= src.length()) {
                    return dest2.toString();
                }
                c = i7;
            }
        } else {
            StringBuilder dest3 = new StringBuilder(src.length());
            do {
                int i8 = c;
                c = i8 + 1;
                char c2 = src.charAt(i8);
                if (!Bidi.IsBidiControlChar(c2)) {
                    dest3.append(c2);
                }
            } while (c < src.length());
            return dest3.toString();
        }
    }

    private static String doWriteForward(char[] text, int start, int limit, int options) {
        return doWriteForward(new String(text, start, limit - start), options);
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x003a A[LOOP:0: B:4:0x0017->B:11:0x003a, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00a4 A[SYNTHETIC] */
    static String writeReverse(String src, int options) {
        int c;
        StringBuffer dest = new StringBuffer(src.length());
        switch (options & 11) {
            case 0:
                int srcLength = src.length();
                do {
                    int i = srcLength;
                    srcLength -= UTF16.getCharCount(UTF16.charAt(src, srcLength - 1));
                    dest.append(src.substring(srcLength, i));
                } while (srcLength > 0);
                break;
            case 1:
                int srcLength2 = src.length();
                while (true) {
                    int srcLength3 = srcLength2;
                    do {
                        c = UTF16.charAt(src, srcLength3 - 1);
                        srcLength3 -= UTF16.getCharCount(c);
                        if (srcLength3 > 0) {
                        }
                        dest.append(src.substring(srcLength3, srcLength2));
                        if (srcLength3 > 0) {
                            break;
                        } else {
                            srcLength2 = srcLength3;
                        }
                    } while (IsCombining(UCharacter.getType(c)));
                    dest.append(src.substring(srcLength3, srcLength2));
                    if (srcLength3 > 0) {
                    }
                }
            default:
                int srcLength4 = src.length();
                do {
                    int i2 = srcLength4;
                    int c2 = UTF16.charAt(src, srcLength4 - 1);
                    srcLength4 -= UTF16.getCharCount(c2);
                    if ((options & 1) != 0) {
                        while (srcLength4 > 0 && IsCombining(UCharacter.getType(c2))) {
                            c2 = UTF16.charAt(src, srcLength4 - 1);
                            srcLength4 -= UTF16.getCharCount(c2);
                        }
                    }
                    if ((options & 8) == 0 || !Bidi.IsBidiControlChar(c2)) {
                        int j = srcLength4;
                        if ((options & 2) != 0) {
                            int c3 = UCharacter.getMirror(c2);
                            UTF16.append(dest, c3);
                            j += UTF16.getCharCount(c3);
                        }
                        dest.append(src.substring(j, i2));
                        continue;
                    }
                } while (srcLength4 > 0);
                break;
        }
        return dest.toString();
    }

    static String doWriteReverse(char[] text, int start, int limit, int options) {
        return writeReverse(new String(text, start, limit - start), options);
    }

    static String writeReordered(Bidi bidi, int options) {
        char uc;
        char uc2;
        char uc3;
        char uc4;
        char[] text = bidi.text;
        int runCount = bidi.countRuns();
        if ((bidi.reorderingOptions & 1) != 0) {
            options = (options | 4) & -9;
        }
        if ((bidi.reorderingOptions & 2) != 0) {
            options = (options | 8) & -5;
        }
        if (!(bidi.reorderingMode == 4 || bidi.reorderingMode == 5 || bidi.reorderingMode == 6 || bidi.reorderingMode == 3)) {
            options &= -5;
        }
        StringBuilder dest = new StringBuilder((options & 4) != 0 ? bidi.length * 2 : bidi.length);
        if ((options & 16) == 0) {
            int run = 0;
            if ((options & 4) == 0) {
                while (true) {
                    int run2 = run;
                    if (run2 >= runCount) {
                        break;
                    }
                    BidiRun bidiRun = bidi.getVisualRun(run2);
                    if (bidiRun.isEvenRun()) {
                        dest.append(doWriteForward(text, bidiRun.start, bidiRun.limit, options & -3));
                    } else {
                        dest.append(doWriteReverse(text, bidiRun.start, bidiRun.limit, options));
                    }
                    run = run2 + 1;
                }
            } else {
                byte[] dirProps = bidi.dirProps;
                int run3 = 0;
                while (run3 < runCount) {
                    BidiRun bidiRun2 = bidi.getVisualRun(run3);
                    int markFlag = bidi.runs[run3].insertRemove;
                    if (markFlag < 0) {
                        markFlag = 0;
                    }
                    if (bidiRun2.isEvenRun()) {
                        if (bidi.isInverse() && dirProps[bidiRun2.start] != 0) {
                            markFlag |= 1;
                        }
                        if ((markFlag & 1) != 0) {
                            uc3 = LRM_CHAR;
                        } else if ((markFlag & 4) != 0) {
                            uc3 = RLM_CHAR;
                        } else {
                            uc3 = 0;
                        }
                        if (uc3 != 0) {
                            dest.append(uc3);
                        }
                        dest.append(doWriteForward(text, bidiRun2.start, bidiRun2.limit, options & -3));
                        if (bidi.isInverse() && dirProps[bidiRun2.limit - 1] != 0) {
                            markFlag |= 2;
                        }
                        if ((markFlag & 2) != 0) {
                            uc4 = LRM_CHAR;
                        } else if ((markFlag & 8) != 0) {
                            uc4 = RLM_CHAR;
                        } else {
                            uc4 = 0;
                        }
                        if (uc4 != 0) {
                            dest.append(uc4);
                        }
                    } else {
                        if (bidi.isInverse() != 0 && !bidi.testDirPropFlagAt(MASK_R_AL, bidiRun2.limit - 1)) {
                            markFlag |= 4;
                        }
                        if ((markFlag & 1) != 0) {
                            uc = LRM_CHAR;
                        } else if ((markFlag & 4) != 0) {
                            uc = RLM_CHAR;
                        } else {
                            uc = 0;
                        }
                        if (uc != 0) {
                            dest.append(uc);
                        }
                        dest.append(doWriteReverse(text, bidiRun2.start, bidiRun2.limit, options));
                        if (bidi.isInverse() && (Bidi.DirPropFlag(dirProps[bidiRun2.start]) & MASK_R_AL) == 0) {
                            markFlag |= 8;
                        }
                        if ((markFlag & 2) != 0) {
                            uc2 = LRM_CHAR;
                        } else if ((markFlag & 8) != 0) {
                            uc2 = RLM_CHAR;
                        } else {
                            uc2 = 0;
                        }
                        if (uc2 != 0) {
                            dest.append(uc2);
                        }
                    }
                    run3++;
                }
                int i = run3;
            }
        } else if ((options & 4) == 0) {
            int run4 = runCount;
            while (true) {
                run4--;
                if (run4 < 0) {
                    break;
                }
                BidiRun bidiRun3 = bidi.getVisualRun(run4);
                if (bidiRun3.isEvenRun()) {
                    dest.append(doWriteReverse(text, bidiRun3.start, bidiRun3.limit, options & -3));
                } else {
                    dest.append(doWriteForward(text, bidiRun3.start, bidiRun3.limit, options));
                }
            }
        } else {
            byte[] dirProps2 = bidi.dirProps;
            int run5 = runCount;
            while (true) {
                run5--;
                if (run5 < 0) {
                    break;
                }
                BidiRun bidiRun4 = bidi.getVisualRun(run5);
                if (bidiRun4.isEvenRun()) {
                    if (dirProps2[bidiRun4.limit - 1] != 0) {
                        dest.append(LRM_CHAR);
                    }
                    dest.append(doWriteReverse(text, bidiRun4.start, bidiRun4.limit, options & -3));
                    if (dirProps2[bidiRun4.start] != 0) {
                        dest.append(LRM_CHAR);
                    }
                } else {
                    if ((Bidi.DirPropFlag(dirProps2[bidiRun4.start]) & MASK_R_AL) == 0) {
                        dest.append(RLM_CHAR);
                    }
                    dest.append(doWriteForward(text, bidiRun4.start, bidiRun4.limit, options));
                    if ((Bidi.DirPropFlag(dirProps2[bidiRun4.limit - 1]) & MASK_R_AL) == 0) {
                        dest.append(RLM_CHAR);
                    }
                }
            }
            int i2 = run5;
        }
        return dest.toString();
    }
}
