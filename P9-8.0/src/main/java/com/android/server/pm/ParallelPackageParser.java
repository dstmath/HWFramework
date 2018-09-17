package com.android.server.pm;

import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Callback;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageParser.PackageParserException;
import android.os.Trace;
import android.util.DisplayMetrics;
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
    private final Callback mPackageParserCallback;
    private final BlockingQueue<ParseResult> mQueue = new ArrayBlockingQueue(10);
    private final String[] mSeparateProcesses;
    private final ExecutorService mService = ConcurrentUtils.newFixedThreadPool(4, "package-parsing-thread", -2);

    static class ParseResult {
        Package pkg;
        File scanFile;
        Throwable throwable;

        ParseResult() {
        }

        public String toString() {
            return "ParseResult{pkg=" + this.pkg + ", scanFile=" + this.scanFile + ", throwable=" + this.throwable + '}';
        }
    }

    ParallelPackageParser(String[] separateProcesses, boolean onlyCoreApps, DisplayMetrics metrics, File cacheDir, Callback callback) {
        this.mSeparateProcesses = separateProcesses;
        this.mOnlyCore = onlyCoreApps;
        this.mMetrics = metrics;
        this.mCacheDir = cacheDir;
        this.mPackageParserCallback = callback;
    }

    public ParseResult take() {
        try {
            if (this.mInterruptedInThread == null) {
                return (ParseResult) this.mQueue.take();
            }
            throw new InterruptedException("Interrupted in " + this.mInterruptedInThread);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    public void submit(File scanFile, int parseFlags) {
        this.mService.submit(new -$Lambda$ilXJRPJlbkTpQIIJ5M-1Mp8u2aI(parseFlags, this, scanFile));
    }

    /* synthetic */ void lambda$-com_android_server_pm_ParallelPackageParser_3701(File scanFile, int parseFlags) {
        ParseResult pr = new ParseResult();
        Trace.traceBegin(262144, "parallel parsePackage [" + scanFile + "]");
        try {
            PackageParser pp = new PackageParser();
            pp.setSeparateProcesses(this.mSeparateProcesses);
            pp.setOnlyCoreApps(this.mOnlyCore);
            pp.setDisplayMetrics(this.mMetrics);
            pp.setCacheDir(this.mCacheDir);
            pp.setCallback(this.mPackageParserCallback);
            pr.scanFile = scanFile;
            pr.pkg = parsePackage(pp, scanFile, parseFlags);
        } catch (Throwable e) {
            pr.throwable = e;
        } finally {
            Trace.traceEnd(262144);
        }
        try {
            this.mQueue.put(pr);
        } catch (InterruptedException e2) {
            Thread.currentThread().interrupt();
            this.mInterruptedInThread = Thread.currentThread().getName();
        }
    }

    protected Package parsePackage(PackageParser packageParser, File scanFile, int parseFlags) throws PackageParserException {
        return packageParser.parsePackage(scanFile, parseFlags, true);
    }

    public void close() {
        List<Runnable> unfinishedTasks = this.mService.shutdownNow();
        if (!unfinishedTasks.isEmpty()) {
            throw new IllegalStateException("Not all tasks finished before calling close: " + unfinishedTasks);
        }
    }
}
