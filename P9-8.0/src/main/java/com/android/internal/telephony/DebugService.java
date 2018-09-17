package com.android.internal.telephony;

import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class DebugService {
    private static String TAG = "DebugService";

    public DebugService() {
        log("DebugService:");
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args == null || args.length <= 0 || !(TextUtils.equals(args[0], "--metrics") || TextUtils.equals(args[0], "--metricsproto"))) {
            log("Dump telephony.");
            PhoneFactory.dump(fd, pw, args);
            return;
        }
        log("Collecting telephony metrics..");
        TelephonyMetrics.getInstance().dump(fd, pw, args);
    }

    private static void log(String s) {
        Rlog.d(TAG, "DebugService " + s);
    }
}
