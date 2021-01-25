package ohos.appexecfwk.utils;

import ohos.hiviewdfx.HiLogLabel;

public class PerfProfile {
    private static final HiLogLabel PERFPROFILE_LABEL = new HiLogLabel(3, 218108160, "AppPerfProfile");
    private static final int TIME_UNIT = 1000;

    public static long getTimeStamp() {
        return System.nanoTime();
    }

    public static void printDurationTime(long j, long j2, String str) {
        if (j2 < j) {
            AppLog.w(PERFPROFILE_LABEL, "PerfProfile::printDurationTime stop must greater or equal to start", new Object[0]);
        } else if (str == null || str.isEmpty()) {
            AppLog.w(PERFPROFILE_LABEL, "PerfProfile::printDurationTime message invalid", new Object[0]);
        } else {
            AppLog.i(PERFPROFILE_LABEL, "%{public}s spend time %{public}s us", str, String.valueOf((j2 - j) / 1000));
        }
    }
}
