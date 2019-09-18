package java.text;

import java.text.AttributedCharacterIterator;
import java.util.Map;

/* compiled from: AttributedString */
class AttributeEntry implements Map.Entry<AttributedCharacterIterator.Attribute, Object> {
    private AttributedCharacterIterator.Attribute key;
    private Object value;

    AttributeEntry(AttributedCharacterIterator.Attribute key2, Object value2) {
        this.key = key2;
        this.value = value2;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof AttributeEntry)) {
            return false;
        }
        AttributeEntry other = (AttributeEntry) o;
        if (other.key.equals(this.key) && (this.value != null ? other.value.equals(this.value) : other.value == null)) {
            z = true;
        }
        return z;
    }

    public AttributedCharacterIterator.Attribute getKey() {
        return this.key;
    }

    public Object getValue() {
        return this.value;
    }

    public Object setValue(Object newValue) {
        throw new UnsupportedOperationException();
    }

    public int hashCode() {
        return this.key.hashCode() ^ (this.value == null ? 0 : this.value.hashCode());
    }

    public String toString() {
        return this.key.toString() + "=" + this.value.toString();
    }
}
