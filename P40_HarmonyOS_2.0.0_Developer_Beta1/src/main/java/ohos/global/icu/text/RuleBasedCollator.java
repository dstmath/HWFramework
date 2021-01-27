package ohos.global.icu.text;

import java.lang.reflect.InvocationTargetException;
import java.text.CharacterIterator;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ohos.global.icu.impl.ClassLoaderUtil;
import ohos.global.icu.impl.Normalizer2Impl;
import ohos.global.icu.impl.coll.BOCSU;
import ohos.global.icu.impl.coll.CollationCompare;
import ohos.global.icu.impl.coll.CollationData;
import ohos.global.icu.impl.coll.CollationFastLatin;
import ohos.global.icu.impl.coll.CollationKeys;
import ohos.global.icu.impl.coll.CollationLoader;
import ohos.global.icu.impl.coll.CollationRoot;
import ohos.global.icu.impl.coll.CollationSettings;
import ohos.global.icu.impl.coll.CollationTailoring;
import ohos.global.icu.impl.coll.ContractionsAndExpansions;
import ohos.global.icu.impl.coll.FCDUTF16CollationIterator;
import ohos.global.icu.impl.coll.SharedObject;
import ohos.global.icu.impl.coll.TailoredSet;
import ohos.global.icu.impl.coll.UTF16CollationIterator;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.VersionInfo;

public final class RuleBasedCollator extends Collator {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private boolean actualLocaleIsSameAsValid;
    private CollationBuffer collationBuffer;
    CollationData data;
    private Lock frozenLock;
    SharedObject.Reference<CollationSettings> settings;
    CollationTailoring tailoring;
    private ULocale validLocale;

    @Deprecated
    public boolean isHiraganaQuaternary() {
        return false;
    }

    public RuleBasedCollator(String str) throws Exception {
        if (str != null) {
            this.validLocale = ULocale.ROOT;
            internalBuildTailoring(str);
            return;
        }
        throw new IllegalArgumentException("Collation rules can not be null");
    }

    private final void internalBuildTailoring(String str) throws Exception {
        CollationTailoring root = CollationRoot.getRoot();
        try {
            Class<?> loadClass = ClassLoaderUtil.getClassLoader(getClass()).loadClass("ohos.global.icu.impl.coll.CollationBuilder");
            CollationTailoring collationTailoring = (CollationTailoring) loadClass.getMethod("parseAndBuild", String.class).invoke(loadClass.getConstructor(CollationTailoring.class).newInstance(root), str);
            collationTailoring.actualLocale = null;
            adoptTailoring(collationTailoring);
        } catch (InvocationTargetException e) {
            throw ((Exception) e.getTargetException());
        }
    }

    @Override // ohos.global.icu.text.Collator, java.lang.Object
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

    public CollationElementIterator getCollationElementIterator(String str) {
        initMaxExpansions();
        return new CollationElementIterator(str, this);
    }

    public CollationElementIterator getCollationElementIterator(CharacterIterator characterIterator) {
        initMaxExpansions();
        return new CollationElementIterator((CharacterIterator) characterIterator.clone(), this);
    }

    public CollationElementIterator getCollationElementIterator(UCharacterIterator uCharacterIterator) {
        initMaxExpansions();
        return new CollationElementIterator(uCharacterIterator, this);
    }

    @Override // ohos.global.icu.text.Collator, ohos.global.icu.util.Freezable
    public boolean isFrozen() {
        return this.frozenLock != null;
    }

    @Override // ohos.global.icu.text.Collator, ohos.global.icu.util.Freezable
    public Collator freeze() {
        if (!isFrozen()) {
            this.frozenLock = new ReentrantLock();
            if (this.collationBuffer == null) {
                this.collationBuffer = new CollationBuffer(this.data);
            }
        }
        return this;
    }

    /* Return type fixed from 'ohos.global.icu.text.RuleBasedCollator' to match base method */
    @Override // ohos.global.icu.text.Collator, ohos.global.icu.util.Freezable
    public Collator cloneAsThawed() {
        try {
            RuleBasedCollator ruleBasedCollator = (RuleBasedCollator) super.clone();
            ruleBasedCollator.settings = this.settings.clone();
            ruleBasedCollator.collationBuffer = null;
            ruleBasedCollator.frozenLock = null;
            return ruleBasedCollator;
        } catch (CloneNotSupportedException unused) {
            return null;
        }
    }

    private void checkNotFrozen() {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen RuleBasedCollator");
        }
    }

    private final CollationSettings getOwnedSettings() {
        return this.settings.copyOnWrite();
    }

    private final CollationSettings getDefaultSettings() {
        return this.tailoring.settings.readOnly();
    }

    @Deprecated
    public void setHiraganaQuaternary(boolean z) {
        checkNotFrozen();
    }

    @Deprecated
    public void setHiraganaQuaternaryDefault() {
        checkNotFrozen();
    }

    public void setUpperCaseFirst(boolean z) {
        checkNotFrozen();
        if (z != isUpperCaseFirst()) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setCaseFirst(z ? 768 : 0);
            setFastLatinOptions(ownedSettings);
        }
    }

    public void setLowerCaseFirst(boolean z) {
        checkNotFrozen();
        if (z != isLowerCaseFirst()) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setCaseFirst(z ? 512 : 0);
            setFastLatinOptions(ownedSettings);
        }
    }

    public final void setCaseFirstDefault() {
        checkNotFrozen();
        SharedObject defaultSettings = getDefaultSettings();
        if (this.settings.readOnly() != defaultSettings) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setCaseFirstDefault(((CollationSettings) defaultSettings).options);
            setFastLatinOptions(ownedSettings);
        }
    }

    public void setAlternateHandlingDefault() {
        checkNotFrozen();
        SharedObject defaultSettings = getDefaultSettings();
        if (this.settings.readOnly() != defaultSettings) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setAlternateHandlingDefault(((CollationSettings) defaultSettings).options);
            setFastLatinOptions(ownedSettings);
        }
    }

    public void setCaseLevelDefault() {
        checkNotFrozen();
        SharedObject defaultSettings = getDefaultSettings();
        if (this.settings.readOnly() != defaultSettings) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setFlagDefault(1024, ((CollationSettings) defaultSettings).options);
            setFastLatinOptions(ownedSettings);
        }
    }

    public void setDecompositionDefault() {
        checkNotFrozen();
        SharedObject defaultSettings = getDefaultSettings();
        if (this.settings.readOnly() != defaultSettings) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setFlagDefault(1, ((CollationSettings) defaultSettings).options);
            setFastLatinOptions(ownedSettings);
        }
    }

    public void setFrenchCollationDefault() {
        checkNotFrozen();
        SharedObject defaultSettings = getDefaultSettings();
        if (this.settings.readOnly() != defaultSettings) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setFlagDefault(2048, ((CollationSettings) defaultSettings).options);
            setFastLatinOptions(ownedSettings);
        }
    }

    public void setStrengthDefault() {
        checkNotFrozen();
        SharedObject defaultSettings = getDefaultSettings();
        if (this.settings.readOnly() != defaultSettings) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setStrengthDefault(((CollationSettings) defaultSettings).options);
            setFastLatinOptions(ownedSettings);
        }
    }

    public void setNumericCollationDefault() {
        checkNotFrozen();
        SharedObject defaultSettings = getDefaultSettings();
        if (this.settings.readOnly() != defaultSettings) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setFlagDefault(2, ((CollationSettings) defaultSettings).options);
            setFastLatinOptions(ownedSettings);
        }
    }

    public void setFrenchCollation(boolean z) {
        checkNotFrozen();
        if (z != isFrenchCollation()) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setFlag(2048, z);
            setFastLatinOptions(ownedSettings);
        }
    }

    public void setAlternateHandlingShifted(boolean z) {
        checkNotFrozen();
        if (z != isAlternateHandlingShifted()) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setAlternateHandlingShifted(z);
            setFastLatinOptions(ownedSettings);
        }
    }

    public void setCaseLevel(boolean z) {
        checkNotFrozen();
        if (z != isCaseLevel()) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setFlag(1024, z);
            setFastLatinOptions(ownedSettings);
        }
    }

    @Override // ohos.global.icu.text.Collator
    public void setDecomposition(int i) {
        boolean z;
        checkNotFrozen();
        if (i == 16) {
            z = false;
        } else if (i == 17) {
            z = true;
        } else {
            throw new IllegalArgumentException("Wrong decomposition mode.");
        }
        if (z != this.settings.readOnly().getFlag(1)) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setFlag(1, z);
            setFastLatinOptions(ownedSettings);
        }
    }

    @Override // ohos.global.icu.text.Collator
    public void setStrength(int i) {
        checkNotFrozen();
        if (i != getStrength()) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setStrength(i);
            setFastLatinOptions(ownedSettings);
        }
    }

    @Override // ohos.global.icu.text.Collator
    public RuleBasedCollator setMaxVariable(int i) {
        int i2;
        if (i == -1) {
            i2 = -1;
        } else if (4096 > i || i > 4099) {
            throw new IllegalArgumentException("illegal max variable group " + i);
        } else {
            i2 = i - 4096;
        }
        if (i2 == this.settings.readOnly().getMaxVariable()) {
            return this;
        }
        SharedObject defaultSettings = getDefaultSettings();
        if (this.settings.readOnly() == defaultSettings && i2 < 0) {
            return this;
        }
        CollationSettings ownedSettings = getOwnedSettings();
        if (i == -1) {
            i = defaultSettings.getMaxVariable() + 4096;
        }
        long lastPrimaryForGroup = this.data.getLastPrimaryForGroup(i);
        ownedSettings.setMaxVariable(i2, ((CollationSettings) defaultSettings).options);
        ownedSettings.variableTop = lastPrimaryForGroup;
        setFastLatinOptions(ownedSettings);
        return this;
    }

    @Override // ohos.global.icu.text.Collator
    public int getMaxVariable() {
        return this.settings.readOnly().getMaxVariable() + 4096;
    }

    @Override // ohos.global.icu.text.Collator
    @Deprecated
    public int setVariableTop(String str) {
        long j;
        long j2;
        checkNotFrozen();
        if (str == null || str.length() == 0) {
            throw new IllegalArgumentException("Variable top argument string can not be null or zero in length.");
        }
        boolean isNumeric = this.settings.readOnly().isNumeric();
        if (this.settings.readOnly().dontCheckFCD()) {
            UTF16CollationIterator uTF16CollationIterator = new UTF16CollationIterator(this.data, isNumeric, str, 0);
            j = uTF16CollationIterator.nextCE();
            j2 = uTF16CollationIterator.nextCE();
        } else {
            FCDUTF16CollationIterator fCDUTF16CollationIterator = new FCDUTF16CollationIterator(this.data, isNumeric, str, 0);
            j = fCDUTF16CollationIterator.nextCE();
            j2 = fCDUTF16CollationIterator.nextCE();
        }
        if (j == 4311744768L || j2 != 4311744768L) {
            throw new IllegalArgumentException("Variable top argument string must map to exactly one collation element");
        }
        internalSetVariableTop(j >>> 32);
        return (int) this.settings.readOnly().variableTop;
    }

    @Override // ohos.global.icu.text.Collator
    @Deprecated
    public void setVariableTop(int i) {
        checkNotFrozen();
        internalSetVariableTop(((long) i) & 4294967295L);
    }

    private void internalSetVariableTop(long j) {
        if (j != this.settings.readOnly().variableTop) {
            int groupForPrimary = this.data.getGroupForPrimary(j);
            if (groupForPrimary < 4096 || 4099 < groupForPrimary) {
                throw new IllegalArgumentException("The variable top must be a primary weight in the space/punctuation/symbols/currency symbols range");
            }
            long lastPrimaryForGroup = this.data.getLastPrimaryForGroup(groupForPrimary);
            if (lastPrimaryForGroup != this.settings.readOnly().variableTop) {
                CollationSettings ownedSettings = getOwnedSettings();
                ownedSettings.setMaxVariable(groupForPrimary - 4096, getDefaultSettings().options);
                ownedSettings.variableTop = lastPrimaryForGroup;
                setFastLatinOptions(ownedSettings);
            }
        }
    }

    public void setNumericCollation(boolean z) {
        checkNotFrozen();
        if (z != getNumericCollation()) {
            CollationSettings ownedSettings = getOwnedSettings();
            ownedSettings.setFlag(2, z);
            setFastLatinOptions(ownedSettings);
        }
    }

    @Override // ohos.global.icu.text.Collator
    public void setReorderCodes(int... iArr) {
        checkNotFrozen();
        int length = iArr != null ? iArr.length : 0;
        if (length == 1 && iArr[0] == 103) {
            length = 0;
        }
        if (length == 0) {
            if (this.settings.readOnly().reorderCodes.length == 0) {
                return;
            }
        } else if (Arrays.equals(iArr, this.settings.readOnly().reorderCodes)) {
            return;
        }
        SharedObject defaultSettings = getDefaultSettings();
        if (length != 1 || iArr[0] != -1) {
            CollationSettings ownedSettings = getOwnedSettings();
            if (length == 0) {
                ownedSettings.resetReordering();
            } else {
                ownedSettings.setReordering(this.data, (int[]) iArr.clone());
            }
            setFastLatinOptions(ownedSettings);
        } else if (this.settings.readOnly() != defaultSettings) {
            CollationSettings ownedSettings2 = getOwnedSettings();
            ownedSettings2.copyReorderingFrom(defaultSettings);
            setFastLatinOptions(ownedSettings2);
        }
    }

    private void setFastLatinOptions(CollationSettings collationSettings) {
        collationSettings.fastLatinOptions = CollationFastLatin.getOptions(this.data, collationSettings, collationSettings.fastLatinPrimaries);
    }

    public String getRules() {
        return this.tailoring.getRules();
    }

    public String getRules(boolean z) {
        if (!z) {
            return this.tailoring.getRules();
        }
        return CollationLoader.getRootRules() + this.tailoring.getRules();
    }

    @Override // ohos.global.icu.text.Collator
    public UnicodeSet getTailoredSet() {
        UnicodeSet unicodeSet = new UnicodeSet();
        if (this.data.base != null) {
            new TailoredSet(unicodeSet).forData(this.data);
        }
        return unicodeSet;
    }

    public void getContractionsAndExpansions(UnicodeSet unicodeSet, UnicodeSet unicodeSet2, boolean z) throws Exception {
        if (unicodeSet != null) {
            unicodeSet.clear();
        }
        if (unicodeSet2 != null) {
            unicodeSet2.clear();
        }
        new ContractionsAndExpansions(unicodeSet, unicodeSet2, (ContractionsAndExpansions.CESink) null, z).forData(this.data);
    }

    /* access modifiers changed from: package-private */
    @Deprecated
    public void internalAddContractions(int i, UnicodeSet unicodeSet) {
        new ContractionsAndExpansions(unicodeSet, (UnicodeSet) null, (ContractionsAndExpansions.CESink) null, false).forCodePoint(this.data, i);
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:3:0x0004 */
    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: ohos.global.icu.text.RuleBasedCollator */
    /* JADX DEBUG: Multi-variable search result rejected for r0v1, resolved type: ohos.global.icu.text.RuleBasedCollator$CollationBuffer */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [ohos.global.icu.text.CollationKey] */
    /* JADX WARN: Type inference failed for: r0v2, types: [ohos.global.icu.text.RuleBasedCollator$CollationBuffer] */
    @Override // ohos.global.icu.text.Collator
    public CollationKey getCollationKey(String str) {
        CollationBuffer collationBuffer2 = 0;
        if (str == null) {
            return collationBuffer2;
        }
        try {
            collationBuffer2 = getCollationBuffer();
            return getCollationKey(str, collationBuffer2);
        } finally {
            releaseCollationBuffer(collationBuffer2);
        }
    }

    private CollationKey getCollationKey(String str, CollationBuffer collationBuffer2) {
        collationBuffer2.rawCollationKey = getRawCollationKey(str, collationBuffer2.rawCollationKey, collationBuffer2);
        return new CollationKey(str, collationBuffer2.rawCollationKey);
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:3:0x0004 */
    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: ohos.global.icu.text.RuleBasedCollator */
    /* JADX DEBUG: Multi-variable search result rejected for r0v1, resolved type: ohos.global.icu.text.RuleBasedCollator$CollationBuffer */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [ohos.global.icu.text.RawCollationKey] */
    /* JADX WARN: Type inference failed for: r0v2, types: [ohos.global.icu.text.RuleBasedCollator$CollationBuffer] */
    @Override // ohos.global.icu.text.Collator
    public RawCollationKey getRawCollationKey(String str, RawCollationKey rawCollationKey) {
        CollationBuffer collationBuffer2 = 0;
        if (str == null) {
            return collationBuffer2;
        }
        try {
            collationBuffer2 = getCollationBuffer();
            return getRawCollationKey(str, rawCollationKey, collationBuffer2);
        } finally {
            releaseCollationBuffer(collationBuffer2);
        }
    }

    /* access modifiers changed from: private */
    public static final class CollationKeyByteSink extends CollationKeys.SortKeyByteSink {
        private RawCollationKey key_;

        CollationKeyByteSink(RawCollationKey rawCollationKey) {
            super(rawCollationKey.bytes);
            this.key_ = rawCollationKey;
        }

        /* access modifiers changed from: protected */
        public void AppendBeyondCapacity(byte[] bArr, int i, int i2, int i3) {
            if (Resize(i2, i3)) {
                System.arraycopy(bArr, i, this.buffer_, i3, i2);
            }
        }

        /* access modifiers changed from: protected */
        public boolean Resize(int i, int i2) {
            int length = this.buffer_.length * 2;
            int i3 = (i * 2) + i2;
            if (length >= i3) {
                i3 = length;
            }
            if (i3 < 200) {
                i3 = 200;
            }
            byte[] bArr = new byte[i3];
            System.arraycopy(this.buffer_, 0, bArr, 0, i2);
            this.key_.bytes = bArr;
            this.buffer_ = bArr;
            return true;
        }
    }

    private RawCollationKey getRawCollationKey(CharSequence charSequence, RawCollationKey rawCollationKey, CollationBuffer collationBuffer2) {
        if (rawCollationKey == null) {
            rawCollationKey = new RawCollationKey(simpleKeyLengthEstimate(charSequence));
        } else if (rawCollationKey.bytes == null) {
            rawCollationKey.bytes = new byte[simpleKeyLengthEstimate(charSequence)];
        }
        CollationKeyByteSink collationKeyByteSink = new CollationKeyByteSink(rawCollationKey);
        writeSortKey(charSequence, collationKeyByteSink, collationBuffer2);
        rawCollationKey.size = collationKeyByteSink.NumberOfBytesAppended();
        return rawCollationKey;
    }

    private int simpleKeyLengthEstimate(CharSequence charSequence) {
        return (charSequence.length() * 2) + 10;
    }

    private void writeSortKey(CharSequence charSequence, CollationKeyByteSink collationKeyByteSink, CollationBuffer collationBuffer2) {
        boolean isNumeric = this.settings.readOnly().isNumeric();
        if (this.settings.readOnly().dontCheckFCD()) {
            collationBuffer2.leftUTF16CollIter.setText(isNumeric, charSequence, 0);
            CollationKeys.writeSortKeyUpToQuaternary(collationBuffer2.leftUTF16CollIter, this.data.compressibleBytes, this.settings.readOnly(), collationKeyByteSink, 1, CollationKeys.SIMPLE_LEVEL_FALLBACK, true);
        } else {
            collationBuffer2.leftFCDUTF16Iter.setText(isNumeric, charSequence, 0);
            CollationKeys.writeSortKeyUpToQuaternary(collationBuffer2.leftFCDUTF16Iter, this.data.compressibleBytes, this.settings.readOnly(), collationKeyByteSink, 1, CollationKeys.SIMPLE_LEVEL_FALLBACK, true);
        }
        if (this.settings.readOnly().getStrength() == 15) {
            writeIdenticalLevel(charSequence, collationKeyByteSink);
        }
        collationKeyByteSink.Append(0);
    }

    private void writeIdenticalLevel(CharSequence charSequence, CollationKeyByteSink collationKeyByteSink) {
        int decompose = this.data.nfcImpl.decompose(charSequence, 0, charSequence.length(), (Normalizer2Impl.ReorderingBuffer) null);
        collationKeyByteSink.Append(1);
        collationKeyByteSink.key_.size = collationKeyByteSink.NumberOfBytesAppended();
        int writeIdenticalLevelRun = decompose != 0 ? BOCSU.writeIdenticalLevelRun(0, charSequence, 0, decompose, collationKeyByteSink.key_) : 0;
        if (decompose < charSequence.length()) {
            int length = charSequence.length() - decompose;
            StringBuilder sb = new StringBuilder();
            this.data.nfcImpl.decompose(charSequence, decompose, charSequence.length(), sb, length);
            BOCSU.writeIdenticalLevelRun(writeIdenticalLevelRun, sb, 0, sb.length(), collationKeyByteSink.key_);
        }
        collationKeyByteSink.setBufferAndAppended(collationKeyByteSink.key_.bytes, collationKeyByteSink.key_.size);
    }

    @Deprecated
    public long[] internalGetCEs(CharSequence charSequence) {
        Throwable th;
        CollationBuffer collationBuffer2;
        UTF16CollationIterator uTF16CollationIterator;
        try {
            collationBuffer2 = getCollationBuffer();
            try {
                boolean isNumeric = this.settings.readOnly().isNumeric();
                if (this.settings.readOnly().dontCheckFCD()) {
                    collationBuffer2.leftUTF16CollIter.setText(isNumeric, charSequence, 0);
                    uTF16CollationIterator = collationBuffer2.leftUTF16CollIter;
                } else {
                    collationBuffer2.leftFCDUTF16Iter.setText(isNumeric, charSequence, 0);
                    uTF16CollationIterator = collationBuffer2.leftFCDUTF16Iter;
                }
                int fetchCEs = uTF16CollationIterator.fetchCEs() - 1;
                long[] jArr = new long[fetchCEs];
                System.arraycopy(uTF16CollationIterator.getCEs(), 0, jArr, 0, fetchCEs);
                releaseCollationBuffer(collationBuffer2);
                return jArr;
            } catch (Throwable th2) {
                th = th2;
                releaseCollationBuffer(collationBuffer2);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            collationBuffer2 = null;
            releaseCollationBuffer(collationBuffer2);
            throw th;
        }
    }

    @Override // ohos.global.icu.text.Collator
    public int getStrength() {
        return this.settings.readOnly().getStrength();
    }

    @Override // ohos.global.icu.text.Collator
    public int getDecomposition() {
        return (this.settings.readOnly().options & 1) != 0 ? 17 : 16;
    }

    public boolean isUpperCaseFirst() {
        return this.settings.readOnly().getCaseFirst() == 768;
    }

    public boolean isLowerCaseFirst() {
        return this.settings.readOnly().getCaseFirst() == 512;
    }

    public boolean isAlternateHandlingShifted() {
        return this.settings.readOnly().getAlternateHandling();
    }

    public boolean isCaseLevel() {
        return (this.settings.readOnly().options & 1024) != 0;
    }

    public boolean isFrenchCollation() {
        return (this.settings.readOnly().options & 2048) != 0;
    }

    @Override // ohos.global.icu.text.Collator
    public int getVariableTop() {
        return (int) this.settings.readOnly().variableTop;
    }

    public boolean getNumericCollation() {
        return (this.settings.readOnly().options & 2) != 0;
    }

    @Override // ohos.global.icu.text.Collator
    public int[] getReorderCodes() {
        return (int[]) this.settings.readOnly().reorderCodes.clone();
    }

    @Override // ohos.global.icu.text.Collator, java.util.Comparator, java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        RuleBasedCollator ruleBasedCollator = (RuleBasedCollator) obj;
        if (!this.settings.readOnly().equals(ruleBasedCollator.settings.readOnly())) {
            return false;
        }
        CollationData collationData = this.data;
        if (collationData == ruleBasedCollator.data) {
            return true;
        }
        boolean z = collationData.base == null;
        boolean z2 = ruleBasedCollator.data.base == null;
        if (z != z2) {
            return false;
        }
        String rules = this.tailoring.getRules();
        String rules2 = ruleBasedCollator.tailoring.getRules();
        return ((z || rules.length() != 0) && ((z2 || rules2.length() != 0) && rules.equals(rules2))) || getTailoredSet().equals(ruleBasedCollator.getTailoredSet());
    }

    @Override // ohos.global.icu.text.Collator, java.lang.Object
    public int hashCode() {
        int hashCode = this.settings.readOnly().hashCode();
        if (this.data.base == null) {
            return hashCode;
        }
        UnicodeSetIterator unicodeSetIterator = new UnicodeSetIterator(getTailoredSet());
        while (unicodeSetIterator.next() && unicodeSetIterator.codepoint != UnicodeSetIterator.IS_STRING) {
            hashCode ^= this.data.getCE32(unicodeSetIterator.codepoint);
        }
        return hashCode;
    }

    @Override // ohos.global.icu.text.Collator
    public int compare(String str, String str2) {
        return doCompare(str, str2);
    }

    /* access modifiers changed from: private */
    public static abstract class NFDIterator {
        private String decomp;
        private int index;

        /* access modifiers changed from: protected */
        public abstract int nextRawCodePoint();

        NFDIterator() {
        }

        /* access modifiers changed from: package-private */
        public final void reset() {
            this.index = -1;
        }

        /* access modifiers changed from: package-private */
        public final int nextCodePoint() {
            int i = this.index;
            if (i >= 0) {
                if (i == this.decomp.length()) {
                    this.index = -1;
                } else {
                    int codePointAt = Character.codePointAt(this.decomp, this.index);
                    this.index += Character.charCount(codePointAt);
                    return codePointAt;
                }
            }
            return nextRawCodePoint();
        }

        /* access modifiers changed from: package-private */
        public final int nextDecomposedCodePoint(Normalizer2Impl normalizer2Impl, int i) {
            if (this.index >= 0) {
                return i;
            }
            this.decomp = normalizer2Impl.getDecomposition(i);
            String str = this.decomp;
            if (str == null) {
                return i;
            }
            int codePointAt = Character.codePointAt(str, 0);
            this.index = Character.charCount(codePointAt);
            return codePointAt;
        }
    }

    /* access modifiers changed from: private */
    public static class UTF16NFDIterator extends NFDIterator {
        protected int pos;
        protected CharSequence s;

        UTF16NFDIterator() {
        }

        /* access modifiers changed from: package-private */
        public void setText(CharSequence charSequence, int i) {
            reset();
            this.s = charSequence;
            this.pos = i;
        }

        /* access modifiers changed from: protected */
        @Override // ohos.global.icu.text.RuleBasedCollator.NFDIterator
        public int nextRawCodePoint() {
            if (this.pos == this.s.length()) {
                return -1;
            }
            int codePointAt = Character.codePointAt(this.s, this.pos);
            this.pos += Character.charCount(codePointAt);
            return codePointAt;
        }
    }

    /* access modifiers changed from: private */
    public static final class FCDUTF16NFDIterator extends UTF16NFDIterator {
        private StringBuilder str;

        FCDUTF16NFDIterator() {
        }

        /* access modifiers changed from: package-private */
        public void setText(Normalizer2Impl normalizer2Impl, CharSequence charSequence, int i) {
            reset();
            int makeFCD = normalizer2Impl.makeFCD(charSequence, i, charSequence.length(), (Normalizer2Impl.ReorderingBuffer) null);
            if (makeFCD == charSequence.length()) {
                this.s = charSequence;
                this.pos = i;
                return;
            }
            StringBuilder sb = this.str;
            if (sb == null) {
                this.str = new StringBuilder();
            } else {
                sb.setLength(0);
            }
            this.str.append(charSequence, i, makeFCD);
            normalizer2Impl.makeFCD(charSequence, makeFCD, charSequence.length(), new Normalizer2Impl.ReorderingBuffer(normalizer2Impl, this.str, charSequence.length() - i));
            this.s = this.str;
            this.pos = 0;
        }
    }

    private static final int compareNFDIter(Normalizer2Impl normalizer2Impl, NFDIterator nFDIterator, NFDIterator nFDIterator2) {
        while (true) {
            int nextCodePoint = nFDIterator.nextCodePoint();
            int nextCodePoint2 = nFDIterator2.nextCodePoint();
            if (nextCodePoint != nextCodePoint2) {
                int i = -2;
                int nextDecomposedCodePoint = nextCodePoint < 0 ? -2 : nextCodePoint == 65534 ? -1 : nFDIterator.nextDecomposedCodePoint(normalizer2Impl, nextCodePoint);
                if (nextCodePoint2 >= 0) {
                    i = nextCodePoint2 == 65534 ? -1 : nFDIterator2.nextDecomposedCodePoint(normalizer2Impl, nextCodePoint2);
                }
                if (nextDecomposedCodePoint < i) {
                    return -1;
                }
                if (nextDecomposedCodePoint > i) {
                    return 1;
                }
            } else if (nextCodePoint < 0) {
                return 0;
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.text.Collator
    @Deprecated
    public int doCompare(CharSequence charSequence, CharSequence charSequence2) {
        Throwable th;
        CollationBuffer collationBuffer2;
        int i;
        if (charSequence == charSequence2) {
            return 0;
        }
        int i2 = 0;
        while (true) {
            if (i2 != charSequence.length()) {
                if (i2 == charSequence2.length() || charSequence.charAt(i2) != charSequence2.charAt(i2)) {
                    break;
                }
                i2++;
            } else if (i2 == charSequence2.length()) {
                return 0;
            }
        }
        CollationSettings readOnly = this.settings.readOnly();
        boolean isNumeric = readOnly.isNumeric();
        if (i2 > 0 && ((i2 != charSequence.length() && this.data.isUnsafeBackward(charSequence.charAt(i2), isNumeric)) || (i2 != charSequence2.length() && this.data.isUnsafeBackward(charSequence2.charAt(i2), isNumeric)))) {
            do {
                i2--;
                if (i2 <= 0) {
                    break;
                }
            } while (this.data.isUnsafeBackward(charSequence.charAt(i2), isNumeric));
        }
        int i3 = readOnly.fastLatinOptions;
        int compareUTF16 = (i3 < 0 || (i2 != charSequence.length() && charSequence.charAt(i2) > 383) || (i2 != charSequence2.length() && charSequence2.charAt(i2) > 383)) ? -2 : CollationFastLatin.compareUTF16(this.data.fastLatinTable, readOnly.fastLatinPrimaries, i3, charSequence, charSequence2, i2);
        CollationBuffer collationBuffer3 = null;
        if (compareUTF16 == -2) {
            try {
                collationBuffer2 = getCollationBuffer();
                try {
                    if (readOnly.dontCheckFCD()) {
                        collationBuffer2.leftUTF16CollIter.setText(isNumeric, charSequence, i2);
                        collationBuffer2.rightUTF16CollIter.setText(isNumeric, charSequence2, i2);
                        i = CollationCompare.compareUpToQuaternary(collationBuffer2.leftUTF16CollIter, collationBuffer2.rightUTF16CollIter, readOnly);
                    } else {
                        collationBuffer2.leftFCDUTF16Iter.setText(isNumeric, charSequence, i2);
                        collationBuffer2.rightFCDUTF16Iter.setText(isNumeric, charSequence2, i2);
                        i = CollationCompare.compareUpToQuaternary(collationBuffer2.leftFCDUTF16Iter, collationBuffer2.rightFCDUTF16Iter, readOnly);
                    }
                    compareUTF16 = i;
                    releaseCollationBuffer(collationBuffer2);
                } catch (Throwable th2) {
                    th = th2;
                    releaseCollationBuffer(collationBuffer2);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                collationBuffer2 = collationBuffer3;
                releaseCollationBuffer(collationBuffer2);
                throw th;
            }
        }
        if (compareUTF16 != 0 || readOnly.getStrength() < 15) {
            return compareUTF16;
        }
        try {
            collationBuffer3 = getCollationBuffer();
            Normalizer2Impl normalizer2Impl = this.data.nfcImpl;
            if (readOnly.dontCheckFCD()) {
                collationBuffer3.leftUTF16NFDIter.setText(charSequence, i2);
                collationBuffer3.rightUTF16NFDIter.setText(charSequence2, i2);
                return compareNFDIter(normalizer2Impl, collationBuffer3.leftUTF16NFDIter, collationBuffer3.rightUTF16NFDIter);
            }
            collationBuffer3.leftFCDUTF16NFDIter.setText(normalizer2Impl, charSequence, i2);
            collationBuffer3.rightFCDUTF16NFDIter.setText(normalizer2Impl, charSequence2, i2);
            int compareNFDIter = compareNFDIter(normalizer2Impl, collationBuffer3.leftFCDUTF16NFDIter, collationBuffer3.rightFCDUTF16NFDIter);
            releaseCollationBuffer(collationBuffer3);
            return compareNFDIter;
        } finally {
            releaseCollationBuffer(collationBuffer3);
        }
    }

    RuleBasedCollator(CollationTailoring collationTailoring, ULocale uLocale) {
        this.data = collationTailoring.data;
        this.settings = collationTailoring.settings.clone();
        this.tailoring = collationTailoring;
        this.validLocale = uLocale;
        this.actualLocaleIsSameAsValid = false;
    }

    private void adoptTailoring(CollationTailoring collationTailoring) {
        this.data = collationTailoring.data;
        this.settings = collationTailoring.settings.clone();
        this.tailoring = collationTailoring;
        this.validLocale = collationTailoring.actualLocale;
        this.actualLocaleIsSameAsValid = false;
    }

    /* access modifiers changed from: package-private */
    public final boolean isUnsafe(int i) {
        return this.data.isUnsafeBackward(i, this.settings.readOnly().isNumeric());
    }

    /* access modifiers changed from: private */
    public static final class CollationBuffer {
        FCDUTF16CollationIterator leftFCDUTF16Iter;
        FCDUTF16NFDIterator leftFCDUTF16NFDIter;
        UTF16CollationIterator leftUTF16CollIter;
        UTF16NFDIterator leftUTF16NFDIter;
        RawCollationKey rawCollationKey;
        FCDUTF16CollationIterator rightFCDUTF16Iter;
        FCDUTF16NFDIterator rightFCDUTF16NFDIter;
        UTF16CollationIterator rightUTF16CollIter;
        UTF16NFDIterator rightUTF16NFDIter;

        private CollationBuffer(CollationData collationData) {
            this.leftUTF16CollIter = new UTF16CollationIterator(collationData);
            this.rightUTF16CollIter = new UTF16CollationIterator(collationData);
            this.leftFCDUTF16Iter = new FCDUTF16CollationIterator(collationData);
            this.rightFCDUTF16Iter = new FCDUTF16CollationIterator(collationData);
            this.leftUTF16NFDIter = new UTF16NFDIterator();
            this.rightUTF16NFDIter = new UTF16NFDIterator();
            this.leftFCDUTF16NFDIter = new FCDUTF16NFDIterator();
            this.rightFCDUTF16NFDIter = new FCDUTF16NFDIterator();
        }
    }

    @Override // ohos.global.icu.text.Collator
    public VersionInfo getVersion() {
        int i = this.tailoring.version;
        int major = VersionInfo.UCOL_RUNTIME_VERSION.getMajor();
        return VersionInfo.getInstance((i >>> 24) + (major << 4) + (major >> 4), (i >> 16) & 255, (i >> 8) & 255, i & 255);
    }

    @Override // ohos.global.icu.text.Collator
    public VersionInfo getUCAVersion() {
        VersionInfo version = getVersion();
        return VersionInfo.getInstance(version.getMinor() >> 3, version.getMinor() & 7, version.getMilli() >> 6, 0);
    }

    private final CollationBuffer getCollationBuffer() {
        if (isFrozen()) {
            this.frozenLock.lock();
        } else if (this.collationBuffer == null) {
            this.collationBuffer = new CollationBuffer(this.data);
        }
        return this.collationBuffer;
    }

    private final void releaseCollationBuffer(CollationBuffer collationBuffer2) {
        if (isFrozen()) {
            this.frozenLock.unlock();
        }
    }

    @Override // ohos.global.icu.text.Collator
    public ULocale getLocale(ULocale.Type type) {
        if (type == ULocale.ACTUAL_LOCALE) {
            return this.actualLocaleIsSameAsValid ? this.validLocale : this.tailoring.actualLocale;
        }
        if (type == ULocale.VALID_LOCALE) {
            return this.validLocale;
        }
        throw new IllegalArgumentException("unknown ULocale.Type " + type);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.text.Collator
    public void setLocale(ULocale uLocale, ULocale uLocale2) {
        if (Objects.equals(uLocale2, this.tailoring.actualLocale)) {
            this.actualLocaleIsSameAsValid = false;
        } else {
            this.actualLocaleIsSameAsValid = true;
        }
        this.validLocale = uLocale;
    }
}
