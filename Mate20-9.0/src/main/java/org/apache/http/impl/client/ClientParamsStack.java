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
        this.log = LogFactory.getLog((Class) getClass());
        this.applicationParams = aparams;
        this.clientParams = cparams;
        this.requestParams = rparams;
        this.overrideParams = oparams;
    }

    public ClientParamsStack(ClientParamsStack stack) {
        this(stack.getApplicationParams(), stack.getClientParams(), stack.getRequestParams(), stack.getOverrideParams());
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    public ClientParamsStack(ClientParamsStack stack, HttpParams aparams, HttpParams cparams, HttpParams rparams, HttpParams oparams) {
        this(r0, r1, r2, r3);
        HttpParams httpParams;
        HttpParams httpParams2;
        HttpParams httpParams3;
        HttpParams httpParams4;
        if (aparams != null) {
            httpParams = aparams;
        } else {
            httpParams = stack.getApplicationParams();
        }
        if (cparams != null) {
            httpParams2 = cparams;
        } else {
            httpParams2 = stack.getClientParams();
        }
        if (rparams != null) {
            httpParams3 = rparams;
        } else {
            httpParams3 = stack.getRequestParams();
        }
        if (oparams != null) {
            httpParams4 = oparams;
        } else {
            httpParams4 = stack.getOverrideParams();
        }
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

    public Object getParameter(String name) {
        if (name != null) {
            Object result = null;
            if (this.overrideParams != null) {
                result = this.overrideParams.getParameter(name);
            }
            if (result == null && this.requestParams != null) {
                result = this.requestParams.getParameter(name);
            }
            if (result == null && this.clientParams != null) {
                result = this.clientParams.getParameter(name);
            }
            if (result == null && this.applicationParams != null) {
                result = this.applicationParams.getParameter(name);
            }
            if (this.log.isDebugEnabled()) {
                Log log2 = this.log;
                log2.debug("'" + name + "': " + result);
            }
            return result;
        }
        throw new IllegalArgumentException("Parameter name must not be null.");
    }

    public HttpParams setParameter(String name, Object value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Setting parameters in a stack is not supported.");
    }

    public boolean removeParameter(String name) {
        throw new UnsupportedOperationException("Removing parameters in a stack is not supported.");
    }

    public HttpParams copy() {
        return this;
    }
}
