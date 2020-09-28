package android.text;

import java.text.CharacterIterator;

public class CharSequenceCharacterIterator implements CharacterIterator {
    private final int mBeginIndex;
    private final CharSequence mCharSeq;
    private final int mEndIndex;
    private int mIndex;

    public CharSequenceCharacterIterator(CharSequence text, int start, int end) {
        this.mCharSeq = text;
        this.mIndex = start;
        this.mBeginIndex = start;
        this.mEndIndex = end;
    }

    public char first() {
        this.mIndex = this.mBeginIndex;
        return current();
    }

    public char last() {
        int i = this.mBeginIndex;
        int i2 = this.mEndIndex;
        if (i == i2) {
            this.mIndex = i2;
            return 65535;
        }
        this.mIndex = i2 - 1;
        return this.mCharSeq.charAt(this.mIndex);
    }

    public char current() {
        int i = this.mIndex;
        if (i == this.mEndIndex) {
            return 65535;
        }
        return this.mCharSeq.charAt(i);
    }

    public char next() {
        this.mIndex++;
        int i = this.mIndex;
        int i2 = this.mEndIndex;
        if (i < i2) {
            return this.mCharSeq.charAt(i);
        }
        this.mIndex = i2;
        return 65535;
    }

    public char previous() {
        int i = this.mIndex;
        if (i <= this.mBeginIndex) {
            return 65535;
        }
        this.mIndex = i - 1;
        return this.mCharSeq.charAt(this.mIndex);
    }

    public char setIndex(int position) {
        if (this.mBeginIndex > position || position > this.mEndIndex) {
            throw new IllegalArgumentException("invalid position");
        }
        this.mIndex = position;
        return current();
    }

    public int getBeginIndex() {
        return this.mBeginIndex;
    }

    public int getEndIndex() {
        return this.mEndIndex;
    }

    public int getIndex() {
        return this.mIndex;
    }

    @Override // java.lang.Object
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
}
