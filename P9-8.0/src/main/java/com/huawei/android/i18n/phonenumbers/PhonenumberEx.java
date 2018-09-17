package com.huawei.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;

public final class PhonenumberEx {

    public static class PhoneNumberEx {
        private PhoneNumber pNumber;

        protected PhoneNumberEx(PhoneNumber number) {
            this.pNumber = number;
        }

        public PhoneNumber getPhoneNumber() {
            return this.pNumber;
        }
    }

    private PhonenumberEx() {
    }
}
