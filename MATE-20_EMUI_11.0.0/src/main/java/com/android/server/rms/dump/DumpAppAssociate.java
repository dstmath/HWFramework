package com.android.server.rms.dump;

import android.content.Context;
import android.rms.utils.Utils;
import com.android.server.mtm.utils.SparseSet;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import java.io.PrintWriter;

public final class DumpAppAssociate {
    private static final String TAG = "DumpAppAssociate";

    private static boolean dumpIntelligentRecg(PrintWriter pw, Context context, String[] args) {
        if (!Utils.scanArgs(args, "intlrecg")) {
            return false;
        }
        DumpIntelligentRecg.dumpIntelligentRecg(pw, context, args);
        return true;
    }

    private static boolean dumpBaseDump(AwareAppAssociate assoc, PrintWriter pw, String arg) {
        if ("enable".equals(arg)) {
            AwareAppAssociate.enable();
            return true;
        } else if ("disable".equals(arg)) {
            AwareAppAssociate.disable();
            return true;
        } else if ("enable_log".equals(arg)) {
            AwareAppAssociate.enableDebug();
            return true;
        } else if ("disable_log".equals(arg)) {
            AwareAppAssociate.disableDebug();
            return true;
        } else if ("enable_record".equals(arg)) {
            AwareAppAssociate.enableRecord();
            return true;
        } else if ("disable_record".equals(arg)) {
            AwareAppAssociate.disableRecord();
            return true;
        } else if ("-home".equals(arg)) {
            assoc.dumpHome(pw);
            return true;
        } else if ("-prev".equals(arg)) {
            assoc.dumpPrev(pw);
            return true;
        } else if ("-w".equals(arg)) {
            assoc.dumpWidget(pw);
            return true;
        } else if ("-vw".equals(arg)) {
            assoc.dumpVisibleWindow(pw);
            return true;
        } else if (!"-pkgProc".equals(arg)) {
            return false;
        } else {
            assoc.dumpPkgProc(pw);
            return true;
        }
    }

    private static void dumpAssocDump(boolean acquire, AwareAppAssociate assoc, String pid, PrintWriter pw) {
        if (pid == null) {
            assoc.dump(pw);
            return;
        }
        try {
            int tmp = Integer.parseInt(pid);
            if (acquire) {
                SparseSet strong = new SparseSet();
                assoc.getAssocListForPid(tmp, strong);
                pw.print("strong:" + strong + "\n");
                return;
            }
            assoc.dumpPid(tmp, pw);
        } catch (NumberFormatException e) {
            pw.println("Bad pid:" + pid);
        }
    }

    public static void dumpAppAssociate(PrintWriter pw, Context context, String[] args) {
        String pid = null;
        boolean hasPid = false;
        boolean record = false;
        boolean acquire = false;
        AwareAppAssociate assoc = AwareAppAssociate.getInstance();
        if (!dumpIntelligentRecg(pw, context, args)) {
            if (args != null) {
                int length = args.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    String arg = args[i];
                    if (arg != null) {
                        if (!dumpBaseDump(assoc, pw, arg)) {
                            if ("-p".equals(arg)) {
                                hasPid = true;
                            } else if ("-r".equals(arg)) {
                                record = true;
                            } else if ("-f".equals(arg)) {
                                assoc.dumpFore(pw);
                                return;
                            } else if ("-fr".equals(arg)) {
                                assoc.dumpRecentFore(pw);
                                return;
                            } else if ("-a".equals(arg)) {
                                acquire = true;
                            } else if (hasPid) {
                                pid = arg;
                                break;
                            }
                        } else {
                            return;
                        }
                    }
                    i++;
                }
            }
            if (record) {
                assoc.dumpRecord(pw);
            } else {
                dumpAssocDump(acquire, assoc, pid, pw);
            }
        }
    }
}
