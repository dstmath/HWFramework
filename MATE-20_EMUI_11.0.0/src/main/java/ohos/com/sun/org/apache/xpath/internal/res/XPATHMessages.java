package ohos.com.sun.org.apache.xpath.internal.res;

import java.text.MessageFormat;
import java.util.ListResourceBundle;
import ohos.com.sun.org.apache.bcel.internal.util.SecuritySupport;
import ohos.com.sun.org.apache.xml.internal.res.XMLMessages;

public class XPATHMessages extends XMLMessages {
    private static ListResourceBundle XPATHBundle = null;
    private static final String XPATH_ERROR_RESOURCES = "ohos.com.sun.org.apache.xpath.internal.res.XPATHErrorResources";

    public static final String createXPATHMessage(String str, Object[] objArr) {
        if (XPATHBundle == null) {
            XPATHBundle = SecuritySupport.getResourceBundle("ohos.com.sun.org.apache.xpath.internal.res.XPATHErrorResources");
        }
        ListResourceBundle listResourceBundle = XPATHBundle;
        return listResourceBundle != null ? createXPATHMsg(listResourceBundle, str, objArr) : "Could not load any resource bundles.";
    }

    public static final String createXPATHWarning(String str, Object[] objArr) {
        if (XPATHBundle == null) {
            XPATHBundle = SecuritySupport.getResourceBundle("ohos.com.sun.org.apache.xpath.internal.res.XPATHErrorResources");
        }
        ListResourceBundle listResourceBundle = XPATHBundle;
        return listResourceBundle != null ? createXPATHMsg(listResourceBundle, str, objArr) : "Could not load any resource bundles.";
    }

    public static final String createXPATHMsg(ListResourceBundle listResourceBundle, String str, Object[] objArr) {
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
