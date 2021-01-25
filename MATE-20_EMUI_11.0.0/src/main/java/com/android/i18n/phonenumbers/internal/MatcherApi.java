package com.android.i18n.phonenumbers.internal;

import com.android.i18n.phonenumbers.Phonemetadata;

public interface MatcherApi {
    boolean matchNationalNumber(CharSequence charSequence, Phonemetadata.PhoneNumberDesc phoneNumberDesc, boolean z);
}
