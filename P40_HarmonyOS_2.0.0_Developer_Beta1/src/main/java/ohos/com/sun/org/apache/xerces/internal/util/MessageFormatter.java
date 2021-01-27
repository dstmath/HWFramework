package ohos.com.sun.org.apache.xerces.internal.util;

import java.util.Locale;
import java.util.MissingResourceException;

public interface MessageFormatter {
    String formatMessage(Locale locale, String str, Object[] objArr) throws MissingResourceException;
}
