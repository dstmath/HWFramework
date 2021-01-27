package huawei.android.telephony.wrapper;

public class WrapperFactory {
    private static Factory mInstance = new DummyWrapperFactory();

    public interface Factory {
        HuaweiTelephonyManagerWrapper getHuaweiTelephonyManagerWrapper();

        MSimTelephonyManagerWrapper getMSimTelephonyManagerWrapper();
    }

    static {
        if (mInstance == null) {
        }
    }

    public static HuaweiTelephonyManagerWrapper getHuaweiTelephonyManagerWrapper() {
        return mInstance.getHuaweiTelephonyManagerWrapper();
    }

    public static MSimTelephonyManagerWrapper getMSimTelephonyManagerWrapper() {
        return mInstance.getMSimTelephonyManagerWrapper();
    }
}
