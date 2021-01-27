package ohos.com.sun.org.apache.xml.internal.dtm.ref;

public final class ExtendedType {
    private int hash;
    private String localName;
    private String namespace;
    private int nodetype;

    public ExtendedType(int i, String str, String str2) {
        this.nodetype = i;
        this.namespace = str;
        this.localName = str2;
        this.hash = i + str.hashCode() + str2.hashCode();
    }

    public ExtendedType(int i, String str, String str2, int i2) {
        this.nodetype = i;
        this.namespace = str;
        this.localName = str2;
        this.hash = i2;
    }

    /* access modifiers changed from: protected */
    public void redefine(int i, String str, String str2) {
        this.nodetype = i;
        this.namespace = str;
        this.localName = str2;
        this.hash = i + str.hashCode() + str2.hashCode();
    }

    /* access modifiers changed from: protected */
    public void redefine(int i, String str, String str2, int i2) {
        this.nodetype = i;
        this.namespace = str;
        this.localName = str2;
        this.hash = i2;
    }

    public int hashCode() {
        return this.hash;
    }

    public boolean equals(ExtendedType extendedType) {
        try {
            if (extendedType.nodetype != this.nodetype || !extendedType.localName.equals(this.localName) || !extendedType.namespace.equals(this.namespace)) {
                return false;
            }
            return true;
        } catch (NullPointerException unused) {
            return false;
        }
    }

    public int getNodeType() {
        return this.nodetype;
    }

    public String getLocalName() {
        return this.localName;
    }

    public String getNamespace() {
        return this.namespace;
    }
}
