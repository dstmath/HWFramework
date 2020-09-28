package com.huawei.g11n.tmr.address.fr;

import com.huawei.g11n.tmr.util.ItemList;
import com.huawei.g11n.tmr.util.PatternCache;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

public class SerEngine {
    private static final int BUI_MARK = 1;
    private static final int POS_MARK = 4;
    private static final int STR_MARK = 2;

    public static int getType(String sentence) {
        Matcher mBui1 = matchers(sentence, "pBui1");
        Matcher mStr1 = matchers(sentence, "pStr1");
        int type = 0;
        if (mBui1.find()) {
            type = 0 | 1;
        }
        if (mStr1.find()) {
            return type | 2;
        }
        return type;
    }

    public static int[] search(String sentence) {
        return merge(filter(filteDual(findAddress(sentence)), sentence).getData(), sentence).toArray();
    }

    private static int[] filteDual(int[] r) {
        ItemList list = new ItemList();
        int size = r[0];
        for (int i = 0; i < size; i++) {
            list.add(r[(i * 2) + 1], r[(i * 2) + 2]);
        }
        list.sort();
        list.delRel();
        return list.getData();
    }

    private static ItemList filter(int[] r, String sentence) {
        ItemList il = new ItemList();
        int size = r[0];
        if (size > 0) {
            il.add(r[1], r[2]);
        }
        for (int i = 1; i < size; i++) {
            int b1 = il.getCurrenBegin();
            int e1 = il.getCurrenEnd();
            int b = r[(i * 2) + 1];
            int e = r[(i * 2) + 2];
            if (e1 > b) {
                if (e1 < e) {
                    if (b > b1) {
                        il.changeEnd(b);
                        il.add(b, e);
                    } else if (b == b1) {
                        il.changeEnd(e);
                    }
                } else if (e1 == e && b > b1 && (getType(sentence.substring(b1, b)) & getType(sentence.substring(b, e)) & 2) != 0) {
                    il.changeEnd(b);
                    il.add(b, e);
                }
            } else if (e1 != b) {
                il.add(b, e);
            } else if ((getType(sentence.substring(b1, e1)) & getType(sentence.substring(b, e)) & 2) != 0) {
                il.add(b, e);
            } else {
                il.changeEnd(e);
            }
        }
        return il;
    }

    private static ItemList merge(int[] r, String sentence) {
        ItemList il = new ItemList();
        int size = r[0];
        if (size > 0) {
            il.add(r[1], r[2]);
        }
        for (int i = 1; i < size; i++) {
            int b1 = il.getCurrenBegin();
            int e1 = il.getCurrenEnd();
            int b = r[(i * 2) + 1];
            int e = r[(i * 2) + 2];
            if (e1 > b || !matchers(sentence.substring(il.getCurrenEnd(), b), "cat").matches() || (getType(sentence.substring(b1, e1)) & getType(sentence.substring(b, e))) != 0) {
                il.add(b, e);
            } else {
                il.changeEnd(e);
            }
        }
        return il;
    }

    private static int[] findAddress(String sentence) {
        List<Integer> beginList;
        Matcher m2 = null;
        Matcher mBui1 = matchers(sentence, "pBui1");
        Matcher mStr1 = matchers(sentence, "pStr1");
        Matcher mPos1 = matchers(sentence, "mPos1");
        Matcher mNum = matchers(sentence, "pNumber");
        List<Integer> arrayList = new ArrayList<>();
        List<Integer> arrayList2 = new ArrayList<>();
        List<Integer> beginListC = new ArrayList<>();
        while (mBui1.find()) {
            arrayList.add(Integer.valueOf(mBui1.start()));
            m2 = m2;
        }
        while (mStr1.find()) {
            arrayList2.add(Integer.valueOf(mStr1.start()));
            m2 = m2;
        }
        while (mNum.find()) {
            arrayList2.add(Integer.valueOf(mNum.start()));
            m2 = m2;
        }
        beginListC.addAll(arrayList);
        beginListC.addAll(arrayList2);
        mBui1.reset();
        mStr1.reset();
        Collections.sort(beginListC);
        Collections.sort(arrayList);
        Collections.sort(arrayList2);
        int type = 0;
        if (mBui1.find()) {
            type = 0 | 1;
        }
        if (mStr1.find()) {
            type |= 2;
        }
        if (mPos1.find()) {
            type |= 4;
        }
        List<Matcher> mList = new ArrayList<>();
        if (type == 1) {
            mList.add(matchers(sentence, "pBuiCity"));
            beginList = arrayList;
        } else if (type == 2) {
            mList.add(matchers(sentence, "pStrCity"));
            beginList = arrayList2;
        } else if (type == 3) {
            mList.add(matchers(sentence, "pBuiStrCity"));
            mList.add(matchers(sentence, "pBuiCity"));
            mList.add(matchers(sentence, "pStrCity"));
            beginList = beginListC;
        } else if (type == 5) {
            mList.add(matchers(sentence, "pBuiCity"));
            beginList = arrayList;
        } else if (type == 6) {
            mList.add(matchers(sentence, "pStrPos"));
            mList.add(matchers(sentence, "pStrCity"));
            beginList = arrayList2;
        } else if (type != 7) {
            beginList = new ArrayList<>();
        } else {
            mList.add(matchers(sentence, "pBuiCity"));
            mList.add(matchers(sentence, "pBuiStrPos"));
            mList.add(matchers(sentence, "pBuiStrCity"));
            mList.add(matchers(sentence, "pStrPos"));
            mList.add(matchers(sentence, "pStrCity"));
            beginList = beginListC;
        }
        List<Integer> indAddr = new LinkedList<>();
        for (Matcher mam : mList) {
            indAddr.addAll(cal(mam, beginList, type));
        }
        if (indAddr.size() == 0) {
            return new int[1];
        }
        int t = indAddr.size();
        int[] re = new int[(t + 1)];
        re[0] = t / 2;
        for (int i = 0; i < t; i++) {
            re[i + 1] = indAddr.get(i).intValue();
        }
        return re;
    }

    private static String cleanSymbol(String address) {
        Matcher m = matchers(address, "pSpe");
        if (m.lookingAt()) {
            return m.group(1);
        }
        return address;
    }

    private static Matcher matchers(String t, String reg) {
        return PatternCache.getPattern(reg, "com.huawei.g11n.tmr.address.fr.ReguExp").matcher(t);
    }

    private static List<Integer> cal(Matcher m2, List<Integer> beginList, int type) {
        List<Integer> indAddr = new LinkedList<>();
        String address = null;
        int t = 0;
        int index = 0;
        while (index < beginList.size()) {
            boolean flag = false;
            if (m2.find(beginList.get(index).intValue())) {
                address = m2.group();
                t = m2.start();
                flag = true;
            }
            index++;
            if (flag) {
                if ((type & 1) > 0) {
                    int lenSpe = address.length();
                    address = cleanSymbol(address);
                    t += lenSpe - address.length();
                }
                indAddr.add(Integer.valueOf(t));
                indAddr.add(Integer.valueOf(address.length() + t));
            }
        }
        return indAddr;
    }
}
