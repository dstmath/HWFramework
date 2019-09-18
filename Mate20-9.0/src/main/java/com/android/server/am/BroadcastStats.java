package com.android.server.am;

import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.TimeUtils;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public final class BroadcastStats {
    static final Comparator<ActionEntry> ACTIONS_COMPARATOR = new Comparator<ActionEntry>() {
        public int compare(ActionEntry o1, ActionEntry o2) {
            if (o1.mTotalDispatchTime < o2.mTotalDispatchTime) {
                return -1;
            }
            if (o1.mTotalDispatchTime > o2.mTotalDispatchTime) {
                return 1;
            }
            return 0;
        }
    };
    final ArrayMap<String, ActionEntry> mActions = new ArrayMap<>();
    long mEndRealtime;
    long mEndUptime;
    final long mStartRealtime = SystemClock.elapsedRealtime();
    final long mStartUptime = SystemClock.uptimeMillis();

    static final class ActionEntry {
        final String mAction;
        final ArrayMap<String, ViolationEntry> mBackgroundCheckViolations = new ArrayMap<>();
        long mMaxDispatchTime;
        final ArrayMap<String, PackageEntry> mPackages = new ArrayMap<>();
        int mReceiveCount;
        int mSkipCount;
        long mTotalDispatchTime;

        ActionEntry(String action) {
            this.mAction = action;
        }
    }

    static final class PackageEntry {
        int mSendCount;

        PackageEntry() {
        }
    }

    static final class ViolationEntry {
        int mCount;

        ViolationEntry() {
        }
    }

    public void addBroadcast(String action, String srcPackage, int receiveCount, int skipCount, long dispatchTime) {
        ActionEntry ae = this.mActions.get(action);
        if (ae == null) {
            ae = new ActionEntry(action);
            this.mActions.put(action, ae);
        }
        ae.mReceiveCount += receiveCount;
        ae.mSkipCount += skipCount;
        ae.mTotalDispatchTime += dispatchTime;
        if (ae.mMaxDispatchTime < dispatchTime) {
            ae.mMaxDispatchTime = dispatchTime;
        }
        PackageEntry pe = ae.mPackages.get(srcPackage);
        if (pe == null) {
            pe = new PackageEntry();
            ae.mPackages.put(srcPackage, pe);
        }
        pe.mSendCount++;
    }

    public void addBackgroundCheckViolation(String action, String targetPackage) {
        ActionEntry ae = this.mActions.get(action);
        if (ae == null) {
            ae = new ActionEntry(action);
            this.mActions.put(action, ae);
        }
        ViolationEntry ve = ae.mBackgroundCheckViolations.get(targetPackage);
        if (ve == null) {
            ve = new ViolationEntry();
            ae.mBackgroundCheckViolations.put(targetPackage, ve);
        }
        ve.mCount++;
    }

    public boolean dumpStats(PrintWriter pw, String prefix, String dumpPackage) {
        boolean printedSomething = false;
        ArrayList<ActionEntry> actions = new ArrayList<>(this.mActions.size());
        for (int i = this.mActions.size() - 1; i >= 0; i--) {
            actions.add(this.mActions.valueAt(i));
        }
        Collections.sort(actions, ACTIONS_COMPARATOR);
        for (int i2 = actions.size() - 1; i2 >= 0; i2--) {
            ActionEntry ae = actions.get(i2);
            if (dumpPackage == null || ae.mPackages.containsKey(dumpPackage)) {
                printedSomething = true;
                pw.print(prefix);
                pw.print(ae.mAction);
                pw.println(":");
                pw.print(prefix);
                pw.print("  Number received: ");
                pw.print(ae.mReceiveCount);
                pw.print(", skipped: ");
                pw.println(ae.mSkipCount);
                pw.print(prefix);
                pw.print("  Total dispatch time: ");
                TimeUtils.formatDuration(ae.mTotalDispatchTime, pw);
                pw.print(", max: ");
                TimeUtils.formatDuration(ae.mMaxDispatchTime, pw);
                pw.println();
                for (int j = ae.mPackages.size() - 1; j >= 0; j--) {
                    pw.print(prefix);
                    pw.print("  Package ");
                    pw.print(ae.mPackages.keyAt(j));
                    pw.print(": ");
                    pw.print(ae.mPackages.valueAt(j).mSendCount);
                    pw.println(" times");
                }
                for (int j2 = ae.mBackgroundCheckViolations.size() - 1; j2 >= 0; j2--) {
                    pw.print(prefix);
                    pw.print("  Bg Check Violation ");
                    pw.print(ae.mBackgroundCheckViolations.keyAt(j2));
                    pw.print(": ");
                    pw.print(ae.mBackgroundCheckViolations.valueAt(j2).mCount);
                    pw.println(" times");
                }
            }
        }
        return printedSomething;
    }

    public void dumpCheckinStats(PrintWriter pw, String dumpPackage) {
        pw.print("broadcast-stats,1,");
        pw.print(this.mStartRealtime);
        pw.print(",");
        pw.print(this.mEndRealtime == 0 ? SystemClock.elapsedRealtime() : this.mEndRealtime);
        pw.print(",");
        pw.println((this.mEndUptime == 0 ? SystemClock.uptimeMillis() : this.mEndUptime) - this.mStartUptime);
        for (int i = this.mActions.size() - 1; i >= 0; i--) {
            ActionEntry ae = this.mActions.valueAt(i);
            if (dumpPackage == null || ae.mPackages.containsKey(dumpPackage)) {
                pw.print("a,");
                pw.print(this.mActions.keyAt(i));
                pw.print(",");
                pw.print(ae.mReceiveCount);
                pw.print(",");
                pw.print(ae.mSkipCount);
                pw.print(",");
                pw.print(ae.mTotalDispatchTime);
                pw.print(",");
                pw.print(ae.mMaxDispatchTime);
                pw.println();
                for (int j = ae.mPackages.size() - 1; j >= 0; j--) {
                    pw.print("p,");
                    pw.print(ae.mPackages.keyAt(j));
                    pw.print(",");
                    pw.print(ae.mPackages.valueAt(j).mSendCount);
                    pw.println();
                }
                for (int j2 = ae.mBackgroundCheckViolations.size() - 1; j2 >= 0; j2--) {
                    pw.print("v,");
                    pw.print(ae.mBackgroundCheckViolations.keyAt(j2));
                    pw.print(",");
                    pw.print(ae.mBackgroundCheckViolations.valueAt(j2).mCount);
                    pw.println();
                }
            }
        }
    }
}
