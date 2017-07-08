package com.android.server.mtm.test;

import android.os.Bundle;
import android.os.SystemClock;
import com.android.server.mtm.condition.GroupConditionMatchor;
import com.android.server.rms.shrinker.ProcessStopShrinker;
import java.io.PrintWriter;

public final class TestGroupConditions {
    static GroupConditionMatchor testmatchor;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.mtm.test.TestGroupConditions.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.mtm.test.TestGroupConditions.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.mtm.test.TestGroupConditions.<clinit>():void");
    }

    public static final void test(PrintWriter pw, String[] args) {
        if (args[1] != null && pw != null) {
            if (args.length < 2) {
                pw.println("Bad command conditiontest:");
            }
            String cmd = args[1];
            GroupConditionMatchor groupConditionMatchor;
            if ("enable_log".equals(cmd)) {
                groupConditionMatchor = testmatchor;
                GroupConditionMatchor.enableDebug();
            } else if ("disable_log".equals(cmd)) {
                groupConditionMatchor = testmatchor;
                GroupConditionMatchor.disableDebug();
            } else if ("conditionMatch".equals(cmd)) {
                runConditionMatch(pw, args);
            } else {
                pw.println("Bad command :" + cmd);
            }
        }
    }

    private static void runConditionMatch(PrintWriter pw, String[] args) {
        if (args.length < 6) {
            pw.println("args is invalid");
            return;
        }
        int pid = Integer.parseInt(args[2]);
        int uid = Integer.parseInt(args[3]);
        int conditiontype = Integer.parseInt(args[4]);
        int conditionattribute = Integer.parseInt(args[5]);
        Bundle rsbundles = new Bundle();
        rsbundles.putInt(ProcessStopShrinker.PID_KEY, pid);
        rsbundles.putInt("uid", uid);
        Bundle bundles = new Bundle();
        bundles.putBundle("resourcebundle", rsbundles);
        bundles.putInt("conditiontype", conditiontype);
        bundles.putInt("conditionattribute", conditionattribute);
        bundles.putString("conditionextend", "aaaaaaa");
        bundles.putString("combinedcondition", args[4]);
        long starttime = SystemClock.uptimeMillis();
        long durtime = SystemClock.uptimeMillis() - starttime;
        pw.println("test result is :" + testmatchor.conditionMatch(conditiontype, bundles) + "(0:UNMATCHED|1:MATCHED|2:FORBIDDEN)");
        pw.println("total time:" + durtime + "(ms)");
    }
}
