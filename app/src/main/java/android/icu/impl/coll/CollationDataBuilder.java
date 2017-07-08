package android.icu.impl.coll;

import android.icu.impl.Norm2AllModes;
import android.icu.impl.Normalizer2Impl;
import android.icu.impl.Normalizer2Impl.Hangul;
import android.icu.impl.Trie2.Range;
import android.icu.impl.Trie2Writable;
import android.icu.lang.UCharacter;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.text.UnicodeSetIterator;
import android.icu.util.CharsTrie;
import android.icu.util.CharsTrie.Entry;
import android.icu.util.CharsTrieBuilder;
import android.icu.util.StringTrieBuilder.Option;
import com.android.dex.DexFormat;
import dalvik.bytecode.Opcodes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import libcore.icu.DateUtilsBridge;
import libcore.icu.ICU;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

final class CollationDataBuilder {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int IS_BUILDER_JAMO_CE32 = 256;
    protected CollationData base;
    protected CollationSettings baseSettings;
    protected UVector32 ce32s;
    protected UVector64 ce64s;
    protected DataBuilderCollationIterator collIter;
    protected ArrayList<ConditionalCE32> conditionalCE32s;
    protected UnicodeSet contextChars;
    protected StringBuilder contexts;
    protected CollationFastLatinBuilder fastLatinBuilder;
    protected boolean fastLatinEnabled;
    protected boolean modified;
    protected Normalizer2Impl nfcImpl;
    protected Trie2Writable trie;
    protected UnicodeSet unsafeBackwardSet;

    interface CEModifier {
        long modifyCE(long j);

        long modifyCE32(int i);
    }

    private static final class ConditionalCE32 {
        int builtCE32;
        int ce32;
        String context;
        int defaultCE32;
        int next;

        ConditionalCE32(String ct, int ce) {
            this.context = ct;
            this.ce32 = ce;
            this.defaultCE32 = 1;
            this.builtCE32 = 1;
            this.next = -1;
        }

        boolean hasContext() {
            return this.context.length() > 1 ? true : CollationDataBuilder.-assertionsDisabled;
        }

        int prefixLength() {
            return this.context.charAt(0);
        }
    }

    private static final class CopyHelper {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        CollationDataBuilder dest;
        long[] modifiedCEs;
        CEModifier modifier;
        CollationDataBuilder src;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationDataBuilder.CopyHelper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.coll.CollationDataBuilder.CopyHelper.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationDataBuilder.CopyHelper.<clinit>():void");
        }

        CopyHelper(CollationDataBuilder s, CollationDataBuilder d, CEModifier m) {
            this.modifiedCEs = new long[31];
            this.src = s;
            this.dest = d;
            this.modifier = m;
        }

        void copyRangeCE32(int start, int end, int ce32) {
            ce32 = copyCE32(ce32);
            this.dest.trie.setRange(start, end, ce32, true);
            if (CollationDataBuilder.isBuilderContextCE32(ce32)) {
                this.dest.contextChars.add(start, end);
            }
        }

        int copyCE32(int ce32) {
            long ce;
            if (Collation.isSpecialCE32(ce32)) {
                int tag = Collation.tagFromCE32(ce32);
                int srcIndex;
                int length;
                boolean isModified;
                int i;
                int j;
                if (tag == 5) {
                    int[] srcCE32s = this.src.ce32s.getBuffer();
                    srcIndex = Collation.indexFromCE32(ce32);
                    length = Collation.lengthFromCE32(ce32);
                    isModified = CollationDataBuilder.-assertionsDisabled;
                    for (i = 0; i < length; i++) {
                        ce32 = srcCE32s[srcIndex + i];
                        if (!Collation.isSpecialCE32(ce32)) {
                            ce = this.modifier.modifyCE32(ce32);
                            if (ce != Collation.NO_CE) {
                                if (!isModified) {
                                    for (j = 0; j < i; j++) {
                                        this.modifiedCEs[j] = Collation.ceFromCE32(srcCE32s[srcIndex + j]);
                                    }
                                    isModified = true;
                                }
                                this.modifiedCEs[i] = ce;
                            }
                        }
                        if (isModified) {
                            this.modifiedCEs[i] = Collation.ceFromCE32(ce32);
                        }
                    }
                    if (isModified) {
                        return this.dest.encodeCEs(this.modifiedCEs, length);
                    }
                    return this.dest.encodeExpansion32(srcCE32s, srcIndex, length);
                } else if (tag == 6) {
                    long[] srcCEs = this.src.ce64s.getBuffer();
                    srcIndex = Collation.indexFromCE32(ce32);
                    length = Collation.lengthFromCE32(ce32);
                    isModified = CollationDataBuilder.-assertionsDisabled;
                    for (i = 0; i < length; i++) {
                        long srcCE = srcCEs[srcIndex + i];
                        ce = this.modifier.modifyCE(srcCE);
                        if (ce != Collation.NO_CE) {
                            if (!isModified) {
                                for (j = 0; j < i; j++) {
                                    this.modifiedCEs[j] = srcCEs[srcIndex + j];
                                }
                                isModified = true;
                            }
                            this.modifiedCEs[i] = ce;
                        } else if (isModified) {
                            this.modifiedCEs[i] = srcCE;
                        }
                    }
                    if (isModified) {
                        return this.dest.encodeCEs(this.modifiedCEs, length);
                    }
                    return this.dest.encodeExpansion(srcCEs, srcIndex, length);
                } else if (tag == 7) {
                    ConditionalCE32 cond = this.src.getConditionalCE32ForCE32(ce32);
                    if (!-assertionsDisabled) {
                        if ((cond.hasContext() ? null : 1) == null) {
                            throw new AssertionError();
                        }
                    }
                    int destIndex = this.dest.addConditionalCE32(cond.context, copyCE32(cond.ce32));
                    ce32 = CollationDataBuilder.makeBuilderContextCE32(destIndex);
                    while (cond.next >= 0) {
                        cond = this.src.getConditionalCE32(cond.next);
                        ConditionalCE32 prevDestCond = this.dest.getConditionalCE32(destIndex);
                        destIndex = this.dest.addConditionalCE32(cond.context, copyCE32(cond.ce32));
                        this.dest.unsafeBackwardSet.addAll(cond.context.substring(cond.prefixLength() + 1));
                        prevDestCond.next = destIndex;
                    }
                    return ce32;
                } else if (-assertionsDisabled) {
                    return ce32;
                } else {
                    Object obj = (tag == 1 || tag == 2 || tag == 4) ? 1 : tag == 12 ? 1 : null;
                    if (obj != null) {
                        return ce32;
                    }
                    throw new AssertionError();
                }
            }
            ce = this.modifier.modifyCE32(ce32);
            if (ce == Collation.NO_CE) {
                return ce32;
            }
            return this.dest.encodeOneCE(ce);
        }
    }

    private static final class DataBuilderCollationIterator extends CollationIterator {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        protected final CollationDataBuilder builder;
        protected final CollationData builderData;
        protected final int[] jamoCE32s;
        protected int pos;
        protected CharSequence s;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationDataBuilder.DataBuilderCollationIterator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.coll.CollationDataBuilder.DataBuilderCollationIterator.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationDataBuilder.DataBuilderCollationIterator.<clinit>():void");
        }

        DataBuilderCollationIterator(CollationDataBuilder b, CollationData newData) {
            super(newData, CollationDataBuilder.-assertionsDisabled);
            this.jamoCE32s = new int[67];
            this.builder = b;
            this.builderData = newData;
            this.builderData.base = this.builder.base;
            for (int j = 0; j < 67; j++) {
                this.jamoCE32s[j] = Collation.makeCE32FromTagAndIndex(7, CollationDataBuilder.jamoCpFromIndex(j)) | CollationDataBuilder.IS_BUILDER_JAMO_CE32;
            }
            this.builderData.jamoCE32s = this.jamoCE32s;
        }

        int fetchCEs(CharSequence str, int start, long[] ces, int cesLength) {
            this.builderData.ce32s = this.builder.ce32s.getBuffer();
            this.builderData.ces = this.builder.ce64s.getBuffer();
            this.builderData.contexts = this.builder.contexts.toString();
            reset();
            this.s = str;
            this.pos = start;
            while (this.pos < this.s.length()) {
                CollationData d;
                clearCEs();
                int c = Character.codePointAt(this.s, this.pos);
                this.pos += Character.charCount(c);
                int ce32 = this.builder.trie.get(c);
                if (ce32 == Opcodes.OP_AND_LONG_2ADDR) {
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

        protected void forwardNumCodePoints(int num) {
            this.pos = Character.offsetByCodePoints(this.s, this.pos, num);
        }

        protected void backwardNumCodePoints(int num) {
            this.pos = Character.offsetByCodePoints(this.s, this.pos, -num);
        }

        protected int getDataCE32(int c) {
            return this.builder.trie.get(c);
        }

        protected int getCE32FromBuilderData(int ce32) {
            if (!-assertionsDisabled && !Collation.hasCE32Tag(ce32, 7)) {
                throw new AssertionError();
            } else if ((ce32 & CollationDataBuilder.IS_BUILDER_JAMO_CE32) != 0) {
                return this.builder.trie.get(Collation.indexFromCE32(ce32));
            } else {
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
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationDataBuilder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.coll.CollationDataBuilder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationDataBuilder.<clinit>():void");
    }

    CollationDataBuilder() {
        this.contextChars = new UnicodeSet();
        this.contexts = new StringBuilder();
        this.unsafeBackwardSet = new UnicodeSet();
        this.nfcImpl = Norm2AllModes.getNFCInstance().impl;
        this.base = null;
        this.baseSettings = null;
        this.trie = null;
        this.ce32s = new UVector32();
        this.ce64s = new UVector64();
        this.conditionalCE32s = new ArrayList();
        this.modified = -assertionsDisabled;
        this.fastLatinEnabled = -assertionsDisabled;
        this.fastLatinBuilder = null;
        this.collIter = null;
        this.ce32s.addElement(0);
    }

    void initForTailoring(CollationData b) {
        if (this.trie != null) {
            throw new IllegalStateException("attempt to reuse a CollationDataBuilder");
        } else if (b == null) {
            throw new IllegalArgumentException("null CollationData");
        } else {
            this.base = b;
            this.trie = new Trie2Writable(Opcodes.OP_AND_LONG_2ADDR, -195323);
            for (int c = Opcodes.OP_AND_LONG_2ADDR; c <= Opcodes.OP_CONST_CLASS_JUMBO; c++) {
                this.trie.set(c, Opcodes.OP_AND_LONG_2ADDR);
            }
            this.trie.setRange(Hangul.HANGUL_BASE, Hangul.HANGUL_END, Collation.makeCE32FromTagAndIndex(12, 0), true);
            this.unsafeBackwardSet.addAll(b.unsafeBackwardSet);
        }
    }

    boolean isCompressibleLeadByte(int b) {
        return this.base.isCompressibleLeadByte(b);
    }

    boolean isCompressiblePrimary(long p) {
        return isCompressibleLeadByte(((int) p) >>> 24);
    }

    boolean hasMappings() {
        return this.modified;
    }

    boolean isAssigned(int c) {
        return Collation.isAssignedCE32(this.trie.get(c));
    }

    void add(CharSequence prefix, CharSequence s, long[] ces, int cesLength) {
        addCE32(prefix, s, encodeCEs(ces, cesLength));
    }

    int encodeCEs(long[] ces, int cesLength) {
        if (cesLength < 0 || cesLength > 31) {
            throw new IllegalArgumentException("mapping to too many CEs");
        } else if (!isMutable()) {
            throw new IllegalStateException("attempt to add mappings after build()");
        } else if (cesLength == 0) {
            return encodeOneCEAsCE32(0);
        } else {
            if (cesLength == 1) {
                return encodeOneCE(ces[0]);
            }
            if (cesLength == 2) {
                long ce0 = ces[0];
                long ce1 = ces[1];
                long p0 = ce0 >>> 32;
                if ((72057594037862655L & ce0) == 83886080 && (-4278190081L & ce1) == 1280 && p0 != 0) {
                    return (((((int) p0) | ((((int) ce0) & Normalizer2Impl.JAMO_VT) << 8)) | ((((int) ce1) >> 16) & Normalizer2Impl.JAMO_VT)) | Opcodes.OP_AND_LONG_2ADDR) | 4;
                }
            }
            int[] newCE32s = new int[31];
            for (int i = 0; i != cesLength; i++) {
                int ce32 = encodeOneCEAsCE32(ces[i]);
                if (ce32 == 1) {
                    return encodeExpansion(ces, 0, cesLength);
                }
                newCE32s[i] = ce32;
            }
            return encodeExpansion32(newCE32s, 0, cesLength);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void addCE32(CharSequence prefix, CharSequence s, int ce32) {
        if (s.length() == 0) {
            throw new IllegalArgumentException("mapping from empty string");
        } else if (isMutable()) {
            int c = Character.codePointAt(s, 0);
            int cLength = Character.charCount(c);
            int oldCE32 = this.trie.get(c);
            boolean hasContext = (prefix.length() != 0 || s.length() > cLength) ? true : -assertionsDisabled;
            if (oldCE32 == Opcodes.OP_AND_LONG_2ADDR) {
                int baseCE32 = this.base.getFinalCE32(this.base.getCE32(c));
                if (hasContext || Collation.ce32HasContext(baseCE32)) {
                    oldCE32 = copyFromBaseCE32(c, baseCE32, true);
                    this.trie.set(c, oldCE32);
                }
            }
            ConditionalCE32 cond;
            if (hasContext) {
                if (isBuilderContextCE32(oldCE32)) {
                    cond = getConditionalCE32ForCE32(oldCE32);
                    cond.builtCE32 = 1;
                } else {
                    int index = addConditionalCE32(DexFormat.MAGIC_SUFFIX, oldCE32);
                    this.trie.set(c, makeBuilderContextCE32(index));
                    this.contextChars.add(c);
                    cond = getConditionalCE32(index);
                }
                CharSequence suffix = s.subSequence(cLength, s.length());
                String context = ((char) prefix.length()) + prefix + suffix;
                this.unsafeBackwardSet.addAll(suffix);
                while (true) {
                    int next = cond.next;
                    if (next < 0) {
                        break;
                    }
                    ConditionalCE32 nextCond = getConditionalCE32(next);
                    int cmp = context.compareTo(nextCond.context);
                    if (cmp < 0) {
                        break;
                    } else if (cmp == 0) {
                        break;
                    } else {
                        cond = nextCond;
                    }
                }
                cond.next = addConditionalCE32(context, ce32);
            } else if (isBuilderContextCE32(oldCE32)) {
                cond = getConditionalCE32ForCE32(oldCE32);
                cond.builtCE32 = 1;
                cond.ce32 = ce32;
            } else {
                this.trie.set(c, ce32);
            }
            this.modified = true;
        } else {
            throw new IllegalStateException("attempt to add mappings after build()");
        }
    }

    void copyFrom(CollationDataBuilder src, CEModifier modifier) {
        if (isMutable()) {
            CopyHelper helper = new CopyHelper(src, this, modifier);
            Iterator<Range> trieIterator = src.trie.iterator();
            while (trieIterator.hasNext()) {
                Range range = (Range) trieIterator.next();
                if (range.leadSurrogate) {
                    break;
                }
                enumRangeForCopy(range.startCodePoint, range.endCodePoint, range.value, helper);
            }
            this.modified |= src.modified;
            return;
        }
        throw new IllegalStateException("attempt to copyFrom() after build()");
    }

    void optimize(UnicodeSet set) {
        if (!set.isEmpty()) {
            UnicodeSetIterator iter = new UnicodeSetIterator(set);
            while (iter.next() && iter.codepoint != UnicodeSetIterator.IS_STRING) {
                int c = iter.codepoint;
                if (this.trie.get(c) == Opcodes.OP_AND_LONG_2ADDR) {
                    this.trie.set(c, copyFromBaseCE32(c, this.base.getFinalCE32(this.base.getCE32(c)), true));
                }
            }
            this.modified = true;
        }
    }

    void suppressContractions(UnicodeSet set) {
        if (!set.isEmpty()) {
            UnicodeSetIterator iter = new UnicodeSetIterator(set);
            while (iter.next() && iter.codepoint != UnicodeSetIterator.IS_STRING) {
                int c = iter.codepoint;
                int ce32 = this.trie.get(c);
                if (ce32 == Opcodes.OP_AND_LONG_2ADDR) {
                    ce32 = this.base.getFinalCE32(this.base.getCE32(c));
                    if (Collation.ce32HasContext(ce32)) {
                        this.trie.set(c, copyFromBaseCE32(c, ce32, -assertionsDisabled));
                    }
                } else if (isBuilderContextCE32(ce32)) {
                    this.trie.set(c, getConditionalCE32ForCE32(ce32).ce32);
                    this.contextChars.remove(c);
                }
            }
            this.modified = true;
        }
    }

    void enableFastLatin() {
        this.fastLatinEnabled = true;
    }

    void build(CollationData data) {
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

    int getCEs(CharSequence s, long[] ces, int cesLength) {
        return getCEs(s, 0, ces, cesLength);
    }

    int getCEs(CharSequence prefix, CharSequence s, long[] ces, int cesLength) {
        int prefixLength = prefix.length();
        if (prefixLength == 0) {
            return getCEs(s, 0, ces, cesLength);
        }
        return getCEs(new StringBuilder(prefix).append(s), prefixLength, ces, cesLength);
    }

    protected int getCE32FromOffsetCE32(boolean fromBase, int c, int ce32) {
        int i = Collation.indexFromCE32(ce32);
        return Collation.makeLongPrimaryCE32(Collation.getThreeBytePrimaryForOffsetData(c, fromBase ? this.base.ces[i] : this.ce64s.elementAti(i)));
    }

    protected int addCE(long ce) {
        int length = this.ce64s.size();
        for (int i = 0; i < length; i++) {
            if (ce == this.ce64s.elementAti(i)) {
                return i;
            }
        }
        this.ce64s.addElement(ce);
        return length;
    }

    protected int addCE32(int ce32) {
        int length = this.ce32s.size();
        for (int i = 0; i < length; i++) {
            if (ce32 == this.ce32s.elementAti(i)) {
                return i;
            }
        }
        this.ce32s.addElement(ce32);
        return length;
    }

    protected int addConditionalCE32(String context, int ce32) {
        Object obj = null;
        if (!-assertionsDisabled) {
            if (context.length() != 0) {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        int index = this.conditionalCE32s.size();
        if (index > 524287) {
            throw new IndexOutOfBoundsException("too many context-sensitive mappings");
        }
        this.conditionalCE32s.add(new ConditionalCE32(context, ce32));
        return index;
    }

    protected ConditionalCE32 getConditionalCE32(int index) {
        return (ConditionalCE32) this.conditionalCE32s.get(index);
    }

    protected ConditionalCE32 getConditionalCE32ForCE32(int ce32) {
        return getConditionalCE32(Collation.indexFromCE32(ce32));
    }

    protected static int makeBuilderContextCE32(int index) {
        return Collation.makeCE32FromTagAndIndex(7, index);
    }

    protected static boolean isBuilderContextCE32(int ce32) {
        return Collation.hasCE32Tag(ce32, 7);
    }

    protected static int encodeOneCEAsCE32(long ce) {
        int i = 0;
        long p = ce >>> 32;
        int lower32 = (int) ce;
        int t = lower32 & DexFormat.MAX_TYPE_IDX;
        if (!-assertionsDisabled) {
            if ((t & Collation.CASE_MASK) != Collation.CASE_MASK) {
                i = 1;
            }
            if (i == 0) {
                throw new AssertionError();
            }
        }
        if ((281470698455295L & ce) == 0) {
            return (((int) p) | (lower32 >>> 16)) | (t >> 8);
        }
        if ((1099511627775L & ce) == 83887360) {
            return Collation.makeLongPrimaryCE32(p);
        }
        if (p == 0 && (t & Opcodes.OP_CONST_CLASS_JUMBO) == 0) {
            return Collation.makeLongSecondaryCE32(lower32);
        }
        return 1;
    }

    protected int encodeOneCE(long ce) {
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

    protected int encodeExpansion(long[] ces, int start, int length) {
        int i;
        long first = ces[start];
        int ce64sMax = this.ce64s.size() - length;
        for (i = 0; i <= ce64sMax; i++) {
            int j;
            if (first == this.ce64s.elementAti(i)) {
                if (i > 524287) {
                    throw new IndexOutOfBoundsException("too many mappings");
                }
                j = 1;
                while (j != length) {
                    if (this.ce64s.elementAti(i + j) == ces[start + j]) {
                        j++;
                    }
                }
                return Collation.makeCE32FromTagIndexAndLength(6, i, length);
            }
        }
        i = this.ce64s.size();
        if (i > 524287) {
            throw new IndexOutOfBoundsException("too many mappings");
        }
        for (j = 0; j < length; j++) {
            this.ce64s.addElement(ces[start + j]);
        }
        return Collation.makeCE32FromTagIndexAndLength(6, i, length);
    }

    protected int encodeExpansion32(int[] newCE32s, int start, int length) {
        int i;
        int first = newCE32s[start];
        int ce32sMax = this.ce32s.size() - length;
        for (i = 0; i <= ce32sMax; i++) {
            int j;
            if (first == this.ce32s.elementAti(i)) {
                if (i > 524287) {
                    throw new IndexOutOfBoundsException("too many mappings");
                }
                j = 1;
                while (j != length) {
                    if (this.ce32s.elementAti(i + j) == newCE32s[start + j]) {
                        j++;
                    }
                }
                return Collation.makeCE32FromTagIndexAndLength(5, i, length);
            }
        }
        i = this.ce32s.size();
        if (i > 524287) {
            throw new IndexOutOfBoundsException("too many mappings");
        }
        for (j = 0; j < length; j++) {
            this.ce32s.addElement(newCE32s[start + j]);
        }
        return Collation.makeCE32FromTagIndexAndLength(5, i, length);
    }

    protected int copyFromBaseCE32(int c, int ce32, boolean withContext) {
        if (!Collation.isSpecialCE32(ce32)) {
            return ce32;
        }
        ConditionalCE32 head;
        switch (Collation.tagFromCE32(ce32)) {
            case NodeFilter.SHOW_ELEMENT /*1*/:
            case NodeFilter.SHOW_ATTRIBUTE /*2*/:
            case NodeFilter.SHOW_TEXT /*4*/:
                break;
            case XmlPullParser.CDSECT /*5*/:
                ce32 = encodeExpansion32(this.base.ce32s, Collation.indexFromCE32(ce32), Collation.lengthFromCE32(ce32));
                break;
            case XmlPullParser.ENTITY_REF /*6*/:
                ce32 = encodeExpansion(this.base.ces, Collation.indexFromCE32(ce32), Collation.lengthFromCE32(ce32));
                break;
            case NodeFilter.SHOW_CDATA_SECTION /*8*/:
                int trieIndex = Collation.indexFromCE32(ce32);
                ce32 = this.base.getCE32FromContexts(trieIndex);
                if (withContext) {
                    int index;
                    head = new ConditionalCE32(XmlPullParser.NO_NAMESPACE, 0);
                    StringBuilder context = new StringBuilder(DexFormat.MAGIC_SUFFIX);
                    if (Collation.isContractionCE32(ce32)) {
                        index = copyContractionsFromBaseCE32(context, c, ce32, head);
                    } else {
                        index = addConditionalCE32(context.toString(), copyFromBaseCE32(c, ce32, true));
                        head.next = index;
                    }
                    ConditionalCE32 cond = getConditionalCE32(index);
                    CharsTrie.Iterator prefixes = CharsTrie.iterator(this.base.contexts, trieIndex + 2, 0);
                    while (prefixes.hasNext()) {
                        Entry entry = prefixes.next();
                        context.setLength(0);
                        context.append(entry.chars).reverse().insert(0, (char) entry.chars.length());
                        ce32 = entry.value;
                        if (Collation.isContractionCE32(ce32)) {
                            index = copyContractionsFromBaseCE32(context, c, ce32, cond);
                        } else {
                            index = addConditionalCE32(context.toString(), copyFromBaseCE32(c, ce32, true));
                            cond.next = index;
                        }
                        cond = getConditionalCE32(index);
                    }
                    ce32 = makeBuilderContextCE32(head.next);
                    this.contextChars.add(c);
                    break;
                }
                return copyFromBaseCE32(c, ce32, -assertionsDisabled);
            case XmlPullParser.COMMENT /*9*/:
                if (withContext) {
                    head = new ConditionalCE32(XmlPullParser.NO_NAMESPACE, 0);
                    copyContractionsFromBaseCE32(new StringBuilder(DexFormat.MAGIC_SUFFIX), c, ce32, head);
                    ce32 = makeBuilderContextCE32(head.next);
                    this.contextChars.add(c);
                    break;
                }
                return copyFromBaseCE32(c, this.base.getCE32FromContexts(Collation.indexFromCE32(ce32)), -assertionsDisabled);
            case ICU.U_ILLEGAL_CHAR_FOUND /*12*/:
                throw new UnsupportedOperationException("We forbid tailoring of Hangul syllables.");
            case Opcodes.OP_RETURN_VOID /*14*/:
                ce32 = getCE32FromOffsetCE32(true, c, ce32);
                break;
            case ICU.U_BUFFER_OVERFLOW_ERROR /*15*/:
                ce32 = encodeOneCE(Collation.unassignedCEFromCodePoint(c));
                break;
            default:
                throw new AssertionError("copyFromBaseCE32(c, ce32, withContext) requires ce32 == base.getFinalCE32(ce32)");
        }
        return ce32;
    }

    protected int copyContractionsFromBaseCE32(StringBuilder context, int c, int ce32, ConditionalCE32 cond) {
        int index;
        boolean z = true;
        int trieIndex = Collation.indexFromCE32(ce32);
        if ((ce32 & IS_BUILDER_JAMO_CE32) != 0) {
            if (!-assertionsDisabled) {
                if (!(context.length() > 1)) {
                    throw new AssertionError();
                }
            }
            index = -1;
        } else {
            ce32 = this.base.getCE32FromContexts(trieIndex);
            if (!-assertionsDisabled) {
                int i;
                if (Collation.isContractionCE32(ce32)) {
                    i = 0;
                } else {
                    boolean z2 = true;
                }
                if (i == 0) {
                    throw new AssertionError();
                }
            }
            index = addConditionalCE32(context.toString(), copyFromBaseCE32(c, ce32, true));
            cond.next = index;
            cond = getConditionalCE32(index);
        }
        int suffixStart = context.length();
        CharsTrie.Iterator suffixes = CharsTrie.iterator(this.base.contexts, trieIndex + 2, 0);
        while (suffixes.hasNext()) {
            Entry entry = suffixes.next();
            context.append(entry.chars);
            index = addConditionalCE32(context.toString(), copyFromBaseCE32(c, entry.value, true));
            cond.next = index;
            cond = getConditionalCE32(index);
            context.setLength(suffixStart);
        }
        if (!-assertionsDisabled) {
            if (index < 0) {
                z = -assertionsDisabled;
            }
            if (!z) {
                throw new AssertionError();
            }
        }
        return index;
    }

    private static void enumRangeForCopy(int start, int end, int value, CopyHelper helper) {
        if (value != -1 && value != Opcodes.OP_AND_LONG_2ADDR) {
            helper.copyRangeCE32(start, end, value);
        }
    }

    protected boolean getJamoCE32s(int[] jamoCE32s) {
        int j;
        boolean z = this.base == null ? true : -assertionsDisabled;
        boolean needToCopyFromBase = -assertionsDisabled;
        for (j = 0; j < 67; j++) {
            int jamo = jamoCpFromIndex(j);
            boolean fromBase = -assertionsDisabled;
            int ce32 = this.trie.get(jamo);
            z |= Collation.isAssignedCE32(ce32);
            if (ce32 == Opcodes.OP_AND_LONG_2ADDR) {
                fromBase = true;
                ce32 = this.base.getCE32(jamo);
            }
            if (Collation.isSpecialCE32(ce32)) {
                switch (Collation.tagFromCE32(ce32)) {
                    case XmlPullParser.START_DOCUMENT /*0*/:
                    case XmlPullParser.END_TAG /*3*/:
                    case XmlPullParser.IGNORABLE_WHITESPACE /*7*/:
                    case XmlPullParser.DOCDECL /*10*/:
                    case ICU.U_TRUNCATED_CHAR_FOUND /*11*/:
                    case ICU.U_ILLEGAL_CHAR_FOUND /*12*/:
                    case Opcodes.OP_MOVE_EXCEPTION /*13*/:
                        throw new AssertionError(String.format("unexpected special tag in ce32=0x%08x", new Object[]{Integer.valueOf(ce32)}));
                    case XmlPullParser.CDSECT /*5*/:
                    case XmlPullParser.ENTITY_REF /*6*/:
                    case NodeFilter.SHOW_CDATA_SECTION /*8*/:
                    case XmlPullParser.COMMENT /*9*/:
                        if (!fromBase) {
                            break;
                        }
                        ce32 = Opcodes.OP_AND_LONG_2ADDR;
                        needToCopyFromBase = true;
                        break;
                    case Opcodes.OP_RETURN_VOID /*14*/:
                        ce32 = getCE32FromOffsetCE32(fromBase, jamo, ce32);
                        break;
                    case ICU.U_BUFFER_OVERFLOW_ERROR /*15*/:
                        if (-assertionsDisabled || fromBase) {
                            ce32 = Opcodes.OP_AND_LONG_2ADDR;
                            needToCopyFromBase = true;
                            break;
                        }
                        throw new AssertionError();
                        break;
                    default:
                        break;
                }
            }
            jamoCE32s[j] = ce32;
        }
        if (z && needToCopyFromBase) {
            for (j = 0; j < 67; j++) {
                if (jamoCE32s[j] == 192) {
                    jamo = jamoCpFromIndex(j);
                    jamoCE32s[j] = copyFromBaseCE32(jamo, this.base.getCE32(jamo), true);
                }
            }
        }
        return z;
    }

    protected void setDigitTags() {
        UnicodeSetIterator iter = new UnicodeSetIterator(new UnicodeSet("[:Nd:]"));
        while (iter.next()) {
            if (!-assertionsDisabled) {
                if ((iter.codepoint != UnicodeSetIterator.IS_STRING ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            int c = iter.codepoint;
            int ce32 = this.trie.get(c);
            if (!(ce32 == Opcodes.OP_AND_LONG_2ADDR || ce32 == -1)) {
                int index = addCE32(ce32);
                if (index > 524287) {
                    throw new IndexOutOfBoundsException("too many mappings");
                }
                this.trie.set(c, Collation.makeCE32FromTagIndexAndLength(10, index, UCharacter.digit(c)));
            }
        }
    }

    protected void setLeadSurrogates() {
        for (char c = UCharacter.MIN_SURROGATE; c < UCharacter.MIN_LOW_SURROGATE; c = (char) (c + 1)) {
            int leadValue = -1;
            Iterator<Range> trieIterator = this.trie.iteratorForLeadSurrogate(c);
            while (trieIterator.hasNext()) {
                int value = ((Range) trieIterator.next()).value;
                if (value != -1) {
                    if (value != Opcodes.OP_AND_LONG_2ADDR) {
                        leadValue = NodeFilter.SHOW_DOCUMENT_TYPE;
                        break;
                    }
                    value = IS_BUILDER_JAMO_CE32;
                } else {
                    value = 0;
                }
                if (leadValue < 0) {
                    leadValue = value;
                } else if (leadValue != value) {
                    leadValue = NodeFilter.SHOW_DOCUMENT_TYPE;
                    break;
                }
            }
            this.trie.setForLeadSurrogateCodeUnit(c, Collation.makeCE32FromTagAndIndex(13, 0) | leadValue);
        }
    }

    protected void buildMappings(CollationData data) {
        if (isMutable()) {
            int c;
            buildContexts();
            int[] jamoCE32s = new int[67];
            int jamoIndex = -1;
            int ce32;
            int limit;
            if (getJamoCE32s(jamoCE32s)) {
                int i;
                jamoIndex = this.ce32s.size();
                for (i = 0; i < 67; i++) {
                    this.ce32s.addElement(jamoCE32s[i]);
                }
                boolean isAnyJamoVTSpecial = -assertionsDisabled;
                for (i = 19; i < 67; i++) {
                    if (Collation.isSpecialCE32(jamoCE32s[i])) {
                        isAnyJamoVTSpecial = true;
                        break;
                    }
                }
                int hangulCE32 = Collation.makeCE32FromTagAndIndex(12, 0);
                c = Hangul.HANGUL_BASE;
                i = 0;
                while (i < 19) {
                    ce32 = hangulCE32;
                    if (!(isAnyJamoVTSpecial || Collation.isSpecialCE32(jamoCE32s[i]))) {
                        ce32 = hangulCE32 | IS_BUILDER_JAMO_CE32;
                    }
                    limit = c + Hangul.JAMO_VT_COUNT;
                    this.trie.setRange(c, limit - 1, ce32, true);
                    c = limit;
                    i++;
                }
            } else {
                c = Hangul.HANGUL_BASE;
                while (c < Hangul.HANGUL_LIMIT) {
                    ce32 = this.base.getCE32(c);
                    if (-assertionsDisabled || Collation.hasCE32Tag(ce32, 12)) {
                        limit = c + Hangul.JAMO_VT_COUNT;
                        this.trie.setRange(c, limit - 1, ce32, true);
                        c = limit;
                    } else {
                        throw new AssertionError();
                    }
                }
            }
            setDigitTags();
            setLeadSurrogates();
            this.ce32s.setElementAt(this.trie.get(0), 0);
            this.trie.set(0, Collation.makeCE32FromTagAndIndex(11, 0));
            data.trie = this.trie.toTrie2_32();
            c = DateUtilsBridge.FORMAT_ABBREV_MONTH;
            int i2 = UTF16.SURROGATE_MIN_VALUE;
            while (i2 < UTF16.TRAIL_SURROGATE_MIN_VALUE) {
                if (this.unsafeBackwardSet.containsSome(c, c + Opcodes.OP_NEW_INSTANCE_JUMBO)) {
                    this.unsafeBackwardSet.add(i2);
                }
                i2 = (char) (i2 + 1);
                c += NodeFilter.SHOW_DOCUMENT_FRAGMENT;
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

    protected void clearContexts() {
        this.contexts.setLength(0);
        UnicodeSetIterator iter = new UnicodeSetIterator(this.contextChars);
        while (iter.next()) {
            if (!-assertionsDisabled) {
                if ((iter.codepoint != UnicodeSetIterator.IS_STRING ? 1 : 0) == 0) {
                    throw new AssertionError();
                }
            }
            int ce32 = this.trie.get(iter.codepoint);
            if (-assertionsDisabled || isBuilderContextCE32(ce32)) {
                getConditionalCE32ForCE32(ce32).builtCE32 = 1;
            } else {
                throw new AssertionError();
            }
        }
    }

    protected void buildContexts() {
        this.contexts.setLength(0);
        UnicodeSetIterator iter = new UnicodeSetIterator(this.contextChars);
        while (iter.next()) {
            if (!-assertionsDisabled) {
                if ((iter.codepoint != UnicodeSetIterator.IS_STRING ? 1 : 0) == 0) {
                    throw new AssertionError();
                }
            }
            int c = iter.codepoint;
            int ce32 = this.trie.get(c);
            if (isBuilderContextCE32(ce32)) {
                this.trie.set(c, buildContext(getConditionalCE32ForCE32(ce32)));
            } else {
                throw new AssertionError("Impossible: No context data for c in contextChars.");
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected int buildContext(ConditionalCE32 head) {
        int index;
        if (!-assertionsDisabled) {
            if ((head.hasContext() ? null : 1) == null) {
                throw new AssertionError();
            }
        }
        if (!-assertionsDisabled) {
            if ((head.next >= 0 ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        CharsTrieBuilder prefixBuilder = new CharsTrieBuilder();
        CharsTrieBuilder contractionBuilder = new CharsTrieBuilder();
        ConditionalCE32 cond = head;
        while (true) {
            int ce32;
            Object obj;
            if (!-assertionsDisabled) {
                if (!(cond != head ? cond.hasContext() : true)) {
                    break;
                }
            }
            int prefixLength = cond.prefixLength();
            StringBuilder prefix = new StringBuilder().append(cond.context, 0, prefixLength + 1);
            String prefixString = prefix.toString();
            ConditionalCE32 firstCond = cond;
            ConditionalCE32 lastCond = cond;
            while (cond.next >= 0) {
                cond = getConditionalCE32(cond.next);
                if (!cond.context.startsWith(prefixString)) {
                    break;
                }
                lastCond = cond;
            }
            int suffixStart = prefixLength + 1;
            if (lastCond.context.length() == suffixStart) {
                if (!-assertionsDisabled) {
                    if ((firstCond == lastCond ? 1 : null) == null) {
                        break;
                    }
                }
                ce32 = lastCond.ce32;
                cond = lastCond;
            } else {
                contractionBuilder.clear();
                int emptySuffixCE32 = 1;
                int flags = 0;
                if (firstCond.context.length() == suffixStart) {
                    emptySuffixCE32 = firstCond.ce32;
                    cond = getConditionalCE32(firstCond.next);
                } else {
                    flags = IS_BUILDER_JAMO_CE32;
                    cond = head;
                    while (true) {
                        int length = cond.prefixLength();
                        if (length == prefixLength) {
                            break;
                        }
                        int i = cond.defaultCE32;
                        if (r0 != 1) {
                            if (length != 0) {
                                if (!prefixString.regionMatches(prefix.length() - length, cond.context, 1, length)) {
                                }
                            }
                            emptySuffixCE32 = cond.defaultCE32;
                        }
                        cond = getConditionalCE32(cond.next);
                    }
                    cond = firstCond;
                }
                flags |= NodeFilter.SHOW_DOCUMENT_TYPE;
                while (true) {
                    String suffix = cond.context.substring(suffixStart);
                    if (this.nfcImpl.getFCD16(suffix.codePointAt(0)) <= 255) {
                        flags &= -513;
                    }
                    if (this.nfcImpl.getFCD16(suffix.codePointBefore(suffix.length())) > 255) {
                        flags |= NodeFilter.SHOW_DOCUMENT_FRAGMENT;
                    }
                    contractionBuilder.add(suffix, cond.ce32);
                    if (cond == lastCond) {
                        break;
                    }
                    cond = getConditionalCE32(cond.next);
                }
                index = addContextTrie(emptySuffixCE32, contractionBuilder);
                if (index > 524287) {
                    break;
                }
                ce32 = Collation.makeCE32FromTagAndIndex(9, index) | flags;
            }
            if (!-assertionsDisabled) {
                if (cond == lastCond) {
                    obj = 1;
                } else {
                    obj = null;
                }
                if (obj == null) {
                    break;
                }
            }
            firstCond.defaultCE32 = ce32;
            if (prefixLength != 0) {
                prefix.delete(0, 1);
                prefix.reverse();
                prefixBuilder.add(prefix, ce32);
                if (cond.next < 0) {
                    break;
                }
            } else if (cond.next < 0) {
                return ce32;
            }
            cond = getConditionalCE32(cond.next);
        }
        if (!-assertionsDisabled) {
            i = head.defaultCE32;
            if (r0 != 1) {
                obj = 1;
            } else {
                obj = null;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        index = addContextTrie(head.defaultCE32, prefixBuilder);
        if (index <= 524287) {
            return Collation.makeCE32FromTagAndIndex(8, index);
        }
        throw new IndexOutOfBoundsException("too many context-sensitive mappings");
    }

    protected int addContextTrie(int defaultCE32, CharsTrieBuilder trieBuilder) {
        StringBuilder context = new StringBuilder();
        context.append((char) (defaultCE32 >> 16)).append((char) defaultCE32);
        context.append(trieBuilder.buildCharSequence(Option.SMALL));
        int index = this.contexts.indexOf(context.toString());
        if (index >= 0) {
            return index;
        }
        index = this.contexts.length();
        this.contexts.append(context);
        return index;
    }

    protected void buildFastLatinTable(CollationData data) {
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

    protected int getCEs(CharSequence s, int start, long[] ces, int cesLength) {
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
            return i + Hangul.JAMO_L_BASE;
        }
        i -= 19;
        if (i < 21) {
            return i + Hangul.JAMO_V_BASE;
        }
        return (i - 21) + 4520;
    }

    protected final boolean isMutable() {
        return (this.trie == null || this.unsafeBackwardSet == null || this.unsafeBackwardSet.isFrozen()) ? -assertionsDisabled : true;
    }
}
