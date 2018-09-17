package com.android.server.emcom;

import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.android.server.emcom.grabservice.AutoGrabService;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SmartcareConfigSerializer {
    private static final String CONFIG_FILE_NAME = "/smartcare.xml";
    private static final String CONFIG_RELATIVE_PATH = "/emcom/emcomctr";
    private static final String DEFAULT_CONFIG_FILE = "/system/emui/base/emcom/emcomctr/smartcare.xml";
    private static final String TAG = "ConfigSerialier";
    private static final String TAG_APP_REPORT_ORDER = "app_report_order";
    private static final String TAG_MAX_APP_CNT = "max_report_apps";
    private static final String TAG_NAME = "name";
    private static final String TAG_PKG = "package";
    private static final String TAG_PKG_NAME = "package_name";
    private static String TAG_SMARTCARE_DEFAULT = MemoryConstant.MEM_SCENE_DEFAULT;
    private static String TAG_SMARTCARE_PRODUCT_CONFIG = "product_config";
    private static String TAG_SMARTCARE_REGION_CONFIG = "region_config";
    private static final String TAG_SMARTCARE_SWITCH = "smartcare_switch";
    private static String TAG_SMARTCARE_VERSION = "smartcare";
    private static final String TAG_SWITCH = "switch";
    private static final String TAG_TYPE = "type";
    private static final String TAG_VERSION = "version";
    private static final String TAG_VIDEO_INTBUFFER_FULL_DELAY_THRES = "video_intbuffer_full_delay_thres";
    private static final String TAG_VIDEO_SR_DELAY_THRES = "video_sr_delay_thres";
    private static final String TAG_VIDEO_STALLING_DURATION_THRES = "video_stalling_duration_thres";
    private static final String TAG_VIDEO_STALLING_TIMES_THRES = "video_stalling_times_thres";
    private static final Object TAG_WECHAT = SmartcareConstants.WEBCHAT_TYPE;
    private static volatile SmartcareConfigSerializer mInstance;
    protected String mConfigAppReportOrder = null;
    protected final HashMap<String, AppData> mConfigApplications = new HashMap();
    protected int mConfigMaxAppCnt = 0;
    private FeatureManager mFeatureManager;
    private String mLocalProductName;
    private String mLocalRegion;
    protected boolean mSmartcareSwitchOn = false;
    private String mSmartcareVersion;
    protected int mVideoIntBufferFullDelayThres = 0;
    protected int mVideoSRDelayThres = 0;
    protected int mVideoStallingDurationThres = 0;
    protected int mVideoStallingTimesThres = 0;
    private Map<String, String> mWechatConfigMap = new HashMap();

    public static class AppData {
        public String packageName;
        public boolean switchOn;
        public String type;

        public AppData(String packageName, String type, boolean switchOn) {
            this.packageName = packageName;
            this.type = type;
            this.switchOn = switchOn;
        }

        public String toString() {
            return "packageName=" + this.packageName + ",type=" + this.type + ",switchOn=" + this.switchOn;
        }
    }

    private SmartcareConfigSerializer() {
        readConfig();
    }

    public static synchronized SmartcareConfigSerializer getInstance() {
        SmartcareConfigSerializer smartcareConfigSerializer;
        synchronized (SmartcareConfigSerializer.class) {
            if (mInstance == null) {
                mInstance = new SmartcareConfigSerializer();
            }
            smartcareConfigSerializer = mInstance;
        }
        return smartcareConfigSerializer;
    }

    public void ensureConfigApplications() {
        if (this.mConfigApplications.isEmpty()) {
            readConfig();
            if (this.mConfigApplications.isEmpty()) {
                Log.w(TAG, "Nothong is read from config");
            }
        }
    }

    public int readConfigForCotaUpdate() {
        return readConfig();
    }

    public String getAppType(String pkgName) {
        String str = null;
        if (pkgName == null) {
            return null;
        }
        ensureConfigApplications();
        AppData appData = (AppData) this.mConfigApplications.get(pkgName);
        if (appData != null) {
            str = appData.type;
        }
        return str;
    }

    public boolean configedHasType(String type) {
        String[] configTypes = getConfigedTypes();
        if (configTypes != null && configTypes.length > 0) {
            for (String equals : configTypes) {
                if (equals.equals(type)) {
                    return true;
                }
            }
        }
        return false;
    }

    public HashMap<String, AppData> getConfigedApps() {
        return this.mConfigApplications;
    }

    public String[] getConfigedTypes() {
        if (this.mConfigAppReportOrder != null) {
            return this.mConfigAppReportOrder.split(",");
        }
        Log.w(TAG, "Null config types is found");
        return null;
    }

    public int getConfigedMaxReportSize() {
        return this.mConfigMaxAppCnt;
    }

    public int getConfigedVideoSRDelayThres() {
        return this.mVideoSRDelayThres;
    }

    public int getConfigedVideoIntBufferDelayThres() {
        return this.mVideoIntBufferFullDelayThres;
    }

    public int getConfigedStallingTimesThres() {
        return this.mVideoStallingTimesThres;
    }

    public int getConfigedStallingDurationThres() {
        return this.mVideoStallingDurationThres;
    }

    public boolean smartcareSwitchOn() {
        return this.mSmartcareSwitchOn;
    }

    private File getConfigPackagesFile() {
        String CONFIG_FILE = DEFAULT_CONFIG_FILE;
        String[] cfgFileInfo = HwCfgFilePolicy.getDownloadCfgFile(CONFIG_RELATIVE_PATH, "/emcom/emcomctr/smartcare.xml");
        if (cfgFileInfo == null) {
            Log.e(TAG, "Both default and cota config files not exist");
        } else {
            Log.d(TAG, "get config file path and version.");
            if (cfgFileInfo[0] != null) {
                CONFIG_FILE = cfgFileInfo[0];
            }
        }
        return new File(CONFIG_FILE);
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x00b8 A:{SYNTHETIC, Splitter: B:37:0x00b8} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x009d A:{SYNTHETIC, Splitter: B:28:0x009d} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0082 A:{SYNTHETIC, Splitter: B:19:0x0082} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00ca A:{SYNTHETIC, Splitter: B:44:0x00ca} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final int readConfig() {
        FileInputStream str = null;
        int parseResult = 6;
        try {
            this.mFeatureManager = FeatureManager.getInstance();
            this.mSmartcareSwitchOn = this.mFeatureManager.isFeatureEnable(2);
            Log.w(TAG, "in readConfi, smartcareSwitchOn = " + this.mSmartcareSwitchOn);
            this.mLocalRegion = SystemProperties.get("ro.product.locale.region", "");
            this.mLocalProductName = SystemProperties.get("ro.product.model", "");
            File configPackagesFile = getConfigPackagesFile();
            if (configPackagesFile.exists()) {
                FileInputStream str2 = new FileInputStream(configPackagesFile);
                try {
                    parseResult = parserConfigPackagesFile(str2);
                    notifyGrabService();
                    if (str2 != null) {
                        try {
                            str2.close();
                        } catch (IOException e) {
                            Log.e(TAG, "Unable to close the str");
                        }
                    }
                    return parseResult;
                } catch (FileNotFoundException e2) {
                    str = str2;
                    Log.e(TAG, "readConfig : FileNotFoundException");
                    if (str != null) {
                    }
                    return parseResult;
                } catch (XmlPullParserException e3) {
                    str = str2;
                    Log.e(TAG, "readConfig : XmlPullParserException");
                    if (str != null) {
                    }
                    return parseResult;
                } catch (IOException e4) {
                    str = str2;
                    try {
                        Log.e(TAG, "readConfig : IOException");
                        if (str != null) {
                        }
                        return parseResult;
                    } catch (Throwable th) {
                        if (str != null) {
                            try {
                                str.close();
                            } catch (IOException e5) {
                                Log.e(TAG, "Unable to close the str");
                            }
                        }
                        return parseResult;
                    }
                } catch (Throwable th2) {
                    str = str2;
                    if (str != null) {
                    }
                    return parseResult;
                }
            }
            Log.w(TAG, "Unable to find config file");
            return 6;
        } catch (FileNotFoundException e6) {
            Log.e(TAG, "readConfig : FileNotFoundException");
            if (str != null) {
                try {
                    str.close();
                } catch (IOException e7) {
                    Log.e(TAG, "Unable to close the str");
                }
            }
            return parseResult;
        } catch (XmlPullParserException e8) {
            Log.e(TAG, "readConfig : XmlPullParserException");
            if (str != null) {
                try {
                    str.close();
                } catch (IOException e9) {
                    Log.e(TAG, "Unable to close the str");
                }
            }
            return parseResult;
        } catch (IOException e10) {
            Log.e(TAG, "readConfig : IOException");
            if (str != null) {
                try {
                    str.close();
                } catch (IOException e11) {
                    Log.e(TAG, "Unable to close the str");
                }
            }
            return parseResult;
        }
    }

    private void notifyGrabService() {
        Handler handler = AutoGrabService.getHandler();
        if (handler == null) {
            Log.w(TAG, "AutoGrabService handler is null.");
        } else {
            Message.obtain(handler, 9).sendToTarget();
        }
    }

    private final void parserPackage(XmlPullParser parser, AppData appData) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        int outerDepth = parser.getDepth();
        while (eventType != 1) {
            if (eventType == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (eventType == 3 || eventType == 4) {
                eventType = parser.next();
            } else {
                if (parser.getName().equals("package_name")) {
                    if (appData != null) {
                        eventType = parser.next();
                        appData.packageName = parser.getText();
                        Log.w(TAG, "setPkg: " + appData.packageName);
                    }
                } else if (parser.getName().equals("type")) {
                    if (appData != null) {
                        eventType = parser.next();
                        appData.type = parser.getText();
                        Log.w(TAG, "settype: " + appData.type);
                        this.mConfigApplications.put(appData.packageName, appData);
                    }
                } else if (parser.getName().equals(TAG_SWITCH) && appData != null) {
                    eventType = parser.next();
                    String switchOn = parser.getText();
                    if (!TextUtils.isEmpty(switchOn)) {
                        boolean z;
                        if (Integer.parseInt(switchOn) != 0) {
                            z = true;
                        } else {
                            z = false;
                        }
                        appData.switchOn = z;
                    }
                    Log.w(TAG, "switchOn: " + appData.switchOn);
                }
                eventType = parser.next();
            }
        }
    }

    private final int parserConfigPackagesFile(FileInputStream str) throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(str, null);
        int eventType;
        do {
            eventType = parser.next();
            if (eventType == 2) {
                break;
            }
        } while (eventType != 1);
        return parserVersion(parser);
    }

    private int parserVersion(XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        int outerDepth = parser.getDepth();
        while (eventType != 1 && (eventType != 3 || parser.getDepth() > outerDepth)) {
            if (eventType == 2 && TAG_SMARTCARE_VERSION.equals(parser.getName())) {
                String smartcareVersion = parser.getAttributeValue(null, TAG_VERSION);
                if (!compareVersion(smartcareVersion)) {
                    return 5;
                }
                this.mSmartcareVersion = smartcareVersion;
                if (parserRegion(parser)) {
                    return 8;
                }
                return 6;
            }
            eventType = parser.next();
        }
        return 6;
    }

    private boolean parserRegion(XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        int outerDepth = parser.getDepth();
        boolean parseResult = false;
        boolean bSpecificRegionConfigFound = false;
        while (eventType != 1 && ((eventType != 3 || parser.getDepth() > outerDepth) && (bSpecificRegionConfigFound ^ 1) != 0)) {
            if (eventType == 2 && TAG_SMARTCARE_REGION_CONFIG.equals(parser.getName())) {
                String smartcareRegionName = parser.getAttributeValue(null, "name");
                if (!TextUtils.isEmpty(this.mLocalRegion) && this.mLocalRegion.equals(smartcareRegionName)) {
                    bSpecificRegionConfigFound = true;
                    clearConfig();
                    parseResult |= parserProduct(parser);
                } else if (TAG_SMARTCARE_DEFAULT.equals(smartcareRegionName)) {
                    parseResult |= parserProduct(parser);
                }
            }
            eventType = parser.next();
        }
        return parseResult;
    }

    private boolean parserProduct(XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        int outerDepth = parser.getDepth();
        boolean parseResult = false;
        boolean bSpecificProduceConfigFound = false;
        while (eventType != 1 && ((eventType != 3 || parser.getDepth() > outerDepth) && (bSpecificProduceConfigFound ^ 1) != 0)) {
            if (eventType == 2 && TAG_SMARTCARE_PRODUCT_CONFIG.equals(parser.getName())) {
                String smartcareProductName = parser.getAttributeValue(null, "name");
                if (this.mLocalProductName.contains(smartcareProductName)) {
                    bSpecificProduceConfigFound = true;
                    clearConfig();
                    parserConfig(parser);
                    parseResult = true;
                } else if (TAG_SMARTCARE_DEFAULT.equals(smartcareProductName)) {
                    parserConfig(parser);
                    parseResult = true;
                }
            }
            eventType = parser.next();
        }
        return parseResult;
    }

    private final void parserConfig(XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        int outerDepth = parser.getDepth();
        while (eventType != 1) {
            if (eventType != 3 || parser.getDepth() > outerDepth) {
                switch (eventType) {
                    case 2:
                        if (!parser.getName().equals("package")) {
                            if (!parser.getName().equals(TAG_APP_REPORT_ORDER)) {
                                if (!parser.getName().equals(TAG_MAX_APP_CNT)) {
                                    if (!parser.getName().equals(TAG_VIDEO_SR_DELAY_THRES)) {
                                        if (!parser.getName().equals(TAG_VIDEO_INTBUFFER_FULL_DELAY_THRES)) {
                                            if (!parser.getName().equals(TAG_VIDEO_STALLING_TIMES_THRES)) {
                                                if (!parser.getName().equals(TAG_VIDEO_STALLING_DURATION_THRES)) {
                                                    if (!parser.getName().equals(TAG_WECHAT)) {
                                                        break;
                                                    }
                                                    readWechat(parser);
                                                    break;
                                                }
                                                eventType = parser.next();
                                                String videoStallingDurationThres = parser.getText();
                                                if (!TextUtils.isEmpty(videoStallingDurationThres)) {
                                                    this.mVideoStallingDurationThres = Integer.parseInt(videoStallingDurationThres);
                                                }
                                                Log.d(TAG, "video stalling duration thres : " + this.mVideoStallingDurationThres);
                                                break;
                                            }
                                            eventType = parser.next();
                                            String videoStallingTimesThres = parser.getText();
                                            if (!TextUtils.isEmpty(videoStallingTimesThres)) {
                                                this.mVideoStallingTimesThres = Integer.parseInt(videoStallingTimesThres);
                                            }
                                            Log.d(TAG, "video stalling times thres: " + this.mVideoStallingTimesThres);
                                            break;
                                        }
                                        eventType = parser.next();
                                        String videoIntbufferDelayThres = parser.getText();
                                        if (!TextUtils.isEmpty(videoIntbufferDelayThres)) {
                                            this.mVideoIntBufferFullDelayThres = Integer.parseInt(videoIntbufferDelayThres);
                                        }
                                        Log.d(TAG, "video int buffer delay thres: " + this.mVideoIntBufferFullDelayThres);
                                        break;
                                    }
                                    eventType = parser.next();
                                    String videoSRDelayThres = parser.getText();
                                    if (!TextUtils.isEmpty(videoSRDelayThres)) {
                                        this.mVideoSRDelayThres = Integer.parseInt(videoSRDelayThres);
                                    }
                                    Log.d(TAG, "video sr delay thres: " + this.mVideoSRDelayThres);
                                    break;
                                }
                                eventType = parser.next();
                                String appCnt = parser.getText();
                                if (!TextUtils.isEmpty(appCnt)) {
                                    this.mConfigMaxAppCnt = Integer.parseInt(appCnt);
                                }
                                Log.d(TAG, "max app cnt: " + this.mConfigMaxAppCnt);
                                break;
                            }
                            eventType = parser.next();
                            this.mConfigAppReportOrder = parser.getText();
                            Log.d(TAG, "app report order: " + this.mConfigAppReportOrder);
                            break;
                        }
                        parserPackage(parser, new AppData());
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            } else {
                return;
            }
        }
    }

    private void readWechat(XmlPullParser parser) throws XmlPullParserException, IOException {
        String version = parser.getAttributeValue(null, TAG_VERSION);
        if (!TextUtils.isEmpty(version) && parser.next() == 4) {
            String config = parser.getText();
            if (!TextUtils.isEmpty(config)) {
                this.mWechatConfigMap.put(version, config);
            }
            parser.nextTag();
        }
    }

    public Map<String, String> getWechatConfigMap() {
        return this.mWechatConfigMap;
    }

    private void clearConfig() {
        this.mConfigApplications.clear();
        this.mConfigAppReportOrder = null;
        this.mConfigMaxAppCnt = 0;
        this.mVideoSRDelayThres = 0;
        this.mVideoIntBufferFullDelayThres = 0;
        this.mVideoStallingTimesThres = 0;
        this.mVideoStallingDurationThres = 0;
        this.mWechatConfigMap.clear();
    }

    private boolean compareVersion(String version) {
        boolean z = true;
        if (TextUtils.isEmpty(version)) {
            Log.e(TAG, "version code is empty!.");
            return false;
        } else if (TextUtils.isEmpty(this.mSmartcareVersion)) {
            this.mSmartcareVersion = version;
            return true;
        } else {
            if (version.compareTo(this.mSmartcareVersion) <= 0) {
                z = false;
            }
            return z;
        }
    }
}
