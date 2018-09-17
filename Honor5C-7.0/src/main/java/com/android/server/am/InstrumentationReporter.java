package com.android.server.am;

import android.app.IInstrumentationWatcher;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.Process;
import android.util.Slog;
import com.android.server.job.controllers.JobStatus;
import java.util.ArrayList;

public class InstrumentationReporter {
    static final boolean DEBUG = false;
    static final int REPORT_TYPE_FINISHED = 1;
    static final int REPORT_TYPE_STATUS = 0;
    static final String TAG = "ActivityManager";
    final Object mLock;
    ArrayList<Report> mPendingReports;
    Thread mThread;

    final class MyThread extends Thread {
        public MyThread() {
            super("InstrumentationReporter");
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            Process.setThreadPriority(InstrumentationReporter.REPORT_TYPE_STATUS);
            boolean waited = InstrumentationReporter.DEBUG;
            while (true) {
                synchronized (InstrumentationReporter.this.mLock) {
                    ArrayList<Report> reports = InstrumentationReporter.this.mPendingReports;
                    InstrumentationReporter.this.mPendingReports = null;
                    if (reports != null && !reports.isEmpty()) {
                        waited = InstrumentationReporter.DEBUG;
                        for (int i = InstrumentationReporter.REPORT_TYPE_STATUS; i < reports.size(); i += InstrumentationReporter.REPORT_TYPE_FINISHED) {
                            Report rep = (Report) reports.get(i);
                            try {
                                if (rep.mType == 0) {
                                    rep.mWatcher.instrumentationStatus(rep.mName, rep.mResultCode, rep.mResults);
                                } else {
                                    rep.mWatcher.instrumentationFinished(rep.mName, rep.mResultCode, rep.mResults);
                                }
                            } catch (Exception e) {
                                Slog.i(InstrumentationReporter.TAG, "Failure reporting to instrumentation watcher: comp=" + rep.mName + " results=" + rep.mResults, e);
                            }
                        }
                    } else if (waited) {
                        InstrumentationReporter.this.mThread = null;
                        return;
                    } else {
                        try {
                            InstrumentationReporter.this.mLock.wait(JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
                        } catch (InterruptedException e2) {
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

    public InstrumentationReporter() {
        this.mLock = new Object();
    }

    public void reportStatus(IInstrumentationWatcher watcher, ComponentName name, int resultCode, Bundle results) {
        report(new Report(REPORT_TYPE_STATUS, watcher, name, resultCode, results));
    }

    public void reportFinished(IInstrumentationWatcher watcher, ComponentName name, int resultCode, Bundle results) {
        report(new Report(REPORT_TYPE_FINISHED, watcher, name, resultCode, results));
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
