package huawei.android.widget;

import android.content.Context;
import android.content.pm.PackageManager;
import android.rms.iaware.DataContract;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.widget.DefaultHwWechatOptimizeImpl;
import com.huawei.android.app.ActivityThreadEx;
import com.huawei.android.os.SystemPropertiesEx;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwWechatOptimizeImpl extends DefaultHwWechatOptimizeImpl {
    private static final String CONFIG_FILE_PATH = "/data/app_acc/app_config.xml";
    private static final boolean IS_DEBUG;
    private static final Object LOCK = new Object();
    private static final int MIN_VERSION_STRING_SPLIT_LENGHT = 2;
    private static final String SWITCH_FILE_PATH = "/data/app_acc/app_switch.xml";
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
        if (SystemPropertiesEx.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        IS_DEBUG = z;
    }

    private HwWechatOptimizeImpl() {
        if (SystemPropertiesEx.getBoolean("persist.sys.enable_iaware", false)) {
            this.mCurrentPackageName = ActivityThreadEx.currentPackageName();
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

    public static HwWechatOptimizeImpl getInstance() {
        HwWechatOptimizeImpl hwWechatOptimizeImpl;
        synchronized (LOCK) {
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

    /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
        android.util.Log.e(huawei.android.widget.HwWechatOptimizeImpl.TAG, "failed parsing config file");
     */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00b7 A[ExcHandler: IOException | NumberFormatException | XmlPullParserException (e java.lang.Throwable), Splitter:B:27:0x007d] */
    private boolean loadFile(String xmlTag) {
        if (!isXmlTagValide(xmlTag) || this.mParser == null) {
            return false;
        }
        File file = XML_TAG_SWITCH.equals(xmlTag) ? new File(SWITCH_FILE_PATH) : new File(CONFIG_FILE_PATH);
        if (!file.exists()) {
            return false;
        }
        InputStream inputStream = new FileInputStream(file);
        this.mParser.setInput(inputStream, StandardCharsets.UTF_8.name());
        int outerDepth = this.mParser.getDepth();
        int type = this.mParser.next();
        while (hasNext(type, outerDepth)) {
            if (type == 3 || type == 4) {
                type = this.mParser.next();
            } else {
                String tagName = this.mParser.getName();
                if (!XML_TAG_SWITCH.equals(xmlTag) || !XML_TAG_SWITCH.equals(tagName)) {
                    try {
                        if (!XML_TAG_CONFIG.equals(xmlTag) || !XML_TAG_CONFIG.equals(tagName) || !"AppList".equals(this.mParser.getAttributeValue(null, "name"))) {
                            type = this.mParser.next();
                        } else {
                            boolean checkAppListFromXml = checkAppListFromXml(this.mParser);
                            closeStream(inputStream);
                            return checkAppListFromXml;
                        }
                    } catch (IOException | NumberFormatException | XmlPullParserException e) {
                    } catch (Throwable th) {
                        closeStream(inputStream);
                        throw th;
                    }
                } else {
                    boolean z = true;
                    if (Integer.parseInt(this.mParser.nextText()) != 1) {
                        z = false;
                    }
                    closeStream(inputStream);
                    return z;
                }
            }
        }
        closeStream(inputStream);
        return false;
    }

    private void closeStream(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "close file input stream fail!");
            }
        }
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
        Context context = ActivityThreadEx.currentApplication();
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
