package com.android.i18n.phonenumbers.internal;

import com.android.i18n.phonenumbers.Phonemetadata.PhoneNumberDesc;
import com.android.i18n.phonenumbers.RegexCache;
import java.util.regex.Matcher;

public final class RegexBasedMatcher implements MatcherApi {
    private final RegexCache regexCache;

    public static MatcherApi create() {
        return new RegexBasedMatcher();
    }

    private RegexBasedMatcher() {
        this.regexCache = new RegexCache(100);
    }

    public boolean matchesNationalNumber(String nationalNumber, PhoneNumberDesc numberDesc, boolean allowPrefixMatch) {
        Matcher nationalNumberPatternMatcher = this.regexCache.getPatternForRegex(numberDesc.getNationalNumberPattern()).matcher(nationalNumber);
        if (nationalNumberPatternMatcher.matches()) {
            return true;
        }
        return allowPrefixMatch ? nationalNumberPatternMatcher.lookingAt() : false;
    }

    public boolean matchesPossibleNumber(String nationalNumber, PhoneNumberDesc numberDesc) {
        return this.regexCache.getPatternForRegex(numberDesc.getPossibleNumberPattern()).matcher(nationalNumber).matches();
    }
}
