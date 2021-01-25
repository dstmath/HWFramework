package jcifs.http;

import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

class NtlmHttpServletRequest extends HttpServletRequestWrapper {
    Principal principal;

    NtlmHttpServletRequest(HttpServletRequest req, Principal principal2) {
        super(req);
        this.principal = principal2;
    }

    public String getRemoteUser() {
        return this.principal.getName();
    }

    public Principal getUserPrincipal() {
        return this.principal;
    }

    public String getAuthType() {
        return "NTLM";
    }
}
