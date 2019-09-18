package android.icu.impl.coll;

import android.icu.impl.Norm2AllModes;
import android.icu.impl.Normalizer2Impl;
import android.icu.impl.coll.CollationDataBuilder;
import android.icu.impl.coll.CollationRuleParser;
import android.icu.text.CanonicalIterator;
import android.icu.text.DateTimePatternGenerator;
import android.icu.text.Normalizer2;
import android.icu.text.UnicodeSet;
import android.icu.text.UnicodeSetIterator;
import android.icu.util.ULocale;
import java.text.ParseException;

public final class CollationBuilder extends CollationRuleParser.Sink {
    static final /* synthetic */ boolean $assertionsDisabled = false;
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

    private static final class BundleImporter implements CollationRuleParser.Importer {
        BundleImporter() {
        }

        public String getRules(String localeID, String collationType) {
            return CollationLoader.loadRules(new ULocale(localeID), collationType);
        }
    }

    private static final class CEFinalizer implements CollationDataBuilder.CEModifier {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private long[] finalCEs;

        static {
            Class<CollationBuilder> cls = CollationBuilder.class;
        }

        CEFinalizer(long[] ces) {
            this.finalCEs = ces;
        }

        public long modifyCE32(int ce32) {
            if (CollationBuilder.isTempCE32(ce32)) {
                return this.finalCEs[CollationBuilder.indexFromTempCE32(ce32)] | ((long) ((ce32 & 192) << 8));
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

    static {
        COMPOSITES.remove(Normalizer2Impl.Hangul.HANGUL_BASE, Normalizer2Impl.Hangul.HANGUL_END);
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
        if (this.baseData.rootElements != null) {
            CollationTailoring tailoring = new CollationTailoring(this.base.settings);
            CollationRuleParser parser = new CollationRuleParser(this.baseData);
            this.variableTop = this.base.settings.readOnly().variableTop;
            parser.setSink(this);
            parser.setImporter(new BundleImporter());
            CollationSettings ownedSettings = tailoring.settings.copyOnWrite();
            parser.parse(ruleString, ownedSettings);
            if (this.dataBuilder.hasMappings()) {
                makeTailoredCEs();
                closeOverComposites();
                finalizeCEs();
                this.optimizeSet.add(0, 127);
                this.optimizeSet.add(192, 255);
                this.optimizeSet.remove(Normalizer2Impl.Hangul.HANGUL_BASE, Normalizer2Impl.Hangul.HANGUL_END);
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
        throw new UnsupportedOperationException("missing root elements data, tailoring not supported");
    }

    /* access modifiers changed from: package-private */
    public void addReset(int strength, CharSequence str) {
        int index;
        int index2;
        long node;
        int previousWeight16;
        if (str.charAt(0) == 65534) {
            this.ces[0] = getSpecialResetPosition(str);
            this.cesLength = 1;
        } else {
            this.cesLength = this.dataBuilder.getCEs(this.nfd.normalize(str), this.ces, 0);
            if (this.cesLength > 31) {
                throw new IllegalArgumentException("reset position maps to too many collation elements (more than 31)");
            }
        }
        if (strength != 15) {
            int index3 = findOrInsertNodeForCEs(strength);
            long node2 = this.nodes.elementAti(index3);
            while (strengthFromNode(node2) > strength) {
                index3 = previousIndexFromNode(node2);
                node2 = this.nodes.elementAti(index3);
            }
            if (strengthFromNode(node2) == strength && isTailoredNode(node2)) {
                index = previousIndexFromNode(node2);
            } else if (strength == 0) {
                long p = weight32FromNode(node2);
                if (p == 0) {
                    throw new UnsupportedOperationException("reset primary-before ignorable not possible");
                } else if (p <= this.rootElements.getFirstPrimary()) {
                    throw new UnsupportedOperationException("reset primary-before first non-ignorable not supported");
                } else if (p != 4278321664L) {
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
            } else {
                int index4 = findCommonNode(index3, 1);
                if (strength >= 2) {
                    index4 = findCommonNode(index4, 2);
                }
                long node3 = this.nodes.elementAti(index4);
                if (strengthFromNode(node3) != strength) {
                    index2 = findOrInsertWeakNode(index4, getWeight16Before(index4, node3, strength), strength);
                } else if (weight16FromNode(node3) == 0) {
                    throw new UnsupportedOperationException(strength == 1 ? "reset secondary-before secondary ignorable not possible" : "reset tertiary-before completely ignorable not possible");
                } else {
                    int weight16 = getWeight16Before(index4, node3, strength);
                    int previousIndex = previousIndexFromNode(node3);
                    long j = node3;
                    int i = previousIndex;
                    while (true) {
                        node = this.nodes.elementAti(i);
                        int previousStrength = strengthFromNode(node);
                        if (previousStrength >= strength) {
                            if (previousStrength == strength && !isTailoredNode(node)) {
                                previousWeight16 = weight16FromNode(node);
                                break;
                            }
                            i = previousIndexFromNode(node);
                        } else {
                            previousWeight16 = Collation.COMMON_WEIGHT16;
                            break;
                        }
                    }
                    if (previousWeight16 == weight16) {
                        index2 = previousIndex;
                    } else {
                        node = nodeFromWeight16(weight16) | nodeFromStrength(strength);
                        index2 = insertNodeBetween(previousIndex, index4, node);
                    }
                    long j2 = node;
                }
                strength = ceStrength(this.ces[this.cesLength - 1]);
            }
            this.ces[this.cesLength - 1] = tempCEFromIndexAndStrength(index, strength);
        }
    }

    private int getWeight16Before(int index, long node, int level) {
        int t;
        int weight16;
        int strengthFromNode = strengthFromNode(node);
        int s = Collation.COMMON_WEIGHT16;
        if (strengthFromNode == 2) {
            t = weight16FromNode(node);
        } else {
            t = 1280;
        }
        while (strengthFromNode(node) > 1) {
            node = this.nodes.elementAti(previousIndexFromNode(node));
        }
        if (isTailoredNode(node)) {
            return 256;
        }
        if (strengthFromNode(node) == 1) {
            s = weight16FromNode(node);
        }
        while (strengthFromNode(node) > 0) {
            node = this.nodes.elementAti(previousIndexFromNode(node));
        }
        if (isTailoredNode(node)) {
            return 256;
        }
        long p = weight32FromNode(node);
        if (level == 1) {
            weight16 = this.rootElements.getSecondaryBefore(p, s);
        } else {
            weight16 = this.rootElements.getTertiaryBefore(p, s, t);
        }
        return weight16;
    }

    private long getSpecialResetPosition(CharSequence str) {
        boolean isBoundary;
        int strength;
        long ce;
        int strength2 = 0;
        boolean isBoundary2 = false;
        CollationRuleParser.Position pos = CollationRuleParser.POSITION_VALUES[str.charAt(1) - 10240];
        switch (pos) {
            case FIRST_TERTIARY_IGNORABLE:
                return 0;
            case LAST_TERTIARY_IGNORABLE:
                return 0;
            case FIRST_SECONDARY_IGNORABLE:
                int nextIndexFromNode = nextIndexFromNode(this.nodes.elementAti(findOrInsertNodeForRootCE(0, 2)));
                int index = nextIndexFromNode;
                if (nextIndexFromNode != 0) {
                    long node = this.nodes.elementAti(index);
                    if (isTailoredNode(node) && strengthFromNode(node) == 2) {
                        return tempCEFromIndexAndStrength(index, 2);
                    }
                }
                return this.rootElements.getFirstTertiaryCE();
            case LAST_SECONDARY_IGNORABLE:
                ce = this.rootElements.getLastTertiaryCE();
                strength2 = 2;
                break;
            case FIRST_PRIMARY_IGNORABLE:
                long node2 = this.nodes.elementAti(findOrInsertNodeForRootCE(0, 1));
                while (true) {
                    int nextIndexFromNode2 = nextIndexFromNode(node2);
                    int index2 = nextIndexFromNode2;
                    if (nextIndexFromNode2 != 0) {
                        node2 = this.nodes.elementAti(index2);
                        int strength3 = strengthFromNode(node2);
                        if (strength3 >= 1) {
                            if (strength3 == 1) {
                                if (isTailoredNode(node2)) {
                                    if (nodeHasBefore3(node2)) {
                                        index2 = nextIndexFromNode(this.nodes.elementAti(nextIndexFromNode(node2)));
                                    }
                                    return tempCEFromIndexAndStrength(index2, 1);
                                }
                            }
                        }
                    }
                }
                strength = 1;
                isBoundary = false;
                ce = this.rootElements.getFirstSecondaryCE();
                break;
            case LAST_PRIMARY_IGNORABLE:
                ce = this.rootElements.getLastSecondaryCE();
                strength2 = 1;
                break;
            case FIRST_VARIABLE:
                ce = this.rootElements.getFirstPrimaryCE();
                isBoundary2 = true;
                break;
            case LAST_VARIABLE:
                ce = this.rootElements.lastCEWithPrimaryBefore(this.variableTop + 1);
                break;
            case FIRST_REGULAR:
                ce = this.rootElements.firstCEWithPrimaryAtLeast(this.variableTop + 1);
                isBoundary2 = true;
                break;
            case LAST_REGULAR:
                ce = this.rootElements.firstCEWithPrimaryAtLeast(this.baseData.getFirstPrimaryForGroup(17));
                break;
            case FIRST_IMPLICIT:
                ce = this.baseData.getSingleCE(19968);
                break;
            case LAST_IMPLICIT:
                throw new UnsupportedOperationException("reset to [last implicit] not supported");
            case FIRST_TRAILING:
                ce = Collation.makeCE(4278321664L);
                isBoundary2 = true;
                break;
            case LAST_TRAILING:
                throw new IllegalArgumentException("LDML forbids tailoring to U+FFFF");
            default:
                return 0;
        }
        strength = strength2;
        isBoundary = isBoundary2;
        int index3 = findOrInsertNodeForRootCE(ce, strength);
        long node3 = this.nodes.elementAti(index3);
        if ((pos.ordinal() & 1) == 0) {
            if (!nodeHasAnyBefore(node3) && isBoundary) {
                int nextIndexFromNode3 = nextIndexFromNode(node3);
                index3 = nextIndexFromNode3;
                if (nextIndexFromNode3 != 0) {
                    node3 = this.nodes.elementAti(index3);
                    ce = tempCEFromIndexAndStrength(index3, strength);
                } else {
                    long p = ce >>> 32;
                    ce = Collation.makeCE(this.rootElements.getPrimaryAfter(p, this.rootElements.findPrimary(p), this.baseData.isCompressiblePrimary(p)));
                    index3 = findOrInsertNodeForRootCE(ce, 0);
                    node3 = this.nodes.elementAti(index3);
                }
            }
            if (nodeHasAnyBefore(node3) != 0) {
                if (nodeHasBefore2(node3)) {
                    index3 = nextIndexFromNode(this.nodes.elementAti(nextIndexFromNode(node3)));
                    node3 = this.nodes.elementAti(index3);
                }
                if (nodeHasBefore3(node3)) {
                    index3 = nextIndexFromNode(this.nodes.elementAti(nextIndexFromNode(node3)));
                }
                ce = tempCEFromIndexAndStrength(index3, strength);
            }
        } else {
            while (true) {
                int nextIndex = nextIndexFromNode(node3);
                if (nextIndex != 0) {
                    long nextNode = this.nodes.elementAti(nextIndex);
                    if (strengthFromNode(nextNode) >= strength) {
                        index3 = nextIndex;
                        node3 = nextNode;
                    }
                }
            }
            if (isTailoredNode(node3) != 0) {
                ce = tempCEFromIndexAndStrength(index3, strength);
            }
        }
        return ce;
    }

    /* access modifiers changed from: package-private */
    public void addRelation(int strength, CharSequence prefix, CharSequence str, CharSequence extension) {
        String nfdPrefix;
        int i = strength;
        CharSequence charSequence = prefix;
        CharSequence charSequence2 = str;
        if (prefix.length() == 0) {
            nfdPrefix = "";
        } else {
            nfdPrefix = this.nfd.normalize(charSequence);
        }
        String nfdPrefix2 = nfdPrefix;
        String nfdString = this.nfd.normalize(charSequence2);
        int nfdLength = nfdString.length();
        if (nfdLength >= 2) {
            char c = nfdString.charAt(0);
            if (Normalizer2Impl.Hangul.isJamoL(c) || Normalizer2Impl.Hangul.isJamoV(c)) {
                throw new UnsupportedOperationException("contractions starting with conjoining Jamo L or V not supported");
            }
            char c2 = nfdString.charAt(nfdLength - 1);
            if (Normalizer2Impl.Hangul.isJamoL(c2) || (Normalizer2Impl.Hangul.isJamoV(c2) && Normalizer2Impl.Hangul.isJamoL(nfdString.charAt(nfdLength - 2)))) {
                throw new UnsupportedOperationException("contractions ending with conjoining Jamo L or L+V not supported");
            }
        }
        if (i != 15) {
            int index = findOrInsertNodeForCEs(strength);
            long ce = this.ces[this.cesLength - 1];
            if (i == 0 && !isTempCE(ce) && (ce >>> 32) == 0) {
                throw new UnsupportedOperationException("tailoring primary after ignorables not supported");
            } else if (i == 3 && ce == 0) {
                throw new UnsupportedOperationException("tailoring quaternary after tertiary ignorables not supported");
            } else {
                int index2 = insertTailoredNodeAfter(index, i);
                int tempStrength = ceStrength(ce);
                if (i < tempStrength) {
                    tempStrength = i;
                }
                this.ces[this.cesLength - 1] = tempCEFromIndexAndStrength(index2, tempStrength);
            }
        }
        setCaseBits(nfdString);
        int cesLengthBeforeExtension = this.cesLength;
        if (extension.length() != 0) {
            this.cesLength = this.dataBuilder.getCEs(this.nfd.normalize(extension), this.ces, this.cesLength);
            if (this.cesLength > 31) {
                throw new IllegalArgumentException("extension string adds too many collation elements (more than 31 total)");
            }
        } else {
            CharSequence charSequence3 = extension;
        }
        int ce32 = -1;
        if ((!nfdPrefix2.contentEquals(charSequence) || !nfdString.contentEquals(charSequence2)) && !ignorePrefix(charSequence) && !ignoreString(charSequence2)) {
            ce32 = addIfDifferent(charSequence, charSequence2, this.ces, this.cesLength, -1);
        }
        addWithClosure(nfdPrefix2, nfdString, this.ces, this.cesLength, ce32);
        this.cesLength = cesLengthBeforeExtension;
    }

    private int findOrInsertNodeForCEs(int strength) {
        long ce;
        while (true) {
            if (this.cesLength == 0) {
                this.ces[0] = 0;
                ce = 0;
                this.cesLength = 1;
                break;
            }
            ce = this.ces[this.cesLength - 1];
            if (ceStrength(ce) <= strength) {
                break;
            }
            this.cesLength--;
        }
        if (isTempCE(ce)) {
            return indexFromTempCE(ce);
        }
        if (((int) (ce >>> 56)) != 254) {
            return findOrInsertNodeForRootCE(ce, strength);
        }
        throw new UnsupportedOperationException("tailoring relative to an unassigned code point not supported");
    }

    private int findOrInsertNodeForRootCE(long ce, int strength) {
        int index = findOrInsertNodeForPrimary(ce >>> 32);
        if (strength < 1) {
            return index;
        }
        int lower32 = (int) ce;
        int index2 = findOrInsertWeakNode(index, lower32 >>> 16, 1);
        if (strength >= 2) {
            return findOrInsertWeakNode(index2, lower32 & Collation.ONLY_TERTIARY_MASK, 2);
        }
        return index2;
    }

    private static final int binarySearchForRootPrimaryNode(int[] rootPrimaryIndexes2, int length, long[] nodes2, long p) {
        if (length == 0) {
            return -1;
        }
        int start = 0;
        int limit = length;
        while (true) {
            int i = (int) ((((long) start) + ((long) limit)) / 2);
            long nodePrimary = nodes2[rootPrimaryIndexes2[i]] >>> 32;
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
        int nextIndex;
        if (weight16 == 1280) {
            return findCommonNode(index, level);
        }
        long node = this.nodes.elementAti(index);
        if (weight16 != 0 && weight16 < 1280) {
            int hasThisLevelBefore = level == 1 ? 64 : 32;
            if ((((long) hasThisLevelBefore) & node) == 0) {
                long commonNode = nodeFromWeight16(Collation.COMMON_WEIGHT16) | nodeFromStrength(level);
                if (level == 1) {
                    commonNode |= 32 & node;
                    node &= -33;
                }
                this.nodes.setElementAt(((long) hasThisLevelBefore) | node, index);
                int nextIndex2 = nextIndexFromNode(node);
                int index2 = insertNodeBetween(index, nextIndex2, nodeFromWeight16(weight16) | nodeFromStrength(level));
                insertNodeBetween(index2, nextIndex2, commonNode);
                return index2;
            }
        }
        while (true) {
            int nextIndex3 = nextIndexFromNode(node);
            nextIndex = nextIndex3;
            if (nextIndex3 == 0) {
                break;
            }
            node = this.nodes.elementAti(nextIndex);
            int nextStrength = strengthFromNode(node);
            if (nextStrength <= level) {
                if (nextStrength < level) {
                    break;
                } else if (!isTailoredNode(node)) {
                    int nextWeight16 = weight16FromNode(node);
                    if (nextWeight16 == weight16) {
                        return nextIndex;
                    }
                    if (nextWeight16 > weight16) {
                        break;
                    }
                } else {
                    continue;
                }
            }
            index = nextIndex;
        }
        return insertNodeBetween(index, nextIndex, nodeFromWeight16(weight16) | nodeFromStrength(level));
    }

    private int insertTailoredNodeAfter(int index, int strength) {
        int nextIndex;
        if (strength >= 1) {
            index = findCommonNode(index, 1);
            if (strength >= 2) {
                index = findCommonNode(index, 2);
            }
        }
        long node = this.nodes.elementAti(index);
        while (true) {
            int nextIndexFromNode = nextIndexFromNode(node);
            nextIndex = nextIndexFromNode;
            if (nextIndexFromNode == 0) {
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
        int newIndex = this.nodes.size();
        this.nodes.addElement(node | nodeFromPreviousIndex(index) | nodeFromNextIndex(nextIndex));
        this.nodes.setElementAt(changeNodeNextIndex(this.nodes.elementAti(index), newIndex), index);
        if (nextIndex != 0) {
            this.nodes.setElementAt(changeNodePreviousIndex(this.nodes.elementAti(nextIndex), newIndex), nextIndex);
        }
        return newIndex;
    }

    private int findCommonNode(int index, int strength) {
        long node = this.nodes.elementAti(index);
        if (strengthFromNode(node) >= strength) {
            return index;
        }
        if (strength != 1 ? !nodeHasBefore3(node) : !nodeHasBefore2(node)) {
            return index;
        }
        long node2 = this.nodes.elementAti(nextIndexFromNode(node));
        while (true) {
            int index2 = nextIndexFromNode(node2);
            node2 = this.nodes.elementAti(index2);
            if (!isTailoredNode(node2) && strengthFromNode(node2) <= strength && weight16FromNode(node2) >= 1280) {
                return index2;
            }
        }
    }

    private void setCaseBits(CharSequence nfdString) {
        int numTailoredPrimaries = 0;
        for (int i = 0; i < this.cesLength; i++) {
            if (ceStrength(this.ces[i]) == 0) {
                numTailoredPrimaries++;
            }
        }
        long cases = 0;
        int i2 = 14;
        if (numTailoredPrimaries > 0) {
            UTF16CollationIterator baseCEs = new UTF16CollationIterator(this.baseData, false, nfdString, 0);
            int baseCEsLength = baseCEs.fetchCEs() - 1;
            int lastCase = 0;
            int numBasePrimaries = 0;
            long cases2 = 0;
            int i3 = 0;
            while (true) {
                if (i3 >= baseCEsLength) {
                    break;
                }
                long ce = baseCEs.getCE(i3);
                if ((ce >>> 32) != 0) {
                    numBasePrimaries++;
                    int c = (((int) ce) >> i2) & 3;
                    if (numBasePrimaries < numTailoredPrimaries) {
                        cases2 |= ((long) c) << ((numBasePrimaries - 1) * 2);
                    } else if (numBasePrimaries == numTailoredPrimaries) {
                        lastCase = c;
                    } else if (c != lastCase) {
                        lastCase = 1;
                        break;
                    }
                }
                i3++;
                i2 = 14;
            }
            if (numBasePrimaries >= numTailoredPrimaries) {
                cases = cases2 | (((long) lastCase) << ((numTailoredPrimaries - 1) * 2));
            } else {
                cases = cases2;
            }
        }
        int i4 = 0;
        while (true) {
            int i5 = i4;
            if (i5 < this.cesLength) {
                long ce2 = this.ces[i5] & -49153;
                int strength = ceStrength(ce2);
                if (strength == 0) {
                    ce2 |= (3 & cases) << 14;
                    cases >>>= 2;
                } else if (strength == 2) {
                    ce2 |= 32768;
                }
                this.ces[i5] = ce2;
                i4 = i5 + 1;
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void suppressContractions(UnicodeSet set) {
        this.dataBuilder.suppressContractions(set);
    }

    /* access modifiers changed from: package-private */
    public void optimize(UnicodeSet set) {
        this.optimizeSet.addAll(set);
    }

    private int addWithClosure(CharSequence nfdPrefix, CharSequence nfdString, long[] newCEs, int newCEsLength, int ce32) {
        int ce322 = addOnlyClosure(nfdPrefix, nfdString, newCEs, newCEsLength, addIfDifferent(nfdPrefix, nfdString, newCEs, newCEsLength, ce32));
        addTailComposites(nfdPrefix, nfdString);
        return ce322;
    }

    private int addOnlyClosure(CharSequence nfdPrefix, CharSequence nfdString, long[] newCEs, int newCEsLength, int ce32) {
        CharSequence charSequence = nfdString;
        if (nfdPrefix.length() == 0) {
            CanonicalIterator stringIter = new CanonicalIterator(nfdString.toString());
            int ce322 = ce32;
            String prefix = "";
            while (true) {
                String str = stringIter.next();
                if (str == null) {
                    return ce322;
                }
                if (!ignoreString(str) && !str.contentEquals(charSequence)) {
                    ce322 = addIfDifferent(prefix, str, newCEs, newCEsLength, ce322);
                }
            }
        } else {
            CanonicalIterator prefixIter = new CanonicalIterator(nfdPrefix.toString());
            CanonicalIterator stringIter2 = new CanonicalIterator(nfdString.toString());
            int ce323 = ce32;
            while (true) {
                CanonicalIterator stringIter3 = stringIter2;
                String prefix2 = prefixIter.next();
                if (prefix2 == null) {
                    return ce323;
                }
                if (ignorePrefix(prefix2)) {
                    stringIter2 = stringIter3;
                } else {
                    boolean samePrefix = prefix2.contentEquals(nfdPrefix);
                    int ce324 = ce323;
                    while (true) {
                        boolean samePrefix2 = samePrefix;
                        String str2 = stringIter3.next();
                        if (str2 == null) {
                            break;
                        }
                        if (!ignoreString(str2) && (!samePrefix2 || !str2.contentEquals(charSequence))) {
                            ce324 = addIfDifferent(prefix2, str2, newCEs, newCEsLength, ce324);
                        }
                        samePrefix = samePrefix2;
                    }
                    stringIter3.reset();
                    stringIter2 = stringIter3;
                    ce323 = ce324;
                }
            }
        }
    }

    private void addTailComposites(CharSequence nfdPrefix, CharSequence nfdString) {
        int indexAfterLastStarter = nfdString.length();
        while (true) {
            int indexAfterLastStarter2 = indexAfterLastStarter;
            if (indexAfterLastStarter2 != 0) {
                CharSequence charSequence = nfdString;
                int lastStarter = Character.codePointBefore(charSequence, indexAfterLastStarter2);
                if (this.nfd.getCombiningClass(lastStarter) != 0) {
                    indexAfterLastStarter = indexAfterLastStarter2 - Character.charCount(lastStarter);
                } else if (!Normalizer2Impl.Hangul.isJamoL(lastStarter)) {
                    UnicodeSet composites = new UnicodeSet();
                    if (this.nfcImpl.getCanonStartSet(lastStarter, composites)) {
                        StringBuilder newNFDString = new StringBuilder();
                        StringBuilder newString = new StringBuilder();
                        long[] newCEs = new long[31];
                        UnicodeSetIterator iter = new UnicodeSetIterator(composites);
                        while (true) {
                            UnicodeSetIterator iter2 = iter;
                            if (iter2.next()) {
                                int composite = iter2.codepoint;
                                int i = composite;
                                if (mergeCompositeIntoString(charSequence, indexAfterLastStarter2, composite, this.nfd.getDecomposition(composite), newNFDString, newString)) {
                                    CharSequence charSequence2 = nfdPrefix;
                                    int newCEsLength = this.dataBuilder.getCEs(charSequence2, (CharSequence) newNFDString, newCEs, 0);
                                    if (newCEsLength <= 31) {
                                        int newCEsLength2 = newCEsLength;
                                        int ce32 = addIfDifferent(charSequence2, newString, newCEs, newCEsLength, -1);
                                        if (ce32 != -1) {
                                            int i2 = ce32;
                                            addOnlyClosure(nfdPrefix, newNFDString, newCEs, newCEsLength2, ce32);
                                        }
                                    }
                                }
                                iter = iter2;
                            } else {
                                return;
                            }
                        }
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            } else {
                return;
            }
        }
    }

    private boolean mergeCompositeIntoString(CharSequence nfdString, int indexAfterLastStarter, int composite, CharSequence decomp, StringBuilder newNFDString, StringBuilder newString) {
        CharSequence charSequence = nfdString;
        int sourceIndex = indexAfterLastStarter;
        CharSequence charSequence2 = decomp;
        StringBuilder sb = newNFDString;
        StringBuilder sb2 = newString;
        boolean z = true;
        int lastStarterLength = Character.offsetByCodePoints(charSequence2, 0, 1);
        if (lastStarterLength == decomp.length() || equalSubSequences(charSequence, sourceIndex, charSequence2, lastStarterLength)) {
            return false;
        }
        sb.setLength(0);
        sb.append(charSequence, 0, sourceIndex);
        sb2.setLength(0);
        sb2.append(charSequence, 0, sourceIndex - lastStarterLength);
        sb2.appendCodePoint(composite);
        int sourceChar = -1;
        int sourceCC = 0;
        int decompIndex = lastStarterLength;
        int sourceIndex2 = sourceIndex;
        int decompCC = 0;
        while (true) {
            if (sourceChar < 0) {
                if (sourceIndex2 >= nfdString.length()) {
                    break;
                }
                sourceChar = Character.codePointAt(charSequence, sourceIndex2);
                sourceCC = this.nfd.getCombiningClass(sourceChar);
            }
            if (decompIndex >= decomp.length()) {
                break;
            }
            int decompChar = Character.codePointAt(charSequence2, decompIndex);
            decompCC = this.nfd.getCombiningClass(decompChar);
            if (decompCC == 0 || sourceCC < decompCC) {
                return false;
            }
            if (decompCC < sourceCC) {
                sb.appendCodePoint(decompChar);
                decompIndex += Character.charCount(decompChar);
            } else if (decompChar != sourceChar) {
                return false;
            } else {
                sb.appendCodePoint(decompChar);
                decompIndex += Character.charCount(decompChar);
                sourceIndex2 += Character.charCount(decompChar);
                sourceChar = -1;
            }
            z = true;
        }
        if (sourceChar >= 0) {
            if (sourceCC < decompCC) {
                return false;
            }
            sb.append(charSequence, sourceIndex2, nfdString.length());
            sb2.append(charSequence, sourceIndex2, nfdString.length());
        } else if (decompIndex < decomp.length()) {
            sb.append(charSequence2, decompIndex, decomp.length());
        }
        return z;
    }

    private boolean equalSubSequences(CharSequence left, int leftStart, CharSequence right, int rightStart) {
        int leftLength = left.length();
        if (leftLength - leftStart != right.length() - rightStart) {
            return false;
        }
        while (leftStart < leftLength) {
            int leftStart2 = leftStart + 1;
            int rightStart2 = rightStart + 1;
            if (left.charAt(leftStart) != right.charAt(rightStart)) {
                return false;
            }
            leftStart = leftStart2;
            rightStart = rightStart2;
        }
        return true;
    }

    private boolean ignorePrefix(CharSequence s) {
        return !isFCD(s);
    }

    private boolean ignoreString(CharSequence s) {
        return !isFCD(s) || Normalizer2Impl.Hangul.isHangul(s.charAt(0));
    }

    private boolean isFCD(CharSequence s) {
        return this.fcd.isNormalized(s);
    }

    private void closeOverComposites() {
        UnicodeSetIterator iter = new UnicodeSetIterator(COMPOSITES);
        while (true) {
            UnicodeSetIterator iter2 = iter;
            if (iter2.next()) {
                this.cesLength = this.dataBuilder.getCEs(this.nfd.getDecomposition(iter2.codepoint), this.ces, 0);
                if (this.cesLength <= 31) {
                    addIfDifferent("", iter2.getString(), this.ces, this.cesLength, -1);
                }
                iter = iter2;
            } else {
                return;
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
        for (int i = 0; i < ces1Length; i++) {
            if (ces1[i] != ces2[i]) {
                return false;
            }
        }
        return true;
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
        int pIndex;
        int nextIndex;
        long p;
        int t;
        int i;
        int s;
        int nextIndex2;
        int pIndex2;
        CollationBuilder collationBuilder;
        int sLimit;
        CollationWeights primaries;
        int i2;
        int pIndex3;
        int tLimit;
        CollationBuilder collationBuilder2 = this;
        CollationWeights primaries2 = new CollationWeights();
        CollationWeights secondaries = new CollationWeights();
        CollationWeights tertiaries = new CollationWeights();
        long[] nodesArray = collationBuilder2.nodes.getBuffer();
        int rpi = 0;
        while (true) {
            int rpi2 = rpi;
            if (rpi2 < collationBuilder2.rootPrimaryIndexes.size()) {
                long node = nodesArray[collationBuilder2.rootPrimaryIndexes.elementAti(rpi2)];
                long p2 = weight32FromNode(node);
                int s2 = p2 == 0 ? 0 : Collation.COMMON_WEIGHT16;
                int t2 = s2;
                boolean pIsTailored = false;
                boolean sIsTailored = false;
                boolean tIsTailored = false;
                int pIndex4 = p2 == 0 ? 0 : collationBuilder2.rootElements.findPrimary(p2);
                int nextIndex3 = nextIndexFromNode(node);
                long p3 = p2;
                int t3 = t2;
                int q = 0;
                long j = node;
                while (true) {
                    int nextIndex4 = nextIndex3;
                    if (nextIndex4 == 0) {
                        break;
                    }
                    int i3 = nextIndex4;
                    int rpi3 = rpi2;
                    long node2 = nodesArray[i3];
                    int nextIndex5 = nextIndexFromNode(node2);
                    int strength = strengthFromNode(node2);
                    if (strength != 3) {
                        if (strength == 2) {
                            if (isTailoredNode(node2)) {
                                if (!tIsTailored) {
                                    int tCount = 1 + countTailoredNodes(nodesArray, nextIndex5, 2);
                                    if (t3 == 0) {
                                        tLimit = ((int) collationBuilder2.rootElements.getFirstTertiaryCE()) & Collation.ONLY_TERTIARY_MASK;
                                        t3 = collationBuilder2.rootElements.getTertiaryBoundary() - 256;
                                    } else if (!pIsTailored && !sIsTailored) {
                                        tLimit = collationBuilder2.rootElements.getTertiaryAfter(pIndex4, s2, t3);
                                    } else if (t3 == 256) {
                                        tLimit = Collation.COMMON_WEIGHT16;
                                    } else {
                                        tLimit = collationBuilder2.rootElements.getTertiaryBoundary();
                                    }
                                    tertiaries.initForTertiary();
                                    primaries = primaries2;
                                    int i4 = q;
                                    int i5 = tLimit;
                                    int t4 = t3;
                                    i2 = strength;
                                    pIndex3 = pIndex4;
                                    if (tertiaries.allocWeights((long) t3, (long) tLimit, tCount)) {
                                        tIsTailored = true;
                                        int i6 = t4;
                                    } else {
                                        throw new UnsupportedOperationException("tertiary tailoring gap too small");
                                    }
                                } else {
                                    primaries = primaries2;
                                    int i7 = q;
                                    i2 = strength;
                                    pIndex3 = pIndex4;
                                }
                                t = (int) tertiaries.nextWeight();
                            } else {
                                primaries = primaries2;
                                int i8 = q;
                                i2 = strength;
                                pIndex3 = pIndex4;
                                t = weight16FromNode(node2);
                                tIsTailored = false;
                            }
                            pIndex = pIndex3;
                            int strength2 = i2;
                            nextIndex = nextIndex5;
                            primaries2 = primaries;
                            collationBuilder2 = this;
                        } else {
                            CollationWeights primaries3 = primaries2;
                            int i9 = q;
                            int strength3 = strength;
                            int pIndex5 = pIndex4;
                            if (strength3 == 1) {
                                if (isTailoredNode(node2)) {
                                    if (!sIsTailored) {
                                        int sCount = 1 + countTailoredNodes(nodesArray, nextIndex5, 1);
                                        if (s2 == 0) {
                                            collationBuilder = this;
                                            s2 = collationBuilder.rootElements.getSecondaryBoundary() - 256;
                                            sLimit = (int) (collationBuilder.rootElements.getFirstSecondaryCE() >> 16);
                                        } else {
                                            collationBuilder = this;
                                            if (!pIsTailored) {
                                                sLimit = collationBuilder.rootElements.getSecondaryAfter(pIndex5, s2);
                                            } else if (s2 == 256) {
                                                sLimit = Collation.COMMON_WEIGHT16;
                                            } else {
                                                sLimit = collationBuilder.rootElements.getSecondaryBoundary();
                                            }
                                        }
                                        int sLimit2 = sLimit;
                                        if (s2 == 1280) {
                                            s2 = collationBuilder.rootElements.getLastCommonSecondary();
                                        }
                                        secondaries.initForSecondary();
                                        pIndex2 = pIndex5;
                                        int i10 = strength3;
                                        CollationWeights collationWeights = secondaries;
                                        i = Collation.COMMON_WEIGHT16;
                                        int i11 = t3;
                                        int i12 = sLimit2;
                                        collationBuilder2 = collationBuilder;
                                        if (collationWeights.allocWeights((long) s2, (long) sLimit2, sCount)) {
                                            sIsTailored = true;
                                        } else {
                                            throw new UnsupportedOperationException("secondary tailoring gap too small");
                                        }
                                    } else {
                                        pIndex2 = pIndex5;
                                        int i13 = strength3;
                                        int i14 = t3;
                                        collationBuilder2 = this;
                                        i = Collation.COMMON_WEIGHT16;
                                    }
                                    s = (int) secondaries.nextWeight();
                                    nextIndex = nextIndex5;
                                } else {
                                    pIndex2 = pIndex5;
                                    int i15 = strength3;
                                    int i16 = t3;
                                    collationBuilder2 = this;
                                    i = Collation.COMMON_WEIGHT16;
                                    s = weight16FromNode(node2);
                                    nextIndex = nextIndex5;
                                    sIsTailored = false;
                                }
                                primaries2 = primaries3;
                                pIndex = pIndex2;
                            } else {
                                int pIndex6 = pIndex5;
                                int i17 = strength3;
                                int i18 = t3;
                                collationBuilder2 = this;
                                i = Collation.COMMON_WEIGHT16;
                                if (!pIsTailored) {
                                    int pCount = countTailoredNodes(nodesArray, nextIndex5, 0) + 1;
                                    long p4 = p3;
                                    boolean isCompressible = collationBuilder2.baseData.isCompressiblePrimary(p4);
                                    int pIndex7 = pIndex6;
                                    long pLimit = collationBuilder2.rootElements.getPrimaryAfter(p4, pIndex7, isCompressible);
                                    CollationWeights primaries4 = primaries3;
                                    primaries4.initForPrimary(isCompressible);
                                    nextIndex2 = nextIndex5;
                                    primaries2 = primaries4;
                                    long j2 = p4;
                                    pIndex = pIndex7;
                                    if (primaries4.allocWeights(p4, pLimit, pCount)) {
                                        pIsTailored = true;
                                    } else {
                                        throw new UnsupportedOperationException("primary tailoring gap too small");
                                    }
                                } else {
                                    nextIndex2 = nextIndex5;
                                    long j3 = p3;
                                    primaries2 = primaries3;
                                    pIndex = pIndex6;
                                }
                                p3 = primaries2.nextWeight();
                                s = 1280;
                                sIsTailored = false;
                            }
                            t = s2 == 0 ? 0 : i;
                            tIsTailored = false;
                        }
                        q = 0;
                        t3 = t;
                        p = p3;
                    } else if (q != 3) {
                        q++;
                        nextIndex = nextIndex5;
                        int i19 = strength;
                        pIndex = pIndex4;
                        p = p3;
                    } else {
                        throw new UnsupportedOperationException("quaternary tailoring gap too small");
                    }
                    if (isTailoredNode(node2)) {
                        nodesArray[i3] = Collation.makeCE(p, s2, t3, q);
                    }
                    p3 = p;
                    long j4 = node2;
                    int i20 = i3;
                    nextIndex3 = nextIndex;
                    rpi2 = rpi3;
                    pIndex4 = pIndex;
                }
            } else {
                return;
            }
            rpi = rpi2 + 1;
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
        return 4629700417037541376L + (((long) (1040384 & index)) << 43) + (((long) (index & 8128)) << 42) + ((long) ((index & 63) << 24)) + ((long) (strength << 8));
    }

    /* access modifiers changed from: private */
    public static int indexFromTempCE(long tempCE) {
        long tempCE2 = tempCE - 4629700417037541376L;
        return (((int) (tempCE2 >> 43)) & 1040384) | (((int) (tempCE2 >> 42)) & 8128) | (((int) (tempCE2 >> 24)) & 63);
    }

    private static int strengthFromTempCE(long tempCE) {
        return (((int) tempCE) >> 8) & 3;
    }

    /* access modifiers changed from: private */
    public static boolean isTempCE(long ce) {
        int sec = ((int) ce) >>> 24;
        return 6 <= sec && sec <= 69;
    }

    /* access modifiers changed from: private */
    public static int indexFromTempCE32(int tempCE32) {
        int tempCE322 = tempCE32 - 1077937696;
        return ((tempCE322 >> 11) & 1040384) | ((tempCE322 >> 10) & 8128) | ((tempCE322 >> 8) & 63);
    }

    /* access modifiers changed from: private */
    public static boolean isTempCE32(int ce32) {
        return (ce32 & 255) >= 2 && 6 <= ((ce32 >> 8) & 255) && ((ce32 >> 8) & 255) <= 69;
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
