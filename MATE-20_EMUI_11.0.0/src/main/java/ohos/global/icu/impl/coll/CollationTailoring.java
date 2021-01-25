package ohos.global.icu.impl.coll;

import java.util.Map;
import ohos.agp.components.InputAttribute;
import ohos.global.icu.impl.Norm2AllModes;
import ohos.global.icu.impl.Trie2_32;
import ohos.global.icu.impl.coll.SharedObject;
import ohos.global.icu.text.UnicodeSet;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;
import ohos.global.icu.util.VersionInfo;

public final class CollationTailoring {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public ULocale actualLocale = ULocale.ROOT;
    public CollationData data;
    public Map<Integer, Integer> maxExpansions;
    CollationData ownedData;
    private String rules;
    private UResourceBundle rulesResource;
    public SharedObject.Reference<CollationSettings> settings;
    Trie2_32 trie;
    UnicodeSet unsafeBackwardSet;
    public int version = 0;

    CollationTailoring(SharedObject.Reference<CollationSettings> reference) {
        if (reference != null) {
            this.settings = reference.clone();
        } else {
            this.settings = new SharedObject.Reference<>(new CollationSettings());
        }
    }

    /* access modifiers changed from: package-private */
    public void ensureOwnedData() {
        if (this.ownedData == null) {
            this.ownedData = new CollationData(Norm2AllModes.getNFCInstance().impl);
        }
        this.data = this.ownedData;
    }

    /* access modifiers changed from: package-private */
    public void setRules(String str) {
        this.rules = str;
    }

    /* access modifiers changed from: package-private */
    public void setRulesResource(UResourceBundle uResourceBundle) {
        this.rulesResource = uResourceBundle;
    }

    public String getRules() {
        String str = this.rules;
        if (str != null) {
            return str;
        }
        UResourceBundle uResourceBundle = this.rulesResource;
        return uResourceBundle != null ? uResourceBundle.getString() : "";
    }

    static VersionInfo makeBaseVersion(VersionInfo versionInfo) {
        return VersionInfo.getInstance(VersionInfo.UCOL_BUILDER_VERSION.getMajor(), (versionInfo.getMajor() << 3) + versionInfo.getMinor(), versionInfo.getMilli() << 6, 0);
    }

    /* access modifiers changed from: package-private */
    public void setVersion(int i, int i2) {
        int i3 = i2 >> 16;
        int i4 = 65280 & i3;
        int i5 = i3 & 255;
        int i6 = i2 & 255;
        this.version = (i & 16760832) | (VersionInfo.UCOL_BUILDER_VERSION.getMajor() << 24) | ((i4 + (i4 >> 6)) & 16128) | (((i5 << 3) + (i5 >> 5) + ((i2 >> 8) & 255) + (i6 << 4) + (i6 >> 4)) & 255);
    }

    /* access modifiers changed from: package-private */
    public int getUCAVersion() {
        int i = this.version;
        return ((i >> 14) & 3) | ((i >> 12) & InputAttribute.TYPE_MASK_VARIATION);
    }
}
