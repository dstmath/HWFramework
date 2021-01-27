package ohos.com.sun.org.apache.xerces.internal.impl.dtd;

public class XMLContentSpec {
    public static final short CONTENTSPECNODE_ANY = 6;
    public static final short CONTENTSPECNODE_ANY_LAX = 22;
    public static final short CONTENTSPECNODE_ANY_LOCAL = 8;
    public static final short CONTENTSPECNODE_ANY_LOCAL_LAX = 24;
    public static final short CONTENTSPECNODE_ANY_LOCAL_SKIP = 40;
    public static final short CONTENTSPECNODE_ANY_OTHER = 7;
    public static final short CONTENTSPECNODE_ANY_OTHER_LAX = 23;
    public static final short CONTENTSPECNODE_ANY_OTHER_SKIP = 39;
    public static final short CONTENTSPECNODE_ANY_SKIP = 38;
    public static final short CONTENTSPECNODE_CHOICE = 4;
    public static final short CONTENTSPECNODE_LEAF = 0;
    public static final short CONTENTSPECNODE_ONE_OR_MORE = 3;
    public static final short CONTENTSPECNODE_SEQ = 5;
    public static final short CONTENTSPECNODE_ZERO_OR_MORE = 2;
    public static final short CONTENTSPECNODE_ZERO_OR_ONE = 1;
    public Object otherValue;
    public short type;
    public Object value;

    public interface Provider {
        boolean getContentSpec(int i, XMLContentSpec xMLContentSpec);
    }

    public XMLContentSpec() {
        clear();
    }

    public XMLContentSpec(short s, Object obj, Object obj2) {
        setValues(s, obj, obj2);
    }

    public XMLContentSpec(XMLContentSpec xMLContentSpec) {
        setValues(xMLContentSpec);
    }

    public XMLContentSpec(Provider provider, int i) {
        setValues(provider, i);
    }

    public void clear() {
        this.type = -1;
        this.value = null;
        this.otherValue = null;
    }

    public void setValues(short s, Object obj, Object obj2) {
        this.type = s;
        this.value = obj;
        this.otherValue = obj2;
    }

    public void setValues(XMLContentSpec xMLContentSpec) {
        this.type = xMLContentSpec.type;
        this.value = xMLContentSpec.value;
        this.otherValue = xMLContentSpec.otherValue;
    }

    public void setValues(Provider provider, int i) {
        if (!provider.getContentSpec(i, this)) {
            clear();
        }
    }

    public int hashCode() {
        return this.otherValue.hashCode() | (this.type << 16) | (this.value.hashCode() << 8);
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof XMLContentSpec)) {
            return false;
        }
        XMLContentSpec xMLContentSpec = (XMLContentSpec) obj;
        if (this.type == xMLContentSpec.type && this.value == xMLContentSpec.value && this.otherValue == xMLContentSpec.otherValue) {
            return true;
        }
        return false;
    }
}
