package org.bouncycastle.i18n;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.TimeZone;

public class ErrorBundle extends MessageBundle {
    public static final String DETAIL_ENTRY = "details";
    public static final String SUMMARY_ENTRY = "summary";

    public ErrorBundle(String str, String str2) throws NullPointerException {
        super(str, str2);
    }

    public ErrorBundle(String str, String str2, String str3) throws NullPointerException, UnsupportedEncodingException {
        super(str, str2, str3);
    }

    public ErrorBundle(String str, String str2, String str3, Object[] objArr) throws NullPointerException, UnsupportedEncodingException {
        super(str, str2, str3, objArr);
    }

    public ErrorBundle(String str, String str2, Object[] objArr) throws NullPointerException {
        super(str, str2, objArr);
    }

    public String getDetail(Locale locale) throws MissingEntryException {
        return getEntry(DETAIL_ENTRY, locale, TimeZone.getDefault());
    }

    public String getDetail(Locale locale, TimeZone timeZone) throws MissingEntryException {
        return getEntry(DETAIL_ENTRY, locale, timeZone);
    }

    public String getSummary(Locale locale) throws MissingEntryException {
        return getEntry(SUMMARY_ENTRY, locale, TimeZone.getDefault());
    }

    public String getSummary(Locale locale, TimeZone timeZone) throws MissingEntryException {
        return getEntry(SUMMARY_ENTRY, locale, timeZone);
    }
}
