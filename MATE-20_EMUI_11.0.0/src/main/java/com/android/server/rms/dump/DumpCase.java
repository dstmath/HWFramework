package com.android.server.rms.dump;

import android.content.Context;
import android.rms.utils.Utils;
import java.io.PrintWriter;

public final class DumpCase {
    public static boolean dump(Context context, PrintWriter pw, String[] args) {
        if (!Utils.DEBUG || !Utils.scanArgs(args, "--dump-ActivityManagerService")) {
            return false;
        }
        DumpActivityManagerService.lockAms(context, pw, args);
        return true;
    }
}
