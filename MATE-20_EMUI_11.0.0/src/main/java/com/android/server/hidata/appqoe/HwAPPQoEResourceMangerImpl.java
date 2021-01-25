package com.android.server.hidata.appqoe;

import android.emcom.EmcomManager;
import android.net.wifi.ScanResult;
import android.text.TextUtils;
import android.util.Xml;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.hidata.mplink.HwMpLinkConfigInfo;
import com.android.server.hidata.mplink.HwMpLinkContentAware;
import com.android.server.location.HwLocalLocationProvider;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
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
    private static final String TAG = "HiData_HwAPPQoEResourceMangerImpl";
    private boolean isXmlLoadFinsh = false;
    private List<HwAPPQoEAPKConfig> mAPKConfigList = new ArrayList();
    private List<HwAppQoeBlackListConfig> mBlackListConfigList = new ArrayList();
    private List<HwAPPQoEGameConfig> mGameConfigList = new ArrayList();
    private HwMpLinkContentAware mHwMpLinkContentAware;
    private final Object mLock = new Object();
    private List<HwAppQoeOuiBlackListConfig> mOuiBlackListConfigList = new ArrayList();
    private List<HwPowerParameterConfig> mPowerParameterConfigList = new ArrayList();
    private List<HwAppQoeWhiteListConfig> mWhiteListConfigList = new ArrayList();

    public HwAPPQoEResourceMangerImpl() {
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
            HwAPPQoEUtils.logD(HwAPPQoEResourceMangerImpl.TAG, false, "Start read thread", new Object[0]);
            HwAPPQoEResourceMangerImpl.this.readAppConfigList();
        }
    }

    public void onConfigFilePathChanged() {
        new Thread(new MyRunnable()).start();
    }

    public void responseForParaUpdate(int result) {
        HwAPPQoEUtils.logD(TAG, false, "appqoe response,  result: %{public}d", Integer.valueOf(result));
        EmcomManager mEmcomManager = EmcomManager.getInstance();
        if (mEmcomManager != null) {
            mEmcomManager.responseForParaUpgrade(16, 1, result);
        }
    }

    public String getConfigFilePath() {
        HwAPPQoEUtils.logD(TAG, false, "getConfigFilePath, enter", new Object[0]);
        try {
            String[] cfgFileInfo = HwCfgFilePolicy.getDownloadCfgFile(CFG_VER_DIR, "emcom/noncell/hidata_config_cust.xml");
            if (cfgFileInfo == null) {
                HwAPPQoEUtils.logE(HwAPPQoEUtils.TAG, false, "Both default and cota config files not exist", new Object[0]);
                responseForParaUpdate(0);
                return null;
            } else if (cfgFileInfo[0].contains("/cota")) {
                HwAPPQoEUtils.logD(TAG, false, "cota config file path is: %{public}s, version:%{public}s", cfgFileInfo[0], cfgFileInfo[1]);
                return cfgFileInfo[0];
            } else {
                HwAPPQoEUtils.logD(TAG, false, "system config file path is: %{public}s, version:%{public}s", cfgFileInfo[0], cfgFileInfo[1]);
                return cfgFileInfo[0];
            }
        } catch (Exception e) {
            HwAPPQoEUtils.logE(TAG, false, "getConfigFilePath failed by Exception", new Object[0]);
            responseForParaUpdate(6);
            return null;
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:324:0x04a4 */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:26:0x0098 */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: Multiple debug info for r20v20 java.io.InputStream: [D('inputStream' java.io.InputStream), D('upgradedWhiteListConfigList' java.util.List<com.android.server.hidata.appqoe.HwAppQoeWhiteListConfig>)] */
    /* JADX WARN: Type inference failed for: r20v25 */
    /* JADX WARN: Type inference failed for: r20v26 */
    /* JADX WARN: Type inference failed for: r20v27 */
    /* JADX WARN: Type inference failed for: r20v28 */
    /* JADX WARN: Type inference failed for: r20v37 */
    /* JADX WARN: Type inference failed for: r20v38 */
    /* JADX WARN: Type inference failed for: r20v39 */
    /* JADX WARN: Type inference failed for: r20v44 */
    /* JADX WARN: Type inference failed for: r20v45 */
    /* JADX WARN: Type inference failed for: r20v50 */
    /* JADX WARN: Type inference failed for: r20v51 */
    /* JADX WARNING: Removed duplicated region for block: B:282:0x065f A[SYNTHETIC, Splitter:B:282:0x065f] */
    /* JADX WARNING: Removed duplicated region for block: B:290:0x0685 A[SYNTHETIC, Splitter:B:290:0x0685] */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x06aa A[SYNTHETIC, Splitter:B:298:0x06aa] */
    /* JADX WARNING: Removed duplicated region for block: B:306:0x06cc  */
    /* JADX WARNING: Removed duplicated region for block: B:318:0x06df A[SYNTHETIC, Splitter:B:318:0x06df] */
    public void readAppConfigList() {
        InputStream inputStream;
        Throwable th;
        InputStream inputStream2;
        FileNotFoundException e;
        IOException e2;
        XmlPullParserException e3;
        Throwable th2;
        String configFilePath;
        File configPath;
        ArrayList arrayList;
        List<HwAppQoeBlackListConfig> upGratedBlackListConfigList;
        List<HwAPPQoEGameConfig> mUpGratedGameConfigList;
        XmlPullParser xmlPullParser = null;
        String configFilePath2 = getConfigFilePath();
        if (configFilePath2 == null) {
            HwAPPQoEUtils.logD(TAG, false, "readAppConfigList, configPath is null", new Object[0]);
            return;
        }
        File configPath2 = new File(configFilePath2);
        if (!configPath2.exists()) {
            responseForParaUpdate(0);
            HwAPPQoEUtils.logD(TAG, false, "readAppConfigList, configPath not exit", new Object[0]);
            return;
        }
        HwAPPQoEAPKConfig mAPKConfig = null;
        HwAPPQoEGameConfig mGameConfig = null;
        HwAppQoeBlackListConfig blackListConfig = null;
        HwAppQoeWhiteListConfig whiteListConfig = null;
        HwAppQoeOuiBlackListConfig ouiBlackListConfig = null;
        HwMpLinkConfigInfo mHwMpLinkConfigInfo = null;
        HwPowerParameterConfig powerParameterConfig = null;
        int i = 1;
        try {
            InputStream inputStream3 = new FileInputStream(configPath2);
            try {
                xmlPullParser = Xml.newPullParser();
            } catch (FileNotFoundException e4) {
                inputStream2 = inputStream3;
                e = e4;
                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 1:%{public}s", e.getMessage());
                responseForParaUpdate(0);
                if (inputStream2 != null) {
                }
                synchronized (this.mLock) {
                }
            } catch (IOException e5) {
                inputStream2 = inputStream3;
                e2 = e5;
                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 2:%{public}s", e2.getMessage());
                responseForParaUpdate(6);
                if (inputStream2 != null) {
                }
                synchronized (this.mLock) {
                }
            } catch (XmlPullParserException e6) {
                inputStream2 = inputStream3;
                e3 = e6;
                try {
                    HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 3:%{public}s", e3.getMessage());
                    responseForParaUpdate(6);
                    if (inputStream2 != null) {
                    }
                    synchronized (this.mLock) {
                    }
                } catch (Throwable th3) {
                    th = th3;
                    inputStream = inputStream2;
                    if (inputStream != null) {
                    }
                    throw th;
                }
            } catch (Throwable th4) {
                inputStream = inputStream3;
                th = th4;
                if (inputStream != null) {
                }
                throw th;
            }
            try {
                xmlPullParser.setInput(inputStream3, "utf-8");
                List<HwAPPQoEAPKConfig> mUpGratedAPKConfigList = new ArrayList<>();
                List<HwAPPQoEGameConfig> mUpGratedGameConfigList2 = new ArrayList<>();
                List<HwAppQoeBlackListConfig> upGratedBlackListConfigList2 = new ArrayList<>();
                InputStream inputStream4 = new ArrayList();
                List<HwAppQoeOuiBlackListConfig> upGratedOuiBlackListConfigList = new ArrayList<>();
                List<HwPowerParameterConfig> upgradedPowerParameterConfigList = new ArrayList<>();
                HwPowerParameterConfig powerParameterConfig2 = null;
                HwAppQoeOuiBlackListConfig ouiBlackListConfig2 = null;
                HwAppQoeWhiteListConfig whiteListConfig2 = null;
                HwAppQoeBlackListConfig blackListConfig2 = null;
                HwAPPQoEGameConfig mGameConfig2 = null;
                HwAPPQoEAPKConfig mAPKConfig2 = null;
                int eventType = xmlPullParser.getEventType();
                while (eventType != i) {
                    if (eventType == 0) {
                        configFilePath = configFilePath2;
                        mUpGratedGameConfigList = mUpGratedGameConfigList2;
                        configPath = configPath2;
                        upGratedBlackListConfigList = upGratedBlackListConfigList2;
                        arrayList = inputStream4;
                        inputStream4 = inputStream3;
                    } else if (eventType == 2) {
                        configFilePath = configFilePath2;
                        mUpGratedGameConfigList = mUpGratedGameConfigList2;
                        configPath = configPath2;
                        upGratedBlackListConfigList = upGratedBlackListConfigList2;
                        arrayList = inputStream4;
                        inputStream4 = inputStream3;
                        upgradedPowerParameterConfigList = upgradedPowerParameterConfigList;
                        if ("APKInfo".equals(xmlPullParser.getName())) {
                            HwAPPQoEAPKConfig mAPKConfig3 = new HwAPPQoEAPKConfig();
                            try {
                                mAPKConfig3.packageName = xmlPullParser.getAttributeValue(0);
                                mAPKConfig2 = mAPKConfig3;
                            } catch (FileNotFoundException e7) {
                                mAPKConfig = mAPKConfig3;
                                mGameConfig = mGameConfig2;
                                blackListConfig = blackListConfig2;
                                whiteListConfig = whiteListConfig2;
                                ouiBlackListConfig = ouiBlackListConfig2;
                                e = e7;
                                powerParameterConfig = powerParameterConfig2;
                                inputStream2 = inputStream4;
                                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 1:%{public}s", e.getMessage());
                                responseForParaUpdate(0);
                                if (inputStream2 != null) {
                                }
                                synchronized (this.mLock) {
                                }
                            } catch (IOException e8) {
                                mAPKConfig = mAPKConfig3;
                                mGameConfig = mGameConfig2;
                                blackListConfig = blackListConfig2;
                                whiteListConfig = whiteListConfig2;
                                ouiBlackListConfig = ouiBlackListConfig2;
                                e2 = e8;
                                powerParameterConfig = powerParameterConfig2;
                                inputStream2 = inputStream4;
                                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 2:%{public}s", e2.getMessage());
                                responseForParaUpdate(6);
                                if (inputStream2 != null) {
                                }
                                synchronized (this.mLock) {
                                }
                            } catch (XmlPullParserException e9) {
                                mAPKConfig = mAPKConfig3;
                                mGameConfig = mGameConfig2;
                                blackListConfig = blackListConfig2;
                                whiteListConfig = whiteListConfig2;
                                ouiBlackListConfig = ouiBlackListConfig2;
                                e3 = e9;
                                powerParameterConfig = powerParameterConfig2;
                                inputStream2 = inputStream4;
                                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 3:%{public}s", e3.getMessage());
                                responseForParaUpdate(6);
                                if (inputStream2 != null) {
                                }
                                synchronized (this.mLock) {
                                }
                            } catch (Throwable th5) {
                                th = th5;
                                inputStream = inputStream4;
                                if (inputStream != null) {
                                }
                                throw th;
                            }
                        } else if ("GameInfo".equals(xmlPullParser.getName())) {
                            HwAPPQoEGameConfig mGameConfig3 = new HwAPPQoEGameConfig();
                            try {
                                mGameConfig3.mGameName = xmlPullParser.getAttributeValue(0);
                                mGameConfig2 = mGameConfig3;
                            } catch (FileNotFoundException e10) {
                                mAPKConfig = mAPKConfig2;
                                blackListConfig = blackListConfig2;
                                whiteListConfig = whiteListConfig2;
                                ouiBlackListConfig = ouiBlackListConfig2;
                                mGameConfig = mGameConfig3;
                                powerParameterConfig = powerParameterConfig2;
                                e = e10;
                                inputStream2 = inputStream4;
                                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 1:%{public}s", e.getMessage());
                                responseForParaUpdate(0);
                                if (inputStream2 != null) {
                                }
                                synchronized (this.mLock) {
                                }
                            } catch (IOException e11) {
                                mAPKConfig = mAPKConfig2;
                                blackListConfig = blackListConfig2;
                                whiteListConfig = whiteListConfig2;
                                ouiBlackListConfig = ouiBlackListConfig2;
                                mGameConfig = mGameConfig3;
                                powerParameterConfig = powerParameterConfig2;
                                e2 = e11;
                                inputStream2 = inputStream4;
                                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 2:%{public}s", e2.getMessage());
                                responseForParaUpdate(6);
                                if (inputStream2 != null) {
                                }
                                synchronized (this.mLock) {
                                }
                            } catch (XmlPullParserException e12) {
                                mAPKConfig = mAPKConfig2;
                                blackListConfig = blackListConfig2;
                                whiteListConfig = whiteListConfig2;
                                ouiBlackListConfig = ouiBlackListConfig2;
                                mGameConfig = mGameConfig3;
                                powerParameterConfig = powerParameterConfig2;
                                e3 = e12;
                                inputStream2 = inputStream4;
                                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 3:%{public}s", e3.getMessage());
                                responseForParaUpdate(6);
                                if (inputStream2 != null) {
                                }
                                synchronized (this.mLock) {
                                }
                            } catch (Throwable th6) {
                                th = th6;
                                inputStream = inputStream4;
                                if (inputStream != null) {
                                }
                                throw th;
                            }
                        } else if ("mplink_version".equals(xmlPullParser.getName())) {
                            if (this.mHwMpLinkContentAware != null) {
                                this.mHwMpLinkContentAware.setMpLinkVersion(xmlPullParser.nextText());
                            }
                        } else if ("mplink_enable".equals(xmlPullParser.getName())) {
                            if (this.mHwMpLinkContentAware != null) {
                                this.mHwMpLinkContentAware.setMpLinkEnable(xmlPullParser.nextText());
                            }
                        } else if ("vendor".equals(xmlPullParser.getName())) {
                            mHwMpLinkConfigInfo = new HwMpLinkConfigInfo();
                            mHwMpLinkConfigInfo.setmVendorOui(xmlPullParser.getAttributeValue(0));
                        } else if ("AppBlackList".equals(xmlPullParser.getName())) {
                            HwAppQoeBlackListConfig blackListConfig3 = new HwAppQoeBlackListConfig();
                            try {
                                blackListConfig3.setPackageName(xmlPullParser.getAttributeValue(0));
                                blackListConfig2 = blackListConfig3;
                            } catch (FileNotFoundException e13) {
                                mAPKConfig = mAPKConfig2;
                                mGameConfig = mGameConfig2;
                                whiteListConfig = whiteListConfig2;
                                ouiBlackListConfig = ouiBlackListConfig2;
                                blackListConfig = blackListConfig3;
                                powerParameterConfig = powerParameterConfig2;
                                e = e13;
                                inputStream2 = inputStream4;
                                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 1:%{public}s", e.getMessage());
                                responseForParaUpdate(0);
                                if (inputStream2 != null) {
                                    try {
                                        inputStream2.close();
                                    } catch (Exception e14) {
                                    }
                                }
                                synchronized (this.mLock) {
                                }
                            } catch (IOException e15) {
                                mAPKConfig = mAPKConfig2;
                                mGameConfig = mGameConfig2;
                                whiteListConfig = whiteListConfig2;
                                ouiBlackListConfig = ouiBlackListConfig2;
                                blackListConfig = blackListConfig3;
                                powerParameterConfig = powerParameterConfig2;
                                e2 = e15;
                                inputStream2 = inputStream4;
                                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 2:%{public}s", e2.getMessage());
                                responseForParaUpdate(6);
                                if (inputStream2 != null) {
                                    try {
                                        inputStream2.close();
                                    } catch (Exception e16) {
                                    }
                                }
                                synchronized (this.mLock) {
                                }
                            } catch (XmlPullParserException e17) {
                                mAPKConfig = mAPKConfig2;
                                mGameConfig = mGameConfig2;
                                whiteListConfig = whiteListConfig2;
                                ouiBlackListConfig = ouiBlackListConfig2;
                                blackListConfig = blackListConfig3;
                                powerParameterConfig = powerParameterConfig2;
                                e3 = e17;
                                inputStream2 = inputStream4;
                                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 3:%{public}s", e3.getMessage());
                                responseForParaUpdate(6);
                                if (inputStream2 != null) {
                                    try {
                                        inputStream2.close();
                                    } catch (Exception e18) {
                                    }
                                }
                                synchronized (this.mLock) {
                                }
                            } catch (Throwable th7) {
                                th = th7;
                                inputStream = inputStream4;
                                if (inputStream != null) {
                                    try {
                                        inputStream.close();
                                    } catch (Exception e19) {
                                        HwAPPQoEUtils.logE(TAG, false, "readAppConfigList failed by Exception", new Object[0]);
                                    }
                                }
                                throw th;
                            }
                        } else if ("AppWhiteList".equals(xmlPullParser.getName())) {
                            HwAppQoeWhiteListConfig whiteListConfig3 = new HwAppQoeWhiteListConfig();
                            try {
                                whiteListConfig3.setPackageName(xmlPullParser.getAttributeValue(0));
                                whiteListConfig2 = whiteListConfig3;
                            } catch (FileNotFoundException e20) {
                                mAPKConfig = mAPKConfig2;
                                mGameConfig = mGameConfig2;
                                blackListConfig = blackListConfig2;
                                ouiBlackListConfig = ouiBlackListConfig2;
                                whiteListConfig = whiteListConfig3;
                                powerParameterConfig = powerParameterConfig2;
                                e = e20;
                                inputStream2 = inputStream4;
                                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 1:%{public}s", e.getMessage());
                                responseForParaUpdate(0);
                                if (inputStream2 != null) {
                                }
                                synchronized (this.mLock) {
                                }
                            } catch (IOException e21) {
                                mAPKConfig = mAPKConfig2;
                                mGameConfig = mGameConfig2;
                                blackListConfig = blackListConfig2;
                                ouiBlackListConfig = ouiBlackListConfig2;
                                whiteListConfig = whiteListConfig3;
                                powerParameterConfig = powerParameterConfig2;
                                e2 = e21;
                                inputStream2 = inputStream4;
                                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 2:%{public}s", e2.getMessage());
                                responseForParaUpdate(6);
                                if (inputStream2 != null) {
                                }
                                synchronized (this.mLock) {
                                }
                            } catch (XmlPullParserException e22) {
                                mAPKConfig = mAPKConfig2;
                                mGameConfig = mGameConfig2;
                                blackListConfig = blackListConfig2;
                                ouiBlackListConfig = ouiBlackListConfig2;
                                whiteListConfig = whiteListConfig3;
                                powerParameterConfig = powerParameterConfig2;
                                e3 = e22;
                                inputStream2 = inputStream4;
                                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 3:%{public}s", e3.getMessage());
                                responseForParaUpdate(6);
                                if (inputStream2 != null) {
                                }
                                synchronized (this.mLock) {
                                }
                            } catch (Throwable th8) {
                                th = th8;
                                inputStream = inputStream4;
                                if (inputStream != null) {
                                }
                                throw th;
                            }
                        } else if ("OuiBlackList".equals(xmlPullParser.getName())) {
                            HwAppQoeOuiBlackListConfig ouiBlackListConfig3 = new HwAppQoeOuiBlackListConfig();
                            try {
                                ouiBlackListConfig3.setOuiName(xmlPullParser.getAttributeValue(0));
                                ouiBlackListConfig2 = ouiBlackListConfig3;
                            } catch (FileNotFoundException e23) {
                                powerParameterConfig = powerParameterConfig2;
                                mAPKConfig = mAPKConfig2;
                                mGameConfig = mGameConfig2;
                                blackListConfig = blackListConfig2;
                                whiteListConfig = whiteListConfig2;
                                ouiBlackListConfig = ouiBlackListConfig3;
                                e = e23;
                                inputStream2 = inputStream4;
                                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 1:%{public}s", e.getMessage());
                                responseForParaUpdate(0);
                                if (inputStream2 != null) {
                                }
                                synchronized (this.mLock) {
                                }
                            } catch (IOException e24) {
                                powerParameterConfig = powerParameterConfig2;
                                mAPKConfig = mAPKConfig2;
                                mGameConfig = mGameConfig2;
                                blackListConfig = blackListConfig2;
                                whiteListConfig = whiteListConfig2;
                                ouiBlackListConfig = ouiBlackListConfig3;
                                e2 = e24;
                                inputStream2 = inputStream4;
                                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 2:%{public}s", e2.getMessage());
                                responseForParaUpdate(6);
                                if (inputStream2 != null) {
                                }
                                synchronized (this.mLock) {
                                }
                            } catch (XmlPullParserException e25) {
                                powerParameterConfig = powerParameterConfig2;
                                mAPKConfig = mAPKConfig2;
                                mGameConfig = mGameConfig2;
                                blackListConfig = blackListConfig2;
                                whiteListConfig = whiteListConfig2;
                                ouiBlackListConfig = ouiBlackListConfig3;
                                e3 = e25;
                                inputStream2 = inputStream4;
                                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 3:%{public}s", e3.getMessage());
                                responseForParaUpdate(6);
                                if (inputStream2 != null) {
                                }
                                synchronized (this.mLock) {
                                }
                            } catch (Throwable th9) {
                                th = th9;
                                inputStream = inputStream4;
                                if (inputStream != null) {
                                }
                                throw th;
                            }
                        } else if ("PowerParameter".equals(xmlPullParser.getName())) {
                            HwPowerParameterConfig powerParameterConfig3 = new HwPowerParameterConfig();
                            try {
                                powerParameterConfig3.setPowerParameterName(xmlPullParser.getAttributeValue(0));
                                powerParameterConfig2 = powerParameterConfig3;
                            } catch (FileNotFoundException e26) {
                                mAPKConfig = mAPKConfig2;
                                mGameConfig = mGameConfig2;
                                blackListConfig = blackListConfig2;
                                whiteListConfig = whiteListConfig2;
                                ouiBlackListConfig = ouiBlackListConfig2;
                                powerParameterConfig = powerParameterConfig3;
                                e = e26;
                                inputStream2 = inputStream4;
                                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 1:%{public}s", e.getMessage());
                                responseForParaUpdate(0);
                                if (inputStream2 != null) {
                                }
                                synchronized (this.mLock) {
                                }
                            } catch (IOException e27) {
                                mAPKConfig = mAPKConfig2;
                                mGameConfig = mGameConfig2;
                                blackListConfig = blackListConfig2;
                                whiteListConfig = whiteListConfig2;
                                ouiBlackListConfig = ouiBlackListConfig2;
                                powerParameterConfig = powerParameterConfig3;
                                e2 = e27;
                                inputStream2 = inputStream4;
                                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 2:%{public}s", e2.getMessage());
                                responseForParaUpdate(6);
                                if (inputStream2 != null) {
                                }
                                synchronized (this.mLock) {
                                }
                            } catch (XmlPullParserException e28) {
                                mAPKConfig = mAPKConfig2;
                                mGameConfig = mGameConfig2;
                                blackListConfig = blackListConfig2;
                                whiteListConfig = whiteListConfig2;
                                ouiBlackListConfig = ouiBlackListConfig2;
                                powerParameterConfig = powerParameterConfig3;
                                e3 = e28;
                                inputStream2 = inputStream4;
                                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 3:%{public}s", e3.getMessage());
                                responseForParaUpdate(6);
                                if (inputStream2 != null) {
                                }
                                synchronized (this.mLock) {
                                }
                            } catch (Throwable th10) {
                                th = th10;
                                inputStream = inputStream4;
                                if (inputStream != null) {
                                }
                                throw th;
                            }
                        } else if (mAPKConfig2 != null) {
                            fillAPKConfig(mAPKConfig2, xmlPullParser.getName(), xmlPullParser.nextText());
                        } else if (mGameConfig2 != null) {
                            fillGameConfig(mGameConfig2, xmlPullParser.getName(), xmlPullParser.nextText());
                        } else if (mHwMpLinkConfigInfo != null) {
                            fillMpLinkConfig(mHwMpLinkConfigInfo, xmlPullParser.getName(), xmlPullParser.nextText());
                        } else if (blackListConfig2 != null) {
                            fillBlackListConfig(blackListConfig2, xmlPullParser.getName(), xmlPullParser.nextText());
                        } else if (whiteListConfig2 != null) {
                            fillWhiteListConfig(whiteListConfig2, xmlPullParser.getName(), xmlPullParser.nextText());
                        } else if (ouiBlackListConfig2 != null) {
                            fillOuiBlackListConfig(ouiBlackListConfig2, xmlPullParser.getName(), xmlPullParser.nextText());
                        } else if (powerParameterConfig2 != null) {
                            fillPowerParameterConfig(powerParameterConfig2, xmlPullParser.getName(), xmlPullParser.nextText());
                        }
                    } else if (eventType != 3) {
                        configFilePath = configFilePath2;
                        mUpGratedGameConfigList = mUpGratedGameConfigList2;
                        configPath = configPath2;
                        upGratedBlackListConfigList = upGratedBlackListConfigList2;
                        arrayList = inputStream4;
                        inputStream4 = inputStream3;
                    } else {
                        configFilePath = configFilePath2;
                        try {
                            if ("APKInfo".equals(xmlPullParser.getName())) {
                                try {
                                    mUpGratedAPKConfigList.add(mAPKConfig2);
                                    mAPKConfig2 = null;
                                    mUpGratedGameConfigList = mUpGratedGameConfigList2;
                                    configPath = configPath2;
                                    upGratedBlackListConfigList = upGratedBlackListConfigList2;
                                    arrayList = inputStream4;
                                    inputStream4 = inputStream3;
                                } catch (FileNotFoundException e29) {
                                    inputStream2 = inputStream3;
                                    mAPKConfig = mAPKConfig2;
                                    mGameConfig = mGameConfig2;
                                    blackListConfig = blackListConfig2;
                                    whiteListConfig = whiteListConfig2;
                                    ouiBlackListConfig = ouiBlackListConfig2;
                                    e = e29;
                                    powerParameterConfig = powerParameterConfig2;
                                    HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 1:%{public}s", e.getMessage());
                                    responseForParaUpdate(0);
                                    if (inputStream2 != null) {
                                    }
                                    synchronized (this.mLock) {
                                    }
                                } catch (IOException e30) {
                                    inputStream2 = inputStream3;
                                    mAPKConfig = mAPKConfig2;
                                    mGameConfig = mGameConfig2;
                                    blackListConfig = blackListConfig2;
                                    whiteListConfig = whiteListConfig2;
                                    ouiBlackListConfig = ouiBlackListConfig2;
                                    e2 = e30;
                                    powerParameterConfig = powerParameterConfig2;
                                    HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 2:%{public}s", e2.getMessage());
                                    responseForParaUpdate(6);
                                    if (inputStream2 != null) {
                                    }
                                    synchronized (this.mLock) {
                                    }
                                } catch (XmlPullParserException e31) {
                                    inputStream2 = inputStream3;
                                    mAPKConfig = mAPKConfig2;
                                    mGameConfig = mGameConfig2;
                                    blackListConfig = blackListConfig2;
                                    whiteListConfig = whiteListConfig2;
                                    ouiBlackListConfig = ouiBlackListConfig2;
                                    e3 = e31;
                                    powerParameterConfig = powerParameterConfig2;
                                    HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 3:%{public}s", e3.getMessage());
                                    responseForParaUpdate(6);
                                    if (inputStream2 != null) {
                                    }
                                    synchronized (this.mLock) {
                                    }
                                } catch (Throwable th11) {
                                    inputStream = inputStream3;
                                    th = th11;
                                    if (inputStream != null) {
                                    }
                                    throw th;
                                }
                            } else if ("GameInfo".equals(xmlPullParser.getName())) {
                                mUpGratedGameConfigList = mUpGratedGameConfigList2;
                                mUpGratedGameConfigList.add(mGameConfig2);
                                mGameConfig2 = null;
                                configPath = configPath2;
                                upGratedBlackListConfigList = upGratedBlackListConfigList2;
                                arrayList = inputStream4;
                                inputStream4 = inputStream3;
                            } else {
                                mUpGratedGameConfigList = mUpGratedGameConfigList2;
                                configPath = configPath2;
                                try {
                                    if ("vendor".equals(xmlPullParser.getName())) {
                                        try {
                                            if (this.mHwMpLinkContentAware != null) {
                                                this.mHwMpLinkContentAware.addMpLinkDeviceApp(mHwMpLinkConfigInfo);
                                            }
                                            mHwMpLinkConfigInfo = null;
                                            upGratedBlackListConfigList = upGratedBlackListConfigList2;
                                            arrayList = inputStream4;
                                            inputStream4 = inputStream3;
                                        } catch (FileNotFoundException e32) {
                                            inputStream2 = inputStream3;
                                            mAPKConfig = mAPKConfig2;
                                            mGameConfig = mGameConfig2;
                                            blackListConfig = blackListConfig2;
                                            whiteListConfig = whiteListConfig2;
                                            ouiBlackListConfig = ouiBlackListConfig2;
                                            e = e32;
                                            powerParameterConfig = powerParameterConfig2;
                                            HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 1:%{public}s", e.getMessage());
                                            responseForParaUpdate(0);
                                            if (inputStream2 != null) {
                                            }
                                            synchronized (this.mLock) {
                                            }
                                        } catch (IOException e33) {
                                            inputStream2 = inputStream3;
                                            mAPKConfig = mAPKConfig2;
                                            mGameConfig = mGameConfig2;
                                            blackListConfig = blackListConfig2;
                                            whiteListConfig = whiteListConfig2;
                                            ouiBlackListConfig = ouiBlackListConfig2;
                                            e2 = e33;
                                            powerParameterConfig = powerParameterConfig2;
                                            HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 2:%{public}s", e2.getMessage());
                                            responseForParaUpdate(6);
                                            if (inputStream2 != null) {
                                            }
                                            synchronized (this.mLock) {
                                            }
                                        } catch (XmlPullParserException e34) {
                                            inputStream2 = inputStream3;
                                            mAPKConfig = mAPKConfig2;
                                            mGameConfig = mGameConfig2;
                                            blackListConfig = blackListConfig2;
                                            whiteListConfig = whiteListConfig2;
                                            ouiBlackListConfig = ouiBlackListConfig2;
                                            e3 = e34;
                                            powerParameterConfig = powerParameterConfig2;
                                            HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 3:%{public}s", e3.getMessage());
                                            responseForParaUpdate(6);
                                            if (inputStream2 != null) {
                                            }
                                            synchronized (this.mLock) {
                                            }
                                        } catch (Throwable th12) {
                                            inputStream = inputStream3;
                                            th = th12;
                                            if (inputStream != null) {
                                            }
                                            throw th;
                                        }
                                    } else if ("AppBlackList".equals(xmlPullParser.getName())) {
                                        upGratedBlackListConfigList = upGratedBlackListConfigList2;
                                        upGratedBlackListConfigList.add(blackListConfig2);
                                        blackListConfig2 = null;
                                        arrayList = inputStream4;
                                        inputStream4 = inputStream3;
                                    } else {
                                        upGratedBlackListConfigList = upGratedBlackListConfigList2;
                                        if ("AppWhiteList".equals(xmlPullParser.getName())) {
                                            arrayList = inputStream4;
                                            arrayList.add(whiteListConfig2);
                                            whiteListConfig2 = null;
                                            inputStream4 = inputStream3;
                                        } else {
                                            arrayList = inputStream4;
                                            inputStream4 = inputStream3;
                                            try {
                                                if ("OuiBlackList".equals(xmlPullParser.getName())) {
                                                    upGratedOuiBlackListConfigList.add(ouiBlackListConfig2);
                                                    ouiBlackListConfig2 = null;
                                                    upGratedOuiBlackListConfigList = upGratedOuiBlackListConfigList;
                                                } else {
                                                    upGratedOuiBlackListConfigList = upGratedOuiBlackListConfigList;
                                                    if ("PowerParameter".equals(xmlPullParser.getName())) {
                                                        upgradedPowerParameterConfigList.add(powerParameterConfig2);
                                                        powerParameterConfig2 = null;
                                                        upgradedPowerParameterConfigList = upgradedPowerParameterConfigList;
                                                    }
                                                }
                                            } catch (FileNotFoundException e35) {
                                                e = e35;
                                                mAPKConfig = mAPKConfig2;
                                                mGameConfig = mGameConfig2;
                                                blackListConfig = blackListConfig2;
                                                whiteListConfig = whiteListConfig2;
                                                ouiBlackListConfig = ouiBlackListConfig2;
                                                powerParameterConfig = powerParameterConfig2;
                                                inputStream2 = inputStream4;
                                                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 1:%{public}s", e.getMessage());
                                                responseForParaUpdate(0);
                                                if (inputStream2 != null) {
                                                }
                                                synchronized (this.mLock) {
                                                }
                                            } catch (IOException e36) {
                                                e2 = e36;
                                                mAPKConfig = mAPKConfig2;
                                                mGameConfig = mGameConfig2;
                                                blackListConfig = blackListConfig2;
                                                whiteListConfig = whiteListConfig2;
                                                ouiBlackListConfig = ouiBlackListConfig2;
                                                powerParameterConfig = powerParameterConfig2;
                                                inputStream2 = inputStream4;
                                                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 2:%{public}s", e2.getMessage());
                                                responseForParaUpdate(6);
                                                if (inputStream2 != null) {
                                                }
                                                synchronized (this.mLock) {
                                                }
                                            } catch (XmlPullParserException e37) {
                                                e3 = e37;
                                                mAPKConfig = mAPKConfig2;
                                                mGameConfig = mGameConfig2;
                                                blackListConfig = blackListConfig2;
                                                whiteListConfig = whiteListConfig2;
                                                ouiBlackListConfig = ouiBlackListConfig2;
                                                powerParameterConfig = powerParameterConfig2;
                                                inputStream2 = inputStream4;
                                                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 3:%{public}s", e3.getMessage());
                                                responseForParaUpdate(6);
                                                if (inputStream2 != null) {
                                                }
                                                synchronized (this.mLock) {
                                                }
                                            } catch (Throwable th13) {
                                                th = th13;
                                                inputStream = inputStream4;
                                                if (inputStream != null) {
                                                }
                                                throw th;
                                            }
                                        }
                                    }
                                } catch (FileNotFoundException e38) {
                                    inputStream2 = inputStream3;
                                    e = e38;
                                    mAPKConfig = mAPKConfig2;
                                    mGameConfig = mGameConfig2;
                                    blackListConfig = blackListConfig2;
                                    whiteListConfig = whiteListConfig2;
                                    ouiBlackListConfig = ouiBlackListConfig2;
                                    powerParameterConfig = powerParameterConfig2;
                                    HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 1:%{public}s", e.getMessage());
                                    responseForParaUpdate(0);
                                    if (inputStream2 != null) {
                                    }
                                    synchronized (this.mLock) {
                                    }
                                } catch (IOException e39) {
                                    inputStream2 = inputStream3;
                                    e2 = e39;
                                    mAPKConfig = mAPKConfig2;
                                    mGameConfig = mGameConfig2;
                                    blackListConfig = blackListConfig2;
                                    whiteListConfig = whiteListConfig2;
                                    ouiBlackListConfig = ouiBlackListConfig2;
                                    powerParameterConfig = powerParameterConfig2;
                                    HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 2:%{public}s", e2.getMessage());
                                    responseForParaUpdate(6);
                                    if (inputStream2 != null) {
                                    }
                                    synchronized (this.mLock) {
                                    }
                                } catch (XmlPullParserException e40) {
                                    inputStream2 = inputStream3;
                                    e3 = e40;
                                    mAPKConfig = mAPKConfig2;
                                    mGameConfig = mGameConfig2;
                                    blackListConfig = blackListConfig2;
                                    whiteListConfig = whiteListConfig2;
                                    ouiBlackListConfig = ouiBlackListConfig2;
                                    powerParameterConfig = powerParameterConfig2;
                                    HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 3:%{public}s", e3.getMessage());
                                    responseForParaUpdate(6);
                                    if (inputStream2 != null) {
                                    }
                                    synchronized (this.mLock) {
                                    }
                                } catch (Throwable th14) {
                                    inputStream = inputStream3;
                                    th = th14;
                                    if (inputStream != null) {
                                    }
                                    throw th;
                                }
                            }
                        } catch (FileNotFoundException e41) {
                            inputStream2 = inputStream3;
                            e = e41;
                            mAPKConfig = mAPKConfig2;
                            mGameConfig = mGameConfig2;
                            blackListConfig = blackListConfig2;
                            whiteListConfig = whiteListConfig2;
                            ouiBlackListConfig = ouiBlackListConfig2;
                            powerParameterConfig = powerParameterConfig2;
                            HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 1:%{public}s", e.getMessage());
                            responseForParaUpdate(0);
                            if (inputStream2 != null) {
                            }
                            synchronized (this.mLock) {
                            }
                        } catch (IOException e42) {
                            inputStream2 = inputStream3;
                            e2 = e42;
                            mAPKConfig = mAPKConfig2;
                            mGameConfig = mGameConfig2;
                            blackListConfig = blackListConfig2;
                            whiteListConfig = whiteListConfig2;
                            ouiBlackListConfig = ouiBlackListConfig2;
                            powerParameterConfig = powerParameterConfig2;
                            HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 2:%{public}s", e2.getMessage());
                            responseForParaUpdate(6);
                            if (inputStream2 != null) {
                            }
                            synchronized (this.mLock) {
                            }
                        } catch (XmlPullParserException e43) {
                            inputStream2 = inputStream3;
                            e3 = e43;
                            mAPKConfig = mAPKConfig2;
                            mGameConfig = mGameConfig2;
                            blackListConfig = blackListConfig2;
                            whiteListConfig = whiteListConfig2;
                            ouiBlackListConfig = ouiBlackListConfig2;
                            powerParameterConfig = powerParameterConfig2;
                            HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 3:%{public}s", e3.getMessage());
                            responseForParaUpdate(6);
                            if (inputStream2 != null) {
                            }
                            synchronized (this.mLock) {
                            }
                        } catch (Throwable th15) {
                            inputStream = inputStream3;
                            th = th15;
                            if (inputStream != null) {
                            }
                            throw th;
                        }
                    }
                    upGratedBlackListConfigList2 = upGratedBlackListConfigList;
                    configPath2 = configPath;
                    i = 1;
                    mUpGratedGameConfigList2 = mUpGratedGameConfigList;
                    configFilePath2 = configFilePath;
                    eventType = xmlPullParser.next();
                    inputStream3 = inputStream4;
                    inputStream4 = arrayList;
                }
                InputStream inputStream5 = inputStream3;
                try {
                    synchronized (this.mLock) {
                        try {
                            this.mAPKConfigList.clear();
                            this.mAPKConfigList.addAll(mUpGratedAPKConfigList);
                            mUpGratedAPKConfigList.clear();
                            this.mGameConfigList.clear();
                            this.mGameConfigList.addAll(mUpGratedGameConfigList2);
                            mUpGratedGameConfigList2.clear();
                            this.mBlackListConfigList.clear();
                            this.mBlackListConfigList.addAll(upGratedBlackListConfigList2);
                            upGratedBlackListConfigList2.clear();
                            this.mWhiteListConfigList.clear();
                            this.mWhiteListConfigList.addAll(inputStream4);
                            inputStream4.clear();
                            this.mPowerParameterConfigList.clear();
                            try {
                                this.mPowerParameterConfigList.addAll(upgradedPowerParameterConfigList);
                                upgradedPowerParameterConfigList.clear();
                                this.mOuiBlackListConfigList.clear();
                                try {
                                    this.mOuiBlackListConfigList.addAll(upGratedOuiBlackListConfigList);
                                    upGratedOuiBlackListConfigList.clear();
                                } catch (Throwable th16) {
                                    th2 = th16;
                                    throw th2;
                                }
                            } catch (Throwable th17) {
                                th2 = th17;
                                throw th2;
                            }
                        } catch (Throwable th18) {
                            th2 = th18;
                            throw th2;
                        }
                    }
                } catch (FileNotFoundException e44) {
                    e = e44;
                    mAPKConfig = mAPKConfig2;
                    mGameConfig = mGameConfig2;
                    blackListConfig = blackListConfig2;
                    whiteListConfig = whiteListConfig2;
                    ouiBlackListConfig = ouiBlackListConfig2;
                    powerParameterConfig = powerParameterConfig2;
                    inputStream2 = inputStream5;
                    HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 1:%{public}s", e.getMessage());
                    responseForParaUpdate(0);
                    if (inputStream2 != null) {
                    }
                    synchronized (this.mLock) {
                    }
                } catch (IOException e45) {
                    e2 = e45;
                    mAPKConfig = mAPKConfig2;
                    mGameConfig = mGameConfig2;
                    blackListConfig = blackListConfig2;
                    whiteListConfig = whiteListConfig2;
                    ouiBlackListConfig = ouiBlackListConfig2;
                    powerParameterConfig = powerParameterConfig2;
                    inputStream2 = inputStream5;
                    HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 2:%{public}s", e2.getMessage());
                    responseForParaUpdate(6);
                    if (inputStream2 != null) {
                    }
                    synchronized (this.mLock) {
                    }
                } catch (XmlPullParserException e46) {
                    e3 = e46;
                    mAPKConfig = mAPKConfig2;
                    mGameConfig = mGameConfig2;
                    blackListConfig = blackListConfig2;
                    whiteListConfig = whiteListConfig2;
                    ouiBlackListConfig = ouiBlackListConfig2;
                    powerParameterConfig = powerParameterConfig2;
                    inputStream2 = inputStream5;
                    HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 3:%{public}s", e3.getMessage());
                    responseForParaUpdate(6);
                    if (inputStream2 != null) {
                    }
                    synchronized (this.mLock) {
                    }
                } catch (Throwable th19) {
                    th = th19;
                    inputStream = inputStream5;
                    if (inputStream != null) {
                    }
                    throw th;
                }
                try {
                    responseForParaUpdate(8);
                    try {
                        inputStream5.close();
                    } catch (Exception e47) {
                        HwAPPQoEUtils.logE(TAG, false, "readAppConfigList failed by Exception", new Object[0]);
                    }
                } catch (FileNotFoundException e48) {
                    e = e48;
                    mAPKConfig = mAPKConfig2;
                    mGameConfig = mGameConfig2;
                    blackListConfig = blackListConfig2;
                    whiteListConfig = whiteListConfig2;
                    ouiBlackListConfig = ouiBlackListConfig2;
                    xmlPullParser = xmlPullParser;
                    powerParameterConfig = powerParameterConfig2;
                    inputStream2 = inputStream5;
                } catch (IOException e49) {
                    e2 = e49;
                    mAPKConfig = mAPKConfig2;
                    mGameConfig = mGameConfig2;
                    blackListConfig = blackListConfig2;
                    whiteListConfig = whiteListConfig2;
                    ouiBlackListConfig = ouiBlackListConfig2;
                    xmlPullParser = xmlPullParser;
                    powerParameterConfig = powerParameterConfig2;
                    inputStream2 = inputStream5;
                    HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 2:%{public}s", e2.getMessage());
                    responseForParaUpdate(6);
                    if (inputStream2 != null) {
                    }
                    synchronized (this.mLock) {
                    }
                } catch (XmlPullParserException e50) {
                    e3 = e50;
                    mAPKConfig = mAPKConfig2;
                    mGameConfig = mGameConfig2;
                    blackListConfig = blackListConfig2;
                    whiteListConfig = whiteListConfig2;
                    ouiBlackListConfig = ouiBlackListConfig2;
                    xmlPullParser = xmlPullParser;
                    powerParameterConfig = powerParameterConfig2;
                    inputStream2 = inputStream5;
                    HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 3:%{public}s", e3.getMessage());
                    responseForParaUpdate(6);
                    if (inputStream2 != null) {
                    }
                    synchronized (this.mLock) {
                    }
                } catch (Throwable th20) {
                    th = th20;
                    inputStream = inputStream5;
                    if (inputStream != null) {
                    }
                    throw th;
                }
            } catch (FileNotFoundException e51) {
                inputStream2 = inputStream3;
                e = e51;
                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 1:%{public}s", e.getMessage());
                responseForParaUpdate(0);
                if (inputStream2 != null) {
                }
                synchronized (this.mLock) {
                }
            } catch (IOException e52) {
                inputStream2 = inputStream3;
                e2 = e52;
                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 2:%{public}s", e2.getMessage());
                responseForParaUpdate(6);
                if (inputStream2 != null) {
                }
                synchronized (this.mLock) {
                }
            } catch (XmlPullParserException e53) {
                inputStream2 = inputStream3;
                e3 = e53;
                HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 3:%{public}s", e3.getMessage());
                responseForParaUpdate(6);
                if (inputStream2 != null) {
                }
                synchronized (this.mLock) {
                }
            } catch (Throwable th21) {
                inputStream = inputStream3;
                th = th21;
                if (inputStream != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e54) {
            inputStream2 = null;
            e = e54;
            HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 1:%{public}s", e.getMessage());
            responseForParaUpdate(0);
            if (inputStream2 != null) {
            }
            synchronized (this.mLock) {
            }
        } catch (IOException e55) {
            inputStream2 = null;
            e2 = e55;
            HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 2:%{public}s", e2.getMessage());
            responseForParaUpdate(6);
            if (inputStream2 != null) {
            }
            synchronized (this.mLock) {
            }
        } catch (XmlPullParserException e56) {
            inputStream2 = null;
            e3 = e56;
            HwAPPQoEUtils.logD(TAG, false, "readAppConfigList exception 3:%{public}s", e3.getMessage());
            responseForParaUpdate(6);
            if (inputStream2 != null) {
            }
            synchronized (this.mLock) {
            }
        } catch (Throwable th22) {
            inputStream = null;
            th = th22;
            if (inputStream != null) {
            }
            throw th;
        }
        synchronized (this.mLock) {
            this.isXmlLoadFinsh = true;
        }
        return;
        HwAPPQoEUtils.logE(TAG, false, "readAppConfigList failed by Exception", new Object[0]);
        synchronized (this.mLock) {
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public void fillAPKConfig(HwAPPQoEAPKConfig config, String elementName, String elementValue) {
        if (elementName == null || config == null) {
            HwAPPQoEUtils.logD(TAG, false, "fillAPKConfig, input error", new Object[0]);
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
                case '\b':
                    config.monitorUserLearning = Integer.parseInt(elementValue);
                    break;
                case '\t':
                    config.mAction = Integer.parseInt(elementValue);
                    break;
                case '\n':
                    config.mHistoryQoeBadTH = Float.parseFloat(elementValue);
                    break;
                case 11:
                    config.mGeneralStallTH = Integer.parseInt(elementValue);
                    break;
                case '\f':
                    config.mAggressiveStallTH = Integer.parseInt(elementValue);
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
                    HwAPPQoEUtils.logD(TAG, false, "fillAPKConfig, invalid element name:%{public}s", elementName);
                    break;
            }
        } catch (NumberFormatException e) {
            HwAPPQoEUtils.logE(TAG, false, "fillAPKConfig NumberFormatException name: %{public}s", elementName);
        }
        fillAppConfigByWifiPro(config, elementName, elementValue);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void fillAppConfigByWifiPro(HwAPPQoEAPKConfig config, String elementName, String elementValue) {
        if (elementName == null || config == null) {
            HwAPPQoEUtils.logD(TAG, false, "fillAPKConfig, input error", new Object[0]);
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
                    HwAPPQoEUtils.logD(TAG, false, "fillAPKConfig, invalid element name:%{public}s", elementName);
                    break;
            }
        } catch (NumberFormatException e) {
            HwAPPQoEUtils.logE(TAG, false, "fillAPKConfig NumberFormatException name: %{public}s", elementName);
        }
        fillAppConfigByWifiProForOta(config, elementName, elementValue);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void fillAppConfigByWifiProForOta(HwAPPQoEAPKConfig config, String elementName, String elementValue) {
        if (elementName == null || config == null) {
            HwAPPQoEUtils.logD(TAG, false, "fillAPKConfig, input error", new Object[0]);
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
                    HwAPPQoEUtils.logD(TAG, false, "fillAPKConfig, invalid element name:%{public}s", elementName);
                    return;
            }
        } catch (NumberFormatException e) {
            HwAPPQoEUtils.logE(TAG, false, "fillAPKConfig NumberFormatException name: %{public}s", elementName);
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public void fillGameConfig(HwAPPQoEGameConfig config, String elementName, String elementValue) {
        if (elementName == null || config == null) {
            HwAPPQoEUtils.logD(TAG, false, "fillGameConfig, input error", new Object[0]);
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
                    config.mScenceId = Integer.parseInt(elementValue);
                    return;
                case 2:
                    config.mGameKQI = Integer.parseInt(elementValue);
                    return;
                case 3:
                    config.mGameRtt = Integer.parseInt(elementValue);
                    return;
                case 4:
                    config.mGameAction = Integer.parseInt(elementValue);
                    return;
                case 5:
                    config.mHistoryQoeBadTH = Float.parseFloat(elementValue);
                    return;
                case 6:
                    config.setGameSpecialInfoSources(Integer.parseInt(elementValue));
                    return;
                case 7:
                    config.mReserved = elementValue;
                    return;
                default:
                    HwAPPQoEUtils.logD(TAG, false, "fillGameConfig, invalid element name: %{public}s", elementName);
                    return;
            }
        } catch (NumberFormatException e) {
            HwAPPQoEUtils.logE(TAG, false, "fillGameConfig NumberFormatException name: %{public}s", elementName);
        }
    }

    private void fillBlackListConfig(HwAppQoeBlackListConfig config, String elementName, String elementValue) {
        if (elementName == null || config == null) {
            HwAPPQoEUtils.logD(TAG, false, "fillBlackListConfig, input error", new Object[0]);
        } else {
            HwAPPQoEUtils.logD(TAG, false, "fillBlackListConfig, no feature id now", new Object[0]);
        }
    }

    private void fillWhiteListConfig(HwAppQoeWhiteListConfig config, String elementName, String elementValue) {
        if (elementName == null || config == null) {
            HwAPPQoEUtils.logD(TAG, false, "fillWhiteListConfig, input error", new Object[0]);
        } else {
            HwAPPQoEUtils.logD(TAG, false, "fillWhiteListConfig, no feature id now", new Object[0]);
        }
    }

    private void fillOuiBlackListConfig(HwAppQoeOuiBlackListConfig config, String elementName, String elementValue) {
        if (elementName == null || config == null) {
            HwAPPQoEUtils.logD(TAG, false, "fillOuiBlackListConfig, input error", new Object[0]);
            return;
        }
        char c = 65535;
        try {
            if (elementName.hashCode() == 1638328516 && elementName.equals("mFeatureId")) {
                c = 0;
            }
            if (c != 0) {
                HwAPPQoEUtils.logD(TAG, false, "fillOuiBlackListConfig, invalid element name: %{public}s", elementName);
            } else {
                config.setFeatureId(Integer.parseInt(elementValue));
            }
        } catch (NumberFormatException e) {
            HwAPPQoEUtils.logE(TAG, false, "fillOuiBlackListConfig NumberFormatException name: %{public}s", elementName);
        }
    }

    public void fillPowerParameterConfig(HwPowerParameterConfig config, String elementName, String elementValue) {
        if (elementName == null || config == null || TextUtils.isEmpty(elementValue)) {
            HwAPPQoEUtils.logE(TAG, false, "fillPowerParameterConfig, input error", new Object[0]);
            return;
        }
        char c = 65535;
        try {
            if (elementName.hashCode() == 81434961 && elementName.equals("VALUE")) {
                c = 0;
            }
            if (c != 0) {
                HwAPPQoEUtils.logD(TAG, false, "fillPowerParameterConfig, invalid element name: %{public}s", elementName);
            } else {
                config.setPowerParameterValue((double) Float.parseFloat(elementValue));
            }
        } catch (NumberFormatException e) {
            HwAPPQoEUtils.logE(TAG, false, "fillPowerParameterConfig NumberFormatException name: %{public}s", elementName);
        }
    }

    public void fillMpLinkConfig(HwMpLinkConfigInfo config, String elementName, String elementValue) {
        if (elementName == null || config == null) {
            HwAPPQoEUtils.logD(TAG, false, "fillGameConfig, input error", new Object[0]);
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
                config.setmCustMac(elementValue);
                return;
            case 1:
                config.setmAppName(elementValue);
                return;
            case 2:
                config.setmMultiNetwork(elementValue);
                return;
            case 3:
                config.setmGatewayType(elementValue);
                return;
            case 4:
                config.setmEncryptType(elementValue);
                return;
            case 5:
                config.setmReserved(elementValue);
                return;
            case 6:
                config.setCondition(elementValue);
                return;
            default:
                HwAPPQoEUtils.logD(TAG, false, "fillMpLinkConfig, invalid element name: %{public}s", elementName);
                return;
        }
    }

    public HwAPPQoEAPKConfig checkIsMonitorAPKScence(String packageName, String className) {
        HwAPPQoEAPKConfig config = null;
        synchronized (this.mLock) {
            if (this.isXmlLoadFinsh && this.mAPKConfigList.size() != 0) {
                if (packageName != null) {
                    HwAPPQoEUtils.logD(TAG, false, "checkIsMonitorAPKScence input :%{public}s,%{public}s", packageName, className);
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
        if (config != null) {
            HwAPPQoEUtils.logD(TAG, false, "checkIsMonitorAPKScence end:%{public}s", config.toString());
        }
        return config;
    }

    public HwAPPQoEAPKConfig checkIsMonitorVideoScence(String packageName, String className) {
        HwAPPQoEAPKConfig config = null;
        synchronized (this.mLock) {
            if (this.isXmlLoadFinsh && this.mAPKConfigList.size() != 0) {
                if (packageName != null) {
                    HwAPPQoEUtils.logD(TAG, false, "checkIsMonitorVideoScence input :%{public}s,%{public}s", packageName, className);
                    Iterator<HwAPPQoEAPKConfig> it = this.mAPKConfigList.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        HwAPPQoEAPKConfig apkConfig = it.next();
                        if (className != null && className.contains(apkConfig.className) && packageName.contains(apkConfig.packageName)) {
                            config = apkConfig;
                            break;
                        }
                    }
                }
            }
            return null;
        }
        if (config != null) {
            HwAPPQoEUtils.logD(TAG, false, "checkIsMonitorVideoScence end:%{public}s", config.toString());
        }
        return config;
    }

    public HwAPPQoEGameConfig checkIsMonitorGameScence(String packageName) {
        HwAPPQoEGameConfig config = null;
        synchronized (this.mLock) {
            if (this.isXmlLoadFinsh && this.mGameConfigList.size() != 0) {
                if (packageName != null) {
                    HwAPPQoEUtils.logD(TAG, false, "checkIsMonitorGameScence input :%{public}s", packageName);
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
        if (config != null) {
            HwAPPQoEUtils.logD(TAG, false, "checkIsMonitorGameScence end:%{public}s", config.toString());
        }
        return config;
    }

    public boolean isInBlackListScene(String packageName) {
        HwAppQoeBlackListConfig config = null;
        boolean isInBlackList = false;
        synchronized (this.mLock) {
            if (this.isXmlLoadFinsh && !this.mBlackListConfigList.isEmpty()) {
                if (packageName != null) {
                    HwAPPQoEUtils.logD(TAG, false, "checkIsMonitorBlackListScence input :%{public}s", packageName);
                    Iterator<HwAppQoeBlackListConfig> it = this.mBlackListConfigList.iterator();
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
            HwAPPQoEUtils.logD(TAG, false, "checkIsMonitorBlackListScence end:%{public}s", config.toString());
        }
        return isInBlackList;
    }

    public boolean isInWhiteListScene(String packageName) {
        HwAppQoeWhiteListConfig config = null;
        boolean isInWhiteList = false;
        synchronized (this.mLock) {
            if (this.isXmlLoadFinsh && !this.mWhiteListConfigList.isEmpty()) {
                if (packageName != null) {
                    HwAPPQoEUtils.logD(TAG, false, "checkIsMonitorWhiteListScence input :%{public}s", packageName);
                    Iterator<HwAppQoeWhiteListConfig> it = this.mWhiteListConfigList.iterator();
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
            HwAPPQoEUtils.logD(TAG, false, "checkIsMonitorWhiteListScence end:%{public}s", config.toString());
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
                HwAPPQoEUtils.logE(TAG, false, "parse ouiblacklist string fail", new Object[0]);
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
            if (this.isXmlLoadFinsh && !this.mOuiBlackListConfigList.isEmpty()) {
                if (ie.bytes != null) {
                    HwAPPQoEUtils.logD(TAG, false, "checkIsInOuiBlackList", new Object[0]);
                    Iterator<HwAppQoeOuiBlackListConfig> it = this.mOuiBlackListConfigList.iterator();
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
                                        HwAPPQoEUtils.logD(TAG, false, "checkOuiBlackList is :%{public}s", ouiBlackListConfig.getOuiName());
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
            if (this.isXmlLoadFinsh && !this.mPowerParameterConfigList.isEmpty()) {
                if (!TextUtils.isEmpty(powerParameter)) {
                    for (HwPowerParameterConfig powerParameterConfig : this.mPowerParameterConfigList) {
                        if (powerParameter.equals(powerParameterConfig.getPowerParameterName())) {
                            return powerParameterConfig;
                        }
                    }
                    HwAPPQoEUtils.logD(TAG, false, "getPowerParameterConfig, not found", new Object[0]);
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

    public HwAPPQoEGameConfig getGameScenceConfig(int appId) {
        synchronized (this.mLock) {
            if (this.isXmlLoadFinsh) {
                if (this.mGameConfigList.size() != 0) {
                    HwAPPQoEUtils.logD(TAG, false, "getGameScenceConfig input :%{public}d", Integer.valueOf(appId));
                    for (HwAPPQoEGameConfig gameConfig : this.mGameConfigList) {
                        if (appId == gameConfig.mGameId) {
                            HwAPPQoEUtils.logD(TAG, false, "getGameScenceConfig:%{public}s", gameConfig.toString());
                            return gameConfig;
                        }
                    }
                    HwAPPQoEUtils.logD(TAG, false, "getGameScenceConfig, not found", new Object[0]);
                    return null;
                }
            }
            return null;
        }
    }

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
            HwAPPQoEUtils.logD(TAG, false, "getScenceAction, action:%{public}d", Integer.valueOf(scenceAction));
            return scenceAction;
        }
    }

    public HwAPPQoEAPKConfig getAPKScenceConfig(int scenceId) {
        synchronized (this.mLock) {
            if (this.isXmlLoadFinsh) {
                if (this.mAPKConfigList.size() != 0) {
                    HwAPPQoEUtils.logD(TAG, false, "getAPKScenceConfig input :%{public}d", Integer.valueOf(scenceId));
                    for (HwAPPQoEAPKConfig apkConfig : this.mAPKConfigList) {
                        if (scenceId == apkConfig.mScenceId) {
                            HwAPPQoEUtils.logD(TAG, false, "getAPKScenceConfig:%{public}s", apkConfig.toString());
                            return apkConfig;
                        }
                    }
                    HwAPPQoEUtils.logD(TAG, false, "getAPKScenceConfig, not found", new Object[0]);
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
