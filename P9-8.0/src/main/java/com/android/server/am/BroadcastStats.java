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
    final ArrayMap<String, ActionEntry> mActions = new ArrayMap();
    long mEndRealtime;
    long mEndUptime;
    final long mStartRealtime = SystemClock.elapsedRealtime();
    final long mStartUptime = SystemClock.uptimeMillis();

    static final class ActionEntry {
        final String mAction;
        final ArrayMap<String, ViolationEntry> mBackgroundCheckViolations = new ArrayMap();
        long mMaxDispatchTime;
        final ArrayMap<String, PackageEntry> mPackages = new ArrayMap();
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
        ActionEntry ae = (ActionEntry) this.mActions.get(action);
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
        PackageEntry pe = (PackageEntry) ae.mPackages.get(srcPackage);
        if (pe == null) {
            pe = new PackageEntry();
            ae.mPackages.put(srcPackage, pe);
        }
        pe.mSendCount++;
    }

    public void addBackgroundCheckViolation(String action, String targetPackage) {
        ActionEntry ae = (ActionEntry) this.mActions.get(action);
        if (ae == null) {
            ae = new ActionEntry(action);
            this.mActions.put(action, ae);
        }
        ViolationEntry ve = (ViolationEntry) ae.mBackgroundCheckViolations.get(targetPackage);
        if (ve == null) {
            ve = new ViolationEntry();
            ae.mBackgroundCheckViolations.put(targetPackage, ve);
        }
        ve.mCount++;
    }

    public boolean dumpStats(PrintWriter pw, String prefix, String dumpPackage) {
        int i;
        boolean printedSomething = false;
        ArrayList<ActionEntry> actions = new ArrayList(this.mActions.size());
        for (i = this.mActions.size() - 1; i >= 0; i--) {
            actions.add((ActionEntry) this.mActions.valueAt(i));
        }
        Collections.sort(actions, ACTIONS_COMPARATOR);
        for (i = actions.size() - 1; i >= 0; i--) {
            ActionEntry ae = (ActionEntry) actions.get(i);
            if (dumpPackage == null || (ae.mPackages.containsKey(dumpPackage) ^ 1) == 0) {
                int j;
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
                for (j = ae.mPackages.size() - 1; j >= 0; j--) {
                    pw.print(prefix);
                    pw.print("  Package ");
                    pw.print((String) ae.mPackages.keyAt(j));
                    pw.print(": ");
                    pw.print(((PackageEntry) ae.mPackages.valueAt(j)).mSendCount);
                    pw.println(" times");
                }
                for (j = ae.mBackgroundCheckViolations.size() - 1; j >= 0; j--) {
                    pw.print(prefix);
                    pw.print("  Bg Check Violation ");
                    pw.print((String) ae.mBackgroundCheckViolations.keyAt(j));
                    pw.print(": ");
                    pw.print(((ViolationEntry) ae.mBackgroundCheckViolations.valueAt(j)).mCount);
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
            ActionEntry ae = (ActionEntry) this.mActions.valueAt(i);
            if (dumpPackage == null || (ae.mPackages.containsKey(dumpPackage) ^ 1) == 0) {
                int j;
                pw.print("a,");
                pw.print((String) this.mActions.keyAt(i));
                pw.print(",");
                pw.print(ae.mReceiveCount);
                pw.print(",");
                pw.print(ae.mSkipCount);
                pw.print(",");
                pw.print(ae.mTotalDispatchTime);
                pw.print(",");
                pw.print(ae.mMaxDispatchTime);
                pw.println();
                for (j = ae.mPackages.size() - 1; j >= 0; j--) {
                    pw.print("p,");
                    pw.print((String) ae.mPackages.keyAt(j));
                    PackageEntry pe = (PackageEntry) ae.mPackages.valueAt(j);
                    pw.print(",");
                    pw.print(pe.mSendCount);
                    pw.println();
                }
                for (j = ae.mBackgroundCheckViolations.size() - 1; j >= 0; j--) {
                    pw.print("v,");
                    pw.print((String) ae.mBackgroundCheckViolations.keyAt(j));
                    ViolationEntry ve = (ViolationEntry) ae.mBackgroundCheckViolations.valueAt(j);
                    pw.print(",");
                    pw.print(ve.mCount);
                    pw.println();
                }
            }
        }
    }
}
