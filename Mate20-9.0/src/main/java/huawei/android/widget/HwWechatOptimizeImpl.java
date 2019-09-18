package huawei.android.widget;

import android.app.ActivityThread;
import android.content.Context;
import android.content.pm.PackageManager;
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

        public AppData() {
        }

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
                if ("com.tencent.mm".equals(this.mCurrentPackageName) && loadFile(XML_TAG_SWITCH) && loadFile(XML_TAG_CONFIG)) {
                    this.mIsEffect = true;
                }
                Log.d(TAG, "mIsEffect:" + this.mIsEffect);
            }
        }
    }

    private File getFile(String fileName) {
        return new File(fileName);
    }

    private boolean loadFile(String xmlTag) {
        File file;
        if (xmlTag == null || "".equals(xmlTag)) {
            return false;
        }
        if (XML_TAG_SWITCH.equals(xmlTag)) {
            file = getFile(SWITCH_FILEPATH);
        } else if (!XML_TAG_CONFIG.equals(xmlTag)) {
            return false;
        } else {
            file = getFile(CONFIG_FILEPATH);
        }
        if (!file.exists()) {
            return false;
        }
        InputStream is = null;
        XmlPullParser parser = null;
        try {
            is = new FileInputStream(file);
            parser = Xml.newPullParser();
            parser.setInput(is, StandardCharsets.UTF_8.name());
            int outerDepth = parser.getDepth();
            while (true) {
                int next = parser.next();
                int type = next;
                if (next == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        Log.e(TAG, "close file input stream fail!");
                    }
                    if (parser != null) {
                        try {
                            ((KXmlParser) parser).close();
                        } catch (Exception e2) {
                            Log.e(TAG, "parser close error");
                        }
                    }
                    return false;
                } else if (type != 3) {
                    if (type != 4) {
                        String tagName = parser.getName();
                        if (XML_TAG_SWITCH.equals(xmlTag)) {
                            if (XML_TAG_SWITCH.equals(tagName)) {
                                if (Integer.parseInt(parser.nextText()) == 1) {
                                    try {
                                        is.close();
                                    } catch (IOException e3) {
                                        Log.e(TAG, "close file input stream fail!");
                                    }
                                    if (parser != null) {
                                        try {
                                            ((KXmlParser) parser).close();
                                        } catch (Exception e4) {
                                            Log.e(TAG, "parser close error");
                                        }
                                    }
                                    return true;
                                }
                                try {
                                    is.close();
                                } catch (IOException e5) {
                                    Log.e(TAG, "close file input stream fail!");
                                }
                                if (parser != null) {
                                    try {
                                        ((KXmlParser) parser).close();
                                    } catch (Exception e6) {
                                        Log.e(TAG, "parser close error");
                                    }
                                }
                                return false;
                            }
                        } else if (!XML_TAG_CONFIG.equals(xmlTag)) {
                            try {
                                is.close();
                            } catch (IOException e7) {
                                Log.e(TAG, "close file input stream fail!");
                            }
                            if (parser != null) {
                                try {
                                    ((KXmlParser) parser).close();
                                } catch (Exception e8) {
                                    Log.e(TAG, "parser close error");
                                }
                            }
                            return false;
                        } else if (XML_TAG_CONFIG.equals(tagName) && TEXT_NAME.equals(parser.getAttributeValue(null, ATTR_NAME))) {
                            boolean appOptimized = false;
                            if (checkAppListFromXml(parser)) {
                                appOptimized = true;
                            }
                            try {
                                is.close();
                            } catch (IOException e9) {
                                Log.e(TAG, "close file input stream fail!");
                            }
                            if (parser != null) {
                                try {
                                    ((KXmlParser) parser).close();
                                } catch (Exception e10) {
                                    Log.e(TAG, "parser close error");
                                }
                            }
                            return appOptimized;
                        }
                    }
                }
            }
        } catch (XmlPullParserException e11) {
            Log.e(TAG, "failed parsing " + xmlTag + " file parser error");
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e12) {
                    Log.e(TAG, "close file input stream fail!");
                }
            }
            if (parser != null) {
                try {
                    ((KXmlParser) parser).close();
                } catch (Exception e13) {
                    Log.e(TAG, "parser close error");
                }
            }
            return false;
        } catch (IOException e14) {
            Log.e(TAG, "failed parsing " + xmlTag + " file IO error ");
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e15) {
                    Log.e(TAG, "close file input stream fail!");
                }
            }
            if (parser != null) {
                try {
                    ((KXmlParser) parser).close();
                } catch (Exception e16) {
                    Log.e(TAG, "parser close error");
                }
            }
            return false;
        } catch (NumberFormatException e17) {
            Log.e(TAG, xmlTag + " number format error");
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e18) {
                    Log.e(TAG, "close file input stream fail!");
                }
            }
            if (parser != null) {
                try {
                    ((KXmlParser) parser).close();
                } catch (Exception e19) {
                    Log.e(TAG, "parser close error");
                }
            }
            return false;
        } catch (Throwable th) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e20) {
                    Log.e(TAG, "close file input stream fail!");
                }
            }
            if (parser != null) {
                try {
                    ((KXmlParser) parser).close();
                } catch (Exception e21) {
                    Log.e(TAG, "parser close error");
                }
            }
            throw th;
        }
    }

    private boolean checkAppListFromXml(XmlPullParser parser) throws XmlPullParserException, IOException, NumberFormatException {
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                return false;
            }
            if (!(type == 3 || type == 4 || !XML_TAG_ITEM.equals(parser.getName()))) {
                this.mAppData = new AppData();
                readAppDataFromXml(parser, this.mAppData);
                if (!(this.mAppData.mAppName == null || this.mAppData.mSupportVersion == null || !this.mAppData.mAppName.equals(this.mCurrentPackageName))) {
                    return isWechatVersionSupport(this.mAppData.mAppName, this.mAppData.mSupportVersion);
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void readAppDataFromXml(XmlPullParser parser, AppData appdata) throws XmlPullParserException, IOException, NumberFormatException {
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1) {
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
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean versionInRange(int checkedVersion, String versionRanage) {
        String versionPreRange;
        if (versionRanage == null) {
            return false;
        }
        int versionIndex = versionRanage.indexOf(";");
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
            int i = 0;
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
