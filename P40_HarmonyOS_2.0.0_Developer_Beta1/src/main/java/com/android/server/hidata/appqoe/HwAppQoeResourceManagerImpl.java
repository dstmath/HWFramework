package com.android.server.hidata.appqoe;

import android.emcom.EmcomManager;
import android.net.wifi.ScanResult;
import android.text.TextUtils;
import android.util.Xml;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.hidata.arbitration.HwArbitrationDefs;
import com.android.server.hidata.mplink.HwMpLinkConfigInfo;
import com.android.server.hidata.mplink.HwMpLinkContentAware;
import com.android.server.location.HwLocalLocationProvider;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwAppQoeResourceManagerImpl {
    private static final String CFG_APP_REGION = "mAppRegion";
    private static final String CFG_FILE_NAME = "/hidata_config_cust.xml";
    private static final String CFG_GAME_SPECIALINFO_SOURCES = "mGameSpecialInfoSources";
    private static final String CFG_VER_DIR = "emcom/noncell";
    private static final int FEATURE_ID = 3;
    private static final int FORMAT_OUI_VENDOR_LENGTH = 3;
    private static final int HEX_FLAG = 16;
    private static final int HI110X_MASK = 255;
    private static final int PARA_UPGRADE_FILE_NOTEXIST = 0;
    private static final int PARA_UPGRADE_RESPONSE_FILE_ERROR = 6;
    private static final int PARA_UPGRADE_RESPONSE_UPGRADE_ALREADY = 4;
    private static final int PARA_UPGRADE_RESPONSE_UPGRADE_FAILURE = 9;
    private static final int PARA_UPGRADE_RESPONSE_UPGRADE_PENDING = 7;
    private static final int PARA_UPGRADE_RESPONSE_UPGRADE_SUCCESS = 8;
    private static final int PARA_UPGRADE_RESPONSE_VERSION_MISMATCH = 5;
    private static final String TAG = (HwArbitrationDefs.BASE_TAG + HwAppQoeResourceManagerImpl.class.getSimpleName());
    private volatile boolean isXmlLoadFinish = false;
    private HwAppQoeApkConfig mApkConfig = null;
    private List<HwAppQoeApkConfig> mApkConfigList = new ArrayList();
    private List<HwAppQoeBlackListConfig> mBlackConfigList = new ArrayList();
    private HwAppQoeBlackListConfig mBlackListConfig = null;
    private HwAppQoeGameConfig mGameConfig = null;
    private List<HwAppQoeGameConfig> mGameConfigList = new ArrayList();
    private HwMpLinkConfigInfo mHwMpLinkConfigInfo = null;
    private HwMpLinkContentAware mHwMpLinkContentAware;
    private final Object mLock = new Object();
    private List<HwAppQoeOuiBlackListConfig> mOuiBlackConfigList = new ArrayList();
    private HwAppQoeOuiBlackListConfig mOuiBlackListConfig = null;
    private HwPowerParameterConfig mPowerParameterConfig = null;
    private List<HwPowerParameterConfig> mPowerParameterConfigList = new ArrayList();
    private List<HwAppQoeWhiteListConfig> mWhiteConfigList = new ArrayList();
    private HwAppQoeWhiteListConfig mWhiteListConfig = null;

    public HwAppQoeResourceManagerImpl() {
        init();
    }

    private void init() {
        this.mHwMpLinkContentAware = HwMpLinkContentAware.onlyGetInstance();
        new Thread(new MyRunnable()).start();
    }

    public class MyRunnable implements Runnable {
        public MyRunnable() {
        }

        @Override // java.lang.Runnable
        public void run() {
            HwAppQoeUtils.logD(HwAppQoeResourceManagerImpl.TAG, false, "Start read thread", new Object[0]);
            HwAppQoeResourceManagerImpl.this.readAppConfigList();
        }
    }

    public void onConfigFilePathChanged() {
        new Thread(new MyRunnable()).start();
    }

    public void responseForParaUpdate(int result) {
        HwAppQoeUtils.logD(TAG, false, "app qoe response, result: %{public}d", Integer.valueOf(result));
        EmcomManager mEmcomManager = EmcomManager.getInstance();
        if (mEmcomManager != null) {
            mEmcomManager.responseForParaUpgrade(16, 1, result);
        }
    }

    public String getConfigFilePath() {
        HwAppQoeUtils.logD(TAG, false, "getConfigFilePath, enter", new Object[0]);
        try {
            String[] cfgFileInfo = HwCfgFilePolicy.getDownloadCfgFile(CFG_VER_DIR, "emcom/noncell/hidata_config_cust.xml");
            if (cfgFileInfo == null) {
                HwAppQoeUtils.logE(HwAppQoeUtils.TAG, false, "Both default and cota config files not exist", new Object[0]);
                responseForParaUpdate(0);
                return null;
            } else if (cfgFileInfo[0].contains("/cota")) {
                HwAppQoeUtils.logD(TAG, false, "cota config file path is: %{public}s, version:%{public}s", cfgFileInfo[0], cfgFileInfo[1]);
                return cfgFileInfo[0];
            } else {
                HwAppQoeUtils.logD(TAG, false, "system config file path is: %{public}s, version:%{public}s", cfgFileInfo[0], cfgFileInfo[1]);
                return cfgFileInfo[0];
            }
        } catch (NoClassDefFoundError e) {
            HwAppQoeUtils.logE(TAG, false, "getConfigFilePath failed by Exception", new Object[0]);
            responseForParaUpdate(6);
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x0097 A[SYNTHETIC] */
    public void readAppConfigList() {
        InputStream inputStream = null;
        XmlPullParser xmlPullParser = null;
        String configFilePath = getConfigFilePath();
        if (configFilePath == null) {
            HwAppQoeUtils.logD(TAG, false, "readAppConfigList, configPath is null", new Object[0]);
            return;
        }
        File configPath = new File(configFilePath);
        if (!configPath.exists()) {
            responseForParaUpdate(0);
            HwAppQoeUtils.logD(TAG, false, "readAppConfigList, configPath not exit", new Object[0]);
            return;
        }
        try {
            inputStream = new FileInputStream(configPath);
            xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(inputStream, "utf-8");
            clearListInfo();
            parseEventType(xmlPullParser);
            responseForParaUpdate(8);
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        } catch (IOException e2) {
            HwAppQoeUtils.logD(TAG, false, "readAppConfigList exception 2:%{public}s", e2.getMessage());
            responseForParaUpdate(6);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                }
            }
        } catch (XmlPullParserException e4) {
            HwAppQoeUtils.logD(TAG, false, "readAppConfigList exception 3:%{public}s", e4.getMessage());
            responseForParaUpdate(6);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e6) {
                    HwAppQoeUtils.logE(TAG, false, "readAppConfigList failed by Exception", new Object[0]);
                }
            }
            throw th;
        }
        synchronized (this.mLock) {
            this.isXmlLoadFinish = true;
        }
        return;
        HwAppQoeUtils.logE(TAG, false, "readAppConfigList failed by Exception", new Object[0]);
        synchronized (this.mLock) {
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private boolean isParseXmlStartInfo(XmlPullParser xmlPullParser) {
        if (xmlPullParser == null) {
            HwAppQoeUtils.logE(TAG, false, "parseXmlStartInfo, xmlPullParser is null", new Object[0]);
            return false;
        }
        String name = xmlPullParser.getName();
        char c = 65535;
        try {
            switch (name.hashCode()) {
                case -1704837152:
                    if (name.equals("GameInfo")) {
                        c = 1;
                        break;
                    }
                    break;
                case -1088263108:
                    if (name.equals("AppBlackList")) {
                        c = 5;
                        break;
                    }
                    break;
                case -820075192:
                    if (name.equals("vendor")) {
                        c = 4;
                        break;
                    }
                    break;
                case -79922998:
                    if (name.equals("APKInfo")) {
                        c = 0;
                        break;
                    }
                    break;
                case 110259268:
                    if (name.equals("PowerParameter")) {
                        c = '\b';
                        break;
                    }
                    break;
                case 219841382:
                    if (name.equals("AppWhiteList")) {
                        c = 6;
                        break;
                    }
                    break;
                case 1655145445:
                    if (name.equals("mplink_enable")) {
                        c = 3;
                        break;
                    }
                    break;
                case 1699667290:
                    if (name.equals("OuiBlackList")) {
                        c = 7;
                        break;
                    }
                    break;
                case 1731103414:
                    if (name.equals("mplink_version")) {
                        c = 2;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    this.mApkConfig = new HwAppQoeApkConfig();
                    this.mApkConfig.packageName = xmlPullParser.getAttributeValue(0);
                    return true;
                case 1:
                    this.mGameConfig = new HwAppQoeGameConfig();
                    this.mGameConfig.mGameName = xmlPullParser.getAttributeValue(0);
                    return true;
                case 2:
                    if (this.mHwMpLinkContentAware != null) {
                        this.mHwMpLinkContentAware.setMpLinkVersion(xmlPullParser.nextText());
                    }
                    return true;
                case 3:
                    if (this.mHwMpLinkContentAware != null) {
                        this.mHwMpLinkContentAware.setMpLinkEnable(xmlPullParser.nextText());
                    }
                    return true;
                case 4:
                    this.mHwMpLinkConfigInfo = new HwMpLinkConfigInfo();
                    this.mHwMpLinkConfigInfo.setVendorOui(xmlPullParser.getAttributeValue(0));
                    return true;
                case 5:
                    this.mBlackListConfig = new HwAppQoeBlackListConfig();
                    this.mBlackListConfig.setPackageName(xmlPullParser.getAttributeValue(0));
                    return true;
                case 6:
                    this.mWhiteListConfig = new HwAppQoeWhiteListConfig();
                    this.mWhiteListConfig.setPackageName(xmlPullParser.getAttributeValue(0));
                    return true;
                case 7:
                    this.mOuiBlackListConfig = new HwAppQoeOuiBlackListConfig();
                    this.mOuiBlackListConfig.setOuiName(xmlPullParser.getAttributeValue(0));
                    return true;
                case '\b':
                    this.mPowerParameterConfig = new HwPowerParameterConfig();
                    this.mPowerParameterConfig.setPowerParameterName(xmlPullParser.getAttributeValue(0));
                    return true;
            }
        } catch (IOException e) {
            HwAppQoeUtils.logD(TAG, false, "parseXmlStartInfo exception 2:%{public}s", e.getMessage());
            responseForParaUpdate(6);
        } catch (XmlPullParserException e2) {
            HwAppQoeUtils.logD(TAG, false, "parseXmlStartInfo exception 3:%{public}s", e2.getMessage());
            responseForParaUpdate(6);
        }
        return false;
    }

    private void fillXmlStartInfo(XmlPullParser xmlPullParser) {
        if (xmlPullParser == null) {
            HwAppQoeUtils.logE(TAG, false, "fillXmlStartInfo, xmlPullParser is null", new Object[0]);
            return;
        }
        try {
            if (this.mApkConfig != null) {
                fillApkConfig(this.mApkConfig, xmlPullParser.getName(), xmlPullParser.nextText());
            } else if (this.mGameConfig != null) {
                fillGameConfig(this.mGameConfig, xmlPullParser.getName(), xmlPullParser.nextText());
            } else if (this.mHwMpLinkConfigInfo != null) {
                fillMpLinkConfig(this.mHwMpLinkConfigInfo, xmlPullParser.getName(), xmlPullParser.nextText());
            } else if (this.mBlackListConfig != null) {
                fillBlackListConfig(this.mBlackListConfig, xmlPullParser.getName(), xmlPullParser.nextText());
            } else if (this.mWhiteListConfig != null) {
                fillWhiteListConfig(this.mWhiteListConfig, xmlPullParser.getName(), xmlPullParser.nextText());
            } else if (this.mOuiBlackListConfig != null) {
                fillOuiBlackListConfig(this.mOuiBlackListConfig, xmlPullParser.getName(), xmlPullParser.nextText());
            } else if (this.mPowerParameterConfig != null) {
                fillPowerParameterConfig(this.mPowerParameterConfig, xmlPullParser.getName(), xmlPullParser.nextText());
            }
        } catch (IOException e) {
            HwAppQoeUtils.logD(TAG, false, "fillXmlStartInfo exception 2:%{public}s", e.getMessage());
            responseForParaUpdate(6);
        } catch (XmlPullParserException e2) {
            HwAppQoeUtils.logD(TAG, false, "fillXmlStartInfo exception 3:%{public}s", e2.getMessage());
            responseForParaUpdate(6);
        }
    }

    private void clearListInfo() {
        List<HwAppQoeApkConfig> list = this.mApkConfigList;
        if (list != null) {
            list.clear();
        } else {
            this.mApkConfigList = new ArrayList();
        }
        List<HwAppQoeGameConfig> list2 = this.mGameConfigList;
        if (list2 != null) {
            list2.clear();
        } else {
            this.mGameConfigList = new ArrayList();
        }
        List<HwAppQoeBlackListConfig> list3 = this.mBlackConfigList;
        if (list3 != null) {
            list3.clear();
        } else {
            this.mBlackConfigList = new ArrayList();
        }
        List<HwAppQoeWhiteListConfig> list4 = this.mWhiteConfigList;
        if (list4 != null) {
            list4.clear();
        } else {
            this.mWhiteConfigList = new ArrayList();
        }
        List<HwAppQoeOuiBlackListConfig> list5 = this.mOuiBlackConfigList;
        if (list5 != null) {
            list5.clear();
        } else {
            this.mOuiBlackConfigList = new ArrayList();
        }
        List<HwPowerParameterConfig> list6 = this.mPowerParameterConfigList;
        if (list6 != null) {
            list6.clear();
        } else {
            this.mPowerParameterConfigList = new ArrayList();
        }
    }

    private void parseXmlEndInfo(XmlPullParser xmlPullParser) {
        if (xmlPullParser == null) {
            HwAppQoeUtils.logE(TAG, false, "parseXmlEndInfo, xmlPullParser is null", new Object[0]);
        } else if ("APKInfo".equals(xmlPullParser.getName())) {
            this.mApkConfigList.add(this.mApkConfig);
            this.mApkConfig = null;
        } else if ("GameInfo".equals(xmlPullParser.getName())) {
            this.mGameConfigList.add(this.mGameConfig);
            this.mGameConfig = null;
        } else if ("vendor".equals(xmlPullParser.getName())) {
            HwMpLinkContentAware hwMpLinkContentAware = this.mHwMpLinkContentAware;
            if (hwMpLinkContentAware != null) {
                hwMpLinkContentAware.addMpLinkDeviceApp(this.mHwMpLinkConfigInfo);
            }
            this.mHwMpLinkConfigInfo = null;
        } else if ("AppBlackList".equals(xmlPullParser.getName())) {
            this.mBlackConfigList.add(this.mBlackListConfig);
            this.mBlackListConfig = null;
        } else if ("AppWhiteList".equals(xmlPullParser.getName())) {
            this.mWhiteConfigList.add(this.mWhiteListConfig);
            this.mWhiteListConfig = null;
        } else if ("OuiBlackList".equals(xmlPullParser.getName())) {
            this.mOuiBlackConfigList.add(this.mOuiBlackListConfig);
            this.mOuiBlackListConfig = null;
        } else if ("PowerParameter".equals(xmlPullParser.getName())) {
            this.mPowerParameterConfigList.add(this.mPowerParameterConfig);
            this.mPowerParameterConfig = null;
        }
    }

    private void parseEventType(XmlPullParser xmlPullParser) {
        if (xmlPullParser == null) {
            HwAppQoeUtils.logE(TAG, false, "parseEventType, xmlPullParser is null", new Object[0]);
            return;
        }
        try {
            int eventType = xmlPullParser.getEventType();
            while (eventType != 1) {
                if (eventType != 2) {
                    if (eventType == 3) {
                        parseXmlEndInfo(xmlPullParser);
                    }
                } else if (!isParseXmlStartInfo(xmlPullParser)) {
                    fillXmlStartInfo(xmlPullParser);
                }
                eventType = xmlPullParser.next();
            }
        } catch (IOException e) {
            HwAppQoeUtils.logD(TAG, false, "parseEventType exception 2:%{public}s", e.getMessage());
            responseForParaUpdate(6);
        } catch (XmlPullParserException e2) {
            HwAppQoeUtils.logD(TAG, false, "parseEventType exception 3:%{public}s", e2.getMessage());
            responseForParaUpdate(6);
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void fillApkConfig(HwAppQoeApkConfig config, String elementName, String elementValue) {
        if (elementName == null || config == null) {
            HwAppQoeUtils.logD(TAG, false, "fillAPKConfig, input error", new Object[0]);
            return;
        }
        char c = 65535;
        try {
            switch (elementName.hashCode()) {
                case -2137635847:
                    if (elementName.equals("mHistoryQoeBadTH")) {
                        c = '\n';
                        break;
                    }
                    break;
                case -1814569853:
                    if (elementName.equals("mAggressiveStallTH")) {
                        c = '\f';
                        break;
                    }
                    break;
                case -1110914385:
                    if (elementName.equals("mAppId")) {
                        c = 1;
                        break;
                    }
                    break;
                case -923620903:
                    if (elementName.equals("mGeneralStallTH")) {
                        c = 11;
                        break;
                    }
                    break;
                case -836023979:
                    if (elementName.equals("mAppPeriod")) {
                        c = 5;
                        break;
                    }
                    break;
                case -779093368:
                    if (elementName.equals(CFG_APP_REGION)) {
                        c = '\r';
                        break;
                    }
                    break;
                case -154939613:
                    if (elementName.equals("mScenceId")) {
                        c = 3;
                        break;
                    }
                    break;
                case -154462957:
                    if (elementName.equals("mScenseId")) {
                        c = 2;
                        break;
                    }
                    break;
                case -90462973:
                    if (elementName.equals("mAction")) {
                        c = '\t';
                        break;
                    }
                    break;
                case -9888733:
                    if (elementName.equals(AppActConstant.ATTR_CLASS_NAME)) {
                        c = 0;
                        break;
                    }
                    break;
                case 3328234:
                    if (elementName.equals("mQci")) {
                        c = 7;
                        break;
                    }
                    break;
                case 183978075:
                    if (elementName.equals("mAppAlgorithm")) {
                        c = 6;
                        break;
                    }
                    break;
                case 186353520:
                    if (elementName.equals("mPlayActivity")) {
                        c = 14;
                        break;
                    }
                    break;
                case 270217909:
                    if (elementName.equals("mReserved")) {
                        c = 15;
                        break;
                    }
                    break;
                case 292004483:
                    if (elementName.equals("monitorUserLearning")) {
                        c = '\b';
                        break;
                    }
                    break;
                case 1427238722:
                    if (elementName.equals("mScenceType")) {
                        c = 4;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    config.className = elementValue;
                    break;
                case 1:
                    config.mAppId = Integer.parseInt(elementValue);
                    break;
                case 2:
                case 3:
                    config.mScenesId = Integer.parseInt(elementValue);
                    break;
                case 4:
                    config.mScenesType = Integer.parseInt(elementValue);
                    break;
                case 5:
                    config.mAppPeriod = Integer.parseInt(elementValue);
                    break;
                case 6:
                    config.mAppAlgorithm = Integer.parseInt(elementValue);
                    break;
                case 7:
                    config.mQci = Integer.parseInt(elementValue);
                    break;
                case '\b':
                    config.monitorUserLearning = Integer.parseInt(elementValue);
                    break;
                case '\t':
                    config.mAction = Integer.parseInt(elementValue);
                    break;
                case '\n':
                    config.mHistoryQoeBadTh = Float.parseFloat(elementValue);
                    break;
                case 11:
                    config.mGeneralStallTh = Integer.parseInt(elementValue);
                    break;
                case '\f':
                    config.mAggressiveStallTh = Integer.parseInt(elementValue);
                    break;
                case '\r':
                    config.setAppRegion(Integer.parseInt(elementValue));
                    break;
                case 14:
                    config.setPlayActivity(Integer.parseInt(elementValue));
                    break;
                case 15:
                    config.mReserved = elementValue;
                    break;
                default:
                    HwAppQoeUtils.logD(TAG, false, "fillAPKConfig, invalid element name:%{public}s", elementName);
                    break;
            }
        } catch (NumberFormatException e) {
            HwAppQoeUtils.logE(TAG, false, "fillAPKConfig NumberFormatException name: %{public}s", elementName);
        }
        fillAppConfigByWifiPro(config, elementName, elementValue);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void fillAppConfigByWifiPro(HwAppQoeApkConfig config, String elementName, String elementValue) {
        if (elementName == null || config == null) {
            HwAppQoeUtils.logD(TAG, false, "fillAPKConfig, input error", new Object[0]);
            return;
        }
        char c = 65535;
        try {
            switch (elementName.hashCode()) {
                case -1794263281:
                    if (elementName.equals("mTcpResendRate")) {
                        c = 7;
                        break;
                    }
                    break;
                case -1733421470:
                    if (elementName.equals("mBadContinuousCnt")) {
                        c = 6;
                        break;
                    }
                    break;
                case -1642512042:
                    if (elementName.equals("mDetectCycle")) {
                        c = '\b';
                        break;
                    }
                    break;
                case -1560918121:
                    if (elementName.equals("mBadCount")) {
                        c = 4;
                        break;
                    }
                    break;
                case -947980069:
                    if (elementName.equals("mSwitchType")) {
                        c = 1;
                        break;
                    }
                    break;
                case -885057435:
                    if (elementName.equals("mGoodCount")) {
                        c = 5;
                        break;
                    }
                    break;
                case 3329733:
                    if (elementName.equals("mRtt")) {
                        c = 2;
                        break;
                    }
                    break;
                case 270217909:
                    if (elementName.equals("mReserved")) {
                        c = '\t';
                        break;
                    }
                    break;
                case 513355390:
                    if (elementName.equals("mThreshold")) {
                        c = 3;
                        break;
                    }
                    break;
                case 1803858441:
                    if (elementName.equals("mWlanPlus")) {
                        c = 0;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    config.setWlanPlus(Integer.parseInt(elementValue));
                    break;
                case 1:
                    config.setSwitchType(Integer.parseInt(elementValue));
                    break;
                case 2:
                    config.setRtt(Float.parseFloat(elementValue));
                    break;
                case 3:
                    config.setThreshlod(Integer.parseInt(elementValue));
                    break;
                case 4:
                    config.setBadCount(Integer.parseInt(elementValue));
                    break;
                case 5:
                    config.setGoodCount(Integer.parseInt(elementValue));
                    break;
                case 6:
                    config.setBadContinuousCnt(Integer.parseInt(elementValue));
                    break;
                case 7:
                    config.setTcpResendRate(Float.parseFloat(elementValue));
                    break;
                case '\b':
                    config.setDetectCycle(Integer.parseInt(elementValue));
                    break;
                case '\t':
                    config.mReserved = elementValue;
                    break;
                default:
                    HwAppQoeUtils.logD(TAG, false, "fillAPKConfig, invalid element name:%{public}s", elementName);
                    break;
            }
        } catch (NumberFormatException e) {
            HwAppQoeUtils.logE(TAG, false, "fillAPKConfig NumberFormatException name: %{public}s", elementName);
        }
        fillAppConfigByWifiProForOta(config, elementName, elementValue);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void fillAppConfigByWifiProForOta(HwAppQoeApkConfig config, String elementName, String elementValue) {
        if (elementName == null || config == null) {
            HwAppQoeUtils.logD(TAG, false, "fillAPKConfig, input error", new Object[0]);
            return;
        }
        char c = 65535;
        try {
            switch (elementName.hashCode()) {
                case -2106982590:
                    if (elementName.equals("mTxGoodTh")) {
                        c = 7;
                        break;
                    }
                    break;
                case -1223386122:
                    if (elementName.equals("mNoise2gTh")) {
                        c = 0;
                        break;
                    }
                    break;
                case -1223296749:
                    if (elementName.equals("mNoise5gTh")) {
                        c = 1;
                        break;
                    }
                    break;
                case 36855169:
                    if (elementName.equals("mChLoad2gTh")) {
                        c = 4;
                        break;
                    }
                    break;
                case 36944542:
                    if (elementName.equals("mChLoad5gTh")) {
                        c = 5;
                        break;
                    }
                    break;
                case 270217909:
                    if (elementName.equals("mReserved")) {
                        c = '\n';
                        break;
                    }
                    break;
                case 1285856841:
                    if (elementName.equals("mLinkSpeed2gTh")) {
                        c = 2;
                        break;
                    }
                    break;
                case 1285946214:
                    if (elementName.equals("mLinkSpeed5gTh")) {
                        c = 3;
                        break;
                    }
                    break;
                case 1728102408:
                    if (elementName.equals("mTxBadTh")) {
                        c = '\b';
                        break;
                    }
                    break;
                case 1877454002:
                    if (elementName.equals("mTcpRttTh")) {
                        c = '\t';
                        break;
                    }
                    break;
                case 1931657155:
                    if (elementName.equals("mOtaRateTh")) {
                        c = 6;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    config.setNoise2gTh(Integer.parseInt(elementValue));
                    return;
                case 1:
                    config.setNoise5gTh(Integer.parseInt(elementValue));
                    return;
                case 2:
                    config.setLinkSpeed2gTh(Integer.parseInt(elementValue));
                    return;
                case 3:
                    config.setLinkSpeed5gTh(Integer.parseInt(elementValue));
                    return;
                case 4:
                    config.setChLoad2gTh(Integer.parseInt(elementValue));
                    return;
                case 5:
                    config.setChLoad5gTh(Integer.parseInt(elementValue));
                    return;
                case 6:
                    config.setOtaRateTh(Float.parseFloat(elementValue));
                    return;
                case 7:
                    config.setTxGoodTh(Float.parseFloat(elementValue));
                    return;
                case '\b':
                    config.setTxBadTh(Float.parseFloat(elementValue));
                    return;
                case '\t':
                    config.setTcpRttTh(Float.parseFloat(elementValue));
                    return;
                case '\n':
                    config.mReserved = elementValue;
                    return;
                default:
                    HwAppQoeUtils.logD(TAG, false, "fillAPKConfig, invalid element name:%{public}s", elementName);
                    return;
            }
        } catch (NumberFormatException e) {
            HwAppQoeUtils.logE(TAG, false, "fillAPKConfig NumberFormatException name: %{public}s", elementName);
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void fillGameConfig(HwAppQoeGameConfig config, String elementName, String elementValue) {
        if (elementName == null || config == null) {
            HwAppQoeUtils.logD(TAG, false, "fillGameConfig, input error", new Object[0]);
            return;
        }
        char c = 65535;
        try {
            switch (elementName.hashCode()) {
                case -2137635847:
                    if (elementName.equals("mHistoryQoeBadTH")) {
                        c = 5;
                        break;
                    }
                    break;
                case -1838174908:
                    if (elementName.equals("mGameKQI")) {
                        c = 2;
                        break;
                    }
                    break;
                case -1838167053:
                    if (elementName.equals("mGameRtt")) {
                        c = 3;
                        break;
                    }
                    break;
                case -503942891:
                    if (elementName.equals("mGameAction")) {
                        c = 4;
                        break;
                    }
                    break;
                case -331393776:
                    if (elementName.equals(CFG_GAME_SPECIALINFO_SOURCES)) {
                        c = 6;
                        break;
                    }
                    break;
                case -154939613:
                    if (elementName.equals("mScenceId")) {
                        c = 1;
                        break;
                    }
                    break;
                case 79251322:
                    if (elementName.equals("mGameId")) {
                        c = 0;
                        break;
                    }
                    break;
                case 270217909:
                    if (elementName.equals("mReserved")) {
                        c = 7;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    config.mGameId = Integer.parseInt(elementValue);
                    return;
                case 1:
                    config.mScenesId = Integer.parseInt(elementValue);
                    return;
                case 2:
                    config.mGameKqi = Integer.parseInt(elementValue);
                    return;
                case 3:
                    config.mGameRtt = Integer.parseInt(elementValue);
                    return;
                case 4:
                    config.mGameAction = Integer.parseInt(elementValue);
                    return;
                case 5:
                    config.mHistoryQoeBadTh = Float.parseFloat(elementValue);
                    return;
                case 6:
                    config.setGameSpecialInfoSources(Integer.parseInt(elementValue));
                    return;
                case 7:
                    config.mReserved = elementValue;
                    return;
                default:
                    HwAppQoeUtils.logD(TAG, false, "fillGameConfig, invalid element name: %{public}s", elementName);
                    return;
            }
        } catch (NumberFormatException e) {
            HwAppQoeUtils.logE(TAG, false, "fillGameConfig NumberFormatException name: %{public}s", elementName);
        }
    }

    private void fillBlackListConfig(HwAppQoeBlackListConfig config, String elementName, String elementValue) {
        if (elementName == null || config == null) {
            HwAppQoeUtils.logD(TAG, false, "fillBlackListConfig, input error", new Object[0]);
        } else {
            HwAppQoeUtils.logD(TAG, false, "fillBlackListConfig, no feature id now", new Object[0]);
        }
    }

    private void fillWhiteListConfig(HwAppQoeWhiteListConfig config, String elementName, String elementValue) {
        if (elementName == null || config == null) {
            HwAppQoeUtils.logD(TAG, false, "fillWhiteListConfig, input error", new Object[0]);
        } else {
            HwAppQoeUtils.logD(TAG, false, "fillWhiteListConfig, no feature id now", new Object[0]);
        }
    }

    private void fillOuiBlackListConfig(HwAppQoeOuiBlackListConfig config, String elementName, String elementValue) {
        if (elementName == null || config == null) {
            HwAppQoeUtils.logD(TAG, false, "fillOuiBlackListConfig, input error", new Object[0]);
            return;
        }
        char c = 65535;
        try {
            if (elementName.hashCode() == 1638328516 && elementName.equals("mFeatureId")) {
                c = 0;
            }
            if (c != 0) {
                HwAppQoeUtils.logD(TAG, false, "fillOuiBlackListConfig, invalid element name: %{public}s", elementName);
            } else {
                config.setFeatureId(Integer.parseInt(elementValue));
            }
        } catch (NumberFormatException e) {
            HwAppQoeUtils.logE(TAG, false, "fillOuiBlackListConfig NumberFormatException name: %{public}s", elementName);
        }
    }

    private void fillPowerParameterConfig(HwPowerParameterConfig config, String elementName, String elementValue) {
        if (elementName == null || config == null || TextUtils.isEmpty(elementValue)) {
            HwAppQoeUtils.logE(TAG, false, "fillPowerParameterConfig, input error", new Object[0]);
            return;
        }
        char c = 65535;
        try {
            if (elementName.hashCode() == 81434961 && elementName.equals("VALUE")) {
                c = 0;
            }
            if (c != 0) {
                HwAppQoeUtils.logD(TAG, false, "fillPowerParameterConfig, invalid element name: %{public}s", elementName);
            } else {
                config.setPowerParameterValue((double) Float.parseFloat(elementValue));
            }
        } catch (NumberFormatException e) {
            HwAppQoeUtils.logE(TAG, false, "fillPowerParameterConfig NumberFormatException name: %{public}s", elementName);
        }
    }

    private void fillMpLinkConfig(HwMpLinkConfigInfo config, String elementName, String elementValue) {
        if (elementName == null || config == null) {
            HwAppQoeUtils.logD(TAG, false, "fillGameConfig, input error", new Object[0]);
            return;
        }
        char c = 65535;
        switch (elementName.hashCode()) {
            case -861311717:
                if (elementName.equals("condition")) {
                    c = 6;
                    break;
                }
                break;
            case -793183188:
                if (elementName.equals("appname")) {
                    c = 1;
                    break;
                }
                break;
            case -434367618:
                if (elementName.equals("gatewaytype")) {
                    c = 3;
                    break;
                }
                break;
            case -350385368:
                if (elementName.equals("reserved")) {
                    c = 5;
                    break;
                }
                break;
            case 353371935:
                if (elementName.equals("encrypttype")) {
                    c = 4;
                    break;
                }
                break;
            case 398021374:
                if (elementName.equals("multnetwork")) {
                    c = 2;
                    break;
                }
                break;
            case 1127930396:
                if (elementName.equals("custmac")) {
                    c = 0;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                config.setCustMac(elementValue);
                return;
            case 1:
                config.setAppName(elementValue);
                return;
            case 2:
                config.setMultiNetwork(elementValue);
                return;
            case 3:
                config.setGatewayType(elementValue);
                return;
            case 4:
                config.setEncryptType(elementValue);
                return;
            case 5:
                config.setReserved(elementValue);
                return;
            case 6:
                config.setCondition(elementValue);
                return;
            default:
                HwAppQoeUtils.logD(TAG, false, "fillMpLinkConfig, invalid element name: %{public}s", elementName);
                return;
        }
    }

    public HwAppQoeApkConfig checkIsMonitorApkScenes(String packageName, String className) {
        HwAppQoeApkConfig config = null;
        synchronized (this.mLock) {
            if (this.isXmlLoadFinish && !this.mApkConfigList.isEmpty() && !TextUtils.isEmpty(packageName)) {
                if (!TextUtils.isEmpty(className)) {
                    HwAppQoeUtils.logD(TAG, false, "checkIsMonitorApkScenes input :%{public}s,%{public}s", packageName, className);
                    Iterator<HwAppQoeApkConfig> it = this.mApkConfigList.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        HwAppQoeApkConfig apkConfig = it.next();
                        if (className.contains(apkConfig.className)) {
                            if (apkConfig.mScenesType == 3) {
                                config = null;
                            } else {
                                config = apkConfig;
                            }
                        } else if (packageName.equals(apkConfig.packageName) && apkConfig.mScenesId % 1000 == 0) {
                            config = apkConfig;
                        }
                    }
                }
            }
            return null;
        }
        if (config != null) {
            HwAppQoeUtils.logD(TAG, false, "checkIsMonitorApkScenes end:%{public}s", config.toString());
        }
        return config;
    }

    public HwAppQoeApkConfig checkIsMonitorVideoScenes(String packageName, String className) {
        HwAppQoeApkConfig config = null;
        synchronized (this.mLock) {
            if (this.isXmlLoadFinish && !this.mApkConfigList.isEmpty() && !TextUtils.isEmpty(packageName)) {
                if (!TextUtils.isEmpty(className)) {
                    HwAppQoeUtils.logD(TAG, false, "checkIsMonitorVideoScenes input :%{public}s,%{public}s", packageName, className);
                    Iterator<HwAppQoeApkConfig> it = this.mApkConfigList.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        HwAppQoeApkConfig apkConfig = it.next();
                        if (className.contains(apkConfig.className) && packageName.contains(apkConfig.packageName)) {
                            config = apkConfig;
                            break;
                        }
                    }
                }
            }
            return null;
        }
        if (config != null) {
            HwAppQoeUtils.logD(TAG, false, "checkIsMonitorVideoScenes end:%{public}s", config.toString());
        }
        return config;
    }

    public HwAppQoeGameConfig checkIsMonitorGameScenes(String packageName) {
        HwAppQoeGameConfig config = null;
        synchronized (this.mLock) {
            if (this.isXmlLoadFinish && !this.mGameConfigList.isEmpty()) {
                if (!TextUtils.isEmpty(packageName)) {
                    HwAppQoeUtils.logD(TAG, false, "checkIsMonitorGameScenes input :%{public}s", packageName);
                    Iterator<HwAppQoeGameConfig> it = this.mGameConfigList.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        HwAppQoeGameConfig gameConfig = it.next();
                        if (packageName.contains(gameConfig.mGameName)) {
                            config = gameConfig;
                            break;
                        }
                    }
                }
            }
            return null;
        }
        if (config != null) {
            HwAppQoeUtils.logD(TAG, false, "checkIsMonitorGameScenes end:%{public}s", config.toString());
        }
        return config;
    }

    public boolean isInBlackListScenes(String packageName) {
        HwAppQoeBlackListConfig config = null;
        boolean isInBlackList = false;
        synchronized (this.mLock) {
            if (this.isXmlLoadFinish && !this.mBlackConfigList.isEmpty()) {
                if (!TextUtils.isEmpty(packageName)) {
                    HwAppQoeUtils.logD(TAG, false, "checkIsMonitorBlackListScenes input :%{public}s", packageName);
                    Iterator<HwAppQoeBlackListConfig> it = this.mBlackConfigList.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        HwAppQoeBlackListConfig blackListConfig = it.next();
                        if (packageName.contains(blackListConfig.getPackageName())) {
                            isInBlackList = true;
                            config = blackListConfig;
                            break;
                        }
                    }
                }
            }
            return false;
        }
        if (config != null) {
            HwAppQoeUtils.logD(TAG, false, "checkIsMonitorBlackListScenes end:%{public}s", config.toString());
        }
        return isInBlackList;
    }

    public boolean isInWhiteListScenes(String packageName) {
        HwAppQoeWhiteListConfig config = null;
        boolean isInWhiteList = false;
        synchronized (this.mLock) {
            if (this.isXmlLoadFinish && !this.mWhiteConfigList.isEmpty()) {
                if (!TextUtils.isEmpty(packageName)) {
                    HwAppQoeUtils.logD(TAG, false, "checkIsMonitorWhiteListScenes input :%{public}s", packageName);
                    Iterator<HwAppQoeWhiteListConfig> it = this.mWhiteConfigList.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        HwAppQoeWhiteListConfig whiteListConfig = it.next();
                        if (packageName.contains(whiteListConfig.getPackageName())) {
                            isInWhiteList = true;
                            config = whiteListConfig;
                            break;
                        }
                    }
                }
            }
            return false;
        }
        if (config != null) {
            HwAppQoeUtils.logD(TAG, false, "checkIsMonitorWhiteListScenes end:%{public}s", config.toString());
        }
        return isInWhiteList;
    }

    private boolean isInOuiBlackList(ScanResult.InformationElement ie, String[] ouiBlackListString) {
        if (ie.bytes == null || ouiBlackListString == null || ie.bytes.length <= 0) {
            return false;
        }
        for (int index = 0; index < 3; index++) {
            try {
                if ((ie.bytes[index] & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY) != Integer.parseInt(ouiBlackListString[index], 16)) {
                    return false;
                }
            } catch (NumberFormatException e) {
                HwAppQoeUtils.logE(TAG, false, "parse oui blacklist string fail", new Object[0]);
                return true;
            }
        }
        return true;
    }

    private boolean isInFeatureIdBlackList(ScanResult.InformationElement ie, int featureId) {
        if (ie.bytes == null || featureId < 0 || ie.bytes.length < 4 || (ie.bytes[3] & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY) != featureId) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x006e, code lost:
        return false;
     */
    public boolean isInRouterBlackList(ScanResult.InformationElement ie) {
        boolean isInOuiBlackList = false;
        boolean isBlackListFeatureId = false;
        synchronized (this.mLock) {
            if (this.isXmlLoadFinish && !this.mOuiBlackConfigList.isEmpty()) {
                if (ie.bytes != null) {
                    HwAppQoeUtils.logD(TAG, false, "checkIsInOuiBlackList", new Object[0]);
                    Iterator<HwAppQoeOuiBlackListConfig> it = this.mOuiBlackConfigList.iterator();
                    while (true) {
                        if (it.hasNext()) {
                            HwAppQoeOuiBlackListConfig ouiBlackListConfig = it.next();
                            if (ouiBlackListConfig != null) {
                                if (ouiBlackListConfig.getOuiName() != null) {
                                    String[] tempStringArray = ouiBlackListConfig.getOuiName().split(AwarenessInnerConstants.COLON_KEY);
                                    if (tempStringArray.length == 3) {
                                        isInOuiBlackList = isInOuiBlackList(ie, tempStringArray);
                                    }
                                    isBlackListFeatureId = isInFeatureIdBlackList(ie, ouiBlackListConfig.getFeatureId());
                                    if (isInOuiBlackList && isBlackListFeatureId) {
                                        HwAppQoeUtils.logD(TAG, false, "checkOuiBlackList is :%{public}s", ouiBlackListConfig.getOuiName());
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                    return isInOuiBlackList & isBlackListFeatureId;
                }
            }
            return false;
        }
    }

    public HwPowerParameterConfig getPowerParameterConfig(String powerParameter) {
        synchronized (this.mLock) {
            if (this.isXmlLoadFinish && !this.mPowerParameterConfigList.isEmpty()) {
                if (!TextUtils.isEmpty(powerParameter)) {
                    for (HwPowerParameterConfig powerParameterConfig : this.mPowerParameterConfigList) {
                        if (powerParameter.equals(powerParameterConfig.getPowerParameterName())) {
                            return powerParameterConfig;
                        }
                    }
                    HwAppQoeUtils.logD(TAG, false, "getPowerParameterConfig, not found", new Object[0]);
                    return null;
                }
            }
            return null;
        }
    }

    public List<HwPowerParameterConfig> getPowerParameterConfigList() {
        List<HwPowerParameterConfig> list;
        synchronized (this.mLock) {
            list = this.mPowerParameterConfigList;
        }
        return list;
    }

    public HwAppQoeGameConfig getGameScenesConfig(int appId) {
        synchronized (this.mLock) {
            if (this.isXmlLoadFinish) {
                if (!this.mGameConfigList.isEmpty()) {
                    HwAppQoeUtils.logD(TAG, false, "getGameScenesConfig input :%{public}d", Integer.valueOf(appId));
                    for (HwAppQoeGameConfig gameConfig : this.mGameConfigList) {
                        if (appId == gameConfig.mGameId) {
                            HwAppQoeUtils.logD(TAG, false, "getGameScenesConfig:%{public}s", gameConfig.toString());
                            return gameConfig;
                        }
                    }
                    HwAppQoeUtils.logD(TAG, false, "getGameScenesConfig, not found", new Object[0]);
                    return null;
                }
            }
            return null;
        }
    }

    public int getScenesAction(int appType, int appId, int scenesId) {
        int scenesAction = -1;
        synchronized (this.mLock) {
            if (this.isXmlLoadFinish) {
                if (!this.mApkConfigList.isEmpty()) {
                    if (appType == 1000 || appType == 4000) {
                        Iterator<HwAppQoeApkConfig> it = this.mApkConfigList.iterator();
                        while (true) {
                            if (!it.hasNext()) {
                                break;
                            }
                            HwAppQoeApkConfig apkConfig = it.next();
                            if (appId == apkConfig.mAppId && scenesId == apkConfig.mScenesId) {
                                scenesAction = apkConfig.mAction;
                                break;
                            }
                        }
                    }
                    if (appType == 2000) {
                        Iterator<HwAppQoeGameConfig> it2 = this.mGameConfigList.iterator();
                        while (true) {
                            if (!it2.hasNext()) {
                                break;
                            }
                            HwAppQoeGameConfig gameConfig = it2.next();
                            if (appId == gameConfig.mGameId && scenesId == gameConfig.mScenesId) {
                                scenesAction = gameConfig.mGameAction;
                                break;
                            }
                        }
                    }
                    HwAppQoeUtils.logD(TAG, false, "getScenesAction, action:%{public}d", Integer.valueOf(scenesAction));
                    return scenesAction;
                }
            }
            return -1;
        }
    }

    public HwAppQoeApkConfig getApkScenesConfig(int scenesId) {
        synchronized (this.mLock) {
            if (this.isXmlLoadFinish) {
                if (!this.mApkConfigList.isEmpty()) {
                    HwAppQoeUtils.logD(TAG, false, "getApkScenesConfig input :%{public}d", Integer.valueOf(scenesId));
                    for (HwAppQoeApkConfig apkConfig : this.mApkConfigList) {
                        if (scenesId == apkConfig.mScenesId) {
                            HwAppQoeUtils.logD(TAG, false, "getApkScenesConfig:%{public}s", apkConfig.toString());
                            return apkConfig;
                        }
                    }
                    HwAppQoeUtils.logD(TAG, false, "getApkScenesConfig, not found", new Object[0]);
                    return null;
                }
            }
            return null;
        }
    }

    public List<HwAppQoeApkConfig> getApkConfigList() {
        List<HwAppQoeApkConfig> list;
        synchronized (this.mLock) {
            list = this.mApkConfigList;
        }
        return list;
    }

    public List<HwAppQoeGameConfig> getGameConfigList() {
        List<HwAppQoeGameConfig> list;
        synchronized (this.mLock) {
            list = this.mGameConfigList;
        }
        return list;
    }
}
