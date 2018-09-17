package java.text;

import android.icu.text.BreakIterator;

class IcuIteratorWrapper extends BreakIterator {
    private BreakIterator wrapped;

    IcuIteratorWrapper(BreakIterator iterator) {
        this.wrapped = iterator;
    }

    public Object clone() {
        IcuIteratorWrapper result = (IcuIteratorWrapper) super.clone();
        result.wrapped = (BreakIterator) this.wrapped.clone();
        return result;
    }

    public boolean equals(Object that) {
        if (that instanceof IcuIteratorWrapper) {
            return this.wrapped.equals(((IcuIteratorWrapper) that).wrapped);
        }
        return false;
    }

    public String toString() {
        return this.wrapped.toString();
    }

    public int hashCode() {
        return this.wrapped.hashCode();
    }

    public int first() {
        return this.wrapped.first();
    }

    public int last() {
        return this.wrapped.last();
    }

    public int next(int n) {
        return this.wrapped.next(n);
    }

    public int next() {
        return this.wrapped.next();
    }

    public int previous() {
        return this.wrapped.previous();
    }

    protected static final void checkOffset(int offset, CharacterIterator text) {
        if (offset < text.getBeginIndex() || offset > text.getEndIndex()) {
            throw new IllegalArgumentException("offset out of bounds");
        }
    }

    public int following(int offset) {
        checkOffset(offset, getText());
        return this.wrapped.following(offset);
    }

    public int preceding(int offset) {
        checkOffset(offset, getText());
        return this.wrapped.preceding(offset);
    }

    public boolean isBoundary(int offset) {
        checkOffset(offset, getText());
        return this.wrapped.isBoundary(offset);
    }

    public int current() {
        return this.wrapped.current();
    }

    public CharacterIterator getText() {
        return this.wrapped.getText();
    }

    public void setText(String newText) {
        this.wrapped.setText(newText);
    }

    public void setText(CharacterIterator newText) {
        newText.current();
        this.wrapped.setText(newText);
    }
}
