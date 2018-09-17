package com.android.server.am;

import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.TimeUtils;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public final class BroadcastStats {
    static final Comparator<ActionEntry> ACTIONS_COMPARATOR = null;
    final ArrayMap<String, ActionEntry> mActions;
    long mEndRealtime;
    long mEndUptime;
    final long mStartRealtime;
    final long mStartUptime;

    static final class ActionEntry {
        final String mAction;
        long mMaxDispatchTime;
        final ArrayMap<String, PackageEntry> mPackages;
        int mReceiveCount;
        int mSkipCount;
        long mTotalDispatchTime;

        ActionEntry(String action) {
            this.mPackages = new ArrayMap();
            this.mAction = action;
        }
    }

    static final class PackageEntry {
        int mSendCount;

        PackageEntry() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.am.BroadcastStats.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.am.BroadcastStats.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.BroadcastStats.<clinit>():void");
    }

    public BroadcastStats() {
        this.mActions = new ArrayMap();
        this.mStartRealtime = SystemClock.elapsedRealtime();
        this.mStartUptime = SystemClock.uptimeMillis();
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
                    pw.print((String) ae.mPackages.keyAt(j));
                    pw.print(": ");
                    pw.print(((PackageEntry) ae.mPackages.valueAt(j)).mSendCount);
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
            if (dumpPackage == null || ae.mPackages.containsKey(dumpPackage)) {
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
                for (int j = ae.mPackages.size() - 1; j >= 0; j--) {
                    pw.print("p,");
                    pw.print((String) ae.mPackages.keyAt(j));
                    PackageEntry pe = (PackageEntry) ae.mPackages.valueAt(j);
                    pw.print(",");
                    pw.print(pe.mSendCount);
                    pw.println();
                }
            }
        }
    }
}
