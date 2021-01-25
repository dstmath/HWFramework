package org.apache.http.auth;

import java.security.Principal;

@Deprecated
public interface Credentials {
    String getPassword();

    Principal getUserPrincipal();
}
