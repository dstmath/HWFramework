package org.apache.xml.res;

import java.text.MessageFormat;
import java.util.ListResourceBundle;
import java.util.Locale;

public class XMLMessages {
    protected static final String BAD_CODE = "BAD_CODE";
    protected static final String FORMAT_FAILED = "FORMAT_FAILED";
    private static ListResourceBundle XMLBundle = new XMLErrorResources();
    protected Locale fLocale = Locale.getDefault();

    public void setLocale(Locale locale) {
        this.fLocale = locale;
    }

    public Locale getLocale() {
        return this.fLocale;
    }

    public static final String createXMLMessage(String msgKey, Object[] args) {
        return createMsg(XMLBundle, msgKey, args);
    }

    public static final String createMsg(ListResourceBundle fResourceBundle, String msgKey, Object[] args) {
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
