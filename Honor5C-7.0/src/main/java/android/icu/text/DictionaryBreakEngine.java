package android.icu.text;

import android.icu.impl.CharacterIteration;
import java.text.CharacterIterator;
import java.util.BitSet;

abstract class DictionaryBreakEngine implements LanguageBreakEngine {
    UnicodeSet fSet;
    private BitSet fTypes;

    static class DequeI {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        private int[] data;
        private int firstIdx;
        private int lastIdx;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.DictionaryBreakEngine.DequeI.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.DictionaryBreakEngine.DequeI.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.DictionaryBreakEngine.DequeI.<clinit>():void");
        }

        DequeI() {
            this.data = new int[50];
            this.lastIdx = 4;
            this.firstIdx = 4;
        }

        int size() {
            return this.firstIdx - this.lastIdx;
        }

        boolean isEmpty() {
            return size() == 0;
        }

        private void grow() {
            int[] newData = new int[(this.data.length * 2)];
            System.arraycopy(this.data, 0, newData, 0, this.data.length);
            this.data = newData;
        }

        void offer(int v) {
            Object obj = null;
            if (!-assertionsDisabled) {
                if (this.lastIdx > 0) {
                    obj = 1;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            int[] iArr = this.data;
            int i = this.lastIdx - 1;
            this.lastIdx = i;
            iArr[i] = v;
        }

        void push(int v) {
            if (this.firstIdx >= this.data.length) {
                grow();
            }
            int[] iArr = this.data;
            int i = this.firstIdx;
            this.firstIdx = i + 1;
            iArr[i] = v;
        }

        int pop() {
            Object obj = null;
            if (!-assertionsDisabled) {
                if (size() > 0) {
                    obj = 1;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            int[] iArr = this.data;
            int i = this.firstIdx - 1;
            this.firstIdx = i;
            return iArr[i];
        }

        int peek() {
            Object obj = null;
            if (!-assertionsDisabled) {
                if (size() > 0) {
                    obj = 1;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            return this.data[this.firstIdx - 1];
        }

        int peekLast() {
            Object obj = null;
            if (!-assertionsDisabled) {
                if (size() > 0) {
                    obj = 1;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            return this.data[this.lastIdx];
        }

        int pollLast() {
            Object obj = null;
            if (!-assertionsDisabled) {
                if (size() > 0) {
                    obj = 1;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            int[] iArr = this.data;
            int i = this.lastIdx;
            this.lastIdx = i + 1;
            return iArr[i];
        }

        boolean contains(int v) {
            for (int i = this.lastIdx; i < this.firstIdx; i++) {
                if (this.data[i] == v) {
                    return true;
                }
            }
            return false;
        }
    }

    static class PossibleWord {
        private static final int POSSIBLE_WORD_LIST_MAX = 20;
        private int[] count;
        private int current;
        private int[] lengths;
        private int mark;
        private int offset;
        private int prefix;

        public PossibleWord() {
            this.lengths = new int[POSSIBLE_WORD_LIST_MAX];
            this.count = new int[1];
            this.offset = -1;
        }

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

    abstract int divideUpDictionaryRange(CharacterIterator characterIterator, int i, int i2, DequeI dequeI);

    public DictionaryBreakEngine(Integer... breakTypes) {
        this.fSet = new UnicodeSet();
        this.fTypes = new BitSet(32);
        for (Integer type : breakTypes) {
            this.fTypes.set(type.intValue());
        }
    }

    public boolean handles(int c, int breakType) {
        if (this.fTypes.get(breakType)) {
            return this.fSet.contains(c);
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int findBreaks(CharacterIterator text, int startPos, int endPos, boolean reverse, int breakType, DequeI foundBreaks) {
        int current;
        int rangeStart;
        int rangeEnd;
        int start = text.getIndex();
        int c = CharacterIteration.current32(text);
        if (reverse) {
            boolean isDict = this.fSet.contains(c);
            while (true) {
                current = text.getIndex();
                if (current > startPos && isDict) {
                    isDict = this.fSet.contains(CharacterIteration.previous32(text));
                } else if (current >= startPos) {
                    rangeStart = startPos;
                } else {
                    rangeStart = current + (isDict ? 0 : 1);
                }
            }
            if (current >= startPos) {
                if (isDict) {
                }
                rangeStart = current + (isDict ? 0 : 1);
            } else {
                rangeStart = startPos;
            }
            rangeEnd = start + 1;
        } else {
            while (true) {
                current = text.getIndex();
                if (current >= endPos || !this.fSet.contains(c)) {
                    rangeStart = start;
                    rangeEnd = current;
                } else {
                    CharacterIteration.next32(text);
                    c = CharacterIteration.current32(text);
                }
            }
            rangeStart = start;
            rangeEnd = current;
        }
        int result = divideUpDictionaryRange(text, rangeStart, rangeEnd, foundBreaks);
        text.setIndex(current);
        return result;
    }

    void setCharacters(UnicodeSet set) {
        this.fSet = new UnicodeSet(set);
        this.fSet.compact();
    }
}
