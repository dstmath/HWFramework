package ohos.com.sun.org.apache.xerces.internal.impl.io;

import java.io.CharConversionException;
import java.util.Locale;
import ohos.com.sun.org.apache.xerces.internal.util.MessageFormatter;

public class MalformedByteSequenceException extends CharConversionException {
    static final long serialVersionUID = 8436382245048328739L;
    private Object[] fArguments;
    private String fDomain;
    private MessageFormatter fFormatter;
    private String fKey;
    private Locale fLocale;
    private String fMessage;

    public MalformedByteSequenceException(MessageFormatter messageFormatter, Locale locale, String str, String str2, Object[] objArr) {
        this.fFormatter = messageFormatter;
        this.fLocale = locale;
        this.fDomain = str;
        this.fKey = str2;
        this.fArguments = objArr;
    }

    public String getDomain() {
        return this.fDomain;
    }

    public String getKey() {
        return this.fKey;
    }

    public Object[] getArguments() {
        return this.fArguments;
    }

    @Override // java.lang.Throwable
    public String getMessage() {
        if (this.fMessage == null) {
            this.fMessage = this.fFormatter.formatMessage(this.fLocale, this.fKey, this.fArguments);
            this.fFormatter = null;
            this.fLocale = null;
        }
        return this.fMessage;
    }
}
