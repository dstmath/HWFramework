package ohos.com.sun.org.apache.xerces.internal.jaxp.validation;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import ohos.com.sun.org.apache.xerces.internal.utils.SecuritySupport;

/* access modifiers changed from: package-private */
public final class JAXPValidationMessageFormatter {
    JAXPValidationMessageFormatter() {
    }

    public static String formatMessage(Locale locale, String str, Object[] objArr) throws MissingResourceException {
        ResourceBundle resourceBundle;
        String str2;
        if (locale != null) {
            resourceBundle = SecuritySupport.getResourceBundle("com.sun.org.apache.xerces.internal.impl.msg.JAXPValidationMessages", locale);
        } else {
            resourceBundle = SecuritySupport.getResourceBundle("com.sun.org.apache.xerces.internal.impl.msg.JAXPValidationMessages");
        }
        try {
            String string = resourceBundle.getString(str);
            if (objArr != null) {
                try {
                    str2 = MessageFormat.format(string, objArr);
                } catch (Exception unused) {
                    str2 = resourceBundle.getString("FormatFailed") + " " + resourceBundle.getString(str);
                }
            } else {
                str2 = string;
            }
            if (str2 != null) {
                return str2;
            }
            if (objArr.length > 0) {
                StringBuffer stringBuffer = new StringBuffer(str);
                stringBuffer.append('?');
                for (int i = 0; i < objArr.length; i++) {
                    if (i > 0) {
                        stringBuffer.append('&');
                    }
                    stringBuffer.append(String.valueOf(objArr[i]));
                }
            }
            return str;
        } catch (MissingResourceException unused2) {
            throw new MissingResourceException(str, resourceBundle.getString("BadMessageKey"), str);
        }
    }
}
