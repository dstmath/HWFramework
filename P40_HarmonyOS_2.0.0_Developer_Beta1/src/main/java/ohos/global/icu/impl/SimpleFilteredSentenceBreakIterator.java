package ohos.global.icu.impl;

import java.text.CharacterIterator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.text.BreakIterator;
import ohos.global.icu.text.FilteredBreakIteratorBuilder;
import ohos.global.icu.text.UCharacterIterator;
import ohos.global.icu.util.BytesTrie;
import ohos.global.icu.util.CharsTrie;
import ohos.global.icu.util.CharsTrieBuilder;
import ohos.global.icu.util.StringTrieBuilder;
import ohos.global.icu.util.ULocale;

public class SimpleFilteredSentenceBreakIterator extends BreakIterator {
    private CharsTrie backwardsTrie;
    private BreakIterator delegate;
    private CharsTrie forwardsPartialTrie;
    private UCharacterIterator text;

    public SimpleFilteredSentenceBreakIterator(BreakIterator breakIterator, CharsTrie charsTrie, CharsTrie charsTrie2) {
        this.delegate = breakIterator;
        this.forwardsPartialTrie = charsTrie;
        this.backwardsTrie = charsTrie2;
    }

    private final void resetState() {
        this.text = UCharacterIterator.getInstance((CharacterIterator) this.delegate.getText().clone());
    }

    private final boolean breakExceptionAt(int i) {
        CharsTrie charsTrie;
        this.text.setIndex(i);
        this.backwardsTrie.reset();
        if (this.text.previousCodePoint() != 32) {
            this.text.nextCodePoint();
        }
        BytesTrie.Result result = BytesTrie.Result.INTERMEDIATE_VALUE;
        int i2 = -1;
        int i3 = -1;
        while (true) {
            int previousCodePoint = this.text.previousCodePoint();
            if (previousCodePoint == -1) {
                break;
            }
            result = this.backwardsTrie.nextForCodePoint(previousCodePoint);
            if (!result.hasNext()) {
                break;
            } else if (result.hasValue()) {
                i2 = this.text.getIndex();
                i3 = this.backwardsTrie.getValue();
            }
        }
        if (result.matches()) {
            i3 = this.backwardsTrie.getValue();
            i2 = this.text.getIndex();
        }
        if (i2 < 0) {
            return false;
        }
        if (i3 == 2) {
            return true;
        }
        if (i3 != 1 || (charsTrie = this.forwardsPartialTrie) == null) {
            return false;
        }
        charsTrie.reset();
        BytesTrie.Result result2 = BytesTrie.Result.INTERMEDIATE_VALUE;
        this.text.setIndex(i2);
        do {
            int nextCodePoint = this.text.nextCodePoint();
            if (nextCodePoint == -1) {
                break;
            }
            result2 = this.forwardsPartialTrie.nextForCodePoint(nextCodePoint);
        } while (result2.hasNext());
        if (result2.matches()) {
            return true;
        }
        return false;
    }

    private final int internalNext(int i) {
        if (!(i == -1 || this.backwardsTrie == null)) {
            resetState();
            int length = this.text.getLength();
            while (i != -1 && i != length && breakExceptionAt(i)) {
                i = this.delegate.next();
            }
        }
        return i;
    }

    private final int internalPrev(int i) {
        if (!(i == 0 || i == -1 || this.backwardsTrie == null)) {
            resetState();
            while (i != -1 && i != 0 && breakExceptionAt(i)) {
                i = this.delegate.previous();
            }
        }
        return i;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SimpleFilteredSentenceBreakIterator simpleFilteredSentenceBreakIterator = (SimpleFilteredSentenceBreakIterator) obj;
        return this.delegate.equals(simpleFilteredSentenceBreakIterator.delegate) && this.text.equals(simpleFilteredSentenceBreakIterator.text) && this.backwardsTrie.equals(simpleFilteredSentenceBreakIterator.backwardsTrie) && this.forwardsPartialTrie.equals(simpleFilteredSentenceBreakIterator.forwardsPartialTrie);
    }

    public int hashCode() {
        return (this.forwardsPartialTrie.hashCode() * 39) + (this.backwardsTrie.hashCode() * 11) + this.delegate.hashCode();
    }

    public Object clone() {
        return (SimpleFilteredSentenceBreakIterator) SimpleFilteredSentenceBreakIterator.super.clone();
    }

    public int first() {
        return this.delegate.first();
    }

    public int preceding(int i) {
        return internalPrev(this.delegate.preceding(i));
    }

    public int previous() {
        return internalPrev(this.delegate.previous());
    }

    public int current() {
        return this.delegate.current();
    }

    public boolean isBoundary(int i) {
        if (!this.delegate.isBoundary(i)) {
            return false;
        }
        if (this.backwardsTrie == null) {
            return true;
        }
        resetState();
        return !breakExceptionAt(i);
    }

    public int next() {
        return internalNext(this.delegate.next());
    }

    public int next(int i) {
        return internalNext(this.delegate.next(i));
    }

    public int following(int i) {
        return internalNext(this.delegate.following(i));
    }

    public int last() {
        return this.delegate.last();
    }

    public CharacterIterator getText() {
        return this.delegate.getText();
    }

    public void setText(CharacterIterator characterIterator) {
        this.delegate.setText(characterIterator);
    }

    public static class Builder extends FilteredBreakIteratorBuilder {
        static final int AddToForward = 2;
        static final int MATCH = 2;
        static final int PARTIAL = 1;
        static final int SuppressInReverse = 1;
        private HashSet<CharSequence> filterSet;

        public Builder(Locale locale) {
            this(ULocale.forLocale(locale));
        }

        public Builder(ULocale uLocale) {
            this.filterSet = new HashSet<>();
            ICUResourceBundle findWithFallback = ICUResourceBundle.getBundleInstance(ICUData.ICU_BRKITR_BASE_NAME, uLocale, ICUResourceBundle.OpenType.LOCALE_ROOT).findWithFallback("exceptions/SentenceBreak");
            if (findWithFallback != null) {
                int size = findWithFallback.getSize();
                for (int i = 0; i < size; i++) {
                    this.filterSet.add(findWithFallback.get(i).getString());
                }
            }
        }

        public Builder() {
            this.filterSet = new HashSet<>();
        }

        public boolean suppressBreakAfter(CharSequence charSequence) {
            return this.filterSet.add(charSequence);
        }

        public boolean unsuppressBreakAfter(CharSequence charSequence) {
            return this.filterSet.remove(charSequence);
        }

        public BreakIterator wrapIteratorWithFilter(BreakIterator breakIterator) {
            int i;
            if (this.filterSet.isEmpty()) {
                return breakIterator;
            }
            CharsTrieBuilder charsTrieBuilder = new CharsTrieBuilder();
            CharsTrieBuilder charsTrieBuilder2 = new CharsTrieBuilder();
            int size = this.filterSet.size();
            CharSequence[] charSequenceArr = new CharSequence[size];
            int[] iArr = new int[size];
            Iterator<CharSequence> it = this.filterSet.iterator();
            int i2 = 0;
            while (it.hasNext()) {
                charSequenceArr[i2] = it.next();
                iArr[i2] = 0;
                i2++;
            }
            int i3 = 0;
            for (int i4 = 0; i4 < size; i4++) {
                String charSequence = charSequenceArr[i4].toString();
                int indexOf = charSequence.indexOf(46);
                int i5 = -1;
                if (indexOf > -1 && (i = indexOf + 1) != charSequence.length()) {
                    int i6 = 0;
                    int i7 = -1;
                    while (i6 < size) {
                        if (i6 != i4 && charSequence.regionMatches(0, charSequenceArr[i6].toString(), 0, i)) {
                            if (iArr[i6] == 0) {
                                iArr[i6] = 3;
                            } else if ((iArr[i6] & 1) != 0) {
                                i7 = i6;
                            }
                        }
                        i6++;
                        i5 = -1;
                    }
                    if (i7 == i5 && iArr[i4] == 0) {
                        StringBuilder sb = new StringBuilder(charSequence.substring(0, i));
                        sb.reverse();
                        charsTrieBuilder.add(sb, 1);
                        i3++;
                        iArr[i4] = 3;
                    }
                }
            }
            int i8 = 0;
            for (int i9 = 0; i9 < size; i9++) {
                String charSequence2 = charSequenceArr[i9].toString();
                if (iArr[i9] == 0) {
                    charsTrieBuilder.add(new StringBuilder(charSequence2).reverse(), 2);
                    i3++;
                } else {
                    charsTrieBuilder2.add(charSequence2, 2);
                    i8++;
                }
            }
            CharsTrie charsTrie = null;
            CharsTrie build = i3 > 0 ? charsTrieBuilder.build(StringTrieBuilder.Option.FAST) : null;
            if (i8 > 0) {
                charsTrie = charsTrieBuilder2.build(StringTrieBuilder.Option.FAST);
            }
            return new SimpleFilteredSentenceBreakIterator(breakIterator, charsTrie, build);
        }
    }
}
