package com.huawei.g11n.tmr.address.es;

import com.huawei.g11n.tmr.util.PatternCache;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SerEngine {
    public static int[] search(String sentence) {
        int i;
        boolean hasOther;
        String str = sentence;
        boolean hasPrep = false;
        int end = 2;
        int[] otherIndex = new int[2];
        List<int[]> listOther = new ArrayList<>();
        String temp = null;
        List<int[]> indAddr = new LinkedList<>();
        Matcher mAll = matchers(str, "p1");
        HashSet<String> blackDictionary = new HashSet<String>() {
            private static final long serialVersionUID = -2265126958429208163L;

            {
                add("cine Mexicano");
                add("Parque Nacional");
                add("esto");
                add("aquello");
                add("mío");
                add("tuyo");
                add("estos");
                add("bonito");
                add("grande");
                add("pequeo");
                add("bueno");
                add("puente de suspensión");
                add("Puente Colgante");
                add("puente");
                add("puente de acero");
                add("puente de arco");
                add("calle de dirección única");
                add("calle de dos carriles");
                add("restaurante antigua");
                add("taberna");
                add("institución");
                add("banco");
                add("club");
                add("tribunal");
                add("mercado de Navidad");
                add("mercado de pulgas");
                add("Mercado Libre");
                add("TEATRO MIS AMORES");
                add("gimnasio EN VERANO");
                add("Puente Genil");
                add("CALLE SEGUIREMOS");
                add("HOTEL OXIDAO");
                add("colegio JAJAJAAAJAJAJAJJAAJAJJAJAJA");
            }
        };
        while (true) {
            i = 1;
            if (!mAll.find()) {
                break;
            }
            temp = mAll.group();
            if (blackDictionary.contains(temp)) {
                break;
            }
            boolean hasPrep2 = hasPrep;
            int start = mAll.start();
            int end2 = mAll.group().length() + start;
            Matcher cAll = matchers(str.substring(end2), "p2");
            if (cAll.lookingAt()) {
                end2 += cAll.end();
            }
            indAddr.add(new int[]{1, start, end2});
            otherIndex[0] = start;
            otherIndex[1] = end2;
            listOther.add(otherIndex);
            int i2 = end2;
            end = 2;
            hasPrep = hasPrep2;
        }
        Matcher mAll2 = matchers(str, "p3");
        while (mAll2.find()) {
            int start2 = mAll2.start();
            int end3 = mAll2.group().length() + start2;
            Matcher cAll2 = matchers(str.substring(end3), "p2");
            if (cAll2.lookingAt()) {
                end3 += cAll2.end();
            }
            indAddr.add(new int[]{2, start2, end3});
            int i3 = end3;
            hasPrep = hasPrep;
            end = 2;
            i = 1;
        }
        Matcher mAll3 = matchers(str, "p5");
        boolean hasOther2 = false;
        while (mAll3.find()) {
            int start3 = mAll3.start();
            int end4 = mAll3.group().length() + start3;
            Matcher mprep = matchers(str.substring(0, start3), "pgrep");
            if (mprep.lookingAt()) {
                hasPrep = Pattern.compile("\\p{Blank}{1,6}").matcher(str.substring(mprep.end(), start3)).matches();
            }
            boolean hasPrep3 = hasPrep;
            Iterator<int[]> it = listOther.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                int[] eachOther = it.next();
                if (eachOther[0] <= end4 || eachOther[0] - end4 >= 10) {
                    if (eachOther[1] < start3 && start3 - eachOther[1] < 10 && matchers(str.substring(eachOther[1], start3), "pPrepAndSc").matches()) {
                        hasOther = true;
                        break;
                    }
                } else if (matchers(str.substring(end4, eachOther[0]), "pPrepAndSc").matches()) {
                    hasOther = true;
                    break;
                }
            }
            hasOther2 = hasOther;
            if (hasPrep3 || hasOther2) {
                Matcher cAll3 = matchers(str.substring(end4), "p2");
                if (cAll3.lookingAt()) {
                    end4 += cAll3.end();
                }
                indAddr.add(new int[]{2, start3, end4});
                int i4 = start3;
            } else {
                int i5 = start3;
                int i6 = end4;
            }
            hasPrep = hasPrep3;
            end = 2;
            i = 1;
        }
        int n = indAddr.size();
        if (n == 0) {
            return new int[i];
        }
        int n2 = merge(indAddr, n, str);
        int[] re = new int[((end * n2) + i)];
        re[0] = n2;
        for (int i7 = 0; i7 < n2; i7++) {
            re[(end * i7) + 1] = indAddr.get(i7)[i];
            re[(end * i7) + 2] = indAddr.get(i7)[end] - 1;
        }
        return re;
    }

    private static int merge(List<int[]> indx, int len, String sentence) {
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

    private static Matcher matchers(String t, String reg) {
        return PatternCache.getPattern(reg, "com.huawei.g11n.tmr.address.es.ReguExp").matcher(t);
    }
}
