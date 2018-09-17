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
        StringBuffer dest;
        int i;
        int c;
        switch (options & 10) {
            case 0:
                return src;
            case 2:
                dest = new StringBuffer(src.length());
                i = 0;
                do {
                    c = UTF16.charAt(src, i);
                    i += UTF16.getCharCount(c);
                    UTF16.append(dest, UCharacter.getMirror(c));
                } while (i < src.length());
                return dest.toString();
            case 8:
                StringBuilder dest2 = new StringBuilder(src.length());
                i = 0;
                while (true) {
                    int i2 = i + 1;
                    char c2 = src.charAt(i);
                    if (!Bidi.IsBidiControlChar(c2)) {
                        dest2.append(c2);
                    }
                    if (i2 >= src.length()) {
                        return dest2.toString();
                    }
                    i = i2;
                }
            default:
                dest = new StringBuffer(src.length());
                i = 0;
                do {
                    c = UTF16.charAt(src, i);
                    i += UTF16.getCharCount(c);
                    if (!Bidi.IsBidiControlChar(c)) {
                        UTF16.append(dest, UCharacter.getMirror(c));
                    }
                } while (i < src.length());
                return dest.toString();
        }
    }

    private static String doWriteForward(char[] text, int start, int limit, int options) {
        return doWriteForward(new String(text, start, limit - start), options);
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x0053 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x007d A:{LOOP_END, LOOP:3: B:15:0x005c->B:22:0x007d} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static String writeReverse(String src, int options) {
        StringBuffer dest = new StringBuffer(src.length());
        int srcLength;
        int i;
        int c;
        switch (options & 11) {
            case 0:
                srcLength = src.length();
                do {
                    i = srcLength;
                    srcLength -= UTF16.getCharCount(UTF16.charAt(src, srcLength - 1));
                    dest.append(src.substring(srcLength, i));
                } while (srcLength > 0);
                break;
            case 1:
                srcLength = src.length();
                while (true) {
                    i = srcLength;
                    do {
                        c = UTF16.charAt(src, srcLength - 1);
                        srcLength -= UTF16.getCharCount(c);
                        if (srcLength > 0) {
                        }
                        dest.append(src.substring(srcLength, i));
                        if (srcLength > 0) {
                            break;
                        }
                    } while (IsCombining(UCharacter.getType(c)));
                    dest.append(src.substring(srcLength, i));
                    if (srcLength > 0) {
                    }
                }
            default:
                srcLength = src.length();
                while (true) {
                    i = srcLength;
                    c = UTF16.charAt(src, srcLength - 1);
                    srcLength -= UTF16.getCharCount(c);
                    if ((options & 1) != 0) {
                        while (srcLength > 0 && IsCombining(UCharacter.getType(c))) {
                            c = UTF16.charAt(src, srcLength - 1);
                            srcLength -= UTF16.getCharCount(c);
                        }
                    }
                    if ((options & 8) == 0 || !Bidi.IsBidiControlChar(c)) {
                        int j = srcLength;
                        if ((options & 2) != 0) {
                            c = UCharacter.getMirror(c);
                            UTF16.append(dest, c);
                            j += UTF16.getCharCount(c);
                        }
                        dest.append(src.substring(j, i));
                    }
                    if (srcLength <= 0) {
                        break;
                    }
                }
                break;
        }
        return dest.toString();
    }

    static String doWriteReverse(char[] text, int start, int limit, int options) {
        return writeReverse(new String(text, start, limit - start), options);
    }

    static String writeReordered(Bidi bidi, int options) {
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
        byte[] dirProps;
        int run;
        BidiRun bidiRun;
        if ((options & 16) != 0) {
            if ((options & 4) != 0) {
                dirProps = bidi.dirProps;
                run = runCount;
                while (true) {
                    run--;
                    if (run < 0) {
                        break;
                    }
                    bidiRun = bidi.getVisualRun(run);
                    if (bidiRun.isEvenRun()) {
                        if (dirProps[bidiRun.limit - 1] != (byte) 0) {
                            dest.append(LRM_CHAR);
                        }
                        dest.append(doWriteReverse(text, bidiRun.start, bidiRun.limit, options & -3));
                        if (dirProps[bidiRun.start] != (byte) 0) {
                            dest.append(LRM_CHAR);
                        }
                    } else {
                        if ((Bidi.DirPropFlag(dirProps[bidiRun.start]) & MASK_R_AL) == 0) {
                            dest.append(RLM_CHAR);
                        }
                        dest.append(doWriteForward(text, bidiRun.start, bidiRun.limit, options));
                        if ((Bidi.DirPropFlag(dirProps[bidiRun.limit - 1]) & MASK_R_AL) == 0) {
                            dest.append(RLM_CHAR);
                        }
                    }
                }
            } else {
                run = runCount;
                while (true) {
                    run--;
                    if (run < 0) {
                        break;
                    }
                    bidiRun = bidi.getVisualRun(run);
                    if (bidiRun.isEvenRun()) {
                        dest.append(doWriteReverse(text, bidiRun.start, bidiRun.limit, options & -3));
                    } else {
                        dest.append(doWriteForward(text, bidiRun.start, bidiRun.limit, options));
                    }
                }
            }
        } else if ((options & 4) == 0) {
            for (run = 0; run < runCount; run++) {
                bidiRun = bidi.getVisualRun(run);
                if (bidiRun.isEvenRun()) {
                    dest.append(doWriteForward(text, bidiRun.start, bidiRun.limit, options & -3));
                } else {
                    dest.append(doWriteReverse(text, bidiRun.start, bidiRun.limit, options));
                }
            }
        } else {
            dirProps = bidi.dirProps;
            for (run = 0; run < runCount; run++) {
                bidiRun = bidi.getVisualRun(run);
                int markFlag = bidi.runs[run].insertRemove;
                if (markFlag < 0) {
                    markFlag = 0;
                }
                char uc;
                if (bidiRun.isEvenRun()) {
                    if (bidi.isInverse() && dirProps[bidiRun.start] != (byte) 0) {
                        markFlag |= 1;
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
                    dest.append(doWriteForward(text, bidiRun.start, bidiRun.limit, options & -3));
                    if (bidi.isInverse() && dirProps[bidiRun.limit - 1] != (byte) 0) {
                        markFlag |= 2;
                    }
                    if ((markFlag & 2) != 0) {
                        uc = LRM_CHAR;
                    } else if ((markFlag & 8) != 0) {
                        uc = RLM_CHAR;
                    } else {
                        uc = 0;
                    }
                    if (uc != 0) {
                        dest.append(uc);
                    }
                } else {
                    if (bidi.isInverse() && (bidi.testDirPropFlagAt(MASK_R_AL, bidiRun.limit - 1) ^ 1) != 0) {
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
                    dest.append(doWriteReverse(text, bidiRun.start, bidiRun.limit, options));
                    if (bidi.isInverse() && (Bidi.DirPropFlag(dirProps[bidiRun.start]) & MASK_R_AL) == 0) {
                        markFlag |= 8;
                    }
                    if ((markFlag & 2) != 0) {
                        uc = LRM_CHAR;
                    } else if ((markFlag & 8) != 0) {
                        uc = RLM_CHAR;
                    } else {
                        uc = 0;
                    }
                    if (uc != 0) {
                        dest.append(uc);
                    }
                }
            }
        }
        return dest.toString();
    }
}
