package com.android.server.rms.dump;

import android.content.Context;
import com.android.server.pfw.autostartup.comm.XmlConst;
import com.android.server.rms.iaware.appmng.AlarmManagerDumpRadar;
import com.android.server.rms.iaware.appmng.AwareWakeUpManager;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class DumpAlarmManager {
    public static final String TAG = "DumpAlarmManager";
    private static volatile Map<String, Consumer<Params>> consumers = new HashMap();

    static class Params {
        public String[] args;
        public Context context;
        public PrintWriter pw;

        public Params(Context context2, PrintWriter pw2, String[] args2) {
            this.context = context2;
            this.pw = pw2;
            this.args = args2;
        }
    }

    public static final void dump(Context context, PrintWriter pw, String[] args) {
        if (pw != null) {
            if (args == null || args.length < 2 || args[1] == null) {
                pw.println("  Bad command");
                return;
            }
            String cmd = args[1];
            synchronized (DumpAlarmManager.class) {
                if (consumers.isEmpty()) {
                    consumers.put("delay", $$Lambda$DumpAlarmManager$sTyRBtkgkC1zrLkNBktCJFPBVgI.INSTANCE);
                    consumers.put("bigData", $$Lambda$DumpAlarmManager$6WCcZOOF0NyNxY14ThrN_Sa9_K8.INSTANCE);
                    consumers.put("debugLog", $$Lambda$DumpAlarmManager$yU5hWyGyhPsIICiAzmBOdnA7mnA.INSTANCE);
                    consumers.put("debug", $$Lambda$DumpAlarmManager$TAuUJte3WTeURAu60wUGpaASTRk.INSTANCE);
                    consumers.put("param", $$Lambda$DumpAlarmManager$cgrs9dHL55EiLtqSDRMXo9OSo.INSTANCE);
                }
                Consumer<Params> func = consumers.get(cmd);
                if (func == null) {
                    pw.println("  Bad command: " + cmd);
                    return;
                }
                try {
                    func.accept(new Params(context, pw, args));
                } catch (Exception e) {
                    pw.println("  Bad command:");
                    pw.println(e.toString());
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static void setDebugSwitch(Context mContext, PrintWriter pw, String[] args) {
        if (pw != null) {
            if (XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_.equals(args[2])) {
                AwareWakeUpManager.getInstance().setDebugSwitch(true);
                pw.println("  debug on");
            } else {
                AwareWakeUpManager.getInstance().setDebugSwitch(false);
                pw.println("  debug off");
            }
        }
    }

    /* access modifiers changed from: private */
    public static void dumpDebugLog(Context mContext, PrintWriter pw, String[] args) {
        if (pw != null) {
            AwareWakeUpManager.getInstance().dumpDebugLog(pw);
        }
    }

    /* access modifiers changed from: private */
    public static void delay(Context mContext, PrintWriter pw, String[] args) {
        if (pw != null) {
            if (args == null || args.length < 6) {
                pw.println("  delay parameter error!");
                return;
            }
            int userId = Integer.parseInt(args[2]);
            String pkg = args[3];
            String tag = args[4];
            long delay = Long.parseLong(args[5]);
            AwareWakeUpManager.getInstance().setDebugParam(userId, pkg, tag, delay);
            pw.println("  delay alarm set user:" + userId + " pkg:" + pkg + " tag:" + tag + " delay:" + delay);
        }
    }

    /* access modifiers changed from: private */
    public static void dumpBigData(Context mContext, PrintWriter pw, String[] args) {
        if (pw != null) {
            pw.println(AlarmManagerDumpRadar.getInstance().saveBigData(false));
        }
    }
}
