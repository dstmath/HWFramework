package com.android.okhttp;

import com.android.okhttp.internal.Util;

public final class Challenge {
    private final String realm;
    private final String scheme;

    public Challenge(String scheme2, String realm2) {
        this.scheme = scheme2;
        this.realm = realm2;
    }

    public String getScheme() {
        return this.scheme;
    }

    public String getRealm() {
        return this.realm;
    }

    public boolean equals(Object o) {
        return (o instanceof Challenge) && Util.equal(this.scheme, ((Challenge) o).scheme) && Util.equal(this.realm, ((Challenge) o).realm);
    }

    public int hashCode() {
        int i = 0;
        int hashCode = 31 * ((31 * 29) + (this.realm != null ? this.realm.hashCode() : 0));
        if (this.scheme != null) {
            i = this.scheme.hashCode();
        }
        return hashCode + i;
    }

    public String toString() {
        return this.scheme + " realm=\"" + this.realm + "\"";
    }
}
