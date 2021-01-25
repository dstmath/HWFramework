package com.android.server.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Flog;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.server.hidata.appqoe.HwAPPQoEUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class HwUninstalledAppManager {
    private static final String DATA_APP_DIR = "/data/app/";
    private static final String DATA_DATA_DIR = "/data/data/";
    private static final String FLAG_XML_CONFIG_STRING = "string";
    private static final String FLAG_XML_CONFIG_VALUE = "values";
    private static final boolean IS_DEBUG = SystemProperties.get("ro.dbg.pms_log", "0").equals("on");
    private static final String TAG = "HwUninstalledAppManager";
    static final Map<String, String> UNINSTALLED_APPS = new HashMap();
    private static final String UNINSTALLED_DELAPP_DIR = "/data/system/";
    private static final String UNINSTALLED_DELAPP_FILE = "uninstalled_delapp.xml";
    static final List<String> UNINSTALLED_DEL_APPS = new ArrayList();
    static final Map<String, String> UNINSTALLED_RENAMED_APPS = new HashMap();
    private static volatile HwUninstalledAppManager sInstance;
    static List<String> sOldDataBackups = new ArrayList();
    private IHwPackageManagerServiceEx mHwPmsEx;
    private IHwPackageManagerServiceExInner mHwPmsExInner;

    private HwUninstalledAppManager(IHwPackageManagerServiceExInner pmsExInner, IHwPackageManagerServiceEx pmsEx) {
        this.mHwPmsExInner = pmsExInner;
        this.mHwPmsEx = pmsEx;
    }

    public static synchronized HwUninstalledAppManager getInstance(IHwPackageManagerServiceExInner pmsExInner, IHwPackageManagerServiceEx pmsEx) {
        HwUninstalledAppManager hwUninstalledAppManager;
        synchronized (HwUninstalledAppManager.class) {
            if (sInstance == null) {
                sInstance = new HwUninstalledAppManager(pmsExInner, pmsEx);
            }
            hwUninstalledAppManager = sInstance;
        }
        return hwUninstalledAppManager;
    }

    public void loadCorrectUninstallDelapp() {
        if (this.mHwPmsExInner.getIPmsInner().getIsPreNUpgradeInner()) {
            File fileExt = new File(DATA_DATA_DIR, UNINSTALLED_DELAPP_FILE);
            if (fileExt.exists()) {
                loadUninstalledDelApp(fileExt, false);
                Slog.w(TAG, "Compatible Fix for pre-N update verify uninstalled App!");
            }
        }
        File file = new File("/data/system/", UNINSTALLED_DELAPP_FILE);
        if (file.exists()) {
            try {
                loadUninstalledDelApp(file);
            } catch (IndexOutOfBoundsException e) {
                Slog.w(TAG, "load uninstalld delapp fail, try another way!");
                loadUninstalledDelApp(file, false);
            }
        }
    }

    public void addUninstallDataToCache(String packageName, String codePath) {
        if (packageName == null || codePath == null || codePath.startsWith(DATA_APP_DIR)) {
            Slog.d(TAG, "Add path to cache failed!");
            return;
        }
        if (!UNINSTALLED_DEL_APPS.contains(packageName)) {
            UNINSTALLED_DEL_APPS.add(packageName);
        }
        UNINSTALLED_APPS.put(packageName, codePath);
        Slog.i(TAG, "Add path to cache packageName:" + packageName + ",codePath:" + codePath);
    }

    public void removeFromUninstalledDelapp(String packageName) {
        if (UNINSTALLED_DEL_APPS.contains(packageName)) {
            UNINSTALLED_DEL_APPS.remove(packageName);
            UNINSTALLED_APPS.remove(packageName);
            recordUninstalledDelapp(null, null);
        }
    }

    public void recordUninstalledDelapp(String packageName, String path) {
        Throwable th;
        String oldPackageName;
        String str;
        if (UNINSTALLED_DEL_APPS.contains(packageName)) {
            Slog.d(TAG, "duplicate recordUninstalledDelapp here, return!");
            return;
        }
        File file = new File("/data/system/", UNINSTALLED_DELAPP_FILE);
        if (packageName != null) {
            loadUninstalledDelApp(file);
        }
        try {
            OutputStream stream = new FileOutputStream(file, false);
            try {
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(stream, "utf-8");
                out.startDocument(null, true);
                out.startTag(null, FLAG_XML_CONFIG_VALUE);
                boolean isRenamePackage = isRenamePackage(packageName);
                if (isRenamePackage) {
                    try {
                        oldPackageName = getOldPackageName(packageName);
                    } catch (Throwable th2) {
                        th = th2;
                    }
                } else {
                    oldPackageName = null;
                }
                String str2 = "name";
                if (packageName != null) {
                    out.startTag(null, FLAG_XML_CONFIG_STRING);
                    out.attribute(null, str2, packageName);
                    out.attribute(null, "codePath", path);
                    if (isRenamePackage) {
                        Slog.i(TAG, "the uninstalled app " + packageName + " is the renamed package. old package name is:" + oldPackageName);
                        out.attribute(null, "oldName", oldPackageName);
                    }
                    out.endTag(null, FLAG_XML_CONFIG_STRING);
                }
                for (Iterator<String> it = UNINSTALLED_DEL_APPS.iterator(); it.hasNext(); it = it) {
                    String item = it.next();
                    try {
                        out.startTag(null, FLAG_XML_CONFIG_STRING);
                        out.attribute(null, str2, item);
                        out.attribute(null, "codePath", UNINSTALLED_APPS.get(item));
                        String oldName = getOldPackageName(item);
                        if (!TextUtils.isEmpty(oldName)) {
                            str = null;
                            out.attribute(null, "oldName", oldName);
                        } else {
                            str = null;
                        }
                        out.endTag(str, FLAG_XML_CONFIG_STRING);
                        str2 = str2;
                        file = file;
                    } catch (Throwable th3) {
                        th = th3;
                        try {
                            throw th;
                        } catch (Throwable th4) {
                            $closeResource(th, stream);
                            throw th4;
                        }
                    }
                }
                out.endTag(null, FLAG_XML_CONFIG_VALUE);
                out.endDocument();
                if (packageName != null) {
                    UNINSTALLED_DEL_APPS.add(packageName);
                    UNINSTALLED_APPS.put(packageName, path);
                    if (isRenamePackage) {
                        UNINSTALLED_RENAMED_APPS.put(oldPackageName, packageName);
                    }
                }
            } catch (Throwable th5) {
                th = th5;
                throw th;
            }
            try {
                $closeResource(null, stream);
            } catch (IOException e) {
            } catch (Exception e2) {
                Slog.w(TAG, "recordUninstalledDelapp, failed parsing catch Exception.");
            }
        } catch (IOException e3) {
            Slog.w(TAG, "parsing failed! IOException.");
        } catch (Exception e4) {
            Slog.w(TAG, "recordUninstalledDelapp, failed parsing catch Exception.");
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    private boolean isRenamePackage(String packageName) {
        if (packageName == null) {
            return false;
        }
        Map<String, String> renamedPackages = new HashMap<>();
        renamedPackages.putAll(new HashMap<>(this.mHwPmsEx.getHwRenamedPackages(4)));
        renamedPackages.putAll(new HashMap<>(UNINSTALLED_RENAMED_APPS));
        if (renamedPackages.size() != 0 && renamedPackages.containsValue(packageName)) {
            return true;
        }
        return false;
    }

    private String getOldPackageName(String packageName) {
        Map<String, String> renamedPackages = new HashMap<>();
        renamedPackages.putAll(new HashMap<>(this.mHwPmsEx.getHwRenamedPackages(4)));
        renamedPackages.putAll(new HashMap<>(UNINSTALLED_RENAMED_APPS));
        for (Map.Entry<String, String> renamedPackage : renamedPackages.entrySet()) {
            if (packageName.equals(renamedPackage.getValue())) {
                return renamedPackage.getKey();
            }
        }
        return "";
    }

    private void loadUninstalledDelApp(File file) {
        loadUninstalledDelApp(file, true);
    }

    /* JADX WARNING: Removed duplicated region for block: B:101:0x01a3  */
    /* JADX WARNING: Removed duplicated region for block: B:104:0x01ae  */
    /* JADX WARNING: Removed duplicated region for block: B:117:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:118:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:119:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:120:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x0155  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x016f  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x0189  */
    private void loadUninstalledDelApp(File file, boolean isIncludeCodePath) {
        Throwable th;
        StringBuilder sb;
        Exception e;
        Throwable th2;
        XmlPullParser parser;
        int type;
        int i;
        boolean isSuccess = true;
        String exceptionName = "";
        Map<String, String> unistalledMap = new HashMap<>();
        unistalledMap.putAll(UNINSTALLED_APPS);
        UNINSTALLED_DEL_APPS.clear();
        UNINSTALLED_APPS.clear();
        String str = null;
        try {
            try {
                FileInputStream stream = new FileInputStream(file);
                try {
                    parser = Xml.newPullParser();
                    parser.setInput(stream, null);
                    type = parser.next();
                    String tag = parser.getName();
                    if (FLAG_XML_CONFIG_VALUE.equals(tag)) {
                        parser.next();
                        int outerDepth = parser.getDepth();
                        while (true) {
                            int type2 = parser.next();
                            if (type2 == i) {
                                break;
                            }
                            if (type2 == 3) {
                                if (parser.getDepth() <= outerDepth) {
                                    break;
                                }
                            }
                            if (type2 != 3) {
                                if (type2 != 4) {
                                    if (FLAG_XML_CONFIG_STRING.equals(parser.getName())) {
                                        String pkgName = parser.getAttributeValue(str, "name");
                                        if (!TextUtils.isEmpty(pkgName)) {
                                            try {
                                                loadUninstalledDelApp(pkgName.intern(), parser, isIncludeCodePath, unistalledMap);
                                            } catch (Throwable th3) {
                                                th2 = th3;
                                            }
                                        }
                                    }
                                }
                            }
                            str = null;
                            i = 1;
                        }
                        try {
                            $closeResource(null, stream);
                            if (1 == 0) {
                                sb = new StringBuilder();
                                sb.append("Error reading file:");
                                sb.append(file.getName());
                                sb.append(", occur to ");
                                sb.append(exceptionName);
                                Slog.w(TAG, sb.toString());
                                HwPackageManagerServiceUtils.reportPmsParseFileException(file.getName(), exceptionName, -1, (String) null);
                                return;
                            }
                            return;
                        } catch (FileNotFoundException e2) {
                            exceptionName = "FileNotFoundException";
                            Slog.w(TAG, "FileNotFoundException. file is not exist.");
                            if (0 == 0) {
                                sb = new StringBuilder();
                                sb.append("Error reading file:");
                                sb.append(file.getName());
                                sb.append(", occur to ");
                                sb.append(exceptionName);
                                Slog.w(TAG, sb.toString());
                                HwPackageManagerServiceUtils.reportPmsParseFileException(file.getName(), exceptionName, -1, (String) null);
                                return;
                            }
                            return;
                        } catch (XmlPullParserException e3) {
                            exceptionName = "XmlPullParserException";
                            Slog.w(TAG, "XmlPullParserException. failed parsing catch XmlPullParserException.");
                            if (0 == 0) {
                                sb = new StringBuilder();
                                sb.append("Error reading file:");
                                sb.append(file.getName());
                                sb.append(", occur to ");
                                sb.append(exceptionName);
                                Slog.w(TAG, sb.toString());
                                HwPackageManagerServiceUtils.reportPmsParseFileException(file.getName(), exceptionName, -1, (String) null);
                                return;
                            }
                            return;
                        } catch (IOException e4) {
                            exceptionName = "IOException";
                            Slog.w(TAG, "IOException. failed parsing catch IOException.");
                            if (0 == 0) {
                                sb = new StringBuilder();
                                sb.append("Error reading file:");
                                sb.append(file.getName());
                                sb.append(", occur to ");
                                sb.append(exceptionName);
                                Slog.w(TAG, sb.toString());
                                HwPackageManagerServiceUtils.reportPmsParseFileException(file.getName(), exceptionName, -1, (String) null);
                                return;
                            }
                            return;
                        } catch (Exception e5) {
                            e = e5;
                            isSuccess = false;
                            try {
                                exceptionName = e.getClass().toString();
                                Slog.w(TAG, "Exception. failed parsing catch " + exceptionName);
                                if (0 == 0) {
                                    sb = new StringBuilder();
                                    sb.append("Error reading file:");
                                    sb.append(file.getName());
                                    sb.append(", occur to ");
                                    sb.append(exceptionName);
                                    Slog.w(TAG, sb.toString());
                                    HwPackageManagerServiceUtils.reportPmsParseFileException(file.getName(), exceptionName, -1, (String) null);
                                    return;
                                }
                                return;
                            } catch (Throwable th4) {
                                th = th4;
                            }
                        }
                    } else {
                        throw new XmlPullParserException("Settings do not start with policies tag: found " + tag);
                    }
                } catch (Throwable th5) {
                    th2 = th5;
                    try {
                        throw th2;
                    } catch (Throwable th6) {
                        $closeResource(th2, stream);
                        throw th6;
                    }
                }
            } catch (FileNotFoundException e6) {
                exceptionName = "FileNotFoundException";
                Slog.w(TAG, "FileNotFoundException. file is not exist.");
                if (0 == 0) {
                }
            } catch (XmlPullParserException e7) {
                exceptionName = "XmlPullParserException";
                Slog.w(TAG, "XmlPullParserException. failed parsing catch XmlPullParserException.");
                if (0 == 0) {
                }
            } catch (IOException e8) {
                exceptionName = "IOException";
                Slog.w(TAG, "IOException. failed parsing catch IOException.");
                if (0 == 0) {
                }
            } catch (Exception e9) {
                e = e9;
                isSuccess = false;
                exceptionName = e.getClass().toString();
                Slog.w(TAG, "Exception. failed parsing catch " + exceptionName);
                if (0 == 0) {
                }
            } catch (Throwable th7) {
                th = th7;
                if (!isSuccess) {
                }
                throw th;
            }
        } catch (FileNotFoundException e10) {
            exceptionName = "FileNotFoundException";
            Slog.w(TAG, "FileNotFoundException. file is not exist.");
            if (0 == 0) {
            }
        } catch (XmlPullParserException e11) {
            exceptionName = "XmlPullParserException";
            Slog.w(TAG, "XmlPullParserException. failed parsing catch XmlPullParserException.");
            if (0 == 0) {
            }
        } catch (IOException e12) {
            exceptionName = "IOException";
            Slog.w(TAG, "IOException. failed parsing catch IOException.");
            if (0 == 0) {
            }
        } catch (Exception e13) {
            e = e13;
            isSuccess = false;
            exceptionName = e.getClass().toString();
            Slog.w(TAG, "Exception. failed parsing catch " + exceptionName);
            if (0 == 0) {
            }
        } catch (Throwable th8) {
            th = th8;
            if (!isSuccess) {
                Slog.w(TAG, "Error reading file:" + file.getName() + ", occur to " + exceptionName);
                HwPackageManagerServiceUtils.reportPmsParseFileException(file.getName(), exceptionName, -1, (String) null);
            }
            throw th;
        }
        while (true) {
            i = 1;
            if (type == 1 || type == 2) {
                break;
            }
            type = parser.next();
        }
    }

    private void loadUninstalledDelApp(String pkgName, XmlPullParser parser, boolean isIncludeCodePath, Map<String, String> unistalledMap) {
        if (!isIncludeCodePath) {
            sOldDataBackups.add(pkgName);
            return;
        }
        String codePath = parser.getAttributeValue(null, "codePath");
        if (TextUtils.isEmpty(codePath)) {
            codePath = unistalledMap.get(pkgName);
        }
        if (!TextUtils.isEmpty(codePath)) {
            UNINSTALLED_DEL_APPS.add(pkgName);
            UNINSTALLED_APPS.put(pkgName, codePath);
            String oldPkgName = parser.getAttributeValue(null, "oldName");
            if (!TextUtils.isEmpty(oldPkgName)) {
                UNINSTALLED_RENAMED_APPS.put(oldPkgName, pkgName);
            }
        }
    }

    private boolean isUninstalledDelapp(String s) {
        if (sOldDataBackups.size() != 0) {
            return sOldDataBackups.contains(s);
        }
        if (UNINSTALLED_DEL_APPS.size() != 0) {
            return UNINSTALLED_DEL_APPS.contains(s);
        }
        return false;
    }

    public List<String> getScanInstallList() {
        if (UNINSTALLED_APPS.size() == 0) {
            return new ArrayList(0);
        }
        List<String> installList = new ArrayList<>();
        int currentUserId = UserHandle.getCallingUserId();
        IHwPackageManagerInner pmsInner = this.mHwPmsExInner.getIPmsInner();
        for (String str : UNINSTALLED_APPS.keySet()) {
            PackageSetting psTemp = pmsInner.getSettings().getPackageLPr(str);
            if (psTemp == null || !psTemp.getInstalled(currentUserId)) {
                installList.add(UNINSTALLED_APPS.get(str));
            }
        }
        return installList;
    }

    public List<String> getOldDataBackup() {
        return sOldDataBackups;
    }

    public Map<String, String> getUninstalledMap() {
        return UNINSTALLED_APPS;
    }

    private boolean isApplicationInstalled(PackageParser.Package pkg) {
        int userId = UserHandle.getCallingUserId();
        IHwPackageManagerInner pmsInner = this.mHwPmsExInner.getIPmsInner();
        PackageSetting ps = (PackageSetting) pmsInner.getSettings().mPackages.get(pkg.applicationInfo.packageName);
        if (ps == null) {
            if (IS_DEBUG) {
                Log.w(TAG, "isApplicationInstalled PackageSetting is null, return false");
            }
            return false;
        } else if (ps.pkg == null) {
            if (IS_DEBUG) {
                Log.w(TAG, "isApplicationInstalled pkg is null, return false");
            }
            return false;
        } else {
            ApplicationInfo info = pmsInner.getApplicationInfo(pkg.applicationInfo.packageName, 8192, userId);
            if (IS_DEBUG) {
                Log.e(TAG, "isApplicationInstalled: pkg " + pkg + ", applicationInfo " + info + ", packageSetting " + ps);
            }
            if (info != null) {
                return true;
            }
            return false;
        }
    }

    public boolean needInstallRemovablePreApk(PackageParser.Package pkg, int hwFlags) {
        if (pkg == null) {
            return false;
        }
        if ((33554432 & hwFlags) == 0 || isApplicationInstalled(pkg) || !isUninstalledDelapp(pkg.packageName)) {
            return true;
        }
        Flog.i((int) HwAPPQoEUtils.MSG_CHR_CELL_GOOD_AFTER_MPLINK, "needInstallRemovablePreApk :" + pkg.packageName);
        return false;
    }

    public Map<String, String> getUninstalledRenamedPackagesMaps() {
        return UNINSTALLED_RENAMED_APPS;
    }
}
