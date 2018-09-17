package com.android.server.usage;

import android.app.AppOpsManager;
import android.app.usage.ExternalStorageStats;
import android.app.usage.IStorageStatsManager.Stub;
import android.app.usage.StorageStats;
import android.app.usage.UsageStatsManagerInternal;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
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
import android.provider.Settings.Global;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseLongArray;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.Preconditions;
import com.android.server.IoThread;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.pm.Installer;
import com.android.server.pm.Installer.InstallerException;
import com.android.server.storage.CacheQuotaStrategy;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class StorageStatsService extends Stub {
    private static final long DEFAULT_QUOTA = 67108864;
    private static final long DELAY_IN_MILLIS = 30000;
    private static final String PROP_DISABLE_QUOTA = "fw.disable_quota";
    private static final String PROP_VERIFY_STORAGE = "fw.verify_storage";
    private static final String TAG = "StorageStatsService";
    private final AppOpsManager mAppOps;
    private final ArrayMap<String, SparseLongArray> mCacheQuotas = new ArrayMap();
    private final Context mContext;
    private final H mHandler;
    private final Installer mInstaller;
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
                        if (((double) Math.abs(this.mPreviousBytes - this.mStats.getAvailableBytes())) > this.mMinimumThresholdBytes) {
                            this.mPreviousBytes = this.mStats.getAvailableBytes();
                            recalculateQuotas(getInitializedStrategy());
                            StorageStatsService.this.notifySignificantDelta();
                        }
                        sendEmptyMessageDelayed(100, StorageStatsService.DELAY_IN_MILLIS);
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
                        sendEmptyMessageDelayed(100, StorageStatsService.DELAY_IN_MILLIS);
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

    private void invalidateMounts() {
        try {
            this.mInstaller.invalidateMounts();
        } catch (InstallerException e) {
            Slog.wtf(TAG, "Failed to invalidate mounts", e);
        }
    }

    private void enforcePermission(int callingUid, String callingPackage) {
        int mode = this.mAppOps.checkOp(43, callingUid, callingPackage);
        switch (mode) {
            case 0:
                return;
            case 3:
                this.mContext.enforceCallingOrSelfPermission("android.permission.PACKAGE_USAGE_STATS", TAG);
                return;
            default:
                throw new SecurityException("Package " + callingPackage + " from UID " + callingUid + " blocked by mode " + mode);
        }
    }

    public boolean isQuotaSupported(String volumeUuid, String callingPackage) {
        enforcePermission(Binder.getCallingUid(), callingPackage);
        try {
            return this.mInstaller.isQuotaSupported(volumeUuid);
        } catch (InstallerException e) {
            throw new ParcelableException(new IOException(e.getMessage()));
        }
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
            long usableSpace;
            if (isQuotaSupported(volumeUuid, callingPackage)) {
                usableSpace = path.getUsableSpace() + Math.max(0, getCacheBytes(volumeUuid, callingPackage) - this.mStorage.getStorageCacheBytes(path));
                Binder.restoreCallingIdentity(token);
                return usableSpace;
            }
            usableSpace = path.getUsableSpace();
            Binder.restoreCallingIdentity(token);
            return usableSpace;
        } catch (FileNotFoundException e) {
            throw new ParcelableException(e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
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
            return ((SparseLongArray) this.mCacheQuotas.get(volumeUuid)).get(uid, DEFAULT_QUOTA);
        }
        return DEFAULT_QUOTA;
    }

    public StorageStats queryStatsForPackage(String volumeUuid, String packageName, int userId, String callingPackage) {
        if (userId != UserHandle.getCallingUserId()) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS", TAG);
        }
        try {
            ApplicationInfo appInfo = this.mPackage.getApplicationInfoAsUser(packageName, 8192, userId);
            if (Binder.getCallingUid() != appInfo.uid) {
                enforcePermission(Binder.getCallingUid(), callingPackage);
            }
            if (ArrayUtils.defeatNullable(this.mPackage.getPackagesForUid(appInfo.uid)).length == 1) {
                return queryStatsForUid(volumeUuid, appInfo.uid, callingPackage);
            }
            int appId = UserHandle.getUserId(appInfo.uid);
            String[] packageNames = new String[]{packageName};
            long[] ceDataInodes = new long[1];
            String[] codePaths = new String[0];
            if (!appInfo.isSystemApp() || (appInfo.isUpdatedSystemApp() ^ 1) == 0) {
                codePaths = (String[]) ArrayUtils.appendElement(String.class, codePaths, appInfo.getCodePath());
            }
            PackageStats stats = new PackageStats(TAG);
            try {
                this.mInstaller.getAppSize(volumeUuid, packageNames, userId, 0, appId, ceDataInodes, codePaths, stats);
                return translate(stats);
            } catch (InstallerException e) {
                throw new ParcelableException(new IOException(e.getMessage()));
            }
        } catch (NameNotFoundException e2) {
            throw new ParcelableException(e2);
        }
    }

    public StorageStats queryStatsForUid(String volumeUuid, int uid, String callingPackage) {
        int userId = UserHandle.getUserId(uid);
        int appId = UserHandle.getAppId(uid);
        if (userId != UserHandle.getCallingUserId()) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS", TAG);
        }
        if (Binder.getCallingUid() != uid) {
            enforcePermission(Binder.getCallingUid(), callingPackage);
        }
        String[] packageNames = ArrayUtils.defeatNullable(this.mPackage.getPackagesForUid(uid));
        long[] ceDataInodes = new long[packageNames.length];
        String[] codePaths = new String[0];
        int i = 0;
        while (i < packageNames.length) {
            try {
                ApplicationInfo appInfo = this.mPackage.getApplicationInfoAsUser(packageNames[i], 8192, userId);
                if (!appInfo.isSystemApp() || (appInfo.isUpdatedSystemApp() ^ 1) == 0) {
                    codePaths = (String[]) ArrayUtils.appendElement(String.class, codePaths, appInfo.getCodePath());
                }
                i++;
            } catch (Throwable e) {
                throw new ParcelableException(e);
            }
        }
        PackageStats stats = new PackageStats(TAG);
        try {
            this.mInstaller.getAppSize(volumeUuid, packageNames, userId, getDefaultFlags(), appId, ceDataInodes, codePaths, stats);
            if (SystemProperties.getBoolean(PROP_VERIFY_STORAGE, false)) {
                PackageStats packageStats = new PackageStats(TAG);
                this.mInstaller.getAppSize(volumeUuid, packageNames, userId, 0, appId, ceDataInodes, codePaths, packageStats);
                checkEquals("UID " + uid, packageStats, stats);
            }
            return translate(stats);
        } catch (InstallerException e2) {
            throw new ParcelableException(new IOException(e2.getMessage()));
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
        } catch (InstallerException e) {
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
                checkEquals("External " + userId, this.mInstaller.getExternalSize(volumeUuid, userId, 0, appIds), stats);
            }
            ExternalStorageStats res = new ExternalStorageStats();
            res.totalBytes = stats[0];
            res.audioBytes = stats[1];
            res.videoBytes = stats[2];
            res.imageBytes = stats[3];
            res.appBytes = stats[4];
            return res;
        } catch (InstallerException e) {
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

    static boolean isCacheQuotaCalculationsEnabled(ContentResolver resolver) {
        if (Global.getInt(resolver, "enable_cache_quota_calculation", 1) != 0) {
            return true;
        }
        return false;
    }

    void notifySignificantDelta() {
        this.mContext.getContentResolver().notifyChange(Uri.parse("content://com.android.externalstorage.documents/"), null, false);
    }
}
