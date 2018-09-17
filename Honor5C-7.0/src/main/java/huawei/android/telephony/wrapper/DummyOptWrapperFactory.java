package huawei.android.telephony.wrapper;

import huawei.android.telephony.wrapper.OptWrapperFactory.Factory;

public class DummyOptWrapperFactory implements Factory {
    public MSimUiccControllerWrapper getMSimUiccControllerWrapper() {
        return DummyMSimUiccControllerWrapper.getInstance();
    }

    public IIccPhoneBookMSimWrapper getIIccPhoneBookMSimWrapper() {
        return DummyIIccPhoneBookMSimWrapper.getInstance();
    }

    public PhoneWrapper getPhoneWrapper() {
        return DummyPhoneWrapper.getInstance();
    }
}
