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
import java.util.List;
import java.util.Locale;
import libcore.io.Libcore;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class PackagePublicityUtils {
    private static final String HW_PUBLICITY_ALL_DIRS = "/system/etc:/version/etc:cust/etc:product/etc:vendor/etc";
    private static final String HW_PUBLICITY_PERMISSION_PATH = "emui/china/xml/publicity_permission.xml";
    public static final Comparator<PackagePublicityInfo> PUBLICITYINFO_COMPARATOR = new Comparator<PackagePublicityInfo>() {
        private final Collator sCollator = Collator.getInstance(Locale.CHINA);

        public int compare(PackagePublicityInfo object1, PackagePublicityInfo object2) {
            int compareResult = this.sCollator.compare(object1.getLabel(), object2.getLabel());
            if (compareResult != 0) {
                return compareResult;
            }
            return this.sCollator.compare(object1.getPackageName(), object2.getPackageName());
        }
    };
    private static final String TAG = "PackagePublicityUtils";
    private static List<String> mHwPlublicityAppList = null;

    @SuppressLint({"PreferForInArrayList"})
    private static List<PackagePublicityInfo> loadPackagePublicityInfo() {
        ArrayList<File> publicityFileList = new ArrayList();
        try {
            publicityFileList = getPublicityFileList("publicity_all.xml");
        } catch (NoClassDefFoundError er) {
            Slog.e(TAG, er.getMessage());
        }
        List<PackagePublicityInfo> publicityInfos = new ArrayList();
        for (File file : publicityFileList) {
            if (file != null && (file.exists() ^ 1) == 0) {
                publicityInfos.addAll(getPackagePublicityInfoFromCust(file));
            }
        }
        return publicityInfos;
    }

    private static ArrayList<File> getPublicityFileList(String fileName) {
        ArrayList<File> fileList = new ArrayList();
        if (TextUtils.isEmpty(fileName)) {
            Log.e(TAG, "Error: file = [" + fileName + "]");
            return fileList;
        }
        String[] dirs = HW_PUBLICITY_ALL_DIRS.split(":");
        for (String file : dirs) {
            File file2 = new File(file, fileName);
            if (file2.exists()) {
                fileList.add(file2);
            }
        }
        if (fileList.size() == 0) {
            Log.w(TAG, "No publicity file found for:" + fileName);
        }
        return fileList;
    }

    public static List<PackagePublicityInfo> getPackagePublicityInfoFromCust(File file) {
        FileNotFoundException e;
        XmlPullParserException e2;
        IOException e3;
        Throwable th;
        List<PackagePublicityInfo> result = new ArrayList();
        if (file == null || (file.exists() ^ 1) != 0) {
            return result;
        }
        InputStream in = null;
        try {
            InputStream in2 = new FileInputStream(file);
            try {
                XmlPullParser xml = Xml.newPullParser();
                xml.setInput(in2, "utf-8");
                while (true) {
                    int xmlEventType = xml.next();
                    if (xmlEventType == 1) {
                        if (in2 != null) {
                            try {
                                in2.close();
                            } catch (IOException e4) {
                                Slog.e(TAG, "close FileInputStram error");
                            }
                        }
                    } else if (xmlEventType == 2 && "packageInfo".equals(xml.getName())) {
                        PackagePublicityInfo packagePublicityInfo = new PackagePublicityInfo();
                        String mPackage = xml.getAttributeValue(null, "package");
                        packagePublicityInfo.setPackageName(mPackage);
                        String label = xml.getAttributeValue(null, HwDevicePolicyManagerServiceUtil.EXCHANGE_LABEL);
                        if (TextUtils.isEmpty(label)) {
                            packagePublicityInfo.setLabel(mPackage);
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
                        if (!("".equals(mPackage) || mPackage == null)) {
                            result.add(packagePublicityInfo);
                        }
                    }
                }
            } catch (FileNotFoundException e5) {
                e = e5;
                in = in2;
            } catch (XmlPullParserException e6) {
                e2 = e6;
                in = in2;
            } catch (IOException e7) {
                e3 = e7;
                in = in2;
            } catch (Throwable th2) {
                th = th2;
                in = in2;
            }
        } catch (FileNotFoundException e8) {
            e = e8;
            try {
                Slog.w(TAG, "Error FileNotFound while trying to read from publicity_all.xml", e);
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e9) {
                        Slog.e(TAG, "close FileInputStram error");
                    }
                }
                return result;
            } catch (Throwable th3) {
                th = th3;
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e10) {
                        Slog.e(TAG, "close FileInputStram error");
                    }
                }
                throw th;
            }
        } catch (XmlPullParserException e11) {
            e2 = e11;
            Slog.e(TAG, "Error XmlPullParser while trying to read from publicity_all.xml", e2);
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e12) {
                    Slog.e(TAG, "close FileInputStram error");
                }
            }
            return result;
        } catch (IOException e13) {
            e3 = e13;
            Slog.e(TAG, "Error while trying to read from publicity_all.xml", e3);
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e14) {
                    Slog.e(TAG, "close FileInputStram error");
                }
            }
            return result;
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0116 A:{SYNTHETIC, Splitter: B:24:0x0116} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x011b A:{Catch:{ IOException -> 0x0228 }} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x01f0 A:{SYNTHETIC, Splitter: B:56:0x01f0} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x01f5 A:{Catch:{ IOException -> 0x0207 }} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0197 A:{SYNTHETIC, Splitter: B:45:0x0197} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x019c A:{Catch:{ IOException -> 0x01ae }} */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x024c A:{SYNTHETIC, Splitter: B:67:0x024c} */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x0251 A:{Catch:{ IOException -> 0x0262 }} */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x024c A:{SYNTHETIC, Splitter: B:67:0x024c} */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x0251 A:{Catch:{ IOException -> 0x0262 }} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0116 A:{SYNTHETIC, Splitter: B:24:0x0116} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x011b A:{Catch:{ IOException -> 0x0228 }} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x01f0 A:{SYNTHETIC, Splitter: B:56:0x01f0} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x01f5 A:{Catch:{ IOException -> 0x0207 }} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0197 A:{SYNTHETIC, Splitter: B:45:0x0197} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x019c A:{Catch:{ IOException -> 0x01ae }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void writeAllPakcagePublicityInfoIntoFile(Context context, ParceledListSlice<ApplicationInfo> slice) {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        IllegalArgumentException e3;
        File file = getPublicityFile();
        if (file != null && !file.exists()) {
            List<PackagePublicityInfo> allPackagePublicityInfo = loadPackagePublicityInfo();
            if (allPackagePublicityInfo.size() != 0) {
                handlePublicityInfos(context, slice, allPackagePublicityInfo);
                FileOutputStream out = null;
                BufferedOutputStream str = null;
                try {
                    FileOutputStream out2 = new FileOutputStream(file);
                    try {
                        BufferedOutputStream str2 = new BufferedOutputStream(out2);
                        try {
                            XmlSerializer serializer = new FastXmlSerializer();
                            serializer.setOutput(str2, "utf-8");
                            serializer.startDocument(null, Boolean.valueOf(true));
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
                            str2.flush();
                            FileUtils.sync(out2);
                            if (str2 != null) {
                                try {
                                    str2.close();
                                } catch (IOException e4) {
                                    Slog.e(TAG, "Error close writing package manager settings" + e4.getMessage());
                                }
                            }
                            if (out2 != null) {
                                out2.close();
                            }
                            FileUtils.setPermissions(file.toString(), 432, -1, -1);
                        } catch (FileNotFoundException e5) {
                            e2 = e5;
                            str = str2;
                            out = out2;
                            try {
                                Slog.e(TAG, "File not found when writing pakcagePublicity file: " + e2.getMessage());
                                if (str != null) {
                                    try {
                                        str.close();
                                    } catch (IOException e42) {
                                        Slog.e(TAG, "Error close writing package manager settings" + e42.getMessage());
                                    }
                                }
                                if (out != null) {
                                    out.close();
                                }
                                FileUtils.setPermissions(file.toString(), 432, -1, -1);
                            } catch (Throwable th2) {
                                th = th2;
                                if (str != null) {
                                }
                                if (out != null) {
                                }
                                FileUtils.setPermissions(file.toString(), 432, -1, -1);
                                throw th;
                            }
                        } catch (IllegalArgumentException e6) {
                            e3 = e6;
                            str = str2;
                            out = out2;
                            Slog.e(TAG, "IllegalArgument when writing pakcagePublicity file: " + e3.getMessage());
                            if (str != null) {
                                try {
                                    str.close();
                                } catch (IOException e422) {
                                    Slog.e(TAG, "Error close writing package manager settings" + e422.getMessage());
                                }
                            }
                            if (out != null) {
                                out.close();
                            }
                            FileUtils.setPermissions(file.toString(), 432, -1, -1);
                        } catch (IOException e7) {
                            e422 = e7;
                            str = str2;
                            out = out2;
                            Slog.e(TAG, "IOException when writing pakcagePublicity file: " + e422.getMessage());
                            if (str != null) {
                                try {
                                    str.close();
                                } catch (IOException e4222) {
                                    Slog.e(TAG, "Error close writing package manager settings" + e4222.getMessage());
                                }
                            }
                            if (out != null) {
                                out.close();
                            }
                            FileUtils.setPermissions(file.toString(), 432, -1, -1);
                        } catch (Throwable th3) {
                            th = th3;
                            str = str2;
                            out = out2;
                            if (str != null) {
                                try {
                                    str.close();
                                } catch (IOException e42222) {
                                    Slog.e(TAG, "Error close writing package manager settings" + e42222.getMessage());
                                    throw th;
                                }
                            }
                            if (out != null) {
                                out.close();
                            }
                            FileUtils.setPermissions(file.toString(), 432, -1, -1);
                            throw th;
                        }
                    } catch (FileNotFoundException e8) {
                        e2 = e8;
                        out = out2;
                        Slog.e(TAG, "File not found when writing pakcagePublicity file: " + e2.getMessage());
                        if (str != null) {
                        }
                        if (out != null) {
                        }
                        FileUtils.setPermissions(file.toString(), 432, -1, -1);
                    } catch (IllegalArgumentException e9) {
                        e3 = e9;
                        out = out2;
                        Slog.e(TAG, "IllegalArgument when writing pakcagePublicity file: " + e3.getMessage());
                        if (str != null) {
                        }
                        if (out != null) {
                        }
                        FileUtils.setPermissions(file.toString(), 432, -1, -1);
                    } catch (IOException e10) {
                        e42222 = e10;
                        out = out2;
                        Slog.e(TAG, "IOException when writing pakcagePublicity file: " + e42222.getMessage());
                        if (str != null) {
                        }
                        if (out != null) {
                        }
                        FileUtils.setPermissions(file.toString(), 432, -1, -1);
                    } catch (Throwable th4) {
                        th = th4;
                        out = out2;
                        if (str != null) {
                        }
                        if (out != null) {
                        }
                        FileUtils.setPermissions(file.toString(), 432, -1, -1);
                        throw th;
                    }
                } catch (FileNotFoundException e11) {
                    e2 = e11;
                    Slog.e(TAG, "File not found when writing pakcagePublicity file: " + e2.getMessage());
                    if (str != null) {
                    }
                    if (out != null) {
                    }
                    FileUtils.setPermissions(file.toString(), 432, -1, -1);
                } catch (IllegalArgumentException e12) {
                    e3 = e12;
                    Slog.e(TAG, "IllegalArgument when writing pakcagePublicity file: " + e3.getMessage());
                    if (str != null) {
                    }
                    if (out != null) {
                    }
                    FileUtils.setPermissions(file.toString(), 432, -1, -1);
                } catch (IOException e13) {
                    e42222 = e13;
                    Slog.e(TAG, "IOException when writing pakcagePublicity file: " + e42222.getMessage());
                    if (str != null) {
                    }
                    if (out != null) {
                    }
                    FileUtils.setPermissions(file.toString(), 432, -1, -1);
                }
            }
        }
    }

    public static File getPublicityFile() {
        SecurityException e;
        File publicityFile = null;
        try {
            File systemDir = new File(Environment.getDataDirectory(), "system");
            if (systemDir.exists() || systemDir.mkdirs()) {
                File publicityFile2 = new File(systemDir, "publicity_all.xml");
                try {
                    if (publicityFile2.exists()) {
                        return null;
                    }
                    Slog.i(TAG, "first boot. init publicity_all.xml");
                    publicityFile = publicityFile2;
                    return publicityFile;
                } catch (SecurityException e2) {
                    e = e2;
                    publicityFile = publicityFile2;
                    Slog.i(TAG, "PakcagePublicity file SecurityException: " + e.getMessage());
                    return publicityFile;
                }
            }
            Slog.i(TAG, "PakcagePublicity file create error");
            return null;
        } catch (SecurityException e3) {
            e = e3;
            Slog.i(TAG, "PakcagePublicity file SecurityException: " + e.getMessage());
            return publicityFile;
        }
    }

    private static void handlePublicityInfos(Context context, ParceledListSlice<ApplicationInfo> slice, List<PackagePublicityInfo> pkgPubInfo) {
        filterPublicityInfos(new HwInvisibleAppsFilter(context), slice, pkgPubInfo, context.getPackageManager());
        sortPackagePublicityInfos(pkgPubInfo);
    }

    /* JADX WARNING: Removed duplicated region for block: B:54:0x00c6 A:{SYNTHETIC, Splitter: B:54:0x00c6} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x006c A:{SYNTHETIC, Splitter: B:25:0x006c} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00a9 A:{SYNTHETIC, Splitter: B:46:0x00a9} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x008e A:{SYNTHETIC, Splitter: B:38:0x008e} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void initPubPermissions(List<String> pubPermissionGroup, List<String> pubPermission) {
        Throwable th;
        XmlPullParserException e;
        IOException e2;
        File file = new File("system", HW_PUBLICITY_PERMISSION_PATH);
        if (file.exists()) {
            InputStream in = null;
            try {
                InputStream in2 = new FileInputStream(file);
                try {
                    XmlPullParser xml = Xml.newPullParser();
                    xml.setInput(in2, "utf-8");
                    while (true) {
                        int xmlEventType = xml.next();
                        if (xmlEventType == 1) {
                            break;
                        } else if (xmlEventType == 2 && "permissionInfo".equals(xml.getName())) {
                            String group = xml.getAttributeValue(null, "group");
                            String name = xml.getAttributeValue(null, "name");
                            if (!(TextUtils.isEmpty(group) || (TextUtils.isEmpty(name) ^ 1) == 0)) {
                                pubPermissionGroup.add(group);
                                pubPermission.add(name);
                            }
                        }
                    }
                    if (in2 != null) {
                        try {
                            in2.close();
                        } catch (IOException e3) {
                            Log.e(TAG, "close FileInputStram error");
                        }
                    }
                } catch (FileNotFoundException e4) {
                    in = in2;
                    try {
                        Log.w(TAG, "Error FileNotFound while trying to read from publicity_permission.xml");
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e5) {
                                Log.e(TAG, "close FileInputStram error");
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e6) {
                                Log.e(TAG, "close FileInputStram error");
                            }
                        }
                        throw th;
                    }
                } catch (XmlPullParserException e7) {
                    e = e7;
                    in = in2;
                    Log.e(TAG, "Error XmlPullParser while trying to read from publicity_permission.xml", e);
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e8) {
                            Log.e(TAG, "close FileInputStram error");
                        }
                    }
                } catch (IOException e9) {
                    e2 = e9;
                    in = in2;
                    Log.e(TAG, "Error while trying to read from publicity_permission.xml", e2);
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e10) {
                            Log.e(TAG, "close FileInputStram error");
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    in = in2;
                    if (in != null) {
                    }
                    throw th;
                }
            } catch (FileNotFoundException e11) {
                Log.w(TAG, "Error FileNotFound while trying to read from publicity_permission.xml");
                if (in != null) {
                }
            } catch (XmlPullParserException e12) {
                e = e12;
                Log.e(TAG, "Error XmlPullParser while trying to read from publicity_permission.xml", e);
                if (in != null) {
                }
            } catch (IOException e13) {
                e2 = e13;
                Log.e(TAG, "Error while trying to read from publicity_permission.xml", e2);
                if (in != null) {
                }
            }
        }
    }

    private static void filterPublicityInfos(HwInvisibleAppsFilter filter, ParceledListSlice<ApplicationInfo> slice, List<PackagePublicityInfo> pkgPubInfo, PackageManager pm) {
        List<String> pubPermissionGroup = new ArrayList();
        List<String> pubPermission = new ArrayList();
        initPubPermissions(pubPermissionGroup, pubPermission);
        List<String> sysWhiteList = AntiMalPreInstallScanner.getInstance().getSysWhiteList();
        if (slice == null) {
            Slog.w(TAG, "FiltePublicityInfo: getInstalledApplications is null");
            pkgPubInfo.clear();
        } else if (sysWhiteList == null || sysWhiteList.size() == 0) {
            Slog.w(TAG, "FiltePublicityInfo: getSysWhiteList is null or isEmpty");
            pkgPubInfo.clear();
        } else {
            List<ApplicationInfo> allApps = slice.getList();
            if (allApps == null || allApps.size() == 0) {
                pkgPubInfo.clear();
                return;
            }
            allApps = filter.filterHideApp(allApps);
            List<String> allInstallAppName = new ArrayList();
            for (ApplicationInfo app : allApps) {
                allInstallAppName.add(app.packageName);
            }
            List<PackagePublicityInfo> insatlledPkgs = new ArrayList();
            List<String> existPkg = new ArrayList();
            PackageManagerService pms = (PackageManagerService) ServiceManager.getService("package");
            for (PackagePublicityInfo pubInfo : pkgPubInfo) {
                String pubInfoPkg = pubInfo.getPackageName();
                if (!existPkg.contains(pubInfoPkg) && allInstallAppName.contains(pubInfoPkg) && sysWhiteList.contains(pubInfoPkg)) {
                    PackageInfo pkgInfo = pms.getPackageInfo(pubInfoPkg, 4096, 0);
                    pubInfo.setUsePermission(getUsePermission(pkgInfo, pubPermissionGroup, pubPermission));
                    String label = pkgInfo.applicationInfo.loadLabel(pm).toString();
                    if (TextUtils.isEmpty(label)) {
                        pubInfo.setLabel(pubInfoPkg);
                    } else {
                        pubInfo.setLabel(label);
                    }
                    PackageInfo foundPkgInfo = pms.getPackageInfo(pubInfoPkg, 64, 0);
                    if (foundPkgInfo.signatures != null) {
                        pubInfo.setSignature(getSignatureString(foundPkgInfo.signatures));
                        insatlledPkgs.add(pubInfo);
                        existPkg.add(pubInfoPkg);
                    }
                }
            }
            Slog.w(TAG, "insatlledPkgs size = " + insatlledPkgs.size());
            pkgPubInfo.clear();
            pkgPubInfo.addAll(insatlledPkgs);
        }
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
        List<String> usePermissionGroup = new ArrayList();
        List<String> requestedPermissionList = Arrays.asList(requestedPermissions);
        for (String permission : pubPermission) {
            if (requestedPermissionList.contains(permission)) {
                String prermissionGroup = (String) pubPermissionGroup.get(pubPermission.indexOf(permission));
                if (!usePermissionGroup.contains(prermissionGroup)) {
                    usePermissionGroup.add(prermissionGroup);
                    usePermissionBuilder.append(prermissionGroup).append(",");
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
        if (pkgPubInfo != null && pkgPubInfo.size() > 0 && PUBLICITYINFO_COMPARATOR != null) {
            Collections.sort(pkgPubInfo, PUBLICITYINFO_COMPARATOR);
        }
    }

    public static List<String> getHwPublicityAppList(Context context) {
        if (mHwPlublicityAppList == null) {
            File publicityFile = new File(Environment.getDataDirectory(), "system/publicity_all.xml");
            if (publicityFile.exists()) {
                mHwPlublicityAppList = new ArrayList();
                List<PackagePublicityInfo> readResult = getPackagePublicityInfoFromCust(publicityFile);
                PackageManagerService pms = (PackageManagerService) ServiceManager.getService("package");
                PackageManager pm = context.getPackageManager();
                List<String> sysWhiteList = AntiMalPreInstallScanner.getInstance().getSysWhiteList();
                if (sysWhiteList == null || sysWhiteList.size() == 0) {
                    Slog.w(TAG, "FiltePublicityInfo: getSysWhiteList is null or isEmpty");
                    return mHwPlublicityAppList;
                }
                for (PackagePublicityInfo pp : readResult) {
                    String pkName = pp.getPackageName();
                    PackageInfo pkgInfo = pms.getPackageInfo(pkName, 64, 0);
                    if (!(pkgInfo == null || pkgInfo.signatures == null || pkgInfo.applicationInfo == null || !sysWhiteList.contains(pkName) || !pp.getSignature().equals(getSignatureString(pkgInfo.signatures)))) {
                        String label = pkgInfo.applicationInfo.loadLabel(pm).toString();
                        if (TextUtils.isEmpty(label)) {
                            label = pkName;
                        }
                        mHwPlublicityAppList.add(pkName + "+++++" + label);
                    }
                }
            }
        }
        return mHwPlublicityAppList;
    }

    private static String getSignatureString(Signature[] signatures) {
        StringBuilder signatureStrBuilder = new StringBuilder();
        for (Signature toCharsString : signatures) {
            signatureStrBuilder.append(toCharsString.toCharsString()).append(",");
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
