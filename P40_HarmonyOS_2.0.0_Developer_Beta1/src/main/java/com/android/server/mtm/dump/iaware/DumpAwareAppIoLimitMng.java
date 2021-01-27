package com.android.server.mtm.dump.iaware;

import android.content.Context;
import com.android.server.mtm.iaware.appmng.appiolimit.AwareAppDefaultIoLimit;
import com.android.server.mtm.iaware.appmng.appiolimit.AwareAppIoLimitMng;
import java.io.PrintWriter;

public final class DumpAwareAppIoLimitMng {
    public static void dump(Context context, PrintWriter pw, String[] args) {
        if (args != null && args.length >= 2 && args[1] != null) {
            if ("disable_log".equals(args[1])) {
                AwareAppIoLimitMng.disableDebug();
                pw.println("  iAware App Io Limit log disabled");
            } else if ("enable_log".equals(args[1])) {
                AwareAppIoLimitMng.enableDebug();
                pw.println("  iAware App Io Limit log enabled");
            } else if ("light_iolimit".equals(args[1])) {
                AwareAppDefaultIoLimit.getInstance().dumpLightIolimit(pw);
                pw.println("  iAware App Light Io Limit pids");
            } else if ("heavy_iolimit".equals(args[1])) {
                AwareAppDefaultIoLimit.getInstance().dumpHeavyIolimit(pw);
                pw.println("  iAware App Heavy Io limit pids");
            }
        }
    }
}
