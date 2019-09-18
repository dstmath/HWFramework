package com.android.server.pm;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.util.ArraySet;
import android.util.Log;
import com.android.server.HwServiceExFactory;
import com.android.server.LocalServices;
import com.android.server.PinnerService;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.dex.DexManager;
import com.android.server.pm.dex.DexoptOptions;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BackgroundDexOptService extends JobService implements IHwBackgroundDexOptInner {
    private static final boolean BOOT_DELAYOPT = SystemProperties.getBoolean("pm.dexopt.boot.delayopt", false);
    private static final String BROADCAST_PIECE = "android.intent.action.PIECE_CLEAN";
    private static final String BROADCAST_PIECE_PERMISSION = "com.huawei.systemmanager.permission.ACCESS_INTERFACE";
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_DELAYOPT = false;
    private static final int DELAYOPT_PERIOD = 300;
    private static final long IDLE_OPTIMIZATION_PERIOD = TimeUnit.DAYS.toMillis(1);
    private static final int JOB_IDLE_OPTIMIZE = 800;
    private static final int JOB_POST_BOOT_UPDATE = 801;
    private static final int JOB_POST_BOOT_UPDATE_DELAYOPT = 8001;
    private static final int LOW_THRESHOLD_MULTIPLIER_FOR_DOWNGRADE = 2;
    private static final int OPTIMIZE_ABORT_BY_JOB_SCHEDULER = 2;
    private static final int OPTIMIZE_ABORT_NO_SPACE_LEFT = 3;
    private static final int OPTIMIZE_CONTINUE = 1;
    private static final int OPTIMIZE_PROCESSED = 0;
    private static final String TAG = "BackgroundDexOptService";
    /* access modifiers changed from: private */
    public static long mBoot_Delayopt_Begin = 0;
    /* access modifiers changed from: private */
    public static AtomicBoolean mBoot_Delayopt_Power = new AtomicBoolean(false);
    /* access modifiers changed from: private */
    public static AtomicBoolean mBoot_Delayopt_Screen = new AtomicBoolean(false);
    private static final long mDowngradeUnusedAppsThresholdInMillis = getDowngradeUnusedAppsThresholdInMillis();
    private static ComponentName sDexoptServiceName = new ComponentName(PackageManagerService.PLATFORM_PACKAGE_NAME, BackgroundDexOptService.class.getName());
    static final ArraySet<String> sFailedPackageNamesPrimary = new ArraySet<>();
    static final ArraySet<String> sFailedPackageNamesSecondary = new ArraySet<>();
    private AtomicBoolean is_BroadcastReceiver_Dexopt = new AtomicBoolean(false);
    private final AtomicBoolean mAbortIdleOptimization = new AtomicBoolean(false);
    /* access modifiers changed from: private */
    public final AtomicBoolean mAbortPostBootUpdate = new AtomicBoolean(false);
    private BroadcastReceiverDexopt mBroadcastReceiver_Dexopt = new BroadcastReceiverDexopt();
    private final File mDataDir = Environment.getDataDirectory();
    private final AtomicBoolean mExitPostBootUpdate = new AtomicBoolean(false);
    IHwBackgroundDexOptServiceEx mHwBDOSEx = null;
    /* access modifiers changed from: private */
    public ThreadDexopt mThread_Dexopt = new ThreadDexopt();

    private class BroadcastReceiverDexopt extends BroadcastReceiver {
        private BroadcastReceiverDexopt() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:22:0x004d  */
        /* JADX WARNING: Removed duplicated region for block: B:23:0x005f  */
        /* JADX WARNING: Removed duplicated region for block: B:28:0x0096  */
        /* JADX WARNING: Removed duplicated region for block: B:29:0x00a7  */
        /* JADX WARNING: Removed duplicated region for block: B:42:? A[RETURN, SYNTHETIC] */
        public void onReceive(Context context, Intent intent) {
            char c;
            String action = intent.getAction();
            int hashCode = action.hashCode();
            if (hashCode == -2128145023) {
                if (action.equals("android.intent.action.SCREEN_OFF")) {
                    c = 0;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                    }
                }
            } else if (hashCode == -1886648615) {
                if (action.equals("android.intent.action.ACTION_POWER_DISCONNECTED")) {
                    c = 3;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                    }
                }
            } else if (hashCode == -1454123155) {
                if (action.equals("android.intent.action.SCREEN_ON")) {
                    c = 1;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                    }
                }
            } else if (hashCode == 1019184907 && action.equals("android.intent.action.ACTION_POWER_CONNECTED")) {
                c = 2;
                switch (c) {
                    case 0:
                        BackgroundDexOptService.mBoot_Delayopt_Screen.set(true);
                        if (BackgroundDexOptService.mBoot_Delayopt_Power.get()) {
                            long unused = BackgroundDexOptService.mBoot_Delayopt_Begin = System.currentTimeMillis();
                            BackgroundDexOptService.this.mAbortPostBootUpdate.set(false);
                            if (!BackgroundDexOptService.this.mThread_Dexopt.isAlive()) {
                                BackgroundDexOptService.this.mThread_Dexopt.start();
                                return;
                            }
                            return;
                        }
                        return;
                    case 1:
                        BackgroundDexOptService.mBoot_Delayopt_Screen.set(false);
                        BackgroundDexOptService.this.mAbortPostBootUpdate.set(true);
                        return;
                    case 2:
                        BackgroundDexOptService.mBoot_Delayopt_Power.set(true);
                        if (BackgroundDexOptService.mBoot_Delayopt_Screen.get()) {
                            long unused2 = BackgroundDexOptService.mBoot_Delayopt_Begin = System.currentTimeMillis();
                            BackgroundDexOptService.this.mAbortPostBootUpdate.set(false);
                            if (!BackgroundDexOptService.this.mThread_Dexopt.isAlive()) {
                                BackgroundDexOptService.this.mThread_Dexopt.start();
                                return;
                            }
                            return;
                        }
                        return;
                    case 3:
                        BackgroundDexOptService.mBoot_Delayopt_Power.set(false);
                        BackgroundDexOptService.this.mAbortPostBootUpdate.set(true);
                        return;
                    default:
                        return;
                }
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
            }
        }
    }

    private class ThreadDexopt extends Thread {
        AtomicBoolean alive;
        JobParameters params;
        long period;

        private ThreadDexopt() {
            this.alive = new AtomicBoolean(false);
        }

        public void run() {
            this.period = 300;
            while (this.alive.get()) {
                try {
                    Thread.sleep(this.period * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!this.alive.get()) {
                    break;
                }
                long end = System.currentTimeMillis();
                if (BackgroundDexOptService.mBoot_Delayopt_Screen.get() && BackgroundDexOptService.mBoot_Delayopt_Power.get()) {
                    if (300 <= ((end - BackgroundDexOptService.mBoot_Delayopt_Begin) / 1000) + 1) {
                        PackageManagerService pm = (PackageManagerService) ServiceManager.getService("package");
                        if (pm.isStorageLow()) {
                            BackgroundDexOptService.this.jobFinished(this.params, false);
                            BackgroundDexOptService.this.unregisterReceiverDexopt();
                            return;
                        }
                        ArraySet<String> pkgs = pm.getOptimizablePackages();
                        if (pkgs.isEmpty()) {
                            BackgroundDexOptService.this.jobFinished(this.params, false);
                            BackgroundDexOptService.this.unregisterReceiverDexopt();
                            return;
                        }
                        if (!BackgroundDexOptService.this.runPostBootUpdate(this.params, pm, pkgs)) {
                            BackgroundDexOptService.this.jobFinished(this.params, false);
                            BackgroundDexOptService.this.unregisterReceiverDexopt();
                        }
                        return;
                    }
                    this.period = (300 - ((end - BackgroundDexOptService.mBoot_Delayopt_Begin) / 1000)) + 1;
                } else {
                    return;
                }
            }
        }
    }

    public static void schedule(Context context) {
        if (!isBackgroundDexoptDisabled()) {
            JobScheduler js = (JobScheduler) context.getSystemService("jobscheduler");
            if (!BOOT_DELAYOPT) {
                js.schedule(new JobInfo.Builder(JOB_POST_BOOT_UPDATE, sDexoptServiceName).setMinimumLatency(TimeUnit.MINUTES.toMillis(1)).setOverrideDeadline(TimeUnit.MINUTES.toMillis(1)).build());
            } else {
                js.schedule(new JobInfo.Builder(JOB_POST_BOOT_UPDATE_DELAYOPT, sDexoptServiceName).setMinimumLatency(TimeUnit.MINUTES.toMillis(1)).setOverrideDeadline(TimeUnit.MINUTES.toMillis(1)).build());
                js.schedule(new JobInfo.Builder(JOB_POST_BOOT_UPDATE, sDexoptServiceName).setMinimumLatency(TimeUnit.HOURS.toMillis(12)).setOverrideDeadline(TimeUnit.DAYS.toMillis(1)).build());
            }
            if (BatteryManager.HW_BATTERY_LEV_JOB_ALLOWED > 0) {
                js.schedule(new JobInfo.Builder(JOB_IDLE_OPTIMIZE, sDexoptServiceName).setRequiresDeviceIdle(true).setRequiresCharging(true).setHwRequiresBatteryLevJobAllowed(true).setPeriodic(IDLE_OPTIMIZATION_PERIOD).build());
            } else {
                js.schedule(new JobInfo.Builder(JOB_IDLE_OPTIMIZE, sDexoptServiceName).setRequiresDeviceIdle(true).setRequiresCharging(true).setPeriodic(IDLE_OPTIMIZATION_PERIOD).build());
            }
        }
    }

    public static void notifyPackageChanged(String packageName) {
        synchronized (sFailedPackageNamesPrimary) {
            sFailedPackageNamesPrimary.remove(packageName);
        }
        synchronized (sFailedPackageNamesSecondary) {
            sFailedPackageNamesSecondary.remove(packageName);
        }
    }

    private int getBatteryLevel() {
        Intent intent = registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        int level = intent.getIntExtra("level", -1);
        int scale = intent.getIntExtra("scale", -1);
        if (!intent.getBooleanExtra("present", true)) {
            return 100;
        }
        if (level < 0 || scale <= 0) {
            return 0;
        }
        return (100 * level) / scale;
    }

    private long getLowStorageThreshold(Context context) {
        long lowThreshold = StorageManager.from(context).getStorageLowBytes(this.mDataDir);
        if (lowThreshold == 0) {
            Log.e(TAG, "Invalid low storage threshold");
        }
        return lowThreshold;
    }

    /* access modifiers changed from: private */
    public boolean runPostBootUpdate(JobParameters jobParams, PackageManagerService pm, ArraySet<String> pkgs) {
        if (this.mExitPostBootUpdate.get()) {
            return false;
        }
        final ArraySet<String> arraySet = pkgs;
        final PackageManagerService packageManagerService = pm;
        final JobParameters jobParameters = jobParams;
        AnonymousClass1 r1 = new Thread("BackgroundDexOptService_PostBootUpdate") {
            public void run() {
                Log.i(BackgroundDexOptService.TAG, "Performing postBootUpdate(" + arraySet.size() + ") ...");
                if (PackageManagerService.sIsMygote) {
                    MplDexOptAdaptor.getInstance().getDexOptNeededCachePrepare(packageManagerService.getOptimizablePkgList(), 1, false, packageManagerService.getDexManager(), packageManagerService, 1);
                }
                BackgroundDexOptService.this.postBootUpdate(jobParameters, packageManagerService, arraySet);
                boolean isUpgradeDoFstrim = SystemProperties.getBoolean("ro.config.upgrade_clean_notify", false);
                if (packageManagerService.isUpgrade() && isUpgradeDoFstrim) {
                    Log.i(BackgroundDexOptService.TAG, "Start piece clean.");
                    BackgroundDexOptService.this.broadcastPieceClean(BackgroundDexOptService.this);
                }
                if (PackageManagerService.sIsMygote) {
                    MplDexOptAdaptor.getInstance().getDexOptNeededCacheClear();
                }
                Log.i(BackgroundDexOptService.TAG, "Performing postBootUpdate finished!");
            }
        };
        r1.start();
        return true;
    }

    /* access modifiers changed from: private */
    public void postBootUpdate(JobParameters jobParams, PackageManagerService pm, ArraySet<String> pkgs) {
        int lowBatteryThreshold = getResources().getInteger(17694805);
        long lowThreshold = getLowStorageThreshold(this);
        if (jobParams.getJobId() != JOB_POST_BOOT_UPDATE_DELAYOPT) {
            this.mAbortPostBootUpdate.set(false);
        }
        ArraySet<String> updatedPackages = new ArraySet<>();
        Iterator<String> it = pkgs.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            String pkg = it.next();
            if (!this.mAbortPostBootUpdate.get()) {
                if (this.mExitPostBootUpdate.get() || getBatteryLevel() < lowBatteryThreshold) {
                    break;
                }
                long usableSpace = this.mDataDir.getUsableSpace();
                if (usableSpace < lowThreshold) {
                    Log.w(TAG, "Aborting background dex opt job due to low storage: " + usableSpace);
                    break;
                }
                if (pm.performDexOptWithStatus(new DexoptOptions(pkg, 1, 4)) == 1) {
                    updatedPackages.add(pkg);
                }
            } else {
                return;
            }
        }
        PackageManagerService packageManagerService = pm;
        notifyPinService(updatedPackages);
        JobParameters jobParameters = jobParams;
        jobFinished(jobParameters, false);
        if (jobParameters.getJobId() == JOB_POST_BOOT_UPDATE_DELAYOPT) {
            unregisterReceiverDexopt();
        }
    }

    private boolean runIdleOptimization(JobParameters jobParams, PackageManagerService pm, ArraySet<String> pkgs) {
        final PackageManagerService packageManagerService = pm;
        final ArraySet<String> arraySet = pkgs;
        final JobParameters jobParameters = jobParams;
        AnonymousClass2 r0 = new Thread("BackgroundDexOptService_IdleOptimization") {
            public void run() {
                if (BackgroundDexOptService.this.idleOptimization(packageManagerService, arraySet, BackgroundDexOptService.this) != 2) {
                    Log.w(BackgroundDexOptService.TAG, "Idle optimizations aborted because of space constraints.");
                    BackgroundDexOptService.this.jobFinished(jobParameters, false);
                }
                Log.i(BackgroundDexOptService.TAG, "Performing idle optimizations finished!");
            }
        };
        r0.start();
        return true;
    }

    /* access modifiers changed from: private */
    public int idleOptimization(PackageManagerService pm, ArraySet<String> pkgs, Context context) {
        Log.i(TAG, "Performing idle optimizations");
        this.mExitPostBootUpdate.set(true);
        this.mAbortIdleOptimization.set(false);
        if (this.mHwBDOSEx == null) {
            this.mHwBDOSEx = HwServiceExFactory.getHwBackgroundDexOptServiceEx(this, context);
        }
        long lowStorageThreshold = getLowStorageThreshold(context);
        int result = optimizePackages(pm, pkgs, lowStorageThreshold, true, sFailedPackageNamesPrimary);
        if (result == 2) {
            return result;
        }
        if (SystemProperties.getBoolean("dalvik.vm.dexopt.secondary", false)) {
            int result2 = reconcileSecondaryDexFiles(pm.getDexManager());
            if (result2 == 2) {
                return result2;
            }
            result = optimizePackages(pm, pkgs, lowStorageThreshold, false, sFailedPackageNamesSecondary);
        }
        return result;
    }

    private int optimizePackages(PackageManagerService pm, ArraySet<String> pkgs, long lowStorageThreshold, boolean is_for_primary_dex, ArraySet<String> failedPackageNames) {
        boolean downgrade;
        int reason;
        Iterator<String> it;
        boolean success;
        PackageManagerService packageManagerService = pm;
        long j = lowStorageThreshold;
        ArraySet<String> arraySet = failedPackageNames;
        ArraySet arraySet2 = new ArraySet();
        Set<String> unusedPackages = packageManagerService.getUnusedPackages(mDowngradeUnusedAppsThresholdInMillis);
        boolean shouldDowngrade = shouldDowngrade(2 * j);
        Iterator<String> it2 = pkgs.iterator();
        while (it2.hasNext()) {
            String pkg = it2.next();
            int abort_code = abortIdleOptimizations(j);
            if (abort_code == 2) {
                return abort_code;
            }
            synchronized (failedPackageNames) {
                if (!arraySet.contains(pkg)) {
                    if (!unusedPackages.contains(pkg) || !shouldDowngrade) {
                        if (abort_code != 3) {
                            reason = 3;
                            downgrade = false;
                        } else {
                            continue;
                        }
                    } else if (!is_for_primary_dex || packageManagerService.canHaveOatDir(pkg)) {
                        reason = 5;
                        downgrade = true;
                    } else {
                        packageManagerService.deleteOatArtifactsOfPackage(pkg);
                    }
                    synchronized (failedPackageNames) {
                        arraySet.add(pkg);
                    }
                    if (this.mHwBDOSEx != null) {
                        it = it2;
                        reason = this.mHwBDOSEx.getReason(reason, 3, 7, pkg);
                    } else {
                        it = it2;
                    }
                    int dexoptFlags = 5 | (downgrade ? 32 : 0) | 512;
                    if (is_for_primary_dex) {
                        int result = packageManagerService.performDexOptWithStatus(new DexoptOptions(pkg, reason, dexoptFlags));
                        success = result != -1;
                        if (result == 1) {
                            arraySet2.add(pkg);
                        }
                    } else {
                        success = packageManagerService.performDexOpt(new DexoptOptions(pkg, reason, dexoptFlags | 8));
                    }
                    if (success) {
                        synchronized (failedPackageNames) {
                            arraySet.remove(pkg);
                        }
                    }
                    it2 = it;
                    j = lowStorageThreshold;
                }
            }
        }
        notifyPinService(arraySet2);
        return 0;
    }

    private int reconcileSecondaryDexFiles(DexManager dm) {
        for (String p : dm.getAllPackagesWithSecondaryDexFiles()) {
            if (this.mAbortIdleOptimization.get()) {
                return 2;
            }
            dm.reconcileSecondaryDexFiles(p);
        }
        return 0;
    }

    private int abortIdleOptimizations(long lowStorageThreshold) {
        if (this.mAbortIdleOptimization.get()) {
            return 2;
        }
        long usableSpace = this.mDataDir.getUsableSpace();
        if (usableSpace >= lowStorageThreshold) {
            return 1;
        }
        Log.w(TAG, "Aborting background dex opt job due to low storage: " + usableSpace);
        return 3;
    }

    private boolean shouldDowngrade(long lowStorageThresholdForDowngrade) {
        if (this.mDataDir.getUsableSpace() < lowStorageThresholdForDowngrade) {
            return true;
        }
        return false;
    }

    public static boolean runIdleOptimizationsNow(PackageManagerService pm, Context context, List<String> packageNames) {
        ArraySet<String> packagesToOptimize;
        BackgroundDexOptService bdos = new BackgroundDexOptService();
        if (packageNames == null) {
            packagesToOptimize = pm.getOptimizablePackages();
        } else {
            packagesToOptimize = new ArraySet<>(packageNames);
        }
        return bdos.idleOptimization(pm, packagesToOptimize, context) == 0;
    }

    public boolean onStartJob(JobParameters params) {
        boolean result;
        PackageManagerService pm = (PackageManagerService) ServiceManager.getService("package");
        boolean z = false;
        if (pm.isStorageLow()) {
            return false;
        }
        ArraySet<String> pkgs = pm.getOptimizablePackages();
        if (pkgs.isEmpty()) {
            return false;
        }
        if (params.getJobId() == JOB_POST_BOOT_UPDATE) {
            if (BOOT_DELAYOPT) {
                this.mAbortPostBootUpdate.set(true);
                unregisterReceiverDexopt();
                this.mThread_Dexopt.alive.set(false);
            }
            result = runPostBootUpdate(params, pm, pkgs);
        } else if (params.getJobId() == JOB_POST_BOOT_UPDATE_DELAYOPT) {
            mBoot_Delayopt_Screen.set(!((PowerManager) getSystemService("power")).isScreenOn());
            Intent bm = registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
            AtomicBoolean atomicBoolean = mBoot_Delayopt_Power;
            if (2 == bm.getIntExtra("status", -1) || 5 == bm.getIntExtra("status", -1)) {
                z = true;
            }
            atomicBoolean.set(z);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            intentFilter.addAction("android.intent.action.SCREEN_ON");
            intentFilter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
            intentFilter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
            registerReceiverDexopt(intentFilter);
            this.mThread_Dexopt.params = params;
            this.mThread_Dexopt.alive.set(true);
            if (!this.mThread_Dexopt.isAlive() && mBoot_Delayopt_Screen.get() && mBoot_Delayopt_Power.get()) {
                mBoot_Delayopt_Begin = System.currentTimeMillis();
                this.mThread_Dexopt.start();
            }
            result = true;
        } else {
            result = runIdleOptimization(params, pm, pkgs);
        }
        return result;
    }

    public boolean onStopJob(JobParameters params) {
        if (params.getJobId() == JOB_POST_BOOT_UPDATE) {
            this.mAbortPostBootUpdate.set(true);
            return false;
        } else if (params.getJobId() == JOB_POST_BOOT_UPDATE_DELAYOPT) {
            this.mAbortPostBootUpdate.set(true);
            unregisterReceiverDexopt();
            this.mThread_Dexopt.alive.set(false);
            return true;
        } else {
            this.mAbortIdleOptimization.set(true);
            return true;
        }
    }

    private void notifyPinService(ArraySet<String> updatedPackages) {
        PinnerService pinnerService = (PinnerService) LocalServices.getService(PinnerService.class);
        if (pinnerService != null) {
            Log.i(TAG, "Pinning optimized code " + updatedPackages);
            pinnerService.update(updatedPackages);
        }
    }

    /* access modifiers changed from: private */
    public void broadcastPieceClean(Context context) {
        context.sendBroadcast(new Intent(BROADCAST_PIECE), BROADCAST_PIECE_PERMISSION);
    }

    private static long getDowngradeUnusedAppsThresholdInMillis() {
        String sysPropValue = SystemProperties.get("pm.dexopt.downgrade_after_inactive_days");
        if (sysPropValue != null && !sysPropValue.isEmpty()) {
            return TimeUnit.DAYS.toMillis(Long.parseLong(sysPropValue));
        }
        Log.w(TAG, "SysProp pm.dexopt.downgrade_after_inactive_days not set");
        return JobStatus.NO_LATEST_RUNTIME;
    }

    private static boolean isBackgroundDexoptDisabled() {
        return SystemProperties.getBoolean("pm.dexopt.disable_bg_dexopt", false);
    }

    private synchronized void registerReceiverDexopt(IntentFilter intentFilter) {
        if (!this.is_BroadcastReceiver_Dexopt.get()) {
            registerReceiver(this.mBroadcastReceiver_Dexopt, intentFilter);
            this.is_BroadcastReceiver_Dexopt.set(true);
        }
    }

    /* access modifiers changed from: private */
    public synchronized void unregisterReceiverDexopt() {
        if (this.is_BroadcastReceiver_Dexopt.get()) {
            unregisterReceiver(this.mBroadcastReceiver_Dexopt);
            this.is_BroadcastReceiver_Dexopt.set(false);
        }
    }
}
