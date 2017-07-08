package com.android.server.mtm.test;

import android.os.SystemClock;
import com.android.server.mtm.policy.MultiTaskPolicyList;
import com.android.server.mtm.policy.MultiTaskPolicyList.MultiTaskResourceConfig;
import com.android.server.mtm.policy.MultiTaskPolicyList.PolicyConditionConfig;
import com.android.server.mtm.policy.MultiTaskPolicyList.PolicyConfig;
import java.io.PrintWriter;
import java.util.ArrayList;

public final class TestStaticPolicy {
    private static MultiTaskPolicyList mStaticPolicyList;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.mtm.test.TestStaticPolicy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.mtm.test.TestStaticPolicy.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.mtm.test.TestStaticPolicy.<clinit>():void");
    }

    public static final void test(PrintWriter pw, String[] args) {
        MultiTaskPolicyList mStaticPolicyList = MultiTaskPolicyList.getInstance();
        if (args[1] != null && pw != null) {
            String cmd = args[1];
            if ("dump".equals(cmd)) {
                mStaticPolicyList.dump(pw);
            } else if ("enable_log".equals(cmd)) {
                MultiTaskPolicyList.enableDebug();
            } else if ("disable_log".equals(cmd)) {
                MultiTaskPolicyList.disableDebug();
            } else if ("getStaticPolicy".equals(cmd)) {
                runGetStaticPolicy(pw, args);
            } else {
                pw.println("Bad command :" + cmd);
            }
        }
    }

    private static void runGetStaticPolicy(PrintWriter pw, String[] args) {
        int length = args.length;
        if (r0 != 5 || args[2] == null || args[3] == null || args[4] == null) {
            pw.println("args is invalid");
            return;
        }
        int resourcetype = Integer.parseInt(args[2]);
        String resourceextend = args[3];
        int resourcestatus = Integer.parseInt(args[4]);
        if (mStaticPolicyList == null) {
            pw.println("MultiTaskPolicyList.getInstance is null");
            mStaticPolicyList = MultiTaskPolicyList.getInstance();
        }
        long starttime = SystemClock.uptimeMillis();
        MultiTaskResourceConfig resourcepolicy = mStaticPolicyList.getStaticPolicy(resourcetype, resourceextend, resourcestatus);
        long durtime = SystemClock.uptimeMillis() - starttime;
        if (resourcepolicy == null) {
            pw.println("static policy not exist with resourcetype:" + resourcetype + "|resourceextend:" + resourceextend + "|resourcestatus:" + resourcestatus);
            return;
        }
        ArrayList<PolicyConfig> policyconfig = resourcepolicy.getPolicy();
        if (policyconfig == null || policyconfig.size() <= 0) {
            pw.println("static policy config not exist with resourcetype:" + resourcetype + "|resourceextend:" + resourceextend + "|resourcestatus:" + resourcestatus);
            return;
        }
        for (int i = policyconfig.size() - 1; i >= 0; i--) {
            PolicyConfig mypolicy = (PolicyConfig) policyconfig.get(i);
            if (mypolicy != null) {
                pw.println("policytype=" + mypolicy.policytype);
                pw.println("policyname=" + mypolicy.policyname);
                pw.println("policyvalue=" + mypolicy.policyvalue);
                ArrayList<PolicyConditionConfig> conditions = mypolicy.getPolicycondition();
                if (conditions != null && conditions.size() > 0) {
                    for (int j = conditions.size() - 1; j >= 0; j--) {
                        PolicyConditionConfig mcondition = (PolicyConditionConfig) conditions.get(j);
                        if (mcondition != null) {
                            pw.println("conditiontype=" + mcondition.conditiontype);
                            pw.println("conditionname=" + mcondition.conditionname);
                            pw.println("conditionattribute=" + mcondition.conditionattribute);
                            pw.println("conditionextend=" + mcondition.conditionextend);
                            pw.println("combinedcondition=" + mcondition.combinedcondition);
                        }
                    }
                }
            }
        }
        pw.println("total time:" + durtime + "(ms)");
    }
}
