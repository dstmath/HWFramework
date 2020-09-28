package com.huawei.g11n.tmr.address.de;

import com.huawei.g11n.tmr.util.PatternCache;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SerEngine {
    public static int[] search(String sentence) {
        int start;
        int end;
        int end2;
        int start2;
        boolean flag;
        boolean flag2;
        int start3 = 0;
        int end3 = 0;
        int offset = 0;
        boolean flag3 = true;
        List<int[]> indAddr = new LinkedList<>();
        Matcher mAddr = matchers(sentence, "pRegWT");
        while (mAddr.find()) {
            indAddr.add(new int[]{7, mAddr.start(), mAddr.end()});
        }
        Matcher mAddr2 = matchers(sentence, "pRegED");
        while (mAddr2.find()) {
            offset = noBlack(mAddr2.group(1), mAddr2.group(2), mAddr2.group(3));
            if (1 != 0) {
                start3 = mAddr2.start() + offset;
                end3 = mAddr2.end();
                Matcher mTemp = matchers(sentence.substring(end3), "pReg_city");
                if (mTemp.lookingAt()) {
                    end3 += mTemp.end();
                    if (mTemp.group(1) != null) {
                        flag3 = false;
                    }
                }
                indAddr.add(new int[]{2, start3, end3});
            } else {
                start3 = start3;
            }
        }
        boolean hasPrep = false;
        boolean hasOther = false;
        Matcher mAddr3 = matchers(sentence, "pRegED_Independ");
        while (mAddr3.find()) {
            if (keyNotOnly(mAddr3.group(1), mAddr3.group(3), mAddr3.group(4))) {
                offset = noBlack(mAddr3.group(1), mAddr3.group(2), mAddr3.group(3));
                if (1 != 0) {
                    start2 = mAddr3.start();
                    end2 = mAddr3.end();
                    sentence.substring(start2, end2);
                    String temp = sentence.substring(end2);
                    Matcher prepTemp = matchers(sentence.substring(0, start2), "pRegPrep");
                    Matcher mTemp2 = matchers(temp, "pReg_city");
                    Matcher strTemp = matchers(temp, "pRegST");
                    if (strTemp.lookingAt()) {
                        hasOther = Pattern.compile("(\\p{Blank}+|\\p{Blank}*(-)\\p{Blank}*|\\p{Blank}*,\\p{Blank}*)").matcher(sentence.substring(end2, strTemp.start() + end2)).matches();
                    }
                    if (mTemp2.lookingAt()) {
                        end2 += mTemp2.end();
                        if (mTemp2.group(1) != null) {
                            flag = false;
                        } else {
                            flag = flag3;
                        }
                    } else {
                        flag = flag3;
                    }
                    if (prepTemp.find()) {
                        flag2 = flag;
                        hasPrep = Pattern.compile("\\p{Blank}{0,6}").matcher(sentence.substring(prepTemp.end(), start2)).matches();
                    } else {
                        flag2 = flag;
                    }
                    if (hasPrep || hasOther) {
                        indAddr.add(new int[]{2, start2, end2});
                    }
                    offset = offset;
                    flag3 = flag2;
                } else {
                    end2 = end3;
                    start2 = start3;
                }
            } else {
                end2 = end3;
                start2 = start3;
            }
        }
        Matcher mAddr4 = matchers(sentence, "pRegST");
        while (mAddr4.find()) {
            noBlack(mAddr4.group(1), mAddr4.group(2), mAddr4.group(3));
            if (1 != 0) {
                start = mAddr4.start();
                end = mAddr4.end();
                sentence.substring(start, end);
                Matcher mTemp3 = matchers(sentence.substring(end), "pReg_city");
                if (mTemp3.lookingAt()) {
                    end += mTemp3.end();
                    if (mTemp3.group(1) != null) {
                    }
                }
                indAddr.add(new int[]{1, start, end});
            } else {
                start = start3;
                end = end3;
            }
        }
        int n = indAddr.size();
        if (n == 0) {
            return new int[1];
        }
        int n2 = merge(indAddr, n);
        int[] re = new int[((n2 * 3) + 1)];
        re[0] = n2;
        int i = 0;
        while (i < n2) {
            re[(i * 2) + 1] = indAddr.get(i)[1];
            re[(i * 2) + 2] = indAddr.get(i)[2] - 1;
            i++;
            start3 = start3;
        }
        return re;
    }

    private static int merge(List<int[]> indx, int len) {
        Collections.sort(indx, new Comparator<int[]>() {
            /* class com.huawei.g11n.tmr.address.de.SerEngine.AnonymousClass1 */

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
