package com.android.server.am;

import android.app.IInstrumentationWatcher;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.Process;
import com.android.server.job.controllers.JobStatus;
import java.util.ArrayList;

public class InstrumentationReporter {
    static final boolean DEBUG = false;
    static final int REPORT_TYPE_FINISHED = 1;
    static final int REPORT_TYPE_STATUS = 0;
    static final String TAG = "ActivityManager";
    final Object mLock = new Object();
    ArrayList<Report> mPendingReports;
    Thread mThread;

    final class MyThread extends Thread {
        public MyThread() {
            super("InstrumentationReporter");
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x001d, code lost:
            r1 = false;
            r2 = 0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0023, code lost:
            if (r2 >= r3.size()) goto L_0x0005;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0025, code lost:
            r4 = r3.get(r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x002d, code lost:
            if (r4.mType != 0) goto L_0x003b;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x002f, code lost:
            r4.mWatcher.instrumentationStatus(r4.mName, r4.mResultCode, r4.mResults);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x003b, code lost:
            r4.mWatcher.instrumentationFinished(r4.mName, r4.mResultCode, r4.mResults);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x0047, code lost:
            r5 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0048, code lost:
            android.util.Slog.i("ActivityManager", "Failure reporting to instrumentation watcher: comp=" + r4.mName + " results=" + r4.mResults, r5);
         */
        public void run() {
            Process.setThreadPriority(0);
            boolean waited = false;
            while (true) {
                synchronized (InstrumentationReporter.this.mLock) {
                    ArrayList<Report> reports = InstrumentationReporter.this.mPendingReports;
                    InstrumentationReporter.this.mPendingReports = null;
                    if (reports != null) {
                        if (reports.isEmpty()) {
                        }
                    }
                    if (!waited) {
                        try {
                            InstrumentationReporter.this.mLock.wait(JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
                        } catch (InterruptedException e) {
                        }
                        waited = true;
                    } else {
                        InstrumentationReporter.this.mThread = null;
                        return;
                    }
                }
            }
            int i = i + 1;
        }
    }

    final class Report {
        final ComponentName mName;
        final int mResultCode;
        final Bundle mResults;
        final int mType;
        final IInstrumentationWatcher mWatcher;

        Report(int type, IInstrumentationWatcher watcher, ComponentName name, int resultCode, Bundle results) {
            this.mType = type;
            this.mWatcher = watcher;
            this.mName = name;
            this.mResultCode = resultCode;
            this.mResults = results;
        }
    }

    public void reportStatus(IInstrumentationWatcher watcher, ComponentName name, int resultCode, Bundle results) {
        Report report = new Report(0, watcher, name, resultCode, results);
        report(report);
    }

    public void reportFinished(IInstrumentationWatcher watcher, ComponentName name, int resultCode, Bundle results) {
        Report report = new Report(1, watcher, name, resultCode, results);
        report(report);
    }

    private void report(Report report) {
        synchronized (this.mLock) {
            if (this.mThread == null) {
                this.mThread = new MyThread();
                this.mThread.start();
            }
            if (this.mPendingReports == null) {
                this.mPendingReports = new ArrayList<>();
            }
            this.mPendingReports.add(report);
            this.mLock.notifyAll();
        }
    }
}
