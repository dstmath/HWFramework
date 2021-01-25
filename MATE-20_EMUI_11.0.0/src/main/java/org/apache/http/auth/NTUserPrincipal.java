package org.apache.http.auth;

import java.security.Principal;
import java.util.Locale;
import org.apache.http.util.LangUtils;

@Deprecated
public class NTUserPrincipal implements Principal {
    private final String domain;
    private final String ntname;
    private final String username;

    public NTUserPrincipal(String domain2, String username2) {
        if (username2 != null) {
            this.username = username2;
            if (domain2 != null) {
                this.domain = domain2.toUpperCase(Locale.ENGLISH);
            } else {
                this.domain = null;
            }
            String str = this.domain;
            if (str == null || str.length() <= 0) {
                this.ntname = this.username;
                return;
            }
            this.ntname = this.domain + '/' + this.username;
            return;
        }
        throw new IllegalArgumentException("User name may not be null");
    }

    @Override // java.security.Principal
    public String getName() {
        return this.ntname;
    }

    public String getDomain() {
        return this.domain;
    }

    public String getUsername() {
        return this.username;
    }

    @Override // java.security.Principal, java.lang.Object
    public int hashCode() {
        return LangUtils.hashCode(LangUtils.hashCode(17, this.username), this.domain);
    }

    @Override // java.security.Principal, java.lang.Object
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (o instanceof NTUserPrincipal) {
            NTUserPrincipal that = (NTUserPrincipal) o;
            if (LangUtils.equals(this.username, that.username) && LangUtils.equals(this.domain, that.domain)) {
                return true;
            }
        }
        return false;
    }

    @Override // java.security.Principal, java.lang.Object
    public String toString() {
        return this.ntname;
    }
}
