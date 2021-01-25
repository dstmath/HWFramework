package com.android.server.am;

import android.app.IInstrumentationWatcher;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.Process;
import android.os.RemoteException;
import android.util.Slog;
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

    /* access modifiers changed from: package-private */
    public final class MyThread extends Thread {
        public MyThread() {
            super("InstrumentationReporter");
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            ArrayList<Report> reports;
            Process.setThreadPriority(0);
            boolean waited = false;
            while (true) {
                synchronized (InstrumentationReporter.this.mLock) {
                    reports = InstrumentationReporter.this.mPendingReports;
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
                waited = false;
                for (int i = 0; i < reports.size(); i++) {
                    Report rep = reports.get(i);
                    try {
                        if (rep.mType == 0) {
                            rep.mWatcher.instrumentationStatus(rep.mName, rep.mResultCode, rep.mResults);
                        } else {
                            rep.mWatcher.instrumentationFinished(rep.mName, rep.mResultCode, rep.mResults);
                        }
                    } catch (RemoteException e2) {
                        Slog.i("ActivityManager", "Failure reporting to instrumentation watcher: comp=" + rep.mName + " results=" + rep.mResults);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final class Report {
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
                this.mPendingReports = new ArrayList<>();
            }
            this.mPendingReports.add(report);
            this.mLock.notifyAll();
        }
    }
}
