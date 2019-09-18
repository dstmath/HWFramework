package android.content.pm;

import android.content.pm.PackageParser;
import android.hardware.display.HwFoldScreenState;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
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

public class HwPackageParser implements IHwPackageParser {
    private static Map<String, Float> APP_ASPECTS = null;
    private static final String APP_NAME = "app";
    private static final String ATTR_NAME = "name";
    private static final String CUST_FILE_DIR = "system/etc";
    private static final String CUST_FILE_NAME = "benchmar_app.xml";
    private static final boolean FASTBOOT_UNLOCK = SystemProperties.getBoolean("ro.fastboot.unlock", false);
    private static final String FILE_FULLSCREEN_WHITELIST = "hw_fullscreen_apps.xml";
    private static final String FILE_POLICY_CLASS_NAME = "com.huawei.cust.HwCfgFilePolicy";
    private static final String FILE_TAHITI_APP_ASPECT = "hw_tahiti_app_aspect_list.xml";
    private static final boolean HIDE_PRODUCT_INFO = SystemProperties.getBoolean("ro.build.hide", false);
    private static final int MAX_NUM = 500;
    private static final String METHOD_NAME_FOR_FILE = "getCfgFile";
    private static final String TAG = "BENCHMAR_APP";
    private static final String TAG_ARRAY = "array";
    private static final String TAG_ARRAYITEM = "value";
    private static final String TAG_DEVICE = "device";
    private static final String TAG_ITEM = "item";
    private static final String XML_ATTRIBUTE_PACKAGE_NAME = "package_name";
    private static final String XML_ELEMENT_FULLSCREEN_APP_ITEM = "fullscreen_app";
    private static final String XML_ELEMENT_FULLSCREEN_APP_LIST = "fullscreen_whitelist";
    private static Set<String> mBenchmarkApp;
    private static List<String> mHwFullScreenAppList = null;
    private static HwPackageParser mInstance = null;
    private static final Object mInstanceLock = new Object();
    private static final Object mLock = new Object();
    private static final HashMap<String, Object> sAppMap = new HashMap<>();

    static {
        initFullScreenList();
        initBenchmarkList();
        initTahitiAppAspectList();
    }

    private static void initTahitiAppAspectList() {
        APP_ASPECTS = new HashMap();
        if (HwFoldScreenState.isFoldScreenDevice()) {
            File configFile = getCustomizedFileName(FILE_TAHITI_APP_ASPECT, 0);
            if (configFile == null || !configFile.exists()) {
                Log.i(TAG, "hw_tahiti_app_aspect_list.xml does not exists.");
                return;
            }
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(configFile);
                XmlPullParser xmlParser = Xml.newPullParser();
                xmlParser.setInput(inputStream, null);
                while (true) {
                    int next = xmlParser.next();
                    int xmlEventType = next;
                    if (next == 1) {
                        try {
                            break;
                        } catch (IOException e) {
                            Log.e(TAG, "initTahitiAppAspectList:- IOE while closing stream", e);
                        }
                    } else if (xmlEventType == 2 && "package".equals(xmlParser.getName())) {
                        String packageName = xmlParser.getAttributeValue(null, ATTR_NAME);
                        if (!TextUtils.isEmpty(packageName)) {
                            String aspectStr = xmlParser.getAttributeValue(null, "defaultAspect");
                            float defaultAspect = HwFoldScreenState.getScreenFoldFullRatio();
                            Log.d("Tahiti", "packageName: " + packageName + ", defaultAspect: " + defaultAspect);
                            if (!TextUtils.isEmpty(aspectStr)) {
                                try {
                                    String[] aspectStrs = aspectStr.split(":");
                                    float defaultAspect2 = Float.valueOf(aspectStrs[0]).floatValue() / Float.valueOf(aspectStrs[1]).floatValue();
                                    try {
                                        if (Float.isInfinite(defaultAspect2)) {
                                            defaultAspect = HwFoldScreenState.getScreenFoldFullRatio();
                                        } else {
                                            defaultAspect = defaultAspect2;
                                        }
                                    } catch (Exception e2) {
                                        defaultAspect = defaultAspect2;
                                        Log.e(TAG, "wrong aspect " + aspectStr + " for " + packageName);
                                        APP_ASPECTS.put(packageName, Float.valueOf(defaultAspect));
                                    }
                                } catch (Exception e3) {
                                    Log.e(TAG, "wrong aspect " + aspectStr + " for " + packageName);
                                    APP_ASPECTS.put(packageName, Float.valueOf(defaultAspect));
                                }
                            }
                            APP_ASPECTS.put(packageName, Float.valueOf(defaultAspect));
                        }
                    }
                }
                inputStream.close();
            } catch (FileNotFoundException e4) {
                Log.e(TAG, "initCloneAppsFromCust, FileNotFoundException");
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (XmlPullParserException e5) {
                Log.e(TAG, "initCloneAppsFromCust", e5);
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e6) {
                Log.e(TAG, "initCloneAppsFromCust", e6);
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e7) {
                        Log.e(TAG, "initTahitiAppAspectList:- IOE while closing stream", e7);
                    }
                }
                throw th;
            }
        }
    }

    public Float getDefaultAspect(String pkgName) {
        if (APP_ASPECTS == null) {
            initTahitiAppAspectList();
        }
        return APP_ASPECTS.get(pkgName);
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
                if (inputStream != null) {
                    inputStream.close();
                    return;
                }
                return;
            } catch (XmlPullParserException e2) {
                Log.e(TAG, "parser fullscreen xml fail");
                if (inputStream != null) {
                    inputStream.close();
                    return;
                }
                return;
            } catch (IOException e3) {
                Log.e(TAG, "parser fullscreen IO fail");
                if (inputStream != null) {
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
                if (inputStream != null) {
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
        return (File) filePolicyClazz.getMethod(METHOD_NAME_FOR_FILE, new Class[]{String.class, Integer.TYPE}).invoke(filePolicyClazz, new Object[]{fileName, Integer.valueOf(type)});
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

    public void initMetaData(PackageParser.Activity a) {
        String navigationHide = a.metaData.getString("hwc-navi");
        if (navigationHide == null) {
            return;
        }
        if (navigationHide.startsWith("ro.config")) {
            a.info.navigationHide = SystemProperties.getBoolean(navigationHide, false);
            return;
        }
        a.info.navigationHide = true;
    }

    private static boolean readBenchmarkAppFromXml(HashMap<String, Object> sMap, String fileDir, String fileName) {
        File mFile = new File(fileDir, fileName);
        InputStream inputStream = null;
        int i = 0;
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
                while (true) {
                    int i2 = i + 1;
                    if (i > MAX_NUM) {
                        break;
                    }
                    XmlUtils.nextElement(parser);
                    String element = parser.getName();
                    if (element == null) {
                        break;
                    }
                    if (parsingArray) {
                        if (!element.equals("value")) {
                            sMap.put(arrayName, array.toArray(new String[array.size()]));
                            parsingArray = false;
                        }
                    }
                    if (element.equals(TAG_ARRAY)) {
                        parsingArray = true;
                        array.clear();
                        arrayName = parser.getAttributeValue(null, ATTR_NAME);
                    } else if (element.equals(TAG_ITEM) || element.equals("value")) {
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
                }
                if (parsingArray) {
                    sMap.put(arrayName, array.toArray(new String[array.size()]));
                }
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.w(TAG, "readBenchmarkAppFromXml  inputStream close IOException");
                }
            } catch (XmlPullParserException e2) {
                Log.w(TAG, "readBenchmarkAppFromXml  XmlPullParserException");
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e3) {
                Log.w(TAG, "readBenchmarkAppFromXml  IOException");
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e4) {
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
        boolean result = false;
        String mspesConfig = SystemProperties.get("ro.mspes.config", null);
        long mspesConfigValue = 0;
        if (!TextUtils.isEmpty(mspesConfig)) {
            try {
                mspesConfigValue = Long.decode(mspesConfig.trim()).longValue();
            } catch (NumberFormatException e) {
                Log.w(TAG, " ro.mspes.config  is not a number");
            }
            if ((((long) 1) & mspesConfigValue) != 0) {
                result = true;
            }
        } else if (HIDE_PRODUCT_INFO || FASTBOOT_UNLOCK) {
            return true;
        }
        return result;
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
                    for (String add : BenchmarkApp) {
                        mBenchmarkApp.add(add);
                    }
                }
            }
        }
        return true;
    }

    public void needStopApp(String packageName, File packageDir) throws PackageParser.PackageParserException {
        if (initBenchmarkList()) {
            synchronized (mLock) {
                for (String appName : mBenchmarkApp) {
                    if (packageName.contains(appName)) {
                        throw new PackageParser.PackageParserException(-2, "Inconsistent package " + packageName + " in " + packageDir);
                    }
                }
            }
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
}
