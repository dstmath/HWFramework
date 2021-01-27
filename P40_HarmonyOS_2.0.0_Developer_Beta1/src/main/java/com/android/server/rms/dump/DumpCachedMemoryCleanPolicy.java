package com.android.server.rms.dump;

import com.android.server.rms.iaware.memory.policy.CachedMemoryCleanPolicy;
import java.io.PrintWriter;

public final class DumpCachedMemoryCleanPolicy {
    public static void dump(PrintWriter pw, String[] args) {
        if (pw != null && args != null) {
            if (args.length < 2) {
                pw.println("  Need more args");
                return;
            }
            String argsFirst = args[1];
            if ("info".equals(argsFirst)) {
                CachedMemoryCleanPolicy.getInstance().dumpInfo(pw);
            } else if ("cachedKill".equals(argsFirst)) {
                dumpCachedKill(pw, args);
            } else if ("cachedKillWithRecord".equals(argsFirst)) {
                dumpCachedKillWithRecord(pw, args);
            } else {
                pw.println("  Bad command");
            }
        }
    }

    private static void dumpCachedKill(PrintWriter pw, String[] args) {
        if (args.length < 3) {
            pw.println("  Need more args");
        }
        try {
            CachedMemoryCleanPolicy.getInstance().dumpCachedKill(pw, Integer.parseInt(args[2]));
        } catch (NumberFormatException e) {
            pw.println("The second param need num, it means pid.");
        }
    }

    private static void dumpCachedKillWithRecord(PrintWriter pw, String[] args) {
        if (args.length < 6) {
            pw.println("  Need more args");
        }
        try {
            CachedMemoryCleanPolicy.getInstance().dumpCachedKillWithRecord(pw, Integer.parseInt(args[2]), Integer.parseInt(args[3]), args[4], args[5]);
        } catch (NumberFormatException e) {
            pw.println("The second param need num, it means pid.");
        }
    }
}
