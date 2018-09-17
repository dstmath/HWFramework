package org.apache.xpath.res;

import java.text.MessageFormat;
import java.util.ListResourceBundle;
import org.apache.xml.res.XMLMessages;

public class XPATHMessages extends XMLMessages {
    private static ListResourceBundle XPATHBundle = new XPATHErrorResources();
    private static final String XPATH_ERROR_RESOURCES = "org.apache.xpath.res.XPATHErrorResources";

    public static final String createXPATHMessage(String msgKey, Object[] args) {
        return createXPATHMsg(XPATHBundle, msgKey, args);
    }

    public static final String createXPATHWarning(String msgKey, Object[] args) {
        return createXPATHMsg(XPATHBundle, msgKey, args);
    }

    public static final String createXPATHMsg(ListResourceBundle fResourceBundle, String msgKey, Object[] args) {
        String fmsg;
        boolean throwex = false;
        String msg = null;
        if (msgKey != null) {
            msg = fResourceBundle.getString(msgKey);
        }
        if (msg == null) {
            msg = fResourceBundle.getString("BAD_CODE");
            throwex = true;
        }
        if (args != null) {
            try {
                int n = args.length;
                for (int i = 0; i < n; i++) {
                    if (args[i] == null) {
                        args[i] = "";
                    }
                }
                fmsg = MessageFormat.format(msg, args);
            } catch (Exception e) {
                fmsg = fResourceBundle.getString("FORMAT_FAILED") + " " + msg;
            }
        } else {
            fmsg = msg;
        }
        if (!throwex) {
            return fmsg;
        }
        throw new RuntimeException(fmsg);
    }
}
