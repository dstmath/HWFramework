package org.apache.http.client.methods;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.client.utils.CloneUtils;
import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.conn.ConnectionReleaseTrigger;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicRequestLine;
import org.apache.http.message.HeaderGroup;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

@Deprecated
public abstract class HttpRequestBase extends AbstractHttpMessage implements HttpUriRequest, AbortableHttpRequest, Cloneable {
    private Lock abortLock = new ReentrantLock();
    private boolean aborted;
    private ClientConnectionRequest connRequest;
    private ConnectionReleaseTrigger releaseTrigger;
    private URI uri;

    public abstract String getMethod();

    public ProtocolVersion getProtocolVersion() {
        return HttpProtocolParams.getVersion(getParams());
    }

    public URI getURI() {
        return this.uri;
    }

    public RequestLine getRequestLine() {
        String method = getMethod();
        ProtocolVersion ver = getProtocolVersion();
        URI uri2 = getURI();
        String uritext = null;
        if (uri2 != null) {
            uritext = uri2.toASCIIString();
        }
        if (uritext == null || uritext.length() == 0) {
            uritext = "/";
        }
        return new BasicRequestLine(method, uritext, ver);
    }

    public void setURI(URI uri2) {
        this.uri = uri2;
    }

    public void setConnectionRequest(ClientConnectionRequest connRequest2) throws IOException {
        this.abortLock.lock();
        try {
            if (!this.aborted) {
                this.releaseTrigger = null;
                this.connRequest = connRequest2;
                return;
            }
            throw new IOException("Request already aborted");
        } finally {
            this.abortLock.unlock();
        }
    }

    public void setReleaseTrigger(ConnectionReleaseTrigger releaseTrigger2) throws IOException {
        this.abortLock.lock();
        try {
            if (!this.aborted) {
                this.connRequest = null;
                this.releaseTrigger = releaseTrigger2;
                return;
            }
            throw new IOException("Request already aborted");
        } finally {
            this.abortLock.unlock();
        }
    }

    public void abort() {
        this.abortLock.lock();
        try {
            if (!this.aborted) {
                this.aborted = true;
                ClientConnectionRequest localRequest = this.connRequest;
                ConnectionReleaseTrigger localTrigger = this.releaseTrigger;
                this.abortLock.unlock();
                if (localRequest != null) {
                    localRequest.abortRequest();
                }
                if (localTrigger != null) {
                    try {
                        localTrigger.abortConnection();
                    } catch (IOException e) {
                    }
                }
            }
        } finally {
            this.abortLock.unlock();
        }
    }

    public boolean isAborted() {
        return this.aborted;
    }

    public Object clone() throws CloneNotSupportedException {
        HttpRequestBase clone = (HttpRequestBase) super.clone();
        clone.abortLock = new ReentrantLock();
        clone.aborted = false;
        clone.releaseTrigger = null;
        clone.connRequest = null;
        clone.headergroup = (HeaderGroup) CloneUtils.clone(this.headergroup);
        clone.params = (HttpParams) CloneUtils.clone(this.params);
        return clone;
    }
}
