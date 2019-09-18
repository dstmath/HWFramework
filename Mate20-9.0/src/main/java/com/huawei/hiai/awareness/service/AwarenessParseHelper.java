package com.huawei.hiai.awareness.service;

import android.text.TextUtils;
import com.huawei.hiai.awareness.AwarenessConstants;
import com.huawei.hiai.awareness.common.log.LogUtil;
import com.huawei.opcollect.collector.receivercollection.SysEventUtil;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class AwarenessParseHelper {
    private static final String MAPKEY = "MAPFENCE";
    private static final String TAG = "AwarenessParseHelper";
    private static HashMap<String, AwarenessFence> fenceHashMap = new HashMap<>();

    public static String parseAwarenessKey(AwarenessFence awarenessFence) {
        List<HashMap<Integer, ArrayList<String>>> storeFenceStr = new ArrayList<>();
        parseAwarenessFence(awarenessFence, storeFenceStr, null);
        return initStoreFenceStr(storeFenceStr).toString();
    }

    public static String parseAwareness2String(AwarenessFence awarenessFence) {
        List<HashMap<Integer, ArrayList<String>>> storeFenceStr = new ArrayList<>();
        parseAwarenessFence(awarenessFence, storeFenceStr, null);
        return initStoreFenceStr(storeFenceStr).toString();
    }

    private static void parseAwarenessFence(AwarenessFence awarenessFence, List<HashMap<Integer, ArrayList<String>>> storeFenceStr, HashMap<Integer, ArrayList<String>> tmpkeyFences) {
        if (awarenessFence != null) {
            int logic = awarenessFence.getLogic();
            if (logic == 1) {
                if (tmpkeyFences != null) {
                    HashMap<Integer, ArrayList<String>> tmpStrList = tmpkeyFences;
                    Iterator<Integer> integerIterator = tmpStrList.keySet().iterator();
                    Integer integer = null;
                    if (integerIterator.hasNext()) {
                        integer = integerIterator.next();
                    }
                    if (integer != null) {
                        List<String> stringList = null;
                        Iterator<Map.Entry<Integer, ArrayList<String>>> entries = tmpStrList.entrySet().iterator();
                        while (true) {
                            if (!entries.hasNext()) {
                                break;
                            }
                            Map.Entry<Integer, ArrayList<String>> tmpHashmap = entries.next();
                            if (tmpHashmap.getKey().equals(integer)) {
                                stringList = tmpHashmap.getValue();
                                break;
                            }
                        }
                        if (stringList != null) {
                            stringList.add(awarenessFence.getCheckKey());
                            return;
                        }
                        return;
                    }
                    return;
                }
                HashMap<Integer, ArrayList<String>> keyFences = new HashMap<>();
                ArrayList<String> stringList2 = new ArrayList<>();
                stringList2.add(SysEventUtil.ON);
                stringList2.add(awarenessFence.getCheckKey());
                keyFences.put(Integer.valueOf(logic), stringList2);
                storeFenceStr.add(keyFences);
            } else if ((logic == 2 || logic == 3 || logic == 4) && awarenessFence.getListFences() != null) {
                int size = awarenessFence.getListFences().size();
                HashMap<Integer, ArrayList<String>> keyFences2 = new HashMap<>();
                ArrayList<String> stringList3 = new ArrayList<>();
                stringList3.add(size + "");
                keyFences2.put(Integer.valueOf(logic), stringList3);
                storeFenceStr.add(keyFences2);
                for (int i = 0; i < size; i++) {
                    parseAwarenessFence(awarenessFence.getListFences().get(i), storeFenceStr, keyFences2);
                }
            }
        }
    }

    private static StringBuffer initStoreFenceStr(List<HashMap<Integer, ArrayList<String>>> storeFenceStr) {
        StringBuffer stringBuffer = new StringBuffer();
        if (storeFenceStr != null) {
            int size = storeFenceStr.size();
            for (int i = 0; i < size; i++) {
                HashMap<Integer, ArrayList<String>> listHashMap = storeFenceStr.get(i);
                if (listHashMap.size() > 0) {
                    if (i == 0) {
                        getFenceString(listHashMap, stringBuffer, false);
                    } else {
                        getFenceString(listHashMap, stringBuffer, true);
                    }
                }
            }
        }
        LogUtil.d(TAG, " stringBuffer :" + stringBuffer.toString());
        return stringBuffer;
    }

    private static void getFenceString(HashMap<Integer, ArrayList<String>> listHashMap, StringBuffer content, boolean isNeedStr) {
        if (listHashMap != null && content != null) {
            Iterator<Integer> integerIterator = listHashMap.keySet().iterator();
            Integer integer = null;
            if (integerIterator.hasNext()) {
                integer = integerIterator.next();
            }
            List<String> stringList = null;
            if (integer != null) {
                Iterator<Map.Entry<Integer, ArrayList<String>>> entries = listHashMap.entrySet().iterator();
                while (true) {
                    if (!entries.hasNext()) {
                        break;
                    }
                    Map.Entry<Integer, ArrayList<String>> tmpHashmap = entries.next();
                    if (tmpHashmap.getKey().equals(integer)) {
                        stringList = tmpHashmap.getValue();
                        break;
                    }
                }
            }
            if (stringList != null) {
                int size = stringList.size();
                if (isNeedStr) {
                    content.append("!");
                }
                if (size > 0) {
                    String tmpStr = stringList.get(0);
                    if (Integer.parseInt(tmpStr) > size - 1) {
                        if (size == 1) {
                            content.append(integer).append("R").append(tmpStr);
                        } else {
                            content.append(integer).append("R").append(tmpStr).append("!");
                        }
                        for (int i = 1; i < size; i++) {
                            String tmpStr2 = stringList.get(i);
                            if (!TextUtils.isEmpty(tmpStr2)) {
                                if (i == 1) {
                                    content.append(tmpStr2);
                                } else {
                                    content.append("!").append(tmpStr2);
                                }
                            }
                        }
                        return;
                    }
                    content.append(integer).append(":");
                    for (int i2 = 0; i2 < size; i2++) {
                        String tmpStr3 = stringList.get(i2);
                        if (!TextUtils.isEmpty(tmpStr3)) {
                            if (i2 == 0) {
                                content.append(tmpStr3);
                            } else {
                                content.append(";").append(tmpStr3);
                            }
                        }
                    }
                }
            }
        }
    }

    public static AwarenessFence parseString2Awareness(String storeStr) {
        if (storeStr == null) {
            return null;
        }
        fenceHashMap.clear();
        if (!storeStr.contains("!")) {
            String[] oneFenceStr = storeStr.split(";");
            if (oneFenceStr.length == 2) {
                String[] logicCountStr = oneFenceStr[0].split(":");
                int logic = -1;
                int count = -1;
                if (logicCountStr.length == 2) {
                    try {
                        logic = Integer.parseInt(logicCountStr[0]);
                        count = Integer.parseInt(logicCountStr[1]);
                    } catch (NumberFormatException e) {
                    }
                }
                if (logic == 1 && count == 1) {
                    return getAwarenessFence(storeStr);
                }
                return null;
            } else if (oneFenceStr.length >= 2) {
                String[] logicCountStr2 = oneFenceStr[0].split(":");
                int logic2 = -1;
                int count2 = -1;
                if (logicCountStr2.length == 2) {
                    try {
                        logic2 = Integer.parseInt(logicCountStr2[0]);
                        count2 = Integer.parseInt(logicCountStr2[1]);
                    } catch (NumberFormatException e2) {
                    }
                }
                String leaveStr = storeStr.substring(oneFenceStr[0].length() + 1);
                if (TextUtils.isEmpty(leaveStr) || logic2 == 1 || count2 < 1) {
                    return null;
                }
                return getAwarenessOneMoreFence(logic2, count2, leaveStr);
            }
        }
        Stack<String> stringStack = new Stack<>();
        List<String> fenceList = new ArrayList<>();
        for (String push : storeStr.split("!")) {
            stringStack.push(push);
        }
        AwarenessFence awarenessFence = null;
        while (!stringStack.isEmpty()) {
            String tmpStr = stringStack.pop();
            if (tmpStr.contains("R")) {
                String[] spilitRstr = tmpStr.split("R");
                awarenessFence = assembleAwareness(spilitRstr[0], spilitRstr[1], fenceList);
                String key = MAPKEY + fenceList.size();
                if (!stringStack.isEmpty()) {
                    fenceList.add(0, key);
                    fenceHashMap.put(key, awarenessFence);
                }
            } else {
                fenceList.add(0, tmpStr);
            }
        }
        return awarenessFence;
    }

    private static AwarenessFence assembleAwareness(String logicStr, String countStr, List<String> fenceList) {
        if (TextUtils.isEmpty(logicStr) || TextUtils.isEmpty(countStr)) {
            return null;
        }
        int logic = -1;
        int count = -1;
        try {
            logic = Integer.parseInt(logicStr);
            count = Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            LogUtil.e(TAG, "assembleAwareness NumberFormatException");
        }
        if (logic == -1 || count == -1 || count > fenceList.size()) {
            return null;
        }
        if (logic == 2) {
            List<AwarenessFence> awarenessFenceList = getListAwareness(count, fenceList);
            if (awarenessFenceList.size() != 0) {
                return new AwarenessFence(2, awarenessFenceList);
            }
            return null;
        } else if (logic == 4) {
            List<AwarenessFence> awarenessFenceList2 = getListAwareness(count, fenceList);
            if (awarenessFenceList2.size() != 0) {
                return new AwarenessFence(4, awarenessFenceList2);
            }
            return null;
        } else if (logic != 3) {
            return null;
        } else {
            List<AwarenessFence> awarenessFenceList3 = getListAwareness(count, fenceList);
            if (awarenessFenceList3.size() != 0) {
                return new AwarenessFence(3, awarenessFenceList3);
            }
            return null;
        }
    }

    private static List<AwarenessFence> getListAwareness(int count, List<String> fenceList) {
        List<AwarenessFence> awarenessFences = new ArrayList<>();
        int removeSize = 0;
        for (int i = 0; i < count; i++) {
            String tmpStrfence = fenceList.get(i);
            if (!tmpStrfence.contains(MAPKEY) || !fenceHashMap.containsKey(tmpStrfence)) {
                removeSize++;
                AwarenessFence tmpAwareness = getAwarenessFence(tmpStrfence);
                if (tmpAwareness != null) {
                    awarenessFences.add(tmpAwareness);
                }
            } else {
                awarenessFences.add(fenceHashMap.get(tmpStrfence));
                LogUtil.d(TAG, "   fenceHashMap.remove  ");
                fenceHashMap.remove(tmpStrfence);
            }
        }
        for (int i2 = 0; i2 < removeSize; i2++) {
            fenceList.remove(0);
        }
        LogUtil.d(TAG, " removeSize : " + removeSize + " count : " + count);
        return awarenessFences;
    }

    private static AwarenessFence getAwarenessOneMoreFence(int logic, int count, String oneMoreString) {
        String[] oneStringFence = oneMoreString.split(";");
        List<AwarenessFence> awarenessFences = new ArrayList<>();
        for (String awarenessFence : oneStringFence) {
            AwarenessFence awarenessFence2 = getAwarenessFence(awarenessFence);
            if (awarenessFence2 != null) {
                awarenessFences.add(awarenessFence2);
            }
        }
        if (awarenessFences.size() > 0 && awarenessFences.size() == count && (logic == 3 || logic == 2 || logic == 4)) {
            return new AwarenessFence(logic, awarenessFences);
        }
        return null;
    }

    private static AwarenessFence getAwarenessFence(String oneFenceStr) {
        if (oneFenceStr != null && !oneFenceStr.contains(":") && oneFenceStr.contains(",")) {
            String[] activity = oneFenceStr.split(",");
            int type = Integer.parseInt(activity[0]);
            int status = Integer.parseInt(activity[1]);
            int action = 0;
            String secondAction = "";
            try {
                if (activity[2] != null && activity[2].contains(AwarenessConstants.SECOND_ACTION_SPLITE_TAG)) {
                    String[] actionGroup = activity[2].split(AwarenessConstants.SECOND_ACTION_SPLITE_TAG);
                    if (actionGroup.length == 2) {
                        action = Integer.parseInt(actionGroup[0]);
                        secondAction = actionGroup[1];
                    }
                } else if (activity[2] != null && !activity[2].contains(AwarenessConstants.SECOND_ACTION_SPLITE_TAG)) {
                    action = Integer.parseInt(activity[2]);
                }
                return new AwarenessFence(type, status, action, secondAction);
            } catch (NumberFormatException e) {
                LogUtil.d(TAG, " getAwarenessFence NumberFormatException 1 ");
            }
        }
        if (oneFenceStr == null || !oneFenceStr.contains(";")) {
            return null;
        }
        String[] tmpStrGroup = oneFenceStr.split(";");
        if (tmpStrGroup.length > 1) {
            String tmpStr = tmpStrGroup[0];
            if (tmpStr.contains(":")) {
                String[] topStr = tmpStr.split(":");
                int logic = 0;
                if (topStr.length == 2) {
                    try {
                        logic = Integer.parseInt(topStr[0]);
                    } catch (NumberFormatException e2) {
                        LogUtil.d(TAG, " getAwarenessFence NumberFormatException 2 ");
                    }
                }
                if (logic == 3 || logic == 2 || logic == 4) {
                    List<AwarenessFence> fenceList = new ArrayList<>();
                    int action2 = 0;
                    String secondAction2 = "";
                    for (int i = 0; i < tmpStrGroup.length - 1; i++) {
                        String[] activity2 = tmpStrGroup[i + 1].split(",");
                        try {
                            int type2 = Integer.parseInt(activity2[0]);
                            int status2 = Integer.parseInt(activity2[1]);
                            if (activity2[2] != null && activity2[2].contains(AwarenessConstants.SECOND_ACTION_SPLITE_TAG)) {
                                String[] actionGroup2 = activity2[2].split(AwarenessConstants.SECOND_ACTION_SPLITE_TAG);
                                if (actionGroup2.length == 2) {
                                    action2 = Integer.parseInt(actionGroup2[0]);
                                    secondAction2 = actionGroup2[1];
                                }
                            } else if (activity2[2] != null && !activity2[2].contains(AwarenessConstants.SECOND_ACTION_SPLITE_TAG)) {
                                action2 = Integer.parseInt(activity2[2]);
                            }
                            fenceList.add(new AwarenessFence(type2, status2, action2, secondAction2));
                        } catch (NumberFormatException e3) {
                            LogUtil.d(TAG, " getAwarenessFence NumberFormatException 3 ");
                        }
                    }
                    return new AwarenessFence(logic, fenceList);
                } else if (logic == 1) {
                    String[] activity3 = tmpStrGroup[1].split(",");
                    int type3 = Integer.parseInt(activity3[0]);
                    int status3 = Integer.parseInt(activity3[1]);
                    int action3 = 0;
                    String secondAction3 = "";
                    try {
                        if (activity3[2] != null && activity3[2].contains(AwarenessConstants.SECOND_ACTION_SPLITE_TAG)) {
                            String[] actionGroup3 = activity3[2].split(AwarenessConstants.SECOND_ACTION_SPLITE_TAG);
                            if (actionGroup3.length == 2) {
                                action3 = Integer.parseInt(actionGroup3[0]);
                                secondAction3 = actionGroup3[1];
                            }
                        } else if (activity3[2] != null && !activity3[2].contains(AwarenessConstants.SECOND_ACTION_SPLITE_TAG)) {
                            action3 = Integer.parseInt(activity3[2]);
                        }
                        return new AwarenessFence(type3, status3, action3, secondAction3);
                    } catch (NumberFormatException e4) {
                        LogUtil.d(TAG, " getAwarenessFence NumberFormatException 4 ");
                    }
                }
            }
        }
        return null;
    }

    public static String parseTimeLong2SecondAction(long time) {
        String secondAction;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int year = calendar.get(1);
        int month = calendar.get(2) + 1;
        int day = calendar.get(5);
        int hour24 = calendar.get(11);
        LogUtil.d(TAG, "secondAction : " + secondAction);
        return secondAction;
    }
}
