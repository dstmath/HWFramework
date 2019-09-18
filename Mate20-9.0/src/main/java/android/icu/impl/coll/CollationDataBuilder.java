package android.icu.impl.coll;

import android.icu.impl.Norm2AllModes;
import android.icu.impl.Normalizer2Impl;
import android.icu.impl.Trie2;
import android.icu.impl.Trie2Writable;
import android.icu.lang.UCharacter;
import android.icu.text.UnicodeSet;
import android.icu.text.UnicodeSetIterator;
import android.icu.util.CharsTrie;
import android.icu.util.CharsTrieBuilder;
import android.icu.util.StringTrieBuilder;
import dalvik.bytecode.Opcodes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

final class CollationDataBuilder {
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

    interface CEModifier {
        long modifyCE(long j);

        long modifyCE32(int i);
    }

    private static final class ConditionalCE32 {
        int builtCE32 = 1;
        int ce32;
        String context;
        int defaultCE32 = 1;
        int next = -1;

        ConditionalCE32(String ct, int ce) {
            this.context = ct;
            this.ce32 = ce;
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

    private static final class CopyHelper {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        CollationDataBuilder dest;
        long[] modifiedCEs = new long[31];
        CEModifier modifier;
        CollationDataBuilder src;

        static {
            Class<CollationDataBuilder> cls = CollationDataBuilder.class;
        }

        CopyHelper(CollationDataBuilder s, CollationDataBuilder d, CEModifier m) {
            this.src = s;
            this.dest = d;
            this.modifier = m;
        }

        /* access modifiers changed from: package-private */
        public void copyRangeCE32(int start, int end, int ce32) {
            int ce322 = copyCE32(ce32);
            this.dest.trie.setRange(start, end, ce322, true);
            if (CollationDataBuilder.isBuilderContextCE32(ce322)) {
                this.dest.contextChars.add(start, end);
            }
        }

        /* access modifiers changed from: package-private */
        public int copyCE32(int ce32) {
            int ce322 = ce32;
            boolean isSpecialCE32 = Collation.isSpecialCE32(ce32);
            long j = Collation.NO_CE;
            if (!isSpecialCE32) {
                long ce = this.modifier.modifyCE32(ce322);
                if (ce != Collation.NO_CE) {
                    return this.dest.encodeOneCE(ce);
                }
                return ce322;
            }
            int tag = Collation.tagFromCE32(ce32);
            if (tag == 5) {
                int[] srcCE32s = this.src.ce32s.getBuffer();
                int srcIndex = Collation.indexFromCE32(ce32);
                int length = Collation.lengthFromCE32(ce32);
                boolean isModified = false;
                int i = ce322;
                for (int i2 = 0; i2 < length; i2++) {
                    int ce323 = srcCE32s[srcIndex + i2];
                    if (!Collation.isSpecialCE32(ce323)) {
                        long modifyCE32 = this.modifier.modifyCE32(ce323);
                        long ce2 = modifyCE32;
                        if (modifyCE32 != Collation.NO_CE) {
                            if (!isModified) {
                                for (int j2 = 0; j2 < i2; j2++) {
                                    this.modifiedCEs[j2] = Collation.ceFromCE32(srcCE32s[srcIndex + j2]);
                                }
                                isModified = true;
                            }
                            this.modifiedCEs[i2] = ce2;
                        }
                    }
                    if (isModified) {
                        this.modifiedCEs[i2] = Collation.ceFromCE32(ce323);
                    }
                }
                if (isModified) {
                    return this.dest.encodeCEs(this.modifiedCEs, length);
                }
                return this.dest.encodeExpansion32(srcCE32s, srcIndex, length);
            } else if (tag == 6) {
                long[] srcCEs = this.src.ce64s.getBuffer();
                int srcIndex2 = Collation.indexFromCE32(ce32);
                int length2 = Collation.lengthFromCE32(ce32);
                boolean isModified2 = false;
                int i3 = 0;
                while (i3 < length2) {
                    long srcCE = srcCEs[srcIndex2 + i3];
                    long ce3 = this.modifier.modifyCE(srcCE);
                    if (ce3 != j) {
                        if (!isModified2) {
                            for (int j3 = 0; j3 < i3; j3++) {
                                this.modifiedCEs[j3] = srcCEs[srcIndex2 + j3];
                            }
                            isModified2 = true;
                        }
                        this.modifiedCEs[i3] = ce3;
                    } else if (isModified2) {
                        this.modifiedCEs[i3] = srcCE;
                    }
                    i3++;
                    j = Collation.NO_CE;
                }
                if (isModified2) {
                    return this.dest.encodeCEs(this.modifiedCEs, length2);
                }
                return this.dest.encodeExpansion(srcCEs, srcIndex2, length2);
            } else if (tag != 7) {
                return ce322;
            } else {
                ConditionalCE32 cond = this.src.getConditionalCE32ForCE32(ce322);
                int destIndex = this.dest.addConditionalCE32(cond.context, copyCE32(cond.ce32));
                int ce324 = CollationDataBuilder.makeBuilderContextCE32(destIndex);
                while (cond.next >= 0) {
                    cond = this.src.getConditionalCE32(cond.next);
                    ConditionalCE32 prevDestCond = this.dest.getConditionalCE32(destIndex);
                    destIndex = this.dest.addConditionalCE32(cond.context, copyCE32(cond.ce32));
                    this.dest.unsafeBackwardSet.addAll((CharSequence) cond.context.substring(cond.prefixLength() + 1));
                    prevDestCond.next = destIndex;
                }
                return ce324;
            }
        }
    }

    private static final class DataBuilderCollationIterator extends CollationIterator {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        protected final CollationDataBuilder builder;
        protected final CollationData builderData;
        protected final int[] jamoCE32s = new int[67];
        protected int pos;
        protected CharSequence s;

        static {
            Class<CollationDataBuilder> cls = CollationDataBuilder.class;
        }

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        DataBuilderCollationIterator(CollationDataBuilder b, CollationData newData) {
            super(newData, false);
            this.builder = b;
            this.builderData = newData;
            this.builderData.base = this.builder.base;
            for (int j = 0; j < 67; j++) {
                this.jamoCE32s[j] = Collation.makeCE32FromTagAndIndex(7, CollationDataBuilder.jamoCpFromIndex(j)) | 256;
            }
            this.builderData.jamoCE32s = this.jamoCE32s;
        }

        /* access modifiers changed from: package-private */
        public int fetchCEs(CharSequence str, int start, long[] ces, int cesLength) {
            CollationData d;
            this.builderData.ce32s = this.builder.ce32s.getBuffer();
            this.builderData.ces = this.builder.ce64s.getBuffer();
            this.builderData.contexts = this.builder.contexts.toString();
            reset();
            this.s = str;
            this.pos = start;
            while (this.pos < this.s.length()) {
                clearCEs();
                int c = Character.codePointAt(this.s, this.pos);
                this.pos += Character.charCount(c);
                int ce32 = this.builder.trie.get(c);
                if (ce32 == 192) {
                    d = this.builder.base;
                    ce32 = this.builder.base.getCE32(c);
                } else {
                    d = this.builderData;
                }
                appendCEsFromCE32(d, c, ce32, true);
                for (int i = 0; i < getCEsLength(); i++) {
                    long ce = getCE(i);
                    if (ce != 0) {
                        if (cesLength < 31) {
                            ces[cesLength] = ce;
                        }
                        cesLength++;
                    }
                }
            }
            return cesLength;
        }

        public void resetToOffset(int newOffset) {
            reset();
            this.pos = newOffset;
        }

        public int getOffset() {
            return this.pos;
        }

        public int nextCodePoint() {
            if (this.pos == this.s.length()) {
                return -1;
            }
            int c = Character.codePointAt(this.s, this.pos);
            this.pos += Character.charCount(c);
            return c;
        }

        public int previousCodePoint() {
            if (this.pos == 0) {
                return -1;
            }
            int c = Character.codePointBefore(this.s, this.pos);
            this.pos -= Character.charCount(c);
            return c;
        }

        /* access modifiers changed from: protected */
        public void forwardNumCodePoints(int num) {
            this.pos = Character.offsetByCodePoints(this.s, this.pos, num);
        }

        /* access modifiers changed from: protected */
        public void backwardNumCodePoints(int num) {
            this.pos = Character.offsetByCodePoints(this.s, this.pos, -num);
        }

        /* access modifiers changed from: protected */
        public int getDataCE32(int c) {
            return this.builder.trie.get(c);
        }

        /* access modifiers changed from: protected */
        public int getCE32FromBuilderData(int ce32) {
            if ((ce32 & 256) != 0) {
                return this.builder.trie.get(Collation.indexFromCE32(ce32));
            }
            ConditionalCE32 cond = this.builder.getConditionalCE32ForCE32(ce32);
            if (cond.builtCE32 == 1) {
                try {
                    cond.builtCE32 = this.builder.buildContext(cond);
                } catch (IndexOutOfBoundsException e) {
                    this.builder.clearContexts();
                    cond.builtCE32 = this.builder.buildContext(cond);
                }
                this.builderData.contexts = this.builder.contexts.toString();
            }
            return cond.builtCE32;
        }
    }

    CollationDataBuilder() {
        this.ce32s.addElement(0);
    }

    /* access modifiers changed from: package-private */
    public void initForTailoring(CollationData b) {
        if (this.trie != null) {
            throw new IllegalStateException("attempt to reuse a CollationDataBuilder");
        } else if (b != null) {
            this.base = b;
            this.trie = new Trie2Writable(192, -195323);
            for (int c = 192; c <= 255; c++) {
                this.trie.set(c, 192);
            }
            this.trie.setRange(Normalizer2Impl.Hangul.HANGUL_BASE, Normalizer2Impl.Hangul.HANGUL_END, Collation.makeCE32FromTagAndIndex(12, 0), true);
            this.unsafeBackwardSet.addAll(b.unsafeBackwardSet);
        } else {
            throw new IllegalArgumentException("null CollationData");
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isCompressibleLeadByte(int b) {
        return this.base.isCompressibleLeadByte(b);
    }

    /* access modifiers changed from: package-private */
    public boolean isCompressiblePrimary(long p) {
        return isCompressibleLeadByte(((int) p) >>> 24);
    }

    /* access modifiers changed from: package-private */
    public boolean hasMappings() {
        return this.modified;
    }

    /* access modifiers changed from: package-private */
    public boolean isAssigned(int c) {
        return Collation.isAssignedCE32(this.trie.get(c));
    }

    /* access modifiers changed from: package-private */
    public void add(CharSequence prefix, CharSequence s, long[] ces, int cesLength) {
        addCE32(prefix, s, encodeCEs(ces, cesLength));
    }

    /* access modifiers changed from: package-private */
    public int encodeCEs(long[] ces, int cesLength) {
        long[] jArr = ces;
        int i = cesLength;
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
                long ce0 = jArr[0];
                long ce1 = jArr[1];
                long p0 = ce0 >>> 32;
                if ((72057594037862655L & ce0) == 83886080 && (-4278190081L & ce1) == 1280 && p0 != 0) {
                    return ((int) p0) | ((((int) ce0) & 65280) << 8) | ((((int) ce1) >> 16) & 65280) | 192 | 4;
                }
            }
            int[] newCE32s = new int[31];
            for (int i2 = 0; i2 != i; i2++) {
                int ce32 = encodeOneCEAsCE32(jArr[i2]);
                if (ce32 == 1) {
                    return encodeExpansion(jArr, 0, i);
                }
                newCE32s[i2] = ce32;
            }
            return encodeExpansion32(newCE32s, 0, i);
        }
    }

    /* access modifiers changed from: package-private */
    public void addCE32(CharSequence prefix, CharSequence s, int ce32) {
        ConditionalCE32 cond;
        CharSequence charSequence = s;
        int i = ce32;
        if (s.length() == 0) {
            CharSequence charSequence2 = prefix;
            throw new IllegalArgumentException("mapping from empty string");
        } else if (isMutable()) {
            boolean hasContext = false;
            int c = Character.codePointAt(charSequence, 0);
            int cLength = Character.charCount(c);
            int oldCE32 = this.trie.get(c);
            if (prefix.length() != 0 || s.length() > cLength) {
                hasContext = true;
            }
            if (oldCE32 == 192) {
                int baseCE32 = this.base.getFinalCE32(this.base.getCE32(c));
                if (hasContext || Collation.ce32HasContext(baseCE32)) {
                    oldCE32 = copyFromBaseCE32(c, baseCE32, true);
                    this.trie.set(c, oldCE32);
                }
            }
            if (!hasContext) {
                if (!isBuilderContextCE32(oldCE32)) {
                    this.trie.set(c, i);
                } else {
                    ConditionalCE32 cond2 = getConditionalCE32ForCE32(oldCE32);
                    cond2.builtCE32 = 1;
                    cond2.ce32 = i;
                }
                CharSequence charSequence3 = prefix;
            } else {
                if (!isBuilderContextCE32(oldCE32)) {
                    int index = addConditionalCE32("\u0000", oldCE32);
                    this.trie.set(c, makeBuilderContextCE32(index));
                    this.contextChars.add(c);
                    cond = getConditionalCE32(index);
                } else {
                    cond = getConditionalCE32ForCE32(oldCE32);
                    cond.builtCE32 = 1;
                }
                String context = ((char) prefix.length()) + prefix + suffix;
                this.unsafeBackwardSet.addAll(charSequence.subSequence(cLength, s.length()));
                while (true) {
                    int next = cond.next;
                    if (next < 0) {
                        cond.next = addConditionalCE32(context, i);
                        break;
                    }
                    ConditionalCE32 nextCond = getConditionalCE32(next);
                    int cmp = context.compareTo(nextCond.context);
                    if (cmp < 0) {
                        int index2 = addConditionalCE32(context, i);
                        cond.next = index2;
                        getConditionalCE32(index2).next = next;
                        break;
                    } else if (cmp == 0) {
                        nextCond.ce32 = i;
                        break;
                    } else {
                        cond = nextCond;
                    }
                }
            }
            this.modified = true;
        } else {
            CharSequence charSequence4 = prefix;
            throw new IllegalStateException("attempt to add mappings after build()");
        }
    }

    /* access modifiers changed from: package-private */
    public void copyFrom(CollationDataBuilder src, CEModifier modifier) {
        if (isMutable()) {
            CopyHelper helper = new CopyHelper(src, this, modifier);
            Iterator<Trie2.Range> trieIterator = src.trie.iterator();
            while (trieIterator.hasNext()) {
                Trie2.Range next = trieIterator.next();
                Trie2.Range range = next;
                if (next.leadSurrogate) {
                    break;
                }
                enumRangeForCopy(range.startCodePoint, range.endCodePoint, range.value, helper);
            }
            this.modified |= src.modified;
            return;
        }
        throw new IllegalStateException("attempt to copyFrom() after build()");
    }

    /* access modifiers changed from: package-private */
    public void optimize(UnicodeSet set) {
        if (!set.isEmpty()) {
            UnicodeSetIterator iter = new UnicodeSetIterator(set);
            while (iter.next() && iter.codepoint != UnicodeSetIterator.IS_STRING) {
                int c = iter.codepoint;
                if (this.trie.get(c) == 192) {
                    this.trie.set(c, copyFromBaseCE32(c, this.base.getFinalCE32(this.base.getCE32(c)), true));
                }
            }
            this.modified = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void suppressContractions(UnicodeSet set) {
        if (!set.isEmpty()) {
            UnicodeSetIterator iter = new UnicodeSetIterator(set);
            while (iter.next() && iter.codepoint != UnicodeSetIterator.IS_STRING) {
                int c = iter.codepoint;
                int ce32 = this.trie.get(c);
                if (ce32 == 192) {
                    int ce322 = this.base.getFinalCE32(this.base.getCE32(c));
                    if (Collation.ce32HasContext(ce322)) {
                        this.trie.set(c, copyFromBaseCE32(c, ce322, false));
                    }
                } else if (isBuilderContextCE32(ce32)) {
                    this.trie.set(c, getConditionalCE32ForCE32(ce32).ce32);
                    this.contextChars.remove(c);
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
    public void build(CollationData data) {
        buildMappings(data);
        if (this.base != null) {
            data.numericPrimary = this.base.numericPrimary;
            data.compressibleBytes = this.base.compressibleBytes;
            data.numScripts = this.base.numScripts;
            data.scriptsIndex = this.base.scriptsIndex;
            data.scriptStarts = this.base.scriptStarts;
        }
        buildFastLatinTable(data);
    }

    /* access modifiers changed from: package-private */
    public int getCEs(CharSequence s, long[] ces, int cesLength) {
        return getCEs(s, 0, ces, cesLength);
    }

    /* access modifiers changed from: package-private */
    public int getCEs(CharSequence prefix, CharSequence s, long[] ces, int cesLength) {
        int prefixLength = prefix.length();
        if (prefixLength == 0) {
            return getCEs(s, 0, ces, cesLength);
        }
        StringBuilder sb = new StringBuilder(prefix);
        sb.append(s);
        return getCEs((CharSequence) sb, prefixLength, ces, cesLength);
    }

    /* access modifiers changed from: protected */
    public int getCE32FromOffsetCE32(boolean fromBase, int c, int ce32) {
        int i = Collation.indexFromCE32(ce32);
        return Collation.makeLongPrimaryCE32(Collation.getThreeBytePrimaryForOffsetData(c, fromBase ? this.base.ces[i] : this.ce64s.elementAti(i)));
    }

    /* access modifiers changed from: protected */
    public int addCE(long ce) {
        int length = this.ce64s.size();
        for (int i = 0; i < length; i++) {
            if (ce == this.ce64s.elementAti(i)) {
                return i;
            }
        }
        this.ce64s.addElement(ce);
        return length;
    }

    /* access modifiers changed from: protected */
    public int addCE32(int ce32) {
        int length = this.ce32s.size();
        for (int i = 0; i < length; i++) {
            if (ce32 == this.ce32s.elementAti(i)) {
                return i;
            }
        }
        this.ce32s.addElement(ce32);
        return length;
    }

    /* access modifiers changed from: protected */
    public int addConditionalCE32(String context, int ce32) {
        int index = this.conditionalCE32s.size();
        if (index <= 524287) {
            this.conditionalCE32s.add(new ConditionalCE32(context, ce32));
            return index;
        }
        throw new IndexOutOfBoundsException("too many context-sensitive mappings");
    }

    /* access modifiers changed from: protected */
    public ConditionalCE32 getConditionalCE32(int index) {
        return this.conditionalCE32s.get(index);
    }

    /* access modifiers changed from: protected */
    public ConditionalCE32 getConditionalCE32ForCE32(int ce32) {
        return getConditionalCE32(Collation.indexFromCE32(ce32));
    }

    protected static int makeBuilderContextCE32(int index) {
        return Collation.makeCE32FromTagAndIndex(7, index);
    }

    protected static boolean isBuilderContextCE32(int ce32) {
        return Collation.hasCE32Tag(ce32, 7);
    }

    protected static int encodeOneCEAsCE32(long ce) {
        long p = ce >>> 32;
        int lower32 = (int) ce;
        int t = 65535 & lower32;
        if ((281470698455295L & ce) == 0) {
            return ((int) p) | (lower32 >>> 16) | (t >> 8);
        }
        if ((1099511627775L & ce) == 83887360) {
            return Collation.makeLongPrimaryCE32(p);
        }
        if (p == 0 && (t & 255) == 0) {
            return Collation.makeLongSecondaryCE32(lower32);
        }
        return 1;
    }

    /* access modifiers changed from: protected */
    public int encodeOneCE(long ce) {
        int ce32 = encodeOneCEAsCE32(ce);
        if (ce32 != 1) {
            return ce32;
        }
        int index = addCE(ce);
        if (index <= 524287) {
            return Collation.makeCE32FromTagIndexAndLength(6, index, 1);
        }
        throw new IndexOutOfBoundsException("too many mappings");
    }

    /* access modifiers changed from: protected */
    public int encodeExpansion(long[] ces, int start, int length) {
        long first = ces[start];
        int ce64sMax = this.ce64s.size() - length;
        for (int i = 0; i <= ce64sMax; i++) {
            if (first == this.ce64s.elementAti(i)) {
                if (i <= 524287) {
                    int j = 1;
                    while (j != length) {
                        if (this.ce64s.elementAti(i + j) == ces[start + j]) {
                            j++;
                        }
                    }
                    return Collation.makeCE32FromTagIndexAndLength(6, i, length);
                }
                throw new IndexOutOfBoundsException("too many mappings");
            }
        }
        int i2 = this.ce64s.size();
        if (i2 <= 524287) {
            for (int j2 = 0; j2 < length; j2++) {
                this.ce64s.addElement(ces[start + j2]);
            }
            return Collation.makeCE32FromTagIndexAndLength(6, i2, length);
        }
        throw new IndexOutOfBoundsException("too many mappings");
    }

    /* access modifiers changed from: protected */
    public int encodeExpansion32(int[] newCE32s, int start, int length) {
        int first = newCE32s[start];
        int ce32sMax = this.ce32s.size() - length;
        for (int i = 0; i <= ce32sMax; i++) {
            if (first == this.ce32s.elementAti(i)) {
                if (i <= 524287) {
                    int j = 1;
                    while (j != length) {
                        if (this.ce32s.elementAti(i + j) == newCE32s[start + j]) {
                            j++;
                        }
                    }
                    return Collation.makeCE32FromTagIndexAndLength(5, i, length);
                }
                throw new IndexOutOfBoundsException("too many mappings");
            }
        }
        int i2 = this.ce32s.size();
        if (i2 <= 524287) {
            for (int j2 = 0; j2 < length; j2++) {
                this.ce32s.addElement(newCE32s[start + j2]);
            }
            return Collation.makeCE32FromTagIndexAndLength(5, i2, length);
        }
        throw new IndexOutOfBoundsException("too many mappings");
    }

    /* access modifiers changed from: protected */
    public int copyFromBaseCE32(int c, int ce32, boolean withContext) {
        int index;
        int index2;
        if (!Collation.isSpecialCE32(ce32)) {
            return ce32;
        }
        switch (Collation.tagFromCE32(ce32)) {
            case 1:
            case 2:
            case 4:
                break;
            case 5:
                ce32 = encodeExpansion32(this.base.ce32s, Collation.indexFromCE32(ce32), Collation.lengthFromCE32(ce32));
                break;
            case 6:
                ce32 = encodeExpansion(this.base.ces, Collation.indexFromCE32(ce32), Collation.lengthFromCE32(ce32));
                break;
            case 8:
                int trieIndex = Collation.indexFromCE32(ce32);
                int ce322 = this.base.getCE32FromContexts(trieIndex);
                if (withContext) {
                    ConditionalCE32 head = new ConditionalCE32("", 0);
                    StringBuilder context = new StringBuilder("\u0000");
                    if (Collation.isContractionCE32(ce322)) {
                        index = copyContractionsFromBaseCE32(context, c, ce322, head);
                    } else {
                        index = addConditionalCE32(context.toString(), copyFromBaseCE32(c, ce322, true));
                        int i = index;
                        head.next = index;
                    }
                    ConditionalCE32 cond = getConditionalCE32(index);
                    CharsTrie.Iterator prefixes = CharsTrie.iterator(this.base.contexts, trieIndex + 2, 0);
                    while (prefixes.hasNext()) {
                        CharsTrie.Entry entry = prefixes.next();
                        context.setLength(0);
                        context.append(entry.chars);
                        context.reverse().insert(0, (char) entry.chars.length());
                        int ce323 = entry.value;
                        if (Collation.isContractionCE32(ce323)) {
                            index2 = copyContractionsFromBaseCE32(context, c, ce323, cond);
                        } else {
                            int addConditionalCE32 = addConditionalCE32(context.toString(), copyFromBaseCE32(c, ce323, true));
                            index2 = addConditionalCE32;
                            cond.next = addConditionalCE32;
                        }
                        cond = getConditionalCE32(index2);
                    }
                    ce32 = makeBuilderContextCE32(head.next);
                    this.contextChars.add(c);
                    break;
                } else {
                    return copyFromBaseCE32(c, ce322, false);
                }
            case 9:
                if (withContext) {
                    ConditionalCE32 head2 = new ConditionalCE32("", 0);
                    copyContractionsFromBaseCE32(new StringBuilder("\u0000"), c, ce32, head2);
                    ce32 = makeBuilderContextCE32(head2.next);
                    this.contextChars.add(c);
                    break;
                } else {
                    return copyFromBaseCE32(c, this.base.getCE32FromContexts(Collation.indexFromCE32(ce32)), false);
                }
            case 12:
                throw new UnsupportedOperationException("We forbid tailoring of Hangul syllables.");
            case 14:
                ce32 = getCE32FromOffsetCE32(true, c, ce32);
                break;
            case 15:
                ce32 = encodeOneCE(Collation.unassignedCEFromCodePoint(c));
                break;
            default:
                throw new AssertionError("copyFromBaseCE32(c, ce32, withContext) requires ce32 == base.getFinalCE32(ce32)");
        }
        return ce32;
    }

    /* access modifiers changed from: protected */
    public int copyContractionsFromBaseCE32(StringBuilder context, int c, int ce32, ConditionalCE32 cond) {
        int index;
        int trieIndex = Collation.indexFromCE32(ce32);
        if ((ce32 & 256) != 0) {
            index = -1;
        } else {
            index = addConditionalCE32(context.toString(), copyFromBaseCE32(c, this.base.getCE32FromContexts(trieIndex), true));
            cond.next = index;
            cond = getConditionalCE32(index);
        }
        int suffixStart = context.length();
        CharsTrie.Iterator suffixes = CharsTrie.iterator(this.base.contexts, trieIndex + 2, 0);
        while (suffixes.hasNext()) {
            CharsTrie.Entry entry = suffixes.next();
            context.append(entry.chars);
            int addConditionalCE32 = addConditionalCE32(context.toString(), copyFromBaseCE32(c, entry.value, true));
            index = addConditionalCE32;
            cond.next = addConditionalCE32;
            cond = getConditionalCE32(index);
            context.setLength(suffixStart);
        }
        return index;
    }

    private static void enumRangeForCopy(int start, int end, int value, CopyHelper helper) {
        if (value != -1 && value != 192) {
            helper.copyRangeCE32(start, end, value);
        }
    }

    /* access modifiers changed from: protected */
    public boolean getJamoCE32s(int[] jamoCE32s) {
        int jamo = 0;
        boolean needToCopyFromBase = false;
        boolean anyJamoAssigned = this.base == null;
        for (int j = 0; j < 67; j++) {
            int jamo2 = jamoCpFromIndex(j);
            boolean fromBase = false;
            int ce32 = this.trie.get(jamo2);
            anyJamoAssigned |= Collation.isAssignedCE32(ce32);
            if (ce32 == 192) {
                fromBase = true;
                ce32 = this.base.getCE32(jamo2);
            }
            if (Collation.isSpecialCE32(ce32)) {
                switch (Collation.tagFromCE32(ce32)) {
                    case 0:
                    case 3:
                    case 7:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                        throw new AssertionError(String.format("unexpected special tag in ce32=0x%08x", new Object[]{Integer.valueOf(ce32)}));
                    case 5:
                    case 6:
                    case 8:
                    case 9:
                        if (!fromBase) {
                            break;
                        } else {
                            ce32 = 192;
                            needToCopyFromBase = true;
                            break;
                        }
                    case 14:
                        ce32 = getCE32FromOffsetCE32(fromBase, jamo2, ce32);
                        break;
                    case 15:
                        ce32 = 192;
                        needToCopyFromBase = true;
                        break;
                }
            }
            jamoCE32s[j] = ce32;
        }
        if (anyJamoAssigned && needToCopyFromBase) {
            while (true) {
                int j2 = jamo;
                if (j2 < 67) {
                    if (jamoCE32s[j2] == 192) {
                        int jamo3 = jamoCpFromIndex(j2);
                        jamoCE32s[j2] = copyFromBaseCE32(jamo3, this.base.getCE32(jamo3), true);
                    }
                    jamo = j2 + 1;
                }
            }
        }
        return anyJamoAssigned;
    }

    /* access modifiers changed from: protected */
    public void setDigitTags() {
        UnicodeSetIterator iter = new UnicodeSetIterator(new UnicodeSet("[:Nd:]"));
        while (iter.next()) {
            int c = iter.codepoint;
            int ce32 = this.trie.get(c);
            if (!(ce32 == 192 || ce32 == -1)) {
                int index = addCE32(ce32);
                if (index <= 524287) {
                    this.trie.set(c, Collation.makeCE32FromTagIndexAndLength(10, index, UCharacter.digit(c)));
                } else {
                    throw new IndexOutOfBoundsException("too many mappings");
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setLeadSurrogates() {
        int value;
        for (char lead = 55296; lead < 56320; lead = (char) (lead + 1)) {
            int leadValue = -1;
            Iterator<Trie2.Range> trieIterator = this.trie.iteratorForLeadSurrogate(lead);
            while (true) {
                if (!trieIterator.hasNext()) {
                    break;
                }
                int value2 = trieIterator.next().value;
                if (value2 != -1) {
                    if (value2 != 192) {
                        leadValue = 512;
                        break;
                    }
                    value = 256;
                } else {
                    value = 0;
                }
                if (leadValue < 0) {
                    leadValue = value;
                } else if (leadValue != value) {
                    leadValue = 512;
                    break;
                }
            }
            this.trie.setForLeadSurrogateCodeUnit(lead, Collation.makeCE32FromTagAndIndex(13, 0) | leadValue);
        }
    }

    /* access modifiers changed from: protected */
    public void buildMappings(CollationData data) {
        int limit;
        int limit2;
        if (isMutable()) {
            buildContexts();
            int[] jamoCE32s = new int[67];
            int jamoIndex = -1;
            if (getJamoCE32s(jamoCE32s)) {
                jamoIndex = this.ce32s.size();
                for (int i = 0; i < 67; i++) {
                    this.ce32s.addElement(jamoCE32s[i]);
                }
                boolean isAnyJamoVTSpecial = false;
                int i2 = 19;
                while (true) {
                    if (i2 >= 67) {
                        break;
                    } else if (Collation.isSpecialCE32(jamoCE32s[i2])) {
                        isAnyJamoVTSpecial = true;
                        break;
                    } else {
                        i2++;
                    }
                }
                int hangulCE32 = Collation.makeCE32FromTagAndIndex(12, 0);
                int c = 44032;
                for (int i3 = 0; i3 < 19; i3++) {
                    int ce32 = hangulCE32;
                    if (!isAnyJamoVTSpecial && !Collation.isSpecialCE32(jamoCE32s[i3])) {
                        ce32 |= 256;
                    }
                    this.trie.setRange(c, (c + Normalizer2Impl.Hangul.JAMO_VT_COUNT) - 1, ce32, true);
                    c = limit2;
                }
            } else {
                int c2 = Normalizer2Impl.Hangul.HANGUL_BASE;
                while (c2 < 55204) {
                    this.trie.setRange(c2, (c2 + Normalizer2Impl.Hangul.JAMO_VT_COUNT) - 1, this.base.getCE32(c2), true);
                    c2 = limit;
                }
            }
            setDigitTags();
            setLeadSurrogates();
            this.ce32s.setElementAt(this.trie.get(0), 0);
            this.trie.set(0, Collation.makeCE32FromTagAndIndex(11, 0));
            data.trie = this.trie.toTrie2_32();
            int c3 = 65536;
            char lead = 55296;
            while (lead < 56320) {
                if (this.unsafeBackwardSet.containsSome(c3, c3 + Opcodes.OP_NEW_INSTANCE_JUMBO)) {
                    this.unsafeBackwardSet.add((int) lead);
                }
                lead = (char) (lead + 1);
                c3 += 1024;
            }
            this.unsafeBackwardSet.freeze();
            data.ce32s = this.ce32s.getBuffer();
            data.ces = this.ce64s.getBuffer();
            data.contexts = this.contexts.toString();
            data.base = this.base;
            if (jamoIndex >= 0) {
                data.jamoCE32s = jamoCE32s;
            } else {
                data.jamoCE32s = this.base.jamoCE32s;
            }
            data.unsafeBackwardSet = this.unsafeBackwardSet;
            return;
        }
        throw new IllegalStateException("attempt to build() after build()");
    }

    /* access modifiers changed from: protected */
    public void clearContexts() {
        this.contexts.setLength(0);
        UnicodeSetIterator iter = new UnicodeSetIterator(this.contextChars);
        while (iter.next()) {
            getConditionalCE32ForCE32(this.trie.get(iter.codepoint)).builtCE32 = 1;
        }
    }

    /* access modifiers changed from: protected */
    public void buildContexts() {
        this.contexts.setLength(0);
        UnicodeSetIterator iter = new UnicodeSetIterator(this.contextChars);
        while (iter.next()) {
            int c = iter.codepoint;
            int ce32 = this.trie.get(c);
            if (isBuilderContextCE32(ce32)) {
                this.trie.set(c, buildContext(getConditionalCE32ForCE32(ce32)));
            } else {
                throw new AssertionError("Impossible: No context data for c in contextChars.");
            }
        }
    }

    /* access modifiers changed from: protected */
    public int buildContext(ConditionalCE32 head) {
        int emptySuffixCE32;
        ConditionalCE32 cond;
        CharsTrieBuilder prefixBuilder;
        CharsTrieBuilder prefixBuilder2 = new CharsTrieBuilder();
        CharsTrieBuilder contractionBuilder = new CharsTrieBuilder();
        ConditionalCE32 lastCond = head;
        while (true) {
            int prefixLength = lastCond.prefixLength();
            int i = 0;
            StringBuilder prefix = new StringBuilder().append(lastCond.context, 0, prefixLength + 1);
            String prefixString = prefix.toString();
            ConditionalCE32 firstCond = lastCond;
            ConditionalCE32 cond2 = lastCond;
            while (cond2.next >= 0) {
                ConditionalCE32 conditionalCE32 = getConditionalCE32(cond2.next);
                cond2 = conditionalCE32;
                if (!conditionalCE32.context.startsWith(prefixString)) {
                    break;
                }
                lastCond = cond2;
            }
            int suffixStart = prefixLength + 1;
            if (lastCond.context.length() == suffixStart) {
                emptySuffixCE32 = lastCond.ce32;
                cond = lastCond;
            } else {
                contractionBuilder.clear();
                int emptySuffixCE322 = 1;
                int flags = 0;
                if (firstCond.context.length() == suffixStart) {
                    emptySuffixCE322 = firstCond.ce32;
                    cond = getConditionalCE32(firstCond.next);
                } else {
                    flags = 0 | 256;
                    ConditionalCE32 cond3 = head;
                    while (true) {
                        int length = cond3.prefixLength();
                        if (length == prefixLength) {
                            break;
                        }
                        ConditionalCE32 conditionalCE322 = head;
                        if (cond3.defaultCE32 != 1) {
                            if (length != 0) {
                                prefixBuilder = prefixBuilder2;
                                if (!prefixString.regionMatches(prefix.length() - length, cond3.context, 1, length)) {
                                }
                            } else {
                                prefixBuilder = prefixBuilder2;
                            }
                            emptySuffixCE322 = cond3.defaultCE32;
                        } else {
                            prefixBuilder = prefixBuilder2;
                        }
                        cond3 = getConditionalCE32(cond3.next);
                        prefixBuilder2 = prefixBuilder;
                        i = 0;
                    }
                    cond = firstCond;
                }
                int flags2 = flags | 512;
                while (true) {
                    String suffix = cond.context.substring(suffixStart);
                    if (this.nfcImpl.getFCD16(suffix.codePointAt(i)) <= 255) {
                        flags2 &= -513;
                    }
                    if (this.nfcImpl.getFCD16(suffix.codePointBefore(suffix.length())) > 255) {
                        flags2 |= 1024;
                    }
                    contractionBuilder.add(suffix, cond.ce32);
                    if (cond == lastCond) {
                        break;
                    }
                    ConditionalCE32 conditionalCE323 = head;
                    cond = getConditionalCE32(cond.next);
                    i = 0;
                }
                int fcd16 = addContextTrie(emptySuffixCE322, contractionBuilder);
                if (fcd16 <= 524287) {
                    emptySuffixCE32 = Collation.makeCE32FromTagAndIndex(9, fcd16) | flags2;
                } else {
                    ConditionalCE32 conditionalCE324 = head;
                    throw new IndexOutOfBoundsException("too many context-sensitive mappings");
                }
            }
            int ce32 = emptySuffixCE32;
            firstCond.defaultCE32 = ce32;
            if (prefixLength != 0) {
                prefix.delete(0, 1);
                prefix.reverse();
                prefixBuilder2.add(prefix, ce32);
                if (cond.next < 0) {
                    int index = addContextTrie(head.defaultCE32, prefixBuilder2);
                    if (index <= 524287) {
                        return Collation.makeCE32FromTagAndIndex(8, index);
                    }
                    throw new IndexOutOfBoundsException("too many context-sensitive mappings");
                }
            } else if (cond.next < 0) {
                return ce32;
            }
            ConditionalCE32 conditionalCE325 = head;
            lastCond = getConditionalCE32(cond.next);
        }
    }

    /* access modifiers changed from: protected */
    public int addContextTrie(int defaultCE32, CharsTrieBuilder trieBuilder) {
        StringBuilder context = new StringBuilder();
        context.append((char) (defaultCE32 >> 16));
        context.append((char) defaultCE32);
        context.append(trieBuilder.buildCharSequence(StringTrieBuilder.Option.SMALL));
        int index = this.contexts.indexOf(context.toString());
        if (index >= 0) {
            return index;
        }
        int index2 = this.contexts.length();
        this.contexts.append(context);
        return index2;
    }

    /* access modifiers changed from: protected */
    public void buildFastLatinTable(CollationData data) {
        if (this.fastLatinEnabled) {
            this.fastLatinBuilder = new CollationFastLatinBuilder();
            if (this.fastLatinBuilder.forData(data)) {
                char[] header = this.fastLatinBuilder.getHeader();
                char[] table = this.fastLatinBuilder.getTable();
                if (this.base != null && Arrays.equals(header, this.base.fastLatinTableHeader) && Arrays.equals(table, this.base.fastLatinTable)) {
                    this.fastLatinBuilder = null;
                    header = this.base.fastLatinTableHeader;
                    table = this.base.fastLatinTable;
                }
                data.fastLatinTableHeader = header;
                data.fastLatinTable = table;
            } else {
                this.fastLatinBuilder = null;
            }
        }
    }

    /* access modifiers changed from: protected */
    public int getCEs(CharSequence s, int start, long[] ces, int cesLength) {
        if (this.collIter == null) {
            this.collIter = new DataBuilderCollationIterator(this, new CollationData(this.nfcImpl));
            if (this.collIter == null) {
                return 0;
            }
        }
        return this.collIter.fetchCEs(s, start, ces, cesLength);
    }

    protected static int jamoCpFromIndex(int i) {
        if (i < 19) {
            return Normalizer2Impl.Hangul.JAMO_L_BASE + i;
        }
        int i2 = i - 19;
        if (i2 < 21) {
            return Normalizer2Impl.Hangul.JAMO_V_BASE + i2;
        }
        return 4520 + (i2 - 21);
    }

    /* access modifiers changed from: protected */
    public final boolean isMutable() {
        return (this.trie == null || this.unsafeBackwardSet == null || this.unsafeBackwardSet.isFrozen()) ? false : true;
    }
}
