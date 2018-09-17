package java.text;

public final class StringCharacterIterator implements CharacterIterator {
    private int begin;
    private int end;
    private int pos;
    private String text;

    public StringCharacterIterator(String text) {
        this(text, 0);
    }

    public StringCharacterIterator(String text, int pos) {
        this(text, 0, text.length(), pos);
    }

    public StringCharacterIterator(String text, int begin, int end, int pos) {
        if (text == null) {
            throw new NullPointerException();
        }
        this.text = text;
        if (begin < 0 || begin > end || end > text.length()) {
            throw new IllegalArgumentException("Invalid substring range");
        } else if (pos < begin || pos > end) {
            throw new IllegalArgumentException("Invalid position");
        } else {
            this.begin = begin;
            this.end = end;
            this.pos = pos;
        }
    }

    public void setText(String text) {
        if (text == null) {
            throw new NullPointerException();
        }
        this.text = text;
        this.begin = 0;
        this.end = text.length();
        this.pos = 0;
    }

    public char first() {
        this.pos = this.begin;
        return current();
    }

    public char last() {
        if (this.end != this.begin) {
            this.pos = this.end - 1;
        } else {
            this.pos = this.end;
        }
        return current();
    }

    public char setIndex(int p) {
        if (p < this.begin || p > this.end) {
            throw new IllegalArgumentException("Invalid index");
        }
        this.pos = p;
        return current();
    }

    public char current() {
        if (this.pos < this.begin || this.pos >= this.end) {
            return 65535;
        }
        return this.text.charAt(this.pos);
    }

    public char next() {
        if (this.pos < this.end - 1) {
            this.pos++;
            return this.text.charAt(this.pos);
        }
        this.pos = this.end;
        return 65535;
    }

    public char previous() {
        if (this.pos <= this.begin) {
            return 65535;
        }
        this.pos--;
        return this.text.charAt(this.pos);
    }

    public int getBeginIndex() {
        return this.begin;
    }

    public int getEndIndex() {
        return this.end;
    }

    public int getIndex() {
        return this.pos;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StringCharacterIterator)) {
            return false;
        }
        StringCharacterIterator that = (StringCharacterIterator) obj;
        return hashCode() == that.hashCode() && this.text.equals(that.text) && this.pos == that.pos && this.begin == that.begin && this.end == that.end;
    }

    public int hashCode() {
        return ((this.text.hashCode() ^ this.pos) ^ this.begin) ^ this.end;
    }

    public Object clone() {
        try {
            return (StringCharacterIterator) super.clone();
        } catch (Throwable e) {
            throw new InternalError(e);
        }
    }
}
