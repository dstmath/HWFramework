package com.huawei.g11n.tmr.address.pt;

import com.huawei.g11n.tmr.util.PatternCache;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

public class SerEngine {
    private static final int BUI_MARK = 2;
    private static final int POS_MARK = 4;
    private static final int STR_MARK = 1;

    public static void main(String[] arg) {
        int[] r = search("Rua da Torre 274 RC ESQ  Edifício EPAL, Rua da Torre 234  Edifício EPAL, RUA FLORES 14 RC ESQ,BAIRRO DA PEDRA,2785-174 SÃO DOMINGOS DE RANA,PORTUGAL");
        for (int i = 0; i < r[0]; i++) {
            PrintStream printStream = System.out;
            printStream.println("识别总个数：" + r[0]);
            PrintStream printStream2 = System.out;
            printStream2.println(String.valueOf(r[(i * 2) + 1]) + "," + r[(i * 2) + 2]);
            System.out.println("Rua da Torre 274 RC ESQ  Edifício EPAL, Rua da Torre 234  Edifício EPAL, RUA FLORES 14 RC ESQ,BAIRRO DA PEDRA,2785-174 SÃO DOMINGOS DE RANA,PORTUGAL".substring(r[(i * 2) + 1], r[(i * 2) + 2] + 1));
        }
    }

    public static int[] search(String sentence) {
        int i;
        List<int[]> indAddr = new LinkedList<>();
        Matcher mAll = matchers(sentence, "pbs");
        while (true) {
            i = 2;
            if (!mAll.find()) {
                break;
            }
            int type = 0;
            if (mAll.group("street1") != null) {
                type = 0 | 1;
            }
            if (mAll.group("street2") != null) {
                type |= 1;
            }
            if (mAll.group("build") != null) {
                type |= 2;
            }
            int start = mAll.start();
            int end = mAll.end();
            String restSen = sentence.substring(end);
            if (matchers(restSen, "pnum").find()) {
                Matcher mTemp = matchers(restSen, "pCityWithCode");
                if (mTemp.lookingAt()) {
                    type |= 4;
                    end += mTemp.end();
                }
            } else {
                Matcher mTemp2 = matchers(restSen, "pCityNoCode");
                if (mTemp2.lookingAt()) {
                    end += mTemp2.end();
                }
            }
            indAddr.add(new int[]{start, end, type});
        }
        Matcher mAll2 = matchers(sentence, "pat");
        while (mAll2.find()) {
            int type2 = 2;
            int start2 = mAll2.start(1);
            int end2 = mAll2.end(1);
            String restSen2 = sentence.substring(end2);
            if (matchers(restSen2, "pnum").find()) {
                Matcher mTemp3 = matchers(restSen2, "pCityWithCode");
                if (mTemp3.lookingAt()) {
                    type2 = 2 | 4;
                    end2 += mTemp3.end();
                }
            } else {
                Matcher mTemp4 = matchers(restSen2, "pCityNoCode");
                if (mTemp4.lookingAt()) {
                    end2 += mTemp4.end();
                }
            }
            indAddr.add(new int[]{start2, end2, type2});
            i = 2;
        }
        Matcher mAll3 = matchers(sentence, "pPos");
        while (mAll3.find()) {
            int[] iArr = new int[3];
            iArr[0] = mAll3.start();
            iArr[1] = mAll3.end();
            iArr[i] = 4;
            indAddr.add(iArr);
        }
        int n = indAddr.size();
        if (n == 0) {
            return new int[1];
        }
        int n2 = merge(indAddr, n, sentence);
        int[] re = new int[((n2 * 2) + 1)];
        re[0] = n2;
        for (int i2 = 0; i2 < n2; i2++) {
            re[(i2 * 2) + 1] = indAddr.get(i2)[0];
            re[(i2 * 2) + i] = indAddr.get(i2)[1] - 1;
        }
        return re;
    }

    private static int merge(List<int[]> indx, int len, String source) {
        Collections.sort(indx, new Comparator<int[]>() {
            /* class com.huawei.g11n.tmr.address.pt.SerEngine.AnonymousClass1 */

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
            } else {
                Matcher m = matchers(source.substring(indx.get(i)[1], indx.get(i + 1)[0]), "pCat");
                int t1 = indx.get(i)[2];
                int t2 = indx.get(i + 1)[2];
                if (m.matches() && (t1 & t2) == 0) {
                    indx.get(i)[1] = indx.get(i + 1)[1];
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
        return PatternCache.getPattern(reg, "com.huawei.g11n.tmr.address.pt.ReguExp").matcher(t);
    }
}
