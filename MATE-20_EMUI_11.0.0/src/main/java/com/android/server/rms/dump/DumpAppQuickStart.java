package com.android.server.rms.dump;

import com.android.server.rms.iaware.resource.StartResParallelManager;
import java.io.PrintWriter;

public final class DumpAppQuickStart {
    public static void dump(PrintWriter pw, String[] args) {
        if (pw != null && args != null) {
            if (args.length < 3) {
                pw.println("  Need more args");
            } else if ("setPreloadEnable".equals(args[1])) {
                String enable = args[2];
                if ("true".equals(enable)) {
                    StartResParallelManager.getInstance().dumpSetPreloadEnable(pw, true);
                } else if ("false".equals(enable)) {
                    StartResParallelManager.getInstance().dumpSetPreloadEnable(pw, false);
                }
            } else {
                pw.println("  Bad command");
            }
        }
    }
}
