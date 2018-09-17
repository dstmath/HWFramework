package org.apache.xalan.res;

import java.util.ListResourceBundle;
import org.apache.xml.res.XMLMessages;
import org.apache.xpath.res.XPATHMessages;

public class XSLMessages extends XPATHMessages {
    private static ListResourceBundle XSLTBundle = new XSLTErrorResources();

    public static final String createMessage(String msgKey, Object[] args) {
        return XMLMessages.createMsg(XSLTBundle, msgKey, args);
    }

    public static final String createWarning(String msgKey, Object[] args) {
        return XMLMessages.createMsg(XSLTBundle, msgKey, args);
    }
}
