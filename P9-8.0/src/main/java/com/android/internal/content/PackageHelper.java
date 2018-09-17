package com.android.internal.content;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInstaller.SessionParams;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageParser.PackageLite;
import android.os.Environment;
import android.os.FileUtils;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.storage.IStorageManager;
import android.os.storage.IStorageManager.Stub;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.provider.Settings.Global;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.Log;
import com.android.internal.R;
import com.android.internal.content.NativeLibraryHelper.Handle;
import huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import libcore.io.IoUtils;

public class PackageHelper {
    public static final int APP_INSTALL_AUTO = 0;
    public static final int APP_INSTALL_EXTERNAL = 2;
    public static final int APP_INSTALL_INTERNAL = 1;
    public static final int RECOMMEND_FAILED_ALREADY_EXISTS = -4;
    public static final int RECOMMEND_FAILED_INSUFFICIENT_STORAGE = -1;
    public static final int RECOMMEND_FAILED_INVALID_APK = -2;
    public static final int RECOMMEND_FAILED_INVALID_LOCATION = -3;
    public static final int RECOMMEND_FAILED_INVALID_URI = -6;
    public static final int RECOMMEND_FAILED_VERSION_DOWNGRADE = -7;
    public static final int RECOMMEND_INSTALL_EPHEMERAL = 3;
    public static final int RECOMMEND_INSTALL_EXTERNAL = 2;
    public static final int RECOMMEND_INSTALL_INTERNAL = 1;
    public static final int RECOMMEND_MEDIA_UNAVAILABLE = -5;
    private static final String TAG = "PackageHelper";
    private static final boolean localLOGV = false;
    private static HwCustPackageHelper mCustPkgHelper = ((HwCustPackageHelper) HwCustUtils.createObj(HwCustPackageHelper.class, new Object[0]));
    private static TestableInterface sDefaultTestableInterface = null;

    public static abstract class TestableInterface {
        public abstract boolean getAllow3rdPartyOnInternalConfig(Context context);

        public abstract File getDataDirectory();

        public abstract ApplicationInfo getExistingAppInfo(Context context, String str);

        public abstract boolean getForceAllowOnExternalSetting(Context context);

        public abstract StorageManager getStorageManager(Context context);

        public boolean fitsOnInternalStorage(Context context, SessionParams params) throws IOException {
            return params.sizeBytes <= getStorageManager(context).getAllocatableBytes(getDataDirectory(), PackageHelper.translateAllocateFlags(params.installFlags));
        }
    }

    public static IStorageManager getStorageManager() throws RemoteException {
        IBinder service = ServiceManager.getService("mount");
        if (service != null) {
            return Stub.asInterface(service);
        }
        Log.e(TAG, "Can't get storagemanager service");
        throw new RemoteException("Could not contact storagemanager service");
    }

    public static String createSdDir(long sizeBytes, String cid, String sdEncKey, int uid, boolean isExternal) {
        int sizeMb = ((int) ((1048576 + sizeBytes) / 1048576)) + 1;
        try {
            IStorageManager storageManager = getStorageManager();
            if (storageManager.createSecureContainer(cid, sizeMb, "ext4", sdEncKey, uid, isExternal) == 0) {
                return storageManager.getSecureContainerPath(cid);
            }
            Log.e(TAG, "Failed to create secure container " + cid);
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "StorageManagerService running?");
            return null;
        }
    }

    public static boolean resizeSdDir(long sizeBytes, String cid, String sdEncKey) {
        try {
            if (getStorageManager().resizeSecureContainer(cid, ((int) ((sizeBytes + 1048576) / 1048576)) + 1, sdEncKey) == 0) {
                return true;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "StorageManagerService running?");
        }
        Log.e(TAG, "Failed to create secure container " + cid);
        return false;
    }

    public static String mountSdDir(String cid, String key, int ownerUid) {
        return mountSdDir(cid, key, ownerUid, true);
    }

    public static String mountSdDir(String cid, String key, int ownerUid, boolean readOnly) {
        try {
            int rc = getStorageManager().mountSecureContainer(cid, key, ownerUid, readOnly);
            if (rc == 0) {
                return getStorageManager().getSecureContainerPath(cid);
            }
            Log.i(TAG, "Failed to mount container " + cid + " rc : " + rc);
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "StorageManagerService running?");
            return null;
        }
    }

    public static boolean unMountSdDir(String cid) {
        try {
            int rc = getStorageManager().unmountSecureContainer(cid, true);
            if (rc == 0) {
                return true;
            }
            Log.e(TAG, "Failed to unmount " + cid + " with rc " + rc);
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "StorageManagerService running?");
            return false;
        }
    }

    public static boolean renameSdDir(String oldId, String newId) {
        try {
            int rc = getStorageManager().renameSecureContainer(oldId, newId);
            if (rc == 0) {
                return true;
            }
            Log.e(TAG, "Failed to rename " + oldId + " to " + newId + "with rc " + rc);
            return false;
        } catch (RemoteException e) {
            Log.i(TAG, "Failed ot rename  " + oldId + " to " + newId + " with exception : " + e);
            return false;
        }
    }

    public static String getSdDir(String cid) {
        try {
            return getStorageManager().getSecureContainerPath(cid);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get container path for " + cid + " with exception " + e);
            return null;
        }
    }

    public static String getSdFilesystem(String cid) {
        try {
            return getStorageManager().getSecureContainerFilesystemPath(cid);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get container path for " + cid + " with exception " + e);
            return null;
        }
    }

    public static boolean finalizeSdDir(String cid) {
        try {
            if (getStorageManager().finalizeSecureContainer(cid) == 0) {
                return true;
            }
            Log.i(TAG, "Failed to finalize container " + cid);
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to finalize container " + cid + " with exception " + e);
            return false;
        }
    }

    public static boolean destroySdDir(String cid) {
        try {
            if (getStorageManager().destroySecureContainer(cid, true) == 0) {
                return true;
            }
            Log.i(TAG, "Failed to destroy container " + cid);
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to destroy container " + cid + " with exception " + e);
            return false;
        }
    }

    public static String[] getSecureContainerList() {
        try {
            return getStorageManager().getSecureContainerList();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get secure container list with exception" + e);
            return null;
        }
    }

    public static boolean isContainerMounted(String cid) {
        try {
            return getStorageManager().isSecureContainerMounted(cid);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to find out if container " + cid + " mounted");
            return false;
        }
    }

    public static long extractPublicFiles(File apkFile, File publicZipFile) throws IOException {
        FileOutputStream fstr;
        AutoCloseable publicZipOutStream;
        if (publicZipFile == null) {
            fstr = null;
            publicZipOutStream = null;
        } else {
            fstr = new FileOutputStream(publicZipFile);
            publicZipOutStream = new ZipOutputStream(fstr);
            Log.d(TAG, "Extracting " + apkFile + " to " + publicZipFile);
        }
        long size = 0;
        ZipFile privateZip;
        try {
            privateZip = new ZipFile(apkFile.getAbsolutePath());
            for (ZipEntry zipEntry : Collections.list(privateZip.entries())) {
                String zipEntryName = zipEntry.getName();
                if ("AndroidManifest.xml".equals(zipEntryName) || "resources.arsc".equals(zipEntryName) || zipEntryName.startsWith("res/")) {
                    size += zipEntry.getSize();
                    if (publicZipFile != null) {
                        copyZipEntry(zipEntry, privateZip, publicZipOutStream);
                    }
                }
            }
            try {
                privateZip.close();
            } catch (IOException e) {
            }
            if (publicZipFile != null) {
                publicZipOutStream.finish();
                publicZipOutStream.flush();
                FileUtils.sync(fstr);
                publicZipOutStream.close();
                FileUtils.setPermissions(publicZipFile.getAbsolutePath(), DisplayMetrics.DENSITY_420, -1, -1);
            }
            IoUtils.closeQuietly(publicZipOutStream);
            return size;
        } catch (Throwable th) {
            IoUtils.closeQuietly(publicZipOutStream);
        }
    }

    private static void copyZipEntry(ZipEntry zipEntry, ZipFile inZipFile, ZipOutputStream outZipStream) throws IOException {
        ZipEntry newEntry;
        byte[] buffer = new byte[4096];
        if (zipEntry.getMethod() == 0) {
            newEntry = new ZipEntry(zipEntry);
        } else {
            newEntry = new ZipEntry(zipEntry.getName());
        }
        outZipStream.putNextEntry(newEntry);
        InputStream data = inZipFile.getInputStream(zipEntry);
        while (true) {
            try {
                int num = data.read(buffer);
                if (num <= 0) {
                    break;
                }
                outZipStream.write(buffer, 0, num);
            } finally {
                IoUtils.closeQuietly(data);
            }
        }
        outZipStream.flush();
    }

    public static boolean fixSdPermissions(String cid, int gid, String filename) {
        try {
            if (getStorageManager().fixPermissionsSecureContainer(cid, gid, filename) == 0) {
                return true;
            }
            Log.i(TAG, "Failed to fixperms container " + cid);
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to fixperms container " + cid + " with exception " + e);
            return false;
        }
    }

    private static synchronized TestableInterface getDefaultTestableInterface() {
        TestableInterface testableInterface;
        synchronized (PackageHelper.class) {
            if (sDefaultTestableInterface == null) {
                sDefaultTestableInterface = new TestableInterface() {
                    public StorageManager getStorageManager(Context context) {
                        return (StorageManager) context.getSystemService(StorageManager.class);
                    }

                    public boolean getForceAllowOnExternalSetting(Context context) {
                        return Global.getInt(context.getContentResolver(), Global.FORCE_ALLOW_ON_EXTERNAL, 0) != 0;
                    }

                    public boolean getAllow3rdPartyOnInternalConfig(Context context) {
                        return context.getResources().getBoolean(R.bool.config_allow3rdPartyAppOnInternal);
                    }

                    public ApplicationInfo getExistingAppInfo(Context context, String packageName) {
                        ApplicationInfo existingInfo = null;
                        try {
                            return context.getPackageManager().getApplicationInfo(packageName, 4194304);
                        } catch (NameNotFoundException e) {
                            Log.e(PackageHelper.TAG, "ApplicationInfo is not found in function getExistingAppInfo");
                            return existingInfo;
                        }
                    }

                    public File getDataDirectory() {
                        return Environment.getDataDirectory();
                    }
                };
            }
            testableInterface = sDefaultTestableInterface;
        }
        return testableInterface;
    }

    @Deprecated
    public static String resolveInstallVolume(Context context, String packageName, int installLocation, long sizeBytes, TestableInterface testInterface) throws IOException {
        SessionParams params = new SessionParams(-1);
        params.appPackageName = packageName;
        params.installLocation = installLocation;
        params.sizeBytes = sizeBytes;
        return resolveInstallVolume(context, params, testInterface);
    }

    public static String resolveInstallVolume(Context context, SessionParams params) throws IOException {
        return resolveInstallVolume(context, params.appPackageName, params.installLocation, params.sizeBytes, getDefaultTestableInterface());
    }

    public static String resolveInstallVolume(Context context, SessionParams params, TestableInterface testInterface) throws IOException {
        boolean forceAllowOnExternal = testInterface.getForceAllowOnExternalSetting(context);
        boolean allow3rdPartyOnInternal = testInterface.getAllow3rdPartyOnInternalConfig(context);
        ApplicationInfo existingInfo = testInterface.getExistingAppInfo(context, params.appPackageName);
        boolean fitsOnInternal = testInterface.fitsOnInternalStorage(context, params);
        StorageManager storageManager = testInterface.getStorageManager(context);
        if (existingInfo == null || !existingInfo.isSystemApp()) {
            ArraySet<String> allCandidates = new ArraySet();
            VolumeInfo bestCandidate = null;
            long bestCandidateAvailBytes = Long.MIN_VALUE;
            for (VolumeInfo vol : storageManager.getVolumes()) {
                boolean isInternalStorage = "private".equals(vol.id);
                if (vol.type == 1 && vol.isMountedWritable()) {
                    if (!isInternalStorage || allow3rdPartyOnInternal) {
                        long availBytes = storageManager.getAllocatableBytes(new File(vol.path), translateAllocateFlags(params.installFlags));
                        if (availBytes >= params.sizeBytes) {
                            allCandidates.add(vol.fsUuid);
                        }
                        if (availBytes >= bestCandidateAvailBytes) {
                            bestCandidate = vol;
                            bestCandidateAvailBytes = availBytes;
                        }
                    }
                }
            }
            if (forceAllowOnExternal || params.installLocation != 1) {
                if (existingInfo != null) {
                    if (Objects.equals(existingInfo.volumeUuid, StorageManager.UUID_PRIVATE_INTERNAL) && fitsOnInternal) {
                        return StorageManager.UUID_PRIVATE_INTERNAL;
                    }
                    if (allCandidates.contains(existingInfo.volumeUuid)) {
                        return existingInfo.volumeUuid;
                    }
                    throw new IOException("Not enough space on existing volume " + existingInfo.volumeUuid + " for " + params.appPackageName + " upgrade");
                } else if (bestCandidate != null) {
                    return bestCandidate.fsUuid;
                } else {
                    throw new IOException("No special requests, but no room on allowed volumes.  allow3rdPartyOnInternal? " + allow3rdPartyOnInternal);
                }
            } else if (existingInfo != null && (Objects.equals(existingInfo.volumeUuid, StorageManager.UUID_PRIVATE_INTERNAL) ^ 1) != 0) {
                throw new IOException("Cannot automatically move " + params.appPackageName + " from " + existingInfo.volumeUuid + " to internal storage");
            } else if (!allow3rdPartyOnInternal) {
                throw new IOException("Not allowed to install non-system apps on internal storage");
            } else if (fitsOnInternal) {
                return StorageManager.UUID_PRIVATE_INTERNAL;
            } else {
                throw new IOException("Requested internal only, but not enough space");
            }
        } else if (fitsOnInternal) {
            return StorageManager.UUID_PRIVATE_INTERNAL;
        } else {
            throw new IOException("Not enough space on existing volume " + existingInfo.volumeUuid + " for system app " + params.appPackageName + " upgrade");
        }
    }

    public static boolean fitsOnInternal(Context context, SessionParams params) throws IOException {
        return params.sizeBytes <= ((StorageManager) context.getSystemService(StorageManager.class)).getAllocatableBytes(Environment.getDataDirectory(), translateAllocateFlags(params.installFlags));
    }

    public static boolean fitsOnExternal(Context context, SessionParams params) {
        StorageManager storage = (StorageManager) context.getSystemService(StorageManager.class);
        StorageVolume primary = storage.getPrimaryVolume();
        if (params.sizeBytes <= 0 || (primary.isEmulated() ^ 1) == 0 || !"mounted".equals(primary.getState()) || params.sizeBytes > storage.getStorageBytesUntilLow(primary.getPathFile())) {
            return false;
        }
        return true;
    }

    @Deprecated
    public static int resolveInstallLocation(Context context, String packageName, int installLocation, long sizeBytes, int installFlags) {
        SessionParams params = new SessionParams(-1);
        params.appPackageName = packageName;
        params.installLocation = installLocation;
        params.sizeBytes = sizeBytes;
        params.installFlags = installFlags;
        try {
            return resolveInstallLocation(context, params);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static int resolveInstallLocation(Context context, SessionParams params) throws IOException {
        int prefer;
        boolean checkBoth;
        int i = 1;
        ApplicationInfo existingInfo = null;
        try {
            existingInfo = context.getPackageManager().getApplicationInfo(params.appPackageName, 4194304);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "ApplicationInfo is not found in function resolveInstallLocation");
        }
        boolean ephemeral = false;
        if ((params.installFlags & 2048) != 0) {
            prefer = 1;
            ephemeral = true;
            checkBoth = false;
        } else if ((params.installFlags & 16) != 0) {
            prefer = 1;
            checkBoth = false;
        } else if ((params.installFlags & 8) != 0) {
            prefer = 2;
            checkBoth = false;
        } else if (params.installLocation == 1) {
            prefer = 1;
            checkBoth = false;
        } else if (params.installLocation == 2) {
            prefer = 2;
            checkBoth = true;
        } else if (params.installLocation == 0) {
            if (existingInfo == null) {
                prefer = 1;
            } else if ((existingInfo.flags & 262144) != 0) {
                prefer = 2;
            } else {
                prefer = 1;
            }
            checkBoth = true;
        } else {
            prefer = 1;
            checkBoth = false;
        }
        boolean fitsOnInternal = false;
        if (checkBoth || prefer == 1) {
            fitsOnInternal = fitsOnInternal(context, params);
        }
        boolean fitsOnExternal = false;
        if (checkBoth || prefer == 2) {
            fitsOnExternal = fitsOnExternal(context, params);
        }
        if (prefer == 1) {
            if (fitsOnInternal) {
                if (ephemeral) {
                    i = 3;
                }
                return i;
            }
        } else if (prefer == 2 && fitsOnExternal) {
            return 2;
        }
        if (checkBoth) {
            if (fitsOnInternal) {
                return 1;
            }
            if (fitsOnExternal) {
                return 2;
            }
        }
        return -1;
    }

    public static long calculateInstalledSize(PackageLite pkg, boolean isForwardLocked, String abiOverride) throws IOException {
        AutoCloseable autoCloseable = null;
        try {
            autoCloseable = Handle.create(pkg);
            long calculateInstalledSize = calculateInstalledSize(pkg, autoCloseable, isForwardLocked, abiOverride);
            return calculateInstalledSize;
        } finally {
            IoUtils.closeQuietly(autoCloseable);
        }
    }

    public static long calculateInstalledSize(PackageLite pkg, Handle handle, boolean isForwardLocked, String abiOverride) throws IOException {
        long sizeBytes = 0;
        for (String codePath : pkg.getAllCodePaths()) {
            File codeFile = new File(codePath);
            sizeBytes += codeFile.length();
            if (isForwardLocked) {
                sizeBytes += extractPublicFiles(codeFile, null);
            }
        }
        return sizeBytes + NativeLibraryHelper.sumNativeBinariesWithOverride(handle, abiOverride);
    }

    public static String replaceEnd(String str, String before, String after) {
        if (str.endsWith(before)) {
            return str.substring(0, str.length() - before.length()) + after;
        }
        throw new IllegalArgumentException("Expected " + str + " to end with " + before);
    }

    public static int translateAllocateFlags(int installFlags) {
        if ((32768 & installFlags) != 0) {
            return 1;
        }
        return 0;
    }
}
