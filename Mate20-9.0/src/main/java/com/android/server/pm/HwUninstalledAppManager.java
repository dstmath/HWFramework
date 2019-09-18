package com.android.server.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Flog;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.server.pfw.autostartup.comm.XmlConst;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class HwUninstalledAppManager {
    private static final String DATA_DATA_DIR = "/data/data/";
    private static final boolean DEBUG = DEBUG_FLAG;
    private static final boolean DEBUG_FLAG = SystemProperties.get("ro.dbg.pms_log", "0").equals(XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_);
    private static final String TAG = "HwUninstalledAppManager";
    private static final String UNINSTALLED_DELAPP_DIR = "/data/system/";
    private static final String UNINSTALLED_DELAPP_FILE = "uninstalled_delapp.xml";
    static final ArrayList<String> UNINSTALLED_DELAPP_LIST = new ArrayList<>();
    static final Map<String, String> UNINSTALLED_MAP = new HashMap();
    private static volatile HwUninstalledAppManager mInstance;
    static List<String> mOldDataBackup = new ArrayList();
    private IHwPackageManagerServiceExInner mHwPmsExInner;

    private HwUninstalledAppManager(IHwPackageManagerServiceExInner pmsEx) {
        this.mHwPmsExInner = pmsEx;
    }

    public static HwUninstalledAppManager getInstance(IHwPackageManagerServiceExInner pmsEx) {
        if (mInstance == null) {
            synchronized (HwUninstalledAppManager.class) {
                if (mInstance == null) {
                    mInstance = new HwUninstalledAppManager(pmsEx);
                }
            }
        }
        return mInstance;
    }

    public void loadCorrectUninstallDelapp() {
        if (this.mHwPmsExInner.getIPmsInner().getIsPreNUpgradeInner()) {
            File fileExt = new File(DATA_DATA_DIR, UNINSTALLED_DELAPP_FILE);
            if (fileExt.exists()) {
                loadUninstalledDelapp(fileExt, false);
                Slog.w(TAG, "Compatible Fix for pre-N update verify uninstalled App!");
            }
        }
        File file = new File("/data/system/", UNINSTALLED_DELAPP_FILE);
        if (file.exists()) {
            try {
                loadUninstalledDelapp(file);
            } catch (IndexOutOfBoundsException e) {
                Slog.w(TAG, "load uninstalld delapp fail, try another way!");
                loadUninstalledDelapp(file, false);
            }
        }
    }

    public void addUnisntallDataToCache(String packageName, String codePath) {
        if (packageName == null || codePath == null || codePath.startsWith("/data/app/")) {
            Slog.d(TAG, "Add path to cache failed!");
            return;
        }
        if (!UNINSTALLED_DELAPP_LIST.contains(packageName)) {
            UNINSTALLED_DELAPP_LIST.add(packageName);
        }
        UNINSTALLED_MAP.put(packageName, codePath);
        Slog.i(TAG, "Add path to cache packageName:" + packageName + ",codePath:" + codePath);
    }

    public void removeFromUninstalledDelapp(String s) {
        if (UNINSTALLED_DELAPP_LIST != null && UNINSTALLED_DELAPP_LIST.contains(s)) {
            UNINSTALLED_DELAPP_LIST.remove(s);
            UNINSTALLED_MAP.remove(s);
            recordUninstalledDelapp(null, null);
        }
    }

    public void recordUninstalledDelapp(String s, String path) {
        if (UNINSTALLED_DELAPP_LIST.contains(s)) {
            Slog.d(TAG, "duplicate recordUninstalledDelapp here, return!");
            return;
        }
        File file = new File("/data/system/", UNINSTALLED_DELAPP_FILE);
        if (s != null) {
            loadUninstalledDelapp(file);
        }
        FileOutputStream stream = null;
        try {
            FileOutputStream stream2 = new FileOutputStream(file, false);
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(stream2, "utf-8");
            out.startDocument(null, true);
            out.startTag(null, "values");
            if (s != null) {
                out.startTag(null, "string");
                out.attribute(null, "name", s);
                out.attribute(null, "codePath", path);
                out.endTag(null, "string");
            }
            int listSize = UNINSTALLED_DELAPP_LIST.size();
            for (int i = 0; i < listSize; i++) {
                String temp = UNINSTALLED_DELAPP_LIST.get(i);
                out.startTag(null, "string");
                out.attribute(null, "name", temp);
                out.attribute(null, "codePath", UNINSTALLED_MAP.get(temp));
                out.endTag(null, "string");
            }
            out.endTag(null, "values");
            out.endDocument();
            if (s != null) {
                UNINSTALLED_DELAPP_LIST.add(s);
                UNINSTALLED_MAP.put(s, path);
            }
            try {
                stream2.close();
            } catch (IOException e) {
                Log.e(TAG, "recordUninstalledDelapp()");
            }
        } catch (IOException e2) {
            Slog.w(TAG, "failed parsing " + file + " " + e2);
            if (stream != null) {
                stream.close();
            }
        } catch (Exception e3) {
            Slog.w(TAG, "failed parsing " + file + " " + e3);
            if (stream != null) {
                stream.close();
            }
        } catch (Throwable th) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e4) {
                    Log.e(TAG, "recordUninstalledDelapp()");
                }
            }
            throw th;
        }
    }

    private void loadUninstalledDelapp(File file) {
        loadUninstalledDelapp(file, true);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:42:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:?, code lost:
        return;
     */
    private void loadUninstalledDelapp(File file, boolean isIncludeCodePath) {
        XmlPullParser parser;
        int i;
        IHwPackageManagerInner pmsInner;
        File file2 = file;
        Map<String, String> unistalledMap = new HashMap<>();
        unistalledMap.putAll(UNINSTALLED_MAP);
        UNINSTALLED_DELAPP_LIST.clear();
        UNINSTALLED_MAP.clear();
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file2);
            parser = Xml.newPullParser();
            parser.setInput(stream, null);
            while (true) {
                int next = parser.next();
                int type = next;
                i = 1;
                if (next == 1 || type == 2) {
                } else {
                    Slog.d(TAG, "loadUninstalledDelapp");
                }
            }
            if ("values".equals(parser.getName())) {
                int next2 = parser.next();
                int type2 = parser.getDepth();
                pmsInner = this.mHwPmsExInner.getIPmsInner();
                while (true) {
                    int next3 = parser.next();
                    int type3 = next3;
                    if (next3 == i || (type3 == 3 && parser.getDepth() <= type2)) {
                        try {
                            break;
                        } catch (IOException e) {
                            IOException iOException = e;
                            Log.e(TAG, "loadUninstalledDelapp()");
                            return;
                        }
                    } else {
                        if (type3 != 3) {
                            if (type3 != 4) {
                                if ("string".equals(parser.getName()) && parser.getAttributeValue(0) != null) {
                                    if (isIncludeCodePath) {
                                        UNINSTALLED_DELAPP_LIST.add(parser.getAttributeValue(0));
                                        UNINSTALLED_MAP.put(parser.getAttributeValue(0), parser.getAttributeValue(i));
                                    } else {
                                        mOldDataBackup.add(parser.getAttributeValue(0));
                                    }
                                }
                            }
                        }
                        int i2 = type3;
                        i = 1;
                    }
                }
            } else {
                throw new XmlPullParserException("Settings do not start with policies tag: found " + tag);
            }
        } catch (IndexOutOfBoundsException e2) {
            if (!pmsInner.getSystemReadyInner()) {
                throw e2;
            } else if (unistalledMap.get(parser.getAttributeValue(0)) != null) {
                UNINSTALLED_MAP.put(parser.getAttributeValue(0), unistalledMap.get(parser.getAttributeValue(0)));
            } else {
                Log.i(TAG, "loadUninstalledDelapp pkg:" + parser.getAttributeValue(0) + " is remove!");
                if (UNINSTALLED_DELAPP_LIST.contains(parser.getAttributeValue(0))) {
                    UNINSTALLED_DELAPP_LIST.remove(parser.getAttributeValue(0));
                }
            }
        } catch (FileNotFoundException e3) {
            this.mHwPmsExInner.reportEventStream(907400025, e3.getMessage());
            Slog.w(TAG, "file is not exist " + e3);
            if (stream != null) {
                stream.close();
            }
        } catch (XmlPullParserException e4) {
            this.mHwPmsExInner.reportEventStream(907400025, e4.getMessage());
            Slog.w(TAG, "failed parsing " + file2 + " " + e4);
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e5) {
            try {
                this.mHwPmsExInner.reportEventStream(907400025, e5.getMessage());
                Slog.w(TAG, "failed parsing " + file2 + " " + e5);
                if (stream != null) {
                    stream.close();
                }
            } catch (Throwable th) {
                FileInputStream stream2 = stream;
                Throwable th2 = th;
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (IOException e6) {
                        IOException iOException2 = e6;
                        Log.e(TAG, "loadUninstalledDelapp()");
                    }
                }
                throw th2;
            }
        }
    }

    private boolean isUninstalledDelapp(String s) {
        if (mOldDataBackup.size() != 0) {
            return mOldDataBackup.contains(s);
        }
        if (UNINSTALLED_DELAPP_LIST.size() != 0) {
            return UNINSTALLED_DELAPP_LIST.contains(s);
        }
        return false;
    }

    public List<String> getScanInstallList() {
        if (UNINSTALLED_MAP == null || UNINSTALLED_MAP.size() == 0) {
            return null;
        }
        List<String> res = new ArrayList<>();
        int currentUserId = UserHandle.getCallingUserId();
        IHwPackageManagerInner pmsInner = this.mHwPmsExInner.getIPmsInner();
        for (String str : UNINSTALLED_MAP.keySet()) {
            PackageSetting psTemp = pmsInner.getSettings().getPackageLPr(str);
            if (psTemp == null || !psTemp.getInstalled(currentUserId)) {
                res.add(UNINSTALLED_MAP.get(str));
            }
        }
        return res;
    }

    public List<String> getOldDataBackup() {
        return mOldDataBackup;
    }

    public Map<String, String> getUninstalledMap() {
        return UNINSTALLED_MAP;
    }

    private boolean isApplicationInstalled(PackageParser.Package pkg) {
        int userId = UserHandle.getCallingUserId();
        IHwPackageManagerInner pmsInner = this.mHwPmsExInner.getIPmsInner();
        PackageSetting p = (PackageSetting) pmsInner.getSettings().mPackages.get(pkg.applicationInfo.packageName);
        boolean z = false;
        if (p == null) {
            if (DEBUG) {
                Log.w(TAG, "isApplicationInstalled PackageSetting is null, return false");
            }
            return false;
        } else if (p.pkg == null) {
            if (DEBUG) {
                Log.w(TAG, "isApplicationInstalled pkg is null, return false");
            }
            return false;
        } else {
            ApplicationInfo info = pmsInner.getApplicationInfo(pkg.applicationInfo.packageName, 8192, userId);
            if (DEBUG) {
                Log.e(TAG, "isApplicationInstalled: pkg " + pkg + ", applicationInfo " + info + ", packageSetting " + p);
            }
            if (info != null) {
                z = true;
            }
            return z;
        }
    }

    public boolean needInstallRemovablePreApk(PackageParser.Package pkg, int hwFlags) {
        if ((33554432 & hwFlags) == 0 || isApplicationInstalled(pkg) || !isUninstalledDelapp(pkg.packageName)) {
            return true;
        }
        Flog.i(205, "needInstallRemovablePreApk :" + pkg.packageName);
        return false;
    }
}
