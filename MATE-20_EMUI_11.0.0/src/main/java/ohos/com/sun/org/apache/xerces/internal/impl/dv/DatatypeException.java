package ohos.com.sun.org.apache.xerces.internal.impl.dv;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import ohos.com.sun.org.apache.xerces.internal.utils.SecuritySupport;

public class DatatypeException extends Exception {
    static final long serialVersionUID = 1940805832730465578L;
    protected Object[] args;
    protected String key;

    public DatatypeException(String str, Object[] objArr) {
        super(str);
        this.key = str;
        this.args = objArr;
    }

    public String getKey() {
        return this.key;
    }

    public Object[] getArgs() {
        return this.args;
    }

    @Override // java.lang.Throwable
    public String getMessage() {
        ResourceBundle resourceBundle = SecuritySupport.getResourceBundle("com.sun.org.apache.xerces.internal.impl.msg.XMLSchemaMessages");
        if (resourceBundle != null) {
            String string = resourceBundle.getString(this.key);
            if (string != null) {
                Object[] objArr = this.args;
                if (objArr == null) {
                    return string;
                }
                try {
                    return MessageFormat.format(string, objArr);
                } catch (Exception unused) {
                    String string2 = resourceBundle.getString("FormatFailed");
                    return string2 + " " + resourceBundle.getString(this.key);
                }
            } else {
                throw new MissingResourceException(resourceBundle.getString("BadMessageKey"), "com.sun.org.apache.xerces.internal.impl.msg.XMLSchemaMessages", this.key);
            }
        } else {
            throw new MissingResourceException("Property file not found!", "com.sun.org.apache.xerces.internal.impl.msg.XMLSchemaMessages", this.key);
        }
    }
}
