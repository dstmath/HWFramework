package org.apache.http.message;

import java.util.Locale;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.ReasonPhraseCatalog;
import org.apache.http.StatusLine;

@Deprecated
public class BasicHttpResponse extends AbstractHttpMessage implements HttpResponse {
    private HttpEntity entity;
    private Locale locale;
    private ReasonPhraseCatalog reasonCatalog;
    private StatusLine statusline;

    public BasicHttpResponse(StatusLine statusline2, ReasonPhraseCatalog catalog, Locale locale2) {
        if (statusline2 != null) {
            this.statusline = statusline2;
            this.reasonCatalog = catalog;
            this.locale = locale2 != null ? locale2 : Locale.getDefault();
            return;
        }
        throw new IllegalArgumentException("Status line may not be null.");
    }

    public BasicHttpResponse(StatusLine statusline2) {
        this(statusline2, (ReasonPhraseCatalog) null, (Locale) null);
    }

    public BasicHttpResponse(ProtocolVersion ver, int code, String reason) {
        this(new BasicStatusLine(ver, code, reason), (ReasonPhraseCatalog) null, (Locale) null);
    }

    @Override // org.apache.http.HttpMessage
    public ProtocolVersion getProtocolVersion() {
        return this.statusline.getProtocolVersion();
    }

    @Override // org.apache.http.HttpResponse
    public StatusLine getStatusLine() {
        return this.statusline;
    }

    @Override // org.apache.http.HttpResponse
    public HttpEntity getEntity() {
        return this.entity;
    }

    @Override // org.apache.http.HttpResponse
    public Locale getLocale() {
        return this.locale;
    }

    @Override // org.apache.http.HttpResponse
    public void setStatusLine(StatusLine statusline2) {
        if (statusline2 != null) {
            this.statusline = statusline2;
            return;
        }
        throw new IllegalArgumentException("Status line may not be null");
    }

    @Override // org.apache.http.HttpResponse
    public void setStatusLine(ProtocolVersion ver, int code) {
        this.statusline = new BasicStatusLine(ver, code, getReason(code));
    }

    @Override // org.apache.http.HttpResponse
    public void setStatusLine(ProtocolVersion ver, int code, String reason) {
        this.statusline = new BasicStatusLine(ver, code, reason);
    }

    @Override // org.apache.http.HttpResponse
    public void setStatusCode(int code) {
        this.statusline = new BasicStatusLine(this.statusline.getProtocolVersion(), code, getReason(code));
    }

    @Override // org.apache.http.HttpResponse
    public void setReasonPhrase(String reason) {
        if (reason == null || (reason.indexOf(10) < 0 && reason.indexOf(13) < 0)) {
            this.statusline = new BasicStatusLine(this.statusline.getProtocolVersion(), this.statusline.getStatusCode(), reason);
            return;
        }
        throw new IllegalArgumentException("Line break in reason phrase.");
    }

    @Override // org.apache.http.HttpResponse
    public void setEntity(HttpEntity entity2) {
        this.entity = entity2;
    }

    @Override // org.apache.http.HttpResponse
    public void setLocale(Locale loc) {
        if (loc != null) {
            this.locale = loc;
            int code = this.statusline.getStatusCode();
            this.statusline = new BasicStatusLine(this.statusline.getProtocolVersion(), code, getReason(code));
            return;
        }
        throw new IllegalArgumentException("Locale may not be null.");
    }

    /* access modifiers changed from: protected */
    public String getReason(int code) {
        ReasonPhraseCatalog reasonPhraseCatalog = this.reasonCatalog;
        if (reasonPhraseCatalog == null) {
            return null;
        }
        return reasonPhraseCatalog.getReason(code, this.locale);
    }
}
