package com.android.server.rms.iaware.cpu;

import com.android.server.rms.iaware.feature.RFeature;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public final class CpusetDumpsys {
    public static final boolean doDumpsys(RFeature feature, FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args == null || args.length <= 0 || feature == null || !(feature instanceof CPUFeature)) {
            return false;
        }
        CPUFeature cf = (CPUFeature) feature;
        if (!"--test-Cpuset".equals(args[0])) {
            return false;
        }
        int length = args.length;
        int i = 0;
        while (i < length) {
            String arg = args[i];
            if ("enable_log".equals(arg)) {
                cf.featureSwitch(1, true);
                break;
            } else if ("disable_log".equals(arg)) {
                cf.featureSwitch(1, false);
                break;
            } else {
                i++;
            }
        }
        return true;
    }
}
