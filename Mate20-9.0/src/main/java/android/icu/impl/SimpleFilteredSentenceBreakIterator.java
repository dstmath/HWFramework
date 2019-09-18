package android.icu.impl;

import android.icu.impl.ICUResourceBundle;
import android.icu.text.BreakIterator;
import android.icu.text.FilteredBreakIteratorBuilder;
import android.icu.text.UCharacterIterator;
import android.icu.util.BytesTrie;
import android.icu.util.CharsTrie;
import android.icu.util.CharsTrieBuilder;
import android.icu.util.StringTrieBuilder;
import android.icu.util.ULocale;
import java.text.CharacterIterator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

public class SimpleFilteredSentenceBreakIterator extends BreakIterator {
    private CharsTrie backwardsTrie;
    private BreakIterator delegate;
    private CharsTrie forwardsPartialTrie;
    private UCharacterIterator text;

    public static class Builder extends FilteredBreakIteratorBuilder {
        static final int AddToForward = 2;
        static final int MATCH = 2;
        static final int PARTIAL = 1;
        static final int SuppressInReverse = 1;
        private HashSet<CharSequence> filterSet;

        public Builder(Locale loc) {
            this(ULocale.forLocale(loc));
        }

        public Builder(ULocale loc) {
            this.filterSet = new HashSet<>();
            ICUResourceBundle breaks = ICUResourceBundle.getBundleInstance(ICUData.ICU_BRKITR_BASE_NAME, loc, ICUResourceBundle.OpenType.LOCALE_ROOT).findWithFallback("exceptions/SentenceBreak");
            if (breaks != null) {
                int size = breaks.getSize();
                for (int index = 0; index < size; index++) {
                    this.filterSet.add(((ICUResourceBundle) breaks.get(index)).getString());
                }
            }
        }

        public Builder() {
            this.filterSet = new HashSet<>();
        }

        public boolean suppressBreakAfter(CharSequence str) {
            return this.filterSet.add(str);
        }

        public boolean unsuppressBreakAfter(CharSequence str) {
            return this.filterSet.remove(str);
        }

        public BreakIterator wrapIteratorWithFilter(BreakIterator adoptBreakIterator) {
            CharsTrie charsTrie;
            CharsTrie backwardsTrie;
            CharsTrie backwardsTrie2;
            int fwdCount;
            CharsTrie backwardsTrie3;
            CharsTrie backwardsTrie4;
            int fwdCount2;
            BreakIterator breakIterator = adoptBreakIterator;
            if (this.filterSet.isEmpty()) {
                return breakIterator;
            }
            CharsTrieBuilder builder = new CharsTrieBuilder();
            CharsTrieBuilder builder2 = new CharsTrieBuilder();
            int revCount = 0;
            int fwdCount3 = 0;
            int subCount = this.filterSet.size();
            CharSequence[] ustrs = new CharSequence[subCount];
            int[] partials = new int[subCount];
            CharsTrie backwardsTrie5 = null;
            CharsTrie forwardsPartialTrie = null;
            int i = 0;
            Iterator<CharSequence> it = this.filterSet.iterator();
            while (true) {
                charsTrie = null;
                if (!it.hasNext()) {
                    break;
                }
                ustrs[i] = it.next();
                partials[i] = 0;
                i++;
            }
            int i2 = 0;
            while (i2 < subCount) {
                String thisStr = ustrs[i2].toString();
                int nn = thisStr.indexOf(46);
                if (nn <= -1) {
                    fwdCount = fwdCount3;
                    backwardsTrie2 = backwardsTrie5;
                    backwardsTrie3 = charsTrie;
                } else if (nn + 1 != thisStr.length()) {
                    int sameAs = -1;
                    int j = 0;
                    while (j < subCount) {
                        if (j == i2) {
                            fwdCount2 = fwdCount3;
                            backwardsTrie4 = backwardsTrie5;
                        } else {
                            fwdCount2 = fwdCount3;
                            backwardsTrie4 = backwardsTrie5;
                            if (thisStr.regionMatches(0, ustrs[j].toString(), 0, nn + 1)) {
                                if (partials[j] == 0) {
                                    partials[j] = 3;
                                } else if ((partials[j] & 1) != 0) {
                                    sameAs = j;
                                }
                            }
                        }
                        j++;
                        fwdCount3 = fwdCount2;
                        backwardsTrie5 = backwardsTrie4;
                    }
                    fwdCount = fwdCount3;
                    backwardsTrie2 = backwardsTrie5;
                    if (sameAs == -1 && partials[i2] == 0) {
                        backwardsTrie3 = null;
                        StringBuilder prefix = new StringBuilder(thisStr.substring(0, nn + 1));
                        prefix.reverse();
                        builder.add(prefix, 1);
                        revCount++;
                        partials[i2] = 3;
                    } else {
                        backwardsTrie3 = null;
                    }
                } else {
                    fwdCount = fwdCount3;
                    backwardsTrie2 = backwardsTrie5;
                    backwardsTrie3 = null;
                }
                i2++;
                charsTrie = backwardsTrie3;
                fwdCount3 = fwdCount;
                backwardsTrie5 = backwardsTrie2;
            }
            int fwdCount4 = fwdCount3;
            CharsTrie backwardsTrie6 = backwardsTrie5;
            for (int i3 = 0; i3 < subCount; i3++) {
                String thisStr2 = ustrs[i3].toString();
                if (partials[i3] == 0) {
                    builder.add(new StringBuilder(thisStr2).reverse(), 2);
                    revCount++;
                } else {
                    builder2.add(thisStr2, 2);
                    fwdCount4++;
                }
            }
            if (revCount > 0) {
                backwardsTrie = builder.build(StringTrieBuilder.Option.FAST);
            } else {
                backwardsTrie = backwardsTrie6;
            }
            if (fwdCount4 > 0) {
                forwardsPartialTrie = builder2.build(StringTrieBuilder.Option.FAST);
            }
            return new SimpleFilteredSentenceBreakIterator(breakIterator, forwardsPartialTrie, backwardsTrie);
        }
    }

    public SimpleFilteredSentenceBreakIterator(BreakIterator adoptBreakIterator, CharsTrie forwardsPartialTrie2, CharsTrie backwardsTrie2) {
        this.delegate = adoptBreakIterator;
        this.forwardsPartialTrie = forwardsPartialTrie2;
        this.backwardsTrie = backwardsTrie2;
    }

    private final void resetState() {
        this.text = UCharacterIterator.getInstance((CharacterIterator) this.delegate.getText().clone());
    }

    private final boolean breakExceptionAt(int n) {
        BytesTrie.Result nextForCodePoint;
        int bestPosn = -1;
        int bestValue = -1;
        this.text.setIndex(n);
        this.backwardsTrie.reset();
        int previousCodePoint = this.text.previousCodePoint();
        int i = previousCodePoint;
        if (previousCodePoint != 32) {
            int uch = this.text.nextCodePoint();
        }
        BytesTrie.Result r = BytesTrie.Result.INTERMEDIATE_VALUE;
        while (true) {
            int previousCodePoint2 = this.text.previousCodePoint();
            int uch2 = previousCodePoint2;
            if (previousCodePoint2 == -1) {
                break;
            }
            BytesTrie.Result nextForCodePoint2 = this.backwardsTrie.nextForCodePoint(uch2);
            r = nextForCodePoint2;
            if (!nextForCodePoint2.hasNext()) {
                break;
            } else if (r.hasValue()) {
                bestPosn = this.text.getIndex();
                bestValue = this.backwardsTrie.getValue();
            }
        }
        if (r.matches()) {
            bestValue = this.backwardsTrie.getValue();
            bestPosn = this.text.getIndex();
        }
        if (bestPosn >= 0) {
            if (bestValue == 2) {
                return true;
            }
            if (bestValue == 1 && this.forwardsPartialTrie != null) {
                this.forwardsPartialTrie.reset();
                BytesTrie.Result rfwd = BytesTrie.Result.INTERMEDIATE_VALUE;
                this.text.setIndex(bestPosn);
                do {
                    int nextCodePoint = this.text.nextCodePoint();
                    int uch3 = nextCodePoint;
                    if (nextCodePoint == -1) {
                        break;
                    }
                    nextForCodePoint = this.forwardsPartialTrie.nextForCodePoint(uch3);
                    rfwd = nextForCodePoint;
                } while (nextForCodePoint.hasNext());
                if (rfwd.matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    private final int internalNext(int n) {
        if (n == -1 || this.backwardsTrie == null) {
            return n;
        }
        resetState();
        int textLen = this.text.getLength();
        while (n != -1 && n != textLen && breakExceptionAt(n)) {
            n = this.delegate.next();
        }
        return n;
    }

    private final int internalPrev(int n) {
        if (n == 0 || n == -1 || this.backwardsTrie == null) {
            return n;
        }
        resetState();
        while (n != -1 && n != 0 && breakExceptionAt(n)) {
            n = this.delegate.previous();
        }
        return n;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SimpleFilteredSentenceBreakIterator other = (SimpleFilteredSentenceBreakIterator) obj;
        if (this.delegate.equals(other.delegate) && this.text.equals(other.text) && this.backwardsTrie.equals(other.backwardsTrie) && this.forwardsPartialTrie.equals(other.forwardsPartialTrie)) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return (this.forwardsPartialTrie.hashCode() * 39) + (this.backwardsTrie.hashCode() * 11) + this.delegate.hashCode();
    }

    public Object clone() {
        return (SimpleFilteredSentenceBreakIterator) super.clone();
    }

    public int first() {
        return this.delegate.first();
    }

    public int preceding(int offset) {
        return internalPrev(this.delegate.preceding(offset));
    }

    public int previous() {
        return internalPrev(this.delegate.previous());
    }

    public int current() {
        return this.delegate.current();
    }

    public boolean isBoundary(int offset) {
        if (!this.delegate.isBoundary(offset)) {
            return false;
        }
        if (this.backwardsTrie == null) {
            return true;
        }
        resetState();
        return !breakExceptionAt(offset);
    }

    public int next() {
        return internalNext(this.delegate.next());
    }

    public int next(int n) {
        return internalNext(this.delegate.next(n));
    }

    public int following(int offset) {
        return internalNext(this.delegate.following(offset));
    }

    public int last() {
        return this.delegate.last();
    }

    public CharacterIterator getText() {
        return this.delegate.getText();
    }

    public void setText(CharacterIterator newText) {
        this.delegate.setText(newText);
    }
}
