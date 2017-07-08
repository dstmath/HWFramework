package libcore.icu;

import java.text.CollationKey;

public final class CollationKeyICU extends CollationKey {
    private final android.icu.text.CollationKey key;

    public CollationKeyICU(String source, android.icu.text.CollationKey key) {
        super(source);
        this.key = key;
    }

    public int compareTo(CollationKey other) {
        android.icu.text.CollationKey otherKey;
        if (other instanceof CollationKeyICU) {
            otherKey = ((CollationKeyICU) other).key;
        } else {
            otherKey = new android.icu.text.CollationKey(other.getSourceString(), other.toByteArray());
        }
        return this.key.compareTo(otherKey);
    }

    public boolean equals(Object object) {
        boolean z = true;
        if (object == this) {
            return true;
        }
        if (!(object instanceof CollationKey)) {
            return false;
        }
        if (compareTo((CollationKey) object) != 0) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return this.key.hashCode();
    }

    public byte[] toByteArray() {
        return this.key.toByteArray();
    }
}
