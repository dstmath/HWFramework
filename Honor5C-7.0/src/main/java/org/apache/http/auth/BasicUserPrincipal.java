package org.apache.http.auth;

import java.security.Principal;
import org.apache.http.util.LangUtils;

@Deprecated
public final class BasicUserPrincipal implements Principal {
    private final String username;

    public BasicUserPrincipal(String username) {
        if (username == null) {
            throw new IllegalArgumentException("User name may not be null");
        }
        this.username = username;
    }

    public String getName() {
        return this.username;
    }

    public int hashCode() {
        return LangUtils.hashCode(17, this.username);
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (o instanceof BasicUserPrincipal) {
            if (LangUtils.equals(this.username, ((BasicUserPrincipal) o).username)) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("[principal: ");
        buffer.append(this.username);
        buffer.append("]");
        return buffer.toString();
    }
}
