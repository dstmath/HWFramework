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

        /* JADX WARNING: Removed duplicated region for block: B:33:0x0060 A:{Splitter: B:23:0x003f, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
        /* JADX WARNING: Missing block: B:19:0x0031, code:
            r5 = false;
            r2 = 0;
     */
        /* JADX WARNING: Missing block: B:21:0x0037, code:
            if (r2 >= r4.size()) goto L_0x0005;
     */
        /* JADX WARNING: Missing block: B:22:0x0039, code:
            r3 = (com.android.server.am.InstrumentationReporter.Report) r4.get(r2);
     */
        /* JADX WARNING: Missing block: B:25:0x0041, code:
            if (r3.mType != 0) goto L_0x0054;
     */
        /* JADX WARNING: Missing block: B:26:0x0043, code:
            r3.mWatcher.instrumentationStatus(r3.mName, r3.mResultCode, r3.mResults);
     */
        /* JADX WARNING: Missing block: B:27:0x004e, code:
            r2 = r2 + 1;
     */
        /* JADX WARNING: Missing block: B:32:?, code:
            r3.mWatcher.instrumentationFinished(r3.mName, r3.mResultCode, r3.mResults);
     */
        /* JADX WARNING: Missing block: B:33:0x0060, code:
            r0 = move-exception;
     */
        /* JADX WARNING: Missing block: B:34:0x0061, code:
            android.util.Slog.i("ActivityManager", "Failure reporting to instrumentation watcher: comp=" + r3.mName + " results=" + r3.mResults, r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            Process.setThreadPriority(0);
            boolean waited = false;
            while (true) {
                synchronized (InstrumentationReporter.this.mLock) {
                    ArrayList<Report> reports = InstrumentationReporter.this.mPendingReports;
                    InstrumentationReporter.this.mPendingReports = null;
                    if (reports == null || reports.isEmpty()) {
                        if (waited) {
                            InstrumentationReporter.this.mThread = null;
                            return;
                        }
                        try {
                            InstrumentationReporter.this.mLock.wait(JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
                        } catch (InterruptedException e) {
                        }
                        waited = true;
                    }
                }
            }
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
        report(new Report(0, watcher, name, resultCode, results));
    }

    public void reportFinished(IInstrumentationWatcher watcher, ComponentName name, int resultCode, Bundle results) {
        report(new Report(1, watcher, name, resultCode, results));
    }

    private void report(Report report) {
        synchronized (this.mLock) {
            if (this.mThread == null) {
                this.mThread = new MyThread();
                this.mThread.start();
            }
            if (this.mPendingReports == null) {
                this.mPendingReports = new ArrayList();
            }
            this.mPendingReports.add(report);
            this.mLock.notifyAll();
        }
    }
}
