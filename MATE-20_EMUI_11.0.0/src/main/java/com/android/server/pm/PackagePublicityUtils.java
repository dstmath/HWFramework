package com.android.server.pm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.HwInvisibleAppsFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.content.pm.Signature;
import android.os.Environment;
import android.os.FileUtils;
import android.os.ParcelFileDescriptor;
import android.os.ServiceManager;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class PackagePublicityUtils {
    private static final String HW_PUBLICITY_ALL_DIRS = "/system/etc:/version/etc:cust/etc:hw_product/etc:product/etc:vendor/etc:preload/etc:preas/etc:vendor/preavs/etc";
    private static final String HW_PUBLICITY_PERMISSION_PATH = "emui/china/xml/publicity_permission.xml";
    private static final String PMS_PACKAGE = "package";
    private static final String PUBLICITY_FILE_NAME = "system/publicity_all.xml";
    private static final String SEPARATE = ",";
    private static final String TAG = "PackagePublicityUtils";
    private static final String TAG_NAME_PACKAGE_INFO = "packageInfo";
    private static final String UTF_8 = "utf-8";
    private static List<String> sHwPublicityAppList = null;
    private static Comparator<PackagePublicityInfo> sPublicityInfoComparator = new Comparator<PackagePublicityInfo>() {
        /* class com.android.server.pm.PackagePublicityUtils.AnonymousClass1 */
        private final Collator mCollator = Collator.getInstance(Locale.CHINA);

        public int compare(PackagePublicityInfo object1, PackagePublicityInfo object2) {
            int compareResult = this.mCollator.compare(object1.getLabel(), object2.getLabel());
            if (compareResult != 0) {
                return compareResult;
            }
            return this.mCollator.compare(object1.getPackageName(), object2.getPackageName());
        }
    };

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX DEBUG: Type inference failed for r1v3. Raw type applied. Possible types: java.util.List<java.io.File> */
    @SuppressLint({"PreferForInArrayList"})
    private static List<PackagePublicityInfo> loadPackagePublicityInfo() {
        List<File> publicityFileList = new ArrayList<>();
        try {
            publicityFileList = getPublicityFileList("publicity_all.xml");
        } catch (NoClassDefFoundError er) {
            Slog.e(TAG, er.getMessage());
        }
        List<PackagePublicityInfo> publicityInfos = new ArrayList<>();
        for (File file : publicityFileList) {
            if (file != null && file.exists()) {
                publicityInfos.addAll(getPackagePublicityInfoFromCust(file));
            }
        }
        return publicityInfos;
    }

    private static List<File> getPublicityFileList(String fileName) {
        List<File> fileList = new ArrayList<>();
        if (TextUtils.isEmpty(fileName)) {
            Log.e(TAG, "Error: file = [" + fileName + "]");
            return fileList;
        }
        for (String dir : HW_PUBLICITY_ALL_DIRS.split(AwarenessInnerConstants.COLON_KEY)) {
            File file = new File(dir, fileName);
            if (file.exists()) {
                fileList.add(file);
            }
        }
        if (fileList.size() == 0) {
            Log.w(TAG, "No publicity file found for:" + fileName);
        }
        return fileList;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00bf, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00c0, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00c3, code lost:
        throw r4;
     */
    public static List<PackagePublicityInfo> getPackagePublicityInfoFromCust(File file) {
        List<PackagePublicityInfo> resultList = new ArrayList<>();
        if (file == null || !file.exists()) {
            return resultList;
        }
        try {
            InputStream in = new FileInputStream(file);
            XmlPullParser xml = Xml.newPullParser();
            xml.setInput(in, UTF_8);
            while (true) {
                int xmlEventType = xml.next();
                if (xmlEventType == 1) {
                    break;
                } else if (xmlEventType == 2 && TAG_NAME_PACKAGE_INFO.equals(xml.getName())) {
                    String mPackage = xml.getAttributeValue(null, PMS_PACKAGE);
                    if (!TextUtils.isEmpty(mPackage)) {
                        String mPackage2 = mPackage.intern();
                        PackagePublicityInfo packagePublicityInfo = new PackagePublicityInfo();
                        packagePublicityInfo.setPackageName(mPackage2);
                        String label = xml.getAttributeValue(null, "label");
                        if (TextUtils.isEmpty(label)) {
                            packagePublicityInfo.setLabel(mPackage2);
                        } else {
                            packagePublicityInfo.setLabel(label.intern());
                        }
                        packagePublicityInfo.setFeature(xml.getAttributeValue(null, "feature"));
                        packagePublicityInfo.setAuthor(xml.getAttributeValue(null, "author"));
                        packagePublicityInfo.setLauncherInfo(xml.getAttributeValue(null, "launcher"));
                        packagePublicityInfo.setUninstallInfo(xml.getAttributeValue(null, "uninstall"));
                        packagePublicityInfo.setPackageFileName(xml.getAttributeValue(null, "packageFileName"));
                        packagePublicityInfo.setUsePermission(xml.getAttributeValue(null, "use-permission"));
                        packagePublicityInfo.setCategory(xml.getAttributeValue(null, "app-category"));
                        packagePublicityInfo.setSignature(xml.getAttributeValue(null, "app-signature"));
                        resultList.add(packagePublicityInfo);
                    }
                }
            }
            $closeResource(null, in);
        } catch (FileNotFoundException e) {
            Slog.w(TAG, "read publicity_all.xml error");
        } catch (XmlPullParserException e2) {
            Slog.e(TAG, "XmlPullParserException while trying to read from publicity_all.xml");
        } catch (IOException e3) {
            Slog.e(TAG, "IOException while trying to read from publicity_all.xml");
        }
        return resultList;
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

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00df, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00e0, code lost:
        $closeResource(r0, r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00e4, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00e8, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00e9, code lost:
        $closeResource(r0, r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00ed, code lost:
        throw r0;
     */
    public static void writeAllPakcagePublicityInfoIntoFile(Context context, ParceledListSlice<ApplicationInfo> slice) {
        File file = getPublicityFile();
        if (file == null) {
            return;
        }
        if (!file.exists()) {
            List<PackagePublicityInfo> pkgPublicityInfoList = loadPackagePublicityInfo();
            if (pkgPublicityInfoList == null) {
                return;
            }
            if (pkgPublicityInfoList.size() != 0) {
                handlePublicityInfos(context, slice, pkgPublicityInfoList);
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    BufferedOutputStream str = new BufferedOutputStream(out);
                    XmlSerializer serializer = new FastXmlSerializer();
                    serializer.setOutput(str, UTF_8);
                    serializer.startDocument(null, true);
                    serializer.startTag(null, "packageList");
                    for (PackagePublicityInfo info : pkgPublicityInfoList) {
                        serializer.startTag(null, TAG_NAME_PACKAGE_INFO);
                        serializer.attribute(null, PMS_PACKAGE, info.getPackageName());
                        serializer.attribute(null, "label", info.getLabel());
                        serializer.attribute(null, "feature", info.getFeature());
                        serializer.attribute(null, "author", info.getAuthor());
                        serializer.attribute(null, "launcher", info.getLauncherInfo());
                        serializer.attribute(null, "uninstall", info.getUninstallInfo());
                        serializer.attribute(null, "packageFileName", info.getPackageFileName());
                        serializer.attribute(null, "use-permission", info.getUsePermission());
                        serializer.attribute(null, "app-category", info.getCategory());
                        serializer.attribute(null, "app-signature", info.getSignature());
                        serializer.endTag(null, TAG_NAME_PACKAGE_INFO);
                    }
                    serializer.endTag(null, "packageList");
                    serializer.endDocument();
                    str.flush();
                    FileUtils.sync(out);
                    $closeResource(null, str);
                    $closeResource(null, out);
                } catch (FileNotFoundException e) {
                    Slog.e(TAG, "writing pakcagePublicity file error.");
                } catch (IllegalArgumentException e2) {
                    Slog.e(TAG, "IllegalArgumentException when writing pakcagePublicity file.");
                } catch (IOException e3) {
                    Slog.e(TAG, "IOException when writing pakcagePublicity file.");
                } catch (Throwable th) {
                    FileUtils.setPermissions(file.toString(), 432, -1, -1);
                    throw th;
                }
                FileUtils.setPermissions(file.toString(), 432, -1, -1);
            }
        }
    }

    public static File getPublicityFile() {
        File publicityFile = null;
        try {
            File systemDir = new File(Environment.getDataDirectory(), "system");
            if (systemDir.exists() || systemDir.mkdirs()) {
                publicityFile = new File(systemDir, "publicity_all.xml");
                if (publicityFile.exists()) {
                    return null;
                }
                Slog.i(TAG, "first boot. init publicity_all.xml");
                return publicityFile;
            }
            Slog.i(TAG, "PakcagePublicity file create error");
            return null;
        } catch (SecurityException e) {
            Slog.i(TAG, "PakcagePublicity file SecurityException: " + e.getMessage());
        }
    }

    public static void deletePublicityFile() {
        try {
            File publicityFile = new File(Environment.getDataDirectory(), PUBLICITY_FILE_NAME);
            if (publicityFile.exists() && publicityFile.delete()) {
                Slog.i(TAG, "Delete publicity file ...");
            }
        } catch (SecurityException e) {
            Slog.i(TAG, "Delete publicity file error: " + e.getMessage());
        }
    }

    private static void handlePublicityInfos(Context context, ParceledListSlice<ApplicationInfo> slice, List<PackagePublicityInfo> pkgPubInfo) {
        filterPublicityInfos(new HwInvisibleAppsFilter(context), slice, pkgPubInfo, context.getPackageManager());
        sortPackagePublicityInfos(pkgPubInfo);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0063, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0064, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0067, code lost:
        throw r4;
     */
    private static void initPubPermissions(List<String> pubPermissionGroup, List<String> pubPermission) {
        File file = new File("system", HW_PUBLICITY_PERMISSION_PATH);
        if (file.exists()) {
            try {
                InputStream in = new FileInputStream(file);
                XmlPullParser xml = Xml.newPullParser();
                xml.setInput(in, UTF_8);
                while (true) {
                    int xmlEventType = xml.next();
                    if (xmlEventType == 1) {
                        $closeResource(null, in);
                        return;
                    } else if (xmlEventType == 2 && "permissionInfo".equals(xml.getName())) {
                        String group = xml.getAttributeValue(null, "group");
                        String name = xml.getAttributeValue(null, "name");
                        if (!TextUtils.isEmpty(group) && !TextUtils.isEmpty(name)) {
                            pubPermissionGroup.add(group);
                            pubPermission.add(name.intern());
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                Log.w(TAG, "read publicity_permission.xml error");
            } catch (XmlPullParserException e2) {
                Log.e(TAG, "XmlPullParserException while trying to read from publicity_permission.xml");
            } catch (IOException e3) {
                Log.e(TAG, "IOException while trying to read from publicity_permission.xml");
            }
        }
    }

    private static void filterPublicityInfos(HwInvisibleAppsFilter filter, ParceledListSlice<ApplicationInfo> slice, List<PackagePublicityInfo> pkgPubInfo, PackageManager pm) {
        if (validatePublicityInfos(slice, pkgPubInfo)) {
            List<String> pubPermissionsList = new ArrayList<>();
            List<String> pubPermissions = new ArrayList<>();
            initPubPermissions(pubPermissionsList, pubPermissions);
            List<ApplicationInfo> allApps = filter.filterHideApp(slice.getList());
            List<String> allInstallAppNames = new ArrayList<>();
            for (ApplicationInfo app : allApps) {
                allInstallAppNames.add(app.packageName);
            }
            List<PackagePublicityInfo> insatlledPkgs = new ArrayList<>();
            List<String> existPkgList = new ArrayList<>();
            PackageManagerService pms = ServiceManager.getService(PMS_PACKAGE);
            for (PackagePublicityInfo pubInfo : pkgPubInfo) {
                String pubInfoPkg = pubInfo.getPackageName();
                if (!existPkgList.contains(pubInfoPkg)) {
                    if (allInstallAppNames.contains(pubInfoPkg)) {
                        PackageInfo pkgInfo = pms.getPackageInfo(pubInfoPkg, 4096, 0);
                        pubInfo.setUsePermission(getUsePermission(pkgInfo, pubPermissionsList, pubPermissions));
                        if (pkgInfo != null) {
                            if (pkgInfo.applicationInfo != null) {
                                CharSequence label = pkgInfo.applicationInfo.loadUnsafeLabel(pm);
                                if (label == null || TextUtils.isEmpty(label.toString())) {
                                    pubInfo.setLabel(pubInfoPkg);
                                } else {
                                    pubInfo.setLabel(label.toString());
                                }
                                PackageInfo foundPkgInfo = pms.getPackageInfo(pubInfoPkg, 64, 0);
                                if (foundPkgInfo.signatures != null) {
                                    pubInfo.setSignature(getSignatureString(foundPkgInfo.signatures));
                                    insatlledPkgs.add(pubInfo);
                                    existPkgList.add(pubInfoPkg);
                                }
                            }
                        }
                    }
                }
            }
            Slog.w(TAG, "insatlledPkgs size = " + insatlledPkgs.size());
            pkgPubInfo.clear();
            pkgPubInfo.addAll(insatlledPkgs);
        }
    }

    private static boolean validatePublicityInfos(ParceledListSlice<ApplicationInfo> slice, List<PackagePublicityInfo> pkgPubInfo) {
        if (slice == null) {
            Slog.w(TAG, "FiltePublicityInfo: getInstalledApplications is null");
            pkgPubInfo.clear();
            return false;
        }
        List<ApplicationInfo> allApps = slice.getList();
        if (allApps != null && allApps.size() != 0) {
            return true;
        }
        pkgPubInfo.clear();
        return false;
    }

    private static String getUsePermission(PackageInfo pkgInfo, List<String> pubPermissionGroup, List<String> pubPermission) {
        String[] requestedPermissions;
        if (pkgInfo == null || (requestedPermissions = pkgInfo.requestedPermissions) == null || requestedPermissions.length <= 0) {
            return "";
        }
        StringBuilder usePermissionBuilder = new StringBuilder();
        List<String> usePermissionList = new ArrayList<>();
        List<String> requestedPermissionList = Arrays.asList(requestedPermissions);
        for (String permission : pubPermission) {
            if (requestedPermissionList.contains(permission)) {
                String prermissionGroup = pubPermissionGroup.get(pubPermission.indexOf(permission));
                if (!usePermissionList.contains(prermissionGroup)) {
                    usePermissionList.add(prermissionGroup);
                    usePermissionBuilder.append(prermissionGroup);
                    usePermissionBuilder.append(",");
                }
            }
        }
        String usePermission = usePermissionBuilder.toString();
        if (TextUtils.isEmpty(usePermission) || !usePermission.endsWith(",")) {
            return usePermission;
        }
        return usePermission.substring(0, usePermission.length() - 1);
    }

    private static void sortPackagePublicityInfos(List<PackagePublicityInfo> pkgPubInfo) {
        Comparator<PackagePublicityInfo> comparator;
        if (pkgPubInfo != null && pkgPubInfo.size() > 0 && (comparator = sPublicityInfoComparator) != null) {
            Collections.sort(pkgPubInfo, comparator);
            sPublicityInfoComparator = null;
        }
    }

    public static List<String> getHwPublicityAppList(Context context) {
        String str;
        List<String> list = sHwPublicityAppList;
        if (list != null) {
            return list;
        }
        File publicityFile = new File(Environment.getDataDirectory(), PUBLICITY_FILE_NAME);
        if (!publicityFile.exists()) {
            return sHwPublicityAppList;
        }
        sHwPublicityAppList = new ArrayList();
        List<PackagePublicityInfo> readResultList = getPackagePublicityInfoFromCust(publicityFile);
        PackageManagerService pms = ServiceManager.getService(PMS_PACKAGE);
        for (PackagePublicityInfo pp : readResultList) {
            String pkName = pp.getPackageName();
            String label = "";
            PackageInfo pkgInfo = pms.getPackageInfo(pkName, 64, 0);
            if (pkgInfo == null) {
                label = pp.getLabel();
            } else if (!(pkgInfo.signatures == null || pkgInfo.applicationInfo == null)) {
                if (pp.getSignature().equals(getSignatureString(pkgInfo.signatures)) && context != null) {
                    CharSequence labelCharSequence = pkgInfo.applicationInfo.loadUnsafeLabel(context.getPackageManager());
                    if (labelCharSequence == null || TextUtils.isEmpty(labelCharSequence.toString())) {
                        str = pkName;
                    } else {
                        str = labelCharSequence.toString();
                    }
                    label = str;
                }
            }
            sHwPublicityAppList.add(pkName + "+++++" + label);
        }
        return sHwPublicityAppList;
    }

    private static String getSignatureString(Signature[] signatures) {
        StringBuilder signatureStrBuilder = new StringBuilder();
        for (Signature signature : signatures) {
            signatureStrBuilder.append(signature.toCharsString());
            signatureStrBuilder.append(",");
        }
        String signatureStr = signatureStrBuilder.toString();
        if (TextUtils.isEmpty(signatureStr) || !signatureStr.endsWith(",")) {
            return signatureStr;
        }
        return signatureStr.substring(0, signatureStr.length() - 1);
    }

    public static ParcelFileDescriptor getHwPublicityAppParcelFileDescriptor() {
        try {
            File target = new File(Environment.getDataDirectory(), PUBLICITY_FILE_NAME);
            if (target.exists()) {
                return new ParcelFileDescriptor(Os.open(target.getCanonicalPath(), OsConstants.O_RDONLY, 0));
            }
            return null;
        } catch (ErrnoException e) {
            Slog.w(TAG, "getHwPublicityAppParcelFileDescriptor file not found .");
            return null;
        } catch (IOException e2) {
            Slog.w(TAG, "failed open " + e2.getMessage());
            return null;
        }
    }
}
