package com.huawei.g11n.tmr.address.de;

import com.huawei.g11n.tmr.util.PatternCache;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SerEngine {
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0184  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x01a2  */
    public static int[] search(String sentence) {
        int i;
        int start;
        int i2;
        int i3;
        int i4;
        boolean flag;
        int offset;
        String sub;
        boolean flag2;
        boolean flag3;
        int i5;
        String str = sentence;
        int start2 = 0;
        boolean flag4 = true;
        List<int[]> indAddr = new LinkedList<>();
        Matcher mAddr = matchers(str, "pRegWT");
        while (true) {
            i = 3;
            start = 1;
            if (!mAddr.find()) {
                break;
            }
            indAddr.add(new int[]{7, mAddr.start(), mAddr.end()});
        }
        Matcher mAddr2 = matchers(str, "pRegED");
        while (mAddr2.find()) {
            int offset2 = noBlack(mAddr2.group(start), mAddr2.group(2), mAddr2.group(3));
            if (1 != 0) {
                start2 = mAddr2.start() + offset2;
                int end = mAddr2.end();
                Matcher mTemp = matchers(str.substring(end), "pReg_city");
                if (mTemp.lookingAt()) {
                    end += mTemp.end();
                    i5 = 1;
                    if (mTemp.group(1) != null) {
                        flag4 = false;
                    }
                } else {
                    i5 = 1;
                }
                i = 3;
                int[] iArr = new int[3];
                iArr[0] = 2;
                iArr[i5] = start2;
                iArr[2] = end;
                indAddr.add(iArr);
                start = i5;
            } else {
                i = 3;
                start = 1;
            }
        }
        boolean hasPrep = false;
        boolean hasOther = false;
        String sub2 = "";
        Matcher mAddr3 = matchers(str, "pRegED_Independ");
        while (mAddr3.find()) {
            if (keyNotOnly(mAddr3.group(start), mAddr3.group(i), mAddr3.group(4))) {
                int start3 = start2;
                int offset3 = noBlack(mAddr3.group(1), mAddr3.group(2), mAddr3.group(3));
                if (1 != 0) {
                    start2 = mAddr3.start();
                    int end2 = mAddr3.end();
                    String substring = str.substring(start2, end2);
                    String temp = str.substring(end2);
                    Matcher prepTemp = matchers(str.substring(0, start2), "pRegPrep");
                    Matcher mTemp2 = matchers(temp, "pReg_city");
                    Matcher strTemp = matchers(temp, "pRegST");
                    if (strTemp.lookingAt()) {
                        sub = str.substring(end2, strTemp.start() + end2);
                        offset = offset3;
                        flag = flag4;
                        hasOther = Pattern.compile("(\\p{Blank}+|\\p{Blank}*(-)\\p{Blank}*|\\p{Blank}*,\\p{Blank}*)").matcher(sub).matches();
                    } else {
                        offset = offset3;
                        flag = flag4;
                        sub = sub2;
                    }
                    if (mTemp2.lookingAt() != 0) {
                        end2 += mTemp2.end();
                        if (mTemp2.group(1) != null) {
                            flag2 = false;
                            if (!prepTemp.find()) {
                                int prepEnd = prepTemp.end();
                                String sub3 = str.substring(prepEnd, start2);
                                int i6 = prepEnd;
                                flag3 = flag2;
                                hasPrep = Pattern.compile("\\p{Blank}{0,6}").matcher(sub3).matches();
                                sub2 = sub3;
                            } else {
                                flag3 = flag2;
                                sub2 = sub;
                            }
                            if (!hasPrep || hasOther) {
                                i4 = 1;
                                indAddr.add(new int[]{2, start2, end2});
                            } else {
                                i4 = 1;
                            }
                            int i7 = offset;
                            flag4 = flag3;
                            i3 = 3;
                        }
                    }
                    flag2 = flag;
                    if (!prepTemp.find()) {
                    }
                    if (!hasPrep) {
                    }
                    i4 = 1;
                    indAddr.add(new int[]{2, start2, end2});
                    int i72 = offset;
                    flag4 = flag3;
                    i3 = 3;
                } else {
                    boolean z = flag4;
                    start2 = start3;
                    i3 = 3;
                    i4 = 1;
                }
            } else {
                boolean z2 = flag4;
                i3 = 3;
                i4 = 1;
            }
        }
        Matcher mAddr4 = matchers(str, "pRegST");
        while (mAddr4.find()) {
            int start4 = start2;
            int offset4 = noBlack(mAddr4.group(start), mAddr4.group(2), mAddr4.group(i));
            if (1 != 0) {
                start2 = mAddr4.start();
                int end3 = mAddr4.end();
                String substring2 = str.substring(start2, end3);
                Matcher mTemp3 = matchers(str.substring(end3), "pReg_city");
                if (mTemp3.lookingAt()) {
                    end3 += mTemp3.end();
                    i2 = 1;
                    if (mTemp3.group(1) != null) {
                    }
                } else {
                    i2 = 1;
                }
                int offset5 = offset4;
                int[] iArr2 = new int[i];
                iArr2[0] = 1;
                iArr2[i2] = start2;
                iArr2[2] = end3;
                indAddr.add(iArr2);
                int i8 = offset5;
            } else {
                start2 = start4;
                i2 = 1;
            }
        }
        int n = indAddr.size();
        if (n == 0) {
            return new int[start];
        }
        int n2 = merge(indAddr, n);
        int[] re = new int[((i * n2) + start)];
        re[0] = n2;
        int i9 = 0;
        while (i9 < n2) {
            re[(2 * i9) + 1] = indAddr.get(i9)[1];
            re[(2 * i9) + 2] = indAddr.get(i9)[2] - 1;
            i9++;
            start2 = start2;
        }
        return re;
    }

    private static int merge(List<int[]> indx, int len) {
        Collections.sort(indx, new Comparator<int[]>() {
            public int compare(int[] pair1, int[] pair2) {
                if (pair1[1] < pair2[1]) {
                    return -1;
                }
                if (pair1[1] == pair2[1]) {
                    return 0;
                }
                return 1;
            }
        });
        int len2 = len - 1;
        int i = 0;
        while (i < len2) {
            if (indx.get(i)[2] >= indx.get(i + 1)[1] && indx.get(i)[2] < indx.get(i + 1)[2]) {
                if (indx.get(i)[2] > indx.get(i + 1)[1]) {
                    indx.get(i)[2] = indx.get(i + 1)[1];
                }
                if ((indx.get(i)[0] & indx.get(i + 1)[0]) == 0 && !(indx.get(i)[0] == 1 && indx.get(i + 1)[0] == 2)) {
                    indx.get(i)[2] = indx.get(i + 1)[2];
                    indx.get(i)[0] = indx.get(i)[0] | indx.get(i + 1)[0];
                    indx.remove(i + 1);
                    len2--;
                    i--;
                }
            }
            i++;
        }
        return len2 + 1;
    }

    private static int noBlack(String wordsFirst, String wordSecond, String wordBlank) {
        if (wordsFirst.length() > 0) {
            Matcher mBlack = matchers(wordsFirst, "pRegBlackKeyIndi_withBlank");
            if (!mBlack.lookingAt()) {
                return 0;
            }
            int offset = mBlack.group().length();
            if (offset >= wordsFirst.length() && wordBlank.length() <= 0 && matchers(wordSecond, "pRegBlackKeyUnIndi").matches()) {
                return -1;
            }
            return offset;
        } else if (wordBlank.length() > 0) {
            if (matchers(wordSecond, "pRegBlackKeyIndi").matches()) {
                return -1;
            }
            return 0;
        } else if (matchers(wordSecond, "pRegBlackKeyUnIndi").matches()) {
            return -1;
        } else {
            return 0;
        }
    }

    private static boolean keyNotOnly(String wordsFirst, String wordBlank, String keyword) {
        if (wordsFirst == null || wordBlank == null || keyword == null) {
            return false;
        }
        if (matchers(keyword, "pRegBlackKeyNoSingal").matches() && wordBlank.length() == 0 && wordsFirst.length() == 0) {
            return false;
        }
        return true;
    }

    private static Matcher matchers(String t, String reg) {
        return PatternCache.getPattern(reg, "com.huawei.g11n.tmr.address.de.ReguExp").matcher(t);
    }
}
