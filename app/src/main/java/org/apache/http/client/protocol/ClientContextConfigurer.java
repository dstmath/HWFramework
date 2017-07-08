package org.apache.http.client.protocol;

import java.util.List;
import org.apache.http.auth.AuthSchemeRegistry;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.cookie.CookieSpecRegistry;
import org.apache.http.protocol.HttpContext;

@Deprecated
public class ClientContextConfigurer implements ClientContext {
    private final HttpContext context;

    public ClientContextConfigurer(HttpContext context) {
        if (context == null) {
            throw new IllegalArgumentException("HTTP context may not be null");
        }
        this.context = context;
    }

    public void setCookieSpecRegistry(CookieSpecRegistry registry) {
        this.context.setAttribute(ClientContext.COOKIESPEC_REGISTRY, registry);
    }

    public void setAuthSchemeRegistry(AuthSchemeRegistry registry) {
        this.context.setAttribute(ClientContext.AUTHSCHEME_REGISTRY, registry);
    }

    public void setCookieStore(CookieStore store) {
        this.context.setAttribute(ClientContext.COOKIE_STORE, store);
    }

    public void setCredentialsProvider(CredentialsProvider provider) {
        this.context.setAttribute(ClientContext.CREDS_PROVIDER, provider);
    }

    public void setAuthSchemePref(List<String> list) {
        this.context.setAttribute(ClientContext.AUTH_SCHEME_PREF, list);
    }
}
