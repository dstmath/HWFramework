package org.bouncycastle.i18n;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.TimeZone;

public class MessageBundle extends TextBundle {
    public static final String TITLE_ENTRY = "title";

    public MessageBundle(String str, String str2) throws NullPointerException {
        super(str, str2);
    }

    public MessageBundle(String str, String str2, String str3) throws NullPointerException, UnsupportedEncodingException {
        super(str, str2, str3);
    }

    public MessageBundle(String str, String str2, String str3, Object[] objArr) throws NullPointerException, UnsupportedEncodingException {
        super(str, str2, str3, objArr);
    }

    public MessageBundle(String str, String str2, Object[] objArr) throws NullPointerException {
        super(str, str2, objArr);
    }

    public String getTitle(Locale locale) throws MissingEntryException {
        return getEntry(TITLE_ENTRY, locale, TimeZone.getDefault());
    }

    public String getTitle(Locale locale, TimeZone timeZone) throws MissingEntryException {
        return getEntry(TITLE_ENTRY, locale, timeZone);
    }
}
