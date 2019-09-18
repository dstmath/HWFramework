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
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.server.devicepolicy.HwDevicePolicyManagerServiceUtil;
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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import libcore.io.Libcore;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class PackagePublicityUtils {
    private static final String HW_PUBLICITY_ALL_DIRS = "/system/etc:/version/etc:cust/etc:product/etc:vendor/etc:preload/etc:preas/etc:preavs/etc";
    private static final String HW_PUBLICITY_PERMISSION_PATH = "emui/china/xml/publicity_permission.xml";
    private static final String TAG = "PackagePublicityUtils";
    private static List<String> mHwPlublicityAppList = null;
    private static Comparator<PackagePublicityInfo> mPubliCityInfoComparator = new Comparator<PackagePublicityInfo>() {
        private final Collator sCollator = Collator.getInstance(Locale.CHINA);

        public int compare(PackagePublicityInfo object1, PackagePublicityInfo object2) {
            int compareResult = this.sCollator.compare(object1.getLabel(), object2.getLabel());
            if (compareResult != 0) {
                return compareResult;
            }
            return this.sCollator.compare(object1.getPackageName(), object2.getPackageName());
        }
    };

    @SuppressLint({"PreferForInArrayList"})
    private static List<PackagePublicityInfo> loadPackagePublicityInfo() {
        ArrayList<File> publicityFileList = new ArrayList<>();
        try {
            publicityFileList = getPublicityFileList("publicity_all.xml");
        } catch (NoClassDefFoundError er) {
            Slog.e(TAG, er.getMessage());
        }
        List<PackagePublicityInfo> publicityInfos = new ArrayList<>();
        Iterator<File> it = publicityFileList.iterator();
        while (it.hasNext()) {
            File file = it.next();
            if (file != null && file.exists()) {
                publicityInfos.addAll(getPackagePublicityInfoFromCust(file));
            }
        }
        return publicityInfos;
    }

    private static ArrayList<File> getPublicityFileList(String fileName) {
        ArrayList<File> fileList = new ArrayList<>();
        if (TextUtils.isEmpty(fileName)) {
            Log.e(TAG, "Error: file = [" + fileName + "]");
            return fileList;
        }
        for (String dir : HW_PUBLICITY_ALL_DIRS.split(":")) {
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

    public static List<PackagePublicityInfo> getPackagePublicityInfoFromCust(File file) {
        List<PackagePublicityInfo> result = new ArrayList<>();
        if (file == null || !file.exists()) {
            return result;
        }
        InputStream in = null;
        try {
            InputStream in2 = new FileInputStream(file);
            XmlPullParser xml = Xml.newPullParser();
            xml.setInput(in2, "utf-8");
            while (true) {
                int next = xml.next();
                int xmlEventType = next;
                if (next == 1) {
                    try {
                        break;
                    } catch (IOException e) {
                        Slog.e(TAG, "close FileInputStram error");
                    }
                } else if (xmlEventType == 2 && "packageInfo".equals(xml.getName())) {
                    PackagePublicityInfo packagePublicityInfo = new PackagePublicityInfo();
                    String pkg = xml.getAttributeValue(null, "package");
                    packagePublicityInfo.setPackageName(pkg);
                    String label = xml.getAttributeValue(null, HwDevicePolicyManagerServiceUtil.EXCHANGE_LABEL);
                    if (TextUtils.isEmpty(label)) {
                        packagePublicityInfo.setLabel(pkg);
                    } else {
                        packagePublicityInfo.setLabel(label);
                    }
                    packagePublicityInfo.setFeature(xml.getAttributeValue(null, "feature"));
                    packagePublicityInfo.setAuthor(xml.getAttributeValue(null, "author"));
                    packagePublicityInfo.setIsLauncher(xml.getAttributeValue(null, "launcher"));
                    packagePublicityInfo.setIsUninstall(xml.getAttributeValue(null, "uninstall"));
                    packagePublicityInfo.setPackageFileName(xml.getAttributeValue(null, "packageFileName"));
                    packagePublicityInfo.setUsePermission(xml.getAttributeValue(null, "use-permission"));
                    packagePublicityInfo.setCategory(xml.getAttributeValue(null, "app-category"));
                    packagePublicityInfo.setSignature(xml.getAttributeValue(null, "app-signature"));
                    if (!"".equals(pkg)) {
                        if (pkg != null) {
                            result.add(packagePublicityInfo);
                        }
                    }
                }
            }
            in2.close();
        } catch (FileNotFoundException e2) {
            Slog.w(TAG, "Error FileNotFound while trying to read from publicity_all.xml", e2);
            if (in != null) {
                in.close();
            }
        } catch (XmlPullParserException e3) {
            Slog.e(TAG, "Error XmlPullParser while trying to read from publicity_all.xml", e3);
            if (in != null) {
                in.close();
            }
        } catch (IOException e4) {
            Slog.e(TAG, "Error while trying to read from publicity_all.xml", e4);
            if (in != null) {
                in.close();
            }
        } catch (Throwable th) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e5) {
                    Slog.e(TAG, "close FileInputStram error");
                }
            }
            throw th;
        }
        return result;
    }

    public static void writeAllPakcagePublicityInfoIntoFile(Context context, ParceledListSlice<ApplicationInfo> slice) {
        String str;
        StringBuilder sb;
        File file = getPublicityFile();
        if (file != null && !file.exists()) {
            List<PackagePublicityInfo> allPackagePublicityInfo = loadPackagePublicityInfo();
            if (allPackagePublicityInfo.size() != 0) {
                handlePublicityInfos(context, slice, allPackagePublicityInfo);
                FileOutputStream out = null;
                BufferedOutputStream str2 = null;
                try {
                    FileOutputStream out2 = new FileOutputStream(file);
                    BufferedOutputStream str3 = new BufferedOutputStream(out2);
                    XmlSerializer serializer = new FastXmlSerializer();
                    serializer.setOutput(str3, "utf-8");
                    serializer.startDocument(null, true);
                    serializer.startTag(null, "packageList");
                    for (PackagePublicityInfo info : allPackagePublicityInfo) {
                        serializer.startTag(null, "packageInfo");
                        serializer.attribute(null, "package", info.getPackageName());
                        serializer.attribute(null, HwDevicePolicyManagerServiceUtil.EXCHANGE_LABEL, info.getLabel());
                        serializer.attribute(null, "feature", info.getFeature());
                        serializer.attribute(null, "author", info.getAuthor());
                        serializer.attribute(null, "launcher", info.getIsLauncher());
                        serializer.attribute(null, "uninstall", info.getIsUninstall());
                        serializer.attribute(null, "packageFileName", info.getPackageFileName());
                        serializer.attribute(null, "use-permission", info.getUsePermission());
                        serializer.attribute(null, "app-category", info.getCategory());
                        serializer.attribute(null, "app-signature", info.getSignature());
                        serializer.endTag(null, "packageInfo");
                    }
                    serializer.endTag(null, "packageList");
                    serializer.endDocument();
                    str3.flush();
                    FileUtils.sync(out2);
                    try {
                        str3.close();
                        out2.close();
                        FileUtils.setPermissions(file.toString(), 432, -1, -1);
                    } catch (IOException e) {
                        e = e;
                        str = TAG;
                        sb = new StringBuilder();
                        sb.append("Error close writing package manager settings");
                        sb.append(e.getMessage());
                        Slog.e(str, sb.toString());
                    }
                } catch (FileNotFoundException e2) {
                    Slog.e(TAG, "File not found when writing pakcagePublicity file: " + e2.getMessage());
                    if (str2 != null) {
                        try {
                            str2.close();
                        } catch (IOException e3) {
                            e = e3;
                            str = TAG;
                            sb = new StringBuilder();
                            sb.append("Error close writing package manager settings");
                            sb.append(e.getMessage());
                            Slog.e(str, sb.toString());
                        }
                    }
                    if (out != null) {
                        out.close();
                    }
                    FileUtils.setPermissions(file.toString(), 432, -1, -1);
                } catch (IllegalArgumentException e4) {
                    Slog.e(TAG, "IllegalArgument when writing pakcagePublicity file: " + e4.getMessage());
                    if (str2 != null) {
                        try {
                            str2.close();
                        } catch (IOException e5) {
                            e = e5;
                            str = TAG;
                            sb = new StringBuilder();
                            sb.append("Error close writing package manager settings");
                            sb.append(e.getMessage());
                            Slog.e(str, sb.toString());
                        }
                    }
                    if (out != null) {
                        out.close();
                    }
                    FileUtils.setPermissions(file.toString(), 432, -1, -1);
                } catch (IOException e6) {
                    Slog.e(TAG, "IOException when writing pakcagePublicity file: " + e6.getMessage());
                    if (str2 != null) {
                        try {
                            str2.close();
                        } catch (IOException e7) {
                            e = e7;
                            str = TAG;
                            sb = new StringBuilder();
                            sb.append("Error close writing package manager settings");
                            sb.append(e.getMessage());
                            Slog.e(str, sb.toString());
                        }
                    }
                    if (out != null) {
                        out.close();
                    }
                    FileUtils.setPermissions(file.toString(), 432, -1, -1);
                } catch (Throwable th) {
                    if (str2 != null) {
                        try {
                            str2.close();
                        } catch (IOException e8) {
                            Slog.e(TAG, "Error close writing package manager settings" + e8.getMessage());
                            throw th;
                        }
                    }
                    if (out != null) {
                        out.close();
                    }
                    FileUtils.setPermissions(file.toString(), 432, -1, -1);
                    throw th;
                }
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
            File publicityFile = new File(Environment.getDataDirectory(), "system/publicity_all.xml");
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

    private static void initPubPermissions(List<String> pubPermissionGroup, List<String> pubPermission) {
        File file = new File("system", HW_PUBLICITY_PERMISSION_PATH);
        if (file.exists()) {
            InputStream in = null;
            try {
                InputStream in2 = new FileInputStream(file);
                XmlPullParser xml = Xml.newPullParser();
                xml.setInput(in2, "utf-8");
                while (true) {
                    int next = xml.next();
                    int xmlEventType = next;
                    if (next == 1) {
                        try {
                            break;
                        } catch (IOException e) {
                            Log.e(TAG, "close FileInputStram error");
                        }
                    } else if (xmlEventType == 2 && "permissionInfo".equals(xml.getName())) {
                        String group = xml.getAttributeValue(null, "group");
                        String name = xml.getAttributeValue(null, "name");
                        if (!TextUtils.isEmpty(group) && !TextUtils.isEmpty(name)) {
                            pubPermissionGroup.add(group);
                            pubPermission.add(name);
                        }
                    }
                }
                in2.close();
            } catch (FileNotFoundException e2) {
                Log.w(TAG, "Error FileNotFound while trying to read from publicity_permission.xml");
                if (in != null) {
                    in.close();
                }
            } catch (XmlPullParserException e3) {
                Log.e(TAG, "Error XmlPullParser while trying to read from publicity_permission.xml", e3);
                if (in != null) {
                    in.close();
                }
            } catch (IOException e4) {
                Log.e(TAG, "Error while trying to read from publicity_permission.xml", e4);
                if (in != null) {
                    in.close();
                }
            } catch (Throwable th) {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e5) {
                        Log.e(TAG, "close FileInputStram error");
                    }
                }
                throw th;
            }
        }
    }

    private static void filterPublicityInfos(HwInvisibleAppsFilter filter, ParceledListSlice<ApplicationInfo> slice, List<PackagePublicityInfo> pkgPubInfo, PackageManager pm) {
        List<String> list;
        List<String> list2;
        List<String> pubPermissionGroup = new ArrayList<>();
        List<String> pubPermission = new ArrayList<>();
        initPubPermissions(pubPermissionGroup, pubPermission);
        List<String> sysWhiteList = AntiMalPreInstallScanner.getInstance().getSysWhiteList();
        if (slice == null) {
            Slog.w(TAG, "FiltePublicityInfo: getInstalledApplications is null");
            pkgPubInfo.clear();
            return;
        }
        if (sysWhiteList == null) {
            HwInvisibleAppsFilter hwInvisibleAppsFilter = filter;
            PackageManager packageManager = pm;
            List<String> list3 = pubPermissionGroup;
            List<String> list4 = pubPermission;
            List<PackagePublicityInfo> list5 = pkgPubInfo;
        } else if (sysWhiteList.size() == 0) {
            HwInvisibleAppsFilter hwInvisibleAppsFilter2 = filter;
            PackageManager packageManager2 = pm;
            ArrayList arrayList = pubPermissionGroup;
            ArrayList arrayList2 = pubPermission;
            List<PackagePublicityInfo> list6 = pkgPubInfo;
        } else {
            List<ApplicationInfo> allApps = slice.getList();
            if (allApps == null) {
                HwInvisibleAppsFilter hwInvisibleAppsFilter3 = filter;
                PackageManager packageManager3 = pm;
                List<String> list7 = pubPermissionGroup;
                List<String> list8 = pubPermission;
                List<PackagePublicityInfo> list9 = pkgPubInfo;
            } else if (allApps.size() == 0) {
                HwInvisibleAppsFilter hwInvisibleAppsFilter4 = filter;
                PackageManager packageManager4 = pm;
                ArrayList arrayList3 = pubPermissionGroup;
                ArrayList arrayList4 = pubPermission;
                List<PackagePublicityInfo> list10 = pkgPubInfo;
            } else {
                List<ApplicationInfo> allApps2 = filter.filterHideApp(allApps);
                List<String> allInstallAppName = new ArrayList<>();
                for (ApplicationInfo app : allApps2) {
                    allInstallAppName.add(app.packageName);
                }
                List<PackagePublicityInfo> insatlledPkgs = new ArrayList<>();
                List<String> existPkg = new ArrayList<>();
                PackageManagerService pms = ServiceManager.getService("package");
                for (PackagePublicityInfo pubInfo : pkgPubInfo) {
                    String pubInfoPkg = pubInfo.getPackageName();
                    if (!existPkg.contains(pubInfoPkg)) {
                        if (!allInstallAppName.contains(pubInfoPkg) || !sysWhiteList.contains(pubInfoPkg)) {
                            PackageManager packageManager5 = pm;
                            list2 = pubPermissionGroup;
                            list = pubPermission;
                        } else {
                            PackageInfo pkgInfo = pms.getPackageInfo(pubInfoPkg, 4096, 0);
                            pubInfo.setUsePermission(getUsePermission(pkgInfo, pubPermissionGroup, pubPermission));
                            String label = pkgInfo.applicationInfo.loadUnsafeLabel(pm).toString();
                            if (TextUtils.isEmpty(label)) {
                                pubInfo.setLabel(pubInfoPkg);
                            } else {
                                pubInfo.setLabel(label);
                            }
                            list2 = pubPermissionGroup;
                            list = pubPermission;
                            PackageInfo foundPkgInfo = pms.getPackageInfo(pubInfoPkg, 64, 0);
                            if (foundPkgInfo.signatures != null) {
                                pubInfo.setSignature(getSignatureString(foundPkgInfo.signatures));
                                insatlledPkgs.add(pubInfo);
                                existPkg.add(pubInfoPkg);
                            }
                        }
                        pubPermissionGroup = list2;
                        pubPermission = list;
                    }
                }
                PackageManager packageManager6 = pm;
                List<String> list11 = pubPermissionGroup;
                List<String> list12 = pubPermission;
                Slog.w(TAG, "insatlledPkgs size = " + insatlledPkgs.size());
                pkgPubInfo.clear();
                pkgPubInfo.addAll(insatlledPkgs);
                return;
            }
            pkgPubInfo.clear();
            return;
        }
        Slog.w(TAG, "FiltePublicityInfo: getSysWhiteList is null or isEmpty");
        pkgPubInfo.clear();
    }

    private static String getUsePermission(PackageInfo pkgInfo, List<String> pubPermissionGroup, List<String> pubPermission) {
        if (pkgInfo == null) {
            return "";
        }
        String[] requestedPermissions = pkgInfo.requestedPermissions;
        if (requestedPermissions == null || requestedPermissions.length <= 0) {
            return "";
        }
        StringBuilder usePermissionBuilder = new StringBuilder();
        List<String> usePermissionGroup = new ArrayList<>();
        List<String> requestedPermissionList = Arrays.asList(requestedPermissions);
        for (String permission : pubPermission) {
            if (requestedPermissionList.contains(permission)) {
                String prermissionGroup = pubPermissionGroup.get(pubPermission.indexOf(permission));
                if (!usePermissionGroup.contains(prermissionGroup)) {
                    usePermissionGroup.add(prermissionGroup);
                    usePermissionBuilder.append(prermissionGroup);
                    usePermissionBuilder.append(",");
                }
            }
        }
        String usePermission = usePermissionBuilder.toString();
        if (!TextUtils.isEmpty(usePermission) && usePermission.endsWith(",")) {
            usePermission = usePermission.substring(0, usePermission.length() - 1);
        }
        return usePermission;
    }

    private static void sortPackagePublicityInfos(List<PackagePublicityInfo> pkgPubInfo) {
        if (pkgPubInfo != null && pkgPubInfo.size() > 0 && mPubliCityInfoComparator != null) {
            Collections.sort(pkgPubInfo, mPubliCityInfoComparator);
            mPubliCityInfoComparator = null;
        }
    }

    public static List<String> getHwPublicityAppList(Context context) {
        if (mHwPlublicityAppList != null) {
            return mHwPlublicityAppList;
        }
        File publicityFile = new File(Environment.getDataDirectory(), "system/publicity_all.xml");
        if (!publicityFile.exists()) {
            return mHwPlublicityAppList;
        }
        mHwPlublicityAppList = new ArrayList();
        List<PackagePublicityInfo> readResult = getPackagePublicityInfoFromCust(publicityFile);
        PackageManagerService pms = ServiceManager.getService("package");
        PackageManager pm = context.getPackageManager();
        List<String> sysWhiteList = AntiMalPreInstallScanner.getInstance().getSysWhiteList();
        if (sysWhiteList == null || sysWhiteList.size() == 0) {
            Slog.w(TAG, "FiltePublicityInfo: getSysWhiteList is null or isEmpty");
            return mHwPlublicityAppList;
        }
        for (PackagePublicityInfo pp : readResult) {
            String pkName = pp.getPackageName();
            if (sysWhiteList.contains(pkName)) {
                String label = "";
                PackageInfo pkgInfo = pms.getPackageInfo(pkName, 64, 0);
                if (pkgInfo == null) {
                    label = pp.getLabel();
                } else if (!(pkgInfo.signatures == null || pkgInfo.applicationInfo == null)) {
                    if (pp.getSignature().equals(getSignatureString(pkgInfo.signatures))) {
                        label = pkgInfo.applicationInfo.loadUnsafeLabel(pm).toString();
                        if (TextUtils.isEmpty(label)) {
                            label = pkName;
                        }
                    } else {
                        Slog.w(TAG, "getHwPublicityAppList");
                    }
                }
                mHwPlublicityAppList.add(pkName + "+++++" + label);
            }
        }
        return mHwPlublicityAppList;
    }

    private static String getSignatureString(Signature[] signatures) {
        StringBuilder signatureStrBuilder = new StringBuilder();
        for (Signature sign : signatures) {
            signatureStrBuilder.append(sign.toCharsString());
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
            File target = new File(Environment.getDataDirectory(), "system/publicity_all.xml");
            if (target.exists()) {
                return new ParcelFileDescriptor(Libcore.os.open(target.getAbsolutePath(), OsConstants.O_RDONLY, 0));
            }
            return null;
        } catch (ErrnoException e) {
            Slog.w(TAG, "getHwPlublicityAppParcelFileDescriptor file not found .");
            return null;
        }
    }
}
