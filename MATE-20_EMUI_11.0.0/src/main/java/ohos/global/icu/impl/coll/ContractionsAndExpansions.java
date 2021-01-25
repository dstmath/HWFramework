package ohos.global.icu.impl.coll;

import java.util.Iterator;
import ohos.global.icu.impl.Trie2;
import ohos.global.icu.text.UnicodeSet;
import ohos.global.icu.util.CharsTrie;

public final class ContractionsAndExpansions {
    static final /* synthetic */ boolean $assertionsDisabled = false;
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

    public ContractionsAndExpansions(UnicodeSet unicodeSet, UnicodeSet unicodeSet2, CESink cESink, boolean z) {
        this.contractions = unicodeSet;
        this.expansions = unicodeSet2;
        this.sink = cESink;
        this.addPrefixes = z;
    }

    public void forData(CollationData collationData) {
        if (collationData.base != null) {
            this.checkTailored = -1;
        }
        this.data = collationData;
        Iterator<Trie2.Range> it = this.data.trie.iterator();
        while (it.hasNext()) {
            Trie2.Range next = it.next();
            if (next.leadSurrogate) {
                break;
            }
            enumCnERange(next.startCodePoint, next.endCodePoint, next.value, this);
        }
        if (collationData.base != null) {
            this.tailored.freeze();
            this.checkTailored = 1;
            this.data = collationData.base;
            Iterator<Trie2.Range> it2 = this.data.trie.iterator();
            while (it2.hasNext()) {
                Trie2.Range next2 = it2.next();
                if (!next2.leadSurrogate) {
                    enumCnERange(next2.startCodePoint, next2.endCodePoint, next2.value, this);
                } else {
                    return;
                }
            }
        }
    }

    private void enumCnERange(int i, int i2, int i3, ContractionsAndExpansions contractionsAndExpansions) {
        int i4 = contractionsAndExpansions.checkTailored;
        if (i4 != 0) {
            if (i4 < 0) {
                if (i3 != 192) {
                    contractionsAndExpansions.tailored.add(i, i2);
                } else {
                    return;
                }
            } else if (i == i2) {
                if (contractionsAndExpansions.tailored.contains(i)) {
                    return;
                }
            } else if (contractionsAndExpansions.tailored.containsSome(i, i2)) {
                if (contractionsAndExpansions.ranges == null) {
                    contractionsAndExpansions.ranges = new UnicodeSet();
                }
                contractionsAndExpansions.ranges.set(i, i2).removeAll(contractionsAndExpansions.tailored);
                int rangeCount = contractionsAndExpansions.ranges.getRangeCount();
                for (int i5 = 0; i5 < rangeCount; i5++) {
                    contractionsAndExpansions.handleCE32(contractionsAndExpansions.ranges.getRangeStart(i5), contractionsAndExpansions.ranges.getRangeEnd(i5), i3);
                }
            }
        }
        contractionsAndExpansions.handleCE32(i, i2, i3);
    }

    public void forCodePoint(CollationData collationData, int i) {
        int ce32 = collationData.getCE32(i);
        if (ce32 == 192) {
            collationData = collationData.base;
            ce32 = collationData.getCE32(i);
        }
        this.data = collationData;
        handleCE32(i, i, ce32);
    }

    private void handleCE32(int i, int i2, int i3) {
        while ((i3 & 255) >= 192) {
            switch (Collation.tagFromCE32(i3)) {
                case 0:
                    return;
                case 1:
                    CESink cESink = this.sink;
                    if (cESink != null) {
                        cESink.handleCE(Collation.ceFromLongPrimaryCE32(i3));
                        return;
                    }
                    return;
                case 2:
                    CESink cESink2 = this.sink;
                    if (cESink2 != null) {
                        cESink2.handleCE(Collation.ceFromLongSecondaryCE32(i3));
                        return;
                    }
                    return;
                case 3:
                case 7:
                case 13:
                    throw new AssertionError(String.format("Unexpected CE32 tag type %d for ce32=0x%08x", Integer.valueOf(Collation.tagFromCE32(i3)), Integer.valueOf(i3)));
                case 4:
                    if (this.sink != null) {
                        this.ces[0] = Collation.latinCE0FromCE32(i3);
                        this.ces[1] = Collation.latinCE1FromCE32(i3);
                        this.sink.handleExpansion(this.ces, 0, 2);
                    }
                    if (this.unreversedPrefix.length() == 0) {
                        addExpansions(i, i2);
                        return;
                    }
                    return;
                case 5:
                    if (this.sink != null) {
                        int indexFromCE32 = Collation.indexFromCE32(i3);
                        int lengthFromCE32 = Collation.lengthFromCE32(i3);
                        for (int i4 = 0; i4 < lengthFromCE32; i4++) {
                            this.ces[i4] = Collation.ceFromCE32(this.data.ce32s[indexFromCE32 + i4]);
                        }
                        this.sink.handleExpansion(this.ces, 0, lengthFromCE32);
                    }
                    if (this.unreversedPrefix.length() == 0) {
                        addExpansions(i, i2);
                        return;
                    }
                    return;
                case 6:
                    if (this.sink != null) {
                        this.sink.handleExpansion(this.data.ces, Collation.indexFromCE32(i3), Collation.lengthFromCE32(i3));
                    }
                    if (this.unreversedPrefix.length() == 0) {
                        addExpansions(i, i2);
                        return;
                    }
                    return;
                case 8:
                    handlePrefixes(i, i2, i3);
                    return;
                case 9:
                    handleContractions(i, i2, i3);
                    return;
                case 10:
                    i3 = this.data.ce32s[Collation.indexFromCE32(i3)];
                    break;
                case 11:
                    i3 = this.data.ce32s[0];
                    break;
                case 12:
                    if (this.sink != null) {
                        UTF16CollationIterator uTF16CollationIterator = new UTF16CollationIterator(this.data);
                        StringBuilder sb = new StringBuilder(1);
                        for (int i5 = i; i5 <= i2; i5++) {
                            sb.setLength(0);
                            sb.appendCodePoint(i5);
                            uTF16CollationIterator.setText(false, sb, 0);
                            this.sink.handleExpansion(uTF16CollationIterator.getCEs(), 0, uTF16CollationIterator.fetchCEs() - 1);
                        }
                    }
                    if (this.unreversedPrefix.length() == 0) {
                        addExpansions(i, i2);
                        return;
                    }
                    return;
                case 14:
                case 15:
                    return;
            }
        }
        CESink cESink3 = this.sink;
        if (cESink3 != null) {
            cESink3.handleCE(Collation.ceFromSimpleCE32(i3));
        }
    }

    private void handlePrefixes(int i, int i2, int i3) {
        int indexFromCE32 = Collation.indexFromCE32(i3);
        handleCE32(i, i2, this.data.getCE32FromContexts(indexFromCE32));
        if (this.addPrefixes) {
            CharsTrie.Iterator it = new CharsTrie(this.data.contexts, indexFromCE32 + 2).iterator();
            while (it.hasNext()) {
                CharsTrie.Entry next = it.next();
                setPrefix(next.chars);
                addStrings(i, i2, this.contractions);
                addStrings(i, i2, this.expansions);
                handleCE32(i, i2, next.value);
            }
            resetPrefix();
        }
    }

    /* access modifiers changed from: package-private */
    public void handleContractions(int i, int i2, int i3) {
        int indexFromCE32 = Collation.indexFromCE32(i3);
        if ((i3 & 256) == 0) {
            handleCE32(i, i2, this.data.getCE32FromContexts(indexFromCE32));
        }
        CharsTrie.Iterator it = new CharsTrie(this.data.contexts, indexFromCE32 + 2).iterator();
        while (it.hasNext()) {
            CharsTrie.Entry next = it.next();
            this.suffix = next.chars.toString();
            addStrings(i, i2, this.contractions);
            if (this.unreversedPrefix.length() != 0) {
                addStrings(i, i2, this.expansions);
            }
            handleCE32(i, i2, next.value);
        }
        this.suffix = null;
    }

    /* access modifiers changed from: package-private */
    public void addExpansions(int i, int i2) {
        if (this.unreversedPrefix.length() == 0 && this.suffix == null) {
            UnicodeSet unicodeSet = this.expansions;
            if (unicodeSet != null) {
                unicodeSet.add(i, i2);
                return;
            }
            return;
        }
        addStrings(i, i2, this.expansions);
    }

    /* access modifiers changed from: package-private */
    public void addStrings(int i, int i2, UnicodeSet unicodeSet) {
        if (unicodeSet != null) {
            StringBuilder sb = new StringBuilder(this.unreversedPrefix);
            do {
                sb.appendCodePoint(i);
                String str = this.suffix;
                if (str != null) {
                    sb.append(str);
                }
                unicodeSet.add(sb);
                sb.setLength(this.unreversedPrefix.length());
                i++;
            } while (i <= i2);
        }
    }

    private void setPrefix(CharSequence charSequence) {
        this.unreversedPrefix.setLength(0);
        StringBuilder sb = this.unreversedPrefix;
        sb.append(charSequence);
        sb.reverse();
    }

    private void resetPrefix() {
        this.unreversedPrefix.setLength(0);
    }
}
