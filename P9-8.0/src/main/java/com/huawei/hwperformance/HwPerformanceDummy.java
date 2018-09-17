package com.huawei.hwperformance;

public class HwPerformanceDummy implements HwPerformance {
    private static HwPerformance mInstance = new HwPerformanceDummy();

    public static HwPerformance getDefault() {
        return mInstance;
    }

    public int perfConfigSet(int[] tags, int[] values, String pkg_name) {
        return 0;
    }

    public int perfConfigGet(int[] tags, int[] values) {
        return 0;
    }
}
