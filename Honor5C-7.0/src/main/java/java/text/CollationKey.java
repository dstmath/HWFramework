package java.text;

public abstract class CollationKey implements Comparable<CollationKey> {
    private final String source;

    public abstract int compareTo(CollationKey collationKey);

    public abstract byte[] toByteArray();

    public String getSourceString() {
        return this.source;
    }

    protected CollationKey(String source) {
        if (source == null) {
            throw new NullPointerException();
        }
        this.source = source;
    }
}
