package com.android.server.rms.dump;

import com.android.server.rms.iaware.appmng.AwareComponentPreloadManager;
import java.io.PrintWriter;

public final class DumpAwareComponentPreMgr {
    public static void dump(PrintWriter pw, String[] args) {
        if (pw != null && args != null) {
            if (args.length < 2) {
                pw.println("  Need more args");
            }
            if ("info".equals(args[1])) {
                AwareComponentPreloadManager.getInstance().dumpInfo(pw);
            } else {
                pw.println("  Bad command");
            }
        }
    }
}
