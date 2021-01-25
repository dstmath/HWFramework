package huawei.android.hwperformance;

public class HwPerformanceDummy implements HwPerformance {
    private static HwPerformance mInstance = new HwPerformanceDummy();

    public static HwPerformance getDefault() {
        return mInstance;
    }

    @Override // huawei.android.hwperformance.HwPerformance
    public int perfConfigSet(int[] tags, int[] values, String pkg_name) {
        return 0;
    }

    @Override // huawei.android.hwperformance.HwPerformance
    public int perfConfigGet(int[] tags, int[] values) {
        return 0;
    }
}
