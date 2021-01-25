package org.apache.http.impl.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.params.AbstractHttpParams;
import org.apache.http.params.HttpParams;

@Deprecated
public class ClientParamsStack extends AbstractHttpParams {
    protected final HttpParams applicationParams;
    protected final HttpParams clientParams;
    private final Log log;
    protected final HttpParams overrideParams;
    protected final HttpParams requestParams;

    public ClientParamsStack(HttpParams aparams, HttpParams cparams, HttpParams rparams, HttpParams oparams) {
        this.log = LogFactory.getLog(getClass());
        this.applicationParams = aparams;
        this.clientParams = cparams;
        this.requestParams = rparams;
        this.overrideParams = oparams;
    }

    public ClientParamsStack(ClientParamsStack stack) {
        this(stack.getApplicationParams(), stack.getClientParams(), stack.getRequestParams(), stack.getOverrideParams());
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public ClientParamsStack(ClientParamsStack stack, HttpParams aparams, HttpParams cparams, HttpParams rparams, HttpParams oparams) {
        this(aparams != null ? aparams : stack.getApplicationParams(), cparams != null ? cparams : stack.getClientParams(), rparams != null ? rparams : stack.getRequestParams(), oparams != null ? oparams : stack.getOverrideParams());
    }

    public final HttpParams getApplicationParams() {
        return this.applicationParams;
    }

    public final HttpParams getClientParams() {
        return this.clientParams;
    }

    public final HttpParams getRequestParams() {
        return this.requestParams;
    }

    public final HttpParams getOverrideParams() {
        return this.overrideParams;
    }

    @Override // org.apache.http.params.HttpParams
    public Object getParameter(String name) {
        HttpParams httpParams;
        HttpParams httpParams2;
        HttpParams httpParams3;
        if (name != null) {
            Object result = null;
            HttpParams httpParams4 = this.overrideParams;
            if (httpParams4 != null) {
                result = httpParams4.getParameter(name);
            }
            if (result == null && (httpParams3 = this.requestParams) != null) {
                result = httpParams3.getParameter(name);
            }
            if (result == null && (httpParams2 = this.clientParams) != null) {
                result = httpParams2.getParameter(name);
            }
            if (result == null && (httpParams = this.applicationParams) != null) {
                result = httpParams.getParameter(name);
            }
            if (this.log.isDebugEnabled()) {
                Log log2 = this.log;
                log2.debug("'" + name + "': " + result);
            }
            return result;
        }
        throw new IllegalArgumentException("Parameter name must not be null.");
    }

    @Override // org.apache.http.params.HttpParams
    public HttpParams setParameter(String name, Object value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Setting parameters in a stack is not supported.");
    }

    @Override // org.apache.http.params.HttpParams
    public boolean removeParameter(String name) {
        throw new UnsupportedOperationException("Removing parameters in a stack is not supported.");
    }

    @Override // org.apache.http.params.HttpParams
    public HttpParams copy() {
        return this;
    }
}
