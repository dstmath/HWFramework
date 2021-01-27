package com.android.server.mtm.dump.iaware;

import android.content.Context;
import com.android.server.mtm.iaware.appmng.appswap.AwareAppSwapMng;
import java.io.PrintWriter;

public final class DumpAwareAppSwapMng {
    private static final String DISABLE_LOG = "disable_log";
    private static final String ENABLE_LOG = "enable_log";
    private static final String SWAP_PKG = "pkg";
    private static final String SWAP_POLICY = "swap_all";

    public static void dump(Context context, PrintWriter pw, String[] args) {
        if (args != null && args.length >= 2 && args[1] != null) {
            if (DISABLE_LOG.equals(args[1])) {
                AwareAppSwapMng.disableDebug();
                pw.println("  iAware App swap log disabled");
            } else if (ENABLE_LOG.equals(args[1])) {
                AwareAppSwapMng.enableDebug();
                pw.println("  iAware App swap log enabled");
            } else if (SWAP_POLICY.equals(args[1])) {
                AwareAppSwapMng.getInstance().dumpAppSwap(pw);
            } else if (SWAP_PKG.equals(args[1])) {
                AwareAppSwapMng.getInstance().dumpAppSwapByPkg(pw, args[2]);
            } else {
                pw.println("Bad command :" + args[1]);
            }
        }
    }
}
