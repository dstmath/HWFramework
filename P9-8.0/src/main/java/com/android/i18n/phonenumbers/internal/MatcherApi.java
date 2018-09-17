package com.android.i18n.phonenumbers.internal;

import com.android.i18n.phonenumbers.Phonemetadata.PhoneNumberDesc;

public interface MatcherApi {
    boolean matchesNationalNumber(String str, PhoneNumberDesc phoneNumberDesc, boolean z);
}
