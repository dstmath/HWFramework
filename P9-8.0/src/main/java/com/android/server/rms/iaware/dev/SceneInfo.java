package com.android.server.rms.iaware.dev;

import android.rms.iaware.AwareConfig.SubItem;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SceneInfo {
    public static final String ITEM_GPS_MODE = "mode";
    private static final String ITEM_RULE = "rule";
    public static final String ITEM_RULE_ALLOW = "allow";
    public static final String ITEM_RULE_NOT_ALLOW = "not_allow";
    public static final String ITEM_RULE_SERVICE = "Service_Rule";
    private static final String TAG = "SceneInfo";
    private static final Map<String, Class<?>> mRuleObjMap = new ArrayMap();
    private final List<RuleBase> mRulesList = new ArrayList();
    private int mSceneId;
    private String mSceneName;

    static {
        mRuleObjMap.put(ITEM_RULE_ALLOW, RuleAllow.class);
        mRuleObjMap.put(ITEM_RULE_SERVICE, RuleService.class);
        mRuleObjMap.put(ITEM_RULE_NOT_ALLOW, RuleNotAllow.class);
    }

    public SceneInfo(String sceneName, int sceneId) {
        this.mSceneName = sceneName;
        this.mSceneId = sceneId;
    }

    public boolean fillSceneInfo(List<SubItem> subItemList, List<String> ruleList) {
        if (subItemList == null) {
            return false;
        }
        for (SubItem subItem : subItemList) {
            if (subItem == null) {
                AwareLog.e(TAG, "WifiSchedFeature subItem null");
                return false;
            }
            Map<String, String> properties = subItem.getProperties();
            if (properties == null || properties.isEmpty()) {
                AwareLog.e(TAG, "WifiSchedFeature properties null");
                return false;
            }
            String rule = (String) properties.get(ITEM_RULE);
            if (rule == null || (ruleList.contains(rule) ^ 1) != 0) {
                AwareLog.e(TAG, "WifiSchedFeature ruleList not contains");
                return false;
            }
            RuleBase ruleObj = createRuleBaseObj(rule);
            if (ruleObj == null) {
                AwareLog.e(TAG, "WifiSchedFeature createRuleBaseObj error");
                return false;
            } else if (ruleObj.fillRuleInfo(subItem)) {
                insertRuleList(ruleObj);
            } else {
                AwareLog.e(TAG, "WifiSchedFeature fillRuleInfo error");
                return false;
            }
        }
        return true;
    }

    private void insertRuleList(RuleBase ruleObj) {
        int prio = ruleObj.getPriority();
        AwareLog.d(TAG, "prio is " + prio);
        int size = this.mRulesList.size();
        int i = 0;
        while (i < size && ((RuleBase) this.mRulesList.get(i)).mPriority <= prio) {
            i++;
        }
        this.mRulesList.add(i, ruleObj);
    }

    public int isMatch(Object... obj) {
        if (obj == null) {
            return -1;
        }
        int size = this.mRulesList.size();
        for (int i = 0; i < size; i++) {
            if (((RuleBase) this.mRulesList.get(i)).isMatch(obj)) {
                return i;
            }
        }
        return -1;
    }

    public long getMode(int index) {
        if (index < 0 || index >= this.mRulesList.size()) {
            return -1;
        }
        RuleBase rule = (RuleBase) this.mRulesList.get(index);
        if (rule == null) {
            return -1;
        }
        return rule.getMode();
    }

    private RuleBase createRuleBaseObj(String rule) {
        if (rule == null) {
            return null;
        }
        Class<?> classObj = (Class) mRuleObjMap.get(rule);
        if (classObj == null) {
            return null;
        }
        try {
            return (RuleBase) classObj.newInstance();
        } catch (InstantiationException e) {
            AwareLog.e(TAG, " InstantiationException, createRuleBaseObj error!");
            return null;
        } catch (IllegalAccessException e2) {
            AwareLog.e(TAG, " IllegalAccessException, createRuleBaseObj error!");
            return null;
        }
    }

    public RuleBase getRuleBase(String ruleName) {
        if (ruleName == null || ruleName.isEmpty()) {
            return null;
        }
        Class<?> classObj = (Class) mRuleObjMap.get(ruleName);
        if (classObj == null) {
            return null;
        }
        for (RuleBase rulebase : this.mRulesList) {
            if (rulebase != null && classObj.isInstance(rulebase)) {
                return rulebase;
            }
        }
        return null;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("scene name : ").append(this.mSceneName);
        s.append(", scene id : ").append(this.mSceneId);
        s.append(", rule num : ").append(this.mRulesList.size());
        s.append(", [ ").append(this.mRulesList.toString()).append(" ]");
        return s.toString();
    }

    public String getSceneName() {
        return this.mSceneName;
    }

    public void setSceneName(String sceneName) {
        this.mSceneName = sceneName;
    }

    public int getSceneId() {
        return this.mSceneId;
    }

    public void setSceneId(int seneId) {
        this.mSceneId = seneId;
    }
}
