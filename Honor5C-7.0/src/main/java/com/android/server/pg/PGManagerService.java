package com.android.server.pg;

import android.content.Context;
import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.WorkSource;
import android.util.Log;
import com.android.server.LocalServices;
import com.android.server.LocationManagerService;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.ProcessList;
import com.android.server.power.PowerManagerService;
import com.huawei.pgmng.api.IPGManager.Stub;
import java.util.ArrayList;
import java.util.List;

public class PGManagerService extends Stub {
    private static final String TAG = "PGManagerService";
    private static final Object mLock = null;
    private static PGManagerService sInstance;
    private ActivityManagerService mAM;
    private final Context mContext;
    private LocationManagerService mLMS;
    private PowerManagerService mPms;
    private ProcBatteryStats mProcStats;
    private boolean mSystemReady;

    class LocalService extends PGManagerInternal {
        LocalService() {
        }

        public void noteStartWakeLock(String tag, WorkSource ws, String pkgName, int uid) {
            PGManagerService.this.mProcStats.processWakeLock(HdmiCecKeycode.UI_SOUND_PRESENTATION_SELECT_AUDIO_AUTO_EQUALIZER, tag, ws, pkgName, uid);
        }

        public void noteStopWakeLock(String tag, WorkSource ws, String pkgName, int uid) {
            PGManagerService.this.mProcStats.processWakeLock(161, tag, ws, pkgName, uid);
        }

        public void noteChangeWakeLock(String tag, WorkSource ws, String pkgName, int uid, String newTag, WorkSource newWs, String newPkgName, int newUid) {
            PGManagerService.this.mProcStats.processWakeLock(HdmiCecKeycode.UI_SOUND_PRESENTATION_SELECT_AUDIO_AUTO_EQUALIZER, newTag, newWs, newPkgName, newUid);
            PGManagerService.this.mProcStats.processWakeLock(161, tag, ws, pkgName, uid);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pg.PGManagerService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.pg.PGManagerService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pg.PGManagerService.<clinit>():void");
    }

    public PGManagerService(Context context) {
        this.mProcStats = null;
        this.mContext = context;
        this.mProcStats = new ProcBatteryStats(this.mContext);
        LocalServices.addService(PGManagerInternal.class, new LocalService());
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
            this.mAM = activityManagerService;
            this.mPms = powerManagerService;
            this.mLMS = location;
            this.mSystemReady = true;
            this.mProcStats.onSystemReady();
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
        } else if (ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE != Binder.getCallingUid()) {
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
        } else if (ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE != Binder.getCallingUid()) {
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
        } else if (ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE != Binder.getCallingUid()) {
            Log.e(TAG, "setProxyBCActions permission not allowed. uid = " + Binder.getCallingUid());
        } else {
            Log.i(TAG, "proxy BC Actions:" + actions);
            this.mAM.setProxyBCActions(actions);
        }
    }

    public void setActionExcludePkg(String action, String pkg) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for setActionExcludePkg action:" + action + " pkg:" + pkg);
        } else if (ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE != Binder.getCallingUid()) {
            Log.e(TAG, "setActionExcludePkg permission not allowed. uid = " + Binder.getCallingUid());
        } else {
            Log.i(TAG, "set action:" + action + " pkg:" + pkg);
            this.mAM.setActionExcludePkg(action, pkg);
        }
    }

    public void proxyBCConfig(int type, String key, List<String> value) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for proxyBCConfig type:" + type + " key:" + key + " value:" + value);
        } else if (ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE != Binder.getCallingUid()) {
            Log.e(TAG, "proxyBCConfig permission not allowed. uid = " + Binder.getCallingUid());
        } else {
            Log.i(TAG, "proxy config:" + type + " ," + key + " ," + value);
            this.mAM.proxyBCConfig(type, key, value);
        }
    }

    public void proxyWakeLockByPidUid(int pid, int uid, boolean proxy) {
        Log.i(TAG, "proxyWakeLockByPidUid, pid: " + pid + ", uid: " + uid + ", proxy: " + proxy);
        if (ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE == Binder.getCallingUid() && this.mSystemReady) {
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
        if (ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE == Binder.getCallingUid() && this.mSystemReady) {
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
        if (ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE == Binder.getCallingUid() && this.mSystemReady) {
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
        } else if (ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE == Binder.getCallingUid()) {
            return this.mPms.getWakeLockByUid(uid, wakeflag);
        } else {
            Log.e(TAG, "getWakeLockByUid permission not allowed. uid = " + Binder.getCallingUid());
            return false;
        }
    }

    public void setLcdRatio(int ratio, boolean autoAdjust) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for setLcdRatio");
        } else if (ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE != Binder.getCallingUid()) {
            Log.e(TAG, "setLcdRatio permission not allowed. uid = " + Binder.getCallingUid());
        } else {
            this.mPms.setLcdRatio(ratio, autoAdjust);
        }
    }

    public boolean proxyApp(String pkg, int uid, boolean proxy) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for proxyApp");
            return false;
        } else if (ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE == Binder.getCallingUid()) {
            return this.mLMS.proxyGps(pkg, uid, proxy);
        } else {
            Log.e(TAG, "proxyApp permission not allowed. uid = " + Binder.getCallingUid());
            return false;
        }
    }

    public void configBrightnessRange(int ratioMin, int ratioMax, int autoLimit) {
        if (!this.mSystemReady) {
            Log.w(TAG, "not ready for configBrightnessRange");
        } else if (ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE != Binder.getCallingUid()) {
            Log.e(TAG, "configBrightnessRange permission not allowed. uid = " + Binder.getCallingUid());
        } else {
            this.mPms.configBrightnessRange(ratioMin, ratioMax, autoLimit);
        }
    }
}
