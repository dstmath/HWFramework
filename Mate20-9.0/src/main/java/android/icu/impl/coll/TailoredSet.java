package android.icu.impl.coll;

import android.icu.impl.Normalizer2Impl;
import android.icu.impl.Trie2;
import android.icu.impl.Utility;
import android.icu.text.UnicodeSet;
import android.icu.util.CharsTrie;
import java.util.Iterator;

public final class TailoredSet {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private CollationData baseData;
    private CollationData data;
    private String suffix;
    private UnicodeSet tailored;
    private StringBuilder unreversedPrefix = new StringBuilder();

    public TailoredSet(UnicodeSet t) {
        this.tailored = t;
    }

    public void forData(CollationData d) {
        this.data = d;
        this.baseData = d.base;
        Iterator<Trie2.Range> trieIterator = this.data.trie.iterator();
        while (trieIterator.hasNext()) {
            Trie2.Range next = trieIterator.next();
            Trie2.Range range = next;
            if (!next.leadSurrogate) {
                enumTailoredRange(range.startCodePoint, range.endCodePoint, range.value, this);
            } else {
                return;
            }
        }
    }

    private void enumTailoredRange(int start, int end, int ce32, TailoredSet ts) {
        if (ce32 != 192) {
            ts.handleCE32(start, end, ce32);
        }
    }

    private void handleCE32(int start, int end, int ce32) {
        if (Collation.isSpecialCE32(ce32)) {
            ce32 = this.data.getIndirectCE32(ce32);
            if (ce32 == 192) {
                return;
            }
        }
        do {
            int baseCE32 = this.baseData.getFinalCE32(this.baseData.getCE32(start));
            if (!Collation.isSelfContainedCE32(ce32) || !Collation.isSelfContainedCE32(baseCE32)) {
                compare(start, ce32, baseCE32);
            } else if (ce32 != baseCE32) {
                this.tailored.add(start);
            }
            start++;
        } while (start <= end);
    }

    private void compare(int c, int ce32, int baseCE32) {
        int tag;
        if (Collation.isPrefixCE32(ce32)) {
            int dataIndex = Collation.indexFromCE32(ce32);
            ce32 = this.data.getFinalCE32(this.data.getCE32FromContexts(dataIndex));
            if (Collation.isPrefixCE32(baseCE32)) {
                int baseIndex = Collation.indexFromCE32(baseCE32);
                baseCE32 = this.baseData.getFinalCE32(this.baseData.getCE32FromContexts(baseIndex));
                comparePrefixes(c, this.data.contexts, dataIndex + 2, this.baseData.contexts, baseIndex + 2);
            } else {
                addPrefixes(this.data, c, this.data.contexts, dataIndex + 2);
            }
        } else if (Collation.isPrefixCE32(baseCE32)) {
            int baseIndex2 = Collation.indexFromCE32(baseCE32);
            baseCE32 = this.baseData.getFinalCE32(this.baseData.getCE32FromContexts(baseIndex2));
            addPrefixes(this.baseData, c, this.baseData.contexts, baseIndex2 + 2);
        }
        if (Collation.isContractionCE32(ce32) != 0) {
            int dataIndex2 = Collation.indexFromCE32(ce32);
            if ((ce32 & 256) != 0) {
                ce32 = 1;
            } else {
                ce32 = this.data.getFinalCE32(this.data.getCE32FromContexts(dataIndex2));
            }
            if (Collation.isContractionCE32(baseCE32)) {
                int baseIndex3 = Collation.indexFromCE32(baseCE32);
                if ((baseCE32 & 256) != 0) {
                    baseCE32 = 1;
                } else {
                    baseCE32 = this.baseData.getFinalCE32(this.baseData.getCE32FromContexts(baseIndex3));
                }
                compareContractions(c, this.data.contexts, dataIndex2 + 2, this.baseData.contexts, baseIndex3 + 2);
            } else {
                addContractions(c, this.data.contexts, dataIndex2 + 2);
            }
        } else if (Collation.isContractionCE32(baseCE32)) {
            int baseIndex4 = Collation.indexFromCE32(baseCE32);
            baseCE32 = this.baseData.getFinalCE32(this.baseData.getCE32FromContexts(baseIndex4));
            addContractions(c, this.baseData.contexts, baseIndex4 + 2);
        }
        int baseTag = -1;
        if (Collation.isSpecialCE32(ce32) != 0) {
            tag = Collation.tagFromCE32(ce32);
        } else {
            tag = -1;
        }
        if (Collation.isSpecialCE32(baseCE32)) {
            baseTag = Collation.tagFromCE32(baseCE32);
        }
        if (baseTag == 14) {
            if (!Collation.isLongPrimaryCE32(ce32)) {
                add(c);
                return;
            }
            if (Collation.primaryFromLongPrimaryCE32(ce32) != Collation.getThreeBytePrimaryForOffsetData(c, this.baseData.ces[Collation.indexFromCE32(baseCE32)])) {
                add(c);
                return;
            }
        }
        if (tag != baseTag) {
            add(c);
            return;
        }
        int i = 0;
        if (tag == 5) {
            int length = Collation.lengthFromCE32(ce32);
            if (length != Collation.lengthFromCE32(baseCE32)) {
                add(c);
                return;
            }
            int idx0 = Collation.indexFromCE32(ce32);
            int idx1 = Collation.indexFromCE32(baseCE32);
            while (true) {
                if (i >= length) {
                    break;
                } else if (this.data.ce32s[idx0 + i] != this.baseData.ce32s[idx1 + i]) {
                    add(c);
                    break;
                } else {
                    i++;
                }
            }
        } else if (tag == 6) {
            int length2 = Collation.lengthFromCE32(ce32);
            if (length2 != Collation.lengthFromCE32(baseCE32)) {
                add(c);
                return;
            }
            int idx02 = Collation.indexFromCE32(ce32);
            int idx12 = Collation.indexFromCE32(baseCE32);
            while (true) {
                if (i >= length2) {
                    break;
                } else if (this.data.ces[idx02 + i] != this.baseData.ces[idx12 + i]) {
                    add(c);
                    break;
                } else {
                    i++;
                }
            }
        } else if (tag == 12) {
            StringBuilder jamos = new StringBuilder();
            int length3 = Normalizer2Impl.Hangul.decompose(c, jamos);
            if (this.tailored.contains((int) jamos.charAt(0)) || this.tailored.contains((int) jamos.charAt(1)) || (length3 == 3 && this.tailored.contains((int) jamos.charAt(2)))) {
                add(c);
            }
        } else if (ce32 != baseCE32) {
            add(c);
        }
    }

    private void comparePrefixes(int c, CharSequence p, int pidx, CharSequence q, int qidx) {
        int i = c;
        CharsTrie.Iterator prefixes = new CharsTrie(p, pidx).iterator();
        CharsTrie.Iterator basePrefixes = new CharsTrie(q, qidx).iterator();
        CharsTrie.Entry te = null;
        String bp = null;
        String tp = null;
        CharsTrie.Entry be = null;
        while (true) {
            if (tp == null) {
                if (prefixes.hasNext()) {
                    te = prefixes.next();
                    tp = te.chars.toString();
                } else {
                    te = null;
                    tp = "￿";
                }
            }
            if (bp == null) {
                if (basePrefixes.hasNext()) {
                    be = basePrefixes.next();
                    bp = be.chars.toString();
                } else {
                    be = null;
                    bp = "￿";
                }
            }
            if (!Utility.sameObjects(tp, "￿") || !Utility.sameObjects(bp, "￿")) {
                int cmp = tp.compareTo(bp);
                if (cmp < 0) {
                    addPrefix(this.data, tp, i, te.value);
                    te = null;
                    tp = null;
                } else if (cmp > 0) {
                    addPrefix(this.baseData, bp, i, be.value);
                    be = null;
                    bp = null;
                } else {
                    setPrefix(tp);
                    compare(i, te.value, be.value);
                    resetPrefix();
                    be = null;
                    te = null;
                    bp = null;
                    tp = null;
                }
            } else {
                return;
            }
        }
    }

    private void compareContractions(int c, CharSequence p, int pidx, CharSequence q, int qidx) {
        int i = c;
        CharsTrie.Iterator suffixes = new CharsTrie(p, pidx).iterator();
        CharsTrie.Iterator baseSuffixes = new CharsTrie(q, qidx).iterator();
        CharsTrie.Entry te = null;
        String bs = null;
        String ts = null;
        CharsTrie.Entry be = null;
        while (true) {
            if (ts == null) {
                if (suffixes.hasNext()) {
                    te = suffixes.next();
                    ts = te.chars.toString();
                } else {
                    te = null;
                    ts = "￿￿";
                }
            }
            if (bs == null) {
                if (baseSuffixes.hasNext()) {
                    be = baseSuffixes.next();
                    bs = be.chars.toString();
                } else {
                    be = null;
                    bs = "￿￿";
                }
            }
            if (!Utility.sameObjects(ts, "￿￿") || !Utility.sameObjects(bs, "￿￿")) {
                int cmp = ts.compareTo(bs);
                if (cmp < 0) {
                    addSuffix(i, ts);
                    te = null;
                    ts = null;
                } else if (cmp > 0) {
                    addSuffix(i, bs);
                    be = null;
                    bs = null;
                } else {
                    this.suffix = ts;
                    compare(i, te.value, be.value);
                    this.suffix = null;
                    be = null;
                    te = null;
                    bs = null;
                    ts = null;
                }
            } else {
                return;
            }
        }
    }

    private void addPrefixes(CollationData d, int c, CharSequence p, int pidx) {
        CharsTrie.Iterator prefixes = new CharsTrie(p, pidx).iterator();
        while (prefixes.hasNext()) {
            CharsTrie.Entry e = prefixes.next();
            addPrefix(d, e.chars, c, e.value);
        }
    }

    private void addPrefix(CollationData d, CharSequence pfx, int c, int ce32) {
        setPrefix(pfx);
        int ce322 = d.getFinalCE32(ce32);
        if (Collation.isContractionCE32(ce322)) {
            addContractions(c, d.contexts, Collation.indexFromCE32(ce322) + 2);
        }
        this.tailored.add((CharSequence) new StringBuilder(this.unreversedPrefix.appendCodePoint(c)));
        resetPrefix();
    }

    private void addContractions(int c, CharSequence p, int pidx) {
        CharsTrie.Iterator suffixes = new CharsTrie(p, pidx).iterator();
        while (suffixes.hasNext()) {
            addSuffix(c, suffixes.next().chars);
        }
    }

    private void addSuffix(int c, CharSequence sfx) {
        UnicodeSet unicodeSet = this.tailored;
        StringBuilder appendCodePoint = new StringBuilder(this.unreversedPrefix).appendCodePoint(c);
        appendCodePoint.append(sfx);
        unicodeSet.add((CharSequence) appendCodePoint);
    }

    private void add(int c) {
        if (this.unreversedPrefix.length() == 0 && this.suffix == null) {
            this.tailored.add(c);
            return;
        }
        StringBuilder s = new StringBuilder(this.unreversedPrefix);
        s.appendCodePoint(c);
        if (this.suffix != null) {
            s.append(this.suffix);
        }
        this.tailored.add((CharSequence) s);
    }

    private void setPrefix(CharSequence pfx) {
        this.unreversedPrefix.setLength(0);
        StringBuilder sb = this.unreversedPrefix;
        sb.append(pfx);
        sb.reverse();
    }

    private void resetPrefix() {
        this.unreversedPrefix.setLength(0);
    }
}
