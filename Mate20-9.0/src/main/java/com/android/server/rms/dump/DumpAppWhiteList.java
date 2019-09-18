package com.android.server.rms.dump;

import android.content.Context;
import com.android.server.rms.iaware.appmng.AppMngConfig;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList;
import java.io.PrintWriter;

public final class DumpAppWhiteList {
    public static final void dumpAppWhiteList(PrintWriter pw, Context context, String[] args) {
        if (args != null) {
            int length = args.length;
            int i = 0;
            while (i < length) {
                String arg = args[i];
                if ("enable".equals(arg)) {
                    AwareDefaultConfigList.enable(context);
                    return;
                } else if ("disable".equals(arg)) {
                    AwareDefaultConfigList.disable();
                    return;
                } else if ("enable_log".equals(arg)) {
                    AwareDefaultConfigList.enableDebug();
                    return;
                } else if ("disable_log".equals(arg)) {
                    AwareDefaultConfigList.disableDebug();
                    return;
                } else if ("enable_restart".equals(arg)) {
                    AppMngConfig.setRestartFlag(true);
                    return;
                } else if ("disable_restart".equals(arg)) {
                    AppMngConfig.setRestartFlag(false);
                    return;
                } else {
                    i++;
                }
            }
        }
        AwareDefaultConfigList.getInstance().dump(null, pw);
    }
}
