package com.huawei.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.Phonenumber;

public final class PhonenumberEx {

    public static class PhoneNumberEx {
        private Phonenumber.PhoneNumber pNumber;

        protected PhoneNumberEx(Phonenumber.PhoneNumber number) {
            this.pNumber = number;
        }

        public Phonenumber.PhoneNumber getPhoneNumber() {
            return this.pNumber;
        }
    }

    private PhonenumberEx() {
    }
}
