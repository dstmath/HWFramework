package com.android.server.pm;

import android.annotation.SuppressLint;
import android.content.pm.FeatureInfo;
import android.text.TextUtils;
import android.util.Xml;
import com.huawei.android.internal.util.FastXmlSerializerEx;
import com.huawei.android.internal.util.XmlUtilsEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.util.SlogEx;
import com.huawei.hwpartbasicplatformservices.BuildConfig;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class HwForbidUninstallManager {
    private static final String ATTR_XML_CODE_PATH = "codePath";
    private static final String ATTR_XML_NAME = "name";
    private static final String FLAG_APK_END = ".apk";
    private static final String FLAG_XML_PACKAGE = "package";
    private static final String FLAG_XML_PACKAGES = "packages";
    private static final boolean IS_DEBUG = "on".equals(SystemPropertiesEx.get("ro.dbg.pms_log", "0"));
    private static final String NFCTAG_SAVE_PATCH = "/system/app/HwNfcTag.apk";
    private static final String NFC_DEVICE_PATH = SystemPropertiesEx.get("nfc.node", "/dev/pn544");
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
            this.mUninstallApk += ";" + string;
            return;
        }
        this.mUninstallApk = string;
    }

    private void restoreUninstallApk(String restoreApk) {
        if (!(this.mUninstallApk == null || restoreApk == null)) {
            for (String apkPath : Pattern.compile("\\s*|\n|\r|\t").matcher(restoreApk).replaceAll(BuildConfig.FLAVOR).split(";")) {
                this.mUninstallApk = this.mUninstallApk.replaceAll(apkPath, BuildConfig.FLAVOR);
            }
        }
    }

    private void setScanInstallApk(XmlPullParser parser) {
        String pkgName = XmlUtilsEx.readStringAttribute(parser, "packagename");
        String path = XmlUtilsEx.readStringAttribute(parser, "path");
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
            SlogEx.e(TAG, "HwCfgFilePolicy NoClassDefFoundError");
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
            if (str == null || BuildConfig.FLAVOR.equals(str)) {
                this.mUninstallApk = "/system/app/NfcNci_45.apk;/system/app/HwNfcTag.apk";
            } else if (!this.mUninstallApk.contains(NFC_SAVE_PATCH)) {
                this.mUninstallApk += ";" + NFC_SAVE_PATCH + ";" + NFCTAG_SAVE_PATCH;
            }
        }
        if (IS_DEBUG) {
            SlogEx.d(TAG, "mUninstallApk: " + this.mUninstallApk);
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
                    SlogEx.e(TAG, "Close input stream exception");
                }
            } catch (XmlPullParserException e2) {
                SlogEx.e(TAG, "Parser xml error");
                if (0 != 0) {
                    in.close();
                }
            } catch (IOException e3) {
                SlogEx.e(TAG, "loadUninstallApps xml exception");
                if (0 != 0) {
                    in.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        in.close();
                    } catch (IOException e4) {
                        SlogEx.e(TAG, "Close input stream exception");
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
            codePath = HwPackageManagerServiceUtilsEx.getCustPackagePath(codePath);
        }
        if (TextUtils.isEmpty(codePath)) {
            return false;
        }
        return this.mScanInstallApkMap.containsValue(codePath);
    }

    public String getScanInstallApkCodePath(String pkgName) {
        return this.mScanInstallApkMap.containsKey(pkgName) ? this.mScanInstallApkMap.get(pkgName) : BuildConfig.FLAVOR;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00a2, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00a7, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00a8, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00ab, code lost:
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
                SlogEx.w(TAG, "can not find remove unstall config.");
                return;
            } catch (XmlPullParserException e2) {
                SlogEx.w(TAG, "XmlPullParserException. failed parsing catch XmlPullParserException.");
                return;
            } catch (IOException e3) {
                SlogEx.w(TAG, "IOException. failed parsing catch IOException.");
                return;
            } catch (Exception e4) {
                SlogEx.w(TAG, "Exception. failed parsing catch exception");
                return;
            }
            if (!FLAG_XML_PACKAGES.equals(tag)) {
                SlogEx.e(TAG, "xml do not start with packages tag: found " + tag);
                stream.close();
            } else if (parser.next() == 1) {
                stream.close();
            } else {
                int outerDepth = parser.getDepth();
                while (true) {
                    int type2 = parser.next();
                    if (type2 == 1 || (type2 == 3 && parser.getDepth() <= outerDepth)) {
                        break;
                    } else if (!(type2 == 3 || type2 == 4 || !FLAG_XML_PACKAGE.equals(parser.getName()))) {
                        this.mRemoveUnstallApkMap.put(XmlUtilsEx.readStringAttribute(parser, ATTR_XML_NAME), XmlUtilsEx.readStringAttribute(parser, ATTR_XML_CODE_PATH));
                    }
                }
                stream.close();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00c0, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00c5, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00c6, code lost:
        r0.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00c9, code lost:
        throw r1;
     */
    public void writeRemoveUnstallApks(String pkgName, String codePath) {
        SlogEx.i(TAG, "start writeRemoveUnstallApks for pkgName: " + pkgName + ", codePath: " + codePath);
        if (TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(codePath)) {
            SlogEx.i(TAG, "pkgName or codePath is empty");
            return;
        }
        if (codePath.endsWith(FLAG_APK_END)) {
            codePath = HwPackageManagerServiceUtilsEx.getCustPackagePath(codePath);
        }
        if (TextUtils.isEmpty(codePath)) {
            SlogEx.i(TAG, "codePath is empty");
        } else if (!this.mRemoveUnstallApkMap.containsKey(pkgName) || !codePath.equals(this.mRemoveUnstallApkMap.get(pkgName))) {
            this.mRemoveUnstallApkMap.put(pkgName, codePath);
            File file = new File("/data/system/", REMOVE_UNSTALL_APK_FILE);
            if (!file.exists()) {
                try {
                    if (!file.createNewFile()) {
                        SlogEx.w(TAG, "createNewFile error!");
                        return;
                    }
                } catch (IOException e) {
                    SlogEx.w(TAG, "createNewFile error!");
                    return;
                }
            }
            try {
                FileOutputStream stream = new FileOutputStream(file, false);
                FastXmlSerializerEx out = new FastXmlSerializerEx();
                out.setOutput(stream, "utf-8");
                out.startDocument((String) null, true);
                out.startTag((String) null, FLAG_XML_PACKAGES);
                out.startTag((String) null, FLAG_XML_PACKAGE);
                XmlUtilsEx.writeStringAttribute(out, ATTR_XML_NAME, pkgName);
                XmlUtilsEx.writeStringAttribute(out, ATTR_XML_CODE_PATH, codePath);
                out.endTag((String) null, FLAG_XML_PACKAGE);
                out.endTag((String) null, FLAG_XML_PACKAGES);
                out.endDocument();
                stream.close();
            } catch (IOException e2) {
                SlogEx.w(TAG, "writeToXml failed! IOException.");
            } catch (Exception e3) {
                SlogEx.w(TAG, "writeToXml failed! Exception.");
            }
        } else {
            SlogEx.i(TAG, "the same record already exists in the mRemoveUnstallApkMap.");
        }
    }

    public boolean isRemoveUnstallApk(File file) {
        if (file == null) {
            SlogEx.i(TAG, "isRemoveUnstallApk file is null");
            return false;
        } else if (!isScanInstallApk(file.getPath())) {
            SlogEx.i(TAG, file.getPath() + " is not scanInstallApk");
            return false;
        } else {
            String packageName = HwPackageManagerUtils.getPackageNameFromFile(file);
            SlogEx.i(TAG, "isRemoveUnstallApk packageName: " + packageName);
            if (TextUtils.isEmpty(packageName) || !this.mRemoveUnstallApkMap.containsKey(packageName)) {
                return false;
            }
            SlogEx.i(TAG, packageName + " is removeUnstallApk");
            return true;
        }
    }
}
