package com.android.server.pm;

import android.content.pm.PackageParser;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Xml;
import android.webkit.WebViewUpdateService;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.android.server.job.controllers.JobStatus;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class HwMaplePMServiceUtils {
    private static final String DISABLED_MAPLE_BACKUP_FILE = "/data/system/disabled-maple-pkgs-backup.xml";
    private static final String DISABLED_MAPLE_FILE = "/data/system/disabled-maple-pkgs.xml";
    private static final int LOAD_DISABLED_MAPLE_PKGS = 1002;
    private static final int MAX_DELAYED_COMMIT_COUNT = 6;
    private static final int SAVE_DISABLED_MAPLE_PKGS = 1001;
    private static final int SAVE_DISABLED_MAPLE_PKGS_DELAY = 10000;
    static final String TAG = "HwMaplePMServiceUtils";
    private static boolean appEscapeDisable = "1".equals(SystemProperties.get("persist.mygote.app.escape.disable"));
    private static File disabledMapleBackupFile = new File(DISABLED_MAPLE_BACKUP_FILE);
    private static File disabledMapleFile = new File(DISABLED_MAPLE_FILE);
    private static Set<String> disabledMaplePkgs = new HashSet();
    private static MapleHandler mapleHandler;
    private static int pendingCommit = 0;
    private static int webViewInfoFlags = 0;

    public static String getMapleAppType(PackageParser.Package pkg) {
        if (pkg == null || pkg.mAppMetaData == null) {
            return null;
        }
        return pkg.mAppMetaData.getString("com.huawei.maple.flag");
    }

    public static boolean isMapleApk(PackageParser.Package pkg) {
        String type = getMapleAppType(pkg);
        if (type == null) {
            return false;
        }
        if ("m".equals(type) || "mo".equals(type)) {
            return true;
        }
        return false;
    }

    public static String getMapleClassPath(PackageParser.Package pkg) {
        if (pkg == null || pkg.mAppMetaData == null) {
            return null;
        }
        return pkg.mAppMetaData.getString("com.huawei.maple.classpath");
    }

    public static void init(Looper looper) {
        if (appEscapeDisable) {
            deleteDisabledMapleFile();
        } else if (mapleHandler == null) {
            mapleHandler = new MapleHandler(looper);
        }
    }

    public static void loadDisabledMaplePkgs() {
        MapleHandler mapleHandler2;
        if (!appEscapeDisable && (mapleHandler2 = mapleHandler) != null) {
            mapleHandler2.sendMessage(mapleHandler2.obtainMessage(LOAD_DISABLED_MAPLE_PKGS));
        }
    }

    public static void deleteDisabledMapleFile() {
        if (disabledMapleFile.exists()) {
            Log.d(TAG, "Clean up maple PMS file");
            disabledMapleFile.delete();
        }
        if (disabledMapleBackupFile.exists()) {
            Log.d(TAG, "Clean up maple PMS backup file");
            disabledMapleBackupFile.delete();
        }
    }

    public static boolean getMapleEnableFlag(String packageName) {
        boolean enable;
        if (appEscapeDisable) {
            return true;
        }
        synchronized (disabledMaplePkgs) {
            enable = !disabledMaplePkgs.contains(packageName);
        }
        return enable;
    }

    public static void setMapleEnableFlag(String packageName, boolean flag) {
        if (!appEscapeDisable) {
            synchronized (disabledMaplePkgs) {
                if (flag) {
                    disabledMaplePkgs.remove(packageName);
                } else {
                    disabledMaplePkgs.add(packageName);
                }
            }
            commit();
        }
    }

    public static void removeDisabledMaplePkg(String packageName) {
        if (!appEscapeDisable) {
            synchronized (disabledMaplePkgs) {
                disabledMaplePkgs.remove(packageName);
            }
            commit();
        }
    }

    private static void commit() {
        MapleHandler mapleHandler2 = mapleHandler;
        if (mapleHandler2 == null) {
            Log.w(TAG, "HwMaplePMServiceUtils has not been initialized!");
            return;
        }
        pendingCommit++;
        mapleHandler2.removeMessages(1001);
        if (pendingCommit <= 6) {
            MapleHandler mapleHandler3 = mapleHandler;
            mapleHandler3.sendMessageDelayed(mapleHandler3.obtainMessage(1001), JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
            return;
        }
        MapleHandler mapleHandler4 = mapleHandler;
        mapleHandler4.sendMessage(mapleHandler4.obtainMessage(1001));
    }

    /* access modifiers changed from: private */
    public static void loadDisabledMaplePkgs(Set<String> disabledMaplePkgs2) {
        int type;
        BufferedInputStream inputStream = null;
        if (disabledMapleBackupFile.exists()) {
            try {
                inputStream = new BufferedInputStream(new FileInputStream(disabledMapleBackupFile));
                Log.d(TAG, "Loading from maple PMS backup file");
                if (disabledMapleFile.exists()) {
                    Log.d(TAG, "Clean up maple PMS file");
                    disabledMapleFile.delete();
                }
            } catch (IOException e) {
                Log.e(TAG, "Can't open maple PMS backup file!");
            }
        }
        if (inputStream == null) {
            if (!disabledMapleFile.exists()) {
                Log.w(TAG, "Maple PMS file not exists!");
                return;
            }
            try {
                inputStream = new BufferedInputStream(new FileInputStream(disabledMapleFile));
                Log.d(TAG, "Loading from maple PMS file");
            } catch (IOException e2) {
                Log.e(TAG, "Can't open maple PMS file!");
                return;
            }
        }
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(inputStream, StandardCharsets.UTF_8.name());
            do {
                type = parser.next();
                if (type == 2) {
                    break;
                }
            } while (type != 1);
            if (type != 2) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    Log.e(TAG, "Can't close maple PMS file!");
                }
            } else {
                int outerDepth = parser.getDepth();
                while (true) {
                    int type2 = parser.next();
                    if (type2 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                        try {
                            inputStream.close();
                            return;
                        } catch (IOException e4) {
                            Log.e(TAG, "Can't close maple PMS file!");
                            return;
                        }
                    } else if (!(type2 == 3 || type2 == 4)) {
                        if (parser.getName().equals("package")) {
                            String pkg = parser.getAttributeValue(null, Settings.ATTR_NAME);
                            if (pkg != null) {
                                disabledMaplePkgs2.add(pkg);
                            }
                        } else {
                            XmlUtils.skipCurrentTag(parser);
                        }
                    }
                }
            }
        } catch (XmlPullParserException e5) {
            Log.e(TAG, "Parse disabled maple pkgs file failed!");
            inputStream.close();
        } catch (IOException e6) {
            Log.e(TAG, "Load disabled maple pkgs failed!");
            inputStream.close();
        } catch (Throwable th) {
            try {
                inputStream.close();
            } catch (IOException e7) {
                Log.e(TAG, "Can't close maple PMS file!");
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public static void saveDisabledMaplePkgs(Set<String> disabledMaplePkgs2) {
        if (disabledMapleFile.exists()) {
            if (disabledMapleBackupFile.exists()) {
                Log.d(TAG, "Clean up maple PMS file");
                disabledMapleFile.delete();
            } else if (!disabledMapleFile.renameTo(disabledMapleBackupFile)) {
                Log.e(TAG, "Unable to backup maple PMS file, current changes will be lost at reboot");
                return;
            }
        }
        BufferedOutputStream outputStream = null;
        try {
            FileOutputStream fstr = new FileOutputStream(disabledMapleFile);
            BufferedOutputStream outputStream2 = new BufferedOutputStream(fstr);
            XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(outputStream2, StandardCharsets.UTF_8.name());
            serializer.startDocument(null, true);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag(null, "packages");
            for (String pkg : disabledMaplePkgs2) {
                serializer.startTag(null, "package");
                serializer.attribute(null, Settings.ATTR_NAME, pkg);
                serializer.endTag(null, "package");
            }
            serializer.endTag(null, "packages");
            serializer.endDocument();
            outputStream2.flush();
            FileUtils.sync(fstr);
            try {
                outputStream2.close();
                disabledMapleBackupFile.delete();
                FileUtils.setPermissions(disabledMapleFile.toString(), 432, -1, -1);
            } catch (IOException e) {
                Log.e(TAG, "Can't close maple PMS file, save xml fail!");
                if (disabledMapleFile.exists()) {
                    Log.d(TAG, "Clean up maple PMS file");
                    disabledMapleFile.delete();
                }
            }
        } catch (IOException e2) {
            Log.e(TAG, "Can't open maple PMS file, save xml fail!");
            if (0 != 0) {
                outputStream.close();
                disabledMapleBackupFile.delete();
                FileUtils.setPermissions(disabledMapleFile.toString(), 432, -1, -1);
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    outputStream.close();
                    disabledMapleBackupFile.delete();
                    FileUtils.setPermissions(disabledMapleFile.toString(), 432, -1, -1);
                    return;
                } catch (IOException e3) {
                    Log.e(TAG, "Can't close maple PMS file, save xml fail!");
                    throw th;
                }
            }
            throw th;
        }
    }

    public static void dumpDisabledMaplePkgs(PrintWriter pw) {
        if (appEscapeDisable) {
            pw.println("App escape disabled");
            return;
        }
        pw.println("Disabled maple app:");
        synchronized (disabledMaplePkgs) {
            Iterator<String> it = disabledMaplePkgs.iterator();
            while (it.hasNext()) {
                pw.println("  " + it.next());
            }
        }
        pw.println("Pending commit:" + pendingCommit);
    }

    /* access modifiers changed from: private */
    public static class MapleHandler extends Handler {
        MapleHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1001) {
                synchronized (HwMaplePMServiceUtils.disabledMaplePkgs) {
                    HwMaplePMServiceUtils.saveDisabledMaplePkgs(HwMaplePMServiceUtils.disabledMaplePkgs);
                    int unused = HwMaplePMServiceUtils.pendingCommit = 0;
                    Log.d(HwMaplePMServiceUtils.TAG, HwMaplePMServiceUtils.disabledMaplePkgs.size() + " disabled maple pkgs saved.");
                }
            } else if (i == HwMaplePMServiceUtils.LOAD_DISABLED_MAPLE_PKGS) {
                if (HwMaplePMServiceUtils.pendingCommit == 0) {
                    synchronized (HwMaplePMServiceUtils.disabledMaplePkgs) {
                        HwMaplePMServiceUtils.disabledMaplePkgs.clear();
                        HwMaplePMServiceUtils.loadDisabledMaplePkgs(HwMaplePMServiceUtils.disabledMaplePkgs);
                        Log.d(HwMaplePMServiceUtils.TAG, "Loaded " + HwMaplePMServiceUtils.disabledMaplePkgs.size() + " disabled maple pkgs.");
                    }
                    return;
                }
                removeMessages(1001);
                sendMessage(obtainMessage(1001));
                sendMessage(obtainMessage(HwMaplePMServiceUtils.LOAD_DISABLED_MAPLE_PKGS));
            }
        }
    }

    public static boolean isNeedToCopyMapleSo(PackageParser.Package pkg) {
        if (pkg == null) {
            return false;
        }
        boolean isMaple = isMapleApk(pkg);
        boolean isSystemApp = (pkg.applicationInfo.flags & 1) != 0;
        boolean isUpdateSystemapp = pkg.isUpdatedSystemApp();
        if (!isMaple) {
            return false;
        }
        if (!isSystemApp || isUpdateSystemapp) {
            return true;
        }
        return false;
    }

    public static boolean isMaplePkgGcOnly(PackageParser.Package pkg) {
        String type;
        if (pkg == null || pkg.mAppMetaData == null || (type = pkg.mAppMetaData.getString("ArkGcType")) == null) {
            return false;
        }
        return "gconly".equals(type);
    }

    public static void setWebViewInfoFlags(int appInfoFlags) {
        webViewInfoFlags = appInfoFlags;
    }

    public static int getWebViewInfoFlags() {
        return webViewInfoFlags;
    }

    public static boolean getDependWebView(PackageParser.Package pkg) {
        if (appEscapeDisable || pkg == null || pkg.mAppMetaData == null) {
            return false;
        }
        return pkg.mAppMetaData.getBoolean("com.huawei.dependsWebView", false);
    }

    public static boolean isHwWebViewAndMaple() {
        if (!"com.huawei.webview".equals(WebViewUpdateService.getCurrentWebViewPackageName())) {
            return false;
        }
        int webViewAppInfo = getWebViewInfoFlags();
        Log.d(TAG, "isHwWebViewAndMaple webView webViewAppInfo " + webViewAppInfo);
        if ((16777216 & webViewAppInfo) == 0 && (4194304 & webViewAppInfo) == 0) {
            return false;
        }
        return true;
    }
}
