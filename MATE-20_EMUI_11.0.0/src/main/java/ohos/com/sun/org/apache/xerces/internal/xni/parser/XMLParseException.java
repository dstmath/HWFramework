package ohos.com.sun.org.apache.xerces.internal.xni.parser;

import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;

public class XMLParseException extends XNIException {
    static final long serialVersionUID = 1732959359448549967L;
    protected String fBaseSystemId;
    protected int fCharacterOffset = -1;
    protected int fColumnNumber = -1;
    protected String fExpandedSystemId;
    protected int fLineNumber = -1;
    protected String fLiteralSystemId;
    protected String fPublicId;

    public XMLParseException(XMLLocator xMLLocator, String str) {
        super(str);
        if (xMLLocator != null) {
            this.fPublicId = xMLLocator.getPublicId();
            this.fLiteralSystemId = xMLLocator.getLiteralSystemId();
            this.fExpandedSystemId = xMLLocator.getExpandedSystemId();
            this.fBaseSystemId = xMLLocator.getBaseSystemId();
            this.fLineNumber = xMLLocator.getLineNumber();
            this.fColumnNumber = xMLLocator.getColumnNumber();
            this.fCharacterOffset = xMLLocator.getCharacterOffset();
        }
    }

    public XMLParseException(XMLLocator xMLLocator, String str, Exception exc) {
        super(str, exc);
        if (xMLLocator != null) {
            this.fPublicId = xMLLocator.getPublicId();
            this.fLiteralSystemId = xMLLocator.getLiteralSystemId();
            this.fExpandedSystemId = xMLLocator.getExpandedSystemId();
            this.fBaseSystemId = xMLLocator.getBaseSystemId();
            this.fLineNumber = xMLLocator.getLineNumber();
            this.fColumnNumber = xMLLocator.getColumnNumber();
            this.fCharacterOffset = xMLLocator.getCharacterOffset();
        }
    }

    public String getPublicId() {
        return this.fPublicId;
    }

    public String getExpandedSystemId() {
        return this.fExpandedSystemId;
    }

    public String getLiteralSystemId() {
        return this.fLiteralSystemId;
    }

    public String getBaseSystemId() {
        return this.fBaseSystemId;
    }

    public int getLineNumber() {
        return this.fLineNumber;
    }

    public int getColumnNumber() {
        return this.fColumnNumber;
    }

    public int getCharacterOffset() {
        return this.fCharacterOffset;
    }

    @Override // java.lang.Throwable, java.lang.Object
    public String toString() {
        Exception exception;
        StringBuffer stringBuffer = new StringBuffer();
        String str = this.fPublicId;
        if (str != null) {
            stringBuffer.append(str);
        }
        stringBuffer.append(':');
        String str2 = this.fLiteralSystemId;
        if (str2 != null) {
            stringBuffer.append(str2);
        }
        stringBuffer.append(':');
        String str3 = this.fExpandedSystemId;
        if (str3 != null) {
            stringBuffer.append(str3);
        }
        stringBuffer.append(':');
        String str4 = this.fBaseSystemId;
        if (str4 != null) {
            stringBuffer.append(str4);
        }
        stringBuffer.append(':');
        stringBuffer.append(this.fLineNumber);
        stringBuffer.append(':');
        stringBuffer.append(this.fColumnNumber);
        stringBuffer.append(':');
        stringBuffer.append(this.fCharacterOffset);
        stringBuffer.append(':');
        String message = getMessage();
        if (message == null && (exception = getException()) != null) {
            message = exception.getMessage();
        }
        if (message != null) {
            stringBuffer.append(message);
        }
        return stringBuffer.toString();
    }
}
