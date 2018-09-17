package com.android.server.pfw.policy;

import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.BatteryStats.Uid;
import android.os.BatteryStats.Uid.Proc;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.util.SparseArray;
import com.android.internal.os.BatteryStatsHelper;
import com.android.internal.os.BatteryStatsImpl;
import com.android.server.pfw.HwPFWService;
import com.android.server.pfw.log.HwPFWLogger;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwPFWAppRestartPolicy extends HwPFWPolicy {
    private static final long CHECK_ONE_HOUR_TIME = 3600000;
    private static final long CHECK_TOTAL_LAUNCH_TIME = 14400000;
    private static final int MAX_PROC_LAUNCH_TIMES = 10;
    private static final int MAX_PROC_TOTAL_LAUNCH_TIMES = 30;
    private static final String TAG = "PFW.HwPFWAppRestartPolicy";
    private static AtomicBoolean exists;
    private long mAppRestartInitTime0;
    private long mAppRestartInitTime1;
    private Context mContext;
    private boolean mEnabled;
    private boolean mIsScrOff;
    private Map<String, Integer> mLastHrProcLaunchMap;
    private Map<String, Integer> mLastProcLaunchMap;
    private Handler mProcCalcHandler;
    private Runnable mProcLaunch;
    private Map<String, Integer> mProcLaunchMap;
    private HwPFWService mService;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pfw.policy.HwPFWAppRestartPolicy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.pfw.policy.HwPFWAppRestartPolicy.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pfw.policy.HwPFWAppRestartPolicy.<clinit>():void");
    }

    public HwPFWAppRestartPolicy(Context context, HwPFWService service) {
        super(context);
        this.mAppRestartInitTime0 = 0;
        this.mAppRestartInitTime1 = 0;
        this.mLastProcLaunchMap = new HashMap();
        this.mLastHrProcLaunchMap = new HashMap();
        this.mProcLaunchMap = new HashMap();
        this.mEnabled = true;
        this.mIsScrOff = false;
        this.mProcLaunch = new Runnable() {
            public void run() {
                HwPFWLogger.d(HwPFWAppRestartPolicy.TAG, "process launch counts runnable");
                new Thread() {
                    public void run() {
                        HwPFWAppRestartPolicy.this.calcProcLaunchFlg(false);
                    }
                }.start();
            }
        };
        this.mContext = context;
        this.mService = service;
        this.mProcCalcHandler = new Handler();
    }

    public void handleBroadcastIntent(Intent intent) {
        if (this.mEnabled) {
            String action = intent.getAction();
            if (action != null) {
                HwPFWLogger.d(TAG, "receive action = " + action);
                if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                    calcProcLaunchFlg(true);
                    this.mAppRestartInitTime0 = SystemClock.elapsedRealtime();
                    this.mAppRestartInitTime1 = this.mAppRestartInitTime0;
                } else if (action.equals("android.intent.action.SCREEN_ON")) {
                    this.mIsScrOff = false;
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    this.mIsScrOff = true;
                } else if (action.equals(HwPFWService.ACTION_PFW_WAKEUP_TIMER)) {
                    this.mProcCalcHandler.postDelayed(this.mProcLaunch, 0);
                } else if (action.equals("android.intent.action.ACTION_POWER_DISCONNECTED") && this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED")).getIntExtra(MemoryConstant.MEM_FILECACHE_ITEM_LEVEL, 0) >= 90) {
                    synchronized (this.mLastHrProcLaunchMap) {
                        this.mLastHrProcLaunchMap.clear();
                        HwPFWLogger.d(TAG, "clear mLastHrProcLaunchMap");
                    }
                    synchronized (this.mLastProcLaunchMap) {
                        this.mLastProcLaunchMap.clear();
                        HwPFWLogger.d(TAG, "clear mLastProcLaunchMap");
                    }
                }
            }
        }
    }

    private void cloneProcHashMap(Map<String, Integer> src, Map<String, Integer> dst) {
        dst.clear();
        for (Entry<String, Integer> ent : src.entrySet()) {
            dst.put((String) ent.getKey(), (Integer) ent.getValue());
        }
    }

    private String getCurDefaultIms() {
        String inputMethod = Secure.getString(this.mContext.getContentResolver(), "default_input_method");
        if (inputMethod == null) {
            return null;
        }
        String[] defaultIms = inputMethod.split("/");
        HwPFWLogger.d(TAG, "curDefault IMs = " + defaultIms[0]);
        return defaultIms[0];
    }

    private void calcProcLaunchFlg(boolean isFstTimes) {
        if (exists.compareAndSet(false, true)) {
            long now = SystemClock.elapsedRealtime();
            getProcLaunches(0);
            if (isFstTimes) {
                synchronized (this.mLastProcLaunchMap) {
                    cloneProcHashMap(this.mProcLaunchMap, this.mLastProcLaunchMap);
                }
                synchronized (this.mLastHrProcLaunchMap) {
                    cloneProcHashMap(this.mProcLaunchMap, this.mLastHrProcLaunchMap);
                }
                exists.set(false);
                return;
            }
            String ims = getCurDefaultIms();
            for (Entry<String, Integer> ent : this.mProcLaunchMap.entrySet()) {
                String proc = (String) ent.getKey();
                if (proc != null) {
                    boolean isUnprotectedApp;
                    int count = 0;
                    if (ent.getValue() != null) {
                        count = ((Integer) ent.getValue()).intValue();
                    }
                    int preCount = 0;
                    synchronized (this.mLastProcLaunchMap) {
                        if (this.mLastProcLaunchMap.get(proc) != null) {
                            preCount = ((Integer) this.mLastProcLaunchMap.get(proc)).intValue();
                        }
                    }
                    int preHrCount = 0;
                    synchronized (this.mLastHrProcLaunchMap) {
                        if (this.mLastHrProcLaunchMap.get(proc) != null) {
                            preHrCount = ((Integer) this.mLastHrProcLaunchMap.get(proc)).intValue();
                        }
                    }
                    String pacName = proc.split(":")[0];
                    ApplicationInfo ai = null;
                    try {
                        ai = this.mContext.getPackageManager().getApplicationInfo(pacName, 0);
                    } catch (Exception e) {
                        HwPFWLogger.d(TAG, "failed to get application info");
                    }
                    if (ai != null) {
                        if ((ai.flags & 1) != 0 && ai.hwFlags == 0) {
                        }
                    }
                    synchronized (this.mService.getStopPkgListLock()) {
                        isUnprotectedApp = this.mService.getStopPkgList().contains(pacName);
                    }
                    boolean isNeedProcess = false;
                    if (!isUnprotectedApp && count - preCount >= MAX_PROC_TOTAL_LAUNCH_TIMES) {
                        isNeedProcess = true;
                    } else if (isUnprotectedApp && count - preCount >= MAX_PROC_LAUNCH_TIMES) {
                        isNeedProcess = true;
                    } else if (isUnprotectedApp && count - preHrCount >= MAX_PROC_TOTAL_LAUNCH_TIMES) {
                        isNeedProcess = true;
                        if (this.mIsScrOff && ims != null && ims.equals(pacName)) {
                            this.mService.getActivityManager().killBackgroundProcesses(ims);
                        }
                    }
                    HwPFWLogger.d(TAG, "proc: " + proc + " launch " + count + "times, lastTimes = " + preCount);
                    if (isNeedProcess) {
                        String pac = getActivityRunTopPac();
                        if ((pac == null || !pac.equals(pacName)) && (ims == null || !ims.equals(pacName))) {
                            HwPFWLogger.d(TAG, pacName + " is clean and added to forbid restart list");
                            this.mService.addForbidRestartApp(pacName);
                            this.mService.getActivityManager().forceStopPackage(pacName);
                        }
                    }
                }
            }
            if (now - this.mAppRestartInitTime1 >= 30) {
                synchronized (this.mLastHrProcLaunchMap) {
                    cloneProcHashMap(this.mProcLaunchMap, this.mLastHrProcLaunchMap);
                }
                this.mAppRestartInitTime1 = now;
            }
            if (now - this.mAppRestartInitTime0 >= CHECK_TOTAL_LAUNCH_TIME) {
                this.mService.clearAllForbidRestartApp();
                this.mAppRestartInitTime0 = now;
            }
            synchronized (this.mLastProcLaunchMap) {
                cloneProcHashMap(this.mProcLaunchMap, this.mLastProcLaunchMap);
            }
            exists.set(false);
        }
    }

    private String getActivityRunTopPac() {
        String pacName = null;
        try {
            ComponentName cn = ((RunningTaskInfo) this.mService.getActivityManager().getRunningTasks(1).get(0)).topActivity;
            if (cn != null) {
                pacName = cn.getPackageName();
            }
        } catch (Exception ex) {
            HwPFWLogger.e(TAG, "getActivityRunTopPac catch Exception :" + ex.getMessage());
        }
        return pacName;
    }

    private void getProcLaunches(int which) {
        synchronized (this.mService.getBatteryLockObject()) {
            BatteryStatsHelper bsh = this.mService.getBatteryStatsHelper();
            if (bsh == null) {
                return;
            }
            bsh.clearStats();
            BatteryStatsImpl stats = (BatteryStatsImpl) bsh.getStats();
            if (stats == null) {
                return;
            }
            SparseArray<? extends Uid> uidStats = stats.getUidStats();
            int NU = uidStats.size();
            this.mProcLaunchMap.clear();
            for (int iu = 0; iu < NU; iu++) {
                Uid u = (Uid) uidStats.valueAt(iu);
                if (u.getUid() >= LifeCycleStateMachine.TIME_OUT_TIME) {
                    Map<String, ? extends Proc> processStats = u.getProcessStats();
                    if (processStats.size() > 0) {
                        for (Entry<String, ? extends Proc> ent : processStats.entrySet()) {
                            String procName = (String) ent.getKey();
                            int starts = ((Proc) ent.getValue()).getStarts(which);
                            if (starts > 0) {
                                this.mProcLaunchMap.put(procName, Integer.valueOf(starts));
                            }
                        }
                    }
                }
            }
        }
    }
}
