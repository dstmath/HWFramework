package com.android.server.rms.dump;

import android.content.Context;
import com.android.server.rms.dualfwk.AwareMiddleware;
import com.android.server.rms.iaware.appmng.AwareFakeActivityRecg;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.appmng.ContinuePowerDevMng;
import com.huawei.android.util.SlogEx;
import java.io.PrintWriter;
import junit.framework.Assert;

public final class DumpIntelligentRecg extends Assert {
    private static final int DUMP_BLUETOOTH = 3;
    private static final int DUMP_CHECK_VISIBLE_WINDOW = 4;
    private static final int DUMP_CONTINUE = 1;
    private static final int DUMP_FROZEN = 2;
    private static final int DUMP_RETURN = 0;
    private static final String TAG = "TestIntelligentRecg";

    public static void dumpIntelligentRecg(PrintWriter pw, Context context, String[] args) {
        if (pw != null && context != null && args != null) {
            dumpIntelligentRecgWrapper(pw, args);
        }
    }

    private static void dumpIntelligentRecgWrapper(PrintWriter pw, String[] args) {
        boolean hasCheckVw = false;
        int userId = 0;
        String pkg = null;
        int type = 3;
        int ret = 0;
        boolean hasType = false;
        String pkg2 = null;
        boolean hasUserId = false;
        boolean hasUid = false;
        for (String arg : args) {
            if (hasUid) {
                doHasUid(pw, ret, arg);
                return;
            }
            if (hasCheckVw) {
                if (!hasUserId) {
                    hasUserId = true;
                    try {
                        userId = Integer.parseInt(arg);
                    } catch (NumberFormatException e) {
                        SlogEx.e(TAG, "dump args is illegal!");
                    }
                } else {
                    if (pkg2 == null) {
                        pkg = arg;
                        pkg2 = 1;
                    } else if (!hasType) {
                        hasType = true;
                        type = Integer.parseInt(arg);
                    }
                }
            }
            int ret2 = dumpDump(pw, arg);
            if (ret2 == 0) {
                return;
            }
            if (ret2 == 2 || ret2 == 3) {
                hasUid = true;
                ret = ret2;
            } else {
                if (ret2 == 4) {
                    hasCheckVw = true;
                    ret = ret2;
                } else {
                    ret = ret2;
                }
            }
        }
        doVisibleWindowDump(pw, hasCheckVw, userId, pkg, type);
        dumpMultiArgs(pw, args);
    }

    private static void doVisibleWindowDump(PrintWriter pw, boolean hasCheckVw, int userId, String pkg, int type) {
        if (hasCheckVw) {
            AwareIntelligentRecg.getInstance().dumpIsVisibleWindow(pw, userId, pkg, type);
        }
    }

    private static void doHasUid(PrintWriter pw, int ret, String arg) {
        if (ret == 2) {
            try {
                AwareIntelligentRecg.getInstance().dumpFrozen(pw, Integer.parseInt(arg));
            } catch (NumberFormatException e) {
                SlogEx.e(TAG, "dump args is illegal!");
            }
        } else if (ret == 3) {
            AwareIntelligentRecg.getInstance().dumpBluetooth(pw, Integer.parseInt(arg));
        }
    }

    private static int dumpDump(PrintWriter pw, String arg) {
        if ("enable".equals(arg)) {
            AwareIntelligentRecg.commEnable();
            return 0;
        } else if ("disable".equals(arg)) {
            AwareIntelligentRecg.commDisable();
            return 0;
        } else if ("enable_log".equals(arg)) {
            AwareIntelligentRecg.enableDebug();
            return 0;
        } else if ("disable_log".equals(arg)) {
            AwareIntelligentRecg.disableDebug();
            return 0;
        } else if ("input".equals(arg)) {
            AwareIntelligentRecg.getInstance().dumpInputMethod(pw);
            return 0;
        } else if ("access".equals(arg)) {
            AwareIntelligentRecg.getInstance().dumpAccessibility(pw);
            return 0;
        } else if ("noclean".equals(arg)) {
            AwareIntelligentRecg.getInstance().dumpNotClean(pw);
            return 0;
        } else if ("gmscaller".equals(arg)) {
            AwareIntelligentRecg.getInstance().dumpGmsCallerList(pw);
            return 0;
        } else if ("bgc".equals(arg)) {
            AwareIntelligentRecg.getInstance().dumpBgCheckExcludeInfo(pw);
            return 0;
        } else if ("gmsapp".equals(arg)) {
            AwareIntelligentRecg.getInstance().dumpGmsAppList(pw);
            return 0;
        } else if ("regalive".equals(arg)) {
            AwareIntelligentRecg.getInstance().dumpKeepAlivePkgs(pw);
            return 0;
        } else if (!"sms".equals(arg)) {
            return dumpDumpEx(pw, arg);
        } else {
            AwareIntelligentRecg.getInstance().dumpSms(pw);
            return 0;
        }
    }

    private static int dumpDumpEx(PrintWriter pw, String arg) {
        if ("tts".equals(arg)) {
            AwareIntelligentRecg.getInstance().dumpTts(pw);
            return 0;
        } else if ("push".equals(arg)) {
            AwareIntelligentRecg.getInstance().dumpPushSdk(pw);
            return 0;
        } else if ("wallpaper".equals(arg)) {
            AwareIntelligentRecg.getInstance().dumpWallpaper(pw);
            return 0;
        } else if ("toast".equals(arg)) {
            AwareIntelligentRecg.getInstance().dumpToastWindow(pw);
            return 0;
        } else if ("dtts".equals(arg)) {
            AwareIntelligentRecg.getInstance().dumpDefaultTts(pw);
            return 0;
        } else if ("alarm".equals(arg)) {
            AwareIntelligentRecg.getInstance().dumpAlarms(pw);
            return 0;
        } else if ("alarmaction".equals(arg)) {
            AwareIntelligentRecg.getInstance().dumpAlarmActions(pw);
            return 0;
        } else if ("frozen".equals(arg)) {
            return 2;
        } else {
            if ("bluetooth".equals(arg)) {
                return 3;
            }
            if ("fa".equals(arg)) {
                AwareFakeActivityRecg.self().dumpRecgFakeActivity(pw, null);
                return 0;
            } else if ("kbg".equals(arg)) {
                AwareIntelligentRecg.getInstance().dumpKbgApp(pw);
                return 0;
            } else if ("apptype".equals(arg)) {
                AwareIntelligentRecg.getInstance().dumpDefaultAppType(pw);
                return 0;
            } else if ("checkvw".equals(arg)) {
                return 4;
            } else {
                if ("hwstop".equals(arg)) {
                    AwareIntelligentRecg.getInstance().dumpHwStopList(pw);
                    return 0;
                } else if ("camerarecord".equals(arg)) {
                    AwareIntelligentRecg.getInstance().dumpCameraRecording(pw);
                    return 0;
                } else if (!"screenrecord".equals(arg)) {
                    return dumpDumpExOther(pw, arg);
                } else {
                    AwareIntelligentRecg.getInstance().dumpScreenRecording(pw);
                    return 0;
                }
            }
        }
    }

    private static int dumpAppSceneInfo(PrintWriter pw, String[] args) {
        if (args.length < 5 || !"appscenemng".equals(args[2])) {
            return 0;
        }
        String pkg = args[3];
        try {
            AwareIntelligentRecg.getInstance().dumpAppSceneInfo(pw, Integer.parseInt(args[4]), pkg);
        } catch (NumberFormatException e) {
            SlogEx.e(TAG, "dump args is illegal");
        }
        return 0;
    }

    private static void dumpMultiArgs(PrintWriter pw, String[] args) {
        dumpAppSceneInfo(pw, args);
        dumpPreloadPkg(pw, args);
    }

    private static void dumpPreloadPkg(PrintWriter pw, String[] args) {
        String pkg;
        if (args.length >= 6 && "preload".equals(args[2]) && (pkg = args[3]) != null && !pkg.isEmpty()) {
            try {
                ContinuePowerDevMng.getInstance().dumpPreloadAppPkgs(pw, pkg, Integer.parseInt(args[4]), Integer.parseInt(args[5]));
            } catch (NumberFormatException e) {
                SlogEx.e(TAG, "dump args is illegal");
            }
        }
    }

    private static int dumpDumpExOther(PrintWriter pw, String arg) {
        if ("allowstartpkgs".equals(arg)) {
            AwareIntelligentRecg.getInstance().dumpAllowStartPkgs(pw);
            return 0;
        } else if ("gmscontrol".equals(arg)) {
            AwareIntelligentRecg.getInstance().dumpGmsControlInfo(pw);
            return 0;
        } else if (!"zapppkgs".equals(arg)) {
            return 1;
        } else {
            AwareMiddleware.getInstance().dumpZAppPkgs(pw);
            return 0;
        }
    }
}
