package ohos.com.sun.org.apache.xalan.internal.res;

import java.util.ListResourceBundle;
import ohos.com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xpath.internal.res.XPATHMessages;

public class XSLMessages extends XPATHMessages {
    private static ListResourceBundle XSLTBundle = null;
    private static final String XSLT_ERROR_RESOURCES = "ohos.com.sun.org.apache.xalan.internal.res.XSLTErrorResources";

    public static String createMessage(String str, Object[] objArr) {
        if (XSLTBundle == null) {
            XSLTBundle = SecuritySupport.getResourceBundle("ohos.com.sun.org.apache.xalan.internal.res.XSLTErrorResources");
        }
        ListResourceBundle listResourceBundle = XSLTBundle;
        return listResourceBundle != null ? createMsg(listResourceBundle, str, objArr) : "Could not load any resource bundles.";
    }

    public static String createWarning(String str, Object[] objArr) {
        if (XSLTBundle == null) {
            XSLTBundle = SecuritySupport.getResourceBundle("ohos.com.sun.org.apache.xalan.internal.res.XSLTErrorResources");
        }
        ListResourceBundle listResourceBundle = XSLTBundle;
        return listResourceBundle != null ? createMsg(listResourceBundle, str, objArr) : "Could not load any resource bundles.";
    }
}
