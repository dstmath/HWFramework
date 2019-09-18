package com.android.server.hidata.appqoe;

import android.emcom.EmcomManager;
import android.util.Xml;
import com.android.server.hidata.channelqoe.HwCHQciConfig;
import com.android.server.hidata.channelqoe.HwCHQciManager;
import com.android.server.hidata.mplink.HwMpLinkConfigInfo;
import com.android.server.hidata.mplink.HwMpLinkContentAware;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwAPPQoEResourceMangerImpl {
    private static final String CFG_FILE_NAME = "/hidata_config_cust.xml";
    private static final String CFG_VER_DIR = "emcom/noncell";
    private static final int PARA_UPGRADE_FILE_NOTEXIST = 0;
    private static final int PARA_UPGRADE_RESPONSE_FILE_ERROR = 6;
    private static final int PARA_UPGRADE_RESPONSE_UPGRADE_ALREADY = 4;
    private static final int PARA_UPGRADE_RESPONSE_UPGRADE_FAILURE = 9;
    private static final int PARA_UPGRADE_RESPONSE_UPGRADE_PENDING = 7;
    private static final int PARA_UPGRADE_RESPONSE_UPGRADE_SUCCESS = 8;
    private static final int PARA_UPGRADE_RESPONSE_VERSION_MISMATCH = 5;
    private static final String TAG = "HiData_HwAPPQoEResourceMangerImpl";
    private boolean isXmlLoadFinsh = false;
    private List<HwAPPQoEAPKConfig> mAPKConfigList = new ArrayList();
    private List<HwAPPQoEGameConfig> mGameConfigList = new ArrayList();
    private List<String> mHiCureAppConfigList = new ArrayList();
    private HwCHQciManager mHwCHQciManager;
    private HwMpLinkContentAware mHwMpLinkContentAware;
    private final Object mLock = new Object();

    public class MyRunnable implements Runnable {
        public MyRunnable() {
        }

        public void run() {
            HwAPPQoEUtils.logD(HwAPPQoEResourceMangerImpl.TAG, "Start read thread");
            HwAPPQoEResourceMangerImpl.this.readAppConfigList();
        }
    }

    public HwAPPQoEResourceMangerImpl() {
        init();
    }

    private void init() {
        this.mHwMpLinkContentAware = HwMpLinkContentAware.onlyGetInstance();
        this.mHwCHQciManager = HwCHQciManager.getInstance();
        new Thread(new MyRunnable()).start();
    }

    public void onConfigFilePathChanged() {
        new Thread(new MyRunnable()).start();
    }

    public void responseForParaUpdate(int result) {
        HwAPPQoEUtils.logD(TAG, "appqoe response,  result: " + result);
        EmcomManager mEmcomManager = EmcomManager.getInstance();
        if (mEmcomManager != null) {
            mEmcomManager.responseForParaUpgrade(16, 1, result);
        }
    }

    public String getConfigFilePath() {
        HwAPPQoEUtils.logD(TAG, "getConfigFilePath, enter");
        try {
            String[] cfgFileInfo = HwCfgFilePolicy.getDownloadCfgFile(CFG_VER_DIR, "emcom/noncell/hidata_config_cust.xml");
            if (cfgFileInfo == null) {
                HwAPPQoEUtils.logE("Both default and cota config files not exist");
                responseForParaUpdate(0);
                return null;
            } else if (cfgFileInfo[0].contains("/cota")) {
                HwAPPQoEUtils.logD(TAG, "cota config file path is: " + cfgFileInfo[0] + ", version:" + cfgFileInfo[1]);
                return cfgFileInfo[0];
            } else {
                HwAPPQoEUtils.logD(TAG, "system config file path is: " + cfgFileInfo[0] + ", version:" + cfgFileInfo[1]);
                return cfgFileInfo[0];
            }
        } catch (Exception e) {
            HwAPPQoEUtils.logD(TAG, "getConfigFilePath exception:" + e);
            responseForParaUpdate(6);
            return null;
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00bc, code lost:
        r10 = r0;
     */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x0298  */
    public void readAppConfigList() {
        HwAPPQoEAPKConfig mAPKConfig;
        HwAPPQoEGameConfig mGameConfig;
        HwMpLinkConfigInfo mHwMpLinkConfigInfo;
        HwCHQciConfig mQCIConfig;
        StringBuilder sb;
        String str;
        HwCHQciConfig hwCHQciConfig;
        InputStream inputStream = null;
        XmlPullParser xmlPullParser = null;
        String configFilePath = getConfigFilePath();
        if (configFilePath == null) {
            HwAPPQoEUtils.logD(TAG, "readAppConfigList, configPath is null");
            return;
        }
        File configPath = new File(configFilePath);
        if (!configPath.exists()) {
            responseForParaUpdate(0);
            HwAPPQoEUtils.logD(TAG, "readAppConfigList, configPath not exit");
            return;
        }
        mAPKConfig = null;
        mGameConfig = null;
        mHwMpLinkConfigInfo = null;
        mQCIConfig = null;
        int i = 1;
        try {
            inputStream = new FileInputStream(configPath);
            xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(inputStream, "utf-8");
            List<HwAPPQoEAPKConfig> mUpGratedAPKConfigList = new ArrayList<>();
            List<HwAPPQoEGameConfig> mUpGratedGameConfigList = new ArrayList<>();
            List<String> mUpGratedHiCureConfigList = new ArrayList<>();
            int eventType = xmlPullParser.getEventType();
            while (true) {
                int eventType2 = eventType;
                if (eventType2 != i) {
                    if (eventType2 != 0) {
                        switch (eventType2) {
                            case 2:
                                if (!"APKInfo".equals(xmlPullParser.getName())) {
                                    if (!"GameInfo".equals(xmlPullParser.getName())) {
                                        if (!"mplink_version".equals(xmlPullParser.getName())) {
                                            if (!"mplink_enable".equals(xmlPullParser.getName())) {
                                                if (!"vendor".equals(xmlPullParser.getName())) {
                                                    if (!"QciInfo".equals(xmlPullParser.getName())) {
                                                        if ("HiCureInfo".equals(xmlPullParser.getName())) {
                                                            mUpGratedHiCureConfigList.add(xmlPullParser.getAttributeValue(0));
                                                        }
                                                        if (mAPKConfig == null) {
                                                            if (mGameConfig == null) {
                                                                if (mHwMpLinkConfigInfo == null) {
                                                                    if (mQCIConfig == null) {
                                                                        break;
                                                                    } else {
                                                                        fillQCIConfig(mQCIConfig, xmlPullParser.getName(), xmlPullParser.nextText());
                                                                        break;
                                                                    }
                                                                } else {
                                                                    fillMpLinkConfig(mHwMpLinkConfigInfo, xmlPullParser.getName(), xmlPullParser.nextText());
                                                                    break;
                                                                }
                                                            } else {
                                                                fillGameConfig(mGameConfig, xmlPullParser.getName(), xmlPullParser.nextText());
                                                                break;
                                                            }
                                                        } else {
                                                            fillAPKConfig(mAPKConfig, xmlPullParser.getName(), xmlPullParser.nextText());
                                                            break;
                                                        }
                                                    } else {
                                                        hwCHQciConfig = new HwCHQciConfig();
                                                    }
                                                } else {
                                                    mHwMpLinkConfigInfo = new HwMpLinkConfigInfo();
                                                    mHwMpLinkConfigInfo.setmVendorOui(xmlPullParser.getAttributeValue(0));
                                                    break;
                                                }
                                            } else if (this.mHwMpLinkContentAware == null) {
                                                break;
                                            } else {
                                                this.mHwMpLinkContentAware.setMpLinkEnable(xmlPullParser.nextText());
                                                break;
                                            }
                                        } else if (this.mHwMpLinkContentAware == null) {
                                            break;
                                        } else {
                                            this.mHwMpLinkContentAware.setMpLinkVersion(xmlPullParser.nextText());
                                            break;
                                        }
                                    } else {
                                        mGameConfig = new HwAPPQoEGameConfig();
                                        mGameConfig.mGameName = xmlPullParser.getAttributeValue(0);
                                        break;
                                    }
                                } else {
                                    mAPKConfig = new HwAPPQoEAPKConfig();
                                    mAPKConfig.packageName = xmlPullParser.getAttributeValue(0);
                                    break;
                                }
                            case 3:
                                if ("APKInfo".equals(xmlPullParser.getName())) {
                                    mUpGratedAPKConfigList.add(mAPKConfig);
                                    mAPKConfig = null;
                                    break;
                                } else if ("GameInfo".equals(xmlPullParser.getName())) {
                                    mUpGratedGameConfigList.add(mGameConfig);
                                    mGameConfig = null;
                                    break;
                                } else if ("vendor".equals(xmlPullParser.getName())) {
                                    if (this.mHwMpLinkContentAware != null) {
                                        this.mHwMpLinkContentAware.addMpLinkDeviceAPP(mHwMpLinkConfigInfo);
                                    }
                                    mHwMpLinkConfigInfo = null;
                                    break;
                                } else if (!"QciInfo".equals(xmlPullParser.getName())) {
                                    break;
                                } else {
                                    if (this.mHwCHQciManager != null) {
                                        this.mHwCHQciManager.addConfig(mQCIConfig);
                                    }
                                    hwCHQciConfig = null;
                                }
                        }
                    }
                    eventType = xmlPullParser.next();
                    i = 1;
                } else {
                    synchronized (this.mLock) {
                        this.mAPKConfigList.clear();
                        this.mAPKConfigList.addAll(mUpGratedAPKConfigList);
                        mUpGratedAPKConfigList.clear();
                        this.mGameConfigList.clear();
                        this.mGameConfigList.addAll(mUpGratedGameConfigList);
                        mUpGratedGameConfigList.clear();
                        this.mHiCureAppConfigList.clear();
                        this.mHiCureAppConfigList.addAll(mUpGratedHiCureConfigList);
                        mUpGratedHiCureConfigList.clear();
                    }
                    responseForParaUpdate(8);
                    try {
                        inputStream.close();
                    } catch (Exception e) {
                        e = e;
                        Exception exc = e;
                        str = TAG;
                        sb = new StringBuilder();
                    }
                    InputStream inputStream2 = inputStream;
                    XmlPullParser xmlPullParser2 = xmlPullParser;
                    HwAPPQoEAPKConfig hwAPPQoEAPKConfig = mAPKConfig;
                    HwAPPQoEGameConfig hwAPPQoEGameConfig = mGameConfig;
                    HwMpLinkConfigInfo hwMpLinkConfigInfo = mHwMpLinkConfigInfo;
                    HwCHQciConfig hwCHQciConfig2 = mQCIConfig;
                    synchronized (this.mLock) {
                        this.isXmlLoadFinsh = true;
                    }
                }
            }
        } catch (FileNotFoundException e2) {
            HwAPPQoEUtils.logD(TAG, "readAppConfigList exception 1:" + e2);
            responseForParaUpdate(0);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e3) {
                    e = e3;
                    Exception exc2 = e;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (IOException e4) {
            HwAPPQoEUtils.logD(TAG, "readAppConfigList exception 2:" + e4);
            responseForParaUpdate(6);
            e4.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e5) {
                    e = e5;
                    Exception exc3 = e;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (XmlPullParserException e6) {
            try {
                HwAPPQoEUtils.logD(TAG, "readAppConfigList exception 3:" + e6);
                responseForParaUpdate(6);
                e6.printStackTrace();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e7) {
                        e = e7;
                        Exception exc4 = e;
                        str = TAG;
                        sb = new StringBuilder();
                    }
                }
            } catch (Throwable th) {
                XmlPullParser xmlPullParser3 = xmlPullParser;
                InputStream inputStream3 = inputStream;
                Throwable th2 = th;
                if (inputStream3 != null) {
                    try {
                        inputStream3.close();
                    } catch (Exception e8) {
                        Exception exc5 = e8;
                        HwAPPQoEUtils.logD(TAG, "readAppConfigList exception 4:" + e8);
                    }
                }
                throw th2;
            }
        }
        sb.append("readAppConfigList exception 4:");
        sb.append(e);
        HwAPPQoEUtils.logD(str, sb.toString());
        InputStream inputStream22 = inputStream;
        XmlPullParser xmlPullParser22 = xmlPullParser;
        HwAPPQoEAPKConfig hwAPPQoEAPKConfig2 = mAPKConfig;
        HwAPPQoEGameConfig hwAPPQoEGameConfig2 = mGameConfig;
        HwMpLinkConfigInfo hwMpLinkConfigInfo2 = mHwMpLinkConfigInfo;
        HwCHQciConfig hwCHQciConfig22 = mQCIConfig;
        synchronized (this.mLock) {
        }
    }

    public void fillAPKConfig(HwAPPQoEAPKConfig config, String elementName, String elementValue) {
        if (elementName == null || config == null) {
            HwAPPQoEUtils.logD(TAG, "fillAPKConfig, input error");
            return;
        }
        char c = 65535;
        switch (elementName.hashCode()) {
            case -2137635847:
                if (elementName.equals("mHistoryQoeBadTH")) {
                    c = 10;
                    break;
                }
                break;
            case -1814569853:
                if (elementName.equals("mAggressiveStallTH")) {
                    c = 12;
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
                    c = 9;
                    break;
                }
                break;
            case -9888733:
                if (elementName.equals("className")) {
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
                    c = 13;
                    break;
                }
                break;
            case 270217909:
                if (elementName.equals("mReserved")) {
                    c = 14;
                    break;
                }
                break;
            case 292004483:
                if (elementName.equals("monitorUserLearning")) {
                    c = 8;
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
                config.mScenceId = Integer.parseInt(elementValue);
                break;
            case 4:
                config.mScenceType = Integer.parseInt(elementValue);
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
            case 8:
                config.monitorUserLearning = Integer.parseInt(elementValue);
                break;
            case 9:
                config.mAction = Integer.parseInt(elementValue);
                break;
            case 10:
                config.mHistoryQoeBadTH = Float.parseFloat(elementValue);
                break;
            case 11:
                config.mGeneralStallTH = Integer.parseInt(elementValue);
                break;
            case 12:
                config.mAggressiveStallTH = Integer.parseInt(elementValue);
                break;
            case 13:
                config.mPlayActivity = Integer.parseInt(elementValue);
                break;
            case 14:
                config.mReserved = elementValue;
                break;
            default:
                HwAPPQoEUtils.logD(TAG, "fillAPKConfig, invalid element name:" + elementName);
                break;
        }
    }

    public void fillGameConfig(HwAPPQoEGameConfig config, String elementName, String elementValue) {
        if (elementName == null || config == null) {
            HwAPPQoEUtils.logD(TAG, "fillGameConfig, input error");
            return;
        }
        char c = 65535;
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
                    c = 6;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                config.mGameId = Integer.parseInt(elementValue);
                break;
            case 1:
                config.mScenceId = Integer.parseInt(elementValue);
                break;
            case 2:
                config.mGameKQI = Integer.parseInt(elementValue);
                break;
            case 3:
                config.mGameRtt = Integer.parseInt(elementValue);
                break;
            case 4:
                config.mGameAction = Integer.parseInt(elementValue);
                break;
            case 5:
                config.mHistoryQoeBadTH = Float.parseFloat(elementValue);
                break;
            case 6:
                config.mReserved = elementValue;
                break;
            default:
                HwAPPQoEUtils.logD(TAG, "fillGameConfig, invalid element name: " + elementName);
                break;
        }
    }

    public void fillQCIConfig(HwCHQciConfig config, String elementName, String elementValue) {
        if (elementName == null || config == null) {
            HwAPPQoEUtils.logD(TAG, "fillQCIConfig, input error");
            return;
        }
        char c = 65535;
        switch (elementName.hashCode()) {
            case 79991:
                if (elementName.equals("QCI")) {
                    c = 0;
                    break;
                }
                break;
            case 81490:
                if (elementName.equals("RTT")) {
                    c = 1;
                    break;
                }
                break;
            case 2525271:
                if (elementName.equals("RSSI")) {
                    c = 2;
                    break;
                }
                break;
            case 2582043:
                if (elementName.equals("TPUT")) {
                    c = 4;
                    break;
                }
                break;
            case 1986988747:
                if (elementName.equals("CHLOAD")) {
                    c = 3;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                config.mQci = Integer.parseInt(elementValue);
                break;
            case 1:
                config.mRtt = Integer.parseInt(elementValue);
                break;
            case 2:
                config.mRssi = Integer.parseInt(elementValue);
                break;
            case 3:
                config.mChload = Integer.parseInt(elementValue);
                break;
            case 4:
                config.mTput = Integer.parseInt(elementValue);
                break;
            default:
                HwAPPQoEUtils.logD(TAG, "fillQCIConfig, invalid element name: " + elementName);
                break;
        }
    }

    public void fillMpLinkConfig(HwMpLinkConfigInfo config, String elementName, String elementValue) {
        if (elementName == null || config == null) {
            HwAPPQoEUtils.logD(TAG, "fillGameConfig, input error");
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
                if (elementName.equals(MemoryConstant.MEM_POLICY_BIGAPPNAME)) {
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
            case 1127930396:
                if (elementName.equals("custmac")) {
                    c = 0;
                    break;
                }
                break;
            case 1182110946:
                if (elementName.equals("mutnetwork")) {
                    c = 2;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                config.setmCustMac(elementValue);
                break;
            case 1:
                config.setmAppName(elementValue);
                break;
            case 2:
                config.setmMultNetwork(elementValue);
                break;
            case 3:
                config.setmGatewayType(elementValue);
                break;
            case 4:
                config.setmEncryptType(elementValue);
                break;
            case 5:
                config.setmReserved(elementValue);
                break;
            case 6:
                config.setCondition(elementValue);
                break;
            default:
                HwAPPQoEUtils.logD(TAG, "fillMpLinkConfig, invalid element name: " + elementName);
                break;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0068, code lost:
        if (r0 == null) goto L_0x0080;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x006a, code lost:
        com.android.server.hidata.appqoe.HwAPPQoEUtils.logD(TAG, "checkIsMonitorAPKScence end:" + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0080, code lost:
        return r0;
     */
    public HwAPPQoEAPKConfig checkIsMonitorAPKScence(String packageName, String className) {
        HwAPPQoEAPKConfig config = null;
        synchronized (this.mLock) {
            if (this.isXmlLoadFinsh && this.mAPKConfigList.size() != 0) {
                if (packageName != null) {
                    HwAPPQoEUtils.logD(TAG, "checkIsMonitorAPKScence input :" + packageName + "," + className);
                    Iterator<HwAPPQoEAPKConfig> it = this.mAPKConfigList.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        HwAPPQoEAPKConfig apkConfig = it.next();
                        if (className == null || !className.contains(apkConfig.className)) {
                            if (packageName.equals(apkConfig.packageName) && apkConfig.mScenceId % 1000 == 0) {
                                config = apkConfig;
                            }
                        } else if (3 == apkConfig.mScenceType) {
                            config = null;
                        } else {
                            config = apkConfig;
                        }
                    }
                }
            }
            return null;
        }
    }

    public boolean checkIsMonitorHiCureAppScence(String packageName) {
        synchronized (this.mLock) {
            if (this.isXmlLoadFinsh && this.mHiCureAppConfigList.size() != 0) {
                if (packageName != null) {
                    HwAPPQoEUtils.logD(TAG, "checkIsMonitorHiCureAppScence input :" + packageName);
                    boolean contains = this.mHiCureAppConfigList.contains(packageName);
                    return contains;
                }
            }
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0051, code lost:
        if (r0 == null) goto L_0x0069;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0053, code lost:
        com.android.server.hidata.appqoe.HwAPPQoEUtils.logD(TAG, "checkIsMonitorVideoScence end:" + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0069, code lost:
        return r0;
     */
    public HwAPPQoEAPKConfig checkIsMonitorVideoScence(String packageName, String className) {
        HwAPPQoEAPKConfig config = null;
        synchronized (this.mLock) {
            if (this.isXmlLoadFinsh && this.mAPKConfigList.size() != 0) {
                if (packageName != null) {
                    HwAPPQoEUtils.logD(TAG, "checkIsMonitorVideoScence input :" + packageName + "," + className);
                    Iterator<HwAPPQoEAPKConfig> it = this.mAPKConfigList.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        HwAPPQoEAPKConfig apkConfig = it.next();
                        if (className != null && className.contains(apkConfig.className)) {
                            config = apkConfig;
                            break;
                        }
                    }
                }
            }
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0047, code lost:
        if (r0 == null) goto L_0x005f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0049, code lost:
        com.android.server.hidata.appqoe.HwAPPQoEUtils.logD(TAG, "checkIsMonitorGameScence end:" + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x005f, code lost:
        return r0;
     */
    public HwAPPQoEGameConfig checkIsMonitorGameScence(String packageName) {
        HwAPPQoEGameConfig config = null;
        synchronized (this.mLock) {
            if (this.isXmlLoadFinsh && this.mGameConfigList.size() != 0) {
                if (packageName != null) {
                    HwAPPQoEUtils.logD(TAG, "checkIsMonitorGameScence input :" + packageName);
                    Iterator<HwAPPQoEGameConfig> it = this.mGameConfigList.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        HwAPPQoEGameConfig gameConfig = it.next();
                        if (packageName.contains(gameConfig.mGameName)) {
                            config = gameConfig;
                            break;
                        }
                    }
                }
            }
            return null;
        }
    }

    public HwAPPQoEGameConfig getGameScenceConfig(int appId) {
        synchronized (this.mLock) {
            if (this.isXmlLoadFinsh) {
                if (this.mGameConfigList.size() != 0) {
                    HwAPPQoEUtils.logD(TAG, "getGameScenceConfig input :" + appId);
                    for (HwAPPQoEGameConfig gameConfig : this.mGameConfigList) {
                        if (appId == gameConfig.mGameId) {
                            HwAPPQoEUtils.logD(TAG, "getGameScenceConfig:" + gameConfig.toString());
                            return gameConfig;
                        }
                    }
                    HwAPPQoEUtils.logD(TAG, "getGameScenceConfig, not found");
                    return null;
                }
            }
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0066, code lost:
        com.android.server.hidata.appqoe.HwAPPQoEUtils.logD(TAG, "getScenceAction, action:" + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x007c, code lost:
        return r0;
     */
    public int getScenceAction(int appType, int appId, int scenceId) {
        int scenceAction = -1;
        synchronized (this.mLock) {
            if (!this.isXmlLoadFinsh) {
                return -1;
            }
            if ((1000 == appType || 4000 == appType) && this.mAPKConfigList.size() != 0) {
                Iterator<HwAPPQoEAPKConfig> it = this.mAPKConfigList.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    HwAPPQoEAPKConfig apkConfig = it.next();
                    if (appId == apkConfig.mAppId && scenceId == apkConfig.mScenceId) {
                        scenceAction = apkConfig.mAction;
                        break;
                    }
                }
            }
            if (2000 == appType && this.mGameConfigList.size() != 0) {
                Iterator<HwAPPQoEGameConfig> it2 = this.mGameConfigList.iterator();
                while (true) {
                    if (!it2.hasNext()) {
                        break;
                    }
                    HwAPPQoEGameConfig gameConfig = it2.next();
                    if (appId == gameConfig.mGameId && scenceId == gameConfig.mScenceId) {
                        scenceAction = gameConfig.mGameAction;
                        break;
                    }
                }
            }
        }
    }

    public HwAPPQoEAPKConfig getAPKScenceConfig(int scenceId) {
        synchronized (this.mLock) {
            if (this.isXmlLoadFinsh) {
                if (this.mAPKConfigList.size() != 0) {
                    HwAPPQoEUtils.logD(TAG, "getAPKScenceConfig input :" + scenceId);
                    for (HwAPPQoEAPKConfig apkConfig : this.mAPKConfigList) {
                        if (scenceId == apkConfig.mScenceId) {
                            HwAPPQoEUtils.logD(TAG, "getAPKScenceConfig:" + apkConfig.toString());
                            return apkConfig;
                        }
                    }
                    HwAPPQoEUtils.logD(TAG, "getAPKScenceConfig, not found");
                    return null;
                }
            }
            return null;
        }
    }

    public List<HwAPPQoEAPKConfig> getAPKConfigList() {
        List<HwAPPQoEAPKConfig> list;
        synchronized (this.mLock) {
            list = this.mAPKConfigList;
        }
        return list;
    }

    public List<HwAPPQoEGameConfig> getGameConfigList() {
        List<HwAPPQoEGameConfig> list;
        synchronized (this.mLock) {
            list = this.mGameConfigList;
        }
        return list;
    }
}
