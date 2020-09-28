package huawei.android.widget;

import android.app.ActivityThread;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.SystemProperties;
import android.rms.iaware.DataContract;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.widget.IHwWechatOptimize;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwWechatOptimizeImpl implements IHwWechatOptimize {
    private static final String CONFIG_FILEPATH = "/data/app_acc/app_config.xml";
    private static final int DEFAULT_DEBUG_TYPE = 3;
    private static final boolean IS_DEBUG;
    private static final int MIN_VERSION_STRING_SPLIT_LENGHT = 2;
    private static final String SWITCH_FILEPATH = "/data/app_acc/app_switch.xml";
    private static final String TAG = "HwWechatOptimizeImpl";
    private static final String XML_TAG_CONFIG = "config";
    private static final String XML_TAG_SWITCH = "switch";
    private static HwWechatOptimizeImpl sHwWechatOptimizeImpl = null;
    private AppData mAppData = null;
    private String mCurrentPackageName = null;
    private boolean mIsEffect = false;
    private boolean mIsFling = false;
    private XmlPullParser mParser = null;

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        IS_DEBUG = z;
    }

    private HwWechatOptimizeImpl() {
        if (SystemProperties.getBoolean("persist.sys.enable_iaware", false)) {
            this.mCurrentPackageName = ActivityThread.currentPackageName();
            String str = this.mCurrentPackageName;
            if (str != null && !str.isEmpty()) {
                if ("com.tencent.mm".equals(this.mCurrentPackageName)) {
                    this.mParser = Xml.newPullParser();
                    if (loadFile(XML_TAG_SWITCH) && loadFile(XML_TAG_CONFIG)) {
                        this.mIsEffect = true;
                    }
                }
                if (IS_DEBUG) {
                    Log.d(TAG, "mIsEffect:" + this.mIsEffect);
                }
            }
        }
    }

    public static synchronized HwWechatOptimizeImpl getInstance() {
        HwWechatOptimizeImpl hwWechatOptimizeImpl;
        synchronized (HwWechatOptimizeImpl.class) {
            if (sHwWechatOptimizeImpl == null) {
                sHwWechatOptimizeImpl = new HwWechatOptimizeImpl();
            }
            hwWechatOptimizeImpl = sHwWechatOptimizeImpl;
        }
        return hwWechatOptimizeImpl;
    }

    private boolean hasNext(int type, int outerDepth) {
        if (type == 1 || (type == 3 && this.mParser.getDepth() <= outerDepth)) {
            return false;
        }
        return true;
    }

    private boolean isXmlTagValide(String xmlTag) {
        if (TextUtils.isEmpty(xmlTag)) {
            return false;
        }
        if (XML_TAG_SWITCH.equals(xmlTag) || XML_TAG_CONFIG.equals(xmlTag)) {
            return true;
        }
        return false;
    }

    private boolean loadFile(String xmlTag) {
        boolean z = false;
        if (!isXmlTagValide(xmlTag) || this.mParser == null) {
            return false;
        }
        File file = XML_TAG_SWITCH.equals(xmlTag) ? new File(SWITCH_FILEPATH) : new File(CONFIG_FILEPATH);
        if (!file.exists()) {
            return false;
        }
        InputStream inputStream = null;
        try {
            InputStream inputStream2 = new FileInputStream(file);
            this.mParser.setInput(inputStream2, StandardCharsets.UTF_8.name());
            int outerDepth = this.mParser.getDepth();
            int type = this.mParser.next();
            while (hasNext(type, outerDepth)) {
                if (type != 3) {
                    if (type != 4) {
                        String tagName = this.mParser.getName();
                        if (XML_TAG_SWITCH.equals(xmlTag) && XML_TAG_SWITCH.equals(tagName)) {
                            boolean z2 = true;
                            if (Integer.parseInt(this.mParser.nextText()) != 1) {
                                z2 = z;
                            }
                            try {
                                inputStream2.close();
                            } catch (IOException e) {
                                Log.e(TAG, "close file input stream fail!");
                            }
                            return z2;
                        } else if (!XML_TAG_CONFIG.equals(xmlTag) || !XML_TAG_CONFIG.equals(tagName) || !"AppList".equals(this.mParser.getAttributeValue(null, "name"))) {
                            type = this.mParser.next();
                            z = false;
                        } else {
                            boolean checkAppListFromXml = checkAppListFromXml(this.mParser);
                            try {
                                inputStream2.close();
                            } catch (IOException e2) {
                                Log.e(TAG, "close file input stream fail!");
                            }
                            return checkAppListFromXml;
                        }
                    }
                }
                type = this.mParser.next();
                z = false;
            }
            try {
                inputStream2.close();
            } catch (IOException e3) {
                Log.e(TAG, "close file input stream fail!");
                return false;
            }
        } catch (XmlPullParserException e4) {
            Log.e(TAG, "failed parsing " + xmlTag + " file parser error");
            if (0 == 0) {
                return false;
            }
            inputStream.close();
        } catch (IOException e5) {
            Log.e(TAG, "failed parsing " + xmlTag + " file IO error ");
            if (0 == 0) {
                return false;
            }
            inputStream.close();
        } catch (NumberFormatException e6) {
            Log.e(TAG, xmlTag + " number format error");
            if (0 == 0) {
                return false;
            }
            inputStream.close();
        } catch (Throwable e7) {
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e8) {
                    Log.e(TAG, "close file input stream fail!");
                }
            }
            throw e7;
        }
        return false;
    }

    private boolean checkAppListFromXml(XmlPullParser parser) throws XmlPullParserException, IOException, NumberFormatException {
        int outerDepth = parser.getDepth();
        int type = parser.next();
        while (hasNext(type, outerDepth)) {
            if (type == 3 || type == 4) {
                type = parser.next();
            } else {
                if ("item".equals(parser.getName())) {
                    this.mAppData = new AppData();
                    readAppDataFromXml(parser, this.mAppData);
                    if (!(this.mAppData.mAppName == null || this.mAppData.mSupportVersion == null || !this.mAppData.mAppName.equals(this.mCurrentPackageName))) {
                        return isWechatVersionSupport(this.mAppData.mAppName, this.mAppData.mSupportVersion);
                    }
                }
                type = parser.next();
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void readAppDataFromXml(XmlPullParser parser, AppData appData) throws XmlPullParserException, IOException, NumberFormatException {
        int outerDepth = parser.getDepth();
        int type = parser.next();
        while (hasNext(type, outerDepth)) {
            if (type == 3 || type == 4) {
                type = parser.next();
            } else {
                String tag = parser.getName();
                if (DataContract.BaseProperty.PACKAGE_NAME.equals(tag)) {
                    appData.mAppName = parser.nextText();
                } else if ("supportVersion".equals(tag)) {
                    appData.mSupportVersion = parser.nextText();
                } else if ("flingVelocity".equals(tag)) {
                    appData.mFlingVelocity = Integer.parseInt(parser.nextText());
                } else if ("idleVelocity".equals(tag)) {
                    appData.mIdleVelocity = Integer.parseInt(parser.nextText());
                } else {
                    Log.e(TAG, "Unknown tag: " + tag);
                }
                type = parser.next();
            }
        }
    }

    public boolean isWechatOptimizeEffect() {
        return this.mIsEffect;
    }

    public int getWechatFlingVelocity() {
        AppData appData = this.mAppData;
        if (appData == null) {
            return 0;
        }
        return appData.mFlingVelocity;
    }

    public int getWechatIdleVelocity() {
        AppData appData = this.mAppData;
        if (appData == null) {
            return 0;
        }
        return appData.mIdleVelocity;
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
            if (IS_DEBUG) {
                Log.d(TAG, "isWechatVersionSupport currentVersionCode:" + currentVersionCode);
            }
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
        for (String versionPre : versionPreRange.split(",")) {
            String[] versionStartAndEnds = versionPre.split("-");
            if (versionStartAndEnds.length >= 2) {
                try {
                    int checkedVersionStart = Integer.parseInt(versionStartAndEnds[0]);
                    int checkedVersionEnd = Integer.parseInt(versionStartAndEnds[1]);
                    if (checkedVersion >= checkedVersionStart && checkedVersion <= checkedVersionEnd) {
                        return true;
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "version number format error");
                    return false;
                }
            }
        }
        if (versionIndex < 0) {
            return false;
        }
        for (String versionPos : versionRanage.substring(versionIndex + 1).split(",")) {
            try {
                if (checkedVersion == Integer.parseInt(versionPos)) {
                    return true;
                }
            } catch (NumberFormatException e2) {
                Log.e(TAG, "version number format error");
                return false;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public static class AppData {
        String mAppName;
        int mFlingVelocity;
        int mIdleVelocity;
        String mSupportVersion;

        AppData() {
        }

        AppData(String name, String supportVersion, int flingVelocity, int idleVelocity) {
            this.mAppName = name;
            this.mSupportVersion = supportVersion;
            this.mFlingVelocity = flingVelocity;
            this.mIdleVelocity = idleVelocity;
        }
    }
}
