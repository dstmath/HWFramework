package com.android.server.rms.iaware.memory.utils;

import com.android.server.rms.iaware.feature.MemoryFeature;
import com.android.server.rms.iaware.feature.RFeature;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public final class MemoryDumpsys {
    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static final boolean doDumpsys(RFeature feature, FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args == null || args.length <= 0 || feature == null || !(feature instanceof MemoryFeature) || !"--test-Memory".equals(args[0]) || args.length != 2) {
            return false;
        }
        if ("enable_eventTracker".equals(args[1])) {
            EventTracker.getInstance().enableTracker();
            return true;
        } else if (!"disable_eventTracker".equals(args[1])) {
            return false;
        } else {
            EventTracker.getInstance().disableTracker();
            return true;
        }
    }
}
