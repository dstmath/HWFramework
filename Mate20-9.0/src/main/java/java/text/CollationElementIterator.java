package java.text;

public final class CollationElementIterator {
    public static final int NULLORDER = -1;
    private android.icu.text.CollationElementIterator icuIterator;

    CollationElementIterator(android.icu.text.CollationElementIterator iterator) {
        this.icuIterator = iterator;
    }

    public void reset() {
        this.icuIterator.reset();
    }

    public int next() {
        return this.icuIterator.next();
    }

    public int previous() {
        return this.icuIterator.previous();
    }

    public static final int primaryOrder(int order) {
        return android.icu.text.CollationElementIterator.primaryOrder(order);
    }

    public static final short secondaryOrder(int order) {
        return (short) android.icu.text.CollationElementIterator.secondaryOrder(order);
    }

    public static final short tertiaryOrder(int order) {
        return (short) android.icu.text.CollationElementIterator.tertiaryOrder(order);
    }

    public void setOffset(int newOffset) {
        this.icuIterator.setOffset(newOffset);
    }

    public int getOffset() {
        return this.icuIterator.getOffset();
    }

    public int getMaxExpansion(int order) {
        return this.icuIterator.getMaxExpansion(order);
    }

    public void setText(String source) {
        this.icuIterator.setText(source);
    }

    public void setText(CharacterIterator source) {
        this.icuIterator.setText(source);
    }
}
