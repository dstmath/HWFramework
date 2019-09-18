package com.android.server.rms.iaware.dev;

import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RuleService extends RuleBase {
    private static final int MATCH_PARAM_NUM = 2;
    private static final String MODE = "mode";
    private static final String PRIO = "prio";
    private static final String SPLIT_SYMBOL = ",";
    private static final String TAG = "RuleService";
    public final List<String> mServiceList = new ArrayList();

    public List<String> getServiceList() {
        return this.mServiceList;
    }

    public boolean fillRuleInfo(AwareConfig.SubItem subItem) {
        if (subItem == null) {
            return false;
        }
        Map<String, String> properties = subItem.getProperties();
        if (properties == null || properties.isEmpty()) {
            AwareLog.e(TAG, "WifiSchedFeature properties null");
            return false;
        }
        try {
            this.mMode = Long.parseLong(properties.get("mode"));
            this.mPriority = Integer.parseInt(properties.get(PRIO));
            if (this.mMode < 0 || this.mPriority < 0) {
                AwareLog.e(TAG, "WifiSchedFeature mode or prio error");
                return false;
            }
            String pkgStr = subItem.getValue();
            if (pkgStr == null || pkgStr.trim().isEmpty()) {
                AwareLog.e(TAG, "WifiSchedFeature pkgStr error");
                return true;
            }
            String[] infoStr = pkgStr.split(",");
            for (int i = 0; i < infoStr.length; i++) {
                if (infoStr[i] != null && !infoStr[i].trim().isEmpty()) {
                    this.mServiceList.add(infoStr[i].trim());
                }
            }
            return true;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "NumberFormatException, mode is not number");
            return false;
        }
    }

    public boolean isMatch(Object... obj) {
        if (obj == null || obj.length < 2) {
            return false;
        }
        try {
            Object typeObj = obj[0];
            if (typeObj != null && (typeObj instanceof String) && this.mServiceList.contains(typeObj)) {
                return true;
            }
            return false;
        } catch (ArrayIndexOutOfBoundsException e) {
            AwareLog.e(TAG, "ArrayIndexOutOfBoundsException, length is " + obj.length);
            return false;
        }
    }

    public String toString() {
        return "RuleService, mode : " + this.mMode;
    }
}
