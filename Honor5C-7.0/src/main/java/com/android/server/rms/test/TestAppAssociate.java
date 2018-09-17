package com.android.server.rms.test;

import android.content.Context;
import android.util.ArraySet;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import java.io.PrintWriter;
import junit.framework.Assert;

public final class TestAppAssociate extends Assert {
    public static final String TAG = "TestAppAssociate";

    public static final void testAppAssociate(PrintWriter pw, Context context, String[] args) {
        String pid = null;
        boolean hasPid = false;
        boolean record = false;
        boolean acquire = false;
        AwareAppAssociate assoc = AwareAppAssociate.getInstance();
        if (assoc == null) {
            pw.print("Can not getInstance of AwareAppAssociate");
            return;
        }
        if (args != null) {
            int i = 0;
            int length = args.length;
            while (i < length) {
                String arg = args[i];
                if ("enable".equals(arg)) {
                    AwareAppAssociate.enable();
                    return;
                } else if ("disable".equals(arg)) {
                    AwareAppAssociate.disable();
                    return;
                } else if (arg.equals("enable_log")) {
                    AwareAppAssociate.enableDebug();
                    return;
                } else if (arg.equals("disable_log")) {
                    AwareAppAssociate.disableDebug();
                    return;
                } else if (arg.equals("enable_record")) {
                    AwareAppAssociate.enableRecord();
                    return;
                } else if (arg.equals("disable_record")) {
                    AwareAppAssociate.disableRecord();
                    return;
                } else {
                    if (arg.equals("-p")) {
                        hasPid = true;
                    } else if (arg.equals("-w")) {
                        assoc.dumpWidget(pw);
                        return;
                    } else if (arg.equals("-vw")) {
                        assoc.dumpVisibleWindow(pw);
                        return;
                    } else if (arg.equals("-r")) {
                        record = true;
                    } else if (arg.equals("-f")) {
                        assoc.dumpFore(pw);
                        return;
                    } else if (arg.equals("-a")) {
                        acquire = true;
                    } else if (arg.equals("-home")) {
                        assoc.dumpHome(pw);
                        return;
                    } else if (arg.equals("-prev")) {
                        assoc.dumpPrev(pw);
                        return;
                    } else if (hasPid) {
                        pid = arg;
                        break;
                    }
                    i++;
                }
            }
        }
        if (record) {
            assoc.dumpRecord(pw);
            return;
        }
        if (pid == null) {
            assoc.dump(pw);
        } else {
            try {
                int tmp = Integer.parseInt(pid);
                if (acquire) {
                    ArraySet<Integer> strong = new ArraySet();
                    assoc.getAssocListForPid(tmp, strong);
                    pw.print("strong:" + strong + "\n");
                } else {
                    assoc.dumpPid(tmp, pw);
                }
            } catch (NumberFormatException e) {
                pw.println("Bad pid:" + pid);
            }
        }
    }
}
