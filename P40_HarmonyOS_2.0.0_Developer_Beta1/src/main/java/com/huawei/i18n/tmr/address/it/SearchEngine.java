package com.huawei.i18n.tmr.address.it;

import com.huawei.i18n.tmr.address.util.PatternCache;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

public class SearchEngine {
    private static HashSet<String> blackDictionary = new HashSet<String>() {
        /* class com.huawei.i18n.tmr.address.it.SearchEngine.AnonymousClass1 */
        private static final long serialVersionUID = 2852005425220592267L;

        {
            add("Piazzapulita ");
            add("Tokio Hotel ");
            add("Piazza Università ");
            add("Ristorante Cinese ");
            add("Giardino Inglese ");
            add("Ristorante Italiano ");
            add("Facoltà di Lettere ");
            add("Strada Grande Comunicazione ");
        }
    };

    public static int[] search(String input) {
        List<int[]> addressList = new LinkedList<>();
        matchBuildCity(input, addressList);
        matchBuildPreposition(input, addressList);
        matchStreet(input, addressList);
        if (addressList.size() == 0) {
            return new int[]{0};
        }
        int addressNum = merge(addressList);
        int[] searchResult = new int[((addressNum * 2) + 1)];
        searchResult[0] = addressNum;
        for (int i = 0; i < addressNum; i++) {
            searchResult[(i * 2) + 1] = addressList.get(i)[0];
            searchResult[(i * 2) + 2] = addressList.get(i)[1] - 1;
        }
        return searchResult;
    }

    private static void matchStreet(String input, List<int[]> addressList) {
        Matcher matcherST = matchers(input, "patternST");
        while (matcherST.find()) {
            boolean flag = deleteStrBlack(matcherST.group(3)).booleanValue();
            int start = matcherST.start();
            int end = matcherST.end();
            if (!flag) {
                addressList.add(new int[]{start, end});
            }
            Matcher matcherPT = matchers(input.substring(end), "patternPCcity");
            if (matcherPT.lookingAt()) {
                int end2 = matcherPT.end() + end;
                addressList.add(new int[]{matcherPT.start() + end, end2});
                String subString2 = input.substring(end2);
                if (!matchRegion(input, addressList, end2, subString2)) {
                    Matcher matcherCountry = matchers(subString2, "patterncountry");
                    if (matcherCountry.lookingAt()) {
                        addressList.add(new int[]{matcherCountry.start() + end2, matcherCountry.end() + end2});
                    }
                }
            }
        }
    }

    private static void matchBuildPreposition(String input, List<int[]> addressList) {
        Matcher matcher = matchers(input, "patternED");
        while (matcher.find()) {
            String ss = matcher.group(4);
            if (!blackDictionary.contains(matcher.group()) && !deleteBuiBlack(ss).booleanValue()) {
                addressList.add(new int[]{matcher.start(), matcher.end()});
                String temp = input.substring(matcher.end());
                int nED = matcher.end();
                Matcher matcherPT = matchers(temp, "patternPCcity");
                if (matcherPT.lookingAt()) {
                    int n1 = matcherPT.end() + nED;
                    addressList.add(new int[]{matcherPT.start() + nED, n1});
                    String temp1 = input.substring(n1);
                    if (!matchRegion(input, addressList, n1, temp1)) {
                        Matcher matcherCountry = matchers(temp1, "patterncountry");
                        if (matcherCountry.lookingAt()) {
                            addressList.add(new int[]{matcherCountry.start() + n1, matcherCountry.end() + n1});
                        }
                    }
                }
            }
        }
    }

    private static boolean matchRegion(String input, List<int[]> addressList, int n1, String temp1) {
        Matcher matcherRegion = matchers(temp1, "patternregion");
        if (!matcherRegion.lookingAt()) {
            return false;
        }
        int n2 = matcherRegion.end() + n1;
        addressList.add(new int[]{matcherRegion.start() + n1, n2});
        Matcher matcherCountry = matchers(input.substring(n2), "patterncountry");
        if (matcherCountry.lookingAt()) {
            addressList.add(new int[]{matcherCountry.start() + n2, matcherCountry.end() + n2});
        }
        return true;
    }

    private static void matchBuildCity(String input, List<int[]> addressList) {
        Matcher matcher = matchers(input, "patternEDnopre");
        while (matcher.find()) {
            String group4 = matcher.group(4);
            if (!blackDictionary.contains(matcher.group()) && !deleteBuiBlack(group4).booleanValue()) {
                addressList.add(new int[]{matcher.start(), matcher.end()});
                String temp = input.substring(matcher.end());
                int nED = matcher.end();
                Matcher matcherPT = matchers(temp, "patternPCcity");
                if (matcherPT.lookingAt()) {
                    int end = matcherPT.end() + nED;
                    addressList.add(new int[]{matcherPT.start() + nED, end});
                    String subString = input.substring(end);
                    if (!matchRegion(input, addressList, end, subString)) {
                        Matcher matcherCountry = matchers(subString, "patterncountry");
                        if (matcherCountry.lookingAt()) {
                            addressList.add(new int[]{matcherCountry.start() + end, matcherCountry.end() + end});
                        }
                    }
                }
            }
        }
    }

    private static int merge(List<int[]> indx) {
        Collections.sort(indx, new Comparator<int[]>() {
            /* class com.huawei.i18n.tmr.address.it.SearchEngine.AnonymousClass2 */

            public int compare(int[] pair1, int[] pair2) {
                return Integer.compare(pair1[0], pair2[0]);
            }
        });
        int len = indx.size() - 1;
        int i = 0;
        while (i < len) {
            if (indx.get(i)[1] >= indx.get(i + 1)[0]) {
                if (indx.get(i)[1] < indx.get(i + 1)[1]) {
                    indx.get(i)[1] = indx.get(i + 1)[1];
                }
                indx.remove(i + 1);
                len--;
                i--;
            }
            i++;
        }
        return len + 1;
    }

    private static Boolean deleteBuiBlack(String addressB) {
        if (addressB == null) {
            return false;
        }
        return Boolean.valueOf(matchers(addressB, "patternbb").matches());
    }

    private static Boolean deleteStrBlack(String addressS) {
        if (addressS == null) {
            return false;
        }
        return Boolean.valueOf(matchers(addressS, "patternbs").matches());
    }

    private static Matcher matchers(String input, String regex) {
        return PatternCache.getPattern(regex, "com.huawei.i18n.tmr.address.it.RegularExpression").matcher(input);
    }
}
