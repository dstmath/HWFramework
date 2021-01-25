package ohos.com.sun.org.apache.xml.internal.serializer.utils;

import java.text.MessageFormat;
import java.util.ListResourceBundle;
import java.util.Locale;
import ohos.com.sun.org.apache.xalan.internal.utils.SecuritySupport;

public final class Messages {
    private final Locale m_locale = Locale.getDefault();
    private ListResourceBundle m_resourceBundle;
    private String m_resourceBundleName;

    Messages(String str) {
        this.m_resourceBundleName = str;
    }

    private Locale getLocale() {
        return this.m_locale;
    }

    public final String createMessage(String str, Object[] objArr) {
        if (this.m_resourceBundle == null) {
            this.m_resourceBundle = SecuritySupport.getResourceBundle(this.m_resourceBundleName);
        }
        ListResourceBundle listResourceBundle = this.m_resourceBundle;
        if (listResourceBundle != null) {
            return createMsg(listResourceBundle, str, objArr);
        }
        return "Could not load the resource bundles: " + this.m_resourceBundleName;
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x009c A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x009d  */
    private final String createMsg(ListResourceBundle listResourceBundle, String str, Object[] objArr) {
        String str2;
        String str3;
        String str4 = null;
        if (str != null) {
            str2 = listResourceBundle.getString(str);
        } else {
            str = "";
            str2 = null;
        }
        boolean z = false;
        if (str2 == null) {
            try {
                MessageFormat.format(MsgKey.BAD_MSGKEY, str, this.m_resourceBundleName);
            } catch (Exception unused) {
                String str5 = "The message key '" + str + "' is not in the message class '" + this.m_resourceBundleName + "'";
            }
        } else {
            if (objArr != null) {
                try {
                    int length = objArr.length;
                    for (int i = 0; i < length; i++) {
                        if (objArr[i] == null) {
                            objArr[i] = "";
                        }
                    }
                    str4 = MessageFormat.format(str2, objArr);
                } catch (Exception unused2) {
                    try {
                        str3 = MessageFormat.format(MsgKey.BAD_MSGFORMAT, str, this.m_resourceBundleName) + " " + str2;
                    } catch (Exception unused3) {
                        str3 = "The format of message '" + str + "' in message class '" + this.m_resourceBundleName + "' failed.";
                    }
                    str4 = str3;
                }
            } else {
                str4 = str2;
            }
            if (z) {
                return str4;
            }
            throw new RuntimeException(str4);
        }
        z = true;
        if (z) {
        }
    }
}
