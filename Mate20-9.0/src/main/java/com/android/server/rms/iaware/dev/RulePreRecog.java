package com.android.server.rms.iaware.dev;

import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RulePreRecog extends RuleBase {
    private static final int MATCH_PARAM_NUM = 1;
    private static final String SPLIT_SYMBOL = ",";
    private static final String TAG = "RulePreRecog";
    private final List<Integer> mValueList = new ArrayList();

    public boolean fillRuleInfo(AwareConfig.SubItem subItem) {
        if (subItem == null) {
            return false;
        }
        Map<String, String> properties = subItem.getProperties();
        if (properties == null) {
            return false;
        }
        this.mItemValue.putAll(properties);
        String typeStr = subItem.getValue();
        if (typeStr == null || typeStr.trim().isEmpty()) {
            return false;
        }
        List<Integer> typeList = transStringToInteger(typeStr);
        if (typeList == null) {
            return false;
        }
        this.mValueList.addAll(typeList);
        return true;
    }

    private List<Integer> transStringToInteger(String typeStr) {
        if (typeStr == null) {
            AwareLog.e(TAG, "value is null, error!");
            return null;
        }
        String[] infoStr = typeStr.split(",");
        List<Integer> valueList = new ArrayList<>();
        int i = 0;
        while (i < infoStr.length) {
            try {
                if (infoStr[i] != null) {
                    if (!infoStr[i].trim().isEmpty()) {
                        valueList.add(Integer.valueOf(Integer.parseInt(infoStr[i].trim())));
                        i++;
                    }
                }
                return null;
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "transScenceInfoToInteger occur exception, NumberFormatException !!");
                return null;
            }
        }
        return valueList;
    }

    public boolean isMatch(Object... obj) {
        if (obj == null || obj.length < 1) {
            return false;
        }
        Object processName = obj[0];
        if (processName == null || !(processName instanceof String)) {
            return false;
        }
        int appRecoType = AppTypeRecoManager.getInstance().getAppType((String) processName);
        AwareLog.d(TAG, ((String) processName) + " appRecoType is " + appRecoType);
        return this.mValueList.contains(Integer.valueOf(appRecoType));
    }

    public String toString() {
        return "RulePreRecog, mItemValue : [ " + this.mItemValue + " ]" + ", TypeToMode : [ " + this.mValueList.toString() + " ]";
    }
}
