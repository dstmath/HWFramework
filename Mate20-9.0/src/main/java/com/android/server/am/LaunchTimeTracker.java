package com.android.server.am;

import android.os.SystemClock;
import android.os.Trace;
import android.util.Jlog;
import android.util.SparseArray;

class LaunchTimeTracker {
    private final SparseArray<Entry> mWindowingModeLaunchTime = new SparseArray<>();

    static class Entry {
        long mFullyDrawnStartTime;
        long mLaunchStartTime;

        Entry() {
        }

        /* access modifiers changed from: package-private */
        public void setLaunchTime(ActivityRecord r) {
            if (r.displayStartTime == 0) {
                if (r.getStack() != null) {
                    r.getStack().mshortComponentName = r.shortComponentName;
                    if (r.app != null) {
                        Jlog.d(43, r.shortComponentName, r.app.pid, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                    } else {
                        Jlog.d(43, r.shortComponentName, 0, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                    }
                    if (r.task != null) {
                        r.task.isLaunching = true;
                    }
                }
                long uptimeMillis = SystemClock.uptimeMillis();
                r.displayStartTime = uptimeMillis;
                r.fullyDrawnStartTime = uptimeMillis;
                if (this.mLaunchStartTime == 0) {
                    startLaunchTraces(r.packageName);
                    long j = r.displayStartTime;
                    this.mFullyDrawnStartTime = j;
                    this.mLaunchStartTime = j;
                }
            } else if (this.mLaunchStartTime == 0) {
                startLaunchTraces(r.packageName);
                long uptimeMillis2 = SystemClock.uptimeMillis();
                this.mFullyDrawnStartTime = uptimeMillis2;
                this.mLaunchStartTime = uptimeMillis2;
            }
        }

        private void startLaunchTraces(String packageName) {
            if (this.mFullyDrawnStartTime != 0) {
                Trace.asyncTraceEnd(64, "drawing", 0);
            }
            Trace.asyncTraceBegin(64, "launching: " + packageName, 0);
            Trace.asyncTraceBegin(64, "drawing", 0);
        }

        /* access modifiers changed from: private */
        public void stopFullyDrawnTraceIfNeeded() {
            if (this.mFullyDrawnStartTime != 0 && this.mLaunchStartTime == 0) {
                Trace.asyncTraceEnd(64, "drawing", 0);
                this.mFullyDrawnStartTime = 0;
            }
        }
    }

    LaunchTimeTracker() {
    }

    /* access modifiers changed from: package-private */
    public void setLaunchTime(ActivityRecord r) {
        Entry entry = this.mWindowingModeLaunchTime.get(r.getWindowingMode());
        if (entry == null) {
            entry = new Entry();
            this.mWindowingModeLaunchTime.append(r.getWindowingMode(), entry);
        }
        entry.setLaunchTime(r);
    }

    /* access modifiers changed from: package-private */
    public void stopFullyDrawnTraceIfNeeded(int windowingMode) {
        Entry entry = this.mWindowingModeLaunchTime.get(windowingMode);
        if (entry != null) {
            entry.stopFullyDrawnTraceIfNeeded();
        }
    }

    /* access modifiers changed from: package-private */
    public Entry getEntry(int windowingMode) {
        return this.mWindowingModeLaunchTime.get(windowingMode);
    }
}
