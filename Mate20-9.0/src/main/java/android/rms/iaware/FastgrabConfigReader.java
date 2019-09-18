package android.rms.iaware;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import java.util.Map;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class FastgrabConfigReader {
    private static final String CONFIG_FILE_NAME = "/xengine.xml";
    private static final String CONFIG_RELATIVE_PATH = "/emcom/emcomctr";
    private static final String DEFAULT_FILE_PATH = "/system/emui/base/emcom/emcomctr/xengine.xml";
    private static final int INVALID_VALUE = -1;
    private static final String TAG = "FastgrabConfigReader";
    private static final String TOP_IM_CN_PROP = "persist.sys.iaware.topimcn";
    private static final String TOP_IM_CN_PROP_DEFAULT = "";
    private static final int VALID_VERSION_FORMAT_LENGTH = 2;
    private static final int VERSION_CODE_LENGTH = 7;
    private static final String WECHAT = "wechat";
    private static final String XML_ATTR_NAME = "name";
    private static final String XML_ATTR_SWITCH = "switch";
    private static final String XML_ATTR_VERSION = "version";
    private static final String XML_TAG_CONFIG = "config";
    private static final String XML_TAG_IAWARE = "iaware";
    private static final String XML_TAG_PRODUCT = "product_config";
    private static final String XML_TAG_REGION = "region_config";
    private static final String XML_TAG_XENGINE = "xengine";
    private static final String XML_VALUE_DEFAULT = "default";
    private static HashMap<String, String> mAppNameToNodeName = new HashMap<>();
    private static FastgrabConfigReader mFastgrabConfigReader = null;
    private HashMap<String, String> mAppConfig = new HashMap<>();
    private int mHighVersion;
    private int mLowVersion;
    private boolean mSpecificProduceConfigFound;
    private boolean mSpecificRegionConfigFound;
    private int mVersionCode;

    private FastgrabConfigReader() {
    }

    private FastgrabConfigReader(Context context) {
        String topImCN = SystemProperties.get(TOP_IM_CN_PROP, TOP_IM_CN_PROP_DEFAULT);
        if (!TOP_IM_CN_PROP_DEFAULT.equals(topImCN)) {
            mAppNameToNodeName.put(topImCN, WECHAT);
        }
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
        if (SystemProperties.getBoolean("persist.sys.enable_iaware", false) && processName != null) {
            this.mAppConfig.clear();
            PackageManager manager = context.getPackageManager();
            if (manager != null) {
                try {
                    PackageInfo info = manager.getPackageInfo(processName, 0);
                    if (info != null) {
                        this.mVersionCode = info.versionCode;
                        StringBuilder log = new StringBuilder();
                        log.append("process:" + processName + "\nversion:" + this.mVersionCode);
                        File file = new File(getConfigFilePath());
                        if (!file.exists()) {
                            AwareLog.e(TAG, "config file is not exist!");
                            return;
                        }
                        InputStream is = null;
                        XmlPullParser parser = null;
                        try {
                            is = new FileInputStream(file);
                            parser = Xml.newPullParser();
                            parser.setInput(is, StandardCharsets.UTF_8.name());
                            parseXEngineConfig(parser, processName, log);
                        } catch (XmlPullParserException e) {
                            AwareLog.e(TAG, "failed parsing switch file parser error");
                        } catch (IOException e2) {
                            AwareLog.e(TAG, "failed parsing switch file IO error ");
                        } catch (NumberFormatException e3) {
                            AwareLog.e(TAG, "switch number format error");
                        } catch (Throwable th) {
                            closeStream(is, parser);
                            throw th;
                        }
                        closeStream(is, parser);
                    }
                } catch (PackageManager.NameNotFoundException e4) {
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
        String[] cfgFileInfo = HwCfgFilePolicy.getDownloadCfgFile(CONFIG_RELATIVE_PATH, "/emcom/emcomctr/xengine.xml");
        if (cfgFileInfo == null) {
            AwareLog.e(TAG, "both default and cota config files not exist");
            return DEFAULT_FILE_PATH;
        } else if (cfgFileInfo[0] != null) {
            return cfgFileInfo[0];
        } else {
            return DEFAULT_FILE_PATH;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004a, code lost:
        if (r7.equals(XML_TAG_PRODUCT) != false) goto L_0x0062;
     */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x006a  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0073  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x007c  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0066 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0004 A[SYNTHETIC] */
    private void parseXEngineConfig(XmlPullParser parser, String processName, StringBuilder log) throws IOException, XmlPullParserException {
        int targetDepth = -1;
        while (true) {
            int next = parser.next();
            int type = next;
            if (next != 1) {
                int depth = parser.getDepth();
                int currentDepth = depth;
                if (depth >= targetDepth || type != 3) {
                    char c = 2;
                    if (targetDepth == -1 || (targetDepth == currentDepth && type == 2)) {
                        String name = parser.getName();
                        int hashCode = name.hashCode();
                        if (hashCode == -1766891795) {
                            if (name.equals(XML_TAG_REGION)) {
                                c = 1;
                                switch (c) {
                                    case 0:
                                        break;
                                    case 1:
                                        break;
                                    case 2:
                                        break;
                                    case 3:
                                        break;
                                }
                            }
                        } else if (hashCode == -1195682923) {
                            if (name.equals(XML_TAG_IAWARE)) {
                                c = 3;
                                switch (c) {
                                    case 0:
                                        break;
                                    case 1:
                                        break;
                                    case 2:
                                        break;
                                    case 3:
                                        break;
                                }
                            }
                        } else if (hashCode != -444786542) {
                            if (hashCode == 2122563770 && name.equals(XML_TAG_XENGINE)) {
                                c = 0;
                                switch (c) {
                                    case 0:
                                        if (!parseXEngine(parser, processName, log)) {
                                            break;
                                        } else {
                                            targetDepth = currentDepth + 1;
                                            break;
                                        }
                                    case 1:
                                        if (!parseRegion(parser, processName, log)) {
                                            break;
                                        } else {
                                            targetDepth = currentDepth + 1;
                                            break;
                                        }
                                    case 2:
                                        if (!parseProduct(parser, processName, log)) {
                                            break;
                                        } else {
                                            targetDepth = currentDepth + 2;
                                            break;
                                        }
                                    case 3:
                                        parseIaware(parser, processName, log);
                                        return;
                                }
                            }
                        }
                        c = 65535;
                        switch (c) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                        }
                    }
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
        String region = SystemProperties.get("ro.product.locale.region", TOP_IM_CN_PROP_DEFAULT);
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
        String product = SystemProperties.get("ro.product.model", TOP_IM_CN_PROP_DEFAULT);
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
            int next = parser.next();
            int type = next;
            if (next != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                if (type == 2) {
                    String name = parser.getName();
                    String tagName = name;
                    if (name != null && tagName.equals(mAppNameToNodeName.get(processName))) {
                        String switchValue = parser.getAttributeValue(null, XML_ATTR_SWITCH);
                        if (switchValue != null && "1".equals(switchValue)) {
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
            int next = parser.next();
            int type = next;
            if (next != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                if (XML_TAG_CONFIG.equals(parser.getName())) {
                    String supportVersion = parser.getAttributeValue(null, XML_ATTR_VERSION);
                    if (supportVersion == null) {
                        continue;
                    } else {
                        String[] versionStartAndEnd = supportVersion.split("-");
                        if (versionStartAndEnd.length == 2 && this.mVersionCode >= Integer.parseInt(versionStartAndEnd[0]) && this.mVersionCode <= Integer.parseInt(versionStartAndEnd[1])) {
                            parseApp(parser, tagName, log);
                            return;
                        }
                    }
                }
            }
        }
    }

    private void parseApp(XmlPullParser parser, String appName, StringBuilder log) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
            } else if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                String value = parser.nextText();
                if (!(tagName == null || value == null)) {
                    this.mAppConfig.put(tagName, strFormat(value));
                }
            }
        }
        for (Map.Entry<String, String> entry : this.mAppConfig.entrySet()) {
            log.append("\n");
            log.append(entry.getKey());
            log.append(":");
            log.append(entry.getValue());
        }
        AwareLog.i(TAG, "---------- parse complete ----------\n" + log.toString());
    }

    public int getInt(String tagName) {
        String str = this.mAppConfig.get(tagName);
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
        return this.mAppConfig.get(tagName);
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
                int n = 0;
                while (true) {
                    if (n >= 4) {
                        break;
                    }
                    char ch = charArray[m + n + 2];
                    if ((ch < '0' || ch > '9') && ((ch < 'A' || ch > 'F') && (ch < 'a' || ch > 'f'))) {
                        cc = 0;
                    } else {
                        cc = (char) ((Character.digit(ch, 16) << ((3 - n) * 4)) | cc);
                        n++;
                    }
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
