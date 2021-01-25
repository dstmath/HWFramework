package com.android.server.rms.iaware.dev;

import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FeatureXmlConfigParserRt {
    private static final String COMMAND_TAG = "cmd";
    public static final int DEFAULT_MODE = -1;
    private static final String EMPTY_RULE = "";
    private static final String ID_TYPE = "id";
    private static final String MODE_TAG = "mode";
    private static final String PKG_TYPE = "pkg";
    private static final String PRIORITY_TAG = "prio";
    private static final String RULE_TAG = "rule";
    private static final String SCENE_ID_TAG = "scene_id";
    private static final String SCENE_NAME_TAG = "scenename";
    private static final String SCENE_TYPE_TAG = "SceneType";
    private static final String SPILIT_CHAR = ",";
    private static final String SUBSWITCH_ENABLE = "1";
    private static final String SUBSWITCH_TAG = "subswitch";
    private static final String TAG = "FeatureXmlConfigParserRT";

    public static class ConfigItem {
        public int itemId;
        public String itemName;
        public List<ConfigRule> ruleList = new ArrayList();
    }

    public static class ConfigRule {
        public String ruleCmd;
        public int ruleMode;
        public String ruleName;
        public int rulePriority;
        public Set<Integer> ruleSceneIdSets;
        public Set<String> ruleScenePkgSets;
        public SceneType ruleSceneType;

        public enum SceneType {
            SCENE_ID_TYPE,
            PKG_NAME_TYPE
        }

        public ConfigRule(String name) {
            this.ruleName = name;
        }

        public String toString() {
            if (this.ruleSceneIdSets != null) {
                return "mode: " + this.ruleMode + " cmd " + this.ruleCmd + " Scene: " + this.ruleSceneIdSets.toString();
            } else if (this.ruleScenePkgSets == null) {
                return "empty rule";
            } else {
                return "mode: " + this.ruleMode + " cmd " + this.ruleCmd + " Scene: " + this.ruleScenePkgSets.toString();
            }
        }
    }

    public static class FeatureXmlConfig {
        public List<ConfigItem> configItemList;
        public String featureName;
        public boolean subSwitch = false;

        public FeatureXmlConfig(String feature) {
            this.featureName = feature;
            this.configItemList = new ArrayList();
        }
    }

    private static void parseSceneId(String[] sceneStrs, Set<Integer> sceneIdSets) {
        for (String str : sceneStrs) {
            sceneIdSets.add(Integer.valueOf(Integer.parseInt(str)));
        }
    }

    private static void parseScenePkg(String[] sceneStrs, Set<String> scenePkgSets) {
        for (String str : sceneStrs) {
            scenePkgSets.add(str);
        }
    }

    private static boolean parseRuleSceneConfig(String scenes, ConfigRule rule) {
        String[] sceneStrs = scenes.split(SPILIT_CHAR);
        if (sceneStrs.length <= 0) {
            return false;
        }
        if (rule.ruleSceneType == ConfigRule.SceneType.SCENE_ID_TYPE) {
            rule.ruleSceneIdSets = new ArraySet();
            parseSceneId(sceneStrs, rule.ruleSceneIdSets);
            return true;
        }
        rule.ruleScenePkgSets = new ArraySet();
        parseScenePkg(sceneStrs, rule.ruleScenePkgSets);
        return true;
    }

    private static void parseRule(ConfigRule rule, AwareConfig.SubItem subItem, Map<String, String> subItemProps) {
        String modeStr = subItemProps.get(MODE_TAG);
        String cmd = subItemProps.get(COMMAND_TAG);
        String type = subItemProps.get(SCENE_TYPE_TAG);
        String priorityStr = subItemProps.get(PRIORITY_TAG);
        if (ID_TYPE.equals(type)) {
            rule.ruleSceneType = ConfigRule.SceneType.SCENE_ID_TYPE;
        } else if (PKG_TYPE.equals(type)) {
            rule.ruleSceneType = ConfigRule.SceneType.PKG_NAME_TYPE;
        } else {
            return;
        }
        String valueStr = subItem.getValue();
        if (!DevSchedUtil.isStrEmpty(valueStr)) {
            try {
                if (!DevSchedUtil.isStrEmpty(modeStr)) {
                    rule.ruleMode = Integer.parseInt(modeStr);
                } else if (!DevSchedUtil.isStrEmpty(cmd)) {
                    rule.ruleCmd = cmd;
                } else {
                    rule.ruleMode = -1;
                }
                if (priorityStr != null) {
                    rule.rulePriority = Integer.parseInt(priorityStr);
                }
                if (parseRuleSceneConfig(valueStr, rule)) {
                }
            } catch (NumberFormatException e) {
                AwareLog.d(TAG, "parseItemScene: number exception!");
            }
        }
    }

    private static boolean parseItemScene(AwareConfig.Item item, ConfigItem itemParse) {
        List<AwareConfig.SubItem> subItemList = item.getSubItemList();
        if (subItemList == null) {
            return true;
        }
        for (AwareConfig.SubItem subItem : subItemList) {
            Map<String, String> subItemProps = subItem.getProperties();
            if (subItemProps == null) {
                return false;
            }
            String ruleName = subItemProps.get(RULE_TAG);
            if (!DevSchedUtil.isStrEmpty(ruleName)) {
                ConfigRule rule = new ConfigRule(ruleName);
                parseRule(rule, subItem, subItemProps);
                itemParse.ruleList.add(rule);
                AwareLog.d(TAG, "parseItemScene: rule config " + rule.toString());
            }
        }
        return true;
    }

    private static void parseItemSceneId(String sceneIdStr, ConfigItem itemParse) {
        try {
            itemParse.itemId = Integer.parseInt(sceneIdStr);
        } catch (NumberFormatException e) {
            AwareLog.d(TAG, "parse item scene id : number exception!");
        }
    }

    private static boolean parseItemList(List<AwareConfig.Item> itemList, FeatureXmlConfig resultConfig) {
        Map<String, String> configPropertries;
        for (AwareConfig.Item item : itemList) {
            if (!(item == null || (configPropertries = item.getProperties()) == null)) {
                if (SUBSWITCH_ENABLE.equals(configPropertries.get(SUBSWITCH_TAG))) {
                    resultConfig.subSwitch = true;
                } else if (!DevSchedUtil.isStrEmpty(configPropertries.get(SCENE_NAME_TAG))) {
                    ConfigItem itemParse = new ConfigItem();
                    if (!parseItemScene(item, itemParse)) {
                        AwareLog.d(TAG, "parse ConfigItem failed!");
                        return false;
                    }
                    String itemSceneId = configPropertries.get(SCENE_ID_TAG);
                    if (!DevSchedUtil.isStrEmpty(itemSceneId)) {
                        parseItemSceneId(itemSceneId, itemParse);
                    }
                    resultConfig.configItemList.add(itemParse);
                } else {
                    continue;
                }
            }
        }
        return true;
    }

    public static FeatureXmlConfig parseFeatureXmlConfig(String feature, int isCust) {
        AwareLog.d(TAG, "parseFeatureXmlConfig: feature " + feature);
        List<AwareConfig.Item> itemList = DevXmlConfig.getItemList(feature, isCust);
        if (itemList.isEmpty()) {
            return null;
        }
        FeatureXmlConfig resultConfig = new FeatureXmlConfig(feature);
        if (!parseItemList(itemList, resultConfig)) {
            AwareLog.d(TAG, "parseItemList: " + feature + " failed!");
            return null;
        }
        AwareLog.d(TAG, "parseFeatureXmlConfig: " + feature + " switch " + resultConfig.subSwitch);
        return resultConfig;
    }

    public static List<ConfigRule> getFirstRuleList(FeatureXmlConfig config) {
        List<ConfigRule> defaultConfigRuleList = Collections.emptyList();
        if (config == null || config.configItemList == null || config.configItemList.isEmpty()) {
            return defaultConfigRuleList;
        }
        return config.configItemList.get(0).ruleList;
    }

    public static ConfigRule getFirstRule(FeatureXmlConfig config) {
        ConfigRule defaultConfigRule = new ConfigRule("");
        List<ConfigRule> ruleList = getFirstRuleList(config);
        if (!ruleList.isEmpty()) {
            return ruleList.get(0);
        }
        return defaultConfigRule;
    }
}
