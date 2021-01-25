package ohos.global.icu.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class CodePointMap implements Iterable<Range> {
    static final /* synthetic */ boolean $assertionsDisabled = false;

    public enum RangeOption {
        NORMAL,
        FIXED_LEAD_SURROGATES,
        FIXED_ALL_SURROGATES
    }

    public interface ValueFilter {
        int apply(int i);
    }

    public abstract int get(int i);

    public abstract boolean getRange(int i, ValueFilter valueFilter, Range range);

    public static final class Range {
        private int end = -1;
        private int start = -1;
        private int value = 0;

        public int getStart() {
            return this.start;
        }

        public int getEnd() {
            return this.end;
        }

        public int getValue() {
            return this.value;
        }

        public void set(int i, int i2, int i3) {
            this.start = i;
            this.end = i2;
            this.value = i3;
        }
    }

    private final class RangeIterator implements Iterator<Range> {
        private Range range;

        private RangeIterator() {
            this.range = new Range();
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return -1 <= this.range.end && this.range.end < 1114111;
        }

        @Override // java.util.Iterator
        public Range next() {
            if (CodePointMap.this.getRange(this.range.end + 1, null, this.range)) {
                return this.range;
            }
            throw new NoSuchElementException();
        }

        @Override // java.util.Iterator
        public final void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public class StringIterator {
        @Deprecated
        protected int c = -1;
        @Deprecated
        protected CharSequence s;
        @Deprecated
        protected int sIndex;
        @Deprecated
        protected int value = 0;

        @Deprecated
        protected StringIterator(CharSequence charSequence, int i) {
            this.s = charSequence;
            this.sIndex = i;
        }

        public void reset(CharSequence charSequence, int i) {
            this.s = charSequence;
            this.sIndex = i;
            this.c = -1;
            this.value = 0;
        }

        public boolean next() {
            if (this.sIndex >= this.s.length()) {
                return false;
            }
            this.c = Character.codePointAt(this.s, this.sIndex);
            this.sIndex += Character.charCount(this.c);
            this.value = CodePointMap.this.get(this.c);
            return true;
        }

        public boolean previous() {
            int i = this.sIndex;
            if (i <= 0) {
                return false;
            }
            this.c = Character.codePointBefore(this.s, i);
            this.sIndex -= Character.charCount(this.c);
            this.value = CodePointMap.this.get(this.c);
            return true;
        }

        public final int getIndex() {
            return this.sIndex;
        }

        public final int getCodePoint() {
            return this.c;
        }

        public final int getValue() {
            return this.value;
        }
    }

    protected CodePointMap() {
    }

    public boolean getRange(int i, RangeOption rangeOption, int i2, ValueFilter valueFilter, Range range) {
        if (!getRange(i, valueFilter, range)) {
            return false;
        }
        if (rangeOption == RangeOption.NORMAL) {
            return true;
        }
        int i3 = rangeOption == RangeOption.FIXED_ALL_SURROGATES ? 57343 : 56319;
        int i4 = range.end;
        if (i4 >= 55295 && i <= i3) {
            if (range.value == i2) {
                if (i4 >= i3) {
                    return true;
                }
            } else if (i <= 55295) {
                range.end = 55295;
                return true;
            } else {
                range.value = i2;
                if (i4 > i3) {
                    range.end = i3;
                    return true;
                }
            }
            if (!getRange(i3 + 1, valueFilter, range) || range.value != i2) {
                range.start = i;
                range.end = i3;
                range.value = i2;
            } else {
                range.start = i;
                return true;
            }
        }
        return true;
    }

    @Override // java.lang.Iterable
    public Iterator<Range> iterator() {
        return new RangeIterator();
    }

    public StringIterator stringIterator(CharSequence charSequence, int i) {
        return new StringIterator(charSequence, i);
    }
}
