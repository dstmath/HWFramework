package ohos.com.sun.org.apache.xerces.internal.impl.dtd;

public class XMLEntityDecl {
    public String baseSystemId;
    public boolean inExternal;
    public boolean isPE;
    public String name;
    public String notation;
    public String publicId;
    public String systemId;
    public String value;

    public void setValues(String str, String str2, String str3, String str4, String str5, boolean z, boolean z2) {
        setValues(str, str2, str3, str4, str5, null, z, z2);
    }

    public void setValues(String str, String str2, String str3, String str4, String str5, String str6, boolean z, boolean z2) {
        this.name = str;
        this.publicId = str2;
        this.systemId = str3;
        this.baseSystemId = str4;
        this.notation = str5;
        this.value = str6;
        this.isPE = z;
        this.inExternal = z2;
    }

    public void clear() {
        this.name = null;
        this.publicId = null;
        this.systemId = null;
        this.baseSystemId = null;
        this.notation = null;
        this.value = null;
        this.isPE = false;
        this.inExternal = false;
    }
}
