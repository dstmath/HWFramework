package com.huawei.i18n.tmr.address.pt;

import com.huawei.i18n.tmr.address.util.PatternCache;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

public class SearchEngine {
    private static final int BUI_MARK = 2;
    private static final int POS_MARK = 4;
    private static final int STR_MARK = 1;

    public static int[] search(String input) {
        List<int[]> addressList = new LinkedList<>();
        matchPbs(input, addressList);
        matchPat(input, addressList);
        matchPPos(input, addressList);
        return addressList.size() == 0 ? new int[]{0} : getAddressIntArr(merge(addressList, input), addressList);
    }

    private static int[] getAddressIntArr(int addressNum, List<int[]> addressList) {
        int[] searchResult = new int[((addressNum * 2) + 1)];
        searchResult[0] = addressNum;
        for (int i = 0; i < addressNum; i++) {
            searchResult[(i * 2) + 1] = addressList.get(i)[0];
            searchResult[(i * 2) + 2] = addressList.get(i)[1] - 1;
        }
        return searchResult;
    }

    private static void matchPPos(String input, List<int[]> addressList) {
        Matcher pPosMatcher = matchers(input, "pPos");
        while (pPosMatcher.find()) {
            addressList.add(new int[]{pPosMatcher.start(), pPosMatcher.end(), 4});
        }
    }

    private static void matchPat(String input, List<int[]> addressList) {
        Matcher patMatcher = matchers(input, "pat");
        while (patMatcher.find()) {
            int type = 2;
            int start = patMatcher.start(1);
            int end = patMatcher.end(1);
            String restSen = input.substring(end);
            if (matchers(restSen, "pnum").find()) {
                Matcher matcher2 = matchers(restSen, "pCityWithCode");
                if (matcher2.lookingAt()) {
                    type = 2 | 4;
                    end += matcher2.end();
                }
            } else {
                Matcher matcher22 = matchers(restSen, "pCityNoCode");
                if (matcher22.lookingAt()) {
                    end += matcher22.end();
                }
            }
            addressList.add(new int[]{start, end, type});
        }
    }

    private static void matchPbs(String input, List<int[]> addressList) {
        Matcher pbsMatcher = matchers(input, "pbs");
        while (pbsMatcher.find()) {
            int type = 0;
            if (pbsMatcher.group("street1") != null) {
                type = 0 | 1;
            }
            if (pbsMatcher.group("street2") != null) {
                type |= 1;
            }
            if (pbsMatcher.group("build") != null) {
                type |= 2;
            }
            int start = pbsMatcher.start();
            int end = pbsMatcher.end();
            String restStr = input.substring(end);
            if (matchers(restStr, "pnum").find()) {
                Matcher matcher2 = matchers(restStr, "pCityWithCode");
                if (matcher2.lookingAt()) {
                    type |= 4;
                    end += matcher2.end();
                }
            } else {
                Matcher matcher22 = matchers(restStr, "pCityNoCode");
                if (matcher22.lookingAt()) {
                    end += matcher22.end();
                }
            }
            addressList.add(new int[]{start, end, type});
        }
    }

    private static int merge(List<int[]> addressList, String input) {
        Collections.sort(addressList, new Comparator<int[]>() {
            /* class com.huawei.i18n.tmr.address.pt.SearchEngine.AnonymousClass1 */

            public int compare(int[] pair1, int[] pair2) {
                return Integer.compare(pair1[0], pair2[0]);
            }
        });
        int len = addressList.size() - 1;
        int i = 0;
        while (i < len) {
            if (addressList.get(i)[1] >= addressList.get(i + 1)[0]) {
                if (addressList.get(i)[1] < addressList.get(i + 1)[1]) {
                    addressList.get(i)[1] = addressList.get(i + 1)[1];
                }
                addressList.remove(i + 1);
                len--;
                i--;
            } else {
                Matcher matcher = matchers(input.substring(addressList.get(i)[1], addressList.get(i + 1)[0]), "pCat");
                int type1 = addressList.get(i)[2];
                int type2 = addressList.get(i + 1)[2];
                if (matcher.matches() && (type1 & type2) == 0) {
                    addressList.get(i)[1] = addressList.get(i + 1)[1];
                    addressList.remove(i + 1);
                    len--;
                    i--;
                }
            }
            i++;
        }
        return len + 1;
    }

    private static Matcher matchers(String input, String regex) {
        return PatternCache.getPattern(regex, "com.huawei.i18n.tmr.address.pt.RegularExpression").matcher(input);
    }
}
