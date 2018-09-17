package android.content.pm;

import android.content.pm.PackageParser.Activity;
import android.content.pm.PackageParser.PackageParserException;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwPackageParser implements IHwPackageParser {
    private static final String APP_NAME = "app";
    private static final String ATTR_NAME = "name";
    private static final String CUST_FILE_DIR = "system/etc";
    private static final String CUST_FILE_NAME = "benchmar_app.xml";
    private static final boolean FASTBOOT_UNLOCK = SystemProperties.getBoolean("ro.fastboot.unlock", false);
    private static final String FILE_FULLSCREEN_WHITELIST = "hw_fullscreen_apps.xml";
    private static final String FILE_POLICY_CLASS_NAME = "com.huawei.cust.HwCfgFilePolicy";
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
    private static final HashMap<String, Object> sAppMap = new HashMap();

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
                    try {
                        inputStream.close();
                        return;
                    } catch (IOException e2) {
                        Log.e(TAG, "loadFullScreeniWinWhiteList:- IOE while closing stream");
                        return;
                    }
                }
                return;
            } catch (XmlPullParserException e3) {
                Log.e(TAG, "parser fullscreen xml fail");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                        return;
                    } catch (IOException e4) {
                        Log.e(TAG, "loadFullScreeniWinWhiteList:- IOE while closing stream");
                        return;
                    }
                }
                return;
            } catch (IOException e5) {
                Log.e(TAG, "parser fullscreen IO fail");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                        return;
                    } catch (IOException e6) {
                        Log.e(TAG, "loadFullScreeniWinWhiteList:- IOE while closing stream");
                        return;
                    }
                }
                return;
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e7) {
                        Log.e(TAG, "loadFullScreeniWinWhiteList:- IOE while closing stream");
                    }
                }
            }
        }
        if (inputStream != null) {
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setInput(inputStream, null);
            for (int xmlEventType = xmlParser.next(); xmlEventType != 1; xmlEventType = xmlParser.next()) {
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
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e8) {
                Log.e(TAG, "loadFullScreeniWinWhiteList:- IOE while closing stream");
            }
        }
    }

    private static File getCfgFile(String fileName, int type) throws Exception, NoClassDefFoundError {
        Class<?> filePolicyClazz = Class.forName(FILE_POLICY_CLASS_NAME);
        return (File) filePolicyClazz.getMethod(METHOD_NAME_FOR_FILE, new Class[]{String.class, Integer.TYPE}).invoke(filePolicyClazz, new Object[]{fileName, Integer.valueOf(type)});
    }

    private static File getCustomizedFileName(String xmlName, int flag) {
        File file = null;
        try {
            return getCfgFile("xml/" + xmlName, flag);
        } catch (NoClassDefFoundError e) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            return file;
        } catch (Exception e2) {
            Log.d(TAG, "getCustomizedFileName get layout file exception");
            return file;
        }
    }

    public boolean isDefaultFullScreen(String pkgName) {
        if (mHwFullScreenAppList == null) {
            initFullScreenList();
        }
        return mHwFullScreenAppList.contains(pkgName.toLowerCase());
    }

    protected void HwPackageParser() {
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

    public void initMetaData(Activity a) {
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

    private boolean readBenchmarkAppFromXml(HashMap<String, Object> sMap, String fileDir, String fileName) {
        Throwable th;
        File mFile = new File(fileDir, fileName);
        InputStream inputStream = null;
        if (!mFile.exists()) {
            return false;
        }
        if (mFile.canRead()) {
            try {
                InputStream inputStream2 = new FileInputStream(mFile);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(inputStream2, null);
                    boolean parsingArray = false;
                    ArrayList<String> array = new ArrayList();
                    Object arrayName = null;
                    int i = 0;
                    while (true) {
                        int i2 = i;
                        i = i2 + 1;
                        if (i2 <= MAX_NUM) {
                            XmlUtils.nextElement(parser);
                            String element = parser.getName();
                            if (element == null) {
                                break;
                            }
                            if (parsingArray && (element.equals("value") ^ 1) != 0) {
                                sMap.put(arrayName, array.toArray(new String[array.size()]));
                                parsingArray = false;
                            }
                            if (element.equals(TAG_ARRAY)) {
                                parsingArray = true;
                                array.clear();
                                arrayName = parser.getAttributeValue(null, ATTR_NAME);
                            } else if (element.equals(TAG_ITEM) || element.equals("value")) {
                                Object name = null;
                                if (!parsingArray) {
                                    name = parser.getAttributeValue(null, ATTR_NAME);
                                }
                                if (parser.next() == 4) {
                                    String value = parser.getText();
                                    if (element.equals(TAG_ITEM)) {
                                        sMap.put(name, value);
                                    } else if (parsingArray) {
                                        array.add(value);
                                    } else {
                                        continue;
                                    }
                                } else {
                                    continue;
                                }
                            }
                        } else {
                            break;
                        }
                    }
                    if (parsingArray) {
                        sMap.put(arrayName, array.toArray(new String[array.size()]));
                    }
                    if (inputStream2 != null) {
                        try {
                            inputStream2.close();
                        } catch (IOException e) {
                            Log.w(TAG, "readBenchmarkAppFromXml  inputStream close IOException");
                        }
                    }
                    inputStream = inputStream2;
                } catch (XmlPullParserException e2) {
                    inputStream = inputStream2;
                } catch (IOException e3) {
                    inputStream = inputStream2;
                } catch (Throwable th2) {
                    th = th2;
                    inputStream = inputStream2;
                }
            } catch (XmlPullParserException e4) {
                try {
                    Log.w(TAG, "readBenchmarkAppFromXml  XmlPullParserException");
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e5) {
                            Log.w(TAG, "readBenchmarkAppFromXml  inputStream close IOException");
                        }
                    }
                    Log.w(TAG, fileDir + "/" + CUST_FILE_NAME + " be read ! ");
                    return true;
                } catch (Throwable th3) {
                    th = th3;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e6) {
                            Log.w(TAG, "readBenchmarkAppFromXml  inputStream close IOException");
                        }
                    }
                    throw th;
                }
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
            }
            Log.w(TAG, fileDir + "/" + CUST_FILE_NAME + " be read ! ");
            return true;
        }
        Log.w(TAG, "benchmar_app.xml not found! name maybe not right!");
        return false;
    }

    public void needStopApp(String packageName, File packageDir) throws PackageParserException {
        if (HIDE_PRODUCT_INFO || (FASTBOOT_UNLOCK ^ 1) == 0) {
            synchronized (mLock) {
                if (mBenchmarkApp == null) {
                    mBenchmarkApp = new HashSet();
                    readBenchmarkAppFromXml(sAppMap, CUST_FILE_DIR, CUST_FILE_NAME);
                    String[] BenchmarkApp = (String[]) sAppMap.get(APP_NAME);
                    if (BenchmarkApp != null) {
                        for (Object add : BenchmarkApp) {
                            mBenchmarkApp.add(add);
                        }
                    }
                }
                for (String appName : mBenchmarkApp) {
                    if (packageName.contains(appName)) {
                        throw new PackageParserException(-2, "Inconsistent package " + packageName + " in " + packageDir);
                    }
                }
            }
        }
    }
}
