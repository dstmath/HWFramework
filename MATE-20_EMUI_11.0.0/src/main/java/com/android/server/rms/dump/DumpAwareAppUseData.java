package com.android.server.rms.dump;

import com.android.server.rms.iaware.appmng.AwareAppUseDataManager;
import java.io.PrintWriter;

public final class DumpAwareAppUseData {
    public static void dump(PrintWriter pw, String[] args) {
        if (pw != null && args != null) {
            if (args.length < 2) {
                pw.println("  Need more args");
            }
            String argsFirst = args[1];
            if ("getAppUseData".equals(argsFirst)) {
                AwareAppUseDataManager.getInstance().dumpAppUseData(pw);
            } else if ("filterPkgs".equals(argsFirst)) {
                AwareAppUseDataManager.getInstance().dumpFilterPkgs(pw);
            } else if ("importSysApps".equals(argsFirst)) {
                AwareAppUseDataManager.getInstance().dumpImportSysApps(pw);
            } else {
                pw.println("  Bad command");
            }
        }
    }
}
