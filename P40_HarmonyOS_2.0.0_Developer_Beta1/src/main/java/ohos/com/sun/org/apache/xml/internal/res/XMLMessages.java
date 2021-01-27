package ohos.com.sun.org.apache.xml.internal.res;

import java.text.MessageFormat;
import java.util.ListResourceBundle;
import java.util.Locale;
import ohos.com.sun.org.apache.xalan.internal.utils.SecuritySupport;

public class XMLMessages {
    protected static final String BAD_CODE = "BAD_CODE";
    protected static final String FORMAT_FAILED = "FORMAT_FAILED";
    private static ListResourceBundle XMLBundle = null;
    private static final String XML_ERROR_RESOURCES = "ohos.com.sun.org.apache.xml.internal.res.XMLErrorResources";
    protected Locale fLocale = Locale.getDefault();

    public void setLocale(Locale locale) {
        this.fLocale = locale;
    }

    public Locale getLocale() {
        return this.fLocale;
    }

    public static final String createXMLMessage(String str, Object[] objArr) {
        if (XMLBundle == null) {
            XMLBundle = SecuritySupport.getResourceBundle(XML_ERROR_RESOURCES);
        }
        ListResourceBundle listResourceBundle = XMLBundle;
        return listResourceBundle != null ? createMsg(listResourceBundle, str, objArr) : "Could not load any resource bundles.";
    }

    public static final String createMsg(ListResourceBundle listResourceBundle, String str, Object[] objArr) {
        boolean z;
        String string = str != null ? listResourceBundle.getString(str) : null;
        if (string == null) {
            string = listResourceBundle.getString("BAD_CODE");
            z = true;
        } else {
            z = false;
        }
        if (objArr != null) {
            try {
                int length = objArr.length;
                for (int i = 0; i < length; i++) {
                    if (objArr[i] == null) {
                        objArr[i] = "";
                    }
                }
                string = MessageFormat.format(string, objArr);
            } catch (Exception unused) {
                string = listResourceBundle.getString("FORMAT_FAILED") + " " + string;
            }
        }
        if (!z) {
            return string;
        }
        throw new RuntimeException(string);
    }
}
