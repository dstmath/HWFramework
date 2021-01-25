package com.android.server.mtm.dump;

import android.content.Context;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import java.io.PrintWriter;

public final class DumpAppMngListInfo {
    private static AwareAppMngSort sAppGroupMng = null;

    public static void dump(Context context, PrintWriter pw, String[] args) {
        sAppGroupMng = AwareAppMngSort.getInstance(context);
        if (sAppGroupMng != null) {
            if (args.length < 2) {
                pw.println("Bad command");
            } else if (args[1] != null && pw != null) {
                String cmd = args[1];
                if ("dump".equals(cmd)) {
                    if (args.length < 3) {
                        pw.println("Bad command :" + cmd);
                        return;
                    }
                    sAppGroupMng.dump(pw, args[2]);
                } else if ("enable_log".equals(cmd)) {
                    AwareAppMngSort.enableDebug();
                } else if ("disable_log".equals(cmd)) {
                    AwareAppMngSort.disableDebug();
                } else if ("enable_assoc".equals(cmd)) {
                    sAppGroupMng.enableAssocDebug();
                } else if ("disable_assoc".equals(cmd)) {
                    sAppGroupMng.disableAssocDebug();
                } else if ("getstatus_assoc".equals(cmd)) {
                    boolean status = sAppGroupMng.getAssocDebug();
                    pw.println("assoc status: " + status);
                } else if ("type".equals(cmd)) {
                    sAppGroupMng.dumpClassInfo(pw);
                } else if ("removeAlarm".equals(cmd)) {
                    sAppGroupMng.dumpRemoveAlarm(pw, args);
                } else if ("removeInvalidAlarm".equals(cmd)) {
                    sAppGroupMng.dumpRemoveInvalidAlarm(pw, args);
                } else if ("alarm".equals(cmd)) {
                    sAppGroupMng.dumpAlarm(pw, args);
                } else {
                    pw.println("Bad command :" + cmd);
                }
            }
        }
    }
}
