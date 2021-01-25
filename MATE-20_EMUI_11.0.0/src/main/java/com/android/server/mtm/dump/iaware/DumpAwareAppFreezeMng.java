package com.android.server.mtm.dump.iaware;

import android.content.Context;
import com.android.server.mtm.iaware.appmng.appfreeze.AwareAppFreezeMng;
import java.io.PrintWriter;

public final class DumpAwareAppFreezeMng {
    public static void dump(Context context, PrintWriter pw, String[] args) {
        if (args != null) {
            if (args.length >= 2 && args[1] != null) {
                if ("disable_log".equals(args[1])) {
                    AwareAppFreezeMng.disableDebug();
                    pw.println("  iAware fast freeze log disabled");
                } else if ("enable_log".equals(args[1])) {
                    AwareAppFreezeMng.enableDebug();
                    pw.println("  iAware fast freeze log enabled");
                }
            }
            AwareAppFreezeMng.getInstance().dump(pw);
        }
    }

    public static void dumpFreezeApp(Context context, PrintWriter pw, String[] args) {
        if (args != null && args.length >= 3) {
            String pkg = args[1];
            try {
                AwareAppFreezeMng.getInstance().dumpFreezeApp(pw, pkg, Integer.parseInt(args[2]));
            } catch (NumberFormatException e) {
                pw.println("The input Param is invalid!");
            }
        }
    }

    public static void dumpCheckBadApp(Context context, PrintWriter pw, String[] args) {
        if (args != null && args.length >= 3) {
            try {
                AwareAppFreezeMng.getInstance().dumpFreezeBadPid(pw, Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            } catch (NumberFormatException e) {
                pw.println("The input Param is invalid!");
            }
        }
    }
}
