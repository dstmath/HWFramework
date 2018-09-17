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
        Matcher mTemp;
        int offset;
        String mat;
        List<int[]> indAddr = new LinkedList();
        Matcher mAddr = matchers(sentence, "pRegWT");
        while (mAddr.find()) {
            indAddr.add(new int[]{7, mAddr.start(), mAddr.end()});
        }
        mAddr = matchers(sentence, "pRegED");
        while (mAddr.find()) {
            start = mAddr.start() + noBlack(mAddr.group(1), mAddr.group(2), mAddr.group(3));
            end = mAddr.end();
            mTemp = matchers(sentence.substring(end), "pReg_city");
            if (mTemp.lookingAt()) {
                end += mTemp.end();
                if (mTemp.group(1) != null) {
                }
            }
            indAddr.add(new int[]{2, start, end});
        }
        boolean hasPrep = false;
        boolean hasOther = false;
        String sub = "";
        mAddr = matchers(sentence, "pRegED_Independ");
        while (mAddr.find()) {
            if (keyNotOnly(mAddr.group(1), mAddr.group(3), mAddr.group(4))) {
                offset = noBlack(mAddr.group(1), mAddr.group(2), mAddr.group(3));
                start = mAddr.start();
                end = mAddr.end();
                mat = sentence.substring(start, end);
                String temp = sentence.substring(end);
                Matcher prepTemp = matchers(sentence.substring(0, start), "pRegPrep");
                mTemp = matchers(temp, "pReg_city");
                Matcher strTemp = matchers(temp, "pRegST");
                if (strTemp.lookingAt()) {
                    hasOther = Pattern.compile("(\\p{Blank}+|\\p{Blank}*(-)\\p{Blank}*|\\p{Blank}*,\\p{Blank}*)").matcher(sentence.substring(end, strTemp.start() + end)).matches();
                }
                if (mTemp.lookingAt()) {
                    end += mTemp.end();
                    if (mTemp.group(1) != null) {
                    }
                }
                if (prepTemp.find()) {
                    hasPrep = Pattern.compile("\\p{Blank}{0,6}").matcher(sentence.substring(prepTemp.end(), start)).matches();
                }
                if (hasPrep || hasOther) {
                    indAddr.add(new int[]{2, start, end});
                }
            }
        }
        mAddr = matchers(sentence, "pRegST");
        while (mAddr.find()) {
            offset = noBlack(mAddr.group(1), mAddr.group(2), mAddr.group(3));
            start = mAddr.start();
            end = mAddr.end();
            mat = sentence.substring(start, end);
            mTemp = matchers(sentence.substring(end), "pReg_city");
            if (mTemp.lookingAt()) {
                end += mTemp.end();
                if (mTemp.group(1) != null) {
                }
            }
            indAddr.add(new int[]{1, start, end});
        }
        int n = indAddr.size();
        if (n == 0) {
            return new int[1];
        }
        n = merge(indAddr, n);
        int[] re = new int[((n * 3) + 1)];
        re[0] = n;
        for (int i = 0; i < n; i++) {
            re[(i * 2) + 1] = ((int[]) indAddr.get(i))[1];
            re[(i * 2) + 2] = ((int[]) indAddr.get(i))[2] - 1;
        }
        return re;
    }

    private static int merge(List<int[]> indx, int len) {
        Collections.sort(indx, new Comparator<int[]>() {
            public int compare(int[] pair1, int[] pair2) {
                if (pair1[1] < pair2[1]) {
                    return -1;
                }
                if (pair1[1] != pair2[1]) {
                    return 1;
                }
                return 0;
            }
        });
        len--;
        int i = 0;
        while (i < len) {
            if (((int[]) indx.get(i))[2] >= ((int[]) indx.get(i + 1))[1] && ((int[]) indx.get(i))[2] < ((int[]) indx.get(i + 1))[2]) {
                if (((int[]) indx.get(i))[2] > ((int[]) indx.get(i + 1))[1]) {
                    ((int[]) indx.get(i))[2] = ((int[]) indx.get(i + 1))[1];
                }
                if ((((int[]) indx.get(i + 1))[0] & ((int[]) indx.get(i))[0]) == 0 && !(((int[]) indx.get(i))[0] == 1 && ((int[]) indx.get(i + 1))[0] == 2)) {
                    ((int[]) indx.get(i))[2] = ((int[]) indx.get(i + 1))[2];
                    ((int[]) indx.get(i))[0] = ((int[]) indx.get(i + 1))[0] | ((int[]) indx.get(i))[0];
                    indx.remove(i + 1);
                    len--;
                    i--;
                }
            }
            i++;
        }
        return len + 1;
    }

    private static int noBlack(String wordsFirst, String wordSecond, String wordBlank) {
        if (wordsFirst.length() <= 0) {
            return wordBlank.length() <= 0 ? !matchers(wordSecond, "pRegBlackKeyUnIndi").matches() ? 0 : -1 : !matchers(wordSecond, "pRegBlackKeyIndi").matches() ? 0 : -1;
        } else {
            Matcher mBlack = matchers(wordsFirst, "pRegBlackKeyIndi_withBlank");
            if (!mBlack.lookingAt()) {
                return 0;
            }
            int offset = mBlack.group().length();
            if (offset < wordsFirst.length() || wordBlank.length() > 0 || !matchers(wordSecond, "pRegBlackKeyUnIndi").matches()) {
                return offset;
            }
            return -1;
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
