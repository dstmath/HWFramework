package org.apache.http.auth;

import java.security.Principal;
import org.apache.http.util.LangUtils;

@Deprecated
public class UsernamePasswordCredentials implements Credentials {
    private final String password;
    private final BasicUserPrincipal principal;

    public UsernamePasswordCredentials(String usernamePassword) {
        if (usernamePassword != null) {
            int atColon = usernamePassword.indexOf(58);
            if (atColon >= 0) {
                this.principal = new BasicUserPrincipal(usernamePassword.substring(0, atColon));
                this.password = usernamePassword.substring(atColon + 1);
                return;
            }
            this.principal = new BasicUserPrincipal(usernamePassword);
            this.password = null;
            return;
        }
        throw new IllegalArgumentException("Username:password string may not be null");
    }

    public UsernamePasswordCredentials(String userName, String password2) {
        if (userName != null) {
            this.principal = new BasicUserPrincipal(userName);
            this.password = password2;
            return;
        }
        throw new IllegalArgumentException("Username may not be null");
    }

    @Override // org.apache.http.auth.Credentials
    public Principal getUserPrincipal() {
        return this.principal;
    }

    public String getUserName() {
        return this.principal.getName();
    }

    @Override // org.apache.http.auth.Credentials
    public String getPassword() {
        return this.password;
    }

    public int hashCode() {
        return this.principal.hashCode();
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (!(o instanceof UsernamePasswordCredentials) || !LangUtils.equals(this.principal, ((UsernamePasswordCredentials) o).principal)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return this.principal.toString();
    }
}
