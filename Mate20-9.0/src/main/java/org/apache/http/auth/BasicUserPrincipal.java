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

    public String getName() {
        return this.username;
    }

    public int hashCode() {
        return LangUtils.hashCode(17, (Object) this.username);
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (!(o instanceof BasicUserPrincipal) || !LangUtils.equals((Object) this.username, (Object) ((BasicUserPrincipal) o).username)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return "[principal: " + this.username + "]";
    }
}
