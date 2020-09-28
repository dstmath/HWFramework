package android.content.pm;

import android.content.AbsIntentFilter;
import android.content.pm.PackageParserEx;
import android.content.res.XmlResourceParser;
import android.hardware.display.HwFoldScreenState;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.huawei.android.internal.util.XmlUtilsEx;
import com.huawei.android.os.SystemPropertiesEx;
import huawei.android.hwutil.HwFullScreenDisplay;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwPackageParser extends DefaultHwPackageParser {
    private static Map<String, Float> APP_ASPECTS = null;
    private static final String APP_NAME = "app";
    private static Map<String, Integer> APP_VERSIONCODES = null;
    private static final String ATTR_NAME = "name";
    private static final int CONFIG_NO_PACKAGE = 0;
    private static final int CONFIG_NO_RESTART = 1;
    private static final int CONFIG_RESTART_FALSE = 3;
    private static final int CONFIG_RESTART_OTHERS = 4;
    private static final int CONFIG_RESTART_TRUE = 2;
    private static final String CUST_FILE_DIR = "system/etc";
    private static final String CUST_FILE_NAME = "benchmar_app.xml";
    private static final boolean FASTBOOT_UNLOCK = SystemPropertiesEx.getBoolean("ro.fastboot.unlock", false);
    private static final String FILE_FULLSCREEN_WHITELIST = "hw_fullscreen_apps.xml";
    private static final String FILE_POLICY_CLASS_NAME = "com.huawei.cust.HwCfgFilePolicy";
    public static final String FILE_TAHITI_APP_ASPECT = "hw_tahiti_app_aspect_list.xml";
    private static final boolean HIDE_PRODUCT_INFO = SystemPropertiesEx.getBoolean("ro.build.hide", false);
    public static final String HOT_FILE_DIR = "/data/cota/para/xml/HwExtDisplay/fold";
    private static final int INITIAL_CAPACITY = 1200;
    private static final int INVALID_VALUE = -1;
    private static final int MAX_NUM = 500;
    private static final String METHOD_NAME_FOR_FILE = "getCfgFile";
    private static final String PRESET_FILE_DIR = "/hw_product/etc/xml/HwExtDisplay/fold";
    private static final String TAG = "BENCHMAR_APP";
    private static final String TAG_ARRAY = "array";
    private static final String TAG_ARRAYITEM = "value";
    private static final String TAG_DEVICE = "device";
    private static final String TAG_ITEM = "item";
    private static final int TYPE_CONFIG_SWITCH = 1;
    private static final int TYPE_RESTART_ATTRIBUTE = 2;
    private static final String XML_ATTRIBUTE_PACKAGE_NAME = "package_name";
    private static final String XML_ELEMENT_FULLSCREEN_APP_ITEM = "fullscreen_app";
    private static final String XML_ELEMENT_FULLSCREEN_APP_LIST = "fullscreen_whitelist";
    private static boolean isNeedBootUpdate = false;
    private static Set<String> mBenchmarkApp;
    private static List<String> mHwFullScreenAppList = null;
    private static volatile HwPackageParser mInstance = null;
    private static final Object mInstanceLock = new Object();
    private static final Object mLock = new Object();
    private static final HashMap<String, Object> sAppMap = new HashMap<>();
    private static Map<String, Integer> sAppRestarts = null;
    private static int sConfigSwitch = -1;

    static {
        initFullScreenList();
        initBenchmarkList();
        initTahitiAppAspectList();
    }

    public static Map<String, Float> getTahitiAppAspectList() {
        return APP_ASPECTS;
    }

    public static Map<String, Integer> getTahitiAppVersionCodeList() {
        return APP_VERSIONCODES;
    }

    public static boolean getIsNeedBootUpdate() {
        return isNeedBootUpdate;
    }

    public static void initTahitiAppAspectList() {
        APP_ASPECTS = new HashMap(1200);
        APP_VERSIONCODES = new HashMap(10);
        sAppRestarts = new HashMap(1200);
        sConfigSwitch = -1;
        if (HwFoldScreenState.isFoldScreenDevice()) {
            File bakFile = new File(new File(Environment.getDataDirectory(), "system"), FILE_TAHITI_APP_ASPECT);
            File hotFile = new File(HOT_FILE_DIR, FILE_TAHITI_APP_ASPECT);
            File preFile = new File(PRESET_FILE_DIR, FILE_TAHITI_APP_ASPECT);
            File newerfile = findNewerFile(bakFile, hotFile);
            if (newerfile != null && newerfile.equals(hotFile)) {
                isNeedBootUpdate = true;
                Log.i(TAG, "hot file need boot update");
            }
            File file = findNewerFile(bakFile, preFile);
            Log.i(TAG, "ready to parse " + file);
            if (file == null || !file.exists()) {
                Log.i(TAG, file + " does not exists.");
                return;
            }
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
                XmlPullParser xmlParser = Xml.newPullParser();
                xmlParser.setInput(inputStream, null);
                setAppDefaultAspect(xmlParser);
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "initTahitiAppAspectList:- IOE while closing stream");
                }
            } catch (FileNotFoundException e2) {
                Log.e(TAG, "initTahitiAppAspectList, FileNotFoundException");
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (XmlPullParserException e3) {
                Log.e(TAG, "initTahitiAppAspectList XmlPullParserException");
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e4) {
                Log.e(TAG, "initTahitiAppAspectList IOException");
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e5) {
                        Log.e(TAG, "initTahitiAppAspectList:- IOE while closing stream");
                    }
                }
                throw th;
            }
        }
    }

    private static void setAppDefaultAspect(XmlPullParser xmlParser) throws XmlPullParserException, IOException {
        int xmlEventType = xmlParser.next();
        while (xmlEventType != 1) {
            if (xmlEventType != 2) {
                xmlEventType = xmlParser.next();
            } else {
                String elementName = xmlParser.getName();
                if ("package".equals(elementName)) {
                    String packageName = xmlParser.getAttributeValue(null, ATTR_NAME);
                    if (TextUtils.isEmpty(packageName)) {
                        xmlEventType = xmlParser.next();
                    } else {
                        String aspectStr = xmlParser.getAttributeValue(null, "defaultAspect");
                        String versionCodeStr = xmlParser.getAttributeValue(null, "versionCode");
                        processRestart(packageName, xmlParser.getAttributeValue(null, "restart"));
                        if (!TextUtils.isEmpty(aspectStr)) {
                            try {
                                String[] aspectStrs = aspectStr.split(":");
                                float defaultAspect = Float.valueOf(aspectStrs[0]).floatValue() / Float.valueOf(aspectStrs[1]).floatValue();
                                if (Float.isInfinite(defaultAspect)) {
                                    defaultAspect = HwFoldScreenState.getScreenFoldFullRatio();
                                }
                                Log.i(TAG, "packageName: " + packageName + ", defaultAspect: " + defaultAspect);
                                APP_ASPECTS.put(packageName, Float.valueOf(defaultAspect));
                                if (!TextUtils.isEmpty(versionCodeStr) && Integer.parseInt(versionCodeStr) != 0) {
                                    APP_VERSIONCODES.put(packageName, Integer.valueOf(Integer.parseInt(versionCodeStr)));
                                }
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "wrong aspect: " + aspectStr + " or wrong versionCode: " + versionCodeStr + " for " + packageName);
                            } catch (ArithmeticException e2) {
                                Log.e(TAG, "wrong aspect: " + aspectStr + " for " + packageName);
                            }
                        }
                    }
                }
                if ("config_switch".equals(elementName)) {
                    xmlEventType = xmlParser.next();
                    if (xmlEventType == 4) {
                        processConfigSwitch(xmlParser.getText());
                    }
                }
                xmlEventType = xmlParser.next();
            }
        }
    }

    private static void processConfigSwitch(String configSwitch) {
        if (!TextUtils.isEmpty(configSwitch)) {
            try {
                sConfigSwitch = Integer.parseInt(configSwitch);
            } catch (NumberFormatException e) {
                Log.e(TAG, "wrong config_switch: " + configSwitch);
            }
        }
    }

    private static void processRestart(String packageName, String restart) {
        int restartCode;
        if (!TextUtils.isEmpty(packageName)) {
            if (restart == null) {
                restartCode = 1;
            } else if ("true".equals(restart)) {
                restartCode = 2;
            } else if ("false".equals(restart)) {
                restartCode = 3;
            } else {
                restartCode = 4;
            }
            sAppRestarts.put(packageName, Integer.valueOf(restartCode));
        }
    }

    private static File findNewerFile(File fileA, File fileB) {
        if (fileA == null || !fileA.exists()) {
            if (fileB.exists()) {
                return fileB;
            }
            return null;
        } else if (fileB == null || !fileB.exists()) {
            if (fileA.exists()) {
                return fileA;
            }
            return null;
        } else if (fileA.lastModified() < fileB.lastModified()) {
            return fileB;
        } else {
            return fileA;
        }
    }

    public Float getDefaultAspect(String pkgName, int versionCode) {
        int versionCodeXml;
        if (APP_ASPECTS == null) {
            initTahitiAppAspectList();
        }
        Map<String, Integer> map = APP_VERSIONCODES;
        if (map == null || !map.containsKey(pkgName) || (versionCodeXml = APP_VERSIONCODES.get(pkgName).intValue()) == 0 || versionCodeXml > versionCode) {
            return APP_ASPECTS.get(pkgName);
        }
        return null;
    }

    private static void initFullScreenList() {
        mHwFullScreenAppList = new ArrayList();
        File configFile = getCustomizedFileName(FILE_FULLSCREEN_WHITELIST, 0);
        InputStream inputStream = null;
        if (configFile != null) {
            try {
                if (configFile.exists()) {
                    inputStream = new FileInputStream(configFile);
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "fullscreen xml not found");
                if (0 != 0) {
                    inputStream.close();
                    return;
                }
                return;
            } catch (XmlPullParserException e2) {
                Log.e(TAG, "parser fullscreen xml fail");
                if (0 != 0) {
                    inputStream.close();
                    return;
                }
                return;
            } catch (IOException e3) {
                Log.e(TAG, "parser fullscreen IO fail");
                if (0 != 0) {
                    try {
                        inputStream.close();
                        return;
                    } catch (IOException e4) {
                        Log.e(TAG, "loadFullScreeniWinWhiteList:- IOE while closing stream");
                        return;
                    }
                } else {
                    return;
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        inputStream.close();
                    } catch (IOException e5) {
                        Log.e(TAG, "loadFullScreeniWinWhiteList:- IOE while closing stream");
                    }
                }
                throw th;
            }
        }
        if (inputStream != null) {
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setInput(inputStream, null);
            int xmlEventType = xmlParser.next();
            while (true) {
                if (xmlEventType == 1) {
                    break;
                }
                if (xmlEventType != 2) {
                    if (xmlEventType == 3 && XML_ELEMENT_FULLSCREEN_APP_LIST.equals(xmlParser.getName())) {
                        break;
                    }
                } else if (XML_ELEMENT_FULLSCREEN_APP_ITEM.equals(xmlParser.getName())) {
                    String packageName = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_PACKAGE_NAME);
                    if (packageName != null) {
                        packageName = packageName.toLowerCase();
                    }
                    mHwFullScreenAppList.add(packageName);
                }
                xmlEventType = xmlParser.next();
            }
        }
        if (inputStream != null) {
            inputStream.close();
        }
    }

    private static File getCfgFile(String fileName, int type) throws Exception, NoClassDefFoundError {
        Class<?> filePolicyClazz = Class.forName(FILE_POLICY_CLASS_NAME);
        return (File) filePolicyClazz.getMethod(METHOD_NAME_FOR_FILE, String.class, Integer.TYPE).invoke(filePolicyClazz, fileName, Integer.valueOf(type));
    }

    private static File getCustomizedFileName(String xmlName, int flag) {
        try {
            return getCfgFile("xml/" + xmlName, flag);
        } catch (NoClassDefFoundError e) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            return null;
        } catch (Exception e2) {
            Log.d(TAG, "getCustomizedFileName get layout file exception");
            return null;
        }
    }

    public boolean isDefaultFullScreen(String pkgName) {
        if (mHwFullScreenAppList == null) {
            initFullScreenList();
        }
        return mHwFullScreenAppList.contains(pkgName.toLowerCase());
    }

    /* access modifiers changed from: protected */
    public void HwPackageParser() {
    }

    public static HwPackageParser getDefault() {
        if (mInstance == null) {
            synchronized (mInstanceLock) {
                if (mInstance == null) {
                    mInstance = new HwPackageParser();
                }
            }
        }
        return mInstance;
    }

    public void initMetaData(PackageParserEx.ActivityEx a) {
        String navigationHide = a.getMetaData().getString("hwc-navi");
        if (navigationHide == null) {
            return;
        }
        if (navigationHide.startsWith("ro.config")) {
            a.setNavigationHide(SystemPropertiesEx.getBoolean(navigationHide, false));
        } else {
            a.setNavigationHide(true);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:64:0x00f1 A[SYNTHETIC, Splitter:B:64:0x00f1] */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0107  */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x012f A[SYNTHETIC, Splitter:B:78:0x012f] */
    private static boolean readBenchmarkAppFromXml(HashMap<String, Object> sMap, String fileDir, String fileName) {
        Throwable th;
        File mFile = new File(fileDir, fileName);
        InputStream inputStream = null;
        if (!mFile.exists()) {
            return false;
        }
        if (mFile.canRead()) {
            try {
                inputStream = new FileInputStream(mFile);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(inputStream, null);
                boolean parsingArray = false;
                ArrayList<String> array = new ArrayList<>();
                String arrayName = null;
                int i = 0;
                while (true) {
                    int i2 = i + 1;
                    if (i > 500) {
                        break;
                    }
                    XmlUtilsEx.nextElement(parser);
                    String element = parser.getName();
                    if (element == null) {
                        break;
                    }
                    if (parsingArray && !element.equals(TAG_ARRAYITEM)) {
                        sMap.put(arrayName, array.toArray(new String[array.size()]));
                        parsingArray = false;
                    }
                    if (element.equals(TAG_ARRAY)) {
                        try {
                            array.clear();
                            arrayName = parser.getAttributeValue(null, ATTR_NAME);
                            parsingArray = true;
                        } catch (XmlPullParserException e) {
                            Log.w(TAG, "readBenchmarkAppFromXml  XmlPullParserException");
                            if (inputStream != null) {
                            }
                            Log.w(TAG, fileDir + "/" + CUST_FILE_NAME + " be read ! ");
                            return true;
                        } catch (IOException e2) {
                            try {
                                Log.w(TAG, "readBenchmarkAppFromXml  IOException");
                                if (inputStream != null) {
                                }
                                Log.w(TAG, fileDir + "/" + CUST_FILE_NAME + " be read ! ");
                                return true;
                            } catch (Throwable th2) {
                                th = th2;
                            }
                        }
                    } else if (element.equals(TAG_ITEM) || element.equals(TAG_ARRAYITEM)) {
                        String name = null;
                        if (!parsingArray) {
                            name = parser.getAttributeValue(null, ATTR_NAME);
                        }
                        if (parser.next() == 4) {
                            String value = parser.getText();
                            if (element.equals(TAG_ITEM)) {
                                sMap.put(name, value);
                            } else if (parsingArray) {
                                array.add(value);
                            }
                        }
                    }
                    i = i2;
                    mFile = mFile;
                }
                if (parsingArray) {
                    try {
                        sMap.put(arrayName, array.toArray(new String[array.size()]));
                    } catch (XmlPullParserException e3) {
                    } catch (IOException e4) {
                        Log.w(TAG, "readBenchmarkAppFromXml  IOException");
                        if (inputStream != null) {
                        }
                        Log.w(TAG, fileDir + "/" + CUST_FILE_NAME + " be read ! ");
                        return true;
                    } catch (Throwable th3) {
                        th = th3;
                        if (inputStream != null) {
                        }
                        throw th;
                    }
                }
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    Log.w(TAG, "readBenchmarkAppFromXml  inputStream close IOException");
                }
            } catch (XmlPullParserException e6) {
                Log.w(TAG, "readBenchmarkAppFromXml  XmlPullParserException");
                if (inputStream != null) {
                    inputStream.close();
                }
                Log.w(TAG, fileDir + "/" + CUST_FILE_NAME + " be read ! ");
                return true;
            } catch (IOException e7) {
                Log.w(TAG, "readBenchmarkAppFromXml  IOException");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e8) {
                        Log.w(TAG, "readBenchmarkAppFromXml  inputStream close IOException");
                    }
                }
                Log.w(TAG, fileDir + "/" + CUST_FILE_NAME + " be read ! ");
                return true;
            } catch (Throwable th4) {
                th = th4;
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e9) {
                        Log.w(TAG, "readBenchmarkAppFromXml  inputStream close IOException");
                    }
                }
                throw th;
            }
            Log.w(TAG, fileDir + "/" + CUST_FILE_NAME + " be read ! ");
            return true;
        }
        Log.w(TAG, "benchmar_app.xml not found! name maybe not right!");
        return false;
    }

    private static boolean isForbiddenBenchmarkAppInstall() {
        String mspesConfig = SystemPropertiesEx.get("ro.mspes.config", (String) null);
        long mspesConfigValue = 0;
        if (!TextUtils.isEmpty(mspesConfig)) {
            try {
                mspesConfigValue = Long.decode(mspesConfig.trim()).longValue();
            } catch (NumberFormatException e) {
                Log.w(TAG, "ro.mspes.config is not a number");
            }
            if ((((long) 1) & mspesConfigValue) != 0) {
                return true;
            }
            return false;
        } else if (HIDE_PRODUCT_INFO || FASTBOOT_UNLOCK) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean initBenchmarkList() {
        if (!isForbiddenBenchmarkAppInstall()) {
            return false;
        }
        synchronized (mLock) {
            if (mBenchmarkApp == null) {
                mBenchmarkApp = new HashSet();
                readBenchmarkAppFromXml(sAppMap, CUST_FILE_DIR, CUST_FILE_NAME);
                String[] BenchmarkApp = (String[]) sAppMap.get(APP_NAME);
                if (BenchmarkApp != null) {
                    for (String str : BenchmarkApp) {
                        mBenchmarkApp.add(str);
                    }
                }
            }
        }
        return true;
    }

    public boolean needStopApp(String packageName, File packageDir) {
        if (!initBenchmarkList()) {
            return false;
        }
        synchronized (mLock) {
            for (String appName : mBenchmarkApp) {
                if (packageName.contains(appName)) {
                    Log.w(TAG, "need stop install app, package: " + packageName + " in " + packageDir);
                    return true;
                }
            }
            return false;
        }
    }

    public float getDefaultNonFullMaxRatio() {
        return HwFullScreenDisplay.getDefaultNonFullMaxRatio();
    }

    public float getDeviceMaxRatio() {
        return HwFullScreenDisplay.getDeviceMaxRatio();
    }

    public float getExclusionNavBarMaxRatio() {
        return HwFullScreenDisplay.getExclusionNavBarMaxRatio();
    }

    public boolean isFullScreenDevice() {
        return HwFullScreenDisplay.isFullScreenDevice();
    }

    public void parseIntentFilterState(XmlResourceParser parser, String android_resources, AbsIntentFilter outInfo) {
        String name = parser.getAttributeValue(android_resources, ATTR_NAME);
        if (name == null) {
            Log.w(TAG, "No value supplied for <android:name>");
            return;
        }
        String value = parser.getAttributeValue(android_resources, TAG_ARRAYITEM);
        if (value == null) {
            Log.w(TAG, "No value supplied for <android:value>");
            return;
        }
        String[] items = name.split("@");
        int i = 2;
        if (items.length != 2) {
            Log.w(TAG, "state name error");
            return;
        }
        char c = 0;
        String action = items[0];
        if (!items[1].equals("ImplicitBroadcastExpand")) {
            Log.w(TAG, "state flag error");
            return;
        }
        String[] filters = value.split("\\|");
        int i2 = 0;
        while (i2 < filters.length) {
            String[] state = filters[i2].split("=");
            if (state.length != i) {
                Log.w(TAG, "value format error");
                return;
            }
            outInfo.addActionFilter(action, state[c], state[1]);
            i2++;
            c = 0;
            i = 2;
        }
    }

    public void updateBaseProvider(PackageParserEx.PackageEx owner, ProviderInfo info, int splitIndex) {
        if (!(owner == null || owner.splitNames == null || owner.providers == null || owner.providers.size() == 0 || info == null || splitIndex < 0 || splitIndex >= owner.splitNames.length)) {
            for (int i = owner.providers.size() - 1; i >= 0; i--) {
                PackageParserEx.ProviderEx tempP = (PackageParserEx.ProviderEx) owner.providers.get(i);
                if (tempP != null && tempP.getProviderInfo().authority != null && TextUtils.equals(tempP.getProviderInfo().authority, info.authority)) {
                    tempP.getProviderInfo().splitName = owner.splitNames[splitIndex];
                    return;
                }
            }
        }
    }

    public int getDisplayChangeAppRestartConfig(int type, String pkgName) {
        if (type == 1) {
            return getDisplayChangeAppRestartSwitch();
        }
        if (type != 2) {
            return -1;
        }
        return getDisplayChangeAppRestart(pkgName);
    }

    private int getDisplayChangeAppRestartSwitch() {
        return sConfigSwitch;
    }

    private int getDisplayChangeAppRestart(String pkgName) {
        if (pkgName == null) {
            return -1;
        }
        if (sAppRestarts == null) {
            initTahitiAppAspectList();
        }
        if (sAppRestarts.containsKey(pkgName)) {
            return sAppRestarts.get(pkgName).intValue();
        }
        return 0;
    }
}
