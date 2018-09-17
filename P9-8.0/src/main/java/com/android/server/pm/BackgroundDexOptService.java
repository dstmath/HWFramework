package com.android.server.pm;

import android.app.job.JobInfo.Builder;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import com.android.server.LocalServices;
import com.android.server.PinnerService;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.pm.dex.DexManager;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class BackgroundDexOptService extends JobService {
    private static final String ATTR_NAME = "name";
    private static final String BROADCAST_PIECE = "android.intent.action.PIECE_CLEAN";
    private static final String BROADCAST_PIECE_PERMISSION = "com.huawei.systemmanager.permission.ACCESS_INTERFACE";
    private static final String CUST_FILE_PATH = "/xml/hw_aot_compile_apps_config.xml";
    private static final boolean DEBUG = false;
    private static final long IDLE_OPTIMIZATION_PERIOD = TimeUnit.DAYS.toMillis(1);
    private static final int JOB_IDLE_OPTIMIZE = 800;
    private static final int JOB_POST_BOOT_UPDATE = 801;
    private static final int OPTIMIZE_ABORT_BY_JOB_SCHEDULER = 2;
    private static final int OPTIMIZE_ABORT_NO_SPACE_LEFT = 3;
    private static final int OPTIMIZE_CONTINUE = 1;
    private static final int OPTIMIZE_PROCESSED = 0;
    private static final long SHORT_IDLE_OPTIMIZATION_PERIOD = TimeUnit.HOURS.toMillis(1);
    private static final String TAG = "BackgroundDexOptService";
    private static final String TAG_NAME = "speed";
    private static ComponentName sDexoptServiceName = new ComponentName("android", BackgroundDexOptService.class.getName());
    static final ArraySet<String> sFailedPackageNamesPrimary = new ArraySet();
    static final ArraySet<String> sFailedPackageNamesSecondary = new ArraySet();
    private static Builder sIdleOptJobInfoBuilder = new Builder(JOB_IDLE_OPTIMIZE, sDexoptServiceName).setRequiresDeviceIdle(true).setRequiresCharging(true).setPeriodic(IDLE_OPTIMIZATION_PERIOD);
    private static long sIdleOptPeriod = IDLE_OPTIMIZATION_PERIOD;
    private static Object sLock = new Object();
    private final AtomicBoolean mAbortIdleOptimization = new AtomicBoolean(false);
    private final AtomicBoolean mAbortPostBootUpdate = new AtomicBoolean(false);
    private final File mDataDir = Environment.getDataDirectory();
    private final AtomicBoolean mExitPostBootUpdate = new AtomicBoolean(false);
    private final AtomicBoolean mIdleJobExcuting = new AtomicBoolean(false);

    public static void schedule(Context context) {
        JobScheduler js = (JobScheduler) context.getSystemService("jobscheduler");
        js.schedule(new Builder(JOB_POST_BOOT_UPDATE, sDexoptServiceName).setMinimumLatency(TimeUnit.MINUTES.toMillis(1)).setOverrideDeadline(TimeUnit.MINUTES.toMillis(1)).build());
        js.schedule(sIdleOptJobInfoBuilder.build());
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
        if (level < 0 || scale <= 0) {
            return 0;
        }
        return (level * 100) / scale;
    }

    private long getLowStorageThreshold(Context context) {
        long lowThreshold = StorageManager.from(context).getStorageLowBytes(this.mDataDir);
        if (lowThreshold == 0) {
            Log.e(TAG, "Invalid low storage threshold");
        }
        return lowThreshold;
    }

    private boolean runPostBootUpdate(JobParameters jobParams, PackageManagerService pm, ArraySet<String> pkgs) {
        if (this.mExitPostBootUpdate.get()) {
            return false;
        }
        final JobParameters jobParameters = jobParams;
        final PackageManagerService packageManagerService = pm;
        final ArraySet<String> arraySet = pkgs;
        new Thread("BackgroundDexOptService_PostBootUpdate") {
            public void run() {
                BackgroundDexOptService.this.postBootUpdate(jobParameters, packageManagerService, arraySet);
                boolean isUpgradeDoFstrim = SystemProperties.getBoolean("ro.config.upgrade_clean_notify", false);
                if (packageManagerService.isUpgrade() && isUpgradeDoFstrim) {
                    Log.i(BackgroundDexOptService.TAG, "Start piece clean.");
                    BackgroundDexOptService.this.broadcastPieceClean(BackgroundDexOptService.this);
                }
            }
        }.start();
        return true;
    }

    private void postBootUpdate(JobParameters jobParams, PackageManagerService pm, ArraySet<String> pkgs) {
        int lowBatteryThreshold = getResources().getInteger(17694804);
        long lowThreshold = getLowStorageThreshold(this);
        this.mAbortPostBootUpdate.set(false);
        ArraySet<String> updatedPackages = new ArraySet();
        for (String pkg : pkgs) {
            if (!this.mAbortPostBootUpdate.get()) {
                if (this.mExitPostBootUpdate.get() || getBatteryLevel() < lowBatteryThreshold) {
                    break;
                }
                long usableSpace = this.mDataDir.getUsableSpace();
                if (usableSpace < lowThreshold) {
                    Log.w(TAG, "Aborting background dex opt job due to low storage: " + usableSpace);
                    break;
                } else if (pm.performDexOptWithStatus(pkg, false, 1, false) == 1) {
                    updatedPackages.add(pkg);
                }
            } else {
                return;
            }
        }
        notifyPinService(updatedPackages);
        jobFinished(jobParams, false);
    }

    private boolean runIdleOptimization(JobParameters jobParams, PackageManagerService pm, ArraySet<String> pkgs) {
        final PackageManagerService packageManagerService = pm;
        final ArraySet<String> arraySet = pkgs;
        final JobParameters jobParameters = jobParams;
        new Thread("BackgroundDexOptService_IdleOptimization") {
            public void run() {
                BackgroundDexOptService.this.mIdleJobExcuting.set(true);
                if (BackgroundDexOptService.this.idleOptimization(packageManagerService, arraySet, BackgroundDexOptService.this) != 2) {
                    Log.w(BackgroundDexOptService.TAG, "Idle optimizations aborted because of space constraints.");
                    BackgroundDexOptService.this.jobFinished(jobParameters, false);
                }
                BackgroundDexOptService.this.mIdleJobExcuting.set(false);
                synchronized (BackgroundDexOptService.sLock) {
                    if (BackgroundDexOptService.sIdleOptPeriod != BackgroundDexOptService.IDLE_OPTIMIZATION_PERIOD) {
                        BackgroundDexOptService.this.rescheduleIdleOptJobWithPeriodLocked(BackgroundDexOptService.IDLE_OPTIMIZATION_PERIOD);
                    }
                }
                Log.i(BackgroundDexOptService.TAG, "Performing idle optimizations finished!");
            }
        }.start();
        return true;
    }

    private int idleOptimization(PackageManagerService pm, ArraySet<String> pkgs, Context context) {
        Log.i(TAG, "Performing idle optimizations");
        this.mExitPostBootUpdate.set(true);
        this.mAbortIdleOptimization.set(false);
        long lowStorageThreshold = getLowStorageThreshold(context);
        int result = optimizePackages(pm, pkgs, lowStorageThreshold, true, sFailedPackageNamesPrimary);
        if (result == 2) {
            return result;
        }
        if (SystemProperties.getBoolean("dalvik.vm.dexopt.secondary", false)) {
            result = reconcileSecondaryDexFiles(pm.getDexManager());
            if (result == 2) {
                return result;
            }
            result = optimizePackages(pm, pkgs, lowStorageThreshold, false, sFailedPackageNamesSecondary);
        }
        return result;
    }

    private int optimizePackages(PackageManagerService pm, ArraySet<String> pkgs, long lowStorageThreshold, boolean is_for_primary_dex, ArraySet<String> failedPackageNames) {
        ArraySet<String> SPEED_MODE_PKGS = getAllNeedForSpeedApps();
        ArraySet<String> updatedPackages = new ArraySet();
        for (String pkg : pkgs) {
            int abort_code = abortIdleOptimizations(lowStorageThreshold);
            if (abort_code != 1) {
                return abort_code;
            }
            synchronized (failedPackageNames) {
                if (!failedPackageNames.contains(pkg)) {
                    boolean success;
                    failedPackageNames.add(pkg);
                    int compileReason = 3;
                    if (SPEED_MODE_PKGS != null && SPEED_MODE_PKGS.contains(pkg)) {
                        compileReason = 5;
                    }
                    if (is_for_primary_dex) {
                        int result = pm.performDexOptWithStatus(pkg, true, compileReason, false);
                        success = result != -1;
                        if (result == 1) {
                            updatedPackages.add(pkg);
                        }
                    } else {
                        success = pm.performDexOptSecondary(pkg, compileReason, false);
                    }
                    if (success) {
                        synchronized (failedPackageNames) {
                            failedPackageNames.remove(pkg);
                        }
                    } else {
                        continue;
                    }
                }
            }
        }
        notifyPinService(updatedPackages);
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

    private ArraySet<String> getAllNeedForSpeedApps() {
        try {
            File file = HwCfgFilePolicy.getCfgFile(CUST_FILE_PATH, 0);
            if (file != null) {
                return readSpeedAppsFromXml(file);
            }
            Log.i(TAG, "hw_aot_compile_apps_config not exist");
            return null;
        } catch (NoClassDefFoundError e) {
            Log.i(TAG, "get speed apps failed:" + e);
            return null;
        } catch (Throwable th) {
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:62:0x00ea A:{SYNTHETIC, Splitter: B:62:0x00ea} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x009f A:{SYNTHETIC, Splitter: B:47:0x009f} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00e4 A:{SYNTHETIC, Splitter: B:58:0x00e4} */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x00f7 A:{SYNTHETIC, Splitter: B:70:0x00f7} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ArraySet<String> readSpeedAppsFromXml(File config) {
        XmlPullParserException e;
        IOException e2;
        FileInputStream stream = null;
        ArraySet<String> speedApps = null;
        if (!config.exists() || (config.canRead() ^ 1) != 0) {
            return null;
        }
        try {
            FileInputStream stream2 = new FileInputStream(config);
            try {
                int type;
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, StandardCharsets.UTF_8.name());
                do {
                    type = parser.next();
                    if (type == 2) {
                        break;
                    }
                } while (type != 1);
                if (type != 2) {
                    Log.w(TAG, "Failed parsing config, can't find start tag");
                    if (stream2 != null) {
                        try {
                            stream2.close();
                        } catch (IOException e3) {
                        }
                    }
                    return null;
                }
                ArraySet<String> speedApps2 = new ArraySet();
                try {
                    int outerDepth = parser.getDepth();
                    while (true) {
                        type = parser.next();
                        if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                            if (stream2 != null) {
                                try {
                                    stream2.close();
                                } catch (IOException e4) {
                                }
                            }
                        } else if (!(type == 3 || type == 4)) {
                            if (parser.getName().equals(TAG_NAME)) {
                                String name = parser.getAttributeValue(null, ATTR_NAME);
                                if (!TextUtils.isEmpty(name)) {
                                    speedApps2.add(name);
                                }
                            } else {
                                Log.w(TAG, "Unknown element under <config>: " + parser.getName());
                                XmlUtils.skipCurrentTag(parser);
                            }
                        }
                    }
                    if (stream2 != null) {
                    }
                    return speedApps2;
                } catch (XmlPullParserException e5) {
                    e = e5;
                    speedApps = speedApps2;
                    stream = stream2;
                } catch (IOException e6) {
                    e2 = e6;
                    speedApps = speedApps2;
                    stream = stream2;
                } catch (Throwable th) {
                    speedApps = speedApps2;
                    stream = stream2;
                }
            } catch (XmlPullParserException e7) {
                e = e7;
                stream = stream2;
                try {
                    Log.w(TAG, "Failed parsing config " + e);
                    if (stream != null) {
                    }
                    return speedApps;
                } catch (Throwable th2) {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e8) {
                        }
                    }
                    return speedApps;
                }
            } catch (IOException e9) {
                e2 = e9;
                stream = stream2;
                Log.w(TAG, "Failed parsing config " + e2);
                if (stream != null) {
                }
                return speedApps;
            } catch (Throwable th3) {
                stream = stream2;
                if (stream != null) {
                }
                return speedApps;
            }
        } catch (XmlPullParserException e10) {
            e = e10;
            Log.w(TAG, "Failed parsing config " + e);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e11) {
                }
            }
            return speedApps;
        } catch (IOException e12) {
            e2 = e12;
            Log.w(TAG, "Failed parsing config " + e2);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e13) {
                }
            }
            return speedApps;
        }
    }

    public static boolean runIdleOptimizationsNow(PackageManagerService pm, Context context) {
        if (new BackgroundDexOptService().idleOptimization(pm, pm.getOptimizablePackages(), context) == 0) {
            return true;
        }
        return false;
    }

    private void rescheduleIdleOptJobWithPeriodLocked(long period) {
        Log.i(TAG, "rescheduleIdleOptJobWithPeriodLocked with period:" + period);
        if (sIdleOptPeriod != period) {
            JobScheduler js = (JobScheduler) getSystemService("jobscheduler");
            sIdleOptJobInfoBuilder.setPeriodic(period);
            sIdleOptPeriod = period;
            js.schedule(sIdleOptJobInfoBuilder.build());
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0048, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean adjustIdleOptimizationJob() {
        int batteryLevel = getBatteryLevel();
        synchronized (sLock) {
            boolean normalIdlePolicy = sIdleOptPeriod == IDLE_OPTIMIZATION_PERIOD;
            if (batteryLevel < 94) {
                Log.i(TAG, "batteryLevel is " + batteryLevel + " low than 94%, skip current Job");
                if (normalIdlePolicy) {
                    Log.i(TAG, "reschedule it with short period");
                    rescheduleIdleOptJobWithPeriodLocked(SHORT_IDLE_OPTIMIZATION_PERIOD);
                } else {
                    Log.i(TAG, "scheduler already under short period schedule policy");
                }
            } else if (normalIdlePolicy) {
                Log.i(TAG, "batteryLevel is " + batteryLevel + ", scheduler under normal period policy");
                return false;
            } else {
                Log.i(TAG, "batteryLevel is " + batteryLevel + ", scheduler under short period policy");
                if (this.mIdleJobExcuting.get()) {
                    Log.i(TAG, "idle job is already running...");
                    return true;
                }
                Log.i(TAG, "idle job is not running...");
                return false;
            }
        }
    }

    public boolean onStartJob(JobParameters params) {
        PackageManagerService pm = (PackageManagerService) ServiceManager.getService(HwBroadcastRadarUtil.KEY_PACKAGE);
        if (pm.isStorageLow()) {
            return false;
        }
        ArraySet<String> pkgs = pm.getOptimizablePackages();
        if (pkgs.isEmpty()) {
            return false;
        }
        boolean result;
        if (params.getJobId() == JOB_POST_BOOT_UPDATE) {
            result = runPostBootUpdate(params, pm, pkgs);
        } else if (adjustIdleOptimizationJob()) {
            Log.i(TAG, "adjustIdleOptimizationJob return true, current job skipped");
            return false;
        } else {
            Log.i(TAG, "adjustIdleOptimizationJob return false, current job starting");
            result = runIdleOptimization(params, pm, pkgs);
        }
        return result;
    }

    public boolean onStopJob(JobParameters params) {
        if (params.getJobId() == JOB_POST_BOOT_UPDATE) {
            this.mAbortPostBootUpdate.set(true);
        } else {
            this.mAbortIdleOptimization.set(true);
        }
        return false;
    }

    private void notifyPinService(ArraySet<String> updatedPackages) {
        PinnerService pinnerService = (PinnerService) LocalServices.getService(PinnerService.class);
        if (pinnerService != null) {
            Log.i(TAG, "Pinning optimized code " + updatedPackages);
            pinnerService.update(updatedPackages);
        }
    }

    private void broadcastPieceClean(Context context) {
        context.sendBroadcast(new Intent(BROADCAST_PIECE), BROADCAST_PIECE_PERMISSION);
    }
}
