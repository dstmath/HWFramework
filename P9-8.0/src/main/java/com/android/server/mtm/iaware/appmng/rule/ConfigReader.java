package com.android.server.mtm.iaware.appmng.rule;

import android.app.mtm.iaware.appmng.AppMngConstant.AppCleanSource;
import android.app.mtm.iaware.appmng.AppMngConstant.AppFreezeSource;
import android.app.mtm.iaware.appmng.AppMngConstant.AppIoLimitSource;
import android.app.mtm.iaware.appmng.AppMngConstant.AppMngFeature;
import android.app.mtm.iaware.appmng.AppMngConstant.AppStartReason;
import android.app.mtm.iaware.appmng.AppMngConstant.AppStartSource;
import android.app.mtm.iaware.appmng.AppMngConstant.EnumWithDesc;
import android.content.Context;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwareDecrypt;
import android.util.Xml;
import com.android.server.mtm.iaware.appmng.rule.RuleNode.XmlValue;
import com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppMngTag;
import com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppStartTag;
import com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.TagEnum;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ConfigReader {
    private static final /* synthetic */ int[] -android-app-mtm-iaware-appmng-AppMngConstant$AppMngFeatureSwitchesValues = null;
    private static final String ABBR_ALL = "all";
    private static final String ABBR_ALL_BROAD = "allbroad";
    private static final String ABBR_ALL_E_R = "all-R";
    private static final String ABBR_ALL_E_RBB = "all-RBb";
    private static final String CONFIG_FILEPATH = "/system/etc/appmng_config.xml";
    private static final String CONFIG_UPDATEPATH = "/data/system/iaware/appmng_config.xml";
    private static final String CUST_FILEPATH = "/data/cust/xml/appmng_config_cust.xml";
    private static final String CUST_FILEPATH_RELATED = "xml/appmng_config_cust.xml";
    private static final String DEFAULT_VALUE = "default";
    private static final String DIVIDER_OF_VALUE = ",";
    private static final int INDEX_CORRECT = 1;
    private static final int INDEX_UPDATED = 0;
    private static final boolean IS_ABROAD = AwareDefaultConfigList.isAbroadArea();
    private static final int MIN_SUPPORTED_VERSION = 20;
    private static final String TAG = "AppMng.ConfigReader";
    private static final int UNINIT_VALUE = -1;
    private static final int XML_ATTR_DEFAULT_NUM = 2;
    private static final String XML_ATTR_NAME = "name";
    private static final String XML_ATTR_SCOPE = "scope";
    private static final String XML_ATTR_VALUE = "value";
    private static final String XML_ATTR_VERSION = "version";
    private static final String XML_TAG_ABROAD_LIST = "listabroad";
    private static final String XML_TAG_CONFIG = "config";
    private static final String XML_TAG_FEATURE = "feature";
    private static final String XML_TAG_IAWARE = "iaware";
    private static final String XML_TAG_INDEX = "index";
    private static final String XML_TAG_ITEM = "item";
    private static final String XML_TAG_LIST = "list";
    private static final String XML_TAG_MISC = "misc";
    private static final String XML_TAG_POLICY = "policy";
    private static final String XML_TAG_RULE = "rule";
    private static final String XML_TAG_WEIGHT = "weight";
    private HashMap<AppMngFeature, HashMap<EnumWithDesc, Config>> mAllConfig = new HashMap();
    private HashMap<AppMngFeature, HashMap<EnumWithDesc, HashMap<String, ListItem>>> mAllList = new HashMap();
    private HashMap<AppMngFeature, HashMap<String, ArrayList<String>>> mAllMisc = new HashMap();
    private HashMap<AppMngFeature, ArrayList<Boolean>> mConfigCorrectTag = new HashMap();
    private boolean mIsPolicyMissing = true;
    private int mVersion = -1;

    private static /* synthetic */ int[] -getandroid-app-mtm-iaware-appmng-AppMngConstant$AppMngFeatureSwitchesValues() {
        if (-android-app-mtm-iaware-appmng-AppMngConstant$AppMngFeatureSwitchesValues != null) {
            return -android-app-mtm-iaware-appmng-AppMngConstant$AppMngFeatureSwitchesValues;
        }
        int[] iArr = new int[AppMngFeature.values().length];
        try {
            iArr[AppMngFeature.APP_CLEAN.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[AppMngFeature.APP_FREEZE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[AppMngFeature.APP_IOLIMIT.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[AppMngFeature.APP_START.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -android-app-mtm-iaware-appmng-AppMngConstant$AppMngFeatureSwitchesValues = iArr;
        return iArr;
    }

    public ConfigReader() {
        for (Object put : AppMngFeature.values()) {
            this.mConfigCorrectTag.put(put, new ArrayList<Boolean>() {
                {
                    add(Boolean.valueOf(false));
                    add(Boolean.valueOf(true));
                }
            });
        }
    }

    public void parseFile(AppMngFeature targetFeature, Context context) {
        File file = new File(CONFIG_UPDATEPATH);
        if (file.exists()) {
            AwareLog.i(TAG, "reading cloud config !");
            parseFileInternal(file, targetFeature, context);
        }
        if (!isConfigComplete(targetFeature) || (this.mAllConfig.isEmpty() ^ 1) == 0) {
            file = loadCustConfigFile();
            if (file.exists()) {
                AwareLog.i(TAG, "reading cust config !");
                parseFileInternal(file, targetFeature, context);
            }
            if (!isConfigComplete(targetFeature) || (this.mAllConfig.isEmpty() ^ 1) == 0) {
                file = new File(CONFIG_FILEPATH);
                if (file.exists()) {
                    AwareLog.i(TAG, "reading default config !");
                    parseFileInternal(file, targetFeature, context);
                }
                if (!isConfigComplete(targetFeature) || (this.mAllConfig.isEmpty() ^ 1) == 0) {
                    AwareLog.e(TAG, "no valid config !");
                }
            }
        }
    }

    private void parseFileInternal(File file, AppMngFeature targetFeature, Context context) {
        XmlPullParserException e;
        Throwable th;
        InputStream rawIs = null;
        InputStream is = null;
        XmlPullParser parser = null;
        AppMngFeature feature = null;
        try {
            InputStream rawIs2 = new FileInputStream(file);
            try {
                is = IAwareDecrypt.decryptInputStream(context, rawIs2);
                parser = Xml.newPullParser();
                if (parser == null || is == null) {
                    closeStream(rawIs2, is, parser);
                    return;
                }
                parser.setInput(is, StandardCharsets.UTF_8.name());
                while (true) {
                    int eventType = parser.next();
                    if (eventType == 1) {
                        closeStream(rawIs2, is, parser);
                        break;
                    } else if (eventType == 2) {
                        String name = parser.getName();
                        if (name.equals(XML_TAG_FEATURE)) {
                            feature = (AppMngFeature) AppMngFeature.fromString(parser.getAttributeValue(null, "name"));
                            if (targetFeature == null) {
                                parseFeature(parser, feature);
                            } else if (targetFeature.equals(feature)) {
                                parseFeature(parser, feature);
                            }
                        } else if (name.equals(XML_TAG_IAWARE)) {
                            int version = Integer.parseInt(parser.getAttributeValue(null, XML_ATTR_VERSION));
                            if (version < 20) {
                                setConfigFailed(null);
                                AwareLog.e(TAG, "bad version = " + version);
                                closeStream(rawIs2, is, parser);
                                return;
                            }
                            this.mVersion = version;
                        } else {
                            continue;
                        }
                    }
                }
            } catch (XmlPullParserException e2) {
                e = e2;
                rawIs = rawIs2;
            } catch (IOException e3) {
                rawIs = rawIs2;
            } catch (NumberFormatException e4) {
                rawIs = rawIs2;
            } catch (Throwable th2) {
                th = th2;
                rawIs = rawIs2;
            }
        } catch (XmlPullParserException e5) {
            e = e5;
            try {
                AwareLog.e(TAG, "failed parsing file parser error at line:" + e.getLineNumber() + ",col:" + e.getColumnNumber());
                setConfigFailed(feature);
                closeStream(rawIs, is, parser);
            } catch (Throwable th3) {
                th = th3;
                closeStream(rawIs, is, parser);
                throw th;
            }
        } catch (IOException e6) {
            AwareLog.e(TAG, "failed parsing file IO error ");
            setConfigFailed(feature);
            closeStream(rawIs, is, parser);
        } catch (NumberFormatException e7) {
            AwareLog.e(TAG, "value number format error");
            setConfigFailed(feature);
            closeStream(rawIs, is, parser);
        }
    }

    private void closeStream(InputStream rawIs, InputStream is, XmlPullParser parser) {
        if (rawIs != null) {
            try {
                rawIs.close();
            } catch (IOException e) {
                AwareLog.e(TAG, "close file rawIs stream fail!");
            }
        }
        if (is != null) {
            try {
                is.close();
            } catch (IOException e2) {
                AwareLog.e(TAG, "close file is stream fail!");
            }
        }
        if (parser != null) {
            try {
                ((KXmlParser) parser).close();
            } catch (IOException e3) {
                AwareLog.e(TAG, "parser close error");
            }
        }
    }

    private void parseFeature(XmlPullParser parser, AppMngFeature feature) throws XmlPullParserException, IOException, NumberFormatException {
        if (feature == null) {
            AwareLog.e(TAG, "feature name is not right or feature is missing");
            return;
        }
        int outerDepth = parser.getDepth();
        HashMap<EnumWithDesc, Config> featureConfig = new HashMap();
        HashMap<EnumWithDesc, HashMap<String, ListItem>> featureList = new HashMap();
        HashMap<String, ArrayList<String>> featureMisc = new HashMap();
        while (true) {
            int eventType = parser.next();
            if (eventType != 1 && (3 != eventType || parser.getDepth() > outerDepth)) {
                if (eventType == 2) {
                    String tagName = parser.getName();
                    if (tagName != null) {
                        String configName = parser.getAttributeValue(null, "name");
                        if (configName != null) {
                            if (tagName.equals(XML_TAG_CONFIG)) {
                                parseConfig(parser, configName, feature, featureConfig);
                            } else if (tagName.equals(XML_TAG_LIST)) {
                                parseList(parser, configName, feature, featureList);
                            } else if (tagName.equals(XML_TAG_ABROAD_LIST)) {
                                if (IS_ABROAD) {
                                    parseList(parser, configName, feature, featureList);
                                }
                            } else if (tagName.equals(XML_TAG_MISC)) {
                                parseMisc(parser, configName, feature, featureMisc);
                            }
                        }
                    }
                }
            }
        }
        if (isNeedUpdate(feature)) {
            if (featureConfig.isEmpty()) {
                AwareLog.e(TAG, "feature [" + feature + "] has no config");
                setConfigFailed(feature);
            } else {
                this.mAllConfig.put(feature, featureConfig);
            }
            this.mAllList.put(feature, featureList);
            this.mAllMisc.put(feature, featureMisc);
        }
    }

    private void parseList(XmlPullParser parser, String packageName, AppMngFeature feature, HashMap<EnumWithDesc, HashMap<String, ListItem>> featureList) throws XmlPullParserException, IOException, NumberFormatException {
        int outerDepth = parser.getDepth();
        while (true) {
            int eventType = parser.next();
            if (eventType != 1 && (3 != eventType || parser.getDepth() > outerDepth)) {
                if (eventType == 2 && XML_TAG_ITEM.equals(parser.getName())) {
                    String name = parser.getAttributeValue(null, "name");
                    EnumWithDesc configEnum = getConfig(feature, name);
                    if (configEnum != null) {
                        HashMap<String, ListItem> policies = (HashMap) featureList.get(configEnum);
                        if (policies == null) {
                            policies = new HashMap();
                        }
                        ListItem item = (ListItem) policies.get(packageName);
                        if (item == null) {
                            item = new ListItem();
                        }
                        if (AppStartSource.SYSTEM_BROADCAST.equals(configEnum)) {
                            HashMap<String, Integer> complicatePolicy = new HashMap();
                            parseBroadcast(parser, complicatePolicy);
                            item.setComplicatePolicy(complicatePolicy);
                        } else {
                            item.setPolicy(Integer.parseInt(parser.getAttributeValue(null, "value")));
                        }
                        if (AppMngFeature.APP_START.equals(feature)) {
                            item.setIndex(configEnum.getDesc() + AppStartReason.LIST.getDesc());
                        }
                        policies.put(packageName, item);
                        featureList.put(configEnum, policies);
                    } else if (!AppMngFeature.APP_START.equals(feature)) {
                        AwareLog.e(TAG, "no such config named : " + name);
                        setConfigFailed(feature);
                        return;
                    } else if (!dealWithAbbreviation(name, packageName, Integer.parseInt(parser.getAttributeValue(null, "value")), featureList)) {
                        setConfigFailed(feature);
                        return;
                    }
                }
            }
        }
    }

    private void parseBroadcast(XmlPullParser parser, HashMap<String, Integer> policy) throws XmlPullParserException, IOException, NumberFormatException {
        int outerDepth = parser.getDepth();
        while (true) {
            int eventType = parser.next();
            if (eventType == 1) {
                return;
            }
            if (3 == eventType && parser.getDepth() <= outerDepth) {
                return;
            }
            if (eventType == 2 && XML_TAG_ITEM.equals(parser.getName())) {
                policy.put(parser.getAttributeValue(null, "name"), Integer.valueOf(Integer.parseInt(parser.getAttributeValue(null, "value"))));
            }
        }
    }

    private void parseMisc(XmlPullParser parser, String miscName, AppMngFeature feature, HashMap<String, ArrayList<String>> featureMisc) throws XmlPullParserException, IOException, NumberFormatException {
        int outerDepth = parser.getDepth();
        while (true) {
            int eventType = parser.next();
            if (eventType == 1) {
                return;
            }
            if (3 == eventType && parser.getDepth() <= outerDepth) {
                return;
            }
            if (eventType == 2 && XML_TAG_ITEM.equals(parser.getName())) {
                ArrayList<String> miscList = (ArrayList) featureMisc.get(miscName);
                if (miscList == null) {
                    miscList = new ArrayList();
                }
                String content = parser.nextText();
                if (content != null) {
                    miscList.add(content);
                }
                featureMisc.put(miscName, miscList);
            }
        }
    }

    private void parseConfig(XmlPullParser parser, String configName, AppMngFeature feature, HashMap<EnumWithDesc, Config> featureConfig) throws XmlPullParserException, IOException, NumberFormatException {
        HashMap<String, String> properties = new HashMap();
        properties.put("name", configName);
        String configScope = parser.getAttributeValue(null, "scope");
        if (configScope != null) {
            properties.put("scope", configScope);
        }
        RuleNode head = new RuleNode(null, null);
        EnumWithDesc configEnum = getConfig(feature, configName);
        if (configEnum == null) {
            AwareLog.e(TAG, "no such config named : " + configName);
            setConfigFailed(feature);
            return;
        }
        ArrayList<RuleNode> heads = new ArrayList();
        heads.add(head);
        parseRules(parser, feature, configEnum, heads);
        if (head.hasChild()) {
            Config config;
            if (feature.equals(AppMngFeature.APP_START)) {
                config = new AppStartRule(null, head);
            } else {
                config = new AppMngRule(properties, head);
            }
            featureConfig.put(configEnum, config);
            return;
        }
        AwareLog.e(TAG, "empty named : " + configName);
        setConfigFailed(feature);
    }

    /* JADX WARNING: Missing block: B:30:0x00f8, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void parseRules(XmlPullParser parser, AppMngFeature feature, EnumWithDesc config, ArrayList<RuleNode> heads) throws XmlPullParserException, IOException, NumberFormatException {
        int outerDepth = parser.getDepth();
        loop0:
        while (true) {
            int eventType = parser.next();
            if (eventType != 1 && (3 != eventType || parser.getDepth() > outerDepth)) {
                if (2 == eventType) {
                    String name;
                    String tagName = parser.getName();
                    boolean isRule = XML_TAG_RULE.equals(tagName);
                    if (isRule) {
                        name = parser.getAttributeValue(null, "name");
                    } else if (XML_TAG_POLICY.equals(tagName)) {
                        name = XML_TAG_POLICY;
                        this.mIsPolicyMissing = false;
                    } else {
                        AwareLog.e(TAG, "bad tag = " + tagName);
                        setConfigFailed(feature);
                        return;
                    }
                    String rawValue = parser.getAttributeValue(null, "value");
                    TagEnum type = getType(feature, name);
                    if (type == null || rawValue == null) {
                        AwareLog.e(TAG, "bad type or rawValue");
                        setConfigFailed(feature);
                    } else {
                        String[] values = fastSplit(rawValue, ",");
                        boolean isItemAdded = true;
                        ArrayList<RuleNode> nextHeads = new ArrayList();
                        int i = 0;
                        while (i < values.length) {
                            if (values[i] == null || values[i].isEmpty()) {
                                AwareLog.e(TAG, "values format error rawValue = " + rawValue);
                                setConfigFailed(feature);
                            } else {
                                XmlValue xmlValue;
                                if (type.isStringInXml()) {
                                    xmlValue = new XmlValue(values[i]);
                                } else {
                                    xmlValue = new XmlValue(Integer.parseInt(values[i]));
                                }
                                String index = parser.getAttributeValue(null, XML_TAG_INDEX);
                                RuleNode node = new RuleNode(type, xmlValue);
                                if (AppMngFeature.APP_START.equals(feature)) {
                                    if (!isRule) {
                                        xmlValue.setIndex(config.getDesc() + index);
                                    }
                                    for (RuleNode head : heads) {
                                        isItemAdded = isItemAdded ? head.addChildItemSorted(type, node) : false;
                                        nextHeads.add(node);
                                    }
                                } else {
                                    if (!isRule) {
                                        String rawWeight = parser.getAttributeValue(null, XML_TAG_WEIGHT);
                                        int weight = -1;
                                        if (rawWeight != null) {
                                            weight = Integer.parseInt(rawWeight);
                                        }
                                        xmlValue.setWeight(weight);
                                    }
                                    for (RuleNode head2 : heads) {
                                        isItemAdded = isItemAdded ? head2.addChildItem(type, node) : false;
                                        nextHeads.add(node);
                                    }
                                }
                                if (isItemAdded) {
                                    i++;
                                } else {
                                    AwareLog.e(TAG, "rules in same level must have same type");
                                    setConfigFailed(feature);
                                    return;
                                }
                            }
                        }
                        if (isRule) {
                            this.mIsPolicyMissing = true;
                            parseRules(parser, feature, config, nextHeads);
                            if (this.mIsPolicyMissing) {
                                AwareLog.e(TAG, "policy missing in feature : " + feature + ", config : " + config);
                                setConfigFailed(feature);
                                return;
                            }
                        } else {
                            continue;
                        }
                    }
                }
            } else {
                return;
            }
        }
        AwareLog.e(TAG, "bad type or rawValue");
        setConfigFailed(feature);
    }

    private File loadCustConfigFile() {
        try {
            File cfg = HwCfgFilePolicy.getCfgFile(CUST_FILEPATH_RELATED, 0);
            if (cfg != null) {
                AwareLog.d(TAG, "cust path is " + cfg.getAbsolutePath());
                return cfg;
            }
        } catch (NoClassDefFoundError e) {
            AwareLog.e(TAG, "loadCustConfigFile NoClassDefFoundError : HwCfgFilePolicy ");
        }
        return new File(CUST_FILEPATH);
    }

    private EnumWithDesc getConfig(AppMngFeature feature, String configName) {
        switch (-getandroid-app-mtm-iaware-appmng-AppMngConstant$AppMngFeatureSwitchesValues()[feature.ordinal()]) {
            case 1:
                return AppCleanSource.fromString(configName);
            case 2:
                return AppFreezeSource.fromString(configName);
            case 3:
                return AppIoLimitSource.fromString(configName);
            case 4:
                return AppStartSource.fromString(configName);
            default:
                return null;
        }
    }

    private TagEnum getType(AppMngFeature feature, String tagName) {
        switch (-getandroid-app-mtm-iaware-appmng-AppMngConstant$AppMngFeatureSwitchesValues()[feature.ordinal()]) {
            case 1:
            case 2:
            case 3:
                return AppMngTag.fromString(tagName);
            case 4:
                return AppStartTag.fromString(tagName);
            default:
                return null;
        }
    }

    private boolean dealWithAbbreviation(String name, String packageName, int value, HashMap<EnumWithDesc, HashMap<String, ListItem>> featureList) {
        for (AppStartSource tag : AppStartSource.values()) {
            HashMap<String, ListItem> configList = (HashMap) featureList.get(tag);
            if (!name.equals("all")) {
                if (name.equals(ABBR_ALL_E_R)) {
                    if (AppStartSource.SCHEDULE_RESTART.equals(tag)) {
                    }
                } else if (name.equals(ABBR_ALL_BROAD)) {
                    if (!(AppStartSource.SYSTEM_BROADCAST.equals(tag) || AppStartSource.THIRD_BROADCAST.equals(tag))) {
                    }
                } else if (name.equals(ABBR_ALL_E_RBB)) {
                    if (!AppStartSource.SYSTEM_BROADCAST.equals(tag)) {
                        if (!AppStartSource.THIRD_BROADCAST.equals(tag)) {
                            if (AppStartSource.SCHEDULE_RESTART.equals(tag)) {
                            }
                        }
                    }
                } else {
                    AwareLog.e(TAG, "no such config named : " + name);
                    return false;
                }
            }
            if (configList == null) {
                configList = new HashMap();
            }
            ListItem item = (ListItem) configList.get(packageName);
            if (item == null) {
                item = new ListItem();
            }
            if (AppStartSource.SYSTEM_BROADCAST.equals(tag)) {
                HashMap<String, Integer> complicatePolicy = new HashMap();
                complicatePolicy.put("default", Integer.valueOf(value));
                item.setComplicatePolicy(complicatePolicy);
            } else {
                item.setPolicy(value);
            }
            configList.put(packageName, item);
            featureList.put(tag, configList);
        }
        return true;
    }

    private boolean isConfigComplete(AppMngFeature feature) {
        boolean complete = true;
        ArrayList<Boolean> correctTag;
        if (feature == null) {
            for (Entry<AppMngFeature, ArrayList<Boolean>> correctTagEntry : this.mConfigCorrectTag.entrySet()) {
                correctTag = (ArrayList) correctTagEntry.getValue();
                if (!((Boolean) correctTag.get(0)).booleanValue()) {
                    complete = false;
                }
                correctTag.set(1, Boolean.valueOf(true));
            }
            return complete;
        }
        correctTag = (ArrayList) this.mConfigCorrectTag.get(feature);
        complete = ((Boolean) correctTag.get(0)).booleanValue();
        correctTag.set(1, Boolean.valueOf(true));
        return complete;
    }

    private void setConfigFailed(AppMngFeature feature) {
        if (feature == null) {
            for (Entry<AppMngFeature, ArrayList<Boolean>> correctTagEntry : this.mConfigCorrectTag.entrySet()) {
                ((ArrayList) correctTagEntry.getValue()).set(1, Boolean.valueOf(false));
            }
            return;
        }
        ((ArrayList) this.mConfigCorrectTag.get(feature)).set(1, Boolean.valueOf(false));
    }

    private boolean isNeedUpdate(AppMngFeature feature) {
        ArrayList<Boolean> correctTag = (ArrayList) this.mConfigCorrectTag.get(feature);
        boolean isUpdated = ((Boolean) correctTag.get(0)).booleanValue();
        boolean isCorrect = ((Boolean) correctTag.get(1)).booleanValue();
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
        ArrayList<String> res = new ArrayList();
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

    public HashMap<AppMngFeature, HashMap<EnumWithDesc, Config>> getConfig() {
        return this.mAllConfig;
    }

    public HashMap<AppMngFeature, HashMap<EnumWithDesc, HashMap<String, ListItem>>> getList() {
        return this.mAllList;
    }

    public HashMap<AppMngFeature, HashMap<String, ArrayList<String>>> getMisc() {
        return this.mAllMisc;
    }

    public int getVersion() {
        return this.mVersion;
    }
}
