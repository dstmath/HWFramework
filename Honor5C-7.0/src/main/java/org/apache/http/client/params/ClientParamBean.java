package org.apache.http.client.params;

import java.util.Collection;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.conn.ClientConnectionManagerFactory;
import org.apache.http.params.HttpAbstractParamBean;
import org.apache.http.params.HttpParams;

@Deprecated
public class ClientParamBean extends HttpAbstractParamBean {
    public ClientParamBean(HttpParams params) {
        super(params);
    }

    public void setConnectionManagerFactoryClassName(String factory) {
        this.params.setParameter(ClientPNames.CONNECTION_MANAGER_FACTORY_CLASS_NAME, factory);
    }

    public void setConnectionManagerFactory(ClientConnectionManagerFactory factory) {
        this.params.setParameter(ClientPNames.CONNECTION_MANAGER_FACTORY, factory);
    }

    public void setHandleRedirects(boolean handle) {
        this.params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, handle);
    }

    public void setRejectRelativeRedirect(boolean reject) {
        this.params.setBooleanParameter(ClientPNames.REJECT_RELATIVE_REDIRECT, reject);
    }

    public void setMaxRedirects(int maxRedirects) {
        this.params.setIntParameter(ClientPNames.MAX_REDIRECTS, maxRedirects);
    }

    public void setAllowCircularRedirects(boolean allow) {
        this.params.setBooleanParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, allow);
    }

    public void setHandleAuthentication(boolean handle) {
        this.params.setBooleanParameter(ClientPNames.HANDLE_AUTHENTICATION, handle);
    }

    public void setCookiePolicy(String policy) {
        this.params.setParameter(ClientPNames.COOKIE_POLICY, policy);
    }

    public void setVirtualHost(HttpHost host) {
        this.params.setParameter(ClientPNames.VIRTUAL_HOST, host);
    }

    public void setDefaultHeaders(Collection<Header> headers) {
        this.params.setParameter(ClientPNames.DEFAULT_HEADERS, headers);
    }

    public void setDefaultHost(HttpHost host) {
        this.params.setParameter(ClientPNames.DEFAULT_HOST, host);
    }
}
