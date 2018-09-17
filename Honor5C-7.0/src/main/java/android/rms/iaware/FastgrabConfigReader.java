package android.rms.iaware;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Process;
import android.os.SystemProperties;
import android.util.Xml;
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
    private static String CONFIG_FILEPATH = null;
    private static String CONFIG_UPDATE_FILEPATH = null;
    public static final int INVALID_VALUE = -1;
    private static final String TAG = "FastgrabConfigReader";
    private static final int VALID_VERSION_FORMAT_LENGTH = 2;
    private static final String XML_ATTR_SWITCH = "switch";
    private static final String XML_ATTR_VERSION = "version";
    private static final String XML_TAG_CONFIG = "config";
    private static final String XML_TAG_IAWARE = "iaware";
    private static final HashMap<String, String> mAppNameToNodeName = null;
    private static FastgrabConfigReader mFastgrabConfigReader;
    private HashMap<String, String> mAppConfig;
    private int mVersionCode;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.rms.iaware.FastgrabConfigReader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.rms.iaware.FastgrabConfigReader.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.rms.iaware.FastgrabConfigReader.<clinit>():void");
    }

    private FastgrabConfigReader() {
        this.mAppConfig = new HashMap();
    }

    private FastgrabConfigReader(Context context) {
        this.mAppConfig = new HashMap();
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void parseFile(String processName, Context context) {
        Throwable th;
        if (SystemProperties.getBoolean("persist.sys.enable_iaware", false) && processName != null) {
            this.mAppConfig.clear();
            try {
                PackageInfo info = context.getPackageManager().getPackageInfo(processName, 0);
                if (info != null) {
                    this.mVersionCode = info.versionCode;
                    StringBuilder log = new StringBuilder();
                    StringBuilder append = log.append("process:");
                    r16.append(processName).append("\nversion:").append(this.mVersionCode);
                    File file = new File(CONFIG_UPDATE_FILEPATH);
                    if (!file.exists()) {
                        file = new File(CONFIG_FILEPATH);
                        if (!file.exists()) {
                            AwareLog.e(TAG, "config file is not exist!");
                            return;
                        }
                    }
                    InputStream inputStream = null;
                    try {
                        InputStream is = new FileInputStream(file);
                        try {
                            XmlPullParser parser = Xml.newPullParser();
                            parser.setInput(is, StandardCharsets.UTF_8.name());
                            int outerDepth = parser.getDepth();
                            while (true) {
                                int type = parser.next();
                                if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                                    closeStream(is, parser);
                                    inputStream = is;
                                } else if (type == VALID_VERSION_FORMAT_LENGTH && XML_TAG_IAWARE.equals(parser.getName())) {
                                    parseIaware(parser, processName, log);
                                    closeStream(is, parser);
                                    return;
                                }
                            }
                            closeStream(is, parser);
                            inputStream = is;
                        } catch (XmlPullParserException e) {
                            inputStream = is;
                            AwareLog.e(TAG, "failed parsing switch file parser error");
                            closeStream(inputStream, null);
                        } catch (IOException e2) {
                            inputStream = is;
                            AwareLog.e(TAG, "failed parsing switch file IO error ");
                            closeStream(inputStream, null);
                        } catch (NumberFormatException e3) {
                            inputStream = is;
                            try {
                                AwareLog.e(TAG, "switch number format error");
                                closeStream(inputStream, null);
                            } catch (Throwable th2) {
                                th = th2;
                                closeStream(inputStream, null);
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            inputStream = is;
                            closeStream(inputStream, null);
                            throw th;
                        }
                    } catch (XmlPullParserException e4) {
                        AwareLog.e(TAG, "failed parsing switch file parser error");
                        closeStream(inputStream, null);
                    } catch (IOException e5) {
                        AwareLog.e(TAG, "failed parsing switch file IO error ");
                        closeStream(inputStream, null);
                    } catch (NumberFormatException e6) {
                        AwareLog.e(TAG, "switch number format error");
                        closeStream(inputStream, null);
                    }
                }
            } catch (NameNotFoundException e7) {
                AwareLog.e(TAG, "parse version failed ! appName = " + processName);
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void parseIaware(XmlPullParser parser, String processName, StringBuilder log) throws XmlPullParserException, IOException, NumberFormatException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                String tagName = parser.getName();
                if (type == VALID_VERSION_FORMAT_LENGTH && tagName != null && tagName.equals(mAppNameToNodeName.get(processName))) {
                    break;
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void parseVersion(XmlPullParser parser, String tagName, StringBuilder log) throws XmlPullParserException, IOException, NumberFormatException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                if (XML_TAG_CONFIG.equals(parser.getName())) {
                    String supportVersion = parser.getAttributeValue(null, XML_ATTR_VERSION);
                    if (supportVersion != null) {
                        String[] versionStartAndEnd = supportVersion.split("-");
                        if (versionStartAndEnd.length == VALID_VERSION_FORMAT_LENGTH && this.mVersionCode >= Integer.parseInt(versionStartAndEnd[0]) && this.mVersionCode <= Integer.parseInt(versionStartAndEnd[1])) {
                            parseApp(parser, tagName, log);
                            return;
                        }
                    }
                    continue;
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        return INVALID_VALUE;
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
                char cc = '\u0000';
                for (int n = 0; n < 4; n++) {
                    char ch = charArray[(m + n) + VALID_VERSION_FORMAT_LENGTH];
                    if ((ch < '0' || ch > '9') && ((ch < 'A' || ch > 'F') && (ch < 'a' || ch > 'f'))) {
                        cc = '\u0000';
                        break;
                    }
                    cc = (char) ((Character.digit(ch, 16) << ((3 - n) * 4)) | cc);
                }
                if (cc > '\u0000') {
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
