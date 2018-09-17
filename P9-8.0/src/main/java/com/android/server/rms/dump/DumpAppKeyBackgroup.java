package com.android.server.rms.dump;

import android.content.Context;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup;
import java.io.PrintWriter;

public final class DumpAppKeyBackgroup {
    private static final String TAG = "DumpAppKeyBackgroup";
    private static String event = null;
    private static boolean hasCheckType = false;
    private static boolean hasEvent = false;
    private static boolean hasFakeEvent = false;
    private static boolean hasPid = false;
    private static boolean hasPkgName = false;
    private static boolean hasState = false;
    private static boolean hasUid = false;
    private static String pid = null;
    private static String pkg = null;
    private static String state = null;
    private static String uid = null;

    public static final void dumpAppImportance(PrintWriter pw, Context context, String[] args) {
        if (!initArgs(context, args)) {
            try {
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
            } catch (NumberFormatException e) {
                pw.println("dump args is illegal!");
            }
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
