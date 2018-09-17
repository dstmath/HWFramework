package com.android.okhttp;

import com.android.okhttp.internal.Util;

public final class Challenge {
    private final String realm;
    private final String scheme;

    public Challenge(String scheme, String realm) {
        this.scheme = scheme;
        this.realm = realm;
    }

    public String getScheme() {
        return this.scheme;
    }

    public String getRealm() {
        return this.realm;
    }

    public boolean equals(Object o) {
        if ((o instanceof Challenge) && Util.equal(this.scheme, ((Challenge) o).scheme)) {
            return Util.equal(this.realm, ((Challenge) o).realm);
        }
        return false;
    }

    public int hashCode() {
        int hashCode;
        int i = 0;
        if (this.realm != null) {
            hashCode = this.realm.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode = (hashCode + 899) * 31;
        if (this.scheme != null) {
            i = this.scheme.hashCode();
        }
        return hashCode + i;
    }

    public String toString() {
        return this.scheme + " realm=\"" + this.realm + "\"";
    }
}
