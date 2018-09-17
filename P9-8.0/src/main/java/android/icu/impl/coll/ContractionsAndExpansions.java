package android.icu.impl.coll;

import android.icu.impl.Trie2.Range;
import android.icu.text.UnicodeSet;
import android.icu.util.CharsTrie;
import android.icu.util.CharsTrie.Entry;
import java.util.Iterator;

public final class ContractionsAndExpansions {
    static final /* synthetic */ boolean -assertionsDisabled = (ContractionsAndExpansions.class.desiredAssertionStatus() ^ 1);
    private boolean addPrefixes;
    private long[] ces = new long[31];
    private int checkTailored = 0;
    private UnicodeSet contractions;
    private CollationData data;
    private UnicodeSet expansions;
    private UnicodeSet ranges;
    private CESink sink;
    private String suffix;
    private UnicodeSet tailored = new UnicodeSet();
    private StringBuilder unreversedPrefix = new StringBuilder();

    public interface CESink {
        void handleCE(long j);

        void handleExpansion(long[] jArr, int i, int i2);
    }

    public ContractionsAndExpansions(UnicodeSet con, UnicodeSet exp, CESink s, boolean prefixes) {
        this.contractions = con;
        this.expansions = exp;
        this.sink = s;
        this.addPrefixes = prefixes;
    }

    public void forData(CollationData d) {
        Range range;
        if (d.base != null) {
            this.checkTailored = -1;
        }
        this.data = d;
        Iterator<Range> trieIterator = this.data.trie.iterator();
        while (trieIterator.hasNext()) {
            range = (Range) trieIterator.next();
            if ((range.leadSurrogate ^ 1) == 0) {
                break;
            }
            enumCnERange(range.startCodePoint, range.endCodePoint, range.value, this);
        }
        if (d.base != null) {
            this.tailored.freeze();
            this.checkTailored = 1;
            this.data = d.base;
            trieIterator = this.data.trie.iterator();
            while (trieIterator.hasNext()) {
                range = (Range) trieIterator.next();
                if ((range.leadSurrogate ^ 1) == 0) {
                    break;
                }
                enumCnERange(range.startCodePoint, range.endCodePoint, range.value, this);
            }
        }
    }

    private void enumCnERange(int start, int end, int ce32, ContractionsAndExpansions cne) {
        if (cne.checkTailored != 0) {
            if (cne.checkTailored < 0) {
                if (ce32 != 192) {
                    cne.tailored.add(start, end);
                } else {
                    return;
                }
            } else if (start == end) {
                if (cne.tailored.contains(start)) {
                    return;
                }
            } else if (cne.tailored.containsSome(start, end)) {
                if (cne.ranges == null) {
                    cne.ranges = new UnicodeSet();
                }
                cne.ranges.set(start, end).removeAll(cne.tailored);
                int count = cne.ranges.getRangeCount();
                for (int i = 0; i < count; i++) {
                    cne.handleCE32(cne.ranges.getRangeStart(i), cne.ranges.getRangeEnd(i), ce32);
                }
            }
        }
        cne.handleCE32(start, end, ce32);
    }

    public void forCodePoint(CollationData d, int c) {
        int ce32 = d.getCE32(c);
        if (ce32 == 192) {
            d = d.base;
            ce32 = d.getCE32(c);
        }
        this.data = d;
        handleCE32(c, c, ce32);
    }

    private void handleCE32(int start, int end, int ce32) {
        while ((ce32 & 255) >= 192) {
            int length;
            switch (Collation.tagFromCE32(ce32)) {
                case 0:
                    return;
                case 1:
                    if (this.sink != null) {
                        this.sink.handleCE(Collation.ceFromLongPrimaryCE32(ce32));
                    }
                    return;
                case 2:
                    if (this.sink != null) {
                        this.sink.handleCE(Collation.ceFromLongSecondaryCE32(ce32));
                    }
                    return;
                case 3:
                case 7:
                case 13:
                    throw new AssertionError(String.format("Unexpected CE32 tag type %d for ce32=0x%08x", new Object[]{Integer.valueOf(Collation.tagFromCE32(ce32)), Integer.valueOf(ce32)}));
                case 4:
                    if (this.sink != null) {
                        this.ces[0] = Collation.latinCE0FromCE32(ce32);
                        this.ces[1] = Collation.latinCE1FromCE32(ce32);
                        this.sink.handleExpansion(this.ces, 0, 2);
                    }
                    if (this.unreversedPrefix.length() == 0) {
                        addExpansions(start, end);
                    }
                    return;
                case 5:
                    if (this.sink != null) {
                        int idx = Collation.indexFromCE32(ce32);
                        length = Collation.lengthFromCE32(ce32);
                        for (int i = 0; i < length; i++) {
                            this.ces[i] = Collation.ceFromCE32(this.data.ce32s[idx + i]);
                        }
                        this.sink.handleExpansion(this.ces, 0, length);
                    }
                    if (this.unreversedPrefix.length() == 0) {
                        addExpansions(start, end);
                    }
                    return;
                case 6:
                    if (this.sink != null) {
                        this.sink.handleExpansion(this.data.ces, Collation.indexFromCE32(ce32), Collation.lengthFromCE32(ce32));
                    }
                    if (this.unreversedPrefix.length() == 0) {
                        addExpansions(start, end);
                    }
                    return;
                case 8:
                    handlePrefixes(start, end, ce32);
                    return;
                case 9:
                    handleContractions(start, end, ce32);
                    return;
                case 10:
                    ce32 = this.data.ce32s[Collation.indexFromCE32(ce32)];
                    break;
                case 11:
                    if (-assertionsDisabled || (start == 0 && end == 0)) {
                        ce32 = this.data.ce32s[0];
                        break;
                    }
                    throw new AssertionError();
                case 12:
                    if (this.sink != null) {
                        UTF16CollationIterator iter = new UTF16CollationIterator(this.data);
                        StringBuilder hangul = new StringBuilder(1);
                        int c = start;
                        while (c <= end) {
                            hangul.setLength(0);
                            hangul.appendCodePoint(c);
                            iter.setText(false, hangul, 0);
                            length = iter.fetchCEs();
                            if (-assertionsDisabled || (length >= 2 && iter.getCE(length - 1) == Collation.NO_CE)) {
                                this.sink.handleExpansion(iter.getCEs(), 0, length - 1);
                                c++;
                            } else {
                                throw new AssertionError();
                            }
                        }
                    }
                    if (this.unreversedPrefix.length() == 0) {
                        addExpansions(start, end);
                    }
                    return;
                case 14:
                    return;
                case 15:
                    return;
                default:
                    break;
            }
        }
        if (this.sink != null) {
            this.sink.handleCE(Collation.ceFromSimpleCE32(ce32));
        }
    }

    private void handlePrefixes(int start, int end, int ce32) {
        int index = Collation.indexFromCE32(ce32);
        handleCE32(start, end, this.data.getCE32FromContexts(index));
        if (this.addPrefixes) {
            CharsTrie.Iterator prefixes = new CharsTrie(this.data.contexts, index + 2).iterator();
            while (prefixes.hasNext()) {
                Entry e = prefixes.next();
                setPrefix(e.chars);
                addStrings(start, end, this.contractions);
                addStrings(start, end, this.expansions);
                handleCE32(start, end, e.value);
            }
            resetPrefix();
        }
    }

    void handleContractions(int start, int end, int ce32) {
        int index = Collation.indexFromCE32(ce32);
        if ((ce32 & 256) == 0) {
            ce32 = this.data.getCE32FromContexts(index);
            if (-assertionsDisabled || !Collation.isContractionCE32(ce32)) {
                handleCE32(start, end, ce32);
            } else {
                throw new AssertionError();
            }
        } else if (!-assertionsDisabled && this.unreversedPrefix.length() == 0) {
            throw new AssertionError();
        }
        CharsTrie.Iterator suffixes = new CharsTrie(this.data.contexts, index + 2).iterator();
        while (suffixes.hasNext()) {
            Entry e = suffixes.next();
            this.suffix = e.chars.toString();
            addStrings(start, end, this.contractions);
            if (this.unreversedPrefix.length() != 0) {
                addStrings(start, end, this.expansions);
            }
            handleCE32(start, end, e.value);
        }
        this.suffix = null;
    }

    void addExpansions(int start, int end) {
        if (this.unreversedPrefix.length() != 0 || this.suffix != null) {
            addStrings(start, end, this.expansions);
        } else if (this.expansions != null) {
            this.expansions.add(start, end);
        }
    }

    void addStrings(int start, int end, UnicodeSet set) {
        if (set != null) {
            CharSequence s = new StringBuilder(this.unreversedPrefix);
            do {
                s.appendCodePoint(start);
                if (this.suffix != null) {
                    s.append(this.suffix);
                }
                set.add(s);
                s.setLength(this.unreversedPrefix.length());
                start++;
            } while (start <= end);
        }
    }

    private void setPrefix(CharSequence pfx) {
        this.unreversedPrefix.setLength(0);
        this.unreversedPrefix.append(pfx).reverse();
    }

    private void resetPrefix() {
        this.unreversedPrefix.setLength(0);
    }
}
