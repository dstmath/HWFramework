package org.apache.xml.dtm.ref;

public final class ExtendedType {
    private int hash;
    private String localName;
    private String namespace;
    private int nodetype;

    public ExtendedType(int nodetype2, String namespace2, String localName2) {
        this.nodetype = nodetype2;
        this.namespace = namespace2;
        this.localName = localName2;
        this.hash = namespace2.hashCode() + nodetype2 + localName2.hashCode();
    }

    public ExtendedType(int nodetype2, String namespace2, String localName2, int hash2) {
        this.nodetype = nodetype2;
        this.namespace = namespace2;
        this.localName = localName2;
        this.hash = hash2;
    }

    /* access modifiers changed from: protected */
    public void redefine(int nodetype2, String namespace2, String localName2) {
        this.nodetype = nodetype2;
        this.namespace = namespace2;
        this.localName = localName2;
        this.hash = namespace2.hashCode() + nodetype2 + localName2.hashCode();
    }

    /* access modifiers changed from: protected */
    public void redefine(int nodetype2, String namespace2, String localName2, int hash2) {
        this.nodetype = nodetype2;
        this.namespace = namespace2;
        this.localName = localName2;
        this.hash = hash2;
    }

    public int hashCode() {
        return this.hash;
    }

    public boolean equals(ExtendedType other) {
        boolean z = false;
        try {
            if (other.nodetype == this.nodetype && other.localName.equals(this.localName) && other.namespace.equals(this.namespace)) {
                z = true;
            }
            return z;
        } catch (NullPointerException e) {
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
