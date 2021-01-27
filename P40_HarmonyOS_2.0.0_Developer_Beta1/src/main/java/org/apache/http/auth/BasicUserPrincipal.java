package org.apache.http.auth;

import java.security.Principal;
import org.apache.http.util.LangUtils;

@Deprecated
public final class BasicUserPrincipal implements Principal {
    private final String username;

    public BasicUserPrincipal(String username2) {
        if (username2 != null) {
            this.username = username2;
            return;
        }
        throw new IllegalArgumentException("User name may not be null");
    }

    @Override // java.security.Principal
    public String getName() {
        return this.username;
    }

    @Override // java.security.Principal, java.lang.Object
    public int hashCode() {
        return LangUtils.hashCode(17, this.username);
    }

    @Override // java.security.Principal, java.lang.Object
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (!(o instanceof BasicUserPrincipal) || !LangUtils.equals(this.username, ((BasicUserPrincipal) o).username)) {
            return false;
        }
        return true;
    }

    @Override // java.security.Principal, java.lang.Object
    public String toString() {
        return "[principal: " + this.username + "]";
    }
}
