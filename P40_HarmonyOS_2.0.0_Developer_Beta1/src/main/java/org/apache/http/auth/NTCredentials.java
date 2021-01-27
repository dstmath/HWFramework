package org.apache.http.auth;

import java.security.Principal;
import java.util.Locale;
import org.apache.http.util.LangUtils;

@Deprecated
public class NTCredentials implements Credentials {
    private final String password;
    private final NTUserPrincipal principal;
    private final String workstation;

    public NTCredentials(String usernamePassword) {
        String username;
        if (usernamePassword != null) {
            int atColon = usernamePassword.indexOf(58);
            if (atColon >= 0) {
                username = usernamePassword.substring(0, atColon);
                this.password = usernamePassword.substring(atColon + 1);
            } else {
                username = usernamePassword;
                this.password = null;
            }
            int atSlash = username.indexOf(47);
            if (atSlash >= 0) {
                this.principal = new NTUserPrincipal(username.substring(0, atSlash).toUpperCase(Locale.ENGLISH), username.substring(atSlash + 1));
            } else {
                this.principal = new NTUserPrincipal(null, username.substring(atSlash + 1));
            }
            this.workstation = null;
            return;
        }
        throw new IllegalArgumentException("Username:password string may not be null");
    }

    public NTCredentials(String userName, String password2, String workstation2, String domain) {
        if (userName != null) {
            this.principal = new NTUserPrincipal(domain, userName);
            this.password = password2;
            if (workstation2 != null) {
                this.workstation = workstation2.toUpperCase(Locale.ENGLISH);
            } else {
                this.workstation = null;
            }
        } else {
            throw new IllegalArgumentException("User name may not be null");
        }
    }

    @Override // org.apache.http.auth.Credentials
    public Principal getUserPrincipal() {
        return this.principal;
    }

    public String getUserName() {
        return this.principal.getUsername();
    }

    @Override // org.apache.http.auth.Credentials
    public String getPassword() {
        return this.password;
    }

    public String getDomain() {
        return this.principal.getDomain();
    }

    public String getWorkstation() {
        return this.workstation;
    }

    public int hashCode() {
        return LangUtils.hashCode(LangUtils.hashCode(17, this.principal), this.workstation);
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (o instanceof NTCredentials) {
            NTCredentials that = (NTCredentials) o;
            if (LangUtils.equals(this.principal, that.principal) && LangUtils.equals(this.workstation, that.workstation)) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return "[principal: " + this.principal + "][workstation: " + this.workstation + "]";
    }
}
