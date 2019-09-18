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
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ConfigReader {
    private static final String ABBR_ALL = "all";
    private static final String ABBR_ALL_BROAD = "allbroad";
    private static final String ABBR_ALL_E_R = "all-R";
    private static final String ABBR_ALL_E_RBB = "all-RBb";
    private static final String ABBR_EXCLUDE_BG_CHECK = "bgcheck";
    private static final String CONFIG_FILEPATH = "/system/etc/appmng_config.xml";
    private static final int CONFIG_TYPE_CLOUD = 2;
    private static final int CONFIG_TYPE_CUST = 1;
    private static final int CONFIG_TYPE_DEFAULT = 0;
    private static final String CONFIG_UPDATEPATH = "/data/system/iaware/appmng_config.xml";
    private static final String CUST_FILEPATH = "/data/cust/xml/appmng_config_cust.xml";
    private static final String CUST_FILEPATH_RELATED = "xml/appmng_config_cust.xml";
    private static final String DEFAULT_VALUE = "default";
    private static final String DIVIDER_OF_VALUE = ",";
    private static final int INDEX_CORRECT = 1;
    private static final int INDEX_UPDATED = 0;
    private static final boolean IS_ABROAD = AwareDefaultConfigList.isAbroadArea();
    private static final int MIN_SUPPORTED_VERSION = 119;
    private static final String TAG = "AppMng.ConfigReader";
    private static final int UNINIT_VALUE = -1;
    private static final int XML_ATTR_DEFAULT_NUM = 2;
    private static final String XML_ATTR_NAME = "name";
    private static final String XML_ATTR_SCOPE = "scope";
    private static final String XML_ATTR_VALUE = "value";
    private static final String XML_ATTR_VERSION = "version";
    private static final String XML_TAG_ABROAD_LIST = "listabroad";
    private static final String XML_TAG_COMMONCFG = "commoncfg";
    private static final String XML_TAG_CONFIG = "config";
    private static final String XML_TAG_FEATURE = "feature";
    private static final String XML_TAG_HWSTOP = "hwstop";
    private static final String XML_TAG_IAWARE = "iaware";
    private static final String XML_TAG_INDEX = "index";
    private static final String XML_TAG_ITEM = "item";
    private static final String XML_TAG_LIST = "list";
    private static final String XML_TAG_MISC = "misc";
    private static final String XML_TAG_POLICY = "policy";
    private static final String XML_TAG_PROCESSLIST = "processlist";
    private static final String XML_TAG_RULE = "rule";
    private static final String XML_TAG_WEIGHT = "weight";
    private ArrayMap<AppMngConstant.AppMngFeature, ArrayMap<String, String>> mAllCommonCfg = new ArrayMap<>();
    private ArrayMap<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, Config>> mAllConfig = new ArrayMap<>();
    private ArrayMap<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>>> mAllList = new ArrayMap<>();
    private ArrayMap<AppMngConstant.AppMngFeature, ArrayMap<String, ArrayList<String>>> mAllMisc = new ArrayMap<>();
    private ArrayMap<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>>> mAllProcessList = new ArrayMap<>();
    private ArraySet<String> mBGCheckExcludedPkg = new ArraySet<>();
    private ArraySet<String> mBGCheckRemovePkg = new ArraySet<>();
    private ArrayMap<AppMngConstant.AppMngFeature, ArrayList<Boolean>> mConfigCorrectTag = new ArrayMap<>();
    private boolean mIsPolicyMissing = true;
    private int mVersion = -1;

    /* renamed from: com.android.server.mtm.iaware.appmng.rule.ConfigReader$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
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

    private void initConfigCorrectTag() {
        for (AppMngConstant.AppMngFeature put : AppMngConstant.AppMngFeature.values()) {
            this.mConfigCorrectTag.put(put, new ArrayList<Boolean>() {
                {
                    add(false);
                    add(true);
                }
            });
        }
    }

    public void parseFile(AppMngConstant.AppMngFeature targetFeature, Context context) {
        initConfigCorrectTag();
        File file = new File(CONFIG_UPDATEPATH);
        if (file.exists()) {
            AwareLog.i(TAG, "reading cloud config !");
            parseFileInternal(file, targetFeature, context, 2);
        }
        if (!isConfigComplete(targetFeature) || this.mAllConfig.isEmpty()) {
            initConfigCorrectTag();
            File file2 = loadCustConfigFile();
            if (file2.exists()) {
                AwareLog.i(TAG, "reading cust config !");
                parseFileInternal(file2, targetFeature, context, 1);
            }
            initConfigCorrectTag();
            File file3 = new File(CONFIG_FILEPATH);
            if (file3.exists()) {
                AwareLog.i(TAG, "reading default config !");
                parseFileInternal(file3, targetFeature, context, 0);
            }
            removeListItemIfNeed(this.mAllList);
            removeListItemIfNeed(this.mAllProcessList);
            if (!isConfigComplete(targetFeature) || this.mAllConfig.isEmpty()) {
                AwareLog.e(TAG, "no valid config !");
                return;
            }
            return;
        }
        AwareLog.i(TAG, "use cloud config !");
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x005a A[Catch:{ XmlPullParserException -> 0x0102, IOException -> 0x00f3, NumberFormatException -> 0x00e6, all -> 0x00e4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x005c A[Catch:{ XmlPullParserException -> 0x0102, IOException -> 0x00f3, NumberFormatException -> 0x00e6, all -> 0x00e4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00c1 A[Catch:{ XmlPullParserException -> 0x0102, IOException -> 0x00f3, NumberFormatException -> 0x00e6, all -> 0x00e4 }] */
    private void parseFileInternal(File file, AppMngConstant.AppMngFeature targetFeature, Context context, int configType) {
        InputStream rawIs = null;
        InputStream is = null;
        XmlPullParser parser = null;
        AppMngConstant.AppMngFeature feature = null;
        try {
            rawIs = new FileInputStream(file);
            is = IAwareDecrypt.decryptInputStream(context, rawIs);
            parser = Xml.newPullParser();
            if (parser != null) {
                if (is != null) {
                    parser.setInput(is, StandardCharsets.UTF_8.name());
                    while (true) {
                        int next = parser.next();
                        int eventType = next;
                        boolean z = true;
                        if (next != 1) {
                            if (eventType == 2) {
                                String name = parser.getName();
                                int hashCode = name.hashCode();
                                if (hashCode == -1195682923) {
                                    if (name.equals(XML_TAG_IAWARE)) {
                                        switch (z) {
                                            case false:
                                                break;
                                            case true:
                                                break;
                                        }
                                    }
                                } else if (hashCode == -979207434) {
                                    if (name.equals(XML_TAG_FEATURE)) {
                                        z = false;
                                        switch (z) {
                                            case false:
                                                feature = (AppMngConstant.AppMngFeature) AppMngConstant.AppMngFeature.fromString(parser.getAttributeValue(null, "name"));
                                                if (targetFeature != null) {
                                                    if (!targetFeature.equals(feature)) {
                                                        break;
                                                    } else {
                                                        parseFeature(parser, feature, configType);
                                                        break;
                                                    }
                                                } else {
                                                    parseFeature(parser, feature, configType);
                                                    break;
                                                }
                                            case true:
                                                int version = Integer.parseInt(parser.getAttributeValue(null, XML_ATTR_VERSION));
                                                if (version >= 119) {
                                                    AwareLog.i(TAG, "configType = " + configType + " good version = " + version + " system version = " + 119);
                                                    this.mVersion = version;
                                                    break;
                                                } else {
                                                    setConfigFailed(null);
                                                    AwareLog.e(TAG, "configType = " + configType + " bad version = " + version + " system version = " + 119);
                                                    closeStream(rawIs, is, parser);
                                                    return;
                                                }
                                        }
                                    }
                                }
                                z = true;
                                switch (z) {
                                    case false:
                                        break;
                                    case true:
                                        break;
                                }
                            }
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
            setConfigFailed(feature);
        } catch (IOException e2) {
            AwareLog.e(TAG, "failed parsing file IO error ");
            setConfigFailed(feature);
        } catch (NumberFormatException e3) {
            AwareLog.e(TAG, "value number format error");
            setConfigFailed(feature);
        } catch (Throwable th) {
            closeStream(rawIs, is, parser);
            throw th;
        }
    }

    private boolean isCommonFeature(AppMngConstant.AppMngFeature feature) {
        return AppMngConstant.AppMngFeature.COMMON.equals(feature);
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

    private boolean isParserIllegal(String tagName, String configName) {
        if (tagName == null || configName == null) {
            return true;
        }
        return false;
    }

    private boolean isIgnoreParseFeature(AppMngConstant.AppMngFeature feature, int configType) {
        if (feature == null) {
            AwareLog.e(TAG, "feature name is not right or feature is missing");
            return true;
        } else if (!isCommonFeature(feature) || configType != 1) {
            return false;
        } else {
            AwareLog.i(TAG, "cust config, ignore common feature");
            return true;
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0088, code lost:
        if (r4.equals(XML_TAG_LIST) != false) goto L_0x00a0;
     */
    private void parseFeature(XmlPullParser parser, AppMngConstant.AppMngFeature feature, int configType) throws XmlPullParserException, IOException, NumberFormatException {
        ArrayMap<String, String> featureCommonCfg;
        int outerDepth;
        XmlPullParser xmlPullParser = parser;
        AppMngConstant.AppMngFeature appMngFeature = feature;
        int eventType = configType;
        if (!isIgnoreParseFeature(appMngFeature, eventType)) {
            int outerDepth2 = parser.getDepth();
            ArrayMap<AppMngConstant.EnumWithDesc, Config> featureConfig = getFeatureConfig(appMngFeature, eventType);
            ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList = getFeatureList(appMngFeature, eventType);
            ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureProcessList = getFeatureProcessList(appMngFeature, eventType);
            ArrayMap<String, ArrayList<String>> featureMisc = getFeatureMisc(appMngFeature, eventType);
            ArrayMap<String, String> featureCommonCfg2 = new ArrayMap<>();
            while (true) {
                featureCommonCfg = featureCommonCfg2;
                int next = parser.next();
                int eventType2 = next;
                char c = 1;
                if (next == 1) {
                    int i = eventType2;
                    int i2 = outerDepth2;
                } else if (3 != eventType2 || parser.getDepth() > outerDepth2) {
                    if (eventType2 == 2) {
                        String tagName = parser.getName();
                        String configName = xmlPullParser.getAttributeValue(null, "name");
                        if (!isParserIllegal(tagName, configName)) {
                            switch (tagName.hashCode()) {
                                case -1485796711:
                                    if (tagName.equals(XML_TAG_COMMONCFG)) {
                                        c = 5;
                                        break;
                                    }
                                case -1354792126:
                                    if (tagName.equals(XML_TAG_CONFIG)) {
                                        c = 0;
                                        break;
                                    }
                                case 3322014:
                                    break;
                                case 3351788:
                                    if (tagName.equals(XML_TAG_MISC)) {
                                        c = 4;
                                        break;
                                    }
                                case 203227021:
                                    if (tagName.equals(XML_TAG_PROCESSLIST)) {
                                        c = 3;
                                        break;
                                    }
                                case 749196511:
                                    if (tagName.equals(XML_TAG_ABROAD_LIST)) {
                                        c = 2;
                                        break;
                                    }
                                default:
                                    c = 65535;
                                    break;
                            }
                            switch (c) {
                                case 0:
                                    int i3 = eventType2;
                                    outerDepth = outerDepth2;
                                    parseConfig(xmlPullParser, configName, appMngFeature, featureConfig);
                                    break;
                                case 1:
                                    int i4 = eventType2;
                                    outerDepth = outerDepth2;
                                    parseList(xmlPullParser, configName, appMngFeature, featureList);
                                    break;
                                case 2:
                                    int i5 = eventType2;
                                    outerDepth = outerDepth2;
                                    String configName2 = configName;
                                    if (!IS_ABROAD) {
                                        break;
                                    } else {
                                        parseList(xmlPullParser, configName2, appMngFeature, featureList);
                                        break;
                                    }
                                case 3:
                                    int i6 = eventType2;
                                    outerDepth = outerDepth2;
                                    parseList(xmlPullParser, configName, appMngFeature, featureProcessList);
                                    break;
                                case 4:
                                    outerDepth = outerDepth2;
                                    String str = configName;
                                    String str2 = tagName;
                                    int i7 = eventType2;
                                    parseMisc(xmlPullParser, configName, appMngFeature, featureMisc, eventType);
                                    break;
                                case 5:
                                    parseCommonCfg(xmlPullParser, configName, appMngFeature, featureCommonCfg);
                                    String str3 = tagName;
                                    int i8 = eventType2;
                                    outerDepth = outerDepth2;
                                    break;
                                default:
                                    String str4 = tagName;
                                    int i9 = eventType2;
                                    outerDepth = outerDepth2;
                                    String str5 = configName;
                                    break;
                            }
                        } else {
                            outerDepth = outerDepth2;
                        }
                    } else {
                        outerDepth = outerDepth2;
                    }
                    featureCommonCfg2 = featureCommonCfg;
                    outerDepth2 = outerDepth;
                } else {
                    int i10 = eventType2;
                    int i11 = outerDepth2;
                }
            }
            if (isNeedUpdate(appMngFeature)) {
                this.mAllConfig.put(appMngFeature, featureConfig);
                this.mAllList.put(appMngFeature, featureList);
                this.mAllProcessList.put(appMngFeature, featureProcessList);
                this.mAllMisc.put(appMngFeature, featureMisc);
                this.mAllCommonCfg.put(appMngFeature, featureCommonCfg);
            }
        }
    }

    private void parseList(XmlPullParser parser, String packageName, AppMngConstant.AppMngFeature feature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList) throws XmlPullParserException, IOException, NumberFormatException {
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int eventType = next;
            if (next != 1 && (3 != eventType || parser.getDepth() > outerDepth)) {
                if (eventType == 2 && XML_TAG_ITEM.equals(parser.getName())) {
                    String name = parser.getAttributeValue(null, "name");
                    AppMngConstant.EnumWithDesc configEnum = getConfig(feature, name);
                    if (configEnum != null) {
                        ArrayMap<String, ListItem> policies = featureList.get(configEnum);
                        if (policies == null) {
                            policies = new ArrayMap<>();
                        }
                        boolean isSystemBro = isSystemBroadcast(configEnum);
                        ListItem item = policies.get(packageName);
                        if (item == null) {
                            item = new ListItem();
                        } else if (!isSystemBro) {
                        }
                        String rawWeight = parser.getAttributeValue(null, XML_TAG_WEIGHT);
                        if (rawWeight != null) {
                            item.setWeight(Integer.parseInt(rawWeight));
                        }
                        if (isSystemBro) {
                            ArrayMap<String, Integer> complicatePolicy = getComplicatePolicy(item);
                            parseBroadcast(parser, complicatePolicy);
                            item.setComplicatePolicy(complicatePolicy);
                        } else {
                            item.setPolicy(Integer.parseInt(parser.getAttributeValue(null, "value")));
                        }
                        if (AppMngConstant.AppMngFeature.APP_START.equals(feature)) {
                            item.setIndex(configEnum.getDesc() + AppMngConstant.AppStartReason.LIST.getDesc());
                        }
                        policies.put(packageName, item);
                        featureList.put(configEnum, policies);
                    } else if (!AppMngConstant.AppMngFeature.APP_START.equals(feature)) {
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

    private void parseBroadcast(XmlPullParser parser, ArrayMap<String, Integer> policy) throws XmlPullParserException, IOException, NumberFormatException {
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int eventType = next;
            if (next == 1) {
                return;
            }
            if (3 == eventType && parser.getDepth() <= outerDepth) {
                return;
            }
            if (eventType == 2 && XML_TAG_ITEM.equals(parser.getName())) {
                String name = parser.getAttributeValue(null, "name");
                int value = Integer.parseInt(parser.getAttributeValue(null, "value"));
                if (!policy.containsKey(name)) {
                    policy.put(name, Integer.valueOf(value));
                }
            }
        }
    }

    private void parseMisc(XmlPullParser parser, String miscName, AppMngConstant.AppMngFeature feature, ArrayMap<String, ArrayList<String>> featureMisc, int configType) throws XmlPullParserException, IOException, NumberFormatException {
        int outerDepth = parser.getDepth();
        if (featureMisc.get(miscName) == null) {
            ArrayList arrayList = new ArrayList();
            while (true) {
                int next = parser.next();
                int eventType = next;
                if (next == 1 || (3 == eventType && parser.getDepth() <= outerDepth)) {
                    featureMisc.put(miscName, arrayList);
                } else if (eventType == 2 && XML_TAG_ITEM.equals(parser.getName())) {
                    String content = parser.nextText();
                    if (content != null) {
                        arrayList.add(content);
                    }
                }
            }
            featureMisc.put(miscName, arrayList);
        }
    }

    private void parseCommonCfg(XmlPullParser parser, String name, AppMngConstant.AppMngFeature feature, ArrayMap<String, String> featureCommonCfg) throws XmlPullParserException, IOException, NumberFormatException {
        String content = parser.getAttributeValue(null, "value");
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
            parseRules(parser, feature, configEnum, heads);
            if (!head.hasChild()) {
                AwareLog.e(TAG, "empty named : " + configName);
                setConfigFailed(feature);
                return;
            }
            if (feature.equals(AppMngConstant.AppMngFeature.APP_START)) {
                config = new AppStartRule(null, head);
            } else if (feature.equals(AppMngConstant.AppMngFeature.BROADCAST)) {
                config = new BroadcastMngRule(null, head);
            } else {
                config = new AppMngRule(properties, head);
            }
            featureConfig.put(configEnum, config);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:83:0x01d8, code lost:
        android.rms.iaware.AwareLog.e(TAG, "bad type or rawValue");
        setConfigFailed(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x01e2, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x0205, code lost:
        return;
     */
    private void parseRules(XmlPullParser parser, AppMngConstant.AppMngFeature feature, AppMngConstant.EnumWithDesc config, ArrayList<RuleNode> heads) throws XmlPullParserException, IOException, NumberFormatException {
        int outerDepth;
        String name;
        String rawValue;
        RuleNode.XmlValue xmlValue;
        String[] values;
        String name2;
        int eventType;
        RuleNode ruleNode;
        XmlPullParser xmlPullParser = parser;
        AppMngConstant.AppMngFeature appMngFeature = feature;
        AppMngConstant.EnumWithDesc enumWithDesc = config;
        int outerDepth2 = parser.getDepth();
        loop0:
        while (true) {
            int next = parser.next();
            int eventType2 = next;
            if (next != 1) {
                if (3 == eventType2 && parser.getDepth() <= outerDepth2) {
                    int i = outerDepth2;
                    int i2 = eventType2;
                    break;
                }
                if (2 == eventType2) {
                    String tagName = parser.getName();
                    boolean isRule = XML_TAG_RULE.equals(tagName);
                    if (isRule) {
                        name = xmlPullParser.getAttributeValue(null, "name");
                    } else if (XML_TAG_POLICY.equals(tagName)) {
                        name = XML_TAG_POLICY;
                        this.mIsPolicyMissing = false;
                    } else {
                        int i3 = eventType2;
                        AwareLog.e(TAG, "bad tag = " + tagName);
                        setConfigFailed(appMngFeature);
                        return;
                    }
                    rawValue = xmlPullParser.getAttributeValue(null, "value");
                    RuleParserUtil.TagEnum type = getType(appMngFeature, name);
                    if (type == null) {
                        int i4 = eventType2;
                        String str = name;
                        break;
                    } else if (rawValue == null) {
                        int i5 = outerDepth2;
                        int i6 = eventType2;
                        String str2 = name;
                        break;
                    } else {
                        String[] values2 = fastSplit(rawValue, ",");
                        ArrayList<RuleNode> nextHeads = new ArrayList<>();
                        boolean isItemAdded = true;
                        int i7 = 0;
                        while (i7 < values2.length) {
                            if (values2[i7] == null) {
                                int i8 = eventType2;
                                String str3 = name;
                                String[] strArr = values2;
                                break loop0;
                            } else if (values2[i7].isEmpty()) {
                                int i9 = outerDepth2;
                                int i10 = eventType2;
                                String str4 = name;
                                String[] strArr2 = values2;
                                break loop0;
                            } else {
                                if (type.isStringInXml()) {
                                    xmlValue = new RuleNode.XmlValue(values2[i7]);
                                } else {
                                    xmlValue = new RuleNode.XmlValue(Integer.parseInt(values2[i7]));
                                }
                                RuleNode node = new RuleNode(type, xmlValue);
                                int outerDepth3 = outerDepth2;
                                if (AppMngConstant.AppMngFeature.APP_START.equals(appMngFeature) != 0) {
                                    if (!isRule) {
                                        eventType = eventType2;
                                        String index = xmlPullParser.getAttributeValue(null, XML_TAG_INDEX);
                                        name2 = name;
                                        String name3 = xmlPullParser.getAttributeValue(null, XML_TAG_HWSTOP);
                                        StringBuilder sb = new StringBuilder();
                                        values = values2;
                                        sb.append(config.getDesc());
                                        sb.append(index);
                                        xmlValue.setIndex(sb.toString());
                                        if (name3 != null) {
                                            xmlValue.setHwStop(Integer.parseInt(name3));
                                        }
                                    } else {
                                        eventType = eventType2;
                                        name2 = name;
                                        values = values2;
                                    }
                                    Iterator<RuleNode> it = heads.iterator();
                                    while (it.hasNext()) {
                                        isItemAdded = isItemAdded && it.next().addChildItemSorted(type, node);
                                        nextHeads.add(node);
                                    }
                                    ruleNode = null;
                                } else {
                                    eventType = eventType2;
                                    name2 = name;
                                    values = values2;
                                    if (!isRule) {
                                        ruleNode = null;
                                        String rawWeight = xmlPullParser.getAttributeValue(null, XML_TAG_WEIGHT);
                                        int weight = -1;
                                        if (rawWeight != null) {
                                            weight = Integer.parseInt(rawWeight);
                                        }
                                        xmlValue.setWeight(weight);
                                    } else {
                                        ruleNode = null;
                                    }
                                    Iterator<RuleNode> it2 = heads.iterator();
                                    while (it2.hasNext()) {
                                        isItemAdded = isItemAdded && it2.next().addChildItem(type, node);
                                        nextHeads.add(node);
                                    }
                                }
                                if (!isItemAdded) {
                                    AwareLog.e(TAG, "rules in same level must have same type");
                                    setConfigFailed(appMngFeature);
                                    return;
                                }
                                i7++;
                                RuleNode ruleNode2 = node;
                                outerDepth2 = outerDepth3;
                                name = name2;
                                values2 = values;
                                RuleNode node2 = ruleNode;
                                eventType2 = eventType;
                            }
                        }
                        outerDepth = outerDepth2;
                        int i11 = eventType2;
                        String str5 = name;
                        String[] strArr3 = values2;
                        if (isRule) {
                            this.mIsPolicyMissing = true;
                            parseRules(xmlPullParser, appMngFeature, enumWithDesc, nextHeads);
                            if (this.mIsPolicyMissing) {
                                AwareLog.e(TAG, "policy missing in feature : " + appMngFeature + ", config : " + enumWithDesc);
                                setConfigFailed(appMngFeature);
                                return;
                            }
                        } else {
                            continue;
                        }
                    }
                } else {
                    outerDepth = outerDepth2;
                }
                outerDepth2 = outerDepth;
            } else {
                int i12 = eventType2;
                break;
            }
        }
        AwareLog.e(TAG, "values format error rawValue = " + rawValue);
        setConfigFailed(appMngFeature);
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

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x002b, code lost:
        if (r11.equals(ABBR_ALL_E_R) != false) goto L_0x004d;
     */
    private boolean dealWithAbbreviation(String name, String packageName, int value, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList) {
        AppMngConstant.AppStartSource[] values = AppMngConstant.AppStartSource.values();
        int length = values.length;
        int i = 0;
        while (true) {
            char c = 1;
            if (i >= length) {
                return true;
            }
            AppMngConstant.AppStartSource tag = values[i];
            ArrayMap<String, ListItem> configList = featureList.get(tag);
            switch (name.hashCode()) {
                case -913346042:
                    if (name.equals(ABBR_ALL_E_RBB)) {
                        c = 4;
                        break;
                    }
                case -175522845:
                    if (name.equals(ABBR_EXCLUDE_BG_CHECK)) {
                        c = 3;
                        break;
                    }
                case 96673:
                    if (name.equals("all")) {
                        c = 0;
                        break;
                    }
                case 92904230:
                    break;
                case 1800987009:
                    if (name.equals(ABBR_ALL_BROAD)) {
                        c = 2;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 1:
                    if (AppMngConstant.AppStartSource.SCHEDULE_RESTART.equals(tag)) {
                        break;
                    }
                case 2:
                    if (!AppMngConstant.AppStartSource.SYSTEM_BROADCAST.equals(tag) && !AppMngConstant.AppStartSource.THIRD_BROADCAST.equals(tag)) {
                        break;
                    }
                case 4:
                    if (!AppMngConstant.AppStartSource.SYSTEM_BROADCAST.equals(tag)) {
                        if (!AppMngConstant.AppStartSource.THIRD_BROADCAST.equals(tag)) {
                            if (AppMngConstant.AppStartSource.SCHEDULE_RESTART.equals(tag)) {
                                break;
                            }
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                case 0:
                    if (configList == null) {
                        configList = new ArrayMap<>();
                    }
                    if (configList.get(packageName) != null) {
                        break;
                    } else {
                        ListItem item = new ListItem();
                        if (AppMngConstant.AppStartSource.SYSTEM_BROADCAST.equals(tag)) {
                            ArrayMap<String, Integer> complicatePolicy = new ArrayMap<>();
                            complicatePolicy.put("default", Integer.valueOf(value));
                            item.setComplicatePolicy(complicatePolicy);
                        } else {
                            item.setPolicy(value);
                        }
                        configList.put(packageName, item);
                        featureList.put(tag, configList);
                        break;
                    }
                case 3:
                    updateBGCheckPkgIfNeed(packageName, value);
                    break;
                default:
                    AwareLog.e(TAG, "no such config named : " + name);
                    return false;
            }
            i++;
        }
    }

    private boolean isConfigComplete(AppMngConstant.AppMngFeature feature) {
        boolean complete = true;
        if (feature == null) {
            for (Map.Entry<AppMngConstant.AppMngFeature, ArrayList<Boolean>> correctTagEntry : this.mConfigCorrectTag.entrySet()) {
                ArrayList<Boolean> correctTag = correctTagEntry.getValue();
                if (!correctTag.get(0).booleanValue()) {
                    complete = false;
                }
                correctTag.set(1, true);
            }
            return complete;
        }
        ArrayList<Boolean> correctTag2 = this.mConfigCorrectTag.get(feature);
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
        boolean isUpdated = correctTag.get(0).booleanValue();
        boolean isCorrect = correctTag.get(1).booleanValue();
        if (isUpdated) {
            return false;
        }
        correctTag.set(0, Boolean.valueOf(isCorrect));
        return isCorrect;
    }

    private String[] fastSplit(String rawStr, String divider) {
        int prev = 0;
        if (rawStr == null) {
            return new String[0];
        }
        ArrayList<String> res = new ArrayList<>();
        while (true) {
            int indexOf = rawStr.indexOf(divider, prev);
            int pos = indexOf;
            if (indexOf != -1) {
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

    public ArraySet<String> getBGCheckExcludedPkg() {
        return this.mBGCheckExcludedPkg;
    }

    public int getVersion() {
        return this.mVersion;
    }

    private boolean isDefaultConfig(int configType) {
        return configType == 0;
    }

    private ArrayMap<AppMngConstant.EnumWithDesc, Config> getFeatureConfig(AppMngConstant.AppMngFeature feature, int configType) {
        if (!isDefaultConfig(configType)) {
            return new ArrayMap<>();
        }
        ArrayMap<AppMngConstant.EnumWithDesc, Config> featureConfig = this.mAllConfig.get(feature);
        if (featureConfig == null) {
            return new ArrayMap<>();
        }
        return featureConfig;
    }

    private ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> getFeatureList(AppMngConstant.AppMngFeature feature, int configType) {
        if (!isDefaultConfig(configType)) {
            return new ArrayMap<>();
        }
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureListOri = this.mAllList.get(feature);
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList = new ArrayMap<>();
        if (featureListOri == null) {
            return featureList;
        }
        featureList.putAll(featureListOri);
        return featureList;
    }

    private ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> getFeatureProcessList(AppMngConstant.AppMngFeature feature, int configType) {
        if (!isDefaultConfig(configType)) {
            return new ArrayMap<>();
        }
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureProcessListOri = this.mAllProcessList.get(feature);
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureProcessList = new ArrayMap<>();
        if (featureProcessListOri == null) {
            return featureProcessList;
        }
        featureProcessList.putAll(featureProcessListOri);
        return featureProcessList;
    }

    private ArrayMap<String, ArrayList<String>> getFeatureMisc(AppMngConstant.AppMngFeature feature, int configType) {
        if (!isDefaultConfig(configType)) {
            return new ArrayMap<>();
        }
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

    private void updateBGCheckPkgIfNeed(String packageName, int value) {
        if (value == -1) {
            this.mBGCheckRemovePkg.add(packageName);
        } else if (!this.mBGCheckRemovePkg.contains(packageName)) {
            this.mBGCheckExcludedPkg.add(packageName);
        }
    }

    private void removeListItemIfNeed(ArrayMap<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>>> listMap) {
        for (Map.Entry<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>>> featureEntry : listMap.entrySet()) {
            Iterator configIter = featureEntry.getValue().entrySet().iterator();
            while (configIter.hasNext()) {
                ArrayMap<String, ListItem> list = configIter.next().getValue();
                Iterator listIter = list.entrySet().iterator();
                while (listIter.hasNext()) {
                    ListItem item = listIter.next().getValue();
                    if (item.getPolicy() == -1) {
                        ArrayMap<String, Integer> complicatePolicy = item.getComplicatePolicy();
                        if (complicatePolicy == null) {
                            listIter.remove();
                        } else {
                            Iterator policyIter = complicatePolicy.entrySet().iterator();
                            while (policyIter.hasNext()) {
                                Integer value = policyIter.next().getValue();
                                if (value == null || value.intValue() == -1) {
                                    policyIter.remove();
                                }
                            }
                            if (complicatePolicy.isEmpty()) {
                                listIter.remove();
                            }
                        }
                    }
                }
                if (list.isEmpty()) {
                    configIter.remove();
                }
            }
        }
    }
}
