package huawei.android.widget;

import android.app.ActivityThread;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Xml;
import android.widget.IHwWechatOptimize;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwWechatOptimizeImpl implements IHwWechatOptimize {
    private static final String ATTR_NAME = "name";
    private static String CONFIG_FILEPATH = null;
    private static final boolean Debug = true;
    private static String SWITCH_FILEPATH = null;
    private static final String TAG = "HwWechatOptimizeImpl";
    private static final String TEXT_NAME = "AppList";
    private static final String XML_TAG_APPNAME = "packageName";
    private static final String XML_TAG_CONFIG = "config";
    private static final String XML_TAG_FLINGVELOCITY = "flingVelocity";
    private static final String XML_TAG_IDLEVELOCITY = "idleVelocity";
    private static final String XML_TAG_ITEM = "item";
    private static final String XML_TAG_SWITCH = "switch";
    private static final String XML_TAG_VERSION = "supportVersion";
    private static HwWechatOptimizeImpl mHwWechatOptimizeImpl;
    private AppData mAppData;
    private String mCurrentPackageName;
    private boolean mIsEffect;
    private boolean mIsFling;

    private static class AppData {
        public String mAppName;
        public int mFlingVelocity;
        public int mIdleVelocity;
        public String mSupportVersion;

        public AppData(String name, String supportVersion, int flingVelocity, int idleVelocity) {
            this.mAppName = name;
            this.mSupportVersion = supportVersion;
            this.mFlingVelocity = flingVelocity;
            this.mIdleVelocity = idleVelocity;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.widget.HwWechatOptimizeImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.widget.HwWechatOptimizeImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.widget.HwWechatOptimizeImpl.<clinit>():void");
    }

    public static synchronized HwWechatOptimizeImpl getInstance() {
        HwWechatOptimizeImpl hwWechatOptimizeImpl;
        synchronized (HwWechatOptimizeImpl.class) {
            if (mHwWechatOptimizeImpl == null) {
                mHwWechatOptimizeImpl = new HwWechatOptimizeImpl();
            }
            hwWechatOptimizeImpl = mHwWechatOptimizeImpl;
        }
        return hwWechatOptimizeImpl;
    }

    private HwWechatOptimizeImpl() {
        this.mIsFling = false;
        this.mIsEffect = false;
        this.mAppData = null;
        this.mCurrentPackageName = null;
        if (SystemProperties.getBoolean("persist.sys.enable_iaware", false)) {
            if (isSwitchEnabled()) {
                this.mCurrentPackageName = ActivityThread.currentPackageName();
                if (this.mCurrentPackageName != null && !this.mCurrentPackageName.isEmpty()) {
                    if (loadConfigFile()) {
                        this.mIsEffect = Debug;
                    }
                } else {
                    return;
                }
            }
            Log.d(TAG, "mIsEffect:" + this.mIsEffect);
        }
    }

    private File getFile(String fileName) {
        return new File(fileName);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isSwitchEnabled() {
        Throwable th;
        File file = getFile(SWITCH_FILEPATH);
        if (!file.exists()) {
            return false;
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
                        if (is == null) {
                            try {
                                is.close();
                            } catch (IOException e) {
                                Log.e(TAG, "close file input stream fail!");
                            }
                        }
                    } else if (!(type == 3 || type == 4)) {
                        if (XML_TAG_SWITCH.equals(parser.getName())) {
                            break;
                        }
                    }
                }
                if (is == null) {
                } else {
                    is.close();
                }
                if (parser != null) {
                    try {
                        ((KXmlParser) parser).close();
                    } catch (Exception e2) {
                        Log.e(TAG, "parser close error");
                    }
                }
                return false;
            } catch (XmlPullParserException e3) {
                inputStream = is;
                Log.e(TAG, "failed parsing switch file parser error");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e4) {
                        Log.e(TAG, "close file input stream fail!");
                    }
                }
                if (null != null) {
                    try {
                        ((KXmlParser) null).close();
                    } catch (Exception e5) {
                        Log.e(TAG, "parser close error");
                    }
                }
                return false;
            } catch (IOException e6) {
                inputStream = is;
                Log.e(TAG, "failed parsing switch file IO error ");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e7) {
                        Log.e(TAG, "close file input stream fail!");
                    }
                }
                if (null != null) {
                    try {
                        ((KXmlParser) null).close();
                    } catch (Exception e8) {
                        Log.e(TAG, "parser close error");
                    }
                }
                return false;
            } catch (NumberFormatException e9) {
                inputStream = is;
                try {
                    Log.e(TAG, "switch number format error");
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e10) {
                            Log.e(TAG, "close file input stream fail!");
                        }
                    }
                    if (null != null) {
                        try {
                            ((KXmlParser) null).close();
                        } catch (Exception e11) {
                            Log.e(TAG, "parser close error");
                        }
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e12) {
                            Log.e(TAG, "close file input stream fail!");
                        }
                    }
                    if (null != null) {
                        try {
                            ((KXmlParser) null).close();
                        } catch (Exception e13) {
                            Log.e(TAG, "parser close error");
                        }
                    }
                    throw th;
                }
            } catch (Throwable th22) {
                th = th22;
                inputStream = is;
                if (inputStream != null) {
                    inputStream.close();
                }
                if (null != null) {
                    ((KXmlParser) null).close();
                }
                throw th;
            }
        } catch (XmlPullParserException e14) {
            Log.e(TAG, "failed parsing switch file parser error");
            if (inputStream != null) {
                inputStream.close();
            }
            if (null != null) {
                ((KXmlParser) null).close();
            }
            return false;
        } catch (IOException e15) {
            Log.e(TAG, "failed parsing switch file IO error ");
            if (inputStream != null) {
                inputStream.close();
            }
            if (null != null) {
                ((KXmlParser) null).close();
            }
            return false;
        } catch (NumberFormatException e16) {
            Log.e(TAG, "switch number format error");
            if (inputStream != null) {
                inputStream.close();
            }
            if (null != null) {
                ((KXmlParser) null).close();
            }
            return false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean loadConfigFile() {
        Throwable th;
        File file = getFile(CONFIG_FILEPATH);
        if (!file.exists()) {
            return false;
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
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e) {
                                Log.e(TAG, "close file input stream fail!");
                            }
                        }
                    } else if (!(type == 3 || type == 4)) {
                        if (XML_TAG_CONFIG.equals(parser.getName())) {
                            if (TEXT_NAME.equals(parser.getAttributeValue(null, ATTR_NAME))) {
                                break;
                            }
                        } else {
                            continue;
                        }
                    }
                }
                boolean appOptimized = false;
                if (checkAppListFromXml(parser)) {
                    appOptimized = Debug;
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e2) {
                        Log.e(TAG, "close file input stream fail!");
                    }
                }
                if (parser != null) {
                    try {
                        ((KXmlParser) parser).close();
                    } catch (Exception e3) {
                        Log.e(TAG, "parser close error");
                    }
                }
                return appOptimized;
            } catch (XmlPullParserException e4) {
                inputStream = is;
                Log.e(TAG, "failed parsing config file parser error");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e5) {
                        Log.e(TAG, "close file input stream fail!");
                    }
                }
                if (null != null) {
                    try {
                        ((KXmlParser) null).close();
                    } catch (Exception e6) {
                        Log.e(TAG, "parser close error");
                    }
                }
                return false;
            } catch (IOException e7) {
                inputStream = is;
                Log.e(TAG, "failed parsing config file IO error ");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e8) {
                        Log.e(TAG, "close file input stream fail!");
                    }
                }
                if (null != null) {
                    try {
                        ((KXmlParser) null).close();
                    } catch (Exception e9) {
                        Log.e(TAG, "parser close error");
                    }
                }
                return false;
            } catch (NumberFormatException e10) {
                inputStream = is;
                try {
                    Log.e(TAG, "config number format error");
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e11) {
                            Log.e(TAG, "close file input stream fail!");
                        }
                    }
                    if (null != null) {
                        try {
                            ((KXmlParser) null).close();
                        } catch (Exception e12) {
                            Log.e(TAG, "parser close error");
                        }
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e13) {
                            Log.e(TAG, "close file input stream fail!");
                        }
                    }
                    if (null != null) {
                        try {
                            ((KXmlParser) null).close();
                        } catch (Exception e14) {
                            Log.e(TAG, "parser close error");
                        }
                    }
                    throw th;
                }
            } catch (Throwable th22) {
                th = th22;
                inputStream = is;
                if (inputStream != null) {
                    inputStream.close();
                }
                if (null != null) {
                    ((KXmlParser) null).close();
                }
                throw th;
            }
        } catch (XmlPullParserException e15) {
            Log.e(TAG, "failed parsing config file parser error");
            if (inputStream != null) {
                inputStream.close();
            }
            if (null != null) {
                ((KXmlParser) null).close();
            }
            return false;
        } catch (IOException e16) {
            Log.e(TAG, "failed parsing config file IO error ");
            if (inputStream != null) {
                inputStream.close();
            }
            if (null != null) {
                ((KXmlParser) null).close();
            }
            return false;
        } catch (NumberFormatException e17) {
            Log.e(TAG, "config number format error");
            if (inputStream != null) {
                inputStream.close();
            }
            if (null != null) {
                ((KXmlParser) null).close();
            }
            return false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean checkAppListFromXml(XmlPullParser parser) throws XmlPullParserException, IOException, NumberFormatException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                return false;
            }
            if (!(type == 3 || type == 4)) {
                if (XML_TAG_ITEM.equals(parser.getName())) {
                    this.mAppData = new AppData();
                    readAppDataFromXml(parser, this.mAppData);
                    if (!(this.mAppData.mAppName == null || this.mAppData.mSupportVersion == null || !this.mAppData.mAppName.equals(this.mCurrentPackageName))) {
                        break;
                    }
                }
                continue;
            }
        }
        return false;
    }

    void readAppDataFromXml(XmlPullParser parser, AppData appdata) throws XmlPullParserException, IOException, NumberFormatException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                String tag = parser.getName();
                if (XML_TAG_APPNAME.equals(tag)) {
                    appdata.mAppName = parser.nextText();
                } else if (XML_TAG_VERSION.equals(tag)) {
                    appdata.mSupportVersion = parser.nextText();
                } else if (XML_TAG_FLINGVELOCITY.equals(tag)) {
                    appdata.mFlingVelocity = Integer.parseInt(parser.nextText());
                } else if (XML_TAG_IDLEVELOCITY.equals(tag)) {
                    appdata.mIdleVelocity = Integer.parseInt(parser.nextText());
                } else {
                    Log.e(TAG, "Unknown  tag: " + tag);
                }
            }
        }
    }

    public boolean isWechatOptimizeEffect() {
        return this.mIsEffect;
    }

    public int getWechatFlingVelocity() {
        if (this.mAppData == null) {
            return 0;
        }
        return this.mAppData.mFlingVelocity;
    }

    public int getWechatIdleVelocity() {
        if (this.mAppData == null) {
            return 0;
        }
        return this.mAppData.mIdleVelocity;
    }

    public boolean isWechatFling() {
        return this.mIsFling;
    }

    public void setWechatFling(boolean isFling) {
        this.mIsFling = isFling;
    }

    private boolean isWechatVersionSupport(String appName, String supportVersion) {
        Context context = ActivityThread.currentApplication();
        if (context == null) {
            return false;
        }
        try {
            int currentVersionCode = context.getPackageManager().getPackageInfo(appName, 0).versionCode;
            Log.d(TAG, "isWechatVersionSupport currentVersionCode:" + currentVersionCode);
            return versionInRange(currentVersionCode, supportVersion);
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    boolean versionInRange(int checkedVersion, String versionRanage) {
        if (versionRanage == null) {
            return false;
        }
        int i;
        int versionIndex = versionRanage.indexOf(";");
        String versionPreRange;
        if (versionIndex >= 0) {
            versionPreRange = versionRanage.substring(0, versionIndex);
        } else {
            versionPreRange = versionRanage;
        }
        for (String split : versionPreRange.split(",")) {
            String[] VersionStartAndEnd = split.split("-");
            if (VersionStartAndEnd.length >= 2) {
                try {
                    int checkedVersionStart = Integer.parseInt(VersionStartAndEnd[0]);
                    int checkedVersionEnd = Integer.parseInt(VersionStartAndEnd[1]);
                    if (checkedVersion >= checkedVersionStart && checkedVersion <= checkedVersionEnd) {
                        return Debug;
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "version number format error");
                    return false;
                }
            }
        }
        if (versionIndex >= 0) {
            String[] versionPostArray = versionRanage.substring(versionIndex + 1).split(",");
            int versionPostArrayLen = versionPostArray.length;
            i = 0;
            while (i < versionPostArrayLen) {
                try {
                    if (checkedVersion == Integer.parseInt(versionPostArray[i])) {
                        return Debug;
                    }
                    i++;
                } catch (NumberFormatException e2) {
                    Log.e(TAG, "version number format error");
                    return false;
                }
            }
        }
        return false;
    }
}
