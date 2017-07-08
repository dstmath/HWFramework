package jcifs.http;

import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

class NtlmHttpServletRequest extends HttpServletRequestWrapper {
    Principal principal;

    NtlmHttpServletRequest(HttpServletRequest req, Principal principal) {
        super(req);
        this.principal = principal;
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
