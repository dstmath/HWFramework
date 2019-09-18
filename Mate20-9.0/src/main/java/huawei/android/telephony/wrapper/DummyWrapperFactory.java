package huawei.android.telephony.wrapper;

import huawei.android.telephony.wrapper.WrapperFactory;

public class DummyWrapperFactory implements WrapperFactory.Factory {
    public HuaweiTelephonyManagerWrapper getHuaweiTelephonyManagerWrapper() {
        return DummyHuaweiTelephonyManagerWrapper.getInstance();
    }

    public MSimTelephonyManagerWrapper getMSimTelephonyManagerWrapper() {
        return DummyMSimTelephonyManagerWrapper.getInstance();
    }
}
