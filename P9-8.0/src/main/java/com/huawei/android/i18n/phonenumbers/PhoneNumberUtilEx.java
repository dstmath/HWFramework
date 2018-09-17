package com.huawei.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.huawei.android.i18n.phonenumbers.PhonenumberEx.PhoneNumberEx;

public class PhoneNumberUtilEx {
    private static final Object sLock = new Object();
    private PhoneNumberUtil mPhoneNumberUtil;

    private PhoneNumberUtilEx(PhoneNumberUtil pnUtil) {
        this.mPhoneNumberUtil = pnUtil;
    }

    public static PhoneNumberUtilEx getInstance() {
        PhoneNumberUtilEx phoneNumberUtilEx;
        synchronized (sLock) {
            phoneNumberUtilEx = new PhoneNumberUtilEx(PhoneNumberUtil.getInstance());
        }
        return phoneNumberUtilEx;
    }

    public PhoneNumberEx parse(String numberToParse, String defaultRegion) throws NumberParseExceptionEx {
        try {
            return new PhoneNumberEx(this.mPhoneNumberUtil.parse(numberToParse, defaultRegion));
        } catch (NumberParseException e) {
            throw new NumberParseExceptionEx(e);
        }
    }

    public AsYouTypeFormatterEx getAsYouTypeFormatter(String regionCode) {
        return new AsYouTypeFormatterEx(this.mPhoneNumberUtil.getAsYouTypeFormatter(regionCode));
    }
}
