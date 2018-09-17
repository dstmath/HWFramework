package com.android.server.mtm.test;

import android.content.Context;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import java.io.PrintWriter;

public final class TestAppMngListInfo {
    private static AwareAppMngSort appGroupMng;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.mtm.test.TestAppMngListInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.mtm.test.TestAppMngListInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.mtm.test.TestAppMngListInfo.<clinit>():void");
    }

    public static final void test(Context context, PrintWriter pw, String[] args) {
        appGroupMng = AwareAppMngSort.getInstance(context);
        if (appGroupMng != null) {
            if (args.length < 2) {
                pw.println("Bad command");
            } else if (args[1] != null && pw != null) {
                String cmd = args[1];
                if ("dump".equals(cmd)) {
                    if (args.length < 3) {
                        pw.println("Bad command :" + cmd);
                    } else {
                        appGroupMng.dump(pw, args[2]);
                    }
                } else if ("enable_log".equals(cmd)) {
                    AwareAppMngSort.enableDebug();
                } else if ("disable_log".equals(cmd)) {
                    AwareAppMngSort.disableDebug();
                } else if ("enable_assoc".equals(cmd)) {
                    appGroupMng.enableAssocDebug();
                } else if ("disable_assoc".equals(cmd)) {
                    appGroupMng.disableAssocDebug();
                } else if ("getstatus_assoc".equals(cmd)) {
                    pw.println("assoc status: " + appGroupMng.getAssocDebug());
                } else if ("kill_all".equals(cmd)) {
                    appGroupMng.debugKillAllProcess(pw);
                } else if ("forcestop_all".equals(cmd)) {
                    appGroupMng.debugForstopPackageAllProcess(pw);
                } else if (MemoryConstant.MEM_POLICY_KILLACTION.equals(cmd)) {
                    if (args.length < 5) {
                        pw.println("please input the process pid and groupid(0-2), restart(true|false)");
                        return;
                    }
                    String strpid = args[2];
                    int pid = 0;
                    if (strpid != null) {
                        try {
                            pid = Integer.parseInt(strpid);
                        } catch (NumberFormatException e) {
                            pw.println("Bad command :" + cmd);
                            return;
                        }
                    }
                    String strGroupId = args[3];
                    int groupId = 2;
                    if (strGroupId != null) {
                        try {
                            groupId = Integer.parseInt(strGroupId);
                        } catch (NumberFormatException e2) {
                            pw.println("Bad command :" + cmd);
                            return;
                        }
                    }
                    appGroupMng.dumpKillProcess(pw, pid, groupId, Boolean.parseBoolean(args[4]));
                } else if (HwSecDiagnoseConstant.ANTIMAL_APK_TYPE.equals(cmd)) {
                    appGroupMng.dumpClassInfo(pw);
                } else {
                    pw.println("Bad command :" + cmd);
                }
            }
        }
    }
}
