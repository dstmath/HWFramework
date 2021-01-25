package com.android.server.pm.dex;

import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.os.FileUtils;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.ByteStringUtils;
import android.util.EventLog;
import android.util.PackageUtils;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.pm.Installer;
import com.android.server.pm.dex.PackageDynamicCodeLoading;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class DynamicCodeLogger {
    private static final String DCL_DEX_SUBTAG = "dcl";
    private static final String DCL_NATIVE_SUBTAG = "dcln";
    private static final int SNET_TAG = 1397638484;
    private static final String TAG = "DynamicCodeLogger";
    private final Installer mInstaller;
    private final PackageDynamicCodeLoading mPackageDynamicCodeLoading;
    private final IPackageManager mPackageManager;

    DynamicCodeLogger(IPackageManager pms, Installer installer) {
        this(pms, installer, new PackageDynamicCodeLoading());
    }

    @VisibleForTesting
    DynamicCodeLogger(IPackageManager pms, Installer installer, PackageDynamicCodeLoading packageDynamicCodeLoading) {
        this.mPackageManager = pms;
        this.mPackageDynamicCodeLoading = packageDynamicCodeLoading;
        this.mInstaller = installer;
    }

    public Set<String> getAllPackagesWithDynamicCodeLoading() {
        return this.mPackageDynamicCodeLoading.getAllPackagesWithDynamicCodeLoading();
    }

    /* JADX WARNING: Removed duplicated region for block: B:43:0x0101  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0104  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x011a  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0162  */
    public void logDynamicCodeLoading(String packageName) {
        boolean needWrite;
        ApplicationInfo appInfo;
        int storageFlags;
        SparseArray<ApplicationInfo> appInfoByUser;
        PackageDynamicCodeLoading.PackageDynamicCode info;
        Iterator<Map.Entry<String, PackageDynamicCodeLoading.DynamicCodeFile>> it;
        int i;
        String str;
        ApplicationInfo appInfo2;
        byte[] hash;
        String subtag;
        String message;
        String message2;
        Installer.InstallerException e;
        ApplicationInfo appInfo3;
        PackageDynamicCodeLoading.PackageDynamicCode info2 = getPackageDynamicCodeInfo(packageName);
        if (info2 != null) {
            SparseArray<ApplicationInfo> appInfoByUser2 = new SparseArray<>();
            Iterator<Map.Entry<String, PackageDynamicCodeLoading.DynamicCodeFile>> it2 = info2.mFileUsageMap.entrySet().iterator();
            boolean needWrite2 = false;
            while (it2.hasNext()) {
                Map.Entry<String, PackageDynamicCodeLoading.DynamicCodeFile> fileEntry = it2.next();
                String filePath = fileEntry.getKey();
                PackageDynamicCodeLoading.DynamicCodeFile fileInfo = fileEntry.getValue();
                int userId = fileInfo.mUserId;
                if (appInfoByUser2.indexOfKey(userId) >= 0) {
                    appInfo = appInfoByUser2.get(userId);
                    needWrite = needWrite2;
                } else {
                    try {
                        PackageInfo ownerInfo = this.mPackageManager.getPackageInfo(packageName, 0, userId);
                        appInfo3 = ownerInfo == null ? null : ownerInfo.applicationInfo;
                    } catch (RemoteException e2) {
                        appInfo3 = null;
                    }
                    appInfoByUser2.put(userId, appInfo3);
                    if (appInfo3 == null) {
                        Slog.d(TAG, "Could not find package " + packageName + " for user " + userId);
                        appInfo = appInfo3;
                        needWrite = needWrite2 | this.mPackageDynamicCodeLoading.removeUserPackage(packageName, userId);
                    } else {
                        appInfo = appInfo3;
                        needWrite = needWrite2;
                    }
                }
                if (appInfo == null) {
                    needWrite2 = needWrite;
                } else {
                    if (fileIsUnder(filePath, appInfo.credentialProtectedDataDir)) {
                        storageFlags = 2;
                    } else if (fileIsUnder(filePath, appInfo.deviceProtectedDataDir)) {
                        storageFlags = 1;
                    } else {
                        Slog.e(TAG, "Could not infer CE/DE storage for path " + filePath);
                        needWrite2 = needWrite | this.mPackageDynamicCodeLoading.removeFile(packageName, filePath, userId);
                        info2 = info2;
                        appInfoByUser2 = appInfoByUser2;
                    }
                    try {
                        Installer installer = this.mInstaller;
                        int i2 = appInfo.uid;
                        String str2 = appInfo.volumeUuid;
                        info = info2;
                        appInfo2 = appInfo;
                        appInfoByUser = appInfoByUser2;
                        str = TAG;
                        it = it2;
                        i = 0;
                        try {
                            hash = installer.hashSecondaryDexFile(filePath, packageName, i2, str2, storageFlags);
                        } catch (Installer.InstallerException e3) {
                            e = e3;
                            Slog.e(str, "Got InstallerException when hashing file " + filePath + ": " + e.getMessage());
                            hash = null;
                            if (fileInfo.mFileType == 'D') {
                            }
                            message = PackageUtils.computeSha256Digest(new File(filePath).getName().getBytes());
                            if (hash == null) {
                            }
                            Slog.d(str, "Got no hash for " + filePath);
                            needWrite |= this.mPackageDynamicCodeLoading.removeFile(packageName, filePath, userId);
                            message2 = message;
                            while (r6.hasNext()) {
                            }
                            needWrite2 = needWrite;
                            it2 = it;
                            info2 = info;
                            appInfoByUser2 = appInfoByUser;
                        }
                    } catch (Installer.InstallerException e4) {
                        e = e4;
                        info = info2;
                        appInfoByUser = appInfoByUser2;
                        it = it2;
                        appInfo2 = appInfo;
                        str = TAG;
                        i = 0;
                        Slog.e(str, "Got InstallerException when hashing file " + filePath + ": " + e.getMessage());
                        hash = null;
                        if (fileInfo.mFileType == 'D') {
                        }
                        message = PackageUtils.computeSha256Digest(new File(filePath).getName().getBytes());
                        if (hash == null) {
                        }
                        Slog.d(str, "Got no hash for " + filePath);
                        needWrite |= this.mPackageDynamicCodeLoading.removeFile(packageName, filePath, userId);
                        message2 = message;
                        while (r6.hasNext()) {
                        }
                        needWrite2 = needWrite;
                        it2 = it;
                        info2 = info;
                        appInfoByUser2 = appInfoByUser;
                    }
                    if (fileInfo.mFileType == 'D') {
                        subtag = DCL_DEX_SUBTAG;
                    } else {
                        subtag = DCL_NATIVE_SUBTAG;
                    }
                    message = PackageUtils.computeSha256Digest(new File(filePath).getName().getBytes());
                    if (hash == null && hash.length == 32) {
                        message2 = message + ' ' + ByteStringUtils.toHexString(hash);
                    } else {
                        Slog.d(str, "Got no hash for " + filePath);
                        needWrite |= this.mPackageDynamicCodeLoading.removeFile(packageName, filePath, userId);
                        message2 = message;
                    }
                    for (String loadingPackageName : fileInfo.mLoadingPackages) {
                        int loadingUid = -1;
                        if (loadingPackageName.equals(packageName)) {
                            loadingUid = appInfo2.uid;
                        } else {
                            try {
                                loadingUid = this.mPackageManager.getPackageUid(loadingPackageName, i, userId);
                            } catch (RemoteException e5) {
                            }
                        }
                        if (loadingUid != -1) {
                            writeDclEvent(subtag, loadingUid, message2);
                        }
                    }
                    needWrite2 = needWrite;
                    it2 = it;
                    info2 = info;
                    appInfoByUser2 = appInfoByUser;
                }
            }
            if (needWrite2) {
                this.mPackageDynamicCodeLoading.maybeWriteAsync();
            }
        }
    }

    private boolean fileIsUnder(String filePath, String directoryPath) {
        if (directoryPath == null) {
            return false;
        }
        try {
            return FileUtils.contains(new File(directoryPath).getCanonicalPath(), new File(filePath).getCanonicalPath());
        } catch (IOException e) {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public PackageDynamicCodeLoading.PackageDynamicCode getPackageDynamicCodeInfo(String packageName) {
        return this.mPackageDynamicCodeLoading.getPackageDynamicCodeInfo(packageName);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void writeDclEvent(String subtag, int uid, String message) {
        EventLog.writeEvent((int) SNET_TAG, subtag, Integer.valueOf(uid), message);
    }

    /* access modifiers changed from: package-private */
    public void recordDex(int loaderUserId, String dexPath, String owningPackageName, String loadingPackageName) {
        if (this.mPackageDynamicCodeLoading.record(owningPackageName, dexPath, 68, loaderUserId, loadingPackageName)) {
            this.mPackageDynamicCodeLoading.maybeWriteAsync();
        }
    }

    public void recordNative(int loadingUid, String path) {
        try {
            String[] packages = this.mPackageManager.getPackagesForUid(loadingUid);
            if (packages != null && packages.length != 0) {
                String loadingPackageName = packages[0];
                if (this.mPackageDynamicCodeLoading.record(loadingPackageName, path, 78, UserHandle.getUserId(loadingUid), loadingPackageName)) {
                    this.mPackageDynamicCodeLoading.maybeWriteAsync();
                }
            }
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: package-private */
    public void clear() {
        this.mPackageDynamicCodeLoading.clear();
    }

    /* access modifiers changed from: package-private */
    public void removePackage(String packageName) {
        if (this.mPackageDynamicCodeLoading.removePackage(packageName)) {
            this.mPackageDynamicCodeLoading.maybeWriteAsync();
        }
    }

    /* access modifiers changed from: package-private */
    public void removeUserPackage(String packageName, int userId) {
        if (this.mPackageDynamicCodeLoading.removeUserPackage(packageName, userId)) {
            this.mPackageDynamicCodeLoading.maybeWriteAsync();
        }
    }

    /* access modifiers changed from: package-private */
    public void readAndSync(Map<String, Set<Integer>> packageToUsersMap) {
        this.mPackageDynamicCodeLoading.read();
        this.mPackageDynamicCodeLoading.syncData(packageToUsersMap);
    }

    /* access modifiers changed from: package-private */
    public void writeNow() {
        this.mPackageDynamicCodeLoading.writeNow();
    }
}
