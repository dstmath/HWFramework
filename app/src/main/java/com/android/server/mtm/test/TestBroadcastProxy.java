package com.android.server.mtm.test;

import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.srms.AwareBroadcastDebug;
import com.android.server.mtm.iaware.srms.AwareBroadcastPolicy;
import java.io.PrintWriter;

public final class TestBroadcastProxy {
    private static AwareBroadcastPolicy mIawareBrPolicy;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.mtm.test.TestBroadcastProxy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.mtm.test.TestBroadcastProxy.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.mtm.test.TestBroadcastProxy.<clinit>():void");
    }

    public static final void test(PrintWriter pw, String[] args) {
        if (MultiTaskManagerService.self() != null) {
            mIawareBrPolicy = MultiTaskManagerService.self().getIawareBrPolicy();
        }
        if (mIawareBrPolicy == null) {
            pw.println("  iAware proxy broadcast have exception ");
            return;
        }
        if (args.length < 2 || args[1] == null) {
            pw.println("  iAware Proxy broadcast");
            mIawareBrPolicy.dumpIawareBr(pw);
        } else if ("disable_log".equals(args[1])) {
            AwareBroadcastDebug.disableDebug();
            pw.println("  iAware Proxy broadcast log disabled");
        } else if ("enable_log".equals(args[1])) {
            AwareBroadcastDebug.enableDebug();
            pw.println("  iAware Proxy broadcast log enabled");
        } else {
            pw.println("  bad command" + args[1]);
        }
    }
}
