package java.text;

import java.text.AttributedCharacterIterator.Attribute;
import java.util.Map.Entry;

/* compiled from: AttributedString */
class AttributeEntry implements Entry {
    private Attribute key;
    private Object value;

    AttributeEntry(Attribute key, Object value) {
        this.key = key;
        this.value = value;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof AttributeEntry)) {
            return false;
        }
        AttributeEntry other = (AttributeEntry) o;
        if (other.key.equals(this.key)) {
            if (this.value != null) {
                z = other.value.equals(this.value);
            } else if (other.value == null) {
                z = true;
            }
        }
        return z;
    }

    public Object getKey() {
        return this.key;
    }

    public Object getValue() {
        return this.value;
    }

    public Object setValue(Object newValue) {
        throw new UnsupportedOperationException();
    }

    public int hashCode() {
        return (this.value == null ? 0 : this.value.hashCode()) ^ this.key.hashCode();
    }

    public String toString() {
        return this.key.toString() + "=" + this.value.toString();
    }
}
