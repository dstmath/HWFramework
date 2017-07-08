package com.android.server.rms.test;

import android.content.Context;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup;
import java.io.PrintWriter;
import junit.framework.Assert;

public final class TestAppKeyBackgroup extends Assert {
    private static final String TAG = "TestAppKeyBackgroup";
    private static String event;
    private static boolean hasCheckType;
    private static boolean hasEvent;
    private static boolean hasFakeEvent;
    private static boolean hasPid;
    private static boolean hasPkgName;
    private static boolean hasState;
    private static boolean hasUid;
    private static String pid;
    private static String pkg;
    private static String state;
    private static String uid;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.test.TestAppKeyBackgroup.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.test.TestAppKeyBackgroup.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.test.TestAppKeyBackgroup.<clinit>():void");
    }

    public static final void testAppImportance(PrintWriter pw, Context context, String[] args) {
        if (!initArgs(context, args)) {
            if (hasFakeEvent && hasState && hasEvent && event != null && state != null) {
                AwareAppKeyBackgroup.getInstance().dumpFakeEvent(pw, Integer.parseInt(state), Integer.parseInt(event), pid == null ? 0 : Integer.parseInt(pid), pkg, uid == null ? 0 : Integer.parseInt(uid));
                return;
            }
            if (hasPid && hasState && pid != null && state != null) {
                AwareAppKeyBackgroup.getInstance().dumpCheckStateByPid(pw, context, Integer.parseInt(state), Integer.parseInt(pid));
            }
            if (hasPkgName && hasState && pkg != null && state != null) {
                AwareAppKeyBackgroup.getInstance().dumpCheckStateByPkg(pw, context, Integer.parseInt(state), pkg);
            }
            if (hasCheckType && pkg != null) {
                AwareAppKeyBackgroup.getInstance().dumpCheckPkgType(pw, context, pkg);
            }
            if (hasPid && hasUid && pid != null && uid != null) {
                AwareAppKeyBackgroup.getInstance().dumpCheckKeyBackGroup(pw, Integer.parseInt(pid), Integer.parseInt(uid));
            }
            AwareAppKeyBackgroup.getInstance().dump(pw);
        }
    }

    private static boolean initArgs(Context context, String[] args) {
        hasPid = false;
        hasUid = false;
        hasState = false;
        hasCheckType = false;
        hasPkgName = false;
        hasEvent = false;
        hasFakeEvent = false;
        pid = null;
        state = null;
        pkg = null;
        uid = null;
        event = null;
        if (args != null) {
            int length = args.length;
            int i = 0;
            while (i < length) {
                String arg = args[i];
                if ("enable".equals(arg)) {
                    AwareAppKeyBackgroup.enable(context);
                    return true;
                } else if ("disable".equals(arg)) {
                    AwareAppKeyBackgroup.disable();
                    return true;
                } else if ("enable_log".equals(arg)) {
                    AwareAppKeyBackgroup.enableDebug();
                    return true;
                } else if ("disable_log".equals(arg)) {
                    AwareAppKeyBackgroup.disableDebug();
                    return true;
                } else {
                    initVaules(arg);
                    if ("--fake-event".equals(arg)) {
                        hasFakeEvent = true;
                    }
                    if (arg.equals("-p")) {
                        hasPid = true;
                    }
                    if (arg.equals("-u")) {
                        hasUid = true;
                    }
                    if (arg.equals("-s")) {
                        hasState = true;
                    }
                    if (arg.equals("-n")) {
                        hasPkgName = true;
                    }
                    if (arg.equals("-t")) {
                        hasCheckType = true;
                    }
                    if (arg.equals("-e")) {
                        hasEvent = true;
                    }
                    i++;
                }
            }
        }
        return false;
    }

    private static void initVaules(String arg) {
        if (hasPid && pid == null) {
            pid = arg;
        }
        if (hasUid && uid == null) {
            uid = arg;
        }
        if (hasState && state == null) {
            state = arg;
        }
        if (hasEvent && event == null) {
            event = arg;
        }
        if (hasPkgName && pkg == null) {
            pkg = arg;
        }
        if (hasCheckType && pkg == null) {
            pkg = arg;
        }
    }
}
