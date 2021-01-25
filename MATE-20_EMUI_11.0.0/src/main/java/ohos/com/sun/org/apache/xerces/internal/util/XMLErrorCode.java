package ohos.com.sun.org.apache.xerces.internal.util;

/* access modifiers changed from: package-private */
public final class XMLErrorCode {
    private String fDomain;
    private String fKey;

    public XMLErrorCode(String str, String str2) {
        this.fDomain = str;
        this.fKey = str2;
    }

    public void setValues(String str, String str2) {
        this.fDomain = str;
        this.fKey = str2;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof XMLErrorCode)) {
            return false;
        }
        XMLErrorCode xMLErrorCode = (XMLErrorCode) obj;
        if (!this.fDomain.equals(xMLErrorCode.fDomain) || !this.fKey.equals(xMLErrorCode.fKey)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return this.fDomain.hashCode() + this.fKey.hashCode();
    }
}
