package ohos.com.sun.org.apache.xerces.internal.impl.dtd;

public class XMLNotationDecl {
    public String baseSystemId;
    public String name;
    public String publicId;
    public String systemId;

    public void setValues(String str, String str2, String str3, String str4) {
        this.name = str;
        this.publicId = str2;
        this.systemId = str3;
        this.baseSystemId = str4;
    }

    public void clear() {
        this.name = null;
        this.publicId = null;
        this.systemId = null;
        this.baseSystemId = null;
    }
}
