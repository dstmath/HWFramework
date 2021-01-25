package org.ksoap2.serialization;

public class SoapPrimitive extends AttributeContainer {
    public static final Object NullNilElement = new Object();
    public static final Object NullSkip = new Object();
    protected String name;
    protected String namespace;
    protected Object value;

    public SoapPrimitive(String namespace2, String name2, Object value2) {
        this.namespace = namespace2;
        this.name = name2;
        this.value = value2;
    }

    public boolean equals(Object o) {
        String str;
        Object obj;
        if (!(o instanceof SoapPrimitive)) {
            return false;
        }
        SoapPrimitive p = (SoapPrimitive) o;
        if (!(this.name.equals(p.name) && ((str = this.namespace) != null ? str.equals(p.namespace) : p.namespace == null) && ((obj = this.value) != null ? obj.equals(p.value) : p.value == null)) || !attributesAreEqual(p)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hashCode = this.name.hashCode();
        String str = this.namespace;
        return hashCode ^ (str == null ? 0 : str.hashCode());
    }

    public String toString() {
        Object obj = this.value;
        if (obj != null) {
            return obj.toString();
        }
        return null;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getName() {
        return this.name;
    }

    public Object getValue() {
        return this.value;
    }
}
