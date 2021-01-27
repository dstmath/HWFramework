package com.android.server.pm;

import android.content.Context;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.LocalServices;
import com.huawei.server.HwBasicPlatformFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HwEcotaCopyInstallManager {
    private static final String COPY_INSTALL_PACKAGES_TXT = "copyInstallPackages.txt";
    private static final String ECOTA_COPY_INSTALL_DIR = "/data/system/ecota_copy/";
    private static final String ECOTA_INSTALL_PATH = "/cust/ecota/install/";
    private static final String ECOTA_VERSION = SystemProperties.get("ro.product.EcotaVersion", "");
    private static final String TAG = "HwEcotaCopyInstallManager";
    private static final String UTF_8 = "utf-8";
    private List<String> apkFilePathList = new ArrayList();
    private Context context;
    private List<String> ecotaApkList = new ArrayList();
    private List<String> ecotaInstallApkList = new ArrayList();
    private List<String> ecotaUninstallApkList = new ArrayList();
    private DefaultHwPackageManagerUtils hwPackageManagerUtils = HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwPackageManagerUtils();
    private PackageManagerInternal hwPmsEx;
    private List<String> versionAndPackageNameList = new ArrayList();

    public HwEcotaCopyInstallManager(Context ctx) {
        this.context = ctx;
        this.hwPmsEx = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
    }

    public void startEcotaCopyInstall() {
        String oldEcotaVersion;
        Slog.i(TAG, "start ecota copy install ...");
        String ecotaPackagesFile = getFilePath();
        if (ecotaPackagesFile != null) {
            String oldEcotaVersion2 = "";
            this.versionAndPackageNameList = readEcotaVersionAndPackages(ecotaPackagesFile);
            scanApkInEcotaInstallDir(ECOTA_INSTALL_PATH);
            if (this.versionAndPackageNameList.size() != 0 || ECOTA_VERSION.equals("")) {
                if (this.versionAndPackageNameList.size() > 0) {
                    oldEcotaVersion2 = this.versionAndPackageNameList.remove(0);
                }
                if (ECOTA_VERSION.equals(oldEcotaVersion2)) {
                    Slog.i(TAG, "the ecota version do not change ...");
                    return;
                }
                Slog.i(TAG, "the ecota upgrading from " + oldEcotaVersion2 + " to " + ECOTA_VERSION);
                copyInstallEcotaApk(this.apkFilePathList);
                this.ecotaUninstallApkList = getEcotaUninstallApkList(this.versionAndPackageNameList, this.apkFilePathList);
                unInstallEcotaApk(this.ecotaUninstallApkList);
                oldEcotaVersion = ECOTA_VERSION;
            } else {
                Slog.i(TAG, "the first time ecota upgrading ...");
                copyInstallEcotaApk(this.apkFilePathList);
                oldEcotaVersion = ECOTA_VERSION;
            }
            writePackageNamesToFile(ecotaPackagesFile, oldEcotaVersion, this.ecotaApkList);
        }
    }

    private String getFilePath() {
        File fileDir = new File(ECOTA_COPY_INSTALL_DIR);
        try {
            if (fileDir.exists() || fileDir.mkdirs()) {
                return "/data/system/ecota_copy/copyInstallPackages.txt";
            }
            Slog.e(TAG, "Create ecota dir fail.");
            return null;
        } catch (SecurityException e) {
            Slog.e(TAG, "mkdir ecota dir fail.");
            return "/data/system/ecota_copy/copyInstallPackages.txt";
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0053, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0054, code lost:
        $closeResource(r6, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0057, code lost:
        throw r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x005a, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005b, code lost:
        $closeResource(r5, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005e, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0061, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0062, code lost:
        $closeResource(r4, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0065, code lost:
        throw r5;
     */
    private List<String> readEcotaVersionAndPackages(String filePath) {
        Slog.i(TAG, "read ecota version and packages ...");
        List<String> tempApkList = new ArrayList<>();
        if (filePath == null) {
            return tempApkList;
        }
        File ecotaCopyInstallFile = new File(filePath);
        if (!ecotaCopyInstallFile.exists()) {
            return tempApkList;
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(ecotaCopyInstallFile);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, UTF_8);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    $closeResource(null, reader);
                    $closeResource(null, inputStreamReader);
                    $closeResource(null, fileInputStream);
                    break;
                }
                String line2 = line.trim();
                if (!tempApkList.contains(line2)) {
                    tempApkList.add(line2);
                }
            }
        } catch (IOException e) {
            Slog.e(TAG, "readEcotaVersionAndPackages error for IO");
        }
        return tempApkList;
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

    private void scanApkInEcotaInstallDir(String dirName) {
        Slog.i(TAG, "scan apk in ecota install directory ...");
        File fileDir = new File(dirName);
        if (fileDir.exists() && fileDir.isDirectory()) {
            File[] apkFiles = fileDir.listFiles();
            for (int i = 0; i < apkFiles.length; i++) {
                if (apkFiles[i].isFile() && this.hwPackageManagerUtils.isPackageFilenameEx(apkFiles[i].getName())) {
                    this.apkFilePathList.add(apkFiles[i].getPath());
                }
            }
        }
    }

    private List<String> getEcotaUninstallApkList(List<String> recordInstalledApkList, List<String> apkFilePathList2) {
        if (recordInstalledApkList.size() == 0 || apkFilePathList2.size() == 0) {
            return recordInstalledApkList;
        }
        List<String> needUninstallPackageNameList = new ArrayList<>(recordInstalledApkList);
        for (int i = 0; i < apkFilePathList2.size(); i++) {
            String tempPackageName = this.hwPackageManagerUtils.getPackageNameFromApkEx(apkFilePathList2.get(i));
            if (needUninstallPackageNameList.contains(tempPackageName)) {
                needUninstallPackageNameList.remove(tempPackageName);
            }
        }
        return needUninstallPackageNameList;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0043, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0044, code lost:
        $closeResource(r4, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0047, code lost:
        throw r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004a, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x004b, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x004e, code lost:
        throw r4;
     */
    private void writePackageNamesToFile(String filePath, String ecotaVersion, List<String> packageNameList) {
        Slog.i(TAG, "write package names and ecota version to file ...");
        if (filePath != null && packageNameList != null) {
            try {
                FileWriter fileWriter = new FileWriter(new File(filePath));
                BufferedWriter out = new BufferedWriter(fileWriter);
                out.write(ecotaVersion);
                for (int i = 0; i < packageNameList.size(); i++) {
                    out.newLine();
                    out.write(packageNameList.get(i));
                }
                out.flush();
                $closeResource(null, out);
                $closeResource(null, fileWriter);
            } catch (IOException e) {
                Slog.e(TAG, "writePackageNamesToFile error for IO");
            }
        }
    }

    private void copyInstallEcotaApk(List<String> apkFileList) {
        Slog.i(TAG, "copy install ecota apk ...");
        if (!(apkFileList == null || apkFileList.size() == 0)) {
            for (int i = 0; i < apkFileList.size(); i++) {
                String packageName = this.hwPackageManagerUtils.getPackageNameFromApkEx(apkFileList.get(i));
                if (TextUtils.isEmpty(packageName)) {
                    Slog.w(TAG, "Illegal apk file:" + apkFileList.get(i));
                } else {
                    this.ecotaApkList.add(packageName);
                    this.hwPmsEx.installPackageAsUser(apkFileList.get(i), (IPackageInstallObserver2) null, 2, packageName, 0);
                }
            }
        }
    }

    private void unInstallEcotaApk(List<String> ecotaUninstallApkList2) {
        PackageManager pm;
        Slog.i(TAG, "uninstall ecota apk ...");
        if (!(ecotaUninstallApkList2 == null || ecotaUninstallApkList2.size() == 0 || (pm = this.context.getPackageManager()) == null)) {
            for (int i = 0; i < ecotaUninstallApkList2.size(); i++) {
                pm.deletePackageAsUser(ecotaUninstallApkList2.get(i), null, 2, -1);
            }
        }
    }
}
