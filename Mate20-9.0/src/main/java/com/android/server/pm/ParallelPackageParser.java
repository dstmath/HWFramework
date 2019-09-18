package com.android.server.pm;

import android.content.pm.PackageParser;
import android.os.Trace;
import android.util.DisplayMetrics;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ConcurrentUtils;
import java.io.File;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

class ParallelPackageParser implements AutoCloseable {
    private static final int MAX_THREADS = 4;
    private static final int QUEUE_CAPACITY = 10;
    private final File mCacheDir;
    private volatile String mInterruptedInThread;
    private final DisplayMetrics mMetrics;
    private final boolean mOnlyCore;
    private final PackageParser.Callback mPackageParserCallback;
    private final BlockingQueue<ParseResult> mQueue = new ArrayBlockingQueue(10);
    private final String[] mSeparateProcesses;
    private final ExecutorService mService = ConcurrentUtils.newFixedThreadPool(4, "package-parsing-thread", -2);

    static class ParseResult {
        PackageParser.Package pkg;
        File scanFile;
        Throwable throwable;

        ParseResult() {
        }

        public String toString() {
            return "ParseResult{pkg=" + this.pkg + ", scanFile=" + this.scanFile + ", throwable=" + this.throwable + '}';
        }
    }

    ParallelPackageParser(String[] separateProcesses, boolean onlyCoreApps, DisplayMetrics metrics, File cacheDir, PackageParser.Callback callback) {
        this.mSeparateProcesses = separateProcesses;
        this.mOnlyCore = onlyCoreApps;
        this.mMetrics = metrics;
        this.mCacheDir = cacheDir;
        this.mPackageParserCallback = callback;
    }

    public ParseResult take() {
        try {
            if (this.mInterruptedInThread == null) {
                return this.mQueue.take();
            }
            throw new InterruptedException("Interrupted in " + this.mInterruptedInThread);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    public void submit(File scanFile, int parseFlags) {
        this.mService.submit(new Runnable(scanFile, parseFlags) {
            private final /* synthetic */ File f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                ParallelPackageParser.lambda$submit$0(ParallelPackageParser.this, this.f$1, this.f$2);
            }
        });
    }

    public static /* synthetic */ void lambda$submit$0(ParallelPackageParser parallelPackageParser, File scanFile, int parseFlags) {
        ParseResult pr = new ParseResult();
        Trace.traceBegin(262144, "parallel parsePackage [" + scanFile + "]");
        try {
            PackageParser pp = new PackageParser();
            pp.setSeparateProcesses(parallelPackageParser.mSeparateProcesses);
            pp.setOnlyCoreApps(parallelPackageParser.mOnlyCore);
            pp.setDisplayMetrics(parallelPackageParser.mMetrics);
            pp.setCacheDir(parallelPackageParser.mCacheDir);
            pp.setCallback(parallelPackageParser.mPackageParserCallback);
            pr.scanFile = scanFile;
            pr.pkg = parallelPackageParser.parsePackage(pp, scanFile, parseFlags);
        } catch (Throwable th) {
            Trace.traceEnd(262144);
            throw th;
        }
        Trace.traceEnd(262144);
        try {
            parallelPackageParser.mQueue.put(pr);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            parallelPackageParser.mInterruptedInThread = Thread.currentThread().getName();
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public PackageParser.Package parsePackage(PackageParser packageParser, File scanFile, int parseFlags) throws PackageParser.PackageParserException {
        return packageParser.parsePackage(scanFile, parseFlags, true);
    }

    public void close() {
        List<Runnable> unfinishedTasks = this.mService.shutdownNow();
        if (!unfinishedTasks.isEmpty()) {
            throw new IllegalStateException("Not all tasks finished before calling close: " + unfinishedTasks);
        }
    }
}
