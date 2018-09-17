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
import android.os.WorkSource;
import android.util.Log;
import com.android.server.LocalServices;
import com.android.server.LocationManagerService;
import com.android.server.am.ActivityManagerService;
import com.android.server.job.JobSchedulerInternal;
import com.android.server.power.PowerManagerService;
import com.huawei.pgmng.api.IPGManager.Stub;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class PGManagerService extends Stub {
    public static final int BLACK_LIST_TYPE_WIFI = 6;
    public static final int CODE_CLOSE_SOCKETS_FOR_UID = 1107;
    public static final int CONFIG_TYPE_PROXY_SERVICE = 0;
    public static final String DESCRIPTOR_NETWORKMANAGEMENT_SERVICE = "android.os.INetworkManagementService";
    public static final int DISABLE_LIST_TYPE_QUICKTTFF = 4;
    public static final int SUBTYPE_PROXY = 0;
    public static final int SUBTYPE_UNPROXY = 1;
    public static final int SUBTYPE_UNPROXY_ALL = 2;
    private static final String TAG = "PGManagerService";
    public static final int WHITE_LIST_TYPE_GPS = 1;
    public static final int WHITE_LIST_TYPE_QUICKTTFF = 3;
    public static final int WHITE_LIST_TYPE_WIFI = 2;
    public static final int WHITE_LIST_TYPE_WIFI_SLEEP = 7;
    private static final Object mLock = new Object();
    private static HashSet<String> mProxyServiceList = new HashSet();
    private static PGManagerService sInstance = null;
    private ActivityManagerService mAM;
    private final Context mContext;
    private PGGoogleServicePolicy mGoogleServicePolicy = null;
    private LocationManagerService mLMS;
    private PowerManagerService mPms;
    private ProcBatteryStats mProcStats = null;
    private boolean mSystemReady;

    class LocalService extends PGManagerInternal {
        LocalService() {
        }

        public void noteStartWakeLock(String tag, WorkSource ws, String pkgName, int uid) {
            PGManagerService.this.mProcStats.processWakeLock(160, tag, ws, pkgName, uid);
        }

        public void noteStopWakeLock(String tag, WorkSource ws, String pkgName, int uid) {
            PGManagerService.this.mProcStats.processWakeLock(161, tag, ws, pkgName, uid);
        }

        public void noteChangeWakeLock(String tag, WorkSource ws, String pkgName, int uid, String newTag, WorkSource newWs, String newPkgName, int newUid) {
            PGManagerService.this.mProcStats.processWakeLock(160, newTag, newWs, newPkgName, newUid);
            PGManagerService.this.mProcStats.processWakeLock(161, tag, ws, pkgName, uid);
        }

        public boolean isServiceProxy(ComponentName name, String sourcePkg) {
            synchronized (PGManagerService.mProxyServiceList) {
                if (PGManagerService.mProxyServiceList.size() == 0) {
                    return false;
                }
                boolean isServiceMatchList = isServiceMatchList(name, sourcePkg, PGManagerService.mProxyServiceList);
                return isServiceMatchList;
            }
        }

        public boolean isServiceMatchList(ComponentName name, String sourcePkg, Collection<String> list) {
            if (!(name == null || list == null)) {
                String pkg = name.getPackageName();
                String pkgAndCls = pkg + "/" + name.getClassName();
                String pkgAndShortCls = pkg + "/" + name.getShortClassName();
                String anyPkgAndCls = "*/" + name.getClassName();
                if (list.contains(pkg) || ((sourcePkg != null && list.contains(sourcePkg)) || list.contains(pkgAndCls) || list.contains(anyPkgAndCls) || (!pkgAndShortCls.equals(pkgAndCls) && list.contains(pkgAndShortCls)))) {
                    return true;
                }
            }
            return false;
        }

        public boolean isGmsWakeLockFilterTag(int flags, String packageName, WorkSource ws) {
            if (PGManagerService.this.mGoogleServicePolicy != null) {
                return PGManagerService.this.mGoogleServicePolicy.isGmsWakeLockFilterTag(flags, packageName, ws);
            }
            return false;
        }
    }

    public PGManagerService(Context context) {
        this.mContext = context;
        this.mProcStats = new ProcBatteryStats(this.mContext);
        LocalServices.addService(PGManagerInternal.class, new LocalService());
        this.mGoogleServicePolicy = new PGGoogleServicePolicy(this.mContext);
    }

    public static PGManagerService getInstance(Context context) {
        PGManagerService pGManagerService;
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new PGManagerService(context);
                ServiceManager.addService("pgservice", sInstance);
            }
            pGManagerService = sInstance;
        }
        return pGManagerService;
    }

    public void systemReady(ActivityManagerService activityManagerService, PowerManagerService powerManagerService, LocationManagerService location) {
        synchronized (mLock) {
            Log.i(TAG, "PGManagerService--systemReady--begain");
            this.mAM = activityManagerService;
            this.mPms = powerManagerService;
            this.mLMS = location;
            this.mSystemReady = true;
            this.mProcStats.onSystemReady();
            this.mGoogleServicePolicy.onSystemReady();
            Log.i(TAG, "PGManagerService--systemReady--end");
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (this.mProcStats.onTransact(code, data, reply, flags)) {
            return true;
        }
        return super.onTransact(code, data, reply, flags);
    }

    public long proxyBroadcast(List<String> pkgs, boolean proxy) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for proxyBroadcast:" + pkgs + " proxy:" + proxy);
            return -1;
        } else if (1000 != Binder.getCallingUid()) {
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
        } else if (1000 != Binder.getCallingUid()) {
            Log.e(TAG, "proxy broadcast permission not allowed. uid = " + Binder.getCallingUid());
            return -1;
        } else {
            Log.i(TAG, "proxyBroadcastByPid:" + pids + " proxy:" + proxy);
            List ipids = new ArrayList();
            if (pids != null) {
                for (String pid : pids) {
                    ipids.add(Integer.valueOf(Integer.parseInt(pid)));
                }
            } else {
                ipids = null;
            }
            return this.mAM.proxyBroadcastByPid(ipids, proxy);
        }
    }

    public void setProxyBCActions(List<String> actions) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for setProxyBCActions:" + actions);
        } else if (1000 != Binder.getCallingUid()) {
            Log.e(TAG, "setProxyBCActions permission not allowed. uid = " + Binder.getCallingUid());
        } else {
            Log.i(TAG, "proxy BC Actions:" + actions);
            this.mAM.setProxyBCActions(actions);
        }
    }

    public void setActionExcludePkg(String action, String pkg) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for setActionExcludePkg action:" + action + " pkg:" + pkg);
        } else if (1000 != Binder.getCallingUid()) {
            Log.e(TAG, "setActionExcludePkg permission not allowed. uid = " + Binder.getCallingUid());
        } else {
            Log.i(TAG, "set action:" + action + " pkg:" + pkg);
            this.mAM.setActionExcludePkg(action, pkg);
        }
    }

    public void proxyBCConfig(int type, String key, List<String> value) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for proxyBCConfig type:" + type + " key:" + key + " value:" + value);
        } else if (1000 != Binder.getCallingUid()) {
            Log.e(TAG, "proxyBCConfig permission not allowed. uid = " + Binder.getCallingUid());
        } else {
            Log.i(TAG, "proxy config:" + type + " ," + key + " ," + value);
            this.mAM.proxyBCConfig(type, key, value);
        }
    }

    public void proxyWakeLockByPidUid(int pid, int uid, boolean proxy) {
        Log.i(TAG, "proxyWakeLockByPidUid, pid: " + pid + ", uid: " + uid + ", proxy: " + proxy);
        if (1000 == Binder.getCallingUid() && (this.mSystemReady ^ 1) == 0) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mPms.proxyWakeLockByPidUid(pid, uid, proxy);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            Log.w(TAG, "proxyWakeLockByPidUid, system not ready!");
        }
    }

    public void forceReleaseWakeLockByPidUid(int pid, int uid) {
        Log.i(TAG, "forceReleaseWakeLockByPidUid, pid: " + pid + ", uid: " + uid);
        if (1000 == Binder.getCallingUid() && (this.mSystemReady ^ 1) == 0) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mPms.forceReleaseWakeLockByPidUid(pid, uid);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            Log.w(TAG, "forceReleaseWakeLockByPidUid, system not ready!");
        }
    }

    public void forceRestoreWakeLockByPidUid(int pid, int uid) {
        Log.i(TAG, "forceRestoreWakeLockByPidUid, pid: " + pid + ", uid: " + uid);
        if (1000 == Binder.getCallingUid() && (this.mSystemReady ^ 1) == 0) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mPms.forceRestoreWakeLockByPidUid(pid, uid);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            Log.w(TAG, "forceRestoreWakeLockByPidUid, system not ready!");
        }
    }

    public boolean getWakeLockByUid(int uid, int wakeflag) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for getWakeLockByUid ");
            return false;
        } else if (1000 == Binder.getCallingUid()) {
            return this.mPms.getWakeLockByUid(uid, wakeflag);
        } else {
            Log.e(TAG, "getWakeLockByUid permission not allowed. uid = " + Binder.getCallingUid());
            return false;
        }
    }

    public void setLcdRatio(int ratio, boolean autoAdjust) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for setLcdRatio");
        } else if (1000 != Binder.getCallingUid()) {
            Log.e(TAG, "setLcdRatio permission not allowed. uid = " + Binder.getCallingUid());
        } else {
            this.mPms.setLcdRatio(ratio, autoAdjust);
        }
    }

    public boolean proxyApp(String pkg, int uid, boolean proxy) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for proxyApp");
            return false;
        } else if (1000 == Binder.getCallingUid()) {
            return this.mLMS.proxyGps(pkg, uid, proxy);
        } else {
            Log.e(TAG, "proxyApp permission not allowed. uid = " + Binder.getCallingUid());
            return false;
        }
    }

    public void refreshPackageWhitelist(int type, List<String> pkgList) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for refreshGpsWhitelist");
        } else if (1000 != Binder.getCallingUid()) {
            Log.e(TAG, "refreshGpsWhitelist permission not allowed. uid = " + Binder.getCallingUid());
        } else {
            if (type == 2 || type == 6 || type == 7) {
                HwFrameworkFactory.getHwInnerWifiManager().refreshPackageWhitelist(type, pkgList);
            } else {
                this.mLMS.refreshPackageWhitelist(type, pkgList);
            }
        }
    }

    public void configBrightnessRange(int ratioMin, int ratioMax, int autoLimit) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for configBrightnessRange");
        } else if (1000 != Binder.getCallingUid()) {
            Log.e(TAG, "configBrightnessRange permission not allowed. uid = " + Binder.getCallingUid());
        } else {
            this.mPms.configBrightnessRange(ratioMin, ratioMax, autoLimit);
        }
    }

    public void getWlBatteryStats(List<String> list) {
        this.mProcStats.getWlBatteryStats(list);
    }

    public boolean setPgConfig(int type, int subType, List<String> value) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for pgConfig");
            return false;
        } else if (1000 != Binder.getCallingUid()) {
            Log.e(TAG, "pgConfig permission not allowed. uid = " + Binder.getCallingUid());
            return false;
        } else {
            boolean ret = false;
            switch (type) {
                case 0:
                    ret = proxyService(subType, value);
                    break;
            }
            return ret;
        }
    }

    /* JADX WARNING: Missing block: B:9:0x003c, code:
            r0.proxyService(r5, r6);
     */
    /* JADX WARNING: Missing block: B:10:0x0040, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean proxyService(int subType, List<String> value) {
        Log.i(TAG, "proxyService, type:" + subType + ", list: " + value);
        JobSchedulerInternal jobScheduler = (JobSchedulerInternal) LocalServices.getService(JobSchedulerInternal.class);
        synchronized (mProxyServiceList) {
            switch (subType) {
                case 0:
                    mProxyServiceList.addAll(value);
                    break;
                case 1:
                    mProxyServiceList.removeAll(value);
                    break;
                case 2:
                    mProxyServiceList.clear();
                    break;
                default:
                    return false;
            }
        }
    }

    public boolean closeSocketsForUid(int uid) {
        boolean ret = false;
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for close socket");
            return false;
        } else if (1000 != Binder.getCallingUid()) {
            Log.e(TAG, "close socket permission not allowed. uid = " + Binder.getCallingUid());
            return false;
        } else {
            IBinder b = ServiceManager.getService("network_management");
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();
            if (b != null) {
                try {
                    _data.writeInterfaceToken(DESCRIPTOR_NETWORKMANAGEMENT_SERVICE);
                    _data.writeInt(uid);
                    b.transact(CODE_CLOSE_SOCKETS_FOR_UID, _data, _reply, 0);
                    _reply.readException();
                    ret = true;
                } catch (RemoteException localRemoteException) {
                    Log.e(TAG, "close socket error", localRemoteException);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
            _reply.recycle();
            _data.recycle();
            return ret;
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        synchronized (mProxyServiceList) {
            fout.println("ProxyServiceList: " + mProxyServiceList);
        }
    }

    public void killProc(int pid) {
        if (1000 != Binder.getCallingUid()) {
            Log.e(TAG, "killProc permission not allowed. uid = " + Binder.getCallingUid());
            return;
        }
        Log.i(TAG, "killProc pid=" + pid);
        Process.killProcessQuiet(pid);
    }
}
