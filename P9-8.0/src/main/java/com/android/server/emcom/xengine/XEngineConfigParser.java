package com.android.server.emcom.xengine;

import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.android.server.emcom.xengine.XEngineConfigInfo.BoostViewInfo;
import com.android.server.emcom.xengine.XEngineConfigInfo.HicomFeaturesInfo;
import com.android.server.emcom.xengine.XEngineConfigInfo.TimePairInfo;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class XEngineConfigParser {
    private static final String ATTRI_CONFIG_VERSION = "config_version";
    private static final String ATTRI_FOREGROUND = "foreground";
    private static final String ATTRI_GRADE = "grade";
    private static final String ATTRI_NAME = "name";
    private static final String ATTRI_PS = "ps";
    private static final String ATTRI_VERSION = "version";
    private static final String CONFIG_FILE_NAME = "/xengine.xml";
    private static final String CONFIG_RELATIVE_PATH = "/emcom/emcomctr";
    private static final String DEFAULT_FILE_PATH = "/system/emui/base/emcom/emcomctr/xengine.xml";
    private static final String DEFAULT_VALUE = "default";
    private static final int INVALID_VALUE = -1;
    private static final String NAME_SPACE = null;
    private static final String TAG = "XEngineConfigParser";
    private static final String TAG_APP = "app";
    private static final String TAG_AUTOGRAB = "autograb";
    private static final String TAG_CONTAINER = "container";
    private static final String TAG_DELAY = "objectivedelay";
    private static final String TAG_EMCOM_ACCELERATE = "emcom_accelerate";
    private static final String TAG_END = "end";
    private static final String TAG_GAME_SPACE = "game_space";
    private static final String TAG_GRADE = "grade";
    private static final String TAG_HICOM = "hicomfeatures";
    private static final String TAG_KEYWORD = "keyword";
    private static final String TAG_MAXGRADE = "maxGrade";
    private static final String TAG_MAX_COUNT = "maxCount";
    private static final String TAG_MAX_DEPTH = "maxDepth";
    private static final String TAG_MINGRADE = "minGrade";
    private static final String TAG_MP = "multipath";
    private static final String TAG_MT = "multiflow";
    private static final String TAG_PRODUCT_CONFIG = "product_config";
    private static final String TAG_PS = "ps";
    private static final String TAG_REGION_CONFIG = "region_config";
    private static final String TAG_ROOT_VIEW = "rootView";
    private static final String TAG_START = "start";
    private static final String TAG_TIME = "time";
    private static final String TAG_VIEW = "view";
    private static final String TAG_WIFI_PAT = "wifimode";
    private static final String TAG_XENGINE = "xengine";
    private static final String TAG_XENGINE_FEATURES_CONFIG = "xengine_features_config";
    private static XEngineConfigParser s_ConfigParser;
    private ArrayList<XEngineConfigInfo> mAppInfos = new ArrayList();
    private HicomFeaturesInfo mGameSpaceInfo;
    private boolean mIsForceForegroundAcc;
    private ArrayList<String> mPackageNames = new ArrayList();
    private int mParseResult;
    private String mProduct = SystemProperties.get("ro.product.model", "");
    private String mRegion = SystemProperties.get("ro.product.locale.region", "");
    private boolean mSpecificProduceConfigFound;
    private boolean mSpecificRegionConfigFound;
    private String mVersion;

    private XEngineConfigParser() {
    }

    public static synchronized XEngineConfigParser getInstance() {
        XEngineConfigParser xEngineConfigParser;
        synchronized (XEngineConfigParser.class) {
            if (s_ConfigParser == null) {
                s_ConfigParser = new XEngineConfigParser();
            }
            xEngineConfigParser = s_ConfigParser;
        }
        return xEngineConfigParser;
    }

    public boolean parse() {
        String path = getConfigFilePath();
        Log.d(TAG, "get config file path success.");
        this.mParseResult = 9;
        return parse(path);
    }

    private String getConfigFilePath() {
        String path = DEFAULT_FILE_PATH;
        String[] cfgFileInfo = HwCfgFilePolicy.getDownloadCfgFile(CONFIG_RELATIVE_PATH, "/emcom/emcomctr/xengine.xml");
        if (cfgFileInfo == null) {
            Log.e(TAG, "Both default and cota config files not exist");
            return path;
        }
        Log.d(TAG, "get config file path and version.");
        if (cfgFileInfo[0] != null) {
            return cfgFileInfo[0];
        }
        return path;
    }

    private boolean parse(String configUpdatePath) {
        FileInputStream in = getFileInputStream(configUpdatePath);
        if (in != null) {
            XmlPullParser xmlPullParser = null;
            boolean parseXEngine;
            try {
                xmlPullParser = Xml.newPullParser();
                xmlPullParser.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", false);
                xmlPullParser.setInput(in, null);
                xmlPullParser.nextTag();
                parseXEngine = parseXEngine(xmlPullParser);
                return parseXEngine;
            } catch (XmlPullParserException e) {
                parseXEngine = TAG;
                Log.e(parseXEngine, "parse xml error.", e);
            } catch (IOException e1) {
                parseXEngine = TAG;
                Log.e(parseXEngine, "IOException occur.", e1);
            } finally {
                closeStream(in, xmlPullParser);
            }
        }
        this.mParseResult = 0;
        return false;
    }

    private void closeStream(InputStream is, XmlPullParser parser) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "close file input stream fail!");
            }
        }
        if (parser != null) {
            try {
                ((KXmlParser) parser).close();
            } catch (IOException e2) {
                Log.e(TAG, "parser close error");
            }
        }
    }

    private FileInputStream getFileInputStream(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Log.i(TAG, "flie not found in " + filePath);
            return null;
        }
    }

    private boolean parseRegionConfig(XmlPullParser parser) throws IOException, XmlPullParserException {
        Log.d(TAG, "parse region_config tag.");
        parser.require(2, NAME_SPACE, TAG_REGION_CONFIG);
        boolean regionMatch = false;
        this.mSpecificProduceConfigFound = false;
        String name = parser.getAttributeValue(NAME_SPACE, "name");
        if (!TextUtils.isEmpty(name)) {
            if (!this.mSpecificRegionConfigFound && "default".equals(name)) {
                Log.d(TAG, " read default region config.");
                regionMatch = true;
            } else if (!TextUtils.isEmpty(this.mRegion) && this.mRegion.equals(name)) {
                Log.d(TAG, "read current region config.");
                this.mSpecificRegionConfigFound = true;
                regionMatch = true;
            }
        }
        if (regionMatch) {
            boolean result = false;
            while (parser.next() != 3) {
                if (parser.getEventType() == 2) {
                    if (parser.getName().equals(TAG_PRODUCT_CONFIG)) {
                        result |= parseProductConfig(parser);
                    } else {
                        skip(parser);
                    }
                }
            }
            return result;
        }
        Log.d(TAG, "region not match.");
        skipTag(parser);
        return false;
    }

    private boolean parseProductConfig(XmlPullParser parser) throws IOException, XmlPullParserException {
        Log.d(TAG, "parse product_config tag.");
        parser.require(2, NAME_SPACE, TAG_PRODUCT_CONFIG);
        String name = parser.getAttributeValue(NAME_SPACE, "name");
        boolean produceMatch = false;
        if (!TextUtils.isEmpty(name)) {
            if (!this.mSpecificProduceConfigFound && "default".equals(name)) {
                Log.d(TAG, " read default product config.");
                produceMatch = true;
            } else if (!TextUtils.isEmpty(this.mProduct) && this.mProduct.startsWith(name)) {
                Log.d(TAG, "read current product config.");
                produceMatch = true;
                this.mSpecificProduceConfigFound = true;
            }
        }
        if (produceMatch) {
            while (parser.next() != 3) {
                if (parser.getEventType() == 2) {
                    if (parser.getName().equals(TAG_XENGINE_FEATURES_CONFIG)) {
                        clearConfigs();
                        parseXEngineFeatureConfig(parser);
                    } else {
                        skip(parser);
                    }
                }
            }
            this.mParseResult = 8;
            return true;
        }
        Log.d(TAG, "product not match.");
        skipTag(parser);
        return false;
    }

    private void skipTag(XmlPullParser parser) throws XmlPullParserException, IOException {
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                skip(parser);
            }
        }
    }

    private void parseXEngineFeatureConfig(XmlPullParser parser) throws IOException, XmlPullParserException {
        Log.d(TAG, "parse xengine_feature_config tag.");
        parser.require(2, NAME_SPACE, TAG_XENGINE_FEATURES_CONFIG);
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                if (parser.getName().equals(TAG_EMCOM_ACCELERATE)) {
                    parseEmcomAccelerate(parser);
                } else {
                    skip(parser);
                }
            }
        }
    }

    private boolean parseXEngine(XmlPullParser parser) throws IOException, XmlPullParserException {
        Log.d(TAG, "parse xengine tag.");
        parser.require(2, NAME_SPACE, TAG_XENGINE);
        boolean result = false;
        String versionCode = parser.getAttributeValue(NAME_SPACE, ATTRI_VERSION);
        if (checkVersion(versionCode)) {
            this.mVersion = versionCode;
            this.mSpecificRegionConfigFound = false;
            while (parser.next() != 3) {
                if (parser.getEventType() == 2) {
                    if (parser.getName().equals(TAG_REGION_CONFIG)) {
                        result |= parseRegionConfig(parser);
                    } else {
                        skip(parser);
                    }
                }
            }
            return result;
        }
        Log.i(TAG, "verison code is lower than current version, return.");
        return false;
    }

    private boolean checkVersion(String versionCode) {
        boolean z = true;
        if (TextUtils.isEmpty(versionCode)) {
            Log.e(TAG, "version code is empty!.");
            return false;
        } else if (TextUtils.isEmpty(this.mVersion)) {
            return true;
        } else {
            if (versionCode.compareTo(this.mVersion) <= 0) {
                z = false;
            }
            return z;
        }
    }

    private void clearConfigs() {
        Log.d(TAG, "clear current configs.");
        this.mAppInfos.clear();
        this.mPackageNames.clear();
        this.mIsForceForegroundAcc = false;
    }

    private void parseEmcomAccelerate(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, NAME_SPACE, TAG_EMCOM_ACCELERATE);
        this.mIsForceForegroundAcc = readBooleanAttribute(parser, "foreground");
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                String name = parser.getName();
                if (name.equals(TAG_APP)) {
                    parseAppConfig(parser);
                } else if (name.equals(TAG_GAME_SPACE)) {
                    parseGameSpace(parser);
                } else {
                    skip(parser);
                }
            }
        }
    }

    private void parseGameSpace(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, NAME_SPACE, TAG_GAME_SPACE);
        this.mGameSpaceInfo = new HicomFeaturesInfo();
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                String name = parser.getName();
                if (name.equals(TAG_MT)) {
                    this.mGameSpaceInfo.multiFlow = readIntVaule(parser);
                } else if (name.equals(TAG_MP)) {
                    this.mGameSpaceInfo.multiPath = readIntVaule(parser);
                } else if (name.equals(TAG_WIFI_PAT)) {
                    this.mGameSpaceInfo.wifiMode = readIntVaule(parser);
                } else if (name.equals(TAG_DELAY)) {
                    this.mGameSpaceInfo.objectiveDelay = readIntVaule(parser);
                } else if (name.equals(TAG_MAXGRADE)) {
                    this.mGameSpaceInfo.maxGrade = readIntVaule(parser);
                } else if (name.equals(TAG_MINGRADE)) {
                    this.mGameSpaceInfo.minGrade = readIntVaule(parser);
                } else {
                    skip(parser);
                }
            }
        }
    }

    private void readTime(XmlPullParser parser, XEngineConfigInfo config) throws XmlPullParserException, IOException {
        parser.require(2, NAME_SPACE, TAG_TIME);
        Object startTime = null;
        Object endTime = null;
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                String name = parser.getName();
                if (name.equals(TAG_START)) {
                    startTime = readVaule(parser);
                } else if (name.equals(TAG_END)) {
                    endTime = readVaule(parser);
                } else {
                    skip(parser);
                }
            }
        }
        if (!TextUtils.isEmpty(startTime) && (TextUtils.isEmpty(endTime) ^ 1) != 0) {
            TimePairInfo pair = new TimePairInfo();
            pair.startTime = startTime;
            pair.endTime = endTime;
            config.timeInfos.add(pair);
        }
    }

    private void readHicom(XmlPullParser parser, XEngineConfigInfo config) throws XmlPullParserException, IOException {
        parser.require(2, NAME_SPACE, TAG_HICOM);
        HicomFeaturesInfo hiInfo = new HicomFeaturesInfo();
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                String name = parser.getName();
                if (name.equals(TAG_MT)) {
                    hiInfo.multiFlow = readIntVaule(parser);
                } else if (name.equals(TAG_MP)) {
                    hiInfo.multiPath = readIntVaule(parser);
                } else if (name.equals(TAG_WIFI_PAT)) {
                    hiInfo.wifiMode = readIntVaule(parser);
                } else if (name.equals(TAG_DELAY)) {
                    hiInfo.objectiveDelay = readIntVaule(parser);
                } else if (name.equals(TAG_MAXGRADE)) {
                    hiInfo.maxGrade = readIntVaule(parser);
                } else if (name.equals(TAG_MINGRADE)) {
                    hiInfo.minGrade = readIntVaule(parser);
                } else {
                    skip(parser);
                }
            }
        }
        config.hicomFeaturesInfo = hiInfo;
    }

    private void parseAppConfig(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(2, NAME_SPACE, TAG_APP);
        XEngineConfigInfo config = new XEngineConfigInfo();
        config.packageName = parser.getAttributeValue(NAME_SPACE, "name");
        config.grade = readIntAttribute(parser, "grade");
        config.isForeground = readBooleanAttribute(parser, "foreground");
        config.mainCardPsStatus = readIntAttribute(parser, "ps");
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                String name = parser.getName();
                if (name.equals(TAG_VIEW)) {
                    readViews(parser, config);
                } else if (name.equals(TAG_AUTOGRAB)) {
                    readAutograb(parser, config);
                } else if (name.equals(TAG_TIME)) {
                    readTime(parser, config);
                } else if (name.equals(TAG_HICOM)) {
                    readHicom(parser, config);
                } else {
                    skip(parser);
                }
            }
        }
        this.mAppInfos.add(config);
        this.mPackageNames.add(config.packageName);
    }

    private boolean readBooleanAttribute(XmlPullParser parser, String attriName) {
        String value = parser.getAttributeValue(NAME_SPACE, attriName);
        if (TextUtils.isEmpty(value)) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    private int readIntAttribute(XmlPullParser parser, String attriName) {
        int value = -1;
        String attri = parser.getAttributeValue(NAME_SPACE, attriName);
        if (TextUtils.isEmpty(attri)) {
            return value;
        }
        try {
            return Integer.parseInt(attri);
        } catch (NumberFormatException e) {
            Log.e(TAG, "NumberFormatException occur.");
            return value;
        }
    }

    private void readAutograb(XmlPullParser parser, XEngineConfigInfo config) throws XmlPullParserException, IOException {
        parser.require(2, NAME_SPACE, TAG_AUTOGRAB);
        String version = parser.getAttributeValue(NAME_SPACE, ATTRI_VERSION);
        String autoGrabConfig = readVaule(parser);
        if (!TextUtils.isEmpty(autoGrabConfig) && (TextUtils.isEmpty(version) ^ 1) != 0) {
            config.autoGrabParams.put(version, autoGrabConfig);
        }
    }

    private void readViews(XmlPullParser parser, XEngineConfigInfo config) throws XmlPullParserException, IOException {
        parser.require(2, NAME_SPACE, TAG_VIEW);
        BoostViewInfo info = new BoostViewInfo();
        info.version = parser.getAttributeValue(NAME_SPACE, ATTRI_VERSION);
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                String name = parser.getName();
                if (name.equals(TAG_ROOT_VIEW)) {
                    info.rootView = readVaule(parser);
                } else if (name.equals(TAG_CONTAINER)) {
                    info.container = readVaule(parser);
                } else if (name.equals(TAG_KEYWORD)) {
                    info.keyword = readVaule(parser);
                } else if (name.equals(TAG_MAX_DEPTH)) {
                    info.maxDepth = readIntVaule(parser);
                } else if (name.equals(TAG_MAX_COUNT)) {
                    info.maxCount = readIntVaule(parser);
                } else if (name.equals("grade")) {
                    info.grade = readIntVaule(parser);
                } else if (name.equals("ps")) {
                    info.mainCardPsStatus = readIntVaule(parser);
                } else {
                    skip(parser);
                }
            }
        }
        config.viewInfos.add(info);
    }

    private int readIntVaule(XmlPullParser parser) throws IOException, XmlPullParserException {
        String val = readVaule(parser);
        if (!TextUtils.isEmpty(val)) {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException e) {
                Log.e(TAG, "parse vaule error.");
            }
        }
        return -1;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != 2) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case 2:
                    depth++;
                    break;
                case 3:
                    depth--;
                    break;
                default:
                    break;
            }
        }
    }

    private String readVaule(XmlPullParser parser) throws IOException, XmlPullParserException {
        if (parser.next() != 4) {
            return null;
        }
        String result = parser.getText();
        parser.nextTag();
        return result;
    }

    public ArrayList<XEngineConfigInfo> getAppInfos() {
        return this.mAppInfos;
    }

    public ArrayList<String> getAppPackageNames() {
        return this.mPackageNames;
    }

    public XEngineConfigInfo getAppInfoByPackageName(String packageName) {
        int size = this.mAppInfos.size();
        for (int i = 0; i < size; i++) {
            XEngineConfigInfo appInfo = (XEngineConfigInfo) this.mAppInfos.get(i);
            if (packageName.equals(appInfo.packageName)) {
                return appInfo;
            }
        }
        return null;
    }

    public boolean isForceForegroundAcc() {
        return this.mIsForceForegroundAcc;
    }

    public int getParseResult() {
        return this.mParseResult;
    }

    public HicomFeaturesInfo getGameSpaceInfo() {
        return this.mGameSpaceInfo;
    }
}
