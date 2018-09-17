package com.huawei.g11n.tmr.address.es;

import com.huawei.g11n.tmr.util.PatternCache;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SerEngine {
    public static int[] search(String sentence) {
        int start;
        int end;
        Matcher cAll;
        boolean hasPrep = false;
        boolean hasOther = false;
        Object otherIndex = new int[2];
        List<int[]> listOther = new ArrayList();
        List<int[]> indAddr = new LinkedList();
        Matcher mAll = matchers(sentence, "p1");
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
        while (mAll.find() && !blackDictionary.contains(mAll.group())) {
            start = mAll.start();
            end = start + mAll.group().length();
            cAll = matchers(sentence.substring(end), "p2");
            if (cAll.lookingAt()) {
                end += cAll.end();
            }
            indAddr.add(new int[]{1, start, end});
            otherIndex[0] = start;
            otherIndex[1] = end;
            listOther.add(otherIndex);
        }
        mAll = matchers(sentence, "p3");
        while (mAll.find()) {
            end = mAll.start() + mAll.group().length();
            cAll = matchers(sentence.substring(end), "p2");
            if (cAll.lookingAt()) {
                end += cAll.end();
            }
            indAddr.add(new int[]{2, start, end});
        }
        mAll = matchers(sentence, "p5");
        while (mAll.find()) {
            start = mAll.start();
            end = start + mAll.group().length();
            Matcher mprep = matchers(sentence.substring(0, start), "pgrep");
            if (mprep.lookingAt()) {
                hasPrep = Pattern.compile("\\p{Blank}{1,6}").matcher(sentence.substring(mprep.end(), start)).matches();
            }
            for (int[] eachOther : listOther) {
                if (eachOther[0] <= end || eachOther[0] - end >= 10) {
                    if (eachOther[1] < start && start - eachOther[1] < 10 && matchers(sentence.substring(eachOther[1], start), "pPrepAndSc").matches()) {
                        hasOther = true;
                        break;
                    }
                } else if (matchers(sentence.substring(end, eachOther[0]), "pPrepAndSc").matches()) {
                    hasOther = true;
                    break;
                }
            }
            if (hasPrep || hasOther) {
                cAll = matchers(sentence.substring(end), "p2");
                if (cAll.lookingAt()) {
                    end += cAll.end();
                }
                indAddr.add(new int[]{2, start, end});
            }
        }
        int n = indAddr.size();
        if (n == 0) {
            return new int[1];
        }
        n = merge(indAddr, n, sentence);
        int[] re = new int[((n * 2) + 1)];
        re[0] = n;
        for (int i = 0; i < n; i++) {
            re[(i * 2) + 1] = ((int[]) indAddr.get(i))[1];
            re[(i * 2) + 2] = ((int[]) indAddr.get(i))[2] - 1;
        }
        return re;
    }

    private static int merge(List<int[]> indx, int len, String sentence) {
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

    private static Matcher matchers(String t, String reg) {
        return PatternCache.getPattern(reg, "com.huawei.g11n.tmr.address.es.ReguExp").matcher(t);
    }
}
