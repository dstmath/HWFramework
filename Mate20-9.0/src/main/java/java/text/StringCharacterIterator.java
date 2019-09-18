package java.text;

public final class StringCharacterIterator implements CharacterIterator {
    private int begin;
    private int end;
    private int pos;
    private String text;

    public StringCharacterIterator(String text2) {
        this(text2, 0);
    }

    public StringCharacterIterator(String text2, int pos2) {
        this(text2, 0, text2.length(), pos2);
    }

    public StringCharacterIterator(String text2, int begin2, int end2, int pos2) {
        if (text2 != null) {
            this.text = text2;
            if (begin2 < 0 || begin2 > end2 || end2 > text2.length()) {
                throw new IllegalArgumentException("Invalid substring range");
            } else if (pos2 < begin2 || pos2 > end2) {
                throw new IllegalArgumentException("Invalid position");
            } else {
                this.begin = begin2;
                this.end = end2;
                this.pos = pos2;
            }
        } else {
            throw new NullPointerException();
        }
    }

    public void setText(String text2) {
        if (text2 != null) {
            this.text = text2;
            this.begin = 0;
            this.end = text2.length();
            this.pos = 0;
            return;
        }
        throw new NullPointerException();
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
        if (hashCode() == that.hashCode() && this.text.equals(that.text) && this.pos == that.pos && this.begin == that.begin && this.end == that.end) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return ((this.text.hashCode() ^ this.pos) ^ this.begin) ^ this.end;
    }

    public Object clone() {
        try {
            return (StringCharacterIterator) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError((Throwable) e);
        }
    }
}
