package com.android.server.mtm.iaware.appmng.rule;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Context;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwareDecrypt;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Xml;
import com.android.server.mtm.iaware.appmng.rule.RuleNode;
import com.android.server.mtm.iaware.appmng.rule.RuleParserUtil;
import com.android.server.rms.iaware.CommonUtils;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList;
import com.huawei.org.kxml2.io.KXmlParserExt;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ConfigReader {
    private static final String ABBR_ALL = "all";
    private static final String ABBR_ALL_BROAD = "allbroad";
    private static final String ABBR_ALL_E_R = "all-R";
    private static final String ABBR_ALL_E_RBB = "all-RBb";
    private static final String ABBR_EXCLUDE_BG_CHECK = "bgcheck";
    private static final String CONFIG_FILE_NAME = "appmng_config.xml";
    private static final String DEFAULT_VALUE = "default";
    private static final String DIVIDER_OF_VALUE = ",";
    private static final int INDEX_CORRECT = 1;
    private static final int INDEX_MAX = 2;
    private static final int INDEX_UPDATED = 0;
    private static final boolean IS_ABROAD = AwareDefaultConfigList.isAbroadArea();
    private static final int MIN_SUPPORTED_VERSION = 118;
    private static final String TAG = "AppMng.ConfigReader";
    private static final int UNINIT_VALUE = -1;
    private static final String XML_ATTR_NAME = "name";
    private static final String XML_ATTR_SCOPE = "scope";
    private static final String XML_ATTR_VALUE = "value";
    private static final String XML_ATTR_VERSION = "version";
    private static final String XML_TAG_ABROAD_LIST = "listabroad";
    private static final String XML_TAG_COMMON_CFG = "commoncfg";
    private static final String XML_TAG_CONFIG = "config";
    private static final String XML_TAG_FEATURE = "feature";
    private static final String XML_TAG_HWSTOP = "hwstop";
    private static final String XML_TAG_IAWARE = "iaware";
    private static final String XML_TAG_INDEX = "index";
    private static final String XML_TAG_ITEM = "item";
    private static final String XML_TAG_LIST = "list";
    private static final String XML_TAG_MISC = "misc";
    private static final String XML_TAG_POLICY = "policy";
    private static final String XML_TAG_PROCESS_LIST = "processlist";
    private static final String XML_TAG_RULE = "rule";
    private static final String XML_TAG_SWAP = "swap";
    private static final String XML_TAG_WEIGHT = "weight";
    private ArrayMap<AppMngConstant.AppMngFeature, ArrayMap<String, String>> mAllCommonCfg = new ArrayMap<>();
    private ArrayMap<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, Config>> mAllConfig = new ArrayMap<>();
    private ArrayMap<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>>> mAllList = new ArrayMap<>();
    private ArrayMap<AppMngConstant.AppMngFeature, ArrayMap<String, ArrayList<String>>> mAllMisc = new ArrayMap<>();
    private ArrayMap<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>>> mAllProcessList = new ArrayMap<>();
    private ArraySet<String> mBgCheckExcludedPkg = new ArraySet<>();
    private ArraySet<String> mBgCheckRemovePkg = new ArraySet<>();
    private ArrayMap<AppMngConstant.AppMngFeature, ArrayList<Boolean>> mConfigCorrectTag = new ArrayMap<>();
    private boolean mIsPolicyMissing = true;
    private int mVersion = -1;

    private void initConfigCorrectTag() {
        int size = AppMngConstant.AppMngFeature.values().length;
        for (int i = 0; i < size; i++) {
            this.mConfigCorrectTag.put(AppMngConstant.AppMngFeature.values()[i], new ArrayList<Boolean>() {
                /* class com.android.server.mtm.iaware.appmng.rule.ConfigReader.AnonymousClass1 */

                {
                    add(false);
                    add(true);
                }
            });
        }
    }

    public void parseFile(AppMngConstant.AppMngFeature targetFeature, Context context) {
        ArrayList<File> files = HwCfgFilePolicy.getCfgFileList("xml/appmng_config.xml", 0);
        if (files == null || files.isEmpty()) {
            AwareLog.w(TAG, "no valid config from getCfgFileList!");
            return;
        }
        int fileSize = files.size();
        AwareLog.i(TAG, "Get config files successfully, fileSize =" + fileSize);
        for (int fileIndex = fileSize + -1; fileIndex >= 0; fileIndex--) {
            File file = files.get(fileIndex);
            if (file != null && file.exists()) {
                initConfigCorrectTag();
                AwareLog.i(TAG, "reading config file of " + fileIndex);
                parseFileInternal(file, targetFeature, context, fileIndex);
            }
        }
        removeListItemIfNeed(this.mAllList);
        removeListItemIfNeed(this.mAllProcessList);
        if (!isConfigComplete(targetFeature) || this.mAllConfig.isEmpty()) {
            AwareLog.w(TAG, "no valid config !");
        }
    }

    private boolean parseXmlInternal(XmlPullParser parser, AppMngConstant.AppMngFeature targetFeature, AppMngConstant.AppMngFeature[] features, int fileIndex) throws XmlPullParserException, IOException, NumberFormatException {
        if (parser == null) {
            return false;
        }
        String name = parser.getName();
        char c = 65535;
        int hashCode = name.hashCode();
        if (hashCode != -1195682923) {
            if (hashCode == -979207434 && name.equals(XML_TAG_FEATURE)) {
                c = 0;
            }
        } else if (name.equals(XML_TAG_IAWARE)) {
            c = 1;
        }
        if (c == 0) {
            Object obj = AppMngConstant.AppMngFeature.fromString(parser.getAttributeValue(null, "name"));
            if (obj instanceof AppMngConstant.AppMngFeature) {
                features[0] = (AppMngConstant.AppMngFeature) obj;
                if (targetFeature == null || targetFeature.equals(features[0])) {
                    parseFeature(parser, features[0]);
                }
            }
        } else if (c == 1) {
            int version = Integer.parseInt(parser.getAttributeValue(null, XML_ATTR_VERSION));
            if (version < 118) {
                setConfigFailed(null);
                AwareLog.e(TAG, "fileIndex = " + fileIndex + " bad version = " + version + " system version = 118");
                return false;
            }
            AwareLog.i(TAG, "fileIndex = " + fileIndex + " good version = " + version + " system version = 118");
            this.mVersion = version;
        }
        return true;
    }

    private void parseFileInternal(File file, AppMngConstant.AppMngFeature targetFeature, Context context, int fileIndex) {
        InputStream rawIs = null;
        InputStream is = null;
        XmlPullParser parser = null;
        AppMngConstant.AppMngFeature[] features = new AppMngConstant.AppMngFeature[1];
        try {
            rawIs = new FileInputStream(file);
            is = IAwareDecrypt.decryptInputStream(context, rawIs);
            parser = Xml.newPullParser();
            if (parser != null) {
                if (is != null) {
                    parser.setInput(is, StandardCharsets.UTF_8.name());
                    while (true) {
                        int eventType = parser.next();
                        if (eventType == 1) {
                            break;
                        } else if (eventType == 2 && !parseXmlInternal(parser, targetFeature, features, fileIndex)) {
                            closeStream(rawIs, is, parser);
                            return;
                        }
                    }
                    closeStream(rawIs, is, parser);
                    return;
                }
            }
            closeStream(rawIs, is, parser);
        } catch (XmlPullParserException e) {
            int col = e.getColumnNumber();
            int line = e.getLineNumber();
            AwareLog.e(TAG, "failed parsing file parser error at line:" + line + ",col:" + col);
            setConfigFailed(features[0]);
        } catch (IOException e2) {
            AwareLog.e(TAG, "failed parsing file IO error ");
            setConfigFailed(features[0]);
        } catch (NumberFormatException e3) {
            AwareLog.e(TAG, "value number format error");
            setConfigFailed(features[0]);
        } catch (Throwable th) {
            closeStream(null, null, null);
            throw th;
        }
    }

    private boolean isCommonFeature(AppMngConstant.AppMngFeature feature) {
        return AppMngConstant.AppMngFeature.COMMON.equals(feature);
    }

    private void closeStream(InputStream rawIs, InputStream is, XmlPullParser parser) {
        CommonUtils.closeStream(rawIs, TAG, null);
        CommonUtils.closeStream(is, TAG, null);
        try {
            KXmlParserExt.closeKXmlParser(parser);
        } catch (IOException e) {
            AwareLog.e(TAG, "parser close error");
        }
    }

    private boolean isParserIllegal(String tagName, String configName) {
        return tagName == null || configName == null;
    }

    private void parseListAbroadArea(XmlPullParser parser, String configName, AppMngConstant.AppMngFeature feature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList) throws XmlPullParserException, IOException, NumberFormatException {
        if (IS_ABROAD) {
            parseList(parser, configName, feature, featureList);
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void parseFeatureInternal(XmlPullParser parser, AppMngConstant.AppMngFeature feature) throws XmlPullParserException, IOException, NumberFormatException {
        char c;
        int outerDepth = parser.getDepth();
        ArrayMap<AppMngConstant.EnumWithDesc, Config> featureConfig = getFeatureConfig(feature);
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList = getFeatureList(feature);
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureProcessList = getFeatureProcessList(feature);
        ArrayMap<String, ArrayList<String>> featureMisc = getFeatureMisc(feature);
        ArrayMap<String, String> featureCommonCfg = new ArrayMap<>();
        while (true) {
            int eventType = parser.next();
            if (eventType != 1 && (eventType != 3 || parser.getDepth() > outerDepth)) {
                if (eventType == 2) {
                    String configName = parser.getAttributeValue(null, "name");
                    if (!isParserIllegal(parser.getName(), configName)) {
                        String name = parser.getName();
                        switch (name.hashCode()) {
                            case -1485796711:
                                if (name.equals(XML_TAG_COMMON_CFG)) {
                                    c = 5;
                                    break;
                                }
                                c = 65535;
                                break;
                            case -1354792126:
                                if (name.equals(XML_TAG_CONFIG)) {
                                    c = 0;
                                    break;
                                }
                                c = 65535;
                                break;
                            case 3322014:
                                if (name.equals(XML_TAG_LIST)) {
                                    c = 1;
                                    break;
                                }
                                c = 65535;
                                break;
                            case 3351788:
                                if (name.equals(XML_TAG_MISC)) {
                                    c = 4;
                                    break;
                                }
                                c = 65535;
                                break;
                            case 203227021:
                                if (name.equals(XML_TAG_PROCESS_LIST)) {
                                    c = 3;
                                    break;
                                }
                                c = 65535;
                                break;
                            case 749196511:
                                if (name.equals(XML_TAG_ABROAD_LIST)) {
                                    c = 2;
                                    break;
                                }
                                c = 65535;
                                break;
                            default:
                                c = 65535;
                                break;
                        }
                        if (c == 0) {
                            parseConfig(parser, configName, feature, featureConfig);
                        } else if (c == 1) {
                            parseList(parser, configName, feature, featureList);
                        } else if (c == 2) {
                            parseListAbroadArea(parser, configName, feature, featureList);
                        } else if (c == 3) {
                            parseList(parser, configName, feature, featureProcessList);
                        } else if (c == 4) {
                            parseMisc(parser, configName, feature, featureMisc);
                        } else if (c == 5) {
                            parseCommonCfg(parser, configName, feature, featureCommonCfg);
                        }
                    }
                }
            }
        }
        if (isNeedUpdate(feature)) {
            this.mAllConfig.put(feature, featureConfig);
            this.mAllList.put(feature, featureList);
            this.mAllProcessList.put(feature, featureProcessList);
            this.mAllMisc.put(feature, featureMisc);
            this.mAllCommonCfg.put(feature, featureCommonCfg);
        }
    }

    private void parseFeature(XmlPullParser parser, AppMngConstant.AppMngFeature feature) throws XmlPullParserException, IOException, NumberFormatException {
        if (feature == null) {
            AwareLog.w(TAG, "feature name is not right or feature is missing");
        } else {
            parseFeatureInternal(parser, feature);
        }
    }

    private Config parseListRules(XmlPullParser parser, AppMngConstant.AppMngFeature feature, AppMngConstant.EnumWithDesc configEnum) throws XmlPullParserException, IOException, NumberFormatException {
        RuleNode head = new RuleNode(null, null);
        ArrayList<RuleNode> heads = new ArrayList<>();
        heads.add(head);
        parseRules(parser, feature, configEnum, heads);
        if (!head.hasChild()) {
            return null;
        }
        if (AppMngConstant.AppMngFeature.APP_START.equals(feature)) {
            return new AppStartRule(null, head);
        }
        if (!AppMngConstant.AppMngFeature.APP_CLEAN.equals(feature)) {
            return null;
        }
        ArrayMap<String, String> prop = new ArrayMap<>();
        prop.put("scope", "all");
        return new AppMngRule(prop, head);
    }

    private void parseAndFillPolicies(XmlPullParser parser, String packageName, AppMngConstant.AppMngFeature feature, ArrayMap<String, ListItem> policies, AppMngConstant.EnumWithDesc configEnum) throws XmlPullParserException, IOException, NumberFormatException {
        ListItem item;
        boolean isSystemBro = isSystemBroadcast(configEnum);
        ListItem item2 = policies.get(packageName);
        if (item2 == null || isSystemBro) {
            if (item2 == null) {
                item = new ListItem();
            } else {
                item = item2;
            }
            String rawWeight = parser.getAttributeValue(null, XML_TAG_WEIGHT);
            if (rawWeight != null) {
                item.setWeight(Integer.parseInt(rawWeight));
            }
            item.setSwap(parseRawSwap(parser.getAttributeValue(null, XML_TAG_SWAP)));
            if (isSystemBro) {
                ArrayMap<String, Integer> complicatePolicy = getComplicatePolicy(item);
                ArrayMap<String, Config> complicateRules = new ArrayMap<>();
                parseBroadcast(parser, complicatePolicy, complicateRules, feature, configEnum);
                item.setComplicatePolicy(complicatePolicy);
                item.setSysBroadcastRule(complicateRules);
            } else {
                item.setPolicy(Integer.parseInt(parser.getAttributeValue(null, XML_ATTR_VALUE)));
                item.setListRule(parseListRules(parser, feature, configEnum));
            }
            if (AppMngConstant.AppMngFeature.APP_START.equals(feature)) {
                item.setIndex(configEnum.getDesc() + AppMngConstant.AppStartReason.LIST.getDesc());
            }
            policies.put(packageName, item);
        }
    }

    private boolean parseListEntry(XmlPullParser parser, String packageName, AppMngConstant.AppMngFeature feature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList) throws XmlPullParserException, IOException, NumberFormatException {
        ArrayMap<String, ListItem> policies;
        String name = parser.getAttributeValue(null, "name");
        AppMngConstant.EnumWithDesc configEnum = getConfig(feature, name);
        if (configEnum != null) {
            ArrayMap<String, ListItem> policies2 = featureList.get(configEnum);
            if (policies2 == null) {
                policies = new ArrayMap<>();
            } else {
                policies = policies2;
            }
            parseAndFillPolicies(parser, packageName, feature, policies, configEnum);
            featureList.put(configEnum, policies);
            return true;
        } else if (!AppMngConstant.AppMngFeature.APP_START.equals(feature)) {
            AwareLog.e(TAG, "no such config named : " + name);
            setConfigFailed(feature);
            return false;
        } else if (dealWithAbbreviation(name, packageName, Integer.parseInt(parser.getAttributeValue(null, XML_ATTR_VALUE)), featureList, parseListRules(parser, feature, configEnum))) {
            return true;
        } else {
            setConfigFailed(feature);
            return false;
        }
    }

    private void parseList(XmlPullParser parser, String packageName, AppMngConstant.AppMngFeature feature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList) throws XmlPullParserException, IOException, NumberFormatException {
        int outerDepth = parser.getDepth();
        while (true) {
            int eventType = parser.next();
            if (eventType == 1) {
                return;
            }
            if (eventType == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (eventType == 2 && XML_TAG_ITEM.equals(parser.getName()) && !parseListEntry(parser, packageName, feature, featureList)) {
                return;
            }
        }
    }

    private void parseBroadcast(XmlPullParser parser, ArrayMap<String, Integer> policy, ArrayMap<String, Config> rules, AppMngConstant.AppMngFeature feature, AppMngConstant.EnumWithDesc configEnum) throws XmlPullParserException, IOException, NumberFormatException {
        int outerDepth = parser.getDepth();
        while (true) {
            int eventType = parser.next();
            if (eventType == 1) {
                return;
            }
            if (eventType == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (eventType == 2 && XML_TAG_ITEM.equals(parser.getName())) {
                String name = parser.getAttributeValue(null, "name");
                int value = Integer.parseInt(parser.getAttributeValue(null, XML_ATTR_VALUE));
                Config config = parseListRules(parser, feature, configEnum);
                if (!policy.containsKey(name)) {
                    policy.put(name, Integer.valueOf(value));
                    rules.put(name, config);
                }
            }
        }
    }

    private void parseMisc(XmlPullParser parser, String miscName, AppMngConstant.AppMngFeature feature, ArrayMap<String, ArrayList<String>> featureMisc) throws XmlPullParserException, IOException, NumberFormatException {
        String content;
        int outerDepth = parser.getDepth();
        if (featureMisc.get(miscName) == null) {
            ArrayList<String> miscList = new ArrayList<>();
            while (true) {
                int eventType = parser.next();
                if (eventType == 1 || (eventType == 3 && parser.getDepth() <= outerDepth)) {
                    break;
                } else if (eventType == 2 && XML_TAG_ITEM.equals(parser.getName()) && (content = parser.nextText()) != null) {
                    miscList.add(content);
                }
            }
            featureMisc.put(miscName, miscList);
        }
    }

    private void parseCommonCfg(XmlPullParser parser, String name, AppMngConstant.AppMngFeature feature, ArrayMap<String, String> featureCommonCfg) throws XmlPullParserException, IOException, NumberFormatException {
        String content = parser.getAttributeValue(null, XML_ATTR_VALUE);
        if (content != null) {
            featureCommonCfg.put(name, content);
            return;
        }
        AwareLog.e(TAG, "bad commoncfg");
        setConfigFailed(feature);
    }

    private void parseConfig(XmlPullParser parser, String configName, AppMngConstant.AppMngFeature feature, ArrayMap<AppMngConstant.EnumWithDesc, Config> featureConfig) throws XmlPullParserException, IOException, NumberFormatException {
        Config config;
        AppMngConstant.EnumWithDesc configEnum = getConfig(feature, configName);
        if (configEnum == null) {
            AwareLog.e(TAG, "no such config named : " + configName);
            setConfigFailed(feature);
        } else if (!featureConfig.containsKey(configEnum)) {
            ArrayMap<String, String> properties = new ArrayMap<>();
            properties.put("name", configName);
            String configScope = parser.getAttributeValue(null, "scope");
            if (configScope != null) {
                properties.put("scope", configScope);
            }
            RuleNode head = new RuleNode(null, null);
            ArrayList<RuleNode> heads = new ArrayList<>();
            heads.add(head);
            boolean hasTriStateConfig = parseRules(parser, feature, configEnum, heads);
            if (!head.hasChild()) {
                AwareLog.e(TAG, "empty named : " + configName);
                setConfigFailed(feature);
                return;
            }
            if (AppMngConstant.AppMngFeature.APP_START.equals(feature)) {
                config = new AppStartRule(null, head);
            } else if (AppMngConstant.AppMngFeature.BROADCAST.equals(feature)) {
                config = new BroadcastMngRule(null, head);
            } else {
                config = new AppMngRule(properties, head, hasTriStateConfig);
            }
            featureConfig.put(configEnum, config);
        }
    }

    private void setAppStartXmlValue(RuleNode.XmlValue xmlValue, AppMngConstant.EnumWithDesc config, String index, String hwStop) {
        if (config != null) {
            xmlValue.setIndex(config.getDesc() + index);
        }
        if (hwStop != null) {
            xmlValue.setHwStop(Integer.parseInt(hwStop));
        }
    }

    private RuleNode.XmlValue getXmlValueByState(boolean isString, String[] values, int index) {
        if (isString) {
            return new RuleNode.XmlValue(values[index]);
        }
        return new RuleNode.XmlValue(Integer.parseInt(values[index]));
    }

    private ArrayList<RuleNode> parseRawValue(XmlPullParser parser, AppMngConstant.AppMngFeature feature, AppMngConstant.EnumWithDesc config, ArrayList<RuleNode> heads, ParseRuleState state) throws XmlPullParserException, IOException, NumberFormatException {
        String[] values = fastSplit(state.rawValue, DIVIDER_OF_VALUE);
        boolean isItemAdded = true;
        ArrayList<RuleNode> defaultHeads = new ArrayList<>();
        ArrayList<RuleNode> nextHeads = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null || values[i].isEmpty()) {
                AwareLog.e(TAG, "values format error rawValue = " + state.rawValue);
                setConfigFailed(feature);
                return defaultHeads;
            }
            RuleNode.XmlValue xmlValue = getXmlValueByState(state.appMngTagType.isStringInXml(), values, i);
            RuleNode node = new RuleNode(state.appMngTagType, xmlValue);
            if (AppMngConstant.AppMngFeature.APP_START.equals(feature)) {
                if (!state.isRule) {
                    setAppStartXmlValue(xmlValue, config, parser.getAttributeValue(null, XML_TAG_INDEX), parser.getAttributeValue(null, XML_TAG_HWSTOP));
                }
                Iterator<RuleNode> it = heads.iterator();
                while (it.hasNext()) {
                    isItemAdded = isItemAdded && it.next().addChildItemSorted(state.appMngTagType, node);
                    nextHeads.add(node);
                }
            } else {
                if (!state.isRule) {
                    xmlValue.setWeight(parseRawWeight(parser.getAttributeValue(null, XML_TAG_WEIGHT)));
                    xmlValue.setSwap(parseRawSwap(parser.getAttributeValue(null, XML_TAG_SWAP)));
                }
                Iterator<RuleNode> it2 = heads.iterator();
                while (it2.hasNext()) {
                    isItemAdded = isItemAdded && it2.next().addChildItem(state.appMngTagType, node);
                    nextHeads.add(node);
                }
            }
            if (!isItemAdded) {
                AwareLog.e(TAG, "rules in same level must have same type");
                setConfigFailed(feature);
                return defaultHeads;
            }
        }
        return nextHeads;
    }

    private int parseRawWeight(String rawWeight) {
        if (rawWeight != null) {
            return Integer.parseInt(rawWeight);
        }
        return -1;
    }

    private int parseRawSwap(String rawSwap) {
        if (rawSwap != null) {
            return Integer.parseInt(rawSwap);
        }
        return -1;
    }

    private boolean parseStartTag(XmlPullParser parser, AppMngConstant.AppMngFeature feature, AppMngConstant.EnumWithDesc config, ArrayList<RuleNode> heads, ParseRuleState state) throws XmlPullParserException, IOException, NumberFormatException {
        String name;
        String tagName = parser.getName();
        state.isRule = XML_TAG_RULE.equals(tagName);
        if (state.isRule) {
            name = parser.getAttributeValue(null, "name");
        } else if ("policy".equals(tagName)) {
            name = "policy";
            this.mIsPolicyMissing = false;
        } else {
            AwareLog.e(TAG, "bad tag = " + tagName);
            setConfigFailed(feature);
            return false;
        }
        state.rawValue = parser.getAttributeValue(null, XML_ATTR_VALUE);
        state.appMngTagType = getType(feature, name);
        if (!state.hasTriStateConfig && state.appMngTagType == RuleParserUtil.AppMngTag.TRISTATE) {
            state.hasTriStateConfig = true;
        }
        if (state.appMngTagType == null || state.rawValue == null) {
            AwareLog.e(TAG, "bad type or rawValue");
            setConfigFailed(feature);
            return false;
        }
        ArrayList<RuleNode> nextHeads = parseRawValue(parser, feature, config, heads, state);
        if (nextHeads.isEmpty()) {
            return false;
        }
        if (state.isRule) {
            this.mIsPolicyMissing = true;
            state.hasTriStateConfig = parseRules(parser, feature, config, nextHeads) || state.hasTriStateConfig;
            if (this.mIsPolicyMissing) {
                AwareLog.e(TAG, "policy missing in feature : " + feature + ", config : " + config);
                setConfigFailed(feature);
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: private */
    public class ParseRuleState {
        RuleParserUtil.TagEnum appMngTagType;
        boolean hasTriStateConfig;
        boolean isRule;
        String rawValue;

        private ParseRuleState() {
            this.hasTriStateConfig = false;
        }
    }

    private boolean parseRules(XmlPullParser parser, AppMngConstant.AppMngFeature feature, AppMngConstant.EnumWithDesc config, ArrayList<RuleNode> heads) throws XmlPullParserException, IOException, NumberFormatException {
        ParseRuleState state = new ParseRuleState();
        int outerDepth = parser.getDepth();
        while (true) {
            int eventType = parser.next();
            if (eventType == 1 || (eventType == 3 && parser.getDepth() <= outerDepth)) {
                break;
            } else if (eventType == 2 && !parseStartTag(parser, feature, config, heads, state)) {
                return state.hasTriStateConfig;
            }
        }
        return state.hasTriStateConfig;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.mtm.iaware.appmng.rule.ConfigReader$2  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppMngFeature = new int[AppMngConstant.AppMngFeature.values().length];

        static {
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppMngFeature[AppMngConstant.AppMngFeature.APP_START.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppMngFeature[AppMngConstant.AppMngFeature.APP_CLEAN.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppMngFeature[AppMngConstant.AppMngFeature.APP_FREEZE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppMngFeature[AppMngConstant.AppMngFeature.APP_IOLIMIT.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppMngFeature[AppMngConstant.AppMngFeature.BROADCAST.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppMngFeature[AppMngConstant.AppMngFeature.APP_CPULIMIT.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    private AppMngConstant.EnumWithDesc getConfig(AppMngConstant.AppMngFeature feature, String configName) {
        switch (AnonymousClass2.$SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppMngFeature[feature.ordinal()]) {
            case 1:
                return AppMngConstant.AppStartSource.fromString(configName);
            case 2:
                return AppMngConstant.AppCleanSource.fromString(configName);
            case 3:
                return AppMngConstant.AppFreezeSource.fromString(configName);
            case 4:
                return AppMngConstant.AppIoLimitSource.fromString(configName);
            case 5:
                return AppMngConstant.BroadcastSource.fromString(configName);
            case 6:
                return AppMngConstant.AppCpuLimitSource.fromString(configName);
            default:
                return null;
        }
    }

    private RuleParserUtil.TagEnum getType(AppMngConstant.AppMngFeature feature, String tagName) {
        switch (AnonymousClass2.$SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppMngFeature[feature.ordinal()]) {
            case 1:
                return RuleParserUtil.AppStartTag.fromString(tagName);
            case 2:
            case 3:
            case 4:
            case 6:
                return RuleParserUtil.AppMngTag.fromString(tagName);
            case 5:
                return RuleParserUtil.BroadcastTag.fromString(tagName);
            default:
                return null;
        }
    }

    private void dealWithSystemBroadcast(ListItem item, int value, Config config) {
        ArrayMap<String, Integer> complicatePolicy = new ArrayMap<>();
        complicatePolicy.put("default", Integer.valueOf(value));
        item.setComplicatePolicy(complicatePolicy);
        ArrayMap<String, Config> complicateRules = new ArrayMap<>();
        complicateRules.put("default", config);
        item.setSysBroadcastRule(complicateRules);
    }

    private void setItemByAppStartSource(ListItem item, int value, Config config, AppMngConstant.AppStartSource tag) {
        if (AppMngConstant.AppStartSource.SYSTEM_BROADCAST.equals(tag)) {
            dealWithSystemBroadcast(item, value, config);
            return;
        }
        item.setPolicy(value);
        item.setListRule(config);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private boolean dealWithAbbreviation(String name, String packageName, int value, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList, Config config) {
        char c;
        AppMngConstant.AppStartSource[] values = AppMngConstant.AppStartSource.values();
        for (AppMngConstant.AppStartSource tag : values) {
            ArrayMap<String, ListItem> configList = featureList.get(tag);
            switch (name.hashCode()) {
                case -913346042:
                    if (name.equals(ABBR_ALL_E_RBB)) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -175522845:
                    if (name.equals(ABBR_EXCLUDE_BG_CHECK)) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 96673:
                    if (name.equals("all")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 92904230:
                    if (name.equals(ABBR_ALL_E_R)) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 1800987009:
                    if (name.equals(ABBR_ALL_BROAD)) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c != 0) {
                if (c != 1) {
                    if (c != 2) {
                        if (c == 3) {
                            updateBgCheckPkgIfNeed(packageName, value);
                        } else if (c != 4) {
                            AwareLog.e(TAG, "no such config named : " + name);
                            return false;
                        } else if (!AppMngConstant.AppStartSource.SYSTEM_BROADCAST.equals(tag)) {
                            if (!AppMngConstant.AppStartSource.THIRD_BROADCAST.equals(tag)) {
                                if (AppMngConstant.AppStartSource.SCHEDULE_RESTART.equals(tag)) {
                                }
                            }
                        }
                    } else if (!AppMngConstant.AppStartSource.SYSTEM_BROADCAST.equals(tag) && !AppMngConstant.AppStartSource.THIRD_BROADCAST.equals(tag)) {
                    }
                } else if (AppMngConstant.AppStartSource.SCHEDULE_RESTART.equals(tag)) {
                }
            }
            if (configList == null) {
                configList = new ArrayMap<>();
            }
            if (configList.get(packageName) == null) {
                ListItem item = new ListItem();
                setItemByAppStartSource(item, value, config, tag);
                configList.put(packageName, item);
                featureList.put(tag, configList);
            }
        }
        return true;
    }

    private boolean isConfigComplete(AppMngConstant.AppMngFeature feature) {
        boolean complete = true;
        if (feature == null) {
            for (Map.Entry<AppMngConstant.AppMngFeature, ArrayList<Boolean>> correctTagEntry : this.mConfigCorrectTag.entrySet()) {
                ArrayList<Boolean> correctTag = correctTagEntry.getValue();
                if (correctTag.size() < 2) {
                    return false;
                }
                if (!correctTag.get(0).booleanValue()) {
                    complete = false;
                }
                correctTag.set(1, true);
            }
            return complete;
        }
        ArrayList<Boolean> correctTag2 = this.mConfigCorrectTag.get(feature);
        if (correctTag2.size() < 2) {
            return false;
        }
        boolean complete2 = correctTag2.get(0).booleanValue();
        correctTag2.set(1, true);
        return complete2;
    }

    private void setConfigFailed(AppMngConstant.AppMngFeature feature) {
        if (feature == null) {
            for (Map.Entry<AppMngConstant.AppMngFeature, ArrayList<Boolean>> correctTagEntry : this.mConfigCorrectTag.entrySet()) {
                correctTagEntry.getValue().set(1, false);
            }
            return;
        }
        this.mConfigCorrectTag.get(feature).set(1, false);
    }

    private boolean isNeedUpdate(AppMngConstant.AppMngFeature feature) {
        ArrayList<Boolean> correctTag = this.mConfigCorrectTag.get(feature);
        if (correctTag.size() < 2) {
            return false;
        }
        boolean isUpdated = correctTag.get(0).booleanValue();
        boolean isCorrect = correctTag.get(1).booleanValue();
        if (isUpdated) {
            return false;
        }
        correctTag.set(0, Boolean.valueOf(isCorrect));
        return isCorrect;
    }

    private String[] fastSplit(String rawStr, String divider) {
        if (rawStr == null) {
            return new String[0];
        }
        ArrayList<String> res = new ArrayList<>();
        int prev = 0;
        while (true) {
            int pos = rawStr.indexOf(divider, prev);
            if (pos != -1) {
                res.add(rawStr.substring(prev, pos));
                prev = pos + divider.length();
            } else {
                res.add(rawStr.substring(prev));
                return (String[]) res.toArray(new String[res.size()]);
            }
        }
    }

    public ArrayMap<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, Config>> getConfig() {
        return this.mAllConfig;
    }

    public ArrayMap<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>>> getList() {
        return this.mAllList;
    }

    public ArrayMap<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>>> getProcessList() {
        return this.mAllProcessList;
    }

    public ArrayMap<AppMngConstant.AppMngFeature, ArrayMap<String, ArrayList<String>>> getMisc() {
        return this.mAllMisc;
    }

    public ArrayMap<AppMngConstant.AppMngFeature, ArrayMap<String, String>> getCommonCfg() {
        return this.mAllCommonCfg;
    }

    public ArraySet<String> getBgCheckExcludedPkg() {
        return this.mBgCheckExcludedPkg;
    }

    public int getVersion() {
        return this.mVersion;
    }

    private ArrayMap<AppMngConstant.EnumWithDesc, Config> getFeatureConfig(AppMngConstant.AppMngFeature feature) {
        ArrayMap<AppMngConstant.EnumWithDesc, Config> featureConfig = this.mAllConfig.get(feature);
        if (featureConfig == null) {
            return new ArrayMap<>();
        }
        return featureConfig;
    }

    private ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> getFeatureList(AppMngConstant.AppMngFeature feature) {
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureListOri = this.mAllList.get(feature);
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList = new ArrayMap<>();
        if (featureListOri != null) {
            featureList.putAll((ArrayMap<? extends AppMngConstant.EnumWithDesc, ? extends ArrayMap<String, ListItem>>) featureListOri);
        }
        return featureList;
    }

    private ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> getFeatureProcessList(AppMngConstant.AppMngFeature feature) {
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureProcessListOri = this.mAllProcessList.get(feature);
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureProcessList = new ArrayMap<>();
        if (featureProcessListOri != null) {
            featureProcessList.putAll((ArrayMap<? extends AppMngConstant.EnumWithDesc, ? extends ArrayMap<String, ListItem>>) featureProcessListOri);
        }
        return featureProcessList;
    }

    private ArrayMap<String, ArrayList<String>> getFeatureMisc(AppMngConstant.AppMngFeature feature) {
        ArrayMap<String, ArrayList<String>> featureMisc = this.mAllMisc.get(feature);
        if (featureMisc == null) {
            return new ArrayMap<>();
        }
        return featureMisc;
    }

    private boolean isSystemBroadcast(AppMngConstant.EnumWithDesc configEnum) {
        return AppMngConstant.AppStartSource.SYSTEM_BROADCAST.equals(configEnum);
    }

    private ArrayMap<String, Integer> getComplicatePolicy(ListItem item) {
        ArrayMap<String, Integer> complicatePolicy = item.getComplicatePolicy();
        if (complicatePolicy == null) {
            return new ArrayMap<>();
        }
        return complicatePolicy;
    }

    private void updateBgCheckPkgIfNeed(String packageName, int value) {
        if (value == -1) {
            this.mBgCheckRemovePkg.add(packageName);
        } else if (!this.mBgCheckRemovePkg.contains(packageName)) {
            this.mBgCheckExcludedPkg.add(packageName);
        }
    }

    private void removeListItem(ArrayMap<String, ListItem> list) {
        Iterator<Map.Entry<String, ListItem>> listIter = list.entrySet().iterator();
        while (listIter.hasNext()) {
            ListItem item = listIter.next().getValue();
            if (item.getPolicy() == -1) {
                ArrayMap<String, Integer> complicatePolicy = item.getComplicatePolicy();
                if (complicatePolicy == null) {
                    listIter.remove();
                } else {
                    Iterator<Map.Entry<String, Integer>> policyIter = complicatePolicy.entrySet().iterator();
                    while (policyIter.hasNext()) {
                        removepolicyIterIfNeed(policyIter);
                    }
                    if (complicatePolicy.isEmpty()) {
                        listIter.remove();
                    }
                }
            }
        }
    }

    private void removeListItemIfNeed(ArrayMap<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>>> listMap) {
        for (Map.Entry<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>>> featureEntry : listMap.entrySet()) {
            Iterator<Map.Entry<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>>> configIter = featureEntry.getValue().entrySet().iterator();
            while (configIter.hasNext()) {
                ArrayMap<String, ListItem> list = configIter.next().getValue();
                removeListItem(list);
                if (list.isEmpty()) {
                    configIter.remove();
                }
            }
        }
    }

    private void removepolicyIterIfNeed(Iterator<Map.Entry<String, Integer>> policyIter) {
        Integer value = policyIter.next().getValue();
        if (value == null || value.intValue() == -1) {
            policyIter.remove();
        }
    }
}
