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
import com.huawei.sidetouch.TpCommandConstant;
import java.util.HashMap;

public class HwApiCacheMangerEx implements IHwApiCacheManagerEx {
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
    private PackageManager mPkg = null;
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
        this.mPkg = null;
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

    public ApplicationInfo getApplicationInfoAsUser(IPackageManager pm, String packageName, int flags, int userId) throws RemoteException {
        boolean bNeedCache;
        RemoteException e;
        ApplicationInfo ai;
        StringBuffer key = new StringBuffer();
        ApplicationInfo ai2 = null;
        long start = 0;
        if (this.bCanCache && DEBUG_PERF) {
            start = System.nanoTime();
        }
        String curPackageName = ActivityThread.currentPackageName();
        if (!this.bCanCache || packageName == null || curPackageName == null) {
            bNeedCache = false;
        } else {
            bNeedCache = packageName.equals(curPackageName);
        }
        try {
            if (!this.bCanCache || !bNeedCache) {
                ai = pm.getApplicationInfo(packageName, flags, userId);
            } else {
                key.append(packageName);
                key.append(TpCommandConstant.SEPARATE);
                try {
                    key.append(userId);
                    key.append(TpCommandConstant.SEPARATE);
                    try {
                        key.append(flags);
                        synchronized (this.mAppInfoLock) {
                            if (this.sAppInfoCache != null) {
                                ai2 = this.sAppInfoCache.get(key.toString());
                            }
                            if (ai2 != null) {
                                if (DEBUG_PERF && start > 0) {
                                    this.mAppInfoUs += (System.nanoTime() - start) / 1000;
                                    this.mAppInfoTimes++;
                                }
                                return ai2;
                            }
                            ai = pm.getApplicationInfo(packageName, flags, userId);
                            if (this.sAppInfoCache != null) {
                                this.sAppInfoCache.put(key.toString(), ai);
                            }
                        }
                    } catch (RemoteException e2) {
                        e = e2;
                        throw e.rethrowFromSystemServer();
                    }
                } catch (RemoteException e3) {
                    e = e3;
                    throw e.rethrowFromSystemServer();
                }
            }
            if (ai == null) {
                return null;
            }
            if (this.bCanCache && bNeedCache && DEBUG_PERF && start > 0) {
                this.mAppInfoUs += (System.nanoTime() - start) / 1000;
                this.mAppInfoTimes++;
            }
            return ai;
        } catch (RemoteException e4) {
            e = e4;
            throw e.rethrowFromSystemServer();
        }
    }

    public PackageInfo getPackageInfoAsUser(IPackageManager pm, String packageName, int flags, int userId) throws RemoteException {
        boolean bNeedCache;
        RemoteException e;
        PackageInfo pi;
        PackageInfo pi2 = null;
        StringBuffer key = new StringBuffer();
        long start = 0;
        if (this.bCanCache && DEBUG_PERF) {
            start = System.nanoTime();
        }
        String curPackageName = ActivityThread.currentPackageName();
        if (!this.bCanCache || packageName == null || curPackageName == null) {
            bNeedCache = false;
        } else {
            bNeedCache = packageName.equals(curPackageName);
        }
        try {
            if (!this.bCanCache || !bNeedCache) {
                pi = pm.getPackageInfo(packageName, flags, userId);
            } else {
                key.append(packageName);
                key.append(TpCommandConstant.SEPARATE);
                try {
                    key.append(userId);
                    key.append(TpCommandConstant.SEPARATE);
                    try {
                        key.append(flags);
                        synchronized (this.mPackageInfoLock) {
                            if (this.sPackageInfoCache != null) {
                                pi2 = this.sPackageInfoCache.get(key.toString());
                            }
                            if (pi2 != null) {
                                if (DEBUG_PERF && start > 0) {
                                    this.mPackageInfoUs += (System.nanoTime() - start) / 1000;
                                    this.mPackageInfoTimes++;
                                }
                                return pi2;
                            }
                            pi = pm.getPackageInfo(packageName, flags, userId);
                            if (this.sPackageInfoCache != null) {
                                this.sPackageInfoCache.put(key.toString(), pi);
                            }
                        }
                    } catch (RemoteException e2) {
                        e = e2;
                        throw e.rethrowFromSystemServer();
                    }
                } catch (RemoteException e3) {
                    e = e3;
                    throw e.rethrowFromSystemServer();
                }
            }
            if (pi == null) {
                return null;
            }
            if (this.bCanCache && bNeedCache && DEBUG_PERF && start > 0) {
                this.mPackageInfoUs += (System.nanoTime() - start) / 1000;
                this.mPackageInfoTimes++;
            }
            return pi;
        } catch (RemoteException e4) {
            e = e4;
            throw e.rethrowFromSystemServer();
        }
    }

    public int getPackageUidAsUser(IPackageManager pm, String packageName, int flags, int userId) throws RemoteException {
        boolean bNeedCache;
        int uid;
        Integer oUid = null;
        StringBuffer key = new StringBuffer();
        long start = 0;
        if (this.bCanCache && DEBUG_PERF) {
            start = System.nanoTime();
        }
        String curPackageName = ActivityThread.currentPackageName();
        if (!this.bCanCache || packageName == null || curPackageName == null) {
            bNeedCache = false;
        } else {
            bNeedCache = packageName.equals(curPackageName);
        }
        try {
            if (!this.bCanCache || !bNeedCache) {
                uid = pm.getPackageUid(packageName, flags, userId);
            } else {
                key.append(packageName);
                key.append(TpCommandConstant.SEPARATE);
                key.append(userId);
                key.append(TpCommandConstant.SEPARATE);
                key.append(flags);
                synchronized (this.mPackageUidLock) {
                    if (this.sPackageUidCache != null) {
                        oUid = this.sPackageUidCache.get(key.toString());
                    }
                    if (oUid != null) {
                        if (DEBUG_PERF && start > 0) {
                            this.mUidUs += (System.nanoTime() - start) / 1000;
                            this.mUidTimes++;
                        }
                        return oUid.intValue();
                    }
                    uid = pm.getPackageUid(packageName, flags, userId);
                    if (uid >= 0 && this.sPackageUidCache != null) {
                        this.sPackageUidCache.put(key.toString(), Integer.valueOf(uid));
                    }
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
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public StorageVolume[] getVolumeList(IStorageManager storageManager, String packageName, int userId, int flags) throws RemoteException {
        RemoteException e;
        boolean bNeedCache;
        StorageVolume[] volumes;
        Throwable th;
        long end;
        long start = 0;
        long end2 = 0;
        if (storageManager == null) {
            Log.i(TAG, "apicache storageManager is null for " + packageName);
            return new StorageVolume[0];
        }
        boolean bNeedCache2 = doVolumeCacheItem(SystemProperties.getBoolean("persist.sys.getvolumelist.cache", true), packageName);
        if (this.bCanCache && bNeedCache2 && DEBUG_PERF) {
            start = System.nanoTime();
        }
        int uid = getUid(this.bCanCache, packageName, userId);
        if (uid <= 0) {
            Log.i(TAG, "apicache getVolumeList uid is invalid " + uid);
            return new StorageVolume[0];
        }
        StorageVolume[] volumes2 = null;
        try {
            if (!this.bCanCache || !bNeedCache2) {
                bNeedCache = bNeedCache2;
                try {
                    if (DEBUG_PERF && this.bCanCache) {
                        Log.i(TAG, "get volume without apicache for " + packageName);
                    }
                    volumes = storageManager.getVolumeList(uid, packageName, flags);
                } catch (RemoteException e2) {
                    e = e2;
                    throw e.rethrowFromSystemServer();
                }
            } else {
                StringBuffer key = new StringBuffer();
                key.append(packageName);
                key.append(TpCommandConstant.SEPARATE);
                key.append(uid);
                key.append(TpCommandConstant.SEPARATE);
                key.append(flags);
                synchronized (this.mVolumeLock) {
                    try {
                        if (this.sVolumeCache != null) {
                            try {
                                end = 0;
                                try {
                                    volumes2 = this.sVolumeCache.get(key.toString());
                                } catch (Throwable th2) {
                                    th = th2;
                                    end2 = 0;
                                    try {
                                        throw th;
                                    } catch (RemoteException e3) {
                                        e = e3;
                                    }
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                throw th;
                            }
                        } else {
                            end = 0;
                        }
                        if (volumes2 != null) {
                            try {
                                if (DEBUG_PERF && start > 0) {
                                    end2 = System.nanoTime();
                                    try {
                                        this.totalUs += (end2 - start) / 1000;
                                        this.totalTimes++;
                                        end = end2;
                                    } catch (Throwable th4) {
                                        th = th4;
                                        throw th;
                                    }
                                }
                                return volumes2;
                            } catch (Throwable th5) {
                                th = th5;
                                end2 = end;
                                throw th;
                            }
                        } else {
                            bNeedCache = bNeedCache2;
                            volumes = storageManager.getVolumeList(uid, packageName, flags);
                            doStorageVolume(volumes, key.toString());
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        throw th;
                    }
                }
            }
            if (this.bCanCache && bNeedCache && DEBUG_PERF && start > 0) {
                this.totalUs += (System.nanoTime() - start) / 1000;
                this.totalTimes++;
            }
            return volumes;
        } catch (RemoteException e4) {
            e = e4;
            throw e.rethrowFromSystemServer();
        }
    }

    private int getUid(boolean bCanCache2, String packageName, int userId) throws RemoteException {
        if (!bCanCache2) {
            return ActivityThread.getPackageManager().getPackageUid(packageName, 268435456, userId);
        }
        try {
            if (this.mPkg != null) {
                return this.mPkg.getPackageUidAsUser(packageName, 268435456, userId);
            }
            return ActivityThread.getPackageManager().getPackageUid(packageName, 268435456, userId);
        } catch (Exception e) {
            Log.w(TAG, "getUid, apicache getPackageUidAsUser excption");
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
            if (volumes != null) {
                Log.i(TAG, "need clear apicache,because volumes changed,oldCnt=" + this.mVolumeCacheItemCnt + " newCnt=" + volumes.length);
                this.sVolumeCache.clear();
                this.mVolumeCacheItemCnt = volumes.length;
            }
            this.sVolumeCache.put(key, volumes);
        }
    }

    public void apiPreCache(PackageManager app) {
        if (USE_CACHE) {
            this.mPkg = app;
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
            Log.w(TAG, "cacheVolumeList, apicache getVolumeList excption");
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
                Log.w(TAG, "cachePackageInfo, apicache getPackageInfo excption");
            }
        }
    }
}
