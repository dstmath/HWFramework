package ohos.com.sun.org.apache.xerces.internal.impl.xpath;

public class XPathException extends Exception {
    static final long serialVersionUID = -948482312169512085L;
    private String fKey;

    public XPathException() {
        this.fKey = "c-general-xpath";
    }

    public XPathException(String str) {
        this.fKey = str;
    }

    public String getKey() {
        return this.fKey;
    }
}
