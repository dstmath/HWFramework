package com.android.server.pm;

import android.annotation.SuppressLint;
import android.content.pm.FeatureInfo;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class HwForbidUninstallManager {
    private static final String ATTR_XML_CODE_PATH = "codePath";
    private static final String ATTR_XML_NAME = "name";
    private static final String FLAG_APK_END = ".apk";
    private static final String FLAG_XML_PACKAGE = "package";
    private static final String FLAG_XML_PACKAGES = "packages";
    private static final boolean IS_DEBUG = "on".equals(SystemProperties.get("ro.dbg.pms_log", "0"));
    private static final String NFCTAG_SAVE_PATCH = "/system/app/HwNfcTag.apk";
    private static final String NFC_DEVICE_PATH = SystemProperties.get("nfc.node", "/dev/pn544");
    private static final String NFC_FEATURE = "android.hardware.nfc";
    private static final String NFC_HCEF_FEATURE = "android.hardware.nfc.hcef";
    private static final String NFC_HCE_FEATURE = "android.hardware.nfc.hce";
    private static final String NFC_SAVE_PATCH = "/system/app/NfcNci_45.apk";
    private static final String REMOVE_UNSTALL_APK_DIR = "/data/system/";
    private static final String REMOVE_UNSTALL_APK_FILE = "remove_unstall_apk.xml";
    private static final String TAG = "HwForbidUninstallManager";
    private static volatile HwForbidUninstallManager sInstance;
    private IHwPackageManagerServiceExInner mHwPmsExInner;
    private Map<String, String> mRemoveUnstallApkMap = new HashMap();
    private Map<String, String> mScanInstallApkMap = new HashMap();
    private String mUninstallApk = null;

    private HwForbidUninstallManager(IHwPackageManagerServiceExInner pmsEx) {
        this.mHwPmsExInner = pmsEx;
    }

    public static HwForbidUninstallManager getInstance(IHwPackageManagerServiceExInner pmsEx) {
        if (sInstance == null) {
            synchronized (HwForbidUninstallManager.class) {
                if (sInstance == null) {
                    sInstance = new HwForbidUninstallManager(pmsEx);
                }
            }
        }
        return sInstance;
    }

    public boolean isUninstallApk(String filePath) {
        String str = this.mUninstallApk;
        return str != null && str.contains(filePath);
    }

    private void setUninstallApk(String string) {
        if (this.mUninstallApk != null) {
            this.mUninstallApk += AwarenessInnerConstants.SEMI_COLON_KEY + string;
            return;
        }
        this.mUninstallApk = string;
    }

    private void restoreUninstallApk(String restoreApk) {
        if (!(this.mUninstallApk == null || restoreApk == null)) {
            for (String apkPath : Pattern.compile("\\s*|\n|\r|\t").matcher(restoreApk).replaceAll("").split(AwarenessInnerConstants.SEMI_COLON_KEY)) {
                this.mUninstallApk = this.mUninstallApk.replaceAll(apkPath, "");
            }
        }
    }

    private void setScanInstallApk(XmlPullParser parser) {
        String pkgName = XmlUtils.readStringAttribute(parser, "packagename");
        String path = XmlUtils.readStringAttribute(parser, "path");
        if (!TextUtils.isEmpty(pkgName) && !TextUtils.isEmpty(path)) {
            this.mScanInstallApkMap.put(pkgName, path);
        }
    }

    @SuppressLint({"PreferForInArrayList"})
    public void getUninstallApk() {
        Map<String, FeatureInfo> availableFeatures = this.mHwPmsExInner.getIPmsInner().getAvailableFeaturesInner();
        List<File> allList = new ArrayList<>();
        try {
            allList = HwCfgFilePolicy.getCfgFileList("xml/unstall_apk.xml", 0);
        } catch (NoClassDefFoundError e) {
            Slog.e(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        }
        if (allList.size() > 0) {
            for (File list : allList) {
                loadUninstallApps(list);
            }
        }
        if (!new File(NFC_DEVICE_PATH).exists()) {
            if (availableFeatures.containsKey(NFC_FEATURE)) {
                availableFeatures.remove(NFC_FEATURE);
            }
            if (availableFeatures.containsKey(NFC_HCE_FEATURE)) {
                availableFeatures.remove(NFC_HCE_FEATURE);
            }
            if (availableFeatures.containsKey(NFC_HCEF_FEATURE)) {
                availableFeatures.remove(NFC_HCEF_FEATURE);
            }
            String str = this.mUninstallApk;
            if (str == null || "".equals(str)) {
                this.mUninstallApk = "/system/app/NfcNci_45.apk;/system/app/HwNfcTag.apk";
            } else if (!this.mUninstallApk.contains(NFC_SAVE_PATCH)) {
                this.mUninstallApk += AwarenessInnerConstants.SEMI_COLON_KEY + NFC_SAVE_PATCH + AwarenessInnerConstants.SEMI_COLON_KEY + NFCTAG_SAVE_PATCH;
            }
        }
        if (IS_DEBUG) {
            Slog.d(TAG, "mUninstallApk: " + this.mUninstallApk);
        }
    }

    private void loadUninstallApps(File list) {
        File file = list;
        if (this.mHwPmsExInner.getCust() != null) {
            file = this.mHwPmsExInner.getCust().customizeUninstallApk(file);
        }
        if (file.exists()) {
            FileInputStream in = null;
            try {
                FileInputStream in2 = new FileInputStream(file);
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(in2, null);
                for (int eventType = xpp.getEventType(); eventType != 1; eventType = xpp.next()) {
                    if (eventType == 2) {
                        if ("apk".equals(xpp.getName())) {
                            setUninstallApk(xpp.nextText());
                        } else if ("restoreapk".equals(xpp.getName())) {
                            restoreUninstallApk(xpp.nextText());
                        } else if ("scan_install_apk".equals(xpp.getName())) {
                            setScanInstallApk(xpp);
                        }
                    }
                }
                try {
                    in2.close();
                } catch (IOException e) {
                    Slog.e(TAG, "Close input stream exception");
                }
            } catch (XmlPullParserException e2) {
                Slog.e(TAG, "Parser xml error");
                if (0 != 0) {
                    in.close();
                }
            } catch (IOException e3) {
                Slog.e(TAG, "loadUninstallApps xml exception");
                if (0 != 0) {
                    in.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        in.close();
                    } catch (IOException e4) {
                        Slog.e(TAG, "Close input stream exception");
                    }
                }
                throw th;
            }
        }
    }

    public boolean isScanInstallApk(String codePath) {
        if (TextUtils.isEmpty(codePath) || this.mScanInstallApkMap.isEmpty()) {
            return false;
        }
        if (codePath.endsWith(FLAG_APK_END)) {
            codePath = HwPackageManagerServiceUtils.getCustPackagePath(codePath);
        }
        if (TextUtils.isEmpty(codePath)) {
            return false;
        }
        return this.mScanInstallApkMap.containsValue(codePath);
    }

    public String getScanInstallApkCodePath(String pkgName) {
        return this.mScanInstallApkMap.containsKey(pkgName) ? this.mScanInstallApkMap.get(pkgName) : "";
    }

    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00a4, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00a5, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00a8, code lost:
        throw r4;
     */
    public void loadRemoveUnstallApks() {
        FileInputStream stream;
        XmlPullParser parser;
        String tag;
        this.mRemoveUnstallApkMap.clear();
        File file = new File("/data/system/", REMOVE_UNSTALL_APK_FILE);
        if (file.exists()) {
            try {
                stream = new FileInputStream(file);
                parser = Xml.newPullParser();
                parser.setInput(stream, null);
                int type = parser.next();
                while (type != 1 && type != 2) {
                    type = parser.next();
                }
                tag = parser.getName();
            } catch (FileNotFoundException e) {
                Slog.w(TAG, "can not find remove unstall config.");
                return;
            } catch (XmlPullParserException e2) {
                Slog.w(TAG, "XmlPullParserException. failed parsing catch XmlPullParserException.");
                return;
            } catch (IOException e3) {
                Slog.w(TAG, "IOException. failed parsing catch IOException.");
                return;
            } catch (Exception e4) {
                Slog.w(TAG, "Exception. failed parsing catch exception");
                return;
            }
            if (!FLAG_XML_PACKAGES.equals(tag)) {
                Slog.e(TAG, "xml do not start with packages tag: found " + tag);
                $closeResource(null, stream);
            } else if (parser.next() == 1) {
                $closeResource(null, stream);
            } else {
                int outerDepth = parser.getDepth();
                while (true) {
                    int type2 = parser.next();
                    if (type2 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                        break;
                    } else if (!(type2 == 3 || type2 == 4 || !FLAG_XML_PACKAGE.equals(parser.getName()))) {
                        this.mRemoveUnstallApkMap.put(XmlUtils.readStringAttribute(parser, ATTR_XML_NAME), XmlUtils.readStringAttribute(parser, ATTR_XML_CODE_PATH));
                    }
                }
                $closeResource(null, stream);
            }
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

    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00c4, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00c5, code lost:
        $closeResource(r0, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00c8, code lost:
        throw r1;
     */
    public void writeRemoveUnstallApks(String pkgName, String codePath) {
        Slog.i(TAG, "start writeRemoveUnstallApks for pkgName: " + pkgName + ", codePath: " + codePath);
        if (TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(codePath)) {
            Slog.i(TAG, "pkgName or codePath is empty");
            return;
        }
        if (codePath.endsWith(FLAG_APK_END)) {
            codePath = HwPackageManagerServiceUtils.getCustPackagePath(codePath);
        }
        if (TextUtils.isEmpty(codePath)) {
            Slog.i(TAG, "codePath is empty");
        } else if (!this.mRemoveUnstallApkMap.containsKey(pkgName) || !codePath.equals(this.mRemoveUnstallApkMap.get(pkgName))) {
            this.mRemoveUnstallApkMap.put(pkgName, codePath);
            File file = new File("/data/system/", REMOVE_UNSTALL_APK_FILE);
            if (!file.exists()) {
                try {
                    if (!file.createNewFile()) {
                        Slog.w(TAG, "createNewFile error!");
                        return;
                    }
                } catch (IOException e) {
                    Slog.w(TAG, "createNewFile error!");
                    return;
                }
            }
            try {
                OutputStream stream = new FileOutputStream(file, false);
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(stream, "utf-8");
                out.startDocument(null, true);
                out.startTag(null, FLAG_XML_PACKAGES);
                out.startTag(null, FLAG_XML_PACKAGE);
                XmlUtils.writeStringAttribute(out, ATTR_XML_NAME, pkgName);
                XmlUtils.writeStringAttribute(out, ATTR_XML_CODE_PATH, codePath);
                out.endTag(null, FLAG_XML_PACKAGE);
                out.endTag(null, FLAG_XML_PACKAGES);
                out.endDocument();
                $closeResource(null, stream);
            } catch (IOException e2) {
                Slog.w(TAG, "writeToXml failed! IOException.");
            } catch (Exception e3) {
                Slog.w(TAG, "writeToXml failed! Exception.");
            }
        } else {
            Slog.i(TAG, "the same record already exists in the mRemoveUnstallApkMap.");
        }
    }

    public boolean isRemoveUnstallApk(File file) {
        if (file == null) {
            Slog.i(TAG, "isRemoveUnstallApk file is null");
            return false;
        } else if (!isScanInstallApk(file.getPath())) {
            Slog.i(TAG, file.getPath() + " is not scanInstallApk");
            return false;
        } else {
            String packageName = HwPackageManagerUtils.getPackageNameFromFile(file);
            Slog.i(TAG, "isRemoveUnstallApk packageName: " + packageName);
            if (TextUtils.isEmpty(packageName) || !this.mRemoveUnstallApkMap.containsKey(packageName)) {
                return false;
            }
            Slog.i(TAG, packageName + " is removeUnstallApk");
            return true;
        }
    }
}
