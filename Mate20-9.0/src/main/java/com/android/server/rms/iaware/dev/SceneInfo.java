package com.android.server.rms.iaware.dev;

import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SceneInfo {
    public static final int INVALID_VALUE = -1;
    private static final String ITEM_RULE = "rule";
    public static final String ITEM_RULE_ALLOW = "allow";
    private static final String ITEM_RULE_DEFAULT = "Default";
    private static final String ITEM_RULE_IS_HOME = "IsHome";
    public static final String ITEM_RULE_NOT_ALLOW = "not_allow";
    private static final String ITEM_RULE_PRE_RECOG = "Pre_Recog";
    public static final String ITEM_RULE_SERVICE = "Service_Rule";
    private static final String TAG = "SceneInfo";
    private static final Map<String, Class<?>> mRuleObjMap = new ArrayMap();
    private RuleBase mDefaultRule = null;
    private final Map<String, String> mItemValues = new ArrayMap();
    private final List<RuleBase> mRulesList = new ArrayList();
    private int mSceneId;
    private String mSceneName;

    static {
        mRuleObjMap.put(ITEM_RULE_ALLOW, RuleAllow.class);
        mRuleObjMap.put(ITEM_RULE_SERVICE, RuleService.class);
        mRuleObjMap.put(ITEM_RULE_NOT_ALLOW, RuleNotAllow.class);
        mRuleObjMap.put(ITEM_RULE_PRE_RECOG, RulePreRecog.class);
        mRuleObjMap.put(ITEM_RULE_IS_HOME, RuleIsHome.class);
        mRuleObjMap.put(ITEM_RULE_DEFAULT, RuleDefault.class);
    }

    public SceneInfo(String sceneName, int sceneId) {
        this.mSceneName = sceneName;
        this.mSceneId = sceneId;
    }

    public SceneInfo(AwareConfig.Item item) {
        init(item);
    }

    private void init(AwareConfig.Item item) {
        if (item != null) {
            Map<String, String> configPropertries = item.getProperties();
            if (configPropertries != null) {
                this.mItemValues.putAll(configPropertries);
            }
            List<AwareConfig.SubItem> ruleItemList = item.getSubItemList();
            if (ruleItemList != null) {
                parseRuleList(ruleItemList);
            }
            this.mSceneName = this.mItemValues.get(DevXmlConfig.ITEM_SCENE_NAME);
            this.mSceneId = getSceneId();
        }
    }

    public boolean fillSceneInfo(List<AwareConfig.SubItem> subItemList, List<String> ruleList) {
        if (subItemList == null) {
            return false;
        }
        for (AwareConfig.SubItem subItem : subItemList) {
            if (subItem == null) {
                AwareLog.e(TAG, "WifiSchedFeature subItem null");
                return false;
            }
            Map<String, String> properties = subItem.getProperties();
            if (properties == null || properties.isEmpty()) {
                AwareLog.e(TAG, "WifiSchedFeature properties null");
                return false;
            }
            String rule = properties.get(ITEM_RULE);
            if (rule == null || !ruleList.contains(rule)) {
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
        while (i < size && this.mRulesList.get(i).mPriority <= prio) {
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
            if (this.mRulesList.get(i).isMatch(obj)) {
                return i;
            }
        }
        return -1;
    }

    public long getMode(int index) {
        if (index < 0 || index >= this.mRulesList.size()) {
            return -1;
        }
        RuleBase rule = this.mRulesList.get(index);
        if (rule == null) {
            return -1;
        }
        return rule.getMode();
    }

    private RuleBase createRuleBaseObj(String rule) {
        if (rule == null) {
            return null;
        }
        Class<?> classObj = mRuleObjMap.get(rule);
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

    public boolean parseRuleList(List<AwareConfig.SubItem> ruleItemList) {
        if (ruleItemList == null) {
            return false;
        }
        for (AwareConfig.SubItem subItem : ruleItemList) {
            if (subItem != null) {
                Map<String, String> properties = subItem.getProperties();
                if (properties != null) {
                    String rule = properties.get(ITEM_RULE);
                    if (rule != null) {
                        RuleBase ruleObj = createRuleBaseObj(rule);
                        if (ruleObj != null && ruleObj.fillRuleInfo(subItem)) {
                            if (rule.equals(ITEM_RULE_DEFAULT)) {
                                this.mDefaultRule = ruleObj;
                            } else {
                                this.mRulesList.add(ruleObj);
                            }
                        }
                    }
                }
            }
        }
        if (this.mDefaultRule != null) {
            this.mRulesList.add(this.mDefaultRule);
            this.mDefaultRule = null;
        }
        return true;
    }

    public RuleBase getRuleBase(int index) {
        if (index < 0 || index >= this.mRulesList.size()) {
            return null;
        }
        return this.mRulesList.get(index);
    }

    public String toString() {
        return "scene name : " + this.mSceneName + ", scene id : " + this.mSceneId + ", rule num : " + this.mRulesList.size() + ",mItemValues [ " + this.mItemValues.toString() + " ]" + ",mRulesList [ " + this.mRulesList.toString() + " ]";
    }

    public int getSceneId() {
        String sceneIdOrg = this.mItemValues.get(DevXmlConfig.ITEM_SCENE_ID);
        try {
            return Integer.parseInt(sceneIdOrg);
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "NumberFormatException, scene id :" + sceneIdOrg);
            return -1;
        }
    }

    public String getRuleItemValue(String ruleItem, int index) {
        if (ruleItem == null) {
            AwareLog.e(TAG, "ruleItem is null, error!");
            return null;
        } else if (index < 0 || index >= this.mRulesList.size()) {
            AwareLog.e(TAG, "index is illegal, index:" + index + ", list size:" + this.mRulesList.size());
            return null;
        } else {
            RuleBase ruleObject = this.mRulesList.get(index);
            if (ruleObject != null) {
                return ruleObject.getItemValue(ruleItem);
            }
            AwareLog.e(TAG, "ruleObject is null, error!");
            return null;
        }
    }

    public List<String> getRuleItemValues(String ruleItem) {
        if (ruleItem == null) {
            AwareLog.e(TAG, "ruleItem is null, error!");
            return null;
        }
        List<String> res = new ArrayList<>();
        for (RuleBase ruleObject : this.mRulesList) {
            if (ruleObject != null) {
                String itemValue = ruleObject.getItemValue(ruleItem);
                if (itemValue != null) {
                    res.add(itemValue);
                }
            }
        }
        return res;
    }
}
