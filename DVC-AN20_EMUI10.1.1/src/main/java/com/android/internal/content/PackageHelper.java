package com.android.internal.content;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.dex.DexMetadataHelper;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.storage.IStorageManager;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.provider.Settings;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.content.NativeLibraryHelper;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Objects;
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
    public static final int RECOMMEND_FAILED_WRONG_INSTALLED_VERSION = -8;
    public static final int RECOMMEND_INSTALL_EPHEMERAL = 3;
    public static final int RECOMMEND_INSTALL_EXTERNAL = 2;
    public static final int RECOMMEND_INSTALL_INTERNAL = 1;
    public static final int RECOMMEND_MEDIA_UNAVAILABLE = -5;
    private static final String TAG = "PackageHelper";
    private static TestableInterface sDefaultTestableInterface = null;

    public static abstract class TestableInterface {
        public abstract boolean getAllow3rdPartyOnInternalConfig(Context context);

        public abstract File getDataDirectory();

        public abstract ApplicationInfo getExistingAppInfo(Context context, String str);

        public abstract boolean getForceAllowOnExternalSetting(Context context);

        public abstract StorageManager getStorageManager(Context context);
    }

    public static IStorageManager getStorageManager() throws RemoteException {
        IBinder service = ServiceManager.getService("mount");
        if (service != null) {
            return IStorageManager.Stub.asInterface(service);
        }
        Log.e(TAG, "Can't get storagemanager service");
        throw new RemoteException("Could not contact storagemanager service");
    }

    private static synchronized TestableInterface getDefaultTestableInterface() {
        TestableInterface testableInterface;
        synchronized (PackageHelper.class) {
            if (sDefaultTestableInterface == null) {
                sDefaultTestableInterface = new TestableInterface() {
                    /* class com.android.internal.content.PackageHelper.AnonymousClass1 */

                    @Override // com.android.internal.content.PackageHelper.TestableInterface
                    public StorageManager getStorageManager(Context context) {
                        return (StorageManager) context.getSystemService(StorageManager.class);
                    }

                    @Override // com.android.internal.content.PackageHelper.TestableInterface
                    public boolean getForceAllowOnExternalSetting(Context context) {
                        return Settings.Global.getInt(context.getContentResolver(), Settings.Global.FORCE_ALLOW_ON_EXTERNAL, 0) != 0;
                    }

                    @Override // com.android.internal.content.PackageHelper.TestableInterface
                    public boolean getAllow3rdPartyOnInternalConfig(Context context) {
                        return context.getResources().getBoolean(R.bool.config_allow3rdPartyAppOnInternal);
                    }

                    @Override // com.android.internal.content.PackageHelper.TestableInterface
                    public ApplicationInfo getExistingAppInfo(Context context, String packageName) {
                        try {
                            return context.getPackageManager().getApplicationInfo(packageName, 4194304);
                        } catch (PackageManager.NameNotFoundException e) {
                            return null;
                        }
                    }

                    @Override // com.android.internal.content.PackageHelper.TestableInterface
                    public File getDataDirectory() {
                        return Environment.getDataDirectory();
                    }
                };
            }
            testableInterface = sDefaultTestableInterface;
        }
        return testableInterface;
    }

    @VisibleForTesting
    @Deprecated
    public static String resolveInstallVolume(Context context, String packageName, int installLocation, long sizeBytes, TestableInterface testInterface) throws IOException {
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(-1);
        params.appPackageName = packageName;
        params.installLocation = installLocation;
        params.sizeBytes = sizeBytes;
        return resolveInstallVolume(context, params, testInterface);
    }

    public static String resolveInstallVolume(Context context, PackageInstaller.SessionParams params) throws IOException {
        return resolveInstallVolume(context, params.appPackageName, params.installLocation, params.sizeBytes, getDefaultTestableInterface());
    }

    @VisibleForTesting
    public static String resolveInstallVolume(Context context, PackageInstaller.SessionParams params, TestableInterface testInterface) throws IOException {
        StorageManager storageManager;
        StorageManager storageManager2 = testInterface.getStorageManager(context);
        boolean forceAllowOnExternal = testInterface.getForceAllowOnExternalSetting(context);
        boolean allow3rdPartyOnInternal = testInterface.getAllow3rdPartyOnInternalConfig(context);
        ApplicationInfo existingInfo = testInterface.getExistingAppInfo(context, params.appPackageName);
        ArraySet<String> allCandidates = new ArraySet<>();
        boolean fitsOnInternal = false;
        VolumeInfo bestCandidate = null;
        long bestCandidateAvailBytes = Long.MIN_VALUE;
        for (VolumeInfo vol : storageManager2.getVolumes()) {
            if (vol.type != 1 || !vol.isMountedWritable()) {
                storageManager = storageManager2;
            } else {
                boolean isInternalStorage = VolumeInfo.ID_PRIVATE_INTERNAL.equals(vol.id);
                long availBytes = storageManager2.getAllocatableBytes(storageManager2.getUuidForPath(new File(vol.path)), translateAllocateFlags(params.installFlags));
                if (isInternalStorage) {
                    storageManager = storageManager2;
                    fitsOnInternal = params.sizeBytes <= availBytes;
                } else {
                    storageManager = storageManager2;
                }
                if (!isInternalStorage || allow3rdPartyOnInternal) {
                    if (availBytes >= params.sizeBytes) {
                        allCandidates.add(vol.fsUuid);
                    }
                    if (availBytes >= bestCandidateAvailBytes) {
                        bestCandidateAvailBytes = availBytes;
                        bestCandidate = vol;
                    }
                }
            }
            storageManager2 = storageManager;
        }
        if (existingInfo == null || !existingInfo.isSystemApp()) {
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
            } else if (existingInfo != null && !Objects.equals(existingInfo.volumeUuid, StorageManager.UUID_PRIVATE_INTERNAL)) {
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

    public static boolean fitsOnInternal(Context context, PackageInstaller.SessionParams params) throws IOException {
        StorageManager storage = (StorageManager) context.getSystemService(StorageManager.class);
        return params.sizeBytes <= storage.getAllocatableBytes(storage.getUuidForPath(Environment.getDataDirectory()), translateAllocateFlags(params.installFlags));
    }

    public static boolean fitsOnExternal(Context context, PackageInstaller.SessionParams params) {
        StorageManager storage = (StorageManager) context.getSystemService(StorageManager.class);
        StorageVolume primary = storage.getPrimaryVolume();
        return params.sizeBytes > 0 && !primary.isEmulated() && Environment.MEDIA_MOUNTED.equals(primary.getState()) && params.sizeBytes <= storage.getStorageBytesUntilLow(primary.getPathFile());
    }

    @Deprecated
    public static int resolveInstallLocation(Context context, String packageName, int installLocation, long sizeBytes, int installFlags) {
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(-1);
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

    public static int resolveInstallLocation(Context context, PackageInstaller.SessionParams params) throws IOException {
        boolean checkBoth;
        int prefer;
        ApplicationInfo existingInfo = null;
        try {
            existingInfo = context.getPackageManager().getApplicationInfo(params.appPackageName, 4194304);
        } catch (PackageManager.NameNotFoundException e) {
        }
        boolean ephemeral = false;
        if ((params.installFlags & 2048) != 0) {
            prefer = 1;
            ephemeral = true;
            checkBoth = false;
        } else if ((params.installFlags & 16) != 0) {
            prefer = 1;
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
                    return 3;
                }
                return 1;
            }
        } else if (prefer == 2 && fitsOnExternal) {
            return 2;
        }
        if (!checkBoth) {
            return -1;
        }
        if (fitsOnInternal) {
            return 1;
        }
        if (fitsOnExternal) {
            return 2;
        }
        return -1;
    }

    @Deprecated
    public static long calculateInstalledSize(PackageParser.PackageLite pkg, boolean isForwardLocked, String abiOverride) throws IOException {
        return calculateInstalledSize(pkg, abiOverride);
    }

    public static long calculateInstalledSize(PackageParser.PackageLite pkg, String abiOverride) throws IOException {
        return calculateInstalledSize(pkg, abiOverride, (FileDescriptor) null);
    }

    public static long calculateInstalledSize(PackageParser.PackageLite pkg, String abiOverride, FileDescriptor fd) throws IOException {
        NativeLibraryHelper.Handle handle;
        if (fd != null) {
            try {
                handle = NativeLibraryHelper.Handle.createFd(pkg, fd);
            } catch (Throwable th) {
                IoUtils.closeQuietly((AutoCloseable) null);
                throw th;
            }
        } else {
            handle = NativeLibraryHelper.Handle.create(pkg);
        }
        long calculateInstalledSize = calculateInstalledSize(pkg, handle, abiOverride);
        IoUtils.closeQuietly(handle);
        return calculateInstalledSize;
    }

    @Deprecated
    public static long calculateInstalledSize(PackageParser.PackageLite pkg, boolean isForwardLocked, NativeLibraryHelper.Handle handle, String abiOverride) throws IOException {
        return calculateInstalledSize(pkg, handle, abiOverride);
    }

    public static long calculateInstalledSize(PackageParser.PackageLite pkg, NativeLibraryHelper.Handle handle, String abiOverride) throws IOException {
        long sizeBytes = 0;
        for (String codePath : pkg.getAllCodePaths()) {
            sizeBytes += new File(codePath).length();
        }
        return sizeBytes + DexMetadataHelper.getPackageDexMetadataSize(pkg) + NativeLibraryHelper.sumNativeBinariesWithOverride(handle, abiOverride);
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
