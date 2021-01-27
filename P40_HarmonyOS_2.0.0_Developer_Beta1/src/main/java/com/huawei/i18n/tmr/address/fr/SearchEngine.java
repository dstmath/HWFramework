package com.huawei.i18n.tmr.address.fr;

import com.huawei.android.os.storage.StorageManagerExt;
import com.huawei.i18n.tmr.address.util.ItemList;
import com.huawei.i18n.tmr.address.util.PatternCache;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

public class SearchEngine {
    private static final int BUI_MARK = 1;
    private static final int POS_MARK = 4;
    private static final int STR_MARK = 2;

    private static int getType(String input) {
        Matcher mBui1 = matchers(input, "pBui1");
        Matcher mStr1 = matchers(input, "pStr1");
        int type = 0;
        if (mBui1.find()) {
            type = 0 | 1;
        }
        if (mStr1.find()) {
            return type | 2;
        }
        return type;
    }

    public static int[] search(String input) {
        return merge(filter(filterDual(findAddress(input)), input).getData(), input).toArray();
    }

    private static int[] filterDual(int[] addressArr) {
        ItemList list = new ItemList();
        int size = addressArr[0];
        for (int i = 0; i < size; i++) {
            list.add(addressArr[(i * 2) + 1], addressArr[(i * 2) + 2]);
        }
        list.sort();
        list.delRel();
        return list.getData();
    }

    private static ItemList filter(int[] addressArr, String sentence) {
        ItemList itemList = new ItemList();
        int size = addressArr[0];
        if (size > 0) {
            itemList.add(addressArr[1], addressArr[2]);
        }
        for (int i = 1; i < size; i++) {
            int begin = itemList.getCurrentBegin();
            int end = itemList.getCurrentEnd();
            int begin2 = addressArr[(i * 2) + 1];
            int end2 = addressArr[(i * 2) + 2];
            if (end <= begin2) {
                if (end != begin2) {
                    itemList.add(begin2, end2);
                } else if ((getType(sentence.substring(begin, end)) & getType(sentence.substring(begin2, end2)) & 2) != 0) {
                    itemList.add(begin2, end2);
                } else {
                    itemList.changeEnd(end2);
                }
            } else if (end >= end2) {
                if (end == end2 && begin2 > begin && (getType(sentence.substring(begin, begin2)) & getType(sentence.substring(begin2, end2)) & 2) != 0) {
                    itemList.changeEnd(begin2);
                    itemList.add(begin2, end2);
                }
            } else if (begin2 > begin) {
                itemList.changeEnd(begin2);
                itemList.add(begin2, end2);
            } else if (begin2 == begin) {
                itemList.changeEnd(end2);
            }
        }
        return itemList;
    }

    private static ItemList merge(int[] addressArr, String sentence) {
        ItemList itemList = new ItemList();
        int size = addressArr[0];
        if (size > 0) {
            itemList.add(addressArr[1], addressArr[2]);
        }
        for (int i = 1; i < size; i++) {
            int begin = itemList.getCurrentBegin();
            int end = itemList.getCurrentEnd();
            int begin2 = addressArr[(i * 2) + 1];
            int end2 = addressArr[(i * 2) + 2];
            if (end > begin2) {
                itemList.add(begin2, end2);
            } else if (!matchers(sentence.substring(itemList.getCurrentEnd(), begin2), "cat").matches()) {
                itemList.add(begin2, end2);
            } else if ((getType(sentence.substring(begin, end)) & getType(sentence.substring(begin2, end2))) == 0) {
                itemList.changeEnd(end2);
            } else {
                itemList.add(begin2, end2);
            }
        }
        return itemList;
    }

    private static int[] findAddress(String input) {
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        List<Integer> beginListC = new ArrayList<>();
        Matcher pBui1Matcher = matchers(input, "pBui1");
        Matcher pStr1Matcher = matchers(input, "pStr1");
        Matcher pNumberMatcher = matchers(input, "pNumber");
        while (pBui1Matcher.find()) {
            arrayList.add(Integer.valueOf(pBui1Matcher.start()));
        }
        while (pStr1Matcher.find()) {
            arrayList2.add(Integer.valueOf(pStr1Matcher.start()));
        }
        while (pNumberMatcher.find()) {
            arrayList2.add(Integer.valueOf(pNumberMatcher.start()));
        }
        beginListC.addAll(arrayList);
        beginListC.addAll(arrayList2);
        pBui1Matcher.reset();
        pStr1Matcher.reset();
        Collections.sort(beginListC);
        Collections.sort(arrayList);
        Collections.sort(arrayList2);
        List<Integer> addressList = getAddressStartEndList(input, arrayList, arrayList2, beginListC, getType(input, pBui1Matcher, pStr1Matcher));
        if (addressList.size() == 0) {
            return new int[]{0};
        }
        int size = addressList.size();
        int[] result = new int[(size + 1)];
        result[0] = size / 2;
        for (int i = 0; i < size; i++) {
            result[i + 1] = addressList.get(i).intValue();
        }
        return result;
    }

    private static int getType(String input, Matcher mBui1, Matcher mStr1) {
        Matcher mPos1Matcher = matchers(input, "mPos1");
        int type = 0;
        if (mBui1.find()) {
            type = 0 | 1;
        }
        if (mStr1.find()) {
            type |= 2;
        }
        if (mPos1Matcher.find()) {
            return type | 4;
        }
        return type;
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0094 A[LOOP:0: B:19:0x008e->B:21:0x0094, LOOP_END] */
    private static List<Integer> getAddressStartEndList(String input, List<Integer> beginListB, List<Integer> beginListS, List<Integer> beginListC, int type) {
        List<Integer> beginList;
        List<Matcher> matcherList = new ArrayList<>();
        if (type != 1) {
            if (type == 2) {
                matcherList.add(matchers(input, "pStrCity"));
                beginList = beginListS;
            } else if (type == 3) {
                matcherList.add(matchers(input, "pBuiStrCity"));
                matcherList.add(matchers(input, "pBuiCity"));
                matcherList.add(matchers(input, "pStrCity"));
                beginList = beginListC;
            } else if (type != 5) {
                if (type == 6) {
                    matcherList.add(matchers(input, "pStrPos"));
                    matcherList.add(matchers(input, "pStrCity"));
                    beginList = beginListS;
                } else if (type != 7) {
                    beginList = new ArrayList<>();
                } else {
                    matcherList.add(matchers(input, "pBuiCity"));
                    matcherList.add(matchers(input, "pBuiStrPos"));
                    matcherList.add(matchers(input, "pBuiStrCity"));
                    matcherList.add(matchers(input, "pStrPos"));
                    matcherList.add(matchers(input, "pStrCity"));
                    beginList = beginListC;
                }
            }
            List<Integer> addressList = new LinkedList<>();
            for (Matcher matcher : matcherList) {
                addressList.addAll(getIndexAddressList(matcher, beginList, type));
            }
            return addressList;
        }
        matcherList.add(matchers(input, "pBuiCity"));
        beginList = beginListB;
        List<Integer> addressList2 = new LinkedList<>();
        while (r3.hasNext()) {
        }
        return addressList2;
    }

    private static String cleanSymbol(String input) {
        Matcher matcher = matchers(input, "pSpe");
        if (matcher.lookingAt()) {
            return matcher.group(1);
        }
        return input;
    }

    private static Matcher matchers(String input, String regex) {
        return PatternCache.getPattern(regex, "com.huawei.i18n.tmr.address.fr.RegularExpression").matcher(input);
    }

    private static List<Integer> getIndexAddressList(Matcher matcher, List<Integer> beginList, int type) {
        List<Integer> indexAddressList = new LinkedList<>();
        String address = StorageManagerExt.INVALID_KEY_DESC;
        int start = 0;
        int index = 0;
        while (index < beginList.size()) {
            boolean flag = false;
            if (matcher.find(beginList.get(index).intValue())) {
                address = matcher.group();
                start = matcher.start();
                flag = true;
            }
            index++;
            if (flag) {
                if ((type & 1) > 0) {
                    int addressLength = address.length();
                    address = cleanSymbol(address);
                    start += addressLength - address.length();
                }
                indexAddressList.add(Integer.valueOf(start));
                indexAddressList.add(Integer.valueOf(address.length() + start));
            }
        }
        return indexAddressList;
    }
}
