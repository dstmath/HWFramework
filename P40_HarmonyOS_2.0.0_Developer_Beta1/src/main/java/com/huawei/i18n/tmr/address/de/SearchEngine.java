package com.huawei.i18n.tmr.address.de;

import com.huawei.i18n.tmr.address.util.PatternCache;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchEngine {
    public static int[] search(String input) {
        List<int[]> addressList = new LinkedList<>();
        matchWhite(input, addressList);
        matchSingleBuilding(input, addressList);
        matchNotSingleBuilding(input, addressList);
        matchStreet(input, addressList);
        return addressList.size() == 0 ? new int[]{0} : getAddressIndex(addressList, merge(addressList));
    }

    private static void matchStreet(String input, List<int[]> addressList) {
        Matcher matcher = matchers(input, "pRegST");
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            Matcher matcher2 = matchers(input.substring(end), "pReg_city");
            if (matcher2.lookingAt()) {
                end += matcher2.end();
            }
            addressList.add(new int[]{1, start, end});
        }
    }

    private static void matchNotSingleBuilding(String input, List<int[]> addressList) {
        boolean hasPrep = false;
        boolean hasOther = false;
        Matcher addressMatcher = matchers(input, "pRegED_Independ");
        while (addressMatcher.find()) {
            if (keyNotOnly(addressMatcher.group(1), addressMatcher.group(3), addressMatcher.group(4))) {
                int start = addressMatcher.start();
                int end = addressMatcher.end();
                String subInput = input.substring(end);
                Matcher pRegPrepMatcher = matchers(input.substring(0, start), "pRegPrep");
                Matcher matcher1 = matchers(subInput, "pReg_city");
                Matcher matcher2 = matchers(subInput, "pRegST");
                if (matcher2.lookingAt()) {
                    hasOther = Pattern.compile("(\\p{Blank}+|\\p{Blank}*(-)\\p{Blank}*|\\p{Blank}*,\\p{Blank}*)").matcher(input.substring(end, matcher2.start() + end)).matches();
                }
                if (matcher1.lookingAt()) {
                    end += matcher1.end();
                }
                if (pRegPrepMatcher.find()) {
                    hasPrep = Pattern.compile("\\p{Blank}{0,6}").matcher(input.substring(pRegPrepMatcher.end(), start)).matches();
                }
                if (hasPrep || hasOther) {
                    addressList.add(new int[]{2, start, end});
                }
            }
        }
    }

    private static void matchSingleBuilding(String input, List<int[]> addressList) {
        Matcher matcher = matchers(input, "pRegED");
        while (matcher.find()) {
            String str = matcher.group(1);
            if (str != null) {
                int start = matcher.start() + noBlack(str, matcher.group(2), matcher.group(3));
                int end = matcher.end();
                Matcher matcher2 = matchers(input.substring(end), "pReg_city");
                if (matcher2.lookingAt()) {
                    end += matcher2.end();
                }
                addressList.add(new int[]{2, start, end});
            }
        }
    }

    private static void matchWhite(String input, List<int[]> addressList) {
        Matcher matcher = matchers(input, "pRegWT");
        while (matcher.find()) {
            addressList.add(new int[]{7, matcher.start(), matcher.end()});
        }
    }

    private static int[] getAddressIndex(List<int[]> addressList, int addressNum) {
        int[] result = new int[((addressNum * 3) + 1)];
        result[0] = addressNum;
        for (int i = 0; i < addressNum; i++) {
            result[(i * 2) + 1] = addressList.get(i)[1];
            result[(i * 2) + 2] = addressList.get(i)[2] - 1;
        }
        return result;
    }

    private static int merge(List<int[]> addressList) {
        Collections.sort(addressList, new Comparator<int[]>() {
            /* class com.huawei.i18n.tmr.address.de.SearchEngine.AnonymousClass1 */

            public int compare(int[] pair1, int[] pair2) {
                return Integer.compare(pair1[1], pair2[1]);
            }
        });
        int len = addressList.size() - 1;
        int i = 0;
        while (i < len) {
            if (addressList.get(i)[2] >= addressList.get(i + 1)[1] && addressList.get(i)[2] < addressList.get(i + 1)[2]) {
                if (addressList.get(i)[2] > addressList.get(i + 1)[1]) {
                    addressList.get(i)[2] = addressList.get(i + 1)[1];
                }
                if ((addressList.get(i)[0] & addressList.get(i + 1)[0]) == 0 && !(addressList.get(i)[0] == 1 && addressList.get(i + 1)[0] == 2)) {
                    addressList.get(i)[2] = addressList.get(i + 1)[2];
                    addressList.get(i)[0] = addressList.get(i)[0] | addressList.get(i + 1)[0];
                    addressList.remove(i + 1);
                    len--;
                    i--;
                }
            }
            i++;
        }
        return len + 1;
    }

    private static int noBlack(String wordsFirst, String wordSecond, String wordBlank) {
        if (wordsFirst.length() > 0) {
            Matcher matcher = matchers(wordsFirst, "pRegBlackKeyIndi_withBlank");
            if (!matcher.lookingAt()) {
                return 0;
            }
            int offset = matcher.group().length();
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

    private static Matcher matchers(String str, String reg) {
        return PatternCache.getPattern(reg, "com.huawei.i18n.tmr.address.de.RegularExpression").matcher(str);
    }
}
