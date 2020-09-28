package com.huawei.g11n.tmr.address.it;

import com.huawei.g11n.tmr.util.PatternCache;
import com.huawei.uikit.effect.BuildConfig;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

public class SerEngine {
    private static HashSet<String> blackDictionary = new HashSet<String>() {
        /* class com.huawei.g11n.tmr.address.it.SerEngine.AnonymousClass1 */
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

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:88:0x0038 */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:94:0x0038 */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: Multiple debug info for r6v9 int: [D('ssq' java.lang.String), D('n2' int)] */
    /* JADX INFO: Multiple debug info for r5v6 int: [D('matcherjie' java.util.regex.Matcher), D('n2' int)] */
    /* JADX INFO: Multiple debug info for r3v5 int: [D('matcher' java.util.regex.Matcher), D('n2' int)] */
    /* JADX WARN: Type inference failed for: r10v0 */
    /* JADX WARN: Type inference failed for: r10v1 */
    /* JADX WARN: Type inference failed for: r10v2, types: [boolean] */
    /* JADX WARN: Type inference failed for: r10v3 */
    /* JADX WARN: Type inference failed for: r10v4 */
    /* JADX WARN: Type inference failed for: r10v6 */
    /* JADX WARN: Type inference failed for: r10v7 */
    /* JADX WARN: Type inference failed for: r10v8 */
    /* JADX WARN: Type inference failed for: r10v9 */
    /* JADX WARN: Type inference failed for: r10v12 */
    /* JADX WARNING: Unknown variable types count: 2 */
    public static int[] search(String sentence) {
        int i;
        ?? r10;
        int m0;
        int m02;
        ?? r102;
        String temp2;
        String ss;
        Matcher matcherjie;
        String ss2;
        Matcher matcher;
        String str;
        List<int[]> addrIndxTemp = new LinkedList<>();
        int senlen = sentence.length();
        Matcher matcher2 = matchers(sentence, "patternEDnopre");
        String temp22 = BuildConfig.FLAVOR;
        while (true) {
            i = 4;
            r10 = 0;
            m0 = 1;
            if (!matcher2.find()) {
                break;
            }
            String ss3 = matcher2.group(4);
            if (blackDictionary.contains(matcher2.group())) {
                matcher = matcher2;
                str = ss3;
            } else if (deleteBuiBlack(ss3).booleanValue()) {
                matcher = matcher2;
                str = ss3;
            } else {
                addrIndxTemp.add(new int[]{matcher2.start(), matcher2.end()});
                String temp = sentence.substring(matcher2.end(), senlen);
                int nED = matcher2.end();
                Matcher matcherPT = matchers(temp, "patternPCcity");
                if (matcherPT.lookingAt()) {
                    int n1 = matcherPT.end() + nED;
                    addrIndxTemp.add(new int[]{matcherPT.start() + nED, n1});
                    String temp1 = sentence.substring(n1, senlen);
                    Matcher matcherregione = matchers(temp1, "patternregion");
                    if (matcherregione.lookingAt()) {
                        matcher = matcher2;
                        int n2 = matcherregione.end() + n1;
                        str = ss3;
                        addrIndxTemp.add(new int[]{matcherregione.start() + n1, n2});
                        Matcher matchercountry = matchers(sentence.substring(n2, senlen), "patterncountry");
                        if (matchercountry.lookingAt()) {
                            addrIndxTemp.add(new int[]{matchercountry.start() + n2, matchercountry.end() + n2});
                        }
                    } else {
                        matcher = matcher2;
                        str = ss3;
                        Matcher matchercountry2 = matchers(temp1, "patterncountry");
                        if (matchercountry2.lookingAt()) {
                            addrIndxTemp.add(new int[]{matchercountry2.start() + n1, matchercountry2.end() + n1});
                        }
                    }
                } else {
                    matcher = matcher2;
                    str = ss3;
                }
            }
            temp22 = str;
            matcher2 = matcher;
        }
        Matcher matcherjie2 = matchers(sentence, "patternED");
        while (matcherjie2.find()) {
            String ss4 = matcherjie2.group(i);
            if (blackDictionary.contains(matcherjie2.group())) {
                ss2 = ss4;
                matcherjie = matcherjie2;
            } else if (deleteBuiBlack(ss4).booleanValue()) {
                ss2 = ss4;
                matcherjie = matcherjie2;
            } else {
                addrIndxTemp.add(new int[]{matcherjie2.start(), matcherjie2.end()});
                String temp3 = sentence.substring(matcherjie2.end(), senlen);
                int nED2 = matcherjie2.end();
                Matcher matcherPT2 = matchers(temp3, "patternPCcity");
                if (matcherPT2.lookingAt()) {
                    int n12 = matcherPT2.end() + nED2;
                    ss2 = ss4;
                    addrIndxTemp.add(new int[]{matcherPT2.start() + nED2, n12});
                    String temp12 = sentence.substring(n12, senlen);
                    Matcher matcherregione2 = matchers(temp12, "patternregion");
                    if (matcherregione2.lookingAt()) {
                        matcherjie = matcherjie2;
                        int n22 = matcherregione2.end() + n12;
                        addrIndxTemp.add(new int[]{matcherregione2.start() + n12, n22});
                        Matcher matchercountry3 = matchers(sentence.substring(n22, senlen), "patterncountry");
                        if (matchercountry3.lookingAt()) {
                            addrIndxTemp.add(new int[]{matchercountry3.start() + n22, matchercountry3.end() + n22});
                        }
                    } else {
                        matcherjie = matcherjie2;
                        Matcher matchercountry4 = matchers(temp12, "patterncountry");
                        if (matchercountry4.lookingAt()) {
                            addrIndxTemp.add(new int[]{matchercountry4.start() + n12, matchercountry4.end() + n12});
                        }
                    }
                } else {
                    ss2 = ss4;
                    matcherjie = matcherjie2;
                }
            }
            temp22 = ss2;
            matcherjie2 = matcherjie;
            i = 4;
            r10 = 0;
            m0 = 1;
        }
        Matcher matcherST = matchers(sentence, "patternST");
        while (matcherST.find()) {
            String ssq = matcherST.group(3);
            Boolean.valueOf((boolean) r102);
            Boolean flag1 = deleteStrBlack(ssq);
            int m = matcherST.start();
            int n = matcherST.end();
            if (!flag1.booleanValue()) {
                ss = temp2;
                int[] iArr = new int[2];
                iArr[r102] = m;
                iArr[1] = n;
                addrIndxTemp.add(iArr);
            } else {
                ss = temp2;
            }
            Matcher matcherPT3 = matchers(sentence.substring(n, senlen), "patternPCcity");
            if (matcherPT3.lookingAt()) {
                int n13 = matcherPT3.end() + n;
                addrIndxTemp.add(new int[]{matcherPT3.start() + n, n13});
                String temp23 = sentence.substring(n13, senlen);
                Matcher matcherregione3 = matchers(temp23, "patternregion");
                if (matcherregione3.lookingAt()) {
                    int n23 = matcherregione3.end() + n13;
                    addrIndxTemp.add(new int[]{matcherregione3.start() + n13, n23});
                    Matcher matchercountry5 = matchers(sentence.substring(n23, senlen), "patterncountry");
                    if (matchercountry5.lookingAt()) {
                        addrIndxTemp.add(new int[]{matchercountry5.start() + n23, matchercountry5.end() + n23});
                        temp2 = ss;
                        r102 = 0;
                        m02 = 1;
                    } else {
                        temp2 = ss;
                        r102 = 0;
                        m02 = 1;
                    }
                } else {
                    Matcher matchercountry6 = matchers(temp23, "patterncountry");
                    if (matchercountry6.lookingAt()) {
                        addrIndxTemp.add(new int[]{matchercountry6.start() + n13, matchercountry6.end() + n13});
                        temp2 = ss;
                        r102 = 0;
                        m02 = 1;
                    } else {
                        temp2 = ss;
                        r102 = 0;
                        m02 = 1;
                    }
                }
            } else {
                temp2 = ss;
                r102 = 0;
                m02 = 1;
            }
        }
        int n3 = addrIndxTemp.size();
        if (n3 == 0) {
            return new int[m02];
        }
        int n4 = merge(addrIndxTemp, n3);
        int[] addrIndx = new int[((n4 * 2) + m02)];
        addrIndx[r102] = n4;
        for (int i2 = 0; i2 < n4; i2++) {
            addrIndx[(i2 * 2) + m02] = addrIndxTemp.get(i2)[r102];
            addrIndx[(i2 * 2) + 2] = addrIndxTemp.get(i2)[m02] - m02;
        }
        return addrIndx;
    }

    private static int merge(List<int[]> indx, int len) {
        Collections.sort(indx, new Comparator<int[]>() {
            /* class com.huawei.g11n.tmr.address.it.SerEngine.AnonymousClass2 */

            public int compare(int[] pair1, int[] pair2) {
                if (pair1[0] < pair2[0]) {
                    return -1;
                }
                if (pair1[0] == pair2[0]) {
                    return 0;
                }
                return 1;
            }
        });
        int len2 = len - 1;
        int i = 0;
        while (i < len2) {
            if (indx.get(i)[1] >= indx.get(i + 1)[0]) {
                if (indx.get(i)[1] < indx.get(i + 1)[1]) {
                    indx.get(i)[1] = indx.get(i + 1)[1];
                }
                indx.remove(i + 1);
                len2--;
                i--;
            }
            i++;
        }
        return len2 + 1;
    }

    private static Boolean deleteBuiBlack(String addressB) {
        if (addressB != null && matchers(addressB, "patternbb").matches()) {
            return true;
        }
        return false;
    }

    private static Boolean deleteStrBlack(String addressS) {
        if (addressS != null && matchers(addressS, "patternbs").matches()) {
            return true;
        }
        return false;
    }

    private static Matcher matchers(String t, String reg) {
        return PatternCache.getPattern(reg, "com.huawei.g11n.tmr.address.it.ReguExp").matcher(t);
    }
}
