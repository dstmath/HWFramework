package org.apache.xml.dtm.ref;

public final class ExtendedType {
    private int hash;
    private String localName;
    private String namespace;
    private int nodetype;

    public ExtendedType(int nodetype, String namespace, String localName) {
        this.nodetype = nodetype;
        this.namespace = namespace;
        this.localName = localName;
        this.hash = (namespace.hashCode() + nodetype) + localName.hashCode();
    }

    public ExtendedType(int nodetype, String namespace, String localName, int hash) {
        this.nodetype = nodetype;
        this.namespace = namespace;
        this.localName = localName;
        this.hash = hash;
    }

    protected void redefine(int nodetype, String namespace, String localName) {
        this.nodetype = nodetype;
        this.namespace = namespace;
        this.localName = localName;
        this.hash = (namespace.hashCode() + nodetype) + localName.hashCode();
    }

    protected void redefine(int nodetype, String namespace, String localName, int hash) {
        this.nodetype = nodetype;
        this.namespace = namespace;
        this.localName = localName;
        this.hash = hash;
    }

    public int hashCode() {
        return this.hash;
    }

    public boolean equals(ExtendedType other) {
        boolean z = false;
        try {
            if (other.nodetype == this.nodetype && other.localName.equals(this.localName)) {
                z = other.namespace.equals(this.namespace);
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
