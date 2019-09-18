package com.android.server.pm;

import android.app.ActivityThread;
import android.app.LoadedApk;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.android.server.job.controllers.JobStatus;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private static final int MAX_APP_MD5_NUMS = 30000;
    private static final int MAX_DELAYED_COMMIT_COUNT = 6;
    private static final int MAX_INPUT_STREAM_LEN = 952320;
    private static final int MAX_SYSTEM_INPUT_STREAM_LEN = 952320;
    private static final int MAX_SYSTEM_MD5_NUMS = 1000;
    private static final int MD5_LEN = 32;
    private static final String PKG_SINAWEIBO = "com.sina.weibolite";
    private static final String PKG_WEBVIEW = "com.google.android.webview";
    private static final int SAVE_DISABLED_MAPLE_PKGS = 1001;
    private static final int SAVE_DISABLED_MAPLE_PKGS_DELAY = 10000;
    private static final String SYSTEM_MD5_FILE = "/system/etc/maple_md5";
    static final String TAG = "HwMaplePMServiceUtils";
    private static final String WEIBO_MD5_FILE_NAME = "maple_bin_check";
    private static boolean appEscapeDisable = "1".equals(SystemProperties.get("persist.mygote.app.escape.disable"));
    private static File disabledMapleBackupFile = new File(DISABLED_MAPLE_BACKUP_FILE);
    private static File disabledMapleFile = new File(DISABLED_MAPLE_FILE);
    /* access modifiers changed from: private */
    public static Set<String> disabledMaplePkgs = new HashSet();
    private static Context mContext = null;
    private static MapleHandler mapleHandler;
    /* access modifiers changed from: private */
    public static int pendingCommit = 0;
    private static String sSystemBaseMd5 = null;

    private static class MapleHandler extends Handler {
        MapleHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1001:
                    synchronized (HwMaplePMServiceUtils.disabledMaplePkgs) {
                        HwMaplePMServiceUtils.saveDisabledMaplePkgs(HwMaplePMServiceUtils.disabledMaplePkgs);
                        int unused = HwMaplePMServiceUtils.pendingCommit = 0;
                        Log.d(HwMaplePMServiceUtils.TAG, HwMaplePMServiceUtils.disabledMaplePkgs.size() + " disabled maple pkgs saved.");
                    }
                    return;
                case HwMaplePMServiceUtils.LOAD_DISABLED_MAPLE_PKGS /*1002*/:
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
                    return;
                default:
                    return;
            }
        }
    }

    public static String getMapleAppType(PackageParser.Package pkg) {
        if (pkg == null || pkg.mAppMetaData == null) {
            return null;
        }
        return pkg.mAppMetaData.getString("com.huawei.maple.flag");
    }

    public static boolean isMapleApk(PackageParser.Package pkg) {
        String type = getMapleAppType(pkg);
        if (type == null || !type.equals("m")) {
            return false;
        }
        return true;
    }

    public static void init(Looper looper, Context context) {
        if (appEscapeDisable) {
            deleteDisabledMapleFile();
            return;
        }
        if (mapleHandler == null) {
            mapleHandler = new MapleHandler(looper);
        }
        mContext = context;
    }

    public static void loadDisabledMaplePkgs() {
        if (!appEscapeDisable && mapleHandler != null) {
            mapleHandler.sendMessage(mapleHandler.obtainMessage(LOAD_DISABLED_MAPLE_PKGS));
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
                    try {
                        disabledMaplePkgs.remove(packageName);
                    } catch (Throwable th) {
                        while (true) {
                            throw th;
                        }
                    }
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
        if (mapleHandler == null) {
            Log.w(TAG, "HwMaplePMServiceUtils has not been initialized!");
            return;
        }
        pendingCommit++;
        mapleHandler.removeMessages(1001);
        if (pendingCommit <= 6) {
            mapleHandler.sendMessageDelayed(mapleHandler.obtainMessage(1001), JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
        } else {
            mapleHandler.sendMessage(mapleHandler.obtainMessage(1001));
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:50:?, code lost:
        r0.close();
     */
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
                int next = parser.next();
                type = next;
                if (next == 2) {
                    break;
                }
            } while (type != 1);
            if (type != 2) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    Log.e(TAG, "Can't close maple PMS file!");
                }
                return;
            }
            int outerDepth = parser.getDepth();
            while (true) {
                int next2 = parser.next();
                int type2 = next2;
                if (next2 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                    try {
                        break;
                    } catch (IOException e4) {
                        Log.e(TAG, "Can't close maple PMS file!");
                    }
                } else if (type2 != 3) {
                    if (type2 != 4) {
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
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00d5  */
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
            if (outputStream != null) {
                outputStream.close();
                disabledMapleBackupFile.delete();
                FileUtils.setPermissions(disabledMapleFile.toString(), 432, -1, -1);
                return;
            }
            if (disabledMapleFile.exists()) {
            }
        } catch (Throwable th) {
            if (outputStream != null) {
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

    private static HashSet<String> getMd5(InputStream in, boolean system) {
        BufferedReader br = null;
        HashSet<String> values = new HashSet<>();
        try {
            br = new BufferedReader(new InputStreamReader(in));
            if (system) {
                String readLine = br.readLine();
                String line = readLine;
                if (readLine != null) {
                    sSystemBaseMd5 = line.length() == 32 ? line : null;
                    if (sSystemBaseMd5 == null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            Log.d(TAG, "close error.");
                        }
                        return null;
                    }
                } else {
                    try {
                        br.close();
                    } catch (IOException e2) {
                        Log.d(TAG, "close error.");
                    }
                    return null;
                }
            } else {
                String readLine2 = br.readLine();
                String line2 = readLine2;
                if (readLine2 != null) {
                    if (sSystemBaseMd5 != null) {
                        if (!line2.equals(sSystemBaseMd5)) {
                        }
                    }
                    Log.d(TAG, "base is not same.");
                    try {
                        br.close();
                    } catch (IOException e3) {
                        Log.d(TAG, "close error.");
                    }
                    return null;
                }
                try {
                    br.close();
                } catch (IOException e4) {
                    Log.d(TAG, "close error.");
                }
                return null;
            }
            while (true) {
                String readLine3 = br.readLine();
                String line3 = readLine3;
                if (readLine3 != null) {
                    String line4 = line3.trim();
                    if (!line4.equals(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS)) {
                        if (!line4.startsWith("#")) {
                            if (line4.length() != 32) {
                                values = null;
                                break;
                            }
                            values.add(line4);
                            if (values.size() > MAX_APP_MD5_NUMS) {
                                Log.d(TAG, "current num : " + values.size() + " max num: " + MAX_APP_MD5_NUMS);
                                values = null;
                                break;
                            }
                        }
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("current line is : ");
                    sb.append(line4.equals(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS) ? "\r" : "#");
                    Log.d(TAG, sb.toString());
                }
            }
            try {
                br.close();
            } catch (IOException e5) {
                Log.d(TAG, "close error.");
            }
        } catch (IOException e6) {
            Log.d(TAG, "read data error.");
            values = null;
            if (br != null) {
                br.close();
            }
        } catch (Throwable th) {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e7) {
                    Log.d(TAG, "close error.");
                }
            }
            throw th;
        }
        return values;
    }

    private static HashSet<String> getSystemMd5() {
        FileInputStream fin = null;
        HashSet<String> ret = null;
        try {
            FileInputStream fin2 = new FileInputStream(new File(SYSTEM_MD5_FILE));
            if (fin2.available() <= 952320) {
                ret = getMd5(fin2, true);
            }
            try {
                fin2.close();
            } catch (IOException e) {
                Log.e(TAG, "close error.");
            }
        } catch (FileNotFoundException e2) {
            Log.e(TAG, "not find.");
            if (fin != null) {
                fin.close();
            }
        } catch (IOException e3) {
            Log.e(TAG, "io error.");
            if (fin != null) {
                fin.close();
            }
        } catch (Throwable th) {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e4) {
                    Log.e(TAG, "close error.");
                }
            }
            throw th;
        }
        return ret;
    }

    private static HashSet<String> getMd5FromApp(ApplicationInfo application) {
        if (application == null || mContext == null) {
            return null;
        }
        HashSet<String> values = null;
        LoadedApk packageInfo = new LoadedApk(ActivityThread.currentActivityThread(), application, null, null, false, false, false);
        InputStream ims = null;
        try {
            ims = packageInfo.getResources().getAssets().open(WEIBO_MD5_FILE_NAME);
            if (ims == null) {
                if (ims != null) {
                    try {
                        ims.close();
                    } catch (IOException e) {
                        Log.e(TAG, "close error.");
                    }
                }
                return null;
            }
            if (ims.available() <= 952320) {
                values = getMd5(ims, false);
            }
            if (ims != null) {
                try {
                    ims.close();
                } catch (IOException e2) {
                    Log.e(TAG, "close error.");
                }
            }
            return values;
        } catch (IOException e3) {
            Log.e(TAG, "error happened.");
            if (ims != null) {
                ims.close();
            }
        } catch (Throwable th) {
            if (ims != null) {
                try {
                    ims.close();
                } catch (IOException e4) {
                    Log.e(TAG, "close error.");
                }
            }
            throw th;
        }
    }

    private static void disableWeiboIfNeeded(boolean needCheck) {
        if (mContext != null) {
            PackageInfo packageInfo = null;
            PackageInfo packageInfo2 = null;
            try {
                PackageManager pm = mContext.getPackageManager();
                if (pm != null) {
                    packageInfo = pm.getPackageInfo(PKG_SINAWEIBO, 0);
                }
                packageInfo2 = packageInfo;
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "can not find the package.");
            }
            if (packageInfo2 != null && packageInfo2.applicationInfo != null && (packageInfo2.applicationInfo.hwFlags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0) {
                if (!needCheck) {
                    setMapleEnableFlag(PKG_SINAWEIBO, false);
                } else {
                    verifyAppMd5(packageInfo2.applicationInfo);
                }
            }
        }
    }

    private static boolean verifyAppMd5(ApplicationInfo applicationInfo) {
        if (applicationInfo == null) {
            return true;
        }
        HashSet<String> sSystemMd5 = getSystemMd5();
        HashSet<String> appMd5s = getMd5FromApp(applicationInfo);
        boolean verified = true;
        String curVal = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        if (appMd5s != null && sSystemMd5 != null) {
            Iterator<String> it = appMd5s.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                String value = it.next();
                if (!sSystemMd5.contains(value)) {
                    verified = false;
                    curVal = value;
                    break;
                }
            }
        } else {
            verified = false;
        }
        if (!verified) {
            if (getMapleEnableFlag(applicationInfo.packageName)) {
                setMapleEnableFlag(applicationInfo.packageName, false);
                Log.d(TAG, "package: " + applicationInfo.packageName + " verify failed at: " + curVal + " disable it.");
            }
        } else if (!getMapleEnableFlag(applicationInfo.packageName)) {
            removeDisabledMaplePkg(applicationInfo.packageName);
        }
        return verified;
    }

    public static boolean verifyMd5(PackageParser.Package pkg) {
        if (appEscapeDisable) {
            return true;
        }
        if (pkg == null || pkg.packageName == null) {
            Log.d(TAG, "parameter error.");
            return true;
        }
        String checkPkg = pkg.packageName;
        boolean isWebView = PKG_WEBVIEW.equals(checkPkg);
        boolean isWeibo = PKG_SINAWEIBO.equals(checkPkg);
        if (!isWebView && !isWeibo) {
            return true;
        }
        if (isWebView) {
            if ((pkg.applicationInfo.flags & 128) != 0) {
                setMapleEnableFlag(checkPkg, false);
                disableWeiboIfNeeded(false);
                Log.d(TAG, "com.google.android.webview disable for update.");
                return false;
            }
            if (!getMapleEnableFlag(checkPkg)) {
                removeDisabledMaplePkg(checkPkg);
                disableWeiboIfNeeded(true);
            }
            return true;
        } else if ((pkg.applicationInfo.hwFlags & DumpState.DUMP_SERVICE_PERMISSIONS) == 0) {
            return true;
        } else {
            if (!isWeibo || getMapleEnableFlag(PKG_WEBVIEW)) {
                return verifyAppMd5(pkg.applicationInfo);
            }
            setMapleEnableFlag(checkPkg, false);
            Log.d(TAG, "com.google.android.webview disable by depended webview.");
            return false;
        }
    }

    public static boolean isWebviewBothApp(PackageSetting ps) {
        if (ps == null || ps.pkg == null || TextUtils.isEmpty(ps.pkg.packageName) || !ps.pkg.packageName.equals(PKG_WEBVIEW) || (ps.pkgFlags & 1) == 0 || (ps.pkgFlags & 128) != 0) {
            return false;
        }
        return true;
    }

    public static boolean isNeedToCopyMapleSO(PackageParser.Package pkg) {
        if (pkg == null) {
            return false;
        }
        boolean isMapleApk = isMapleApk(pkg);
        boolean isSystemApk = (pkg.applicationInfo.flags & 1) != 0;
        if (!isMapleApk || isSystemApk) {
            return false;
        }
        return true;
    }
}
