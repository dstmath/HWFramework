package huawei.android.net.slice;

public interface NetworkSliceStateListener {
    public static final int SLICE_ENVIRONMENT_CODE_AVAILABLE = 0;
    public static final int SLICE_ENVIRONMENT_CODE_DEFAULT_DATA_NOT_ON_MAIN_CARD = 4;
    public static final int SLICE_ENVIRONMENT_CODE_MOBILE_DATA_DISABLED = 2;
    public static final int SLICE_ENVIRONMENT_CODE_NOT_IN_NR_SA = 1;
    public static final int SLICE_ENVIRONMENT_CODE_TOPLIMIT = 8;

    void onNetworkSliceStateChanged(int i);
}
