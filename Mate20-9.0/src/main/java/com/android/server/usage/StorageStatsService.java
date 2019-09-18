package com.android.server.usage;

import android.app.AppOpsManager;
import android.app.usage.ExternalStorageStats;
import android.app.usage.IStorageStatsManager;
import android.app.usage.StorageStats;
import android.app.usage.UsageStatsManagerInternal;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.UserInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelableException;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.DataUnit;
import android.util.Slog;
import android.util.SparseLongArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.Preconditions;
import com.android.server.IoThread;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.media.projection.MediaProjectionManagerService;
import com.android.server.pm.Installer;
import com.android.server.pm.PackageManagerService;
import com.android.server.storage.CacheQuotaStrategy;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class StorageStatsService extends IStorageStatsManager.Stub {
    private static final long DEFAULT_QUOTA = DataUnit.MEBIBYTES.toBytes(64);
    private static final long DELAY_IN_MILLIS = 30000;
    private static final String PROP_DISABLE_QUOTA = "fw.disable_quota";
    private static final String PROP_VERIFY_STORAGE = "fw.verify_storage";
    private static final String TAG = "StorageStatsService";
    private final AppOpsManager mAppOps;
    /* access modifiers changed from: private */
    public final ArrayMap<String, SparseLongArray> mCacheQuotas = new ArrayMap<>();
    /* access modifiers changed from: private */
    public final Context mContext;
    private final H mHandler;
    /* access modifiers changed from: private */
    public final Installer mInstaller;
    private final PackageManager mPackage;
    private final StorageManager mStorage;
    private final UserManager mUser;

    private class H extends Handler {
        private static final boolean DEBUG = false;
        private static final double MINIMUM_CHANGE_DELTA = 0.05d;
        private static final int MSG_CHECK_STORAGE_DELTA = 100;
        private static final int MSG_LOAD_CACHED_QUOTAS_FROM_FILE = 101;
        private static final int UNSET = -1;
        private double mMinimumThresholdBytes = (((double) this.mStats.getTotalBytes()) * MINIMUM_CHANGE_DELTA);
        private long mPreviousBytes = this.mStats.getAvailableBytes();
        private final StatFs mStats = new StatFs(Environment.getDataDirectory().getAbsolutePath());

        public H(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (StorageStatsService.isCacheQuotaCalculationsEnabled(StorageStatsService.this.mContext.getContentResolver())) {
                switch (msg.what) {
                    case 100:
                        if (((double) Math.abs(this.mPreviousBytes - this.mStats.getAvailableBytes())) > this.mMinimumThresholdBytes && MediaProjectionManagerService.sHasStartedInSystemserver) {
                            this.mPreviousBytes = this.mStats.getAvailableBytes();
                            recalculateQuotas(getInitializedStrategy());
                            StorageStatsService.this.notifySignificantDelta();
                        }
                        sendEmptyMessageDelayed(100, 30000);
                        break;
                    case 101:
                        CacheQuotaStrategy strategy = getInitializedStrategy();
                        this.mPreviousBytes = -1;
                        try {
                            this.mPreviousBytes = strategy.setupQuotasFromFile();
                        } catch (IOException e) {
                            Slog.e(StorageStatsService.TAG, "An error occurred while reading the cache quota file.", e);
                        } catch (IllegalStateException e2) {
                            Slog.e(StorageStatsService.TAG, "Cache quota XML file is malformed?", e2);
                        }
                        if (this.mPreviousBytes < 0) {
                            this.mPreviousBytes = this.mStats.getAvailableBytes();
                            recalculateQuotas(strategy);
                        }
                        sendEmptyMessageDelayed(100, 30000);
                        break;
                    default:
                        return;
                }
            }
        }

        private void recalculateQuotas(CacheQuotaStrategy strategy) {
            strategy.recalculateQuotas();
        }

        private CacheQuotaStrategy getInitializedStrategy() {
            return new CacheQuotaStrategy(StorageStatsService.this.mContext, (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class), StorageStatsService.this.mInstaller, StorageStatsService.this.mCacheQuotas);
        }
    }

    public static class Lifecycle extends SystemService {
        private StorageStatsService mService;

        public Lifecycle(Context context) {
            super(context);
        }

        /* JADX WARNING: type inference failed for: r1v1, types: [android.os.IBinder, com.android.server.usage.StorageStatsService] */
        public void onStart() {
            this.mService = new StorageStatsService(getContext());
            publishBinderService("storagestats", this.mService);
        }
    }

    public StorageStatsService(Context context) {
        this.mContext = (Context) Preconditions.checkNotNull(context);
        this.mAppOps = (AppOpsManager) Preconditions.checkNotNull((AppOpsManager) context.getSystemService(AppOpsManager.class));
        this.mUser = (UserManager) Preconditions.checkNotNull((UserManager) context.getSystemService(UserManager.class));
        this.mPackage = (PackageManager) Preconditions.checkNotNull(context.getPackageManager());
        this.mStorage = (StorageManager) Preconditions.checkNotNull((StorageManager) context.getSystemService(StorageManager.class));
        this.mInstaller = new Installer(context);
        this.mInstaller.onStart();
        invalidateMounts();
        this.mHandler = new H(IoThread.get().getLooper());
        this.mHandler.sendEmptyMessage(101);
        this.mStorage.registerListener(new StorageEventListener() {
            public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
                switch (vol.type) {
                    case 1:
                    case 2:
                        if (newState == 2) {
                            StorageStatsService.this.invalidateMounts();
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void invalidateMounts() {
        try {
            this.mInstaller.invalidateMounts();
        } catch (Installer.InstallerException e) {
            Slog.wtf(TAG, "Failed to invalidate mounts", e);
        }
    }

    private void enforcePermission(int callingUid, String callingPackage) {
        int mode = this.mAppOps.noteOp(43, callingUid, callingPackage);
        if (mode == 0) {
            return;
        }
        if (mode == 3) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.PACKAGE_USAGE_STATS", TAG);
            return;
        }
        throw new SecurityException("Package " + callingPackage + " from UID " + callingUid + " blocked by mode " + mode);
    }

    public boolean isQuotaSupported(String volumeUuid, String callingPackage) {
        enforcePermission(Binder.getCallingUid(), callingPackage);
        try {
            return this.mInstaller.isQuotaSupported(volumeUuid);
        } catch (Installer.InstallerException e) {
            throw new ParcelableException(new IOException(e.getMessage()));
        }
    }

    public boolean isReservedSupported(String volumeUuid, String callingPackage) {
        enforcePermission(Binder.getCallingUid(), callingPackage);
        if (volumeUuid == StorageManager.UUID_PRIVATE_INTERNAL) {
            return SystemProperties.getBoolean("vold.has_reserved", false);
        }
        return false;
    }

    public long getTotalBytes(String volumeUuid, String callingPackage) {
        if (volumeUuid == StorageManager.UUID_PRIVATE_INTERNAL) {
            return FileUtils.roundStorageSize(this.mStorage.getPrimaryStorageSize());
        }
        VolumeInfo vol = this.mStorage.findVolumeByUuid(volumeUuid);
        if (vol != null) {
            return FileUtils.roundStorageSize(vol.disk.size);
        }
        throw new ParcelableException(new IOException("Failed to find storage device for UUID " + volumeUuid));
    }

    public long getFreeBytes(String volumeUuid, String callingPackage) {
        long token = Binder.clearCallingIdentity();
        try {
            File path = this.mStorage.findPathForUuid(volumeUuid);
            if (isQuotaSupported(volumeUuid, PackageManagerService.PLATFORM_PACKAGE_NAME)) {
                long usableSpace = path.getUsableSpace() + Math.max(0, getCacheBytes(volumeUuid, PackageManagerService.PLATFORM_PACKAGE_NAME) - this.mStorage.getStorageCacheBytes(path, 0));
                Binder.restoreCallingIdentity(token);
                return usableSpace;
            }
            long cacheTotal = path.getUsableSpace();
            Binder.restoreCallingIdentity(token);
            return cacheTotal;
        } catch (FileNotFoundException e) {
            throw new ParcelableException(e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    public long getCacheBytes(String volumeUuid, String callingPackage) {
        enforcePermission(Binder.getCallingUid(), callingPackage);
        long cacheBytes = 0;
        for (UserInfo user : this.mUser.getUsers()) {
            cacheBytes += queryStatsForUser(volumeUuid, user.id, null).cacheBytes;
        }
        return cacheBytes;
    }

    public long getCacheQuotaBytes(String volumeUuid, int uid, String callingPackage) {
        enforcePermission(Binder.getCallingUid(), callingPackage);
        if (this.mCacheQuotas.containsKey(volumeUuid)) {
            return this.mCacheQuotas.get(volumeUuid).get(uid, DEFAULT_QUOTA);
        }
        return DEFAULT_QUOTA;
    }

    /* JADX WARNING: type inference failed for: r3v12, types: [java.lang.Object[]] */
    /* JADX WARNING: Multi-variable type inference failed */
    public StorageStats queryStatsForPackage(String volumeUuid, String packageName, int userId, String callingPackage) {
        String str = packageName;
        int i = userId;
        String str2 = callingPackage;
        if (i != UserHandle.getCallingUserId()) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS", TAG);
        }
        try {
            ApplicationInfo appInfo = this.mPackage.getApplicationInfoAsUser(str, 8192, i);
            if (Binder.getCallingUid() != appInfo.uid) {
                enforcePermission(Binder.getCallingUid(), str2);
            }
            if (ArrayUtils.defeatNullable(this.mPackage.getPackagesForUid(appInfo.uid)).length == 1) {
                return queryStatsForUid(volumeUuid, appInfo.uid, str2);
            }
            String str3 = volumeUuid;
            int appId = UserHandle.getUserId(appInfo.uid);
            String[] packageNames = {str};
            long[] ceDataInodes = new long[1];
            String[] codePaths = new String[0];
            if (!appInfo.isSystemApp() || appInfo.isUpdatedSystemApp()) {
                codePaths = ArrayUtils.appendElement(String.class, codePaths, appInfo.getCodePath());
            }
            String[] codePaths2 = codePaths;
            PackageStats stats = new PackageStats(TAG);
            try {
                PackageStats stats2 = stats;
                long[] jArr = ceDataInodes;
                try {
                    this.mInstaller.getAppSize(str3, packageNames, i, 0, appId, ceDataInodes, codePaths2, stats2);
                    return translate(stats2);
                } catch (Installer.InstallerException e) {
                    e = e;
                    PackageStats packageStats = stats2;
                    throw new ParcelableException(new IOException(e.getMessage()));
                }
            } catch (Installer.InstallerException e2) {
                e = e2;
                PackageStats packageStats2 = stats;
                long[] jArr2 = ceDataInodes;
                throw new ParcelableException(new IOException(e.getMessage()));
            }
        } catch (PackageManager.NameNotFoundException e3) {
            String str4 = volumeUuid;
            throw new ParcelableException(e3);
        }
    }

    public StorageStats queryStatsForUid(String volumeUuid, int uid, String callingPackage) {
        PackageStats stats;
        int i = uid;
        int userId = UserHandle.getUserId(uid);
        int appId = UserHandle.getAppId(uid);
        if (userId != UserHandle.getCallingUserId()) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS", TAG);
        }
        if (Binder.getCallingUid() == i) {
            String str = callingPackage;
        } else {
            enforcePermission(Binder.getCallingUid(), callingPackage);
        }
        String[] packageNames = ArrayUtils.defeatNullable(this.mPackage.getPackagesForUid(i));
        long[] ceDataInodes = new long[packageNames.length];
        String[] codePaths = new String[0];
        int i2 = 0;
        while (i2 < packageNames.length) {
            try {
                ApplicationInfo appInfo = this.mPackage.getApplicationInfoAsUser(packageNames[i2], 8192, userId);
                if (!appInfo.isSystemApp() || appInfo.isUpdatedSystemApp()) {
                    codePaths = (String[]) ArrayUtils.appendElement(String.class, codePaths, appInfo.getCodePath());
                }
                i2++;
            } catch (PackageManager.NameNotFoundException e) {
                throw new ParcelableException(e);
            }
        }
        PackageStats stats2 = new PackageStats(TAG);
        try {
            PackageStats stats3 = stats2;
            String[] codePaths2 = codePaths;
            long[] ceDataInodes2 = ceDataInodes;
            try {
                this.mInstaller.getAppSize(volumeUuid, packageNames, userId, getDefaultFlags(), appId, ceDataInodes, codePaths, stats3);
                if (SystemProperties.getBoolean(PROP_VERIFY_STORAGE, false)) {
                    PackageStats manualStats = new PackageStats(TAG);
                    this.mInstaller.getAppSize(volumeUuid, packageNames, userId, 0, appId, ceDataInodes2, codePaths2, manualStats);
                    stats = stats3;
                    try {
                        checkEquals("UID " + i, manualStats, stats);
                    } catch (Installer.InstallerException e2) {
                        e = e2;
                    }
                } else {
                    stats = stats3;
                }
                return translate(stats);
            } catch (Installer.InstallerException e3) {
                e = e3;
                PackageStats packageStats = stats3;
                throw new ParcelableException(new IOException(e.getMessage()));
            }
        } catch (Installer.InstallerException e4) {
            e = e4;
            PackageStats packageStats2 = stats2;
            String[] strArr = codePaths;
            long[] jArr = ceDataInodes;
            throw new ParcelableException(new IOException(e.getMessage()));
        }
    }

    public StorageStats queryStatsForUser(String volumeUuid, int userId, String callingPackage) {
        if (userId != UserHandle.getCallingUserId()) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS", TAG);
        }
        enforcePermission(Binder.getCallingUid(), callingPackage);
        int[] appIds = getAppIds(userId);
        PackageStats stats = new PackageStats(TAG);
        try {
            this.mInstaller.getUserSize(volumeUuid, userId, getDefaultFlags(), appIds, stats);
            if (SystemProperties.getBoolean(PROP_VERIFY_STORAGE, false)) {
                PackageStats manualStats = new PackageStats(TAG);
                this.mInstaller.getUserSize(volumeUuid, userId, 0, appIds, manualStats);
                checkEquals("User " + userId, manualStats, stats);
            }
            return translate(stats);
        } catch (Installer.InstallerException e) {
            throw new ParcelableException(new IOException(e.getMessage()));
        }
    }

    public ExternalStorageStats queryExternalStatsForUser(String volumeUuid, int userId, String callingPackage) {
        if (userId != UserHandle.getCallingUserId()) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS", TAG);
        }
        enforcePermission(Binder.getCallingUid(), callingPackage);
        int[] appIds = getAppIds(userId);
        try {
            long[] stats = this.mInstaller.getExternalSize(volumeUuid, userId, getDefaultFlags(), appIds);
            if (SystemProperties.getBoolean(PROP_VERIFY_STORAGE, false)) {
                long[] manualStats = this.mInstaller.getExternalSize(volumeUuid, userId, 0, appIds);
                checkEquals("External " + userId, manualStats, stats);
            }
            ExternalStorageStats res = new ExternalStorageStats();
            res.totalBytes = stats[0];
            res.audioBytes = stats[1];
            res.videoBytes = stats[2];
            res.imageBytes = stats[3];
            res.appBytes = stats[4];
            res.obbBytes = stats[5];
            return res;
        } catch (Installer.InstallerException e) {
            throw new ParcelableException(new IOException(e.getMessage()));
        }
    }

    private int[] getAppIds(int userId) {
        int[] appIds = null;
        for (ApplicationInfo app : this.mPackage.getInstalledApplicationsAsUser(8192, userId)) {
            int appId = UserHandle.getAppId(app.uid);
            if (!ArrayUtils.contains(appIds, appId)) {
                appIds = ArrayUtils.appendInt(appIds, appId);
            }
        }
        return appIds;
    }

    private static int getDefaultFlags() {
        if (SystemProperties.getBoolean(PROP_DISABLE_QUOTA, false)) {
            return 0;
        }
        return 4096;
    }

    private static void checkEquals(String msg, long[] a, long[] b) {
        for (int i = 0; i < a.length; i++) {
            checkEquals(msg + "[" + i + "]", a[i], b[i]);
        }
    }

    private static void checkEquals(String msg, PackageStats a, PackageStats b) {
        checkEquals(msg + " codeSize", a.codeSize, b.codeSize);
        checkEquals(msg + " dataSize", a.dataSize, b.dataSize);
        checkEquals(msg + " cacheSize", a.cacheSize, b.cacheSize);
        checkEquals(msg + " externalCodeSize", a.externalCodeSize, b.externalCodeSize);
        checkEquals(msg + " externalDataSize", a.externalDataSize, b.externalDataSize);
        checkEquals(msg + " externalCacheSize", a.externalCacheSize, b.externalCacheSize);
    }

    private static void checkEquals(String msg, long expected, long actual) {
        if (expected != actual) {
            Slog.e(TAG, msg + " expected " + expected + " actual " + actual);
        }
    }

    private static StorageStats translate(PackageStats stats) {
        StorageStats res = new StorageStats();
        res.codeBytes = stats.codeSize + stats.externalCodeSize;
        res.dataBytes = stats.dataSize + stats.externalDataSize;
        res.cacheBytes = stats.cacheSize + stats.externalCacheSize;
        return res;
    }

    @VisibleForTesting
    static boolean isCacheQuotaCalculationsEnabled(ContentResolver resolver) {
        return Settings.Global.getInt(resolver, "enable_cache_quota_calculation", 1) != 0;
    }

    /* access modifiers changed from: package-private */
    public void notifySignificantDelta() {
        this.mContext.getContentResolver().notifyChange(Uri.parse("content://com.android.externalstorage.documents/"), null, false);
    }
}
