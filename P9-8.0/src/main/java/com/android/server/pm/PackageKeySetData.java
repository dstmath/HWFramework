package com.android.server.pm;

import android.util.ArrayMap;
import com.android.internal.util.ArrayUtils;

public class PackageKeySetData {
    static final long KEYSET_UNASSIGNED = -1;
    private final ArrayMap<String, Long> mKeySetAliases;
    private long mProperSigningKeySet;
    private long[] mUpgradeKeySets;

    PackageKeySetData() {
        this.mKeySetAliases = new ArrayMap();
        this.mProperSigningKeySet = -1;
    }

    PackageKeySetData(PackageKeySetData original) {
        this.mKeySetAliases = new ArrayMap();
        this.mProperSigningKeySet = original.mProperSigningKeySet;
        this.mUpgradeKeySets = ArrayUtils.cloneOrNull(original.mUpgradeKeySets);
        this.mKeySetAliases.putAll(original.mKeySetAliases);
    }

    protected void setProperSigningKeySet(long ks) {
        this.mProperSigningKeySet = ks;
    }

    protected long getProperSigningKeySet() {
        return this.mProperSigningKeySet;
    }

    protected void addUpgradeKeySet(String alias) {
        if (alias != null) {
            Long ks = (Long) this.mKeySetAliases.get(alias);
            if (ks != null) {
                this.mUpgradeKeySets = ArrayUtils.appendLong(this.mUpgradeKeySets, ks.longValue());
                return;
            }
            throw new IllegalArgumentException("Upgrade keyset alias " + alias + "does not refer to a defined keyset alias!");
        }
    }

    protected void addUpgradeKeySetById(long ks) {
        this.mUpgradeKeySets = ArrayUtils.appendLong(this.mUpgradeKeySets, ks);
    }

    protected void removeAllUpgradeKeySets() {
        this.mUpgradeKeySets = null;
    }

    protected long[] getUpgradeKeySets() {
        return this.mUpgradeKeySets;
    }

    protected ArrayMap<String, Long> getAliases() {
        return this.mKeySetAliases;
    }

    protected void setAliases(ArrayMap<String, Long> newAliases) {
        removeAllDefinedKeySets();
        int newAliasSize = newAliases.size();
        for (int i = 0; i < newAliasSize; i++) {
            this.mKeySetAliases.put((String) newAliases.keyAt(i), (Long) newAliases.valueAt(i));
        }
    }

    protected void addDefinedKeySet(long ks, String alias) {
        this.mKeySetAliases.put(alias, Long.valueOf(ks));
    }

    protected void removeAllDefinedKeySets() {
        int aliasSize = this.mKeySetAliases.size();
        for (int i = 0; i < aliasSize; i++) {
            this.mKeySetAliases.removeAt(i);
        }
    }

    protected boolean isUsingDefinedKeySets() {
        return this.mKeySetAliases.size() > 0;
    }

    protected boolean isUsingUpgradeKeySets() {
        return this.mUpgradeKeySets != null && this.mUpgradeKeySets.length > 0;
    }
}
