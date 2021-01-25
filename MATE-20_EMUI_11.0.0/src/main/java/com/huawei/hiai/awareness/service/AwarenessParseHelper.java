package com.huawei.hiai.awareness.service;

import android.text.TextUtils;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import com.huawei.hiai.awareness.log.Logger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AwarenessParseHelper {
    private static final String TAG = "AwarenessParseHelper";

    private AwarenessParseHelper() {
    }

    public static String parseAwareness2String(AwarenessFence awarenessFence) {
        List<HashMap<Integer, ArrayList<String>>> storeFenceList = new ArrayList<>(16);
        parseAwarenessFence(awarenessFence, storeFenceList, null);
        String fenceStr = initStoreFenceStr(storeFenceList);
        Logger.d(TAG, "parseAwareness2String() fenceStr :" + fenceStr);
        return fenceStr;
    }

    private static void parseAwarenessFence(AwarenessFence awarenessFence, List<HashMap<Integer, ArrayList<String>>> storeFenceList, HashMap<Integer, ArrayList<String>> tmpKeyFences) {
        if (awarenessFence == null || storeFenceList == null) {
            Logger.e(TAG, "parseAwarenessFence() param error");
            return;
        }
        int logic = awarenessFence.getLogic();
        if (logic == 1) {
            parseIsRelationFence(awarenessFence, storeFenceList, tmpKeyFences);
        } else if (awarenessFence.getListFences() != null) {
            int size = awarenessFence.getListFences().size();
            ArrayList<String> stringList = new ArrayList<>(1);
            stringList.add(size + "");
            HashMap<Integer, ArrayList<String>> keyFences = new HashMap<>(1);
            keyFences.put(Integer.valueOf(logic), stringList);
            storeFenceList.add(keyFences);
            for (AwarenessFence awarenessFenceTmp : awarenessFence.getListFences()) {
                parseAwarenessFence(awarenessFenceTmp, storeFenceList, keyFences);
            }
        }
    }

    private static void parseIsRelationFence(AwarenessFence awarenessFence, List<HashMap<Integer, ArrayList<String>>> storeFenceList, HashMap<Integer, ArrayList<String>> tmpKeyFences) {
        if (awarenessFence == null || storeFenceList == null) {
            Logger.e(TAG, "parseIsRelationFence() param error");
        } else if (tmpKeyFences != null) {
            Iterator<Integer> integerIterator = tmpKeyFences.keySet().iterator();
            int relation = -1;
            if (integerIterator.hasNext()) {
                relation = integerIterator.next().intValue();
            }
            if (relation != -1) {
                List<String> stringList = null;
                Iterator<Map.Entry<Integer, ArrayList<String>>> entries = tmpKeyFences.entrySet().iterator();
                while (true) {
                    if (!entries.hasNext()) {
                        break;
                    }
                    Map.Entry<Integer, ArrayList<String>> tmpHashMap = entries.next();
                    if (tmpHashMap.getKey().intValue() == relation) {
                        stringList = tmpHashMap.getValue();
                        break;
                    }
                }
                if (stringList != null) {
                    stringList.add(awarenessFence.getFenceKey());
                }
            }
        } else {
            ArrayList<String> stringList2 = new ArrayList<>(2);
            stringList2.add("1");
            stringList2.add(awarenessFence.getFenceKey());
            HashMap<Integer, ArrayList<String>> keyFences = new HashMap<>(1);
            keyFences.put(1, stringList2);
            storeFenceList.add(keyFences);
        }
    }

    private static String initStoreFenceStr(List<HashMap<Integer, ArrayList<String>>> storeFenceList) {
        StringBuffer fenceStringBuffer = new StringBuffer(128);
        if (storeFenceList == null) {
            return fenceStringBuffer.toString();
        }
        int size = storeFenceList.size();
        for (int i = 0; i < size; i++) {
            HashMap<Integer, ArrayList<String>> fenceHashMap = storeFenceList.get(i);
            if (fenceHashMap.size() > 0) {
                if (i == 0) {
                    getFenceString(fenceHashMap, fenceStringBuffer, false);
                } else {
                    getFenceString(fenceHashMap, fenceStringBuffer, true);
                }
            }
        }
        return fenceStringBuffer.toString();
    }

    private static void getFenceString(HashMap<Integer, ArrayList<String>> fenceHashMap, StringBuffer fenceStringBuffer, boolean isNeedAppendSplitKey) {
        if (fenceHashMap == null || fenceStringBuffer == null) {
            Logger.e(TAG, "getFenceString() param error");
            return;
        }
        Iterator<Integer> integerIterator = fenceHashMap.keySet().iterator();
        int relation = -1;
        if (integerIterator.hasNext()) {
            relation = integerIterator.next().intValue();
        }
        List<String> stringList = null;
        if (relation != -1) {
            Iterator<Map.Entry<Integer, ArrayList<String>>> entries = fenceHashMap.entrySet().iterator();
            while (true) {
                if (!entries.hasNext()) {
                    break;
                }
                Map.Entry<Integer, ArrayList<String>> tmpHashMap = entries.next();
                if (tmpHashMap.getKey().intValue() == relation) {
                    stringList = tmpHashMap.getValue();
                    break;
                }
            }
        }
        getFenceStringFromList(relation, stringList, fenceStringBuffer, isNeedAppendSplitKey);
    }

    private static void getFenceStringFromList(int relation, List<String> stringList, StringBuffer fenceStringBuffer, boolean isNeedAppendSplitKey) {
        if (relation == -1 || stringList == null || fenceStringBuffer == null) {
            Logger.e(TAG, "getFenceStringFromList() param error");
            return;
        }
        if (isNeedAppendSplitKey) {
            fenceStringBuffer.append(AwarenessInnerConstants.EXCLAMATORY_MARK_KEY);
        }
        int size = stringList.size();
        if (size > 0 && !buildContentForRelationFence(size, relation, stringList, fenceStringBuffer)) {
            fenceStringBuffer.append(relation);
            fenceStringBuffer.append(AwarenessInnerConstants.COLON_KEY);
            for (int i = 0; i < size; i++) {
                String tmpStr = stringList.get(i);
                if (!TextUtils.isEmpty(tmpStr)) {
                    if (i == 0) {
                        fenceStringBuffer.append(tmpStr);
                    } else {
                        fenceStringBuffer.append(AwarenessInnerConstants.SEMI_COLON_KEY);
                        fenceStringBuffer.append(tmpStr);
                    }
                }
            }
        }
    }

    private static boolean buildContentForRelationFence(int size, int relation, List<String> stringList, StringBuffer fenceStringBuffer) {
        if (relation == -1 || stringList == null || fenceStringBuffer == null) {
            Logger.e(TAG, "buildContentForRelationFence() param error");
            return true;
        }
        String tmpStr = stringList.get(0);
        try {
            if (Integer.parseInt(tmpStr) > size - 1) {
                if (size == 1) {
                    fenceStringBuffer.append(relation);
                    fenceStringBuffer.append(AwarenessInnerConstants.HAS_RELATION_FENCE_SEPARATE_KEY);
                    fenceStringBuffer.append(tmpStr);
                } else {
                    fenceStringBuffer.append(relation);
                    fenceStringBuffer.append(AwarenessInnerConstants.HAS_RELATION_FENCE_SEPARATE_KEY);
                    fenceStringBuffer.append(tmpStr);
                    fenceStringBuffer.append(AwarenessInnerConstants.EXCLAMATORY_MARK_KEY);
                }
                for (int i = 1; i < size; i++) {
                    String tmpStr2 = stringList.get(i);
                    if (!TextUtils.isEmpty(tmpStr2)) {
                        if (i == 1) {
                            fenceStringBuffer.append(tmpStr2);
                        } else {
                            fenceStringBuffer.append(AwarenessInnerConstants.EXCLAMATORY_MARK_KEY);
                            fenceStringBuffer.append(tmpStr2);
                        }
                    }
                }
                return true;
            }
            Logger.d(TAG, "buildContentForRelationFence() needMergeCount not satisfy");
            return false;
        } catch (NumberFormatException e) {
            Logger.e(TAG, "buildContentForRelationFence() NumberFormatException");
            return true;
        }
    }

    public static String parseTimeLong2SecondAction(long timeLong) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeLong);
        String secondAction = calendar.get(1) + AwarenessInnerConstants.DASH_KEY + (calendar.get(2) + 1) + AwarenessInnerConstants.DASH_KEY + calendar.get(5) + AwarenessInnerConstants.TIME_FENCE_DAY_TIME_SPLIT_KEY + calendar.get(11) + AwarenessInnerConstants.TIME_FENCE_HOUR_MINUTE_SPLIT_KEY + calendar.get(12);
        Logger.d(TAG, "parseTimeLong2SecondAction() secondAction : " + secondAction);
        return secondAction;
    }
}
