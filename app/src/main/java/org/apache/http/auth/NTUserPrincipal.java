package org.apache.http.auth;

import java.security.Principal;
import java.util.Locale;
import org.apache.http.util.LangUtils;

@Deprecated
public class NTUserPrincipal implements Principal {
    private final String domain;
    private final String ntname;
    private final String username;

    public NTUserPrincipal(String domain, String username) {
        if (username == null) {
            throw new IllegalArgumentException("User name may not be null");
        }
        this.username = username;
        if (domain != null) {
            this.domain = domain.toUpperCase(Locale.ENGLISH);
        } else {
            this.domain = null;
        }
        if (this.domain == null || this.domain.length() <= 0) {
            this.ntname = this.username;
            return;
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append(this.domain);
        buffer.append('/');
        buffer.append(this.username);
        this.ntname = buffer.toString();
    }

    public String getName() {
        return this.ntname;
    }

    public String getDomain() {
        return this.domain;
    }

    public String getUsername() {
        return this.username;
    }

    public int hashCode() {
        return LangUtils.hashCode(LangUtils.hashCode(17, this.username), this.domain);
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (o instanceof NTUserPrincipal) {
            NTUserPrincipal that = (NTUserPrincipal) o;
            return LangUtils.equals(this.username, that.username) && LangUtils.equals(this.domain, that.domain);
        }
    }

    public String toString() {
        return this.ntname;
    }
}
