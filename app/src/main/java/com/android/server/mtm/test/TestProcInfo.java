package com.android.server.mtm.test;

import android.app.mtm.MultiTaskManager;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import java.io.PrintWriter;

public final class TestProcInfo {
    private static MultiTaskManager mMultiTaskManager;
    private static ProcessInfoCollector mProcInfo;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.mtm.test.TestProcInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.mtm.test.TestProcInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.mtm.test.TestProcInfo.<clinit>():void");
    }

    public static final void test(PrintWriter pw, String[] args) {
        mProcInfo = ProcessInfoCollector.getInstance();
        mMultiTaskManager = MultiTaskManager.getInstance();
        if (args[1] != null && pw != null) {
            String cmd = args[1];
            if ("dump".equals(cmd)) {
                mProcInfo.dump(pw);
            } else if ("enable_log".equals(cmd)) {
                mProcInfo.enableDebug();
            } else if ("disable_log".equals(cmd)) {
                mProcInfo.disableDebug();
            } else if ("ut_lru".equals(cmd)) {
                mProcInfo.getAMSLru();
            } else if ("ut_lrubyid".equals(cmd)) {
                if (args[2] != null) {
                    mProcInfo.getAMSLruBypid(Integer.parseInt(args[2]));
                }
            } else if ("ut_killprocess".equals(cmd)) {
                if (args[2] != null) {
                    mMultiTaskManager.killProcess(Integer.parseInt(args[2]), false);
                }
            } else if ("ut_killprocessRestart".equals(cmd)) {
                if (args.length >= 3 && args[2] != null) {
                    mMultiTaskManager.killProcess(Integer.parseInt(args[2]), true);
                }
            } else if (!"ut_forcestop".equals(cmd)) {
                pw.println("Bad command :" + cmd);
            } else if (args[2] != null) {
                mMultiTaskManager.forcestopApps(Integer.parseInt(args[2]));
            }
        }
    }
}
