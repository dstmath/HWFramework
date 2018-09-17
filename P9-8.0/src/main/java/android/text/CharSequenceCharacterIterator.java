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
        if (this.mBeginIndex == this.mEndIndex) {
            this.mIndex = this.mEndIndex;
            return 65535;
        }
        this.mIndex = this.mEndIndex - 1;
        return this.mCharSeq.charAt(this.mIndex);
    }

    public char current() {
        return this.mIndex == this.mEndIndex ? 65535 : this.mCharSeq.charAt(this.mIndex);
    }

    public char next() {
        this.mIndex++;
        if (this.mIndex < this.mEndIndex) {
            return this.mCharSeq.charAt(this.mIndex);
        }
        this.mIndex = this.mEndIndex;
        return 65535;
    }

    public char previous() {
        if (this.mIndex <= this.mBeginIndex) {
            return 65535;
        }
        this.mIndex--;
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

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
}
