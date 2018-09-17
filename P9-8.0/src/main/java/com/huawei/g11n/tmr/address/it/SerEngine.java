package com.huawei.g11n.tmr.address.it;

import com.huawei.g11n.tmr.util.PatternCache;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

public class SerEngine {
    private static HashSet<String> blackDictionary = new HashSet<String>() {
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

    public static int[] search(String sentence) {
        String temp;
        int nED;
        Matcher matcherPT;
        int m1;
        int n1;
        String temp1;
        Matcher matcherregione;
        int m2;
        int n2;
        Matcher matchercountry;
        int m3;
        int n3;
        int n;
        List<int[]> addrIndxTemp = new LinkedList();
        int senlen = sentence.length();
        Matcher matcher = matchers(sentence, "patternEDnopre");
        String ss = "";
        while (matcher.find()) {
            ss = matcher.group(4);
            if (!(blackDictionary.contains(matcher.group()) || deleteBuiBlack(ss).booleanValue())) {
                addrIndxTemp.add(new int[]{matcher.start(), matcher.end()});
                temp = sentence.substring(matcher.end(), senlen);
                nED = matcher.end();
                matcherPT = matchers(temp, "patternPCcity");
                if (matcherPT.lookingAt()) {
                    m1 = matcherPT.start() + nED;
                    n1 = matcherPT.end() + nED;
                    addrIndxTemp.add(new int[]{m1, n1});
                    temp1 = sentence.substring(n1, senlen);
                    matcherregione = matchers(temp1, "patternregion");
                    if (matcherregione.lookingAt()) {
                        m2 = matcherregione.start() + n1;
                        n2 = matcherregione.end() + n1;
                        addrIndxTemp.add(new int[]{m2, n2});
                        matchercountry = matchers(sentence.substring(n2, senlen), "patterncountry");
                        if (matchercountry.lookingAt()) {
                            m3 = matchercountry.start() + n2;
                            n3 = matchercountry.end() + n2;
                            addrIndxTemp.add(new int[]{m3, n3});
                        }
                    } else {
                        matchercountry = matchers(temp1, "patterncountry");
                        if (matchercountry.lookingAt()) {
                            m3 = matchercountry.start() + n1;
                            n3 = matchercountry.end() + n1;
                            addrIndxTemp.add(new int[]{m3, n3});
                        }
                    }
                }
            }
        }
        Matcher matcherjie = matchers(sentence, "patternED");
        while (matcherjie.find()) {
            ss = matcherjie.group(4);
            if (!(blackDictionary.contains(matcherjie.group()) || deleteBuiBlack(ss).booleanValue())) {
                addrIndxTemp.add(new int[]{matcherjie.start(), matcherjie.end()});
                temp = sentence.substring(matcherjie.end(), senlen);
                nED = matcherjie.end();
                matcherPT = matchers(temp, "patternPCcity");
                if (matcherPT.lookingAt()) {
                    m1 = matcherPT.start() + nED;
                    n1 = matcherPT.end() + nED;
                    addrIndxTemp.add(new int[]{m1, n1});
                    temp1 = sentence.substring(n1, senlen);
                    matcherregione = matchers(temp1, "patternregion");
                    if (matcherregione.lookingAt()) {
                        m2 = matcherregione.start() + n1;
                        n2 = matcherregione.end() + n1;
                        addrIndxTemp.add(new int[]{m2, n2});
                        matchercountry = matchers(sentence.substring(n2, senlen), "patterncountry");
                        if (matchercountry.lookingAt()) {
                            m3 = matchercountry.start() + n2;
                            n3 = matchercountry.end() + n2;
                            addrIndxTemp.add(new int[]{m3, n3});
                        }
                    } else {
                        matchercountry = matchers(temp1, "patterncountry");
                        if (matchercountry.lookingAt()) {
                            m3 = matchercountry.start() + n1;
                            n3 = matchercountry.end() + n1;
                            addrIndxTemp.add(new int[]{m3, n3});
                        }
                    }
                }
            }
        }
        Matcher matcherST = matchers(sentence, "patternST");
        while (matcherST.find()) {
            String ssq = matcherST.group(3);
            Boolean valueOf = Boolean.valueOf(false);
            valueOf = deleteStrBlack(ssq);
            int m = matcherST.start();
            n = matcherST.end();
            if (!valueOf.booleanValue()) {
                addrIndxTemp.add(new int[]{m, n});
            }
            matcherPT = matchers(sentence.substring(n, senlen), "patternPCcity");
            if (matcherPT.lookingAt()) {
                m1 = matcherPT.start() + n;
                n1 = matcherPT.end() + n;
                addrIndxTemp.add(new int[]{m1, n1});
                String temp2 = sentence.substring(n1, senlen);
                matcherregione = matchers(temp2, "patternregion");
                if (matcherregione.lookingAt()) {
                    m2 = matcherregione.start() + n1;
                    n2 = matcherregione.end() + n1;
                    addrIndxTemp.add(new int[]{m2, n2});
                    matchercountry = matchers(sentence.substring(n2, senlen), "patterncountry");
                    if (matchercountry.lookingAt()) {
                        m3 = matchercountry.start() + n2;
                        n3 = matchercountry.end() + n2;
                        addrIndxTemp.add(new int[]{m3, n3});
                    }
                } else {
                    matchercountry = matchers(temp2, "patterncountry");
                    if (matchercountry.lookingAt()) {
                        int m0 = matchercountry.start() + n1;
                        int n0 = matchercountry.end() + n1;
                        addrIndxTemp.add(new int[]{m0, n0});
                    }
                }
            }
        }
        n = addrIndxTemp.size();
        if (n == 0) {
            return new int[1];
        }
        n = merge(addrIndxTemp, n);
        int[] addrIndx = new int[((n * 2) + 1)];
        addrIndx[0] = n;
        for (int i = 0; i < n; i++) {
            addrIndx[(i * 2) + 1] = ((int[]) addrIndxTemp.get(i))[0];
            addrIndx[(i * 2) + 2] = ((int[]) addrIndxTemp.get(i))[1] - 1;
        }
        return addrIndx;
    }

    private static int merge(List<int[]> indx, int len) {
        Collections.sort(indx, new Comparator<int[]>() {
            public int compare(int[] pair1, int[] pair2) {
                if (pair1[0] < pair2[0]) {
                    return -1;
                }
                if (pair1[0] != pair2[0]) {
                    return 1;
                }
                return 0;
            }
        });
        len--;
        int i = 0;
        while (i < len) {
            if (((int[]) indx.get(i))[1] >= ((int[]) indx.get(i + 1))[0]) {
                if (((int[]) indx.get(i))[1] < ((int[]) indx.get(i + 1))[1]) {
                    ((int[]) indx.get(i))[1] = ((int[]) indx.get(i + 1))[1];
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
            return Boolean.valueOf(false);
        }
        if (matchers(addressB, "patternbb").matches()) {
            return Boolean.valueOf(true);
        }
        return Boolean.valueOf(false);
    }

    private static Boolean deleteStrBlack(String addressS) {
        if (addressS == null) {
            return Boolean.valueOf(false);
        }
        if (matchers(addressS, "patternbs").matches()) {
            return Boolean.valueOf(true);
        }
        return Boolean.valueOf(false);
    }

    private static Matcher matchers(String t, String reg) {
        return PatternCache.getPattern(reg, "com.huawei.g11n.tmr.address.it.ReguExp").matcher(t);
    }
}
