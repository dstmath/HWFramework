package com.huawei.i18n.tmr.address.es;

import com.huawei.android.os.storage.StorageManagerExt;
import com.huawei.i18n.tmr.address.util.PatternCache;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchEngine {
    public static int[] search(String input) {
        List<int[]> startEndList = new ArrayList<>();
        List<int[]> addressList = new LinkedList<>();
        matchStreet(input, startEndList, addressList);
        matchSingleBuilding(input, addressList);
        matchNotSingleBuilding(input, startEndList, addressList);
        if (addressList.size() == 0) {
            return new int[]{0};
        }
        int addressNum = merge(addressList);
        int[] result = new int[((addressNum * 2) + 1)];
        result[0] = addressNum;
        for (int i = 0; i < addressNum; i++) {
            result[(i * 2) + 1] = addressList.get(i)[1];
            result[(i * 2) + 2] = addressList.get(i)[2] - 1;
        }
        return result;
    }

    private static void matchNotSingleBuilding(String input, List<int[]> startEndList, List<int[]> addressList) {
        boolean hasPrep = false;
        boolean hasOther = false;
        Matcher p5Matcher = matchers(input, "p5");
        while (p5Matcher.find()) {
            int start = p5Matcher.start();
            int end = p5Matcher.group().length() + start;
            Matcher pgrepMatcher = matchers(input.substring(0, start), "pgrep");
            if (pgrepMatcher.lookingAt()) {
                hasPrep = Pattern.compile("\\p{Blank}{1,6}").matcher(input.substring(pgrepMatcher.end(), start)).matches();
            }
            hasOther = isHasOther(input, startEndList, hasOther, start, end);
            if (hasPrep || hasOther) {
                Matcher p2Matcher = matchers(input.substring(end), "p2");
                if (p2Matcher.lookingAt()) {
                    end += p2Matcher.end();
                }
                addressList.add(new int[]{2, start, end});
            }
        }
    }

    private static boolean isHasOther(String input, List<int[]> startEndList, boolean hasOtherParam, int start, int end) {
        for (int[] eachOther : startEndList) {
            if (eachOther[0] <= end || eachOther[0] - end >= 10) {
                if (eachOther[1] < start && start - eachOther[1] < 10 && matchers(input.substring(eachOther[1], start), "pPrepAndSc").matches()) {
                    return true;
                }
            } else if (matchers(input.substring(end, eachOther[0]), "pPrepAndSc").matches()) {
                return true;
            }
        }
        return hasOtherParam;
    }

    private static void matchSingleBuilding(String input, List<int[]> addressList) {
        Matcher mAll = matchers(input, "p3");
        while (mAll.find()) {
            int start = mAll.start();
            int end = mAll.group().length() + start;
            Matcher cAll = matchers(input.substring(end), "p2");
            if (cAll.lookingAt()) {
                end += cAll.end();
            }
            addressList.add(new int[]{2, start, end});
        }
    }

    private static void matchStreet(String sentence, List<int[]> startEndList, List<int[]> addressList) {
        Matcher mAll = matchers(sentence, "p1");
        HashSet<String> blackDictionary = getBlackDictionarySet();
        while (mAll.find() && !blackDictionary.contains(mAll.group())) {
            int start = mAll.start();
            int end = mAll.group().length() + start;
            Matcher cityMatcher = matchers(sentence.substring(end), "p2");
            if (cityMatcher.lookingAt()) {
                end += cityMatcher.end();
            }
            addressList.add(new int[]{1, start, end});
            int[] startEndIndex = {0, 0};
            startEndIndex[0] = start;
            startEndIndex[1] = end;
            startEndList.add(startEndIndex);
        }
    }

    private static HashSet<String> getBlackDictionarySet() {
        return new HashSet<String>() {
            /* class com.huawei.i18n.tmr.address.es.SearchEngine.AnonymousClass1 */
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
    }

    private static int merge(List<int[]> addressList) {
        Collections.sort(addressList, new Comparator<int[]>() {
            /* class com.huawei.i18n.tmr.address.es.SearchEngine.AnonymousClass2 */

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

    private static Matcher matchers(String input, String regex) {
        String packageName = StorageManagerExt.INVALID_KEY_DESC;
        Package packag = SearchEngine.class.getPackage();
        if (packag != null) {
            packageName = packag.getName();
        }
        return PatternCache.getPattern(regex, packageName + ".RegularExpression").matcher(input);
    }
}
