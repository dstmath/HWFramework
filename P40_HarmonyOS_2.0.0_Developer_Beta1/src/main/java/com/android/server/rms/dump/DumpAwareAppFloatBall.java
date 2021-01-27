package com.android.server.rms.dump;

import com.android.server.rms.iaware.appmng.FloatBallAssociate;
import java.io.PrintWriter;

public final class DumpAwareAppFloatBall {
    private static final String GET_FLOAT_APP = "getFloatApp";

    public static void dump(PrintWriter pw, String[] args) {
        if (pw != null && args != null) {
            if (args.length < 2) {
                pw.println("  Need more args");
            } else if (GET_FLOAT_APP.equals(args[1])) {
                FloatBallAssociate.getInstance().dumpFloatApp(pw);
            } else {
                pw.println("  Bad command");
            }
        }
    }
}
