package com.android.server.rms.dump;

import android.content.Context;
import android.util.Slog;
import com.android.server.rms.iaware.appmng.AwareFakeActivityRecg;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import java.io.PrintWriter;
import junit.framework.Assert;

public final class DumpIntelligentRecg extends Assert {
    private static final int DUMP_BLUETOOTH = 3;
    private static final int DUMP_CONTINUE = 1;
    private static final int DUMP_FROZEN = 2;
    private static final int DUMP_RETURN = 0;
    private static final String TAG = "TestIntelligentRecg";

    public static final void dumpIntelligentRecg(PrintWriter pw, Context context, String[] args) {
        int i = 0;
        int ret = 0;
        if (pw != null && context != null && args != null) {
            boolean hasUid = false;
            int length = args.length;
            while (i < length) {
                String arg = args[i];
                if (hasUid) {
                    if (ret == 2) {
                        try {
                            AwareIntelligentRecg.getInstance().dumpFrozen(pw, Integer.parseInt(arg));
                        } catch (NumberFormatException e) {
                            Slog.e(TAG, "dump args is illegal!");
                        }
                    } else if (ret == 3) {
                        AwareIntelligentRecg.getInstance().dumpBluetooth(pw, Integer.parseInt(arg));
                    }
                    return;
                }
                ret = dumpDump(pw, arg);
                if (ret != 0) {
                    if (ret == 2 || ret == 3) {
                        hasUid = true;
                    }
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    private static final int dumpDump(PrintWriter pw, String arg) {
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
        } else if (!"gmscaller".equals(arg)) {
            return dumpDumpEx(pw, arg);
        } else {
            AwareIntelligentRecg.getInstance().dumpGmsCallerList(pw);
            return 0;
        }
    }

    private static final int dumpDumpEx(PrintWriter pw, String arg) {
        if ("tts".equals(arg)) {
            AwareIntelligentRecg.getInstance().dumpTts(pw);
            return 0;
        } else if ("push".equals(arg)) {
            AwareIntelligentRecg.getInstance().dumpPushSDK(pw);
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
            } else if (!"apptype".equals(arg)) {
                return 1;
            } else {
                AwareIntelligentRecg.getInstance().dumpDefaultAppType(pw);
                return 0;
            }
        }
    }
}
