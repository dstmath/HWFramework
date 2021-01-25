package com.android.server.mtm.dump;

import com.android.server.rms.iaware.HwStartWindowCache;
import java.io.PrintWriter;

public final class DumpMemInfo {
    public static void dump(PrintWriter pw, String[] args) {
        if (pw != null) {
            if (args == null || args.length < 3 || args[1] == null) {
                pw.println("  Bad command");
                return;
            }
            String cmd = args[1];
            String what = args[2];
            if ("startwindow".equals(cmd) && "cache".equals(what)) {
                pw.println(HwStartWindowCache.getInstance().dump());
            }
        }
    }
}
