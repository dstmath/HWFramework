package org.apache.http.message;

import java.util.Iterator;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpMessage;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

@Deprecated
public abstract class AbstractHttpMessage implements HttpMessage {
    protected HeaderGroup headergroup;
    protected HttpParams params;

    protected AbstractHttpMessage(HttpParams params2) {
        this.headergroup = new HeaderGroup();
        this.params = params2;
    }

    protected AbstractHttpMessage() {
        this(null);
    }

    @Override // org.apache.http.HttpMessage
    public boolean containsHeader(String name) {
        return this.headergroup.containsHeader(name);
    }

    @Override // org.apache.http.HttpMessage
    public Header[] getHeaders(String name) {
        return this.headergroup.getHeaders(name);
    }

    @Override // org.apache.http.HttpMessage
    public Header getFirstHeader(String name) {
        return this.headergroup.getFirstHeader(name);
    }

    @Override // org.apache.http.HttpMessage
    public Header getLastHeader(String name) {
        return this.headergroup.getLastHeader(name);
    }

    @Override // org.apache.http.HttpMessage
    public Header[] getAllHeaders() {
        return this.headergroup.getAllHeaders();
    }

    @Override // org.apache.http.HttpMessage
    public void addHeader(Header header) {
        this.headergroup.addHeader(header);
    }

    @Override // org.apache.http.HttpMessage
    public void addHeader(String name, String value) {
        if (name != null) {
            this.headergroup.addHeader(new BasicHeader(name, value));
            return;
        }
        throw new IllegalArgumentException("Header name may not be null");
    }

    @Override // org.apache.http.HttpMessage
    public void setHeader(Header header) {
        this.headergroup.updateHeader(header);
    }

    @Override // org.apache.http.HttpMessage
    public void setHeader(String name, String value) {
        if (name != null) {
            this.headergroup.updateHeader(new BasicHeader(name, value));
            return;
        }
        throw new IllegalArgumentException("Header name may not be null");
    }

    @Override // org.apache.http.HttpMessage
    public void setHeaders(Header[] headers) {
        this.headergroup.setHeaders(headers);
    }

    @Override // org.apache.http.HttpMessage
    public void removeHeader(Header header) {
        this.headergroup.removeHeader(header);
    }

    @Override // org.apache.http.HttpMessage
    public void removeHeaders(String name) {
        if (name != null) {
            Iterator i = this.headergroup.iterator();
            while (i.hasNext()) {
                if (name.equalsIgnoreCase(((Header) i.next()).getName())) {
                    i.remove();
                }
            }
        }
    }

    @Override // org.apache.http.HttpMessage
    public HeaderIterator headerIterator() {
        return this.headergroup.iterator();
    }

    @Override // org.apache.http.HttpMessage
    public HeaderIterator headerIterator(String name) {
        return this.headergroup.iterator(name);
    }

    @Override // org.apache.http.HttpMessage
    public HttpParams getParams() {
        if (this.params == null) {
            this.params = new BasicHttpParams();
        }
        return this.params;
    }

    @Override // org.apache.http.HttpMessage
    public void setParams(HttpParams params2) {
        if (params2 != null) {
            this.params = params2;
            return;
        }
        throw new IllegalArgumentException("HTTP parameters may not be null");
    }
}
