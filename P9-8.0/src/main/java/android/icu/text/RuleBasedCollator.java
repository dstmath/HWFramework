package android.icu.text;

import android.icu.impl.ClassLoaderUtil;
import android.icu.impl.Normalizer2Impl;
import android.icu.impl.Normalizer2Impl.ReorderingBuffer;
import android.icu.impl.Utility;
import android.icu.impl.coll.BOCSU;
import android.icu.impl.coll.Collation;
import android.icu.impl.coll.CollationCompare;
import android.icu.impl.coll.CollationData;
import android.icu.impl.coll.CollationFastLatin;
import android.icu.impl.coll.CollationIterator;
import android.icu.impl.coll.CollationKeys;
import android.icu.impl.coll.CollationKeys.SortKeyByteSink;
import android.icu.impl.coll.CollationLoader;
import android.icu.impl.coll.CollationRoot;
import android.icu.impl.coll.CollationSettings;
import android.icu.impl.coll.CollationTailoring;
import android.icu.impl.coll.ContractionsAndExpansions;
import android.icu.impl.coll.FCDUTF16CollationIterator;
import android.icu.impl.coll.SharedObject;
import android.icu.impl.coll.SharedObject.Reference;
import android.icu.impl.coll.TailoredSet;
import android.icu.impl.coll.UTF16CollationIterator;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Type;
import android.icu.util.VersionInfo;
import java.lang.reflect.InvocationTargetException;
import java.text.CharacterIterator;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class RuleBasedCollator extends Collator {
    static final /* synthetic */ boolean -assertionsDisabled = (RuleBasedCollator.class.desiredAssertionStatus() ^ 1);
    private boolean actualLocaleIsSameAsValid;
    private CollationBuffer collationBuffer;
    CollationData data;
    private Lock frozenLock;
    Reference<CollationSettings> settings;
    CollationTailoring tailoring;
    private ULocale validLocale;

    private static final class CollationBuffer {
        FCDUTF16CollationIterator leftFCDUTF16Iter;
        FCDUTF16NFDIterator leftFCDUTF16NFDIter;
        UTF16CollationIterator leftUTF16CollIter;
        UTF16NFDIterator leftUTF16NFDIter;
        RawCollationKey rawCollationKey;
        FCDUTF16CollationIterator rightFCDUTF16Iter;
        FCDUTF16NFDIterator rightFCDUTF16NFDIter;
        UTF16CollationIterator rightUTF16CollIter;
        UTF16NFDIterator rightUTF16NFDIter;

        /* synthetic */ CollationBuffer(CollationData data, CollationBuffer -this1) {
            this(data);
        }

        private CollationBuffer(CollationData data) {
            this.leftUTF16CollIter = new UTF16CollationIterator(data);
            this.rightUTF16CollIter = new UTF16CollationIterator(data);
            this.leftFCDUTF16Iter = new FCDUTF16CollationIterator(data);
            this.rightFCDUTF16Iter = new FCDUTF16CollationIterator(data);
            this.leftUTF16NFDIter = new UTF16NFDIterator();
            this.rightUTF16NFDIter = new UTF16NFDIterator();
            this.leftFCDUTF16NFDIter = new FCDUTF16NFDIterator();
            this.rightFCDUTF16NFDIter = new FCDUTF16NFDIterator();
        }
    }

    private static final class CollationKeyByteSink extends SortKeyByteSink {
        private RawCollationKey key_;

        CollationKeyByteSink(RawCollationKey key) {
            super(key.bytes);
            this.key_ = key;
        }

        protected void AppendBeyondCapacity(byte[] bytes, int start, int n, int length) {
            if (Resize(n, length)) {
                System.arraycopy(bytes, start, this.buffer_, length, n);
            }
        }

        protected boolean Resize(int appendCapacity, int length) {
            int newCapacity = this.buffer_.length * 2;
            int altCapacity = length + (appendCapacity * 2);
            if (newCapacity < altCapacity) {
                newCapacity = altCapacity;
            }
            if (newCapacity < 200) {
                newCapacity = 200;
            }
            byte[] newBytes = new byte[newCapacity];
            System.arraycopy(this.buffer_, 0, newBytes, 0, length);
            this.key_.bytes = newBytes;
            this.buffer_ = newBytes;
            return true;
        }
    }

    private static abstract class NFDIterator {
        private String decomp;
        private int index;

        protected abstract int nextRawCodePoint();

        NFDIterator() {
        }

        final void reset() {
            this.index = -1;
        }

        final int nextCodePoint() {
            if (this.index >= 0) {
                if (this.index == this.decomp.length()) {
                    this.index = -1;
                } else {
                    int c = Character.codePointAt(this.decomp, this.index);
                    this.index += Character.charCount(c);
                    return c;
                }
            }
            return nextRawCodePoint();
        }

        final int nextDecomposedCodePoint(Normalizer2Impl nfcImpl, int c) {
            if (this.index >= 0) {
                return c;
            }
            this.decomp = nfcImpl.getDecomposition(c);
            if (this.decomp == null) {
                return c;
            }
            c = Character.codePointAt(this.decomp, 0);
            this.index = Character.charCount(c);
            return c;
        }
    }

    private static class UTF16NFDIterator extends NFDIterator {
        protected int pos;
        protected CharSequence s;

        UTF16NFDIterator() {
        }

        void setText(CharSequence seq, int start) {
            reset();
            this.s = seq;
            this.pos = start;
        }

        protected int nextRawCodePoint() {
            if (this.pos == this.s.length()) {
                return -1;
            }
            int c = Character.codePointAt(this.s, this.pos);
            this.pos += Character.charCount(c);
            return c;
        }
    }

    private static final class FCDUTF16NFDIterator extends UTF16NFDIterator {
        private StringBuilder str;

        FCDUTF16NFDIterator() {
        }

        void setText(Normalizer2Impl nfcImpl, CharSequence seq, int start) {
            reset();
            int spanLimit = nfcImpl.makeFCD(seq, start, seq.length(), null);
            if (spanLimit == seq.length()) {
                this.s = seq;
                this.pos = start;
                return;
            }
            if (this.str == null) {
                this.str = new StringBuilder();
            } else {
                this.str.setLength(0);
            }
            this.str.append(seq, start, spanLimit);
            nfcImpl.makeFCD(seq, spanLimit, seq.length(), new ReorderingBuffer(nfcImpl, this.str, seq.length() - start));
            this.s = this.str;
            this.pos = 0;
        }
    }

    public RuleBasedCollator(String rules) throws Exception {
        if (rules == null) {
            throw new IllegalArgumentException("Collation rules can not be null");
        }
        this.validLocale = ULocale.ROOT;
        internalBuildTailoring(rules);
    }

    private final void internalBuildTailoring(String rules) throws Exception {
        CollationTailoring base = CollationRoot.getRoot();
        try {
            Class<?> builderClass = ClassLoaderUtil.getClassLoader(getClass()).loadClass("android.icu.impl.coll.CollationBuilder");
            Object builder = builderClass.getConstructor(new Class[]{CollationTailoring.class}).newInstance(new Object[]{base});
            CollationTailoring t = (CollationTailoring) builderClass.getMethod("parseAndBuild", new Class[]{String.class}).invoke(builder, new Object[]{rules});
            t.actualLocale = null;
            adoptTailoring(t);
        } catch (InvocationTargetException e) {
            throw ((Exception) e.getTargetException());
        }
    }

    public Object clone() throws CloneNotSupportedException {
        if (isFrozen()) {
            return this;
        }
        return cloneAsThawed();
    }

    private final void initMaxExpansions() {
        synchronized (this.tailoring) {
            if (this.tailoring.maxExpansions == null) {
                this.tailoring.maxExpansions = CollationElementIterator.computeMaxExpansions(this.tailoring.data);
            }
        }
    }

    public CollationElementIterator getCollationElementIterator(String source) {
        initMaxExpansions();
        return new CollationElementIterator(source, this);
    }

    public CollationElementIterator getCollationElementIterator(CharacterIterator source) {
        initMaxExpansions();
        return new CollationElementIterator((CharacterIterator) source.clone(), this);
    }

    public CollationElementIterator getCollationElementIterator(UCharacterIterator source) {
        initMaxExpansions();
        return new CollationElementIterator(source, this);
    }

    public boolean isFrozen() {
        return this.frozenLock != null;
    }

    public Collator freeze() {
        if (!isFrozen()) {
            this.frozenLock = new ReentrantLock();
            if (this.collationBuffer == null) {
                this.collationBuffer = new CollationBuffer(this.data, null);
            }
        }
        return this;
    }

    public RuleBasedCollator cloneAsThawed() {
        try {
            RuleBasedCollator result = (RuleBasedCollator) super.clone();
            result.settings = this.settings.clone();
            result.collationBuffer = null;
            result.frozenLock = null;
            return result;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    private void checkNotFrozen() {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen RuleBasedCollator");
        }
    }

    private final CollationSettings getOwnedSettings() {
        return (CollationSettings) this.settings.copyOnWrite();
    }

    private final CollationSettings getDefaultSettings() {
        return (CollationSettings) this.tailoring.settings.readOnly();
    }

    @Deprecated
    public void setHiraganaQuaternary(boolean flag) {
        checkNotFrozen();
    }

    @Deprecated
    public void setHiraganaQuaternaryDefault() {
        checkNotFrozen();
    }

    public void setUpperCaseFirst(boolean upperfirst) {
        checkNotFrozen();
        if (upperfirst != isUpperCaseFirst()) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setCaseFirst(upperfirst ? 768 : 0);
            setFastLatinOptions(ownedSettings);
        }
    }

    public void setLowerCaseFirst(boolean lowerfirst) {
        checkNotFrozen();
        if (lowerfirst != isLowerCaseFirst()) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setCaseFirst(lowerfirst ? 512 : 0);
            setFastLatinOptions(ownedSettings);
        }
    }

    public final void setCaseFirstDefault() {
        checkNotFrozen();
        SharedObject defaultSettings = getDefaultSettings();
        if (this.settings.readOnly() != defaultSettings) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setCaseFirstDefault(defaultSettings.options);
            setFastLatinOptions(ownedSettings);
        }
    }

    public void setAlternateHandlingDefault() {
        checkNotFrozen();
        SharedObject defaultSettings = getDefaultSettings();
        if (this.settings.readOnly() != defaultSettings) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setAlternateHandlingDefault(defaultSettings.options);
            setFastLatinOptions(ownedSettings);
        }
    }

    public void setCaseLevelDefault() {
        checkNotFrozen();
        SharedObject defaultSettings = getDefaultSettings();
        if (this.settings.readOnly() != defaultSettings) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setFlagDefault(1024, defaultSettings.options);
            setFastLatinOptions(ownedSettings);
        }
    }

    public void setDecompositionDefault() {
        checkNotFrozen();
        SharedObject defaultSettings = getDefaultSettings();
        if (this.settings.readOnly() != defaultSettings) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setFlagDefault(1, defaultSettings.options);
            setFastLatinOptions(ownedSettings);
        }
    }

    public void setFrenchCollationDefault() {
        checkNotFrozen();
        SharedObject defaultSettings = getDefaultSettings();
        if (this.settings.readOnly() != defaultSettings) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setFlagDefault(2048, defaultSettings.options);
            setFastLatinOptions(ownedSettings);
        }
    }

    public void setStrengthDefault() {
        checkNotFrozen();
        SharedObject defaultSettings = getDefaultSettings();
        if (this.settings.readOnly() != defaultSettings) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setStrengthDefault(defaultSettings.options);
            setFastLatinOptions(ownedSettings);
        }
    }

    public void setNumericCollationDefault() {
        checkNotFrozen();
        SharedObject defaultSettings = getDefaultSettings();
        if (this.settings.readOnly() != defaultSettings) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setFlagDefault(2, defaultSettings.options);
            setFastLatinOptions(ownedSettings);
        }
    }

    public void setFrenchCollation(boolean flag) {
        checkNotFrozen();
        if (flag != isFrenchCollation()) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setFlag(2048, flag);
            setFastLatinOptions(ownedSettings);
        }
    }

    public void setAlternateHandlingShifted(boolean shifted) {
        checkNotFrozen();
        if (shifted != isAlternateHandlingShifted()) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setAlternateHandlingShifted(shifted);
            setFastLatinOptions(ownedSettings);
        }
    }

    public void setCaseLevel(boolean flag) {
        checkNotFrozen();
        if (flag != isCaseLevel()) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setFlag(1024, flag);
            setFastLatinOptions(ownedSettings);
        }
    }

    public void setDecomposition(int decomposition) {
        boolean flag;
        checkNotFrozen();
        switch (decomposition) {
            case 16:
                flag = false;
                break;
            case 17:
                flag = true;
                break;
            default:
                throw new IllegalArgumentException("Wrong decomposition mode.");
        }
        if (flag != ((CollationSettings) this.settings.readOnly()).getFlag(1)) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setFlag(1, flag);
            setFastLatinOptions(ownedSettings);
        }
    }

    public void setStrength(int newStrength) {
        checkNotFrozen();
        if (newStrength != getStrength()) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setStrength(newStrength);
            setFastLatinOptions(ownedSettings);
        }
    }

    public RuleBasedCollator setMaxVariable(int group) {
        int value;
        if (group == -1) {
            value = -1;
        } else if (4096 > group || group > 4099) {
            throw new IllegalArgumentException("illegal max variable group " + group);
        } else {
            value = group - 4096;
        }
        if (value == ((CollationSettings) this.settings.readOnly()).getMaxVariable()) {
            return this;
        }
        SharedObject defaultSettings = getDefaultSettings();
        if (this.settings.readOnly() == defaultSettings && value < 0) {
            return this;
        }
        CollationSettings ownedSettings = getOwnedSettings();
        if (group == -1) {
            group = defaultSettings.getMaxVariable() + 4096;
        }
        long varTop = this.data.getLastPrimaryForGroup(group);
        if (-assertionsDisabled || varTop != 0) {
            ownedSettings.setMaxVariable(value, defaultSettings.options);
            ownedSettings.variableTop = varTop;
            setFastLatinOptions(ownedSettings);
            return this;
        }
        throw new AssertionError();
    }

    public int getMaxVariable() {
        return ((CollationSettings) this.settings.readOnly()).getMaxVariable() + 4096;
    }

    @Deprecated
    public int setVariableTop(String varTop) {
        checkNotFrozen();
        if (varTop == null || varTop.length() == 0) {
            throw new IllegalArgumentException("Variable top argument string can not be null or zero in length.");
        }
        long ce1;
        boolean numeric = ((CollationSettings) this.settings.readOnly()).isNumeric();
        long ce2;
        if (((CollationSettings) this.settings.readOnly()).dontCheckFCD()) {
            UTF16CollationIterator ci = new UTF16CollationIterator(this.data, numeric, varTop, 0);
            ce1 = ci.nextCE();
            ce2 = ci.nextCE();
        } else {
            FCDUTF16CollationIterator ci2 = new FCDUTF16CollationIterator(this.data, numeric, varTop, 0);
            ce1 = ci2.nextCE();
            ce2 = ci2.nextCE();
        }
        if (ce1 == Collation.NO_CE || ce2 != Collation.NO_CE) {
            throw new IllegalArgumentException("Variable top argument string must map to exactly one collation element");
        }
        internalSetVariableTop(ce1 >>> 32);
        return (int) ((CollationSettings) this.settings.readOnly()).variableTop;
    }

    @Deprecated
    public void setVariableTop(int varTop) {
        checkNotFrozen();
        internalSetVariableTop(((long) varTop) & 4294967295L);
    }

    private void internalSetVariableTop(long varTop) {
        if (varTop != ((CollationSettings) this.settings.readOnly()).variableTop) {
            int group = this.data.getGroupForPrimary(varTop);
            if (group < 4096 || 4099 < group) {
                throw new IllegalArgumentException("The variable top must be a primary weight in the space/punctuation/symbols/currency symbols range");
            }
            long v = this.data.getLastPrimaryForGroup(group);
            if (-assertionsDisabled || (v != 0 && v >= varTop)) {
                varTop = v;
                if (v != ((CollationSettings) this.settings.readOnly()).variableTop) {
                    CollationSettings ownedSettings = getOwnedSettings();
                    ownedSettings.setMaxVariable(group - 4096, getDefaultSettings().options);
                    ownedSettings.variableTop = v;
                    setFastLatinOptions(ownedSettings);
                    return;
                }
                return;
            }
            throw new AssertionError();
        }
    }

    public void setNumericCollation(boolean flag) {
        checkNotFrozen();
        if (flag != getNumericCollation()) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setFlag(2, flag);
            setFastLatinOptions(ownedSettings);
        }
    }

    public void setReorderCodes(int... order) {
        checkNotFrozen();
        int length = order != null ? order.length : 0;
        if (length == 1 && order[0] == 103) {
            length = 0;
        }
        if (!length != 0 ? ((CollationSettings) this.settings.readOnly()).reorderCodes.length == 0 : Arrays.equals(order, ((CollationSettings) this.settings.readOnly()).reorderCodes)) {
            SharedObject defaultSettings = getDefaultSettings();
            CollationSettings ownedSettings;
            if (length == 1 && order[0] == -1) {
                if (this.settings.readOnly() != defaultSettings) {
                    ownedSettings = getOwnedSettings();
                    ownedSettings.copyReorderingFrom(defaultSettings);
                    setFastLatinOptions(ownedSettings);
                }
                return;
            }
            ownedSettings = getOwnedSettings();
            if (length == 0) {
                ownedSettings.resetReordering();
            } else {
                ownedSettings.setReordering(this.data, (int[]) order.clone());
            }
            setFastLatinOptions(ownedSettings);
        }
    }

    private void setFastLatinOptions(CollationSettings ownedSettings) {
        ownedSettings.fastLatinOptions = CollationFastLatin.getOptions(this.data, ownedSettings, ownedSettings.fastLatinPrimaries);
    }

    public String getRules() {
        return this.tailoring.getRules();
    }

    public String getRules(boolean fullrules) {
        if (fullrules) {
            return CollationLoader.getRootRules() + this.tailoring.getRules();
        }
        return this.tailoring.getRules();
    }

    public UnicodeSet getTailoredSet() {
        UnicodeSet tailored = new UnicodeSet();
        if (this.data.base != null) {
            new TailoredSet(tailored).forData(this.data);
        }
        return tailored;
    }

    public void getContractionsAndExpansions(UnicodeSet contractions, UnicodeSet expansions, boolean addPrefixes) throws Exception {
        if (contractions != null) {
            contractions.clear();
        }
        if (expansions != null) {
            expansions.clear();
        }
        new ContractionsAndExpansions(contractions, expansions, null, addPrefixes).forData(this.data);
    }

    void internalAddContractions(int c, UnicodeSet set) {
        new ContractionsAndExpansions(set, null, null, false).forCodePoint(this.data, c);
    }

    public CollationKey getCollationKey(String source) {
        if (source == null) {
            return null;
        }
        CollationBuffer collationBuffer = null;
        try {
            collationBuffer = getCollationBuffer();
            CollationKey collationKey = getCollationKey(source, collationBuffer);
            return collationKey;
        } finally {
            releaseCollationBuffer(collationBuffer);
        }
    }

    private CollationKey getCollationKey(String source, CollationBuffer buffer) {
        buffer.rawCollationKey = getRawCollationKey(source, buffer.rawCollationKey, buffer);
        return new CollationKey(source, buffer.rawCollationKey);
    }

    public RawCollationKey getRawCollationKey(String source, RawCollationKey key) {
        if (source == null) {
            return null;
        }
        CollationBuffer collationBuffer = null;
        try {
            collationBuffer = getCollationBuffer();
            RawCollationKey rawCollationKey = getRawCollationKey(source, key, collationBuffer);
            return rawCollationKey;
        } finally {
            releaseCollationBuffer(collationBuffer);
        }
    }

    private RawCollationKey getRawCollationKey(CharSequence source, RawCollationKey key, CollationBuffer buffer) {
        if (key == null) {
            key = new RawCollationKey(simpleKeyLengthEstimate(source));
        } else if (key.bytes == null) {
            key.bytes = new byte[simpleKeyLengthEstimate(source)];
        }
        CollationKeyByteSink sink = new CollationKeyByteSink(key);
        writeSortKey(source, sink, buffer);
        key.size = sink.NumberOfBytesAppended();
        return key;
    }

    private int simpleKeyLengthEstimate(CharSequence source) {
        return (source.length() * 2) + 10;
    }

    private void writeSortKey(CharSequence s, CollationKeyByteSink sink, CollationBuffer buffer) {
        boolean numeric = ((CollationSettings) this.settings.readOnly()).isNumeric();
        if (((CollationSettings) this.settings.readOnly()).dontCheckFCD()) {
            buffer.leftUTF16CollIter.setText(numeric, s, 0);
            CollationKeys.writeSortKeyUpToQuaternary(buffer.leftUTF16CollIter, this.data.compressibleBytes, (CollationSettings) this.settings.readOnly(), sink, 1, CollationKeys.SIMPLE_LEVEL_FALLBACK, true);
        } else {
            buffer.leftFCDUTF16Iter.setText(numeric, s, 0);
            CollationKeys.writeSortKeyUpToQuaternary(buffer.leftFCDUTF16Iter, this.data.compressibleBytes, (CollationSettings) this.settings.readOnly(), sink, 1, CollationKeys.SIMPLE_LEVEL_FALLBACK, true);
        }
        if (((CollationSettings) this.settings.readOnly()).getStrength() == 15) {
            writeIdenticalLevel(s, sink);
        }
        sink.Append(0);
    }

    private void writeIdenticalLevel(CharSequence s, CollationKeyByteSink sink) {
        int nfdQCYesLimit = this.data.nfcImpl.decompose(s, 0, s.length(), null);
        sink.Append(1);
        sink.key_.size = sink.NumberOfBytesAppended();
        int prev = 0;
        if (nfdQCYesLimit != 0) {
            prev = BOCSU.writeIdenticalLevelRun(0, s, 0, nfdQCYesLimit, sink.key_);
        }
        if (nfdQCYesLimit < s.length()) {
            int destLengthEstimate = s.length() - nfdQCYesLimit;
            StringBuilder nfd = new StringBuilder();
            this.data.nfcImpl.decompose(s, nfdQCYesLimit, s.length(), nfd, destLengthEstimate);
            BOCSU.writeIdenticalLevelRun(prev, nfd, 0, nfd.length(), sink.key_);
        }
        sink.setBufferAndAppended(sink.key_.bytes, sink.key_.size);
    }

    @Deprecated
    public long[] internalGetCEs(CharSequence str) {
        CollationBuffer buffer = null;
        try {
            CollationIterator iter;
            buffer = getCollationBuffer();
            boolean numeric = ((CollationSettings) this.settings.readOnly()).isNumeric();
            if (((CollationSettings) this.settings.readOnly()).dontCheckFCD()) {
                buffer.leftUTF16CollIter.setText(numeric, str, 0);
                iter = buffer.leftUTF16CollIter;
            } else {
                buffer.leftFCDUTF16Iter.setText(numeric, str, 0);
                iter = buffer.leftFCDUTF16Iter;
            }
            int length = iter.fetchCEs() - 1;
            if (-assertionsDisabled || (length >= 0 && iter.getCE(length) == Collation.NO_CE)) {
                long[] ces = new long[length];
                System.arraycopy(iter.getCEs(), 0, ces, 0, length);
                return ces;
            }
            throw new AssertionError();
        } finally {
            releaseCollationBuffer(buffer);
        }
    }

    public int getStrength() {
        return ((CollationSettings) this.settings.readOnly()).getStrength();
    }

    public int getDecomposition() {
        return (((CollationSettings) this.settings.readOnly()).options & 1) != 0 ? 17 : 16;
    }

    public boolean isUpperCaseFirst() {
        return ((CollationSettings) this.settings.readOnly()).getCaseFirst() == 768;
    }

    public boolean isLowerCaseFirst() {
        return ((CollationSettings) this.settings.readOnly()).getCaseFirst() == 512;
    }

    public boolean isAlternateHandlingShifted() {
        return ((CollationSettings) this.settings.readOnly()).getAlternateHandling();
    }

    public boolean isCaseLevel() {
        return (((CollationSettings) this.settings.readOnly()).options & 1024) != 0;
    }

    public boolean isFrenchCollation() {
        return (((CollationSettings) this.settings.readOnly()).options & 2048) != 0;
    }

    @Deprecated
    public boolean isHiraganaQuaternary() {
        return false;
    }

    public int getVariableTop() {
        return (int) ((CollationSettings) this.settings.readOnly()).variableTop;
    }

    public boolean getNumericCollation() {
        return (((CollationSettings) this.settings.readOnly()).options & 2) != 0;
    }

    public int[] getReorderCodes() {
        return (int[]) ((CollationSettings) this.settings.readOnly()).reorderCodes.clone();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        RuleBasedCollator o = (RuleBasedCollator) obj;
        if (!((CollationSettings) this.settings.readOnly()).equals(o.settings.readOnly())) {
            return false;
        }
        if (this.data == o.data) {
            return true;
        }
        boolean thisIsRoot = this.data.base == null;
        boolean otherIsRoot = o.data.base == null;
        if (!-assertionsDisabled && thisIsRoot && otherIsRoot) {
            throw new AssertionError();
        } else if (thisIsRoot != otherIsRoot) {
            return false;
        } else {
            String theseRules = this.tailoring.getRules();
            String otherRules = o.tailoring.getRules();
            return ((thisIsRoot || theseRules.length() != 0) && ((otherIsRoot || otherRules.length() != 0) && theseRules.equals(otherRules))) || getTailoredSet().equals(o.getTailoredSet());
        }
    }

    public int hashCode() {
        int h = ((CollationSettings) this.settings.readOnly()).hashCode();
        if (this.data.base == null) {
            return h;
        }
        UnicodeSetIterator iter = new UnicodeSetIterator(getTailoredSet());
        while (iter.next() && iter.codepoint != UnicodeSetIterator.IS_STRING) {
            h ^= this.data.getCE32(iter.codepoint);
        }
        return h;
    }

    public int compare(String source, String target) {
        return doCompare(source, target);
    }

    private static final int compareNFDIter(Normalizer2Impl nfcImpl, NFDIterator left, NFDIterator right) {
        while (true) {
            int leftCp = left.nextCodePoint();
            int rightCp = right.nextCodePoint();
            if (leftCp != rightCp) {
                if (leftCp < 0) {
                    leftCp = -2;
                } else if (leftCp == 65534) {
                    leftCp = -1;
                } else {
                    leftCp = left.nextDecomposedCodePoint(nfcImpl, leftCp);
                }
                if (rightCp < 0) {
                    rightCp = -2;
                } else if (rightCp == 65534) {
                    rightCp = -1;
                } else {
                    rightCp = right.nextDecomposedCodePoint(nfcImpl, rightCp);
                }
                if (leftCp < rightCp) {
                    return -1;
                }
                if (leftCp > rightCp) {
                    return 1;
                }
            } else if (leftCp < 0) {
                return 0;
            }
        }
    }

    @Deprecated
    protected int doCompare(CharSequence left, CharSequence right) {
        if (left == right) {
            return 0;
        }
        int result;
        CollationBuffer buffer;
        int equalPrefixLength = 0;
        while (equalPrefixLength != left.length()) {
            if (equalPrefixLength == right.length() || left.charAt(equalPrefixLength) != right.charAt(equalPrefixLength)) {
                break;
            }
            equalPrefixLength++;
        }
        if (equalPrefixLength == right.length()) {
            return 0;
        }
        CollationSettings roSettings = (CollationSettings) this.settings.readOnly();
        boolean numeric = roSettings.isNumeric();
        if (equalPrefixLength > 0 && ((equalPrefixLength != left.length() && this.data.isUnsafeBackward(left.charAt(equalPrefixLength), numeric)) || (equalPrefixLength != right.length() && this.data.isUnsafeBackward(right.charAt(equalPrefixLength), numeric)))) {
            while (true) {
                equalPrefixLength--;
                if (equalPrefixLength > 0) {
                    if (!this.data.isUnsafeBackward(left.charAt(equalPrefixLength), numeric)) {
                        break;
                    }
                }
                break;
            }
        }
        int fastLatinOptions = roSettings.fastLatinOptions;
        if (fastLatinOptions < 0 || ((equalPrefixLength != left.length() && left.charAt(equalPrefixLength) > 383) || (equalPrefixLength != right.length() && right.charAt(equalPrefixLength) > 383))) {
            result = -2;
        } else {
            result = CollationFastLatin.compareUTF16(this.data.fastLatinTable, roSettings.fastLatinPrimaries, fastLatinOptions, left, right, equalPrefixLength);
        }
        if (result == -2) {
            buffer = null;
            try {
                buffer = getCollationBuffer();
                if (roSettings.dontCheckFCD()) {
                    buffer.leftUTF16CollIter.setText(numeric, left, equalPrefixLength);
                    buffer.rightUTF16CollIter.setText(numeric, right, equalPrefixLength);
                    result = CollationCompare.compareUpToQuaternary(buffer.leftUTF16CollIter, buffer.rightUTF16CollIter, roSettings);
                } else {
                    buffer.leftFCDUTF16Iter.setText(numeric, left, equalPrefixLength);
                    buffer.rightFCDUTF16Iter.setText(numeric, right, equalPrefixLength);
                    result = CollationCompare.compareUpToQuaternary(buffer.leftFCDUTF16Iter, buffer.rightFCDUTF16Iter, roSettings);
                }
                releaseCollationBuffer(buffer);
            } catch (Throwable th) {
                releaseCollationBuffer(buffer);
            }
        }
        if (result != 0 || roSettings.getStrength() < 15) {
            return result;
        }
        buffer = null;
        try {
            buffer = getCollationBuffer();
            Normalizer2Impl nfcImpl = this.data.nfcImpl;
            int compareNFDIter;
            if (roSettings.dontCheckFCD()) {
                buffer.leftUTF16NFDIter.setText(left, equalPrefixLength);
                buffer.rightUTF16NFDIter.setText(right, equalPrefixLength);
                compareNFDIter = compareNFDIter(nfcImpl, buffer.leftUTF16NFDIter, buffer.rightUTF16NFDIter);
                return compareNFDIter;
            }
            buffer.leftFCDUTF16NFDIter.setText(nfcImpl, left, equalPrefixLength);
            buffer.rightFCDUTF16NFDIter.setText(nfcImpl, right, equalPrefixLength);
            compareNFDIter = compareNFDIter(nfcImpl, buffer.leftFCDUTF16NFDIter, buffer.rightFCDUTF16NFDIter);
            releaseCollationBuffer(buffer);
            return compareNFDIter;
        } finally {
            releaseCollationBuffer(buffer);
        }
    }

    RuleBasedCollator(CollationTailoring t, ULocale vl) {
        this.data = t.data;
        this.settings = t.settings.clone();
        this.tailoring = t;
        this.validLocale = vl;
        this.actualLocaleIsSameAsValid = false;
    }

    private void adoptTailoring(CollationTailoring t) {
        if (-assertionsDisabled || (this.settings == null && this.data == null && this.tailoring == null)) {
            this.data = t.data;
            this.settings = t.settings.clone();
            this.tailoring = t;
            this.validLocale = t.actualLocale;
            this.actualLocaleIsSameAsValid = false;
            return;
        }
        throw new AssertionError();
    }

    final boolean isUnsafe(int c) {
        return this.data.isUnsafeBackward(c, ((CollationSettings) this.settings.readOnly()).isNumeric());
    }

    public VersionInfo getVersion() {
        int version = this.tailoring.version;
        int rtVersion = VersionInfo.UCOL_RUNTIME_VERSION.getMajor();
        return VersionInfo.getInstance(((version >>> 24) + (rtVersion << 4)) + (rtVersion >> 4), (version >> 16) & 255, (version >> 8) & 255, version & 255);
    }

    public VersionInfo getUCAVersion() {
        VersionInfo v = getVersion();
        return VersionInfo.getInstance(v.getMinor() >> 3, v.getMinor() & 7, v.getMilli() >> 6, 0);
    }

    private final CollationBuffer getCollationBuffer() {
        if (isFrozen()) {
            this.frozenLock.lock();
        } else if (this.collationBuffer == null) {
            this.collationBuffer = new CollationBuffer(this.data, null);
        }
        return this.collationBuffer;
    }

    private final void releaseCollationBuffer(CollationBuffer buffer) {
        if (isFrozen()) {
            this.frozenLock.unlock();
        }
    }

    public ULocale getLocale(Type type) {
        if (type == ULocale.ACTUAL_LOCALE) {
            return this.actualLocaleIsSameAsValid ? this.validLocale : this.tailoring.actualLocale;
        } else if (type == ULocale.VALID_LOCALE) {
            return this.validLocale;
        } else {
            throw new IllegalArgumentException("unknown ULocale.Type " + type);
        }
    }

    void setLocale(ULocale valid, ULocale actual) {
        if (!-assertionsDisabled) {
            if ((valid == null) != (actual == null)) {
                throw new AssertionError();
            }
        }
        if (Utility.objectEquals(actual, this.tailoring.actualLocale)) {
            this.actualLocaleIsSameAsValid = false;
        } else if (-assertionsDisabled || Utility.objectEquals(actual, valid)) {
            this.actualLocaleIsSameAsValid = true;
        } else {
            throw new AssertionError();
        }
        this.validLocale = valid;
    }
}
