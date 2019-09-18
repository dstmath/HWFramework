package android.icu.text;

import android.icu.impl.CharacterIteration;
import java.text.CharacterIterator;
import java.util.BitSet;

abstract class DictionaryBreakEngine implements LanguageBreakEngine {
    UnicodeSet fSet = new UnicodeSet();
    private BitSet fTypes = new BitSet(32);

    static class DequeI implements Cloneable {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private int[] data = new int[50];
        private int firstIdx = 4;
        private int lastIdx = 4;

        static {
            Class<DictionaryBreakEngine> cls = DictionaryBreakEngine.class;
        }

        DequeI() {
        }

        public Object clone() throws CloneNotSupportedException {
            DequeI result = (DequeI) super.clone();
            result.data = (int[]) this.data.clone();
            return result;
        }

        /* access modifiers changed from: package-private */
        public int size() {
            return this.firstIdx - this.lastIdx;
        }

        /* access modifiers changed from: package-private */
        public boolean isEmpty() {
            return size() == 0;
        }

        private void grow() {
            int[] newData = new int[(this.data.length * 2)];
            System.arraycopy(this.data, 0, newData, 0, this.data.length);
            this.data = newData;
        }

        /* access modifiers changed from: package-private */
        public void offer(int v) {
            int[] iArr = this.data;
            int i = this.lastIdx - 1;
            this.lastIdx = i;
            iArr[i] = v;
        }

        /* access modifiers changed from: package-private */
        public void push(int v) {
            if (this.firstIdx >= this.data.length) {
                grow();
            }
            int[] iArr = this.data;
            int i = this.firstIdx;
            this.firstIdx = i + 1;
            iArr[i] = v;
        }

        /* access modifiers changed from: package-private */
        public int pop() {
            int[] iArr = this.data;
            int i = this.firstIdx - 1;
            this.firstIdx = i;
            return iArr[i];
        }

        /* access modifiers changed from: package-private */
        public int peek() {
            return this.data[this.firstIdx - 1];
        }

        /* access modifiers changed from: package-private */
        public int peekLast() {
            return this.data[this.lastIdx];
        }

        /* access modifiers changed from: package-private */
        public int pollLast() {
            int[] iArr = this.data;
            int i = this.lastIdx;
            this.lastIdx = i + 1;
            return iArr[i];
        }

        /* access modifiers changed from: package-private */
        public boolean contains(int v) {
            for (int i = this.lastIdx; i < this.firstIdx; i++) {
                if (this.data[i] == v) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public int elementAt(int i) {
            return this.data[this.lastIdx + i];
        }

        /* access modifiers changed from: package-private */
        public void removeAllElements() {
            this.firstIdx = 4;
            this.lastIdx = 4;
        }
    }

    static class PossibleWord {
        private static final int POSSIBLE_WORD_LIST_MAX = 20;
        private int[] count = new int[1];
        private int current;
        private int[] lengths = new int[20];
        private int mark;
        private int offset = -1;
        private int prefix;

        public int candidates(CharacterIterator fIter, DictionaryMatcher dict, int rangeEnd) {
            int start = fIter.getIndex();
            if (start != this.offset) {
                this.offset = start;
                this.prefix = dict.matches(fIter, rangeEnd - start, this.lengths, this.count, this.lengths.length);
                if (this.count[0] <= 0) {
                    fIter.setIndex(start);
                }
            }
            if (this.count[0] > 0) {
                fIter.setIndex(this.lengths[this.count[0] - 1] + start);
            }
            this.current = this.count[0] - 1;
            this.mark = this.current;
            return this.count[0];
        }

        public int acceptMarked(CharacterIterator fIter) {
            fIter.setIndex(this.offset + this.lengths[this.mark]);
            return this.lengths[this.mark];
        }

        public boolean backUp(CharacterIterator fIter) {
            if (this.current <= 0) {
                return false;
            }
            int i = this.offset;
            int[] iArr = this.lengths;
            int i2 = this.current - 1;
            this.current = i2;
            fIter.setIndex(i + iArr[i2]);
            return true;
        }

        public int longestPrefix() {
            return this.prefix;
        }

        public void markCurrent() {
            this.mark = this.current;
        }
    }

    /* access modifiers changed from: package-private */
    public abstract int divideUpDictionaryRange(CharacterIterator characterIterator, int i, int i2, DequeI dequeI);

    public DictionaryBreakEngine(Integer... breakTypes) {
        for (Integer type : breakTypes) {
            this.fTypes.set(type.intValue());
        }
    }

    public boolean handles(int c, int breakType) {
        return this.fTypes.get(breakType) && this.fSet.contains(c);
    }

    public int findBreaks(CharacterIterator text, int startPos, int endPos, int breakType, DequeI foundBreaks) {
        int current;
        int start = text.getIndex();
        int c = CharacterIteration.current32(text);
        while (true) {
            int index = text.getIndex();
            current = index;
            if (index >= endPos || !this.fSet.contains(c)) {
                int result = divideUpDictionaryRange(text, start, current, foundBreaks);
                text.setIndex(current);
            } else {
                CharacterIteration.next32(text);
                c = CharacterIteration.current32(text);
            }
        }
        int result2 = divideUpDictionaryRange(text, start, current, foundBreaks);
        text.setIndex(current);
        return result2;
    }

    /* access modifiers changed from: package-private */
    public void setCharacters(UnicodeSet set) {
        this.fSet = new UnicodeSet(set);
        this.fSet.compact();
    }
}
