package com.android.server.pm;

import android.util.ArrayMap;
import com.android.internal.util.ArrayUtils;

public class PackageKeySetData {
    static final long KEYSET_UNASSIGNED = -1;
    private final ArrayMap<String, Long> mKeySetAliases;
    private long mProperSigningKeySet;
    private long[] mUpgradeKeySets;

    PackageKeySetData() {
        this.mKeySetAliases = new ArrayMap<>();
        this.mProperSigningKeySet = -1;
    }

    PackageKeySetData(PackageKeySetData original) {
        this.mKeySetAliases = new ArrayMap<>();
        this.mProperSigningKeySet = original.mProperSigningKeySet;
        this.mUpgradeKeySets = ArrayUtils.cloneOrNull(original.mUpgradeKeySets);
        this.mKeySetAliases.putAll((ArrayMap<? extends String, ? extends Long>) original.mKeySetAliases);
    }

    /* access modifiers changed from: protected */
    public void setProperSigningKeySet(long ks) {
        this.mProperSigningKeySet = ks;
    }

    /* access modifiers changed from: protected */
    public long getProperSigningKeySet() {
        return this.mProperSigningKeySet;
    }

    /* access modifiers changed from: protected */
    public void addUpgradeKeySet(String alias) {
        if (alias != null) {
            Long ks = this.mKeySetAliases.get(alias);
            if (ks != null) {
                this.mUpgradeKeySets = ArrayUtils.appendLong(this.mUpgradeKeySets, ks.longValue());
                return;
            }
            throw new IllegalArgumentException("Upgrade keyset alias " + alias + "does not refer to a defined keyset alias!");
        }
    }

    /* access modifiers changed from: protected */
    public void addUpgradeKeySetById(long ks) {
        this.mUpgradeKeySets = ArrayUtils.appendLong(this.mUpgradeKeySets, ks);
    }

    /* access modifiers changed from: protected */
    public void removeAllUpgradeKeySets() {
        this.mUpgradeKeySets = null;
    }

    /* access modifiers changed from: protected */
    public long[] getUpgradeKeySets() {
        return this.mUpgradeKeySets;
    }

    /* access modifiers changed from: protected */
    public ArrayMap<String, Long> getAliases() {
        return this.mKeySetAliases;
    }

    /* access modifiers changed from: protected */
    public void setAliases(ArrayMap<String, Long> newAliases) {
        removeAllDefinedKeySets();
        int newAliasSize = newAliases.size();
        for (int i = 0; i < newAliasSize; i++) {
            this.mKeySetAliases.put(newAliases.keyAt(i), newAliases.valueAt(i));
        }
    }

    /* access modifiers changed from: protected */
    public void addDefinedKeySet(long ks, String alias) {
        this.mKeySetAliases.put(alias, Long.valueOf(ks));
    }

    /* access modifiers changed from: protected */
    public void removeAllDefinedKeySets() {
        this.mKeySetAliases.erase();
    }

    /* access modifiers changed from: protected */
    public boolean isUsingDefinedKeySets() {
        return this.mKeySetAliases.size() > 0;
    }

    /* access modifiers changed from: protected */
    public boolean isUsingUpgradeKeySets() {
        long[] jArr = this.mUpgradeKeySets;
        return jArr != null && jArr.length > 0;
    }
}
