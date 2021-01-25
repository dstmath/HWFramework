package com.android.server.gesture;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicyEx;
import com.huawei.android.content.pm.ApplicationInfoEx;
import com.huawei.android.os.EnvironmentEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.util.SlogEx;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import com.huawei.libcore.io.IoUtilsEx;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwGestureNavWhiteConfig extends DefaultHwGestureNavWhiteConfig {
    private static final String CURRENT_ROM_VERSION;
    private static final int FLAG_FULLSCREEN = 1024;
    private static final int FLAG_GESTNAV_SLIDER_ONE = 1;
    private static final String RO_BUILD_HW_VERSION = SystemPropertiesEx.get("ro.huawei.build.version.incremental", "");
    private static final String TAG = "GestureNavWhiteCfg";
    private static final String WHITE_LIST = "GestureNav_whitelist.xml";
    private static final String XML_ATTRIBUTE_ACTION = "action";
    private static final String XML_ATTRIBUTE_NAME = "name";
    private static final String XML_VERSION = "version";
    private static final String XML_WHITE_LIST = "whitelist";
    private static final String XML_WINDOW = "window";
    private static HwGestureNavWhiteConfig sHwGestureNavWhiteConfig;
    private Context mContext;
    private WindowManagerPolicyEx.WindowStateEx mCurrentWin;
    private WindowManagerPolicyEx mPolicy;
    private int mRotation;
    private List<GestureNavAttr> mWindows = new ArrayList();

    static {
        String str;
        if (TextUtils.isEmpty(RO_BUILD_HW_VERSION)) {
            str = SystemPropertiesEx.get("ro.build.version.incremental", "B001");
        } else {
            str = RO_BUILD_HW_VERSION;
        }
        CURRENT_ROM_VERSION = str;
    }

    private HwGestureNavWhiteConfig() {
        new WhitelistReadThread().start();
    }

    public static synchronized HwGestureNavWhiteConfig getInstance() {
        HwGestureNavWhiteConfig hwGestureNavWhiteConfig;
        synchronized (HwGestureNavWhiteConfig.class) {
            if (sHwGestureNavWhiteConfig == null) {
                sHwGestureNavWhiteConfig = new HwGestureNavWhiteConfig();
                SlogEx.d(TAG, "getInstance " + sHwGestureNavWhiteConfig);
            }
            hwGestureNavWhiteConfig = sHwGestureNavWhiteConfig;
        }
        return hwGestureNavWhiteConfig;
    }

    public void init(Context context) {
        SlogEx.d(TAG, "init " + this);
        this.mContext = context;
        this.mPolicy = WindowManagerPolicyEx.getInstance();
    }

    public void updateConfig() {
        unInitList();
        initList();
    }

    public void updateWindow(WindowManagerPolicyEx.WindowStateEx win) {
        this.mCurrentWin = win;
    }

    public void updateRotation(int rotation) {
        this.mRotation = rotation;
    }

    public synchronized boolean isEnable() {
        return isEnable(this.mCurrentWin, this.mRotation, this.mPolicy);
    }

    public synchronized boolean isEnable(WindowManagerPolicyEx.WindowStateEx focusWindow, int rotation, WindowManagerPolicyEx policy) {
        boolean z = false;
        if (focusWindow != null) {
            if (focusWindow.getAttrs() != null) {
                if (rotation != 0) {
                    if (rotation != 2) {
                        GestureNavAttr whiteName = findInList(focusWindow.toString());
                        if (whiteName != null) {
                            return whiteName.mIsAction;
                        } else if (focusWindow.getWindowingMode() == 103) {
                            return false;
                        } else {
                            int lastSystemUiFlags = 0;
                            HwPhoneWindowManager manager = policy.getHwPhoneWindowManager();
                            if (manager != null) {
                                lastSystemUiFlags = manager.getLastSystemUiFlags();
                            }
                            boolean isCurrentWindowFullScreen = (focusWindow.getAttrs().flags & 1024) != 0;
                            boolean isLastSystemUiFlagFullScreen = (lastSystemUiFlags & 4) != 0;
                            boolean isFullScreenApp = isCurrentWindowFullScreen || (isLastSystemUiFlagFullScreen && !focusWindow.toString().contains(GestureNavConst.STATUSBAR_WINDOW));
                            boolean isDisableByAppFlag = false;
                            if (isFullScreenApp) {
                                isDisableByAppFlag = checkGestnavFlags(1, focusWindow);
                            }
                            if (GestureNavConst.DEBUG) {
                                SlogEx.i(TAG, "winFullScreen:" + isCurrentWindowFullScreen + ", sysUiFlag:" + isLastSystemUiFlagFullScreen + ", disabled:" + isDisableByAppFlag);
                            }
                            if (isFullScreenApp && !isDisableByAppFlag) {
                                z = true;
                            }
                            return z;
                        }
                    }
                }
                return false;
            }
        }
        SlogEx.d(TAG, "focusWindow is null");
        return false;
    }

    private boolean checkGestnavFlags(int flags, WindowManagerPolicyEx.WindowStateEx focusWindow) {
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            return false;
        }
        String pckName = focusWindow != null ? focusWindow.getAttrs().packageName : "";
        try {
            ApplicationInfo info = pm.getApplicationInfo(pckName, 0);
            if (info == null || (ApplicationInfoEx.getGestureNavExtraFlags(info) & flags) != flags) {
                return false;
            }
            return true;
        } catch (IllegalArgumentException e) {
            SlogEx.e(TAG, "checkGestnavFlags catch IllegalArgumentException: pckName " + pckName + ",focusWindow: " + focusWindow);
            return false;
        } catch (PackageManager.NameNotFoundException e2) {
            Log.w(TAG, "pckName does n't exist!");
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initList() {
        long now = System.nanoTime();
        loadConfig();
        SlogEx.d(TAG, "load config use:" + (System.nanoTime() - now));
    }

    private String getKeyString(String key) {
        if (key == null) {
            return null;
        }
        if ("*".equals(key.substring(key.length() - 1))) {
            return " " + key.substring(0, key.length() - 1);
        }
        return " " + key + "}";
    }

    private GestureNavAttr findInList(String windowInfo) {
        GestureNavAttr object = findInList(this.mWindows, windowInfo);
        if (object != null) {
            return object;
        }
        return null;
    }

    private GestureNavAttr findInList(List<GestureNavAttr> list, String windowInfo) {
        for (GestureNavAttr attr : list) {
            if (windowInfo.contains(attr.mName)) {
                return attr;
            }
        }
        return null;
    }

    private void unInitList() {
        this.mWindows.clear();
    }

    private void addWindowToList(GestureNavAttr window) {
        if (GestureNavConst.DEBUG) {
            SlogEx.d(TAG, "add name:" + window.mName + " action " + window.mIsAction);
        }
        if (!this.mWindows.contains(window)) {
            this.mWindows.add(window);
        }
    }

    private void loadConfig() {
        String filePath = EnvironmentEx.getDataSystemDirectory() + "/" + CURRENT_ROM_VERSION + AwarenessInnerConstants.DASH_KEY + WHITE_LIST;
        File configFile = new File(filePath);
        if (!configFile.exists()) {
            configFile = HwCfgFilePolicy.getCfgFile("xml/GestureNav_whitelist.xml", 0);
            SlogEx.d(TAG, "load defalut config...");
        } else {
            SlogEx.d(TAG, "load config:" + filePath);
        }
        loadConfig(configFile);
    }

    private void loadConfig(File configFile) {
        InputStream inputStream = null;
        if (configFile != null) {
            try {
                if (configFile.exists()) {
                    inputStream = new FileInputStream(configFile);
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "load GestureNav FileNotFoundException.");
                if (0 != 0) {
                    inputStream.close();
                    return;
                }
                return;
            } catch (XmlPullParserException e2) {
                Log.e(TAG, "load GestureNav XmlPullParserException: ");
                if (0 != 0) {
                    inputStream.close();
                    return;
                }
                return;
            } catch (IOException e3) {
                Log.e(TAG, "load GestureNav IOException: ");
                if (0 != 0) {
                    try {
                        inputStream.close();
                        return;
                    } catch (IOException e4) {
                        Log.e(TAG, "load GestureNav config: IO Exception while closing stream");
                        return;
                    }
                } else {
                    return;
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        inputStream.close();
                    } catch (IOException e5) {
                        Log.e(TAG, "load GestureNav config: IO Exception while closing stream");
                    }
                }
                throw th;
            }
        }
        if (inputStream != null) {
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setInput(inputStream, null);
            for (int xmlEventType = xmlParser.next(); xmlEventType != 1; xmlEventType = xmlParser.next()) {
                if (GestureNavConst.DEBUG) {
                    SlogEx.d(TAG, "xmlname " + xmlParser.getName() + "EventType " + xmlEventType);
                }
                if (xmlEventType == 2 && XML_WINDOW.equals(xmlParser.getName())) {
                    GestureNavAttr window = new GestureNavAttr();
                    window.mName = getKeyString(xmlParser.getAttributeValue(null, XML_ATTRIBUTE_NAME));
                    String value = xmlParser.getAttributeValue(null, "action");
                    window.mIsAction = true;
                    if ("false".equals(value)) {
                        window.mIsAction = false;
                    }
                    addWindowToList(window);
                } else if (xmlEventType != 2 || !XML_VERSION.equals(xmlParser.getName())) {
                    if (xmlEventType == 3 && XML_WHITE_LIST.equals(xmlParser.getName())) {
                        break;
                    }
                } else {
                    String name = xmlParser.getAttributeValue(null, XML_ATTRIBUTE_NAME);
                    Log.d(TAG, "whitelist version :" + name);
                }
            }
        }
        if (inputStream != null) {
            inputStream.close();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0096 A[SYNTHETIC, Splitter:B:46:0x0096] */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00a3 A[SYNTHETIC, Splitter:B:51:0x00a3] */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00b1 A[SYNTHETIC, Splitter:B:56:0x00b1] */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x00bf A[SYNTHETIC, Splitter:B:61:0x00bf] */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x00cc A[SYNTHETIC, Splitter:B:66:0x00cc] */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x00da  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x00e9 A[SYNTHETIC, Splitter:B:75:0x00e9] */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x00f6 A[SYNTHETIC, Splitter:B:80:0x00f6] */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0103 A[SYNTHETIC, Splitter:B:85:0x0103] */
    /* JADX WARNING: Removed duplicated region for block: B:94:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:95:? A[RETURN, SYNTHETIC] */
    private boolean copyFile(FileInputStream srcStream, String filePath) {
        FileChannel srcChannel;
        FileOutputStream destStream;
        Throwable th;
        if (srcStream != null) {
            if (filePath != null) {
                FileOutputStream destStream2 = null;
                FileChannel dstChannel = null;
                FileChannel dstChannel2 = null;
                try {
                    destStream2 = new FileOutputStream(createDestFile(filePath));
                    FileChannel srcChannel2 = srcStream.getChannel();
                    try {
                        FileChannel dstChannel3 = destStream2.getChannel();
                        try {
                            srcChannel2.transferTo(0, srcChannel2.size(), dstChannel3);
                            boolean isSuccess = true;
                            try {
                                srcChannel2.close();
                            } catch (IOException e) {
                                isSuccess = false;
                                Log.e(TAG, "rcChannel close error, IOException:");
                            }
                            try {
                                destStream2.close();
                            } catch (IOException e2) {
                                isSuccess = false;
                                Log.e(TAG, "destStream close error, IOException:");
                            }
                            if (dstChannel3 != null) {
                                try {
                                    dstChannel3.close();
                                } catch (IOException e3) {
                                    Log.e(TAG, "dstChannel close error, IOException:");
                                    return false;
                                }
                            }
                            return isSuccess;
                        } catch (FileNotFoundException e4) {
                            dstChannel2 = dstChannel3;
                            dstChannel = srcChannel2;
                            boolean isSuccess2 = false;
                            Log.e(TAG, "copyFile FileNotFoundException.");
                            if (dstChannel != null) {
                                try {
                                    dstChannel.close();
                                } catch (IOException e5) {
                                    isSuccess2 = false;
                                    Log.e(TAG, "rcChannel close error, IOException:");
                                }
                            }
                            if (destStream2 != null) {
                                try {
                                    destStream2.close();
                                } catch (IOException e6) {
                                    Log.e(TAG, "destStream close error, IOException:");
                                    isSuccess2 = false;
                                }
                            }
                            if (dstChannel2 == null) {
                                return isSuccess2;
                            }
                            dstChannel2.close();
                            return isSuccess2;
                        } catch (IOException e7) {
                            dstChannel2 = dstChannel3;
                            dstChannel = srcChannel2;
                            boolean isSuccess3 = false;
                            try {
                                Log.e(TAG, "init IO error, IOException:");
                                if (dstChannel != null) {
                                    try {
                                        dstChannel.close();
                                    } catch (IOException e8) {
                                        isSuccess3 = false;
                                        Log.e(TAG, "rcChannel close error, IOException:");
                                    }
                                }
                                if (destStream2 != null) {
                                    try {
                                        destStream2.close();
                                    } catch (IOException e9) {
                                        Log.e(TAG, "destStream close error, IOException:");
                                        isSuccess3 = false;
                                    }
                                }
                                if (dstChannel2 == null) {
                                    return isSuccess3;
                                }
                                try {
                                    dstChannel2.close();
                                    return isSuccess3;
                                } catch (IOException e10) {
                                    Log.e(TAG, "dstChannel close error, IOException:");
                                    return false;
                                }
                            } catch (Throwable th2) {
                                srcChannel = dstChannel2;
                                destStream = destStream2;
                                th = th2;
                            }
                        } catch (Throwable th3) {
                            destStream = destStream2;
                            th = th3;
                            srcChannel = dstChannel3;
                            dstChannel = srcChannel2;
                            if (dstChannel != null) {
                                try {
                                    dstChannel.close();
                                } catch (IOException e11) {
                                    Log.e(TAG, "rcChannel close error, IOException:");
                                }
                            }
                            if (destStream != null) {
                                try {
                                    destStream.close();
                                } catch (IOException e12) {
                                    Log.e(TAG, "destStream close error, IOException:");
                                }
                            }
                            if (srcChannel != null) {
                                try {
                                    srcChannel.close();
                                } catch (IOException e13) {
                                    Log.e(TAG, "dstChannel close error, IOException:");
                                }
                            }
                            throw th;
                        }
                    } catch (FileNotFoundException e14) {
                        dstChannel = srcChannel2;
                        boolean isSuccess22 = false;
                        Log.e(TAG, "copyFile FileNotFoundException.");
                        if (dstChannel != null) {
                        }
                        if (destStream2 != null) {
                        }
                        if (dstChannel2 == null) {
                        }
                    } catch (IOException e15) {
                        dstChannel = srcChannel2;
                        boolean isSuccess32 = false;
                        Log.e(TAG, "init IO error, IOException:");
                        if (dstChannel != null) {
                        }
                        if (destStream2 != null) {
                        }
                        if (dstChannel2 == null) {
                        }
                    } catch (Throwable th4) {
                        dstChannel = srcChannel2;
                        srcChannel = null;
                        destStream = destStream2;
                        th = th4;
                        if (dstChannel != null) {
                        }
                        if (destStream != null) {
                        }
                        if (srcChannel != null) {
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e16) {
                    boolean isSuccess222 = false;
                    Log.e(TAG, "copyFile FileNotFoundException.");
                    if (dstChannel != null) {
                    }
                    if (destStream2 != null) {
                    }
                    if (dstChannel2 == null) {
                    }
                } catch (IOException e17) {
                    boolean isSuccess322 = false;
                    Log.e(TAG, "init IO error, IOException:");
                    if (dstChannel != null) {
                    }
                    if (destStream2 != null) {
                    }
                    if (dstChannel2 == null) {
                    }
                }
            }
        }
        return false;
    }

    private File createDestFile(String filePath) {
        File dest = new File(filePath);
        if (dest.exists()) {
            dest.delete();
        }
        try {
            dest.createNewFile();
            return dest;
        } catch (IOException e) {
            Log.e(TAG, "IOException:");
            return dest;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private FileInputStream getStreamFromPath(Context context, String fileName) {
        ParcelFileDescriptor parcelFileDesc = null;
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(fileName));
            if (0 != 0) {
                try {
                    parcelFileDesc.close();
                } catch (IOException e) {
                    Log.e(TAG, "parcelFileDesc error!");
                }
            }
        } catch (FileNotFoundException e2) {
            Log.e(TAG, "getStreamFromPath FileNotFoundException.");
            if (0 != 0) {
                parcelFileDesc.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    parcelFileDesc.close();
                } catch (IOException e3) {
                    Log.e(TAG, "parcelFileDesc error!");
                }
            }
            throw th;
        }
        return inputStream;
    }

    public void updateWhitelistByHot(Context context, String fileName) {
        new WhitelistUpdateThread(context, fileName).start();
    }

    /* access modifiers changed from: private */
    public static final class GestureNavAttr {
        boolean mIsAction = false;
        String mName = null;

        GestureNavAttr() {
        }

        public String toString() {
            return "name:" + this.mName + " action:" + this.mIsAction;
        }
    }

    private static class WhitelistReadThread extends Thread {
        protected WhitelistReadThread() {
            super("HwGestureNavWhiteConfig update thread");
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            HwGestureNavWhiteConfig.getInstance().initList();
        }
    }

    private class WhitelistUpdateThread extends Thread {
        Context mContext = null;
        String mFileName = null;

        protected WhitelistUpdateThread(Context context, String fileName) {
            super("config update thread");
            this.mContext = context;
            this.mFileName = fileName;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            String str = this.mFileName;
            if (str != null) {
                FileInputStream stream = HwGestureNavWhiteConfig.this.getStreamFromPath(this.mContext, str);
                String target = EnvironmentEx.getDataSystemDirectory() + "/" + HwGestureNavWhiteConfig.CURRENT_ROM_VERSION + AwarenessInnerConstants.DASH_KEY + HwGestureNavWhiteConfig.WHITE_LIST;
                if (stream != null) {
                    try {
                        SlogEx.d(HwGestureNavWhiteConfig.TAG, "target " + target);
                        HwGestureNavWhiteConfig.this.copyFile(stream, target);
                        HwGestureNavWhiteConfig.this.updateConfig();
                    } finally {
                        IoUtilsEx.closeQuietly(stream);
                    }
                }
            }
        }
    }
}
