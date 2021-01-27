package ohos.global.icu.impl.coll;

import java.util.Iterator;
import ohos.global.icu.impl.Normalizer2Impl;
import ohos.global.icu.impl.Trie2;
import ohos.global.icu.impl.Utility;
import ohos.global.icu.text.UnicodeSet;
import ohos.global.icu.util.CharsTrie;

public final class TailoredSet {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private CollationData baseData;
    private CollationData data;
    private String suffix;
    private UnicodeSet tailored;
    private StringBuilder unreversedPrefix = new StringBuilder();

    public TailoredSet(UnicodeSet unicodeSet) {
        this.tailored = unicodeSet;
    }

    public void forData(CollationData collationData) {
        this.data = collationData;
        this.baseData = collationData.base;
        Iterator<Trie2.Range> it = this.data.trie.iterator();
        while (it.hasNext()) {
            Trie2.Range next = it.next();
            if (!next.leadSurrogate) {
                enumTailoredRange(next.startCodePoint, next.endCodePoint, next.value, this);
            } else {
                return;
            }
        }
    }

    private void enumTailoredRange(int i, int i2, int i3, TailoredSet tailoredSet) {
        if (i3 != 192) {
            tailoredSet.handleCE32(i, i2, i3);
        }
    }

    private void handleCE32(int i, int i2, int i3) {
        if (!Collation.isSpecialCE32(i3) || (i3 = this.data.getIndirectCE32(i3)) != 192) {
            do {
                CollationData collationData = this.baseData;
                int finalCE32 = collationData.getFinalCE32(collationData.getCE32(i));
                if (!Collation.isSelfContainedCE32(i3) || !Collation.isSelfContainedCE32(finalCE32)) {
                    compare(i, i3, finalCE32);
                } else if (i3 != finalCE32) {
                    this.tailored.add(i);
                }
                i++;
            } while (i <= i2);
        }
    }

    private void compare(int i, int i2, int i3) {
        int i4;
        if (Collation.isPrefixCE32(i2)) {
            int indexFromCE32 = Collation.indexFromCE32(i2);
            CollationData collationData = this.data;
            int finalCE32 = collationData.getFinalCE32(collationData.getCE32FromContexts(indexFromCE32));
            if (Collation.isPrefixCE32(i3)) {
                int indexFromCE322 = Collation.indexFromCE32(i3);
                CollationData collationData2 = this.baseData;
                int finalCE322 = collationData2.getFinalCE32(collationData2.getCE32FromContexts(indexFromCE322));
                comparePrefixes(i, this.data.contexts, indexFromCE32 + 2, this.baseData.contexts, indexFromCE322 + 2);
                i3 = finalCE322;
            } else {
                CollationData collationData3 = this.data;
                addPrefixes(collationData3, i, collationData3.contexts, indexFromCE32 + 2);
            }
            i2 = finalCE32;
        } else if (Collation.isPrefixCE32(i3)) {
            int indexFromCE323 = Collation.indexFromCE32(i3);
            CollationData collationData4 = this.baseData;
            int finalCE323 = collationData4.getFinalCE32(collationData4.getCE32FromContexts(indexFromCE323));
            CollationData collationData5 = this.baseData;
            addPrefixes(collationData5, i, collationData5.contexts, indexFromCE323 + 2);
            i3 = finalCE323;
        }
        if (Collation.isContractionCE32(i2)) {
            int indexFromCE324 = Collation.indexFromCE32(i2);
            if ((i2 & 256) != 0) {
                i2 = 1;
            } else {
                CollationData collationData6 = this.data;
                i2 = collationData6.getFinalCE32(collationData6.getCE32FromContexts(indexFromCE324));
            }
            if (Collation.isContractionCE32(i3)) {
                int indexFromCE325 = Collation.indexFromCE32(i3);
                if ((i3 & 256) != 0) {
                    i3 = 1;
                } else {
                    CollationData collationData7 = this.baseData;
                    i3 = collationData7.getFinalCE32(collationData7.getCE32FromContexts(indexFromCE325));
                }
                compareContractions(i, this.data.contexts, indexFromCE324 + 2, this.baseData.contexts, indexFromCE325 + 2);
            } else {
                addContractions(i, this.data.contexts, indexFromCE324 + 2);
            }
        } else if (Collation.isContractionCE32(i3)) {
            int indexFromCE326 = Collation.indexFromCE32(i3);
            CollationData collationData8 = this.baseData;
            int finalCE324 = collationData8.getFinalCE32(collationData8.getCE32FromContexts(indexFromCE326));
            addContractions(i, this.baseData.contexts, indexFromCE326 + 2);
            i3 = finalCE324;
        }
        int i5 = -1;
        if (Collation.isSpecialCE32(i2)) {
            i4 = Collation.tagFromCE32(i2);
        } else {
            i4 = -1;
        }
        if (Collation.isSpecialCE32(i3)) {
            i5 = Collation.tagFromCE32(i3);
        }
        if (i5 == 14) {
            if (!Collation.isLongPrimaryCE32(i2)) {
                add(i);
                return;
            }
            if (Collation.primaryFromLongPrimaryCE32(i2) != Collation.getThreeBytePrimaryForOffsetData(i, this.baseData.ces[Collation.indexFromCE32(i3)])) {
                add(i);
                return;
            }
        }
        if (i4 != i5) {
            add(i);
            return;
        }
        int i6 = 0;
        if (i4 == 5) {
            int lengthFromCE32 = Collation.lengthFromCE32(i2);
            if (lengthFromCE32 != Collation.lengthFromCE32(i3)) {
                add(i);
                return;
            }
            int indexFromCE327 = Collation.indexFromCE32(i2);
            int indexFromCE328 = Collation.indexFromCE32(i3);
            while (i6 < lengthFromCE32) {
                if (this.data.ce32s[indexFromCE327 + i6] != this.baseData.ce32s[indexFromCE328 + i6]) {
                    add(i);
                    return;
                }
                i6++;
            }
        } else if (i4 == 6) {
            int lengthFromCE322 = Collation.lengthFromCE32(i2);
            if (lengthFromCE322 != Collation.lengthFromCE32(i3)) {
                add(i);
                return;
            }
            int indexFromCE329 = Collation.indexFromCE32(i2);
            int indexFromCE3210 = Collation.indexFromCE32(i3);
            while (i6 < lengthFromCE322) {
                if (this.data.ces[indexFromCE329 + i6] != this.baseData.ces[indexFromCE3210 + i6]) {
                    add(i);
                    return;
                }
                i6++;
            }
        } else if (i4 == 12) {
            StringBuilder sb = new StringBuilder();
            int decompose = Normalizer2Impl.Hangul.decompose(i, sb);
            if (this.tailored.contains(sb.charAt(0)) || this.tailored.contains(sb.charAt(1)) || (decompose == 3 && this.tailored.contains(sb.charAt(2)))) {
                add(i);
            }
        } else if (i2 != i3) {
            add(i);
        }
    }

    private void comparePrefixes(int i, CharSequence charSequence, int i2, CharSequence charSequence2, int i3) {
        CharsTrie.Iterator it = new CharsTrie(charSequence, i2).iterator();
        CharsTrie.Iterator it2 = new CharsTrie(charSequence2, i3).iterator();
        while (true) {
            String str = null;
            String str2 = null;
            CharsTrie.Entry entry = null;
            CharsTrie.Entry entry2 = null;
            while (true) {
                if (str == null) {
                    if (it.hasNext()) {
                        CharsTrie.Entry next = it.next();
                        entry = next;
                        str = next.chars.toString();
                    } else {
                        entry = null;
                        str = "￿";
                    }
                }
                if (str2 == null) {
                    if (it2.hasNext()) {
                        CharsTrie.Entry next2 = it2.next();
                        entry2 = next2;
                        str2 = next2.chars.toString();
                    } else {
                        entry2 = null;
                        str2 = "￿";
                    }
                }
                if (!Utility.sameObjects(str, "￿") || !Utility.sameObjects(str2, "￿")) {
                    int compareTo = str.compareTo(str2);
                    if (compareTo >= 0) {
                        if (compareTo <= 0) {
                            break;
                        }
                        addPrefix(this.baseData, str2, i, entry2.value);
                        str2 = null;
                        entry2 = null;
                    } else {
                        addPrefix(this.data, str, i, entry.value);
                        str = null;
                        entry = null;
                    }
                } else {
                    return;
                }
            }
            setPrefix(str);
            compare(i, entry.value, entry2.value);
            resetPrefix();
        }
    }

    private void compareContractions(int i, CharSequence charSequence, int i2, CharSequence charSequence2, int i3) {
        CharsTrie.Iterator it = new CharsTrie(charSequence, i2).iterator();
        CharsTrie.Iterator it2 = new CharsTrie(charSequence2, i3).iterator();
        while (true) {
            String str = null;
            String str2 = null;
            CharsTrie.Entry entry = null;
            CharsTrie.Entry entry2 = null;
            while (true) {
                if (str == null) {
                    if (it.hasNext()) {
                        CharsTrie.Entry next = it.next();
                        entry = next;
                        str = next.chars.toString();
                    } else {
                        entry = null;
                        str = "￿￿";
                    }
                }
                if (str2 == null) {
                    if (it2.hasNext()) {
                        CharsTrie.Entry next2 = it2.next();
                        entry2 = next2;
                        str2 = next2.chars.toString();
                    } else {
                        entry2 = null;
                        str2 = "￿￿";
                    }
                }
                if (!Utility.sameObjects(str, "￿￿") || !Utility.sameObjects(str2, "￿￿")) {
                    int compareTo = str.compareTo(str2);
                    if (compareTo >= 0) {
                        if (compareTo <= 0) {
                            break;
                        }
                        addSuffix(i, str2);
                        str2 = null;
                        entry2 = null;
                    } else {
                        addSuffix(i, str);
                        str = null;
                        entry = null;
                    }
                } else {
                    return;
                }
            }
            this.suffix = str;
            compare(i, entry.value, entry2.value);
            this.suffix = null;
        }
    }

    private void addPrefixes(CollationData collationData, int i, CharSequence charSequence, int i2) {
        CharsTrie.Iterator it = new CharsTrie(charSequence, i2).iterator();
        while (it.hasNext()) {
            CharsTrie.Entry next = it.next();
            addPrefix(collationData, next.chars, i, next.value);
        }
    }

    private void addPrefix(CollationData collationData, CharSequence charSequence, int i, int i2) {
        setPrefix(charSequence);
        int finalCE32 = collationData.getFinalCE32(i2);
        if (Collation.isContractionCE32(finalCE32)) {
            addContractions(i, collationData.contexts, Collation.indexFromCE32(finalCE32) + 2);
        }
        this.tailored.add(new StringBuilder(this.unreversedPrefix.appendCodePoint(i)));
        resetPrefix();
    }

    private void addContractions(int i, CharSequence charSequence, int i2) {
        CharsTrie.Iterator it = new CharsTrie(charSequence, i2).iterator();
        while (it.hasNext()) {
            addSuffix(i, it.next().chars);
        }
    }

    private void addSuffix(int i, CharSequence charSequence) {
        UnicodeSet unicodeSet = this.tailored;
        StringBuilder appendCodePoint = new StringBuilder(this.unreversedPrefix).appendCodePoint(i);
        appendCodePoint.append(charSequence);
        unicodeSet.add(appendCodePoint);
    }

    private void add(int i) {
        if (this.unreversedPrefix.length() == 0 && this.suffix == null) {
            this.tailored.add(i);
            return;
        }
        StringBuilder sb = new StringBuilder(this.unreversedPrefix);
        sb.appendCodePoint(i);
        String str = this.suffix;
        if (str != null) {
            sb.append(str);
        }
        this.tailored.add(sb);
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
