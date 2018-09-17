package java.util.logging;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public abstract class Formatter {
    public abstract String format(LogRecord logRecord);

    protected Formatter() {
    }

    public String getHead(Handler h) {
        return "";
    }

    public String getTail(Handler h) {
        return "";
    }

    /* JADX WARNING: Missing block: B:12:0x001d, code:
            return r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized String formatMessage(LogRecord record) {
        String format = record.getMessage();
        ResourceBundle catalog = record.getResourceBundle();
        if (catalog != null) {
            try {
                format = catalog.getString(record.getMessage());
            } catch (MissingResourceException e) {
                format = record.getMessage();
            }
        }
        try {
            Object[] parameters = record.getParameters();
            if (parameters != null && parameters.length != 0) {
                if (format.indexOf("{0") < 0 && format.indexOf("{1") < 0) {
                    if (format.indexOf("{2") < 0 && format.indexOf("{3") < 0) {
                        return format;
                    }
                }
                return MessageFormat.format(format, parameters);
            }
        } catch (Exception e2) {
            return format;
        }
    }
}
