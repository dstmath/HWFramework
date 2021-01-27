package ohos.global.icu.impl.coll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import ohos.global.icu.impl.Norm2AllModes;
import ohos.global.icu.impl.Normalizer2Impl;
import ohos.global.icu.impl.Trie2;
import ohos.global.icu.impl.Trie2Writable;
import ohos.global.icu.impl.UCharacterProperty;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.text.UnicodeSet;
import ohos.global.icu.text.UnicodeSetIterator;
import ohos.global.icu.util.CharsTrie;
import ohos.global.icu.util.CharsTrieBuilder;
import ohos.global.icu.util.StringTrieBuilder;

/* access modifiers changed from: package-private */
public final class CollationDataBuilder {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int IS_BUILDER_JAMO_CE32 = 256;
    protected CollationData base = null;
    protected CollationSettings baseSettings = null;
    protected UVector32 ce32s = new UVector32();
    protected UVector64 ce64s = new UVector64();
    protected DataBuilderCollationIterator collIter = null;
    protected ArrayList<ConditionalCE32> conditionalCE32s = new ArrayList<>();
    protected UnicodeSet contextChars = new UnicodeSet();
    protected StringBuilder contexts = new StringBuilder();
    protected CollationFastLatinBuilder fastLatinBuilder = null;
    protected boolean fastLatinEnabled = false;
    protected boolean modified = false;
    protected Normalizer2Impl nfcImpl = Norm2AllModes.getNFCInstance().impl;
    protected Trie2Writable trie = null;
    protected UnicodeSet unsafeBackwardSet = new UnicodeSet();

    /* access modifiers changed from: package-private */
    public interface CEModifier {
        long modifyCE(long j);

        long modifyCE32(int i);
    }

    protected static int jamoCpFromIndex(int i) {
        if (i < 19) {
            return i + Normalizer2Impl.Hangul.JAMO_L_BASE;
        }
        int i2 = i - 19;
        return i2 < 21 ? i2 + Normalizer2Impl.Hangul.JAMO_V_BASE : (i2 - 21) + 4520;
    }

    CollationDataBuilder() {
        this.ce32s.addElement(0);
    }

    /* access modifiers changed from: package-private */
    public void initForTailoring(CollationData collationData) {
        if (this.trie != null) {
            throw new IllegalStateException("attempt to reuse a CollationDataBuilder");
        } else if (collationData != null) {
            this.base = collationData;
            this.trie = new Trie2Writable(192, -195323);
            for (int i = 192; i <= 255; i++) {
                this.trie.set(i, 192);
            }
            this.trie.setRange(Normalizer2Impl.Hangul.HANGUL_BASE, Normalizer2Impl.Hangul.HANGUL_END, Collation.makeCE32FromTagAndIndex(12, 0), true);
            this.unsafeBackwardSet.addAll(collationData.unsafeBackwardSet);
        } else {
            throw new IllegalArgumentException("null CollationData");
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isCompressibleLeadByte(int i) {
        return this.base.isCompressibleLeadByte(i);
    }

    /* access modifiers changed from: package-private */
    public boolean isCompressiblePrimary(long j) {
        return isCompressibleLeadByte(((int) j) >>> 24);
    }

    /* access modifiers changed from: package-private */
    public boolean hasMappings() {
        return this.modified;
    }

    /* access modifiers changed from: package-private */
    public boolean isAssigned(int i) {
        return Collation.isAssignedCE32(this.trie.get(i));
    }

    /* access modifiers changed from: package-private */
    public void add(CharSequence charSequence, CharSequence charSequence2, long[] jArr, int i) {
        addCE32(charSequence, charSequence2, encodeCEs(jArr, i));
    }

    /* access modifiers changed from: package-private */
    public int encodeCEs(long[] jArr, int i) {
        if (i < 0 || i > 31) {
            throw new IllegalArgumentException("mapping to too many CEs");
        } else if (!isMutable()) {
            throw new IllegalStateException("attempt to add mappings after build()");
        } else if (i == 0) {
            return encodeOneCEAsCE32(0);
        } else {
            if (i == 1) {
                return encodeOneCE(jArr[0]);
            }
            if (i == 2) {
                long j = jArr[0];
                long j2 = jArr[1];
                long j3 = j >>> 32;
                if ((72057594037862655L & j) == 83886080 && (-4278190081L & j2) == 1280 && j3 != 0) {
                    return ((int) j3) | ((((int) j) & 65280) << 8) | ((((int) j2) >> 16) & 65280) | 192 | 4;
                }
            }
            int[] iArr = new int[31];
            for (int i2 = 0; i2 != i; i2++) {
                int encodeOneCEAsCE32 = encodeOneCEAsCE32(jArr[i2]);
                if (encodeOneCEAsCE32 == 1) {
                    return encodeExpansion(jArr, 0, i);
                }
                iArr[i2] = encodeOneCEAsCE32;
            }
            return encodeExpansion32(iArr, 0, i);
        }
    }

    /* JADX DEBUG: TODO: convert one arg to string using `String.valueOf()`, args: [(wrap: char : 0x0096: CAST (r2v2 char) = (char) (wrap: int : 0x0092: INVOKE  (r2v1 int) = (r8v0 java.lang.CharSequence) type: INTERFACE call: java.lang.CharSequence.length():int)), (r8v0 java.lang.CharSequence), (r9v1 java.lang.CharSequence)] */
    /* access modifiers changed from: package-private */
    public void addCE32(CharSequence charSequence, CharSequence charSequence2, int i) {
        ConditionalCE32 conditionalCE32;
        if (charSequence2.length() == 0) {
            throw new IllegalArgumentException("mapping from empty string");
        } else if (isMutable()) {
            boolean z = false;
            int codePointAt = Character.codePointAt(charSequence2, 0);
            int charCount = Character.charCount(codePointAt);
            int i2 = this.trie.get(codePointAt);
            if (charSequence.length() != 0 || charSequence2.length() > charCount) {
                z = true;
            }
            if (i2 == 192) {
                CollationData collationData = this.base;
                int finalCE32 = collationData.getFinalCE32(collationData.getCE32(codePointAt));
                if (z || Collation.ce32HasContext(finalCE32)) {
                    i2 = copyFromBaseCE32(codePointAt, finalCE32, true);
                    this.trie.set(codePointAt, i2);
                }
            }
            if (z) {
                if (!isBuilderContextCE32(i2)) {
                    int addConditionalCE32 = addConditionalCE32("\u0000", i2);
                    this.trie.set(codePointAt, makeBuilderContextCE32(addConditionalCE32));
                    this.contextChars.add(codePointAt);
                    conditionalCE32 = getConditionalCE32(addConditionalCE32);
                } else {
                    conditionalCE32 = getConditionalCE32ForCE32(i2);
                    conditionalCE32.builtCE32 = 1;
                }
                CharSequence subSequence = charSequence2.subSequence(charCount, charSequence2.length());
                StringBuilder sb = new StringBuilder();
                sb.append((char) charSequence.length());
                sb.append(charSequence);
                sb.append(subSequence);
                String sb2 = sb.toString();
                this.unsafeBackwardSet.addAll(subSequence);
                while (true) {
                    int i3 = conditionalCE32.next;
                    if (i3 < 0) {
                        conditionalCE32.next = addConditionalCE32(sb2, i);
                        break;
                    }
                    ConditionalCE32 conditionalCE322 = getConditionalCE32(i3);
                    int compareTo = sb2.compareTo(conditionalCE322.context);
                    if (compareTo < 0) {
                        int addConditionalCE322 = addConditionalCE32(sb2, i);
                        conditionalCE32.next = addConditionalCE322;
                        getConditionalCE32(addConditionalCE322).next = i3;
                        break;
                    } else if (compareTo == 0) {
                        conditionalCE322.ce32 = i;
                        break;
                    } else {
                        conditionalCE32 = conditionalCE322;
                    }
                }
            } else if (!isBuilderContextCE32(i2)) {
                this.trie.set(codePointAt, i);
            } else {
                ConditionalCE32 conditionalCE32ForCE32 = getConditionalCE32ForCE32(i2);
                conditionalCE32ForCE32.builtCE32 = 1;
                conditionalCE32ForCE32.ce32 = i;
            }
            this.modified = true;
        } else {
            throw new IllegalStateException("attempt to add mappings after build()");
        }
    }

    /* access modifiers changed from: package-private */
    public void copyFrom(CollationDataBuilder collationDataBuilder, CEModifier cEModifier) {
        if (isMutable()) {
            CopyHelper copyHelper = new CopyHelper(collationDataBuilder, this, cEModifier);
            Iterator<Trie2.Range> it = collationDataBuilder.trie.iterator();
            while (it.hasNext()) {
                Trie2.Range next = it.next();
                if (next.leadSurrogate) {
                    break;
                }
                enumRangeForCopy(next.startCodePoint, next.endCodePoint, next.value, copyHelper);
            }
            this.modified = collationDataBuilder.modified | this.modified;
            return;
        }
        throw new IllegalStateException("attempt to copyFrom() after build()");
    }

    /* access modifiers changed from: package-private */
    public void optimize(UnicodeSet unicodeSet) {
        if (!unicodeSet.isEmpty()) {
            UnicodeSetIterator unicodeSetIterator = new UnicodeSetIterator(unicodeSet);
            while (unicodeSetIterator.next() && unicodeSetIterator.codepoint != UnicodeSetIterator.IS_STRING) {
                int i = unicodeSetIterator.codepoint;
                if (this.trie.get(i) == 192) {
                    CollationData collationData = this.base;
                    this.trie.set(i, copyFromBaseCE32(i, collationData.getFinalCE32(collationData.getCE32(i)), true));
                }
            }
            this.modified = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void suppressContractions(UnicodeSet unicodeSet) {
        if (!unicodeSet.isEmpty()) {
            UnicodeSetIterator unicodeSetIterator = new UnicodeSetIterator(unicodeSet);
            while (unicodeSetIterator.next() && unicodeSetIterator.codepoint != UnicodeSetIterator.IS_STRING) {
                int i = unicodeSetIterator.codepoint;
                int i2 = this.trie.get(i);
                if (i2 == 192) {
                    CollationData collationData = this.base;
                    int finalCE32 = collationData.getFinalCE32(collationData.getCE32(i));
                    if (Collation.ce32HasContext(finalCE32)) {
                        this.trie.set(i, copyFromBaseCE32(i, finalCE32, false));
                    }
                } else if (isBuilderContextCE32(i2)) {
                    this.trie.set(i, getConditionalCE32ForCE32(i2).ce32);
                    this.contextChars.remove(i);
                }
            }
            this.modified = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void enableFastLatin() {
        this.fastLatinEnabled = true;
    }

    /* access modifiers changed from: package-private */
    public void build(CollationData collationData) {
        buildMappings(collationData);
        CollationData collationData2 = this.base;
        if (collationData2 != null) {
            collationData.numericPrimary = collationData2.numericPrimary;
            collationData.compressibleBytes = this.base.compressibleBytes;
            collationData.numScripts = this.base.numScripts;
            collationData.scriptsIndex = this.base.scriptsIndex;
            collationData.scriptStarts = this.base.scriptStarts;
        }
        buildFastLatinTable(collationData);
    }

    /* access modifiers changed from: package-private */
    public int getCEs(CharSequence charSequence, long[] jArr, int i) {
        return getCEs(charSequence, 0, jArr, i);
    }

    /* access modifiers changed from: package-private */
    public int getCEs(CharSequence charSequence, CharSequence charSequence2, long[] jArr, int i) {
        int length = charSequence.length();
        if (length == 0) {
            return getCEs(charSequence2, 0, jArr, i);
        }
        StringBuilder sb = new StringBuilder(charSequence);
        sb.append(charSequence2);
        return getCEs(sb, length, jArr, i);
    }

    /* access modifiers changed from: private */
    public static final class ConditionalCE32 {
        int builtCE32 = 1;
        int ce32;
        String context;
        int defaultCE32 = 1;
        int next = -1;

        ConditionalCE32(String str, int i) {
            this.context = str;
            this.ce32 = i;
        }

        /* access modifiers changed from: package-private */
        public boolean hasContext() {
            return this.context.length() > 1;
        }

        /* access modifiers changed from: package-private */
        public int prefixLength() {
            return this.context.charAt(0);
        }
    }

    /* access modifiers changed from: protected */
    public int getCE32FromOffsetCE32(boolean z, int i, int i2) {
        int indexFromCE32 = Collation.indexFromCE32(i2);
        return Collation.makeLongPrimaryCE32(Collation.getThreeBytePrimaryForOffsetData(i, z ? this.base.ces[indexFromCE32] : this.ce64s.elementAti(indexFromCE32)));
    }

    /* access modifiers changed from: protected */
    public int addCE(long j) {
        int size = this.ce64s.size();
        for (int i = 0; i < size; i++) {
            if (j == this.ce64s.elementAti(i)) {
                return i;
            }
        }
        this.ce64s.addElement(j);
        return size;
    }

    /* access modifiers changed from: protected */
    public int addCE32(int i) {
        int size = this.ce32s.size();
        for (int i2 = 0; i2 < size; i2++) {
            if (i == this.ce32s.elementAti(i2)) {
                return i2;
            }
        }
        this.ce32s.addElement(i);
        return size;
    }

    /* access modifiers changed from: protected */
    public int addConditionalCE32(String str, int i) {
        int size = this.conditionalCE32s.size();
        if (size <= 524287) {
            this.conditionalCE32s.add(new ConditionalCE32(str, i));
            return size;
        }
        throw new IndexOutOfBoundsException("too many context-sensitive mappings");
    }

    /* access modifiers changed from: protected */
    public ConditionalCE32 getConditionalCE32(int i) {
        return this.conditionalCE32s.get(i);
    }

    /* access modifiers changed from: protected */
    public ConditionalCE32 getConditionalCE32ForCE32(int i) {
        return getConditionalCE32(Collation.indexFromCE32(i));
    }

    protected static int makeBuilderContextCE32(int i) {
        return Collation.makeCE32FromTagAndIndex(7, i);
    }

    protected static boolean isBuilderContextCE32(int i) {
        return Collation.hasCE32Tag(i, 7);
    }

    protected static int encodeOneCEAsCE32(long j) {
        long j2 = j >>> 32;
        int i = (int) j;
        int i2 = 65535 & i;
        if ((281470698455295L & j) == 0) {
            return ((int) j2) | (i >>> 16) | (i2 >> 8);
        }
        if ((j & 1099511627775L) == 83887360) {
            return Collation.makeLongPrimaryCE32(j2);
        }
        if (j2 == 0 && (i2 & 255) == 0) {
            return Collation.makeLongSecondaryCE32(i);
        }
        return 1;
    }

    /* access modifiers changed from: protected */
    public int encodeOneCE(long j) {
        int encodeOneCEAsCE32 = encodeOneCEAsCE32(j);
        if (encodeOneCEAsCE32 != 1) {
            return encodeOneCEAsCE32;
        }
        int addCE = addCE(j);
        if (addCE <= 524287) {
            return Collation.makeCE32FromTagIndexAndLength(6, addCE, 1);
        }
        throw new IndexOutOfBoundsException("too many mappings");
    }

    /* access modifiers changed from: protected */
    public int encodeExpansion(long[] jArr, int i, int i2) {
        long j = jArr[i];
        int size = this.ce64s.size() - i2;
        for (int i3 = 0; i3 <= size; i3++) {
            if (j == this.ce64s.elementAti(i3)) {
                if (i3 <= 524287) {
                    for (int i4 = 1; i4 != i2; i4++) {
                        if (this.ce64s.elementAti(i3 + i4) == jArr[i + i4]) {
                        }
                    }
                    return Collation.makeCE32FromTagIndexAndLength(6, i3, i2);
                }
                throw new IndexOutOfBoundsException("too many mappings");
            }
        }
        int size2 = this.ce64s.size();
        if (size2 <= 524287) {
            for (int i5 = 0; i5 < i2; i5++) {
                this.ce64s.addElement(jArr[i + i5]);
            }
            return Collation.makeCE32FromTagIndexAndLength(6, size2, i2);
        }
        throw new IndexOutOfBoundsException("too many mappings");
    }

    /* access modifiers changed from: protected */
    public int encodeExpansion32(int[] iArr, int i, int i2) {
        int i3 = iArr[i];
        int size = this.ce32s.size() - i2;
        for (int i4 = 0; i4 <= size; i4++) {
            if (i3 == this.ce32s.elementAti(i4)) {
                if (i4 <= 524287) {
                    for (int i5 = 1; i5 != i2; i5++) {
                        if (this.ce32s.elementAti(i4 + i5) == iArr[i + i5]) {
                        }
                    }
                    return Collation.makeCE32FromTagIndexAndLength(5, i4, i2);
                }
                throw new IndexOutOfBoundsException("too many mappings");
            }
        }
        int size2 = this.ce32s.size();
        if (size2 <= 524287) {
            for (int i6 = 0; i6 < i2; i6++) {
                this.ce32s.addElement(iArr[i + i6]);
            }
            return Collation.makeCE32FromTagIndexAndLength(5, size2, i2);
        }
        throw new IndexOutOfBoundsException("too many mappings");
    }

    /* access modifiers changed from: protected */
    public int copyFromBaseCE32(int i, int i2, boolean z) {
        int tagFromCE32;
        int i3;
        int i4;
        if (!Collation.isSpecialCE32(i2) || (tagFromCE32 = Collation.tagFromCE32(i2)) == 1 || tagFromCE32 == 2 || tagFromCE32 == 4) {
            return i2;
        }
        if (tagFromCE32 == 5) {
            return encodeExpansion32(this.base.ce32s, Collation.indexFromCE32(i2), Collation.lengthFromCE32(i2));
        }
        if (tagFromCE32 == 6) {
            return encodeExpansion(this.base.ces, Collation.indexFromCE32(i2), Collation.lengthFromCE32(i2));
        }
        if (tagFromCE32 == 8) {
            int indexFromCE32 = Collation.indexFromCE32(i2);
            int cE32FromContexts = this.base.getCE32FromContexts(indexFromCE32);
            if (!z) {
                return copyFromBaseCE32(i, cE32FromContexts, false);
            }
            ConditionalCE32 conditionalCE32 = new ConditionalCE32("", 0);
            StringBuilder sb = new StringBuilder("\u0000");
            if (Collation.isContractionCE32(cE32FromContexts)) {
                i3 = copyContractionsFromBaseCE32(sb, i, cE32FromContexts, conditionalCE32);
            } else {
                i3 = addConditionalCE32(sb.toString(), copyFromBaseCE32(i, cE32FromContexts, true));
                conditionalCE32.next = i3;
            }
            ConditionalCE32 conditionalCE322 = getConditionalCE32(i3);
            CharsTrie.Iterator it = CharsTrie.iterator(this.base.contexts, indexFromCE32 + 2, 0);
            while (it.hasNext()) {
                CharsTrie.Entry next = it.next();
                sb.setLength(0);
                sb.append(next.chars);
                sb.reverse().insert(0, (char) next.chars.length());
                int i5 = next.value;
                if (Collation.isContractionCE32(i5)) {
                    i4 = copyContractionsFromBaseCE32(sb, i, i5, conditionalCE322);
                } else {
                    int addConditionalCE32 = addConditionalCE32(sb.toString(), copyFromBaseCE32(i, i5, true));
                    conditionalCE322.next = addConditionalCE32;
                    i4 = addConditionalCE32;
                }
                conditionalCE322 = getConditionalCE32(i4);
            }
            int makeBuilderContextCE32 = makeBuilderContextCE32(conditionalCE32.next);
            this.contextChars.add(i);
            return makeBuilderContextCE32;
        } else if (tagFromCE32 != 9) {
            if (tagFromCE32 == 12) {
                throw new UnsupportedOperationException("We forbid tailoring of Hangul syllables.");
            } else if (tagFromCE32 == 14) {
                return getCE32FromOffsetCE32(true, i, i2);
            } else {
                if (tagFromCE32 == 15) {
                    return encodeOneCE(Collation.unassignedCEFromCodePoint(i));
                }
                throw new AssertionError("copyFromBaseCE32(c, ce32, withContext) requires ce32 == base.getFinalCE32(ce32)");
            }
        } else if (!z) {
            return copyFromBaseCE32(i, this.base.getCE32FromContexts(Collation.indexFromCE32(i2)), false);
        } else {
            ConditionalCE32 conditionalCE323 = new ConditionalCE32("", 0);
            copyContractionsFromBaseCE32(new StringBuilder("\u0000"), i, i2, conditionalCE323);
            int makeBuilderContextCE322 = makeBuilderContextCE32(conditionalCE323.next);
            this.contextChars.add(i);
            return makeBuilderContextCE322;
        }
    }

    /* access modifiers changed from: protected */
    public int copyContractionsFromBaseCE32(StringBuilder sb, int i, int i2, ConditionalCE32 conditionalCE32) {
        int i3;
        int indexFromCE32 = Collation.indexFromCE32(i2);
        if ((i2 & 256) != 0) {
            i3 = -1;
        } else {
            i3 = addConditionalCE32(sb.toString(), copyFromBaseCE32(i, this.base.getCE32FromContexts(indexFromCE32), true));
            conditionalCE32.next = i3;
            conditionalCE32 = getConditionalCE32(i3);
        }
        int length = sb.length();
        CharsTrie.Iterator it = CharsTrie.iterator(this.base.contexts, indexFromCE32 + 2, 0);
        while (it.hasNext()) {
            CharsTrie.Entry next = it.next();
            sb.append(next.chars);
            i3 = addConditionalCE32(sb.toString(), copyFromBaseCE32(i, next.value, true));
            conditionalCE32.next = i3;
            conditionalCE32 = getConditionalCE32(i3);
            sb.setLength(length);
        }
        return i3;
    }

    /* access modifiers changed from: private */
    public static final class CopyHelper {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        CollationDataBuilder dest;
        long[] modifiedCEs = new long[31];
        CEModifier modifier;
        CollationDataBuilder src;

        CopyHelper(CollationDataBuilder collationDataBuilder, CollationDataBuilder collationDataBuilder2, CEModifier cEModifier) {
            this.src = collationDataBuilder;
            this.dest = collationDataBuilder2;
            this.modifier = cEModifier;
        }

        /* access modifiers changed from: package-private */
        public void copyRangeCE32(int i, int i2, int i3) {
            int copyCE32 = copyCE32(i3);
            this.dest.trie.setRange(i, i2, copyCE32, true);
            if (CollationDataBuilder.isBuilderContextCE32(copyCE32)) {
                this.dest.contextChars.add(i, i2);
            }
        }

        /* access modifiers changed from: package-private */
        public int copyCE32(int i) {
            int encodeExpansion;
            if (!Collation.isSpecialCE32(i)) {
                long modifyCE32 = this.modifier.modifyCE32(i);
                if (modifyCE32 != Collation.NO_CE) {
                    return this.dest.encodeOneCE(modifyCE32);
                }
                return i;
            }
            int tagFromCE32 = Collation.tagFromCE32(i);
            if (tagFromCE32 == 5) {
                int[] buffer = this.src.ce32s.getBuffer();
                int indexFromCE32 = Collation.indexFromCE32(i);
                int lengthFromCE32 = Collation.lengthFromCE32(i);
                boolean z = false;
                for (int i2 = 0; i2 < lengthFromCE32; i2++) {
                    int i3 = buffer[indexFromCE32 + i2];
                    if (!Collation.isSpecialCE32(i3)) {
                        long modifyCE322 = this.modifier.modifyCE32(i3);
                        if (modifyCE322 != Collation.NO_CE) {
                            if (!z) {
                                for (int i4 = 0; i4 < i2; i4++) {
                                    this.modifiedCEs[i4] = Collation.ceFromCE32(buffer[indexFromCE32 + i4]);
                                }
                                z = true;
                            }
                            this.modifiedCEs[i2] = modifyCE322;
                        }
                    }
                    if (z) {
                        this.modifiedCEs[i2] = Collation.ceFromCE32(i3);
                    }
                }
                if (z) {
                    encodeExpansion = this.dest.encodeCEs(this.modifiedCEs, lengthFromCE32);
                } else {
                    encodeExpansion = this.dest.encodeExpansion32(buffer, indexFromCE32, lengthFromCE32);
                }
            } else if (tagFromCE32 == 6) {
                long[] buffer2 = this.src.ce64s.getBuffer();
                int indexFromCE322 = Collation.indexFromCE32(i);
                int lengthFromCE322 = Collation.lengthFromCE32(i);
                boolean z2 = false;
                for (int i5 = 0; i5 < lengthFromCE322; i5++) {
                    long j = buffer2[indexFromCE322 + i5];
                    long modifyCE = this.modifier.modifyCE(j);
                    if (modifyCE != Collation.NO_CE) {
                        if (!z2) {
                            for (int i6 = 0; i6 < i5; i6++) {
                                this.modifiedCEs[i6] = buffer2[indexFromCE322 + i6];
                            }
                            z2 = true;
                        }
                        this.modifiedCEs[i5] = modifyCE;
                    } else if (z2) {
                        this.modifiedCEs[i5] = j;
                    }
                }
                if (z2) {
                    encodeExpansion = this.dest.encodeCEs(this.modifiedCEs, lengthFromCE322);
                } else {
                    encodeExpansion = this.dest.encodeExpansion(buffer2, indexFromCE322, lengthFromCE322);
                }
            } else if (tagFromCE32 != 7) {
                return i;
            } else {
                ConditionalCE32 conditionalCE32ForCE32 = this.src.getConditionalCE32ForCE32(i);
                int addConditionalCE32 = this.dest.addConditionalCE32(conditionalCE32ForCE32.context, copyCE32(conditionalCE32ForCE32.ce32));
                int makeBuilderContextCE32 = CollationDataBuilder.makeBuilderContextCE32(addConditionalCE32);
                while (conditionalCE32ForCE32.next >= 0) {
                    conditionalCE32ForCE32 = this.src.getConditionalCE32(conditionalCE32ForCE32.next);
                    ConditionalCE32 conditionalCE32 = this.dest.getConditionalCE32(addConditionalCE32);
                    int addConditionalCE322 = this.dest.addConditionalCE32(conditionalCE32ForCE32.context, copyCE32(conditionalCE32ForCE32.ce32));
                    this.dest.unsafeBackwardSet.addAll(conditionalCE32ForCE32.context.substring(conditionalCE32ForCE32.prefixLength() + 1));
                    conditionalCE32.next = addConditionalCE322;
                    addConditionalCE32 = addConditionalCE322;
                }
                return makeBuilderContextCE32;
            }
            return encodeExpansion;
        }
    }

    private static void enumRangeForCopy(int i, int i2, int i3, CopyHelper copyHelper) {
        if (i3 != -1 && i3 != 192) {
            copyHelper.copyRangeCE32(i, i2, i3);
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003f, code lost:
        if (r8 != false) goto L_0x0041;
     */
    public boolean getJamoCE32s(int[] iArr) {
        boolean z;
        boolean z2 = this.base == null;
        int i = 0;
        boolean z3 = false;
        while (true) {
            int i2 = 192;
            if (i < 67) {
                int jamoCpFromIndex = jamoCpFromIndex(i);
                int i3 = this.trie.get(jamoCpFromIndex);
                z2 |= Collation.isAssignedCE32(i3);
                if (i3 == 192) {
                    i3 = this.base.getCE32(jamoCpFromIndex);
                    z = true;
                } else {
                    z = false;
                }
                if (Collation.isSpecialCE32(i3)) {
                    switch (Collation.tagFromCE32(i3)) {
                        case 0:
                        case 3:
                        case 7:
                        case 10:
                        case 11:
                        case 12:
                        case 13:
                            throw new AssertionError(String.format("unexpected special tag in ce32=0x%08x", Integer.valueOf(i3)));
                        case 14:
                            i2 = getCE32FromOffsetCE32(z, jamoCpFromIndex, i3);
                            break;
                        case 15:
                            z3 = true;
                            break;
                    }
                    iArr[i] = i2;
                    i++;
                }
                i2 = i3;
                iArr[i] = i2;
                i++;
            } else {
                if (z2 && z3) {
                    for (int i4 = 0; i4 < 67; i4++) {
                        if (iArr[i4] == 192) {
                            int jamoCpFromIndex2 = jamoCpFromIndex(i4);
                            iArr[i4] = copyFromBaseCE32(jamoCpFromIndex2, this.base.getCE32(jamoCpFromIndex2), true);
                        }
                    }
                }
                return z2;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setDigitTags() {
        UnicodeSetIterator unicodeSetIterator = new UnicodeSetIterator(new UnicodeSet("[:Nd:]"));
        while (unicodeSetIterator.next()) {
            int i = unicodeSetIterator.codepoint;
            int i2 = this.trie.get(i);
            if (!(i2 == 192 || i2 == -1)) {
                int addCE32 = addCE32(i2);
                if (addCE32 <= 524287) {
                    this.trie.set(i, Collation.makeCE32FromTagIndexAndLength(10, addCE32, UCharacter.digit(i)));
                } else {
                    throw new IndexOutOfBoundsException("too many mappings");
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setLeadSurrogates() {
        int i;
        int i2;
        for (char c = 55296; c < 56320; c = (char) (c + 1)) {
            Iterator<Trie2.Range> iteratorForLeadSurrogate = this.trie.iteratorForLeadSurrogate(c);
            int i3 = -1;
            while (true) {
                i = 512;
                if (!iteratorForLeadSurrogate.hasNext()) {
                    i = i3;
                    break;
                }
                int i4 = iteratorForLeadSurrogate.next().value;
                if (i4 != -1) {
                    if (i4 != 192) {
                        break;
                    }
                    i2 = 256;
                } else {
                    i2 = 0;
                }
                if (i3 < 0) {
                    i3 = i2;
                } else if (i3 != i2) {
                    break;
                }
            }
            this.trie.setForLeadSurrogateCodeUnit(c, Collation.makeCE32FromTagAndIndex(13, 0) | i);
        }
    }

    /* access modifiers changed from: protected */
    public void buildMappings(CollationData collationData) {
        boolean z;
        if (isMutable()) {
            buildContexts();
            int[] iArr = new int[67];
            int i = -1;
            boolean jamoCE32s = getJamoCE32s(iArr);
            int i2 = Normalizer2Impl.Hangul.HANGUL_BASE;
            if (jamoCE32s) {
                i = this.ce32s.size();
                for (int i3 = 0; i3 < 67; i3++) {
                    this.ce32s.addElement(iArr[i3]);
                }
                int i4 = 19;
                while (true) {
                    if (i4 >= 67) {
                        z = false;
                        break;
                    } else if (Collation.isSpecialCE32(iArr[i4])) {
                        z = true;
                        break;
                    } else {
                        i4++;
                    }
                }
                int makeCE32FromTagAndIndex = Collation.makeCE32FromTagAndIndex(12, 0);
                int i5 = 44032;
                int i6 = 0;
                while (i6 < 19) {
                    int i7 = (z || Collation.isSpecialCE32(iArr[i6])) ? makeCE32FromTagAndIndex : makeCE32FromTagAndIndex | 256;
                    int i8 = i5 + Normalizer2Impl.Hangul.JAMO_VT_COUNT;
                    this.trie.setRange(i5, i8 - 1, i7, true);
                    i6++;
                    i5 = i8;
                }
            } else {
                while (i2 < 55204) {
                    int ce32 = this.base.getCE32(i2);
                    int i9 = i2 + Normalizer2Impl.Hangul.JAMO_VT_COUNT;
                    this.trie.setRange(i2, i9 - 1, ce32, true);
                    i2 = i9;
                }
            }
            setDigitTags();
            setLeadSurrogates();
            this.ce32s.setElementAt(this.trie.get(0), 0);
            this.trie.set(0, Collation.makeCE32FromTagAndIndex(11, 0));
            collationData.trie = this.trie.toTrie2_32();
            int i10 = 65536;
            char c = 55296;
            while (c < 56320) {
                if (this.unsafeBackwardSet.containsSome(i10, i10 + UCharacterProperty.MAX_SCRIPT)) {
                    this.unsafeBackwardSet.add(c);
                }
                c = (char) (c + 1);
                i10 += 1024;
            }
            this.unsafeBackwardSet.freeze();
            collationData.ce32s = this.ce32s.getBuffer();
            collationData.ces = this.ce64s.getBuffer();
            collationData.contexts = this.contexts.toString();
            CollationData collationData2 = this.base;
            collationData.base = collationData2;
            if (i >= 0) {
                collationData.jamoCE32s = iArr;
            } else {
                collationData.jamoCE32s = collationData2.jamoCE32s;
            }
            collationData.unsafeBackwardSet = this.unsafeBackwardSet;
            return;
        }
        throw new IllegalStateException("attempt to build() after build()");
    }

    /* access modifiers changed from: protected */
    public void clearContexts() {
        this.contexts.setLength(0);
        UnicodeSetIterator unicodeSetIterator = new UnicodeSetIterator(this.contextChars);
        while (unicodeSetIterator.next()) {
            getConditionalCE32ForCE32(this.trie.get(unicodeSetIterator.codepoint)).builtCE32 = 1;
        }
    }

    /* access modifiers changed from: protected */
    public void buildContexts() {
        this.contexts.setLength(0);
        UnicodeSetIterator unicodeSetIterator = new UnicodeSetIterator(this.contextChars);
        while (unicodeSetIterator.next()) {
            int i = unicodeSetIterator.codepoint;
            int i2 = this.trie.get(i);
            if (isBuilderContextCE32(i2)) {
                this.trie.set(i, buildContext(getConditionalCE32ForCE32(i2)));
            } else {
                throw new AssertionError("Impossible: No context data for c in contextChars.");
            }
        }
    }

    /* access modifiers changed from: protected */
    public int buildContext(ConditionalCE32 conditionalCE32) {
        int i;
        int i2;
        ConditionalCE32 conditionalCE322;
        int i3;
        CharsTrieBuilder charsTrieBuilder;
        CharsTrieBuilder charsTrieBuilder2 = new CharsTrieBuilder();
        CharsTrieBuilder charsTrieBuilder3 = new CharsTrieBuilder();
        ConditionalCE32 conditionalCE323 = conditionalCE32;
        while (true) {
            int prefixLength = conditionalCE323.prefixLength();
            StringBuilder sb = new StringBuilder();
            int i4 = prefixLength + 1;
            int i5 = 0;
            sb.append((CharSequence) conditionalCE323.context, 0, i4);
            String sb2 = sb.toString();
            ConditionalCE32 conditionalCE324 = conditionalCE323;
            while (conditionalCE324.next >= 0) {
                ConditionalCE32 conditionalCE325 = getConditionalCE32(conditionalCE324.next);
                if (!conditionalCE325.context.startsWith(sb2)) {
                    break;
                }
                conditionalCE324 = conditionalCE325;
            }
            if (conditionalCE324.context.length() == i4) {
                i = conditionalCE324.ce32;
            } else {
                charsTrieBuilder3.clear();
                if (conditionalCE323.context.length() == i4) {
                    int i6 = conditionalCE323.ce32;
                    conditionalCE322 = getConditionalCE32(conditionalCE323.next);
                    i2 = i6;
                    i3 = 0;
                } else {
                    ConditionalCE32 conditionalCE326 = conditionalCE32;
                    int i7 = 1;
                    while (true) {
                        int prefixLength2 = conditionalCE326.prefixLength();
                        if (prefixLength2 == prefixLength) {
                            break;
                        }
                        if (conditionalCE326.defaultCE32 != 1) {
                            if (prefixLength2 != 0) {
                                charsTrieBuilder = charsTrieBuilder2;
                                if (!sb2.regionMatches(sb.length() - prefixLength2, conditionalCE326.context, 1, prefixLength2)) {
                                }
                            } else {
                                charsTrieBuilder = charsTrieBuilder2;
                            }
                            i7 = conditionalCE326.defaultCE32;
                        } else {
                            charsTrieBuilder = charsTrieBuilder2;
                        }
                        conditionalCE326 = getConditionalCE32(conditionalCE326.next);
                        charsTrieBuilder2 = charsTrieBuilder;
                        i5 = 0;
                    }
                    conditionalCE322 = conditionalCE323;
                    i2 = i7;
                    i3 = 256;
                }
                int i8 = i3 | 512;
                while (true) {
                    String substring = conditionalCE322.context.substring(i4);
                    if (this.nfcImpl.getFCD16(substring.codePointAt(i5)) <= 255) {
                        i8 &= -513;
                    }
                    if (this.nfcImpl.getFCD16(substring.codePointBefore(substring.length())) > 255) {
                        i8 |= 1024;
                    }
                    charsTrieBuilder3.add(substring, conditionalCE322.ce32);
                    if (conditionalCE322 == conditionalCE324) {
                        break;
                    }
                    i5 = 0;
                    conditionalCE322 = getConditionalCE32(conditionalCE322.next);
                }
                int addContextTrie = addContextTrie(i2, charsTrieBuilder3);
                if (addContextTrie <= 524287) {
                    i = i8 | Collation.makeCE32FromTagAndIndex(9, addContextTrie);
                    conditionalCE324 = conditionalCE322;
                } else {
                    throw new IndexOutOfBoundsException("too many context-sensitive mappings");
                }
            }
            conditionalCE323.defaultCE32 = i;
            if (prefixLength != 0) {
                sb.delete(0, 1);
                sb.reverse();
                charsTrieBuilder2.add(sb, i);
                if (conditionalCE324.next < 0) {
                    int addContextTrie2 = addContextTrie(conditionalCE32.defaultCE32, charsTrieBuilder2);
                    if (addContextTrie2 <= 524287) {
                        return Collation.makeCE32FromTagAndIndex(8, addContextTrie2);
                    }
                    throw new IndexOutOfBoundsException("too many context-sensitive mappings");
                }
            } else if (conditionalCE324.next < 0) {
                return i;
            }
            conditionalCE323 = getConditionalCE32(conditionalCE324.next);
        }
    }

    /* access modifiers changed from: protected */
    public int addContextTrie(int i, CharsTrieBuilder charsTrieBuilder) {
        StringBuilder sb = new StringBuilder();
        sb.append((char) (i >> 16));
        sb.append((char) i);
        sb.append(charsTrieBuilder.buildCharSequence(StringTrieBuilder.Option.SMALL));
        int indexOf = this.contexts.indexOf(sb.toString());
        if (indexOf >= 0) {
            return indexOf;
        }
        int length = this.contexts.length();
        this.contexts.append((CharSequence) sb);
        return length;
    }

    /* access modifiers changed from: protected */
    public void buildFastLatinTable(CollationData collationData) {
        if (this.fastLatinEnabled) {
            this.fastLatinBuilder = new CollationFastLatinBuilder();
            if (this.fastLatinBuilder.forData(collationData)) {
                char[] header = this.fastLatinBuilder.getHeader();
                char[] table = this.fastLatinBuilder.getTable();
                CollationData collationData2 = this.base;
                if (collationData2 != null && Arrays.equals(header, collationData2.fastLatinTableHeader) && Arrays.equals(table, this.base.fastLatinTable)) {
                    this.fastLatinBuilder = null;
                    header = this.base.fastLatinTableHeader;
                    table = this.base.fastLatinTable;
                }
                collationData.fastLatinTableHeader = header;
                collationData.fastLatinTable = table;
                return;
            }
            this.fastLatinBuilder = null;
        }
    }

    /* access modifiers changed from: protected */
    public int getCEs(CharSequence charSequence, int i, long[] jArr, int i2) {
        if (this.collIter == null) {
            this.collIter = new DataBuilderCollationIterator(this, new CollationData(this.nfcImpl));
            if (this.collIter == null) {
                return 0;
            }
        }
        return this.collIter.fetchCEs(charSequence, i, jArr, i2);
    }

    /* access modifiers changed from: private */
    public static final class DataBuilderCollationIterator extends CollationIterator {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        protected final CollationDataBuilder builder;
        protected final CollationData builderData;
        protected final int[] jamoCE32s = new int[67];
        protected int pos;
        protected CharSequence s;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        DataBuilderCollationIterator(CollationDataBuilder collationDataBuilder, CollationData collationData) {
            super(collationData, false);
            this.builder = collationDataBuilder;
            this.builderData = collationData;
            this.builderData.base = this.builder.base;
            for (int i = 0; i < 67; i++) {
                this.jamoCE32s[i] = Collation.makeCE32FromTagAndIndex(7, CollationDataBuilder.jamoCpFromIndex(i)) | 256;
            }
            this.builderData.jamoCE32s = this.jamoCE32s;
        }

        /* access modifiers changed from: package-private */
        public int fetchCEs(CharSequence charSequence, int i, long[] jArr, int i2) {
            CollationData collationData;
            int i3;
            this.builderData.ce32s = this.builder.ce32s.getBuffer();
            this.builderData.ces = this.builder.ce64s.getBuffer();
            this.builderData.contexts = this.builder.contexts.toString();
            reset();
            this.s = charSequence;
            this.pos = i;
            while (this.pos < this.s.length()) {
                clearCEs();
                int codePointAt = Character.codePointAt(this.s, this.pos);
                this.pos += Character.charCount(codePointAt);
                int i4 = this.builder.trie.get(codePointAt);
                if (i4 == 192) {
                    collationData = this.builder.base;
                    i3 = this.builder.base.getCE32(codePointAt);
                } else {
                    i3 = i4;
                    collationData = this.builderData;
                }
                appendCEsFromCE32(collationData, codePointAt, i3, true);
                for (int i5 = 0; i5 < getCEsLength(); i5++) {
                    long ce = getCE(i5);
                    if (ce != 0) {
                        if (i2 < 31) {
                            jArr[i2] = ce;
                        }
                        i2++;
                    }
                }
            }
            return i2;
        }

        @Override // ohos.global.icu.impl.coll.CollationIterator
        public void resetToOffset(int i) {
            reset();
            this.pos = i;
        }

        @Override // ohos.global.icu.impl.coll.CollationIterator
        public int getOffset() {
            return this.pos;
        }

        @Override // ohos.global.icu.impl.coll.CollationIterator
        public int nextCodePoint() {
            if (this.pos == this.s.length()) {
                return -1;
            }
            int codePointAt = Character.codePointAt(this.s, this.pos);
            this.pos += Character.charCount(codePointAt);
            return codePointAt;
        }

        @Override // ohos.global.icu.impl.coll.CollationIterator
        public int previousCodePoint() {
            int i = this.pos;
            if (i == 0) {
                return -1;
            }
            int codePointBefore = Character.codePointBefore(this.s, i);
            this.pos -= Character.charCount(codePointBefore);
            return codePointBefore;
        }

        /* access modifiers changed from: protected */
        @Override // ohos.global.icu.impl.coll.CollationIterator
        public void forwardNumCodePoints(int i) {
            this.pos = Character.offsetByCodePoints(this.s, this.pos, i);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.global.icu.impl.coll.CollationIterator
        public void backwardNumCodePoints(int i) {
            this.pos = Character.offsetByCodePoints(this.s, this.pos, -i);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.global.icu.impl.coll.CollationIterator
        public int getDataCE32(int i) {
            return this.builder.trie.get(i);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.global.icu.impl.coll.CollationIterator
        public int getCE32FromBuilderData(int i) {
            if ((i & 256) != 0) {
                return this.builder.trie.get(Collation.indexFromCE32(i));
            }
            ConditionalCE32 conditionalCE32ForCE32 = this.builder.getConditionalCE32ForCE32(i);
            if (conditionalCE32ForCE32.builtCE32 == 1) {
                try {
                    conditionalCE32ForCE32.builtCE32 = this.builder.buildContext(conditionalCE32ForCE32);
                } catch (IndexOutOfBoundsException unused) {
                    this.builder.clearContexts();
                    conditionalCE32ForCE32.builtCE32 = this.builder.buildContext(conditionalCE32ForCE32);
                }
                this.builderData.contexts = this.builder.contexts.toString();
            }
            return conditionalCE32ForCE32.builtCE32;
        }
    }

    /* access modifiers changed from: protected */
    public final boolean isMutable() {
        UnicodeSet unicodeSet;
        return (this.trie == null || (unicodeSet = this.unsafeBackwardSet) == null || unicodeSet.isFrozen()) ? false : true;
    }
}
