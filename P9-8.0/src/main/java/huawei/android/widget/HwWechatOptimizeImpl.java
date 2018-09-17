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
    private static String CONFIG_FILEPATH = "/data/app_acc/app_config.xml";
    private static final boolean Debug = true;
    private static String SWITCH_FILEPATH = "/data/app_acc/app_switch.xml";
    private static final String TAG = "HwWechatOptimizeImpl";
    private static final String TEXT_NAME = "AppList";
    private static final String XML_TAG_APPNAME = "packageName";
    private static final String XML_TAG_CONFIG = "config";
    private static final String XML_TAG_FLINGVELOCITY = "flingVelocity";
    private static final String XML_TAG_IDLEVELOCITY = "idleVelocity";
    private static final String XML_TAG_ITEM = "item";
    private static final String XML_TAG_SWITCH = "switch";
    private static final String XML_TAG_VERSION = "supportVersion";
    private static HwWechatOptimizeImpl mHwWechatOptimizeImpl = null;
    private AppData mAppData = null;
    private String mCurrentPackageName = null;
    private boolean mIsEffect = false;
    private boolean mIsFling = false;

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
        if (SystemProperties.getBoolean("persist.sys.enable_iaware", false)) {
            this.mCurrentPackageName = ActivityThread.currentPackageName();
            if (this.mCurrentPackageName != null && !this.mCurrentPackageName.isEmpty()) {
                if ("com.tencent.mm".equals(this.mCurrentPackageName) && isSwitchEnabled() && loadConfigFile()) {
                    this.mIsEffect = true;
                }
                Log.d(TAG, "mIsEffect:" + this.mIsEffect);
            }
        }
    }

    private File getFile(String fileName) {
        return new File(fileName);
    }

    /* JADX WARNING: Removed duplicated region for block: B:140:0x01a0  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00ab A:{SYNTHETIC, Splitter: B:53:0x00ab} */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x0143 A:{SYNTHETIC, Splitter: B:105:0x0143} */
    /* JADX WARNING: Removed duplicated region for block: B:109:0x0149 A:{SYNTHETIC, Splitter: B:109:0x0149} */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0111 A:{SYNTHETIC, Splitter: B:88:0x0111} */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x0117 A:{SYNTHETIC, Splitter: B:92:0x0117} */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x00df A:{SYNTHETIC, Splitter: B:71:0x00df} */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x00e5 A:{SYNTHETIC, Splitter: B:75:0x00e5} */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x016c A:{SYNTHETIC, Splitter: B:120:0x016c} */
    /* JADX WARNING: Removed duplicated region for block: B:124:0x0172 A:{SYNTHETIC, Splitter: B:124:0x0172} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isSwitchEnabled() {
        Throwable th;
        File file = getFile(SWITCH_FILEPATH);
        if (!file.exists()) {
            return false;
        }
        InputStream is = null;
        try {
            InputStream is2 = new FileInputStream(file);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(is2, StandardCharsets.UTF_8.name());
                int outerDepth = parser.getDepth();
                while (true) {
                    int type = parser.next();
                    if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                        if (is2 == null) {
                            try {
                                is2.close();
                            } catch (IOException e) {
                                Log.e(TAG, "close file input stream fail!");
                            }
                        }
                    } else if (!(type == 3 || type == 4)) {
                        if (XML_TAG_SWITCH.equals(parser.getName())) {
                            if (Integer.parseInt(parser.nextText()) == 1) {
                                if (is2 != null) {
                                    try {
                                        is2.close();
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
                                return true;
                            }
                            if (is2 != null) {
                                try {
                                    is2.close();
                                } catch (IOException e4) {
                                    Log.e(TAG, "close file input stream fail!");
                                    is = is2;
                                }
                            }
                            if (parser != null) {
                                try {
                                    ((KXmlParser) parser).close();
                                } catch (Exception e5) {
                                    Log.e(TAG, "parser close error");
                                }
                            }
                            return false;
                        }
                    }
                }
                if (is2 == null) {
                }
                if (parser != null) {
                    try {
                        ((KXmlParser) parser).close();
                    } catch (Exception e6) {
                        Log.e(TAG, "parser close error");
                    }
                }
                return false;
            } catch (XmlPullParserException e7) {
                is = is2;
                Log.e(TAG, "failed parsing switch file parser error");
                if (is != null) {
                }
                if (null != null) {
                }
                return false;
            } catch (IOException e8) {
                is = is2;
                Log.e(TAG, "failed parsing switch file IO error ");
                if (is != null) {
                }
                if (null != null) {
                }
                return false;
            } catch (NumberFormatException e9) {
                is = is2;
                try {
                    Log.e(TAG, "switch number format error");
                    if (is != null) {
                    }
                    if (null != null) {
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (is != null) {
                        try {
                            is.close();
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
                    throw th;
                }
            } catch (Throwable th22) {
                th = th22;
                is = is2;
                if (is != null) {
                }
                if (null != null) {
                }
                throw th;
            }
        } catch (XmlPullParserException e12) {
            Log.e(TAG, "failed parsing switch file parser error");
            if (is != null) {
                try {
                    is.close();
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
            return false;
        } catch (IOException e15) {
            Log.e(TAG, "failed parsing switch file IO error ");
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e16) {
                    Log.e(TAG, "close file input stream fail!");
                }
            }
            if (null != null) {
                try {
                    ((KXmlParser) null).close();
                } catch (Exception e17) {
                    Log.e(TAG, "parser close error");
                }
            }
            return false;
        } catch (NumberFormatException e18) {
            Log.e(TAG, "switch number format error");
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e19) {
                    Log.e(TAG, "close file input stream fail!");
                }
            }
            if (null != null) {
                try {
                    ((KXmlParser) null).close();
                } catch (Exception e20) {
                    Log.e(TAG, "parser close error");
                }
            }
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:127:0x0199  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0094 A:{SYNTHETIC, Splitter: B:40:0x0094} */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x0134 A:{SYNTHETIC, Splitter: B:92:0x0134} */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x013a A:{SYNTHETIC, Splitter: B:96:0x013a} */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x00fe A:{SYNTHETIC, Splitter: B:75:0x00fe} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x0104 A:{SYNTHETIC, Splitter: B:79:0x0104} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00c8 A:{SYNTHETIC, Splitter: B:58:0x00c8} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x00ce A:{SYNTHETIC, Splitter: B:62:0x00ce} */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x0161 A:{SYNTHETIC, Splitter: B:107:0x0161} */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x0167 A:{SYNTHETIC, Splitter: B:111:0x0167} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean loadConfigFile() {
        Throwable th;
        File file = getFile(CONFIG_FILEPATH);
        if (!file.exists()) {
            return false;
        }
        InputStream is = null;
        try {
            InputStream is2 = new FileInputStream(file);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(is2, StandardCharsets.UTF_8.name());
                int outerDepth = parser.getDepth();
                while (true) {
                    int type = parser.next();
                    if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                        if (is2 == null) {
                            try {
                                is2.close();
                            } catch (IOException e) {
                                Log.e(TAG, "close file input stream fail!");
                            }
                        }
                    } else if (!(type == 3 || type == 4)) {
                        if (XML_TAG_CONFIG.equals(parser.getName())) {
                            if (TEXT_NAME.equals(parser.getAttributeValue(null, ATTR_NAME))) {
                                boolean appOptimized = false;
                                if (checkAppListFromXml(parser)) {
                                    appOptimized = true;
                                }
                                if (is2 != null) {
                                    try {
                                        is2.close();
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
                            }
                        } else {
                            continue;
                        }
                    }
                }
                if (is2 == null) {
                }
                if (parser != null) {
                    try {
                        ((KXmlParser) parser).close();
                    } catch (Exception e4) {
                        Log.e(TAG, "parser close error");
                    }
                }
                return false;
            } catch (XmlPullParserException e5) {
                is = is2;
                Log.e(TAG, "failed parsing config file parser error");
                if (is != null) {
                }
                if (null != null) {
                }
                return false;
            } catch (IOException e6) {
                is = is2;
                Log.e(TAG, "failed parsing config file IO error ");
                if (is != null) {
                }
                if (null != null) {
                }
                return false;
            } catch (NumberFormatException e7) {
                is = is2;
                try {
                    Log.e(TAG, "config number format error");
                    if (is != null) {
                    }
                    if (null != null) {
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (is != null) {
                    }
                    if (null != null) {
                    }
                    throw th;
                }
            } catch (Throwable th22) {
                th = th22;
                is = is2;
                if (is != null) {
                    try {
                        is.close();
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
                throw th;
            }
        } catch (XmlPullParserException e10) {
            Log.e(TAG, "failed parsing config file parser error");
            if (is != null) {
                try {
                    is.close();
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
        } catch (IOException e13) {
            Log.e(TAG, "failed parsing config file IO error ");
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e14) {
                    Log.e(TAG, "close file input stream fail!");
                }
            }
            if (null != null) {
                try {
                    ((KXmlParser) null).close();
                } catch (Exception e15) {
                    Log.e(TAG, "parser close error");
                }
            }
            return false;
        } catch (NumberFormatException e16) {
            Log.e(TAG, "config number format error");
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e17) {
                    Log.e(TAG, "close file input stream fail!");
                }
            }
            if (null != null) {
                try {
                    ((KXmlParser) null).close();
                } catch (Exception e18) {
                    Log.e(TAG, "parser close error");
                }
            }
            return false;
        }
    }

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
                        return isWechatVersionSupport(this.mAppData.mAppName, this.mAppData.mSupportVersion);
                    }
                } else {
                    continue;
                }
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
                if ("packageName".equals(tag)) {
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
                        return true;
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
                        return true;
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
