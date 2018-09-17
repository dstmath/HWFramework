package android.icu.impl.coll;

import android.icu.impl.Norm2AllModes;
import android.icu.impl.Normalizer2Impl;
import android.icu.impl.Normalizer2Impl.Hangul;
import android.icu.text.CanonicalIterator;
import android.icu.text.Normalizer2;
import android.icu.text.UnicodeSet;
import android.icu.text.UnicodeSetIterator;
import android.icu.util.ULocale;
import com.android.dex.DexFormat;
import dalvik.bytecode.Opcodes;
import java.text.ParseException;
import libcore.icu.ICU;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

public final class CollationBuilder extends Sink {
    private static final /* synthetic */ int[] -android-icu-impl-coll-CollationRuleParser$PositionSwitchesValues = null;
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final UnicodeSet COMPOSITES = null;
    private static final boolean DEBUG = false;
    private static final int HAS_BEFORE2 = 64;
    private static final int HAS_BEFORE3 = 32;
    private static final int IS_TAILORED = 8;
    private static final int MAX_INDEX = 1048575;
    private CollationTailoring base;
    private CollationData baseData;
    private long[] ces;
    private int cesLength;
    private CollationDataBuilder dataBuilder;
    private boolean fastLatinEnabled;
    private Normalizer2 fcd;
    private Normalizer2Impl nfcImpl;
    private Normalizer2 nfd;
    private UVector64 nodes;
    private UnicodeSet optimizeSet;
    private CollationRootElements rootElements;
    private UVector32 rootPrimaryIndexes;
    private long variableTop;

    private static final class BundleImporter implements Importer {
        BundleImporter() {
        }

        public String getRules(String localeID, String collationType) {
            return CollationLoader.loadRules(new ULocale(localeID), collationType);
        }
    }

    private static final class CEFinalizer implements CEModifier {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        private long[] finalCEs;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationBuilder.CEFinalizer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.coll.CollationBuilder.CEFinalizer.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationBuilder.CEFinalizer.<clinit>():void");
        }

        CEFinalizer(long[] ces) {
            this.finalCEs = ces;
        }

        public long modifyCE32(int ce32) {
            if (!-assertionsDisabled) {
                if ((Collation.isSpecialCE32(ce32) ? null : 1) == null) {
                    throw new AssertionError();
                }
            }
            if (CollationBuilder.isTempCE32(ce32)) {
                return this.finalCEs[CollationBuilder.indexFromTempCE32(ce32)] | ((long) ((ce32 & Opcodes.OP_AND_LONG_2ADDR) << CollationBuilder.IS_TAILORED));
            }
            return Collation.NO_CE;
        }

        public long modifyCE(long ce) {
            if (CollationBuilder.isTempCE(ce)) {
                return this.finalCEs[CollationBuilder.indexFromTempCE(ce)] | (49152 & ce);
            }
            return Collation.NO_CE;
        }
    }

    private static /* synthetic */ int[] -getandroid-icu-impl-coll-CollationRuleParser$PositionSwitchesValues() {
        if (-android-icu-impl-coll-CollationRuleParser$PositionSwitchesValues != null) {
            return -android-icu-impl-coll-CollationRuleParser$PositionSwitchesValues;
        }
        int[] iArr = new int[Position.values().length];
        try {
            iArr[Position.FIRST_IMPLICIT.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Position.FIRST_PRIMARY_IGNORABLE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Position.FIRST_REGULAR.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Position.FIRST_SECONDARY_IGNORABLE.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Position.FIRST_TERTIARY_IGNORABLE.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Position.FIRST_TRAILING.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Position.FIRST_VARIABLE.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Position.LAST_IMPLICIT.ordinal()] = IS_TAILORED;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Position.LAST_PRIMARY_IGNORABLE.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Position.LAST_REGULAR.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Position.LAST_SECONDARY_IGNORABLE.ordinal()] = 11;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Position.LAST_TERTIARY_IGNORABLE.ordinal()] = 12;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Position.LAST_TRAILING.ordinal()] = 13;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Position.LAST_VARIABLE.ordinal()] = 14;
        } catch (NoSuchFieldError e14) {
        }
        -android-icu-impl-coll-CollationRuleParser$PositionSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationBuilder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.coll.CollationBuilder.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationBuilder.<clinit>():void");
    }

    private static final int binarySearchForRootPrimaryNode(int[] r1, int r2, long[] r3, long r4) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationBuilder.binarySearchForRootPrimaryNode(int[], int, long[], long):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationBuilder.binarySearchForRootPrimaryNode(int[], int, long[], long):int");
    }

    private int findOrInsertNodeForPrimary(long r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationBuilder.findOrInsertNodeForPrimary(long):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationBuilder.findOrInsertNodeForPrimary(long):int");
    }

    public CollationBuilder(CollationTailoring b) {
        this.optimizeSet = new UnicodeSet();
        this.ces = new long[31];
        this.nfd = Normalizer2.getNFDInstance();
        this.fcd = Norm2AllModes.getFCDNormalizer2();
        this.nfcImpl = Norm2AllModes.getNFCInstance().impl;
        this.base = b;
        this.baseData = b.data;
        this.rootElements = new CollationRootElements(b.data.rootElements);
        this.variableTop = 0;
        this.dataBuilder = new CollationDataBuilder();
        this.fastLatinEnabled = true;
        this.cesLength = 0;
        this.rootPrimaryIndexes = new UVector32();
        this.nodes = new UVector64();
        this.nfcImpl.ensureCanonIterData();
        this.dataBuilder.initForTailoring(this.baseData);
    }

    public CollationTailoring parseAndBuild(String ruleString) throws ParseException {
        if (this.baseData.rootElements == null) {
            throw new UnsupportedOperationException("missing root elements data, tailoring not supported");
        }
        CollationTailoring tailoring = new CollationTailoring(this.base.settings);
        CollationRuleParser parser = new CollationRuleParser(this.baseData);
        this.variableTop = ((CollationSettings) this.base.settings.readOnly()).variableTop;
        parser.setSink(this);
        parser.setImporter(new BundleImporter());
        CollationSettings ownedSettings = (CollationSettings) tailoring.settings.copyOnWrite();
        parser.parse(ruleString, ownedSettings);
        if (this.dataBuilder.hasMappings()) {
            makeTailoredCEs();
            closeOverComposites();
            finalizeCEs();
            this.optimizeSet.add(0, Opcodes.OP_NEG_FLOAT);
            this.optimizeSet.add(Opcodes.OP_AND_LONG_2ADDR, Opcodes.OP_CONST_CLASS_JUMBO);
            this.optimizeSet.remove(Hangul.HANGUL_BASE, Hangul.HANGUL_END);
            this.dataBuilder.optimize(this.optimizeSet);
            tailoring.ensureOwnedData();
            if (this.fastLatinEnabled) {
                this.dataBuilder.enableFastLatin();
            }
            this.dataBuilder.build(tailoring.ownedData);
            this.dataBuilder = null;
        } else {
            tailoring.data = this.baseData;
        }
        ownedSettings.fastLatinOptions = CollationFastLatin.getOptions(tailoring.data, ownedSettings, ownedSettings.fastLatinPrimaries);
        tailoring.setRules(ruleString);
        tailoring.setVersion(this.base.version, 0);
        return tailoring;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void addReset(int strength, CharSequence str) {
        if (!-assertionsDisabled) {
            if ((str.length() != 0 ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        if (str.charAt(0) == '\ufffe') {
            this.ces[0] = getSpecialResetPosition(str);
            this.cesLength = 1;
            if (!-assertionsDisabled) {
                if (((this.ces[0] & 49344) == 0 ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
        }
        this.cesLength = this.dataBuilder.getCEs(this.nfd.normalize(str), this.ces, 0);
        if (this.cesLength > 31) {
            throw new IllegalArgumentException("reset position maps to too many collation elements (more than 31)");
        }
        if (strength != 15) {
            if (!-assertionsDisabled) {
                Object obj = (strength < 0 || strength > 2) ? null : 1;
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            int index = findOrInsertNodeForCEs(strength);
            long node = this.nodes.elementAti(index);
            while (strengthFromNode(node) > strength) {
                index = previousIndexFromNode(node);
                node = this.nodes.elementAti(index);
            }
            if (strengthFromNode(node) == strength && isTailoredNode(node)) {
                index = previousIndexFromNode(node);
            } else if (strength == 0) {
                long p = weight32FromNode(node);
                if (p != 0) {
                    if (p > this.rootElements.getFirstPrimary()) {
                        if (p != 4278321664L) {
                            index = findOrInsertNodeForPrimary(this.rootElements.getPrimaryBefore(p, this.baseData.isCompressiblePrimary(p)));
                            while (true) {
                                int nextIndex = nextIndexFromNode(this.nodes.elementAti(index));
                                if (nextIndex == 0) {
                                    break;
                                }
                                index = nextIndex;
                            }
                        } else {
                            throw new UnsupportedOperationException("reset primary-before [first trailing] not supported");
                        }
                    }
                    throw new UnsupportedOperationException("reset primary-before first non-ignorable not supported");
                }
                throw new UnsupportedOperationException("reset primary-before ignorable not possible");
            } else {
                index = findCommonNode(index, 1);
                if (strength >= 2) {
                    index = findCommonNode(index, 2);
                }
                node = this.nodes.elementAti(index);
                if (strengthFromNode(node) == strength) {
                    int weight16 = weight16FromNode(node);
                    if (weight16 == 0) {
                        String str2;
                        if (strength == 1) {
                            str2 = "reset secondary-before secondary ignorable not possible";
                        } else {
                            str2 = "reset tertiary-before completely ignorable not possible";
                        }
                        throw new UnsupportedOperationException(str2);
                    }
                    if (!-assertionsDisabled) {
                        if ((weight16 > NodeFilter.SHOW_DOCUMENT ? 1 : null) == null) {
                            throw new AssertionError();
                        }
                    }
                    weight16 = getWeight16Before(index, node, strength);
                    int previousIndex = previousIndexFromNode(node);
                    int i = previousIndex;
                    while (true) {
                        node = this.nodes.elementAti(i);
                        int previousStrength = strengthFromNode(node);
                        if (previousStrength >= strength) {
                            if (previousStrength == strength && !isTailoredNode(node)) {
                                break;
                            }
                            i = previousIndexFromNode(node);
                        } else {
                            break;
                        }
                        if (previousWeight16 != weight16) {
                            index = previousIndex;
                        } else {
                            index = insertNodeBetween(previousIndex, index, nodeFromWeight16(weight16) | nodeFromStrength(strength));
                        }
                    }
                    int previousWeight16 = weight16FromNode(node);
                    if (previousWeight16 != weight16) {
                        index = insertNodeBetween(previousIndex, index, nodeFromWeight16(weight16) | nodeFromStrength(strength));
                    } else {
                        index = previousIndex;
                    }
                } else {
                    index = findOrInsertWeakNode(index, getWeight16Before(index, node, strength), strength);
                }
                strength = ceStrength(this.ces[this.cesLength - 1]);
            }
            this.ces[this.cesLength - 1] = tempCEFromIndexAndStrength(index, strength);
        }
    }

    private int getWeight16Before(int index, long node, int level) {
        int t;
        Object obj = 1;
        if (!-assertionsDisabled) {
            Object obj2;
            if (strengthFromNode(node) < level || !isTailoredNode(node)) {
                int i = 1;
            } else {
                obj2 = null;
            }
            if (obj2 == null) {
                throw new AssertionError();
            }
        }
        if (strengthFromNode(node) == 2) {
            t = weight16FromNode(node);
        } else {
            t = Collation.COMMON_WEIGHT16;
        }
        while (strengthFromNode(node) > 1) {
            node = this.nodes.elementAti(previousIndexFromNode(node));
        }
        if (isTailoredNode(node)) {
            return NodeFilter.SHOW_DOCUMENT;
        }
        int s;
        if (strengthFromNode(node) == 1) {
            s = weight16FromNode(node);
        } else {
            s = Collation.COMMON_WEIGHT16;
        }
        while (strengthFromNode(node) > 0) {
            node = this.nodes.elementAti(previousIndexFromNode(node));
        }
        if (isTailoredNode(node)) {
            return NodeFilter.SHOW_DOCUMENT;
        }
        int weight16;
        long p = weight32FromNode(node);
        if (level == 1) {
            weight16 = this.rootElements.getSecondaryBefore(p, s);
        } else {
            weight16 = this.rootElements.getTertiaryBefore(p, s, t);
            if (!-assertionsDisabled) {
                if ((weight16 & -16192) != 0) {
                    obj = null;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
        }
        return weight16;
    }

    private long getSpecialResetPosition(CharSequence str) {
        long ce;
        long node;
        int index;
        if (!-assertionsDisabled) {
            if ((str.length() == 2 ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        int strength = 0;
        boolean isBoundary = DEBUG;
        Position pos = CollationRuleParser.POSITION_VALUES[str.charAt(1) - 10240];
        switch (-getandroid-icu-impl-coll-CollationRuleParser$PositionSwitchesValues()[pos.ordinal()]) {
            case NodeFilter.SHOW_ELEMENT /*1*/:
                ce = this.baseData.getSingleCE(19968);
                break;
            case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                node = this.nodes.elementAti(findOrInsertNodeForRootCE(0, 1));
                do {
                    index = nextIndexFromNode(node);
                    if (index != 0) {
                        node = this.nodes.elementAti(index);
                        strength = strengthFromNode(node);
                        if (strength < 1) {
                        }
                    }
                    ce = this.rootElements.getFirstSecondaryCE();
                    strength = 1;
                    break;
                } while (strength != 1);
                if (isTailoredNode(node)) {
                    if (nodeHasBefore3(node)) {
                        index = nextIndexFromNode(this.nodes.elementAti(nextIndexFromNode(node)));
                        if (!-assertionsDisabled) {
                            if (!isTailoredNode(this.nodes.elementAti(index))) {
                                throw new AssertionError();
                            }
                        }
                    }
                    return tempCEFromIndexAndStrength(index, 1);
                }
                ce = this.rootElements.getFirstSecondaryCE();
                strength = 1;
            case XmlPullParser.END_TAG /*3*/:
                ce = this.rootElements.firstCEWithPrimaryAtLeast(this.variableTop + 1);
                isBoundary = true;
                break;
            case NodeFilter.SHOW_TEXT /*4*/:
                index = nextIndexFromNode(this.nodes.elementAti(findOrInsertNodeForRootCE(0, 2)));
                if (index != 0) {
                    node = this.nodes.elementAti(index);
                    if (!-assertionsDisabled) {
                        if ((strengthFromNode(node) <= 2 ? 1 : null) == null) {
                            throw new AssertionError();
                        }
                    }
                    if (isTailoredNode(node) && strengthFromNode(node) == 2) {
                        return tempCEFromIndexAndStrength(index, 2);
                    }
                }
                return this.rootElements.getFirstTertiaryCE();
            case XmlPullParser.CDSECT /*5*/:
                return 0;
            case XmlPullParser.ENTITY_REF /*6*/:
                ce = Collation.makeCE(4278321664L);
                isBoundary = true;
                break;
            case XmlPullParser.IGNORABLE_WHITESPACE /*7*/:
                ce = this.rootElements.getFirstPrimaryCE();
                isBoundary = true;
                break;
            case IS_TAILORED /*8*/:
                throw new UnsupportedOperationException("reset to [last implicit] not supported");
            case XmlPullParser.COMMENT /*9*/:
                ce = this.rootElements.getLastSecondaryCE();
                strength = 1;
                break;
            case XmlPullParser.DOCDECL /*10*/:
                ce = this.rootElements.firstCEWithPrimaryAtLeast(this.baseData.getFirstPrimaryForGroup(17));
                break;
            case ICU.U_TRUNCATED_CHAR_FOUND /*11*/:
                ce = this.rootElements.getLastTertiaryCE();
                strength = 2;
                break;
            case ICU.U_ILLEGAL_CHAR_FOUND /*12*/:
                return 0;
            case Opcodes.OP_MOVE_EXCEPTION /*13*/:
                throw new IllegalArgumentException("LDML forbids tailoring to U+FFFF");
            case Opcodes.OP_RETURN_VOID /*14*/:
                ce = this.rootElements.lastCEWithPrimaryBefore(this.variableTop + 1);
                break;
            default:
                if (-assertionsDisabled) {
                    return 0;
                }
                throw new AssertionError();
        }
        index = findOrInsertNodeForRootCE(ce, strength);
        node = this.nodes.elementAti(index);
        if ((pos.ordinal() & 1) == 0) {
            if (!nodeHasAnyBefore(node) && isBoundary) {
                index = nextIndexFromNode(node);
                if (index != 0) {
                    node = this.nodes.elementAti(index);
                    if (-assertionsDisabled || isTailoredNode(node)) {
                        ce = tempCEFromIndexAndStrength(index, strength);
                    } else {
                        throw new AssertionError();
                    }
                }
                if (!-assertionsDisabled) {
                    if ((strength == 0 ? 1 : null) == null) {
                        throw new AssertionError();
                    }
                }
                long p = ce >>> HAS_BEFORE3;
                ce = Collation.makeCE(this.rootElements.getPrimaryAfter(p, this.rootElements.findPrimary(p), this.baseData.isCompressiblePrimary(p)));
                index = findOrInsertNodeForRootCE(ce, 0);
                node = this.nodes.elementAti(index);
            }
            if (nodeHasAnyBefore(node)) {
                if (nodeHasBefore2(node)) {
                    index = nextIndexFromNode(this.nodes.elementAti(nextIndexFromNode(node)));
                    node = this.nodes.elementAti(index);
                }
                if (nodeHasBefore3(node)) {
                    index = nextIndexFromNode(this.nodes.elementAti(nextIndexFromNode(node)));
                }
                if (!-assertionsDisabled) {
                    if (!isTailoredNode(this.nodes.elementAti(index))) {
                        throw new AssertionError();
                    }
                }
                ce = tempCEFromIndexAndStrength(index, strength);
            }
        } else {
            while (true) {
                int nextIndex = nextIndexFromNode(node);
                if (nextIndex != 0) {
                    long nextNode = this.nodes.elementAti(nextIndex);
                    if (strengthFromNode(nextNode) >= strength) {
                        index = nextIndex;
                        node = nextNode;
                    }
                }
                if (isTailoredNode(node)) {
                    ce = tempCEFromIndexAndStrength(index, strength);
                }
            }
        }
        return ce;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void addRelation(int strength, CharSequence prefix, CharSequence str, CharSequence extension) {
        String nfdPrefix;
        if (prefix.length() == 0) {
            nfdPrefix = XmlPullParser.NO_NAMESPACE;
        } else {
            nfdPrefix = this.nfd.normalize(prefix);
        }
        String nfdString = this.nfd.normalize(str);
        int nfdLength = nfdString.length();
        if (nfdLength >= 2) {
            char c = nfdString.charAt(0);
            if (Hangul.isJamoL(c) || Hangul.isJamoV(c)) {
                throw new UnsupportedOperationException("contractions starting with conjoining Jamo L or V not supported");
            }
            c = nfdString.charAt(nfdLength - 1);
            if (!Hangul.isJamoL(c)) {
                if (Hangul.isJamoV(c)) {
                }
            }
            throw new UnsupportedOperationException("contractions ending with conjoining Jamo L or L+V not supported");
        }
        if (strength != 15) {
            int index = findOrInsertNodeForCEs(strength);
            if (!-assertionsDisabled) {
                if ((this.cesLength > 0 ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            long ce = this.ces[this.cesLength - 1];
            if (strength == 0 && !isTempCE(ce) && (ce >>> HAS_BEFORE3) == 0) {
                throw new UnsupportedOperationException("tailoring primary after ignorables not supported");
            } else if (strength == 3 && ce == 0) {
                throw new UnsupportedOperationException("tailoring quaternary after tertiary ignorables not supported");
            } else {
                index = insertTailoredNodeAfter(index, strength);
                int tempStrength = ceStrength(ce);
                if (strength < tempStrength) {
                    tempStrength = strength;
                }
                this.ces[this.cesLength - 1] = tempCEFromIndexAndStrength(index, tempStrength);
            }
        }
        setCaseBits(nfdString);
        int cesLengthBeforeExtension = this.cesLength;
        if (extension.length() != 0) {
            this.cesLength = this.dataBuilder.getCEs(this.nfd.normalize(extension), this.ces, this.cesLength);
            if (this.cesLength > 31) {
                throw new IllegalArgumentException("extension string adds too many collation elements (more than 31 total)");
            }
        }
        int ce32 = -1;
        if (!((nfdPrefix.contentEquals(prefix) && nfdString.contentEquals(str)) || ignorePrefix(prefix) || ignoreString(str))) {
            ce32 = addIfDifferent(prefix, str, this.ces, this.cesLength, -1);
        }
        addWithClosure(nfdPrefix, nfdString, this.ces, this.cesLength, ce32);
        this.cesLength = cesLengthBeforeExtension;
    }

    private int findOrInsertNodeForCEs(int strength) {
        long ce;
        if (!-assertionsDisabled) {
            int i = (strength < 0 || strength > 3) ? 0 : 1;
            if (i == 0) {
                throw new AssertionError();
            }
        }
        while (this.cesLength != 0) {
            ce = this.ces[this.cesLength - 1];
            if (ceStrength(ce) <= strength) {
                break;
            }
            this.cesLength--;
        }
        this.ces[0] = 0;
        ce = 0;
        this.cesLength = 1;
        if (isTempCE(ce)) {
            return indexFromTempCE(ce);
        }
        if (((int) (ce >>> 56)) != SCSU.KATAKANAINDEX) {
            return findOrInsertNodeForRootCE(ce, strength);
        }
        throw new UnsupportedOperationException("tailoring relative to an unassigned code point not supported");
    }

    private int findOrInsertNodeForRootCE(long ce, int strength) {
        int i = 0;
        if (!-assertionsDisabled) {
            if ((((int) (ce >>> 56)) != SCSU.KATAKANAINDEX ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        if (!-assertionsDisabled) {
            if ((192 & ce) == 0) {
                i = 1;
            }
            if (i == 0) {
                throw new AssertionError();
            }
        }
        int index = findOrInsertNodeForPrimary(ce >>> HAS_BEFORE3);
        if (strength < 1) {
            return index;
        }
        int lower32 = (int) ce;
        index = findOrInsertWeakNode(index, lower32 >>> 16, 1);
        if (strength >= 2) {
            return findOrInsertWeakNode(index, lower32 & Collation.ONLY_TERTIARY_MASK, 2);
        }
        return index;
    }

    private int findOrInsertWeakNode(int index, int weight16, int level) {
        Object obj;
        if (!-assertionsDisabled) {
            obj = (index < 0 || index >= this.nodes.size()) ? null : 1;
            if (obj == null) {
                throw new AssertionError();
            }
        }
        if (!-assertionsDisabled) {
            obj = (1 > level || level > 2) ? null : 1;
            if (obj == null) {
                throw new AssertionError();
            }
        }
        if (weight16 == Collation.COMMON_WEIGHT16) {
            return findCommonNode(index, level);
        }
        int nextIndex;
        long node = this.nodes.elementAti(index);
        if (!-assertionsDisabled) {
            if ((strengthFromNode(node) < level ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        if (weight16 != 0 && weight16 < Collation.COMMON_WEIGHT16) {
            int hasThisLevelBefore = level == 1 ? HAS_BEFORE2 : HAS_BEFORE3;
            if ((((long) hasThisLevelBefore) & node) == 0) {
                long commonNode = nodeFromWeight16(Collation.COMMON_WEIGHT16) | nodeFromStrength(level);
                if (level == 1) {
                    commonNode |= 32 & node;
                    node &= -33;
                }
                this.nodes.setElementAt(((long) hasThisLevelBefore) | node, index);
                nextIndex = nextIndexFromNode(node);
                index = insertNodeBetween(index, nextIndex, nodeFromWeight16(weight16) | nodeFromStrength(level));
                insertNodeBetween(index, nextIndex, commonNode);
                return index;
            }
        }
        while (true) {
            nextIndex = nextIndexFromNode(node);
            if (nextIndex == 0) {
                break;
            }
            node = this.nodes.elementAti(nextIndex);
            int nextStrength = strengthFromNode(node);
            if (nextStrength <= level) {
                if (nextStrength < level) {
                    break;
                } else if (isTailoredNode(node)) {
                    continue;
                } else {
                    int nextWeight16 = weight16FromNode(node);
                    if (nextWeight16 == weight16) {
                        return nextIndex;
                    }
                    if (nextWeight16 > weight16) {
                        break;
                    }
                }
            }
            index = nextIndex;
        }
        return insertNodeBetween(index, nextIndex, nodeFromWeight16(weight16) | nodeFromStrength(level));
    }

    private int insertTailoredNodeAfter(int index, int strength) {
        int nextIndex;
        int i = 0;
        if (!-assertionsDisabled) {
            if (index >= 0 && index < this.nodes.size()) {
                i = 1;
            }
            if (i == 0) {
                throw new AssertionError();
            }
        }
        if (strength >= 1) {
            index = findCommonNode(index, 1);
            if (strength >= 2) {
                index = findCommonNode(index, 2);
            }
        }
        long node = this.nodes.elementAti(index);
        while (true) {
            nextIndex = nextIndexFromNode(node);
            if (nextIndex == 0) {
                break;
            }
            node = this.nodes.elementAti(nextIndex);
            if (strengthFromNode(node) <= strength) {
                break;
            }
            index = nextIndex;
        }
        return insertNodeBetween(index, nextIndex, 8 | nodeFromStrength(strength));
    }

    private int insertNodeBetween(int index, int nextIndex, long node) {
        Object obj = 1;
        if (!-assertionsDisabled) {
            if ((previousIndexFromNode(node) == 0 ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        if (!-assertionsDisabled) {
            if ((nextIndexFromNode(node) == 0 ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        if (!-assertionsDisabled) {
            if (nextIndexFromNode(this.nodes.elementAti(index)) != nextIndex) {
                obj = null;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        int newIndex = this.nodes.size();
        this.nodes.addElement(node | (nodeFromPreviousIndex(index) | nodeFromNextIndex(nextIndex)));
        this.nodes.setElementAt(changeNodeNextIndex(this.nodes.elementAti(index), newIndex), index);
        if (nextIndex != 0) {
            this.nodes.setElementAt(changeNodePreviousIndex(this.nodes.elementAti(nextIndex), newIndex), nextIndex);
        }
        return newIndex;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int findCommonNode(int index, int strength) {
        Object obj;
        Object obj2 = 1;
        if (!-assertionsDisabled) {
            obj = (1 > strength || strength > 2) ? null : 1;
            if (obj == null) {
                throw new AssertionError();
            }
        }
        long node = this.nodes.elementAti(index);
        if (strengthFromNode(node) >= strength) {
            return index;
        }
        if (!strength != 1 ? nodeHasBefore2(node) : nodeHasBefore3(node)) {
            return index;
        }
        node = this.nodes.elementAti(nextIndexFromNode(node));
        if (!-assertionsDisabled) {
            obj = (isTailoredNode(node) || strengthFromNode(node) != strength) ? null : weight16FromNode(node) < Collation.COMMON_WEIGHT16 ? 1 : null;
            if (obj == null) {
                throw new AssertionError();
            }
        }
        while (true) {
            index = nextIndexFromNode(node);
            node = this.nodes.elementAti(index);
            if (!-assertionsDisabled) {
                if ((strengthFromNode(node) >= strength ? 1 : null) == null) {
                    break;
                }
            }
            if (!isTailoredNode(node) && strengthFromNode(node) <= strength && weight16FromNode(node) >= Collation.COMMON_WEIGHT16) {
                break;
            }
        }
        if (!-assertionsDisabled) {
            if (weight16FromNode(node) != Collation.COMMON_WEIGHT16) {
                obj2 = null;
            }
            if (obj2 == null) {
                throw new AssertionError();
            }
        }
        return index;
    }

    private void setCaseBits(CharSequence nfdString) {
        long ce;
        int numTailoredPrimaries = 0;
        int i = 0;
        while (true) {
            int i2 = this.cesLength;
            if (i >= r0) {
                break;
            }
            if (ceStrength(this.ces[i]) == 0) {
                numTailoredPrimaries++;
            }
            i++;
        }
        if (!-assertionsDisabled) {
            if ((numTailoredPrimaries <= 31 ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        long cases = 0;
        if (numTailoredPrimaries > 0) {
            Object obj;
            CharSequence s = nfdString;
            UTF16CollationIterator baseCEs = new UTF16CollationIterator(this.baseData, DEBUG, nfdString, 0);
            int baseCEsLength = baseCEs.fetchCEs() - 1;
            if (!-assertionsDisabled) {
                obj = (baseCEsLength < 0 || baseCEs.getCE(baseCEsLength) != Collation.NO_CE) ? null : 1;
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            int lastCase = 0;
            int numBasePrimaries = 0;
            for (i = 0; i < baseCEsLength; i++) {
                ce = baseCEs.getCE(i);
                if ((ce >>> HAS_BEFORE3) != 0) {
                    numBasePrimaries++;
                    int c = (((int) ce) >> 14) & 3;
                    if (!-assertionsDisabled) {
                        obj = (c == 0 || c == 2) ? 1 : null;
                        if (obj == null) {
                            throw new AssertionError();
                        }
                    }
                    if (numBasePrimaries < numTailoredPrimaries) {
                        cases |= ((long) c) << ((numBasePrimaries - 1) * 2);
                    } else if (numBasePrimaries == numTailoredPrimaries) {
                        lastCase = c;
                    } else if (c != lastCase) {
                        lastCase = 1;
                        break;
                    }
                }
            }
            if (numBasePrimaries >= numTailoredPrimaries) {
                cases |= ((long) lastCase) << ((numTailoredPrimaries - 1) * 2);
            }
        }
        i = 0;
        while (true) {
            i2 = this.cesLength;
            if (i < r0) {
                ce = this.ces[i] & -49153;
                int strength = ceStrength(ce);
                if (strength == 0) {
                    ce |= (3 & cases) << 14;
                    cases >>>= 2;
                } else if (strength == 2) {
                    ce |= 32768;
                }
                this.ces[i] = ce;
                i++;
            } else {
                return;
            }
        }
    }

    void suppressContractions(UnicodeSet set) {
        this.dataBuilder.suppressContractions(set);
    }

    void optimize(UnicodeSet set) {
        this.optimizeSet.addAll(set);
    }

    private int addWithClosure(CharSequence nfdPrefix, CharSequence nfdString, long[] newCEs, int newCEsLength, int ce32) {
        ce32 = addOnlyClosure(nfdPrefix, nfdString, newCEs, newCEsLength, addIfDifferent(nfdPrefix, nfdString, newCEs, newCEsLength, ce32));
        addTailComposites(nfdPrefix, nfdString);
        return ce32;
    }

    private int addOnlyClosure(CharSequence nfdPrefix, CharSequence nfdString, long[] newCEs, int newCEsLength, int ce32) {
        CanonicalIterator stringIter;
        String prefix;
        String str;
        if (nfdPrefix.length() != 0) {
            CanonicalIterator prefixIter = new CanonicalIterator(nfdPrefix.toString());
            stringIter = new CanonicalIterator(nfdString.toString());
            while (true) {
                prefix = prefixIter.next();
                if (prefix == null) {
                    break;
                } else if (!ignorePrefix(prefix)) {
                    boolean samePrefix = prefix.contentEquals(nfdPrefix);
                    while (true) {
                        str = stringIter.next();
                        if (str == null) {
                            break;
                        } else if (!(ignoreString(str) || (samePrefix && str.contentEquals(nfdString)))) {
                            ce32 = addIfDifferent(prefix, str, newCEs, newCEsLength, ce32);
                        }
                    }
                    stringIter.reset();
                }
            }
        } else {
            stringIter = new CanonicalIterator(nfdString.toString());
            prefix = XmlPullParser.NO_NAMESPACE;
            while (true) {
                str = stringIter.next();
                if (str == null) {
                    break;
                } else if (!(ignoreString(str) || str.contentEquals(nfdString))) {
                    ce32 = addIfDifferent(prefix, str, newCEs, newCEsLength, ce32);
                }
            }
        }
        return ce32;
    }

    private void addTailComposites(CharSequence nfdPrefix, CharSequence nfdString) {
        int indexAfterLastStarter = nfdString.length();
        while (indexAfterLastStarter != 0) {
            int lastStarter = Character.codePointBefore(nfdString, indexAfterLastStarter);
            if (this.nfd.getCombiningClass(lastStarter) != 0) {
                indexAfterLastStarter -= Character.charCount(lastStarter);
            } else if (!Hangul.isJamoL(lastStarter)) {
                UnicodeSet composites = new UnicodeSet();
                if (this.nfcImpl.getCanonStartSet(lastStarter, composites)) {
                    CharSequence newNFDString = new StringBuilder();
                    StringBuilder newString = new StringBuilder();
                    long[] newCEs = new long[31];
                    UnicodeSetIterator iter = new UnicodeSetIterator(composites);
                    while (iter.next()) {
                        if (!-assertionsDisabled) {
                            if ((iter.codepoint != UnicodeSetIterator.IS_STRING ? 1 : null) == null) {
                                throw new AssertionError();
                            }
                        }
                        int composite = iter.codepoint;
                        if (mergeCompositeIntoString(nfdString, indexAfterLastStarter, composite, this.nfd.getDecomposition(composite), newNFDString, newString)) {
                            int newCEsLength = this.dataBuilder.getCEs(nfdPrefix, newNFDString, newCEs, 0);
                            if (newCEsLength <= 31) {
                                int ce32 = addIfDifferent(nfdPrefix, newString, newCEs, newCEsLength, -1);
                                if (ce32 != -1) {
                                    addOnlyClosure(nfdPrefix, newNFDString, newCEs, newCEsLength, ce32);
                                }
                            }
                        }
                    }
                    return;
                }
                return;
            } else {
                return;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean mergeCompositeIntoString(CharSequence nfdString, int indexAfterLastStarter, int composite, CharSequence decomp, StringBuilder newNFDString, StringBuilder newString) {
        if (!-assertionsDisabled) {
            if ((Character.codePointBefore(nfdString, indexAfterLastStarter) == Character.codePointAt(decomp, 0) ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        int lastStarterLength = Character.offsetByCodePoints(decomp, 0, 1);
        if (lastStarterLength == decomp.length()) {
            return DEBUG;
        }
        if (equalSubSequences(nfdString, indexAfterLastStarter, decomp, lastStarterLength)) {
            return DEBUG;
        }
        newNFDString.setLength(0);
        newNFDString.append(nfdString, 0, indexAfterLastStarter);
        newString.setLength(0);
        newString.append(nfdString, 0, indexAfterLastStarter - lastStarterLength).appendCodePoint(composite);
        int sourceIndex = indexAfterLastStarter;
        int decompIndex = lastStarterLength;
        int sourceChar = -1;
        int sourceCC = 0;
        int decompCC = 0;
        while (true) {
            if (sourceChar < 0) {
                if (sourceIndex < nfdString.length()) {
                    sourceChar = Character.codePointAt(nfdString, sourceIndex);
                    sourceCC = this.nfd.getCombiningClass(sourceChar);
                    if (!-assertionsDisabled) {
                        if ((sourceCC != 0 ? 1 : null) == null) {
                            break;
                        }
                    }
                }
                break;
            }
            if (decompIndex >= decomp.length()) {
                break;
            }
            int decompChar = Character.codePointAt(decomp, decompIndex);
            decompCC = this.nfd.getCombiningClass(decompChar);
            if (decompCC == 0) {
                return DEBUG;
            }
            if (sourceCC < decompCC) {
                return DEBUG;
            }
            if (decompCC < sourceCC) {
                newNFDString.appendCodePoint(decompChar);
                decompIndex += Character.charCount(decompChar);
            } else if (decompChar != sourceChar) {
                return DEBUG;
            } else {
                newNFDString.appendCodePoint(decompChar);
                decompIndex += Character.charCount(decompChar);
                sourceIndex += Character.charCount(decompChar);
                sourceChar = -1;
            }
        }
        if (sourceChar >= 0) {
            if (sourceCC < decompCC) {
                return DEBUG;
            }
            newNFDString.append(nfdString, sourceIndex, nfdString.length());
            newString.append(nfdString, sourceIndex, nfdString.length());
        } else if (decompIndex < decomp.length()) {
            newNFDString.append(decomp, decompIndex, decomp.length());
        }
        if (!-assertionsDisabled && !this.nfd.isNormalized(newNFDString)) {
            throw new AssertionError();
        } else if (!-assertionsDisabled && !this.fcd.isNormalized(newString)) {
            throw new AssertionError();
        } else if (-assertionsDisabled || this.nfd.normalize(newString).equals(newNFDString.toString())) {
            return true;
        } else {
            throw new AssertionError();
        }
    }

    private boolean equalSubSequences(CharSequence left, int leftStart, CharSequence right, int rightStart) {
        int leftLength = left.length();
        if (leftLength - leftStart != right.length() - rightStart) {
            return DEBUG;
        }
        int leftStart2;
        int rightStart2;
        do {
            rightStart2 = rightStart;
            leftStart2 = leftStart;
            if (leftStart2 >= leftLength) {
                return true;
            }
            leftStart = leftStart2 + 1;
            rightStart = rightStart2 + 1;
        } while (left.charAt(leftStart2) == right.charAt(rightStart2));
        return DEBUG;
    }

    private boolean ignorePrefix(CharSequence s) {
        return isFCD(s) ? DEBUG : true;
    }

    private boolean ignoreString(CharSequence s) {
        return isFCD(s) ? Hangul.isHangul(s.charAt(0)) : true;
    }

    private boolean isFCD(CharSequence s) {
        return this.fcd.isNormalized(s);
    }

    private void closeOverComposites() {
        String prefix = XmlPullParser.NO_NAMESPACE;
        UnicodeSetIterator iter = new UnicodeSetIterator(COMPOSITES);
        while (iter.next()) {
            if (!-assertionsDisabled) {
                if ((iter.codepoint != UnicodeSetIterator.IS_STRING ? 1 : 0) == 0) {
                    throw new AssertionError();
                }
            }
            this.cesLength = this.dataBuilder.getCEs(this.nfd.getDecomposition(iter.codepoint), this.ces, 0);
            if (this.cesLength <= 31) {
                addIfDifferent(prefix, iter.getString(), this.ces, this.cesLength, -1);
            }
        }
    }

    private int addIfDifferent(CharSequence prefix, CharSequence str, long[] newCEs, int newCEsLength, int ce32) {
        long[] oldCEs = new long[31];
        if (!sameCEs(newCEs, newCEsLength, oldCEs, this.dataBuilder.getCEs(prefix, str, oldCEs, 0))) {
            if (ce32 == -1) {
                ce32 = this.dataBuilder.encodeCEs(newCEs, newCEsLength);
            }
            this.dataBuilder.addCE32(prefix, str, ce32);
        }
        return ce32;
    }

    private static boolean sameCEs(long[] ces1, int ces1Length, long[] ces2, int ces2Length) {
        if (ces1Length != ces2Length) {
            return DEBUG;
        }
        if (!-assertionsDisabled) {
            if (!(ces1Length <= 31 ? true : DEBUG)) {
                throw new AssertionError();
            }
        }
        for (int i = 0; i < ces1Length; i++) {
            if (ces1[i] != ces2[i]) {
                return DEBUG;
            }
        }
        return true;
    }

    private static final int alignWeightRight(int w) {
        if (w != 0) {
            while ((w & Opcodes.OP_CONST_CLASS_JUMBO) == 0) {
                w >>>= IS_TAILORED;
            }
        }
        return w;
    }

    private void makeTailoredCEs() {
        CollationWeights primaries = new CollationWeights();
        CollationWeights secondaries = new CollationWeights();
        CollationWeights tertiaries = new CollationWeights();
        long[] nodesArray = this.nodes.getBuffer();
        for (int rpi = 0; rpi < this.rootPrimaryIndexes.size(); rpi++) {
            long node = nodesArray[this.rootPrimaryIndexes.elementAti(rpi)];
            long p = weight32FromNode(node);
            int s = p == 0 ? 0 : Collation.COMMON_WEIGHT16;
            int t = s;
            int q = 0;
            boolean pIsTailored = DEBUG;
            boolean sIsTailored = DEBUG;
            boolean tIsTailored = DEBUG;
            int pIndex = p == 0 ? 0 : this.rootElements.findPrimary(p);
            int nextIndex = nextIndexFromNode(node);
            while (nextIndex != 0) {
                int i = nextIndex;
                node = nodesArray[nextIndex];
                nextIndex = nextIndexFromNode(node);
                int strength = strengthFromNode(node);
                if (strength != 3) {
                    if (strength != 2) {
                        if (strength == 1) {
                            if (isTailoredNode(node)) {
                                if (!sIsTailored) {
                                    int sLimit;
                                    int sCount = countTailoredNodes(nodesArray, nextIndex, 1) + 1;
                                    if (s == 0) {
                                        s = this.rootElements.getSecondaryBoundary() - 256;
                                        sLimit = (int) (this.rootElements.getFirstSecondaryCE() >> 16);
                                    } else if (!pIsTailored) {
                                        sLimit = this.rootElements.getSecondaryAfter(pIndex, s);
                                    } else if (s == 256) {
                                        sLimit = Collation.COMMON_WEIGHT16;
                                    } else {
                                        if (!-assertionsDisabled) {
                                            if ((s == 1280 ? 1 : null) == null) {
                                                throw new AssertionError();
                                            }
                                        }
                                        sLimit = this.rootElements.getSecondaryBoundary();
                                    }
                                    if (s == 1280) {
                                        s = this.rootElements.getLastCommonSecondary();
                                    }
                                    secondaries.initForSecondary();
                                    if (secondaries.allocWeights((long) s, (long) sLimit, sCount)) {
                                        sIsTailored = true;
                                    } else {
                                        throw new UnsupportedOperationException("secondary tailoring gap too small");
                                    }
                                }
                                s = (int) secondaries.nextWeight();
                                if (!-assertionsDisabled) {
                                    if ((s != -1 ? 1 : null) == null) {
                                        throw new AssertionError();
                                    }
                                }
                            }
                            s = weight16FromNode(node);
                            sIsTailored = DEBUG;
                        } else if (-assertionsDisabled || isTailoredNode(node)) {
                            if (!pIsTailored) {
                                int pCount = countTailoredNodes(nodesArray, nextIndex, 0) + 1;
                                boolean isCompressible = this.baseData.isCompressiblePrimary(p);
                                long pLimit = this.rootElements.getPrimaryAfter(p, pIndex, isCompressible);
                                primaries.initForPrimary(isCompressible);
                                if (primaries.allocWeights(p, pLimit, pCount)) {
                                    pIsTailored = true;
                                } else {
                                    throw new UnsupportedOperationException("primary tailoring gap too small");
                                }
                            }
                            p = primaries.nextWeight();
                            if (!-assertionsDisabled) {
                                if ((p != 4294967295L ? 1 : null) == null) {
                                    throw new AssertionError();
                                }
                            }
                            s = Collation.COMMON_WEIGHT16;
                            sIsTailored = DEBUG;
                        } else {
                            throw new AssertionError();
                        }
                        t = s == 0 ? 0 : Collation.COMMON_WEIGHT16;
                        tIsTailored = DEBUG;
                    } else if (isTailoredNode(node)) {
                        if (!tIsTailored) {
                            int tLimit;
                            int tCount = countTailoredNodes(nodesArray, nextIndex, 2) + 1;
                            if (t == 0) {
                                t = this.rootElements.getTertiaryBoundary() - 256;
                                tLimit = ((int) this.rootElements.getFirstTertiaryCE()) & Collation.ONLY_TERTIARY_MASK;
                            } else if (!pIsTailored && !sIsTailored) {
                                tLimit = this.rootElements.getTertiaryAfter(pIndex, s, t);
                            } else if (t == 256) {
                                tLimit = Collation.COMMON_WEIGHT16;
                            } else {
                                if (!-assertionsDisabled) {
                                    if ((t == 1280 ? 1 : null) == null) {
                                        throw new AssertionError();
                                    }
                                }
                                tLimit = this.rootElements.getTertiaryBoundary();
                            }
                            if (!-assertionsDisabled) {
                                Object obj;
                                if (tLimit == 16384 || (tLimit & -16192) == 0) {
                                    obj = 1;
                                } else {
                                    obj = null;
                                }
                                if (obj == null) {
                                    throw new AssertionError();
                                }
                            }
                            tertiaries.initForTertiary();
                            if (tertiaries.allocWeights((long) t, (long) tLimit, tCount)) {
                                tIsTailored = true;
                            } else {
                                throw new UnsupportedOperationException("tertiary tailoring gap too small");
                            }
                        }
                        t = (int) tertiaries.nextWeight();
                        if (!-assertionsDisabled) {
                            if ((t != -1 ? 1 : null) == null) {
                                throw new AssertionError();
                            }
                        }
                    } else {
                        t = weight16FromNode(node);
                        tIsTailored = DEBUG;
                    }
                    q = 0;
                } else if (!-assertionsDisabled && !isTailoredNode(node)) {
                    throw new AssertionError();
                } else if (q == 3) {
                    throw new UnsupportedOperationException("quaternary tailoring gap too small");
                } else {
                    q++;
                }
                if (isTailoredNode(node)) {
                    nodesArray[i] = Collation.makeCE(p, s, t, q);
                }
            }
        }
    }

    private static int countTailoredNodes(long[] nodesArray, int i, int strength) {
        int count = 0;
        while (i != 0) {
            long node = nodesArray[i];
            if (strengthFromNode(node) < strength) {
                break;
            }
            if (strengthFromNode(node) == strength) {
                if (!isTailoredNode(node)) {
                    break;
                }
                count++;
            }
            i = nextIndexFromNode(node);
        }
        return count;
    }

    private void finalizeCEs() {
        CollationDataBuilder newBuilder = new CollationDataBuilder();
        newBuilder.initForTailoring(this.baseData);
        newBuilder.copyFrom(this.dataBuilder, new CEFinalizer(this.nodes.getBuffer()));
        this.dataBuilder = newBuilder;
    }

    private static long tempCEFromIndexAndStrength(int index, int strength) {
        return ((((((long) (1040384 & index)) << 43) + 4629700417037541376L) + (((long) (index & 8128)) << 42)) + ((long) ((index & 63) << 24))) + ((long) (strength << IS_TAILORED));
    }

    private static int indexFromTempCE(long tempCE) {
        tempCE -= 4629700417037541376L;
        return ((((int) (tempCE >> 43)) & 1040384) | (((int) (tempCE >> 42)) & 8128)) | (((int) (tempCE >> 24)) & 63);
    }

    private static int strengthFromTempCE(long tempCE) {
        return (((int) tempCE) >> IS_TAILORED) & 3;
    }

    private static boolean isTempCE(long ce) {
        int sec = ((int) ce) >>> 24;
        if (6 > sec || sec > 69) {
            return DEBUG;
        }
        return true;
    }

    private static int indexFromTempCE32(int tempCE32) {
        tempCE32 -= 1077937696;
        return (((tempCE32 >> 11) & 1040384) | ((tempCE32 >> 10) & 8128)) | ((tempCE32 >> IS_TAILORED) & 63);
    }

    private static boolean isTempCE32(int ce32) {
        if ((ce32 & Opcodes.OP_CONST_CLASS_JUMBO) < 2 || 6 > ((ce32 >> IS_TAILORED) & Opcodes.OP_CONST_CLASS_JUMBO) || ((ce32 >> IS_TAILORED) & Opcodes.OP_CONST_CLASS_JUMBO) > 69) {
            return DEBUG;
        }
        return true;
    }

    private static int ceStrength(long ce) {
        if (isTempCE(ce)) {
            return strengthFromTempCE(ce);
        }
        if ((-72057594037927936L & ce) != 0) {
            return 0;
        }
        if ((((int) ce) & -16777216) != 0) {
            return 1;
        }
        if (ce != 0) {
            return 2;
        }
        return 15;
    }

    private static long nodeFromWeight32(long weight32) {
        return weight32 << HAS_BEFORE3;
    }

    private static long nodeFromWeight16(int weight16) {
        return ((long) weight16) << 48;
    }

    private static long nodeFromPreviousIndex(int previous) {
        return ((long) previous) << 28;
    }

    private static long nodeFromNextIndex(int next) {
        return (long) (next << IS_TAILORED);
    }

    private static long nodeFromStrength(int strength) {
        return (long) strength;
    }

    private static long weight32FromNode(long node) {
        return node >>> HAS_BEFORE3;
    }

    private static int weight16FromNode(long node) {
        return ((int) (node >> 48)) & DexFormat.MAX_TYPE_IDX;
    }

    private static int previousIndexFromNode(long node) {
        return ((int) (node >> 28)) & MAX_INDEX;
    }

    private static int nextIndexFromNode(long node) {
        return (((int) node) >> IS_TAILORED) & MAX_INDEX;
    }

    private static int strengthFromNode(long node) {
        return ((int) node) & 3;
    }

    private static boolean nodeHasBefore2(long node) {
        return (64 & node) != 0 ? true : DEBUG;
    }

    private static boolean nodeHasBefore3(long node) {
        return (32 & node) != 0 ? true : DEBUG;
    }

    private static boolean nodeHasAnyBefore(long node) {
        return (96 & node) != 0 ? true : DEBUG;
    }

    private static boolean isTailoredNode(long node) {
        return (8 & node) != 0 ? true : DEBUG;
    }

    private static long changeNodePreviousIndex(long node, int previous) {
        return (-281474708275201L & node) | nodeFromPreviousIndex(previous);
    }

    private static long changeNodeNextIndex(long node, int next) {
        return (-268435201 & node) | nodeFromNextIndex(next);
    }
}
