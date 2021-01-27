package com.android.server.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParserEx;
import android.text.TextUtils;
import android.util.Flog;
import android.util.Log;
import android.util.Xml;
import com.huawei.android.internal.util.FastXmlSerializerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.util.SlogEx;
import com.huawei.hwpartbasicplatformservices.BuildConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwUninstalledAppManager {
    private static final String DATA_APP_DIR = "/data/app/";
    private static final String DATA_DATA_DIR = "/data/data/";
    private static final String FLAG_XML_CONFIG_STRING = "string";
    private static final String FLAG_XML_CONFIG_VALUE = "values";
    private static final boolean IS_DEBUG = SystemPropertiesEx.get("ro.dbg.pms_log", "0").equals("on");
    private static final String TAG = "HwUninstalledAppManager";
    static final Map<String, String> UNINSTALLED_APPS = new HashMap();
    private static final String UNINSTALLED_DELAPP_DIR = "/data/system/";
    private static final String UNINSTALLED_DELAPP_FILE = "uninstalled_delapp.xml";
    static final List<String> UNINSTALLED_DEL_APPS = new ArrayList();
    static final Map<String, String> UNINSTALLED_RENAMED_APPS = new HashMap();
    private static volatile HwUninstalledAppManager sInstance;
    static List<String> sOldDataBackups = new ArrayList();
    private DefaultHwPackageManagerServiceExt mHwPmsEx;
    private IHwPackageManagerServiceExInner mHwPmsExInner;

    private HwUninstalledAppManager(IHwPackageManagerServiceExInner pmsExInner, DefaultHwPackageManagerServiceExt pmsEx) {
        this.mHwPmsExInner = pmsExInner;
        this.mHwPmsEx = pmsEx;
    }

    public static synchronized HwUninstalledAppManager getInstance(IHwPackageManagerServiceExInner pmsExInner, DefaultHwPackageManagerServiceExt pmsEx) {
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
                SlogEx.w(TAG, "Compatible Fix for pre-N update verify uninstalled App!");
            }
        }
        File file = new File("/data/system/", UNINSTALLED_DELAPP_FILE);
        if (file.exists()) {
            try {
                loadUninstalledDelApp(file);
            } catch (IndexOutOfBoundsException e) {
                SlogEx.w(TAG, "load uninstalld delapp fail, try another way!");
                loadUninstalledDelApp(file, false);
            }
        }
    }

    public void addUninstallDataToCache(String packageName, String codePath) {
        if (packageName == null || codePath == null || codePath.startsWith(DATA_APP_DIR)) {
            SlogEx.d(TAG, "Add path to cache failed!");
            return;
        }
        if (!UNINSTALLED_DEL_APPS.contains(packageName)) {
            UNINSTALLED_DEL_APPS.add(packageName);
        }
        UNINSTALLED_APPS.put(packageName, codePath);
        SlogEx.i(TAG, "Add path to cache packageName:" + packageName + ",codePath:" + codePath);
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
            SlogEx.d(TAG, "duplicate recordUninstalledDelapp here, return!");
            return;
        }
        File file = new File("/data/system/", UNINSTALLED_DELAPP_FILE);
        if (packageName != null) {
            loadUninstalledDelApp(file);
        }
        try {
            FileOutputStream stream = new FileOutputStream(file, false);
            try {
                FastXmlSerializerEx out = new FastXmlSerializerEx();
                out.setOutput(stream, "utf-8");
                out.startDocument((String) null, true);
                out.startTag((String) null, FLAG_XML_CONFIG_VALUE);
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
                    out.startTag((String) null, FLAG_XML_CONFIG_STRING);
                    out.attribute((String) null, str2, packageName);
                    out.attribute((String) null, "codePath", path);
                    if (isRenamePackage) {
                        SlogEx.i(TAG, "the uninstalled app " + packageName + " is the renamed package. old package name is:" + oldPackageName);
                        out.attribute((String) null, "oldName", oldPackageName);
                    }
                    out.endTag((String) null, FLAG_XML_CONFIG_STRING);
                }
                for (Iterator<String> it = UNINSTALLED_DEL_APPS.iterator(); it.hasNext(); it = it) {
                    String item = it.next();
                    try {
                        out.startTag((String) null, FLAG_XML_CONFIG_STRING);
                        out.attribute((String) null, str2, item);
                        out.attribute((String) null, "codePath", UNINSTALLED_APPS.get(item));
                        String oldName = getOldPackageName(item);
                        if (!TextUtils.isEmpty(oldName)) {
                            str = null;
                            out.attribute((String) null, "oldName", oldName);
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
                            th.addSuppressed(th4);
                        }
                    }
                }
                out.endTag((String) null, FLAG_XML_CONFIG_VALUE);
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
                stream.close();
                return;
            } catch (IOException e) {
            } catch (Exception e2) {
                SlogEx.w(TAG, "recordUninstalledDelapp, failed parsing catch Exception.");
                return;
            }
        } catch (IOException e3) {
            SlogEx.w(TAG, "parsing failed! IOException.");
            return;
        } catch (Exception e4) {
            SlogEx.w(TAG, "recordUninstalledDelapp, failed parsing catch Exception.");
            return;
        }
        throw th;
    }

    private boolean isRenamePackage(String packageName) {
        if (packageName == null) {
            return false;
        }
        Map<String, String> renamedPackages = new HashMap<>();
        renamedPackages.putAll(new HashMap<>(this.mHwPmsEx.getHwRenamedPackages(HwRenamedPackagePolicyEx.EXCLUSIVE_INSTALL)));
        renamedPackages.putAll(new HashMap<>(UNINSTALLED_RENAMED_APPS));
        if (renamedPackages.size() != 0 && renamedPackages.containsValue(packageName)) {
            return true;
        }
        return false;
    }

    private String getOldPackageName(String packageName) {
        Map<String, String> renamedPackages = new HashMap<>();
        renamedPackages.putAll(new HashMap<>(this.mHwPmsEx.getHwRenamedPackages(HwRenamedPackagePolicyEx.EXCLUSIVE_INSTALL)));
        renamedPackages.putAll(new HashMap<>(UNINSTALLED_RENAMED_APPS));
        for (Map.Entry<String, String> renamedPackage : renamedPackages.entrySet()) {
            if (packageName.equals(renamedPackage.getValue())) {
                return renamedPackage.getKey();
            }
        }
        return BuildConfig.FLAVOR;
    }

    private void loadUninstalledDelApp(File file) {
        loadUninstalledDelApp(file, true);
    }

    /* JADX WARNING: Removed duplicated region for block: B:104:0x01a6  */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x01b1  */
    /* JADX WARNING: Removed duplicated region for block: B:120:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:121:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:122:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:123:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x0158  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x0172  */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x018c  */
    private void loadUninstalledDelApp(File file, boolean isIncludeCodePath) {
        Throwable th;
        StringBuilder sb;
        Exception e;
        Throwable th2;
        XmlPullParser parser;
        int type;
        int i;
        boolean isSuccess = true;
        String exceptionName = BuildConfig.FLAVOR;
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
                            stream.close();
                            if (1 == 0) {
                                sb = new StringBuilder();
                                sb.append("Error reading file:");
                                sb.append(file.getName());
                                sb.append(", occur to ");
                                sb.append(exceptionName);
                                SlogEx.w(TAG, sb.toString());
                                HwPackageManagerServiceUtilsEx.reportPmsParseFileException(file.getName(), exceptionName, -1, (String) null);
                                return;
                            }
                            return;
                        } catch (FileNotFoundException e2) {
                            exceptionName = "FileNotFoundException";
                            SlogEx.w(TAG, "FileNotFoundException. file is not exist.");
                            if (0 == 0) {
                            }
                        } catch (XmlPullParserException e3) {
                            exceptionName = "XmlPullParserException";
                            SlogEx.w(TAG, "XmlPullParserException. failed parsing catch XmlPullParserException.");
                            if (0 == 0) {
                            }
                        } catch (IOException e4) {
                            exceptionName = "IOException";
                            SlogEx.w(TAG, "IOException. failed parsing catch IOException.");
                            if (0 == 0) {
                            }
                        } catch (Exception e5) {
                            e = e5;
                            isSuccess = false;
                            try {
                                exceptionName = e.getClass().toString();
                                SlogEx.w(TAG, "Exception. failed parsing catch " + exceptionName);
                                if (0 == 0) {
                                }
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
                        th2.addSuppressed(th6);
                    }
                }
                throw th;
            } catch (FileNotFoundException e6) {
                exceptionName = "FileNotFoundException";
                SlogEx.w(TAG, "FileNotFoundException. file is not exist.");
                if (0 == 0) {
                }
            } catch (XmlPullParserException e7) {
                exceptionName = "XmlPullParserException";
                SlogEx.w(TAG, "XmlPullParserException. failed parsing catch XmlPullParserException.");
                if (0 == 0) {
                }
            } catch (IOException e8) {
                exceptionName = "IOException";
                SlogEx.w(TAG, "IOException. failed parsing catch IOException.");
                if (0 == 0) {
                }
            } catch (Exception e9) {
                e = e9;
                isSuccess = false;
                exceptionName = e.getClass().toString();
                SlogEx.w(TAG, "Exception. failed parsing catch " + exceptionName);
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
            SlogEx.w(TAG, "FileNotFoundException. file is not exist.");
            if (0 == 0) {
                sb = new StringBuilder();
                sb.append("Error reading file:");
                sb.append(file.getName());
                sb.append(", occur to ");
                sb.append(exceptionName);
                SlogEx.w(TAG, sb.toString());
                HwPackageManagerServiceUtilsEx.reportPmsParseFileException(file.getName(), exceptionName, -1, (String) null);
                return;
            }
            return;
        } catch (XmlPullParserException e11) {
            exceptionName = "XmlPullParserException";
            SlogEx.w(TAG, "XmlPullParserException. failed parsing catch XmlPullParserException.");
            if (0 == 0) {
                sb = new StringBuilder();
                sb.append("Error reading file:");
                sb.append(file.getName());
                sb.append(", occur to ");
                sb.append(exceptionName);
                SlogEx.w(TAG, sb.toString());
                HwPackageManagerServiceUtilsEx.reportPmsParseFileException(file.getName(), exceptionName, -1, (String) null);
                return;
            }
            return;
        } catch (IOException e12) {
            exceptionName = "IOException";
            SlogEx.w(TAG, "IOException. failed parsing catch IOException.");
            if (0 == 0) {
                sb = new StringBuilder();
                sb.append("Error reading file:");
                sb.append(file.getName());
                sb.append(", occur to ");
                sb.append(exceptionName);
                SlogEx.w(TAG, sb.toString());
                HwPackageManagerServiceUtilsEx.reportPmsParseFileException(file.getName(), exceptionName, -1, (String) null);
                return;
            }
            return;
        } catch (Exception e13) {
            e = e13;
            isSuccess = false;
            exceptionName = e.getClass().toString();
            SlogEx.w(TAG, "Exception. failed parsing catch " + exceptionName);
            if (0 == 0) {
                sb = new StringBuilder();
                sb.append("Error reading file:");
                sb.append(file.getName());
                sb.append(", occur to ");
                sb.append(exceptionName);
                SlogEx.w(TAG, sb.toString());
                HwPackageManagerServiceUtilsEx.reportPmsParseFileException(file.getName(), exceptionName, -1, (String) null);
                return;
            }
            return;
        } catch (Throwable th8) {
            th = th8;
            if (!isSuccess) {
                SlogEx.w(TAG, "Error reading file:" + file.getName() + ", occur to " + exceptionName);
                HwPackageManagerServiceUtilsEx.reportPmsParseFileException(file.getName(), exceptionName, -1, (String) null);
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
        int currentUserId = UserHandleEx.getCallingUserId();
        PackageManagerServiceEx pmsInner = this.mHwPmsExInner.getIPmsInner();
        for (String str : UNINSTALLED_APPS.keySet()) {
            PackageSettingEx psTemp = pmsInner.getSettings().getPackageLPr(str);
            if (psTemp == null || psTemp.isObjNull() || !psTemp.getInstalled(currentUserId)) {
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

    private boolean isApplicationInstalled(PackageParserEx.PackageEx pkg) {
        int userId = UserHandleEx.getCallingUserId();
        PackageManagerServiceEx pmsInner = this.mHwPmsExInner.getIPmsInner();
        PackageSettingEx ps = (PackageSettingEx) pmsInner.getSettings().getPackages().get(pkg.getApplicationInfo().packageName);
        if (ps == null || ps.isObjNull()) {
            if (IS_DEBUG) {
                Log.w(TAG, "isApplicationInstalled PackageSetting is null, return false");
            }
            return false;
        } else if (ps.getPkg() == null) {
            if (IS_DEBUG) {
                Log.w(TAG, "isApplicationInstalled pkg is null, return false");
            }
            return false;
        } else {
            ApplicationInfo info = pmsInner.getApplicationInfo(pkg.getApplicationInfo().packageName, 8192, userId);
            if (IS_DEBUG) {
                Log.e(TAG, "isApplicationInstalled: pkg " + pkg + ", applicationInfo " + info + ", packageSetting " + ps);
            }
            if (info != null) {
                return true;
            }
            return false;
        }
    }

    public boolean needInstallRemovablePreApk(PackageParserEx.PackageEx pkg, int hwFlags) {
        if (pkg == null || pkg.getPackageName() == null) {
            return false;
        }
        if ((33554432 & hwFlags) == 0 || isApplicationInstalled(pkg) || !isUninstalledDelapp(pkg.getPackageName())) {
            return true;
        }
        Flog.i(205, "needInstallRemovablePreApk :" + pkg.getPackageName());
        return false;
    }

    public Map<String, String> getUninstalledRenamedPackagesMaps() {
        return UNINSTALLED_RENAMED_APPS;
    }
}
