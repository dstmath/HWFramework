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

    /* JADX WARNING: type inference failed for: r7v2, types: [boolean] */
    /* JADX WARNING: type inference failed for: r7v20 */
    /* JADX WARNING: type inference failed for: r7v22 */
    public static int[] search(String sentence) {
        int i;
        boolean z;
        int i2;
        int i3;
        boolean z2;
        Matcher matcher;
        String ss;
        Matcher matcher2;
        String str = sentence;
        List<int[]> addrIndxTemp = new LinkedList<>();
        int senlen = sentence.length();
        Matcher matcher3 = matchers(str, "patternEDnopre");
        String ss2 = "";
        while (true) {
            i = 4;
            z = false;
            i2 = 2;
            i3 = 1;
            if (!matcher3.find()) {
                break;
            }
            ss2 = matcher3.group(4);
            if (!blackDictionary.contains(matcher3.group()) && !deleteBuiBlack(ss2).booleanValue()) {
                addrIndxTemp.add(new int[]{matcher3.start(), matcher3.end()});
                String temp = str.substring(matcher3.end(), senlen);
                int nED = matcher3.end();
                Matcher matcherPT = matchers(temp, "patternPCcity");
                if (matcherPT.lookingAt()) {
                    int n1 = matcherPT.end() + nED;
                    addrIndxTemp.add(new int[]{matcherPT.start() + nED, n1});
                    String temp1 = str.substring(n1, senlen);
                    Matcher matcherregione = matchers(temp1, "patternregion");
                    if (matcherregione.lookingAt()) {
                        int n2 = matcherregione.end() + n1;
                        matcher2 = matcher3;
                        addrIndxTemp.add(new int[]{matcherregione.start() + n1, n2});
                        Matcher matchercountry = matchers(str.substring(n2, senlen), "patterncountry");
                        if (matchercountry.lookingAt()) {
                            addrIndxTemp.add(new int[]{matchercountry.start() + n2, matchercountry.end() + n2});
                        }
                    } else {
                        matcher2 = matcher3;
                        Matcher matchercountry2 = matchers(temp1, "patterncountry");
                        if (matchercountry2.lookingAt()) {
                            addrIndxTemp.add(new int[]{matchercountry2.start() + n1, matchercountry2.end() + n1});
                        }
                    }
                } else {
                    matcher2 = matcher3;
                }
            } else {
                matcher2 = matcher3;
            }
            matcher3 = matcher2;
            str = sentence;
        }
        Matcher matcherjie = matchers(str, "patternED");
        while (matcherjie.find()) {
            String ss3 = matcherjie.group(i);
            if (!blackDictionary.contains(matcherjie.group()) && !deleteBuiBlack(ss3).booleanValue()) {
                addrIndxTemp.add(new int[]{matcherjie.start(), matcherjie.end()});
                String temp2 = str.substring(matcherjie.end(), senlen);
                int nED2 = matcherjie.end();
                Matcher matcherPT2 = matchers(temp2, "patternPCcity");
                if (matcherPT2.lookingAt()) {
                    int n12 = matcherPT2.end() + nED2;
                    addrIndxTemp.add(new int[]{matcherPT2.start() + nED2, n12});
                    String temp12 = str.substring(n12, senlen);
                    Matcher matcherregione2 = matchers(temp12, "patternregion");
                    if (matcherregione2.lookingAt()) {
                        int n22 = matcherregione2.end() + n12;
                        ss = ss3;
                        matcher = matcherjie;
                        addrIndxTemp.add(new int[]{matcherregione2.start() + n12, n22});
                        String temp22 = str.substring(n22, senlen);
                        Matcher matchercountry3 = matchers(temp22, "patterncountry");
                        if (matchercountry3.lookingAt()) {
                            String str2 = temp22;
                            Matcher matcher4 = matchercountry3;
                            addrIndxTemp.add(new int[]{matchercountry3.start() + n22, matchercountry3.end() + n22});
                        }
                    } else {
                        ss = ss3;
                        matcher = matcherjie;
                        Matcher matchercountry4 = matchers(temp12, "patterncountry");
                        if (matchercountry4.lookingAt()) {
                            Matcher matcher5 = matchercountry4;
                            addrIndxTemp.add(new int[]{matchercountry4.start() + n12, matchercountry4.end() + n12});
                        }
                    }
                } else {
                    ss = ss3;
                    matcher = matcherjie;
                }
            } else {
                ss = ss3;
                matcher = matcherjie;
            }
            ss2 = ss;
            matcherjie = matcher;
            i = 4;
            z = false;
            i2 = 2;
            i3 = 1;
        }
        Matcher matcherST = matchers(str, "patternST");
        ? r7 = z;
        while (matcherST.find()) {
            String ssq = matcherST.group(3);
            Boolean valueOf = Boolean.valueOf(r7);
            Boolean flag1 = deleteStrBlack(ssq);
            int m = matcherST.start();
            int n = matcherST.end();
            if (!flag1.booleanValue()) {
                int[] iArr = new int[i2];
                iArr[r7] = m;
                iArr[i3] = n;
                addrIndxTemp.add(iArr);
            }
            Matcher matcherPT3 = matchers(str.substring(n, senlen), "patternPCcity");
            if (matcherPT3.lookingAt()) {
                int n13 = matcherPT3.end() + n;
                String ss4 = ss2;
                int[] iArr2 = new int[i2];
                iArr2[r7] = matcherPT3.start() + n;
                iArr2[1] = n13;
                addrIndxTemp.add(iArr2);
                String temp23 = str.substring(n13, senlen);
                Matcher matcherregione3 = matchers(temp23, "patternregion");
                if (matcherregione3.lookingAt()) {
                    String str3 = ssq;
                    int n23 = matcherregione3.end() + n13;
                    Matcher matcher6 = matcherregione3;
                    int[] iArr3 = new int[i2];
                    iArr3[0] = matcherregione3.start() + n13;
                    iArr3[1] = n23;
                    addrIndxTemp.add(iArr3);
                    String temp3 = str.substring(n23, senlen);
                    Matcher matchercountry5 = matchers(temp3, "patterncountry");
                    if (matchercountry5.lookingAt()) {
                        int i4 = n23;
                        String str4 = temp3;
                        addrIndxTemp.add(new int[]{matchercountry5.start() + n23, matchercountry5.end() + n23});
                    }
                } else {
                    String str5 = ssq;
                    Matcher matcher7 = matcherregione3;
                    Matcher matchercountry6 = matchers(temp23, "patterncountry");
                    if (matchercountry6.lookingAt()) {
                        String str6 = temp23;
                        Matcher matcher8 = matchercountry6;
                        addrIndxTemp.add(new int[]{matchercountry6.start() + n13, matchercountry6.end() + n13});
                    }
                }
                ss2 = ss4;
                z2 = false;
                i2 = 2;
                i3 = 1;
            } else {
                z2 = r7;
            }
            r7 = z2;
        }
        int n3 = addrIndxTemp.size();
        if (n3 == 0) {
            return new int[i3];
        }
        int n4 = merge(addrIndxTemp, n3);
        int[] addrIndx = new int[((i2 * n4) + i3)];
        addrIndx[r7] = n4;
        for (int i5 = 0; i5 < n4; i5++) {
            addrIndx[(i2 * i5) + i3] = addrIndxTemp.get(i5)[r7];
            addrIndx[(i2 * i5) + i2] = addrIndxTemp.get(i5)[i3] - i3;
        }
        return addrIndx;
    }

    private static int merge(List<int[]> indx, int len) {
        Collections.sort(indx, new Comparator<int[]>() {
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
        if (addressB == null) {
            return false;
        }
        if (matchers(addressB, "patternbb").matches()) {
            return true;
        }
        return false;
    }

    private static Boolean deleteStrBlack(String addressS) {
        if (addressS == null) {
            return false;
        }
        if (matchers(addressS, "patternbs").matches()) {
            return true;
        }
        return false;
    }

    private static Matcher matchers(String t, String reg) {
        return PatternCache.getPattern(reg, "com.huawei.g11n.tmr.address.it.ReguExp").matcher(t);
    }
}
