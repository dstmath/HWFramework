package huawei.android.telephony.wrapper;

import huawei.android.telephony.wrapper.WrapperFactory.Factory;

public class DummyWrapperFactory implements Factory {
    public HuaweiTelephonyManagerWrapper getHuaweiTelephonyManagerWrapper() {
        return DummyHuaweiTelephonyManagerWrapper.getInstance();
    }

    public MSimTelephonyManagerWrapper getMSimTelephonyManagerWrapper() {
        return DummyMSimTelephonyManagerWrapper.getInstance();
    }
}
