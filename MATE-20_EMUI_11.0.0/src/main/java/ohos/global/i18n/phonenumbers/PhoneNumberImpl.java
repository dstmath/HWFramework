package ohos.global.i18n.phonenumbers;

import com.huawei.android.i18n.phonenumbers.PhonenumberEx;

public class PhoneNumberImpl implements PhoneNumber {
    private PhonenumberEx.PhoneNumberEx num;

    public PhoneNumberImpl(PhonenumberEx.PhoneNumberEx phoneNumberEx) {
        this.num = phoneNumberEx;
    }

    public PhonenumberEx.PhoneNumberEx getPhoneNumber() {
        return this.num;
    }
}
