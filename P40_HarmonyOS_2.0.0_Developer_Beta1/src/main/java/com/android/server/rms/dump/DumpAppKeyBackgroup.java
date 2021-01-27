package com.android.server.rms.dump;

import android.content.Context;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup;
import com.android.server.rms.iaware.appmng.AwareAudioFocusManager;
import java.io.PrintWriter;

public final class DumpAppKeyBackgroup {
    private static final String TAG = "DumpAppKeyBackgroup";
    private static String sEvent = null;
    private static boolean sHasCheckType = false;
    private static boolean sHasEvent = false;
    private static boolean sHasFakeEvent = false;
    private static boolean sHasPid = false;
    private static boolean sHasPkgName = false;
    private static boolean sHasState = false;
    private static boolean sHasUid = false;
    private static String sPid = null;
    private static String sPkg = null;
    private static String sState = null;
    private static String sUid = null;

    public static void dumpAppImportance(PrintWriter pw, Context context, String[] args) {
        if (!initArgs(context, args)) {
            try {
                if (sHasFakeEvent && sHasState && sHasEvent && sEvent != null && sState != null) {
                    AwareAppKeyBackgroup.getInstance().dumpFakeEvent(pw, new int[]{Integer.parseInt(sState), Integer.parseInt(sEvent), sPid == null ? 0 : Integer.parseInt(sPid), sUid == null ? 0 : Integer.parseInt(sUid)}, sPkg);
                    return;
                }
                checkHasPid(pw, context);
                checkHasPkgName(pw, context);
                checkHasCheckType(pw, context);
                checkHasPidAndUid(pw);
                AwareAppKeyBackgroup.getInstance().dump(pw);
            } catch (NumberFormatException e) {
                pw.println("dump args is illegal!");
            }
        }
    }

    private static void checkHasPidAndUid(PrintWriter pw) {
        try {
            if (sHasPid && sHasUid && sPid != null && sUid != null) {
                AwareAppKeyBackgroup.getInstance().dumpCheckKeyBackGroup(pw, Integer.parseInt(sPid), Integer.parseInt(sUid));
            }
        } catch (NumberFormatException e) {
            pw.println("dump args is illegal!");
        }
    }

    private static void checkHasCheckType(PrintWriter pw, Context context) {
        if (sHasCheckType && sPkg != null) {
            AwareAppKeyBackgroup.getInstance().dumpCheckPkgType(pw, context, sPkg);
        }
    }

    private static void checkHasPkgName(PrintWriter pw, Context context) {
        if (sHasPkgName && sHasState && sPkg != null && sState != null) {
            AwareAppKeyBackgroup.getInstance().dumpCheckStateByPkg(pw, context, Integer.parseInt(sState), sPkg);
        }
    }

    private static void checkHasPid(PrintWriter pw, Context context) {
        if (sHasPid && sHasState && sPid != null && sState != null) {
            AwareAppKeyBackgroup.getInstance().dumpCheckStateByPid(pw, context, Integer.parseInt(sState), Integer.parseInt(sPid));
        }
    }

    private static boolean initArgs(Context context, String[] args) {
        sHasPid = false;
        sHasUid = false;
        sHasState = false;
        sHasCheckType = false;
        sHasPkgName = false;
        sHasEvent = false;
        sHasFakeEvent = false;
        sPid = null;
        sState = null;
        sPkg = null;
        sUid = null;
        sEvent = null;
        if (args != null) {
            for (String arg : args) {
                if ("enable".equals(arg)) {
                    AwareAppKeyBackgroup.enable(context);
                    return true;
                } else if ("disable".equals(arg)) {
                    AwareAppKeyBackgroup.disable();
                    return true;
                } else if ("enable_log".equals(arg)) {
                    AwareAppKeyBackgroup.enableDebug();
                    AwareAudioFocusManager.enableDebug();
                    return true;
                } else if ("disable_log".equals(arg)) {
                    AwareAppKeyBackgroup.disableDebug();
                    return true;
                } else {
                    initVaules(arg);
                    intArgsEx(arg);
                }
            }
        }
        return false;
    }

    private static void intArgsEx(String arg) {
        if ("--fake-event".equals(arg)) {
            sHasFakeEvent = true;
        }
        if ("-p".equals(arg)) {
            sHasPid = true;
        }
        if ("-u".equals(arg)) {
            sHasUid = true;
        }
        if ("-s".equals(arg)) {
            sHasState = true;
        }
        if ("-n".equals(arg)) {
            sHasPkgName = true;
        }
        if ("-t".equals(arg)) {
            sHasCheckType = true;
        }
        if ("-e".equals(arg)) {
            sHasEvent = true;
        }
    }

    private static void initVaules(String arg) {
        if (sHasPid && sPid == null) {
            sPid = arg;
        }
        if (sHasUid && sUid == null) {
            sUid = arg;
        }
        if (sHasState && sState == null) {
            sState = arg;
        }
        if (sHasEvent && sEvent == null) {
            sEvent = arg;
        }
        if (sHasPkgName && sPkg == null) {
            sPkg = arg;
        }
        if (sHasCheckType && sPkg == null) {
            sPkg = arg;
        }
    }
}
