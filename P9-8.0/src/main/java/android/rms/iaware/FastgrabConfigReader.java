package android.rms.iaware;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.FreezeScreenScene;
import android.os.Process;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Xml;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map.Entry;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class FastgrabConfigReader {
    private static final String CONFIG_FILE_NAME = "/xengine.xml";
    private static final String CONFIG_RELATIVE_PATH = "/emcom/emcomctr";
    private static final String DEFAULT_FILE_PATH = "/system/emui/base/emcom/emcomctr/xengine.xml";
    private static final int INVALID_VALUE = -1;
    private static final String TAG = "FastgrabConfigReader";
    private static final int VALID_VERSION_FORMAT_LENGTH = 2;
    private static final int VERSION_CODE_LENGTH = 7;
    private static final String XML_ATTR_NAME = "name";
    private static final String XML_ATTR_SWITCH = "switch";
    private static final String XML_ATTR_VERSION = "version";
    private static final String XML_TAG_CONFIG = "config";
    private static final String XML_TAG_IAWARE = "iaware";
    private static final String XML_TAG_PRODUCT = "product_config";
    private static final String XML_TAG_REGION = "region_config";
    private static final String XML_TAG_XENGINE = "xengine";
    private static final String XML_VALUE_DEFAULT = "default";
    private static final HashMap<String, String> mAppNameToNodeName = new HashMap<String, String>() {
        {
            put("com.tencent.mm", "wechat");
            put("com.eg.android.AlipayGphone", "alipay");
        }
    };
    private static FastgrabConfigReader mFastgrabConfigReader = null;
    private HashMap<String, String> mAppConfig = new HashMap();
    private int mHighVersion;
    private int mLowVersion;
    private boolean mSpecificProduceConfigFound;
    private boolean mSpecificRegionConfigFound;
    private int mVersionCode;

    private FastgrabConfigReader() {
    }

    private FastgrabConfigReader(Context context) {
        parseFile(getProcessName(), context);
    }

    public static synchronized FastgrabConfigReader getInstance(Context context) {
        FastgrabConfigReader fastgrabConfigReader;
        synchronized (FastgrabConfigReader.class) {
            if (mFastgrabConfigReader == null && context != null) {
                mFastgrabConfigReader = new FastgrabConfigReader(context);
            }
            fastgrabConfigReader = mFastgrabConfigReader;
        }
        return fastgrabConfigReader;
    }

    private String getProcessName() {
        String processName = Process.getCmdlineForPid(Process.myPid());
        if (mAppNameToNodeName.containsKey(processName)) {
            return processName;
        }
        return null;
    }

    private void parseFile(String processName, Context context) {
        Throwable th;
        if (SystemProperties.getBoolean("persist.sys.enable_iaware", false) && processName != null) {
            this.mAppConfig.clear();
            PackageManager manager = context.getPackageManager();
            if (manager != null) {
                try {
                    PackageInfo info = manager.getPackageInfo(processName, 0);
                    if (info != null) {
                        this.mVersionCode = info.versionCode;
                        StringBuilder log = new StringBuilder();
                        log.append("process:").append(processName).append("\nversion:").append(this.mVersionCode);
                        File file = new File(getConfigFilePath());
                        if (file.exists()) {
                            InputStream is = null;
                            XmlPullParser xmlPullParser = null;
                            try {
                                InputStream is2 = new FileInputStream(file);
                                try {
                                    xmlPullParser = Xml.newPullParser();
                                    xmlPullParser.setInput(is2, StandardCharsets.UTF_8.name());
                                    parseXEngineConfig(xmlPullParser, processName, log);
                                    closeStream(is2, xmlPullParser);
                                    is = is2;
                                } catch (XmlPullParserException e) {
                                    is = is2;
                                    AwareLog.e(TAG, "failed parsing switch file parser error");
                                    closeStream(is, xmlPullParser);
                                    return;
                                } catch (IOException e2) {
                                    is = is2;
                                    AwareLog.e(TAG, "failed parsing switch file IO error ");
                                    closeStream(is, xmlPullParser);
                                    return;
                                } catch (NumberFormatException e3) {
                                    is = is2;
                                    try {
                                        AwareLog.e(TAG, "switch number format error");
                                        closeStream(is, xmlPullParser);
                                        return;
                                    } catch (Throwable th2) {
                                        th = th2;
                                        closeStream(is, xmlPullParser);
                                        throw th;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    is = is2;
                                    closeStream(is, xmlPullParser);
                                    throw th;
                                }
                            } catch (XmlPullParserException e4) {
                                AwareLog.e(TAG, "failed parsing switch file parser error");
                                closeStream(is, xmlPullParser);
                                return;
                            } catch (IOException e5) {
                                AwareLog.e(TAG, "failed parsing switch file IO error ");
                                closeStream(is, xmlPullParser);
                                return;
                            } catch (NumberFormatException e6) {
                                AwareLog.e(TAG, "switch number format error");
                                closeStream(is, xmlPullParser);
                                return;
                            }
                            return;
                        }
                        AwareLog.e(TAG, "config file is not exist!");
                    }
                } catch (NameNotFoundException e7) {
                    AwareLog.e(TAG, "parse version failed ! appName = " + processName);
                }
            }
        }
    }

    private void closeStream(InputStream is, XmlPullParser parser) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                AwareLog.e(TAG, "close file input stream fail!");
            }
        }
        if (parser != null) {
            try {
                ((KXmlParser) parser).close();
            } catch (IOException e2) {
                AwareLog.e(TAG, "parser close error");
            }
        }
    }

    private String getConfigFilePath() {
        String path = DEFAULT_FILE_PATH;
        String[] cfgFileInfo = HwCfgFilePolicy.getDownloadCfgFile(CONFIG_RELATIVE_PATH, "/emcom/emcomctr/xengine.xml");
        if (cfgFileInfo == null) {
            AwareLog.e(TAG, "both default and cota config files not exist");
            return path;
        } else if (cfgFileInfo[0] != null) {
            return cfgFileInfo[0];
        } else {
            return path;
        }
    }

    private void parseXEngineConfig(XmlPullParser parser, String processName, StringBuilder log) throws IOException, XmlPullParserException {
        int targetDepth = -1;
        while (true) {
            int type = parser.next();
            if (type == 1) {
                break;
            }
            int currentDepth = parser.getDepth();
            if (currentDepth < targetDepth && type == 3) {
                break;
            } else if (targetDepth == -1 || (targetDepth == currentDepth && type == 2)) {
                String name = parser.getName();
                if (name.equals(XML_TAG_XENGINE)) {
                    if (parseXEngine(parser, processName, log)) {
                        targetDepth = currentDepth + 1;
                    }
                } else if (name.equals(XML_TAG_REGION)) {
                    if (parseRegion(parser, processName, log)) {
                        targetDepth = currentDepth + 1;
                    }
                } else if (name.equals(XML_TAG_PRODUCT)) {
                    if (parseProduct(parser, processName, log)) {
                        targetDepth = currentDepth + 2;
                    }
                } else if (name.equals(XML_TAG_IAWARE)) {
                    parseIaware(parser, processName, log);
                    return;
                }
            }
        }
    }

    private boolean parseXEngine(XmlPullParser parser, String processName, StringBuilder log) throws IOException, XmlPullParserException {
        String versionCode = parser.getAttributeValue(null, XML_ATTR_VERSION);
        if (TextUtils.isEmpty(versionCode)) {
            AwareLog.e(TAG, "version code is empty.");
            return false;
        } else if (versionCode.length() != 7) {
            AwareLog.e(TAG, "verison code length = " + versionCode.length() + " is not correct.");
            return false;
        } else {
            try {
                int high = Integer.parseInt(versionCode.substring(0, 3));
                int low = Integer.parseInt(versionCode.substring(4));
                if (high > this.mHighVersion) {
                    this.mHighVersion = high;
                    this.mLowVersion = low;
                    return true;
                } else if (high != this.mHighVersion || low <= this.mLowVersion) {
                    AwareLog.e(TAG, "verison code is lower than current version.");
                    return false;
                } else {
                    this.mLowVersion = low;
                    return true;
                }
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "version format is not correct.");
                return false;
            }
        }
    }

    private boolean parseRegion(XmlPullParser parser, String processName, StringBuilder log) throws IOException, XmlPullParserException {
        this.mSpecificProduceConfigFound = false;
        String name = parser.getAttributeValue(null, XML_ATTR_NAME);
        String region = SystemProperties.get("ro.product.locale.region", "");
        if (!TextUtils.isEmpty(name)) {
            if (!this.mSpecificRegionConfigFound && XML_VALUE_DEFAULT.equals(name)) {
                AwareLog.i(TAG, " read default region config.");
                return true;
            } else if (!TextUtils.isEmpty(region) && region.equals(name)) {
                AwareLog.i(TAG, "read current region config.");
                this.mSpecificRegionConfigFound = true;
                return true;
            }
        }
        return false;
    }

    private boolean parseProduct(XmlPullParser parser, String processName, StringBuilder log) throws IOException, XmlPullParserException {
        String name = parser.getAttributeValue(null, XML_ATTR_NAME);
        String product = SystemProperties.get("ro.product.model", "");
        if (!TextUtils.isEmpty(name)) {
            if (!this.mSpecificProduceConfigFound && XML_VALUE_DEFAULT.equals(name)) {
                AwareLog.i(TAG, " read default product config.");
                return true;
            } else if (!TextUtils.isEmpty(product) && product.startsWith(name)) {
                AwareLog.i(TAG, "read current product config.");
                this.mSpecificProduceConfigFound = true;
                return true;
            }
        }
        return false;
    }

    private void parseIaware(XmlPullParser parser, String processName, StringBuilder log) throws XmlPullParserException, IOException, NumberFormatException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                if (type == 2) {
                    String tagName = parser.getName();
                    if (tagName != null && tagName.equals(mAppNameToNodeName.get(processName))) {
                        String switchValue = parser.getAttributeValue(null, XML_ATTR_SWITCH);
                        if (switchValue != null && FreezeScreenScene.HUNG_CONFIG_ENABLE.equals(switchValue)) {
                            parseVersion(parser, tagName, log);
                            this.mAppConfig.put(XML_ATTR_SWITCH, switchValue);
                        }
                        return;
                    }
                }
            }
        }
    }

    private void parseVersion(XmlPullParser parser, String tagName, StringBuilder log) throws XmlPullParserException, IOException, NumberFormatException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                if (XML_TAG_CONFIG.equals(parser.getName())) {
                    String supportVersion = parser.getAttributeValue(null, XML_ATTR_VERSION);
                    if (supportVersion != null) {
                        String[] versionStartAndEnd = supportVersion.split("-");
                        if (versionStartAndEnd.length == 2 && this.mVersionCode >= Integer.parseInt(versionStartAndEnd[0]) && this.mVersionCode <= Integer.parseInt(versionStartAndEnd[1])) {
                            parseApp(parser, tagName, log);
                            return;
                        }
                    }
                    continue;
                }
            }
        }
    }

    private void parseApp(XmlPullParser parser, String appName, StringBuilder log) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
            } else if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                String value = parser.nextText();
                if (!(tagName == null || value == null)) {
                    this.mAppConfig.put(tagName, strFormat(value));
                }
            }
        }
        for (Entry<String, String> entry : this.mAppConfig.entrySet()) {
            log.append("\n").append((String) entry.getKey()).append(":").append((String) entry.getValue());
        }
        AwareLog.i(TAG, "---------- parse complete ----------\n" + log.toString());
    }

    public int getInt(String tagName) {
        String str = (String) this.mAppConfig.get(tagName);
        if (str != null) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "failed convert " + str + " to integer, format error!");
            }
        }
        return -1;
    }

    public String getString(String tagName) {
        return (String) this.mAppConfig.get(tagName);
    }

    private String strFormat(String rawStr) {
        if (rawStr == null) {
            return null;
        }
        char[] charArray = rawStr.toCharArray();
        int size = charArray.length;
        StringBuilder sb = new StringBuilder(rawStr.length());
        int m = 0;
        while (m < size) {
            if ('\\' == charArray[m] && m <= size - 6 && 'u' == charArray[m + 1]) {
                char cc = 0;
                for (int n = 0; n < 4; n++) {
                    char ch = charArray[(m + n) + 2];
                    if ((ch < '0' || ch > '9') && ((ch < 'A' || ch > 'F') && (ch < 'a' || ch > 'f'))) {
                        cc = 0;
                        break;
                    }
                    cc = (char) ((Character.digit(ch, 16) << ((3 - n) * 4)) | cc);
                }
                if (cc > 0) {
                    sb.append(cc);
                    m += 5;
                    m++;
                }
            }
            sb.append(charArray[m]);
            m++;
        }
        return sb.toString();
    }
}
