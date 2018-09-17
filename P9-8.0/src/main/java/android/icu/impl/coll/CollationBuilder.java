package android.icu.impl.coll;

import android.icu.impl.Norm2AllModes;
import android.icu.impl.Normalizer2Impl;
import android.icu.impl.Normalizer2Impl.Hangul;
import android.icu.text.CanonicalIterator;
import android.icu.text.DateTimePatternGenerator;
import android.icu.text.Normalizer2;
import android.icu.text.UnicodeSet;
import android.icu.text.UnicodeSetIterator;
import android.icu.util.ULocale;
import java.text.ParseException;

public final class CollationBuilder extends Sink {
    private static final /* synthetic */ int[] -android-icu-impl-coll-CollationRuleParser$PositionSwitchesValues = null;
    static final /* synthetic */ boolean -assertionsDisabled = (CollationBuilder.class.desiredAssertionStatus() ^ 1);
    private static final UnicodeSet COMPOSITES = new UnicodeSet("[:NFD_QC=N:]");
    private static final boolean DEBUG = false;
    private static final int HAS_BEFORE2 = 64;
    private static final int HAS_BEFORE3 = 32;
    private static final int IS_TAILORED = 8;
    private static final int MAX_INDEX = 1048575;
    private CollationTailoring base;
    private CollationData baseData;
    private long[] ces = new long[31];
    private int cesLength;
    private CollationDataBuilder dataBuilder;
    private boolean fastLatinEnabled;
    private Normalizer2 fcd = Norm2AllModes.getFCDNormalizer2();
    private Normalizer2Impl nfcImpl = Norm2AllModes.getNFCInstance().impl;
    private Normalizer2 nfd = Normalizer2.getNFDInstance();
    private UVector64 nodes;
    private UnicodeSet optimizeSet = new UnicodeSet();
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
        static final /* synthetic */ boolean -assertionsDisabled = (CEFinalizer.class.desiredAssertionStatus() ^ 1);
        private long[] finalCEs;

        CEFinalizer(long[] ces) {
            this.finalCEs = ces;
        }

        public long modifyCE32(int ce32) {
            if (!-assertionsDisabled && Collation.isSpecialCE32(ce32)) {
                throw new AssertionError();
            } else if (CollationBuilder.isTempCE32(ce32)) {
                return this.finalCEs[CollationBuilder.indexFromTempCE32(ce32)] | ((long) ((ce32 & 192) << 8));
            } else {
                return Collation.NO_CE;
            }
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
            iArr[Position.LAST_IMPLICIT.ordinal()] = 8;
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

    public CollationBuilder(CollationTailoring b) {
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
            this.optimizeSet.add(0, 127);
            this.optimizeSet.add(192, 255);
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

    void addReset(int strength, CharSequence str) {
        if (-assertionsDisabled || str.length() != 0) {
            if (str.charAt(0) == 65534) {
                this.ces[0] = getSpecialResetPosition(str);
                this.cesLength = 1;
                if (!(-assertionsDisabled || (this.ces[0] & 49344) == 0)) {
                    throw new AssertionError();
                }
            }
            this.cesLength = this.dataBuilder.getCEs(this.nfd.normalize(str), this.ces, 0);
            if (this.cesLength > 31) {
                throw new IllegalArgumentException("reset position maps to too many collation elements (more than 31)");
            }
            if (strength != 15) {
                if (-assertionsDisabled || (strength >= 0 && strength <= 2)) {
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
                            } else if (-assertionsDisabled || weight16 > 256) {
                                int previousWeight16;
                                weight16 = getWeight16Before(index, node, strength);
                                int previousIndex = previousIndexFromNode(node);
                                int i = previousIndex;
                                while (true) {
                                    node = this.nodes.elementAti(i);
                                    int previousStrength = strengthFromNode(node);
                                    if (previousStrength >= strength) {
                                        if (previousStrength == strength && (isTailoredNode(node) ^ 1) != 0) {
                                            previousWeight16 = weight16FromNode(node);
                                            break;
                                        }
                                        i = previousIndexFromNode(node);
                                    } else if (-assertionsDisabled || weight16 >= Collation.COMMON_WEIGHT16 || i == previousIndex) {
                                        previousWeight16 = Collation.COMMON_WEIGHT16;
                                    } else {
                                        throw new AssertionError();
                                    }
                                }
                                if (previousWeight16 == weight16) {
                                    index = previousIndex;
                                } else {
                                    index = insertNodeBetween(previousIndex, index, nodeFromWeight16(weight16) | nodeFromStrength(strength));
                                }
                            } else {
                                throw new AssertionError();
                            }
                        }
                        index = findOrInsertWeakNode(index, getWeight16Before(index, node, strength), strength);
                        strength = ceStrength(this.ces[this.cesLength - 1]);
                    }
                    this.ces[this.cesLength - 1] = tempCEFromIndexAndStrength(index, strength);
                    return;
                }
                throw new AssertionError();
            }
            return;
        }
        throw new AssertionError();
    }

    private int getWeight16Before(int index, long node, int level) {
        if (-assertionsDisabled || strengthFromNode(node) < level || !isTailoredNode(node)) {
            int t;
            if (strengthFromNode(node) == 2) {
                t = weight16FromNode(node);
            } else {
                t = Collation.COMMON_WEIGHT16;
            }
            while (strengthFromNode(node) > 1) {
                node = this.nodes.elementAti(previousIndexFromNode(node));
            }
            if (isTailoredNode(node)) {
                return 256;
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
                return 256;
            }
            int weight16;
            long p = weight32FromNode(node);
            if (level == 1) {
                weight16 = this.rootElements.getSecondaryBefore(p, s);
            } else {
                weight16 = this.rootElements.getTertiaryBefore(p, s, t);
                if (!(-assertionsDisabled || (weight16 & -16192) == 0)) {
                    throw new AssertionError();
                }
            }
            return weight16;
        }
        throw new AssertionError();
    }

    private long getSpecialResetPosition(CharSequence str) {
        if (-assertionsDisabled || str.length() == 2) {
            long ce;
            long node;
            int index;
            int strength = 0;
            boolean isBoundary = false;
            Position pos = CollationRuleParser.POSITION_VALUES[str.charAt(1) - 10240];
            switch (-getandroid-icu-impl-coll-CollationRuleParser$PositionSwitchesValues()[pos.ordinal()]) {
                case 1:
                    ce = this.baseData.getSingleCE(19968);
                    break;
                case 2:
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
                            if (!(-assertionsDisabled || isTailoredNode(this.nodes.elementAti(index)))) {
                                throw new AssertionError();
                            }
                        }
                        return tempCEFromIndexAndStrength(index, 1);
                    }
                    ce = this.rootElements.getFirstSecondaryCE();
                    strength = 1;
                case 3:
                    ce = this.rootElements.firstCEWithPrimaryAtLeast(this.variableTop + 1);
                    isBoundary = true;
                    break;
                case 4:
                    index = nextIndexFromNode(this.nodes.elementAti(findOrInsertNodeForRootCE(0, 2)));
                    if (index != 0) {
                        node = this.nodes.elementAti(index);
                        if (!-assertionsDisabled && strengthFromNode(node) > 2) {
                            throw new AssertionError();
                        } else if (isTailoredNode(node) && strengthFromNode(node) == 2) {
                            return tempCEFromIndexAndStrength(index, 2);
                        }
                    }
                    return this.rootElements.getFirstTertiaryCE();
                case 5:
                    return 0;
                case 6:
                    ce = Collation.makeCE(4278321664L);
                    isBoundary = true;
                    break;
                case 7:
                    ce = this.rootElements.getFirstPrimaryCE();
                    isBoundary = true;
                    break;
                case 8:
                    throw new UnsupportedOperationException("reset to [last implicit] not supported");
                case 9:
                    ce = this.rootElements.getLastSecondaryCE();
                    strength = 1;
                    break;
                case 10:
                    ce = this.rootElements.firstCEWithPrimaryAtLeast(this.baseData.getFirstPrimaryForGroup(17));
                    break;
                case 11:
                    ce = this.rootElements.getLastTertiaryCE();
                    strength = 2;
                    break;
                case 12:
                    return 0;
                case 13:
                    throw new IllegalArgumentException("LDML forbids tailoring to U+FFFF");
                case 14:
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
                    } else if (-assertionsDisabled || strength == 0) {
                        long p = ce >>> 32;
                        ce = Collation.makeCE(this.rootElements.getPrimaryAfter(p, this.rootElements.findPrimary(p), this.baseData.isCompressiblePrimary(p)));
                        index = findOrInsertNodeForRootCE(ce, 0);
                        node = this.nodes.elementAti(index);
                    } else {
                        throw new AssertionError();
                    }
                }
                if (nodeHasAnyBefore(node)) {
                    if (nodeHasBefore2(node)) {
                        index = nextIndexFromNode(this.nodes.elementAti(nextIndexFromNode(node)));
                        node = this.nodes.elementAti(index);
                    }
                    if (nodeHasBefore3(node)) {
                        index = nextIndexFromNode(this.nodes.elementAti(nextIndexFromNode(node)));
                    }
                    if (-assertionsDisabled || isTailoredNode(this.nodes.elementAti(index))) {
                        ce = tempCEFromIndexAndStrength(index, strength);
                    } else {
                        throw new AssertionError();
                    }
                }
            }
            while (true) {
                int nextIndex = nextIndexFromNode(node);
                if (nextIndex != 0) {
                    long nextNode = this.nodes.elementAti(nextIndex);
                    if (strengthFromNode(nextNode) >= strength) {
                        index = nextIndex;
                        node = nextNode;
                    }
                }
            }
            if (isTailoredNode(node)) {
                ce = tempCEFromIndexAndStrength(index, strength);
            }
            return ce;
        }
        throw new AssertionError();
    }

    /* JADX WARNING: Missing block: B:17:0x0061, code:
            if (android.icu.impl.Normalizer2Impl.Hangul.isJamoL(r16.charAt(r14 - 2)) != false) goto L_0x0063;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void addRelation(int strength, CharSequence prefix, CharSequence str, CharSequence extension) {
        String nfdPrefix;
        if (prefix.length() == 0) {
            nfdPrefix = "";
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
            if (-assertionsDisabled || this.cesLength > 0) {
                long ce = this.ces[this.cesLength - 1];
                if (strength == 0 && (isTempCE(ce) ^ 1) != 0 && (ce >>> 32) == 0) {
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
            } else {
                throw new AssertionError();
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
        if (!((nfdPrefix.contentEquals(prefix) && (nfdString.contentEquals(str) ^ 1) == 0) || (ignorePrefix(prefix) ^ 1) == 0 || (ignoreString(str) ^ 1) == 0)) {
            ce32 = addIfDifferent(prefix, str, this.ces, this.cesLength, -1);
        }
        addWithClosure(nfdPrefix, nfdString, this.ces, this.cesLength, ce32);
        this.cesLength = cesLengthBeforeExtension;
    }

    private int findOrInsertNodeForCEs(int strength) {
        if (-assertionsDisabled || (strength >= 0 && strength <= 3)) {
            long ce;
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
            if (((int) (ce >>> 56)) != 254) {
                return findOrInsertNodeForRootCE(ce, strength);
            }
            throw new UnsupportedOperationException("tailoring relative to an unassigned code point not supported");
        }
        throw new AssertionError();
    }

    private int findOrInsertNodeForRootCE(long ce, int strength) {
        if (!-assertionsDisabled && ((int) (ce >>> 56)) == 254) {
            throw new AssertionError();
        } else if (-assertionsDisabled || (192 & ce) == 0) {
            int index = findOrInsertNodeForPrimary(ce >>> 32);
            if (strength < 1) {
                return index;
            }
            int lower32 = (int) ce;
            index = findOrInsertWeakNode(index, lower32 >>> 16, 1);
            if (strength >= 2) {
                return findOrInsertWeakNode(index, lower32 & Collation.ONLY_TERTIARY_MASK, 2);
            }
            return index;
        } else {
            throw new AssertionError();
        }
    }

    private static final int binarySearchForRootPrimaryNode(int[] rootPrimaryIndexes, int length, long[] nodes, long p) {
        if (length == 0) {
            return -1;
        }
        int start = 0;
        int limit = length;
        while (true) {
            int i = (int) ((((long) start) + ((long) limit)) / 2);
            long nodePrimary = nodes[rootPrimaryIndexes[i]] >>> 32;
            if (p == nodePrimary) {
                return i;
            }
            if (p < nodePrimary) {
                if (i == start) {
                    return ~start;
                }
                limit = i;
            } else if (i == start) {
                return ~(start + 1);
            } else {
                start = i;
            }
        }
    }

    private int findOrInsertNodeForPrimary(long p) {
        int rootIndex = binarySearchForRootPrimaryNode(this.rootPrimaryIndexes.getBuffer(), this.rootPrimaryIndexes.size(), this.nodes.getBuffer(), p);
        if (rootIndex >= 0) {
            return this.rootPrimaryIndexes.elementAti(rootIndex);
        }
        int index = this.nodes.size();
        this.nodes.addElement(nodeFromWeight32(p));
        this.rootPrimaryIndexes.insertElementAt(index, ~rootIndex);
        return index;
    }

    private int findOrInsertWeakNode(int index, int weight16, int level) {
        if (!-assertionsDisabled && (index < 0 || index >= this.nodes.size())) {
            throw new AssertionError();
        } else if (!-assertionsDisabled && (1 > level || level > 2)) {
            throw new AssertionError();
        } else if (weight16 == Collation.COMMON_WEIGHT16) {
            return findCommonNode(index, level);
        } else {
            long node = this.nodes.elementAti(index);
            if (-assertionsDisabled || strengthFromNode(node) < level) {
                int nextIndex;
                if (weight16 != 0 && weight16 < Collation.COMMON_WEIGHT16) {
                    int hasThisLevelBefore = level == 1 ? 64 : 32;
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
            throw new AssertionError();
        }
    }

    private int insertTailoredNodeAfter(int index, int strength) {
        if (-assertionsDisabled || (index >= 0 && index < this.nodes.size())) {
            int nextIndex;
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
        throw new AssertionError();
    }

    private int insertNodeBetween(int index, int nextIndex, long node) {
        if (!-assertionsDisabled && previousIndexFromNode(node) != 0) {
            throw new AssertionError();
        } else if (!-assertionsDisabled && nextIndexFromNode(node) != 0) {
            throw new AssertionError();
        } else if (-assertionsDisabled || nextIndexFromNode(this.nodes.elementAti(index)) == nextIndex) {
            int newIndex = this.nodes.size();
            this.nodes.addElement(node | (nodeFromPreviousIndex(index) | nodeFromNextIndex(nextIndex)));
            this.nodes.setElementAt(changeNodeNextIndex(this.nodes.elementAti(index), newIndex), index);
            if (nextIndex != 0) {
                this.nodes.setElementAt(changeNodePreviousIndex(this.nodes.elementAti(nextIndex), newIndex), nextIndex);
            }
            return newIndex;
        } else {
            throw new AssertionError();
        }
    }

    private int findCommonNode(int index, int strength) {
        if (-assertionsDisabled || (1 <= strength && strength <= 2)) {
            long node = this.nodes.elementAti(index);
            if (strengthFromNode(node) >= strength) {
                return index;
            }
            if (((strength == 1 ? nodeHasBefore2(node) : nodeHasBefore3(node)) ^ 1) != 0) {
                return index;
            }
            node = this.nodes.elementAti(nextIndexFromNode(node));
            if (-assertionsDisabled || (!isTailoredNode(node) && strengthFromNode(node) == strength && weight16FromNode(node) < Collation.COMMON_WEIGHT16)) {
                while (true) {
                    index = nextIndexFromNode(node);
                    node = this.nodes.elementAti(index);
                    if (!-assertionsDisabled && strengthFromNode(node) < strength) {
                        throw new AssertionError();
                    } else if (!isTailoredNode(node) && strengthFromNode(node) <= strength && weight16FromNode(node) >= Collation.COMMON_WEIGHT16) {
                        if (-assertionsDisabled || weight16FromNode(node) == Collation.COMMON_WEIGHT16) {
                            return index;
                        }
                        throw new AssertionError();
                    }
                }
            } else {
                throw new AssertionError();
            }
        }
        throw new AssertionError();
    }

    private void setCaseBits(CharSequence nfdString) {
        int i;
        int numTailoredPrimaries = 0;
        for (i = 0; i < this.cesLength; i++) {
            if (ceStrength(this.ces[i]) == 0) {
                numTailoredPrimaries++;
            }
        }
        if (-assertionsDisabled || numTailoredPrimaries <= 31) {
            long ce;
            long cases = 0;
            if (numTailoredPrimaries > 0) {
                CharSequence s = nfdString;
                UTF16CollationIterator baseCEs = new UTF16CollationIterator(this.baseData, false, nfdString, 0);
                int baseCEsLength = baseCEs.fetchCEs() - 1;
                if (-assertionsDisabled || (baseCEsLength >= 0 && baseCEs.getCE(baseCEsLength) == Collation.NO_CE)) {
                    int lastCase = 0;
                    int numBasePrimaries = 0;
                    for (i = 0; i < baseCEsLength; i++) {
                        ce = baseCEs.getCE(i);
                        if ((ce >>> 32) != 0) {
                            numBasePrimaries++;
                            int c = (((int) ce) >> 14) & 3;
                            if (!-assertionsDisabled && c != 0 && c != 2) {
                                throw new AssertionError();
                            } else if (numBasePrimaries < numTailoredPrimaries) {
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
                } else {
                    throw new AssertionError();
                }
            }
            for (i = 0; i < this.cesLength; i++) {
                ce = this.ces[i] & -49153;
                int strength = ceStrength(ce);
                if (strength == 0) {
                    ce |= (3 & cases) << 14;
                    cases >>>= 2;
                } else if (strength == 2) {
                    ce |= 32768;
                }
                this.ces[i] = ce;
            }
            return;
        }
        throw new AssertionError();
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
            prefix = "";
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
                        if (-assertionsDisabled || iter.codepoint != UnicodeSetIterator.IS_STRING) {
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
                        } else {
                            throw new AssertionError();
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

    private boolean mergeCompositeIntoString(CharSequence nfdString, int indexAfterLastStarter, int composite, CharSequence decomp, StringBuilder newNFDString, StringBuilder newString) {
        if (-assertionsDisabled || Character.codePointBefore(nfdString, indexAfterLastStarter) == Character.codePointAt(decomp, 0)) {
            int lastStarterLength = Character.offsetByCodePoints(decomp, 0, 1);
            if (lastStarterLength == decomp.length()) {
                return false;
            }
            if (equalSubSequences(nfdString, indexAfterLastStarter, decomp, lastStarterLength)) {
                return false;
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
                    if (sourceIndex >= nfdString.length()) {
                        break;
                    }
                    sourceChar = Character.codePointAt(nfdString, sourceIndex);
                    sourceCC = this.nfd.getCombiningClass(sourceChar);
                    if (!-assertionsDisabled && sourceCC == 0) {
                        throw new AssertionError();
                    }
                }
                if (decompIndex >= decomp.length()) {
                    break;
                }
                int decompChar = Character.codePointAt(decomp, decompIndex);
                decompCC = this.nfd.getCombiningClass(decompChar);
                if (decompCC == 0) {
                    return false;
                }
                if (sourceCC < decompCC) {
                    return false;
                }
                if (decompCC < sourceCC) {
                    newNFDString.appendCodePoint(decompChar);
                    decompIndex += Character.charCount(decompChar);
                } else if (decompChar != sourceChar) {
                    return false;
                } else {
                    newNFDString.appendCodePoint(decompChar);
                    decompIndex += Character.charCount(decompChar);
                    sourceIndex += Character.charCount(decompChar);
                    sourceChar = -1;
                }
            }
            if (sourceChar >= 0) {
                if (sourceCC < decompCC) {
                    return false;
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
        throw new AssertionError();
    }

    private boolean equalSubSequences(CharSequence left, int leftStart, CharSequence right, int rightStart) {
        int leftLength = left.length();
        if (leftLength - leftStart != right.length() - rightStart) {
            return false;
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
        return false;
    }

    private boolean ignorePrefix(CharSequence s) {
        return isFCD(s) ^ 1;
    }

    private boolean ignoreString(CharSequence s) {
        return isFCD(s) ? Hangul.isHangul(s.charAt(0)) : true;
    }

    private boolean isFCD(CharSequence s) {
        return this.fcd.isNormalized(s);
    }

    static {
        COMPOSITES.remove(Hangul.HANGUL_BASE, Hangul.HANGUL_END);
    }

    private void closeOverComposites() {
        String prefix = "";
        UnicodeSetIterator iter = new UnicodeSetIterator(COMPOSITES);
        while (iter.next()) {
            if (-assertionsDisabled || iter.codepoint != UnicodeSetIterator.IS_STRING) {
                this.cesLength = this.dataBuilder.getCEs(this.nfd.getDecomposition(iter.codepoint), this.ces, 0);
                if (this.cesLength <= 31) {
                    addIfDifferent(prefix, iter.getString(), this.ces, this.cesLength, -1);
                }
            } else {
                throw new AssertionError();
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
            return false;
        }
        if (-assertionsDisabled || ces1Length <= 31) {
            for (int i = 0; i < ces1Length; i++) {
                if (ces1[i] != ces2[i]) {
                    return false;
                }
            }
            return true;
        }
        throw new AssertionError();
    }

    private static final int alignWeightRight(int w) {
        if (w != 0) {
            while ((w & 255) == 0) {
                w >>>= 8;
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
            boolean pIsTailored = false;
            boolean sIsTailored = false;
            boolean tIsTailored = false;
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
                                    } else if (-assertionsDisabled || s == 1280) {
                                        sLimit = this.rootElements.getSecondaryBoundary();
                                    } else {
                                        throw new AssertionError();
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
                                if (!-assertionsDisabled && s == -1) {
                                    throw new AssertionError();
                                }
                            }
                            s = weight16FromNode(node);
                            sIsTailored = false;
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
                            if (-assertionsDisabled || p != 4294967295L) {
                                s = Collation.COMMON_WEIGHT16;
                                sIsTailored = false;
                            } else {
                                throw new AssertionError();
                            }
                        } else {
                            throw new AssertionError();
                        }
                        t = s == 0 ? 0 : Collation.COMMON_WEIGHT16;
                        tIsTailored = false;
                    } else if (isTailoredNode(node)) {
                        if (!tIsTailored) {
                            int tLimit;
                            int tCount = countTailoredNodes(nodesArray, nextIndex, 2) + 1;
                            if (t == 0) {
                                t = this.rootElements.getTertiaryBoundary() - 256;
                                tLimit = ((int) this.rootElements.getFirstTertiaryCE()) & Collation.ONLY_TERTIARY_MASK;
                            } else if (!pIsTailored && (sIsTailored ^ 1) != 0) {
                                tLimit = this.rootElements.getTertiaryAfter(pIndex, s, t);
                            } else if (t == 256) {
                                tLimit = Collation.COMMON_WEIGHT16;
                            } else if (-assertionsDisabled || t == 1280) {
                                tLimit = this.rootElements.getTertiaryBoundary();
                            } else {
                                throw new AssertionError();
                            }
                            if (-assertionsDisabled || tLimit == 16384 || (tLimit & -16192) == 0) {
                                tertiaries.initForTertiary();
                                if (tertiaries.allocWeights((long) t, (long) tLimit, tCount)) {
                                    tIsTailored = true;
                                } else {
                                    throw new UnsupportedOperationException("tertiary tailoring gap too small");
                                }
                            }
                            throw new AssertionError();
                        }
                        t = (int) tertiaries.nextWeight();
                        if (!-assertionsDisabled && t == -1) {
                            throw new AssertionError();
                        }
                    } else {
                        t = weight16FromNode(node);
                        tIsTailored = false;
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
        return ((((((long) (1040384 & index)) << 43) + 4629700417037541376L) + (((long) (index & 8128)) << 42)) + ((long) ((index & 63) << 24))) + ((long) (strength << 8));
    }

    private static int indexFromTempCE(long tempCE) {
        tempCE -= 4629700417037541376L;
        return ((((int) (tempCE >> 43)) & 1040384) | (((int) (tempCE >> 42)) & 8128)) | (((int) (tempCE >> 24)) & 63);
    }

    private static int strengthFromTempCE(long tempCE) {
        return (((int) tempCE) >> 8) & 3;
    }

    private static boolean isTempCE(long ce) {
        int sec = ((int) ce) >>> 24;
        if (6 > sec || sec > 69) {
            return false;
        }
        return true;
    }

    private static int indexFromTempCE32(int tempCE32) {
        tempCE32 -= 1077937696;
        return (((tempCE32 >> 11) & 1040384) | ((tempCE32 >> 10) & 8128)) | ((tempCE32 >> 8) & 63);
    }

    private static boolean isTempCE32(int ce32) {
        if ((ce32 & 255) < 2 || 6 > ((ce32 >> 8) & 255) || ((ce32 >> 8) & 255) > 69) {
            return false;
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
        return weight32 << 32;
    }

    private static long nodeFromWeight16(int weight16) {
        return ((long) weight16) << 48;
    }

    private static long nodeFromPreviousIndex(int previous) {
        return ((long) previous) << 28;
    }

    private static long nodeFromNextIndex(int next) {
        return (long) (next << 8);
    }

    private static long nodeFromStrength(int strength) {
        return (long) strength;
    }

    private static long weight32FromNode(long node) {
        return node >>> 32;
    }

    private static int weight16FromNode(long node) {
        return ((int) (node >> 48)) & DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH;
    }

    private static int previousIndexFromNode(long node) {
        return ((int) (node >> 28)) & MAX_INDEX;
    }

    private static int nextIndexFromNode(long node) {
        return (((int) node) >> 8) & MAX_INDEX;
    }

    private static int strengthFromNode(long node) {
        return ((int) node) & 3;
    }

    private static boolean nodeHasBefore2(long node) {
        return (64 & node) != 0;
    }

    private static boolean nodeHasBefore3(long node) {
        return (32 & node) != 0;
    }

    private static boolean nodeHasAnyBefore(long node) {
        return (96 & node) != 0;
    }

    private static boolean isTailoredNode(long node) {
        return (8 & node) != 0;
    }

    private static long changeNodePreviousIndex(long node, int previous) {
        return (-281474708275201L & node) | nodeFromPreviousIndex(previous);
    }

    private static long changeNodeNextIndex(long node, int next) {
        return (-268435201 & node) | nodeFromNextIndex(next);
    }
}
