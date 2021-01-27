package ohos.com.sun.org.apache.xerces.internal.dom;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import ohos.com.sun.org.apache.xerces.internal.utils.SecuritySupport;

public class DOMMessageFormatter {
    public static final String DOM_DOMAIN = "http://www.w3.org/dom/DOMTR";
    public static final String SERIALIZER_DOMAIN = "http://apache.org/xml/serializer";
    public static final String XML_DOMAIN = "http://www.w3.org/TR/1998/REC-xml-19980210";
    private static ResourceBundle domResourceBundle;
    private static Locale locale;
    private static ResourceBundle serResourceBundle;
    private static ResourceBundle xmlResourceBundle;

    DOMMessageFormatter() {
        locale = Locale.getDefault();
    }

    public static String formatMessage(String str, String str2, Object[] objArr) throws MissingResourceException {
        ResourceBundle resourceBundle = getResourceBundle(str);
        if (resourceBundle == null) {
            init();
            resourceBundle = getResourceBundle(str);
            if (resourceBundle == null) {
                throw new MissingResourceException("Unknown domain" + str, null, str2);
            }
        }
        try {
            String str3 = str2 + ": " + resourceBundle.getString(str2);
            if (objArr != null) {
                try {
                    str3 = MessageFormat.format(str3, objArr);
                } catch (Exception unused) {
                    str3 = resourceBundle.getString("FormatFailed") + " " + resourceBundle.getString(str2);
                }
            }
            if (str3 != null) {
                return str3;
            }
            if (objArr.length > 0) {
                StringBuffer stringBuffer = new StringBuffer(str2);
                stringBuffer.append('?');
                for (int i = 0; i < objArr.length; i++) {
                    if (i > 0) {
                        stringBuffer.append('&');
                    }
                    stringBuffer.append(String.valueOf(objArr[i]));
                }
            }
            return str2;
        } catch (MissingResourceException unused2) {
            throw new MissingResourceException(str2, resourceBundle.getString("BadMessageKey"), str2);
        }
    }

    static ResourceBundle getResourceBundle(String str) {
        if (str == DOM_DOMAIN || str.equals(DOM_DOMAIN)) {
            return domResourceBundle;
        }
        if (str == "http://www.w3.org/TR/1998/REC-xml-19980210" || str.equals("http://www.w3.org/TR/1998/REC-xml-19980210")) {
            return xmlResourceBundle;
        }
        if (str == SERIALIZER_DOMAIN || str.equals(SERIALIZER_DOMAIN)) {
            return serResourceBundle;
        }
        return null;
    }

    public static void init() {
        Locale locale2 = locale;
        if (locale2 != null) {
            domResourceBundle = SecuritySupport.getResourceBundle("com.sun.org.apache.xerces.internal.impl.msg.DOMMessages", locale2);
            serResourceBundle = SecuritySupport.getResourceBundle("com.sun.org.apache.xerces.internal.impl.msg.XMLSerializerMessages", locale);
            xmlResourceBundle = SecuritySupport.getResourceBundle("com.sun.org.apache.xerces.internal.impl.msg.XMLMessages", locale);
            return;
        }
        domResourceBundle = SecuritySupport.getResourceBundle("com.sun.org.apache.xerces.internal.impl.msg.DOMMessages");
        serResourceBundle = SecuritySupport.getResourceBundle("com.sun.org.apache.xerces.internal.impl.msg.XMLSerializerMessages");
        xmlResourceBundle = SecuritySupport.getResourceBundle("com.sun.org.apache.xerces.internal.impl.msg.XMLMessages");
    }

    public static void setLocale(Locale locale2) {
        locale = locale2;
    }
}
