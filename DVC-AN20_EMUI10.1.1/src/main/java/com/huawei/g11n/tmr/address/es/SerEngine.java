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
        int end;
        int end2;
        boolean hasPrep;
        Matcher mAll;
        boolean hasPrep2;
        Matcher mAll2;
        boolean hasPrep3 = false;
        int[] otherIndex = new int[2];
        List<int[]> listOther = new ArrayList<>();
        String temp = null;
        List<int[]> indAddr = new LinkedList<>();
        Matcher mAll3 = matchers(sentence, "p1");
        HashSet<String> blackDictionary = new HashSet<String>() {
            /* class com.huawei.g11n.tmr.address.es.SerEngine.AnonymousClass1 */
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
            end = 1;
            if (!mAll3.find()) {
                break;
            }
            temp = mAll3.group();
            if (blackDictionary.contains(temp)) {
                break;
            }
            int start = mAll3.start();
            int end3 = mAll3.group().length() + start;
            Matcher cAll = matchers(sentence.substring(end3), "p2");
            if (cAll.lookingAt()) {
                end3 += cAll.end();
            }
            indAddr.add(new int[]{1, start, end3});
            otherIndex[0] = start;
            otherIndex[1] = end3;
            listOther.add(otherIndex);
            hasPrep3 = hasPrep3;
        }
        Matcher mAll4 = matchers(sentence, "p3");
        while (mAll4.find()) {
            int start2 = mAll4.start();
            int end4 = mAll4.group().length() + start2;
            Matcher cAll2 = matchers(sentence.substring(end4), "p2");
            if (cAll2.lookingAt()) {
                end4 += cAll2.end();
            }
            indAddr.add(new int[]{2, start2, end4});
            hasPrep3 = hasPrep;
            end = 1;
        }
        Matcher mAll5 = matchers(sentence, "p5");
        boolean hasOther = false;
        while (mAll.find()) {
            int start3 = mAll.start();
            int end5 = mAll.group().length() + start3;
            Matcher mprep = matchers(sentence.substring(0, start3), "pgrep");
            if (mprep.lookingAt()) {
                hasPrep2 = Pattern.compile("\\p{Blank}{1,6}").matcher(sentence.substring(mprep.end(), start3)).matches();
            } else {
                hasPrep2 = hasPrep;
            }
            Iterator<int[]> it = listOther.iterator();
            while (true) {
                if (!it.hasNext()) {
                    mAll2 = mAll;
                    break;
                }
                int[] eachOther = it.next();
                mAll2 = mAll;
                if (eachOther[0] <= end5 || eachOther[0] - end5 >= 10) {
                    if (eachOther[1] >= start3 || start3 - eachOther[1] >= 10) {
                        mAll = mAll2;
                    } else if (matchers(sentence.substring(eachOther[1], start3), "pPrepAndSc").matches()) {
                        hasOther = true;
                        break;
                    } else {
                        mAll = mAll2;
                    }
                } else if (matchers(sentence.substring(end5, eachOther[0]), "pPrepAndSc").matches()) {
                    hasOther = true;
                    break;
                } else {
                    mAll = mAll2;
                }
            }
            if (hasPrep2 || hasOther) {
                Matcher cAll3 = matchers(sentence.substring(end5), "p2");
                if (cAll3.lookingAt()) {
                    end5 += cAll3.end();
                }
                indAddr.add(new int[]{2, start3, end5});
                hasPrep = hasPrep2;
                mAll5 = mAll2;
                end2 = 1;
            } else {
                hasPrep = hasPrep2;
                mAll5 = mAll2;
                end2 = 1;
            }
        }
        int n = indAddr.size();
        if (n == 0) {
            return new int[end2];
        }
        int n2 = merge(indAddr, n, sentence);
        int[] re = new int[((n2 * 2) + end2)];
        re[0] = n2;
        for (int i = 0; i < n2; i++) {
            re[(i * 2) + 1] = indAddr.get(i)[end2];
            re[(i * 2) + 2] = indAddr.get(i)[2] - 1;
        }
        return re;
    }

    private static int merge(List<int[]> indx, int len, String sentence) {
        Collections.sort(indx, new Comparator<int[]>() {
            /* class com.huawei.g11n.tmr.address.es.SerEngine.AnonymousClass2 */

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
