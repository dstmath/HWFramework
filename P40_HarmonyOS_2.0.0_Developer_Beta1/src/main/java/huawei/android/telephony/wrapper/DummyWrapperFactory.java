package huawei.android.telephony.wrapper;

import huawei.android.telephony.wrapper.WrapperFactory;

public class DummyWrapperFactory implements WrapperFactory.Factory {
    @Override // huawei.android.telephony.wrapper.WrapperFactory.Factory
    public HuaweiTelephonyManagerWrapper getHuaweiTelephonyManagerWrapper() {
        return DummyHuaweiTelephonyManagerWrapper.getInstance();
    }

    @Override // huawei.android.telephony.wrapper.WrapperFactory.Factory
    public MSimTelephonyManagerWrapper getMSimTelephonyManagerWrapper() {
        return DummyMSimTelephonyManagerWrapper.getInstance();
    }
}
