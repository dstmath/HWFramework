package ohos.com.sun.org.apache.xerces.internal.xni.parser;

import ohos.com.sun.org.apache.xerces.internal.util.Status;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;

public class XMLConfigurationException extends XNIException {
    static final long serialVersionUID = -5437427404547669188L;
    protected String fIdentifier;
    protected Status fType;

    public XMLConfigurationException(Status status, String str) {
        super(str);
        this.fType = status;
        this.fIdentifier = str;
    }

    public XMLConfigurationException(Status status, String str, String str2) {
        super(str2);
        this.fType = status;
        this.fIdentifier = str;
    }

    public Status getType() {
        return this.fType;
    }

    public String getIdentifier() {
        return this.fIdentifier;
    }
}
