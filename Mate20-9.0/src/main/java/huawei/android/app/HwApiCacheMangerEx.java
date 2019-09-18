package huawei.android.app;

import android.app.ActivityThread;
import android.common.IHwApiCacheManagerEx;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.IStorageManager;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import java.util.HashMap;

public class HwApiCacheMangerEx implements IHwApiCacheManagerEx {
    private static final long CACHE_EXPIRED_TIME_NS = ((SystemProperties.getLong("persist.sys.freqinfo.cachems", 10000) * 1000) * 1000);
    private static final boolean DEBUG_PERF = SystemProperties.getBoolean("persist.sys.freqinfo.debugperf", false);
    private static final String TAG = "HwApiCacheMangerEx";
    private static boolean USE_CACHE = SystemProperties.getBoolean("persist.sys.freqinfo.cache", true);
    private static HwApiCacheMangerEx sInstance;
    private boolean bCanCache = false;
    private final Object mAppInfoLock = new Object();
    private long mAppInfoTimes = 0;
    private long mAppInfoUs = 0;
    private final Object mPackageInfoLock = new Object();
    private long mPackageInfoTimes = 0;
    private long mPackageInfoUs = 0;
    private final Object mPackageUidLock = new Object();
    private long mUidTimes = 0;
    private long mUidUs = 0;
    private int mVolumeCacheItemCnt = 0;
    private final Object mVolumeLock = new Object();
    private HashMap<String, ApplicationInfo> sAppInfoCache = new HashMap<>();
    private HashMap<String, PackageInfo> sPackageInfoCache = new HashMap<>();
    private HashMap<String, Integer> sPackageUidCache = new HashMap<>();
    private HashMap<String, StorageVolume[]> sVolumeCache = new HashMap<>();
    private long totalTimes = 0;
    private long totalUs = 0;

    public void disableCache() {
        this.bCanCache = false;
        USE_CACHE = false;
        synchronized (this.mAppInfoLock) {
            this.sAppInfoCache.clear();
            this.sAppInfoCache = null;
        }
        synchronized (this.mPackageInfoLock) {
            this.sPackageInfoCache.clear();
            this.sPackageInfoCache = null;
        }
        synchronized (this.mPackageUidLock) {
            this.sPackageUidCache.clear();
            this.sPackageUidCache = null;
        }
        synchronized (this.mVolumeLock) {
            this.sVolumeCache.clear();
            this.sVolumeCache = null;
        }
    }

    public static synchronized HwApiCacheMangerEx getDefault() {
        HwApiCacheMangerEx hwApiCacheMangerEx;
        synchronized (HwApiCacheMangerEx.class) {
            if (sInstance == null) {
                sInstance = new HwApiCacheMangerEx();
            }
            hwApiCacheMangerEx = sInstance;
        }
        return hwApiCacheMangerEx;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0084, code lost:
        return r4;
     */
    public ApplicationInfo getApplicationInfoAsUser(IPackageManager pm, String packageName, int flags, int userId) throws RemoteException {
        ApplicationInfo ai;
        String str = packageName;
        StringBuffer key = new StringBuffer();
        ApplicationInfo ai2 = null;
        long start = 0;
        boolean bNeedCache = false;
        if (this.bCanCache && DEBUG_PERF) {
            start = System.nanoTime();
        }
        String curPackageName = ActivityThread.currentPackageName();
        if (!(!this.bCanCache || str == null || curPackageName == null)) {
            bNeedCache = str.equals(curPackageName);
        }
        boolean bNeedCache2 = bNeedCache;
        try {
            if (!this.bCanCache || !bNeedCache2) {
                int i = flags;
                int i2 = userId;
                ai = pm.getApplicationInfo(packageName, flags, userId);
            } else {
                key.append(str);
                key.append("#");
                try {
                    key.append(userId);
                    key.append("#");
                } catch (RemoteException e) {
                    e = e;
                    int i3 = flags;
                    throw e.rethrowFromSystemServer();
                }
                try {
                    key.append(flags);
                    synchronized (this.mAppInfoLock) {
                        if (this.sAppInfoCache != null) {
                            ai2 = this.sAppInfoCache.get(key.toString());
                        }
                        if (ai2 == null) {
                            ai = pm.getApplicationInfo(packageName, flags, userId);
                            if (this.sAppInfoCache != null) {
                                this.sAppInfoCache.put(key.toString(), ai);
                            }
                        } else if (DEBUG_PERF && start > 0) {
                            this.mAppInfoUs += (System.nanoTime() - start) / 1000;
                            this.mAppInfoTimes++;
                        }
                    }
                } catch (RemoteException e2) {
                    e = e2;
                    throw e.rethrowFromSystemServer();
                }
            }
            if (ai == null) {
                return null;
            }
            if (this.bCanCache && bNeedCache2 && DEBUG_PERF && start > 0) {
                this.mAppInfoUs += (System.nanoTime() - start) / 1000;
                this.mAppInfoTimes++;
            }
            return ai;
        } catch (RemoteException e3) {
            e = e3;
            int i4 = flags;
            int i5 = userId;
            throw e.rethrowFromSystemServer();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0084, code lost:
        return r3;
     */
    public PackageInfo getPackageInfoAsUser(IPackageManager pm, String packageName, int flags, int userId) throws RemoteException {
        PackageInfo pi;
        String str = packageName;
        PackageInfo pi2 = null;
        StringBuffer key = new StringBuffer();
        long start = 0;
        boolean bNeedCache = false;
        if (this.bCanCache && DEBUG_PERF) {
            start = System.nanoTime();
        }
        String curPackageName = ActivityThread.currentPackageName();
        if (!(!this.bCanCache || str == null || curPackageName == null)) {
            bNeedCache = str.equals(curPackageName);
        }
        boolean bNeedCache2 = bNeedCache;
        try {
            if (!this.bCanCache || !bNeedCache2) {
                int i = flags;
                int i2 = userId;
                pi = pm.getPackageInfo(packageName, flags, userId);
            } else {
                key.append(str);
                key.append("#");
                try {
                    key.append(userId);
                    key.append("#");
                } catch (RemoteException e) {
                    e = e;
                    int i3 = flags;
                    throw e.rethrowFromSystemServer();
                }
                try {
                    key.append(flags);
                    synchronized (this.mPackageInfoLock) {
                        if (this.sPackageInfoCache != null) {
                            pi2 = this.sPackageInfoCache.get(key.toString());
                        }
                        if (pi2 == null) {
                            pi = pm.getPackageInfo(packageName, flags, userId);
                            if (this.sPackageInfoCache != null) {
                                this.sPackageInfoCache.put(key.toString(), pi);
                            }
                        } else if (DEBUG_PERF && start > 0) {
                            this.mPackageInfoUs += (System.nanoTime() - start) / 1000;
                            this.mPackageInfoTimes++;
                        }
                    }
                } catch (RemoteException e2) {
                    e = e2;
                    throw e.rethrowFromSystemServer();
                }
            }
            if (pi == null) {
                return null;
            }
            if (this.bCanCache && bNeedCache2 && DEBUG_PERF && start > 0) {
                this.mPackageInfoUs += (System.nanoTime() - start) / 1000;
                this.mPackageInfoTimes++;
            }
            return pi;
        } catch (RemoteException e3) {
            e = e3;
            int i4 = flags;
            int i5 = userId;
            throw e.rethrowFromSystemServer();
        }
    }

    public int getPackageUidAsUser(IPackageManager pm, String packageName, int flags, int userId) throws RemoteException {
        boolean bNeedCache;
        int uid;
        String str = packageName;
        Integer oUid = null;
        StringBuffer key = new StringBuffer();
        long start = 0;
        boolean bNeedCache2 = false;
        if (this.bCanCache && DEBUG_PERF) {
            start = System.nanoTime();
        }
        String curPackageName = ActivityThread.currentPackageName();
        if (!(!this.bCanCache || str == null || curPackageName == null)) {
            bNeedCache2 = str.equals(curPackageName);
        }
        boolean bNeedCache3 = bNeedCache2;
        try {
            if (!this.bCanCache || !bNeedCache3) {
                int i = flags;
                int i2 = userId;
                String str2 = curPackageName;
                bNeedCache = bNeedCache3;
                uid = pm.getPackageUid(packageName, flags, userId);
            } else {
                key.append(str);
                key.append("#");
                try {
                    key.append(userId);
                    key.append("#");
                    try {
                        key.append(flags);
                        synchronized (this.mPackageUidLock) {
                            try {
                                if (this.sPackageUidCache != null) {
                                    try {
                                        oUid = this.sPackageUidCache.get(key.toString());
                                    } catch (Throwable th) {
                                        th = th;
                                        String str3 = curPackageName;
                                        boolean z = bNeedCache3;
                                        try {
                                            throw th;
                                        } catch (RemoteException e) {
                                            e = e;
                                            throw e.rethrowFromSystemServer();
                                        }
                                    }
                                }
                                if (oUid != null) {
                                    if (!DEBUG_PERF || start <= 0) {
                                        boolean z2 = bNeedCache3;
                                    } else {
                                        String str4 = curPackageName;
                                        boolean z3 = bNeedCache3;
                                        this.mUidUs += (System.nanoTime() - start) / 1000;
                                        this.mUidTimes++;
                                    }
                                    int intValue = oUid.intValue();
                                    return intValue;
                                }
                                bNeedCache = bNeedCache3;
                                uid = pm.getPackageUid(packageName, flags, userId);
                                if (uid >= 0 && this.sPackageUidCache != null) {
                                    this.sPackageUidCache.put(key.toString(), Integer.valueOf(uid));
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        }
                    } catch (RemoteException e2) {
                        e = e2;
                        String str5 = curPackageName;
                        boolean z4 = bNeedCache3;
                        throw e.rethrowFromSystemServer();
                    }
                } catch (RemoteException e3) {
                    e = e3;
                    int i3 = flags;
                    String str52 = curPackageName;
                    boolean z42 = bNeedCache3;
                    throw e.rethrowFromSystemServer();
                }
            }
            if (uid < 0) {
                return -1;
            }
            if (this.bCanCache && bNeedCache && DEBUG_PERF && start > 0) {
                this.mUidUs += (System.nanoTime() - start) / 1000;
                this.mUidTimes++;
            }
            return uid;
        } catch (RemoteException e4) {
            e = e4;
            int i4 = flags;
            int i5 = userId;
            String str522 = curPackageName;
            boolean z422 = bNeedCache3;
            throw e.rethrowFromSystemServer();
        }
    }

    public StorageVolume[] getVolumeList(IStorageManager storageManager, String packageName, int userId, int flags) throws RemoteException {
        boolean bNeedCache;
        StorageVolume[] volumes;
        long end;
        IStorageManager iStorageManager = storageManager;
        String str = packageName;
        int i = flags;
        long start = 0;
        long end2 = 0;
        if (iStorageManager == null) {
            Log.i(TAG, "apicache storageManager is null for " + str);
            return new StorageVolume[0];
        }
        boolean canCache = SystemProperties.getBoolean("persist.sys.getvolumelist.cache", true);
        boolean bNeedCache2 = doVolumeCacheItem(canCache, str);
        if (this.bCanCache && bNeedCache2 && DEBUG_PERF) {
            start = System.nanoTime();
        }
        int uid = getUid(this.bCanCache, str, userId);
        if (uid <= 0) {
            return new StorageVolume[0];
        }
        StorageVolume[] volumes2 = null;
        try {
            if (!this.bCanCache || !bNeedCache2) {
                boolean z = canCache;
                bNeedCache = bNeedCache2;
                try {
                    if (DEBUG_PERF && this.bCanCache) {
                        Log.i(TAG, "get volume without apicache for " + str);
                    }
                    volumes = iStorageManager.getVolumeList(uid, str, i);
                } catch (RemoteException e) {
                    e = e;
                    throw e.rethrowFromSystemServer();
                }
            } else {
                StringBuffer key = new StringBuffer();
                key.append(str);
                key.append("#");
                key.append(uid);
                key.append("#");
                key.append(i);
                synchronized (this.mVolumeLock) {
                    try {
                        if (this.sVolumeCache != null) {
                            try {
                                end = 0;
                            } catch (Throwable th) {
                                th = th;
                                boolean z2 = canCache;
                                boolean z3 = bNeedCache2;
                                try {
                                    throw th;
                                } catch (RemoteException e2) {
                                    e = e2;
                                    long j = end2;
                                }
                            }
                            try {
                                volumes2 = this.sVolumeCache.get(key.toString());
                            } catch (Throwable th2) {
                                th = th2;
                                boolean z4 = canCache;
                                boolean z5 = bNeedCache2;
                                end2 = end;
                                throw th;
                            }
                        } else {
                            end = 0;
                        }
                        if (volumes2 != null) {
                            try {
                                if (!DEBUG_PERF || start <= 0) {
                                    boolean z6 = bNeedCache2;
                                } else {
                                    end2 = System.nanoTime();
                                    boolean z7 = canCache;
                                    boolean z8 = bNeedCache2;
                                    try {
                                        this.totalUs += (end2 - start) / 1000;
                                        this.totalTimes++;
                                        end = end2;
                                    } catch (Throwable th3) {
                                        th = th3;
                                        throw th;
                                    }
                                }
                                return volumes2;
                            } catch (Throwable th4) {
                                th = th4;
                                end2 = end;
                                throw th;
                            }
                        } else {
                            bNeedCache = bNeedCache2;
                            volumes = iStorageManager.getVolumeList(uid, str, i);
                            doStorageVolume(volumes, key.toString());
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        boolean z9 = canCache;
                        boolean z10 = bNeedCache2;
                        throw th;
                    }
                }
            }
            if (this.bCanCache && bNeedCache && DEBUG_PERF && start > 0) {
                end2 = System.nanoTime();
                this.totalUs += (end2 - start) / 1000;
                this.totalTimes++;
                long j2 = end2;
            }
            return volumes;
        } catch (RemoteException e3) {
            e = e3;
            boolean z11 = canCache;
            boolean z12 = bNeedCache2;
            throw e.rethrowFromSystemServer();
        }
    }

    private int getUid(boolean bCanCache2, String packageName, int userId) throws RemoteException {
        if (!bCanCache2) {
            return ActivityThread.getPackageManager().getPackageUid(packageName, 268435456, userId);
        }
        try {
            return ActivityThread.currentActivityThread().getSystemContext().getPackageManager().getPackageUidAsUser(packageName, 268435456, userId);
        } catch (Exception e) {
            Log.w(TAG, "apicache getPackageUidAsUser excption:" + e.getMessage());
            return -2;
        }
    }

    private boolean doVolumeCacheItem(boolean canCache, String packageName) {
        if (!canCache) {
            Log.i(TAG, "clear apicache now,maybe insert card or remove card or rebooting system_server");
            synchronized (this.mVolumeLock) {
                if (this.mVolumeCacheItemCnt > 0) {
                    this.sVolumeCache.clear();
                    this.mVolumeCacheItemCnt = 0;
                }
            }
            return false;
        }
        String curPackageName = ActivityThread.currentPackageName();
        if (!this.bCanCache || packageName == null || curPackageName == null) {
            return false;
        }
        return packageName.equals(curPackageName);
    }

    private void doStorageVolume(StorageVolume[] volumes, String key) {
        boolean cantCache = false;
        int i = 0;
        while (true) {
            if (volumes == null || i >= volumes.length) {
                break;
            }
            Log.i(TAG, "apicache path=" + volumes[i].getPath() + " state=" + volumes[i].getState() + " key=" + key);
            if (!volumes[i].getState().equals("mounted")) {
                cantCache = true;
                break;
            }
            i++;
        }
        if (volumes == null) {
            cantCache = true;
            Log.i(TAG, "cant apicache now,because volumes is null");
        }
        if (!cantCache && this.sVolumeCache != null) {
            if (this.mVolumeCacheItemCnt > 0 && volumes != null) {
                Log.i(TAG, "need clear apicache,because volumes changed,oldCnt=" + this.mVolumeCacheItemCnt + " newCnt=" + volumes.length);
                this.sVolumeCache.clear();
                this.mVolumeCacheItemCnt = volumes.length;
            }
            this.sVolumeCache.put(key, volumes);
        }
    }

    public void apiPreCache(PackageManager app) {
        if (USE_CACHE) {
            this.bCanCache = true;
            String packageName = ActivityThread.currentPackageName();
            if (DEBUG_PERF) {
                Log.i(TAG, "apicache mCurPackageName=" + packageName + " uptimes=" + SystemClock.elapsedRealtime());
            }
            long start = 0;
            if (packageName != null) {
                int userId = UserHandle.myUserId();
                cacheVolumeList(userId);
                if (DEBUG_PERF) {
                    start = System.nanoTime();
                    Log.i(TAG, "apicache async read begin packageName=" + packageName + " userid=" + userId);
                }
                cachePackageInfo(app, packageName);
                if (DEBUG_PERF) {
                    long end = System.nanoTime();
                    Log.i(TAG, "apicache async read finished packageName=" + packageName + " userid=" + userId + " totalus=" + ((end - start) / 1000));
                }
            }
        }
    }

    public void notifyVolumeStateChanged(int oldState, int newState) {
        Log.i(TAG, "notify for apicache oldState=" + oldState + " newState=" + newState);
        if (newState == 2 || newState == 3 || newState == 7 || newState == 8) {
            SystemProperties.set("persist.sys.getvolumelist.cache", "true");
        } else {
            SystemProperties.set("persist.sys.getvolumelist.cache", "false");
        }
    }

    private void cacheVolumeList(int userId) {
        try {
            StorageManager.getVolumeList(userId, 256);
            StorageManager.getVolumeList(userId, 0);
        } catch (Exception e) {
            Log.w(TAG, "apicache getVolumeList excption:" + e.getMessage());
        }
    }

    private void cachePackageInfo(PackageManager app, String packageName) {
        if (app != null) {
            try {
                app.getPackageInfo(packageName, 0);
                app.getPackageInfo(packageName, 64);
                app.getPackageInfo(packageName, 4096);
                app.getPackageUid(packageName, 0);
            } catch (Exception e) {
                Log.w(TAG, "apicache getPackageInfo excption:" + e.getMessage());
            }
        }
    }
}
