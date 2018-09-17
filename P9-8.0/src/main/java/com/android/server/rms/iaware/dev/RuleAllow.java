package com.android.server.rms.iaware.dev;

import android.rms.iaware.AwareConfig.SubItem;
import android.rms.iaware.AwareLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RuleAllow extends RuleBase {
    private static final int MATCH_PARAM_NUM = 2;
    private static final String MODE = "mode";
    private static final String PRIO = "prio";
    private static final String SPLIT_SYMBOL = ",";
    private static final String TAG = "RuleAllow";
    private final List<Integer> mTypeList = new ArrayList();

    public boolean fillRuleInfo(SubItem subItem) {
        if (subItem == null) {
            return false;
        }
        Map<String, String> properties = subItem.getProperties();
        if (properties == null || properties.isEmpty()) {
            AwareLog.e(TAG, "WifiSchedFeature properties null");
            return false;
        }
        try {
            this.mMode = Long.parseLong((String) properties.get("mode"));
            this.mPriority = Integer.parseInt((String) properties.get(PRIO));
            if (this.mMode < 0 || this.mPriority < 0) {
                AwareLog.e(TAG, "WifiSchedFeature mode or prio error");
                return false;
            }
            String typeStr = subItem.getValue();
            if (typeStr == null || typeStr.trim().isEmpty()) {
                AwareLog.e(TAG, "WifiSchedFeature typeStr null");
                return true;
            } else if (transStringToInteger(typeStr)) {
                return true;
            } else {
                AwareLog.e(TAG, "WifiSchedFeature transStringToInteger error");
                return false;
            }
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "NumberFormatException, mode is not number");
            return false;
        }
    }

    private boolean transStringToInteger(String typeStr) {
        String[] infoStr = typeStr.split(",");
        int i = 0;
        while (i < infoStr.length) {
            try {
                if (!(infoStr[i] == null || infoStr[i].trim().isEmpty())) {
                    this.mTypeList.add(Integer.valueOf(Integer.parseInt(infoStr[i].trim())));
                }
                i++;
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "transScenceInfoToInteger occur exception, NumberFormatException !!");
                return false;
            }
        }
        return true;
    }

    public boolean isMatch(Object... obj) {
        if (obj == null || obj.length < 2) {
            return false;
        }
        try {
            Object typeObj = obj[1];
            return typeObj != null && (typeObj instanceof Integer) && this.mTypeList.contains(typeObj);
        } catch (ArrayIndexOutOfBoundsException e) {
            AwareLog.e(TAG, "ArrayIndexOutOfBoundsException, length is " + obj.length);
            return false;
        }
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("RuleAllow, mode : ").append(this.mMode);
        s.append("mTypeList : ").append(this.mTypeList.toString());
        return s.toString();
    }
}
