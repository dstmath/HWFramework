package com.android.server.pm;

import android.common.HwFrameworkFactory;
import android.common.HwFrameworkMonitor;
import android.content.Context;
import android.content.pm.PackageParser;
import android.os.Bundle;
import android.os.IThermalService;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Temperature;
import android.os.WorkSource;
import android.util.Slog;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.pm.CompilerStats;
import com.android.server.pm.dex.DexoptOptions;
import com.android.server.pm.dex.PackageDexUsage;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class HwParallelPackageDexOptimizer {
    private static final long CONFIG_MAX_CONTINUED_DEXOPT_TIME = SystemProperties.getLong("ro.config.max_continued_dexopt_time", 0);
    private static final int CONFIG_MAX_TEMPRATURE = SystemProperties.getInt("ro.config.dexopt_max_temprature", 0);
    private static final long MAX_CONTINUED_DEXOPT_TIME;
    private static final float MAX_TEMPRATURE;
    private static final int MAX_TIMEOUT = 10;
    private static final int MILLISECONDS_TO_SECONDS = 1000;
    private static final long MIN_APART_DEXOPT_TIME = 300;
    private static final String TAG = "HwParallelPackageDexOptimizer";
    private static final long WAITING_MILLISECONDS = 3000;
    private static final long WAKELOCK_TIMEOUT_MS = 660000;
    private static HwFrameworkMonitor sMonitor = HwFrameworkFactory.getHwFrameworkMonitor();
    private static AtomicInteger sNextJobId = new AtomicInteger(0);
    private float mCurTemprature = 0.0f;
    private AtomicLong mDexOptWorkingTime = new AtomicLong(0);
    private final PowerManager.WakeLock mDexoptWakeLock;
    private ExecutorService mExecutor;
    private long mJobStartTime;
    private PackageDexOptimizer mPackageDexOptimizer = null;
    private String mProcessingPackageName;
    private final Queue<DexOptJob> mQueue = new LinkedBlockingDeque();
    private IThermalService mThermalService;
    private Runnable mWorkRunnable = new Runnable() {
        /* class com.android.server.pm.HwParallelPackageDexOptimizer.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            DexOptJob curJob;
            HwParallelPackageDexOptimizer.this.mWorkerStarted.set(true);
            long lastJobFinishTime = 0;
            while (true) {
                synchronized (HwParallelPackageDexOptimizer.this.mQueue) {
                    int timeout = 0;
                    do {
                        if (!HwParallelPackageDexOptimizer.this.mQueue.isEmpty()) {
                            break;
                        }
                        try {
                            HwParallelPackageDexOptimizer.this.mQueue.wait(3000);
                            timeout++;
                        } catch (InterruptedException e) {
                            Slog.i(HwParallelPackageDexOptimizer.TAG, "The wait of queue is interrupted.");
                        }
                        if (!HwParallelPackageDexOptimizer.this.mQueue.isEmpty()) {
                            break;
                        }
                    } while (timeout <= 10);
                    if (HwParallelPackageDexOptimizer.this.mQueue.isEmpty()) {
                        Slog.i(HwParallelPackageDexOptimizer.TAG, "Worker thread wait timeout, no more job!");
                        HwParallelPackageDexOptimizer.this.mWorkerStarted.set(false);
                        Slog.i(HwParallelPackageDexOptimizer.TAG, "Worker thread quit.");
                        return;
                    }
                    curJob = (DexOptJob) HwParallelPackageDexOptimizer.this.mQueue.poll();
                    if (HwParallelPackageDexOptimizer.this.mQueue.isEmpty()) {
                        HwParallelPackageDexOptimizer.this.mQueue.notifyAll();
                    }
                }
                HwParallelPackageDexOptimizer.this.mJobStartTime = SystemClock.uptimeMillis();
                if (lastJobFinishTime > 0) {
                    Slog.i(HwParallelPackageDexOptimizer.TAG, "Dexopt new job " + (HwParallelPackageDexOptimizer.this.mJobStartTime - lastJobFinishTime) + "ms from last job.");
                }
                HwParallelPackageDexOptimizer.this.handleOptJob(curJob);
                long jobEndTime = SystemClock.uptimeMillis();
                if (HwParallelPackageDexOptimizer.this.mJobStartTime - lastJobFinishTime < HwParallelPackageDexOptimizer.MIN_APART_DEXOPT_TIME) {
                    HwParallelPackageDexOptimizer.this.mDexOptWorkingTime.getAndAdd(jobEndTime - HwParallelPackageDexOptimizer.this.mJobStartTime);
                } else {
                    HwParallelPackageDexOptimizer.this.mDexOptWorkingTime.set(jobEndTime - HwParallelPackageDexOptimizer.this.mJobStartTime);
                }
                Slog.i(HwParallelPackageDexOptimizer.TAG, "Dexopt have continue worked " + HwParallelPackageDexOptimizer.this.mDexOptWorkingTime.get() + "ms.");
                lastJobFinishTime = jobEndTime;
            }
        }
    };
    private AtomicBoolean mWorkerStarted = new AtomicBoolean(false);

    static {
        long j = CONFIG_MAX_CONTINUED_DEXOPT_TIME;
        if (j <= 0) {
            j = HwArbitrationDEFS.DelayTimeMillisA;
        }
        MAX_CONTINUED_DEXOPT_TIME = j;
        int i = CONFIG_MAX_TEMPRATURE;
        MAX_TEMPRATURE = i > 0 ? (float) i : 37.0f;
    }

    public HwParallelPackageDexOptimizer(Context context, PackageDexOptimizer pdo) {
        this.mDexoptWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "*install_dexopt*");
        this.mPackageDexOptimizer = pdo;
        if (this.mThermalService == null) {
            this.mThermalService = IThermalService.Stub.asInterface(ServiceManager.getService("thermalservice"));
        }
    }

    public void submit(PackageParser.Package pkg, String[] instructionSets, CompilerStats.PackageStats packageStats, PackageDexUsage.PackageUseInfo packageUseInfo, DexoptOptions options) {
        DexOptJob job;
        if (pkg.applicationInfo.uid == -1) {
            throw new IllegalArgumentException("Dexopt for " + pkg.packageName + " has invalid uid.");
        } else if (PackageDexOptimizer.canOptimizePackage(pkg)) {
            if (!this.mWorkerStarted.get()) {
                startWorker();
            }
            synchronized (this.mQueue) {
                while (this.mQueue.size() > 0) {
                    Slog.i(TAG, "Waiting the worker to drain out the job queue. The current dexOpt:" + this.mProcessingPackageName + ", dexOptWorkingTime:" + (SystemClock.uptimeMillis() - this.mJobStartTime));
                    try {
                        this.mQueue.wait();
                    } catch (InterruptedException e) {
                        Slog.i(TAG, "The wait of queue is interrupted.");
                    }
                    Slog.i(TAG, "Worker has drained out the job queue.");
                }
                job = new DexOptJob(pkg, instructionSets, packageStats, packageUseInfo, options);
                this.mQueue.add(job);
                this.mQueue.notifyAll();
            }
            sleepIfNeed(job);
        }
    }

    private void sleepIfNeed(DexOptJob job) {
        long dexOptTime = this.mDexOptWorkingTime.get();
        if (dexOptTime >= MAX_CONTINUED_DEXOPT_TIME) {
            updateTemprature();
            if (this.mCurTemprature >= MAX_TEMPRATURE) {
                synchronized (job) {
                    try {
                        job.wait();
                    } catch (InterruptedException e) {
                        Slog.i(TAG, "The wait of job is interrupted.");
                    }
                }
                Slog.i(TAG, "Worker thread continue working over " + dexOptTime + "ms, temprature=" + this.mCurTemprature + ", Going to sleep.");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e2) {
                    Slog.i(TAG, "The sleep of thread is interrupted.");
                }
                Slog.i(TAG, "Worker thread wake up.");
                this.mDexOptWorkingTime.set(0);
            }
        }
    }

    private void updateTemprature() {
        try {
            List<Temperature> temps = this.mThermalService.getCurrentTemperaturesWithType(3);
            if (!temps.isEmpty()) {
                Iterator<Temperature> it = temps.iterator();
                if (it.hasNext()) {
                    Temperature t = it.next();
                    Slog.i(TAG, "Temperature(TYPE_SKIN): " + t);
                    this.mCurTemprature = t.getValue();
                }
            }
        } catch (RemoteException e) {
            Slog.i(TAG, "Update temprature error.");
        }
    }

    private void startWorker() {
        Slog.i(TAG, "Start worker thread with Executor.");
        getExecutorService().submit(this.mWorkRunnable);
    }

    private ExecutorService getExecutorService() {
        if (this.mExecutor == null) {
            this.mExecutor = Executors.newCachedThreadPool();
        }
        return this.mExecutor;
    }

    private long acquireWakeLock(int uid) {
        this.mDexoptWakeLock.setWorkSource(new WorkSource(uid));
        this.mDexoptWakeLock.acquire(WAKELOCK_TIMEOUT_MS);
        return SystemClock.elapsedRealtime();
    }

    private void releaseWakeLock(long acquireTime) {
        if (acquireTime >= 0) {
            try {
                if (this.mDexoptWakeLock.isHeld()) {
                    this.mDexoptWakeLock.release();
                }
                long duration = SystemClock.elapsedRealtime() - acquireTime;
                if (duration >= WAKELOCK_TIMEOUT_MS) {
                    Slog.w(TAG, "WakeLock " + this.mDexoptWakeLock.getTag() + " time out. Operation took " + duration + " ms. Thread: " + Thread.currentThread().getName());
                }
            } catch (Exception e) {
                Slog.w(TAG, "Error while releasing " + this.mDexoptWakeLock.getTag() + " lock.");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleOptJob(DexOptJob job) {
        this.mProcessingPackageName = job.pkg.packageName;
        Slog.i(TAG, "OptJob#" + job.mJobId + ": perform dexopt for package:" + this.mProcessingPackageName);
        long acquireTime = acquireWakeLock(job.pkg.applicationInfo.uid);
        int result = -1;
        try {
            result = this.mPackageDexOptimizer.performDexOptWrapper(job.pkg, job.instructionSets, job.packageStats, job.packageUseInfo, job.options);
            BackgroundDexOptService.notifyPackageChanged(this.mProcessingPackageName);
            Slog.i(TAG, "OptJob#" + job.mJobId + ": finish perform dexopt for package:" + this.mProcessingPackageName);
            synchronized (job) {
                job.notifyAll();
            }
        } finally {
            releaseWakeLock(acquireTime);
            if (result == -1) {
                reportOptJobFailedIssue();
            }
        }
    }

    private void reportOptJobFailedIssue() {
        long dexOptWorkingTime = SystemClock.uptimeMillis() - this.mJobStartTime;
        Slog.i(TAG, "Failed to execute handleOptJob. The current dexOpt:" + this.mProcessingPackageName + ", dexOptWorkingTime:" + dexOptWorkingTime);
        Bundle data = new Bundle();
        data.putString("PACKAGE_NAME", this.mProcessingPackageName);
        data.putInt("WORKING_TIME", (int) (dexOptWorkingTime / 1000));
        HwFrameworkMonitor hwFrameworkMonitor = sMonitor;
        if (hwFrameworkMonitor == null || !hwFrameworkMonitor.monitor(907400032, data)) {
            Slog.i(TAG, "upload big data fail for PMS do parallel opt job failed.");
        } else {
            Slog.i(TAG, "upload big data success for PMS do parallel opt job failed.");
        }
    }

    /* access modifiers changed from: private */
    public static class DexOptJob {
        protected String[] instructionSets;
        protected int mJobId = HwParallelPackageDexOptimizer.sNextJobId.getAndIncrement();
        protected DexoptOptions options;
        protected CompilerStats.PackageStats packageStats;
        protected PackageDexUsage.PackageUseInfo packageUseInfo;
        protected PackageParser.Package pkg;

        DexOptJob(PackageParser.Package pkg2, String[] instructionSets2, CompilerStats.PackageStats packageStats2, PackageDexUsage.PackageUseInfo packageUseInfo2, DexoptOptions options2) {
            this.pkg = pkg2;
            this.instructionSets = instructionSets2;
            this.packageStats = packageStats2;
            this.packageUseInfo = packageUseInfo2;
            this.options = options2;
        }
    }
}
