package com.android.server.pm;

import android.app.job.JobInfo.Builder;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ServiceManager;
import android.util.ArraySet;
import com.android.server.am.HwBroadcastRadarUtil;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BackgroundDexOptService extends JobService {
    static final int JOB_IDLE_OPTIMIZE = 800;
    static final int JOB_POST_BOOT_UPDATE = 801;
    static final long RETRY_LATENCY = 14400000;
    static final String TAG = "BackgroundDexOptService";
    private static ComponentName sDexoptServiceName;
    static final ArraySet<String> sFailedPackageNames = null;
    final AtomicBoolean mAbortIdleOptimization;
    final AtomicBoolean mAbortPostBootUpdate;
    final AtomicBoolean mExitPostBootUpdate;

    /* renamed from: com.android.server.pm.BackgroundDexOptService.1 */
    class AnonymousClass1 extends Thread {
        final /* synthetic */ JobParameters val$jobParams;
        final /* synthetic */ int val$lowBatteryThreshold;
        final /* synthetic */ ArraySet val$pkgs;
        final /* synthetic */ PackageManagerService val$pm;

        AnonymousClass1(String $anonymous0, ArraySet val$pkgs, int val$lowBatteryThreshold, PackageManagerService val$pm, JobParameters val$jobParams) {
            this.val$pkgs = val$pkgs;
            this.val$lowBatteryThreshold = val$lowBatteryThreshold;
            this.val$pm = val$pm;
            this.val$jobParams = val$jobParams;
            super($anonymous0);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            for (String pkg : this.val$pkgs) {
                if (!BackgroundDexOptService.this.mAbortPostBootUpdate.get()) {
                    if (!BackgroundDexOptService.this.mExitPostBootUpdate.get() && BackgroundDexOptService.this.getBatteryLevel() >= this.val$lowBatteryThreshold) {
                        this.val$pm.performDexOpt(pkg, false, 1, false);
                    }
                } else {
                    return;
                }
            }
            BackgroundDexOptService.this.jobFinished(this.val$jobParams, false);
        }
    }

    /* renamed from: com.android.server.pm.BackgroundDexOptService.2 */
    class AnonymousClass2 extends Thread {
        final /* synthetic */ JobParameters val$jobParams;
        final /* synthetic */ ArraySet val$pkgs;
        final /* synthetic */ PackageManagerService val$pm;

        AnonymousClass2(String $anonymous0, ArraySet val$pkgs, PackageManagerService val$pm, JobParameters val$jobParams) {
            this.val$pkgs = val$pkgs;
            this.val$pm = val$pm;
            this.val$jobParams = val$jobParams;
            super($anonymous0);
        }

        public void run() {
            ArraySet<String> SPEED_MODE_SET = new ArraySet();
            SPEED_MODE_SET.add("com.google.android.gms");
            SPEED_MODE_SET.add("com.tencent.mm");
            for (String pkg : this.val$pkgs) {
                if (!BackgroundDexOptService.this.mAbortIdleOptimization.get()) {
                    if (!BackgroundDexOptService.sFailedPackageNames.contains(pkg)) {
                        synchronized (BackgroundDexOptService.sFailedPackageNames) {
                            BackgroundDexOptService.sFailedPackageNames.add(pkg);
                        }
                        int compileReason = 3;
                        if (SPEED_MODE_SET.contains(pkg)) {
                            compileReason = 6;
                        }
                        if (this.val$pm.performDexOpt(pkg, true, compileReason, false)) {
                            synchronized (BackgroundDexOptService.sFailedPackageNames) {
                                BackgroundDexOptService.sFailedPackageNames.remove(pkg);
                            }
                        } else {
                            continue;
                        }
                    }
                } else {
                    return;
                }
            }
            BackgroundDexOptService.this.jobFinished(this.val$jobParams, false);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pm.BackgroundDexOptService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.pm.BackgroundDexOptService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.BackgroundDexOptService.<clinit>():void");
    }

    public BackgroundDexOptService() {
        this.mAbortPostBootUpdate = new AtomicBoolean(false);
        this.mAbortIdleOptimization = new AtomicBoolean(false);
        this.mExitPostBootUpdate = new AtomicBoolean(false);
    }

    public static void schedule(Context context) {
        JobScheduler js = (JobScheduler) context.getSystemService("jobscheduler");
        js.schedule(new Builder(JOB_POST_BOOT_UPDATE, sDexoptServiceName).setMinimumLatency(TimeUnit.MINUTES.toMillis(1)).setOverrideDeadline(TimeUnit.MINUTES.toMillis(1)).build());
        js.schedule(new Builder(JOB_IDLE_OPTIMIZE, sDexoptServiceName).setRequiresDeviceIdle(true).setRequiresCharging(true).setPeriodic(TimeUnit.DAYS.toMillis(1)).build());
    }

    public static void notifyPackageChanged(String packageName) {
        synchronized (sFailedPackageNames) {
            sFailedPackageNames.remove(packageName);
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

    private boolean runPostBootUpdate(JobParameters jobParams, PackageManagerService pm, ArraySet<String> pkgs) {
        if (this.mExitPostBootUpdate.get()) {
            return false;
        }
        int lowBatteryThreshold = getResources().getInteger(17694807);
        this.mAbortPostBootUpdate.set(false);
        new AnonymousClass1("BackgroundDexOptService_PostBootUpdate", pkgs, lowBatteryThreshold, pm, jobParams).start();
        return true;
    }

    private boolean runIdleOptimization(JobParameters jobParams, PackageManagerService pm, ArraySet<String> pkgs) {
        this.mExitPostBootUpdate.set(true);
        this.mAbortIdleOptimization.set(false);
        new AnonymousClass2("BackgroundDexOptService_IdleOptimization", pkgs, pm, jobParams).start();
        return true;
    }

    public boolean onStartJob(JobParameters params) {
        PackageManagerService pm = (PackageManagerService) ServiceManager.getService(HwBroadcastRadarUtil.KEY_PACKAGE);
        if (pm.isStorageLow()) {
            return false;
        }
        ArraySet<String> pkgs = pm.getOptimizablePackages();
        if (pkgs == null || pkgs.isEmpty()) {
            return false;
        }
        if (params.getJobId() == JOB_POST_BOOT_UPDATE) {
            return runPostBootUpdate(params, pm, pkgs);
        }
        return runIdleOptimization(params, pm, pkgs);
    }

    public boolean onStopJob(JobParameters params) {
        if (params.getJobId() == JOB_POST_BOOT_UPDATE) {
            this.mAbortPostBootUpdate.set(true);
        } else {
            this.mAbortIdleOptimization.set(true);
        }
        return false;
    }
}
