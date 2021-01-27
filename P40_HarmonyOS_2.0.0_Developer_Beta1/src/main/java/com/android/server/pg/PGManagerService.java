package com.android.server.pg;

import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.util.Log;
import android.util.Slog;
import com.android.server.AlarmManagerService;
import com.android.server.DeviceIdleController;
import com.android.server.LocalServices;
import com.android.server.LocationManagerService;
import com.android.server.am.ActivityManagerService;
import com.android.server.job.JobSchedulerInternal;
import com.android.server.pg.IPGManager;
import com.android.server.power.PowerManagerService;
import com.android.server.slice.SliceClientPermissions;
import com.huawei.pgmng.log.LogPower;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class PGManagerService extends IPGManager.Stub {
    public static final int BLACK_LIST_TYPE_WIFI = 6;
    private static final int CODE_ENABLE_WIFICHIP_CHECK = 4021;
    private static final int CODE_RESTRICT_WIFI_SCAN = 4001;
    private static final int CONFIG_TYPE_ALARM_EXEMPTION = 8;
    private static final int CONFIG_TYPE_GOOGLE_CTRL = 6;
    private static final int CONFIG_TYPE_PROXY_JOB = 4;
    private static final int CONFIG_TYPE_PROXY_SERVICE = 0;
    private static final int CONFIG_TYPE_PROXY_WAKELOCK = 2;
    private static final int CONFIG_TYPE_QUICK_DOZE = 5;
    private static final int CONFIG_TYPE_WIFICHIP_CHECK = 7;
    private static final int CONFIG_TYPE_WIFI_RESTRICT = 1;
    private static final String DESCRIPTOR_IWIFIMANAGER = "android.net.wifi.IWifiManager";
    public static final int DISABLE_LIST_TYPE_QUICKTTFF = 4;
    public static final boolean DISABLE_PG = SystemProperties.getBoolean("ro.config.pg_disable_pg", false);
    public static final int SUBTYPE_DROP_WAKELOCK = 1;
    public static final int SUBTYPE_PROXY = 0;
    public static final int SUBTYPE_PROXY_NO_WORKSOURCE = 2;
    public static final int SUBTYPE_PROXY_SELF = 3;
    public static final int SUBTYPE_RELEASE_NO_WORKSOURCE = 3;
    public static final int SUBTYPE_UNPROXY = 1;
    public static final int SUBTYPE_UNPROXY_ALL = 2;
    private static final String TAG = "PGManagerService";
    public static final int WHITE_LIST_TYPE_GPS = 1;
    public static final int WHITE_LIST_TYPE_QUICKTTFF = 3;
    public static final int WHITE_LIST_TYPE_WIFI = 2;
    public static final int WHITE_LIST_TYPE_WIFI_SLEEP = 7;
    private static final Object mLock = new Object();
    private static PGManagerService sInstance = null;
    private ActivityManagerService mAM;
    private final Context mContext;
    private final PGGoogleServicePolicy mGoogleServicePolicy;
    private AlarmManagerService mHms;
    private LocationManagerService mLMS;
    private PowerManagerService mPms;
    private final ProcBatteryStats mProcStats;
    private HashSet<String> mProxyJobList = new HashSet<>();
    private HashSet<String> mProxySelfServiceList = new HashSet<>();
    private HashSet<String> mProxyServiceList = new HashSet<>();
    private boolean mSystemReady;

    private PGManagerService(Context context) {
        this.mContext = context;
        if (DISABLE_PG) {
            Log.i(TAG, "pg is disabled, do nothing");
            this.mProcStats = null;
            this.mGoogleServicePolicy = null;
            return;
        }
        this.mProcStats = new ProcBatteryStats(this.mContext);
        LocalServices.addService(PGManagerInternal.class, new LocalService());
        this.mGoogleServicePolicy = new PGGoogleServicePolicy(this.mContext);
    }

    /* JADX WARN: Type inference failed for: r2v0, types: [android.os.IBinder, com.android.server.pg.PGManagerService] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public static PGManagerService getInstance(Context context) {
        PGManagerService pGManagerService;
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new PGManagerService(context);
                if (!DISABLE_PG) {
                    ServiceManager.addService("pgservice", (IBinder) sInstance);
                }
            }
            pGManagerService = sInstance;
        }
        return pGManagerService;
    }

    public void systemReady(ActivityManagerService activityManagerService, PowerManagerService powerManagerService, LocationManagerService location, AlarmManagerService hwAlarmManagerService) {
        if (!DISABLE_PG) {
            synchronized (mLock) {
                Log.i(TAG, "PGManagerService--systemReady--begain");
                this.mAM = activityManagerService;
                this.mHms = hwAlarmManagerService;
                this.mPms = powerManagerService;
                this.mLMS = location;
                this.mSystemReady = true;
                this.mProcStats.onSystemReady();
                this.mGoogleServicePolicy.onSystemReady();
                Log.i(TAG, "PGManagerService--systemReady--end");
            }
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (this.mProcStats.onTransact(code, data, reply, flags)) {
            return true;
        }
        return PGManagerService.super.onTransact(code, data, reply, flags);
    }

    class LocalService extends PGManagerInternal {
        LocalService() {
        }

        @Override // com.android.server.pg.PGManagerInternal
        public void noteStartWakeLock(String tag, WorkSource ws, String pkgName, int uid) {
            PGManagerService.this.mProcStats.processWakeLock(160, tag, ws, pkgName, uid);
        }

        @Override // com.android.server.pg.PGManagerInternal
        public void noteStopWakeLock(String tag, WorkSource ws, String pkgName, int uid) {
            PGManagerService.this.mProcStats.processWakeLock(161, tag, ws, pkgName, uid);
        }

        @Override // com.android.server.pg.PGManagerInternal
        public void noteChangeWakeLock(String tag, WorkSource ws, String pkgName, int uid, String newTag, WorkSource newWs, String newPkgName, int newUid) {
            PGManagerService.this.mProcStats.processWakeLock(161, tag, ws, pkgName, uid);
            PGManagerService.this.mProcStats.processWakeLock(160, newTag, newWs, newPkgName, newUid);
        }

        @Override // com.android.server.pg.PGManagerInternal
        public boolean isServiceProxy(ComponentName name, String sourcePkg) {
            if (sourcePkg == null) {
                synchronized (PGManagerService.this.mProxyServiceList) {
                    if (PGManagerService.this.mProxyServiceList.size() == 0) {
                        return false;
                    }
                    return isServiceMatchList(name, sourcePkg, PGManagerService.this.mProxyServiceList);
                }
            }
            synchronized (PGManagerService.this.mProxyJobList) {
                if (PGManagerService.this.mProxyJobList.size() == 0) {
                    return false;
                }
                return isServiceMatchList(name, sourcePkg, PGManagerService.this.mProxyJobList);
            }
        }

        @Override // com.android.server.pg.PGManagerInternal
        public boolean isServiceMatchList(ComponentName name, String sourcePkg, Collection<String> list) {
            if (name == null || list == null) {
                return false;
            }
            String pkg = name.getPackageName();
            String pkgAndCls = pkg + SliceClientPermissions.SliceAuthority.DELIMITER + name.getClassName();
            String pkgAndShortCls = pkg + SliceClientPermissions.SliceAuthority.DELIMITER + name.getShortClassName();
            String anyPkgAndCls = "*/" + name.getClassName();
            if (list.contains(pkg)) {
                return true;
            }
            if ((sourcePkg != null && list.contains(sourcePkg)) || list.contains(pkgAndCls) || list.contains(anyPkgAndCls)) {
                return true;
            }
            if (pkgAndShortCls.equals(pkgAndCls) || !list.contains(pkgAndShortCls)) {
                return false;
            }
            return true;
        }

        @Override // com.android.server.pg.PGManagerInternal
        public boolean isGmsWakeLockFilterTag(int flags, String packageName, WorkSource ws) {
            if (PGManagerService.this.mGoogleServicePolicy != null) {
                return PGManagerService.this.mGoogleServicePolicy.isGmsWakeLockFilterTag(flags, packageName, ws);
            }
            return false;
        }

        @Override // com.android.server.pg.PGManagerInternal
        public void notifyWakelock(int flags, String tag, int ownerUid, int ownerPid, WorkSource workSource, int eventTag) {
            if (workSource != null) {
                int size = workSource.size();
                for (int i = 0; i < size; i++) {
                    int ownerUid2 = workSource.get(i);
                    if (!(ownerUid2 == 1000 || ownerUid2 == 1001)) {
                        LogPower.push(eventTag, Integer.toString(ownerUid2), Integer.toString(flags), Integer.toString(-2), new String[]{tag});
                    }
                }
            } else if (ownerUid != 1000 && ownerUid != 1001) {
                LogPower.push(eventTag, Integer.toString(ownerUid), Integer.toString(flags), Integer.toString(ownerPid), new String[]{tag});
            }
        }

        @Override // com.android.server.pg.PGManagerInternal
        public boolean isServiceProxySelf(String pkg) {
            synchronized (PGManagerService.this.mProxyServiceList) {
                if (pkg != null) {
                    if (PGManagerService.this.mProxySelfServiceList.size() != 0) {
                        return PGManagerService.this.mProxySelfServiceList.contains(pkg);
                    }
                }
                return false;
            }
        }

        private void checkWorkSourceThenNote(int flags, String tag, int ownerUid, int ownerPid, WorkSource workSource1, WorkSource workSource2, int eventTag) {
            int workSource1size = workSource1.size();
            int workSource2Size = workSource2.size();
            for (int i = 0; i < workSource2Size; i++) {
                int ownerUid2 = workSource2.get(i);
                int j = 0;
                while (true) {
                    if (j >= workSource1size) {
                        break;
                    } else if (workSource1.get(j) == ownerUid2) {
                        break;
                    } else {
                        j++;
                    }
                }
                if (j == workSource1size) {
                    if (ownerUid2 != 1000 && ownerUid2 != 1001) {
                        LogPower.push(eventTag, Integer.toString(ownerUid2), Integer.toString(flags), Integer.toString(-2), new String[]{tag});
                    }
                }
            }
        }

        @Override // com.android.server.pg.PGManagerInternal
        public void notifyWakelock(int flags, String tag, int ownerUid, int ownerPid, WorkSource oldWorkSource, WorkSource newWorkSource) {
            checkWorkSourceThenNote(flags, tag, ownerUid, ownerPid, newWorkSource, oldWorkSource, 161);
            checkWorkSourceThenNote(flags, tag, ownerUid, ownerPid, oldWorkSource, newWorkSource, 160);
        }
    }

    private boolean restrictWifiScan(List<String> pkgs, boolean restrict) {
        if (pkgs != null && pkgs.size() == 0) {
            Log.w(TAG, "pkgs is empty, nothing to do.");
            return false;
        } else if (!restrict || pkgs != null) {
            Log.i(TAG, "start restrictWifiScan:" + pkgs + ", restrict:" + restrict);
            IBinder b = ServiceManager.getService("wifi");
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            if (b != null) {
                try {
                    data.writeInterfaceToken(DESCRIPTOR_IWIFIMANAGER);
                    data.writeStringList(pkgs);
                    data.writeInt(restrict ? 1 : 0);
                    b.transact(CODE_RESTRICT_WIFI_SCAN, data, reply, 0);
                    reply.readException();
                } catch (Exception e) {
                    Log.e(TAG, "restrict wifi scan error", e);
                } catch (Throwable th) {
                    reply.recycle();
                    data.recycle();
                    throw th;
                }
            }
            reply.recycle();
            data.recycle();
            return true;
        } else {
            Log.w(TAG, "illegal parameters.");
            return false;
        }
    }

    public long proxyBroadcast(List<String> pkgs, boolean proxy) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for proxyBroadcast:" + pkgs + " proxy:" + proxy);
            return -1;
        } else if (Binder.getCallingUid() != 1000) {
            Log.e(TAG, "proxy broadcast permission not allowed. uid = " + Binder.getCallingUid());
            return -1;
        } else {
            Log.i(TAG, "proxyBroadcast:" + pkgs + " proxy:" + proxy);
            return this.mAM.proxyBroadcast(pkgs, proxy);
        }
    }

    public long proxyBroadcastByPid(List<String> pids, boolean proxy) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for proxyBroadcastByPid:" + pids + " proxy:" + proxy);
            return -1;
        } else if (Binder.getCallingUid() != 1000) {
            Log.e(TAG, "proxy broadcast permission not allowed. uid = " + Binder.getCallingUid());
            return -1;
        } else {
            Log.i(TAG, "proxyBroadcastByPid:" + pids + " proxy:" + proxy);
            List<Integer> ipids = new ArrayList<>();
            if (pids != null) {
                for (String pid : pids) {
                    try {
                        ipids.add(Integer.valueOf(Integer.parseInt(pid)));
                    } catch (NumberFormatException e) {
                        Slog.d(TAG, "NumberFormatException:" + e.getMessage());
                    }
                }
            } else {
                ipids = null;
            }
            return this.mAM.proxyBroadcastByPid(ipids, proxy);
        }
    }

    public void setProxyBroadcastActions(List<String> actions) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for setProxyBroadcastActions:" + actions);
        } else if (Binder.getCallingUid() != 1000) {
            Log.e(TAG, "setProxyBroadcastActions permission not allowed. uid = " + Binder.getCallingUid());
        } else {
            Log.i(TAG, "proxy Broadcast Actions:" + actions);
            this.mAM.setProxyBroadcastActions(actions);
        }
    }

    public void setActionExcludePkg(String action, String pkg) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for setActionExcludePkg action:" + action + " pkg:" + pkg);
        } else if (Binder.getCallingUid() != 1000) {
            Log.e(TAG, "setActionExcludePkg permission not allowed. uid = " + Binder.getCallingUid());
        } else {
            Log.i(TAG, "set action:" + action + " pkg:" + pkg);
            this.mAM.setActionExcludePkg(action, pkg);
        }
    }

    public void proxyBroadcastConfig(int type, String key, List<String> value) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for proxyBroadcastConfig type:" + type + " key:" + key + " value:" + value);
        } else if (Binder.getCallingUid() != 1000) {
            Log.e(TAG, "proxyBroadcastConfig permission not allowed. uid = " + Binder.getCallingUid());
        } else {
            Log.i(TAG, "proxy config:" + type + " ," + key + " ," + value);
            this.mAM.proxyBroadcastConfig(type, key, value);
        }
    }

    public void proxyWakeLockByPidUid(int pid, int uid, boolean proxy) {
        Log.i(TAG, "proxyWakeLockByPidUid, pid: " + pid + ", uid: " + uid + ", proxy: " + proxy);
        if (Binder.getCallingUid() != 1000 || !this.mSystemReady) {
            Log.w(TAG, "proxyWakeLockByPidUid, system not ready!");
            return;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            this.mPms.proxyWakeLockByPidUid(pid, uid, proxy);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void forceReleaseWakeLockByPidUid(int pid, int uid) {
        Log.i(TAG, "forceReleaseWakeLockByPidUid, pid: " + pid + ", uid: " + uid);
        if (Binder.getCallingUid() != 1000 || !this.mSystemReady) {
            Log.w(TAG, "forceReleaseWakeLockByPidUid, system not ready!");
            return;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            this.mPms.forceReleaseWakeLockByPidUid(pid, uid);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void forceRestoreWakeLockByPidUid(int pid, int uid) {
        Log.i(TAG, "forceRestoreWakeLockByPidUid, pid: " + pid + ", uid: " + uid);
        if (Binder.getCallingUid() != 1000 || !this.mSystemReady) {
            Log.w(TAG, "forceRestoreWakeLockByPidUid, system not ready!");
            return;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            this.mPms.forceRestoreWakeLockByPidUid(pid, uid);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private boolean proxyWakeLock(int subType, List<String> value) {
        Log.i(TAG, "proxyWakeLock, subType: " + subType + ", value: " + value);
        if (Binder.getCallingUid() != 1000 || !this.mSystemReady) {
            Log.w(TAG, "proxyWakeLock, system not ready!");
            return false;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            return this.mPms.proxyedWakeLock(subType, value);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public boolean getWakeLockByUid(int uid, int wakeFlag) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for getWakeLockByUid ");
            return false;
        } else if (Binder.getCallingUid() == 1000) {
            return this.mPms.getWakeLockByUid(uid, wakeFlag);
        } else {
            Log.e(TAG, "getWakeLockByUid permission not allowed. uid = " + Binder.getCallingUid());
            return false;
        }
    }

    public boolean proxyAppGps(String pkg, int uid, boolean proxy) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for proxyAppGps");
            return false;
        } else if (Binder.getCallingUid() == 1000) {
            return this.mLMS.proxyGps(pkg, uid, proxy);
        } else {
            Log.e(TAG, "proxyAppGps permission not allowed. uid = " + Binder.getCallingUid());
            return false;
        }
    }

    public void refreshPackageWhitelist(int type, List<String> pkgList) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for refreshGpsWhitelist");
        } else if (Binder.getCallingUid() != 1000) {
            Log.e(TAG, "refreshGpsWhitelist permission not allowed. uid = " + Binder.getCallingUid());
        } else if (type == 2 || type == 6 || type == 7) {
            HwFrameworkFactory.getHwInnerWifiManager().refreshPackageWhitelist(type, pkgList);
        } else {
            this.mLMS.refreshPackageWhitelist(type, pkgList);
        }
    }

    public void getWakeLockBatteryStats(List<String> list) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for get wakeLock batteryStats");
        } else if (Binder.getCallingUid() != 1000) {
            Log.e(TAG, "get wakeLock batteryStats permission not allowed. uid = " + Binder.getCallingUid());
        } else {
            this.mProcStats.getWakeLockBatteryStats(list);
        }
    }

    public boolean setPgConfig(int type, int subType, List<String> value) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for pgConfig");
            return false;
        } else if (Binder.getCallingUid() != 1000) {
            Log.e(TAG, "pgConfig permission not allowed. uid = " + Binder.getCallingUid());
            return false;
        } else {
            boolean isRestrict = true;
            switch (type) {
                case 0:
                    return proxyService(subType, value);
                case 1:
                    if (subType != 1) {
                        isRestrict = false;
                    }
                    return restrictWifiScan(value, isRestrict);
                case 2:
                    return proxyWakeLock(subType, value);
                case 3:
                default:
                    return false;
                case 4:
                    return proxyJob(subType, value);
                case 5:
                    if (subType <= 0) {
                        isRestrict = false;
                    }
                    return enableQuickDoze(isRestrict);
                case 6:
                    return setGoogleUrl(value);
                case 7:
                    return enableWifichipCheck(subType);
                case 8:
                    return setAlarmExemption(subType, value);
            }
        }
    }

    private boolean setAlarmExemption(int subType, List<String> value) {
        if (this.mHms == null) {
            Log.i(TAG, "setAlarmExemption, HwAlarmManagerService is null!");
            return false;
        }
        synchronized (this.mProxyServiceList) {
            this.mHms.setAlarmExemption(value, subType);
        }
        return true;
    }

    private boolean enableWifichipCheck(int enableType) {
        IBinder binder = ServiceManager.getService("wifi");
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        if (binder != null) {
            try {
                data.writeInterfaceToken(DESCRIPTOR_IWIFIMANAGER);
                data.writeInt(enableType);
                binder.transact(CODE_ENABLE_WIFICHIP_CHECK, data, reply, 0);
                reply.readException();
                reply.recycle();
                data.recycle();
                return true;
            } catch (RemoteException e) {
                Log.e(TAG, "enable wifichip checker error", e);
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        }
        reply.recycle();
        data.recycle();
        return false;
    }

    private boolean setGoogleUrl(List<String> urlList) {
        PGGoogleServicePolicy pGGoogleServicePolicy;
        if (urlList == null || urlList.isEmpty() || (pGGoogleServicePolicy = this.mGoogleServicePolicy) == null) {
            return false;
        }
        pGGoogleServicePolicy.setGoogleUrl(urlList.get(0));
        return true;
    }

    private boolean enableQuickDoze(boolean enable) {
        DeviceIdleController.LocalService idleController = (DeviceIdleController.LocalService) LocalServices.getService(DeviceIdleController.LocalService.class);
        if (idleController == null) {
            return false;
        }
        Log.i(TAG, "enableQuickDoze: " + enable);
        idleController.enableQuickDoze(enable);
        return true;
    }

    private boolean proxyService(int subType, List<String> value) {
        Log.i(TAG, "proxyService, sub type:" + subType + ", list: " + value);
        synchronized (this.mProxyServiceList) {
            if (subType == 0) {
                this.mProxyServiceList.addAll(value);
            } else if (subType == 1) {
                this.mProxyServiceList.removeAll(value);
                this.mProxySelfServiceList.removeAll(value);
            } else if (subType == 2) {
                this.mProxyServiceList.clear();
                this.mProxySelfServiceList.clear();
            } else if (subType != 3) {
                try {
                    return false;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                this.mProxySelfServiceList.addAll(value);
            }
            return true;
        }
    }

    private boolean proxyJob(int subType, List<String> value) {
        Log.i(TAG, "proxyJob, sub type:" + subType + ", list: " + value);
        JobSchedulerInternal jobScheduler = (JobSchedulerInternal) LocalServices.getService(JobSchedulerInternal.class);
        synchronized (this.mProxyJobList) {
            if (subType == 0) {
                this.mProxyJobList.addAll(value);
            } else if (subType == 1) {
                this.mProxyJobList.removeAll(value);
            } else if (subType != 2) {
                try {
                    return false;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                this.mProxyJobList.clear();
            }
            jobScheduler.proxyService(subType, value);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        synchronized (this.mProxyServiceList) {
            fout.println("ProxyServiceList: " + this.mProxyServiceList);
        }
        synchronized (this.mProxyJobList) {
            fout.println("ProxyJobList: " + this.mProxyJobList);
        }
    }

    public void killProc(int pid) {
        if (Binder.getCallingUid() != 1000) {
            Log.e(TAG, "killProc permission not allowed. uid = " + Binder.getCallingUid());
            return;
        }
        Log.i(TAG, "killProc pid=" + pid);
        Process.killProcessQuiet(pid);
    }
}
